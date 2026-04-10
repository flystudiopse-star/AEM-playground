package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RecommendationDashboard {

    private int totalRecommendationsGenerated;
    private int totalUsersServed;
    private double averageClickThroughRate;
    private double averageConversionRate;
    private List<TopPerformingContent> topPerformingContent;
    private List<UserSegmentBreakdown> userSegmentBreakdowns;
    private LocalDateTime generatedAt;
    private String timeRange;
    private Map<String, Object> personalizationMetrics;
    private int activePersonalizationCampaigns;

    public int getTotalRecommendationsGenerated() {
        return totalRecommendationsGenerated;
    }

    public void setTotalRecommendationsGenerated(int totalRecommendationsGenerated) {
        this.totalRecommendationsGenerated = totalRecommendationsGenerated;
    }

    public int getTotalUsersServed() {
        return totalUsersServed;
    }

    public void setTotalUsersServed(int totalUsersServed) {
        this.totalUsersServed = totalUsersServed;
    }

    public double getAverageClickThroughRate() {
        return averageClickThroughRate;
    }

    public void setAverageClickThroughRate(double averageClickThroughRate) {
        this.averageClickThroughRate = averageClickThroughRate;
    }

    public double getAverageConversionRate() {
        return averageConversionRate;
    }

    public void setAverageConversionRate(double averageConversionRate) {
        this.averageConversionRate = averageConversionRate;
    }

    public List<TopPerformingContent> getTopPerformingContent() {
        return topPerformingContent;
    }

    public void setTopPerformingContent(List<TopPerformingContent> topPerformingContent) {
        this.topPerformingContent = topPerformingContent;
    }

    public List<UserSegmentBreakdown> getUserSegmentBreakdowns() {
        return userSegmentBreakdowns;
    }

    public void setUserSegmentBreakdowns(List<UserSegmentBreakdown> userSegmentBreakdowns) {
        this.userSegmentBreakdowns = userSegmentBreakdowns;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public Map<String, Object> getPersonalizationMetrics() {
        return personalizationMetrics;
    }

    public void setPersonalizationMetrics(Map<String, Object> personalizationMetrics) {
        this.personalizationMetrics = personalizationMetrics;
    }

    public int getActivePersonalizationCampaigns() {
        return activePersonalizationCampaigns;
    }

    public void setActivePersonalizationCampaigns(int activePersonalizationCampaigns) {
        this.activePersonalizationCampaigns = activePersonalizationCampaigns;
    }

    public static class TopPerformingContent {
        private String contentPath;
        private String contentTitle;
        private int recommendationCount;
        private double clickThroughRate;

        public String getContentPath() {
            return contentPath;
        }

        public void setContentPath(String contentPath) {
            this.contentPath = contentPath;
        }

        public String getContentTitle() {
            return contentTitle;
        }

        public void setContentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
        }

        public int getRecommendationCount() {
            return recommendationCount;
        }

        public void setRecommendationCount(int recommendationCount) {
            this.recommendationCount = recommendationCount;
        }

        public double getClickThroughRate() {
            return clickThroughRate;
        }

        public void setClickThroughRate(double clickThroughRate) {
            this.clickThroughRate = clickThroughRate;
        }
    }

    public static class UserSegmentBreakdown {
        private String segmentName;
        private int usersCount;
        private double avgEngagementScore;
        private int recommendationsServed;

        public String getSegmentName() {
            return segmentName;
        }

        public void setSegmentName(String segmentName) {
            this.segmentName = segmentName;
        }

        public int getUsersCount() {
            return usersCount;
        }

        public void setUsersCount(int usersCount) {
            this.usersCount = usersCount;
        }

        public double getAvgEngagementScore() {
            return avgEngagementScore;
        }

        public void setAvgEngagementScore(double avgEngagementScore) {
            this.avgEngagementScore = avgEngagementScore;
        }

        public int getRecommendationsServed() {
            return recommendationsServed;
        }

        public void setRecommendationsServed(int recommendationsServed) {
            this.recommendationsServed = recommendationsServed;
        }
    }
}