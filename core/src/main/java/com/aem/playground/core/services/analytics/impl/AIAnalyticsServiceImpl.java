package com.aem.playground.core.services.analytics;

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component(service = com.aem.playground.core.services.analytics.AIAnalyticsService.class, immediate = true)
public class AIAnalyticsServiceImpl implements com.aem.playground.core.services.analytics.AIAnalyticsService {

    private static final Logger LOG = LoggerFactory.getLogger(AIAnalyticsServiceImpl.class);

    @Reference
    private AIService aiService;

    @Override
    public List<AIAnalyticsInsight> generateInsights(List<ContentMetrics> metrics) {
        List<AIAnalyticsInsight> insights = new ArrayList<>();

        insights.addAll(analyzePerformance(metrics));
        insights.addAll(analyzeEngagement(metrics));
        insights.addAll(analyzeSEO(metrics));
        insights.addAll(generateRecommendations(metrics));

        LOG.info("Generated {} AI insights from {} content items", insights.size(), metrics.size());
        return insights;
    }

    @Override
    public List<AIAnalyticsInsight> generateInsightsByType(List<ContentMetrics> metrics, String insightType) {
        List<AIAnalyticsInsight> allInsights = generateInsights(metrics);

        return allInsights.stream()
                .filter(i -> i.getInsightType().equals(insightType))
                .collect(Collectors.toList());
    }

    @Override
    public AIAnalyticsInsight generateRecommendation(ContentMetrics metrics) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following content performance metrics and provide a recommendation:\n");
        prompt.append("Content: ").append(metrics.getContentTitle()).append("\n");
        prompt.append("Path: ").append(metrics.getContentPath()).append("\n");
        prompt.append("Page Views: ").append(metrics.getPageViews()).append("\n");
        prompt.append("Unique Visitors: ").append(metrics.getUniqueVisitors()).append("\n");
        prompt.append("Avg Time on Page: ").append(metrics.getAvgTimeOnPage()).append(" seconds\n");
        prompt.append("Bounce Rate: ").append(metrics.getBounceRate()).append("\n");
        prompt.append("Conversions: ").append(metrics.getConversionCount()).append("\n");
        prompt.append("\nProvide a specific, actionable recommendation to improve this content's performance.");

        String aiRecommendation = callAI(prompt.toString());

        return AIAnalyticsInsight.builder()
                .insightId(UUID.randomUUID().toString())
                .insightType(AIAnalyticsInsight.TYPE_RECOMMENDATION)
                .title("Recommendation for " + metrics.getContentTitle())
                .description("AI-generated recommendation based on performance metrics")
                .recommendation(aiRecommendation)
                .confidence(0.85)
                .affectedPages(Arrays.asList(metrics.getContentPath()))
                .category(AIAnalyticsInsight.CATEGORY_IMPROVEMENT)
                .priority(1)
                .build();
    }

    @Override
    public String generateSummaryReport(List<ContentMetrics> metrics) {
        long totalViews = metrics.stream().mapToLong(ContentMetrics::getPageViews).sum();
        long totalVisitors = metrics.stream().mapToLong(ContentMetrics::getUniqueVisitors).sum();
        long totalConversions = metrics.stream().mapToLong(ContentMetrics::getConversionCount).sum();
        double avgBounceRate = metrics.stream().mapToDouble(ContentMetrics::getBounceRate).average().orElse(0.0);

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a summary report for the following content performance:\n");
        prompt.append("Total Page Views: ").append(totalViews).append("\n");
        prompt.append("Total Unique Visitors: ").append(totalVisitors).append("\n");
        prompt.append("Total Conversions: ").append(totalConversions).append("\n");
        prompt.append("Average Bounce Rate: ").append(String.format("%.2f", avgBounceRate)).append("\n");
        prompt.append("Content Count: ").append(metrics.size()).append("\n");
        prompt.append("\nProvide key insights and recommendations for improvement.");

        return callAI(prompt.toString());
    }

    @Override
    public List<AIAnalyticsInsight> analyzeTrends(List<ContentMetrics> historicalMetrics, List<ContentMetrics> currentMetrics) {
        List<AIAnalyticsInsight> trendInsights = new ArrayList<>();

        for (ContentMetrics current : currentMetrics) {
            ContentMetrics historical = findHistoricalMatch(historicalMetrics, current.getContentPath());

            if (historical != null) {
                double viewChange = calculateChange(historical.getPageViews(), current.getPageViews());
                double conversionChange = calculateChange(historical.getConversionCount(), current.getConversionCount());

                if (Math.abs(viewChange) > 0.2) {
                    String trend = viewChange > 0 ? "increasing" : "decreasing";
                    String category = viewChange > 0 ? AIAnalyticsInsight.CATEGORY_TRENDING : AIAnalyticsInsight.CATEGORY_RISK;

                    AIAnalyticsInsight insight = AIAnalyticsInsight.builder()
                            .insightId(UUID.randomUUID().toString())
                            .insightType(AIAnalyticsInsight.TYPE_TREND)
                            .title("Trend: " + current.getContentTitle() + " is " + trend)
                            .description(String.format("Page views have changed by %.1f%%", viewChange * 100))
                            .recommendation(viewChange > 0
                                    ? "Continue optimizing this content as it shows positive growth."
                                    : "Review and update this content to reverse the declining trend.")
                            .confidence(Math.min(0.95, Math.abs(viewChange) + 0.5))
                            .affectedPages(Arrays.asList(current.getContentPath()))
                            .category(category)
                            .priority(viewChange < 0 ? 1 : 2)
                            .build();

                    trendInsights.add(insight);
                }
            }
        }

        LOG.info("Analyzed trends: {} insights generated", trendInsights.size());
        return trendInsights;
    }

    private List<AIAnalyticsInsight> analyzePerformance(List<ContentMetrics> metrics) {
        List<AIAnalyticsInsight> insights = new ArrayList<>();

        for (ContentMetrics m : metrics) {
            if (m.getPageViews() > 5000) {
                insights.add(AIAnalyticsInsight.builder()
                        .insightId(UUID.randomUUID().toString())
                        .insightType(AIAnalyticsInsight.TYPE_PERFORMANCE)
                        .title("High performer: " + m.getContentTitle())
                        .description("This content has high page views (" + m.getPageViews() + ")")
                        .recommendation("Consider promoting this content similarly across other pages.")
                        .confidence(0.9)
                        .affectedPages(Arrays.asList(m.getContentPath()))
                        .category(AIAnalyticsInsight.CATEGORY_TRENDING)
                        .priority(2)
                        .build());
            } else if (m.getPageViews() < 500) {
                insights.add(AIAnalyticsInsight.builder()
                        .insightId(UUID.randomUUID().toString())
                        .insightType(AIAnalyticsInsight.TYPE_PERFORMANCE)
                        .title("Low performer: " + m.getContentTitle())
                        .description("This content has low page views (" + m.getPageViews() + ")")
                        .recommendation("Review content quality and promotion strategy.")
                        .confidence(0.85)
                        .affectedPages(Arrays.asList(m.getContentPath()))
                        .category(AIAnalyticsInsight.CATEGORY_IMPROVEMENT)
                        .priority(1)
                        .build());
            }
        }

        return insights;
    }

    private List<AIAnalyticsInsight> analyzeEngagement(List<ContentMetrics> metrics) {
        List<AIAnalyticsInsight> insights = new ArrayList<>();

        for (ContentMetrics m : metrics) {
            if (m.getAvgTimeOnPage() > 180) {
                insights.add(AIAnalyticsInsight.builder()
                        .insightId(UUID.randomUUID().toString())
                        .insightType(AIAnalyticsInsight.TYPE_ENGAGEMENT)
                        .title("High engagement: " + m.getContentTitle())
                        .description("Users spend significantly more time on this page (avg " + m.getAvgTimeOnPage() + " seconds)")
                        .recommendation("Use this content as a model for other pages.")
                        .confidence(0.85)
                        .affectedPages(Arrays.asList(m.getContentPath()))
                        .category(AIAnalyticsInsight.CATEGORY_TRENDING)
                        .priority(2)
                        .build());
            }

            if (m.getBounceRate() > 0.7) {
                insights.add(AIAnalyticsInsight.builder()
                        .insightId(UUID.randomUUID().toString())
                        .insightType(AIAnalyticsInsight.TYPE_ENGAGEMENT)
                        .title("High bounce rate: " + m.getContentTitle())
                        .description("High bounce rate (" + String.format("%.1f%%", m.getBounceRate() * 100) + ") indicates users leave quickly")
                        .recommendation("Improve content relevance and add internal links.")
                        .confidence(0.8)
                        .affectedPages(Arrays.asList(m.getContentPath()))
                        .category(AIAnalyticsInsight.CATEGORY_RISK)
                        .priority(1)
                        .build());
            }
        }

        return insights;
    }

    private List<AIAnalyticsInsight> analyzeSEO(List<ContentMetrics> metrics) {
        List<AIAnalyticsInsight> insights = new ArrayList<>();

        insights.add(AIAnalyticsInsight.builder()
                .insightId(UUID.randomUUID().toString())
                .insightType(AIAnalyticsInsight.TYPE_SEO)
                .title("SEO Opportunities Identified")
                .description("Several pages show potential for SEO improvement")
                .recommendation("Optimize meta titles and descriptions for underperforming pages.")
                .confidence(0.75)
                .category(AIAnalyticsInsight.CATEGORY_OPPORTUNITY)
                .priority(2)
                .build());

        return insights;
    }

    private List<AIAnalyticsInsight> generateRecommendations(List<ContentMetrics> metrics) {
        return metrics.stream()
                .filter(m -> m.getPageViews() < 1000)
                .limit(5)
                .map(this::generateRecommendation)
                .collect(Collectors.toList());
    }

    private String callAI(String prompt) {
        try {
            AIGenerationOptions options = AIGenerationOptions.builder()
                .maxTokens(500)
                .build();
            AIService.AIGenerationResult result = aiService.generateText(prompt, options);

            if (result.isSuccess()) {
                return result.getContent();
            }
        } catch (Exception e) {
            LOG.warn("AI service call failed, using fallback", e);
        }

        return generateFallbackRecommendation();
    }

    private String generateFallbackRecommendation() {
        return "Based on the analytics data, we recommend reviewing content structure and " +
               "optimizing for user engagement. Focus on creating compelling titles and " +
               "ensuring content provides value to increase time on page.";
    }

    private ContentMetrics findHistoricalMatch(List<ContentMetrics> historical, String path) {
        return historical.stream()
                .filter(m -> m.getContentPath().equals(path))
                .findFirst()
                .orElse(null);
    }

    private double calculateChange(long oldValue, long newValue) {
        if (oldValue == 0) return newValue > 0 ? 1.0 : 0.0;
        return ((double) newValue - oldValue) / oldValue;
    }
}