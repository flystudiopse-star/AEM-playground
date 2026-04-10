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

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.ContentCategory;
import com.aem.playground.core.services.ContentTag;
import com.aem.playground.core.services.RelatedContentSuggestion;
import com.aem.playground.core.services.TaggableContent;
import com.aem.playground.core.services.TaggingService;
import com.aem.playground.core.services.TaggingServiceConfig;
import com.aem.playground.core.services.TagManagerResult;
import com.aem.playground.core.services.TaxonomyNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = TaggingService.class)
@Designate(ocd = TaggingServiceConfig.class)
public class AITaggingService implements TaggingService {

    private static final Logger log = LoggerFactory.getLogger(AITaggingService.class);

    private static final String DEFAULT_TAGGING_PROMPT = 
        "Analyze the following content and generate relevant tags. " +
        "Return tags in format: tag_name|category|confidence (0.0-1.0). " +
        "Generate up to %d tags with minimum confidence %.1f. " +
        "Content: %s";

    private static final String DEFAULT_CATEGORY_PROMPT = 
        "Analyze the following text and identify up to %d content categories. " +
        "Return categories in format: category_name|parent_category|relevance_score (0.0-1.0). " +
        "Text: %s";

    private static final String DEFAULT_RELATED_PROMPT = 
        "Based on the content '%s' (type: %s), suggest up to %d related content items. " +
        "Return in format: content_id|title|path|score|relation_type|match_reason. " +
        "Text: %s";

    private static final String DEFAULT_TAXONOMY_PROMPT = 
        "Build a hierarchical taxonomy from the following content items. " +
        "Return taxonomy as JSON with fields: id, name, description, parentId, children[]. " +
        "Content items: %s";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedTaggingResult> cache = new ConcurrentHashMap<>();

    private String aiServiceUrl;
    private String apiKey;
    private String model;
    private int maxTags;
    private double minConfidence;
    private int maxTokens;
    private double temperature;
    private boolean enableCategories;
    private boolean enableTaxonomy;
    private boolean enableRelatedContent;
    private boolean cacheEnabled;
    private int cacheSize;

    @Reference
    private AIService aiService;

    @Activate
    @Modified
    protected void activate(TaggingServiceConfig config) {
        this.aiServiceUrl = config.ai_service_url();
        this.apiKey = config.api_key();
        this.model = config.model();
        this.maxTags = config.max_tags();
        this.minConfidence = config.min_confidence();
        this.maxTokens = config.max_tokens();
        this.temperature = PropertiesUtil.toDouble(config.temperature(), 0.7);
        this.enableCategories = config.enable_categories();
        this.enableTaxonomy = config.enable_taxonomy();
        this.enableRelatedContent = config.enable_related_content();
        this.cacheEnabled = config.cache_enabled();
        this.cacheSize = config.cache_size();
        log.info("AITaggingService activated with maxTags: {}, minConfidence: {}", maxTags, minConfidence);
    }

    @Deactivate
    protected void deactivate() {
        cache.clear();
    }

    @Override
    public TaggingResult autoTagContent(TaggableContent content, TaggingOptions options) {
        long startTime = System.currentTimeMillis();

        if (content == null || StringUtils.isBlank(content.getText())) {
            return TaggingResult.error("Content cannot be null or empty", 0);
        }

        TaggingOptions opts = options != null ? options : TaggingOptions.defaultOptions();

        if (cacheEnabled) {
            String cacheKey = generateCacheKey(content.getText(), "tag");
            CachedTaggingResult cached = cache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for tagging: {}", cacheKey);
                return TaggingResult.success(cached.tags, System.currentTimeMillis() - startTime);
            }
        }

        try {
            List<ContentTag> tags = generateTagsWithAI(content, opts);
            tags = filterAndProcessTags(tags, opts);

            long processingTime = System.currentTimeMillis() - startTime;

            if (cacheEnabled && tags.size() > 0) {
                String cacheKey = generateCacheKey(content.getText(), "tag");
                cache.put(cacheKey, new CachedTaggingResult(tags));
                evictOldCacheEntries();
            }

            return TaggingResult.success(tags, processingTime);

        } catch (Exception e) {
            log.error("Error auto-tagging content: {}", e.getMessage());
            return TaggingResult.error(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<ContentCategory> generateCategories(String text, int maxCategories) {
        if (!enableCategories) {
            return new ArrayList<>();
        }

        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }

        try {
            String prompt = String.format(DEFAULT_CATEGORY_PROMPT, maxCategories, text);
            String response = callAI(prompt);

            return parseCategoriesFromResponse(response, maxCategories);

        } catch (Exception e) {
            log.error("Error generating categories: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public TaxonomyNode buildTaxonomy(List<TaggableContent> contentItems) {
        if (!enableTaxonomy || contentItems == null || contentItems.isEmpty()) {
            return TaxonomyNode.root("root", "Content Taxonomy", "Root of content taxonomy");
        }

        try {
            String contentJson = contentItems.stream()
                .map(c -> String.format("{\"id\":\"%s\",\"title\":\"%s\",\"text\":\"%s\"}", 
                    c.getContentId(), c.getTitle(), c.getText()))
                .collect(Collectors.joining(",", "[", "]"));

            String prompt = String.format(DEFAULT_TAXONOMY_PROMPT, contentJson);
            String response = callAI(prompt);

            return parseTaxonomyFromResponse(response);

        } catch (Exception e) {
            log.error("Error building taxonomy: {}", e.getMessage());
            return TaxonomyNode.root("root", "Content Taxonomy", "Error building taxonomy");
        }
    }

    @Override
    public List<RelatedContentSuggestion> suggestRelatedContent(TaggableContent content, int maxSuggestions) {
        if (!enableRelatedContent) {
            return new ArrayList<>();
        }

        if (content == null || StringUtils.isBlank(content.getText())) {
            return new ArrayList<>();
        }

        try {
            String prompt = String.format(DEFAULT_RELATED_PROMPT, 
                content.getTitle(), content.getContentType(), maxSuggestions, content.getText());
            String response = callAI(prompt);

            return parseRelatedContentFromResponse(response, maxSuggestions);

        } catch (Exception e) {
            log.error("Error suggesting related content: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public TagManagerResult manageTag(String tagName, String action, String category) {
        if (StringUtils.isBlank(tagName) || StringUtils.isBlank(action)) {
            return TagManagerResult.failure("Tag name and action are required");
        }

        try {
            switch (action.toLowerCase()) {
                case "add":
                    return handleAddTag(tagName, category);
                case "remove":
                    return handleRemoveTag(tagName);
                case "update":
                    return handleUpdateTag(tagName, category);
                case "list":
                    return handleListTags(category);
                default:
                    return TagManagerResult.failure("Unknown action: " + action);
            }
        } catch (Exception e) {
            log.error("Error managing tag: {}", e.getMessage());
            return TagManagerResult.failure(e.getMessage());
        }
    }

    private List<ContentTag> generateTagsWithAI(TaggableContent content, TaggingOptions opts) throws IOException {
        String existingTagsStr = content.getExistingTags() != null 
            ? String.join(", ", content.getExistingTags()) 
            : "none";

        String promptText = String.format(DEFAULT_TAGGING_PROMPT, 
            opts.getMaxTags(), opts.getMinConfidence(), 
            "Title: " + content.getTitle() + ". Existing tags: " + existingTagsStr + ". Text: " + content.getText());

        String response = callAI(promptText);

        return parseTagsFromResponse(response, opts.getMaxTags());
    }

    private String callAI(String prompt) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        request.put("temperature", temperature);
        request.put("max_tokens", maxTokens);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(aiServiceUrl);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    throw new IOException("AI API returned status: " + statusCode + " - " + responseBody);
                }

                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode choices = rootNode.get("choices");
                if (choices == null || !choices.isArray() || choices.size() == 0) {
                    throw new IOException("Invalid AI response format");
                }

                return choices.get(0).get("message").get("content").asText();
            }
        }
    }

    private List<ContentTag> parseTagsFromResponse(String response, int maxTags) {
        List<ContentTag> tags = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                String name = parts[0].trim();
                String category = parts[1].trim();
                double confidence;

                try {
                    confidence = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException e) {
                    confidence = 0.7;
                }

                if (confidence >= minConfidence) {
                    tags.add(ContentTag.aiGenerated(name, category, confidence));
                }
            }

            if (tags.size() >= maxTags) break;
        }

        return tags;
    }

    private List<ContentTag> filterAndProcessTags(List<ContentTag> tags, TaggingOptions opts) {
        List<ContentTag> filtered = tags.stream()
            .filter(t -> t.getConfidence() >= opts.getMinConfidence())
            .collect(Collectors.toList());

        if (opts.isDedupe()) {
            filtered = filtered.stream()
                .collect(Collectors.toMap(
                    ContentTag::getName,
                    t -> t,
                    (t1, t2) -> t1.getConfidence() >= t2.getConfidence() ? t1 : t2
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
        }

        return filtered.stream().limit(opts.getMaxTags()).collect(Collectors.toList());
    }

    private List<ContentCategory> parseCategoriesFromResponse(String response, int maxCategories) {
        List<ContentCategory> categories = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                String name = parts[0].trim();
                String parent = parts[1].trim().isEmpty() ? null : parts[1].trim();
                double score;

                try {
                    score = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException e) {
                    score = 0.5;
                }

                categories.add(ContentCategory.create(name, parent, null, score, null));
            }

            if (categories.size() >= maxCategories) break;
        }

        return categories;
    }

    private TaxonomyNode parseTaxonomyFromResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return buildTaxonomyNode(rootNode, null, 0);
        } catch (Exception e) {
            log.error("Error parsing taxonomy JSON: {}", e.getMessage());
            return TaxonomyNode.root("root", "Content Taxonomy", "Parse error");
        }
    }

    private TaxonomyNode buildTaxonomyNode(JsonNode node, String parentId, int depth) {
        String id = node.has("id") ? node.get("id").asText() : "node_" + depth;
        String name = node.has("name") ? node.get("name").asText() : "Unknown";
        String description = node.has("description") ? node.get("description").asText() : null;

        List<TaxonomyNode> children = new ArrayList<>();
        if (node.has("children") && node.get("children").isArray()) {
            for (JsonNode childNode : node.get("children")) {
                children.add(buildTaxonomyNode(childNode, id, depth + 1));
            }
        }

        return TaxonomyNode.create(id, name, description, parentId, children, depth, new ArrayList<>());
    }

    private List<RelatedContentSuggestion> parseRelatedContentFromResponse(String response, int maxSuggestions) {
        List<RelatedContentSuggestion> suggestions = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                String contentId = parts[0].trim();
                String title = parts[1].trim();
                String path = parts[2].trim();
                double score;

                try {
                    score = Double.parseDouble(parts[3].trim());
                } catch (NumberFormatException e) {
                    score = 0.5;
                }

                String relationType = parts.length > 4 ? parts[4].trim() : "similar";
                String matchReason = parts.length > 5 ? parts[5].trim() : null;

                suggestions.add(RelatedContentSuggestion.create(contentId, title, path, score, relationType, matchReason));
            }

            if (suggestions.size() >= maxSuggestions) break;
        }

        return suggestions;
    }

    private String generateCacheKey(String content, String type) {
        return type + "_" + Math.abs(content.hashCode());
    }

    private void evictOldCacheEntries() {
        if (cache.size() > cacheSize) {
            int toRemove = cache.size() - cacheSize;
            for (int i = 0; i < toRemove && !cache.isEmpty(); i++) {
                String key = cache.keySet().iterator().next();
                cache.remove(key);
            }
        }
    }

    private TagManagerResult handleAddTag(String tagName, String category) {
        ContentTag tag = ContentTag.manual(tagName, category);
        return TagManagerResult.success("Tag added: " + tagName, Arrays.asList(tag));
    }

    private TagManagerResult handleRemoveTag(String tagName) {
        return TagManagerResult.success("Tag removed: " + tagName, new ArrayList<>());
    }

    private TagManagerResult handleUpdateTag(String tagName, String category) {
        ContentTag tag = ContentTag.manual(tagName, category);
        return TagManagerResult.success("Tag updated: " + tagName, Arrays.asList(tag));
    }

    private TagManagerResult handleListTags(String category) {
        return TagManagerResult.success("Tags listed", new ArrayList<>());
    }

    private static class CachedTaggingResult {
        final List<ContentTag> tags;

        CachedTaggingResult(List<ContentTag> tags) {
            this.tags = tags;
        }
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTags() {
        return maxTags;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public int getCacheSizeActual() {
        return cache.size();
    }
}