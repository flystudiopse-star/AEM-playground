package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ExecutiveSummary {

    private String contentPath;
    private String title;
    private String briefOverview;
    private List<String> keyPoints;
    private String businessImpact;
    private List<String> stakeholders;
    private String recommendation;
    private String decisionRequired;
    private LocalDateTime generatedAt;
    private int maxLength;
    private Map<String, Object> metadata;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBriefOverview() {
        return briefOverview;
    }

    public void setBriefOverview(String briefOverview) {
        this.briefOverview = briefOverview;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public String getBusinessImpact() {
        return businessImpact;
    }

    public void setBusinessImpact(String businessImpact) {
        this.businessImpact = businessImpact;
    }

    public List<String> getStakeholders() {
        return stakeholders;
    }

    public void setStakeholders(List<String> stakeholders) {
        this.stakeholders = stakeholders;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getDecisionRequired() {
        return decisionRequired;
    }

    public void setDecisionRequired(String decisionRequired) {
        this.decisionRequired = decisionRequired;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static ExecutiveSummary create(String contentPath, String title) {
        ExecutiveSummary summary = new ExecutiveSummary();
        summary.setContentPath(contentPath);
        summary.setTitle(title);
        summary.setGeneratedAt(LocalDateTime.now());
        summary.setMaxLength(500);
        return summary;
    }
}