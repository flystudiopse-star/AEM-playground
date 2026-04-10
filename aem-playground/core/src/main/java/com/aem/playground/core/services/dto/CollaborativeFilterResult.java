package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CollaborativeFilterResult {

    private String userId;
    private List<String> similarUserIds;
    private List<ContentRecommendation> collaborativeRecommendations;
    private double similarityThreshold;
    private int maxSimilarUsers;
    private String algorithmType;
    private LocalDateTime calculatedAt;
    private Map<String, Object> similarityScores;
    private int totalSimilarUsersFound;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSimilarUserIds() {
        return similarUserIds;
    }

    public void setSimilarUserIds(List<String> similarUserIds) {
        this.similarUserIds = similarUserIds;
    }

    public List<ContentRecommendation> getCollaborativeRecommendations() {
        return collaborativeRecommendations;
    }

    public void setCollaborativeRecommendations(List<ContentRecommendation> collaborativeRecommendations) {
        this.collaborativeRecommendations = collaborativeRecommendations;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public int getMaxSimilarUsers() {
        return maxSimilarUsers;
    }

    public void setMaxSimilarUsers(int maxSimilarUsers) {
        this.maxSimilarUsers = maxSimilarUsers;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Map<String, Object> getSimilarityScores() {
        return similarityScores;
    }

    public void setSimilarityScores(Map<String, Object> similarityScores) {
        this.similarityScores = similarityScores;
    }

    public int getTotalSimilarUsersFound() {
        return totalSimilarUsersFound;
    }

    public void setTotalSimilarUsersFound(int totalSimilarUsersFound) {
        this.totalSimilarUsersFound = totalSimilarUsersFound;
    }

    public static CollaborativeFilterResult create(String userId) {
        CollaborativeFilterResult result = new CollaborativeFilterResult();
        result.setUserId(userId);
        result.setCalculatedAt(LocalDateTime.now());
        return result;
    }
}