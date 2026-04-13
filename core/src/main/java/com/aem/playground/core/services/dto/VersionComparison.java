package com.aem.playground.core.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionComparison {

    private String contentPath;
    private String versionId1;
    private String versionId2;
    private double similarityScore;
    private List<ContentDifference> differences;
    private String summary;
    private Map<String, Object> metadata;

    public VersionComparison() {
        this.differences = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getVersionId1() {
        return versionId1;
    }

    public void setVersionId1(String versionId1) {
        this.versionId1 = versionId1;
    }

    public String getVersionId2() {
        return versionId2;
    }

    public void setVersionId2(String versionId2) {
        this.versionId2 = versionId2;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public List<ContentDifference> getDifferences() {
        return differences;
    }

    public void setDifferences(List<ContentDifference> differences) {
        this.differences = differences;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}