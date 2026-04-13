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
import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AutoTaggingService;
import com.aem.playground.core.services.AutoTaggingConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component(service = AutoTaggingService.class)
@Designate(ocd = AutoTaggingConfig.class)
public class AutoTaggingServiceImpl implements AutoTaggingService {

    private static final Logger log = LoggerFactory.getLogger(AutoTaggingServiceImpl.class);

    private static final String TAGGING_PROMPT = 
        "Analyze the following content and suggest relevant tags. " +
        "Consider the existing tags: %s. " +
        "Return up to %d tags in format: tag_name|category|confidence (0.0-1.0). " +
        "Content Title: %s. Content: %s";

    private static final String SUGGESTION_PROMPT = 
        "Based on the following content, suggest up to %d relevant tags. " +
        "Return tags in format: tag_name|category|confidence. " +
        "Content: %s";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<TagSuggestion>> tagCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> learnedTags = new ConcurrentHashMap<>();
    private final Map<String, Integer> tagUsageCount = new ConcurrentHashMap<>();
    private final Map<String, List<TagCategory>> categoryHierarchy = new ConcurrentHashMap<>();

    private String aiServiceUrl;
    private String apiKey;
    private String model;
    private int maxTags;
    private double minConfidence;
    private double temperature;
    private int maxTokens;
    private boolean enableLearning;
    private double learningRate;
    private String tagHierarchyJson;
    private String tagCategoriesJson;
    private boolean cacheEnabled;
    private int cacheSize;

    @Reference
    private AIService aiService;

    @Activate
    @Modified
    protected void activate(AutoTaggingConfig config) {
        this.aiServiceUrl = config.ai_service_url();
        this.apiKey = config.api_key();
        this.model = config.model();
        this.maxTags = config.max_tags();
        this.minConfidence = PropertiesUtil.toDouble(config.min_confidence(), 0.5);
        this.temperature = PropertiesUtil.toDouble(config.temperature(), 0.7);
        this.maxTokens = config.max_tokens();
        this.enableLearning = config.enable_learning();
        this.learningRate = PropertiesUtil.toDouble(config.learning_rate(), 0.3);
        this.tagHierarchyJson = config.tag_hierarchy();
        this.tagCategoriesJson = config.tag_categories();
        this.cacheEnabled = config.cache_enabled();
        this.cacheSize = config.cache_size();

        initializeCategories();
        
        log.info("AutoTaggingService activated with maxTags: {}, minConfidence: {}, enableLearning: {}", 
            maxTags, minConfidence, enableLearning);
    }

    @Deactivate
    protected void deactivate() {
        tagCache.clear();
        learnedTags.clear();
        tagUsageCount.clear();
        categoryHierarchy.clear();
    }

    private void initializeCategories() {
        try {
            if (StringUtils.isNotBlank(tagCategoriesJson)) {
                List<TagCategory> categories = objectMapper.readValue(tagCategoriesJson, 
                    new TypeReference<List<TagCategory>>() {});
                for (TagCategory cat : categories) {
                    categoryHierarchy.put(cat.getName(), parseCategoryHierarchy(cat));
                }
            }
            
            if (StringUtils.isNotBlank(tagHierarchyJson)) {
                JsonNode hierarchy = objectMapper.readTree(tagHierarchyJson);
                buildCategoryHierarchy(hierarchy, null);
            }
        } catch (Exception e) {
            log.warn("Error initializing tag categories: {}", e.getMessage());
        }
    }

    private List<TagCategory> parseCategoryHierarchy(TagCategory root) {
        List<TagCategory> result = new ArrayList<>();
        result.add(root);
        if (root.getSubCategories() != null) {
            for (String subCat : root.getSubCategories()) {
                result.addAll(parseCategoryHierarchy(TagCategory.create(subCat, root.getName(), 
                    new ArrayList<>(), new ArrayList<>())));
            }
        }
        return result;
    }

    private void buildCategoryHierarchy(JsonNode node, String parentName) {
        if (node.isArray()) {
            for (JsonNode item : node) {
                buildCategoryHierarchy(item, parentName);
            }
        } else if (node.isObject()) {
            String name = node.has("name") ? node.get("name").asText() : null;
            if (name != null) {
                List<String> subCategories = new ArrayList<>();
                if (node.has("subCategories") && node.get("subCategories").isArray()) {
                    for (JsonNode sub : node.get("subCategories")) {
                        subCategories.add(sub.asText());
                    }
                }
                List<String> allowedTags = new ArrayList<>();
                if (node.has("allowedTags") && node.get("allowedTags").isArray()) {
                    for (JsonNode tag : node.get("allowedTags")) {
                        allowedTags.add(tag.asText());
                    }
                }
                TagCategory cat = TagCategory.create(name, parentName, subCategories, allowedTags);
                categoryHierarchy.put(name, parseCategoryHierarchy(cat));
                
                if (node.has("children")) {
                    buildCategoryHierarchy(node.get("children"), name);
                }
            }
        }
    }

    @Override
    public AutoTaggingResult autoTagContent(String contentId, String title, String content, 
            List<String> existingTags) {
        long startTime = System.currentTimeMillis();

        if (StringUtils.isBlank(content)) {
            return AutoTaggingResult.error("Content cannot be empty", 0);
        }

        try {
            String cacheKey = generateCacheKey(content);
            if (cacheEnabled) {
                List<TagSuggestion> cached = tagCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for content: {}", contentId);
                    return AutoTaggingResult.success(cached, System.currentTimeMillis() - startTime);
                }
            }

            List<TagSuggestion> suggestions = generateTagsWithAI(title, content, existingTags);
            List<TagSuggestion> filtered = filterByConfidence(suggestions);
            filtered = applyLearnedTags(contentId, filtered);
            
            if (enableLearning && existingTags != null && !existingTags.isEmpty()) {
                updateTagUsage(existingTags);
            }

            long processingTime = System.currentTimeMillis() - startTime;

            if (cacheEnabled && !filtered.isEmpty()) {
                tagCache.put(cacheKey, filtered);
                evictOldCacheEntries();
            }

            return AutoTaggingResult.success(filtered, processingTime);

        } catch (Exception e) {
            log.error("Error auto-tagging content: {}", e.getMessage());
            return AutoTaggingResult.error(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<TagSuggestion> suggestTags(String content, int maxSuggestions) {
        if (StringUtils.isBlank(content)) {
            return new ArrayList<>();
        }

        try {
            String prompt = String.format(SUGGESTION_PROMPT, maxSuggestions, content);
            String response = callAI(prompt);
            return parseSuggestionsFromResponse(response, maxSuggestions);
        } catch (Exception e) {
            log.error("Error suggesting tags: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void learnFromUserTags(String contentId, List<String> userTags) {
        if (!enableLearning || userTags == null || userTags.isEmpty()) {
            return;
        }

        log.info("Learning from user tags for content: {}", contentId);
        
        learnedTags.put(contentId, new ArrayList<>(userTags));
        
        for (String tag : userTags) {
            tagUsageCount.merge(tag.toLowerCase(), 1, (a, b) -> a + b);
        }
    }

    @Override
    public List<TagCategory> getTagCategories() {
        List<TagCategory> allCategories = new ArrayList<>();
        for (List<TagCategory> cats : categoryHierarchy.values()) {
            allCategories.addAll(cats);
        }
        return allCategories.stream()
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<TagSuggestion> getSuggestedTagsForContent(String contentId) {
        List<TagSuggestion> learned = new ArrayList<>();
        
        if (learnedTags.containsKey(contentId)) {
            for (String tag : learnedTags.get(contentId)) {
                double confidence = calculateTagConfidence(tag);
                learned.add(TagSuggestion.create(tag, null, confidence, "learned"));
            }
        }
        
        return learned;
    }

    @Override
    public Map<String, Double> getTagUsageStats() {
        Map<String, Double> stats = new HashMap<>();
        int total = tagUsageCount.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<String, Integer> entry : tagUsageCount.entrySet()) {
            stats.put(entry.getKey(), total > 0 ? (double) entry.getValue() / total : 0.0);
        }
        
        return stats;
    }

    @Override
    public void clearLearningData() {
        learnedTags.clear();
        tagUsageCount.clear();
        log.info("Learning data cleared");
    }

    private List<TagSuggestion> generateTagsWithAI(String title, String content, 
            List<String> existingTags) throws IOException {
        String existingTagsStr = existingTags != null ? String.join(", ", existingTags) : "none";
        
        String prompt = String.format(TAGGING_PROMPT, existingTagsStr, maxTags, title, content);
        String response = callAI(prompt);
        
        return parseSuggestionsFromResponse(response, maxTags);
    }

    private String callAI(String prompt) throws IOException {
        AIGenerationOptions options = AIGenerationOptions.builder()
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        AIService.AIGenerationResult result = aiService.generateText(prompt, options);
        
        if (result.isSuccess()) {
            return result.getContent();
        } else {
            throw new IOException("AI service error: " + result.getError());
        }
    }

    private List<TagSuggestion> parseSuggestionsFromResponse(String response, int maxSuggestions) {
        List<TagSuggestion> suggestions = new ArrayList<>();
        
        if (StringUtils.isBlank(response)) {
            return suggestions;
        }
        
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length >= 2) {
                String tagName = parts[0].trim();
                String category = parts.length > 1 ? parts[1].trim() : null;
                double confidence = 0.5;

                if (parts.length > 2) {
                    try {
                        confidence = Double.parseDouble(parts[2].trim());
                    } catch (NumberFormatException e) {
                        confidence = 0.5;
                    }
                }

                if (confidence >= minConfidence && StringUtils.isNotBlank(tagName)) {
                    suggestions.add(TagSuggestion.create(tagName, category, confidence, "ai"));
                }
            }

            if (suggestions.size() >= maxSuggestions) break;
        }

        return suggestions;
    }

    private List<TagSuggestion> filterByConfidence(List<TagSuggestion> suggestions) {
        return suggestions.stream()
            .filter(s -> s.getConfidence() >= minConfidence)
            .collect(Collectors.toList());
    }

    private List<TagSuggestion> applyLearnedTags(String contentId, List<TagSuggestion> current) {
        if (!enableLearning || !learnedTags.containsKey(contentId)) {
            return current;
        }

        List<TagSuggestion> learned = new ArrayList<>();
        List<String> userTags = learnedTags.get(contentId);
        
        for (String tag : userTags) {
            double conf = calculateTagConfidence(tag);
            learned.add(TagSuggestion.create(tag, null, conf, "learned"));
        }

        List<TagSuggestion> combined = new ArrayList<>(current);
        combined.addAll(learned);
        
        return combined.stream()
            .collect(Collectors.toMap(
                TagSuggestion::getTagName,
                s -> s,
                (s1, s2) -> s1.getConfidence() >= s2.getConfidence() ? s1 : s2
            ))
            .values()
            .stream()
            .limit(maxTags)
            .collect(Collectors.toList());
    }

    private double calculateTagConfidence(String tag) {
        Integer count = tagUsageCount.get(tag.toLowerCase());
        if (count == null) {
            return 0.3;
        }
        
        int maxCount = tagUsageCount.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        return 0.3 + (learningRate * ((double) count / maxCount));
    }

    private void updateTagUsage(List<String> tags) {
        for (String tag : tags) {
            tagUsageCount.merge(tag.toLowerCase(), 1, (a, b) -> a + b);
        }
    }

    private String generateCacheKey(String content) {
        return "tag_" + Math.abs(content.hashCode());
    }

    private void evictOldCacheEntries() {
        if (tagCache.size() > cacheSize) {
            int toRemove = tagCache.size() - cacheSize;
            Iterator<String> iterator = tagCache.keySet().iterator();
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
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

    public boolean isEnableLearning() {
        return enableLearning;
    }

    public int getLearnedContentCount() {
        return learnedTags.size();
    }

    public int getTagUsageCount() {
        return tagUsageCount.size();
    }
}