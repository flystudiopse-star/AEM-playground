package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentIssue {

    private String issueId;
    private String issueType;
    private String severity;
    private String description;
    private String contentPath;
    private String suggestedFix;
    private Map<String, Object> issueMetadata;
    private List<String> affectedPages;
    private boolean autoCorrectable;
    private String correctAction;
    private long detectedAt;

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }

    public Map<String, Object> getIssueMetadata() {
        return issueMetadata;
    }

    public void setIssueMetadata(Map<String, Object> issueMetadata) {
        this.issueMetadata = issueMetadata;
    }

    public List<String> getAffectedPages() {
        return affectedPages;
    }

    public void setAffectedPages(List<String> affectedPages) {
        this.affectedPages = affectedPages;
    }

    public boolean isAutoCorrectable() {
        return autoCorrectable;
    }

    public void setAutoCorrectable(boolean autoCorrectable) {
        this.autoCorrectable = autoCorrectable;
    }

    public String getCorrectAction() {
        return correctAction;
    }

    public void setCorrectAction(String correctAction) {
        this.correctAction = correctAction;
    }

    public long getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(long detectedAt) {
        this.detectedAt = detectedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ContentIssue issue = new ContentIssue();

        public Builder issueId(String issueId) {
            issue.issueId = issueId;
            return this;
        }

        public Builder issueType(String issueType) {
            issue.issueType = issueType;
            return this;
        }

        public Builder severity(String severity) {
            issue.severity = severity;
            return this;
        }

        public Builder description(String description) {
            issue.description = description;
            return this;
        }

        public Builder contentPath(String contentPath) {
            issue.contentPath = contentPath;
            return this;
        }

        public Builder suggestedFix(String suggestedFix) {
            issue.suggestedFix = suggestedFix;
            return this;
        }

        public Builder issueMetadata(Map<String, Object> issueMetadata) {
            issue.issueMetadata = issueMetadata;
            return this;
        }

        public Builder affectedPages(List<String> affectedPages) {
            issue.affectedPages = affectedPages;
            return this;
        }

        public Builder autoCorrectable(boolean autoCorrectable) {
            issue.autoCorrectable = autoCorrectable;
            return this;
        }

        public Builder correctAction(String correctAction) {
            issue.correctAction = correctAction;
            return this;
        }

        public Builder detectedAt(long detectedAt) {
            issue.detectedAt = detectedAt;
            return this;
        }

        public ContentIssue build() {
            return issue;
        }
    }
}