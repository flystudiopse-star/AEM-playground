package com.aem.playground.core.services;

import java.util.List;
import java.util.Map;

public class ModerationWorkflowTrigger {

    private String contentPath;
    private String workflowModelId;
    private String triggerReason;
    private List<ModerationViolation> triggeredViolations;
    private String assignedReviewer;
    private int priority;
    private long timestamp;
    private Map<String, Object> metadata;

    public ModerationWorkflowTrigger() {
        this.timestamp = System.currentTimeMillis();
    }

    public static ModerationWorkflowTriggerBuilder builder() {
        return new ModerationWorkflowTriggerBuilder();
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }

    public void setWorkflowModelId(String workflowModelId) {
        this.workflowModelId = workflowModelId;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    public List<ModerationViolation> getTriggeredViolations() {
        return triggeredViolations;
    }

    public void setTriggeredViolations(List<ModerationViolation> triggeredViolations) {
        this.triggeredViolations = triggeredViolations;
    }

    public String getAssignedReviewer() {
        return assignedReviewer;
    }

    public void setAssignedReviewer(String assignedReviewer) {
        this.assignedReviewer = assignedReviewer;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static class ModerationWorkflowTriggerBuilder {
        private final ModerationWorkflowTrigger trigger = new ModerationWorkflowTrigger();

        public ModerationWorkflowTriggerBuilder contentPath(String contentPath) {
            trigger.contentPath = contentPath;
            return this;
        }

        public ModerationWorkflowTriggerBuilder workflowModelId(String workflowModelId) {
            trigger.workflowModelId = workflowModelId;
            return this;
        }

        public ModerationWorkflowTriggerBuilder triggerReason(String triggerReason) {
            trigger.triggerReason = triggerReason;
            return this;
        }

        public ModerationWorkflowTriggerBuilder triggeredViolations(List<ModerationViolation> triggeredViolations) {
            trigger.triggeredViolations = triggeredViolations;
            return this;
        }

        public ModerationWorkflowTriggerBuilder assignedReviewer(String assignedReviewer) {
            trigger.assignedReviewer = assignedReviewer;
            return this;
        }

        public ModerationWorkflowTriggerBuilder priority(int priority) {
            trigger.priority = priority;
            return this;
        }

        public ModerationWorkflowTriggerBuilder metadata(Map<String, Object> metadata) {
            trigger.metadata = metadata;
            return this;
        }

        public ModerationWorkflowTrigger build() {
            return trigger;
        }
    }
}