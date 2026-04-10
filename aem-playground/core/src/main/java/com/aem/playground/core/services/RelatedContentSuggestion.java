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

public class RelatedContentSuggestion {
    private final String contentId;
    private final String title;
    private final String path;
    private final double similarityScore;
    private final String relationType;
    private final String matchReason;

    private RelatedContentSuggestion(String contentId, String title, String path, double similarityScore,
                                   String relationType, String matchReason) {
        this.contentId = contentId;
        this.title = title;
        this.path = path;
        this.similarityScore = similarityScore;
        this.relationType = relationType;
        this.matchReason = matchReason;
    }

    public static RelatedContentSuggestion create(String contentId, String title, String path,
                                                  double similarityScore, String relationType, String matchReason) {
        return new RelatedContentSuggestion(contentId, title, path, similarityScore, relationType, matchReason);
    }

    public static RelatedContentSuggestion similar(String contentId, String title, String path, double score) {
        return new RelatedContentSuggestion(contentId, title, path, score, "similar", null);
    }

    public String getContentId() {
        return contentId;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
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

    public boolean isHighlySimilar() {
        return similarityScore >= 0.8;
    }
}