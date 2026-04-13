package com.aem.playground.core.services.analytics;

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = ReportGenerationService.class, immediate = true)
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerationServiceImpl.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final Map<String, AnalyticsReport> reportCache = new ConcurrentHashMap<>();

    @Reference
    private AnalyticsDataCollector dataCollector;

    @Reference
    private AIAnalyticsService aiAnalyticsService;

    @Reference
    private ContentScoringService contentScoringService;

    @Reference
    private TrendPredictionService trendPredictionService;

    @Reference
    private AIService aiService;

    @Override
    public AnalyticsReport generateWeeklyReport(String rootPath) {
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (7L * 24 * 60 * 60 * 1000);

        LOG.info("Generating weekly report for {}", rootPath);
        return generateReport(rootPath, AnalyticsReport.ReportType.WEEKLY, startDate, endDate);
    }

    @Override
    public AnalyticsReport generateMonthlyReport(String rootPath) {
        long endDate = System.currentTimeMillis();
        long startDate = endDate - (30L * 24 * 60 * 60 * 1000);

        LOG.info("Generating monthly report for {}", rootPath);
        return generateReport(rootPath, AnalyticsReport.ReportType.MONTHLY, startDate, endDate);
    }

    @Override
    public AnalyticsReport generateCustomReport(String rootPath, AnalyticsReport.ReportType reportType, 
                                                long startDate, long endDate) {
        LOG.info("Generating custom {} report for {} from {} to {}", reportType, rootPath, startDate, endDate);
        return generateReport(rootPath, reportType, startDate, endDate);
    }

    private AnalyticsReport generateReport(String rootPath, AnalyticsReport.ReportType reportType, 
                                           long startDate, long endDate) {
        List<ContentMetrics> metrics = dataCollector.getMetricsByDateRange(rootPath, startDate, endDate);
        
        if (metrics.isEmpty()) {
            metrics = dataCollector.collectContentMetrics(rootPath, 20);
        }

        List<AIAnalyticsInsight> insights = aiAnalyticsService.generateInsights(metrics);

        List<ContentScore> contentScores = new ArrayList<>();
        for (ContentMetrics m : metrics) {
            ContentScore score = contentScoringService.scoreContent(m.getContentPath());
            contentScores.add(score);
        }

        List<AnalyticsReport.TrendPrediction> trendPredictions = 
            trendPredictionService.predictTrends(metrics, reportType == AnalyticsReport.ReportType.WEEKLY ? 7 : 30);

        Map<String, Object> summary = generateSummary(metrics, insights, contentScores, trendPredictions);

        String reportId = UUID.randomUUID().toString();
        String periodStart = DATE_FORMAT.format(new Date(startDate));
        String periodEnd = DATE_FORMAT.format(new Date(endDate));

        AnalyticsReport report = AnalyticsReport.builder()
                .reportId(reportId)
                .reportType(reportType)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .contentMetrics(metrics)
                .insights(insights)
                .contentScores(contentScores)
                .summary(summary)
                .trendPredictions(trendPredictions)
                .build();

        reportCache.put(reportId, report);
        
        LOG.info("Generated {} report: {} with {} metrics, {} insights", 
                 reportType, reportId, metrics.size(), insights.size());

        return report;
    }

    private Map<String, Object> generateSummary(List<ContentMetrics> metrics, List<AIAnalyticsInsight> insights,
                                                List<ContentScore> scores, List<AnalyticsReport.TrendPrediction> predictions) {
        Map<String, Object> summary = new HashMap<>();

        long totalViews = metrics.stream().mapToLong(ContentMetrics::getPageViews).sum();
        long totalVisitors = metrics.stream().mapToLong(ContentMetrics::getUniqueVisitors).sum();
        long totalConversions = metrics.stream().mapToLong(ContentMetrics::getConversionCount).sum();
        double avgBounceRate = metrics.stream().mapToDouble(ContentMetrics::getBounceRate).average().orElse(0.0);
        double avgTimeOnPage = metrics.stream().mapToLong(ContentMetrics::getAvgTimeOnPage).average().orElse(0.0);

        summary.put("totalPageViews", totalViews);
        summary.put("totalUniqueVisitors", totalVisitors);
        summary.put("totalConversions", totalConversions);
        summary.put("averageBounceRate", avgBounceRate);
        summary.put("averageTimeOnPage", avgTimeOnPage);
        summary.put("contentItemsAnalyzed", metrics.size());

        double avgOverallScore = scores.stream().mapToDouble(ContentScore::getOverallScore).average().orElse(0.0);
        double avgQualityScore = scores.stream().mapToDouble(ContentScore::getQualityScore).average().orElse(0.0);
        double avgSeoScore = scores.stream().mapToDouble(ContentScore::getSeoScore).average().orElse(0.0);
        double avgEngagementScore = scores.stream().mapToDouble(ContentScore::getEngagementScore).average().orElse(0.0);

        summary.put("averageOverallScore", avgOverallScore);
        summary.put("averageQualityScore", avgQualityScore);
        summary.put("averageSeoScore", avgSeoScore);
        summary.put("averageEngagementScore", avgEngagementScore);

        long highPriorityInsights = insights.stream().filter(i -> i.getPriority() == 1).count();
        summary.put("highPriorityInsights", highPriorityInsights);
        summary.put("totalInsights", insights.size());

        long risingContent = predictions.stream().filter(p -> p.getPredictedGrowth() > 0).count();
        long decliningContent = predictions.stream().filter(p -> p.getPredictedGrowth() < 0).count();
        summary.put("risingContentCount", risingContent);
        summary.put("decliningContentCount", decliningContent);

        return summary;
    }

    @Override
    public List<AnalyticsReport> getReportHistory(String rootPath, int limit) {
        return new ArrayList<>(reportCache.values()).stream()
                .sorted((a, b) -> Long.compare(b.getGeneratedAt(), a.getGeneratedAt()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public AnalyticsReport getReport(String reportId) {
        return reportCache.get(reportId);
    }

    @Override
    public String exportReportAsJson(AnalyticsReport report) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (Exception e) {
            LOG.error("Failed to export report as JSON", e);
            return "{}";
        }
    }

    @Override
    public String exportReportAsPdf(AnalyticsReport report) {
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("ANALYTICS REPORT\n");
        pdfContent.append("================\n\n");
        pdfContent.append("Report ID: ").append(report.getReportId()).append("\n");
        pdfContent.append("Type: ").append(report.getReportType()).append("\n");
        pdfContent.append("Period: ").append(report.getPeriodStart()).append(" to ").append(report.getPeriodEnd()).append("\n");
        pdfContent.append("Generated: ").append(new Date(report.getGeneratedAt())).append("\n\n");

        pdfContent.append("SUMMARY\n");
        pdfContent.append("-------\n");
        Map<String, Object> summary = report.getSummary();
        for (Map.Entry<String, Object> entry : summary.entrySet()) {
            pdfContent.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        pdfContent.append("\n\nTOP INSIGHTS\n");
        pdfContent.append("------------\n");
        for (AIAnalyticsInsight insight : report.getInsights().stream().limit(10).collect(java.util.stream.Collectors.toList())) {
            pdfContent.append("- ").append(insight.getTitle()).append("\n");
            pdfContent.append("  ").append(insight.getDescription()).append("\n");
            pdfContent.append("  Recommendation: ").append(insight.getRecommendation()).append("\n\n");
        }

        LOG.info("Exported report {} as PDF format", report.getReportId());
        return pdfContent.toString();
    }
}