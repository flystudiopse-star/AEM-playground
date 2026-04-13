package com.aem.playground.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModerationResult {

    private String contentPath;
    private boolean isApproved;
    private List<ModerationViolation> violations;
    private double overallScore;
    private String recommendation;
    private long timestamp;
    private Map<String, Object> metadata;

    public ModerationResult() {
        this.violations = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    public static ModerationResultBuilder builder() {
        return new ModerationResultBuilder();
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public List<ModerationViolation> getViolations() {
        return violations;
    }

    public void setViolations(List<ModerationViolation> violations) {
        this.violations = violations;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static class ModerationResultBuilder {
        private final ModerationResult result = new ModerationResult();

        public ModerationResultBuilder contentPath(String contentPath) {
            result.contentPath = contentPath;
            return this;
        }

        public ModerationResultBuilder isApproved(boolean isApproved) {
            result.isApproved = isApproved;
            return this;
        }

        public ModerationResultBuilder violations(List<ModerationViolation> violations) {
            result.violations = violations;
            return this;
        }

        public ModerationResultBuilder overallScore(double overallScore) {
            result.overallScore = overallScore;
            return this;
        }

        public ModerationResultBuilder recommendation(String recommendation) {
            result.recommendation = recommendation;
            return this;
        }

        public ModerationResultBuilder metadata(Map<String, Object> metadata) {
            result.metadata = metadata;
            return this;
        }

        public ModerationResult build() {
            return result;
        }
    }
}
