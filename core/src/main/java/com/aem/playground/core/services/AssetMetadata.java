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

public class AssetMetadata {
    private final String assetId;
    private final String assetPath;
    private final String assetName;
    private final String assetType;
    private final String title;
    private final String description;
    private final List<String> keywords;
    private final String createdDate;
    private final String modifiedDate;
    private final String author;
    private final String location;
    private final String rights;
    private final long fileSize;
    private final String mimeType;
    private final Map<String, String> customMetadata;
    private final List<String> existingTags;
    private final String thumbnailPath;
    private final String aemPath;

    private AssetMetadata(String assetId, String assetPath, String assetName, String assetType,
                         String title, String description, List<String> keywords,
                         String createdDate, String modifiedDate, String author, String location,
                         String rights, long fileSize, String mimeType,
                         Map<String, String> customMetadata, List<String> existingTags,
                         String thumbnailPath, String aemPath) {
        this.assetId = assetId;
        this.assetPath = assetPath;
        this.assetName = assetName;
        this.assetType = assetType;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.author = author;
        this.location = location;
        this.rights = rights;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.customMetadata = customMetadata;
        this.existingTags = existingTags;
        this.thumbnailPath = thumbnailPath;
        this.aemPath = aemPath;
    }

    public static AssetMetadataBuilder builder() {
        return new AssetMetadataBuilder();
    }

    public static AssetMetadata fromAsset(String assetId, String assetPath, String assetType) {
        return new AssetMetadataBuilder()
                .assetId(assetId)
                .assetPath(assetPath)
                .assetType(assetType)
                .build();
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public String getAssetName() {
        return assetName;
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

    public List<String> getKeywords() {
        return keywords;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public String getAuthor() {
        return author;
    }

    public String getLocation() {
        return location;
    }

    public String getRights() {
        return rights;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Map<String, String> getCustomMetadata() {
        return customMetadata;
    }

    public List<String> getExistingTags() {
        return existingTags;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getAemPath() {
        return aemPath;
    }

    public static class AssetMetadataBuilder {
        private String assetId;
        private String assetPath;
        private String assetName;
        private String assetType;
        private String title;
        private String description;
        private List<String> keywords;
        private String createdDate;
        private String modifiedDate;
        private String author;
        private String location;
        private String rights;
        private long fileSize;
        private String mimeType;
        private Map<String, String> customMetadata;
        private List<String> existingTags;
        private String thumbnailPath;
        private String aemPath;

        public AssetMetadataBuilder assetId(String assetId) {
            this.assetId = assetId;
            return this;
        }

        public AssetMetadataBuilder assetPath(String assetPath) {
            this.assetPath = assetPath;
            return this;
        }

        public AssetMetadataBuilder assetName(String assetName) {
            this.assetName = assetName;
            return this;
        }

        public AssetMetadataBuilder assetType(String assetType) {
            this.assetType = assetType;
            return this;
        }

        public AssetMetadataBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AssetMetadataBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AssetMetadataBuilder keywords(List<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public AssetMetadataBuilder createdDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public AssetMetadataBuilder modifiedDate(String modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public AssetMetadataBuilder author(String author) {
            this.author = author;
            return this;
        }

        public AssetMetadataBuilder location(String location) {
            this.location = location;
            return this;
        }

        public AssetMetadataBuilder rights(String rights) {
            this.rights = rights;
            return this;
        }

        public AssetMetadataBuilder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public AssetMetadataBuilder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public AssetMetadataBuilder customMetadata(Map<String, String> customMetadata) {
            this.customMetadata = customMetadata;
            return this;
        }

        public AssetMetadataBuilder existingTags(List<String> existingTags) {
            this.existingTags = existingTags;
            return this;
        }

        public AssetMetadataBuilder thumbnailPath(String thumbnailPath) {
            this.thumbnailPath = thumbnailPath;
            return this;
        }

        public AssetMetadataBuilder aemPath(String aemPath) {
            this.aemPath = aemPath;
            return this;
        }

        public AssetMetadata build() {
            return new AssetMetadata(assetId, assetPath, assetName, assetType,
                    title, description, keywords, createdDate, modifiedDate,
                    author, location, rights, fileSize, mimeType,
                    customMetadata, existingTags, thumbnailPath, aemPath);
        }
    }
}