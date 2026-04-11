package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public interface ContentSchedulerService {

    SchedulingAnalysis analyzeOptimalPublishTimes(String contentPath, String contentType, String targetTimezone);

    ScheduleSuggestion scheduleForBestEngagement(String contentPath, String contentType, String targetTimezone);

    ContentSchedulerSchedulerResult scheduleContent(String contentPath, ZonedDateTime publishTime, ZonedDateTime unpublishTime);

    ContentCalendar createContentCalendar(ZonedDateTime startDate, ZonedDateTime endDate, List<String> contentTypes);

    PublicationFrequencySuggestion suggestPublicationFrequency(String contentType, int historicalContentCount);

    List<TimeSlotRecommendation> getOptimalTimeSlots(String contentType, int numberOfSlots, String targetTimezone);
}