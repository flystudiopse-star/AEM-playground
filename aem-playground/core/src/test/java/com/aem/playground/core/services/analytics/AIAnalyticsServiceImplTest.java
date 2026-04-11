package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIGenerationOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AIAnalyticsServiceImplTest {

    @Mock
    private AIService aiService;

    @InjectMocks
    private AIAnalyticsServiceImpl aiAnalyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
                .thenReturn(AIService.AIGenerationResult.success("AI insight text", null));
    }

    @Test
    void testGenerateInsights() {
        List<ContentMetrics> metrics = createTestMetrics(5);
        List<AIAnalyticsInsight> insights = aiAnalyticsService.generateInsights(metrics);
        
        assertNotNull(insights);
        assertFalse(insights.isEmpty());
    }

    @Test
    void testGenerateInsightsByType() {
        List<ContentMetrics> metrics = createTestMetrics(5);
        List<AIAnalyticsInsight> performanceInsights = 
            aiAnalyticsService.generateInsightsByType(metrics, AIAnalyticsInsight.TYPE_PERFORMANCE);
        
        assertNotNull(performanceInsights);
    }

    @Test
    void testGenerateRecommendation() {
        ContentMetrics metrics = ContentMetrics.builder()
                .contentPath("/content/page1")
                .contentTitle("Test Page")
                .pageViews(1000)
                .uniqueVisitors(500)
                .avgTimeOnPage(120)
                .bounceRate(0.4)
                .conversionCount(10)
                .build();

        AIAnalyticsInsight recommendation = aiAnalyticsService.generateRecommendation(metrics);
        
        assertNotNull(recommendation);
        assertEquals(AIAnalyticsInsight.TYPE_RECOMMENDATION, recommendation.getInsightType());
        assertEquals("Test Page", recommendation.getTitle());
    }

    @Test
    void testGenerateSummaryReport() {
        List<ContentMetrics> metrics = createTestMetrics(3);
        String summary = aiAnalyticsService.generateSummaryReport(metrics);
        
        assertNotNull(summary);
        assertFalse(summary.isEmpty());
    }

    @Test
    void testAnalyzeTrends() {
        List<ContentMetrics> historicalMetrics = createTestMetrics(3);
        List<ContentMetrics> currentMetrics = createTestMetrics(3);

        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
                .thenReturn(AIService.AIGenerationResult.success("Trend analysis", null));

        List<AIAnalyticsInsight> trends = aiAnalyticsService.analyzeTrends(historicalMetrics, currentMetrics);
        
        assertNotNull(trends);
    }

    @Test
    void testEmptyMetrics() {
        List<AIAnalyticsInsight> insights = aiAnalyticsService.generateInsights(new ArrayList<>());
        assertNotNull(insights);
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
}