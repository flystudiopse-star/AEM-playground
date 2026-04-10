package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.ContentSchedulerService;
import com.aem.playground.core.services.ContentSchedulerServiceConfig;
import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContentSchedulerServiceTest {

    private ContentSchedulerService schedulerService;

    @BeforeEach
    void setUp() throws Exception {
        schedulerService = new ContentSchedulerServiceImpl();
        
        ContentSchedulerServiceConfig config = new TestContentSchedulerServiceConfig();
        
        schedulerService.activate(config);
    }

    @Test
    void testAnalyzeOptimalPublishTimes() {
        List<OptimalPublishTime> times = schedulerService.analyzeOptimalPublishTimes("/content/pages/home", 3);

        assertNotNull(times);
        assertFalse(times.isEmpty());
    }

    @Test
    void testAnalyzeOptimalPublishTimesWithNullPath() {
        List<OptimalPublishTime> times = schedulerService.analyzeOptimalPublishTimes(null, 3);

        assertNotNull(times);
        assertTrue(times.isEmpty());
    }

    @Test
    void testAnalyzeOptimalPublishTimesWithZeroSlots() {
        List<OptimalPublishTime> times = schedulerService.analyzeOptimalPublishTimes("/content/pages/home", 0);

        assertNotNull(times);
        assertTrue(times.isEmpty());
    }

    @Test
    void testScheduleForBestEngagement() {
        long preferredTime = System.currentTimeMillis() + 86400000;
        ScheduleRecommendation rec = schedulerService.scheduleForBestEngagement("/content/pages/home", preferredTime);

        assertNotNull(rec);
        assertEquals("/content/pages/home", rec.getContentPath());
    }

    @Test
    void testScheduleForBestEngagementWithNullPath() {
        long preferredTime = System.currentTimeMillis() + 86400000;
        ScheduleRecommendation rec = schedulerService.scheduleForBestEngagement(null, preferredTime);

        assertNotNull(rec);
    }

    @Test
    void testCreateContentCalendar() {
        long startDate = System.currentTimeMillis();
        long endDate = startDate + (30L * 24 * 60 * 60 * 1000);
        
        ContentCalendar calendar = schedulerService.createContentCalendar("/content", startDate, endDate);

        assertNotNull(calendar);
    }

    @Test
    void testCreateContentCalendarWithInvalidDates() {
        long startDate = System.currentTimeMillis();
        long endDate = startDate - 1000;
        
        ContentCalendar calendar = schedulerService.createContentCalendar("/content", startDate, endDate);

        assertNull(calendar);
    }

    @Test
    void testSuggestPublicationFrequency() {
        PublicationFrequency freq = schedulerService.suggestPublicationFrequency("blog");

        assertNotNull(freq);
        assertEquals("blog", freq.getContentType());
    }

    @Test
    void testSuggestPublicationFrequencyWithDefaultType() {
        PublicationFrequency freq = schedulerService.suggestPublicationFrequency(null);

        assertNotNull(freq);
        assertEquals("default", freq.getContentType());
    }

    @Test
    void testAdjustForTimezone() {
        long publishTime = System.currentTimeMillis() + 86400000;
        TimezoneAwareSchedule schedule = schedulerService.adjustForTimezone("/content/pages/home", publishTime, "America/Los_Angeles");

        assertNotNull(schedule);
        assertEquals("/content/pages/home", schedule.getContentPath());
    }

    @Test
    void testAdjustForTimezoneWithInvalidParams() {
        long publishTime = System.currentTimeMillis() + 86400000;
        TimezoneAwareSchedule schedule = schedulerService.adjustForTimezone(null, publishTime, "America/Los_Angeles");

        assertNull(schedule);
    }

    @Test
    void testIntegrateWithAEMScheduler() {
        long scheduledTime = System.currentTimeMillis() + 86400000;
        String jobId = schedulerService.integrateWithAEMScheduler("/content/pages/home", scheduledTime);

        assertNotNull(jobId);
    }

    @Test
    void testIntegrateWithAEMSchedulerWithInvalidParams() {
        long scheduledTime = System.currentTimeMillis() + 86400000;
        String jobId = schedulerService.integrateWithAEMScheduler(null, scheduledTime);

        assertNull(jobId);
    }

    @Test
    void testGetSchedulingAnalytics() {
        SchedulerAnalyticsDashboard dashboard = schedulerService.getSchedulingAnalytics();

        assertNotNull(dashboard);
        assertNotNull(dashboard.getBestTimeSlots());
    }

    @Test
    void testPredictEngagement() {
        long publishTime = System.currentTimeMillis() + 86400000;
        EngagementScore score = schedulerService.predictEngagement("/content/pages/home", publishTime);

        assertNotNull(score);
        assertEquals("/content/pages/home", score.getContentPath());
    }

    @Test
    void testPredictEngagementWithInvalidParams() {
        long publishTime = System.currentTimeMillis() + 86400000;
        EngagementScore score = schedulerService.predictEngagement(null, publishTime);

        assertNull(score);
    }

    @Test
    void testOptimalPublishTimeBuilder() {
        OptimalPublishTime optTime = OptimalPublishTime.builder()
                .timeSlotId("slot-1")
                .hour(9)
                .dayOfWeek(1)
                .engagementScore(0.85)
                .timezone("America/New_York")
                .confidenceLevel(0.9)
                .build();

        assertNotNull(optTime);
        assertEquals("slot-1", optTime.getTimeSlotId());
        assertEquals(9, optTime.getHour());
        assertEquals(1, optTime.getDayOfWeek());
        assertEquals(0.85, optTime.getEngagementScore());
        assertEquals("America/New_York", optTime.getTimezone());
        assertEquals(0.9, optTime.getConfidenceLevel());
    }

    @Test
    void testScheduleRecommendationBuilder() {
        List<String> reasoning = new ArrayList<>();
        reasoning.add("Based on engagement analysis");
        reasoning.add("Optimal time for your audience");

        ScheduleRecommendation rec = ScheduleRecommendation.builder()
                .recommendationId("rec-1")
                .contentPath("/content/pages/home")
                .scheduledPublishTime(System.currentTimeMillis() + 86400000)
                .optimalPublishTime(System.currentTimeMillis() + 172800000)
                .targetTimezone("America/New_York")
                .confidenceScore(0.85)
                .recommendationType("optimal")
                .reasoning(reasoning)
                .build();

        assertNotNull(rec);
        assertEquals("rec-1", rec.getRecommendationId());
        assertEquals("/content/pages/home", rec.getContentPath());
        assertEquals(0.85, rec.getConfidenceScore());
        assertEquals("optimal", rec.getRecommendationType());
    }

    @Test
    void testPublicationFrequencyBuilder() {
        PublicationFrequency freq = PublicationFrequency.builder()
                .frequencyId("freq-1")
                .contentType("blog")
                .frequency("twice-weekly")
                .postsPerWeek(3)
                .optimalPostCount(3)
                .rationale("Recommended 3 posts per week for blog content")
                .confidenceScore(0.85)
                .build();

        assertNotNull(freq);
        assertEquals("freq-1", freq.getFrequencyId());
        assertEquals("blog", freq.getContentType());
        assertEquals("twice-weekly", freq.getFrequency());
        assertEquals(3, freq.getPostsPerWeek());
        assertEquals(0.85, freq.getConfidenceScore());
    }

    @Test
    void testTimezoneAwareScheduleBuilder() {
        TimezoneAwareSchedule schedule = TimezoneAwareSchedule.builder()
                .scheduleId("tz-1")
                .contentPath("/content/pages/home")
                .sourceTimezone("America/New_York")
                .targetTimezone("America/Los_Angeles")
                .originalPublishTime(System.currentTimeMillis())
                .adjustedPublishTime(System.currentTimeMillis() - 10800000)
                .daylightSavingTime(false)
                .build();

        assertNotNull(schedule);
        assertEquals("tz-1", schedule.getScheduleId());
        assertEquals("/content/pages/home", schedule.getContentPath());
        assertEquals("America/New_York", schedule.getSourceTimezone());
        assertEquals("America/Los_Angeles", schedule.getTargetTimezone());
    }

    @Test
    void testSchedulerAnalyticsDashboardBuilder() {
        List<String> bestSlots = new ArrayList<>();
        bestSlots.add("Monday 9:00 AM");
        bestSlots.add("Wednesday 2:00 PM");

        SchedulerAnalyticsDashboard dashboard = SchedulerAnalyticsDashboard.builder()
                .dashboardId("dashboard-1")
                .generatedAt(System.currentTimeMillis())
                .averageEngagement(0.75)
                .optimalPostingFrequency(5.0)
                .bestTimeSlots(bestSlots)
                .totalScheduled(100)
                .totalPublished(85)
                .successRate(0.85)
                .build();

        assertNotNull(dashboard);
        assertEquals("dashboard-1", dashboard.getDashboardId());
        assertEquals(0.75, dashboard.getAverageEngagement());
        assertEquals(5.0, dashboard.getOptimalPostingFrequency());
        assertEquals(100, dashboard.getTotalScheduled());
        assertEquals(85, dashboard.getTotalPublished());
        assertEquals(0.85, dashboard.getSuccessRate());
    }

    @Test
    void testEngagementScoreBuilder() {
        EngagementScore score = EngagementScore.builder()
                .scoreId("eng-1")
                .contentPath("/content/pages/home")
                .predictedEngagement(0.85)
                .confidenceLevel(0.9)
                .scoreCategory("excellent")
                .calculatedAt(System.currentTimeMillis())
                .build();

        assertNotNull(score);
        assertEquals("eng-1", score.getScoreId());
        assertEquals("/content/pages/home", score.getContentPath());
        assertEquals(0.85, score.getPredictedEngagement());
        assertEquals(0.9, score.getConfidenceLevel());
        assertEquals("excellent", score.getScoreCategory());
    }

    @Test
    void testCalendarEntryBuilder() {
        ContentCalendar.CalendarEntry entry = ContentCalendar.CalendarEntry.builder()
                .entryId("entry-1")
                .contentPath("/content/pages/home")
                .scheduledTime(System.currentTimeMillis() + 86400000)
                .status("scheduled")
                .contentTitle("Test Content")
                .contentType("page")
                .predictedEngagement(0.85)
                .build();

        assertNotNull(entry);
        assertEquals("entry-1", entry.getEntryId());
        assertEquals("/content/pages/home", entry.getContentPath());
        assertEquals("scheduled", entry.getStatus());
        assertEquals("Test Content", entry.getContentTitle());
        assertEquals("page", entry.getContentType());
        assertEquals(0.85, entry.getPredictedEngagement());
    }

    private static class TestContentSchedulerServiceConfig implements ContentSchedulerServiceConfig {
        @Override
        public String apiKey() {
            return "test-key";
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
            return 2000;
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
        public String defaultTimezone() {
            return "America/New_York";
        }

        @Override
        public int engagementAnalysisWindow() {
            return 30;
        }

        @Override
        public String defaultPublishFrequency() {
            return "weekly";
        }

        @Override
        public int postsPerWeek() {
            return 5;
        }

        @Override
        public String schedulerName() {
            return "aem-content-scheduler";
        }

        @Override
        public boolean enableTimezoneConversion() {
            return true;
        }

        @Override
        public double minEngagementScore() {
            return 0.5;
        }
    }
}