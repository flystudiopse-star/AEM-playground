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

public class AssetRelationship {
    private final String sourceAssetId;
    private final String targetAssetId;
    private final String targetAssetPath;
    private final String targetAssetTitle;
    private final double similarityScore;
    private final String relationType;
    private final String matchReason;
    private final String confidence;

    private AssetRelationship(String sourceAssetId, String targetAssetId, String targetAssetPath, String targetAssetTitle,
                         double similarityScore, String relationType, String matchReason, String confidence) {
        this.sourceAssetId = sourceAssetId;
        this.targetAssetId = targetAssetId;
        this.targetAssetPath = targetAssetPath;
        this.targetAssetTitle = targetAssetTitle;
        this.similarityScore = similarityScore;
        this.relationType = relationType;
        this.matchReason = matchReason;
        this.confidence = confidence;
    }

    public static AssetRelationship create(String sourceAssetId, String targetAssetId, String targetAssetPath, String targetAssetTitle,
                               double similarityScore, String relationType, String matchReason, String confidence) {
        return new AssetRelationship(sourceAssetId, targetAssetId, targetAssetPath, targetAssetTitle,
                                 similarityScore, relationType, matchReason, confidence);
    }

    public static AssetRelationship similar(String sourceAssetId, String targetAssetId, String targetAssetPath,
                                        String targetAssetTitle, double score) {
        return new AssetRelationship(sourceAssetId, targetAssetId, targetAssetPath, targetAssetTitle,
                                 score, "similar-content", null, "high");
    }

    public static AssetRelationship related(String sourceAssetId, String targetAssetId, String targetAssetPath,
                                      String targetAssetTitle, double score, String reason) {
        return new AssetRelationship(sourceAssetId, targetAssetId, targetAssetPath, targetAssetTitle,
                                 score, "related", reason, "medium");
    }

    public String getSourceAssetId() {
        return sourceAssetId;
    }

    public String getTargetAssetId() {
        return targetAssetId;
    }

    public String getTargetAssetPath() {
        return targetAssetPath;
    }

    public String getTargetAssetTitle() {
        return targetAssetTitle;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public String getRelationType() {
        return relationType;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public String getConfidence() {
        return confidence;
    }

    public boolean isHighlySimilar() {
        return similarityScore >= 0.8;
    }
}