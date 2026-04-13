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

public class ContentTag {
    private final String name;
    private final String category;
    private final double confidence;
    private final String source;

    private ContentTag(String name, String category, double confidence, String source) {
        this.name = name;
        this.category = category;
        this.confidence = confidence;
        this.source = source;
    }

    public static ContentTag create(String name, String category, double confidence, String source) {
        return new ContentTag(name, category, confidence, source);
    }

    public static ContentTag aiGenerated(String name, String category, double confidence) {
        return new ContentTag(name, category, confidence, "ai");
    }

    public static ContentTag manual(String name, String category) {
        return new ContentTag(name, category, 1.0, "manual");
    }

    public String getName() {
        return name;
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

    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
}