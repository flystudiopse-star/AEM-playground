package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ContentAffinityScore {

    private String contentPath;
    private String userId;
    private double affinityScore;
    private List<String> matchedTags;
    private List<String> matchedCategories;
    private List<String> similarUsersWhoViewed;
    private LocalDateTime calculatedAt;
    private String recommendationReason;
    private double confidenceScore;
    private String contentType;
    private Map<String, Object> metadata;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAffinityScore() {
        return affinityScore;
    }

    public void setAffinityScore(double affinityScore) {
        this.affinityScore = affinityScore;
    }

    public List<String> getMatchedTags() {
        return matchedTags;
    }

    public void setMatchedTags(List<String> matchedTags) {
        this.matchedTags = matchedTags;
    }

    public List<String> getMatchedCategories() {
        return matchedCategories;
    }

    public void setMatchedCategories(List<String> matchedCategories) {
        this.matchedCategories = matchedCategories;
    }

    public List<String> getSimilarUsersWhoViewed() {
        return similarUsersWhoViewed;
    }

    public void setSimilarUsersWhoViewed(List<String> similarUsersWhoViewed) {
        this.similarUsersWhoViewed = similarUsersWhoViewed;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static ContentAffinityScore create(String contentPath, String userId, double affinityScore) {
        ContentAffinityScore score = new ContentAffinityScore();
        score.setContentPath(contentPath);
        score.setUserId(userId);
        score.setAffinityScore(affinityScore);
        score.setCalculatedAt(LocalDateTime.now());
        return score;
    }
}
