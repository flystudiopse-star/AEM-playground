package com.aem.playground.core.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionSummary {

    private String versionId;
    private String contentPath;
    private String summary;
    private List<String> keyChanges;
    private long timestamp;
    private String author;
    private Map<String, Object> contentMetadata;
    private double aiConfidence;
    private String versionLabel;

    public VersionSummary() {
        this.keyChanges = new ArrayList<>();
        this.contentMetadata = new HashMap<>();
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeyChanges() {
        return keyChanges;
    }

    public void setKeyChanges(List<String> keyChanges) {
        this.keyChanges = keyChanges;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Map<String, Object> getContentMetadata() {
        return contentMetadata;
    }

    public void setContentMetadata(Map<String, Object> contentMetadata) {
        this.contentMetadata = contentMetadata;
    }

    public double getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }
}