package com.aem.playground.core.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentQAReport {

    public enum OverallStatus {
        PASS("Pass"),
        WARNING("Warning"),
        FAIL("Fail");

        private final String displayName;

        OverallStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String contentPath;
    private final String contentTitle;
    private final long timestamp;
    private final OverallStatus status;
    private final int overallScore;
    private final List<ContentQAIssue> issues;
    private final Map<String, Object> metadata;
    private final List<String> recommendations;

    private ContentQAReport(Builder builder) {
        this.contentPath = builder.contentPath;
        this.contentTitle = builder.contentTitle;
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.overallScore = builder.overallScore;
        this.issues = builder.issues != null ? new ArrayList<>(builder.issues) : new ArrayList<>();
        this.metadata = builder.metadata != null ? new HashMap<>(builder.metadata) : new HashMap<>();
        this.recommendations = builder.recommendations != null ? new ArrayList<>(builder.recommendations) : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getContentPath() {
        return contentPath;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public OverallStatus getStatus() {
        return status;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public List<ContentQAIssue> getIssues() {
        return issues;
    }

    public List<ContentQAIssue> getIssuesByType(ContentQAIssue.IssueType type) {
        return issues.stream()
                .filter(issue -> issue.getType() == type)
                .collect(Collectors.toList());
    }

    public List<ContentQAIssue> getIssuesBySeverity(ContentQAIssue.Severity severity) {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public int getTotalIssues() {
        return issues.size();
    }

    public int getCriticalIssueCount() {
        return (int) issues.stream()
                .filter(issue -> issue.getSeverity() == ContentQAIssue.Severity.CRITICAL)
                .count();
    }

    public int getHighIssueCount() {
        return (int) issues.stream()
                .filter(issue -> issue.getSeverity() == ContentQAIssue.Severity.HIGH)
                .count();
    }

    public int getMediumIssueCount() {
        return (int) issues.stream()
                .filter(issue -> issue.getSeverity() == ContentQAIssue.Severity.MEDIUM)
                .count();
    }

    public int getLowIssueCount() {
        return (int) issues.stream()
                .filter(issue -> issue.getSeverity() == ContentQAIssue.Severity.LOW)
                .count();
    }

    public static class Builder {
        private String contentPath;
        private String contentTitle;
        private long timestamp = System.currentTimeMillis();
        private OverallStatus status;
        private int overallScore;
        private List<ContentQAIssue> issues;
        private Map<String, Object> metadata;
        private List<String> recommendations;

        public Builder contentPath(String contentPath) {
            this.contentPath = contentPath;
            return this;
        }

        public Builder contentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(OverallStatus status) {
            this.status = status;
            return this;
        }

        public Builder overallScore(int overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        public Builder issues(List<ContentQAIssue> issues) {
            this.issues = issues;
            return this;
        }

        public Builder addIssue(ContentQAIssue issue) {
            if (this.issues == null) {
                this.issues = new ArrayList<>();
            }
            this.issues.add(issue);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder recommendations(List<String> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public Builder addRecommendation(String recommendation) {
            if (this.recommendations == null) {
                this.recommendations = new ArrayList<>();
            }
            this.recommendations.add(recommendation);
            return this;
        }

        public ContentQAReport build() {
            if (status == null) {
                status = calculateStatus();
            }
            if (overallScore == 0) {
                overallScore = calculateScore();
            }
            return new ContentQAReport(this);
        }

        private OverallStatus calculateStatus() {
            if (issues == null || issues.isEmpty()) {
                return OverallStatus.PASS;
            }
            boolean hasCritical = issues.stream()
                    .anyMatch(i -> i.getSeverity() == ContentQAIssue.Severity.CRITICAL);
            if (hasCritical) {
                return OverallStatus.FAIL;
            }
            boolean hasHigh = issues.stream()
                    .anyMatch(i -> i.getSeverity() == ContentQAIssue.Severity.HIGH);
            return hasHigh ? OverallStatus.WARNING : OverallStatus.PASS;
        }

        private int calculateScore() {
            if (issues == null || issues.isEmpty()) {
                return 100;
            }
            int score = 100;
            for (ContentQAIssue issue : issues) {
                switch (issue.getSeverity()) {
                    case CRITICAL:
                        score -= 20;
                        break;
                    case HIGH:
                        score -= 10;
                        break;
                    case MEDIUM:
                        score -= 5;
                        break;
                    case LOW:
                        score -= 2;
                        break;
                }
            }
            return Math.max(0, score);
        }
    }
}