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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AIServiceTest {

    @Test
    void testResultSuccess() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", "gpt-4");
        metadata.put("tokens", 100);

        AIService.AIGenerationResult result = AIService.AIGenerationResult.success("Generated text", metadata);

        assertTrue(result.isSuccess());
        assertEquals("Generated text", result.getContent());
        assertEquals("gpt-4", result.getMetadata().get("model"));
        assertNull(result.getError());
    }

    @Test
    void testResultError() {
        AIService.AIGenerationResult result = AIService.AIGenerationResult.error("API error occurred");

        assertFalse(result.isSuccess());
        assertNull(result.getContent());
        assertNull(result.getMetadata());
        assertEquals("API error occurred", result.getError());
    }

    @Test
    void testResultGettersReturnNullOnError() {
        AIService.AIGenerationResult result = AIService.AIGenerationResult.error("Error");

        assertNull(result.getContent());
        assertNull(result.getMetadata());
    }

    @Test
    void testResultMetadataCanBeNull() {
        AIService.AIGenerationResult result = AIService.AIGenerationResult.success("Content", null);

        assertTrue(result.isSuccess());
        assertEquals("Content", result.getContent());
        assertNull(result.getMetadata());
    }
}