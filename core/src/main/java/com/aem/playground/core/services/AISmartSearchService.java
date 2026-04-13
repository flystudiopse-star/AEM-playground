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

import java.util.List;

public interface AISmartSearchService {

    SearchResult search(String query, SearchOptions options);

    void indexContent(String contentId, String content, String contentType);

    void indexContentBatch(List<ContentToIndex> contents);

    void removeFromIndex(String contentId);

    List<String> getSuggestions(String partialQuery, int maxSuggestions);

    void rebuildIndex();

    class SearchResult {
        private final List<SearchHit> hits;
        private final int totalHits;
        private final long searchTimeMs;
        private final String query;
        private final List<String> suggestions;

        private SearchResult(List<SearchHit> hits, int totalHits, long searchTimeMs, 
                           String query, List<String> suggestions) {
            this.hits = hits;
            this.totalHits = totalHits;
            this.searchTimeMs = searchTimeMs;
            this.query = query;
            this.suggestions = suggestions;
        }

        public static SearchResult create(List<SearchHit> hits, int totalHits, long searchTimeMs,
                                         String query, List<String> suggestions) {
            return new SearchResult(hits, totalHits, searchTimeMs, query, suggestions);
        }

        public List<SearchHit> getHits() {
            return hits;
        }

        public int getTotalHits() {
            return totalHits;
        }

        public long getSearchTimeMs() {
            return searchTimeMs;
        }

        public String getQuery() {
            return query;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }
    }

    class SearchHit {
        private final String contentId;
        private final String content;
        private final String contentType;
        private final double score;
        private final String title;
        private final String path;
        private final List<String> highlights;

        private SearchHit(String contentId, String content, String contentType, double score,
                         String title, String path, List<String> highlights) {
            this.contentId = contentId;
            this.content = content;
            this.contentType = contentType;
            this.score = score;
            this.title = title;
            this.path = path;
            this.highlights = highlights;
        }

        public static SearchHit create(String contentId, String content, String contentType,
                                       double score, String title, String path, List<String> highlights) {
            return new SearchHit(contentId, content, contentType, score, title, path, highlights);
        }

        public String getContentId() {
            return contentId;
        }

        public String getContent() {
            return content;
        }

        public String getContentType() {
            return contentType;
        }

        public double getScore() {
            return score;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        public List<String> getHighlights() {
            return highlights;
        }
    }

    class SearchOptions {
        private final int maxResults;
        private final double minScore;
        private final String contentType;
        private final String path;

        private SearchOptions(int maxResults, double minScore, String contentType, String path) {
            this.maxResults = maxResults;
            this.minScore = minScore;
            this.contentType = contentType;
            this.path = path;
        }

        public static SearchOptions create(int maxResults, double minScore, 
                                          String contentType, String path) {
            return new SearchOptions(maxResults, minScore, contentType, path);
        }

        public static SearchOptions defaultOptions() {
            return new SearchOptions(10, 0.0, null, null);
        }

        public int getMaxResults() {
            return maxResults;
        }

        public double getMinScore() {
            return minScore;
        }

        public String getContentType() {
            return contentType;
        }

        public String getPath() {
            return path;
        }
    }

    class ContentToIndex {
        private final String contentId;
        private final String content;
        private final String contentType;
        private final String title;
        private final String path;

        private ContentToIndex(String contentId, String content, String contentType,
                             String title, String path) {
            this.contentId = contentId;
            this.content = content;
            this.contentType = contentType;
            this.title = title;
            this.path = path;
        }

        public static ContentToIndex create(String contentId, String content, String contentType,
                                           String title, String path) {
            return new ContentToIndex(contentId, content, contentType, title, path);
        }

        public String getContentId() {
            return contentId;
        }

        public String getContent() {
            return content;
        }

        public String getContentType() {
            return contentType;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }
    }
}