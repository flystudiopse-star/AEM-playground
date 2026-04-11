package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.TranslationService;
import com.aem.playground.core.services.TranslationServiceConfig;
import com.aem.playground.core.services.dto.BilingualContentComparison;
import com.aem.playground.core.services.dto.TranslationRequest;
import com.aem.playground.core.services.dto.TranslationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ContentTranslatorServiceImplTest {

    @Mock
    private AIService aiService;

    @Mock
    private TranslationServiceConfig config;

    private ContentTranslatorServiceImpl translationService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        translationService = new ContentTranslatorServiceImpl();
        translationService.setAiService(aiService);

        when(config.enableService()).thenReturn(true);
        when(config.translationModel()).thenReturn("gpt-4");
        when(config.temperature()).thenReturn(0.3);
        when(config.maxTokens()).thenReturn(4000);
        when(config.supportedLanguages()).thenReturn("en,de,fr,es,pl,it,pt,nl,ja,zh,ko");
        when(config.defaultSourceLanguage()).thenReturn("en");
        when(config.enableCaching()).thenReturn(true);
        when(config.cacheMaxSize()).thenReturn(500);
        when(config.useMSMLiveCopies()).thenReturn(false);
        when(config.translationPromptTemplate()).thenReturn("Translate from {source_lang} to {target_lang}");

        java.lang.reflect.Method activateMethod = ContentTranslatorServiceImpl.class.getDeclaredMethod("activate", TranslationServiceConfig.class);
        activateMethod.setAccessible(true);
        activateMethod.invoke(translationService, config);
    }

    @Test
    void testServiceActivation() {
        assertTrue(translationService.isEnabled());
        assertEquals("gpt-4", translationService.getTranslationModel());
    }

    @Test
    void testSupportedLanguages() {
        List<String> languages = translationService.getSupportedLanguages();
        assertNotNull(languages);
        assertTrue(languages.contains("en"));
        assertTrue(languages.contains("de"));
        assertTrue(languages.contains("fr"));
        assertTrue(languages.contains("es"));
        assertTrue(languages.contains("pl"));
    }

    @Test
    void testIsLanguageSupported() {
        assertTrue(translationService.isLanguageSupported("en"));
        assertTrue(translationService.isLanguageSupported("de"));
        assertTrue(translationService.isLanguageSupported("DE"));
        assertFalse(translationService.isLanguageSupported("xx"));
        assertFalse(translationService.isLanguageSupported(null));
    }

    @Test
    void testGetLanguageDisplayName() {
        assertEquals("English", translationService.getLanguageDisplayName("en"));
        assertEquals("German", translationService.getLanguageDisplayName("de"));
        assertEquals("French", translationService.getLanguageDisplayName("fr"));
        assertEquals("Spanish", translationService.getLanguageDisplayName("es"));
        assertEquals("Polish", translationService.getLanguageDisplayName("pl"));
        assertEquals("ja", translationService.getLanguageDisplayName("ja"));
    }

    @Test
    void testTranslateContentWithDisabledService() throws Exception {
        when(config.enableService()).thenReturn(false);
        
        java.lang.reflect.Method activateMethod = ContentTranslatorServiceImpl.class.getDeclaredMethod("activate", TranslationServiceConfig.class);
        activateMethod.setAccessible(true);
        activateMethod.invoke(translationService, config);

        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .sourceLanguage("en")
            .targetLanguage("de")
            .build();

        TranslationResult result = translationService.translateContent(request);
        
        assertFalse(result.isSuccess());
        assertEquals("Translation service is disabled", result.getError());
    }

    @Test
    void testTranslateContentWithMissingContentPath() {
        TranslationRequest request = TranslationRequest.builder()
            .sourceLanguage("en")
            .targetLanguage("de")
            .build();

        TranslationResult result = translationService.translateContent(request);
        
        assertFalse(result.isSuccess());
        assertEquals("Content path is required", result.getError());
    }

    @Test
    void testTranslateContentWithUnsupportedSourceLanguage() {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .sourceLanguage("xx")
            .targetLanguage("de")
            .build();

        TranslationResult result = translationService.translateContent(request);
        
        assertFalse(result.isSuccess());
        assertEquals("Unsupported source language: xx", result.getError());
    }

    @Test
    void testTranslateContentWithUnsupportedTargetLanguage() {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .sourceLanguage("en")
            .targetLanguage("xx")
            .build();

        TranslationResult result = translationService.translateContent(request);
        
        assertFalse(result.isSuccess());
        assertEquals("Unsupported target language: xx", result.getError());
    }

    @Test
    void testTranslateContentWithValidRequest() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Translated content"));

        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .sourceLanguage("en")
            .targetLanguage("de")
            .translationType(TranslationRequest.TranslationType.FULL_PAGE)
            .build();

        TranslationResult result = translationService.translateContent(request);
        
        assertTrue(result.isSuccess());
        assertEquals("Translated content", result.getTranslatedContent());
        assertEquals("en", result.getSourceLanguage());
        assertEquals("de", result.getTargetLanguage());
    }

    @Test
    void testTranslateContentWithAIError() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.error("API error"));

        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .sourceLanguage("en")
            .targetLanguage("de")
            .build();

        TranslationResult result = translationService.translateContent(request);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Translation API error"));
    }

    @Test
    void testTranslatePage() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Page translated"));

        TranslationResult result = translationService.translatePage("/content/page", "en", "de");
        
        assertTrue(result.isSuccess());
        assertEquals("Page translated", result.getTranslatedContent());
    }

    @Test
    void testTranslateMetadata() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Metadata translated"));

        TranslationResult result = translationService.translateMetadata("/content/page", "en", "de");
        
        assertTrue(result.isSuccess());
        assertEquals("Metadata translated", result.getTranslatedContent());
    }

    @Test
    void testTranslateComponent() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Component translated"));

        TranslationResult result = translationService.translateComponent("/content/page/components/text", "en", "de");
        
        assertTrue(result.isSuccess());
        assertEquals("Component translated", result.getTranslatedContent());
    }

    @Test
    void testTranslateExperienceFragment() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Fragment translated"));

        TranslationResult result = translationService.translateExperienceFragment("/content/experience-fragments/fragment", "en", "de");
        
        assertTrue(result.isSuccess());
        assertEquals("Fragment translated", result.getTranslatedContent());
    }

    @Test
    void testTranslateToMultipleLanguages() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Translated"));

        List<String> targetLanguages = List.of("de", "fr", "es");
        Map<String, TranslationResult> results = translationService.translateToMultipleLanguages("/content/page", "en", targetLanguages);
        
        assertEquals(3, results.size());
        assertTrue(results.get("de").isSuccess());
        assertTrue(results.get("fr").isSuccess());
        assertTrue(results.get("es").isSuccess());
    }

    @Test
    void testTranslateToMultipleLanguagesWithUnsupportedLanguage() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Translated"));

        List<String> targetLanguages = List.of("de", "xx");
        Map<String, TranslationResult> results = translationService.translateToMultipleLanguages("/content/page", "en", targetLanguages);
        
        assertEquals(2, results.size());
        assertTrue(results.get("de").isSuccess());
        assertFalse(results.get("xx").isSuccess());
    }

    @Test
    void testCreateLanguageBranch() {
        String result = translationService.createLanguageBranch("/content/site/en/page", "de");
        assertEquals("/content/site/de/page", result);
    }

    @Test
    void testCreateLanguageBranchWithDifferentPath() {
        String result = translationService.createLanguageBranch("/content/my-site/language/home", "fr");
        assertEquals("/content/my-site/fr/home", result);
    }

    @Test
    void testCreateLanguageCopy() {
        when(config.useMSMLiveCopies()).thenReturn(false);
        
        String result = translationService.createLanguageCopy("/content/page/en", "de");
        assertEquals("/content/page/de", result);
    }

    @Test
    void testCreateLanguageCopyWithMSM() throws Exception {
        when(config.useMSMLiveCopies()).thenReturn(true);
        
        java.lang.reflect.Method activateMethod = ContentTranslatorServiceImpl.class.getDeclaredMethod("activate", TranslationServiceConfig.class);
        activateMethod.setAccessible(true);
        activateMethod.invoke(translationService, config);

        String result = translationService.createLanguageCopy("/content/page/en", "de");
        assertEquals("/content/page/de", result);
    }

    @Test
    void testCreateLanguageCopyWithUnsupportedLanguage() {
        String result = translationService.createLanguageCopy("/content/page", "xx");
        assertNull(result);
    }

    @Test
    void testDefaultSourceLanguageWhenNotProvided() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Translated"));

        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .targetLanguage("de")
            .build();

        TranslationResult result = translationService.translateContent(request);
        
        assertTrue(result.isSuccess());
    }

    @Test
    void testTranslationResultBuilder() {
        TranslationResult result = TranslationResult.builder()
            .success(true)
            .translatedContent("Translated text")
            .sourceLanguage("en")
            .targetLanguage("de")
            .contentPath("/content/page")
            .tokensUsed(100)
            .translationTimeMs(500)
            .build();

        assertTrue(result.isSuccess());
        assertEquals("Translated text", result.getTranslatedContent());
        assertEquals("en", result.getSourceLanguage());
        assertEquals("de", result.getTargetLanguage());
        assertEquals("/content/page", result.getContentPath());
        assertEquals(100, result.getTokensUsed());
        assertEquals(500, result.getTranslationTimeMs());
    }

    @Test
    void testTranslationResultStaticFactories() {
        TranslationResult successResult = TranslationResult.success("Test translation");
        assertTrue(successResult.isSuccess());
        assertEquals("Test translation", successResult.getTranslatedContent());

        TranslationResult errorResult = TranslationResult.error("Error message");
        assertFalse(errorResult.isSuccess());
        assertEquals("Error message", errorResult.getError());

        TranslationResult partialResult = TranslationResult.partialSuccess("Partial translation", "Warning");
        assertTrue(partialResult.isSuccess());
        assertEquals("Partial translation", partialResult.getTranslatedContent());
        assertEquals("Warning", partialResult.getError());
    }

    @Test
    void testTranslationRequestBuilder() {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .sourceLanguage("en")
            .targetLanguage("de")
            .translationType(TranslationRequest.TranslationType.PAGE_CONTENT)
            .preserveFormatting(true)
            .translateMetadata(true)
            .build();

        assertEquals("/content/page", request.getContentPath());
        assertEquals("en", request.getSourceLanguage());
        assertEquals("de", request.getTargetLanguage());
        assertEquals(TranslationRequest.TranslationType.PAGE_CONTENT, request.getTranslationType());
        assertTrue(request.isPreserveFormatting());
        assertTrue(request.isTranslateMetadata());
    }

    @Test
    void testTranslationRequestDefaultValues() {
        TranslationRequest request = TranslationRequest.builder()
            .contentPath("/content/page")
            .sourceLanguage("en")
            .targetLanguage("de")
            .build();

        assertEquals(TranslationRequest.TranslationType.FULL_PAGE, request.getTranslationType());
        assertTrue(request.isPreserveFormatting());
        assertTrue(request.isTranslateMetadata());
    }

    @Test
    void testCompareBilingualContent() {
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
            .thenReturn(AIService.AIGenerationResult.success("Translated"));

        var comparison = translationService.compareBilingualContent("/content/page", "en", "de");
        
        assertNotNull(comparison);
        assertEquals("/content/page", comparison.getSourcePath());
        assertEquals("en", comparison.getSourceLanguage());
        assertEquals("de", comparison.getTargetLanguage());
    }
}