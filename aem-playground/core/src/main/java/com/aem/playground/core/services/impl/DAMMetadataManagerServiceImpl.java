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
import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIService.AIGenerationResult;
import com.aem.playground.core.services.AssetCategory;
import com.aem.playground.core.services.AssetKeyword;
import com.aem.playground.core.services.AssetMetadata;
import com.aem.playground.core.services.AssetRelationship;
import com.aem.playground.core.services.DAMMetadataManagerConfig;
import com.aem.playground.core.services.DAMMetadataManagerService;
import com.aem.playground.core.services.IntelligentTag;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = DAMMetadataManagerService.class)
@Designate(ocd = DAMMetadataManagerConfig.class)
public class DAMMetadataManagerServiceImpl implements DAMMetadataManagerService {

    private static final Logger log = LoggerFactory.getLogger(DAMMetadataManagerServiceImpl.class);

    private static final String DEFAULT_MODEL = "gpt-4";

    @Reference
    private AIService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedResult> metadataCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String textEndpoint;
    private String defaultModel;
    private int maxKeywords;
    private double minConfidence;
    private int maxTokens;
    private double temperature;
    private boolean enableSmartKeywords;
    private boolean enableCategories;
    private boolean enableRelationships;
    private boolean enableIntelligentTagging;
    private boolean enableMetadataExtraction;
    private boolean cachingEnabled;
    private int cacheMaxSize;

    @Activate
    @Modified
    protected void activate(DAMMetadataManagerConfig config) {
        this.apiKey = config.apiKey();
        this.textEndpoint = PropertiesUtil.toString(config.ai_service_url(), "https://api.openai.com/v1/chat/completions");
        this.defaultModel = PropertiesUtil.toString(config.model(), DEFAULT_MODEL);
        this.maxKeywords = config.max_keywords();
        this.minConfidence = config.min_confidence();
        this.maxTokens = config.max_tokens();
        this.temperature = config.temperature();
        this.enableSmartKeywords = config.enable_smart_keywords();
        this.enableCategories = config.enable_categories();
        this.enableRelationships = config.enable_relationships();
        this.enableIntelligentTagging = config.enable_intelligent_tagging();
        this.enableMetadataExtraction = config.enable_metadata_extraction();
        this.cachingEnabled = config.cache_enabled();
        this.cacheMaxSize = config.cache_size();
        log.info("DAMMetadataManagerService activated with model: {}", defaultModel);
    }

    @Override
    public MetadataExtractionResult extractMetadata(AssetMetadata asset, MetadataExtractionOptions options) {
        long startTime = System.currentTimeMillis();
        
        if (asset == null) {
            return MetadataExtractionResult.error("Asset cannot be null", System.currentTimeMillis() - startTime);
        }

        MetadataExtractionOptions opts = options != null ? options : MetadataExtractionOptions.defaultOptions();

        if (!enableMetadataExtraction) {
            return MetadataExtractionResult.success(asset, System.currentTimeMillis() - startTime);
        }

        if (cachingEnabled) {
            String cacheKey = generateCacheKey(asset.getAssetId() + "metadata");
            CachedResult cached = metadataCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for metadata extraction: {}", cacheKey);
                AssetMetadata cachedMetadata = (AssetMetadata) cached.result;
                return MetadataExtractionResult.success(cachedMetadata, System.currentTimeMillis() - startTime);
            }
        }

        try {
            String prompt = buildMetadataExtractionPrompt(asset, opts);
            AIGenerationOptions aiOptions = AIGenerationOptions.builder()
                    .model(defaultModel)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .enableCache(false)
                    .build();

            AIGenerationResult result = aiService.generateText(prompt, aiOptions);

            if (!result.isSuccess()) {
                return MetadataExtractionResult.error(result.getError(), System.currentTimeMillis() - startTime);
            }

            AssetMetadata extractedMetadata = parseExtractedMetadata(result.getContent(), asset);

            if (cachingEnabled) {
                String cacheKey = generateCacheKey(asset.getAssetId() + "metadata");
                metadataCache.put(cacheKey, new CachedResult(extractedMetadata));
                evictOldCacheEntries(metadataCache);
            }

            return MetadataExtractionResult.success(extractedMetadata, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Error extracting metadata: {}", e.getMessage());
            return MetadataExtractionResult.error(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public KeywordGenerationResult generateSmartKeywords(AssetMetadata asset, KeywordOptions options) {
        long startTime = System.currentTimeMillis();
        
        if (asset == null) {
            return KeywordGenerationResult.error("Asset cannot be null", System.currentTimeMillis() - startTime);
        }

        KeywordOptions opts = options != null ? options : KeywordOptions.defaultOptions();

        if (!enableSmartKeywords) {
            List<AssetKeyword> existingKeywords = new ArrayList<>();
            if (asset.getKeywords() != null) {
                for (String kw : asset.getKeywords()) {
                    existingKeywords.add(AssetKeyword.simple(kw));
                }
            }
            return KeywordGenerationResult.success(existingKeywords, System.currentTimeMillis() - startTime);
        }

        if (cachingEnabled) {
            String cacheKey = generateCacheKey(asset.getAssetId() + "keywords");
            CachedResult cached = metadataCache.get(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<AssetKeyword> cachedKeywords = (List<AssetKeyword>) cached.result;
                return KeywordGenerationResult.success(cachedKeywords, System.currentTimeMillis() - startTime);
            }
        }

        try {
            String prompt = buildKeywordPrompt(asset, opts);
            AIGenerationOptions aiOptions = AIGenerationOptions.builder()
                    .model(defaultModel)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .enableCache(false)
                    .build();

            AIGenerationResult result = aiService.generateText(prompt, aiOptions);

            if (!result.isSuccess()) {
                return KeywordGenerationResult.error(result.getError(), System.currentTimeMillis() - startTime);
            }

            List<AssetKeyword> keywords = parseKeywords(result.getContent(), opts);

            if (cachingEnabled) {
                String cacheKey = generateCacheKey(asset.getAssetId() + "keywords");
                metadataCache.put(cacheKey, new CachedResult(keywords));
                evictOldCacheEntries(metadataCache);
            }

            return KeywordGenerationResult.success(keywords, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Error generating keywords: {}", e.getMessage());
            return KeywordGenerationResult.error(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<AssetCategory> createContentBasedCategories(List<AssetMetadata> assets, int maxCategories) {
        if (assets == null || assets.isEmpty()) {
            return new ArrayList<>();
        }

        if (!enableCategories) {
            return new ArrayList<>();
        }

        try {
            String prompt = buildCategoryPrompt(assets, maxCategories);
            AIGenerationOptions aiOptions = AIGenerationOptions.builder()
                    .model(defaultModel)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .enableCache(false)
                    .build();

            AIGenerationResult result = aiService.generateText(prompt, aiOptions);

            if (!result.isSuccess()) {
                log.error("Error generating categories: {}", result.getError());
                return new ArrayList<>();
            }

            return parseCategories(result.getContent(), maxCategories);

        } catch (Exception e) {
            log.error("Error creating categories: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AssetRelationship> suggestAssetRelationships(AssetMetadata asset, List<AssetMetadata> existingAssets, int maxRelationships) {
        if (asset == null || existingAssets == null || existingAssets.isEmpty()) {
            return new ArrayList<>();
        }

        if (!enableRelationships) {
            return new ArrayList<>();
        }

        try {
            String prompt = buildRelationshipPrompt(asset, existingAssets, maxRelationships);
            AIGenerationOptions aiOptions = AIGenerationOptions.builder()
                    .model(defaultModel)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .enableCache(false)
                    .build();

            AIGenerationResult result = aiService.generateText(prompt, aiOptions);

            if (!result.isSuccess()) {
                log.error("Error generating relationships: {}", result.getError());
                return new ArrayList<>();
            }

            return parseRelationships(result.getContent(), asset.getAssetId());

        } catch (Exception e) {
            log.error("Error suggesting relationships: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public IntelligentTaggingResult addIntelligentTags(AssetMetadata asset, TaggingOptions options) {
        long startTime = System.currentTimeMillis();
        
        if (asset == null) {
            return IntelligentTaggingResult.error("Asset cannot be null", System.currentTimeMillis() - startTime);
        }

        TaggingOptions opts = options != null ? options : TaggingOptions.defaultOptions();

        if (!enableIntelligentTagging) {
            List<IntelligentTag> existingTags = new ArrayList<>();
            if (asset.getExistingTags() != null) {
                for (String tag : asset.getExistingTags()) {
                    existingTags.add(IntelligentTag.aiGenerated(tag, null, 1.0));
                }
            }
            return IntelligentTaggingResult.success(existingTags, System.currentTimeMillis() - startTime);
        }

        try {
            String prompt = buildTaggingPrompt(asset, opts);
            AIGenerationOptions aiOptions = AIGenerationOptions.builder()
                    .model(defaultModel)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .enableCache(false)
                    .build();

            AIGenerationResult result = aiService.generateText(prompt, aiOptions);

            if (!result.isSuccess()) {
                return IntelligentTaggingResult.error(result.getError(), System.currentTimeMillis() - startTime);
            }

            List<IntelligentTag> tags = parseTags(result.getContent(), opts);

            return IntelligentTaggingResult.success(tags, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Error adding intelligent tags: {}", e.getMessage());
            return IntelligentTaggingResult.error(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    private String buildMetadataExtractionPrompt(AssetMetadata asset, MetadataExtractionOptions options) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following asset and extract metadata fields as requested.\n");
        prompt.append("Asset Information:\n");
        
        if (StringUtils.isNotBlank(asset.getAssetName())) {
            prompt.append("- Name: ").append(asset.getAssetName()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getAssetType())) {
            prompt.append("- Type: ").append(asset.getAssetType()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getTitle())) {
            prompt.append("- Title: ").append(asset.getTitle()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getDescription())) {
            prompt.append("- Description: ").append(asset.getDescription()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getMimeType())) {
            prompt.append("- MIME Type: ").append(asset.getMimeType()).append("\n");
        }
        
        prompt.append("\nExtract and return the following metadata fields in JSON format:\n");
        
        Set<String> fields = new HashSet<>();
        if (options.isExtractTitle()) fields.add("title");
        if (options.isExtractDescription()) fields.add("description");
        if (options.isExtractKeywords()) fields.add("keywords");
        if (options.isExtractDate()) fields.add("createdDate");
        if (options.isExtractAuthor()) fields.add("author");
        if (options.isExtractLocation()) fields.add("location");
        if (options.isExtractRights()) fields.add("rights");
        
        prompt.append(String.join(", ", fields));
        prompt.append("\n\nReturn the result as valid JSON with these exact field names.");
        
        return prompt.toString();
    }

    private String buildKeywordPrompt(AssetMetadata asset, KeywordOptions options) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate smart keywords for the following asset. Return up to ")
                .append(options.getMaxKeywords())
                .append(" keywords with confidence scores (0.0-1.0).\n\n");
        
        prompt.append("Asset Information:\n");
        if (StringUtils.isNotBlank(asset.getAssetName())) {
            prompt.append("- Name: ").append(asset.getAssetName()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getTitle())) {
            prompt.append("- Title: ").append(asset.getTitle()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getDescription())) {
            prompt.append("- Description: ").append(asset.getDescription()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getAssetType())) {
            prompt.append("- Type: ").append(asset.getAssetType()).append("\n");
        }
        
        if (options.isIncludeSynonyms()) {
            prompt.append("\nProvide synonyms for each keyword where applicable.");
        }
        
        prompt.append("\n\nReturn as JSON array with fields: keyword, category, confidence, synonyms (optional).");
        
        return prompt.toString();
    }

    private String buildCategoryPrompt(List<AssetMetadata> assets, int maxCategories) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following assets and create ")
                .append(maxCategories)
                .append(" content-based categories.\n\n");
        
        for (int i = 0; i < Math.min(assets.size(), 20); i++) {
            AssetMetadata asset = assets.get(i);
            prompt.append("- ").append(asset.getAssetName());
            if (StringUtils.isNotBlank(asset.getTitle())) {
                prompt.append(" (").append(asset.getTitle()).append(")");
            }
            if (StringUtils.isNotBlank(asset.getAssetType())) {
                prompt.append(" [").append(asset.getAssetType()).append("]");
            }
            prompt.append("\n");
        }
        
        prompt.append("\nReturn as JSON array with fields: name, relevanceScore, associatedAssetIds (list of asset indices).");
        
        return prompt.toString();
    }

    private String buildRelationshipPrompt(AssetMetadata asset, List<AssetMetadata> existingAssets, int maxRelationships) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Find ")
                .append(maxRelationships)
                .append(" related assets from the list below.\n\n");
        
        prompt.append("Target Asset:\n");
        prompt.append("- Name: ").append(asset.getAssetName()).append("\n");
        if (StringUtils.isNotBlank(asset.getTitle())) {
            prompt.append("- Title: ").append(asset.getTitle()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getDescription())) {
            prompt.append("- Description: ").append(asset.getDescription()).append("\n");
        }
        
        prompt.append("\nExisting Assets (provide asset id, path, title/description):\n");
        for (int i = 0; i < Math.min(existingAssets.size(), 30); i++) {
            AssetMetadata existing = existingAssets.get(i);
            prompt.append(i).append(": ").append(existing.getAssetId())
                    .append(" - ").append(existing.getAssetName());
            if (StringUtils.isNotBlank(existing.getTitle())) {
                prompt.append(" (").append(existing.getTitle()).append(")");
            }
            prompt.append("\n");
        }
        
        prompt.append("\nReturn as JSON array with fields: targetIndex, similarityScore (0.0-1.0), relationType, matchReason.");
        
        return prompt.toString();
    }

    private String buildTaggingPrompt(AssetMetadata asset, TaggingOptions options) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate intelligent tags for the following asset. Return up to ")
                .append(options.getMaxTags())
                .append(" tags.\n\n");
        
        prompt.append("Asset Information:\n");
        if (StringUtils.isNotBlank(asset.getAssetName())) {
            prompt.append("- Name: ").append(asset.getAssetName()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getTitle())) {
            prompt.append("- Title: ").append(asset.getTitle()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getDescription())) {
            prompt.append("- Description: ").append(asset.getDescription()).append("\n");
        }
        if (StringUtils.isNotBlank(asset.getAssetType())) {
            prompt.append("- Type: ").append(asset.getAssetType()).append("\n");
        }
        
        if (asset.getKeywords() != null && !asset.getKeywords().isEmpty()) {
            prompt.append("- Keywords: ").append(String.join(", ", asset.getKeywords())).append("\n");
        }
        
        if (options.isIncludeTechnicalTags()) {
            prompt.append("\nInclude technical tags for file format, dimensions, color space, etc.");
        }
        
        prompt.append("\n\nReturn as JSON array with fields: tagName, category, confidence (0.0-1.0), tagType (content/technical/smart), isTechnical (true/false).");
        
        return prompt.toString();
    }

    private AssetMetadata parseExtractedMetadata(String jsonContent, AssetMetadata original) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            
            return AssetMetadata.builder()
                    .assetId(original.getAssetId())
                    .assetPath(original.getAssetPath())
                    .assetName(original.getAssetName())
                    .assetType(original.getAssetType())
                    .title(rootNode.has("title") ? rootNode.get("title").asText() : original.getTitle())
                    .description(rootNode.has("description") ? rootNode.get("description").asText() : original.getDescription())
                    .keywords(parseStringArray(rootNode.get("keywords")))
                    .createdDate(rootNode.has("createdDate") ? rootNode.get("createdDate").asText() : original.getCreatedDate())
                    .author(rootNode.has("author") ? rootNode.get("author").asText() : original.getAuthor())
                    .location(rootNode.has("location") ? rootNode.get("location").asText() : original.getLocation())
                    .rights(rootNode.has("rights") ? rootNode.get("rights").asText() : original.getRights())
                    .mimeType(original.getMimeType())
                    .thumbnailPath(original.getThumbnailPath())
                    .aemPath(original.getAemPath())
                    .build();
        } catch (Exception e) {
            log.error("Error parsing extracted metadata: {}", e.getMessage());
            return original;
        }
    }

    private List<AssetKeyword> parseKeywords(String jsonContent, KeywordOptions options) {
        List<AssetKeyword> keywords = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            if (!rootNode.isArray()) {
                rootNode = objectMapper.readTree("[" + jsonContent + "]");
            }
            
            for (JsonNode node : rootNode) {
                if (keywords.size() >= options.getMaxKeywords()) break;
                
                String keyword = node.has("keyword") ? node.get("keyword").asText() : null;
                String category = node.has("category") ? node.get("category").asText() : null;
                double confidence = node.has("confidence") ? node.get("confidence").asDouble() : 0.5;
                
                if (keyword == null) continue;
                if (confidence < options.getMinConfidence()) continue;
                
                List<String> synonyms = null;
                if (node.has("synonyms") && node.get("synonyms").isArray()) {
                    synonyms = parseStringArray(node.get("synonyms"));
                }
                
                keywords.add(AssetKeyword.create(keyword, category, confidence, "ai", synonyms));
            }
        } catch (Exception e) {
            log.error("Error parsing keywords: {}", e.getMessage());
            
            if (jsonContent != null) {
                for (String kw : jsonContent.split("[,\\n]")) {
                    if (kw.trim().length() > 1) {
                        keywords.add(AssetKeyword.aiGenerated(kw.trim(), null, 0.5));
                    }
                }
            }
        }
        
        return keywords;
    }

    private List<AssetCategory> parseCategories(String jsonContent, int maxCategories) {
        List<AssetCategory> categories = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            if (!rootNode.isArray()) {
                rootNode = objectMapper.readTree("[" + jsonContent + "]");
            }
            
            for (JsonNode node : rootNode) {
                if (categories.size() >= maxCategories) break;
                
                String name = node.has("name") ? node.get("name").asText() : null;
                double relevanceScore = node.has("relevanceScore") ? node.get("relevanceScore").asDouble() : 0.5;
                
                if (name == null) continue;
                
                List<String> assetIds = null;
                if (node.has("associatedAssetIds") && node.get("associatedAssetIds").isArray()) {
                    assetIds = parseStringArray(node.get("associatedAssetIds"));
                }
                
                categories.add(AssetCategory.create(name, null, null, relevanceScore, assetIds, "content-based"));
            }
        } catch (Exception e) {
            log.error("Error parsing categories: {}", e.getMessage());
        }
        
        return categories;
    }

    private List<AssetRelationship> parseRelationships(String jsonContent, String sourceAssetId) {
        List<AssetRelationship> relationships = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            if (!rootNode.isArray()) {
                rootNode = objectMapper.readTree("[" + jsonContent + "]");
            }
            
            for (JsonNode node : rootNode) {
                int targetIndex = node.has("targetIndex") ? node.get("targetIndex").asInt() : -1;
                double similarityScore = node.has("similarityScore") ? node.get("similarityScore").asDouble() : 0.5;
                String relationType = node.has("relationType") ? node.get("relationType").asText() : "related";
                String matchReason = node.has("matchReason") ? node.get("matchReason").asText() : null;
                
                if (targetIndex < 0) continue;
                
                relationships.add(AssetRelationship.create(
                        sourceAssetId,
                        "asset-" + targetIndex,
                        "/content/dam/asset-" + targetIndex,
                        "Asset " + targetIndex,
                        similarityScore,
                        relationType,
                        matchReason,
                        similarityScore >= 0.8 ? "high" : "medium"
                ));
            }
        } catch (Exception e) {
            log.error("Error parsing relationships: {}", e.getMessage());
        }
        
        return relationships;
    }

    private List<IntelligentTag> parseTags(String jsonContent, TaggingOptions options) {
        List<IntelligentTag> tags = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            if (!rootNode.isArray()) {
                rootNode = objectMapper.readTree("[" + jsonContent + "]");
            }
            
            for (JsonNode node : rootNode) {
                if (tags.size() >= options.getMaxTags()) break;
                
                String tagName = node.has("tagName") ? node.get("tagName").asText() : null;
                String category = node.has("category") ? node.get("category").asText() : null;
                double confidence = node.has("confidence") ? node.get("confidence").asDouble() : 0.5;
                String tagType = node.has("tagType") ? node.get("tagType").asText() : "smart";
                boolean isTechnical = node.has("isTechnical") && node.get("isTechnical").asBoolean();
                
                if (tagName == null) continue;
                if (confidence < options.getMinConfidence()) continue;
                
                tags.add(IntelligentTag.create(tagName, category, confidence, "ai", tagType, isTechnical));
            }
        } catch (Exception e) {
            log.error("Error parsing tags: {}", e.getMessage());
            
            if (jsonContent != null) {
                for (String tag : jsonContent.split("[,\\n]")) {
                    if (tag.trim().length() > 1) {
                        tags.add(IntelligentTag.aiGenerated(tag.trim(), null, 0.5));
                    }
                }
            }
        }
        
        return tags;
    }

    private List<String> parseStringArray(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode n : node) {
                result.add(n.asText());
            }
        }
        return result;
    }

    private String generateCacheKey(String input) {
        return String.valueOf(input.hashCode());
    }

    private void evictOldCacheEntries(Map<String, CachedResult> cache) {
        if (cache.size() > cacheMaxSize) {
            int toRemove = cache.size() - cacheMaxSize;
            int count = 0;
            for (String key : cache.keySet()) {
                if (count >= toRemove) break;
                cache.remove(key);
                count++;
            }
        }
    }

    private static class CachedResult {
        final Object result;

        CachedResult(Object result) {
            this.result = result;
        }
    }

    public boolean isEnableSmartKeywords() {
        return enableSmartKeywords;
    }

    public boolean isEnableCategories() {
        return enableCategories;
    }

    public boolean isEnableRelationships() {
        return enableRelationships;
    }

    public boolean isEnableIntelligentTagging() {
        return enableIntelligentTagging;
    }

    public boolean isEnableMetadataExtraction() {
        return enableMetadataExtraction;
    }
}