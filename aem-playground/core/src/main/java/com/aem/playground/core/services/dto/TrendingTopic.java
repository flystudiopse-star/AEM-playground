package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TrendingTopic {

    private String topic;
    private String category;
    private double popularityScore;
    private double growthRate;
    private List<String> relatedKeywords;
    private LocalDateTime detectedAt;
    private LocalDateTime expiresAt;
    private Map<String, Object> metadata;
    private String source;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(double popularityScore) {
        this.popularityScore = popularityScore;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    public List<String> getRelatedKeywords() {
        return relatedKeywords;
    }

    public void setRelatedKeywords(List<String> relatedKeywords) {
        this.relatedKeywords = relatedKeywords;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static TrendingTopic create(String topic, String category) {
        TrendingTopic trending = new TrendingTopic();
        trending.setTopic(topic);
        trending.setCategory(category);
        trending.setDetectedAt(LocalDateTime.now());
        return trending;
    }
}