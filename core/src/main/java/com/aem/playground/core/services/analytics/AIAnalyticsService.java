package com.aem.playground.core.services.analytics;

import java.util.List;

public interface AIAnalyticsService {

    List<AIAnalyticsInsight> generateInsights(List<ContentMetrics> metrics);

    List<AIAnalyticsInsight> generateInsightsByType(List<ContentMetrics> metrics, String insightType);

    AIAnalyticsInsight generateRecommendation(ContentMetrics metrics);

    String generateSummaryReport(List<ContentMetrics> metrics);

    List<AIAnalyticsInsight> analyzeTrends(List<ContentMetrics> historicalMetrics, List<ContentMetrics> currentMetrics);
}