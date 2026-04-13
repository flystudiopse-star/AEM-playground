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

import static org.junit.jupiter.api.Assertions.*;

class AIGenerationOptionsTest {

    @Test
    void testDefaultValues() {
        AIGenerationOptions options = AIGenerationOptions.builder().build();
        
        assertEquals("gpt-4", options.getModel());
        assertEquals(0.7, options.getTemperature(), 0.001);
        assertEquals(1000, options.getMaxTokens());
        assertNull(options.getCustomSystemPrompt());
        assertEquals(1, options.getImageCount());
        assertEquals("1024x1024", options.getImageSize());
        assertTrue(options.isEnableCache());
    }

    @Test
    void testBuilderWithAllOptions() {
        AIGenerationOptions options = AIGenerationOptions.builder()
            .model("gpt-4-turbo")
            .temperature(0.9)
            .maxTokens(2000)
            .customSystemPrompt("You are a helpful assistant")
            .imageCount(2)
            .imageSize("1792x1024")
            .enableCache(false)
            .build();
        
        assertEquals("gpt-4-turbo", options.getModel());
        assertEquals(0.9, options.getTemperature(), 0.001);
        assertEquals(2000, options.getMaxTokens());
        assertEquals("You are a helpful assistant", options.getCustomSystemPrompt());
        assertEquals(2, options.getImageCount());
        assertEquals("1792x1024", options.getImageSize());
        assertFalse(options.isEnableCache());
    }

    @Test
    void testCacheKey() {
        AIGenerationOptions options1 = AIGenerationOptions.builder()
            .model("gpt-4")
            .temperature(0.7)
            .maxTokens(1000)
            .build();
        
        AIGenerationOptions options2 = AIGenerationOptions.builder()
            .model("gpt-4")
            .temperature(0.7)
            .maxTokens(1000)
            .build();
        
        assertEquals(options1.getCacheKey(), options2.getCacheKey());
    }

    @Test
    void testCacheKeyDiffersByModel() {
        AIGenerationOptions options1 = AIGenerationOptions.builder()
            .model("gpt-4")
            .build();
        
        AIGenerationOptions options2 = AIGenerationOptions.builder()
            .model("gpt-3.5-turbo")
            .build();
        
        assertNotEquals(options1.getCacheKey(), options2.getCacheKey());
    }

    @Test
    void testAdditionalParams() {
        AIGenerationOptions options = AIGenerationOptions.builder()
            .additionalParam("key1", "value1")
            .additionalParam("key2", 123)
            .build();
        
        assertEquals("value1", options.getAdditionalParams().get("key1"));
        assertEquals(123, options.getAdditionalParams().get("key2"));
    }

    @Test
    void testFluentInterface() {
        AIGenerationOptions options = AIGenerationOptions.builder()
            .model("custom-model")
            .temperature(0.5)
            .maxTokens(500)
            .customSystemPrompt("Test prompt")
            .imageCount(3)
            .imageSize("512x512")
            .enableCache(true)
            .additionalParam("extra", "param")
            .build();
        
        assertNotNull(options);
    }
}