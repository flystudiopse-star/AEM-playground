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

import static org.junit.jupiter.api.Assertions.*;

class RelatedContentSuggestionTest {

    @Test
    void testCreateWithAllFields() {
        RelatedContentSuggestion suggestion = RelatedContentSuggestion.create(
            "content-1", "Related Article", "/content/articles/related", 0.9, "similar", "Same topic");

        assertEquals("content-1", suggestion.getContentId());
        assertEquals("Related Article", suggestion.getTitle());
        assertEquals("/content/articles/related", suggestion.getPath());
        assertEquals(0.9, suggestion.getSimilarityScore());
        assertEquals("similar", suggestion.getRelationType());
        assertEquals("Same topic", suggestion.getMatchReason());
    }

    @Test
    void testSimilar() {
        RelatedContentSuggestion suggestion = RelatedContentSuggestion.similar(
            "content-2", "Similar Page", "/content/pages/similar", 0.85);

        assertEquals("content-2", suggestion.getContentId());
        assertEquals("Similar Page", suggestion.getTitle());
        assertEquals("/content/pages/similar", suggestion.getPath());
        assertEquals(0.85, suggestion.getSimilarityScore());
        assertEquals("similar", suggestion.getRelationType());
        assertNull(suggestion.getMatchReason());
    }

    @Test
    void testIsHighlySimilarWithHighSimilarity() {
        RelatedContentSuggestion suggestion = RelatedContentSuggestion.similar("id", "Title", "path", 0.9);
        assertTrue(suggestion.isHighlySimilar());
    }

    @Test
    void testIsHighlySimilarWithLowSimilarity() {
        RelatedContentSuggestion suggestion = RelatedContentSuggestion.similar("id", "Title", "path", 0.5);
        assertFalse(suggestion.isHighlySimilar());
    }

    @Test
    void testIsHighlySimilarAtThreshold() {
        RelatedContentSuggestion suggestion = RelatedContentSuggestion.similar("id", "Title", "path", 0.8);
        assertTrue(suggestion.isHighlySimilar());
    }

    @Test
    void testGetters() {
        RelatedContentSuggestion suggestion = RelatedContentSuggestion.create(
            "test-id", "Test Title", "/test/path", 0.75, "related", "keyword match");

        assertEquals("test-id", suggestion.getContentId());
        assertEquals("Test Title", suggestion.getTitle());
        assertEquals("/test/path", suggestion.getPath());
        assertEquals(0.75, suggestion.getSimilarityScore());
        assertEquals("related", suggestion.getRelationType());
        assertEquals("keyword match", suggestion.getMatchReason());
    }
}