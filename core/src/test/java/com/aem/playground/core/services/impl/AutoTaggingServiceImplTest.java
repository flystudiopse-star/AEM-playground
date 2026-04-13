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
import com.aem.playground.core.services.AutoTaggingConfig;
import com.aem.playground.core.services.AutoTaggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AutoTaggingServiceImplTest {

    @Mock
    private AIService aiService;

    private AutoTaggingServiceImpl autoTaggingService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        autoTaggingService = new AutoTaggingServiceImpl();
        
        AutoTaggingConfig config = Mockito.mock(AutoTaggingConfig.class);
        autoTaggingService.activate(config);
    }

    @Test
    void testAutoTagContentWithValidContent() {
        AutoTaggingService.AutoTaggingResult result = autoTaggingService.autoTagContent(
            "content-1", 
            "Java Programming Guide", 
            "Learn Java programming from basics to advanced concepts including OOP, collections, and design patterns.",
            null
        );

        assertNotNull(result);
        assertTrue(result.getProcessingTimeMs() >= 0);
    }

    @Test
    void testAutoTagContentWithEmptyContent() {
        AutoTaggingService.AutoTaggingResult result = autoTaggingService.autoTagContent(
            "content-1", 
            "Empty Content", 
            "",
            null
        );

        assertNotNull(result);
    }

    @Test
    void testAutoTagContentWithNullContent() {
        AutoTaggingService.AutoTaggingResult result = autoTaggingService.autoTagContent(
            null, 
            null, 
            null,
            null
        );

        assertNotNull(result);
    }

    @Test
    void testAutoTagContentWithExistingTags() {
        List<String> existingTags = Arrays.asList("java", "programming");
        
        AutoTaggingService.AutoTaggingResult result = autoTaggingService.autoTagContent(
            "content-1", 
            "Java Tutorial", 
            "Learn Java programming with examples",
            existingTags
        );

        assertNotNull(result);
        assertTrue(result.getProcessingTimeMs() >= 0);
    }

    @Test
    void testSuggestTags() {
        List<AutoTaggingService.TagSuggestion> suggestions = autoTaggingService.suggestTags(
            "This is about cloud computing and AWS services", 
            5
        );

        assertNotNull(suggestions);
    }

    @Test
    void testSuggestTagsWithEmptyContent() {
        List<AutoTaggingService.TagSuggestion> suggestions = autoTaggingService.suggestTags(
            "", 
            5
        );

        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testLearnFromUserTags() {
        List<String> userTags = Arrays.asList("cloud", "aws", "devops");
        
        autoTaggingService.learnFromUserTags("content-1", userTags);

        List<AutoTaggingService.TagSuggestion> learned = autoTaggingService.getSuggestedTagsForContent("content-1");
        
        assertNotNull(learned);
    }

    @Test
    void testLearnFromUserTagsMultipleContent() {
        List<String> tags1 = Arrays.asList("java", "spring");
        List<String> tags2 = Arrays.asList("python", "django");
        
        autoTaggingService.learnFromUserTags("content-1", tags1);
        autoTaggingService.learnFromUserTags("content-2", tags2);

        Map<String, Double> stats = autoTaggingService.getTagUsageStats();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("java"));
        assertTrue(stats.containsKey("python"));
    }

    @Test
    void testGetTagCategories() {
        List<AutoTaggingService.TagCategory> categories = autoTaggingService.getTagCategories();

        assertNotNull(categories);
    }

    @Test
    void testGetSuggestedTagsForContent() {
        List<String> userTags = Arrays.asList("tutorial", "beginner");
        autoTaggingService.learnFromUserTags("content-1", userTags);

        List<AutoTaggingService.TagSuggestion> suggestions = autoTaggingService.getSuggestedTagsForContent("content-1");
        
        assertNotNull(suggestions);
    }

    @Test
    void testGetSuggestedTagsForUnknownContent() {
        List<AutoTaggingService.TagSuggestion> suggestions = autoTaggingService.getSuggestedTagsForContent("unknown-content");
        
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testGetTagUsageStats() {
        Map<String, Double> stats = autoTaggingService.getTagUsageStats();
        
        assertNotNull(stats);
    }

    @Test
    void testClearLearningData() {
        List<String> userTags = Arrays.asList("test", "tags");
        autoTaggingService.learnFromUserTags("content-1", userTags);
        
        autoTaggingService.clearLearningData();
        
        Map<String, Double> stats = autoTaggingService.getTagUsageStats();
        assertTrue(stats.isEmpty());
    }

    @Test
    void testGetConfigurationValues() {
        assertEquals("gpt-4", autoTaggingService.getModel());
        assertEquals(10, autoTaggingService.getMaxTags());
        assertEquals(0.5, autoTaggingService.getMinConfidence(), 0.01);
        assertTrue(autoTaggingService.isEnableLearning());
    }

    @Test
    void testTagSuggestionBuilder() {
        AutoTaggingService.TagSuggestion suggestion = AutoTaggingService.TagSuggestion.create(
            "java", "programming", 0.9, "ai"
        );
        
        assertEquals("java", suggestion.getTagName());
        assertEquals("programming", suggestion.getCategory());
        assertEquals(0.9, suggestion.getConfidence(), 0.01);
        assertEquals("ai", suggestion.getSource());
    }

    @Test
    void testTagCategoryBuilder() {
        List<String> subCategories = Arrays.asList("sub1", "sub2");
        List<String> allowedTags = Arrays.asList("tag1", "tag2");
        
        AutoTaggingService.TagCategory category = AutoTaggingService.TagCategory.create(
            "technology", "root", subCategories, allowedTags
        );
        
        assertEquals("technology", category.getName());
        assertEquals("root", category.getParentCategory());
        assertEquals(2, category.getSubCategories().size());
        assertEquals(2, category.getAllowedTags().size());
    }

    @Test
    void testAutoTaggingResultSuccess() {
        List<AutoTaggingService.TagSuggestion> tags = new ArrayList<>();
        tags.add(AutoTaggingService.TagSuggestion.create("test", "category", 0.8, "ai"));
        
        AutoTaggingService.AutoTaggingResult result = AutoTaggingService.AutoTaggingResult.success(tags, 100L);
        
        assertTrue(result.isSuccess());
        assertEquals(1, result.getSuggestedTags().size());
        assertEquals(100L, result.getProcessingTimeMs());
    }

    @Test
    void testAutoTaggingResultError() {
        AutoTaggingService.AutoTaggingResult result = AutoTaggingService.AutoTaggingResult.error("Error message", 50L);
        
        assertFalse(result.isSuccess());
        assertNull(result.getSuggestedTags());
        assertEquals("Error message", result.getError());
        assertEquals(50L, result.getProcessingTimeMs());
    }

    private static abstract class TestAutoTaggingConfig implements AutoTaggingConfig {
        
        public Class<? extends java.lang.annotation.Annotation> annotationType() { return AutoTaggingConfig.class; }
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
        public double temperature() {
            return 0.7;
        }

        @Override
        public int max_tokens() {
            return 1000;
        }

        @Override
        public boolean enable_learning() {
            return true;
        }

        @Override
        public double learning_rate() {
            return 0.3;
        }

        @Override
        public String tag_hierarchy() {
            return "[]";
        }

        @Override
        public String tag_categories() {
            return "[]";
        }

        @Override
        public boolean cache_enabled() {
            return true;
        }

        @Override
        public int cache_size() {
            return 200;
        }
    }
}