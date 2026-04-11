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

public class IntelligentTag {
    private final String tagName;
    private final String category;
    private final double confidence;
    private final String source;
    private final String tagType;
    private final boolean isTechnical;

    private IntelligentTag(String tagName, String category, double confidence, String source, String tagType, boolean isTechnical) {
        this.tagName = tagName;
        this.category = category;
        this.confidence = confidence;
        this.source = source;
        this.tagType = tagType;
        this.isTechnical = isTechnical;
    }

    public static IntelligentTag create(String tagName, String category, double confidence, String source, String tagType, boolean isTechnical) {
        return new IntelligentTag(tagName, category, confidence, source, tagType, isTechnical);
    }

    public static IntelligentTag aiGenerated(String tagName, String category, double confidence) {
        return new IntelligentTag(tagName, category, confidence, "ai", "smart", false);
    }

    public static IntelligentTag technical(String tagName, String category) {
        return new IntelligentTag(tagName, category, 1.0, "ai", "technical", true);
    }

    public static IntelligentTag contentBased(String tagName, String category, double confidence) {
        return new IntelligentTag(tagName, category, confidence, "ai", "content", false);
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

    public String getTagType() {
        return tagType;
    }

    public boolean isTechnical() {
        return isTechnical;
    }

    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
}