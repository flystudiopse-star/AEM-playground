package com.aem.playground.core.services.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class SchedulingAnalysis {

    private String contentPath;
    private String contentType;
    private String targetTimezone;
    private ZonedDateTime optimalPublishTime;
    private ZonedDateTime optimalUnpublishTime;
    private double confidenceScore;
    private String reasoning;
    private List<String> factors;
    private List<ZonedDateTime> alternativeSlots;
    private Map<String, Double> engagementPredictions;
    private ZonedDateTime analyzedAt;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTargetTimezone() {
        return targetTimezone;
    }

    public void setTargetTimezone(String targetTimezone) {
        this.targetTimezone = targetTimezone;
    }

    public ZonedDateTime getOptimalPublishTime() {
        return optimalPublishTime;
    }

    public void setOptimalPublishTime(ZonedDateTime optimalPublishTime) {
        this.optimalPublishTime = optimalPublishTime;
    }

    public ZonedDateTime getOptimalUnpublishTime() {
        return optimalUnpublishTime;
    }

    public void setOptimalUnpublishTime(ZonedDateTime optimalUnpublishTime) {
        this.optimalUnpublishTime = optimalUnpublishTime;
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

    public void setReason(String reason) {
        this.reasoning = reason;
    }

    public List<String> getFactors() {
        return factors;
    }

    public void setFactors(List<String> factors) {
        this.factors = factors;
    }

    public List<ZonedDateTime> getAlternativeSlots() {
        return alternativeSlots;
    }

    public void setAlternativeSlots(List<ZonedDateTime> alternativeSlots) {
        this.alternativeSlots = alternativeSlots;
    }

    public Map<String, Double> getEngagementPredictions() {
        return engagementPredictions;
    }

    public void setEngagementPredictions(Map<String, Double> engagementPredictions) {
        this.engagementPredictions = engagementPredictions;
    }

    public ZonedDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(ZonedDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public static SchedulingAnalysis create(String contentPath, String contentType, String targetTimezone) {
        SchedulingAnalysis analysis = new SchedulingAnalysis();
        analysis.setContentPath(contentPath);
        analysis.setContentType(contentType);
        analysis.setTargetTimezone(targetTimezone);
        analysis.setAnalyzedAt(ZonedDateTime.now());
        return analysis;
    }
}