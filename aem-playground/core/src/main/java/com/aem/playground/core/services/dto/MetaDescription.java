package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class MetaDescription {

    private String description;
    private int characterCount;
    private int maxCharacters;
    private double confidenceScore;
    private LocalDateTime generatedAt;
    private String sourceContent;
    private Map<String, Object> metadata;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
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

    public static MetaDescription create(int maxCharacters) {
        MetaDescription meta = new MetaDescription();
        meta.setMaxCharacters(maxCharacters);
        meta.setGeneratedAt(LocalDateTime.now());
        return meta;
    }
}