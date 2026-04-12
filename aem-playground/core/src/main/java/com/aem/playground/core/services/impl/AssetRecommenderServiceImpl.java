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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.playground.core.services.AssetRecommenderConfig;
import com.aem.playground.core.services.AssetRecommendationResult;
import com.aem.playground.core.services.AssetRecommendationResult.RecommendedAsset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = AssetRecommendationService.class)
@Designate(ocd = AssetRecommenderConfig.class)
public class AssetRecommenderServiceImpl implements AssetRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(AssetRecommenderServiceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String damBasePath;
    private String embeddingsEndpoint;
    private String embeddingModel;
    private int maxRecommendations;
    private double similarityThreshold;
    private boolean enableSemanticSearch;
    private String collectionsBasePath;

    @Reference
    private AIService aiService;

    private final Map<String, AssetMetadata> assetIndex = new ConcurrentHashMap<>();
    private final Map<String, AssetCollection> collections = new ConcurrentHashMap<>();
    private final Map<String, List<Double>> embeddingsCache = new ConcurrentHashMap<>();
    private String apiKey;

    @Activate
    @Modified
    protected void activate(AssetRecommenderConfig config) {
        this.damBasePath = PropertiesUtil.toString(config.damBasePath(), "/content/dam");
        this.embeddingsEndpoint = PropertiesUtil.toString(config.embeddingsEndpoint(), "https://api.openai.com/v1/embeddings");
        this.embeddingModel = PropertiesUtil.toString(config.embeddingModel(), "text-embedding-ada-002");
        this.maxRecommendations = config.maxRecommendations();
        this.similarityThreshold = config.similarityThreshold();
        this.enableSemanticSearch = config.enableSemanticSearch();
        this.collectionsBasePath = PropertiesUtil.toString(config.collectionsBasePath(), "/content/collections");
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (StringUtils.isBlank(apiKey)) {
            apiKey = "";
        }
        log.info("AssetRecommenderService activated with DAM path: {}", damBasePath);
    }

    @Deactivate
    protected void deactivate() {
        assetIndex.clear();
        collections.clear();
        embeddingsCache.clear();
    }

    @Override
    public AssetRecommendationResult recommendAssetsForPage(String pagePath, String pageContent, int limit) {
        if (StringUtils.isBlank(pagePath) || StringUtils.isBlank(pageContent)) {
            return AssetRecommendationResult.error("Page path and content are required");
        }

        try {
            List<RecommendedAsset> recommendations = new ArrayList<>();

            if (enableSemanticSearch) {
                List<Double> contentEmbedding = getEmbedding(pageContent);
                if (contentEmbedding != null) {
                    recommendations = findSimilarAssetsInternal(contentEmbedding, limit);
                }
            }

            if (recommendations.isEmpty()) {
                recommendations = getDefaultRecommendations(pageContent, limit);
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("pagePath", pagePath);
            metadata.put("method", enableSemanticSearch ? "semantic" : "keyword");
            metadata.put("totalAssets", recommendations.size());

            return AssetRecommendationResult.success(recommendations, metadata);

        } catch (Exception e) {
            log.error("Error recommending assets for page: {}", e.getMessage());
            return AssetRecommendationResult.error(e.getMessage());
        }
    }

    @Override
    public AssetRecommendationResult recommendAssetsByText(String text, int limit) {
        if (StringUtils.isBlank(text)) {
            return AssetRecommendationResult.error("Text query is required");
        }

        try {
            List<RecommendedAsset> recommendations;

            if (enableSemanticSearch) {
                List<Double> textEmbedding = getEmbedding(text);
                if (textEmbedding != null) {
                    recommendations = findSimilarAssetsInternal(textEmbedding, limit);
                } else {
                    recommendations = getKeywordBasedRecommendations(text, limit);
                }
            } else {
                recommendations = getKeywordBasedRecommendations(text, limit);
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("query", text);
            metadata.put("method", enableSemanticSearch ? "semantic" : "keyword");
            metadata.put("totalAssets", recommendations.size());

            return AssetRecommendationResult.success(recommendations, metadata);

        } catch (Exception e) {
            log.error("Error recommending assets by text: {}", e.getMessage());
            return AssetRecommendationResult.error(e.getMessage());
        }
    }

    @Override
    public AssetRecommendationResult findSimilarAssets(String assetPath, int limit) {
        if (StringUtils.isBlank(assetPath)) {
            return AssetRecommendationResult.error("Asset path is required");
        }

        try {
            AssetMetadata sourceAsset = assetIndex.get(assetPath);
            if (sourceAsset == null) {
                return AssetRecommendationResult.error("Asset not found in index: " + assetPath);
            }

            List<Double> sourceEmbedding = embeddingsCache.get(assetPath);
            if (sourceEmbedding == null) {
                sourceEmbedding = getEmbedding(sourceAsset.getDescription());
                if (sourceEmbedding != null) {
                    embeddingsCache.put(assetPath, sourceEmbedding);
                }
            }

            if (sourceEmbedding != null) {
                List<RecommendedAsset> similarAssets = findSimilarAssetsInternal(sourceEmbedding, limit);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("sourceAsset", assetPath);
                metadata.put("method", "semantic");
                return AssetRecommendationResult.success(similarAssets, metadata);
            }

            return AssetRecommendationResult.error("Unable to generate embedding for asset");

        } catch (Exception e) {
            log.error("Error finding similar assets: {}", e.getMessage());
            return AssetRecommendationResult.error(e.getMessage());
        }
    }

    @Override
    public AssetRecommendationResult suggestImagesForContent(String content, int limit) {
        if (StringUtils.isBlank(content)) {
            return AssetRecommendationResult.error("Content is required");
        }

        try {
            List<RecommendedAsset> imageRecommendations = new ArrayList<>();

            if (enableSemanticSearch) {
                List<Double> contentEmbedding = getEmbedding(content);
                if (contentEmbedding != null) {
                    imageRecommendations = assetIndex.values().stream()
                        .filter(a -> "image".equalsIgnoreCase(a.getAssetType()))
                        .map(a -> {
                            List<Double> embedding = embeddingsCache.get(a.getPath());
                            if (embedding == null) {
                                embedding = getEmbedding(a.getDescription());
                                if (embedding != null) {
                                    embeddingsCache.put(a.getPath(), embedding);
                                }
                            }
                            if (embedding != null) {
                                double similarity = calculateCosineSimilarity(contentEmbedding, embedding);
                                if (similarity >= similarityThreshold) {
                                    return RecommendedAsset.builder()
                                        .assetPath(a.getPath())
                                        .relevanceScore(similarity)
                                        .assetType(a.getAssetType())
                                        .title(a.getTitle())
                                        .description(a.getDescription())
                                        .tags(a.getTags())
                                        .build();
                                }
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                        .limit(limit)
                        .collect(Collectors.toList());
                }
            }

            if (imageRecommendations.isEmpty()) {
                imageRecommendations = getDefaultImageRecommendations(content, limit);
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("content", content);
            metadata.put("type", "image");
            return AssetRecommendationResult.success(imageRecommendations, metadata);

        } catch (Exception e) {
            log.error("Error suggesting images for content: {}", e.getMessage());
            return AssetRecommendationResult.error(e.getMessage());
        }
    }

    @Override
    public List<String> createAssetCollection(String collectionName, List<String> assetPaths) {
        if (StringUtils.isBlank(collectionName) || assetPaths == null || assetPaths.isEmpty()) {
            return Collections.emptyList();
        }

        String collectionPath = collectionsBasePath + "/" + collectionName.toLowerCase().replace(" ", "-");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdAt", System.currentTimeMillis());
        metadata.put("assetCount", assetPaths.size());

        AssetCollection collection = new AssetCollection(collectionName, collectionPath, assetPaths, System.currentTimeMillis(), metadata);
        collections.put(collectionName, collection);

        log.info("Created asset collection '{}' with {} assets", collectionName, assetPaths.size());
        return assetPaths;
    }

    @Override
    public List<String> getSuggestedCollections(String context) {
        if (StringUtils.isBlank(context)) {
            return collections.keySet().stream().collect(Collectors.toList());
        }

        return collections.values().stream()
            .filter(c -> c.getName().toLowerCase().contains(context.toLowerCase()) ||
                        c.getMetadata().toString().toLowerCase().contains(context.toLowerCase()))
            .map(AssetCollection::getName)
            .collect(Collectors.toList());
    }

    @Override
    public void indexAssetMetadata(String assetPath, Map<String, Object> metadata) {
        if (StringUtils.isBlank(assetPath) || metadata == null) {
            return;
        }

        String title = (String) metadata.getOrDefault("title", "");
        String description = (String) metadata.getOrDefault("description", "");
        String assetType = (String) metadata.getOrDefault("type", "image");

        AssetMetadata assetMetadata = new AssetMetadata(assetPath, title, description, assetType, metadata);
        assetIndex.put(assetPath, assetMetadata);

        if (StringUtils.isNotBlank(description) && enableSemanticSearch) {
            List<Double> embedding = getEmbedding(description);
            if (embedding != null) {
                embeddingsCache.put(assetPath, embedding);
            }
        }

        log.info("Indexed asset metadata for: {}", assetPath);
    }

    @Override
    public void clearAssetIndex() {
        assetIndex.clear();
        embeddingsCache.clear();
        log.info("Asset index cleared");
    }

    private List<RecommendedAsset> findSimilarAssetsInternal(List<Double> targetEmbedding, int limit) {
        return assetIndex.values().stream()
            .map(asset -> {
                List<Double> assetEmbedding = embeddingsCache.get(asset.getPath());
                if (assetEmbedding == null && StringUtils.isNotBlank(asset.getDescription())) {
                    assetEmbedding = getEmbedding(asset.getDescription());
                    if (assetEmbedding != null) {
                        embeddingsCache.put(asset.getPath(), assetEmbedding);
                    }
                }

                if (assetEmbedding != null) {
                    double similarity = calculateCosineSimilarity(targetEmbedding, assetEmbedding);
                    if (similarity >= similarityThreshold) {
                        return RecommendedAsset.builder()
                            .assetPath(asset.getPath())
                            .relevanceScore(similarity)
                            .assetType(asset.getAssetType())
                            .title(asset.getTitle())
                            .description(asset.getDescription())
                            .tags(asset.getTags())
                            .build();
                    }
                }
                return null;
            })
            .filter(Objects::nonNull)
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<RecommendedAsset> getKeywordBasedRecommendations(String query, int limit) {
        String[] keywords = query.toLowerCase().split("\\s+");

        return assetIndex.values().stream()
            .filter(asset -> {
                String text = (asset.getTitle() + " " + asset.getDescription()).toLowerCase();
                for (String keyword : keywords) {
                    if (text.contains(keyword)) {
                        return true;
                    }
                }
                return false;
            })
            .map(asset -> RecommendedAsset.builder()
                .assetPath(asset.getPath())
                .relevanceScore(0.7)
                .assetType(asset.getAssetType())
                .title(asset.getTitle())
                .description(asset.getDescription())
                .build())
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<RecommendedAsset> getDefaultRecommendations(String pageContent, int limit) {
        List<RecommendedAsset> defaults = new ArrayList<>();

        if (pageContent.toLowerCase().contains("hero") || pageContent.toLowerCase().contains("banner")) {
            defaults.add(RecommendedAsset.builder()
                .assetPath(damBasePath + "/default-hero.jpg")
                .relevanceScore(0.9)
                .assetType("image")
                .title("Default Hero Image")
                .description("A generic hero image for pages")
                .build());
        }

        if (pageContent.toLowerCase().contains("product")) {
            defaults.add(RecommendedAsset.builder()
                .assetPath(damBasePath + "/default-product.jpg")
                .relevanceScore(0.8)
                .assetType("image")
                .title("Default Product Image")
                .description("A generic product placeholder")
                .build());
        }

        defaults.add(RecommendedAsset.builder()
            .assetPath(damBasePath + "/default-background.jpg")
            .relevanceScore(0.6)
            .assetType("image")
            .title("Default Background")
            .description("A versatile background image")
            .build());

        return defaults.stream().limit(limit).collect(Collectors.toList());
    }

    private List<RecommendedAsset> getDefaultImageRecommendations(String content, int limit) {
        List<RecommendedAsset> defaults = new ArrayList<>();

        defaults.add(RecommendedAsset.builder()
            .assetPath(damBasePath + "/general-image-1.jpg")
            .relevanceScore(0.7)
            .assetType("image")
            .title("General Image 1")
            .description("A general-purpose image")
            .build());

        defaults.add(RecommendedAsset.builder()
            .assetPath(damBasePath + "/general-image-2.jpg")
            .relevanceScore(0.6)
            .assetType("image")
            .title("General Image 2")
            .description("Another general-purpose image")
            .build());

        return defaults.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Double> getEmbedding(String text) {
        if (StringUtils.isBlank(apiKey)) {
            log.warn("No API key configured for embeddings");
            return null;
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", embeddingModel);
            request.put("input", text);

            String requestBody = objectMapper.writeValueAsString(request);
            String response = executePostRequest(embeddingsEndpoint, requestBody);

            if (StringUtils.isBlank(response)) {
                return null;
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode data = rootNode.get("data");

            if (data == null || !data.isArray() || data.size() == 0) {
                return null;
            }

            JsonNode embeddingArray = data.get(0).get("embedding");
            if (embeddingArray == null || !embeddingArray.isArray()) {
                return null;
            }

            List<Double> embedding = new ArrayList<>();
            for (JsonNode value : embeddingArray) {
                embedding.add(value.asDouble());
            }

            return embedding;

        } catch (Exception e) {
            log.error("Error generating embedding: {}", e.getMessage());
            return null;
        }
    }

    private String executePostRequest(String endpoint, String body) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    log.error("API error: {} - {}", statusCode, responseBody);
                    throw new IOException("API returned status code: " + statusCode);
                }

                return responseBody;
            }
        }
    }

    private double calculateCosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public int getIndexedAssetCount() {
        return assetIndex.size();
    }

    public int getCollectionCount() {
        return collections.size();
    }

    private static class AssetMetadata {
        private final String path;
        private final String title;
        private final String description;
        private final String assetType;
        private final Map<String, Object> tags;

        AssetMetadata(String path, String title, String description, String assetType, Map<String, Object> tags) {
            this.path = path;
            this.title = title;
            this.description = description;
            this.assetType = assetType;
            this.tags = tags;
        }

        String getPath() {
            return path;
        }

        String getTitle() {
            return title;
        }

        String getDescription() {
            return description;
        }

        String getAssetType() {
            return assetType;
        }

        Map<String, Object> getTags() {
            return tags;
        }
    }
}