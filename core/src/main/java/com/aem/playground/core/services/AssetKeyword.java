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

public class AssetKeyword {
    private final String keyword;
    private final String category;
    private final double confidence;
    private final String source;
    private final List<String> synonyms;

    private AssetKeyword(String keyword, String category, double confidence, String source, List<String> synonyms) {
        this.keyword = keyword;
        this.category = category;
        this.confidence = confidence;
        this.source = source;
        this.synonyms = synonyms;
    }

    public static AssetKeyword create(String keyword, String category, double confidence, String source, List<String> synonyms) {
        return new AssetKeyword(keyword, category, confidence, source, synonyms);
    }

    public static AssetKeyword aiGenerated(String keyword, String category, double confidence) {
        return new AssetKeyword(keyword, category, confidence, "ai", null);
    }

    public static AssetKeyword simple(String keyword) {
        return new AssetKeyword(keyword, null, 1.0, "manual", null);
    }

    public String getKeyword() {
        return keyword;
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

    public List<String> getSynonyms() {
        return synonyms;
    }

    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
}