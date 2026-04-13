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

public interface AssetRecommendationService {

    AssetRecommendationResult recommendAssetsForPage(String pagePath, String pageContent, int limit);

    AssetRecommendationResult recommendAssetsByText(String text, int limit);

    AssetRecommendationResult findSimilarAssets(String assetPath, int limit);

    AssetRecommendationResult suggestImagesForContent(String content, int limit);

    List<String> createAssetCollection(String collectionName, List<String> assetPaths);

    List<String> getSuggestedCollections(String context);

    void indexAssetMetadata(String assetPath, Map<String, Object> metadata);

    void clearAssetIndex();

    class AssetCollection {
        private final String name;
        private final String path;
        private final List<String> assetPaths;
        private final long createdAt;
        private final Map<String, Object> metadata;

        public AssetCollection(String name, String path, List<String> assetPaths, long createdAt, Map<String, Object> metadata) {
            this.name = name;
            this.path = path;
            this.assetPaths = assetPaths;
            this.createdAt = createdAt;
            this.metadata = metadata;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public List<String> getAssetPaths() {
            return assetPaths;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }
}