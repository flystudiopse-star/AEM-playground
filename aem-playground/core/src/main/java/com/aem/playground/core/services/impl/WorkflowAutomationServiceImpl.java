package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.*;
import com.aem.playground.core.services.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component(service = WorkflowAutomationService.class)
@Designate(ocd = WorkflowAutomationServiceConfig.class)
public class WorkflowAutomationServiceImpl implements WorkflowAutomationService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowAutomationServiceImpl.class);

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";

    private static final String SYSTEM_PROMPT = "You are an AI workflow automation expert for Adobe Experience Manager (AEM). " +
            "Analyze content changes and suggest appropriate workflow actions based on complexity and risk.";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, ContentAnalysisResult> analysisCache = new ConcurrentHashMap<>();
    private final Map<String, WorkflowPerformanceMetrics> performanceMetricsMap = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<WorkflowPerformanceMetrics> metricsHistory = new ConcurrentLinkedQueue<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private boolean enableCache;
    private int cacheSize;
    private double simpleChangeThreshold;
    private boolean autoApproveEnabled;
    private String defaultApprovalWorkflow;
    private String defaultReviewWorkflow;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(WorkflowAutomationServiceConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), DEFAULT_API_URL);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        this.simpleChangeThreshold = config.simpleChangeThreshold();
        this.autoApproveEnabled = config.autoApproveEnabled();
        this.defaultApprovalWorkflow = config.defaultApprovalWorkflow();
        this.defaultReviewWorkflow = config.defaultReviewWorkflow();
        log.info("WorkflowAutomationService activated with URL: {}", serviceUrl);
    }

    @Override
    public List<WorkflowAction> suggestWorkflowActions(String contentPath) {
        if (StringUtils.isBlank(contentPath)) {
            return Collections.emptyList();
        }

        try {
            ContentAnalysisResult analysis = analyzeContentForWorkflow(contentPath);
            return generateWorkflowActions(analysis);
        } catch (Exception e) {
            log.error("Error suggesting workflow actions: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ContentAnalysisResult analyzeContentForWorkflow(String contentPath) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorAnalysis("Content path is required");
        }

        try {
            if (enableCache) {
                ContentAnalysisResult cached = analysisCache.get(contentPath);
                if (cached != null) {
                    log.debug("Cache hit for content analysis: {}", contentPath);
                    return cached;
                }
            }

            ContentAnalysisResult analysis = performContentAnalysis(contentPath);

            if (enableCache) {
                analysisCache.put(contentPath, analysis);
                evictOldCacheEntries();
            }

            return analysis;
        } catch (Exception e) {
            log.error("Error analyzing content: {}", e.getMessage());
            return createErrorAnalysis(e.getMessage());
        }
    }

    @Override
    public boolean canAutoApprove(String contentPath) {
        if (!autoApproveEnabled) {
            return false;
        }

        ContentAnalysisResult analysis = analyzeContentForWorkflow(contentPath);
        return analysis != null && analysis.isSimpleChange() && 
               analysis.getComplexityScore() <= simpleChangeThreshold;
    }

    @Override
    public boolean autoApproveContent(String contentPath) {
        if (!canAutoApprove(contentPath)) {
            log.warn("Cannot auto-approve content: {}", contentPath);
            return false;
        }

        try {
            String workflowId = launchWorkflow(defaultApprovalWorkflow, contentPath);
            log.info("Auto-approved content {} with workflow {}", contentPath, workflowId);
            return StringUtils.isNotBlank(workflowId);
        } catch (Exception e) {
            log.error("Error auto-approving content: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public WorkflowRouteDecision routeContentForReview(String contentPath) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorRouteDecision("Content path is required");
        }

        try {
            ContentAnalysisResult analysis = analyzeContentForWorkflow(contentPath);
            return determineRouting(analysis);
        } catch (Exception e) {
            log.error("Error routing content: {}", e.getMessage());
            return createErrorRouteDecision(e.getMessage());
        }
    }

    @Override
    public List<ContentIssue> detectContentIssues(String contentPath) {
        if (StringUtils.isBlank(contentPath)) {
            return Collections.emptyList();
        }

        try {
            ContentAnalysisResult analysis = analyzeContentForWorkflow(contentPath);
            return identifyContentIssues(analysis);
        } catch (Exception e) {
            log.error("Error detecting content issues: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean triggerCorrectionWorkflow(ContentIssue issue) {
        if (issue == null || !issue.isAutoCorrectable()) {
            return false;
        }

        try {
            String workflowId = launchWorkflow("models/content-correction", issue.getContentPath());
            log.info("Triggered correction workflow {} for issue {}", workflowId, issue.getIssueId());
            return StringUtils.isNotBlank(workflowId);
        } catch (Exception e) {
            log.error("Error triggering correction workflow: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<WorkflowPerformanceMetrics> getWorkflowPerformanceAnalytics(String workflowId) {
        if (StringUtils.isBlank(workflowId)) {
            List<WorkflowPerformanceMetrics> allMetrics = new ArrayList<>();
            metricsHistory.forEach(allMetrics::add);
            return allMetrics;
        }

        List<WorkflowPerformanceMetrics> result = new ArrayList<>();
        for (WorkflowPerformanceMetrics metrics : metricsHistory) {
            if (workflowId.equals(metrics.getWorkflowId())) {
                result.add(metrics);
            }
        }
        return result;
    }

    @Override
    public List<WorkflowPerformanceMetrics> getWorkflowPerformanceAnalytics(String workflowId, long startDate, long endDate) {
        List<WorkflowPerformanceMetrics> allMetrics = getWorkflowPerformanceAnalytics(workflowId);
        List<WorkflowPerformanceMetrics> filtered = new ArrayList<>();
        for (WorkflowPerformanceMetrics metrics : allMetrics) {
            if (metrics.getStartTime() >= startDate && metrics.getStartTime() <= endDate) {
                filtered.add(metrics);
            }
        }
        return filtered;
    }

    @Override
    public WorkflowPerformanceMetrics getAggregatePerformanceMetrics() {
        if (metricsHistory.isEmpty()) {
            return null;
        }

        long totalDuration = 0;
        int totalSteps = 0;
        int completedSteps = 0;
        int approvals = 0;
        int rejections = 0;

        for (WorkflowPerformanceMetrics metrics : metricsHistory) {
            totalDuration += metrics.getDuration();
            totalSteps += metrics.getTotalSteps();
            completedSteps += metrics.getStepsCompleted();
            approvals += metrics.getApprovalCount();
            rejections += metrics.getRejectionCount();
        }

        int count = metricsHistory.size();
        WorkflowPerformanceMetrics aggregate = new WorkflowPerformanceMetrics();
        aggregate.setWorkflowId("aggregate");
        aggregate.setWorkflowName("All Workflows");
        aggregate.setDuration(totalDuration / count);
        aggregate.setTotalSteps(totalSteps / count);
        aggregate.setStepsCompleted(completedSteps / count);
        aggregate.setApprovalCount(approvals);
        aggregate.setRejectionCount(rejections);
        aggregate.setCompletionRate(count > 0 ? (double) completedSteps / totalSteps : 0.0);
        aggregate.setAverageStepTime(totalDuration / (totalSteps > 0 ? totalSteps : 1));

        return aggregate;
    }

    @Override
    public String launchWorkflow(String workflowModelId, String contentPath) {
        if (StringUtils.isBlank(workflowModelId) || StringUtils.isBlank(contentPath)) {
            return null;
        }

        String workflowId = "wf-" + System.currentTimeMillis();
        log.info("Launching workflow {} with model {} for content {}", workflowId, workflowModelId, contentPath);

        WorkflowPerformanceMetrics metrics = new WorkflowPerformanceMetrics();
        metrics.setWorkflowId(workflowId);
        metrics.setWorkflowName(workflowModelId);
        metrics.setStartTime(System.currentTimeMillis());
        metrics.setStatus("running");
        metrics.setStepsCompleted(0);
        metrics.setTotalSteps(5);

        performanceMetricsMap.put(workflowId, metrics);
        metricsHistory.add(metrics);

        return workflowId;
    }

    private ContentAnalysisResult performContentAnalysis(String contentPath) {
        try {
            String prompt = buildAnalysisPrompt(contentPath);
            AIGenerationOptions options = AIGenerationOptions.builder()
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();

            AIGenerationResult aiResult = aiService.generateText(prompt, options);

            ContentAnalysisResult analysis = new ContentAnalysisResult();
            analysis.setContentPath(contentPath);
            analysis.setAnalysisId("analysis-" + System.currentTimeMillis());
            analysis.setAnalysisTimestamp(System.currentTimeMillis());

            if (aiResult.isSuccess()) {
                return parseAIAnalysisResponse(analysis, aiResult.getContent());
            } else {
                return createDefaultAnalysis(analysis);
            }
        } catch (Exception e) {
            log.warn("Error calling AI service, using default analysis: {}", e.getMessage());
            ContentAnalysisResult analysis = new ContentAnalysisResult();
            analysis.setContentPath(contentPath);
            analysis.setAnalysisId("analysis-" + System.currentTimeMillis());
            return createDefaultAnalysis(analysis);
        }
    }

    private ContentAnalysisResult parseAIAnalysisResponse(ContentAnalysisResult analysis, String aiResponse) {
        analysis.setComplexityScore(determineComplexity(aiResponse));
        analysis.setSimpleChange(analysis.getComplexityScore() < simpleChangeThreshold);
        analysis.setRiskScore(determineRisk(aiResponse));
        analysis.setRiskLevel(determineRiskLevel(analysis.getRiskScore()));
        analysis.setDetectedChanges(extractChanges(aiResponse));
        analysis.setAffectedComponents(extractComponents(aiResponse));
        analysis.setContentType(determineContentType(aiResponse));

        Map<String, String> recommendations = new HashMap<>();
        recommendations.put("action", determineRecommendedAction(analysis));
        analysis.setRecommendations(recommendations);

        return analysis;
    }

    private ContentAnalysisResult createDefaultAnalysis(ContentAnalysisResult analysis) {
        analysis.setComplexityScore(0.5);
        analysis.setSimpleChange(false);
        analysis.setRiskScore(0.5);
        analysis.setRiskLevel("medium");
        analysis.setDetectedChanges(new ArrayList<>());
        analysis.setAffectedComponents(new ArrayList<>());
        analysis.setContentType("unknown");

        Map<String, String> recommendations = new HashMap<>();
        recommendations.put("action", "review");
        analysis.setRecommendations(recommendations);

        return analysis;
    }

    private ContentAnalysisResult createErrorAnalysis(String error) {
        ContentAnalysisResult analysis = new ContentAnalysisResult();
        analysis.setAnalysisId("error-" + System.currentTimeMillis());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        analysis.setContentMetadata(metadata);

        return analysis;
    }

    private List<WorkflowAction> generateWorkflowActions(ContentAnalysisResult analysis) {
        List<WorkflowAction> actions = new ArrayList<>();

        String recommendedAction = analysis.getRecommendations() != null ? 
                analysis.getRecommendations().get("action") : "review";

        if ("approve".equals(recommendedAction) && analysis.isSimpleChange()) {
            WorkflowAction approveAction = WorkflowAction.builder()
                    .actionId("action-" + System.currentTimeMillis())
                    .actionType("approve")
                    .actionName("Auto-Approve Content")
                    .description("Approve simple content change automatically")
                    .confidenceScore(1.0 - analysis.getComplexityScore())
                    .workflowModelId(defaultApprovalWorkflow)
                    .autoExecute(autoApproveEnabled)
                    .priority(1)
                    .build();
            actions.add(approveAction);
        }

        WorkflowAction reviewAction = WorkflowAction.builder()
                .actionId("action-" + System.currentTimeMillis() + "-review")
                .actionType("review")
                .actionName("Route for Review")
                .description("Route content to appropriate reviewers based on analysis")
                .confidenceScore(analysis.getComplexityScore())
                .workflowModelId(defaultReviewWorkflow)
                .autoExecute(false)
                .priority(2)
                .build();
        actions.add(reviewAction);

        WorkflowAction correctionAction = WorkflowAction.builder()
                .actionId("action-" + System.currentTimeMillis() + "-correction")
                .actionType("correct")
                .actionName("Trigger Corrections")
                .description("Fix detected content issues")
                .confidenceScore(analysis.getRiskScore())
                .workflowModelId("models/content-correction")
                .priority(3)
                .build();
        actions.add(correctionAction);

        return actions;
    }

    private WorkflowRouteDecision determineRouting(ContentAnalysisResult analysis) {
        WorkflowRouteDecision decision = new WorkflowRouteDecision();
        decision.setDecisionId("route-" + System.currentTimeMillis());
        decision.setContentPath(analysis.getContentPath());
        decision.setConfidenceScore(1.0 - analysis.getComplexityScore());

        String riskLevel = analysis.getRiskLevel();
        if ("low".equals(riskLevel)) {
            decision.setTargetWorkflow("models/content-approve");
            decision.setRouteReason("Low risk content - auto-approved");
            decision.setUrgencyLevel("normal");
        } else if ("medium".equals(riskLevel)) {
            decision.setTargetWorkflow("models/content-review");
            decision.setRouteReason("Medium risk content - requires review");
            decision.setUrgencyLevel("elevated");
        } else {
            decision.setTargetWorkflow("models/senior-review");
            decision.setRouteReason("High risk content - requires senior review");
            decision.setUrgencyLevel("urgent");
            decision.setRequiresEscalation(true);
        }

        decision.setEstimatedReviewTime(determineReviewTime(analysis));

        return decision;
    }

    private WorkflowRouteDecision createErrorRouteDecision(String error) {
        WorkflowRouteDecision decision = new WorkflowRouteDecision();
        decision.setDecisionId("error-" + System.currentTimeMillis());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        decision.setRouteMetadata(metadata);

        return decision;
    }

    private List<ContentIssue> identifyContentIssues(ContentAnalysisResult analysis) {
        List<ContentIssue> issues = new ArrayList<>();

        if (analysis.getRiskScore() > 0.5) {
            ContentIssue riskIssue = ContentIssue.builder()
                    .issueId("issue-" + System.currentTimeMillis())
                    .issueType("risk")
                    .severity(analysis.getRiskLevel())
                    .description("Content has elevated risk score")
                    .contentPath(analysis.getContentPath())
                    .suggestedFix("Review content before publishing")
                    .autoCorrectable(false)
                    .detectedAt(System.currentTimeMillis())
                    .build();
            issues.add(riskIssue);
        }

        if (analysis.getAffectedComponents() != null && !analysis.getAffectedComponents().isEmpty()) {
            for (String component : analysis.getAffectedComponents()) {
                if (component.contains("deprecated")) {
                    ContentIssue componentIssue = ContentIssue.builder()
                            .issueId("issue-" + System.currentTimeMillis() + "-" + component)
                            .issueType("deprecated-component")
                            .severity("medium")
                            .description("Component " + component + " is deprecated")
                            .contentPath(analysis.getContentPath())
                            .suggestedFix("Replace with recommended component")
                            .autoCorrectable(true)
                            .correctAction("replace:" + component)
                            .detectedAt(System.currentTimeMillis())
                            .build();
                    issues.add(componentIssue);
                }
            }
        }

        return issues;
    }

    private String buildAnalysisPrompt(String contentPath) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the content at path and determine its complexity and risk level.\n\n");
        prompt.append("Content Path: ").append(contentPath).append("\n\n");
        prompt.append("Provide analysis including:");
        prompt.append("\n1. Content type and scope");
        prompt.append("\n2. Complexity score (0-1)");
        prompt.append("\n3. Risk level (low/medium/high)");
        prompt.append("\n4. List of changes detected");
        prompt.append("\n5. Affected components");
        prompt.append("\n6. Recommended workflow action");

        return prompt.toString();
    }

    private double determineComplexity(String aiResponse) {
        if (aiResponse == null) {
            return 0.5;
        }

        String lower = aiResponse.toLowerCase();
        if (lower.contains("simple") || lower.contains("minor")) {
            return 0.2;
        } else if (lower.contains("complex") || lower.contains("major")) {
            return 0.8;
        }

        return 0.5;
    }

    private double determineRisk(String aiResponse) {
        if (aiResponse == null) {
            return 0.5;
        }

        String lower = aiResponse.toLowerCase();
        if (lower.contains("high risk") || lower.contains("security") || lower.contains("critical")) {
            return 0.8;
        } else if (lower.contains("low risk") || lower.contains("safe")) {
            return 0.2;
        }

        return 0.5;
    }

    private String determineRiskLevel(double riskScore) {
        if (riskScore < 0.3) {
            return "low";
        } else if (riskScore < 0.7) {
            return "medium";
        } else {
            return "high";
        }
    }

    private List<String> extractChanges(String aiResponse) {
        return new ArrayList<>();
    }

    private List<String> extractComponents(String aiResponse) {
        return new ArrayList<>();
    }

    private String determineContentType(String aiResponse) {
        if (aiResponse == null) {
            return "unknown";
        }

        String lower = aiResponse.toLowerCase();
        if (lower.contains("page")) {
            return "page";
        } else if (lower.contains("image") || lower.contains("asset")) {
            return "asset";
        } else if (lower.contains("component")) {
            return "component";
        }

        return "content";
    }

    private String determineRecommendedAction(ContentAnalysisResult analysis) {
        if (analysis.isSimpleChange() && analysis.getComplexityScore() <= simpleChangeThreshold) {
            return "approve";
        } else if ("high".equals(analysis.getRiskLevel())) {
            return "senior-review";
        } else {
            return "review";
        }
    }

    private long determineReviewTime(ContentAnalysisResult analysis) {
        if ("high".equals(analysis.getRiskLevel())) {
            return 480;
        } else if ("medium".equals(analysis.getRiskLevel())) {
            return 240;
        } else {
            return 60;
        }
    }

    private void evictOldCacheEntries() {
        if (analysisCache.size() > cacheSize) {
            int toRemove = analysisCache.size() - cacheSize;
            Iterator<String> iter = analysisCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                analysisCache.remove(iter.next());
            }
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public double getSimpleChangeThreshold() {
        return simpleChangeThreshold;
    }

    public boolean isAutoApproveEnabled() {
        return autoApproveEnabled;
    }

    public String getDefaultApprovalWorkflow() {
        return defaultApprovalWorkflow;
    }

    public String getDefaultReviewWorkflow() {
        return defaultReviewWorkflow;
    }

    public int getAnalysisCacheSize() {
        return analysisCache.size();
    }

    public int getMetricsHistorySize() {
        return metricsHistory.size();
    }
}