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

public class AssetCategory {
    private final String name;
    private final String parentCategory;
    private final List<String> subcategories;
    private final double relevanceScore;
    private final List<String> associatedAssetIds;
    private final String categoryType;

    private AssetCategory(String name, String parentCategory, List<String> subcategories,
                          double relevanceScore, List<String> associatedAssetIds, String categoryType) {
        this.name = name;
        this.parentCategory = parentCategory;
        this.subcategories = subcategories;
        this.relevanceScore = relevanceScore;
        this.associatedAssetIds = associatedAssetIds;
        this.categoryType = categoryType;
    }

    public static AssetCategory create(String name, String parentCategory, List<String> subcategories,
                                     double relevanceScore, List<String> associatedAssetIds, String categoryType) {
        return new AssetCategory(name, parentCategory, subcategories, relevanceScore, associatedAssetIds, categoryType);
    }

    public static AssetCategory simple(String name, double relevanceScore) {
        return new AssetCategory(name, null, null, relevanceScore, null, "content-based");
    }

    public String getName() {
        return name;
    }

    public String getParentCategory() {
        return parentCategory;
    }

    public List<String> getSubcategories() {
        return subcategories;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public List<String> getAssociatedAssetIds() {
        return associatedAssetIds;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public boolean hasChildren() {
        return subcategories != null && !subcategories.isEmpty();
    }
}