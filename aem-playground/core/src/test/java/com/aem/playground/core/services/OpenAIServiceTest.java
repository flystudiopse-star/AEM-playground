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
package com.aem.playground.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.metatype.AttributeDefinition;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenAIServiceTest {

    private OpenAIService openAIService;

    @BeforeEach
    void setUp() {
        openAIService = new OpenAIService();
    }

    private void activateService(OpenAIServiceConfig config) throws Exception {
        java.lang.reflect.Method activateMethod = OpenAIService.class.getDeclaredMethod("activate", OpenAIServiceConfig.class);
        activateMethod.setAccessible(true);
        activateMethod.invoke(openAIService, config);
    }

    @Test
    void testDefaultEndpointValues() throws Exception {
        OpenAIServiceConfig config = mock(OpenAIServiceConfig.class);
        when(config.apiKey()).thenReturn("test-key");
        when(config.textEndpoint()).thenReturn("");
        when(config.imageEndpoint()).thenReturn("");
        when(config.defaultModel()).thenReturn("");
        when(config.defaultImageModel()).thenReturn("");
        when(config.cachingEnabled()).thenReturn(true);
        when(config.cacheMaxSize()).thenReturn(100);

        activateService(config);

        assertEquals("https://api.openai.com/v1/chat/completions", openAIService.getTextEndpoint());
        assertEquals("https://api.openai.com/v1/images/generations", openAIService.getImageEndpoint());
    }

    @Test
    void testCustomEndpointValues() throws Exception {
        OpenAIServiceConfig config = mock(OpenAIServiceConfig.class);
        when(config.apiKey()).thenReturn("test-key");
        when(config.textEndpoint()).thenReturn("https://custom.text.endpoint.com");
        when(config.imageEndpoint()).thenReturn("https://custom.image.endpoint.com");
        when(config.defaultModel()).thenReturn("gpt-4-turbo");
        when(config.defaultImageModel()).thenReturn("dall-e-2");
        when(config.cachingEnabled()).thenReturn(false);
        when(config.cacheMaxSize()).thenReturn(50);

        activateService(config);

        assertEquals("https://custom.text.endpoint.com", openAIService.getTextEndpoint());
        assertEquals("https://custom.image.endpoint.com", openAIService.getImageEndpoint());
        assertFalse(openAIService.isCachingEnabled());
        assertEquals(50, openAIService.getCacheMaxSize());
    }

    @Test
    void testGenerateTextWithEmptyPrompt() {
        AIService.AIGenerationResult result = openAIService.generateText("", AIGenerationOptions.builder().build());
        
        assertFalse(result.isSuccess());
        assertEquals("Prompt cannot be empty", result.getError());
    }

    @Test
    void testGenerateTextWithNullPrompt() {
        AIService.AIGenerationResult result = openAIService.generateText(null, AIGenerationOptions.builder().build());
        
        assertFalse(result.isSuccess());
        assertEquals("Prompt cannot be empty", result.getError());
    }

    @Test
    void testGenerateTextWithBlankPrompt() {
        AIService.AIGenerationResult result = openAIService.generateText("   ", AIGenerationOptions.builder().build());
        
        assertFalse(result.isSuccess());
        assertEquals("Prompt cannot be empty", result.getError());
    }

    @Test
    void testGenerateImageWithEmptyPrompt() {
        AIService.AIGenerationResult result = openAIService.generateImage("", AIGenerationOptions.builder().build());
        
        assertFalse(result.isSuccess());
        assertEquals("Prompt cannot be empty", result.getError());
    }

    @Test
    void testGenerateImageWithNullPrompt() {
        AIService.AIGenerationResult result = openAIService.generateImage(null, AIGenerationOptions.builder().build());
        
        assertFalse(result.isSuccess());
        assertEquals("Prompt cannot be empty", result.getError());
    }

    @Test
    void testClearCacheClearsAllCaches() throws Exception {
        OpenAIServiceConfig config = mock(OpenAIServiceConfig.class);
        when(config.apiKey()).thenReturn("test-key");
        when(config.textEndpoint()).thenReturn("https://api.openai.com/v1/chat/completions");
        when(config.imageEndpoint()).thenReturn("https://api.openai.com/v1/images/generations");
        when(config.defaultModel()).thenReturn("gpt-4");
        when(config.defaultImageModel()).thenReturn("dall-e-3");
        when(config.cachingEnabled()).thenReturn(true);
        when(config.cacheMaxSize()).thenReturn(100);

        activateService(config);

        openAIService.clearCache();

        assertEquals(0, openAIService.getTextCacheSize());
        assertEquals(0, openAIService.getImageCacheSize());
    }

    @Test
    void testGenerateTextWithNullOptionsUsesDefaults() {
        AIService.AIGenerationResult result = openAIService.generateText("Test prompt", null);
        
        assertFalse(result.isSuccess());
    }

    @Test
    void testGenerateImageWithNullOptionsUsesDefaults() {
        AIService.AIGenerationResult result = openAIService.generateImage("Test prompt", null);
        
        assertFalse(result.isSuccess());
    }

    @Test
    void testCacheEnabledByDefault() throws Exception {
        OpenAIServiceConfig config = mock(OpenAIServiceConfig.class);
        when(config.apiKey()).thenReturn("test-key");
        when(config.textEndpoint()).thenReturn("https://api.openai.com/v1/chat/completions");
        when(config.imageEndpoint()).thenReturn("https://api.openai.com/v1/images/generations");
        when(config.defaultModel()).thenReturn("gpt-4");
        when(config.defaultImageModel()).thenReturn("dall-e-3");
        when(config.cachingEnabled()).thenReturn(true);
        when(config.cacheMaxSize()).thenReturn(100);

        activateService(config);

        assertTrue(openAIService.isCachingEnabled());
    }

    @Test
    void testDeactivateClearsCaches() throws Exception {
        OpenAIServiceConfig config = mock(OpenAIServiceConfig.class);
        when(config.apiKey()).thenReturn("test-key");
        when(config.textEndpoint()).thenReturn("https://api.openai.com/v1/chat/completions");
        when(config.imageEndpoint()).thenReturn("https://api.openai.com/v1/images/generations");
        when(config.defaultModel()).thenReturn("gpt-4");
        when(config.defaultImageModel()).thenReturn("dall-e-3");
        when(config.cachingEnabled()).thenReturn(true);
        when(config.cacheMaxSize()).thenReturn(100);

        activateService(config);

        java.lang.reflect.Method deactivateMethod = OpenAIService.class.getDeclaredMethod("deactivate");
        deactivateMethod.setAccessible(true);
        deactivateMethod.invoke(openAIService);

        assertEquals(0, openAIService.getTextCacheSize());
        assertEquals(0, openAIService.getImageCacheSize());
    }

    @Test
    void testCacheKeyGenerationIsDeterministic() {
        AIGenerationOptions opts1 = AIGenerationOptions.builder()
            .model("gpt-4")
            .temperature(0.7)
            .maxTokens(1000)
            .build();
        
        AIGenerationOptions opts2 = AIGenerationOptions.builder()
            .model("gpt-4")
            .temperature(0.7)
            .maxTokens(1000)
            .build();
        
        String key1 = opts1.getCacheKey();
        String key2 = opts2.getCacheKey();
        
        assertEquals(key1, key2);
    }
}