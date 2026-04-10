package com.aem.playground.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModerationReport {

    private String reportId;
    private long generatedAt;
    private long startDate;
    private long endDate;
    private int totalContentReviewed;
    private int totalApproved;
    private int totalRejected;
    private int totalCensored;
    private int totalPendingReview;
    private Map<ModerationCategory, CategoryStats> categoryStats;
    private List<TrendData> trends;
    private List<TopViolation> topViolations;
    private Map<String, Object> additionalMetrics;

    public ModerationReport() {
        this.generatedAt = System.currentTimeMillis();
        this.categoryStats = new HashMap<>();
        this.trends = new ArrayList<>();
        this.topViolations = new ArrayList<>();
        this.additionalMetrics = new HashMap<>();
    }

    public static ModerationReportBuilder builder() {
        return new ModerationReportBuilder();
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getTotalContentReviewed() {
        return totalContentReviewed;
    }

    public void setTotalContentReviewed(int totalContentReviewed) {
        this.totalContentReviewed = totalContentReviewed;
    }

    public int getTotalApproved() {
        return totalApproved;
    }

    public void setTotalApproved(int totalApproved) {
        this.totalApproved = totalApproved;
    }

    public int getTotalRejected() {
        return totalRejected;
    }

    public void setTotalRejected(int totalRejected) {
        this.totalRejected = totalRejected;
    }

    public int getTotalCensored() {
        return totalCensored;
    }

    public void setTotalCensored(int totalCensored) {
        this.totalCensored = totalCensored;
    }

    public int getTotalPendingReview() {
        return totalPendingReview;
    }

    public void setTotalPendingReview(int totalPendingReview) {
        this.totalPendingReview = totalPendingReview;
    }

    public Map<ModerationCategory, CategoryStats> getCategoryStats() {
        return categoryStats;
    }

    public void setCategoryStats(Map<ModerationCategory, CategoryStats> categoryStats) {
        this.categoryStats = categoryStats;
    }

    public List<TrendData> getTrends() {
        return trends;
    }

    public void setTrends(List<TrendData> trends) {
        this.trends = trends;
    }

    public List<TopViolation> getTopViolations() {
        return topViolations;
    }

    public void setTopViolations(List<TopViolation> topViolations) {
        this.topViolations = topViolations;
    }

    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }

    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }

    public static class ModerationReportBuilder {
        private final ModerationReport report = new ModerationReport();

        public ModerationReportBuilder reportId(String reportId) {
            report.reportId = reportId;
            return this;
        }

        public ModerationReportBuilder startDate(long startDate) {
            report.startDate = startDate;
            return this;
        }

        public ModerationReportBuilder endDate(long endDate) {
            report.endDate = endDate;
            return this;
        }

        public ModerationReportBuilder totalContentReviewed(int totalContentReviewed) {
            report.totalContentReviewed = totalContentReviewed;
            return this;
        }

        public ModerationReportBuilder totalApproved(int totalApproved) {
            report.totalApproved = totalApproved;
            return this;
        }

        public ModerationReportBuilder totalRejected(int totalRejected) {
            report.totalRejected = totalRejected;
            return this;
        }

        public ModerationReportBuilder totalCensored(int totalCensored) {
            report.totalCensored = totalCensored;
            return this;
        }

        public ModerationReportBuilder totalPendingReview(int totalPendingReview) {
            report.totalPendingReview = totalPendingReview;
            return this;
        }

        public ModerationReportBuilder categoryStats(Map<ModerationCategory, CategoryStats> categoryStats) {
            report.categoryStats = categoryStats;
            return this;
        }

        public ModerationReportBuilder trends(List<TrendData> trends) {
            report.trends = trends;
            return this;
        }

        public ModerationReportBuilder topViolations(List<TopViolation> topViolations) {
            report.topViolations = topViolations;
            return this;
        }

        public ModerationReportBuilder additionalMetrics(Map<String, Object> additionalMetrics) {
            report.additionalMetrics = additionalMetrics;
            return this;
        }

        public ModerationReport build() {
            return report;
        }
    }

    public static class CategoryStats {
        private int count;
        private double percentage;
        private int autoCensoredCount;
        private int rejectedCount;
        private double averageConfidence;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }

        public int getAutoCensoredCount() {
            return autoCensoredCount;
        }

        public void setAutoCensoredCount(int autoCensoredCount) {
            this.autoCensoredCount = autoCensoredCount;
        }

        public int getRejectedCount() {
            return rejectedCount;
        }

        public void setRejectedCount(int rejectedCount) {
            this.rejectedCount = rejectedCount;
        }

        public double getAverageConfidence() {
            return averageConfidence;
        }

        public void setAverageConfidence(double averageConfidence) {
            this.averageConfidence = averageConfidence;
        }
    }

    public static class TrendData {
        private long date;
        private int reviewed;
        private int approved;
        private int rejected;
        private int censored;

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public int getReviewed() {
            return reviewed;
        }

        public void setReviewed(int reviewed) {
            this.reviewed = reviewed;
        }

        public int getApproved() {
            return approved;
        }

        public void setApproved(int approved) {
            this.approved = approved;
        }

        public int getRejected() {
            return rejected;
        }

        public void setRejected(int rejected) {
            this.rejected = rejected;
        }

        public int getCensored() {
            return censored;
        }

        public void setCensored(int censored) {
            this.censored = censored;
        }
    }

    public static class TopViolation {
        private ModerationCategory category;
        private int count;
        private double avgConfidence;

        public ModerationCategory getCategory() {
            return category;
        }

        public void setCategory(ModerationCategory category) {
            this.category = category;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getAvgConfidence() {
            return avgConfidence;
        }

        public void setAvgConfidence(double avgConfidence) {
            this.avgConfidence = avgConfidence;
        }
    }
}