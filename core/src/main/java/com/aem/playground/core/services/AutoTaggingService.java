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
import java.util.Map;

public interface AutoTaggingService {

    AutoTaggingResult autoTagContent(String contentId, String title, String content, 
            List<String> existingTags);

    List<TagSuggestion> suggestTags(String content, int maxSuggestions);

    void learnFromUserTags(String contentId, List<String> userTags);

    List<TagCategory> getTagCategories();

    List<TagSuggestion> getSuggestedTagsForContent(String contentId);

    Map<String, Double> getTagUsageStats();

    void clearLearningData();

    class AutoTaggingResult {
        private final boolean success;
        private final List<TagSuggestion> suggestedTags;
        private final String error;
        private final long processingTimeMs;

        private AutoTaggingResult(boolean success, List<TagSuggestion> suggestedTags, 
                String error, long processingTimeMs) {
            this.success = success;
            this.suggestedTags = suggestedTags;
            this.error = error;
            this.processingTimeMs = processingTimeMs;
        }

        public static AutoTaggingResult success(List<TagSuggestion> tags, long processingTimeMs) {
            return new AutoTaggingResult(true, tags, null, processingTimeMs);
        }

        public static AutoTaggingResult error(String error, long processingTimeMs) {
            return new AutoTaggingResult(false, null, error, processingTimeMs);
        }

        public boolean isSuccess() {
            return success;
        }

        public List<TagSuggestion> getSuggestedTags() {
            return suggestedTags;
        }

        public String getError() {
            return error;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }

    class TagSuggestion {
        private final String tagName;
        private final String category;
        private final double confidence;
        private final String source;

        private TagSuggestion(String tagName, String category, double confidence, String source) {
            this.tagName = tagName;
            this.category = category;
            this.confidence = confidence;
            this.source = source;
        }

        public static TagSuggestion create(String tagName, String category, double confidence, String source) {
            return new TagSuggestion(tagName, category, confidence, source);
        }

        public String getTagName() {
            return tagName;
        }

        public String getCategory() {
            return category;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getSource() {
            return source;
        }
    }

    class TagCategory {
        private final String name;
        private final String parentCategory;
        private final List<String> subCategories;
        private final List<String> allowedTags;

        private TagCategory(String name, String parentCategory, 
                List<String> subCategories, List<String> allowedTags) {
            this.name = name;
            this.parentCategory = parentCategory;
            this.subCategories = subCategories;
            this.allowedTags = allowedTags;
        }

        public static TagCategory create(String name, String parentCategory, 
                List<String> subCategories, List<String> allowedTags) {
            return new TagCategory(name, parentCategory, subCategories, allowedTags);
        }

        public String getName() {
            return name;
        }

        public String getParentCategory() {
            return parentCategory;
        }

        public List<String> getSubCategories() {
            return subCategories;
        }

        public List<String> getAllowedTags() {
            return allowedTags;
        }
    }
}