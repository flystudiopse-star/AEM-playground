package com.aem.playground.core.services.analytics;

import java.util.List;

public interface ReportGenerationService {

    AnalyticsReport generateWeeklyReport(String rootPath);

    AnalyticsReport generateMonthlyReport(String rootPath);

    AnalyticsReport generateCustomReport(String rootPath, AnalyticsReport.ReportType reportType, long startDate, long endDate);

    List<AnalyticsReport> getReportHistory(String rootPath, int limit);

    AnalyticsReport getReport(String reportId);

    String exportReportAsJson(AnalyticsReport report);

    String exportReportAsPdf(AnalyticsReport report);
}