package com.aem.playground.core.services.dto;

import java.util.Map;

public class EngagementScore {

    private String scoreId;
    private String contentPath;
    private double predictedEngagement;
    private double confidenceLevel;
    private String scoreCategory;
    private Map<String, Double> componentScores;
    private long calculatedAt;
    private Map<String, Object> metadata;

    public String getScoreId() {
        return scoreId;
    }

    public void setScoreId(String scoreId) {
        this.scoreId = scoreId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public double getPredictedEngagement() {
        return predictedEngagement;
    }

    public void setPredictedEngagement(double predictedEngagement) {
        this.predictedEngagement = predictedEngagement;
    }

    public double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getScoreCategory() {
        return scoreCategory;
    }

    public void setScoreCategory(String scoreCategory) {
        this.scoreCategory = scoreCategory;
    }

    public Map<String, Double> getComponentScores() {
        return componentScores;
    }

    public void setComponentScores(Map<String, Double> componentScores) {
        this.componentScores = componentScores;
    }

    public long getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(long calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private EngagementScore score = new EngagementScore();

        public Builder scoreId(String scoreId) {
            score.scoreId = scoreId;
            return this;
        }

        public Builder contentPath(String contentPath) {
            score.contentPath = contentPath;
            return this;
        }

        public Builder predictedEngagement(double predictedEngagement) {
            score.predictedEngagement = predictedEngagement;
            return this;
        }

        public Builder confidenceLevel(double confidenceLevel) {
            score.confidenceLevel = confidenceLevel;
            return this;
        }

        public Builder scoreCategory(String scoreCategory) {
            score.scoreCategory = scoreCategory;
            return this;
        }

        public Builder componentScores(Map<String, Double> componentScores) {
            score.componentScores = componentScores;
            return this;
        }

        public Builder calculatedAt(long calculatedAt) {
            score.calculatedAt = calculatedAt;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            score.metadata = metadata;
            return this;
        }

        public EngagementScore build() {
            return score;
        }
    }
}