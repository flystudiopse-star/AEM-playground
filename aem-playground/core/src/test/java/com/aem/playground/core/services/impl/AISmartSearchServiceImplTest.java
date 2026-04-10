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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AISmartSearchConfig;
import com.aem.playground.core.services.AISmartSearchService;
import com.aem.playground.core.services.AISmartSearchService.ContentToIndex;
import com.aem.playground.core.services.AISmartSearchService.SearchHit;
import com.aem.playground.core.services.AISmartSearchService.SearchOptions;
import com.aem.playground.core.services.AISmartSearchService.SearchResult;

@ExtendWith(MockitoExtension.class)
class AISmartSearchServiceImplTest {

    @Mock
    private AIService aiService;

    private AISmartSearchServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new AISmartSearchServiceImpl();
        setField(service, "aiService", aiService);
        setField(service, "apiKey", "test-api-key");
        setField(service, "embeddingsEndpoint", "https://api.openai.com/v1/embeddings");
        setField(service, "embeddingModel", "text-embedding-ada-002");
        setField(service, "maxIndexSize", 100);
        setField(service, "suggestionCount", 5);
        setField(service, "defaultSearchResults", 10);
        setField(service, "minScoreThreshold", 0.5);
    }

    @Test
    void testSearchWithEmptyQuery() {
        SearchResult result = service.search("", SearchOptions.defaultOptions());
        
        assertNotNull(result);
        assertEquals(0, result.getTotalHits());
        assertEquals("", result.getQuery());
        assertTrue(result.getHits().isEmpty());
    }

    @Test
    void testSearchWithNullQuery() {
        SearchResult result = service.search(null, null);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalHits());
        assertNull(result.getQuery());
        assertTrue(result.getHits().isEmpty());
    }

    @Test
    void testIndexContent() {
        service.indexContent("content1", "This is test content about AEM", "page");
        
        assertEquals(1, service.getIndexSize());
    }

    @Test
    void testIndexContentBatch() {
        List<ContentToIndex> batch = new ArrayList<>();
        batch.add(ContentToIndex.create("id1", "First content", "page", "Title 1", "/content/page1"));
        batch.add(ContentToIndex.create("id2", "Second content", "page", "Title 2", "/content/page2"));
        batch.add(ContentToIndex.create("id3", "Third content", "asset", "Title 3", "/content/dam/page3"));
        
        service.indexContentBatch(batch);
        
        assertEquals(3, service.getIndexSize());
    }

    @Test
    void testIndexContentSkipsNullContent() {
        service.indexContent(null, null, "page");
        service.indexContent("id1", "", "page");
        
        assertEquals(0, service.getIndexSize());
    }

    @Test
    void testRemoveFromIndex() {
        service.indexContent("content1", "Test content", "page");
        service.indexContent("content2", "Another content", "page");
        
        assertEquals(2, service.getIndexSize());
        
        service.removeFromIndex("content1");
        
        assertEquals(1, service.getIndexSize());
    }

    @Test
    void testRemoveNonExistentContent() {
        service.indexContent("content1", "Test content", "page");
        
        service.removeFromIndex("nonexistent");
        
        assertEquals(1, service.getIndexSize());
    }

    @Test
    void testGetSuggestionsEmptyIndex() {
        List<String> suggestions = service.getSuggestions("test", 5);
        
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testGetSuggestionsEmptyQuery() {
        List<String> suggestions = service.getSuggestions("", 5);
        
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testGetSuggestionsWithContent() {
        service.indexContent("content1", "Test content about AEM forms", "page", "AEM Forms Guide", "/content/forms");
        
        List<String> suggestions = service.getSuggestions("AEM", 5);
        
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
    }

    @Test
    void testRebuildIndex() {
        service.indexContent("content1", "Test content 1", "page");
        service.indexContent("content2", "Test content 2", "page");
        service.indexContent("content3", "Test content 3", "page");
        
        assertEquals(3, service.getIndexSize());
        
        service.rebuildIndex();
        
        assertEquals(0, service.getIndexSize());
    }

    @Test
    void testSearchOptionsCreation() {
        SearchOptions opts = SearchOptions.create(20, 0.3, "page", "/content");
        
        assertEquals(20, opts.getMaxResults());
        assertEquals(0.3, opts.getMinScore(), 0.001);
        assertEquals("page", opts.getContentType());
        assertEquals("/content", opts.getPath());
    }

    @Test
    void testSearchOptionsDefaultOptions() {
        SearchOptions opts = SearchOptions.defaultOptions();
        
        assertTrue(opts.getMaxResults() > 0);
        assertEquals(0.0, opts.getMinScore(), 0.001);
        assertNull(opts.getContentType());
        assertNull(opts.getPath());
    }

    @Test
    void testSearchHitCreation() {
        List<String> highlights = Arrays.asList("highlight 1", "highlight 2");
        SearchHit hit = SearchHit.create("id1", "content", "page", 0.85, "Title", "/path", highlights);
        
        assertEquals("id1", hit.getContentId());
        assertEquals("content", hit.getContent());
        assertEquals("page", hit.getContentType());
        assertEquals(0.85, hit.getScore(), 0.001);
        assertEquals("Title", hit.getTitle());
        assertEquals("/path", hit.getPath());
        assertEquals(2, hit.getHighlights().size());
    }

    @Test
    void testSearchResultCreation() {
        List<SearchHit> hits = Arrays.asList(
            SearchHit.create("id1", "content", "page", 0.9, "Title", "/path", Arrays.asList("hl"))
        );
        List<String> suggestions = Arrays.asList("suggestion 1", "suggestion 2");
        
        SearchResult result = SearchResult.create(hits, 5, 100, "query", suggestions);
        
        assertEquals(1, result.getHits().size());
        assertEquals(5, result.getTotalHits());
        assertEquals(100, result.getSearchTimeMs());
        assertEquals("query", result.getQuery());
        assertEquals(2, result.getSuggestions().size());
    }

    @Test
    void testContentToIndexCreation() {
        ContentToIndex content = ContentToIndex.create("id1", "content", "page", "Title", "/path");
        
        assertEquals("id1", content.getContentId());
        assertEquals("content", content.getContent());
        assertEquals("page", content.getContentType());
        assertEquals("Title", content.getTitle());
        assertEquals("/path", content.getPath());
    }

    @Test
    void testSearchWithContentTypeFilter() {
        service.indexContent("content1", "Test content", "page");
        service.indexContent("content2", "Another content", "asset");
        
        SearchOptions opts = SearchOptions.create(10, 0.0, "page", null);
        
        SearchResult result = service.search("test", opts);
        
        assertNotNull(result);
    }

    @Test
    void testSearchWithPathFilter() {
        service.indexContent("content1", "Test content", "page", "Title", "/content/us");
        service.indexContent("content2", "Another content", "page", "Title", "/content/fr");
        
        SearchOptions opts = SearchOptions.create(10, 0.0, null, "/content/us");
        
        SearchResult result = service.search("test", opts);
        
        assertNotNull(result);
    }

    @Test
    void testGetIndexSizeEmpty() {
        assertEquals(0, service.getIndexSize());
    }

    @Test
    void testMaxIndexSizeEviction() throws Exception {
        for (int i = 0; i < 110; i++) {
            service.indexContent("content" + i, "content " + i, "page");
        }
        
        assertTrue(service.getIndexSize() <= 100);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}