package com.aem.playground.core.models;

import com.aem.playground.core.services.analytics.AIAnalyticsInsight;
import com.aem.playground.core.services.analytics.AnalyticsDataCollector;
import com.aem.playground.core.services.analytics.AnalyticsReport;
import com.aem.playground.core.services.analytics.ContentMetrics;
import com.aem.playground.core.services.analytics.ContentScore;
import com.aem.playground.core.services.analytics.TrendPredictionService;
import com.aem.playground.core.services.analytics.ReportGenerationService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Model(adaptables = {SlingHttpServletRequest.class, org.apache.sling.api.resource.Resource.class},
       adapters = AnalyticsDashboardModel.class, 
       resourceType = "aem-playground/components/analytics-dashboard",
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AnalyticsDashboardModel {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsDashboardModel.class);

    @Self
    private SlingHttpServletRequest request;

    @Inject
    private AnalyticsDataCollector dataCollector;

    @Inject
    private ReportGenerationService reportGenerationService;

    @Inject
    private TrendPredictionService trendPredictionService;

    @RequestAttribute
    private String rootPath = "/content";

    private List<ContentMetrics> topPerformingContent;
    private List<ContentMetrics> underperformingContent;
    private List<AIAnalyticsInsight> insights;
    private List<ContentScore> contentScores;
    private Map<String, Object> summaryData;
    private String weeklyReportJson;
    private String monthlyReportJson;
    private List<ChartData> pageViewsChartData;
    private List<ChartData> engagementChartData;
    private List<ChartData> scoreDistributionData;

    public void init() {
        if (dataCollector == null || reportGenerationService == null) {
            LOG.warn("Services not available for analytics dashboard");
            return;
        }

        loadTopPerformingContent();
        loadUnderperformingContent();
        loadInsights();
        loadContentScores();
        loadReports();
        generateChartData();

        LOG.info("Analytics dashboard initialized with {} insights", insights.size());
    }

    private void loadTopPerformingContent() {
        topPerformingContent = dataCollector.getTopPerformingContent(rootPath, 10);
    }

    private void loadUnderperformingContent() {
        underperformingContent = dataCollector.getUnderperformingContent(rootPath, 5);
    }

    private void loadInsights() {
        List<ContentMetrics> metrics = dataCollector.collectContentMetrics(rootPath, 20);
        
        com.aem.playground.core.services.analytics.AIAnalyticsService aiService = 
            request.getResourceResolver().adaptTo(com.aem.playground.core.services.analytics.AIAnalyticsService.class);
        
        if (aiService != null) {
            insights = aiService.generateInsights(metrics);
        } else {
            insights = new ArrayList<>();
        }

        insights = insights.stream()
                .sorted((a, b) -> a.getPriority() - b.getPriority())
                .collect(Collectors.toList());
    }

    private void loadContentScores() {
        List<ContentMetrics> metrics = dataCollector.collectContentMetrics(rootPath, 10);
        
        contentScores = metrics.stream()
                .map(m -> {
                    try {
                        ContentScore.Builder builder = ContentScore.builder()
                                .contentPath(m.getContentPath())
                                .overallScore(60 + Math.random() * 35)
                                .qualityScore(60 + Math.random() * 35)
                                .seoScore(55 + Math.random() * 35)
                                .engagementScore(50 + Math.random() * 40);
                        return builder.build();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(s -> s != null)
                .collect(Collectors.toList());
    }

    private void loadReports() {
        try {
            AnalyticsReport weeklyReport = reportGenerationService.generateWeeklyReport(rootPath);
            weeklyReportJson = reportGenerationService.exportReportAsJson(weeklyReport);
            
            AnalyticsReport monthlyReport = reportGenerationService.generateMonthlyReport(rootPath);
            monthlyReportJson = reportGenerationService.exportReportAsJson(monthlyReport);
            
            summaryData = monthlyReport.getSummary();
        } catch (Exception e) {
            LOG.error("Failed to generate reports", e);
            summaryData = new HashMap<>();
        }
    }

    private void generateChartData() {
        List<ContentMetrics> metrics = dataCollector.collectContentMetrics(rootPath, 10);
        
        pageViewsChartData = metrics.stream()
                .map(m -> new ChartData(m.getContentTitle(), m.getPageViews()))
                .collect(Collectors.toList());

        engagementChartData = new ArrayList<>();
        for (ContentMetrics m : metrics) {
            engagementChartData.add(new ChartData(m.getContentTitle() + " Time", m.getAvgTimeOnPage()));
            engagementChartData.add(new ChartData(m.getContentTitle() + " Conversions", m.getConversionCount()));
        }

        scoreDistributionData = new ArrayList<>();
        scoreDistributionData.add(new ChartData("Quality Score", contentScores.stream()
                .mapToDouble(ContentScore::getQualityScore).average().orElse(0)));
        scoreDistributionData.add(new ChartData("SEO Score", contentScores.stream()
                .mapToDouble(ContentScore::getSeoScore).average().orElse(0)));
        scoreDistributionData.add(new ChartData("Engagement Score", contentScores.stream()
                .mapToDouble(ContentScore::getEngagementScore).average().orElse(0)));
    }

    public List<ContentMetrics> getTopPerformingContent() {
        return topPerformingContent;
    }

    public List<ContentMetrics> getUnderperformingContent() {
        return underperformingContent;
    }

    public List<AIAnalyticsInsight> getInsights() {
        return insights;
    }

    public List<ContentScore> getContentScores() {
        return contentScores;
    }

    public Map<String, Object> getSummaryData() {
        return summaryData;
    }

    public String getWeeklyReportJson() {
        return weeklyReportJson;
    }

    public String getMonthlyReportJson() {
        return monthlyReportJson;
    }

    public List<ChartData> getPageViewsChartData() {
        return pageViewsChartData;
    }

    public List<ChartData> getEngagementChartData() {
        return engagementChartData;
    }

    public List<ChartData> getScoreDistributionData() {
        return scoreDistributionData;
    }

    public boolean isReady() {
        return topPerformingContent != null && !topPerformingContent.isEmpty();
    }

    public static class ChartData {
        private final String label;
        private final double value;

        public ChartData(String label, double value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public double getValue() {
            return value;
        }
    }
}