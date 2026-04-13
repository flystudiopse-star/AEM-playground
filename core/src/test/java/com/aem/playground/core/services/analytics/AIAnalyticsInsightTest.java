package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIAnalyticsInsightTest {

    @Test
    void testBuilderCreatesInstance() {
        AIAnalyticsInsight insight = AIAnalyticsInsight.builder()
                .insightId("insight-1")
                .insightType(AIAnalyticsInsight.TYPE_PERFORMANCE)
                .title("High Performer")
                .description("This page has excellent performance")
                .recommendation("Promote this content")
                .confidence(0.9)
                .build();

        assertNotNull(insight);
        assertEquals("insight-1", insight.getInsightId());
        assertEquals(AIAnalyticsInsight.TYPE_PERFORMANCE, insight.getInsightType());
        assertEquals("High Performer", insight.getTitle());
        assertEquals("This page has excellent performance", insight.getDescription());
        assertEquals("Promote this content", insight.getRecommendation());
        assertEquals(0.9, insight.getConfidence(), 0.01);
    }

    @Test
    void testInsightWithAffectedPages() {
        List<String> pages = Arrays.asList("/content/page1", "/content/page2", "/content/page3");
        
        AIAnalyticsInsight insight = AIAnalyticsInsight.builder()
                .insightId("insight-1")
                .insightType(AIAnalyticsInsight.TYPE_TREND)
                .title("Trending Content")
                .description("Content is trending upward")
                .affectedPages(pages)
                .confidence(0.85)
                .build();

        assertNotNull(insight.getAffectedPages());
        assertEquals(3, insight.getAffectedPages().size());
        assertTrue(insight.getAffectedPages().contains("/content/page1"));
    }

    @Test
    void testInsightCategories() {
        AIAnalyticsInsight trending = AIAnalyticsInsight.builder()
                .insightId("1")
                .category(AIAnalyticsInsight.CATEGORY_TRENDING)
                .build();
        assertEquals(AIAnalyticsInsight.CATEGORY_TRENDING, trending.getCategory());

        AIAnalyticsInsight improvement = AIAnalyticsInsight.builder()
                .insightId("2")
                .category(AIAnalyticsInsight.CATEGORY_IMPROVEMENT)
                .build();
        assertEquals(AIAnalyticsInsight.CATEGORY_IMPROVEMENT, improvement.getCategory());

        AIAnalyticsInsight opportunity = AIAnalyticsInsight.builder()
                .insightId("3")
                .category(AIAnalyticsInsight.CATEGORY_OPPORTUNITY)
                .build();
        assertEquals(AIAnalyticsInsight.CATEGORY_OPPORTUNITY, opportunity.getCategory());

        AIAnalyticsInsight risk = AIAnalyticsInsight.builder()
                .insightId("4")
                .category(AIAnalyticsInsight.CATEGORY_RISK)
                .build();
        assertEquals(AIAnalyticsInsight.CATEGORY_RISK, risk.getCategory());
    }

    @Test
    void testInsightTypes() {
        assertEquals("performance", AIAnalyticsInsight.TYPE_PERFORMANCE);
        assertEquals("trend", AIAnalyticsInsight.TYPE_TREND);
        assertEquals("content_quality", AIAnalyticsInsight.TYPE_CONTENT_QUALITY);
        assertEquals("seo", AIAnalyticsInsight.TYPE_SEO);
        assertEquals("engagement", AIAnalyticsInsight.TYPE_ENGAGEMENT);
        assertEquals("recommendation", AIAnalyticsInsight.TYPE_RECOMMENDATION);
    }

    @Test
    void testPriority() {
        AIAnalyticsInsight highPriority = AIAnalyticsInsight.builder()
                .insightId("1")
                .priority(1)
                .build();
        assertEquals(1, highPriority.getPriority());

        AIAnalyticsInsight mediumPriority = AIAnalyticsInsight.builder()
                .insightId("2")
                .priority(2)
                .build();
        assertEquals(2, mediumPriority.getPriority());
    }

    @Test
    void testDefaultAffectedPages() {
        AIAnalyticsInsight insight = AIAnalyticsInsight.builder()
                .insightId("1")
                .build();

        assertNotNull(insight.getAffectedPages());
        assertTrue(insight.getAffectedPages().isEmpty());
    }

    @Test
    void testConfidenceRange() {
        AIAnalyticsInsight insight = AIAnalyticsInsight.builder()
                .insightId("1")
                .confidence(0.75)
                .build();

        assertTrue(insight.getConfidence() >= 0 && insight.getConfidence() <= 1);
    }
}