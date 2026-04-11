package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ContentHighlight {

    private String id;
    private String contentPath;
    private String highlightText;
    private String context;
    private String highlightType;
    private double importanceScore;
    private int startPosition;
    private int endPosition;
    private List<String> relatedTopics;
    private LocalDateTime extractedAt;
    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getHighlightText() {
        return highlightText;
    }

    public void setHighlightText(String highlightText) {
        this.highlightText = highlightText;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getHighlightType() {
        return highlightType;
    }

    public void setHighlightType(String highlightType) {
        this.highlightType = highlightType;
    }

    public double getImportanceScore() {
        return importanceScore;
    }

    public void setImportanceScore(double importanceScore) {
        this.importanceScore = importanceScore;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public List<String> getRelatedTopics() {
        return relatedTopics;
    }

    public void setRelatedTopics(List<String> relatedTopics) {
        this.relatedTopics = relatedTopics;
    }

    public LocalDateTime getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(LocalDateTime extractedAt) {
        this.extractedAt = extractedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static ContentHighlight create(String contentPath, String highlightText, String highlightType) {
        ContentHighlight highlight = new ContentHighlight();
        highlight.setId("highlight-" + System.currentTimeMillis());
        highlight.setContentPath(contentPath);
        highlight.setHighlightText(highlightText);
        highlight.setHighlightType(highlightType);
        highlight.setExtractedAt(LocalDateTime.now());
        return highlight;
    }
}