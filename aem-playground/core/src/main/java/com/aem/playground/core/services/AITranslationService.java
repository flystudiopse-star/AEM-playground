package com.aem.playground.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = TranslationService.class)
@Designate(ocd = AITranslationService.Config.class)
public class AITranslationService implements TranslationService {

    @ObjectClassDefinition(name = "AI Translation Service Configuration",
            description = "Configuration for AI-powered content translation using OpenAI")
    public @interface Config {

        @AttributeDefinition(name = "AI Service URL", description = "OpenAI API endpoint URL")
        String ai_service_url() default "https://api.openai.com/v1/chat/completions";

        @AttributeDefinition(name = "API Key", description = "OpenAI API key")
        String api_key() default "";

        @AttributeDefinition(name = "Translation Model", description = "Model to use for translation")
        String translation_model() default "gpt-4";

        @AttributeDefinition(name = "Max Tokens", description = "Maximum tokens for AI responses")
        int max_tokens() default 4000;

        @AttributeDefinition(name = "Temperature", description = "Temperature for translation consistency")
        float temperature() default 0.3f;

        @AttributeDefinition(name = "Enable Cache", description = "Enable translation caching")
        boolean enable_cache() default true;

        @AttributeDefinition(name = "Cache Size", description = "Maximum number of cached translations")
        int cache_size() default 500;
    }

    private static final Logger log = LoggerFactory.getLogger(AITranslationService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedTranslation> translationCache = new ConcurrentHashMap<>();

    private String aiServiceUrl;
    private String apiKey;
    private String translationModel;
    private int maxTokens;
    private float temperature;
    private boolean enableCache;
    private int cacheSize;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(Config config) {
        this.aiServiceUrl = config.ai_service_url();
        this.apiKey = config.api_key();
        this.translationModel = config.translation_model();
        this.maxTokens = config.max_tokens();
        this.temperature = config.temperature();
        this.enableCache = config.enable_cache();
        this.cacheSize = config.cache_size();

        log.info("AI Translation Service activated with model: {}", translationModel);
    }

    @Override
    public TranslationResult translateContent(String content, String sourceLanguage, String targetLanguage) {
        if (StringUtils.isBlank(content)) {
            return TranslationResult.error(content, sourceLanguage, targetLanguage, "Content cannot be empty");
        }

        if (StringUtils.isBlank(sourceLanguage) || StringUtils.isBlank(targetLanguage)) {
            return TranslationResult.error(content, sourceLanguage, targetLanguage, "Source and target languages are required");
        }

        if (sourceLanguage.equalsIgnoreCase(targetLanguage)) {
            return TranslationResult.success(content, content, sourceLanguage, targetLanguage, Collections.emptyMap());
        }

        if (enableCache) {
            String cacheKey = generateCacheKey(content, sourceLanguage, targetLanguage);
            CachedTranslation cached = translationCache.get(cacheKey);
            if (cached != null) {
                log.debug("Translation cache hit for {} -> {}", sourceLanguage, targetLanguage);
                return TranslationResult.success(content, cached.translated, sourceLanguage, targetLanguage, cached.metadata);
            }
        }

        try {
            String prompt = buildTranslationPrompt(content, sourceLanguage, targetLanguage);
            String translated = callAITranslation(prompt);

            if (StringUtils.isNotBlank(translated)) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("model", translationModel);
                metadata.put("sourceLength", content.length());
                metadata.put("targetLength", translated.length());

                if (enableCache) {
                    String cacheKey = generateCacheKey(content, sourceLanguage, targetLanguage);
                    translationCache.put(cacheKey, new CachedTranslation(translated, metadata));
                    evictCacheIfNeeded();
                }

                return TranslationResult.success(content, translated, sourceLanguage, targetLanguage, metadata);
            }

            return TranslationResult.error(content, sourceLanguage, targetLanguage, "Translation returned empty result");

        } catch (Exception e) {
            log.error("Translation failed: {}", e.getMessage());
            return TranslationResult.error(content, sourceLanguage, targetLanguage, e.getMessage());
        }
    }

    @Override
    public TranslationResult translatePage(Resource pageResource, String sourceLanguage, String targetLanguage) {
        if (pageResource == null || ResourceUtil.isNonExistingResource(pageResource)) {
            return TranslationResult.error(null, sourceLanguage, targetLanguage, "Page resource is null or non-existing");
        }

        List<TranslationResult> results = new ArrayList<>();

        Map<String, String> pageMetadata = extractPageMetadata(pageResource);
        if (!pageMetadata.isEmpty()) {
            Map<String, String> translatedMetadata = translateMetadata(pageMetadata, sourceLanguage, targetLanguage);
            applyTranslatedMetadata(pageResource, translatedMetadata);
        }

        List<Resource> contentResources = getContentResources(pageResource);
        for (Resource contentResource : contentResources) {
            Map<String, Object> properties = contentResource.getValueMap();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String text = (String) entry.getValue();
                    if (isTranslatableText(text)) {
                        TranslationResult result = translateContent(text, sourceLanguage, targetLanguage);
                        if (result.isSuccess()) {
                            results.add(result);
                        }
                    }
                }
            }
        }

        long successCount = results.stream().filter(TranslationResult::isSuccess).count();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("translatedItems", successCount);
        metadata.put("totalItems", results.size());

        if (successCount > 0) {
            return TranslationResult.success(
                    String.valueOf(results.size()),
                    "Translated " + successCount + " items",
                    sourceLanguage,
                    targetLanguage,
                    metadata
            );
        }

        return TranslationResult.error("0", sourceLanguage, targetLanguage, "No content translated");
    }

    @Override
    public List<TranslationResult> translatePageToMultipleLanguages(Resource pageResource, String sourceLanguage, List<String> targetLanguages) {
        if (pageResource == null || targetLanguages == null || targetLanguages.isEmpty()) {
            return Collections.emptyList();
        }

        return targetLanguages.stream()
                .map(targetLang -> translatePage(pageResource, sourceLanguage, targetLang))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> translateMetadata(Map<String, String> metadata, String sourceLanguage, String targetLanguage) {
        Map<String, String> translated = new HashMap<>();

        if (metadata == null || metadata.isEmpty()) {
            return translated;
        }

        List<String> translatableKeys = Arrays.asList(
                "jcr:title", "jcr:description", "pageTitle", "pageDescription",
                "navTitle", "subtitle", "keywords", "abstract"
        );

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (translatableKeys.contains(key) && StringUtils.isNotBlank(value)) {
                TranslationResult result = translateContent(value, sourceLanguage, targetLanguage);
                if (result.isSuccess()) {
                    translated.put(key, result.getTranslatedContent());
                } else {
                    translated.put(key, value);
                }
            } else {
                translated.put(key, value);
            }
        }

        return translated;
    }

    @Override
    public List<TranslationResult> translateExperienceFragment(Resource fragment, String sourceLanguage, String targetLanguage) {
        List<TranslationResult> results = new ArrayList<>();

        if (fragment == null || ResourceUtil.isNonExistingResource(fragment)) {
            return results;
        }

        List<Resource> variations = fragment.getChildren();
        for (Resource variation : variations) {
            Map<String, Object> properties = variation.getValueMap();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String text = (String) entry.getValue();
                    if (isTranslatableText(text)) {
                        TranslationResult result = translateContent(text, sourceLanguage, targetLanguage);
                        results.add(result);
                    }
                }
            }
        }

        return results;
    }

    @Override
    public Resource createLanguageCopy(Resource sourcePage, String targetLanguage, ResourceResolver resolver) {
        if (sourcePage == null || resolver == null) {
            return null;
        }

        try {
            String sourcePath = sourcePage.getPath();
            String languageFolder = "/" + targetLanguage;
            String targetPath = sourcePath.replaceFirst("/content/", "/content/" + targetLanguage + "/");

            if (targetPath.equals(sourcePath)) {
                targetPath = sourcePath + languageFolder;
            }

            Resource targetParent = resolver.getResource(targetPath.substring(0, targetPath.lastIndexOf('/')));
            if (targetParent == null) {
                Map<String, Object> props = new HashMap<>();
                props.put("jcr:primaryType", "sling:Folder");
                targetParent = resolver.create(resolver.getResource("/content"), targetLanguage, props);
            }

            Map<String, Object> pageProps = new HashMap<>();
            pageProps.put("jcr:primaryType", "cq:Page");
            Resource targetPage = resolver.create(targetParent, sourcePage.getName(), pageProps);

            copyAndTranslateContent(sourcePage, targetPage, targetLanguage, resolver);

            resolver.commit();
            return targetPage;

        } catch (Exception e) {
            log.error("Failed to create language copy: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<TranslationResult> translateComponentContent(Resource component, String sourceLanguage, String targetLanguage) {
        List<TranslationResult> results = new ArrayList<>();

        if (component == null || ResourceUtil.isNonExistingResource(component)) {
            return results;
        }

        Map<String, Object> properties = component.getValueMap();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getValue() instanceof String) {
                String text = (String) entry.getValue();
                if (isTranslatableText(text)) {
                    TranslationResult result = translateContent(text, sourceLanguage, targetLanguage);
                    results.add(result);
                }
            }
        }

        return results;
    }

    @Override
    public List<Language> getSupportedLanguages() {
        return new ArrayList<>(Arrays.asList(
                new Language("en", "English", "English"),
                new Language("de", "German", "Deutsch"),
                new Language("fr", "French", "Français"),
                new Language("es", "Spanish", "Español"),
                new Language("pl", "Polish", "Polski"),
                new Language("it", "Italian", "Italiano"),
                new Language("pt", "Portuguese", "Português"),
                new Language("nl", "Dutch", "Nederlands"),
                new Language("ru", "Russian", "Русский"),
                new Language("ja", "Japanese", "日本語"),
                new Language("ko", "Korean", "한국어"),
                new Language("zh", "Chinese", "中文"),
                new Language("ar", "Arabic", "العربية"),
                new Language("hi", "Hindi", "हिन्दी"),
                new Language("sv", "Swedish", "Svenska"),
                new Language("da", "Danish", "Dansk"),
                new Language("fi", "Finnish", "Suomi"),
                new Language("no", "Norwegian", "Norsk"),
                new Language("cs", "Czech", "Čeština"),
                new Language("hu", "Hungarian", "Magyar")
        ));
    }

    private String buildTranslationPrompt(String content, String sourceLanguage, String targetLanguage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Translate the following content from ");
        prompt.append(getLanguageName(sourceLanguage));
        prompt.append(" to ");
        prompt.append(getLanguageName(targetLanguage));
        prompt.append(".\n\n");
        prompt.append("Requirements:\n");
        prompt.append("- Preserve the original formatting and structure\n");
        prompt.append("- Maintain HTML tags if present\n");
        prompt.append("- Keep brand names and technical terms in original language if appropriate\n");
        prompt.append("- Ensure cultural appropriateness for the target language\n\n");
        prompt.append("Content to translate:\n");
        prompt.append(content);
        prompt.append("\n\nTranslated content:");
        return prompt.toString();
    }

    private String getLanguageName(String code) {
        Language lang = Language.fromCode(code);
        return lang.getName() + " (" + lang.getNativeName() + ")";
    }

    private String callAITranslation(String prompt) {
        if (StringUtils.isBlank(apiKey)) {
            log.warn("API key not configured, using mock translation");
            return performMockTranslation(prompt);
        }

        try {
            String requestBody = String.format(
                    "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": %d, \"temperature\": %.1f}",
                    translationModel,
                    escapeJson(prompt),
                    maxTokens,
                    temperature
            );

            HttpURLConnection connection = (HttpURLConnection) new URL(aiServiceUrl).openConnection();
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
                log.warn("AI API returned response code: {}", responseCode);
            }
        } catch (Exception e) {
            log.error("AI translation API call failed: {}", e.getMessage());
        }

        return performMockTranslation(prompt);
    }

    private String performMockTranslation(String prompt) {
        String content = prompt.replaceAll(".*Content to translate:\\n", "").replaceAll("\\n\\nTranslated content:.*", "");
        return "[Translated] " + content;
    }

    private String generateCacheKey(String content, String sourceLanguage, String targetLanguage) {
        String keyMaterial = content + "|" + sourceLanguage + "|" + targetLanguage;
        return String.valueOf(keyMaterial.hashCode());
    }

    private void evictCacheIfNeeded() {
        if (translationCache.size() > cacheSize) {
            int toRemove = translationCache.size() - cacheSize + 100;
            Iterator<String> iterator = translationCache.keySet().iterator();
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                translationCache.remove(iterator.next());
            }
        }
    }

    private Map<String, String> extractPageMetadata(Resource pageResource) {
        Map<String, String> metadata = new HashMap<>();
        Resource contentResource = pageResource.getChild("jcr:content");
        if (contentResource != null) {
            Map<String, Object> props = contentResource.getValueMap();
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                if (entry.getValue() instanceof String) {
                    metadata.put(entry.getKey(), (String) entry.getValue());
                }
            }
        }
        return metadata;
    }

    private void applyTranslatedMetadata(Resource pageResource, Map<String, String> translatedMetadata) {
    }

    private List<Resource> getContentResources(Resource pageResource) {
        List<Resource> resources = new ArrayList<>();
        Resource contentResource = pageResource.getChild("jcr:content");
        if (contentResource != null) {
            resources.add(contentResource);
            for (Resource child : contentResource.getChildren()) {
                resources.add(child);
            }
        }
        return resources;
    }

    private boolean isTranslatableText(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        if (text.length() < 2) {
            return false;
        }
        if (text.startsWith("{{") || text.startsWith("${")) {
            return false;
        }
        return true;
    }

    private void copyAndTranslateContent(Resource source, Resource target, String targetLanguage, ResourceResolver resolver) {
    }

    private static class CachedTranslation {
        final String translated;
        final Map<String, Object> metadata;

        CachedTranslation(String translated, Map<String, Object> metadata) {
            this.translated = translated;
            this.metadata = metadata;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public int getCacheCount() {
        return translationCache.size();
    }
}