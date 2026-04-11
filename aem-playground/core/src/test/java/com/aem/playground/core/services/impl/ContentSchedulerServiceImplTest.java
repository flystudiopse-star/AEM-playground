package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.ContentSchedulerConfig;
import com.aem.playground.core.services.ContentSchedulerService;
import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentSchedulerServiceImplTest {

    private ContentSchedulerServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ContentSchedulerServiceImpl();

        ContentSchedulerServiceTestConfig config = new ContentSchedulerServiceTestConfig();
        service.activate(config);
    }

    @Test
    void testAnalyzeOptimalPublishTimesWithValidInput() {
        SchedulingAnalysis analysis = service.analyzeOptimalPublishTimes("/content/page/blog-post", "blog", "America/New_York");

        assertNotNull(analysis);
        assertEquals("/content/page/blog-post", analysis.getContentPath());
        assertEquals("blog", analysis.getContentType());
        assertEquals("America/New_York", analysis.getTargetTimezone());
        assertNotNull(analysis.getOptimalPublishTime());
        assertTrue(analysis.getConfidenceScore() >= 0.0);
        assertNotNull(analysis.getReasoning());
        assertNotNull(analysis.getFactors());
    }

    @Test
    void testAnalyzeOptimalPublishTimesWithEmptyContentPath() {
        SchedulingAnalysis analysis = service.analyzeOptimalPublishTimes("", "blog", "America/New_York");

        assertNotNull(analysis);
        assertEquals(0.0, analysis.getConfidenceScore());
    }

    @Test
    void testAnalyzeOptimalPublishTimesWithNullContentType() {
        SchedulingAnalysis analysis = service.analyzeOptimalPublishTimes("/content/page/test", null, "Europe/London");

        assertNotNull(analysis);
        assertEquals("/content/page/test", analysis.getContentPath());
    }

    @Test
    void testAnalyzeOptimalPublishTimesReturnsAlternativeSlots() {
        SchedulingAnalysis analysis = service.analyzeOptimalPublishTimes("/content/page/test", "blog", "America/Los_Angeles");

        assertNotNull(analysis.getAlternativeSlots());
    }

    @Test
    void testAnalyzeOptimalPublishTimesCaching() {
        SchedulingAnalysis analysis1 = service.analyzeOptimalPublishTimes("/content/page/test", "blog", "America/New_York");
        SchedulingAnalysis analysis2 = service.analyzeOptimalPublishTimes("/content/page/test", "blog", "America/New_York");

        assertNotNull(analysis1);
        assertNotNull(analysis2);
    }

    @Test
    void testScheduleForBestEngagementWithValidInput() {
        ScheduleSuggestion suggestion = service.scheduleForBestEngagement("/content/page/blog-post", "blog", "America/New_York");

        assertNotNull(suggestion);
        assertEquals("/content/page/blog-post", suggestion.getContentPath());
        assertNotNull(suggestion.getSuggestedPublishTime());
        assertNotNull(suggestion.getAlternativeTimes());
        assertTrue(suggestion.getConfidenceScore() >= 0.0);
        assertNotNull(suggestion.getReasoning());
        assertNotNull(suggestion.getFactors());
    }

    @Test
    void testScheduleForBestEngagementWithEmptyContentPath() {
        ScheduleSuggestion suggestion = service.scheduleForBestEngagement("", "blog", "America/New_York");

        assertNotNull(suggestion);
        assertEquals(0.0, suggestion.getConfidenceScore());
    }

    @Test
    void testScheduleForBestEngagementHasMultipleAlternatives() {
        ScheduleSuggestion suggestion = service.scheduleForBestEngagement("/content/page/test", "product", "Europe/Paris");

        assertNotNull(suggestion.getAlternativeTimes());
    }

    @Test
    void testScheduleContentWithValidInput() {
        ZonedDateTime futurePublishTime = ZonedDateTime.now(ZoneId.of("America/New_York")).plusDays(1);
        ZonedDateTime futureUnpublishTime = futurePublishTime.plusDays(30);

        ContentSchedulerSchedulerResult result = service.scheduleContent("/content/page/test", futurePublishTime, futureUnpublishTime);

        assertNotNull(result);
        assertEquals("/content/page/test", result.getContentPath());
        assertEquals("scheduled", result.getStatus());
        assertNotNull(result.getSchedulerJobId());
        assertTrue(result.getPredictedEngagement() >= 0.0);
        assertNotNull(result.getMetadata());
    }

    @Test
    void testScheduleContentWithPastPublishTime() {
        ZonedDateTime pastPublishTime = ZonedDateTime.now(ZoneId.of("America/New_York")).minusDays(1);

        ContentSchedulerSchedulerResult result = service.scheduleContent("/content/page/test", pastPublishTime, null);

        assertNotNull(result);
        assertEquals("failed", result.getStatus());
    }

    @Test
    void testScheduleContentWithEmptyContentPath() {
        ZonedDateTime futurePublishTime = ZonedDateTime.now(ZoneId.of("America/New_York")).plusDays(1);

        ContentSchedulerSchedulerResult result = service.scheduleContent("", futurePublishTime, null);

        assertNotNull(result);
        assertEquals("failed", result.getStatus());
    }

    @Test
    void testScheduleContentGeneratesJobId() {
        ZonedDateTime futurePublishTime = ZonedDateTime.now(ZoneId.of("America/New_York")).plusDays(1);

        ContentSchedulerSchedulerResult result1 = service.scheduleContent("/content/page/test1", futurePublishTime, null);
        ContentSchedulerSchedulerResult result2 = service.scheduleContent("/content/page/test2", futurePublishTime, null);

        assertNotNull(result1.getSchedulerJobId());
        assertNotNull(result2.getSchedulerJobId());
    }

    @Test
    void testCreateContentCalendarWithValidDates() {
        ZonedDateTime startDate = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime endDate = startDate.plusDays(30);
        List<String> contentTypes = Arrays.asList("blog", "news", "product");

        ContentCalendar calendar = service.createContentCalendar(startDate, endDate, contentTypes);

        assertNotNull(calendar);
        assertEquals(startDate.toLocalDate(), calendar.getStartDate());
        assertEquals(endDate.toLocalDate(), calendar.getEndDate());
        assertNotNull(calendar.getEntries());
        assertNotNull(calendar.getSuggestedTopics());
    }

    @Test
    void testCreateContentCalendarWithNullDates() {
        ContentCalendar calendar = service.createContentCalendar(null, null, null);

        assertNotNull(calendar);
    }

    @Test
    void testCreateContentCalendarWithInvalidDateRange() {
        ZonedDateTime startDate = ZonedDateTime.now(ZoneId.of("America/New_York")).plusDays(10);
        ZonedDateTime endDate = ZonedDateTime.now(ZoneId.of("America/New_York"));

        ContentCalendar calendar = service.createContentCalendar(startDate, endDate, null);

        assertNotNull(calendar);
    }

    @Test
    void testCreateContentCalendarContainsEntries() {
        ZonedDateTime startDate = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime endDate = startDate.plusDays(60);

        ContentCalendar calendar = service.createContentCalendar(startDate, endDate, Arrays.asList("blog", "product"));

        assertNotNull(calendar.getEntries());
    }

    @Test
    void testSuggestPublicationFrequencyWithValidInput() {
        PublicationFrequencySuggestion suggestion = service.suggestPublicationFrequency("blog", 10);

        assertNotNull(suggestion);
        assertEquals("blog", suggestion.getContentType());
        assertTrue(suggestion.getPostsPerWeek() >= 1);
        assertTrue(suggestion.getPostsPerMonth() >= 1);
        assertTrue(suggestion.getConfidenceScore() >= 0.0);
        assertNotNull(suggestion.getRecommendedFrequency());
        assertNotNull(suggestion.getReasoning());
        assertNotNull(suggestion.getBestDays());
        assertNotNull(suggestion.getPeakHours());
    }

    @Test
    void testSuggestPublicationFrequencyWithZeroHistoricalContent() {
        PublicationFrequencySuggestion suggestion = service.suggestPublicationFrequency("news", 0);

        assertNotNull(suggestion);
        assertTrue(suggestion.getPostsPerWeek() >= 1);
    }

    @Test
    void testSuggestPublicationFrequencyReturnsBestDays() {
        PublicationFrequencySuggestion suggestion = service.suggestPublicationFrequency("product", 8);

        assertNotNull(suggestion.getBestDays());
    }

    @Test
    void testSuggestPublicationFrequencyReturnsPeakHours() {
        PublicationFrequencySuggestion suggestion = service.suggestPublicationFrequency("blog", 5);

        assertNotNull(suggestion.getPeakHours());
    }

    @Test
    void testGetOptimalTimeSlotsWithValidInput() {
        List<TimeSlotRecommendation> slots = service.getOptimalTimeSlots("blog", 5, "America/New_York");

        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        assertEquals(5, slots.size());
    }

    @Test
    void testGetOptimalTimeSlotsWithEmptyContentType() {
        List<TimeSlotRecommendation> slots = service.getOptimalTimeSlots("", 3, "America/New_York");

        assertTrue(slots.isEmpty());
    }

    @Test
    void testGetOptimalTimeSlotsContainsEngagementScores() {
        List<TimeSlotRecommendation> slots = service.getOptimalTimeSlots("product", 5, "Europe/London");

        for (TimeSlotRecommendation slot : slots) {
            assertTrue(slot.getPredictedEngagement() >= 0.0);
            assertTrue(slot.getConfidenceScore() >= 0.0);
            assertNotNull(slot.getReason());
            assertNotNull(slot.getSlotTime());
        }
    }

    @Test
    void testGetOptimalTimeSlotsMarksPeakTimes() {
        List<TimeSlotRecommendation> slots = service.getOptimalTimeSlots("blog", 5, "America/New_York");

        boolean hasPeakTime = false;
        for (TimeSlotRecommendation slot : slots) {
            if (slot.isPeakTime()) {
                hasPeakTime = true;
                break;
            }
        }
        assertTrue(hasPeakTime);
    }

    @Test
    void testSchedulingAnalysisStaticCreate() {
        SchedulingAnalysis analysis = SchedulingAnalysis.create("/content/page/test", "blog", "America/New_York");

        assertEquals("/content/page/test", analysis.getContentPath());
        assertEquals("blog", analysis.getContentType());
        assertEquals("America/New_York", analysis.getTargetTimezone());
        assertNotNull(analysis.getAnalyzedAt());
    }

    @Test
    void testPublicationFrequencySuggestionStaticCreate() {
        PublicationFrequencySuggestion suggestion = PublicationFrequencySuggestion.create("news");

        assertEquals("news", suggestion.getContentType());
        assertNotNull(suggestion.getAnalyzedAt());
    }

    @Test
    void testTimeSlotRecommendationStaticCreate() {
        ZonedDateTime slotTime = ZonedDateTime.now(ZoneId.of("America/New_York")).plusDays(1);
        TimeSlotRecommendation recommendation = TimeSlotRecommendation.create("blog", slotTime);

        assertEquals("blog", recommendation.getContentType());
        assertEquals(slotTime, recommendation.getSlotTime());
    }

    static class ContentSchedulerServiceTestConfig implements ContentSchedulerConfig {
        @Override
        public String defaultTimezone() {
            return "America/New_York";
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
        public int defaultPublishLeadTime() {
            return 24;
        }

        @Override
        public int defaultUnpublishLeadTime() {
            return 30;
        }

        @Override
        public int recommendedSlotsPerDay() {
            return 5;
        }

        @Override
        public double confidenceThreshold() {
            return 0.7;
        }

        @Override
        public String schedulerCron() {
            return "0 0 * * * ?";
        }

        @Override
        public boolean enableAemSchedulerIntegration() {
            return true;
        }
    }
}