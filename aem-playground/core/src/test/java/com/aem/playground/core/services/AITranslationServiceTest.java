package com.aem.playground.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AITranslationServiceTest {

    @Mock
    private AIService aiService;

    private AITranslationService translationService;

    @BeforeEach
    void setUp() {
        translationService = new AITranslationService();
    }

    @Test
    void testTranslateContentEmptyContent() {
        TranslationService.TranslationResult result = translationService.translateContent("", "en", "de");
        assertFalse(result.isSuccess());
        assertEquals("Content cannot be empty", result.getError());
    }

    @Test
    void testTranslateContentNullContent() {
        TranslationService.TranslationResult result = translationService.translateContent(null, "en", "de");
        assertFalse(result.isSuccess());
        assertEquals("Content cannot be empty", result.getError());
    }

    @Test
    void testTranslateContentSameLanguage() {
        TranslationService.TranslationResult result = translationService.translateContent("Hello World", "en", "en");
        assertTrue(result.isSuccess());
        assertEquals("Hello World", result.getTranslatedContent());
    }

    @Test
    void testTranslateContentDifferentLanguages() {
        TranslationService.TranslationResult result = translationService.translateContent("Hello World", "en", "de");
        assertTrue(result.isSuccess());
        assertNotNull(result.getTranslatedContent());
        assertEquals("en", result.getSourceLanguage());
        assertEquals("de", result.getTargetLanguage());
    }

    @Test
    void testTranslateContentWithMetadata() {
        TranslationService.TranslationResult result = translationService.translateContent("Test content", "en", "fr");
        assertTrue(result.isSuccess());
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().containsKey("model"));
        assertTrue(result.getMetadata().containsKey("sourceLength"));
        assertTrue(result.getMetadata().containsKey("targetLength"));
    }

    @Test
    void testTranslateContentMissingSourceLanguage() {
        TranslationService.TranslationResult result = translationService.translateContent("Test", null, "de");
        assertFalse(result.isSuccess());
        assertEquals("Source and target languages are required", result.getError());
    }

    @Test
    void testTranslateContentMissingTargetLanguage() {
        TranslationService.TranslationResult result = translationService.translateContent("Test", "en", null);
        assertFalse(result.isSuccess());
        assertEquals("Source and target languages are required", result.getError());
    }

    @Test
    void testGetSupportedLanguages() {
        java.util.List<TranslationService.Language> languages = translationService.getSupportedLanguages();
        assertNotNull(languages);
        assertFalse(languages.isEmpty());
        assertTrue(languages.size() >= 20);
    }

    @Test
    void testSupportedLanguagesContainsCommonLanguages() {
        java.util.List<TranslationService.Language> languages = translationService.getSupportedLanguages();
        
        java.util.Set<String> codes = languages.stream()
                .map(TranslationService.Language::getCode)
                .collect(java.util.stream.Collectors.toSet());
        
        assertTrue(codes.contains("en"));
        assertTrue(codes.contains("de"));
        assertTrue(codes.contains("fr"));
        assertTrue(codes.contains("es"));
        assertTrue(codes.contains("pl"));
        assertTrue(codes.contains("it"));
        assertTrue(codes.contains("pt"));
    }

    @Test
    void testLanguageFromCode() {
        TranslationService.Language lang = TranslationService.Language.fromCode("de");
        assertEquals("de", lang.getCode());
        assertEquals("German", lang.getName());
        assertEquals("Deutsch", lang.getNativeName());
    }

    @Test
    void testLanguageFromCodeUnknown() {
        TranslationService.Language lang = TranslationService.Language.fromCode("xyz");
        assertEquals("xyz", lang.getCode());
    }

    @Test
    void testTranslateMetadataWithEmptyMap() {
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        java.util.Map<String, String> result = translationService.translateMetadata(metadata, "en", "de");
        assertTrue(result.isEmpty());
    }

    @Test
    void testTranslateMetadataWithNullMap() {
        java.util.Map<String, String> result = translationService.translateMetadata(null, "en", "de");
        assertTrue(result.isEmpty());
    }

    @Test
    void testTranslateMetadataWithTranslatableKeys() {
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        metadata.put("jcr:title", "Test Title");
        metadata.put("jcr:description", "Test Description");
        metadata.put("nonTranslatable", "value123");
        
        java.util.Map<String, String> result = translationService.translateMetadata(metadata, "en", "de");
        assertNotNull(result);
        assertEquals("Test Title", result.get("jcr:title"));
        assertEquals("Test Description", result.get("jcr:description"));
        assertEquals("value123", result.get("nonTranslatable"));
    }

    @Test
    void testTranslationResultSuccess() {
        TranslationService.TranslationResult result = TranslationService.TranslationResult.success(
                "original", "translated", "en", "de", java.util.Collections.emptyMap());
        
        assertTrue(result.isSuccess());
        assertEquals("original", result.getOriginalContent());
        assertEquals("translated", result.getTranslatedContent());
        assertEquals("en", result.getSourceLanguage());
        assertEquals("de", result.getTargetLanguage());
        assertNull(result.getError());
    }

    @Test
    void testTranslationResultError() {
        TranslationService.TranslationResult result = TranslationService.TranslationResult.error(
                "original", "en", "de", "Error occurred");
        
        assertFalse(result.isSuccess());
        assertNull(result.getTranslatedContent());
        assertEquals("Error occurred", result.getError());
        assertEquals("en", result.getSourceLanguage());
        assertEquals("de", result.getTargetLanguage());
    }

    @Test
    void testTranslatePageNullResource() {
        TranslationService.TranslationResult result = translationService.translatePage(null, "en", "de");
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("null or non-existing"));
    }

    @Test
    void testTranslatePageToMultipleLanguagesNullLanguages() {
        java.util.List<TranslationService.TranslationResult> results = 
                translationService.translatePageToMultipleLanguages(null, "en", null);
        assertTrue(results.isEmpty());
    }

    @Test
    void testTranslatePageToMultipleLanguagesEmptyLanguages() {
        java.util.List<TranslationService.TranslationResult> results = 
                translationService.translatePageToMultipleLanguages(null, "en", java.util.Arrays.asList());
        assertTrue(results.isEmpty());
    }

    @Test
    void testTranslateComponentContentNullResource() {
        java.util.List<TranslationService.TranslationResult> results = 
                translationService.translateComponentContent(null, "en", "de");
        assertTrue(results.isEmpty());
    }

    @Test
    void testTranslateExperienceFragmentNullResource() {
        java.util.List<TranslationService.TranslationResult> results = 
                translationService.translateExperienceFragment(null, "en", "de");
        assertTrue(results.isEmpty());
    }

    @Test
    void testCreateLanguageCopyNullParameters() {
        org.apache.sling.api.resource.Resource result = translationService.createLanguageCopy(null, "de", null);
        assertNull(result);
    }

    @Test
    void testServiceConfigurationAccessors() {
        translationService = new AITranslationService();
        
        assertNull(translationService.getApiKey());
        assertFalse(translationService.isEnableCache());
        assertEquals(0, translationService.getCacheSize());
        assertEquals(0, translationService.getCacheCount());
    }
}