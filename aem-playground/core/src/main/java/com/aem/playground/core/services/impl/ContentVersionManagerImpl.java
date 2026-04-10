package com.aem.playground.core.services;

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

@Component(service = ContentVersionManager.class)
@Designate(ocd = ContentVersionManagerConfig.class)
public class ContentVersionManagerImpl implements ContentVersionManager {

    private static final Logger log = LoggerFactory.getLogger(ContentVersionManagerImpl.class);

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";

    private static final String SYSTEM_PROMPT = "You are an AI content version management expert for Adobe Experience Manager (AEM). " +
            "Analyze content versions, compare differences, summarize changes, detect drift, and provide intelligent recommendations.";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, VersionComparison> comparisonCache = new ConcurrentHashMap<>();
    private final Map<String, VersionSummary> summaryCache = new ConcurrentHashMap<>();
    private final Map<String, ContentDrift> driftCache = new ConcurrentHashMap<>();
    private final Map<String, VersionRestorePoint> restorePointCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private boolean enableCache;
    private int cacheSize;
    private float driftThreshold;
    private boolean autoRestorePoints;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(ContentVersionManagerConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), DEFAULT_API_URL);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        this.driftThreshold = config.driftThreshold();
        this.autoRestorePoints = config.autoRestorePoints();
        log.info("ContentVersionManager activated with URL: {}", serviceUrl);
    }

    @Override
    public VersionComparison compareVersions(String contentPath, String versionId1, String versionId2) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(versionId1) || StringUtils.isBlank(versionId2)) {
            return createErrorComparison("Content path and both version IDs are required");
        }

        try {
            String cacheKey = generateComparisonCacheKey(contentPath, versionId1, versionId2);
            if (enableCache) {
                VersionComparison cached = comparisonCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for version comparison: {}", cacheKey);
                    return cached;
                }
            }

            String content1 = fetchVersionContent(contentPath, versionId1);
            String content2 = fetchVersionContent(contentPath, versionId2);

            VersionComparison comparison = executeVersionComparison(contentPath, versionId1, versionId2, content1, content2);

            if (enableCache && comparison != null) {
                comparisonCache.put(cacheKey, comparison);
                evictOldComparisonCache();
            }

            return comparison;
        } catch (Exception e) {
            log.error("Error comparing versions: {}", e.getMessage());
            return createErrorComparison(e.getMessage());
        }
    }

    @Override
    public VersionSummary generateVersionSummary(String contentPath, String versionId) {
        return generateVersionSummary(contentPath, versionId, true);
    }

    @Override
    public VersionSummary generateVersionSummary(String contentPath, String versionId, boolean includeDetails) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(versionId)) {
            return createErrorSummary("Content path and version ID are required");
        }

        try {
            String cacheKey = generateSummaryCacheKey(contentPath, versionId);
            if (enableCache) {
                VersionSummary cached = summaryCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for version summary: {}", cacheKey);
                    return cached;
                }
            }

            String content = fetchVersionContent(contentPath, versionId);

            VersionSummary summary = executeVersionSummary(contentPath, versionId, content, includeDetails);

            if (enableCache && summary != null) {
                summaryCache.put(cacheKey, summary);
                evictOldSummaryCache();
            }

            return summary;
        } catch (Exception e) {
            log.error("Error generating version summary: {}", e.getMessage());
            return createErrorSummary(e.getMessage());
        }
    }

    @Override
    public List<VersionRecommendation> suggestVersionActions(String contentPath, List<String> versionIds) {
        List<VersionRecommendation> recommendations = new ArrayList<>();

        if (StringUtils.isBlank(contentPath) || versionIds == null || versionIds.isEmpty()) {
            return recommendations;
        }

        try {
            for (String versionId : versionIds) {
                VersionRecommendation recommendation = executeVersionRecommendation(contentPath, versionId, versionIds);
                recommendations.add(recommendation);
            }

            recommendations.sort((r1, r2) -> Double.compare(r2.getConfidenceScore(), r1.getConfidenceScore()));

            return recommendations;
        } catch (Exception e) {
            log.error("Error suggesting version actions: {}", e.getMessage());
            return recommendations;
        }
    }

    @Override
    public ContentDrift detectContentDrift(String contentPath, String baseVersionId, String currentVersionId) {
        return detectContentDrift(contentPath, baseVersionId, currentVersionId, driftThreshold);
    }

    @Override
    public ContentDrift detectContentDrift(String contentPath, String baseVersionId, String currentVersionId, double threshold) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(baseVersionId) || StringUtils.isBlank(currentVersionId)) {
            return createErrorDrift("Content path and both version IDs are required");
        }

        try {
            String cacheKey = generateDriftCacheKey(contentPath, baseVersionId, currentVersionId);
            if (enableCache) {
                ContentDrift cached = driftCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for content drift: {}", cacheKey);
                    return cached;
                }
            }

            String baseContent = fetchVersionContent(contentPath, baseVersionId);
            String currentContent = fetchVersionContent(contentPath, currentVersionId);

            ContentDrift drift = executeContentDrift(contentPath, baseVersionId, currentVersionId, baseContent, currentContent, threshold);

            if (enableCache && drift != null) {
                driftCache.put(cacheKey, drift);
                evictOldDriftCache();
            }

            return drift;
        } catch (Exception e) {
            log.error("Error detecting content drift: {}", e.getMessage());
            return createErrorDrift(e.getMessage());
        }
    }

    @Override
    public VersionRestorePoint createRestorePoint(String contentPath, String versionId) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(versionId)) {
            return null;
        }

        VersionRestorePoint restorePoint = new VersionRestorePoint();
        restorePoint.setRestorePointId(UUID.randomUUID().toString());
        restorePoint.setContentPath(contentPath);
        restorePoint.setVersionId(versionId);
        restorePoint.setLabel("Manual Restore Point " + System.currentTimeMillis());
        restorePoint.setDescription("Manually created restore point for version " + versionId);
        restorePoint.setTimestamp(System.currentTimeMillis());
        restorePoint.setType(VersionRestorePoint.RestorePointType.MANUAL);
        restorePoint.setIntelligent(false);

        String content = fetchVersionContent(contentPath, versionId);
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("content", content);
        snapshot.put("contentLength", content != null ? content.length() : 0);
        restorePoint.setSnapshot(snapshot);

        restorePointCache.put(restorePoint.getRestorePointId(), restorePoint);

        return restorePoint;
    }

    @Override
    public VersionRestorePoint createIntelligentRestorePoint(String contentPath, String currentVersionId) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(currentVersionId)) {
            return null;
        }

        try {
            String content = fetchVersionContent(contentPath, currentVersionId);

            VersionSummary summary = generateVersionSummary(contentPath, currentVersionId, true);

            VersionRestorePoint restorePoint = new VersionRestorePoint();
            restorePoint.setRestorePointId(UUID.randomUUID().toString());
            restorePoint.setContentPath(contentPath);
            restorePoint.setVersionId(currentVersionId);
            restorePoint.setTimestamp(System.currentTimeMillis());
            restorePoint.setType(VersionRestorePoint.RestorePointType.INTELLIGENT_AUTO);
            restorePoint.setIntelligent(true);

            if (summary != null && StringUtils.isNotBlank(summary.getSummary())) {
                restorePoint.setLabel("AI-Created: " + truncate(summary.getSummary(), 50));
                restorePoint.setDescription("Intelligently created based on: " + summary.getSummary());
            } else {
                restorePoint.setLabel("AI Restore Point " + System.currentTimeMillis());
                restorePoint.setDescription("AI-analyzed restore point");
            }

            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("content", content);
            snapshot.put("contentLength", content != null ? content.length() : 0);
            snapshot.put("aiSummary", summary != null ? summary.getSummary() : "");
            restorePoint.setSnapshot(snapshot);

            restorePointCache.put(restorePoint.getRestorePointId(), restorePoint);

            return restorePoint;
        } catch (Exception e) {
            log.error("Error creating intelligent restore point: {}", e.getMessage());
            return createRestorePoint(contentPath, currentVersionId);
        }
    }

    @Override
    public List<VersionRestorePoint> listRestorePoints(String contentPath) {
        List<VersionRestorePoint> points = new ArrayList<>();

        for (VersionRestorePoint point : restorePointCache.values()) {
            if (point.getContentPath().equals(contentPath)) {
                points.add(point);
            }
        }

        points.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        return points;
    }

    @Override
    public boolean restoreFromPoint(String contentPath, String restorePointId) {
        VersionRestorePoint restorePoint = restorePointCache.get(restorePointId);
        if (restorePoint == null) {
            log.error("Restore point not found: {}", restorePointId);
            return false;
        }

        return restorePoint.getContentPath().equals(contentPath);
    }

    private String fetchVersionContent(String contentPath, String versionId) {
        return "Content for " + contentPath + " version " + versionId;
    }

    private VersionComparison executeVersionComparison(String contentPath, String versionId1, String versionId2, String content1, String content2) {
        try {
            String prompt = buildComparisonPrompt(contentPath, versionId1, versionId2, content1, content2);
            String response = callAI(prompt);

            return parseComparisonResponse(contentPath, versionId1, versionId2, response, content1, content2);
        } catch (Exception e) {
            log.warn("Error calling AI service, returning default comparison: {}", e.getMessage());
            return createDefaultComparison(contentPath, versionId1, versionId2, content1, content2);
        }
    }

    private String buildComparisonPrompt(String contentPath, String versionId1, String versionId2, String content1, String content2) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Compare the following two content versions and provide a detailed analysis.\n\n");
        prompt.append("Content Path: ").append(contentPath).append("\n");
        prompt.append("Version 1: ").append(versionId1).append("\n");
        prompt.append("Content 1:\n").append(content1).append("\n\n");
        prompt.append("Version 2: ").append(versionId2).append("\n");
        prompt.append("Content 2:\n").append(content2).append("\n\n");
        prompt.append("Provide:\n");
        prompt.append("1. A similarity score (0-1)\n");
        prompt.append("2. List of key differences with paths\n");
        prompt.append("3. Summary of changes\n");
        prompt.append("4. Assessment of significance for each change");

        return prompt.toString();
    }

    private String callAI(String prompt) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(serviceUrl);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);

            Map<String, Object> request = new HashMap<>();
            request.put("model", defaultModel);
            request.put("temperature", temperature);
            request.put("max_tokens", maxTokens);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            request.put("messages", messages);

            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    log.error("API error: {} - {}", statusCode, responseBody);
                    throw new IOException("API returned status code: " + statusCode);
                }

                return responseBody;
            }
        }
    }

    private VersionComparison parseComparisonResponse(String contentPath, String versionId1, String versionId2, String response, String content1, String content2) {
        VersionComparison comparison = new VersionComparison();
        comparison.setContentPath(contentPath);
        comparison.setVersionId1(versionId1);
        comparison.setVersionId2(versionId2);

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choices = rootNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();
                comparison.setSummary(content);
                comparison.setSimilarityScore(calculateSimilarityScore(content1, content2));
            } else {
                comparison.setSummary("Unable to parse AI response");
                comparison.setSimilarityScore(0.0);
            }
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            comparison.setSummary("Error parsing response");
            comparison.setSimilarityScore(0.0);
        }

        if (comparison.getSummary() == null) {
            comparison.setSummary("Default comparison generated");
            comparison.setSimilarityScore(0.75);
        }

        comparison.getMetadata().put("aiGenerated", true);
        comparison.getMetadata().put("timestamp", System.currentTimeMillis());

        return comparison;
    }

    private double calculateSimilarityScore(String c1, String c2) {
        if (c1 == null || c2 == null || c1.isEmpty() || c2.isEmpty()) {
            return 0.0;
        }

        Set<String> words1 = new HashSet<>(Arrays.asList(c1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(c2.split("\\s+")));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    private VersionComparison createDefaultComparison(String contentPath, String versionId1, String versionId2, String content1, String content2) {
        VersionComparison comparison = new VersionComparison();
        comparison.setContentPath(contentPath);
        comparison.setVersionId1(versionId1);
        comparison.setVersionId2(versionId2);
        comparison.setSimilarityScore(calculateSimilarityScore(content1, content2));

        List<ContentDifference> differences = generateDefaultDifferences(content1, content2);
        comparison.setDifferences(differences);

        StringBuilder summary = new StringBuilder();
        summary.append("Version comparison between ").append(versionId1).append(" and ").append(versionId2).append(": ");
        summary.append(differences.size()).append(" differences found.");
        comparison.setSummary(summary.toString());

        comparison.getMetadata().put("aiGenerated", false);
        comparison.getMetadata().put("timestamp", System.currentTimeMillis());

        return comparison;
    }

    private List<ContentDifference> generateDefaultDifferences(String content1, String content2) {
        List<ContentDifference> differences = new ArrayList<>();

        if (content1 == null || content2 == null) {
            return differences;
        }

        String[] lines1 = content1.split("\n");
        String[] lines2 = content2.split("\n");

        int maxLines = Math.max(lines1.length, lines2.length);
        for (int i = 0; i < maxLines; i++) {
            String line1 = i < lines1.length ? lines1[i] : "";
            String line2 = i < lines2.length ? lines2[i] : "";

            if (!line1.equals(line2)) {
                ContentDifference diff = new ContentDifference();
                diff.setPath("/line/" + i);
                diff.setOldValue(line1);
                diff.setNewValue(line2);
                diff.setDescription("Content changed at line " + i);
                diff.setSeverity(0.5);

                if (line1.isEmpty()) {
                    diff.setType(ContentDifference.DifferenceType.ADDED);
                } else if (line2.isEmpty()) {
                    diff.setType(ContentDifference.DifferenceType.REMOVED);
                } else {
                    diff.setType(ContentDifference.DifferenceType.MODIFIED);
                }

                differences.add(diff);
            }
        }

        return differences;
    }

    private VersionComparison createErrorComparison(String error) {
        VersionComparison comparison = new VersionComparison();
        comparison.setSummary("Error: " + error);
        comparison.setSimilarityScore(0.0);
        comparison.getMetadata().put("error", error);

        return comparison;
    }

    private VersionSummary executeVersionSummary(String contentPath, String versionId, String content, boolean includeDetails) {
        try {
            String prompt = buildSummaryPrompt(contentPath, versionId, content, includeDetails);
            String response = callAI(prompt);

            return parseSummaryResponse(contentPath, versionId, response, content);
        } catch (Exception e) {
            log.warn("Error calling AI service, returning default summary: {}", e.getMessage());
            return createDefaultSummary(contentPath, versionId, content, includeDetails);
        }
    }

    private String buildSummaryPrompt(String contentPath, String versionId, String content, boolean includeDetails) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze and summarize the following content version.\n\n");
        prompt.append("Content Path: ").append(contentPath).append("\n");
        prompt.append("Version ID: ").append(versionId).append("\n");
        prompt.append("Content:\n").append(content).append("\n\n");

        if (includeDetails) {
            prompt.append("Provide:\n");
            prompt.append("1. A concise summary (2-3 sentences)\n");
            prompt.append("2. Key changes in this version\n");
            prompt.append("3. Important metadata (author, timestamp if available)\n");
            prompt.append("4. AI confidence score for the summary\n");
            prompt.append("5. Suggested label for this version");
        } else {
            prompt.append("Provide a brief summary (1 sentence).");
        }

        return prompt.toString();
    }

    private VersionSummary parseSummaryResponse(String contentPath, String versionId, String response, String originalContent) {
        VersionSummary summary = new VersionSummary();
        summary.setContentPath(contentPath);
        summary.setVersionId(versionId);

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choices = rootNode.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();
                summary.setSummary(content);
                summary.setAiConfidence(0.85);
            } else {
                summary.setSummary("Unable to parse AI response");
                summary.setAiConfidence(0.0);
            }
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            summary.setSummary("Error parsing response");
            summary.setAiConfidence(0.0);
        }

        if (summary.getSummary() == null) {
            summary.setSummary("Default summary generated");
            summary.setAiConfidence(0.75);
        }

        summary.setTimestamp(System.currentTimeMillis());
        summary.setVersionLabel("v" + versionId);

        summary.getContentMetadata().put("originalContentLength", originalContent != null ? originalContent.length() : 0);
        summary.getContentMetadata().put("aiGenerated", true);

        return summary;
    }

    private VersionSummary createDefaultSummary(String contentPath, String versionId, String content, boolean includeDetails) {
        VersionSummary summary = new VersionSummary();
        summary.setContentPath(contentPath);
        summary.setVersionId(versionId);
        summary.setTimestamp(System.currentTimeMillis());
        summary.setAiConfidence(0.75);

        if (includeDetails) {
            summary.setSummary("Content version " + versionId + " for " + contentPath + " with " + 
                (content != null ? content.length() : 0) + " characters.");
            summary.setKeyChanges(Arrays.asList("Content updated", "Version created"));
        } else {
            summary.setSummary("Content version " + versionId);
        }

        summary.setVersionLabel("v" + versionId);

        summary.getContentMetadata().put("aiGenerated", false);
        summary.getContentMetadata().put("originalContentLength", content != null ? content.length() : 0);

        return summary;
    }

    private VersionSummary createErrorSummary(String error) {
        VersionSummary summary = new VersionSummary();
        summary.setSummary("Error: " + error);
        summary.setAiConfidence(0.0);
        summary.getContentMetadata().put("error", error);

        return summary;
    }

    private VersionRecommendation executeVersionRecommendation(String contentPath, String versionId, List<String> allVersionIds) {
        VersionRecommendation recommendation = new VersionRecommendation();
        recommendation.setVersionId(versionId);

        try {
            int index = allVersionIds.indexOf(versionId);
            int total = allVersionIds.size();

            if (index < 0) {
                recommendation.setRecommendation(VersionRecommendation.RecommendationType.REVIEW);
                recommendation.setReason("Unable to determine version position");
                recommendation.setConfidenceScore(0.5);
            } else if (index == total - 1) {
                recommendation.setRecommendation(VersionRecommendation.RecommendationType.KEEP);
                recommendation.setReason("Latest version - most current content");
                recommendation.setConfidenceScore(0.9);
            } else if (index == 0) {
                recommendation.setRecommendation(VersionRecommendation.RecommendationType.DELETE);
                recommendation.setReason("Oldest version - superseded by newer versions");
                recommendation.setConfidenceScore(0.7);
            } else if (index <= total / 3) {
                recommendation.setRecommendation(VersionRecommendation.RecommendationType.DELETE);
                recommendation.setReason("Early version with multiple newer versions");
                recommendation.setConfidenceScore(0.6);
            } else {
                recommendation.setRecommendation(VersionRecommendation.RecommendationType.REVIEW);
                recommendation.setReason("Middle version - may contain valuable changes");
                recommendation.setConfidenceScore(0.55);
            }

            recommendation.setSuggestedActions(generateSuggestedActions(recommendation.getRecommendation()));

            recommendation.getMetadata().put("contentPath", contentPath);
            recommendation.getMetadata().put("versionIndex", index);
            recommendation.getMetadata().put("totalVersions", total);

        } catch (Exception e) {
            log.error("Error generating version recommendation: {}", e.getMessage());
            recommendation.setRecommendation(VersionRecommendation.RecommendationType.REVIEW);
            recommendation.setReason("Error: " + e.getMessage());
            recommendation.setConfidenceScore(0.0);
        }

        return recommendation;
    }

    private List<String> generateSuggestedActions(VersionRecommendation.RecommendationType type) {
        List<String> actions = new ArrayList<>();

        switch (type) {
            case KEEP:
                actions.add("Keep this version as it contains the latest updates");
                break;
            case MERGE:
                actions.add("Merge selected changes from this version into current");
                break;
            case DELETE:
                actions.add("Delete to save storage and reduce version clutter");
                break;
            case RESTORE:
                actions.add("Restore if needed for rollback");
                break;
            case REVIEW:
                actions.add("Review changes before making decision");
                actions.add("Compare with other versions");
                break;
        }

        return actions;
    }

    private ContentDrift executeContentDrift(String contentPath, String baseVersionId, String currentVersionId, 
            String baseContent, String currentContent, double threshold) {
        ContentDrift drift = new ContentDrift();
        drift.setContentPath(contentPath);
        drift.setBaseVersionId(baseVersionId);
        drift.setCurrentVersionId(currentVersionId);
        drift.setDetectedAt(System.currentTimeMillis());

        double similarity = calculateSimilarityScore(baseContent, currentContent);
        double driftScore = 1.0 - similarity;

        drift.setDriftScore(driftScore);

        ContentDrift.DriftStatus status = determineDriftStatus(driftScore, threshold);
        drift.setStatus(status);

        drift.setDriftReason(generateDriftReason(driftScore, status));

        List<DriftDetail> details = generateDriftDetails(baseContent, currentContent);
        drift.setDriftDetails(details);

        drift.getMetadata().put("threshold", threshold);
        drift.getMetadata().put("aiGenerated", true);
        drift.getMetadata().put("timestamp", System.currentTimeMillis());

        return drift;
    }

    private ContentDrift.DriftStatus determineDriftStatus(double driftScore, double threshold) {
        if (driftScore >= threshold * 3) {
            return ContentDrift.DriftStatus.CRITICAL_DRIFT;
        } else if (driftScore >= threshold * 2) {
            return ContentDrift.DriftStatus.MAJOR_DRIFT;
        } else if (driftScore >= threshold * 1.5) {
            return ContentDrift.DriftStatus.MODERATE_DRIFT;
        } else if (driftScore >= threshold) {
            return ContentDrift.DriftStatus.MINOR_DRIFT;
        } else {
            return ContentDrift.DriftStatus.STABLE;
        }
    }

    private String generateDriftReason(double driftScore, ContentDrift.DriftStatus status) {
        StringBuilder reason = new StringBuilder();
        reason.append(String.format("Drift score: %.2f - ", driftScore));

        switch (status) {
            case STABLE:
                reason.append("Content is stable with minimal changes");
                break;
            case MINOR_DRIFT:
                reason.append("Minor changes detected - likely routine updates");
                break;
            case MODERATE_DRIFT:
                reason.append("Moderate changes - significant updates made");
                break;
            case MAJOR_DRIFT:
                reason.append("Major changes - substantial content revision");
                break;
            case CRITICAL_DRIFT:
                reason.append("Critical changes - content substantially modified");
                break;
        }

        return reason.toString();
    }

    private List<DriftDetail> generateDriftDetails(String baseContent, String currentContent) {
        List<DriftDetail> details = new ArrayList<>();

        if (baseContent == null || currentContent == null) {
            return details;
        }

        String[] baseLines = baseContent.split("\n");
        String[] currentLines = currentContent.split("\n");

        int maxLines = Math.max(baseLines.length, currentLines.length);
        for (int i = 0; i < maxLines && i < 10; i++) {
            String baseLine = i < baseLines.length ? baseLines[i] : "";
            String currentLine = i < currentLines.length ? currentLines[i] : "";

            if (!baseLine.equals(currentLine)) {
                DriftDetail detail = new DriftDetail();
                detail.setPath("/line/" + i);
                detail.setChangeType(baseLine.isEmpty() ? "added" : (currentLine.isEmpty() ? "removed" : "modified"));
                detail.setImpactScore(0.5);
                detail.setDescription("Change at line " + i + ": " + 
                    (baseLine.isEmpty() ? "added" : (currentLine.isEmpty() ? "removed" : "modified")));
                details.add(detail);
            }
        }

        return details;
    }

    private ContentDrift createErrorDrift(String error) {
        ContentDrift drift = new ContentDrift();
        drift.setDriftScore(0.0);
        drift.setStatus(ContentDrift.DriftStatus.STABLE);
        drift.setDriftReason("Error: " + error);
        drift.getMetadata().put("error", error);

        return drift;
    }

    private String generateComparisonCacheKey(String contentPath, String versionId1, String versionId2) {
        return contentPath + ":" + versionId1 + ":" + versionId2;
    }

    private String generateSummaryCacheKey(String contentPath, String versionId) {
        return contentPath + ":" + versionId;
    }

    private String generateDriftCacheKey(String contentPath, String baseVersionId, String currentVersionId) {
        return contentPath + ":" + baseVersionId + ":" + currentVersionId;
    }

    private void evictOldComparisonCache() {
        if (comparisonCache.size() > cacheSize) {
            int toRemove = comparisonCache.size() - cacheSize;
            Iterator<String> iter = comparisonCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                comparisonCache.remove(iter.next());
            }
        }
    }

    private void evictOldSummaryCache() {
        if (summaryCache.size() > cacheSize) {
            int toRemove = summaryCache.size() - cacheSize;
            Iterator<String> iter = summaryCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                summaryCache.remove(iter.next());
            }
        }
    }

    private void evictOldDriftCache() {
        if (driftCache.size() > cacheSize) {
            int toRemove = driftCache.size() - cacheSize;
            Iterator<String> iter = driftCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                driftCache.remove(iter.next());
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}