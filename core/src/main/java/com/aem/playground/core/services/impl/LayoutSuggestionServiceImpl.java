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

@Component(service = LayoutSuggestionService.class)
@Designate(ocd = LayoutSuggestionServiceConfig.class)
public class LayoutSuggestionServiceImpl implements LayoutSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(LayoutSuggestionServiceImpl.class);

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";

    private static final String SYSTEM_PROMPT = "You are an AI layout expert for Adobe Experience Manager (AEM). " +
            "Analyze page content and suggest optimal layouts, component arrangements, and page structures.";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, LayoutSuggestion> suggestionCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private boolean enableCache;
    private int cacheSize;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(LayoutSuggestionServiceConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), DEFAULT_API_URL);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        log.info("LayoutSuggestionService activated with URL: {}", serviceUrl);
    }

    @Override
    public LayoutSuggestion suggestLayout(PageContentAnalysis analysis) {
        return suggestLayout(analysis, null);
    }

    @Override
    public LayoutSuggestion suggestLayout(PageContentAnalysis analysis, String templateType) {
        if (analysis == null || StringUtils.isBlank(analysis.getPagePath())) {
            return createErrorSuggestion("Page content analysis is required");
        }

        try {
            String cacheKey = generateCacheKey(analysis, templateType);
            if (enableCache) {
                LayoutSuggestion cached = suggestionCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for layout suggestion: {}", cacheKey);
                    return cached;
                }
            }

            String prompt = buildLayoutPrompt(analysis, templateType);
            LayoutSuggestion suggestion = executeLayoutSuggestion(analysis, prompt, templateType);

            if (enableCache && suggestion != null) {
                suggestionCache.put(cacheKey, suggestion);
                evictOldCacheEntries();
            }

            return suggestion;
        } catch (Exception e) {
            log.error("Error generating layout suggestion: {}", e.getMessage());
            return createErrorSuggestion(e.getMessage());
        }
    }

    @Override
    public LayoutSuggestion suggestLayoutFromPrompt(PageContentAnalysis analysis, String userPrompt) {
        if (analysis == null || StringUtils.isBlank(analysis.getPagePath())) {
            return createErrorSuggestion("Page content analysis is required");
        }

        if (StringUtils.isBlank(userPrompt)) {
            return suggestLayout(analysis);
        }

        try {
            String prompt = buildLayoutPrompt(analysis, null) + "\n\nAdditional user requirements: " + userPrompt;
            return executeLayoutSuggestion(analysis, prompt, null);
        } catch (Exception e) {
            log.error("Error generating layout suggestion from prompt: {}", e.getMessage());
            return createErrorSuggestion(e.getMessage());
        }
    }

    @Override
    public PageContentAnalysis analyzePageContent(String pagePath) {
        return analyzePageContent(pagePath, false);
    }

    @Override
    public PageContentAnalysis analyzePageContent(String pagePath, boolean includeChildren) {
        if (StringUtils.isBlank(pagePath)) {
            return null;
        }

        PageContentAnalysis analysis = new PageContentAnalysis();
        analysis.setPagePath(pagePath);
        analysis.setPageTitle(extractPageTitle(pagePath));
        analysis.setPageDescription(extractPageDescription(pagePath));
        analysis.setHeadings(extractHeadings(pagePath));
        analysis.setParagraphs(extractParagraphs(pagePath));
        analysis.setKeywords(extractKeywords(pagePath));
        analysis.setImages(extractImages(pagePath));
        analysis.setLinks(extractLinks(pagePath));
        analysis.setContentType(determineContentType(analysis));
        analysis.setTargetAudience(determineTargetAudience(analysis));

        return analysis;
    }

    @Override
    public boolean validateLayoutSuggestion(LayoutSuggestion suggestion) {
        if (suggestion == null) {
            return false;
        }

        if (StringUtils.isBlank(suggestion.getPagePath())) {
            return false;
        }

        if (StringUtils.isBlank(suggestion.getLayoutType())) {
            return false;
        }

        if (suggestion.getComponents() == null || suggestion.getComponents().isEmpty()) {
            return false;
        }

        for (ComponentSuggestion comp : suggestion.getComponents()) {
            if (StringUtils.isBlank(comp.getComponentResourceType())) {
                return false;
            }
        }

        return true;
    }

    private String buildLayoutPrompt(PageContentAnalysis analysis, String templateType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following page content and suggest an optimal AEM page layout.\n\n");

        if (StringUtils.isNotBlank(analysis.getPageTitle())) {
            prompt.append("Page Title: ").append(analysis.getPageTitle()).append("\n");
        }

        if (StringUtils.isNotBlank(analysis.getPageDescription())) {
            prompt.append("Page Description: ").append(analysis.getPageDescription()).append("\n");
        }

        if (analysis.getKeywords() != null && !analysis.getKeywords().isEmpty()) {
            prompt.append("Keywords: ").append(String.join(", ", analysis.getKeywords())).append("\n");
        }

        if (analysis.getHeadings() != null && !analysis.getHeadings().isEmpty()) {
            prompt.append("Headings: ").append(String.join(", ", analysis.getHeadings())).append("\n");
        }

        if (StringUtils.isNotBlank(analysis.getContentType())) {
            prompt.append("Content Type: ").append(analysis.getContentType()).append("\n");
        }

        if (StringUtils.isNotBlank(templateType)) {
            prompt.append("Template Type: ").append(templateType).append("\n");
        }

        prompt.append("\nProvide a detailed layout suggestion including:");
        prompt.append("\n1. Layout type (hero, two-column, three-column, sidebar, etc.)");
        prompt.append("\n2. Component recommendations with resource types (text, image, title, etc.)");
        prompt.append("\n3. Component positions and containers");
        prompt.append("\n4. Responsive layout suggestions for mobile, tablet, and desktop");
        prompt.append("\n5. A/B test opportunities");
        prompt.append("\n6. Experience fragment integration suggestions");

        return prompt.toString();
    }

    private LayoutSuggestion executeLayoutSuggestion(PageContentAnalysis analysis, String prompt, String templateType) {
        try {
            String requestBody = buildRequestBody(prompt);

            String response = executePostRequest(serviceUrl, requestBody);

            if (StringUtils.isBlank(response)) {
                return createDefaultSuggestion(analysis, templateType);
            }

            return parseLayoutResponse(analysis, response, templateType);

        } catch (Exception e) {
            log.warn("Error calling AI service, returning default suggestion: {}", e.getMessage());
            return createDefaultSuggestion(analysis, templateType);
        }
    }

    private String buildRequestBody(String prompt) throws IOException {
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

        return objectMapper.writeValueAsString(request);
    }

    private String executePostRequest(String endpoint, String body) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

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

    private LayoutSuggestion parseLayoutResponse(PageContentAnalysis analysis, String response, String templateType) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choices = rootNode.get("choices");

            if (choices == null || !choices.isArray() || choices.size() == 0) {
                return createDefaultSuggestion(analysis, templateType);
            }

            String content = choices.get(0).get("message").get("content").asText();
            return createSuggestionFromAIResponse(analysis, content, templateType);

        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            return createDefaultSuggestion(analysis, templateType);
        }
    }

    private LayoutSuggestion createSuggestionFromAIResponse(PageContentAnalysis analysis, String content, String templateType) {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath(analysis.getPagePath());
        suggestion.setConfidenceScore(0.85);

        suggestion.setLayoutType(determineLayoutType(analysis, content, templateType));
        suggestion.setComponents(generateComponentSuggestions(analysis));
        suggestion.setResponsiveLayouts(generateResponsiveLayouts());
        suggestion.setAbTestSuggestions(generateABTestSuggestions(analysis));
        suggestion.setExperienceFragmentSuggestions(generateExperienceFragmentSuggestions(analysis));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("aiGenerated", true);
        metadata.put("contentLength", content.length());
        metadata.put("timestamp", System.currentTimeMillis());
        suggestion.setMetadata(metadata);

        return suggestion;
    }

    private LayoutSuggestion createDefaultSuggestion(PageContentAnalysis analysis, String templateType) {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath(analysis.getPagePath());
        suggestion.setConfidenceScore(0.75);

        suggestion.setLayoutType(determineDefaultLayoutType(analysis, templateType));
        suggestion.setComponents(generateComponentSuggestions(analysis));
        suggestion.setResponsiveLayouts(generateResponsiveLayouts());
        suggestion.setAbTestSuggestions(generateABTestSuggestions(analysis));
        suggestion.setExperienceFragmentSuggestions(generateExperienceFragmentSuggestions(analysis));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("aiGenerated", false);
        metadata.put("defaultSuggestion", true);
        metadata.put("timestamp", System.currentTimeMillis());
        suggestion.setMetadata(metadata);

        return suggestion;
    }

    private LayoutSuggestion createErrorSuggestion(String error) {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setLayoutType("error");
        suggestion.setConfidenceScore(0.0);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        suggestion.setMetadata(metadata);

        return suggestion;
    }

    private String determineLayoutType(PageContentAnalysis analysis, String content, String templateType) {
        if (StringUtils.isNotBlank(templateType)) {
            return templateType;
        }

        if (analysis.getContentType() != null) {
            String ct = analysis.getContentType().toLowerCase();
            if (ct.contains("landing") || ct.contains("promotional")) {
                return "hero";
            } else if (ct.contains("blog") || ct.contains("article")) {
                return "two-column";
            } else if (ct.contains("product") || ct.contains("catalog")) {
                return "grid";
            } else if (ct.contains("contact") || ct.contains("form")) {
                return "centered";
            }
        }

        return "responsive";
    }

    private String determineDefaultLayoutType(PageContentAnalysis analysis, String templateType) {
        if (StringUtils.isNotBlank(templateType)) {
            return templateType;
        }

        return "responsive";
    }

    private List<ComponentSuggestion> generateComponentSuggestions(PageContentAnalysis analysis) {
        List<ComponentSuggestion> components = new ArrayList<>();

        String contentType = analysis.getContentType();
        if (contentType == null) {
            contentType = "generic";
        }
        contentType = contentType.toLowerCase();

        int position = 0;

        if (contentType.contains("landing") || contentType.contains("promotional")) {
            ComponentSuggestion hero = new ComponentSuggestion();
            hero.setComponentResourceType("aem-playground/components/hero");
            hero.setComponentName("Hero");
            hero.setPosition(position++);
            hero.setContainer("main");
            hero.setRelevanceScore(0.95);
            components.add(hero);
        } else if (contentType.contains("blog") || contentType.contains("article")) {
            ComponentSuggestion title = new ComponentSuggestion();
            title.setComponentResourceType("aem-playground/components/title");
            title.setComponentName("Title");
            title.setPosition(position++);
            title.setContainer("main");
            title.setRelevanceScore(0.9);
            components.add(title);

            ComponentSuggestion text = new ComponentSuggestion();
            text.setComponentResourceType("aem-playground/components/text");
            text.setComponentName("Text");
            text.setPosition(position++);
            text.setContainer("main");
            text.setRelevanceScore(0.85);
            components.add(text);
        }

        if (!contentType.contains("landing") && !contentType.contains("hero")) {
            ComponentSuggestion title = new ComponentSuggestion();
            title.setComponentResourceType("aem-playground/components/title");
            title.setComponentName("Title");
            title.setPosition(position++);
            title.setContainer("main");
            title.setRelevanceScore(0.8);
            components.add(title);
        }

        ComponentSuggestion text = new ComponentSuggestion();
        text.setComponentResourceType("aem-playground/components/text");
        text.setComponentName("Text");
        text.setPosition(position++);
        text.setContainer("main");
        text.setRelevanceScore(0.7);
        components.add(text);

        if (analysis.getImages() != null && !analysis.getImages().isEmpty()) {
            ComponentSuggestion image = new ComponentSuggestion();
            image.setComponentResourceType("aem-playground/components/image");
            image.setComponentName("Image");
            image.setPosition(position++);
            image.setContainer("main");
            image.setRelevanceScore(0.75);
            components.add(image);
        }

        ComponentSuggestion navigation = new ComponentSuggestion();
        navigation.setComponentResourceType("aem-playground/components/navigation");
        navigation.setComponentName("Navigation");
        navigation.setPosition(position++);
        navigation.setContainer("header");
        navigation.setRelevanceScore(0.9);
        components.add(navigation);

        ComponentSuggestion footer = new ComponentSuggestion();
        footer.setComponentResourceType("aem-playground/components/footer");
        footer.setComponentName("Footer");
        footer.setPosition(position++);
        footer.setContainer("footer");
        footer.setRelevanceScore(0.85);
        components.add(footer);

        return components;
    }

    private List<ResponsiveLayout> generateResponsiveLayouts() {
        List<ResponsiveLayout> layouts = new ArrayList<>();

        ResponsiveLayout mobile = new ResponsiveLayout();
        mobile.setBreakpoint("mobile");
        mobile.setMinWidth(0);
        mobile.setMaxWidth(767);
        mobile.setGridColumns("1");
        mobile.setSpacing("8px");
        layouts.add(mobile);

        ResponsiveLayout tablet = new ResponsiveLayout();
        tablet.setBreakpoint("tablet");
        tablet.setMinWidth(768);
        tablet.setMaxWidth(1199);
        tablet.setGridColumns("2");
        tablet.setSpacing("16px");
        layouts.add(tablet);

        ResponsiveLayout desktop = new ResponsiveLayout();
        desktop.setBreakpoint("desktop");
        desktop.setMinWidth(1200);
        desktop.setMaxWidth(Integer.MAX_VALUE);
        desktop.setGridColumns("12");
        desktop.setSpacing("24px");
        layouts.add(desktop);

        return layouts;
    }

    private List<ABTestSuggestion> generateABTestSuggestions(PageContentAnalysis analysis) {
        List<ABTestSuggestion> suggestions = new ArrayList<>();

        ABTestSuggestion layoutTest = new ABTestSuggestion();
        layoutTest.setTestName("Layout Comparison Test");
        layoutTest.setTestId("layout-test-" + System.currentTimeMillis());
        layoutTest.setVariants(Arrays.asList("variant-a", "variant-b"));
        layoutTest.setMetricToTrack("conversion-rate");
        layoutTest.setEstimatedTrafficPercentage(50.0);
        layoutTest.setSuggestedDurationDays(14);
        layoutTest.setMinimumSampleSize(1000);
        suggestions.add(layoutTest);

        ABTestSuggestion ctaTest = new ABTestSuggestion();
        ctaTest.setTestName("CTA Button Test");
        ctaTest.setTestId("cta-test-" + System.currentTimeMillis());
        ctaTest.setVariants(Arrays.asList("control", "variant-a", "variant-b"));
        ctaTest.setMetricToTrack("click-through-rate");
        ctaTest.setEstimatedTrafficPercentage(33.0);
        ctaTest.setSuggestedDurationDays(7);
        ctaTest.setMinimumSampleSize(500);
        suggestions.add(ctaTest);

        return suggestions;
    }

    private List<ExperienceFragmentSuggestion> generateExperienceFragmentSuggestions(PageContentAnalysis analysis) {
        List<ExperienceFragmentSuggestion> suggestions = new ArrayList<>();

        ExperienceFragmentSuggestion header = new ExperienceFragmentSuggestion();
        header.setFragmentPath("/etc/experience-fragments/header/master");
        header.setFragmentName("Master Header");
        header.setFragmentGroup("navigation");
        header.setVariation("master");
        suggestions.add(header);

        ExperienceFragmentSuggestion footer = new ExperienceFragmentSuggestion();
        footer.setFragmentPath("/etc/experience-fragments/footer/master");
        footer.setFragmentName("Master Footer");
        footer.setFragmentGroup("navigation");
        footer.setVariation("master");
        suggestions.add(footer);

        return suggestions;
    }

    private String generateCacheKey(PageContentAnalysis analysis, String templateType) {
        StringBuilder key = new StringBuilder();
        key.append(analysis.getPagePath());
        if (analysis.getKeywords() != null) {
            key.append(String.join(",", analysis.getKeywords()));
        }
        if (templateType != null) {
            key.append(templateType);
        }
        return Integer.toHexString(key.toString().hashCode());
    }

    private void evictOldCacheEntries() {
        if (suggestionCache.size() > cacheSize) {
            int toRemove = suggestionCache.size() - cacheSize;
            Iterator<String> iter = suggestionCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                suggestionCache.remove(iter.next());
            }
        }
    }

    private String extractPageTitle(String pagePath) {
        return "Sample Page Title";
    }

    private String extractPageDescription(String pagePath) {
        return "Sample page description";
    }

    private List<String> extractHeadings(String pagePath) {
        return Arrays.asList("Welcome", "About Us", "Services");
    }

    private List<String> extractParagraphs(String pagePath) {
        return Arrays.asList("This is sample paragraph 1.", "This is sample paragraph 2.");
    }

    private List<String> extractKeywords(String pagePath) {
        return Arrays.asList("AEM", "Adobe", "Content Management");
    }

    private List<String> extractImages(String pagePath) {
        return Arrays.asList("/content/dam/image1.jpg", "/content/dam/image2.jpg");
    }

    private List<String> extractLinks(String pagePath) {
        return Arrays.asList("/content/page1.html", "/content/page2.html");
    }

    private String determineContentType(PageContentAnalysis analysis) {
        if (analysis.getKeywords() == null || analysis.getKeywords().isEmpty()) {
            return "generic";
        }

        for (String keyword : analysis.getKeywords()) {
            String kw = keyword.toLowerCase();
            if (kw.contains("product") || kw.contains("shop")) {
                return "product";
            } else if (kw.contains("blog") || kw.contains("news")) {
                return "blog";
            } else if (kw.contains("landing") || kw.contains("promo")) {
                return "landing";
            }
        }

        return "generic";
    }

    private String determineTargetAudience(PageContentAnalysis analysis) {
        if (analysis.getKeywords() == null || analysis.getKeywords().isEmpty()) {
            return "general";
        }

        for (String keyword : analysis.getKeywords()) {
            String kw = keyword.toLowerCase();
            if (kw.contains("b2b")) {
                return "business";
            } else if (kw.contains("b2c") || kw.contains("consumer")) {
                return "consumer";
            }
        }

        return "general";
    }
}