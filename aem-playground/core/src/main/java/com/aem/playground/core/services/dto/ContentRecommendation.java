package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ContentRecommendation {

    private String contentPath;
    private String contentTitle;
    private double relevanceScore;
    private String recommendationType;
    private List<String> matchingTags;
    private List<String> matchingCategories;
    private LocalDateTime publishedDate;
    private String contentType;
    private String thumbnailPath;
    private String recommendationReason;
    private Map<String, Object> additionalProperties;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public List<String> getMatchingTags() {
        return matchingTags;
    }

    public void setMatchingTags(List<String> matchingTags) {
        this.matchingTags = matchingTags;
    }

    public List<String> getMatchingCategories() {
        return matchingCategories;
    }

    public void setMatchingCategories(List<String> matchingCategories) {
        this.matchingCategories = matchingCategories;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public static ContentRecommendation create(String contentPath, String contentTitle, double relevanceScore) {
        ContentRecommendation rec = new ContentRecommendation();
        rec.setContentPath(contentPath);
        rec.setContentTitle(contentTitle);
        rec.setRelevanceScore(relevanceScore);
        return rec;
    }
}