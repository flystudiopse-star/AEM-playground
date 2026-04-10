package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentAnalysisResult {

    private String contentPath;
    private String contentType;
    private String analysisId;
    private double complexityScore;
    private boolean isSimpleChange;
    private List<String> detectedChanges;
    private List<String> affectedComponents;
    private Map<String, Object> contentMetadata;
    private double riskScore;
    private String riskLevel;
    private Map<String, String> recommendations;
    private long analysisTimestamp;
    private String contentSummary;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public double getComplexityScore() {
        return complexityScore;
    }

    public void setComplexityScore(double complexityScore) {
        this.complexityScore = complexityScore;
    }

    public boolean isSimpleChange() {
        return isSimpleChange;
    }

    public void setSimpleChange(boolean simpleChange) {
        isSimpleChange = simpleChange;
    }

    public List<String> getDetectedChanges() {
        return detectedChanges;
    }

    public void setDetectedChanges(List<String> detectedChanges) {
        this.detectedChanges = detectedChanges;
    }

    public List<String> getAffectedComponents() {
        return affectedComponents;
    }

    public void setAffectedComponents(List<String> affectedComponents) {
        this.affectedComponents = affectedComponents;
    }

    public Map<String, Object> getContentMetadata() {
        return contentMetadata;
    }

    public void setContentMetadata(Map<String, Object> contentMetadata) {
        this.contentMetadata = contentMetadata;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Map<String, String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(Map<String, String> recommendations) {
        this.recommendations = recommendations;
    }

    public long getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public void setAnalysisTimestamp(long analysisTimestamp) {
        this.analysisTimestamp = analysisTimestamp;
    }

    public String getContentSummary() {
        return contentSummary;
    }

    public void setContentSummary(String contentSummary) {
        this.contentSummary = contentSummary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ContentAnalysisResult result = new ContentAnalysisResult();

        public Builder contentPath(String contentPath) {
            result.contentPath = contentPath;
            return this;
        }

        public Builder contentType(String contentType) {
            result.contentType = contentType;
            return this;
        }

        public Builder analysisId(String analysisId) {
            result.analysisId = analysisId;
            return this;
        }

        public Builder complexityScore(double complexityScore) {
            result.complexityScore = complexityScore;
            return this;
        }

        public Builder isSimpleChange(boolean isSimpleChange) {
            result.isSimpleChange = isSimpleChange;
            return this;
        }

        public Builder detectedChanges(List<String> detectedChanges) {
            result.detectedChanges = detectedChanges;
            return this;
        }

        public Builder affectedComponents(List<String> affectedComponents) {
            result.affectedComponents = affectedComponents;
            return this;
        }

        public Builder contentMetadata(Map<String, Object> contentMetadata) {
            result.contentMetadata = contentMetadata;
            return this;
        }

        public Builder riskScore(double riskScore) {
            result.riskScore = riskScore;
            return this;
        }

        public Builder riskLevel(String riskLevel) {
            result.riskLevel = riskLevel;
            return this;
        }

        public Builder recommendations(Map<String, String> recommendations) {
            result.recommendations = recommendations;
            return this;
        }

        public Builder analysisTimestamp(long analysisTimestamp) {
            result.analysisTimestamp = analysisTimestamp;
            return this;
        }

        public Builder contentSummary(String contentSummary) {
            result.contentSummary = contentSummary;
            return this;
        }

        public ContentAnalysisResult build() {
            return result;
        }
    }
}