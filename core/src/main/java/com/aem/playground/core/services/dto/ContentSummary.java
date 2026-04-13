package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ContentSummary {

    private String contentPath;
    private String summaryText;
    private String title;
    private String contentType;
    private int wordCount;
    private String language;
    private double confidenceScore;
    private LocalDateTime generatedAt;
    private LocalDateTime validUntil;
    private List<String> mainTopics;
    private Map<String, Object> metadata;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public List<String> getMainTopics() {
        return mainTopics;
    }

    public void setMainTopics(List<String> mainTopics) {
        this.mainTopics = mainTopics;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static ContentSummary create(String contentPath, String contentType) {
        ContentSummary summary = new ContentSummary();
        summary.setContentPath(contentPath);
        summary.setContentType(contentType);
        summary.setGeneratedAt(LocalDateTime.now());
        return summary;
    }
}