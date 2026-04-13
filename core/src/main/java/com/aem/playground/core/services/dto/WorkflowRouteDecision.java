package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowRouteDecision {

    private String decisionId;
    private String contentPath;
    private String targetWorkflow;
    private String routeReason;
    private double confidenceScore;
    private List<String> assignees;
    private List<String> reviewers;
    private Map<String, Object> routeMetadata;
    private String urgencyLevel;
    private long estimatedReviewTime;
    private Map<String, String> notificationSettings;
    private boolean requiresEscalation;

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getTargetWorkflow() {
        return targetWorkflow;
    }

    public void setTargetWorkflow(String targetWorkflow) {
        this.targetWorkflow = targetWorkflow;
    }

    public String getRouteReason() {
        return routeReason;
    }

    public void setRouteReason(String routeReason) {
        this.routeReason = routeReason;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public List<String> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<String> assignees) {
        this.assignees = assignees;
    }

    public List<String> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<String> reviewers) {
        this.reviewers = reviewers;
    }

    public Map<String, Object> getRouteMetadata() {
        return routeMetadata;
    }

    public void setRouteMetadata(Map<String, Object> routeMetadata) {
        this.routeMetadata = routeMetadata;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public long getEstimatedReviewTime() {
        return estimatedReviewTime;
    }

    public void setEstimatedReviewTime(long estimatedReviewTime) {
        this.estimatedReviewTime = estimatedReviewTime;
    }

    public Map<String, String> getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(Map<String, String> notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public boolean isRequiresEscalation() {
        return requiresEscalation;
    }

    public void setRequiresEscalation(boolean requiresEscalation) {
        this.requiresEscalation = requiresEscalation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private WorkflowRouteDecision decision = new WorkflowRouteDecision();

        public Builder decisionId(String decisionId) {
            decision.decisionId = decisionId;
            return this;
        }

        public Builder contentPath(String contentPath) {
            decision.contentPath = contentPath;
            return this;
        }

        public Builder targetWorkflow(String targetWorkflow) {
            decision.targetWorkflow = targetWorkflow;
            return this;
        }

        public Builder routeReason(String routeReason) {
            decision.routeReason = routeReason;
            return this;
        }

        public Builder confidenceScore(double confidenceScore) {
            decision.confidenceScore = confidenceScore;
            return this;
        }

        public Builder assignees(List<String> assignees) {
            decision.assignees = assignees;
            return this;
        }

        public Builder reviewers(List<String> reviewers) {
            decision.reviewers = reviewers;
            return this;
        }

        public Builder routeMetadata(Map<String, Object> routeMetadata) {
            decision.routeMetadata = routeMetadata;
            return this;
        }

        public Builder urgencyLevel(String urgencyLevel) {
            decision.urgencyLevel = urgencyLevel;
            return this;
        }

        public Builder estimatedReviewTime(long estimatedReviewTime) {
            decision.estimatedReviewTime = estimatedReviewTime;
            return this;
        }

        public Builder notificationSettings(Map<String, String> notificationSettings) {
            decision.notificationSettings = notificationSettings;
            return this;
        }

        public Builder requiresEscalation(boolean requiresEscalation) {
            decision.requiresEscalation = requiresEscalation;
            return this;
        }

        public WorkflowRouteDecision build() {
            return decision;
        }
    }
}