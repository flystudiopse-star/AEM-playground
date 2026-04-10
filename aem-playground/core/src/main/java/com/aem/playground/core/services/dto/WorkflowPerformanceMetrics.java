package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowPerformanceMetrics {

    private String workflowId;
    private String workflowName;
    private long startTime;
    private long completionTime;
    private long duration;
    private String status;
    private int stepsCompleted;
    private int totalSteps;
    private Map<String, Long> stepDurations;
    private List<String> participants;
    private Map<String, Object> performanceData;
    private double completionRate;
    private long averageStepTime;
    private int approvalCount;
    private int rejectionCount;
    private String bottleneckStep;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStepsCompleted() {
        return stepsCompleted;
    }

    public void setStepsCompleted(int stepsCompleted) {
        this.stepsCompleted = stepsCompleted;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Map<String, Long> getStepDurations() {
        return stepDurations;
    }

    public void setStepDurations(Map<String, Long> stepDurations) {
        this.stepDurations = stepDurations;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Map<String, Object> getPerformanceData() {
        return performanceData;
    }

    public void setPerformanceData(Map<String, Object> performanceData) {
        this.performanceData = performanceData;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public long getAverageStepTime() {
        return averageStepTime;
    }

    public void setAverageStepTime(long averageStepTime) {
        this.averageStepTime = averageStepTime;
    }

    public int getApprovalCount() {
        return approvalCount;
    }

    public void setApprovalCount(int approvalCount) {
        this.approvalCount = approvalCount;
    }

    public int getRejectionCount() {
        return rejectionCount;
    }

    public void setRejectionCount(int rejectionCount) {
        this.rejectionCount = rejectionCount;
    }

    public String getBottleneckStep() {
        return bottleneckStep;
    }

    public void setBottleneckStep(String bottleneckStep) {
        this.bottleneckStep = bottleneckStep;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private WorkflowPerformanceMetrics metrics = new WorkflowPerformanceMetrics();

        public Builder workflowId(String workflowId) {
            metrics.workflowId = workflowId;
            return this;
        }

        public Builder workflowName(String workflowName) {
            metrics.workflowName = workflowName;
            return this;
        }

        public Builder startTime(long startTime) {
            metrics.startTime = startTime;
            return this;
        }

        public Builder completionTime(long completionTime) {
            metrics.completionTime = completionTime;
            return this;
        }

        public Builder duration(long duration) {
            metrics.duration = duration;
            return this;
        }

        public Builder status(String status) {
            metrics.status = status;
            return this;
        }

        public Builder stepsCompleted(int stepsCompleted) {
            metrics.stepsCompleted = stepsCompleted;
            return this;
        }

        public Builder totalSteps(int totalSteps) {
            metrics.totalSteps = totalSteps;
            return this;
        }

        public Builder stepDurations(Map<String, Long> stepDurations) {
            metrics.stepDurations = stepDurations;
            return this;
        }

        public Builder participants(List<String> participants) {
            metrics.participants = participants;
            return this;
        }

        public Builder performanceData(Map<String, Object> performanceData) {
            metrics.performanceData = performanceData;
            return this;
        }

        public Builder completionRate(double completionRate) {
            metrics.completionRate = completionRate;
            return this;
        }

        public Builder averageStepTime(long averageStepTime) {
            metrics.averageStepTime = averageStepTime;
            return this;
        }

        public Builder approvalCount(int approvalCount) {
            metrics.approvalCount = approvalCount;
            return this;
        }

        public Builder rejectionCount(int rejectionCount) {
            metrics.rejectionCount = rejectionCount;
            return this;
        }

        public Builder bottleneckStep(String bottleneckStep) {
            metrics.bottleneckStep = bottleneckStep;
            return this;
        }

        public WorkflowPerformanceMetrics build() {
            return metrics;
        }
    }
}