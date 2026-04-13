package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleSuggestion {

    private String contentPath;
    private LocalDateTime suggestedPublishTime;
    private LocalDateTime suggestedUnpublishTime;
    private List<LocalDateTime> alternativeTimes;
    private double confidenceScore;
    private String reasoning;
    private List<String> factors;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public LocalDateTime getSuggestedPublishTime() {
        return suggestedPublishTime;
    }

    public void setSuggestedPublishTime(LocalDateTime suggestedPublishTime) {
        this.suggestedPublishTime = suggestedPublishTime;
    }

    public LocalDateTime getSuggestedUnpublishTime() {
        return suggestedUnpublishTime;
    }

    public void setSuggestedUnpublishTime(LocalDateTime suggestedUnpublishTime) {
        this.suggestedUnpublishTime = suggestedUnpublishTime;
    }

    public List<LocalDateTime> getAlternativeTimes() {
        return alternativeTimes;
    }

    public void setAlternativeTimes(List<LocalDateTime> alternativeTimes) {
        this.alternativeTimes = alternativeTimes;
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

    public static ScheduleSuggestion create(String contentPath) {
        ScheduleSuggestion suggestion = new ScheduleSuggestion();
        suggestion.setContentPath(contentPath);
        return suggestion;
    }
}