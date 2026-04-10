package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalyticsDashboard {

    private String dashboardId;
    private String name;
    private List<ForecastSummary> forecasts;
    private Map<String, Double> topPerformingContent;
    private Map<String, Double> emergingTrends;
    private List<Alert> alerts;
    private LocalDateTime lastRefreshed;
    private Map<String, Object> overallMetrics;

    public String getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardId = dashboardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ForecastSummary> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<ForecastSummary> forecasts) {
        this.forecasts = forecasts;
    }

    public Map<String, Double> getTopPerformingContent() {
        return topPerformingContent;
    }

    public void setTopPerformingContent(Map<String, Double> topPerformingContent) {
        this.topPerformingContent = topPerformingContent;
    }

    public Map<String, Double> getEmergingTrends() {
        return emergingTrends;
    }

    public void setEmergingTrends(Map<String, Double> emergingTrends) {
        this.emergingTrends = emergingTrends;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

    public LocalDateTime getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(LocalDateTime lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    public Map<String, Object> getOverallMetrics() {
        return overallMetrics;
    }

    public void setOverallMetrics(Map<String, Object> overallMetrics) {
        this.overallMetrics = overallMetrics;
    }

    public static AnalyticsDashboard create(String name) {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();
        dashboard.setDashboardId("dashboard-" + System.currentTimeMillis());
        dashboard.setName(name);
        dashboard.setLastRefreshed(LocalDateTime.now());
        return dashboard;
    }

    public static class ForecastSummary {
        private String contentPath;
        private String contentType;
        private double predictedViews;
        private double confidenceScore;
        private String trendDirection;

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

        public double getPredictedViews() {
            return predictedViews;
        }

        public void setPredictedViews(double predictedViews) {
            this.predictedViews = predictedViews;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public String getTrendDirection() {
            return trendDirection;
        }

        public void setTrendDirection(String trendDirection) {
            this.trendDirection = trendDirection;
        }
    }

    public static class Alert {
        private String alertId;
        private String type;
        private String message;
        private String severity;
        private LocalDateTime createdAt;

        public String getAlertId() {
            return alertId;
        }

        public void setAlertId(String alertId) {
            this.alertId = alertId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}