package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ContentForecastService {

    PerformancePrediction predictContentPerformance(String contentPath, String contentType);

    TrafficForecast forecastTraffic(String contentPath, String contentType, int daysAhead);

    ScheduleSuggestion suggestPublishSchedule(String contentPath, String contentType);

    List<TrendingTopic> identifyTrendingTopics(List<String> contentPaths);

    ContentCalendar generateContentCalendar(LocalDate startDate, LocalDate endDate, List<String> contentTypes);

    AnalyticsDashboard generateAnalyticsDashboard();
}
