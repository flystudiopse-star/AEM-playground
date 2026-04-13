package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ContentFragmentSummary {

    private String fragmentPath;
    private String modelName;
    private String summaryText;
    private List<String> elements;
    private Map<String, String> elementSummaries;
    private String primaryTheme;
    private List<String> tags;
    private String format;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private Map<String, Object> metadata;

    public String getFragmentPath() {
        return fragmentPath;
    }

    public void setFragmentPath(String fragmentPath) {
        this.fragmentPath = fragmentPath;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public List<String> getElements() {
        return elements;
    }

    public void setElements(List<String> elements) {
        this.elements = elements;
    }

    public Map<String, String> getElementSummaries() {
        return elementSummaries;
    }

    public void setElementSummaries(Map<String, String> elementSummaries) {
        this.elementSummaries = elementSummaries;
    }

    public String getPrimaryTheme() {
        return primaryTheme;
    }

    public void setPrimaryTheme(String primaryTheme) {
        this.primaryTheme = primaryTheme;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static ContentFragmentSummary create(String fragmentPath, String modelName) {
        ContentFragmentSummary summary = new ContentFragmentSummary();
        summary.setFragmentPath(fragmentPath);
        summary.setModelName(modelName);
        summary.setCreatedAt(LocalDateTime.now());
        summary.setLastModified(LocalDateTime.now());
        return summary;
    }
}