package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowAction {

    private String actionId;
    private String actionType;
    private String actionName;
    private String description;
    private double confidenceScore;
    private Map<String, Object> metadata;
    private List<String> requiredApprovers;
    private String workflowModelId;
    private Map<String, String> workflowPayload;
    private int priority;
    private boolean autoExecute;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public List<String> getRequiredApprovers() {
        return requiredApprovers;
    }

    public void setRequiredApprovers(List<String> requiredApprovers) {
        this.requiredApprovers = requiredApprovers;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }

    public void setWorkflowModelId(String workflowModelId) {
        this.workflowModelId = workflowModelId;
    }

    public Map<String, String> getWorkflowPayload() {
        return workflowPayload;
    }

    public void setWorkflowPayload(Map<String, String> workflowPayload) {
        this.workflowPayload = workflowPayload;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isAutoExecute() {
        return autoExecute;
    }

    public void setAutoExecute(boolean autoExecute) {
        this.autoExecute = autoExecute;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private WorkflowAction action = new WorkflowAction();

        public Builder actionId(String actionId) {
            action.actionId = actionId;
            return this;
        }

        public Builder actionType(String actionType) {
            action.actionType = actionType;
            return this;
        }

        public Builder actionName(String actionName) {
            action.actionName = actionName;
            return this;
        }

        public Builder description(String description) {
            action.description = description;
            return this;
        }

        public Builder confidenceScore(double confidenceScore) {
            action.confidenceScore = confidenceScore;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            action.metadata = metadata;
            return this;
        }

        public Builder requiredApprovers(List<String> requiredApprovers) {
            action.requiredApprovers = requiredApprovers;
            return this;
        }

        public Builder workflowModelId(String workflowModelId) {
            action.workflowModelId = workflowModelId;
            return this;
        }

        public Builder workflowPayload(Map<String, String> workflowPayload) {
            action.workflowPayload = workflowPayload;
            return this;
        }

        public Builder priority(int priority) {
            action.priority = priority;
            return this;
        }

        public Builder autoExecute(boolean autoExecute) {
            action.autoExecute = autoExecute;
            return this;
        }

        public WorkflowAction build() {
            return action;
        }
    }
}