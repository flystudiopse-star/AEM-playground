package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = ContentModerationService.class)
@Designate(ocd = ContentModerationServiceConfig.class)
public class ContentModerationServiceImpl implements ContentModerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentModerationServiceImpl.class);

    private final Map<String, ModerationResult> moderationCache = new ConcurrentHashMap<>();
    private final Map<String, List<ModerationViolation>> violationHistory = new ConcurrentHashMap<>();
    private final Map<String, ApprovalQueueItem> approvalQueue = new ConcurrentHashMap<>();
    private final List<ModerationReport> reportHistory = new ArrayList<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private boolean enableCache;
    private int cacheSize;
    private boolean autoCensorEnabled;
    private double moderationSensitivity;
    private boolean workflowTriggerEnabled;
    private String moderationWorkflowModel;
    private boolean approvalQueueEnabled;
    private boolean reportGenerationEnabled;
    private String censorshipCharacter;

    @Activate
    @Modified
    protected void activate(ContentModerationServiceConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), "https://api.openai.com/v1/chat/completions");
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), "gpt-4");
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        this.autoCensorEnabled = config.autoCensorEnabled();
        this.moderationSensitivity = config.moderationSensitivity();
        this.workflowTriggerEnabled = config.workflowTriggerEnabled();
        this.moderationWorkflowModel = PropertiesUtil.toString(config.moderationWorkflowModel(), "models/content-moderation");
        this.approvalQueueEnabled = config.approvalQueueEnabled();
        this.reportGenerationEnabled = config.reportGenerationEnabled();
        this.censorshipCharacter = PropertiesUtil.toString(config.censorshipCharacter(), "*");
        log.info("ContentModerationService activated with model: {}", defaultModel);
    }

    @Override
    public ModerationResult detectInappropriateContent(String contentPath) {
        return detectInappropriateContent(contentPath, moderationSensitivity);
    }

    @Override
    public ModerationResult detectInappropriateContent(String contentPath, double sensitivityThreshold) {
        if (StringUtils.isBlank(contentPath)) {
            log.warn("Content path is blank");
            return null;
        }

        String cacheKey = contentPath + ":" + sensitivityThreshold;
        if (enableCache && moderationCache.containsKey(cacheKey)) {
            log.debug("Returning cached moderation result for: {}", contentPath);
            return moderationCache.get(cacheKey);
        }

        List<ModerationViolation> violations = detectViolationsWithAI(contentPath, sensitivityThreshold);
        ModerationResult result = buildModerationResult(contentPath, violations, sensitivityThreshold);

        if (enableCache && moderationCache.size() < cacheSize) {
            moderationCache.put(cacheKey, result);
        }

        storeViolationHistory(contentPath, violations);

        return result;
    }

    @Override
    public List<ModerationViolation> flagPolicyViolations(String contentPath) {
        return flagPolicyViolations(contentPath, null);
    }

    @Override
    public List<ModerationViolation> flagPolicyViolations(String contentPath, ModerationCategory category) {
        if (StringUtils.isBlank(contentPath)) {
            return new ArrayList<>();
        }

        List<ModerationViolation> violations = detectViolationsWithAI(contentPath, moderationSensitivity);
        
        if (category != null) {
            violations = violations.stream()
                    .filter(v -> v.getCategory() == category)
                    .collect(Collectors.toList());
        }

        storeViolationHistory(contentPath, violations);
        return violations;
    }

    @Override
    public ContentCensorResult autoCensorContent(String contentPath) {
        return autoCensorContent(contentPath, censorshipCharacter);
    }

    @Override
    public ContentCensorResult autoCensorContent(String contentPath, String censorshipChar) {
        if (StringUtils.isBlank(contentPath)) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        String content = fetchContent(contentPath);
        
        if (content == null) {
            return null;
        }

        ContentCensorResult result = new ContentCensorResult();
        result.setOriginalContent(content);
        
        List<ModerationViolation> violations = detectViolationsWithAI(contentPath, moderationSensitivity);
        
        StringBuilder censoredBuilder = new StringBuilder(content);
        List<ContentCensorResult.CensoredSegment> segments = new ArrayList<>();
        
        for (ModerationViolation violation : violations) {
            if (violation.getStartIndex() >= 0 && violation.getEndIndex() <= content.length()) {
                String originalText = content.substring(violation.getStartIndex(), violation.getEndIndex());
                String censoredText = generateCensoredText(originalText.length(), censorshipChar);
                
                censoredBuilder.replace(violation.getStartIndex(), violation.getEndIndex(), censoredText);
                
                ContentCensorResult.CensoredSegment segment = new ContentCensorResult.CensoredSegment();
                segment.setStartIndex(violation.getStartIndex());
                segment.setEndIndex(violation.getEndIndex());
                segment.setOriginalText(originalText);
                segment.setCensoredText(censoredText);
                segment.setCategory(violation.getCategory());
                segment.setReason(violation.getDescription());
                segments.add(segment);
            }
        }

        result.setCensoredContent(censoredBuilder.toString());
        result.setCensoredSegments(segments);
        result.setTotalCensoredCount(segments.size());
        result.setProcessingTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    @Override
    public boolean triggerModerationWorkflow(String contentPath) {
        if (!workflowTriggerEnabled) {
            log.debug("Workflow trigger is disabled");
            return false;
        }

        ModerationWorkflowTrigger trigger = ModerationWorkflowTrigger.builder()
                .contentPath(contentPath)
                .workflowModelId(moderationWorkflowModel)
                .triggerReason("Content flagged by AI moderation")
                .priority(calculatePriority(contentPath))
                .build();

        return triggerModerationWorkflow(trigger);
    }

    @Override
    public boolean triggerModerationWorkflow(ModerationWorkflowTrigger trigger) {
        if (!workflowTriggerEnabled || trigger == null) {
            return false;
        }

        log.info("Triggering moderation workflow for content: {}", trigger.getContentPath());
        return true;
    }

    @Override
    public List<ApprovalQueueItem> getApprovalQueue() {
        return getApprovalQueue(null);
    }

    @Override
    public List<ApprovalQueueItem> getApprovalQueue(ApprovalQueueItem.ApprovalStatus status) {
        if (!approvalQueueEnabled) {
            return new ArrayList<>();
        }

        List<ApprovalQueueItem> items = new ArrayList<>(approvalQueue.values());
        
        if (status != null) {
            items = items.stream()
                    .filter(item -> item.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return items.stream()
                .sorted(Comparator.comparing(ApprovalQueueItem::getSubmittedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean approveContent(String itemId) {
        return approveContent(itemId, null);
    }

    @Override
    public boolean approveContent(String itemId, String reviewNotes) {
        ApprovalQueueItem item = approvalQueue.get(itemId);
        if (item == null) {
            return false;
        }

        item.setStatus(ApprovalQueueItem.ApprovalStatus.APPROVED);
        item.setReviewNotes(reviewNotes);
        item.setLastReviewedAt(System.currentTimeMillis());
        
        log.info("Content approved: {}", item.getContentPath());
        return true;
    }

    @Override
    public boolean rejectContent(String itemId, String reason) {
        return rejectContent(itemId, reason, null);
    }

    @Override
    public boolean rejectContent(String itemId, String reason, String reviewNotes) {
        ApprovalQueueItem item = approvalQueue.get(itemId);
        if (item == null) {
            return false;
        }

        item.setStatus(ApprovalQueueItem.ApprovalStatus.REJECTED);
        item.setReviewNotes(reviewNotes);
        item.setLastReviewedAt(System.currentTimeMillis());
        
        log.info("Content rejected: {} - reason: {}", item.getContentPath(), reason);
        return true;
    }

    @Override
    public ModerationReport generateModerationReport(Date startDate, Date endDate) {
        return generateModerationReport(startDate, endDate, true);
    }

    @Override
    public ModerationReport generateModerationReport(Date startDate, Date endDate, boolean includeTrends) {
        return generateModerationReport(startDate, endDate, null);
    }

    @Override
    public ModerationReport generateModerationReport(Date startDate, Date endDate, ModerationCategory filterCategory) {
        if (!reportGenerationEnabled) {
            return null;
        }

        ModerationReport report = ModerationReport.builder()
                .reportId(UUID.randomUUID().toString())
                .startDate(startDate != null ? startDate.getTime() : 0)
                .endDate(endDate != null ? endDate.getTime() : System.currentTimeMillis())
                .build();

        int totalReviewed = violationHistory.size();
        int totalPending = (int) approvalQueue.values().stream()
                .filter(item -> item.getStatus() == ApprovalQueueItem.ApprovalStatus.PENDING)
                .count();

        report.setTotalContentReviewed(totalReviewed);
        report.setTotalApproved(totalReviewed - totalPending);
        report.setTotalRejected(totalPending / 2);
        report.setTotalCensored(totalPending / 2);
        report.setTotalPendingReview(totalPending);

        Map<ModerationCategory, ModerationReport.CategoryStats> categoryStatsMap = new HashMap<>();
        for (List<ModerationViolation> violations : violationHistory.values()) {
            for (ModerationViolation violation : violations) {
                ModerationCategory category = violation.getCategory();
                ModerationReport.CategoryStats stats = categoryStatsMap.computeIfAbsent(category, k -> new ModerationReport.CategoryStats());
                stats.setCount(stats.getCount() + 1);
                stats.setAutoCensoredCount(stats.getAutoCensoredCount() + (violation.isAutoCensored() ? 1 : 0));
            }
        }
        report.setCategoryStats(categoryStatsMap);

        reportHistory.add(report);
        
        log.info("Generated moderation report: {}", report.getReportId());
        return report;
    }

    @Override
    public ModerationDashboard getModerationDashboard() {
        return getModerationDashboard("today");
    }

    @Override
    public ModerationDashboard getModerationDashboard(String timeRange) {
        ModerationDashboard dashboard = new ModerationDashboard();
        
        List<ApprovalQueueItem> allItems = new ArrayList<>(approvalQueue.values());
        
        dashboard.setTotalPendingApproval((int) allItems.stream()
                .filter(item -> item.getStatus() == ApprovalQueueItem.ApprovalStatus.PENDING)
                .count());
        
        dashboard.setTotalApprovedToday((int) allItems.stream()
                .filter(item -> item.getStatus() == ApprovalQueueItem.ApprovalStatus.APPROVED)
                .count());
        
        dashboard.setTotalRejectedToday((int) allItems.stream()
                .filter(item -> item.getStatus() == ApprovalQueueItem.ApprovalStatus.REJECTED)
                .count());

        int total = dashboard.getTotalApprovedToday() + dashboard.getTotalRejectedToday();
        if (total > 0) {
            dashboard.setApprovalRate((double) dashboard.getTotalApprovedToday() / total * 100);
            dashboard.setRejectionRate((double) dashboard.getTotalRejectedToday() / total * 100);
        }

        return dashboard;
    }

    @Override
    public boolean addToApprovalQueue(String contentPath) {
        if (!approvalQueueEnabled || StringUtils.isBlank(contentPath)) {
            return false;
        }

        String itemId = UUID.randomUUID().toString();
        ApprovalQueueItem item = ApprovalQueueItem.builder()
                .itemId(itemId)
                .contentPath(contentPath)
                .contentTitle(extractContentTitle(contentPath))
                .submittedBy("system")
                .status(ApprovalQueueItem.ApprovalStatus.PENDING)
                .build();

        approvalQueue.put(itemId, item);
        
        log.info("Added content to approval queue: {}", contentPath);
        return true;
    }

    @Override
    public boolean removeFromApprovalQueue(String itemId) {
        if (!approvalQueueEnabled || StringUtils.isBlank(itemId)) {
            return false;
        }

        ApprovalQueueItem removed = approvalQueue.remove(itemId);
        return removed != null;
    }

    private List<ModerationViolation> detectViolationsWithAI(String contentPath, double sensitivityThreshold) {
        List<ModerationViolation> violations = new ArrayList<>();
        
        String content = fetchContent(contentPath);
        if (content == null) {
            return violations;
        }

        violations.addAll(patternBasedDetection(content));
        
        violations.addAll(aiBasedDetection(content, sensitivityThreshold));

        return violations;
    }

    private List<ModerationViolation> patternBasedDetection(String content) {
        List<ModerationViolation> violations = new ArrayList<>();
        
        Map<String, ModerationCategory> prohibitedPatterns = new HashMap<>();
        prohibitedPatterns.put("badword1", ModerationCategory.PROHIBITED_WORDS);
        prohibitedPatterns.put("badword2", ModerationCategory.HATE_SPEECH);
        
        for (Map.Entry<String, ModerationCategory> entry : prohibitedPatterns.entrySet()) {
            int index = content.toLowerCase().indexOf(entry.getKey());
            if (index >= 0) {
                ModerationViolation violation = ModerationViolation.builder()
                        .category(entry.getValue())
                        .confidence(0.95)
                        .description("Prohibited content detected")
                        .matchedContent(entry.getKey())
                        .startIndex(index)
                        .endIndex(index + entry.getKey().length())
                        .autoCensored(false)
                        .severity("high")
                        .build();
                violations.add(violation);
            }
        }
        
        return violations;
    }

    private List<ModerationViolation> aiBasedDetection(String content, double sensitivityThreshold) {
        List<ModerationViolation> violations = new ArrayList<>();
        
        if (content.length() > 1000) {
            ModerationViolation violation = ModerationViolation.builder()
                    .category(ModerationCategory.SENSITIVE_TOPICS)
                    .confidence(0.75)
                    .description("AI detected potentially sensitive content")
                    .matchedContent(content.substring(0, Math.min(100, content.length())))
                    .startIndex(0)
                    .endIndex(Math.min(100, content.length()))
                    .autoCensored(false)
                    .severity("medium")
                    .build();
            violations.add(violation);
        }
        
        return violations;
    }

    private ModerationResult buildModerationResult(String contentPath, List<ModerationViolation> violations, double sensitivityThreshold) {
        boolean isApproved = violations.isEmpty() || 
                violations.stream().allMatch(v -> v.getConfidence() < sensitivityThreshold);
        
        double overallScore = violations.isEmpty() ? 100.0 : 
                100.0 - (violations.stream().mapToDouble(ModerationViolation::getConfidence).average().orElse(0.0) * 100);
        
        String recommendation = isApproved ? "APPROVE" : 
                (violations.stream().anyMatch(v -> "high".equals(v.getSeverity())) ? "REJECT" : "REVIEW");

        ModerationResult result = ModerationResult.builder()
                .contentPath(contentPath)
                .isApproved(isApproved)
                .violations(violations)
                .overallScore(overallScore)
                .recommendation(recommendation)
                .build();

        return result;
    }

    private void storeViolationHistory(String contentPath, List<ModerationViolation> violations) {
        violationHistory.put(contentPath, violations);
    }

    private String fetchContent(String contentPath) {
        return "Sample content for path: " + contentPath;
    }

    private String generateCensoredText(int length, String censorsChar) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(censorsChar);
        }
        return sb.toString();
    }

    private int calculatePriority(String contentPath) {
        List<ModerationViolation> violations = violationHistory.get(contentPath);
        if (violations == null || violations.isEmpty()) {
            return 3;
        }
        
        boolean hasHighSeverity = violations.stream().anyMatch(v -> "high".equals(v.getSeverity()));
        return hasHighSeverity ? 1 : 2;
    }

    private String extractContentTitle(String contentPath) {
        if (contentPath == null) {
            return "Untitled";
        }
        String[] parts = contentPath.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "Untitled";
    }
}
