package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class Excerpt {

    private String content;
    private int wordCount;
    private int maxLength;
    private double confidenceScore;
    private LocalDateTime generatedAt;
    private String sourceContent;
    private Map<String, Object> metadata;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
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

    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static Excerpt create(String content, int maxLength) {
        Excerpt excerpt = new Excerpt();
        excerpt.setContent(content);
        excerpt.setMaxLength(maxLength);
        excerpt.setGeneratedAt(LocalDateTime.now());
        return excerpt;
    }
}