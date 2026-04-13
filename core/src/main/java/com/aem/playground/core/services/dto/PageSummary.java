package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PageSummary {

    private String pagePath;
    private String title;
    private String heading;
    private String summaryText;
    private int wordCount;
    private int targetLength;
    private double confidenceScore;
    private LocalDateTime generatedAt;
    private LocalDateTime validUntil;
    private List<String> mainPoints;
    private List<String> keywords;
    private String contentType;
    private Map<String, Object> metadata;

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getTargetLength() {
        return targetLength;
    }

    public void setTargetLength(int targetLength) {
        this.targetLength = targetLength;
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

    public List<String> getMainPoints() {
        return mainPoints;
    }

    public void setMainPoints(List<String> mainPoints) {
        this.mainPoints = mainPoints;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static PageSummary create(String pagePath, String contentType) {
        PageSummary summary = new PageSummary();
        summary.setPagePath(pagePath);
        summary.setContentType(contentType);
        summary.setGeneratedAt(LocalDateTime.now());
        return summary;
    }
}