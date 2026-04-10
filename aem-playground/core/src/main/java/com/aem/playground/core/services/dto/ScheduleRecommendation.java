package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class ScheduleRecommendation {

    private String recommendationId;
    private String contentPath;
    private long scheduledPublishTime;
    private long optimalPublishTime;
    private String targetTimezone;
    private double confidenceScore;
    private String recommendationType;
    private List<String> reasoning;
    private Map<String, Object> metadata;

    public String getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(String recommendationId) {
        this.recommendationId = recommendationId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public long getScheduledPublishTime() {
        return scheduledPublishTime;
    }

    public void setScheduledPublishTime(long scheduledPublishTime) {
        this.scheduledPublishTime = scheduledPublishTime;
    }

    public long getOptimalPublishTime() {
        return optimalPublishTime;
    }

    public void setOptimalPublishTime(long optimalPublishTime) {
        this.optimalPublishTime = optimalPublishTime;
    }

    public String getTargetTimezone() {
        return targetTimezone;
    }

    public void setTargetTimezone(String targetTimezone) {
        this.targetTimezone = targetTimezone;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public List<String> getReasoning() {
        return reasoning;
    }

    public void setReasoning(List<String> reasoning) {
        this.reasoning = reasoning;
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
        private ScheduleRecommendation rec = new ScheduleRecommendation();

        public Builder recommendationId(String recommendationId) {
            rec.recommendationId = recommendationId;
            return this;
        }

        public Builder contentPath(String contentPath) {
            rec.contentPath = contentPath;
            return this;
        }

        public Builder scheduledPublishTime(long scheduledPublishTime) {
            rec.scheduledPublishTime = scheduledPublishTime;
            return this;
        }

        public Builder optimalPublishTime(long optimalPublishTime) {
            rec.optimalPublishTime = optimalPublishTime;
            return this;
        }

        public Builder targetTimezone(String targetTimezone) {
            rec.targetTimezone = targetTimezone;
            return this;
        }

        public Builder confidenceScore(double confidenceScore) {
            rec.confidenceScore = confidenceScore;
            return this;
        }

        public Builder recommendationType(String recommendationType) {
            rec.recommendationType = recommendationType;
            return this;
        }

        public Builder reasoning(List<String> reasoning) {
            rec.reasoning = reasoning;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            rec.metadata = metadata;
            return this;
        }

        public ScheduleRecommendation build() {
            return rec;
        }
    }
}