package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContentMetricsTest {

    @Test
    void testBuilderCreatesInstance() {
        ContentMetrics metrics = ContentMetrics.builder()
                .contentPath("/content/page1")
                .contentTitle("Test Page")
                .contentType("article")
                .pageViews(1000)
                .uniqueVisitors(500)
                .avgTimeOnPage(120)
                .bounceRate(0.4)
                .conversionCount(10)
                .build();

        assertNotNull(metrics);
        assertEquals("/content/page1", metrics.getContentPath());
        assertEquals("Test Page", metrics.getContentTitle());
        assertEquals("article", metrics.getContentType());
        assertEquals(1000, metrics.getPageViews());
        assertEquals(500, metrics.getUniqueVisitors());
        assertEquals(120, metrics.getAvgTimeOnPage());
        assertEquals(0.4, metrics.getBounceRate(), 0.001);
        assertEquals(10, metrics.getConversionCount());
    }

    @Test
    void testBuilderWithAdditionalMetrics() {
        Map<String, Object> additional = new HashMap<>();
        additional.put("socialShares", 50);
        additional.put("comments", 10);

        ContentMetrics metrics = ContentMetrics.builder()
                .contentPath("/content/page1")
                .contentTitle("Test Page")
                .contentType("article")
                .pageViews(100)
                .uniqueVisitors(50)
                .avgTimeOnPage(60)
                .bounceRate(0.3)
                .conversionCount(5)
                .additionalMetrics(additional)
                .build();

        assertNotNull(metrics.getAdditionalMetrics());
        assertEquals(50, metrics.getAdditionalMetrics().get("socialShares"));
        assertEquals(10, metrics.getAdditionalMetrics().get("comments"));
    }

    @Test
    void testBuilderWithTimestamp() {
        long timestamp = System.currentTimeMillis();
        ContentMetrics metrics = ContentMetrics.builder()
                .contentPath("/content/page1")
                .contentTitle("Test Page")
                .contentType("page")
                .pageViews(0)
                .uniqueVisitors(0)
                .avgTimeOnPage(0)
                .bounceRate(0)
                .conversionCount(0)
                .timestamp(timestamp)
                .build();

        assertEquals(timestamp, metrics.getTimestamp());
    }

    @Test
    void testDefaultValues() {
        ContentMetrics metrics = ContentMetrics.builder()
                .contentPath("/content/page1")
                .contentTitle("Test")
                .build();

        assertEquals(0, metrics.getPageViews());
        assertEquals(0, metrics.getUniqueVisitors());
        assertTrue(metrics.getAdditionalMetrics().isEmpty());
    }
}