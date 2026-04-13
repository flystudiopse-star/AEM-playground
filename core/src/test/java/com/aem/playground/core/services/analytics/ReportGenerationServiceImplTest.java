package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIGenerationOptions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportGenerationServiceImplTest {

    @Mock
    private AnalyticsDataCollector dataCollector;

    @Mock
    private AIAnalyticsService aiAnalyticsService;

    @Mock
    private ContentScoringService contentScoringService;

    @Mock
    private TrendPredictionService trendPredictionService;

    @Mock
    private AIService aiService;

    @InjectMocks
    private ReportGenerationServiceImpl reportGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        List<ContentMetrics> testMetrics = createTestMetrics(5);
        when(dataCollector.getMetricsByDateRange(anyString(), anyLong(), anyLong())).thenReturn(testMetrics);
        when(dataCollector.collectContentMetrics(anyString(), anyInt())).thenReturn(testMetrics);
        
        List<AIAnalyticsInsight> testInsights = createTestInsights(3);
        when(aiAnalyticsService.generateInsights(anyList())).thenReturn(testInsights);
        
        when(contentScoringService.scoreContent(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0);
            return ContentScore.builder()
                    .contentPath(path)
                    .overallScore(75.0)
                    .qualityScore(70.0)
                    .seoScore(80.0)
                    .engagementScore(75.0)
                    .build();
        });

        List<AnalyticsReport.TrendPrediction> testPredictions = createTestPredictions(3);
        when(trendPredictionService.predictTrends(anyList(), anyInt())).thenReturn(testPredictions);

        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
                .thenReturn(AIService.AIGenerationResult.success("AI report content", null));
    }

    @Test
    void testGenerateWeeklyReport() {
        AnalyticsReport report = reportGenerationService.generateWeeklyReport("/content");
        
        assertNotNull(report);
        assertEquals(AnalyticsReport.ReportType.WEEKLY, report.getReportType());
        assertNotNull(report.getReportId());
        assertNotNull(report.getPeriodStart());
        assertNotNull(report.getPeriodEnd());
    }

    @Test
    void testGenerateMonthlyReport() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        
        assertNotNull(report);
        assertEquals(AnalyticsReport.ReportType.MONTHLY, report.getReportType());
    }

    @Test
    void testGenerateCustomReport() {
        long now = System.currentTimeMillis();
        long weekAgo = now - (7L * 24 * 60 * 60 * 1000);
        
        AnalyticsReport report = reportGenerationService.generateCustomReport(
                "/content", AnalyticsReport.ReportType.WEEKLY, weekAgo, now);
        
        assertNotNull(report);
        assertEquals(AnalyticsReport.ReportType.WEEKLY, report.getReportType());
    }

    @Test
    void testReportContainsMetrics() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        
        assertNotNull(report.getContentMetrics());
        assertFalse(report.getContentMetrics().isEmpty());
    }

    @Test
    void testReportContainsInsights() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        
        assertNotNull(report.getInsights());
        assertFalse(report.getInsights().isEmpty());
    }

    @Test
    void testReportContainsContentScores() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        
        assertNotNull(report.getContentScores());
        assertFalse(report.getContentScores().isEmpty());
    }

    @Test
    void testReportContainsTrendPredictions() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        
        assertNotNull(report.getTrendPredictions());
    }

    @Test
    void testReportContainsSummary() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        
        assertNotNull(report.getSummary());
        assertTrue(report.getSummary().containsKey("totalPageViews"));
        assertTrue(report.getSummary().containsKey("totalUniqueVisitors"));
        assertTrue(report.getSummary().containsKey("totalConversions"));
    }

    @Test
    void testExportReportAsJson() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        String json = reportGenerationService.exportReportAsJson(report);
        
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("reportId"));
    }

    @Test
    void testExportReportAsPdf() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        String pdf = reportGenerationService.exportReportAsPdf(report);
        
        assertNotNull(pdf);
        assertFalse(pdf.isEmpty());
        assertTrue(pdf.contains("ANALYTICS REPORT"));
    }

    @Test
    void testGetReport() {
        AnalyticsReport report = reportGenerationService.generateMonthlyReport("/content");
        String reportId = report.getReportId();
        
        AnalyticsReport retrieved = reportGenerationService.getReport(reportId);
        
        assertNotNull(retrieved);
        assertEquals(reportId, retrieved.getReportId());
    }

    @Test
    void testGetReportHistory() {
        reportGenerationService.generateWeeklyReport("/content");
        reportGenerationService.generateMonthlyReport("/content");
        
        List<AnalyticsReport> history = reportGenerationService.getReportHistory("/content", 5);
        
        assertNotNull(history);
    }

    private List<ContentMetrics> createTestMetrics(int count) {
        List<ContentMetrics> metrics = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            metrics.add(ContentMetrics.builder()
                    .contentPath("/content/page" + i)
                    .contentTitle("Page " + i)
                    .contentType("article")
                    .pageViews(1000 + i * 100)
                    .uniqueVisitors(500 + i * 50)
                    .avgTimeOnPage(60 + i * 10)
                    .bounceRate(0.3 + i * 0.05)
                    .conversionCount(10 + i)
                    .build());
        }
        return metrics;
    }

    private List<AIAnalyticsInsight> createTestInsights(int count) {
        List<AIAnalyticsInsight> insights = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            insights.add(AIAnalyticsInsight.builder()
                    .insightId("insight-" + i)
                    .insightType(AIAnalyticsInsight.TYPE_PERFORMANCE)
                    .title("Insight " + i)
                    .description("Test insight description")
                    .recommendation("Test recommendation")
                    .confidence(0.8)
                    .priority(1)
                    .build());
        }
        return insights;
    }

    private List<AnalyticsReport.TrendPrediction> createTestPredictions(int count) {
        List<AnalyticsReport.TrendPrediction> predictions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            predictions.add(AnalyticsReport.TrendPrediction.builder()
                    .contentPath("/content/page" + i)
                    .predictedTrend("increasing")
                    .confidence(0.85)
                    .timeframe("next week")
                    .predictedGrowth(0.15)
                    .build());
        }
        return predictions;
    }
}