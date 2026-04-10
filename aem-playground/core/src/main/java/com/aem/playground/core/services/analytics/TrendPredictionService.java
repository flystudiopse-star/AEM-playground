package com.aem.playground.core.services.analytics;

import java.util.List;

public interface TrendPredictionService {

    List<AnalyticsReport.TrendPrediction> predictTrends(List<ContentMetrics> historicalMetrics, int predictionPeriodDays);

    AnalyticsReport.TrendPrediction predictTrendForContent(String contentPath, int predictionPeriodDays);

    List<AnalyticsReport.TrendPrediction> identifyRisingContent(List<ContentMetrics> currentMetrics, List<ContentMetrics> previousMetrics);

    List<AnalyticsReport.TrendPrediction> identifyDecliningContent(List<ContentMetrics> currentMetrics, List<ContentMetrics> previousMetrics);

    String generateTrendAnalysis(List<ContentMetrics> metrics);
}