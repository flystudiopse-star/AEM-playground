package com.aem.playground.core.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentDrift {

    private String contentPath;
    private String baseVersionId;
    private String currentVersionId;
    private double driftScore;
    private DriftStatus status;
    private List<DriftDetail> driftDetails;
    private long detectedAt;
    private String driftReason;
    private Map<String, Object> metadata;

    public ContentDrift() {
        this.driftDetails = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getBaseVersionId() {
        return baseVersionId;
    }

    public void setBaseVersionId(String baseVersionId) {
        this.baseVersionId = baseVersionId;
    }

    public String getCurrentVersionId() {
        return currentVersionId;
    }

    public void setCurrentVersionId(String currentVersionId) {
        this.currentVersionId = currentVersionId;
    }

    public double getDriftScore() {
        return driftScore;
    }

    public void setDriftScore(double driftScore) {
        this.driftScore = driftScore;
    }

    public DriftStatus getStatus() {
        return status;
    }

    public void setStatus(DriftStatus status) {
        this.status = status;
    }

    public List<DriftDetail> getDriftDetails() {
        return driftDetails;
    }

    public void setDriftDetails(List<DriftDetail> driftDetails) {
        this.driftDetails = driftDetails;
    }

    public long getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(long detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getDriftReason() {
        return driftReason;
    }

    public void setDriftReason(String driftReason) {
        this.driftReason = driftReason;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public enum DriftStatus {
        STABLE,
        MINOR_DRIFT,
        MODERATE_DRIFT,
        MAJOR_DRIFT,
        CRITICAL_DRIFT
    }
}