package com.aem.playground.core.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionRestorePoint {

    private String restorePointId;
    private String contentPath;
    private String versionId;
    private String label;
    private String description;
    private long timestamp;
    private String author;
    private RestorePointType type;
    private Map<String, Object> snapshot;
    private List<String> includedPaths;
    private boolean isIntelligent;
    private Map<String, Object> metadata;

    public VersionRestorePoint() {
        this.snapshot = new HashMap<>();
        this.includedPaths = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public String getRestorePointId() {
        return restorePointId;
    }

    public void setRestorePointId(String restorePointId) {
        this.restorePointId = restorePointId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public RestorePointType getType() {
        return type;
    }

    public void setType(RestorePointType type) {
        this.type = type;
    }

    public Map<String, Object> getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Map<String, Object> snapshot) {
        this.snapshot = snapshot;
    }

    public List<String> getIncludedPaths() {
        return includedPaths;
    }

    public void setIncludedPaths(List<String> includedPaths) {
        this.includedPaths = includedPaths;
    }

    public boolean isIntelligent() {
        return isIntelligent;
    }

    public void setIntelligent(boolean intelligent) {
        isIntelligent = intelligent;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public enum RestorePointType {
        MANUAL,
        SCHEDULED,
        INTELLIGENT_AUTO,
        EMERGENCY
    }
}