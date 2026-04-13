package com.aem.playground.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(service = ContentTransformer.class, immediate = true)
@Designate(ocd = AIContentTransformerService.Config.class)
public class AIContentTransformerService implements ContentTransformer {

    @ObjectClassDefinition(name = "AI Content Transformer Configuration",
            description = "Configuration for AI-powered content transformation during SharePoint migration")
    public @interface Config {

        @AttributeDefinition(name = "AI Service URL", description = "OpenAI API endpoint URL")
        String ai_service_url() default "https://api.openai.com/v1/chat/completions";

        @AttributeDefinition(name = "API Key", description = "OpenAI API key")
        String api_key() default "";

        @AttributeDefinition(name = "Vision Model", description = "Model to use for image analysis")
        String vision_model() default "gpt-4o";

        @AttributeDefinition(name = "Text Model", description = "Model to use for text transformation")
        String text_model() default "gpt-4";

        @AttributeDefinition(name = "Max Tokens", description = "Maximum tokens for AI responses")
        int max_tokens() default 2000;

        @AttributeDefinition(name = "Enable Content Cleanup", description = "Enable AI-powered content cleanup")
        boolean enable_cleanup() default true;

        @AttributeDefinition(name = "Enable Metadata Generation", description = "Enable AI metadata generation")
        boolean enable_metadata() default true;

        @AttributeDefinition(name = "Enable Component Mapping", description = "Enable AI component mapping suggestions")
        boolean enable_component_mapping() default true;

        @AttributeDefinition(name = "Enable Redirect Generation", description = "Enable smart redirect mapping generation")
        boolean enable_redirects() default true;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String aiServiceUrl;
    private String apiKey;
    private String visionModel;
    private String textModel;
    private int maxTokens;
    private boolean enableCleanup;
    private boolean enableMetadata;
    private boolean enableComponentMapping;
    private boolean enableRedirects;

    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "<img[^>]+src=['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HEADING_PATTERN = Pattern.compile(
            "<h([1-6])[^>]*>(.*?)</h\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern LIST_PATTERN = Pattern.compile(
            "<(ul|ol)[^>]*>(.*?)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "<table[^>]*>(.*?)</table>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern DIV_PATTERN = Pattern.compile(
            "<div[^>]*>(.*?)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern LINK_PATTERN = Pattern.compile(
            "<a[^>]+href=['\"]([^'\"]+)['\"][^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @PostConstruct
    protected void init() {
        logger.info("AIContentTransformerService initialized");
    }

    @Activate
    protected void activate(final Config config) {
        this.aiServiceUrl = config.ai_service_url();
        this.apiKey = config.api_key();
        this.visionModel = config.vision_model();
        this.textModel = config.text_model();
        this.maxTokens = config.max_tokens();
        this.enableCleanup = config.enable_cleanup();
        this.enableMetadata = config.enable_metadata();
        this.enableComponentMapping = config.enable_component_mapping();
        this.enableRedirects = config.enable_redirects();

        logger.info("AI Content Transformer activated with models: text={}, vision={}", textModel, visionModel);
    }

    @Override
    public String transformContent(String htmlContent, TransformOptions options) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }

        try {
            String prompt = buildTransformationPrompt(htmlContent, options);
            String aiResult = callAIText(prompt);

            if (aiResult != null && !aiResult.isEmpty()) {
                return aiResult;
            }
        } catch (Exception e) {
            logger.error("AI transformation failed: {}", e.getMessage());
        }

        return htmlContent;
    }

    private String buildTransformationPrompt(String htmlContent, TransformOptions options) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Transform the following SharePoint HTML content to AEM-compatible format.\n\n");
        prompt.append("Requirements:\n");
        prompt.append("- Convert to AEM HTL-compatible structure\n");
        
        if (options != null) {
            if (options.getTargetFormat() != null) {
                prompt.append("- Target format: ").append(options.getTargetFormat()).append("\n");
            }
            if (options.isPreserveImages()) {
                prompt.append("- Preserve and optimize image references\n");
            }
            if (options.isCleanupHtml()) {
                prompt.append("- Clean up invalid HTML tags\n");
            }
            if (options.getCustomMappings() != null && !options.getCustomMappings().isEmpty()) {
                prompt.append("- Apply custom component mappings: ");
                options.getCustomMappings().forEach((k, v) -> prompt.append(k).append("->").append(v).append(" "));
                prompt.append("\n");
            }
        }

        prompt.append("\nHTML Content:\n").append(htmlContent);
        prompt.append("\n\nReturn the transformed content in HTML format:");
        
        return prompt.toString();
    }

    @Override
    public String generateImageAltText(byte[] imageData, String imageName) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("API key not configured, returning default alt text");
            return generateDefaultAltText(imageName);
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageData);

            String prompt = "Analyze this image and provide a descriptive alt text for accessibility. " +
                    "Be concise but descriptive. Focus on the main subject and context. " +
                    "Format: Just return the alt text, no explanation.";

            String requestBody = String.format(
                    "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": [{\"type\": \"text\", \"text\": \"%s\"}, {\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64,%s\"}}]}], \"max_tokens\": 100}",
                    visionModel,
                    escapeJson(prompt),
                    base64Image
            );

            return callAI(aiServiceUrl, requestBody, 100);

        } catch (Exception e) {
            logger.error("Failed to generate alt text for image {}: {}", imageName, e.getMessage());
            return generateDefaultAltText(imageName);
        }
    }

    private String generateDefaultAltText(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return "Image";
        }
        String name = imageName.substring(0, imageName.lastIndexOf('.'));
        String camelCase = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        return camelCase.replaceAll("[-_]", " ");
    }

    @Override
    public Map<String, String> generateMetadata(String content, String title) {
        Map<String, String> metadata = new HashMap<>();

        if (!enableMetadata || apiKey == null || apiKey.isEmpty()) {
            return generateBasicMetadata(content, title);
        }

        try {
            String prompt = buildMetadataPrompt(content, title);
            String aiResult = callAIText(prompt);

            if (aiResult != null && !aiResult.isEmpty()) {
                parseMetadataResponse(aiResult, metadata);
            }
        } catch (Exception e) {
            logger.error("AI metadata generation failed: {}", e.getMessage());
            return generateBasicMetadata(content, title);
        }

        if (metadata.isEmpty()) {
            return generateBasicMetadata(content, title);
        }

        return metadata;
    }

    private String buildMetadataPrompt(String content, String title) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following content and generate metadata for AEM page properties.\n\n");
        
        if (title != null && !title.isEmpty()) {
            prompt.append("Page Title: ").append(title).append("\n");
        }
        
        prompt.append("Content excerpt: ").append(content.substring(0, Math.min(content.length(), 2000))).append("\n\n");
        prompt.append("Generate the following metadata (return as JSON object):\n");
        prompt.append("- description: A brief description (max 150 chars)\n");
        prompt.append("- keywords: Comma-separated relevant keywords\n");
        prompt.append("- ogTitle: Open Graph title\n");
        prompt.append("- ogDescription: Open Graph description\n");
        prompt.append("- author: Suggested author name if derivable\n");
        prompt.append("- pageCategory: Content category (e.g., 'blog', 'product', 'landing', 'about')\n");
        
        return prompt.toString();
    }

    private void parseMetadataResponse(String response, Map<String, String> metadata) {
        try {
            JsonNode node = objectMapper.readTree(response);
            metadata.put("description", getTextValue(node, "description"));
            metadata.put("keywords", getTextValue(node, "keywords"));
            metadata.put("ogTitle", getTextValue(node, "ogTitle"));
            metadata.put("ogDescription", getTextValue(node, "ogDescription"));
            metadata.put("author", getTextValue(node, "author"));
            metadata.put("pageCategory", getTextValue(node, "pageCategory"));
        } catch (Exception e) {
            logger.warn("Failed to parse metadata response: {}", e.getMessage());
        }
    }

    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && !fieldNode.isNull()) {
            String value = fieldNode.asText();
            if (value.length() > 150 && "description".equals(field)) {
                value = value.substring(0, 147) + "...";
            }
            return value;
        }
        return null;
    }

    private Map<String, String> generateBasicMetadata(String content, String title) {
        Map<String, String> metadata = new HashMap<>();

        if (title != null && !title.isEmpty()) {
            metadata.put("description", generateDescription(content));
            metadata.put("keywords", extractKeywords(content));
            metadata.put("ogTitle", title);
            metadata.put("ogDescription", generateDescription(content));
        }

        return metadata;
    }

    private String generateDescription(String content) {
        String plainText = content.replaceAll("<[^>]*>", "").trim();
        plainText = plainText.replaceAll("\\s+", " ");
        if (plainText.length() > 150) {
            return plainText.substring(0, 147) + "...";
        }
        return plainText;
    }

    private String extractKeywords(String content) {
        String text = content.replaceAll("<[^>]*>", "").toLowerCase();
        String[] words = text.split("\\W+");
        
        Map<String, Integer> wordCount = new HashMap<>();
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "but", "is", "are", "was", "were",
                "to", "of", "in", "for", "on", "with", "at", "by", "from", "as",
                "this", "that", "these", "those", "it", "its", "be", "have", "has", "had"
        ));

        for (String word : words) {
            if (word.length() > 3 && !stopWords.contains(word)) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
    }

    @Override
    public List<ComponentMapping> suggestComponentMappings(String htmlContent) {
        List<ComponentMapping> mappings = new ArrayList<>();

        if (!enableComponentMapping || htmlContent == null || htmlContent.isEmpty()) {
            return getDefaultComponentMappings();
        }

        try {
            String prompt = buildComponentMappingPrompt(htmlContent);
            String aiResult = callAIText(prompt);

            if (aiResult != null && !aiResult.isEmpty()) {
                parseComponentMappings(aiResult, mappings);
            }
        } catch (Exception e) {
            logger.error("AI component mapping failed: {}", e.getMessage());
        }

        if (mappings.isEmpty()) {
            return getDefaultComponentMappings();
        }

        return mappings;
    }

    private String buildComponentMappingPrompt(String htmlContent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this HTML content and suggest AEM component mappings.\n\n");
        prompt.append("Available AEM components: text, title, image, list, container, ");
        prompt.append("external-link, embed, table, carousel, accordion, form, search, navigation\n\n");
        prompt.append("HTML Content:\n").append(htmlContent.substring(0, Math.min(htmlContent.length(), 3000)));
        prompt.append("\n\nReturn mappings as JSON array with fields: sourceElement, targetComponent, confidence (0-1), reason");
        
        return prompt.toString();
    }

    private void parseComponentMappings(String response, List<ComponentMapping> mappings) {
        try {
            JsonNode node = objectMapper.readTree(response);
            if (node.isArray()) {
                for (JsonNode item : node) {
                    String source = getTextValue(item, "sourceElement");
                    String target = getTextValue(item, "targetComponent");
                    double confidence = 0.8;
                    if (item.has("confidence")) {
                        confidence = item.get("confidence").asDouble();
                    }
                    String reason = getTextValue(item, "reason");
                    
                    if (source != null && target != null) {
                        mappings.add(new ComponentMapping(source, target, confidence, reason));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse component mappings: {}", e.getMessage());
        }
    }

    private List<ComponentMapping> getDefaultComponentMappings() {
        List<ComponentMapping> mappings = new ArrayList<>();
        mappings.add(new ComponentMapping("h1", "aem-playground/components/title", 0.95, "Heading level 1 maps to title component"));
        mappings.add(new ComponentMapping("h2", "aem-playground/components/title", 0.90, "Heading level 2 maps to title component"));
        mappings.add(new ComponentMapping("h3-h6", "aem-playground/components/title", 0.85, "Lower heading levels map to title component"));
        mappings.add(new ComponentMapping("img", "aem-playground/components/image", 0.95, "Image elements map to image component"));
        mappings.add(new ComponentMapping("ul", "aem-playground/components/list", 0.90, "Unordered lists map to list component"));
        mappings.add(new ComponentMapping("ol", "aem-playground/components/list", 0.90, "Ordered lists map to list component"));
        mappings.add(new ComponentMapping("table", "aem-playground/components/table", 0.85, "Tables map to table component"));
        mappings.add(new ComponentMapping("a", "aem-playground/components/external-link", 0.90, "Links map to external-link component"));
        mappings.add(new ComponentMapping("p", "aem-playground/components/text", 0.80, "Paragraphs map to text component"));
        return mappings;
    }

    @Override
    public String cleanupAndOptimize(String htmlContent, CleanupOptions options) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }

        String result = htmlContent;

        if (enableCleanup && apiKey != null && !apiKey.isEmpty()) {
            try {
                String prompt = buildCleanupPrompt(htmlContent, options);
                String aiResult = callAIText(prompt);

                if (aiResult != null && !aiResult.isEmpty()) {
                    result = aiResult;
                }
            } catch (Exception e) {
                logger.error("AI cleanup failed: {}", e.getMessage());
            }
        }

        return applyBasicCleanup(result, options);
    }

    private String buildCleanupPrompt(String htmlContent, CleanupOptions options) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Clean up and optimize the following HTML content for AEM migration.\n\n");
        
        if (options != null) {
            prompt.append("Apply the following transformations:\n");
            if (options.isRemoveEmptyParagraphs()) {
                prompt.append("- Remove empty or whitespace-only paragraphs\n");
            }
            if (options.isNormalizeWhitespace()) {
                prompt.append("- Normalize whitespace and line breaks\n");
            }
            if (options.isRemoveInvalidTags()) {
                prompt.append("- Remove invalid or deprecated HTML tags\n");
            }
            if (options.isFixEncoding()) {
                prompt.append("- Fix encoding issues\n");
            }
        }
        
        prompt.append("\nOriginal HTML:\n").append(htmlContent);
        prompt.append("\n\nReturn the cleaned HTML:");
        
        return prompt.toString();
    }

    private String applyBasicCleanup(String htmlContent, CleanupOptions options) {
        String result = htmlContent;

        if (options == null || options.isRemoveEmptyParagraphs()) {
            result = result.replaceAll("<p[^>]*>\\s*</p>", "");
            result = result.replaceAll("<p[^>]*>&nbsp;</p>", "");
        }

        if (options == null || options.isNormalizeWhitespace()) {
            result = result.replaceAll("\\s+", " ");
        }

        if (options == null || options.isRemoveInvalidTags()) {
            result = result.replaceAll("<span[^>]*>\\s*</span>", "");
            result = result.replaceAll("<div[^>]*>\\s*</div>", "");
        }

        return result.trim();
    }

    @Override
    public List<RedirectMapping> generateRedirectMappings(List<String> sourceUrls, String targetBaseUrl) {
        List<RedirectMapping> redirects = new ArrayList<>();

        if (!enableRedirects || sourceUrls == null || sourceUrls.isEmpty()) {
            return redirects;
        }

        try {
            String prompt = buildRedirectPrompt(sourceUrls, targetBaseUrl);
            String aiResult = callAIText(prompt);

            if (aiResult != null && !aiResult.isEmpty()) {
                parseRedirectMappings(aiResult, redirects, targetBaseUrl);
            }
        } catch (Exception e) {
            logger.error("AI redirect mapping failed: {}", e.getMessage());
        }

        if (redirects.isEmpty()) {
            return generateBasicRedirectMappings(sourceUrls, targetBaseUrl);
        }

        return redirects;
    }

    private String buildRedirectPrompt(List<String> sourceUrls, String targetBaseUrl) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate redirect mappings from SharePoint URLs to AEM URLs.\n\n");
        prompt.append("Target base URL: ").append(targetBaseUrl).append("\n\n");
        prompt.append("Source URLs:\n");
        for (String url : sourceUrls) {
            prompt.append("- ").append(url).append("\n");
        }
        prompt.append("\n\nAnalyze URL patterns and suggest logical redirects. ");
        prompt.append("Return as JSON array with fields: sourceUrl, targetUrl, priority (1-10)");
        
        return prompt.toString();
    }

    private void parseRedirectMappings(String response, List<RedirectMapping> redirects, String targetBaseUrl) {
        try {
            JsonNode node = objectMapper.readTree(response);
            if (node.isArray()) {
                for (JsonNode item : node) {
                    String source = getTextValue(item, "sourceUrl");
                    String target = getTextValue(item, "targetUrl");
                    int priority = 5;
                    if (item.has("priority")) {
                        priority = item.get("priority").asInt();
                    }
                    
                    if (source != null && target != null) {
                        redirects.add(new RedirectMapping(source, target, priority));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse redirect mappings: {}", e.getMessage());
        }
    }

    private List<RedirectMapping> generateBasicRedirectMappings(List<String> sourceUrls, String targetBaseUrl) {
        List<RedirectMapping> redirects = new ArrayList<>();
        
        for (String sourceUrl : sourceUrls) {
            String path = extractPath(sourceUrl);
            String targetPath = targetBaseUrl + "/" + sanitizePath(path);
            
            int priority = path.contains("news") ? 8 : (path.contains("blog") ? 7 : 5);
            
            redirects.add(new RedirectMapping(sourceUrl, targetPath, priority));
        }
        
        return redirects;
    }

    private String extractPath(String url) {
        try {
            if (url.startsWith("http")) {
                int hostEnd = url.indexOf("/", 8);
                if (hostEnd > 0) {
                    return url.substring(hostEnd);
                }
            }
            if (url.startsWith("/")) {
                return url;
            }
        } catch (Exception e) {
        }
        return url;
    }

    private String sanitizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String sanitized = path.toLowerCase()
                .replaceAll("[^a-z0-9/\\-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("/+", "/");
        
        if (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        
        return sanitized;
    }

    private String callAIText(String prompt) {
        String requestBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": %d}",
                textModel,
                escapeJson(prompt),
                maxTokens
        );

        return callAI(aiServiceUrl, requestBody, maxTokens);
    }

    private String callAI(String url, String requestBody, int tokens) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("API key not configured");
            return null;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream responseStream = connection.getInputStream()) {
                    JsonNode rootNode = objectMapper.readTree(responseStream);
                    JsonNode choices = rootNode.get("choices");
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        return choices.get(0).get("message").get("content").asText();
                    }
                }
            } else {
                logger.warn("AI API returned response code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("AI API call failed: {}", e.getMessage());
        }

        return null;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public boolean isEnableCleanup() {
        return enableCleanup;
    }

    public boolean isEnableMetadata() {
        return enableMetadata;
    }

    public boolean isEnableComponentMapping() {
        return enableComponentMapping;
    }

    public boolean isEnableRedirects() {
        return enableRedirects;
    }
}