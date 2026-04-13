package com.aem.playground.core.services.dto;

public class ContentDifference {

    private String path;
    private DifferenceType type;
    private String oldValue;
    private String newValue;
    private String description;
    private double severity;
    private boolean aiDetected;

    public ContentDifference() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public DifferenceType getType() {
        return type;
    }

    public void setType(DifferenceType type) {
        this.type = type;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSeverity() {
        return severity;
    }

    public void setSeverity(double severity) {
        this.severity = severity;
    }

    public boolean isAiDetected() {
        return aiDetected;
    }

    public void setAiDetected(boolean aiDetected) {
        this.aiDetected = aiDetected;
    }

    public enum DifferenceType {
        ADDED,
        REMOVED,
        MODIFIED,
        MOVED,
        FORMAT_CHANGE
    }
}