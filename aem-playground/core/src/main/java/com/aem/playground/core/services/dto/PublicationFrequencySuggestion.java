package com.aem.playground.core.services.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class PublicationFrequencySuggestion {

    private String contentType;
    private String recommendedFrequency;
    private int postsPerWeek;
    private int postsPerMonth;
    private double confidenceScore;
    private String reasoning;
    private List<String> bestDays;
    private List<String> peakHours;
    private Map<String, Object> recommendations = new java.util.HashMap<>();
    private ZonedDateTime analyzedAt;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRecommendedFrequency() {
        return recommendedFrequency;
    }

    public void setRecommendedFrequency(String recommendedFrequency) {
        this.recommendedFrequency = recommendedFrequency;
    }

    public int getPostsPerWeek() {
        return postsPerWeek;
    }

    public void setPostsPerWeek(int postsPerWeek) {
        this.postsPerWeek = postsPerWeek;
    }

    public int getPostsPerMonth() {
        return postsPerMonth;
    }

    public void setPostsPerMonth(int postsPerMonth) {
        this.postsPerMonth = postsPerMonth;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public List<String> getBestDays() {
        return bestDays;
    }

    public void setBestDays(List<String> bestDays) {
        this.bestDays = bestDays;
    }

    public List<String> getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(List<String> peakHours) {
        this.peakHours = peakHours;
    }

    public Map<String, Object> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(Map<String, Object> recommendations) {
        this.recommendations = recommendations;
    }

    public ZonedDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(ZonedDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public static PublicationFrequencySuggestion create(String contentType) {
        PublicationFrequencySuggestion suggestion = new PublicationFrequencySuggestion();
        suggestion.setContentType(contentType);
        suggestion.setAnalyzedAt(ZonedDateTime.now());
        return suggestion;
    }
}