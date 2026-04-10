package com.aem.playground.core.services;

import java.util.HashMap;
import java.util.Map;

public class RecommendedAsset {
    private String path;
    private String name;
    private double score;
    private String reason;
    private Map<String, Object> metadata = new HashMap<>();

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}