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

import com.aem.playground.core.services.AssetRecommendationResult;
import com.aem.playground.core.services.AssetRecommendationResult.RecommendedAsset;
import com.aem.playground.core.services.AssetRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetRecommenderServiceImplTest {

    @Mock
    private com.aem.playground.core.services.AIService aiService;

    private AssetRecommenderServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new AssetRecommenderServiceImpl();

        AssetRecommenderTestConfig config = new AssetRecommenderTestConfig();
        service.activate(config);
        service.aiService = aiService;
    }

    @Test
    void testRecommendAssetsForPageWithValidInput() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Image");
        metadata.put("description", "A test image for testing");
        metadata.put("type", "image");

        service.indexAssetMetadata("/content/dam/test-image.jpg", metadata);

        AssetRecommendationResult result = service.recommendAssetsForPage("/content/page/test", "This is a test page about testing", 5);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAssets());
        assertTrue(result.getMetadata().containsKey("pagePath"));
        assertEquals("/content/page/test", result.getMetadata().get("pagePath"));
    }

    @Test
    void testRecommendAssetsForPageWithEmptyPagePath() {
        AssetRecommendationResult result = service.recommendAssetsForPage("", "Some content", 5);

        assertFalse(result.isSuccess());
        assertEquals("Page path and content are required", result.getError());
    }

    @Test
    void testRecommendAssetsForPageWithNullContent() {
        AssetRecommendationResult result = service.recommendAssetsForPage("/content/page/test", null, 5);

        assertFalse(result.isSuccess());
        assertEquals("Page path and content are required", result.getError());
    }

    @Test
    void testRecommendAssetsByText() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Product Image");
        metadata.put("description", "A product image for e-commerce");
        metadata.put("type", "image");

        service.indexAssetMetadata("/content/dam/product.jpg", metadata);

        AssetRecommendationResult result = service.recommendAssetsByText("product e-commerce", 5);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAssets());
        assertTrue(result.getMetadata().containsKey("query"));
        assertEquals("product e-commerce", result.getMetadata().get("query"));
    }

    @Test
    void testRecommendAssetsByTextWithEmptyQuery() {
        AssetRecommendationResult result = service.recommendAssetsByText("", 5);

        assertFalse(result.isSuccess());
        assertEquals("Text query is required", result.getError());
    }

    @Test
    void testFindSimilarAssets() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Nature Image");
        metadata.put("description", "A beautiful landscape with mountains and rivers");
        metadata.put("type", "image");

        service.indexAssetMetadata("/content/dam/nature.jpg", metadata);

        AssetRecommendationResult result = service.findSimilarAssets("/content/dam/nature.jpg", 5);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAssets());
    }

    @Test
    void testFindSimilarAssetsWithUnindexedAsset() {
        AssetRecommendationResult result = service.findSimilarAssets("/content/dam/nonexistent.jpg", 5);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not found in index"));
    }

    @Test
    void testFindSimilarAssetsWithEmptyPath() {
        AssetRecommendationResult result = service.findSimilarAssets("", 5);

        assertFalse(result.isSuccess());
        assertEquals("Asset path is required", result.getError());
    }

    @Test
    void testSuggestImagesForContent() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Hero Banner");
        metadata.put("description", "A hero banner for homepage");
        metadata.put("type", "image");

        service.indexAssetMetadata("/content/dam/hero.jpg", metadata);

        AssetRecommendationResult result = service.suggestImagesForContent("We need a hero banner for our homepage", 5);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAssets());
        assertEquals("image", result.getMetadata().get("type"));
    }

    @Test
    void testSuggestImagesForContentWithEmptyContent() {
        AssetRecommendationResult result = service.suggestImagesForContent("", 5);

        assertFalse(result.isSuccess());
        assertEquals("Content is required", result.getError());
    }

    @Test
    void testCreateAssetCollection() {
        List<String> assetPaths = Arrays.asList(
            "/content/dam/image1.jpg",
            "/content/dam/image2.jpg",
            "/content/dam/image3.jpg"
        );

        List<String> result = service.createAssetCollection("Test Collection", assetPaths);

        assertEquals(3, result.size());
        assertEquals(assetPaths, result);
    }

    @Test
    void testCreateAssetCollectionWithEmptyName() {
        List<String> assetPaths = Arrays.asList("/content/dam/image1.jpg");

        List<String> result = service.createAssetCollection("", assetPaths);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateAssetCollectionWithEmptyAssets() {
        List<String> result = service.createAssetCollection("Test Collection", new ArrayList<>());

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSuggestedCollections() {
        List<String> assetPaths = Arrays.asList("/content/dam/image1.jpg");
        service.createAssetCollection("Product Images", assetPaths);
        service.createAssetCollection("Banner Images", assetPaths);
        service.createAssetCollection("Team Photos", assetPaths);

        List<String> result = service.getSuggestedCollections("product");

        assertEquals(1, result.size());
        assertEquals("Product Images", result.get(0));
    }

    @Test
    void testGetSuggestedCollectionsWithEmptyContext() {
        List<String> assetPaths = Arrays.asList("/content/dam/image1.jpg");
        service.createAssetCollection("Collection1", assetPaths);
        service.createAssetCollection("Collection2", assetPaths);

        List<String> result = service.getSuggestedCollections("");

        assertEquals(2, result.size());
    }

    @Test
    void testIndexAssetMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Asset");
        metadata.put("description", "A test asset description");
        metadata.put("type", "image");
        metadata.put("tags", Arrays.asList("test", "sample"));

        service.indexAssetMetadata("/content/dam/test-asset.jpg", metadata);

        assertEquals(1, service.getIndexedAssetCount());
    }

    @Test
    void testIndexAssetMetadataWithEmptyPath() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test");

        service.indexAssetMetadata("", metadata);

        assertEquals(0, service.getIndexedAssetCount());
    }

    @Test
    void testIndexAssetMetadataWithNullMetadata() {
        service.indexAssetMetadata("/content/dam/test.jpg", null);

        assertEquals(0, service.getIndexedAssetCount());
    }

    @Test
    void testClearAssetIndex() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test");

        service.indexAssetMetadata("/content/dam/test.jpg", metadata);
        assertEquals(1, service.getIndexedAssetCount());

        service.clearAssetIndex();

        assertEquals(0, service.getIndexedAssetCount());
    }

    @Test
    void testRecommendAssetsForPageReturnsDefaultAssets() {
        AssetRecommendationResult result = service.recommendAssetsForPage("/content/page/hero", "This is a hero section", 3);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAssets());
    }

    @Test
    void testKeywordBasedRecommendations() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Mountain Landscape");
        metadata.put("description", "Beautiful mountain view");
        metadata.put("type", "image");

        service.indexAssetMetadata("/content/dam/mountain.jpg", metadata);

        AssetRecommendationResult result = service.recommendAssetsByText("mountain landscape", 5);

        assertTrue(result.isSuccess());
    }

    @Test
    void testRecommendedAssetBuilder() {
        RecommendedAsset asset = RecommendedAsset.builder()
            .assetPath("/content/dam/test.jpg")
            .relevanceScore(0.95)
            .assetType("image")
            .title("Test Image")
            .description("A test image")
            .tags(new HashMap<>())
            .suggestedCollections(Arrays.asList("Collection1"))
            .build();

        assertEquals("/content/dam/test.jpg", asset.getAssetPath());
        assertEquals(0.95, asset.getRelevanceScore());
        assertEquals("image", asset.getAssetType());
        assertEquals("Test Image", asset.getTitle());
        assertEquals("A test image", asset.getDescription());
    }

    @Test
    void testRecommendedAssetStaticCreate() {
        RecommendedAsset asset1 = RecommendedAsset.create("/content/dam/test.jpg", 0.8, "image");
        assertEquals("/content/dam/test.jpg", asset1.getAssetPath());
        assertEquals(0.8, asset1.getRelevanceScore());

        RecommendedAsset asset2 = RecommendedAsset.create("/content/dam/test2.jpg", 0.7, "video", "Video Title", "Video Description");
        assertEquals("Video Title", asset2.getTitle());
        assertEquals("Video Description", asset2.getDescription());
    }

    @Test
    void testAssetRecommendationResultSuccess() {
        List<RecommendedAsset> assets = Arrays.asList(
            RecommendedAsset.create("/content/dam/1.jpg", 0.9, "image")
        );
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("test", "value");

        AssetRecommendationResult result = AssetRecommendationResult.success(assets, metadata);

        assertTrue(result.isSuccess());
        assertEquals(assets, result.getAssets());
        assertEquals(metadata, result.getMetadata());
        assertNull(result.getError());
    }

    @Test
    void testAssetRecommendationResultSuccessWithNoMetadata() {
        List<RecommendedAsset> assets = Arrays.asList(
            RecommendedAsset.create("/content/dam/1.jpg", 0.9, "image")
        );

        AssetRecommendationResult result = AssetRecommendationResult.success(assets);

        assertTrue(result.isSuccess());
        assertEquals(assets, result.getAssets());
        assertNotNull(result.getMetadata());
    }

    @Test
    void testAssetRecommendationResultError() {
        AssetRecommendationResult result = AssetRecommendationResult.error("Test error");

        assertFalse(result.isSuccess());
        assertNull(result.getAssets());
        assertNull(result.getMetadata());
        assertEquals("Test error", result.getError());
    }

    @Test
    void testAssetCollection() {
        List<String> assetPaths = Arrays.asList("/content/dam/1.jpg", "/content/dam/2.jpg");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdBy", "test");

        AssetRecommendationService.AssetCollection collection = 
            new AssetRecommendationService.AssetCollection("Test", "/content/collections/test", assetPaths, System.currentTimeMillis(), metadata);

        assertEquals("Test", collection.getName());
        assertEquals("/content/collections/test", collection.getPath());
        assertEquals(2, collection.getAssetPaths().size());
        assertEquals(metadata, collection.getMetadata());
    }

    @Test
    void testCollectionCount() {
        List<String> assets = Arrays.asList("/content/dam/1.jpg");
        service.createAssetCollection("Collection1", assets);
        service.createAssetCollection("Collection2", assets);

        assertEquals(2, service.getCollectionCount());
    }

    @Test
    void testEmptyAssetListReturnsEmptyResult() {
        AssetRecommendationResult result = service.recommendAssetsByText("nonexistent search query xyz123", 5);

        assertTrue(result.isSuccess());
    }

    @Test
    void testResultMetadataContainsMethod() {
        AssetRecommendationResult result = service.recommendAssetsByText("test query", 5);

        assertTrue(result.getMetadata().containsKey("method"));
    }

    static class AssetRecommenderTestConfig implements com.aem.playground.core.services.AssetRecommenderConfig {
        @Override
        public String damBasePath() {
            return "/content/dam";
        }

        @Override
        public String embeddingsEndpoint() {
            return "https://api.openai.com/v1/embeddings";
        }

        @Override
        public String embeddingModel() {
            return "text-embedding-ada-002";
        }

        @Override
        public int maxRecommendations() {
            return 10;
        }

        @Override
        public double similarityThreshold() {
            return 0.5;
        }

        @Override
        public boolean enableSemanticSearch() {
            return false;
        }

        @Override
        public String collectionsBasePath() {
            return "/content/collections";
        }
    }
}