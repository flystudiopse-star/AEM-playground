package com.aem.playground.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
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

@Component(service = TemplateGenerator.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = TemplateGeneratorImpl.Config.class)
public class TemplateGeneratorImpl implements TemplateGenerator {

    @ObjectClassDefinition(name = "AI Template Generator Configuration",
            description = "Configuration for AI-powered template generation")
    public @interface Config {

        @AttributeDefinition(name = "AI Service URL", description = "OpenAI API endpoint")
        String ai_service_url() default "https://api.openai.com/v1/chat/completions";

        @AttributeDefinition(name = "AI Model", description = "AI model to use for content generation")
        String ai_model() default "gpt-3.5-turbo";

        @AttributeDefinition(name = "Max Tokens", description = "Maximum tokens for AI response")
        int ai_max_tokens() default 1000;

        @AttributeDefinition(name = "Template Storage Path", description = "JCR path for storing generated templates")
        String template_storage_path() default "/conf/aem-playground/settings/wcm/templates";

        @AttributeDefinition(name = "Enable Debug Logging", description = "Enable debug logging")
        boolean enable_debug() default false;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String aiServiceUrl;
    private String aiModel;
    private int aiMaxTokens;
    private String templateStoragePath;
    private boolean enableDebug;

    private static final Map<String, List<String>> TEMPLATE_COMPONENTS = new HashMap<>();
    private static final Map<String, Map<String, String>> TEMPLATE_POLICIES = new HashMap<>();

    static {
        TEMPLATE_COMPONENTS.put("landing", Arrays.asList(
                "hero", "features", "testimonials", "cta", "footer"
        ));
        TEMPLATE_COMPONENTS.put("blog", Arrays.asList(
                "header", "content", "sidebar", "comments", "footer"
        ));
        TEMPLATE_COMPONENTS.put("product", Arrays.asList(
                "product-hero", "product-details", "specifications", "reviews", "related-products"
        ));
        TEMPLATE_COMPONENTS.put("contact", Arrays.asList(
                "contact-header", "contact-form", "map", "contact-info"
        ));
    }

    static {
        TEMPLATE_POLICIES.put("contact", Map.of(
                "root", "aem-playground/components/container/policy_contact",
                "contact-form", "aem-playground/components/form/container/policy_contact"
        ));
    }

    @PostConstruct
    protected void init() {
        logger.info("TemplateGeneratorImpl initialized");
    }

    @Activate
    protected void activate(final Config config) {
        this.aiServiceUrl = config.ai_service_url();
        this.aiModel = config.ai_model();
        this.aiMaxTokens = config.ai_max_tokens();
        this.templateStoragePath = config.template_storage_path();
        this.enableDebug = config.enable_debug();

        logger.info("Template generator activated with AI service: {}", aiServiceUrl);
    }

    @Override
    public TemplateResult generateTemplate(TemplateRequest request) throws Exception {
        TemplateResult result = new TemplateResult();
        List<String> errors = new ArrayList<>();

        String templateType = request.getTemplateType();
        if (templateType == null || templateType.isEmpty()) {
            errors.add("Template type is required");
            result.setErrors(errors);
            return result;
        }

        String templateName = generateTemplateName(templateType, request.getPageTitle());
        result.setTemplateName(templateName);
        result.setTemplatePath(templateStoragePath + "/" + templateName);

        Map<String, Object> structure = generateTemplateStructure(templateType);
        result.setStructure(structure);

        List<TemplateSection> sections = new ArrayList<>();
        if (request.isGenerateAiContent()) {
            String aiUrl = request.getAiServiceUrl() != null ? request.getAiServiceUrl() : aiServiceUrl;
            sections = generateContentSectionsWithAI(templateType, request.getPageDescription(), aiUrl);
        } else {
            sections = generateContentSections(templateType, request.getPageDescription());
        }
        result.setSections(sections);

        Map<String, String> policies = generatePolicyMappings(templateType);
        result.setPolicyMappings(policies);

        try {
            byte[] thumbnail = generatePreviewThumbnail(templateType);
            result.setPreviewThumbnail(thumbnail);
        } catch (Exception e) {
            logger.warn("Failed to generate preview thumbnail: {}", e.getMessage());
        }

        result.setErrors(errors.isEmpty() ? null : errors);
        return result;
    }

    @Override
    public List<TemplateSection> generateContentSections(String templateType, String pageDescription) throws Exception {
        List<TemplateSection> sections = new ArrayList<>();
        List<String> componentList = TEMPLATE_COMPONENTS.getOrDefault(templateType, Collections.emptyList());

        for (String component : componentList) {
            TemplateSection section = new TemplateSection();
            section.setSectionName(component);
            section.setComponentType("aem-playground/components/" + component);
            section.setProperties(generateDefaultProperties(templateType, component));
            section.setContent(generateDefaultContent(templateType, component, pageDescription));
            sections.add(section);
        }

        return sections;
    }

    public List<TemplateSection> generateContentSectionsWithAI(String templateType, String description, String aiUrl) throws Exception {
        List<TemplateSection> sections = generateContentSections(templateType, description);

        String prompt = buildContentPrompt(templateType, description);
        String aiContent = callAI(prompt, aiUrl);

        if (aiContent != null && !aiContent.isEmpty()) {
            parseAIResponse(sections, aiContent);
        }

        return sections;
    }

    @Override
    public Map<String, String> generatePolicyMappings(String templateType) {
        return TEMPLATE_POLICIES.getOrDefault(templateType, new HashMap<>());
    }

    @Override
    public byte[] generatePreviewThumbnail(String templateType) throws Exception {
        String svg = generateThumbnailSvg(templateType);
        return svg.getBytes(StandardCharsets.UTF_8);
    }

    private String generateTemplateName(String templateType, String pageTitle) {
        String sanitizedTitle = pageTitle != null ? pageTitle.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase() : templateType;
        return sanitizedTitle + "-" + System.currentTimeMillis();
    }

    private Map<String, Object> generateTemplateStructure(String templateType) {
        Map<String, Object> structure = new HashMap<>();
        structure.put("jcr:primaryType", "cq:Page");
        
        Map<String, Object> jcrContent = new HashMap<>();
        jcrContent.put("jcr:primaryType", "cq:PageContent");
        jcrContent.put("sling:resourceType", "aem-playground/components/page");
        jcrContent.put("cq:template", templateStoragePath + "/" + templateType);

        Map<String, Object> root = new HashMap<>();
        root.put("jcr:primaryType", "nt:unstructured");
        root.put("sling:resourceType", "aem-playground/components/container");
        root.put("layout", "responsiveGrid");

        List<String> components = TEMPLATE_COMPONENTS.getOrDefault(templateType, Collections.emptyList());
        for (String component : components) {
            Map<String, Object> componentNode = new HashMap<>();
            componentNode.put("jcr:primaryType", "nt:unstructured");
            componentNode.put("sling:resourceType", "aem-playground/components/" + component);
            componentNode.put("editable", "{Boolean}true");
            root.put(component, componentNode);
        }

        jcrContent.put("root", root);
        structure.put("jcr:content", jcrContent);

        return structure;
    }

    private Map<String, String> generateDefaultProperties(String templateType, String component) {
        Map<String, String> props = new HashMap<>();
        
        switch (component) {
            case "hero":
                props.put("layout", "full-width");
                props.put("showTitle", "true");
                props.put("showSubtitle", "true");
                break;
            case "features":
                props.put("columns", "3");
                props.put("layout", "grid");
                break;
            case "contact-form":
                props.put("formAction", "/bin/servlet");
                props.put("formMethod", "POST");
                break;
            default:
                props.put("layout", "responsive");
        }

        return props;
    }

    private String generateDefaultContent(String templateType, String component, String pageDescription) {
        switch (component) {
            case "hero":
                return pageDescription != null ? pageDescription : "Welcome to Our Site";
            case "features":
                return "Feature section content";
            case "contact-form":
                return "Contact form for inquiries";
            default:
                return component + " content for " + templateType + " template";
        }
    }

    private String buildContentPrompt(String templateType, String description) {
        return String.format(
            "Generate content sections for an AEM %s template. Page description: %s. " +
            "Provide JSON with sections having: name, title, description, and content for each section.",
            templateType, description != null ? description : "general page"
        );
    }

    private String callAI(String prompt, String aiUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(aiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": %d}",
                aiModel, escapeJson(prompt), aiMaxTokens
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream responseStream = connection.getInputStream()) {
                    return parseAIResponse(responseStream);
                }
            } else {
                logger.warn("AI service returned response code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("Failed to call AI service: {}", e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private String parseAIResponse(InputStream inputStream) throws Exception {
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String json = response.toString();
        int contentStart = json.indexOf("\"content\":\"") + 11;
        int contentEnd = json.indexOf("\"", contentStart);
        if (contentStart > 10 && contentEnd > contentStart) {
            String content = json.substring(contentStart, contentEnd);
            return content.replace("\\n", "\n").replace("\\\"", "\"");
        }
        return null;
    }

    private void parseAIResponse(List<TemplateSection> sections, String aiContent) {
        if (aiContent == null || aiContent.isEmpty()) return;

        for (TemplateSection section : sections) {
            String sectionKey = section.getSectionName().toLowerCase();
            if (aiContent.toLowerCase().contains(sectionKey)) {
                String sectionContent = extractSectionContent(aiContent, sectionKey);
                if (sectionContent != null) {
                    section.setContent(sectionContent);
                }
            }
        }
    }

    private String extractSectionContent(String aiContent, String sectionKey) {
        int startIndex = aiContent.toLowerCase().indexOf(sectionKey);
        if (startIndex == -1) return null;
        
        startIndex = aiContent.indexOf("\"content\":", startIndex);
        if (startIndex == -1) return null;
        
        startIndex += 10;
        while (startIndex < aiContent.length() && (aiContent.charAt(startIndex) == ' ' || aiContent.charAt(startIndex) == '\"')) {
            startIndex++;
        }
        
        int endIndex = startIndex;
        while (endIndex < aiContent.length() && aiContent.charAt(endIndex) != '"') {
            endIndex++;
        }
        
        return aiContent.substring(startIndex, endIndex);
    }

    private String generateThumbnailSvg(String templateType) {
        String backgroundColor = "#4A90D9";
        String templateName = templateType.substring(0, 1).toUpperCase() + templateType.substring(1);
        
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
            "  <rect width=\"400\" height=\"300\" fill=\"%s\"/>\n" +
            "  <rect x=\"20\" y=\"20\" width=\"360\" height=\"260\" fill=\"white\" rx=\"10\"/>\n" +
            "  <rect x=\"40\" y=\"40\" width=\"320\" height=\"40\" fill=\"#E0E0E0\" rx=\"5\"/>\n" +
            "  <rect x=\"40\" y=\"90\" width=\"180\" height=\"100\" fill=\"#E0E0E0\" rx=\"5\"/>\n" +
            "  <rect x=\"230\" y=\"90\" width=\"130\" height=\"45\" fill=\"#E0E0E0\" rx=\"5\"/>\n" +
            "  <rect x=\"230\" y=\"145\" width=\"130\" height=\"45\" fill=\"#E0E0E0\" rx=\"5\"/>\n" +
            "  <rect x=\"40\" y=\"200\" width=\"320\" height=\"30\" fill=\"#E0E0E0\" rx=\"5\"/>\n" +
            "  <text x=\"200\" y=\"270\" font-family=\"Arial\" font-size=\"14\" fill=\"#666\" text-anchor=\"middle\">%s Template</text>\n" +
            "</svg>",
            backgroundColor, templateName
        );
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}