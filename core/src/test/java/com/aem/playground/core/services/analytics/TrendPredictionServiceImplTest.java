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

class TrendPredictionServiceImplTest {

    @Mock
    private AIService aiService;

    @InjectMocks
    private TrendPredictionServiceImpl trendPredictionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
                .thenReturn(AIService.AIGenerationResult.success("Trend analysis", null));
    }

    @Test
    void testPredictTrends() {
        List<ContentMetrics> historicalMetrics = createTestMetrics(3);
        List<AnalyticsReport.TrendPrediction> predictions = 
            trendPredictionService.predictTrends(historicalMetrics, 7);
        
        assertNotNull(predictions);
        assertFalse(predictions.isEmpty());
    }

    @Test
    void testPredictTrendForContent() {
        AnalyticsReport.TrendPrediction prediction = 
            trendPredictionService.predictTrendForContent("/content/page1", 7);
        
        assertNotNull(prediction);
        assertEquals("/content/page1", prediction.getContentPath());
        assertNotNull(prediction.getPredictedTrend());
        assertTrue(prediction.getConfidence() >= 0 && prediction.getConfidence() <= 1);
    }

    @Test
    void testIdentifyRisingContent() {
        List<ContentMetrics> currentMetrics = createTestMetrics(3);
        List<ContentMetrics> previousMetrics = createLowerTestMetrics(3);

        List<AnalyticsReport.TrendPrediction> rising = 
            trendPredictionService.identifyRisingContent(currentMetrics, previousMetrics);
        
        assertNotNull(rising);
    }

    @Test
    void testIdentifyDecliningContent() {
        List<ContentMetrics> currentMetrics = createLowerTestMetrics(3);
        List<ContentMetrics> previousMetrics = createTestMetrics(3);

        List<AnalyticsReport.TrendPrediction> declining = 
            trendPredictionService.identifyDecliningContent(currentMetrics, previousMetrics);
        
        assertNotNull(declining);
    }

    @Test
    void testGenerateTrendAnalysis() {
        List<ContentMetrics> metrics = createTestMetrics(5);
        String analysis = trendPredictionService.generateTrendAnalysis(metrics);
        
        assertNotNull(analysis);
        assertFalse(analysis.isEmpty());
    }

    @Test
    void testEmptyMetrics() {
        List<AnalyticsReport.TrendPrediction> predictions = 
            trendPredictionService.predictTrends(new ArrayList<>(), 7);
        
        assertNotNull(predictions);
    }

    @Test
    void testPredictionTimeframes() {
        AnalyticsReport.TrendPrediction weeklyPrediction = 
            trendPredictionService.predictTrendForContent("/content/page1", 7);
        
        assertEquals("next week", weeklyPrediction.getTimeframe());

        AnalyticsReport.TrendPrediction monthlyPrediction = 
            trendPredictionService.predictTrendForContent("/content/page1", 30);
        
        assertEquals("next month", monthlyPrediction.getTimeframe());
    }

    @Test
    void testConfidenceRange() {
        List<ContentMetrics> metrics = createTestMetrics(5);
        List<AnalyticsReport.TrendPrediction> predictions = 
            trendPredictionService.predictTrends(metrics, 7);
        
        for (AnalyticsReport.TrendPrediction p : predictions) {
            assertTrue(p.getConfidence() >= 0 && p.getConfidence() <= 1);
        }
    }

    private List<ContentMetrics> createTestMetrics(int count) {
        List<ContentMetrics> metrics = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            metrics.add(ContentMetrics.builder()
                    .contentPath("/content/page" + i)
                    .contentTitle("Page " + i)
                    .pageViews(1000 + i * 100)
                    .uniqueVisitors(500 + i * 50)
                    .avgTimeOnPage(60 + i * 10)
                    .bounceRate(0.3 + i * 0.05)
                    .conversionCount(10 + i)
                    .build());
        }
        return metrics;
    }

    private List<ContentMetrics> createLowerTestMetrics(int count) {
        List<ContentMetrics> metrics = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            metrics.add(ContentMetrics.builder()
                    .contentPath("/content/page" + i)
                    .contentTitle("Page " + i)
                    .pageViews(500 + i * 50)
                    .uniqueVisitors(250 + i * 25)
                    .avgTimeOnPage(30 + i * 5)
                    .bounceRate(0.5 + i * 0.05)
                    .conversionCount(5 + i)
                    .build());
        }
        return metrics;
    }
}