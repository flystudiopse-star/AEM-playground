package com.aem.playground.core.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionRecommendation {

    private String versionId;
    private RecommendationType recommendation;
    private String reason;
    private double confidenceScore;
    private List<String> suggestedActions;
    private Map<String, Object> metadata;

    public VersionRecommendation() {
        this.suggestedActions = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public RecommendationType getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(RecommendationType recommendation) {
        this.recommendation = recommendation;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public enum RecommendationType {
        KEEP,
        MERGE,
        DELETE,
        RESTORE,
        REVIEW
    }
}