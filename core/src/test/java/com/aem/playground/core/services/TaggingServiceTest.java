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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaggingServiceTest {

    @Test
    void testTaggingResultSuccess() {
        List<ContentTag> tags = Arrays.asList(
            ContentTag.aiGenerated("java", "programming", 0.9),
            ContentTag.aiGenerated("coding", "skill", 0.8)
        );

        TaggingService.TaggingResult result = TaggingService.TaggingResult.success(tags, 150L);

        assertTrue(result.isSuccess());
        assertEquals(tags, result.getTags());
        assertEquals(150L, result.getProcessingTimeMs());
        assertNull(result.getError());
    }

    @Test
    void testTaggingResultError() {
        TaggingService.TaggingResult result = TaggingService.TaggingResult.error("API error", 50L);

        assertFalse(result.isSuccess());
        assertNull(result.getTags());
        assertEquals("API error", result.getError());
        assertEquals(50L, result.getProcessingTimeMs());
    }

    @Test
    void testTaggingOptionsBuilder() {
        TaggingService.TaggingOptions options = TaggingService.TaggingOptions.builder()
            .maxTags(20)
            .minConfidence(0.8)
            .includeCategories(false)
            .dedupe(false)
            .build();

        assertEquals(20, options.getMaxTags());
        assertEquals(0.8, options.getMinConfidence());
        assertFalse(options.isIncludeCategories());
        assertFalse(options.isDedupe());
    }

    @Test
    void testTaggingOptionsDefaults() {
        TaggingService.TaggingOptions options = TaggingService.TaggingOptions.defaultOptions();

        assertEquals(10, options.getMaxTags());
        assertEquals(0.5, options.getMinConfidence());
        assertTrue(options.isIncludeCategories());
        assertTrue(options.isDedupe());
    }

    @Test
    void testTaggingOptionsBuilderWithDefaults() {
        TaggingService.TaggingOptions options = TaggingService.TaggingOptions.builder().build();

        assertEquals(10, options.getMaxTags());
        assertEquals(0.5, options.getMinConfidence());
    }

    @Test
    void testTaggingResultProcessingTimeAlwaysPositive() {
        TaggingService.TaggingResult successResult = TaggingService.TaggingResult.success(
            Collections.emptyList(), 0L);
        assertTrue(successResult.getProcessingTimeMs() >= 0);

        TaggingService.TaggingResult errorResult = TaggingService.TaggingResult.error("error", 0L);
        assertTrue(errorResult.getProcessingTimeMs() >= 0);
    }
}