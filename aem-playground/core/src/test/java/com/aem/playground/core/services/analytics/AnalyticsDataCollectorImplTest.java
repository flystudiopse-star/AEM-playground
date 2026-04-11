package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.OpenAIService;
import org.apache.sling.models.factory.ModelFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalyticsDataCollectorImplTest {

    @Mock
    private OpenAIService openAIService;

    @Mock
    private ModelFactory modelFactory;

    @InjectMocks
    private AnalyticsDataCollectorImpl dataCollector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCollectContentMetrics() {
        List<ContentMetrics> metrics = dataCollector.collectContentMetrics("/content", 5);
        
        assertNotNull(metrics);
        assertEquals(5, metrics.size());
    }

    @Test
    void testGetContentMetrics() {
        ContentMetrics metrics = dataCollector.getContentMetrics("/content/page1");
        
        assertNotNull(metrics);
        assertEquals("/content/page1", metrics.getContentPath());
    }

    @Test
    void testGetMetricsByDateRange() {
        long now = System.currentTimeMillis();
        long weekAgo = now - (7L * 24 * 60 * 60 * 1000);
        
        List<ContentMetrics> metrics = dataCollector.getMetricsByDateRange("/content", weekAgo, now);
        
        assertNotNull(metrics);
    }

    @Test
    void testGetTopPerformingContent() {
        List<ContentMetrics> topContent = dataCollector.getTopPerformingContent("/content", 3);
        
        assertNotNull(topContent);
        assertTrue(topContent.size() <= 3);
    }

    @Test
    void testGetUnderperformingContent() {
        List<ContentMetrics> underperforming = dataCollector.getUnderperformingContent("/content", 3);
        
        assertNotNull(underperforming);
        assertTrue(underperforming.size() <= 3);
    }

    @Test
    void testContentMetricsHaveRequiredFields() {
        List<ContentMetrics> metrics = dataCollector.collectContentMetrics("/content", 1);
        ContentMetrics m = metrics.get(0);
        
        assertNotNull(m.getContentPath());
        assertNotNull(m.getContentTitle());
        assertTrue(m.getPageViews() >= 0);
        assertTrue(m.getUniqueVisitors() >= 0);
    }
}