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

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIGenerationResult;
import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AssetCategory;
import com.aem.playground.core.services.AssetKeyword;
import com.aem.playground.core.services.AssetMetadata;
import com.aem.playground.core.services.AssetRelationship;
import com.aem.playground.core.services.DAMMetadataManagerConfig;
import com.aem.playground.core.services.DAMMetadataManagerService;
import com.aem.playground.core.services.IntelligentTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DAMMetadataManagerServiceImplTest {

    @Mock
    private AIService aiService;

    private DAMMetadataManagerServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new DAMMetadataManagerServiceImpl();
        
        DAMMetadataManagerConfig config = new TestDAMMetadataManagerConfig();
        service.activate(config);
    }

    @Test
    void testExtractMetadataWithValidAsset() {
        AssetMetadata asset = AssetMetadata.builder()
                .assetId("asset-1")
                .assetPath("/content/dam/image.jpg")
                .assetName("image.jpg")
                .assetType("image/jpeg")
                .title("Sample Image")
                .description("A beautiful sample image")
                .build();

        DAMMetadataManagerService.MetadataExtractionOptions options = 
                DAMMetadataManagerService.MetadataExtractionOptions.defaultOptions();

        DAMMetadataManagerService.MetadataExtractionResult result = service.extractMetadata(asset, options);

        assertNotNull(result);
        assertTrue(result.getProcessingTimeMs() >= 0);
    }

    @Test
    void testExtractMetadataWithNullAsset() {
        DAMMetadataManagerService.MetadataExtractionResult result = service.extractMetadata(
                null, DAMMetadataManagerService.MetadataExtractionOptions.defaultOptions());

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testExtractMetadataWithDisabledFeature() {
        DAMMetadataManagerServiceImpl disabledService = new DAMMetadataManagerServiceImpl();
        disabledService.activate(new TestDAMMetadataManagerConfigDisabled());
        
        AssetMetadata asset = AssetMetadata.builder()
                .assetId("asset-1")
                .assetPath("/content/dam/image.jpg")
                .assetType("image/jpeg")
                .build();

        DAMMetadataManagerService.MetadataExtractionResult result = disabledService.extractMetadata(
                asset, DAMMetadataManagerService.MetadataExtractionOptions.defaultOptions());

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testGenerateSmartKeywordsWithValidAsset() {
        AssetMetadata asset = AssetMetadata.builder()
                .assetId("asset-1")
                .assetPath("/content/dam/photo.jpg")
                .assetName("photo.jpg")
                .title("Beach Sunset")
                .description("Beautiful sunset over the beach with orange sky")
                .assetType("image/jpeg")
                .build();

        DAMMetadataManagerService.KeywordOptions options = 
                DAMMetadataManagerService.KeywordOptions.builder()
                        .maxKeywords(5)
                        .minConfidence(0.3)
                        .build();

        DAMMetadataManagerService.KeywordGenerationResult result = service.generateSmartKeywords(asset, options);

        assertNotNull(result);
        assertTrue(result.getProcessingTimeMs() >= 0);
    }

    @Test
    void testGenerateSmartKeywordsWithNullAsset() {
        DAMMetadataManagerService.KeywordGenerationResult result = service.generateSmartKeywords(
                null, DAMMetadataManagerService.KeywordOptions.defaultOptions());

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testGenerateSmartKeywordsWithDisabledFeature() {
        DAMMetadataManagerServiceImpl disabledService = new DAMMetadataManagerServiceImpl();
        disabledService.activate(new TestDAMMetadataManagerConfigDisabled());
        
        AssetMetadata asset = AssetMetadata.builder()
                .assetId("asset-1")
                .assetPath("/content/dam/photo.jpg")
                .assetType("image/jpeg")
                .keywords(Arrays.asList("existing", "keywords"))
                .build();

        DAMMetadataManagerService.KeywordGenerationResult result = disabledService.generateSmartKeywords(
                asset, DAMMetadataManagerService.KeywordOptions.defaultOptions());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getKeywords().size());
    }

    @Test
    void testCreateContentBasedCategories() {
        List<AssetMetadata> assets = Arrays.asList(
                AssetMetadata.builder()
                        .assetId("asset-1")
                        .assetPath("/content/dam/cat1.jpg")
                        .assetName("cat1.jpg")
                        .assetType("image/jpeg")
                        .title("Nature Photo")
                        .build(),
                AssetMetadata.builder()
                        .assetId("asset-2")
                        .assetPath("/content/dam/cat2.jpg")
                        .assetName("cat2.jpg")
                        .assetType("image/jpeg")
                        .title("City Photo")
                        .build(),
                AssetMetadata.builder()
                        .assetId("asset-3")
                        .assetPath("/content/dam/cat3.jpg")
                        .assetName("cat3.jpg")
                        .assetType("image/jpeg")
                        .title("Beach Photo")
                        .build()
        );

        List<AssetCategory> categories = service.createContentBasedCategories(assets, 5);

        assertNotNull(categories);
    }

    @Test
    void testCreateContentBasedCategoriesWithEmptyList() {
        List<AssetCategory> categories = service.createContentBasedCategories(new ArrayList<>(), 5);

        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }

    @Test
    void testCreateContentBasedCategoriesWithNullList() {
        List<AssetCategory> categories = service.createContentBasedCategories(null, 5);

        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }

    @Test
    void testSuggestAssetRelationships() {
        AssetMetadata targetAsset = AssetMetadata.builder()
                .assetId("target-1")
                .assetPath("/content/dam/target.jpg")
                .assetName("target.jpg")
                .title("Target Image")
                .description("A target image for relationships")
                .build();

        List<AssetMetadata> existingAssets = Arrays.asList(
                AssetMetadata.builder()
                        .assetId("asset-1")
                        .assetPath("/content/dam/related1.jpg")
                        .assetName("related1.jpg")
                        .title("Related Image 1")
                        .build(),
                AssetMetadata.builder()
                        .assetId("asset-2")
                        .assetPath("/content/dam/related2.jpg")
                        .assetName("related2.jpg")
                        .title("Related Image 2")
                        .build()
        );

        List<AssetRelationship> relationships = service.suggestAssetRelationships(
                targetAsset, existingAssets, 5);

        assertNotNull(relationships);
    }

    @Test
    void testSuggestAssetRelationshipsWithNullTarget() {
        List<AssetRelationship> relationships = service.suggestAssetRelationships(
                null, new ArrayList<>(), 5);

        assertNotNull(relationships);
        assertTrue(relationships.isEmpty());
    }

    @Test
    void testSuggestAssetRelationshipsWithEmptyList() {
        AssetMetadata asset = AssetMetadata.builder()
                .assetId("asset-1")
                .assetPath("/content/dam/test.jpg")
                .build();

        List<AssetRelationship> relationships = service.suggestAssetRelationships(
                asset, new ArrayList<>(), 5);

        assertNotNull(relationships);
        assertTrue(relationships.isEmpty());
    }

    @Test
    void testAddIntelligentTags() {
        AssetMetadata asset = AssetMetadata.builder()
                .assetId("asset-1")
                .assetPath("/content/dam/photo.jpg")
                .assetName("landscape.jpg")
                .title("Mountain Landscape")
                .description("Beautiful mountain landscape at sunset")
                .assetType("image/jpeg")
                .keywords(Arrays.asList("nature", "mountain"))
                .build();

        DAMMetadataManagerService.TaggingOptions options = 
                DAMMetadataManagerService.TaggingOptions.builder()
                        .maxTags(5)
                        .minConfidence(0.3)
                        .includeTechnicalTags(true)
                        .build();

        DAMMetadataManagerService.IntelligentTaggingResult result = service.addIntelligentTags(asset, options);

        assertNotNull(result);
        assertTrue(result.getProcessingTimeMs() >= 0);
    }

    @Test
    void testAddIntelligentTagsWithNullAsset() {
        DAMMetadataManagerService.IntelligentTaggingResult result = service.addIntelligentTags(
                null, DAMMetadataManagerService.TaggingOptions.defaultOptions());

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testAddIntelligentTagsWithDisabledFeature() {
        DAMMetadataManagerServiceImpl disabledService = new DAMMetadataManagerServiceImpl();
        disabledService.activate(new TestDAMMetadataManagerConfigDisabled());
        
        AssetMetadata asset = AssetMetadata.builder()
                .assetId("asset-1")
                .assetPath("/content/dam/photo.jpg")
                .assetType("image/jpeg")
                .existingTags(Arrays.asList("existing-tag"))
                .build();

        DAMMetadataManagerService.IntelligentTaggingResult result = disabledService.addIntelligentTags(
                asset, DAMMetadataManagerService.TaggingOptions.defaultOptions());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTags().size());
    }

    @Test
    void testKeywordOptionsBuilder() {
        DAMMetadataManagerService.KeywordOptions options = 
                DAMMetadataManagerService.KeywordOptions.builder()
                        .maxKeywords(15)
                        .minConfidence(0.7)
                        .includeSynonyms(true)
                        .dedupe(false)
                        .build();

        assertEquals(15, options.getMaxKeywords());
        assertEquals(0.7, options.getMinConfidence());
        assertTrue(options.isIncludeSynonyms());
        assertFalse(options.isDedupe());
    }

    @Test
    void testKeywordOptionsDefaults() {
        DAMMetadataManagerService.KeywordOptions options = 
                DAMMetadataManagerService.KeywordOptions.defaultOptions();

        assertEquals(10, options.getMaxKeywords());
        assertEquals(0.5, options.getMinConfidence());
        assertTrue(options.isIncludeSynonyms());
        assertTrue(options.isDedupe());
    }

    @Test
    void testTaggingOptionsBuilder() {
        DAMMetadataManagerService.TaggingOptions options = 
                DAMMetadataManagerService.TaggingOptions.builder()
                        .maxTags(15)
                        .minConfidence(0.7)
                        .includeCategories(true)
                        .includeTechnicalTags(false)
                        .build();

        assertEquals(15, options.getMaxTags());
        assertEquals(0.7, options.getMinConfidence());
        assertTrue(options.isIncludeCategories());
        assertFalse(options.isIncludeTechnicalTags());
    }

    @Test
    void testTaggingOptionsDefaults() {
        DAMMetadataManagerService.TaggingOptions options = 
                DAMMetadataManagerService.TaggingOptions.defaultOptions();

        assertEquals(10, options.getMaxTags());
        assertEquals(0.5, options.getMinConfidence());
        assertTrue(options.isIncludeCategories());
        assertTrue(options.isIncludeTechnicalTags());
    }

    @Test
    void testMetadataExtractionOptionsBuilder() {
        DAMMetadataManagerService.MetadataExtractionOptions options = 
                DAMMetadataManagerService.MetadataExtractionOptions.builder()
                        .extractTitle(false)
                        .extractDescription(true)
                        .extractKeywords(true)
                        .extractDate(false)
                        .build();

        assertFalse(options.isExtractTitle());
        assertTrue(options.isExtractDescription());
        assertTrue(options.isExtractKeywords());
        assertFalse(options.isExtractDate());
    }

    @Test
    void testMetadataExtractionOptionsDefaults() {
        DAMMetadataManagerService.MetadataExtractionOptions options = 
                DAMMetadataManagerService.MetadataExtractionOptions.defaultOptions();

        assertTrue(options.isExtractTitle());
        assertTrue(options.isExtractDescription());
        assertTrue(options.isExtractKeywords());
        assertTrue(options.isExtractDate());
        assertTrue(options.isExtractAuthor());
        assertTrue(options.isExtractLocation());
    }

    @Test
    void testAssetKeywordCreation() {
        AssetKeyword keyword = AssetKeyword.aiGenerated("nature", "outdoor", 0.9);

        assertEquals("nature", keyword.getKeyword());
        assertEquals("outdoor", keyword.getCategory());
        assertEquals(0.9, keyword.getConfidence());
        assertTrue(keyword.isHighConfidence());
    }

    @Test
    void testIntelligentTagCreation() {
        IntelligentTag tag = IntelligentTag.aiGenerated("landscape", "nature", 0.85);

        assertEquals("landscape", tag.getTagName());
        assertEquals("nature", tag.getCategory());
        assertEquals(0.85, tag.getConfidence());
        assertTrue(tag.isHighConfidence());
        assertFalse(tag.isTechnical());
    }

    @Test
    void testIntelligentTagTechnical() {
        IntelligentTag tag = IntelligentTag.technical("jpeg", "format");

        assertEquals("jpeg", tag.getTagName());
        assertTrue(tag.isTechnical());
    }

    @Test
    void testAssetCategoryCreation() {
        AssetCategory category = AssetCategory.simple("Nature", 0.95);

        assertEquals("Nature", category.getName());
        assertEquals(0.95, category.getRelevanceScore());
    }

    @Test
    void testAssetRelationshipCreation() {
        AssetRelationship relationship = AssetRelationship.similar(
                "source-1", "target-1", "/path/to/asset", "Target Asset", 0.9);

        assertEquals("source-1", relationship.getSourceAssetId());
        assertEquals("target-1", relationship.getTargetAssetId());
        assertTrue(relationship.isHighlySimilar());
    }

    private static abstract class TestDAMMetadataManagerConfig implements DAMMetadataManagerConfig {
        
        public int cacheSize() { return 100; }
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
        public int max_keywords() {
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
        public boolean enable_smart_keywords() {
            return true;
        }

        @Override
        public boolean enable_categories() {
            return true;
        }

        @Override
        public boolean enable_relationships() {
            return true;
        }

        @Override
        public boolean enable_intelligent_tagging() {
            return true;
        }

        @Override
        public boolean enable_metadata_extraction() {
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

    private static abstract class TestDAMMetadataManagerConfigDisabled implements DAMMetadataManagerConfig {
        
        public int cacheSize() { return 0; }
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
        public int max_keywords() {
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
        public boolean enable_smart_keywords() {
            return false;
        }

        @Override
        public boolean enable_categories() {
            return false;
        }

        @Override
        public boolean enable_relationships() {
            return false;
        }

        @Override
        public boolean enable_intelligent_tagging() {
            return false;
        }

        @Override
        public boolean enable_metadata_extraction() {
            return false;
        }

        @Override
        public boolean cache_enabled() {
            return false;
        }

        @Override
        public int cache_size() {
            return 100;
        }
    }
}