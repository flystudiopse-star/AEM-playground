package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class KeyTakeaway {

    private String id;
    private String title;
    private String description;
    private String category;
    private double relevanceScore;
    private int priority;
    private boolean isActionable;
    private String actionItem;
    private LocalDateTime detectedAt;
    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isActionable() {
        return isActionable;
    }

    public void setActionable(boolean actionable) {
        isActionable = actionable;
    }

    public String getActionItem() {
        return actionItem;
    }

    public void setActionItem(String actionItem) {
        this.actionItem = actionItem;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static KeyTakeaway create(String title, String description, String category) {
        KeyTakeaway takeaway = new KeyTakeaway();
        takeaway.setId("takeaway-" + System.currentTimeMillis());
        takeaway.setTitle(title);
        takeaway.setDescription(description);
        takeaway.setCategory(category);
        takeaway.setDetectedAt(LocalDateTime.now());
        return takeaway;
    }
}