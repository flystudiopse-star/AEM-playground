package com.aem.playground.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.TranslationService;
import com.aem.playground.core.services.TranslationServiceConfig;
import com.aem.playground.core.services.dto.BilingualContentComparison;
import com.aem.playground.core.services.dto.TranslationRequest;
import com.aem.playground.core.services.dto.TranslationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = TranslationService.class)
@Designate(ocd = TranslationServiceConfig.class)
public class ContentTranslatorServiceImpl implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(ContentTranslatorServiceImpl.class);

    private static final Map<String, String> LANGUAGE_NAMES = new HashMap<>();
    static {
        LANGUAGE_NAMES.put("en", "English");
        LANGUAGE_NAMES.put("de", "German");
        LANGUAGE_NAMES.put("fr", "French");
        LANGUAGE_NAMES.put("es", "Spanish");
        LANGUAGE_NAMES.put("pl", "Polish");
        LANGUAGE_NAMES.put("it", "Italian");
        LANGUAGE_NAMES.put("pt", "Portuguese");
        LANGUAGE_NAMES.put("nl", "Dutch");
        LANGUAGE_NAMES.put("ja", "Japanese");
        LANGUAGE_NAMES.put("zh", "Chinese");
        LANGUAGE_NAMES.put("ko", "Korean");
        LANGUAGE_NAMES.put("ru", "Russian");
        LANGUAGE_NAMES.put("ar", "Arabic");
        LANGUAGE_NAMES.put("sv", "Swedish");
        LANGUAGE_NAMES.put("da", "Danish");
        LANGUAGE_NAMES.put("fi", "Finnish");
        LANGUAGE_NAMES.put("no", "Norwegian");
        LANGUAGE_NAMES.put("cs", "Czech");
        LANGUAGE_NAMES.put("hu", "Hungarian");
        LANGUAGE_NAMES.put("ro", "Romanian");
    }

    private final Map<String, CachedTranslation> translationCache = new ConcurrentHashMap<>();

    @Reference
    private AIService aiService;

    private boolean enabled;
    private String translationModel;
    private double temperature;
    private int maxTokens;
    private List<String> supportedLanguages;
    private String defaultSourceLanguage;
    private boolean cachingEnabled;
    private int cacheMaxSize;
    private boolean useMSMLiveCopies;
    private String promptTemplate;

    @Activate
    @Modified
    protected void activate(TranslationServiceConfig config) {
        this.enabled = config.enableService();
        this.translationModel = StringUtils.isNotBlank(config.translationModel()) 
            ? config.translationModel() 
            : "gpt-4";
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.defaultSourceLanguage = config.defaultSourceLanguage();
        this.cachingEnabled = config.enableCaching();
        this.cacheMaxSize = config.cacheMaxSize();
        this.useMSMLiveCopies = config.useMSMLiveCopies();
        this.promptTemplate = PropertiesUtil.toString(config.translationPromptTemplate(), 
            "Translate the following content from {source_lang} to {target_lang}. Preserve HTML tags, formatting, and structure.");

        String languages = config.supportedLanguages();
        this.supportedLanguages = Arrays.asList(StringUtils.split(languages, ", "));
        if (this.supportedLanguages.isEmpty()) {
            this.supportedLanguages = Arrays.asList("en", "de", "fr", "es", "pl", "it", "pt", "nl", "ja", "zh", "ko");
        }

        log.info("ContentTranslatorServiceImpl activated with {} supported languages", supportedLanguages.size());
    }

    @Override
    public TranslationResult translateContent(TranslationRequest request) {
        if (!enabled) {
            return TranslationResult.error("Translation service is disabled");
        }

        if (request == null || StringUtils.isBlank(request.getContentPath())) {
            return TranslationResult.error("Content path is required");
        }

        if (StringUtils.isBlank(request.getSourceLanguage())) {
            request = TranslationRequest.builder()
                .contentPath(request.getContentPath())
                .sourceLanguage(defaultSourceLanguage)
                .targetLanguage(request.getTargetLanguage())
                .translationType(request.getTranslationType())
                .preserveFormatting(request.isPreserveFormatting())
                .translateMetadata(request.isTranslateMetadata())
                .build();
        }

        if (!isLanguageSupported(request.getSourceLanguage())) {
            return TranslationResult.error("Unsupported source language: " + request.getSourceLanguage());
        }

        if (!isLanguageSupported(request.getTargetLanguage())) {
            return TranslationResult.error("Unsupported target language: " + request.getTargetLanguage());
        }

        long startTime = System.currentTimeMillis();

        try {
            String contentToTranslate = buildTranslationContent(request);
            String prompt = buildPrompt(contentToTranslate, request.getSourceLanguage(), request.getTargetLanguage());

            AIGenerationOptions options = AIGenerationOptions.builder()
                .model(translationModel)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .enableCache(cachingEnabled)
                .build();

            AIService.AIGenerationResult result = aiService.generateText(prompt, options);

            if (!result.isSuccess()) {
                return TranslationResult.error("Translation API error: " + result.getError());
            }

            String translatedContent = result.getContent();
            int tokensUsed = result.getMetadata() != null && result.getMetadata().containsKey("tokens")
                ? (int) result.getMetadata().get("tokens")
                : 0;

            TranslationResult translationResult = TranslationResult.builder()
                .success(true)
                .translatedContent(translatedContent)
                .sourceLanguage(request.getSourceLanguage())
                .targetLanguage(request.getTargetLanguage())
                .contentPath(request.getContentPath())
                .tokensUsed(tokensUsed)
                .translationTimeMs(System.currentTimeMillis() - startTime)
                .build();

            if (cachingEnabled) {
                cacheTranslation(request, translationResult);
            }

            return translationResult;

        } catch (Exception e) {
            log.error("Error translating content: {}", e.getMessage());
            return TranslationResult.error(e.getMessage());
        }
    }

    @Override
    public TranslationResult translatePage(String pagePath, String sourceLanguage, String targetLanguage) {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath(pagePath)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .translationType(TranslationRequest.TranslationType.FULL_PAGE)
            .translateMetadata(true)
            .build();
        return translateContent(request);
    }

    @Override
    public TranslationResult translateMetadata(String pagePath, String sourceLanguage, String targetLanguage) {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath(pagePath)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .translationType(TranslationRequest.TranslationType.METADATA)
            .translateMetadata(true)
            .build();
        return translateContent(request);
    }

    @Override
    public TranslationResult translateComponent(String componentPath, String sourceLanguage, String targetLanguage) {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath(componentPath)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .translationType(TranslationRequest.TranslationType.COMPONENT_CONTENT)
            .build();
        return translateContent(request);
    }

    @Override
    public TranslationResult translateExperienceFragment(String fragmentPath, String sourceLanguage, String targetLanguage) {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath(fragmentPath)
            .sourceLanguage(sourceLanguage)
            .targetLanguage(targetLanguage)
            .translationType(TranslationRequest.TranslationType.EXPERIENCE_FRAGMENT)
            .build();
        return translateContent(request);
    }

    @Override
    public Map<String, TranslationResult> translateToMultipleLanguages(String pagePath, String sourceLanguage, List<String> targetLanguages) {
        Map<String, TranslationResult> results = new HashMap<>();

        for (String targetLanguage : targetLanguages) {
            if (isLanguageSupported(targetLanguage)) {
                results.put(targetLanguage, translatePage(pagePath, sourceLanguage, targetLanguage));
            } else {
                results.put(targetLanguage, TranslationResult.error("Unsupported language: " + targetLanguage));
            }
        }

        return results;
    }

    @Override
    public String createLanguageCopy(String sourcePath, String targetLanguage) {
        if (!isLanguageSupported(targetLanguage)) {
            log.error("Unsupported target language: {}", targetLanguage);
            return null;
        }

        if (useMSMLiveCopies) {
            return createMSMLiveCopy(sourcePath, targetLanguage);
        } else {
            return createLanguageBranch(sourcePath, targetLanguage);
        }
    }

    @Override
    public String createLanguageBranch(String sourcePath, String languageBranch) {
        String[] pathParts = sourcePath.split("/");
        StringBuilder newPath = new StringBuilder();

        int contentIndex = -1;
        for (int i = 0; i < pathParts.length; i++) {
            if ("content".equals(pathParts[i])) {
                contentIndex = i;
                break;
            }
        }

        if (contentIndex >= 0) {
            for (int i = 0; i <= contentIndex; i++) {
                newPath.append("/").append(pathParts[i]);
            }
            newPath.append("/").append(languageBranch);

            if (contentIndex + 1 < pathParts.length) {
                for (int i = contentIndex + 1; i < pathParts.length; i++) {
                    newPath.append("/").append(pathParts[i]);
                }
            }
        }

        String branchPath = newPath.toString();
        log.info("Created language branch: {} from {}", branchPath, sourcePath);
        return branchPath;
    }

    @Override
    public List<String> getSupportedLanguages() {
        return new ArrayList<>(supportedLanguages);
    }

    @Override
    public boolean isLanguageSupported(String languageCode) {
        return supportedLanguages != null && supportedLanguages.contains(
            languageCode != null ? languageCode.toLowerCase(Locale.ROOT) : null
        );
    }

    @Override
    public String getLanguageDisplayName(String languageCode) {
        String name = LANGUAGE_NAMES.get(languageCode);
        return name != null ? name : languageCode;
    }

    private String buildTranslationContent(TranslationRequest request) {
        StringBuilder content = new StringBuilder();

        switch (request.getTranslationType()) {
            case PAGE_CONTENT:
                content.append("Page Content from: ").append(request.getContentPath());
                break;
            case COMPONENT_CONTENT:
                content.append("Component Content from: ").append(request.getContentPath());
                break;
            case METADATA:
                content.append("Metadata from: ").append(request.getContentPath());
                break;
            case EXPERIENCE_FRAGMENT:
                content.append("Experience Fragment from: ").append(request.getContentPath());
                break;
            case FULL_PAGE:
                content.append("Full Page Content from: ").append(request.getContentPath());
                break;
            default:
                content.append("Content from: ").append(request.getContentPath());
        }

        content.append("\n\nTranslate the above content.");
        return content.toString();
    }

    private String buildPrompt(String content, String sourceLanguage, String targetLanguage) {
        String sourceName = getLanguageDisplayName(sourceLanguage);
        String targetName = getLanguageDisplayName(targetLanguage);

        return promptTemplate
            .replace("{source_lang}", sourceName)
            .replace("{target_lang}", targetName)
            + "\n\n" + content;
    }

    private void cacheTranslation(TranslationRequest request, TranslationResult result) {
        String cacheKey = generateCacheKey(request);

        if (translationCache.size() >= cacheMaxSize) {
            evictOldCacheEntries();
        }

        translationCache.put(cacheKey, new CachedTranslation(
            request.getSourceLanguage(),
            request.getTargetLanguage(),
            result.getTranslatedContent(),
            result.getTokensUsed()
        ));
    }

    private String generateCacheKey(TranslationRequest request) {
        return request.getContentPath() + "|" + request.getSourceLanguage() + "|" + request.getTargetLanguage();
    }

    private void evictOldCacheEntries() {
        int toRemove = translationCache.size() - cacheMaxSize + 10;
        for (int i = 0; i < toRemove && !translationCache.isEmpty(); i++) {
            String key = translationCache.keys().nextElement();
            translationCache.remove(key);
        }
    }

    public BilingualContentComparison compareBilingualContent(String sourcePath, String sourceLanguage, String targetLanguage) {
        BilingualContentComparison comparison = new BilingualContentComparison();
        comparison.setSourcePath(sourcePath);
        comparison.setSourceLanguage(sourceLanguage);
        comparison.setTargetLanguage(targetLanguage);

        ComparisonResult sourceContent = fetchContent(sourcePath, sourceLanguage);
        ComparisonResult targetContent = fetchContent(sourcePath, targetLanguage);

        if (sourceContent.components != null) {
            for (Map.Entry<String, String> entry : sourceContent.components.entrySet()) {
                String sourceCompContent = entry.getValue();
                String targetCompContent = targetContent.components.get(entry.getKey());

                comparison.addComponentPair(entry.getKey(), sourceCompContent, targetCompContent);

                if (targetCompContent == null) {
                    comparison.addDifference(new BilingualContentComparison.ContentDifference(
                        entry.getKey(), sourceCompContent, null,
                        BilingualContentComparison.DifferenceType.MISSING_IN_TARGET
                    ));
                } else if (!sourceCompContent.equals(targetCompContent)) {
                    comparison.addDifference(new BilingualContentComparison.ContentDifference(
                        entry.getKey(), sourceCompContent, targetCompContent,
                        BilingualContentComparison.DifferenceType.MODIFIED
                    ));
                } else {
                    comparison.addDifference(new BilingualContentComparison.ContentDifference(
                        entry.getKey(), sourceCompContent, targetCompContent,
                        BilingualContentComparison.DifferenceType.EQUAL
                    ));
                }
            }
        }

        return comparison;
    }

    private ComparisonResult fetchContent(String path, String language) {
        ComparisonResult result = new ComparisonResult();
        result.components = new HashMap<>();
        result.metadata = new HashMap<>();

        result.components.put(path + "/jcr:content", "Sample content for " + language);
        result.metadata.put("jcr:title", "Title in " + language);
        result.metadata.put("jcr:description", "Description in " + language);

        return result;
    }

    private String createMSMLiveCopy(String sourcePath, String targetLanguage) {
        String lcPath = sourcePath + "_" + targetLanguage;
        log.info("Created MSM Live Copy: {} from {}", lcPath, sourcePath);
        return lcPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTranslationModel() {
        return translationModel;
    }

    public List<String> getSupportedLanguagesList() {
        return supportedLanguages;
    }

    public void setAiService(AIService aiService) {
        this.aiService = aiService;
    }

    private static class CachedTranslation {
        final String sourceLanguage;
        final String targetLanguage;
        final String translatedContent;
        final int tokensUsed;

        CachedTranslation(String sourceLanguage, String targetLanguage, String translatedContent, int tokensUsed) {
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
            this.translatedContent = translatedContent;
            this.tokensUsed = tokensUsed;
        }
    }

    private static class ComparisonResult {
        Map<String, String> components;
        Map<String, String> metadata;
    }
}