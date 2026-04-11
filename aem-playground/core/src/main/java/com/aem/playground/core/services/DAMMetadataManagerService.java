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

public interface DAMMetadataManagerService {

    MetadataExtractionResult extractMetadata(AssetMetadata asset, MetadataExtractionOptions options);

    KeywordGenerationResult generateSmartKeywords(AssetMetadata asset, KeywordOptions options);

    List<AssetCategory> createContentBasedCategories(List<AssetMetadata> assets, int maxCategories);

    List<AssetRelationship> suggestAssetRelationships(AssetMetadata asset, List<AssetMetadata> existingAssets, int maxRelationships);

    IntelligentTaggingResult addIntelligentTags(AssetMetadata asset, TaggingOptions options);

    class MetadataExtractionResult {
        private final boolean success;
        private final AssetMetadata metadata;
        private final String error;
        private final long processingTimeMs;

        private MetadataExtractionResult(boolean success, AssetMetadata metadata, String error, long processingTimeMs) {
            this.success = success;
            this.metadata = metadata;
            this.error = error;
            this.processingTimeMs = processingTimeMs;
        }

        public static MetadataExtractionResult success(AssetMetadata metadata, long processingTimeMs) {
            return new MetadataExtractionResult(true, metadata, null, processingTimeMs);
        }

        public static MetadataExtractionResult error(String error, long processingTimeMs) {
            return new MetadataExtractionResult(false, null, error, processingTimeMs);
        }

        public boolean isSuccess() {
            return success;
        }

        public AssetMetadata getMetadata() {
            return metadata;
        }

        public String getError() {
            return error;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }

    class MetadataExtractionOptions {
        private final boolean extractTitle;
        private final boolean extractDescription;
        private final boolean extractKeywords;
        private final boolean extractDate;
        private final boolean extractAuthor;
        private final boolean extractLocation;
        private final boolean extractRights;
        private final boolean extractCustomMetadata;

        private MetadataExtractionOptions(boolean extractTitle, boolean extractDescription, boolean extractKeywords,
                                     boolean extractDate, boolean extractAuthor, boolean extractLocation,
                                     boolean extractRights, boolean extractCustomMetadata) {
            this.extractTitle = extractTitle;
            this.extractDescription = extractDescription;
            this.extractKeywords = extractKeywords;
            this.extractDate = extractDate;
            this.extractAuthor = extractAuthor;
            this.extractLocation = extractLocation;
            this.extractRights = extractRights;
            this.extractCustomMetadata = extractCustomMetadata;
        }

        public static MetadataExtractionOptions defaultOptions() {
            return new MetadataExtractionOptions(true, true, true, true, true, true, true, true);
        }

        public static MetadataExtractionOptionsBuilder builder() {
            return new MetadataExtractionOptionsBuilder();
        }

        public boolean isExtractTitle() {
            return extractTitle;
        }

        public boolean isExtractDescription() {
            return extractDescription;
        }

        public boolean isExtractKeywords() {
            return extractKeywords;
        }

        public boolean isExtractDate() {
            return extractDate;
        }

        public boolean isExtractAuthor() {
            return extractAuthor;
        }

        public boolean isExtractLocation() {
            return extractLocation;
        }

        public boolean isExtractRights() {
            return extractRights;
        }

        public boolean isExtractCustomMetadata() {
            return extractCustomMetadata;
        }
    }

    class MetadataExtractionOptionsBuilder {
        private boolean extractTitle = true;
        private boolean extractDescription = true;
        private boolean extractKeywords = true;
        private boolean extractDate = true;
        private boolean extractAuthor = true;
        private boolean extractLocation = true;
        private boolean extractRights = true;
        private boolean extractCustomMetadata = true;

        public MetadataExtractionOptionsBuilder extractTitle(boolean extractTitle) {
            this.extractTitle = extractTitle;
            return this;
        }

        public MetadataExtractionOptionsBuilder extractDescription(boolean extractDescription) {
            this.extractDescription = extractDescription;
            return this;
        }

        public MetadataExtractionOptionsBuilder extractKeywords(boolean extractKeywords) {
            this.extractKeywords = extractKeywords;
            return this;
        }

        public MetadataExtractionOptionsBuilder extractDate(boolean extractDate) {
            this.extractDate = extractDate;
            return this;
        }

        public MetadataExtractionOptionsBuilder extractAuthor(boolean extractAuthor) {
            this.extractAuthor = extractAuthor;
            return this;
        }

        public MetadataExtractionOptionsBuilder extractLocation(boolean extractLocation) {
            this.extractLocation = extractLocation;
            return this;
        }

        public MetadataExtractionOptionsBuilder extractRights(boolean extractRights) {
            this.extractRights = extractRights;
            return this;
        }

        public MetadataExtractionOptionsBuilder extractCustomMetadata(boolean extractCustomMetadata) {
            this.extractCustomMetadata = extractCustomMetadata;
            return this;
        }

        public MetadataExtractionOptions build() {
            return new MetadataExtractionOptions(extractTitle, extractDescription, extractKeywords,
                                                   extractDate, extractAuthor, extractLocation,
                                                   extractRights, extractCustomMetadata);
        }
    }

    class KeywordGenerationResult {
        private final boolean success;
        private final List<AssetKeyword> keywords;
        private final String error;
        private final long processingTimeMs;

        private KeywordGenerationResult(boolean success, List<AssetKeyword> keywords, String error, long processingTimeMs) {
            this.success = success;
            this.keywords = keywords;
            this.error = error;
            this.processingTimeMs = processingTimeMs;
        }

        public static KeywordGenerationResult success(List<AssetKeyword> keywords, long processingTimeMs) {
            return new KeywordGenerationResult(true, keywords, null, processingTimeMs);
        }

        public static KeywordGenerationResult error(String error, long processingTimeMs) {
            return new KeywordGenerationResult(false, null, error, processingTimeMs);
        }

        public boolean isSuccess() {
            return success;
        }

        public List<AssetKeyword> getKeywords() {
            return keywords;
        }

        public String getError() {
            return error;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }

    class KeywordOptions {
        private final int maxKeywords;
        private final double minConfidence;
        private final boolean includeSynonyms;
        private final boolean dedupe;

        private KeywordOptions(int maxKeywords, double minConfidence, boolean includeSynonyms, boolean dedupe) {
            this.maxKeywords = maxKeywords;
            this.minConfidence = minConfidence;
            this.includeSynonyms = includeSynonyms;
            this.dedupe = dedupe;
        }

        public static KeywordOptions defaultOptions() {
            return new KeywordOptions(10, 0.5, true, true);
        }

        public static KeywordOptionsBuilder builder() {
            return new KeywordOptionsBuilder();
        }

        public int getMaxKeywords() {
            return maxKeywords;
        }

        public double getMinConfidence() {
            return minConfidence;
        }

        public boolean isIncludeSynonyms() {
            return includeSynonyms;
        }

        public boolean isDedupe() {
            return dedupe;
        }
    }

    class KeywordOptionsBuilder {
        private int maxKeywords = 10;
        private double minConfidence = 0.5;
        private boolean includeSynonyms = true;
        private boolean dedupe = true;

        public KeywordOptionsBuilder maxKeywords(int maxKeywords) {
            this.maxKeywords = maxKeywords;
            return this;
        }

        public KeywordOptionsBuilder minConfidence(double minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public KeywordOptionsBuilder includeSynonyms(boolean includeSynonyms) {
            this.includeSynonyms = includeSynonyms;
            return this;
        }

        public KeywordOptionsBuilder dedupe(boolean dedupe) {
            this.dedupe = dedupe;
            return this;
        }

        public KeywordOptions build() {
            return new KeywordOptions(maxKeywords, minConfidence, includeSynonyms, dedupe);
        }
    }

    class IntelligentTaggingResult {
        private final boolean success;
        private final List<IntelligentTag> tags;
        private final String error;
        private final long processingTimeMs;

        private IntelligentTaggingResult(boolean success, List<IntelligentTag> tags, String error, long processingTimeMs) {
            this.success = success;
            this.tags = tags;
            this.error = error;
            this.processingTimeMs = processingTimeMs;
        }

        public static IntelligentTaggingResult success(List<IntelligentTag> tags, long processingTimeMs) {
            return new IntelligentTaggingResult(true, tags, null, processingTimeMs);
        }

        public static IntelligentTaggingResult error(String error, long processingTimeMs) {
            return new IntelligentTaggingResult(false, null, error, processingTimeMs);
        }

        public boolean isSuccess() {
            return success;
        }

        public List<IntelligentTag> getTags() {
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
        private final boolean includeTechnicalTags;

        private TaggingOptions(int maxTags, double minConfidence, boolean includeCategories, boolean dedupe, boolean includeTechnicalTags) {
            this.maxTags = maxTags;
            this.minConfidence = minConfidence;
            this.includeCategories = includeCategories;
            this.dedupe = dedupe;
            this.includeTechnicalTags = includeTechnicalTags;
        }

        public static TaggingOptions defaultOptions() {
            return new TaggingOptions(10, 0.5, true, true, true);
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

        public boolean isIncludeTechnicalTags() {
            return includeTechnicalTags;
        }
    }

    class TaggingOptionsBuilder {
        private int maxTags = 10;
        private double minConfidence = 0.5;
        private boolean includeCategories = true;
        private boolean dedupe = true;
        private boolean includeTechnicalTags = true;

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

        public TaggingOptionsBuilder includeTechnicalTags(boolean includeTechnicalTags) {
            this.includeTechnicalTags = includeTechnicalTags;
            return this;
        }

        public TaggingOptions build() {
            return new TaggingOptions(maxTags, minConfidence, includeCategories, dedupe, includeTechnicalTags);
        }
    }
}