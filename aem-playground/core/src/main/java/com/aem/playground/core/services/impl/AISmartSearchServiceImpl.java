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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(service = AISmartSearchService.class)
@Designate(ocd = AISmartSearchConfig.class)
public class AISmartSearchServiceImpl implements AISmartSearchService {

    private static final Logger log = LoggerFactory.getLogger(AISmartSearchServiceImpl.class);
    private static final int EMBEDDING_DIMENSION = 1536;
    private static final int SUGGESTION_CACHE_MAX = 100;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, IndexedContent> contentIndex = new ConcurrentHashMap<>();
    private final Map<String, double[]> embeddingCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> suggestionsCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String embeddingsEndpoint;
    private String embeddingModel;
    private int maxIndexSize;
    private int suggestionCount;
    private int defaultSearchResults;
    private double minScoreThreshold;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(AISmartSearchConfig config) {
        this.apiKey = config.apiKey();
        this.embeddingsEndpoint = PropertiesUtil.toString(config.embeddingsEndpoint(), 
            "https://api.openai.com/v1/embeddings");
        this.embeddingModel = PropertiesUtil.toString(config.embeddingModel(), "text-embedding-ada-002");
        this.maxIndexSize = config.maxIndexSize();
        this.suggestionCount = config.suggestionCount();
        this.defaultSearchResults = config.defaultSearchResults();
        this.minScoreThreshold = config.minScoreThreshold();
        log.info("AISmartSearchService activated with endpoint: {}", embeddingsEndpoint);
    }

    @Override
    public SearchResult search(String query, SearchOptions options) {
        long startTime = System.currentTimeMillis();

        if (StringUtils.isBlank(query)) {
            return SearchResult.create(Collections.emptyList(), 0, 0, query, Collections.emptyList());
        }

        SearchOptions opts = options != null ? options : SearchOptions.defaultOptions();
        List<String> suggestions = getSuggestionsInternal(query, suggestionCount);

        try {
            double[] queryEmbedding = generateEmbedding(query);
            if (queryEmbedding == null) {
                log.error("Failed to generate embedding for query: {}", query);
                return SearchResult.create(Collections.emptyList(), 0, 
                    System.currentTimeMillis() - startTime, query, suggestions);
            }

            List<SearchHit> allHits = new ArrayList<>();
            for (IndexedContent content : contentIndex.values()) {
                if (opts.getContentType() != null && !opts.getContentType().equals(content.contentType)) {
                    continue;
                }
                if (opts.getPath() != null && !content.path.startsWith(opts.getPath())) {
                    continue;
                }

                double similarity = cosineSimilarity(queryEmbedding, content.embedding);
                if (similarity >= opts.getMinScore()) {
                    List<String> highlights = generateHighlights(content.content, query);
                    allHits.add(SearchHit.create(
                        content.contentId,
                        content.content,
                        content.contentType,
                        similarity,
                        content.title,
                        content.path,
                        highlights
                    ));
                }
            }

            allHits.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
            
            int totalHits = allHits.size();
            int maxResults = opts.getMaxResults() > 0 ? opts.getMaxResults() : defaultSearchResults;
            List<SearchHit> hits = allHits.stream()
                .limit(maxResults)
                .collect(Collectors.toList());

            long searchTime = System.currentTimeMillis() - startTime;
            return SearchResult.create(hits, totalHits, searchTime, query, suggestions);

        } catch (Exception e) {
            log.error("Error performing search: {}", e.getMessage());
            return SearchResult.create(Collections.emptyList(), 0, 
                System.currentTimeMillis() - startTime, query, suggestions);
        }
    }

    @Override
    public void indexContent(String contentId, String content, String contentType) {
        indexContent(contentId, content, contentType, null, null);
    }

    @Override
    public void indexContentBatch(List<ContentToIndex> contents) {
        for (ContentToIndex item : contents) {
            indexContent(item.getContentId(), item.getContent(), item.getContentType(),
                item.getTitle(), item.getPath());
        }
    }

    private void indexContent(String contentId, String content, String contentType, String title, String path) {
        if (StringUtils.isBlank(contentId) || StringUtils.isBlank(content)) {
            log.warn("Skipping indexing: missing contentId or content");
            return;
        }

        if (contentIndex.size() >= maxIndexSize) {
            evictOldestEntry();
        }

        try {
            double[] embedding = generateEmbedding(content);
            if (embedding == null) {
                log.error("Failed to generate embedding for content: {}", contentId);
                return;
            }

            IndexedContent indexed = new IndexedContent();
            indexed.contentId = contentId;
            indexed.content = content;
            indexed.contentType = contentType;
            indexed.title = title != null ? title : contentId;
            indexed.path = path != null ? path : "";
            indexed.embedding = embedding;
            indexed.indexedAt = System.currentTimeMillis();

            contentIndex.put(contentId, indexed);
            log.debug("Indexed content: {}", contentId);

        } catch (Exception e) {
            log.error("Error indexing content {}: {}", contentId, e.getMessage());
        }
    }

    @Override
    public void removeFromIndex(String contentId) {
        contentIndex.remove(contentId);
        log.debug("Removed content from index: {}", contentId);
    }

    @Override
    public List<String> getSuggestions(String partialQuery, int maxSuggestions) {
        return getSuggestionsInternal(partialQuery, maxSuggestions);
    }

    private List<String> getSuggestionsInternal(String partialQuery, int maxSuggestions) {
        if (StringUtils.isBlank(partialQuery)) {
            return Collections.emptyList();
        }

        String cacheKey = partialQuery.toLowerCase() + ":" + maxSuggestions;
        List<String> cached = suggestionsCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Set<String> suggestions = new HashSet<>();
        String lowerQuery = partialQuery.toLowerCase();

        for (IndexedContent content : contentIndex.values()) {
            if (content.title != null && content.title.toLowerCase().contains(lowerQuery)) {
                suggestions.add(content.title);
            }
            if (content.content != null) {
                String[] words = content.content.split("\\s+");
                for (String word : words) {
                    if (word.toLowerCase().startsWith(lowerQuery) && word.length() > lowerQuery.length()) {
                        suggestions.add(word);
                    }
                }
            }
            if (suggestions.size() >= maxSuggestions) {
                break;
            }
        }

        List<String> result = suggestions.stream().limit(maxSuggestions).collect(Collectors.toList());
        
        if (suggestionsCache.size() > SUGGESTION_CACHE_MAX) {
            suggestionsCache.clear();
        }
        suggestionsCache.put(cacheKey, result);
        
        return result;
    }

    @Override
    public void rebuildIndex() {
        log.info("Rebuilding search index");
        contentIndex.clear();
        embeddingCache.clear();
        suggestionsCache.clear();
    }

    private double[] generateEmbedding(String text) throws IOException {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        String cacheKey = text.hashCode() + ":" + text.length();
        double[] cached = embeddingCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(embeddingsEndpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);

            Map<String, Object> request = new HashMap<>();
            request.put("model", embeddingModel);
            request.put("input", text);

            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    log.error("Embedding API error: {} - {}", statusCode, responseBody);
                    return null;
                }

                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode data = rootNode.get("data");
                if (data == null || !data.isArray() || data.size() == 0) {
                    log.error("Invalid embeddings response");
                    return null;
                }

                JsonNode embeddingArray = data.get(0).get("embedding");
                if (embeddingArray == null || !embeddingArray.isArray()) {
                    log.error("No embedding in response");
                    return null;
                }

                double[] embedding = new double[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    embedding[i] = embeddingArray.get(i).asDouble();
                }

                if (embeddingCache.size() > 1000) {
                    embeddingCache.clear();
                }
                embeddingCache.put(cacheKey, embedding);
                
                return embedding;
            }
        }
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator == 0.0) {
            return 0.0;
        }

        return dotProduct / denominator;
    }

    private List<String> generateHighlights(String content, String query) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }

        List<String> highlights = new ArrayList<>();
        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();
        
        int index = lowerContent.indexOf(lowerQuery);
        if (index >= 0) {
            int start = Math.max(0, index - 50);
            int end = Math.min(content.length(), index + query.length() + 50);
            String highlight = content.substring(start, end);
            if (start > 0) highlight = "..." + highlight;
            if (end < content.length()) highlight = highlight + "...";
            highlights.add(highlight);
        }

        String[] queryWords = query.split("\\s+");
        for (String word : queryWords) {
            if (word.length() > 3) {
                index = lowerContent.indexOf(word.toLowerCase());
                if (index >= 0 && highlights.size() < 3) {
                    int start = Math.max(0, index - 30);
                    int end = Math.min(content.length(), index + word.length() + 30);
                    String highlight = content.substring(start, end);
                    if (start > 0) highlight = "..." + highlight;
                    if (end < content.length()) highlight = highlight + "...";
                    if (!highlights.contains(highlight)) {
                        highlights.add(highlight);
                    }
                }
            }
        }

        return highlights;
    }

    private void evictOldestEntry() {
        if (contentIndex.isEmpty()) {
            return;
        }
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        for (Map.Entry<String, IndexedContent> entry : contentIndex.entrySet()) {
            if (entry.getValue().indexedAt < oldestTime) {
                oldestTime = entry.getValue().indexedAt;
                oldestKey = entry.getKey();
            }
        }
        if (oldestKey != null) {
            contentIndex.remove(oldestKey);
            log.debug("Evicted oldest entry: {}", oldestKey);
        }
    }

    public int getIndexSize() {
        return contentIndex.size();
    }

    private static class IndexedContent {
        String contentId;
        String content;
        String contentType;
        String title;
        String path;
        double[] embedding;
        long indexedAt;
    }
}