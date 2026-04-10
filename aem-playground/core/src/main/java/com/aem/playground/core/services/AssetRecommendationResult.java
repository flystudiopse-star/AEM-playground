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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetRecommendationResult {

    private final List<RecommendedAsset> assets;
    private final Map<String, Object> metadata;
    private final boolean success;
    private final String error;

    private AssetRecommendationResult(List<RecommendedAsset> assets, Map<String, Object> metadata, boolean success, String error) {
        this.assets = assets;
        this.metadata = metadata;
        this.success = success;
        this.error = error;
    }

    public static AssetRecommendationResult success(List<RecommendedAsset> assets, Map<String, Object> metadata) {
        return new AssetRecommendationResult(assets, metadata, true, null);
    }

    public static AssetRecommendationResult success(List<RecommendedAsset> assets) {
        return new AssetRecommendationResult(assets, new HashMap<>(), true, null);
    }

    public static AssetRecommendationResult error(String error) {
        return new AssetRecommendationResult(null, null, false, error);
    }

    public List<RecommendedAsset> getAssets() {
        return assets;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public static class RecommendedAsset {
        private final String assetPath;
        private final double relevanceScore;
        private final String assetType;
        private final String title;
        private final String description;
        private final Map<String, Object> tags;
        private final List<String> suggestedCollections;

        private RecommendedAsset(String assetPath, double relevanceScore, String assetType, 
                                   String title, String description, Map<String, Object> tags,
                                   List<String> suggestedCollections) {
            this.assetPath = assetPath;
            this.relevanceScore = relevanceScore;
            this.assetType = assetType;
            this.title = title;
            this.description = description;
            this.tags = tags;
            this.suggestedCollections = suggestedCollections;
        }

        public static RecommendedAsset create(String assetPath, double relevanceScore, String assetType) {
            return new RecommendedAsset(assetPath, relevanceScore, assetType, null, null, null, null);
        }

        public static RecommendedAsset create(String assetPath, double relevanceScore, String assetType,
                                                String title, String description) {
            return new RecommendedAsset(assetPath, relevanceScore, assetType, title, description, null, null);
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getAssetPath() {
            return assetPath;
        }

        public double getRelevanceScore() {
            return relevanceScore;
        }

        public String getAssetType() {
            return assetType;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getTags() {
            return tags;
        }

        public List<String> getSuggestedCollections() {
            return suggestedCollections;
        }

        public static class Builder {
            private String assetPath;
            private double relevanceScore;
            private String assetType;
            private String title;
            private String description;
            private Map<String, Object> tags;
            private List<String> suggestedCollections;

            public Builder assetPath(String assetPath) {
                this.assetPath = assetPath;
                return this;
            }

            public Builder relevanceScore(double relevanceScore) {
                this.relevanceScore = relevanceScore;
                return this;
            }

            public Builder assetType(String assetType) {
                this.assetType = assetType;
                return this;
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder tags(Map<String, Object> tags) {
                this.tags = tags;
                return this;
            }

            public Builder suggestedCollections(List<String> suggestedCollections) {
                this.suggestedCollections = suggestedCollections;
                return this;
            }

            public RecommendedAsset build() {
                return new RecommendedAsset(assetPath, relevanceScore, assetType, title, description, tags, suggestedCollections);
            }
        }
    }
}