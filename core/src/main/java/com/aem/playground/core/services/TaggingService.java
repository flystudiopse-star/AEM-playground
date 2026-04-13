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

public interface TaggingService {

    TaggingResult autoTagContent(TaggableContent content, TaggingOptions options);

    List<ContentCategory> generateCategories(String text, int maxCategories);

    TaxonomyNode buildTaxonomy(List<TaggableContent> contentItems);

    List<RelatedContentSuggestion> suggestRelatedContent(TaggableContent content, int maxSuggestions);

    TagManagerResult manageTag(String tagName, String action, String category);

    class TaggingResult {
        private final boolean success;
        private final List<ContentTag> tags;
        private final String error;
        private final long processingTimeMs;

        private TaggingResult(boolean success, List<ContentTag> tags, String error, long processingTimeMs) {
            this.success = success;
            this.tags = tags;
            this.error = error;
            this.processingTimeMs = processingTimeMs;
        }

        public static TaggingResult success(List<ContentTag> tags, long processingTimeMs) {
            return new TaggingResult(true, tags, null, processingTimeMs);
        }

        public static TaggingResult error(String error, long processingTimeMs) {
            return new TaggingResult(false, null, error, processingTimeMs);
        }

        public boolean isSuccess() {
            return success;
        }

        public List<ContentTag> getTags() {
            return tags;
        }

        public String getError() {
            return error;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }

    class TaggingOptions {
        private final int maxTags;
        private final double minConfidence;
        private final boolean includeCategories;
        private final boolean dedupe;

        private TaggingOptions(int maxTags, double minConfidence, boolean includeCategories, boolean dedupe) {
            this.maxTags = maxTags;
            this.minConfidence = minConfidence;
            this.includeCategories = includeCategories;
            this.dedupe = dedupe;
        }

        public static TaggingOptions defaultOptions() {
            return new TaggingOptions(10, 0.5, true, true);
        }

        public static TaggingOptionsBuilder builder() {
            return new TaggingOptionsBuilder();
        }

        public int getMaxTags() {
            return maxTags;
        }

        public double getMinConfidence() {
            return minConfidence;
        }

        public boolean isIncludeCategories() {
            return includeCategories;
        }

        public boolean isDedupe() {
            return dedupe;
        }
    }

    class TaggingOptionsBuilder {
        private int maxTags = 10;
        private double minConfidence = 0.5;
        private boolean includeCategories = true;
        private boolean dedupe = true;

        public TaggingOptionsBuilder maxTags(int maxTags) {
            this.maxTags = maxTags;
            return this;
        }

        public TaggingOptionsBuilder minConfidence(double minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public TaggingOptionsBuilder includeCategories(boolean includeCategories) {
            this.includeCategories = includeCategories;
            return this;
        }

        public TaggingOptionsBuilder dedupe(boolean dedupe) {
            this.dedupe = dedupe;
            return this;
        }

        public TaggingOptions build() {
            return new TaggingOptions(maxTags, minConfidence, includeCategories, dedupe);
        }
    }
}