package com.aem.playground.core.services.analytics;

import java.util.List;

public interface AnalyticsDataCollector {

    List<ContentMetrics> collectContentMetrics(String rootPath, int maxItems);

    ContentMetrics getContentMetrics(String contentPath);

    List<ContentMetrics> getMetricsByDateRange(String rootPath, long startDate, long endDate);

    List<ContentMetrics> getTopPerformingContent(String rootPath, int limit);

    List<ContentMetrics> getUnderperformingContent(String rootPath, int limit);
}