package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RelatedContentRecommendation {

    private String sourceContentPath;
    private List<ContentRecommendation> recommendations;
    private String algorithmUsed;
    private LocalDateTime generatedAt;
    private int maxRecommendations;
    private double minimumRelevanceThreshold;
    private Map<String, Object> algorithmParameters;

    public String getSourceContentPath() {
        return sourceContentPath;
    }

    public void setSourceContentPath(String sourceContentPath) {
        this.sourceContentPath = sourceContentPath;
    }

    public List<ContentRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<ContentRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public void setAlgorithmUsed(String algorithmUsed) {
        this.algorithmUsed = algorithmUsed;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public int getMaxRecommendations() {
        return maxRecommendations;
    }

    public void setMaxRecommendations(int maxRecommendations) {
        this.maxRecommendations = maxRecommendations;
    }

    public double getMinimumRelevanceThreshold() {
        return minimumRelevanceThreshold;
    }

    public void setMinimumRelevanceThreshold(double minimumRelevanceThreshold) {
        this.minimumRelevanceThreshold = minimumRelevanceThreshold;
    }

    public Map<String, Object> getAlgorithmParameters() {
        return algorithmParameters;
    }

    public void setAlgorithmParameters(Map<String, Object> algorithmParameters) {
        this.algorithmParameters = algorithmParameters;
    }

    public static RelatedContentRecommendation create(String sourceContentPath) {
        RelatedContentRecommendation rec = new RelatedContentRecommendation();
        rec.setSourceContentPath(sourceContentPath);
        rec.setGeneratedAt(LocalDateTime.now());
        return rec;
    }
}