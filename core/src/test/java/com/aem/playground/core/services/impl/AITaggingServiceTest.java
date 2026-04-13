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

import com.aem.playground.core.services.ContentCategory;
import com.aem.playground.core.services.ContentTag;
import com.aem.playground.core.services.RelatedContentSuggestion;
import com.aem.playground.core.services.TaggableContent;
import com.aem.playground.core.services.TaggingService;
import com.aem.playground.core.services.TagManagerResult;
import com.aem.playground.core.services.TaggingServiceConfig;
import com.aem.playground.core.services.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AITaggingServiceTest {

    @Mock
    private AIService aiService;

    private AITaggingService taggingService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        taggingService = new AITaggingService();
        
        TaggingServiceConfig config = new TestTaggingServiceConfig();
        
        taggingService.activate(config);
    }

    @Test
    void testAutoTagContentWithValidContent() {
        TaggableContent content = TaggableContent.fromText("content-1", "Java Programming Guide", 
            "Learn Java programming from basics to advanced concepts including OOP, collections, and design patterns.");

        TaggingService.TaggingOptions options = TaggingService.TaggingOptions.builder()
            .maxTags(5)
            .minConfidence(0.3)
            .build();

        TaggingService.TaggingResult result = taggingService.autoTagContent(content, options);

        assertNotNull(result);
        assertTrue(result.getProcessingTimeMs() >= 0);
    }

    @Test
    void testAutoTagContentWithNullContent() {
        TaggingService.TaggingResult result = taggingService.autoTagContent(null, TaggingService.TaggingOptions.defaultOptions());

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testAutoTagContentWithEmptyText() {
        TaggableContent content = TaggableContent.fromText("content-1", "Empty Content", "");

        TaggingService.TaggingResult result = taggingService.autoTagContent(content, TaggingService.TaggingOptions.defaultOptions());

        assertFalse(result.isSuccess());
    }

    @Test
    void testGenerateCategories() {
        String text = "This article covers cloud computing, AWS services, and deployment strategies.";

        List<ContentCategory> categories = taggingService.generateCategories(text, 5);

        assertNotNull(categories);
    }

    @Test
    void testGenerateCategoriesWithEmptyText() {
        List<ContentCategory> categories = taggingService.generateCategories("", 5);

        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }

    @Test
    void testBuildTaxonomyWithContentList() {
        List<TaggableContent> contentItems = Arrays.asList(
            TaggableContent.fromText("1", "Java Tutorial", "Learn Java programming"),
            TaggableContent.fromText("2", "Python Guide", "Learn Python programming"),
            TaggableContent.fromText("3", "Web Development", "Build websites with HTML CSS JS")
        );

        com.aem.playground.core.services.TaxonomyNode taxonomy = taggingService.buildTaxonomy(contentItems);

        assertNotNull(taxonomy);
        assertTrue(taxonomy.isRoot());
    }

    @Test
    void testBuildTaxonomyWithEmptyList() {
        com.aem.playground.core.services.TaxonomyNode taxonomy = taggingService.buildTaxonomy(new ArrayList<>());

        assertNotNull(taxonomy);
        assertTrue(taxonomy.isRoot());
    }

    @Test
    void testBuildTaxonomyWithNullList() {
        com.aem.playground.core.services.TaxonomyNode taxonomy = taggingService.buildTaxonomy(null);

        assertNotNull(taxonomy);
        assertTrue(taxonomy.isRoot());
    }

    @Test
    void testSuggestRelatedContent() {
        TaggableContent content = TaggableContent.fromText("content-1", "Spring Boot Guide", 
            "Learn Spring Boot framework for building microservices.");

        List<RelatedContentSuggestion> suggestions = taggingService.suggestRelatedContent(content, 5);

        assertNotNull(suggestions);
    }

    @Test
    void testSuggestRelatedContentWithNullContent() {
        List<RelatedContentSuggestion> suggestions = taggingService.suggestRelatedContent(null, 5);

        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testManageTagWithAddAction() {
        TagManagerResult result = taggingService.manageTag("java", "add", "programming");

        assertTrue(result.isSuccess());
        assertEquals("Tag added: java", result.getError());
    }

    @Test
    void testManageTagWithRemoveAction() {
        TagManagerResult result = taggingService.manageTag("java", "remove", null);

        assertTrue(result.isSuccess());
        assertEquals("Tag removed: java", result.getError());
    }

    @Test
    void testManageTagWithUpdateAction() {
        TagManagerResult result = taggingService.manageTag("java", "update", "language");

        assertTrue(result.isSuccess());
        assertEquals("Tag updated: java", result.getError());
    }

    @Test
    void testManageTagWithListAction() {
        TagManagerResult result = taggingService.manageTag("", "list", null);

        assertTrue(result.isSuccess());
        assertEquals("Tags listed", result.getError());
    }

    @Test
    void testManageTagWithUnknownAction() {
        TagManagerResult result = taggingService.manageTag("java", "invalid", null);

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testManageTagWithEmptyTagName() {
        TagManagerResult result = taggingService.manageTag("", "add", null);

        assertFalse(result.isSuccess());
    }

    @Test
    void testTaggingOptionsBuilder() {
        TaggingService.TaggingOptions options = TaggingService.TaggingOptions.builder()
            .maxTags(15)
            .minConfidence(0.7)
            .includeCategories(true)
            .dedupe(false)
            .build();

        assertEquals(15, options.getMaxTags());
        assertEquals(0.7, options.getMinConfidence());
        assertTrue(options.isIncludeCategories());
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

    private static abstract class TestTaggingServiceConfig implements TaggingServiceConfig {
        
        public Class<? extends java.lang.annotation.Annotation> annotationType() { return TaggingServiceConfig.class; }
        @Override
        public String ai_service_url() {
            return "https://api.openai.com/v1/chat/completions";
        }

        @Override
        public String api_key() {
            return "test-key";
        }

        @Override
        public String model() {
            return "gpt-4";
        }

        @Override
        public int max_tags() {
            return 10;
        }

        @Override
        public double min_confidence() {
            return 0.5;
        }

        @Override
        public int max_tokens() {
            return 2000;
        }

        @Override
        public double temperature() {
            return 0.7;
        }

        @Override
        public boolean enable_categories() {
            return true;
        }

        @Override
        public boolean enable_taxonomy() {
            return true;
        }

        @Override
        public boolean enable_related_content() {
            return true;
        }

        @Override
        public boolean cache_enabled() {
            return true;
        }

        @Override
        public int cache_size() {
            return 100;
        }
    }
}