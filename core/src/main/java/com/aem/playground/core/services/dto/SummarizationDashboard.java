package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SummarizationDashboard {

    private String dashboardId;
    private String name;
    private int totalSummariesGenerated;
    private int totalExecutiveSummaries;
    private int totalKeyTakeawaysExtracted;
    private int totalHighlightsExtracted;
    private int totalContentFragmentsProcessed;
    private Map<String, Integer> summariesByContentType;
    private Map<String, Integer> highlightsByType;
    private List<SummaryStatistics> recentActivity;
    private LocalDateTime lastRefreshed;

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

    public int getTotalSummariesGenerated() {
        return totalSummariesGenerated;
    }

    public void setTotalSummariesGenerated(int totalSummariesGenerated) {
        this.totalSummariesGenerated = totalSummariesGenerated;
    }

    public int getTotalExecutiveSummaries() {
        return totalExecutiveSummaries;
    }

    public void setTotalExecutiveSummaries(int totalExecutiveSummaries) {
        this.totalExecutiveSummaries = totalExecutiveSummaries;
    }

    public int getTotalKeyTakeawaysExtracted() {
        return totalKeyTakeawaysExtracted;
    }

    public void setTotalKeyTakeawaysExtracted(int totalKeyTakeawaysExtracted) {
        this.totalKeyTakeawaysExtracted = totalKeyTakeawaysExtracted;
    }

    public int getTotalHighlightsExtracted() {
        return totalHighlightsExtracted;
    }

    public void setTotalHighlightsExtracted(int totalHighlightsExtracted) {
        this.totalHighlightsExtracted = totalHighlightsExtracted;
    }

    public int getTotalContentFragmentsProcessed() {
        return totalContentFragmentsProcessed;
    }

    public void setTotalContentFragmentsProcessed(int totalContentFragmentsProcessed) {
        this.totalContentFragmentsProcessed = totalContentFragmentsProcessed;
    }

    public Map<String, Integer> getSummariesByContentType() {
        return summariesByContentType;
    }

    public void setSummariesByContentType(Map<String, Integer> summariesByContentType) {
        this.summariesByContentType = summariesByContentType;
    }

    public Map<String, Integer> getHighlightsByType() {
        return highlightsByType;
    }

    public void setHighlightsByType(Map<String, Integer> highlightsByType) {
        this.highlightsByType = highlightsByType;
    }

    public List<SummaryStatistics> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<SummaryStatistics> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public LocalDateTime getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(LocalDateTime lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    public static SummarizationDashboard create(String name) {
        SummarizationDashboard dashboard = new SummarizationDashboard();
        dashboard.setDashboardId("dashboard-" + System.currentTimeMillis());
        dashboard.setName(name);
        dashboard.setLastRefreshed(LocalDateTime.now());
        return dashboard;
    }

    public static class SummaryStatistics {
        private String contentPath;
        private String summaryType;
        private LocalDateTime generatedAt;
        private double confidenceScore;

        public String getContentPath() {
            return contentPath;
        }

        public void setContentPath(String contentPath) {
            this.contentPath = contentPath;
        }

        public String getSummaryType() {
            return summaryType;
        }

        public void setSummaryType(String summaryType) {
            this.summaryType = summaryType;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }
    }
}