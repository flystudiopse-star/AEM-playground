package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class SchedulerAnalyticsDashboard {

    private String dashboardId;
    private long generatedAt;
    private double averageEngagement;
    private double optimalPostingFrequency;
    private List<String> bestTimeSlots;
    private Map<String, Double> engagementByDay;
    private Map<Integer, Double> engagementByHour;
    private Map<String, Integer> scheduledByContentType;
    private int totalScheduled;
    private int totalPublished;
    private double successRate;
    private Map<String, Object> metadata;

    public String getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardId = dashboardId;
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public double getAverageEngagement() {
        return averageEngagement;
    }

    public void setAverageEngagement(double averageEngagement) {
        this.averageEngagement = averageEngagement;
    }

    public double getOptimalPostingFrequency() {
        return optimalPostingFrequency;
    }

    public void setOptimalPostingFrequency(double optimalPostingFrequency) {
        this.optimalPostingFrequency = optimalPostingFrequency;
    }

    public List<String> getBestTimeSlots() {
        return bestTimeSlots;
    }

    public void setBestTimeSlots(List<String> bestTimeSlots) {
        this.bestTimeSlots = bestTimeSlots;
    }

    public Map<String, Double> getEngagementByDay() {
        return engagementByDay;
    }

    public void setEngagementByDay(Map<String, Double> engagementByDay) {
        this.engagementByDay = engagementByDay;
    }

    public Map<Integer, Double> getEngagementByHour() {
        return engagementByHour;
    }

    public void setEngagementByHour(Map<Integer, Double> engagementByHour) {
        this.engagementByHour = engagementByHour;
    }

    public Map<String, Integer> getScheduledByContentType() {
        return scheduledByContentType;
    }

    public void setScheduledByContentType(Map<String, Integer> scheduledByContentType) {
        this.scheduledByContentType = scheduledByContentType;
    }

    public int getTotalScheduled() {
        return totalScheduled;
    }

    public void setTotalScheduled(int totalScheduled) {
        this.totalScheduled = totalScheduled;
    }

    public int getTotalPublished() {
        return totalPublished;
    }

    public void setTotalPublished(int totalPublished) {
        this.totalPublished = totalPublished;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SchedulerAnalyticsDashboard dashboard = new SchedulerAnalyticsDashboard();

        public Builder dashboardId(String dashboardId) {
            dashboard.dashboardId = dashboardId;
            return this;
        }

        public Builder generatedAt(long generatedAt) {
            dashboard.generatedAt = generatedAt;
            return this;
        }

        public Builder averageEngagement(double averageEngagement) {
            dashboard.averageEngagement = averageEngagement;
            return this;
        }

        public Builder optimalPostingFrequency(double optimalPostingFrequency) {
            dashboard.optimalPostingFrequency = optimalPostingFrequency;
            return this;
        }

        public Builder bestTimeSlots(List<String> bestTimeSlots) {
            dashboard.bestTimeSlots = bestTimeSlots;
            return this;
        }

        public Builder engagementByDay(Map<String, Double> engagementByDay) {
            dashboard.engagementByDay = engagementByDay;
            return this;
        }

        public Builder engagementByHour(Map<Integer, Double> engagementByHour) {
            dashboard.engagementByHour = engagementByHour;
            return this;
        }

        public Builder scheduledByContentType(Map<String, Integer> scheduledByContentType) {
            dashboard.scheduledByContentType = scheduledByContentType;
            return this;
        }

        public Builder totalScheduled(int totalScheduled) {
            dashboard.totalScheduled = totalScheduled;
            return this;
        }

        public Builder totalPublished(int totalPublished) {
            dashboard.totalPublished = totalPublished;
            return this;
        }

        public Builder successRate(double successRate) {
            dashboard.successRate = successRate;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            dashboard.metadata = metadata;
            return this;
        }

        public SchedulerAnalyticsDashboard build() {
            return dashboard;
        }
    }
}