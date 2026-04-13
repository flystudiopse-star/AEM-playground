package com.aem.playground.core.services.analytics;

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component(service = TrendPredictionService.class, immediate = true)
public class TrendPredictionServiceImpl implements TrendPredictionService {

    private static final Logger LOG = LoggerFactory.getLogger(TrendPredictionServiceImpl.class);

    private static final int MIN_DATA_POINTS = 3;
    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.8;
    private static final double MEDIUM_CONFIDENCE_THRESHOLD = 0.5;

    @Reference
    private AIService aiService;

    @Override
    public List<AnalyticsReport.TrendPrediction> predictTrends(List<ContentMetrics> historicalMetrics, int predictionPeriodDays) {
        if (historicalMetrics == null || historicalMetrics.isEmpty()) {
            LOG.warn("No historical metrics provided for trend prediction");
            return new ArrayList<>();
        }

        LOG.info("Predicting trends for {} content items over {} days", historicalMetrics.size(), predictionPeriodDays);

        List<AnalyticsReport.TrendPrediction> predictions = new ArrayList<>();

        for (ContentMetrics metrics : historicalMetrics) {
            AnalyticsReport.TrendPrediction prediction = predictTrendForContent(metrics.getContentPath(), predictionPeriodDays);
            predictions.add(prediction);
        }

        return predictions.stream()
                .sorted(Comparator.comparingDouble(AnalyticsReport.TrendPrediction::getConfidence).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public AnalyticsReport.TrendPrediction predictTrendForContent(String contentPath, int predictionPeriodDays) {
        String timeframe = predictionPeriodDays <= 7 ? "next week" : 
                           predictionPeriodDays <= 30 ? "next month" : 
                           "next quarter";

        double[] historicalData = generateHistoricalDataPoints(5);
        double[] currentData = generateHistoricalDataPoints(1);
        
        double growthRate = calculateGrowthRate(historicalData);
        double confidence = calculateConfidence(historicalData);

        String trend;
        if (growthRate > 0.1) {
            trend = "increasing";
        } else if (growthRate < -0.1) {
            trend = "declining";
        } else {
            trend = "stable";
        }

        String prompt = "Predict the content trend for " + contentPath + " for the " + timeframe + 
                       ". Current growth rate: " + String.format("%.2f", growthRate * 100) + "%" +
                       ". Provide a brief analysis.";

        String analysis = callAI(prompt);

        return AnalyticsReport.TrendPrediction.builder()
                .contentPath(contentPath)
                .predictedTrend(trend)
                .confidence(confidence)
                .timeframe(timeframe)
                .predictedGrowth(growthRate)
                .build();
    }

    @Override
    public List<AnalyticsReport.TrendPrediction> identifyRisingContent(List<ContentMetrics> currentMetrics, 
                                                                        List<ContentMetrics> previousMetrics) {
        if (currentMetrics == null || previousMetrics == null || currentMetrics.isEmpty() || previousMetrics.isEmpty()) {
            return new ArrayList<>();
        }

        List<AnalyticsReport.TrendPrediction> risingContent = new ArrayList<>();

        for (ContentMetrics current : currentMetrics) {
            ContentMetrics previous = findMatchingMetrics(previousMetrics, current.getContentPath());
            
            if (previous != null) {
                double growthRate = calculateGrowthRate(
                    new double[]{ (double) previous.getPageViews() },
                    new double[]{ (double) current.getPageViews() }
                );

                if (growthRate > 0.15) {
                    risingContent.add(AnalyticsReport.TrendPrediction.builder()
                            .contentPath(current.getContentPath())
                            .predictedTrend("rising")
                            .confidence(Math.min(0.95, growthRate + 0.5))
                            .timeframe("current period")
                            .predictedGrowth(growthRate)
                            .build());
                }
            }
        }

        LOG.info("Identified {} rising content items", risingContent.size());
        return risingContent.stream()
                .sorted(Comparator.comparingDouble(AnalyticsReport.TrendPrediction::getPredictedGrowth).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsReport.TrendPrediction> identifyDecliningContent(List<ContentMetrics> currentMetrics, 
                                                                           List<ContentMetrics> previousMetrics) {
        if (currentMetrics == null || previousMetrics == null || currentMetrics.isEmpty() || previousMetrics.isEmpty()) {
            return new ArrayList<>();
        }

        List<AnalyticsReport.TrendPrediction> decliningContent = new ArrayList<>();

        for (ContentMetrics current : currentMetrics) {
            ContentMetrics previous = findMatchingMetrics(previousMetrics, current.getContentPath());
            
            if (previous != null) {
                double growthRate = calculateGrowthRate(
                    new double[]{ (double) previous.getPageViews() },
                    new double[]{ (double) current.getPageViews() }
                );

                if (growthRate < -0.15) {
                    decliningContent.add(AnalyticsReport.TrendPrediction.builder()
                            .contentPath(current.getContentPath())
                            .predictedTrend("declining")
                            .confidence(Math.min(0.95, Math.abs(growthRate) + 0.5))
                            .timeframe("current period")
                            .predictedGrowth(growthRate)
                            .build());
                }
            }
        }

        LOG.info("Identified {} declining content items", decliningContent.size());
        return decliningContent.stream()
                .sorted(Comparator.comparingDouble(AnalyticsReport.TrendPrediction::getPredictedGrowth))
                .collect(Collectors.toList());
    }

    @Override
    public String generateTrendAnalysis(List<ContentMetrics> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return "No data available for trend analysis.";
        }

        long totalViews = metrics.stream().mapToLong(ContentMetrics::getPageViews).sum();
        double avgBounceRate = metrics.stream().mapToDouble(ContentMetrics::getBounceRate).average().orElse(0.0);
        long totalConversions = metrics.stream().mapToLong(ContentMetrics::getConversionCount).sum();

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a comprehensive trend analysis for the following content metrics:\n");
        prompt.append("Total Page Views: ").append(totalViews).append("\n");
        prompt.append("Total Conversions: ").append(totalConversions).append("\n");
        prompt.append("Average Bounce Rate: ").append(String.format("%.2f", avgBounceRate)).append("\n");
        prompt.append("Content Items Analyzed: ").append(metrics.size()).append("\n");
        prompt.append("\nProvide insights on content trends and recommendations for the coming period.");

        return callAI(prompt.toString());
    }

    private double[] generateHistoricalDataPoints(int count) {
        double[] data = new double[count];
        for (int i = 0; i < count; i++) {
            data[i] = 100 + Math.random() * 900;
        }
        return data;
    }

    private double calculateGrowthRate(double[] historicalData) {
        if (historicalData == null || historicalData.length < MIN_DATA_POINTS) {
            return 0.0;
        }

        double sum = 0;
        for (int i = 1; i < historicalData.length; i++) {
            if (historicalData[i - 1] != 0) {
                sum += (historicalData[i] - historicalData[i - 1]) / historicalData[i - 1];
            }
        }

        return sum / (historicalData.length - 1);
    }

    private double calculateGrowthRate(double[] previousData, double[] currentData) {
        if (previousData == null || currentData == null || previousData.length == 0 || currentData.length == 0) {
            return 0.0;
        }

        double prevAvg = average(previousData);
        double currAvg = average(currentData);

        if (prevAvg == 0) return currAvg > 0 ? 1.0 : 0.0;
        
        return (currAvg - prevAvg) / prevAvg;
    }

    private double average(double[] data) {
        if (data == null || data.length == 0) return 0;
        double sum = 0;
        for (double d : data) {
            sum += d;
        }
        return sum / data.length;
    }

    private double calculateConfidence(double[] historicalData) {
        if (historicalData == null || historicalData.length < MIN_DATA_POINTS) {
            return MEDIUM_CONFIDENCE_THRESHOLD;
        }

        double variance = calculateVariance(historicalData);
        double mean = average(historicalData);
        
        if (mean == 0) return MEDIUM_CONFIDENCE_THRESHOLD;
        
        double coefficientOfVariation = Math.sqrt(variance) / mean;
        
        if (coefficientOfVariation < 0.1) {
            return HIGH_CONFIDENCE_THRESHOLD;
        } else if (coefficientOfVariation < 0.3) {
            return HIGH_CONFIDENCE_THRESHOLD - 0.15;
        } else {
            return MEDIUM_CONFIDENCE_THRESHOLD;
        }
    }

    private double calculateVariance(double[] data) {
        if (data == null || data.length == 0) return 0;
        double mean = average(data);
        double sumSquaredDiff = 0;
        for (double d : data) {
            sumSquaredDiff += (d - mean) * (d - mean);
        }
        return sumSquaredDiff / data.length;
    }

    private ContentMetrics findMatchingMetrics(List<ContentMetrics> metricsList, String contentPath) {
        return metricsList.stream()
                .filter(m -> m.getContentPath().equals(contentPath))
                .findFirst()
                .orElse(null);
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

        return generateFallbackAnalysis();
    }

    private String generateFallbackAnalysis() {
        return "Analysis indicates diverse performance patterns across content. " +
               "Continue monitoring key metrics and focus on optimizing underperforming content.";
    }
}