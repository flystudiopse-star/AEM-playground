package com.aem.playground.core.services.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsReport {
    private final String reportId;
    private final ReportType reportType;
    private final String periodStart;
    private final String periodEnd;
    private final List<ContentMetrics> contentMetrics;
    private final List<AIAnalyticsInsight> insights;
    private final List<ContentScore> contentScores;
    private final Map<String, Object> summary;
    private final List<TrendPrediction> trendPredictions;
    private final long generatedAt;

    private AnalyticsReport(Builder builder) {
        this.reportId = builder.reportId;
        this.reportType = builder.reportType;
        this.periodStart = builder.periodStart;
        this.periodEnd = builder.periodEnd;
        this.contentMetrics = builder.contentMetrics != null ? builder.contentMetrics : new ArrayList<>();
        this.insights = builder.insights != null ? builder.insights : new ArrayList<>();
        this.contentScores = builder.contentScores != null ? builder.contentScores : new ArrayList<>();
        this.summary = builder.summary != null ? builder.summary : new HashMap<>();
        this.trendPredictions = builder.trendPredictions != null ? builder.trendPredictions : new ArrayList<>();
        this.generatedAt = builder.generatedAt;
    }

    public String getReportId() {
        return reportId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public List<ContentMetrics> getContentMetrics() {
        return contentMetrics;
    }

    public List<AIAnalyticsInsight> getInsights() {
        return insights;
    }

    public List<ContentScore> getContentScores() {
        return contentScores;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }

    public List<TrendPrediction> getTrendPredictions() {
        return trendPredictions;
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ReportType {
        WEEKLY,
        MONTHLY
    }

    public static class Builder {
        private String reportId;
        private ReportType reportType;
        private String periodStart;
        private String periodEnd;
        private List<ContentMetrics> contentMetrics;
        private List<AIAnalyticsInsight> insights;
        private List<ContentScore> contentScores;
        private Map<String, Object> summary;
        private List<TrendPrediction> trendPredictions;
        private long generatedAt = System.currentTimeMillis();

        public Builder reportId(String reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder reportType(ReportType reportType) {
            this.reportType = reportType;
            return this;
        }

        public Builder periodStart(String periodStart) {
            this.periodStart = periodStart;
            return this;
        }

        public Builder periodEnd(String periodEnd) {
            this.periodEnd = periodEnd;
            return this;
        }

        public Builder contentMetrics(List<ContentMetrics> contentMetrics) {
            this.contentMetrics = contentMetrics;
            return this;
        }

        public Builder insights(List<AIAnalyticsInsight> insights) {
            this.insights = insights;
            return this;
        }

        public Builder contentScores(List<ContentScore> contentScores) {
            this.contentScores = contentScores;
            return this;
        }

        public Builder summary(Map<String, Object> summary) {
            this.summary = summary;
            return this;
        }

        public Builder trendPredictions(List<TrendPrediction> trendPredictions) {
            this.trendPredictions = trendPredictions;
            return this;
        }

        public Builder generatedAt(long generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public AnalyticsReport build() {
            return new AnalyticsReport(this);
        }
    }

    public static class TrendPrediction {
        private final String contentPath;
        private final String predictedTrend;
        private final double confidence;
        private final String timeframe;
        private final double predictedGrowth;

        public TrendPrediction(String contentPath, String predictedTrend, double confidence, String timeframe, double predictedGrowth) {
            this.contentPath = contentPath;
            this.predictedTrend = predictedTrend;
            this.confidence = confidence;
            this.timeframe = timeframe;
            this.predictedGrowth = predictedGrowth;
        }

        public String getContentPath() {
            return contentPath;
        }

        public String getPredictedTrend() {
            return predictedTrend;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getTimeframe() {
            return timeframe;
        }

        public double getPredictedGrowth() {
            return predictedGrowth;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String contentPath;
            private String predictedTrend;
            private double confidence;
            private String timeframe;
            private double predictedGrowth;

            public Builder contentPath(String contentPath) {
                this.contentPath = contentPath;
                return this;
            }

            public Builder predictedTrend(String predictedTrend) {
                this.predictedTrend = predictedTrend;
                return this;
            }

            public Builder confidence(double confidence) {
                this.confidence = confidence;
                return this;
            }

            public Builder timeframe(String timeframe) {
                this.timeframe = timeframe;
                return this;
            }

            public Builder predictedGrowth(double predictedGrowth) {
                this.predictedGrowth = predictedGrowth;
                return this;
            }

            public TrendPrediction build() {
                return new TrendPrediction(contentPath, predictedTrend, confidence, timeframe, predictedGrowth);
            }
        }
    }
}