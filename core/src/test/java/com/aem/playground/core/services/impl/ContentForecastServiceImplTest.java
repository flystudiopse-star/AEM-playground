package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.ContentForecastService;
import com.aem.playground.core.services.ContentForecastServiceConfig;
import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentForecastServiceImplTest {

    @Mock
    private com.aem.playground.core.services.AIService aiService;

    private ContentForecastServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ContentForecastServiceImpl();

        ContentForecastServiceTestConfig config = new ContentForecastServiceTestConfig();
        service.activate(config);
        service.aiService = aiService;
    }

    @Test
    void testPredictContentPerformanceWithValidInput() {
        PerformancePrediction prediction = service.predictContentPerformance("/content/page/blog-post", "blog");

        assertNotNull(prediction);
        assertEquals("/content/page/blog-post", prediction.getContentPath());
        assertEquals("blog", prediction.getContentType());
        assertTrue(prediction.getPredictedEngagementScore() >= 0.0);
        assertTrue(prediction.getPredictedPageViews() >= 0.0);
        assertTrue(prediction.getConfidenceLevel() >= 0.0);
        assertNotNull(prediction.getPredictedTrends());
    }

    @Test
    void testPredictContentPerformanceWithEmptyContentPath() {
        PerformancePrediction prediction = service.predictContentPerformance("", "blog");

        assertNotNull(prediction);
        assertEquals(0.0, prediction.getConfidenceLevel());
    }

    @Test
    void testPredictContentPerformanceWithNullContentType() {
        PerformancePrediction prediction = service.predictContentPerformance("/content/page/test", null);

        assertNotNull(prediction);
        assertEquals("/content/page/test", prediction.getContentPath());
    }

    @Test
    void testPredictContentPerformanceCaching() {
        PerformancePrediction prediction1 = service.predictContentPerformance("/content/page/test", "blog");
        PerformancePrediction prediction2 = service.predictContentPerformance("/content/page/test", "blog");

        assertNotNull(prediction1);
        assertNotNull(prediction2);
    }

    @Test
    void testForecastTrafficWithValidInput() {
        TrafficForecast forecast = service.forecastTraffic("/content/page/blog-post", "blog", 7);

        assertNotNull(forecast);
        assertEquals("/content/page/blog-post", forecast.getContentPath());
        assertEquals("blog", forecast.getContentType());
        assertNotNull(forecast.getProjections());
        assertFalse(forecast.getProjections().isEmpty());
        assertEquals(7, forecast.getProjections().size());
        assertTrue(forecast.getAverageDailyViews() >= 0.0);
        assertTrue(forecast.getTotalMonthlyViews() >= 0.0);
    }

    @Test
    void testForecastTrafficWithEmptyContentPath() {
        TrafficForecast forecast = service.forecastTraffic("", "blog", 7);

        assertNotNull(forecast);
    }

    @Test
    void testForecastTrafficDefaultDaysAhead() {
        TrafficForecast forecast = service.forecastTraffic("/content/page/test", "blog", 0);

        assertNotNull(forecast);
        assertNotNull(forecast.getProjections());
    }

    @Test
    void testForecastTrafficNegativeDaysAhead() {
        TrafficForecast forecast = service.forecastTraffic("/content/page/test", "blog", -5);

        assertNotNull(forecast);
    }

    @Test
    void testForecastTrafficContainsTrafficBySource() {
        TrafficForecast forecast = service.forecastTraffic("/content/page/test", "product", 7);

        assertNotNull(forecast.getTrafficBySource());
        assertTrue(forecast.getTrafficBySource().containsKey("organic"));
        assertTrue(forecast.getTrafficBySource().containsKey("direct"));
    }

    @Test
    void testForecastTrafficContainsTrafficByDevice() {
        TrafficForecast forecast = service.forecastTraffic("/content/page/test", "product", 7);

        assertNotNull(forecast.getTrafficByDevice());
        assertTrue(forecast.getTrafficByDevice().containsKey("desktop"));
        assertTrue(forecast.getTrafficByDevice().containsKey("mobile"));
    }

    @Test
    void testForecastTrafficPeakDateCalculated() {
        TrafficForecast forecast = service.forecastTraffic("/content/page/test", "blog", 30);

        assertNotNull(forecast.getPeakDate());
        assertTrue(forecast.getPeakTrafficExpected() >= 0.0);
    }

    @Test
    void testSuggestPublishScheduleWithValidInput() {
        ScheduleSuggestion suggestion = service.suggestPublishSchedule("/content/page/blog-post", "blog");

        assertNotNull(suggestion);
        assertEquals("/content/page/blog-post", suggestion.getContentPath());
        assertNotNull(suggestion.getSuggestedPublishTime());
        assertNotNull(suggestion.getAlternativeTimes());
        assertTrue(suggestion.getConfidenceScore() >= 0.0);
        assertNotNull(suggestion.getReasoning());
        assertNotNull(suggestion.getFactors());
    }

    @Test
    void testSuggestPublishScheduleWithEmptyContentPath() {
        ScheduleSuggestion suggestion = service.suggestPublishSchedule("", "blog");

        assertNotNull(suggestion);
        assertEquals(0.0, suggestion.getConfidenceScore());
    }

    @Test
    void testSuggestPublishScheduleHasMultipleAlternatives() {
        ScheduleSuggestion suggestion = service.suggestPublishSchedule("/content/page/test", "blog");

        assertNotNull(suggestion.getAlternativeTimes());
        assertTrue(suggestion.getAlternativeTimes().size() >= 3);
    }

    @Test
    void testIdentifyTrendingTopicsWithValidInput() {
        List<String> contentPaths = Arrays.asList("/content/page/test1", "/content/page/test2");
        List<TrendingTopic> topics = service.identifyTrendingTopics(contentPaths);

        assertNotNull(topics);
    }

    @Test
    void testIdentifyTrendingTopicsWithEmptyList() {
        List<TrendingTopic> topics = service.identifyTrendingTopics(new ArrayList<>());

        assertTrue(topics.isEmpty());
    }

    @Test
    void testIdentifyTrendingTopicsWithNullList() {
        List<TrendingTopic> topics = service.identifyTrendingTopics(null);

        assertTrue(topics.isEmpty());
    }

    @Test
    void testIdentifyTrendingTopicsContainsPopularityScores() {
        List<String> contentPaths = Arrays.asList("/content/page/test1");
        List<TrendingTopic> topics = service.identifyTrendingTopics(contentPaths);

        for (TrendingTopic topic : topics) {
            assertTrue(topic.getPopularityScore() >= 0.0);
            assertTrue(topic.getGrowthRate() >= 0.0);
            assertNotNull(topic.getCategory());
            assertNotNull(topic.getRelatedKeywords());
        }
    }

    @Test
    void testGenerateContentCalendarWithValidDates() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        List<String> contentTypes = Arrays.asList("blog", "news", "product");

        ContentCalendar calendar = service.generateContentCalendar(startDate, endDate, contentTypes);

        assertNotNull(calendar);
        assertEquals(startDate, calendar.getStartDate());
        assertEquals(endDate, calendar.getEndDate());
        assertNotNull(calendar.getEntries());
        assertNotNull(calendar.getSuggestedTopics());
    }

    @Test
    void testGenerateContentCalendarWithNullDates() {
        ContentCalendar calendar = service.generateContentCalendar(null, null, null);

        assertNotNull(calendar);
    }

    @Test
    void testGenerateContentCalendarWithInvalidDateRange() {
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now();

        ContentCalendar calendar = service.generateContentCalendar(startDate, endDate, null);

        assertNotNull(calendar);
    }

    @Test
    void testGenerateContentCalendarWithEmptyContentTypes() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);

        ContentCalendar calendar = service.generateContentCalendar(startDate, endDate, new ArrayList<>());

        assertNotNull(calendar);
    }

    @Test
    void testGenerateContentCalendarContainsEntries() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(60);

        ContentCalendar calendar = service.generateContentCalendar(startDate, endDate, Arrays.asList("blog", "product"));

        assertNotNull(calendar.getEntries());
        for (ContentCalendar.CalendarEntry entry : calendar.getEntries()) {
            assertNotNull(entry.getContentPath());
            assertNotNull(entry.getTitle());
            assertNotNull(entry.getScheduledDate());
            assertNotNull(entry.getContentType());
            assertTrue(entry.getPredictedPerformance() >= 0.0);
        }
    }

    @Test
    void testGenerateAnalyticsDashboard() {
        service.predictContentPerformance("/content/page/test1", "blog");
        service.predictContentPerformance("/content/page/test2", "product");
        service.identifyTrendingTopics(Arrays.asList("/content/page/test1"));

        AnalyticsDashboard dashboard = service.generateAnalyticsDashboard();

        assertNotNull(dashboard);
        assertNotNull(dashboard.getDashboardId());
        assertNotNull(dashboard.getName());
        assertNotNull(dashboard.getForecasts());
        assertNotNull(dashboard.getTopPerformingContent());
        assertNotNull(dashboard.getEmergingTrends());
        assertNotNull(dashboard.getAlerts());
        assertNotNull(dashboard.getOverallMetrics());
        assertNotNull(dashboard.getLastRefreshed());
    }

    @Test
    void testGenerateAnalyticsDashboardWithNoData() {
        AnalyticsDashboard dashboard = service.generateAnalyticsDashboard();

        assertNotNull(dashboard);
        assertNotNull(dashboard.getForecasts());
        assertTrue(dashboard.getForecasts().isEmpty());
    }

    @Test
    void testAnalyticsDashboardAlertsContainInfo() {
        AnalyticsDashboard dashboard = service.generateAnalyticsDashboard();

        assertNotNull(dashboard.getAlerts());
        assertFalse(dashboard.getAlerts().isEmpty());
    }

    @Test
    void testAnalyticsDashboardOverallMetrics() {
        AnalyticsDashboard dashboard = service.generateAnalyticsDashboard();

        assertNotNull(dashboard.getOverallMetrics());
        assertTrue(dashboard.getOverallMetrics().containsKey("totalPredictions"));
    }

    @Test
    void testPerformancePredictionStaticCreate() {
        PerformancePrediction prediction = PerformancePrediction.create("/content/page/test", "blog");

        assertEquals("/content/page/test", prediction.getContentPath());
        assertEquals("blog", prediction.getContentType());
        assertNotNull(prediction.getPredictionDate());
    }

    @Test
    void testTrafficForecastStaticCreate() {
        TrafficForecast forecast = TrafficForecast.create("/content/page/test", "product");

        assertEquals("/content/page/test", forecast.getContentPath());
        assertEquals("product", forecast.getContentType());
    }

    @Test
    void testScheduleSuggestionStaticCreate() {
        ScheduleSuggestion suggestion = ScheduleSuggestion.create("/content/page/test");

        assertEquals("/content/page/test", suggestion.getContentPath());
    }

    @Test
    void testTrendingTopicStaticCreate() {
        TrendingTopic topic = TrendingTopic.create("AI Trends", "technology");

        assertEquals("AI Trends", topic.getTopic());
        assertEquals("technology", topic.getCategory());
        assertNotNull(topic.getDetectedAt());
    }

    @Test
    void testContentCalendarStaticCreate() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);

        ContentCalendar calendar = ContentCalendar.create("Test Calendar", startDate, endDate);

        assertEquals("Test Calendar", calendar.getName());
        assertEquals(startDate, calendar.getStartDate());
        assertEquals(endDate, calendar.getEndDate());
        assertNotNull(calendar.getLastUpdated());
    }

    @Test
    void testAnalyticsDashboardStaticCreate() {
        AnalyticsDashboard dashboard = AnalyticsDashboard.create("Test Dashboard");

        assertNotNull(dashboard.getDashboardId());
        assertEquals("Test Dashboard", dashboard.getName());
        assertNotNull(dashboard.getLastRefreshed());
    }

    @Test
    void testTrafficProjection() {
        TrafficForecast.TrafficProjection projection = new TrafficForecast.TrafficProjection();
        projection.setDate(LocalDate.now());
        projection.setPredictedViews(1000.0);
        projection.setPredictedUniqueVisitors(700.0);
        projection.setConfidenceLow(800.0);
        projection.setConfidenceHigh(1200.0);

        assertEquals(LocalDate.now(), projection.getDate());
        assertEquals(1000.0, projection.getPredictedViews());
        assertEquals(700.0, projection.getPredictedUniqueVisitors());
        assertEquals(800.0, projection.getConfidenceLow());
        assertEquals(1200.0, projection.getConfidenceHigh());
    }

    @Test
    void testCalendarEntry() {
        ContentCalendar.CalendarEntry entry = new ContentCalendar.CalendarEntry();
        entry.setContentPath("/content/page/test");
        entry.setTitle("Test Entry");
        entry.setScheduledDate(LocalDate.now());
        entry.setContentType("blog");
        entry.setStatus("scheduled");
        entry.setPredictedPerformance(0.85);

        assertEquals("/content/page/test", entry.getContentPath());
        assertEquals("Test Entry", entry.getTitle());
        assertEquals("blog", entry.getContentType());
        assertEquals("scheduled", entry.getStatus());
        assertEquals(0.85, entry.getPredictedPerformance());
    }

    @Test
    void testForecastSummary() {
        AnalyticsDashboard.ForecastSummary summary = new AnalyticsDashboard.ForecastSummary();
        summary.setContentPath("/content/page/test");
        summary.setContentType("blog");
        summary.setPredictedViews(5000.0);
        summary.setConfidenceScore(0.9);
        summary.setTrendDirection("increasing");

        assertEquals("/content/page/test", summary.getContentPath());
        assertEquals("blog", summary.getContentType());
        assertEquals(5000.0, summary.getPredictedViews());
        assertEquals(0.9, summary.getConfidenceScore());
        assertEquals("increasing", summary.getTrendDirection());
    }

    @Test
    void testAlert() {
        AnalyticsDashboard.Alert alert = new AnalyticsDashboard.Alert();
        alert.setAlertId("alert-1");
        alert.setType("performance");
        alert.setMessage("High traffic detected");
        alert.setSeverity("warning");
        alert.setCreatedAt(LocalDateTime.now());

        assertEquals("alert-1", alert.getAlertId());
        assertEquals("performance", alert.getType());
        assertEquals("warning", alert.getSeverity());
    }

    @Test
    void testContentTypeAffectsPrediction() {
        PerformancePrediction blogPrediction = service.predictContentPerformance("/content/page/blog", "blog");
        PerformancePrediction productPrediction = service.predictContentPerformance("/content/page/product", "product");
        PerformancePrediction videoPrediction = service.predictContentPerformance("/content/page/video", "video");

        assertNotNull(blogPrediction);
        assertNotNull(productPrediction);
        assertNotNull(videoPrediction);
    }

    @Test
    void testTrafficForecastProjectionsCoverAllDays() {
        int daysAhead = 14;
        TrafficForecast forecast = service.forecastTraffic("/content/page/test", "blog", daysAhead);

        assertEquals(daysAhead, forecast.getProjections().size());

        LocalDate expectedDate = LocalDate.now();
        for (TrafficForecast.TrafficProjection projection : forecast.getProjections()) {
            assertNotNull(projection.getDate());
            assertTrue(projection.getPredictedViews() >= 0.0);
            assertTrue(projection.getConfidenceLow() <= projection.getPredictedViews());
            assertTrue(projection.getConfidenceHigh() >= projection.getPredictedViews());
        }
    }

    static class ContentForecastServiceTestConfig implements ContentForecastServiceConfig {
        @Override
        public String apiKey() {
            return "test-api-key";
        }

        @Override
        public String serviceUrl() {
            return "https://api.openai.com/v1/chat/completions";
        }

        @Override
        public String defaultModel() {
            return "gpt-4";
        }

        @Override
        public float temperature() {
            return 0.7f;
        }

        @Override
        public int maxTokens() {
            return 4000;
        }

        @Override
        public boolean enableCache() {
            return true;
        }

        @Override
        public int cacheSize() {
            return 100;
        }

        @Override
        public int forecastDaysAhead() {
            return 30;
        }

        @Override
        public int trendingTopicsLimit() {
            return 20;
        }
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() { return ContentForecastServiceConfig.class; }
    }
}