package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;

import java.util.List;

public interface ContentSchedulerService {

    List<OptimalPublishTime> analyzeOptimalPublishTimes(String contentPath, int numberOfSlots);

    ScheduleRecommendation scheduleForBestEngagement(String contentPath, long preferredPublishTime);

    ContentCalendar createContentCalendar(String rootPath, long startDate, long endDate);

    PublicationFrequency suggestPublicationFrequency(String contentType);

    TimezoneAwareSchedule adjustForTimezone(String contentPath, long publishTime, String targetTimezone);

    String integrateWithAEMScheduler(String contentPath, long scheduledTime);

    SchedulerAnalyticsDashboard getSchedulingAnalytics();

    EngagementScore predictEngagement(String contentPath, long publishTime);
}