package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.Map;

public class DriftDetail {

    private String path;
    private String changeType;
    private double impactScore;
    private String description;
    private Map<String, Object> metadata;

    public DriftDetail() {
        this.metadata = new HashMap<>();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public double getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(double impactScore) {
        this.impactScore = impactScore;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}