package com.aem.playground.core.services.impl;

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
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.ContentError;
import com.aem.playground.core.services.ErrorDetectionService;
import com.aem.playground.core.services.ErrorDetectionServiceConfig;
import com.aem.playground.core.services.ErrorFix;
import com.aem.playground.core.services.ErrorSeverity;
import com.aem.playground.core.services.ErrorType;
import com.aem.playground.core.services.FixStep;
import com.aem.playground.core.services.FixType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component(service = ErrorDetectionService.class)
@Designate(ocd = ErrorDetectionServiceConfig.class)
public class AIErrorDetectionService implements ErrorDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AIErrorDetectionService.class);

    private static final String DEFAULT_LINK_DETECTION_PROMPT = 
        "Analyze the following content and identify all broken or invalid links. " +
        "Return each broken link in format: url|reason|suggested_fix. " +
        "Content path: %s. Content: %s";

    private static final String DEFAULT_ASSET_DETECTION_PROMPT = 
        "Analyze the following content and identify any missing assets or references. " +
        "Return each missing asset in format: reference|location|asset_type|suggested_alternative. " +
        "Content path: %s. Content: %s";

    private static final String DEFAULT_STRUCTURE_PROMPT = 
        "Analyze the following content for structural issues. " +
        "Check for: missing required fields, invalid component nesting, inconsistent data. " +
        "Return each issue in format: issue_type|location|description|severity. " +
        "Content path: %s. Content: %s";

    private static final String DEFAULT_AUTHORING_PROMPT = 
        "Analyze the following content for authoring errors. " +
        "Check for: typos, grammatical errors, inconsistent formatting, invalid metadata. " +
        "Return each error in format: error_type|location|description|severity. " +
        "Content path: %s. Content: %s";

    private static final String DEFAULT_FIX_PROMPT = 
        "For the following error, suggest an automatic fix. " +
        "Error type: %s. Error message: %s. Content path: %s. Location: %s. " +
        "Return in format: fix_type|description|original_value|suggested_value|confidence|step1_action|step1_target|...";

    private static final Pattern LINK_PATTERN = Pattern.compile(
        "href=[\"']([^\"']+)[\"']|<a[^>]+href=[\"']([^\"']+)[\"']|src=[\"']([^\"']+)[\"']|data-src=[\"']([^\"']+)[\"']",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REFERENCE_PATTERN = Pattern.compile(
        "reference=[\"']([^\"']+)[\"']|path=[\"']([^\"']+)[\"']|dam:asset=[\"']([^\"']+)[\"']",
        Pattern.CASE_INSENSITIVE
    );

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedErrorResult> cache = new ConcurrentHashMap<>();
    private final Map<String, List<ContentError>> errorHistory = new ConcurrentHashMap<>();

    private String aiServiceUrl;
    private String apiKey;
    private String model;
    private int maxTokens;
    private double temperature;
    private boolean enableBrokenLinkDetection;
    private boolean enableMissingAssetDetection;
    private boolean enableStructureIssueDetection;
    private boolean enableAuthoringErrorDetection;
    private boolean enableAutoFixSuggestions;
    private boolean enableErrorDashboard;
    private boolean cacheEnabled;
    private int cacheSize;
    private int errorHistoryDays;

    @Reference
    private AIService aiService;

    @Activate
    @Modified
    protected void activate(ErrorDetectionServiceConfig config) {
        this.aiServiceUrl = config.ai_service_url();
        this.apiKey = config.api_key();
        this.model = config.model();
        this.maxTokens = config.max_tokens();
        this.temperature = PropertiesUtil.toDouble(config.temperature(), 0.3);
        this.enableBrokenLinkDetection = config.enable_broken_link_detection();
        this.enableMissingAssetDetection = config.enable_missing_asset_detection();
        this.enableStructureIssueDetection = config.enable_structure_issue_detection();
        this.enableAuthoringErrorDetection = config.enable_authoring_error_detection();
        this.enableAutoFixSuggestions = config.enable_auto_fix_suggestions();
        this.enableErrorDashboard = config.enable_error_dashboard();
        this.cacheEnabled = config.cache_enabled();
        this.cacheSize = config.cache_size();
        this.errorHistoryDays = config.error_history_days();
        log.info("AIErrorDetectionService activated with detection features enabled");
    }

    @Deactivate
    protected void deactivate() {
        cache.clear();
        errorHistory.clear();
    }

    @Override
    public ErrorReport detectErrors(String contentPath, String content) {
        long startTime = System.currentTimeMillis();

        if (StringUtils.isBlank(contentPath)) {
            contentPath = "unknown";
        }

        List<ContentError> allErrors = new ArrayList<>();

        if (enableBrokenLinkDetection) {
            allErrors.addAll(detectBrokenLinks(contentPath, content));
        }
        if (enableMissingAssetDetection) {
            allErrors.addAll(detectMissingAssets(contentPath, content));
        }
        if (enableStructureIssueDetection) {
            allErrors.addAll(detectContentStructureIssues(contentPath, content));
        }
        if (enableAuthoringErrorDetection) {
            allErrors.addAll(detectAuthoringErrors(contentPath, content));
        }

        List<ContentError> aiErrors = detectErrorsWithAI(contentPath, content);
        allErrors.addAll(aiErrors);

        long processingTime = System.currentTimeMillis() - startTime;

        if (!allErrors.isEmpty()) {
            recordErrorHistory(contentPath, allErrors);
        }

        return ErrorReport.create(contentPath, allErrors, processingTime);
    }

    @Override
    public List<ContentError> detectBrokenLinks(String contentPath, String content) {
        List<ContentError> errors = new ArrayList<>();

        if (!enableBrokenLinkDetection || StringUtils.isBlank(content)) {
            return errors;
        }

        Matcher linkMatcher = LINK_PATTERN.matcher(content);
        int position = 0;
        while (linkMatcher.find(position)) {
            String url = linkMatcher.group(1) != null ? linkMatcher.group(1) : 
                         linkMatcher.group(2) != null ? linkMatcher.group(2) :
                         linkMatcher.group(3) != null ? linkMatcher.group(3) :
                         linkMatcher.group(4);

            if (url != null) {
                String location = "position:" + linkMatcher.start();
                
                if (isLikelyBrokenLink(url)) {
                    errors.add(ContentError.builder()
                        .errorId("link_" + System.currentTimeMillis() + "_" + position)
                        .type(ErrorType.BROKEN_LINK)
                        .severity(ErrorSeverity.WARNING)
                        .message("Potentially broken or invalid link: " + url)
                        .contentPath(contentPath)
                        .location(location)
                        .suggestedFix("Verify the link target or replace with valid URL")
                        .metadata(createLinkMetadata(url))
                        .build());
                }
            }
            position = linkMatcher.end();
        }

        return errors;
    }

    @Override
    public List<ContentError> detectMissingAssets(String contentPath, String content) {
        List<ContentError> errors = new ArrayList<>();

        if (!enableMissingAssetDetection || StringUtils.isBlank(content)) {
            return errors;
        }

        Matcher refMatcher = REFERENCE_PATTERN.matcher(content);
        int position = 0;
        while (refMatcher.find(position)) {
            String reference = refMatcher.group(1) != null ? refMatcher.group(1) : refMatcher.group(2);
            
            if (reference != null && !isValidReference(reference)) {
                errors.add(ContentError.builder()
                    .errorId("asset_" + System.currentTimeMillis() + "_" + position)
                    .type(ErrorType.MISSING_ASSET)
                    .severity(ErrorSeverity.WARNING)
                    .message("Missing asset reference: " + reference)
                    .contentPath(contentPath)
                    .location("position:" + refMatcher.start())
                    .suggestedFix("Upload missing asset or update reference to existing asset")
                    .metadata(createAssetMetadata(reference))
                    .build());
            }
            position = refMatcher.end();
        }

        return errors;
    }

    @Override
    public List<ContentError> detectContentStructureIssues(String contentPath, String content) {
        List<ContentError> errors = new ArrayList<>();

        if (!enableStructureIssueDetection || StringUtils.isBlank(content)) {
            return errors;
        }

        if (content.contains("jcr:title") && !content.contains("jcr:title\"")) {
            errors.add(ContentError.builder()
                .errorId("struct_" + System.currentTimeMillis() + "_1")
                .type(ErrorType.CONTENT_STRUCTURE)
                .severity(ErrorSeverity.INFO)
                .message("Missing jcr:title property in content node")
                .contentPath(contentPath)
                .location("jcr:content")
                .suggestedFix("Add jcr:title property with appropriate title")
                .build());
        }

        if (content.contains("cq:allowedTemplates") && !content.contains("sling:resourceType")) {
            errors.add(ContentError.builder()
                .errorId("struct_" + System.currentTimeMillis() + "_2")
                .type(ErrorType.CONTENT_STRUCTURE)
                .severity(ErrorSeverity.WARNING)
                .message("Content missing sling:resourceType")
                .contentPath(contentPath)
                .location("jcr:content")
                .suggestedFix("Add sling:resourceType to enable template assignment")
                .build());
        }

        return errors;
    }

    @Override
    public List<ContentError> detectAuthoringErrors(String contentPath, String content) {
        List<ContentError> errors = new ArrayList<>();

        if (!enableAuthoringErrorDetection || StringUtils.isBlank(content)) {
            return errors;
        }

        if (content.length() > 0 && (content.endsWith(" ") || content.startsWith(" "))) {
            errors.add(ContentError.builder()
                .errorId("auth_" + System.currentTimeMillis() + "_1")
                .type(ErrorType.AUTHORING_ERROR)
                .severity(ErrorSeverity.INFO)
                .message("Content has leading or trailing whitespace")
                .contentPath(contentPath)
                .location("content start/end")
                .suggestedFix("Trim whitespace from content")
                .build());
        }

        if (content.contains("[") && !content.contains("]")) {
            errors.add(ContentError.builder()
                .errorId("auth_" + System.currentTimeMillis() + "_2")
                .type(ErrorType.AUTHORING_ERROR)
                .severity(ErrorSeverity.WARNING)
                .message("Unmatched opening bracket detected")
                .contentPath(contentPath)
                .location("content")
                .suggestedFix("Close all opening brackets")
                .build());
        }

        return errors;
    }

    @Override
    public ErrorFix suggestFix(ContentError error) {
        if (!enableAutoFixSuggestions || error == null) {
            return createDefaultFix(error);
        }

        try {
            String prompt = String.format(DEFAULT_FIX_PROMPT,
                error.getType(),
                error.getMessage(),
                error.getContentPath(),
                error.getLocation());

            String response = callAI(prompt);

            return parseFixFromResponse(response, error.getErrorId());

        } catch (Exception e) {
            log.error("Error generating fix suggestion: {}", e.getMessage());
            return createDefaultFix(error);
        }
    }

    @Override
    public ErrorDashboard getErrorDashboard(String startDate, String endDate) {
        if (!enableErrorDashboard) {
            return ErrorDashboard.create(startDate, endDate, 0, 0, 0, 
                                         new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        List<ContentError> allErrors = errorHistory.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        int totalErrors = allErrors.size();
        int resolvedErrors = 0;
        int openErrors = totalErrors;

        List<ErrorSummary> errorsByType = calculateErrorsByType(allErrors);
        List<ErrorTrend> errorTrends = calculateErrorTrends(allErrors);
        List<ContentError> recentErrors = allErrors.stream()
            .sorted((e1, e2) -> Long.compare(e2.getDetectedAt(), e1.getDetectedAt()))
            .limit(10)
            .collect(Collectors.toList());

        return ErrorDashboard.create(startDate, endDate, totalErrors, resolvedErrors, openErrors,
                                     errorsByType, errorTrends, recentErrors);
    }

    private List<ContentError> detectErrorsWithAI(String contentPath, String content) {
        if (StringUtils.isBlank(content)) {
            return new ArrayList<>();
        }

        try {
            String prompt = String.format(
                "Analyze this AEM content for all types of errors: broken links, missing assets, " +
                "structure issues, and authoring errors. Content path: %s. Content: %s. " +
                "Return in format: error_type|severity|location|message|suggested_fix",
                contentPath, content
            );

            String response = callAI(prompt);
            return parseErrorsFromAIResponse(response, contentPath);

        } catch (Exception e) {
            log.error("Error in AI error detection: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String callAI(String prompt) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        request.put("temperature", temperature);
        request.put("max_tokens", maxTokens);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(aiServiceUrl);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    throw new IOException("AI API returned status: " + statusCode);
                }

                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode choices = rootNode.get("choices");
                if (choices == null || !choices.isArray() || choices.size() == 0) {
                    throw new IOException("Invalid AI response format");
                }

                return choices.get(0).get("message").get("content").asText();
            }
        }
    }

    private List<ContentError> parseErrorsFromAIResponse(String response, String contentPath) {
        List<ContentError> errors = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                try {
                    ErrorType type = ErrorType.valueOf(parts[0].trim().toUpperCase().replace(" ", "_"));
                    ErrorSeverity severity = ErrorSeverity.valueOf(parts[1].trim().toUpperCase());
                    String location = parts[2].trim();
                    String message = parts[3].trim();
                    String suggestedFix = parts.length > 4 ? parts[4].trim() : null;

                    errors.add(ContentError.builder()
                        .errorId("ai_" + System.currentTimeMillis() + "_" + errors.size())
                        .type(type)
                        .severity(severity)
                        .message(message)
                        .contentPath(contentPath)
                        .location(location)
                        .suggestedFix(suggestedFix)
                        .build());
                } catch (IllegalArgumentException e) {
                    log.debug("Could not parse error line: {}", line);
                }
            }
        }

        return errors;
    }

    private ErrorFix parseFixFromResponse(String response, String errorId) {
        String[] lines = response.split("\n");
        if (lines.length == 0) {
            return createDefaultFixByErrorId(errorId);
        }

        String[] parts = lines[0].split("\\|");
        if (parts.length < 3) {
            return createDefaultFixByErrorId(errorId);
        }

        try {
            FixType fixType = FixType.valueOf(parts[0].trim().toUpperCase().replace(" ", "_"));
            String description = parts[1].trim();
            String originalValue = parts.length > 2 ? parts[2].trim() : null;
            String suggestedValue = parts.length > 3 ? parts[3].trim() : null;
            double confidence = parts.length > 4 ? Double.parseDouble(parts[4].trim()) : 0.8;

            List<FixStep> steps = new ArrayList<>();
            for (int i = 5; i + 1 < parts.length; i += 2) {
                steps.add(FixStep.builder()
                    .stepNumber(steps.size() + 1)
                    .action(parts[i].trim())
                    .target(i + 1 < parts.length ? parts[i + 1].trim() : null)
                    .description("Execute " + parts[i].trim())
                    .build());
            }

            return ErrorFix.builder()
                .fixId("fix_" + System.currentTimeMillis())
                .errorId(errorId)
                .fixType(fixType)
                .description(description)
                .originalValue(originalValue)
                .suggestedValue(suggestedValue)
                .steps(steps)
                .confidence(confidence)
                .build();

        } catch (IllegalArgumentException e) {
            return createDefaultFixByErrorId(errorId);
        }
    }

    private boolean isLikelyBrokenLink(String url) {
        if (StringUtils.isBlank(url)) {
            return true;
        }
        return url.startsWith("#") || url.contains("undefined") || url.contains("null");
    }

    private boolean isValidReference(String reference) {
        if (StringUtils.isBlank(reference)) {
            return false;
        }
        return !reference.contains("undefined") && !reference.contains("null");
    }

    private Map<String, Object> createLinkMetadata(String url) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("url", url);
        metadata.put("linkType", url.startsWith("/") ? "internal" : "external");
        return metadata;
    }

    private Map<String, Object> createAssetMetadata(String reference) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reference", reference);
        metadata.put("assetType", "unknown");
        return metadata;
    }

    private void recordErrorHistory(String contentPath, List<ContentError> errors) {
        errorHistory.put(contentPath, errors);
        cleanupOldHistory();
    }

    private void cleanupOldHistory() {
        long cutoffTime = System.currentTimeMillis() - (errorHistoryDays * 24L * 60L * 60L * 1000L);
        errorHistory.entrySet().removeIf(entry -> 
            entry.getValue().stream().allMatch(e -> e.getDetectedAt() < cutoffTime));
    }

    private List<ErrorSummary> calculateErrorsByType(List<ContentError> errors) {
        Map<ErrorType, Long> counts = errors.stream()
            .collect(Collectors.groupingBy(ContentError::getType, Collectors.counting()));

        return counts.entrySet().stream()
            .map(entry -> ErrorSummary.create(entry.getKey(), entry.getValue().intValue()))
            .collect(Collectors.toList());
    }

    private List<ErrorTrend> calculateErrorTrends(List<ContentError> errors) {
        Map<String, Long> countsByDay = errors.stream()
            .collect(Collectors.groupingBy(
                e -> new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(e.getDetectedAt())),
                Collectors.counting()
            ));

        return countsByDay.entrySet().stream()
            .map(entry -> ErrorTrend.create(entry.getKey(), entry.getValue().intValue()))
            .sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
            .collect(Collectors.toList());
    }

    private ErrorFix createDefaultFix(ContentError error) {
        if (error == null) {
            return ErrorFix.builder()
                .fixId("fix_default_" + System.currentTimeMillis())
                .fixType(FixType.NOTIFY_AUTHOR)
                .description("Manual review required")
                .confidence(0.0)
                .build();
        }
        return createDefaultFixByErrorId(error.getErrorId());
    }

    private ErrorFix createDefaultFixByErrorId(String errorId) {
        FixType defaultFixType = FixType.NOTIFY_AUTHOR;
        String defaultDescription = "Manual review and correction required";

        if (errorId != null) {
            if (errorId.startsWith("link_")) {
                defaultFixType = FixType.REPLACE_LINK;
                defaultDescription = "Verify and update the broken link";
            } else if (errorId.startsWith("asset_")) {
                defaultFixType = FixType.ADD_MISSING_ASSET;
                defaultDescription = "Upload missing asset or update reference";
            } else if (errorId.startsWith("struct_")) {
                defaultFixType = FixType.UPDATE_CONTENT;
                defaultDescription = "Fix content structure issue";
            } else if (errorId.startsWith("auth_")) {
                defaultFixType = FixType.AUTO_CORRECT;
                defaultDescription = "Auto-correct authoring error";
            }
        }

        return ErrorFix.builder()
            .fixId("fix_" + System.currentTimeMillis())
            .errorId(errorId)
            .fixType(defaultFixType)
            .description(defaultDescription)
            .confidence(0.5)
            .build();
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public String getModel() {
        return model;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    private static class CachedErrorResult {
        final List<ContentError> errors;

        CachedErrorResult(List<ContentError> errors) {
            this.errors = errors;
        }
    }
}