package com.aem.playground.core.services.dto;

import java.util.Map;

public class PublicationFrequency {

    private String frequencyId;
    private String contentType;
    private String frequency;
    private int postsPerWeek;
    private int optimalPostCount;
    private String rationale;
    private double confidenceScore;
    private Map<String, Object> metadata;

    public String getFrequencyId() {
        return frequencyId;
    }

    public void setFrequencyId(String frequencyId) {
        this.frequencyId = frequencyId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getPostsPerWeek() {
        return postsPerWeek;
    }

    public void setPostsPerWeek(int postsPerWeek) {
        this.postsPerWeek = postsPerWeek;
    }

    public int getOptimalPostCount() {
        return optimalPostCount;
    }

    public void setOptimalPostCount(int optimalPostCount) {
        this.optimalPostCount = optimalPostCount;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
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
        private PublicationFrequency freq = new PublicationFrequency();

        public Builder frequencyId(String frequencyId) {
            freq.frequencyId = frequencyId;
            return this;
        }

        public Builder contentType(String contentType) {
            freq.contentType = contentType;
            return this;
        }

        public Builder frequency(String frequency) {
            freq.frequency = frequency;
            return this;
        }

        public Builder postsPerWeek(int postsPerWeek) {
            freq.postsPerWeek = postsPerWeek;
            return this;
        }

        public Builder optimalPostCount(int optimalPostCount) {
            freq.optimalPostCount = optimalPostCount;
            return this;
        }

        public Builder rationale(String rationale) {
            freq.rationale = rationale;
            return this;
        }

        public Builder confidenceScore(double confidenceScore) {
            freq.confidenceScore = confidenceScore;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            freq.metadata = metadata;
            return this;
        }

        public PublicationFrequency build() {
            return freq;
        }
    }
}