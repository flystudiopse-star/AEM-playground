/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.TranslationService;
import com.aem.playground.core.services.TranslationServiceConfig;
import com.aem.playground.core.services.TranslationService.TranslationRequest;
import com.aem.playground.core.services.TranslationService.TranslationResult;
import com.aem.playground.core.services.TranslationService.BatchTranslationResult;
import com.aem.playground.core.services.TranslationService.TargetLanguage;
import com.aem.playground.core.services.TranslationService.LanguageDetectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationServiceImplTest {

    private TranslationServiceImpl translationService;

    @Mock
    private AIService aiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        translationService = new TranslationServiceImpl();
        setField(translationService, "aiService", aiService);
    }

    private void activateService(TranslationServiceConfig config) throws Exception {
        java.lang.reflect.Method activateMethod = TranslationServiceImpl.class.getDeclaredMethod("activate", TranslationServiceConfig.class);
        activateMethod.setAccessible(true);
        activateMethod.invoke(translationService, config);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private TranslationServiceConfig createMockConfig(String defaultTargetLanguage, boolean cachingEnabled, 
                                                       int cacheMaxSize, int cacheTtlMinutes, String apiKey,
                                                       String model, double temperature, int maxTokens, int batchSize) {
        TranslationServiceConfig config = mock(TranslationServiceConfig.class);
        when(config.defaultTargetLanguage()).thenReturn(defaultTargetLanguage);
        when(config.cachingEnabled()).thenReturn(cachingEnabled);
        when(config.cacheMaxSize()).thenReturn(cacheMaxSize);
        when(config.cacheTtlMinutes()).thenReturn(cacheTtlMinutes);
        when(config.apiKey()).thenReturn(apiKey);
        when(config.translationModel()).thenReturn(model);
        when(config.temperature()).thenReturn(temperature);
        when(config.maxTokens()).thenReturn(maxTokens);
        when(config.batchSize()).thenReturn(batchSize);
        return config;
    }

    @Test
    void testDefaultConfigurationValues() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        
        activateService(config);
        
        assertEquals("en", translationService.getDefaultTargetLanguage());
        assertTrue(translationService.isCachingEnabled());
        assertEquals(500, translationService.getCacheMaxSize());
        assertEquals(10, translationService.getBatchSize());
    }

    @Test
    void testGetSupportedLanguages() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        Set<TargetLanguage> supportedLanguages = translationService.getSupportedLanguages();
        
        assertEquals(8, supportedLanguages.size());
        assertTrue(supportedLanguages.contains(TargetLanguage.ENGLISH));
        assertTrue(supportedLanguages.contains(TargetLanguage.SPANISH));
        assertTrue(supportedLanguages.contains(TargetLanguage.FRENCH));
        assertTrue(supportedLanguages.contains(TargetLanguage.GERMAN));
        assertTrue(supportedLanguages.contains(TargetLanguage.POLISH));
        assertTrue(supportedLanguages.contains(TargetLanguage.CHINESE));
        assertTrue(supportedLanguages.contains(TargetLanguage.JAPANESE));
        assertTrue(supportedLanguages.contains(TargetLanguage.ARABIC));
    }

    @Test
    void testTranslatePageWithEmptyContent() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        TranslationResult result = translationService.translatePage("", "en", TargetLanguage.SPANISH);
        
        assertFalse(result.isSuccess());
        assertEquals("Content cannot be empty", result.getError());
    }

    @Test
    void testTranslatePageWithNullSourceLanguage() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        TranslationResult result = translationService.translatePage("Hello world", null, TargetLanguage.SPANISH);
        
        assertFalse(result.isSuccess());
        assertEquals("Source language cannot be empty", result.getError());
    }

    @Test
    void testTranslateContentFragmentSuccess() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", false, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        Map<String, Object> aiMetadata = new HashMap<>();
        aiMetadata.put("model", "gpt-4");
        AIService.AIGenerationResult aiResult = AIService.AIGenerationResult.success("Hola mundo", aiMetadata);
        when(aiService.generateText(anyString(), any())).thenReturn(aiResult);
        
        TranslationResult result = translationService.translateContentFragment("Hello world", "en", TargetLanguage.SPANISH);
        
        assertTrue(result.isSuccess());
        assertEquals("Hola mundo", result.getTranslatedContent());
        assertEquals("en", result.getSourceLanguage());
        assertEquals("es", result.getTargetLanguage());
    }

    @Test
    void testBatchTranslateWithEmptyList() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        BatchTranslationResult result = translationService.batchTranslate(new ArrayList<>());
        
        assertFalse(result.isSuccess());
        assertEquals("Request list cannot be empty", result.getError());
    }

    @Test
    void testBatchTranslateExceedsMaxSize() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 2);
        activateService(config);
        
        List<TranslationRequest> requests = new ArrayList<>();
        requests.add(new TranslationRequest("Hello", "en", TargetLanguage.SPANISH));
        requests.add(new TranslationRequest("World", "en", TargetLanguage.FRENCH));
        requests.add(new TranslationRequest("Test", "en", TargetLanguage.GERMAN));
        
        BatchTranslationResult result = translationService.batchTranslate(requests);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Batch size exceeds maximum"));
    }

    @Test
    void testBatchTranslateSuccess() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", false, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        Map<String, Object> aiMetadata = new HashMap<>();
        aiMetadata.put("model", "gpt-4");
        AIService.AIGenerationResult aiResult = AIService.AIGenerationResult.success("Translated", aiMetadata);
        when(aiService.generateText(anyString(), any())).thenReturn(aiResult);
        
        List<TranslationRequest> requests = new ArrayList<>();
        requests.add(new TranslationRequest("Hello", "en", TargetLanguage.SPANISH));
        requests.add(new TranslationRequest("World", "en", TargetLanguage.FRENCH));
        
        BatchTranslationResult result = translationService.batchTranslate(requests);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    void testDetectLanguageWithEmptyContent() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        LanguageDetectionResult result = translationService.detectLanguage("");
        
        assertFalse(result.isSuccess());
        assertEquals("Content cannot be empty", result.getError());
    }

    @Test
    void testDetectLanguageSuccess() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", false, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        Map<String, Object> aiMetadata = new HashMap<>();
        aiMetadata.put("model", "gpt-4");
        AIService.AIGenerationResult aiResult = AIService.AIGenerationResult.success("en", aiMetadata);
        when(aiService.generateText(anyString(), any())).thenReturn(aiResult);
        
        LanguageDetectionResult result = translationService.detectLanguage("Hello world");
        
        assertTrue(result.isSuccess());
        assertEquals("en", result.getDetectedLanguage());
        assertEquals(0.9, result.getConfidence(), 0.01);
    }

    @Test
    void testClearCache() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        translationService.clearCache();
        
        assertEquals(0, translationService.getCacheSize());
    }

    @Test
    void testDeactivateClearsCache() throws Exception {
        TranslationServiceConfig config = createMockConfig("en", true, 500, 60, "test-key", "gpt-4", 0.3, 4000, 10);
        activateService(config);
        
        java.lang.reflect.Method deactivateMethod = TranslationServiceImpl.class.getDeclaredMethod("deactivate");
        deactivateMethod.setAccessible(true);
        deactivateMethod.invoke(translationService);
        
        assertEquals(0, translationService.getCacheSize());
    }

    @Test
    void testTargetLanguageCodes() {
        assertEquals("en", TargetLanguage.ENGLISH.getCode());
        assertEquals("es", TargetLanguage.SPANISH.getCode());
        assertEquals("fr", TargetLanguage.FRENCH.getCode());
        assertEquals("de", TargetLanguage.GERMAN.getCode());
        assertEquals("pl", TargetLanguage.POLISH.getCode());
        assertEquals("zh", TargetLanguage.CHINESE.getCode());
        assertEquals("ja", TargetLanguage.JAPANESE.getCode());
        assertEquals("ar", TargetLanguage.ARABIC.getCode());
    }

    @Test
    void testTargetLanguageDisplayNames() {
        assertEquals("English", TargetLanguage.ENGLISH.getDisplayName());
        assertEquals("Spanish", TargetLanguage.SPANISH.getDisplayName());
        assertEquals("French", TargetLanguage.FRENCH.getDisplayName());
        assertEquals("German", TargetLanguage.GERMAN.getDisplayName());
        assertEquals("Polish", TargetLanguage.POLISH.getDisplayName());
        assertEquals("Chinese", TargetLanguage.CHINESE.getDisplayName());
        assertEquals("Japanese", TargetLanguage.JAPANESE.getDisplayName());
        assertEquals("Arabic", TargetLanguage.ARABIC.getDisplayName());
    }
}