package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsReportTest {

    @Test
    void testBuilderCreatesInstance() {
        AnalyticsReport report = AnalyticsReport.builder()
                .reportId("report-1")
                .reportType(AnalyticsReport.ReportType.WEEKLY)
                .periodStart("2024-01-01")
                .periodEnd("2024-01-07")
                .build();

        assertNotNull(report);
        assertEquals("report-1", report.getReportId());
        assertEquals(AnalyticsReport.ReportType.WEEKLY, report.getReportType());
        assertEquals("2024-01-01", report.getPeriodStart());
        assertEquals("2024-01-07", report.getPeriodEnd());
    }

    @Test
    void testReportWithContentMetrics() {
        List<ContentMetrics> metrics = new ArrayList<>();
        metrics.add(ContentMetrics.builder()
                .contentPath("/content/page1")
                .contentTitle("Page 1")
                .pageViews(1000)
                .uniqueVisitors(500)
                .avgTimeOnPage(60)
                .bounceRate(0.4)
                .conversionCount(10)
                .build());

        AnalyticsReport report = AnalyticsReport.builder()
                .reportId("report-1")
                .reportType(AnalyticsReport.ReportType.MONTHLY)
                .periodStart("2024-01-01")
                .periodEnd("2024-01-31")
                .contentMetrics(metrics)
                .build();

        assertNotNull(report.getContentMetrics());
        assertEquals(1, report.getContentMetrics().size());
    }

    @Test
    void testReportWithInsights() {
        List<AIAnalyticsInsight> insights = new ArrayList<>();
        insights.add(AIAnalyticsInsight.builder()
                .insightId("insight-1")
                .insightType(AIAnalyticsInsight.TYPE_PERFORMANCE)
                .title("High performer")
                .description("Page performs well")
                .confidence(0.9)
                .build());

        AnalyticsReport report = AnalyticsReport.builder()
                .reportId("report-1")
                .reportType(AnalyticsReport.ReportType.WEEKLY)
                .periodStart("2024-01-01")
                .periodEnd("2024-01-07")
                .insights(insights)
                .build();

        assertNotNull(report.getInsights());
        assertEquals(1, report.getInsights().size());
    }

    @Test
    void testReportWithSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPageViews", 10000);
        summary.put("totalUniqueVisitors", 5000);
        summary.put("averageBounceRate", 0.35);

        AnalyticsReport report = AnalyticsReport.builder()
                .reportId("report-1")
                .reportType(AnalyticsReport.ReportType.WEEKLY)
                .periodStart("2024-01-01")
                .periodEnd("2024-01-07")
                .summary(summary)
                .build();

        assertNotNull(report.getSummary());
        assertEquals(10000, report.getSummary().get("totalPageViews"));
        assertEquals(5000, report.getSummary().get("totalUniqueVisitors"));
    }

    @Test
    void testReportWithTrendPredictions() {
        List<AnalyticsReport.TrendPrediction> predictions = new ArrayList<>();
        predictions.add(AnalyticsReport.TrendPrediction.builder()
                .contentPath("/content/page1")
                .predictedTrend("increasing")
                .confidence(0.85)
                .timeframe("next week")
                .predictedGrowth(0.15)
                .build());

        AnalyticsReport report = AnalyticsReport.builder()
                .reportId("report-1")
                .reportType(AnalyticsReport.ReportType.MONTHLY)
                .periodStart("2024-01-01")
                .periodEnd("2024-01-31")
                .trendPredictions(predictions)
                .build();

        assertNotNull(report.getTrendPredictions());
        assertEquals(1, report.getTrendPredictions().size());
    }

    @Test
    void testTrendPredictionBuilder() {
        AnalyticsReport.TrendPrediction prediction = AnalyticsReport.TrendPrediction.builder()
                .contentPath("/content/page1")
                .predictedTrend("increasing")
                .confidence(0.9)
                .timeframe("next week")
                .predictedGrowth(0.25)
                .build();

        assertNotNull(prediction);
        assertEquals("/content/page1", prediction.getContentPath());
        assertEquals("increasing", prediction.getPredictedTrend());
        assertEquals(0.9, prediction.getConfidence(), 0.01);
        assertEquals("next week", prediction.getTimeframe());
        assertEquals(0.25, prediction.getPredictedGrowth(), 0.01);
    }

    @Test
    void testReportTypeEnum() {
        assertEquals("WEEKLY", AnalyticsReport.ReportType.WEEKLY.name());
        assertEquals("MONTHLY", AnalyticsReport.ReportType.MONTHLY.name());
    }
}