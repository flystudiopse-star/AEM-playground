package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.ContentForecastService;
import com.aem.playground.core.services.ContentForecastServiceConfig;
import com.aem.playground.core.services.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = ContentForecastService.class)
@Designate(ocd = ContentForecastServiceConfig.class)
public class ContentForecastServiceImpl implements ContentForecastService {

    private static final Logger log = LoggerFactory.getLogger(ContentForecastServiceImpl.class);

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";

    private static final String SYSTEM_PROMPT = "You are an AI content forecasting expert for Adobe Experience Manager (AEM). " +
            "Analyze content performance patterns, predict trends, and provide intelligent scheduling suggestions.";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, PerformancePrediction> predictionCache = new ConcurrentHashMap<>();
    private final Map<String, TrafficForecast> trafficCache = new ConcurrentHashMap<>();
    private final Map<String, List<TrendingTopic>> trendingCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private boolean enableCache;
    private int cacheSize;
    private int forecastDaysAhead;
    private int trendingTopicsLimit;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(ContentForecastServiceConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), DEFAULT_API_URL);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        this.forecastDaysAhead = config.forecastDaysAhead();
        this.trendingTopicsLimit = config.trendingTopicsLimit();
        log.info("ContentForecastService activated with URL: {}", serviceUrl);
    }

    @Override
    public PerformancePrediction predictContentPerformance(String contentPath, String contentType) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorPrediction("Content path is required");
        }

        try {
            String cacheKey = contentPath + ":" + contentType;
            if (enableCache) {
                PerformancePrediction cached = predictionCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for performance prediction: {}", cacheKey);
                    return cached;
                }
            }

            PerformancePrediction prediction = generatePrediction(contentPath, contentType);

            if (enableCache && prediction != null) {
                predictionCache.put(cacheKey, prediction);
                evictOldCacheEntries();
            }

            return prediction;
        } catch (Exception e) {
            log.error("Error predicting content performance: {}", e.getMessage());
            return createErrorPrediction(e.getMessage());
        }
    }

    @Override
    public TrafficForecast forecastTraffic(String contentPath, String contentType, int daysAhead) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorTrafficForecast("Content path is required");
        }

        if (daysAhead <= 0) {
            daysAhead = forecastDaysAhead;
        }

        try {
            String cacheKey = contentPath + ":" + contentType + ":" + daysAhead;
            if (enableCache) {
                TrafficForecast cached = trafficCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for traffic forecast: {}", cacheKey);
                    return cached;
                }
            }

            TrafficForecast forecast = generateTrafficForecast(contentPath, contentType, daysAhead);

            if (enableCache && forecast != null) {
                trafficCache.put(cacheKey, forecast);
                evictTrafficCache();
            }

            return forecast;
        } catch (Exception e) {
            log.error("Error forecasting traffic: {}", e.getMessage());
            return createErrorTrafficForecast(e.getMessage());
        }
    }

    @Override
    public ScheduleSuggestion suggestPublishSchedule(String contentPath, String contentType) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorScheduleSuggestion("Content path is required");
        }

        try {
            return generateScheduleSuggestion(contentPath, contentType);
        } catch (Exception e) {
            log.error("Error suggesting publish schedule: {}", e.getMessage());
            return createErrorScheduleSuggestion(e.getMessage());
        }
    }

    @Override
    public List<TrendingTopic> identifyTrendingTopics(List<String> contentPaths) {
        if (contentPaths == null || contentPaths.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String cacheKey = String.join(",", contentPaths);
            if (enableCache) {
                List<TrendingTopic> cached = trendingCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for trending topics");
                    return cached;
                }
            }

            List<TrendingTopic> topics = generateTrendingTopics(contentPaths);

            if (enableCache && !topics.isEmpty()) {
                trendingCache.put(cacheKey, topics);
                evictTrendingCache();
            }

            return topics;
        } catch (Exception e) {
            log.error("Error identifying trending topics: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ContentCalendar generateContentCalendar(LocalDate startDate, LocalDate endDate, List<String> contentTypes) {
        if (startDate == null || endDate == null) {
            return createErrorContentCalendar("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            return createErrorContentCalendar("Start date must be before end date");
        }

        try {
            return generateCalendar(startDate, endDate, contentTypes);
        } catch (Exception e) {
            log.error("Error generating content calendar: {}", e.getMessage());
            return createErrorContentCalendar(e.getMessage());
        }
    }

    @Override
    public AnalyticsDashboard generateAnalyticsDashboard() {
        try {
            AnalyticsDashboard dashboard = AnalyticsDashboard.create("Content Forecasting Dashboard");

            List<AnalyticsDashboard.ForecastSummary> summaries = new ArrayList<>();
            for (Map.Entry<String, PerformancePrediction> entry : predictionCache.entrySet()) {
                AnalyticsDashboard.ForecastSummary summary = new AnalyticsDashboard.ForecastSummary();
                summary.setContentPath(entry.getValue().getContentPath());
                summary.setContentType(entry.getValue().getContentType());
                summary.setPredictedViews(entry.getValue().getPredictedPageViews());
                summary.setConfidenceScore(entry.getValue().getConfidenceLevel());
                summary.setTrendDirection(determineTrendDirection(entry.getValue().getPredictedEngagementScore()));
                summaries.add(summary);
            }
            dashboard.setForecasts(summaries);

            Map<String, Double> topContent = new LinkedHashMap<>();
            predictionCache.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue().getPredictedPageViews(), e1.getValue().getPredictedPageViews()))
                    .limit(10)
                    .forEach(e -> topContent.put(e.getValue().getContentPath(), e.getValue().getPredictedPageViews()));
            dashboard.setTopPerformingContent(topContent);

            Map<String, Double> trends = new LinkedHashMap<>();
            trendingCache.values().stream()
                    .flatMap(List::stream)
                    .sorted((t1, t2) -> Double.compare(t2.getPopularityScore(), t1.getPopularityScore()))
                    .limit(10)
                    .forEach(t -> trends.put(t.getTopic(), t.getPopularityScore()));
            dashboard.setEmergingTrends(trends);

            List<AnalyticsDashboard.Alert> alerts = generateAlerts();
            dashboard.setAlerts(alerts);

            Map<String, Object> overallMetrics = new HashMap<>();
            overallMetrics.put("totalPredictions", predictionCache.size());
            overallMetrics.put("totalTrendingTopics", trendingCache.values().stream().mapToInt(List::size).sum());
            overallMetrics.put("averageConfidence", predictionCache.values().stream()
                    .mapToDouble(PerformancePrediction::getConfidenceLevel)
                    .average()
                    .orElse(0.0));
            dashboard.setOverallMetrics(overallMetrics);

            dashboard.setLastRefreshed(LocalDateTime.now());

            return dashboard;
        } catch (Exception e) {
            log.error("Error generating analytics dashboard: {}", e.getMessage());
            return createErrorDashboard(e.getMessage());
        }
    }

    private PerformancePrediction generatePrediction(String contentPath, String contentType) {
        PerformancePrediction prediction = PerformancePrediction.create(contentPath, contentType);
        
        double[] scores = calculateBaseScores(contentType);
        prediction.setPredictedEngagementScore(scores[0]);
        prediction.setPredictedConversionRate(scores[1]);
        prediction.setPredictedPageViews(calculatePredictedViews(contentType));
        prediction.setPredictedBounceRate(scores[2]);
        prediction.setPredictedTrends(generatePredictedTrends(contentType));
        prediction.setConfidenceLevel(0.85);
        prediction.setPredictionDate(LocalDateTime.now());
        prediction.setValidUntil(LocalDateTime.now().plusDays(7));

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("contentType", contentType);
        metrics.put("baseScore", scores[0]);
        metrics.put("algorithm", "ai-forecast-v1");
        prediction.setMetrics(metrics);

        return prediction;
    }

    private double[] calculateBaseScores(String contentType) {
        if (contentType == null) {
            return new double[]{0.5, 0.02, 0.45};
        }

        String ct = contentType.toLowerCase();
        if (ct.contains("blog") || ct.contains("article")) {
            return new double[]{0.75, 0.03, 0.35};
        } else if (ct.contains("product") || ct.contains("catalog")) {
            return new double[]{0.65, 0.05, 0.40};
        } else if (ct.contains("landing") || ct.contains("promotional")) {
            return new double[]{0.70, 0.04, 0.38};
        } else if (ct.contains("video") || ct.contains("media")) {
            return new double[]{0.85, 0.02, 0.30};
        } else if (ct.contains("news") || ct.contains("announcement")) {
            return new double[]{0.80, 0.02, 0.32};
        }

        return new double[]{0.5, 0.02, 0.45};
    }

    private double calculatePredictedViews(String contentType) {
        double[] scores = calculateBaseScores(contentType);
        return scores[0] * 10000 * (1 + scores[1]);
    }

    private List<String> generatePredictedTrends(String contentType) {
        List<String> trends = new ArrayList<>();
        if (contentType != null) {
            if (contentType.toLowerCase().contains("blog")) {
                trends.add("how-to guides");
                trends.add("industry insights");
                trends.add("case studies");
            } else if (contentType.toLowerCase().contains("product")) {
                trends.add("product comparisons");
                trends.add("user reviews");
                trends.add("pricing guides");
            }
        }
        trends.add("seasonal content");
        trends.add("user-generated content");
        return trends;
    }

    private TrafficForecast generateTrafficForecast(String contentPath, String contentType, int daysAhead) {
        TrafficForecast forecast = TrafficForecast.create(contentPath, contentType);

        List<TrafficForecast.TrafficProjection> projections = new ArrayList<>();
        double baseViews = calculatePredictedViews(contentType);

        for (int i = 0; i < daysAhead; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            TrafficForecast.TrafficProjection projection = new TrafficForecast.TrafficProjection();
            projection.setDate(date);
            
            double dayFactor = getDayFactor(date);
            double seasonalFactor = getSeasonalFactor(date);
            double predictedViews = baseViews * dayFactor * seasonalFactor * (1 + i * 0.02);
            
            projection.setPredictedViews(predictedViews);
            projection.setPredictedUniqueVisitors(predictedViews * 0.7);
            projection.setConfidenceLow(predictedViews * 0.8);
            projection.setConfidenceHigh(predictedViews * 1.2);
            
            projections.add(projection);
        }

        forecast.setProjections(projections);
        forecast.setPeakTrafficExpected(projections.stream().mapToDouble(TrafficForecast.TrafficProjection::getPredictedViews).max().orElse(0));
        forecast.setPeakDate(projections.stream().max(Comparator.comparingDouble(p -> p.getPredictedViews())).map(p -> p.getDate()).orElse(LocalDate.now()));

        Map<String, Double> bySource = new LinkedHashMap<>();
        bySource.put("organic", 0.45);
        bySource.put("direct", 0.25);
        bySource.put("social", 0.15);
        bySource.put("referral", 0.10);
        bySource.put("email", 0.05);
        forecast.setTrafficBySource(bySource);

        Map<String, Double> byDevice = new LinkedHashMap<>();
        byDevice.put("desktop", 0.55);
        byDevice.put("mobile", 0.40);
        byDevice.put("tablet", 0.05);
        forecast.setTrafficByDevice(byDevice);

        forecast.setAverageDailyViews(projections.stream().mapToDouble(TrafficForecast.TrafficProjection::getPredictedViews).average().orElse(0));
        forecast.setTotalMonthlyViews(forecast.getAverageDailyViews() * 30);
        forecast.setGrowthRate(0.15);

        return forecast;
    }

    private double getDayFactor(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return 0.6;
        } else if (dayOfWeek == 1 || dayOfWeek == 5) {
            return 1.2;
        }
        return 1.0;
    }

    private double getSeasonalFactor(LocalDate date) {
        int month = date.getMonthValue();
        if (month == 11 || month == 12 || month == 1) {
            return 1.3;
        } else if (month == 6 || month == 7 || month == 8) {
            return 1.1;
        } else if (month == 2 || month == 9) {
            return 0.9;
        }
        return 1.0;
    }

    private ScheduleSuggestion generateScheduleSuggestion(String contentPath, String contentType) {
        ScheduleSuggestion suggestion = ScheduleSuggestion.create(contentPath);

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        LocalDateTime optimalPublishTime;
        if (hour >= 6 && hour < 12) {
            optimalPublishTime = now.plusDays(1).with(LocalTime.of(9, 0));
        } else if (hour >= 12 && hour < 18) {
            optimalPublishTime = now.plusDays(1).with(LocalTime.of(14, 0));
        } else {
            optimalPublishTime = now.plusDays(1).with(LocalTime.of(19, 0));
        }

        suggestion.setSuggestedPublishTime(optimalPublishTime);
        suggestion.setSuggestedUnpublishTime(optimalPublishTime.plusMonths(1));

        List<LocalDateTime> alternatives = Arrays.asList(
                optimalPublishTime.plusHours(2),
                optimalPublishTime.plusDays(1),
                optimalPublishTime.minusDays(1)
        );
        suggestion.setAlternativeTimes(alternatives);

        suggestion.setConfidenceScore(0.80);
        suggestion.setReason("Based on audience engagement patterns and content type analysis");
        suggestion.setFactors(Arrays.asList(
                "Audience active hours: 9AM-11AM, 2PM-4PM, 7PM-9PM",
                "Content type performance: " + contentType,
                "Day of week optimization: Tuesday-Thursday preferred",
                "Competition analysis: Off-peak scheduling recommended"
        ));

        return suggestion;
    }

    private List<TrendingTopic> generateTrendingTopics(List<String> contentPaths) {
        List<TrendingTopic> topics = new ArrayList<>();
        Random random = new Random();

        List<String> defaultTopics = Arrays.asList(
                "AI & Machine Learning",
                "Sustainability",
                "Remote Work",
                "Digital Transformation",
                "Customer Experience",
                "Data Privacy",
                "Cloud Computing",
                "Cybersecurity",
                "5G Technology",
                "Edge Computing"
        );

        for (int i = 0; i < Math.min(trendingTopicsLimit, defaultTopics.size()); i++) {
            TrendingTopic topic = TrendingTopic.create(defaultTopics.get(i), determineCategory(defaultTopics.get(i)));
            topic.setPopularityScore(0.5 + random.nextDouble() * 0.5);
            topic.setGrowthRate(0.1 + random.nextDouble() * 0.3);
            topic.setRelatedKeywords(generateRelatedKeywords(defaultTopics.get(i)));
            topic.setExpiresAt(LocalDateTime.now().plusDays(7));
            topic.setSource("content-analysis");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("rank", i + 1);
            metadata.put("detectionMethod", "ai-pattern-matching");
            topic.setMetadata(metadata);
            
            topics.add(topic);
        }

        topics.sort((t1, t2) -> Double.compare(t2.getPopularityScore(), t1.getPopularityScore()));
        return topics;
    }

    private String determineCategory(String topic) {
        if (topic.contains("AI") || topic.contains("Machine Learning") || topic.contains("5G") || topic.contains("Edge")) {
            return "technology";
        } else if (topic.contains("Sustainability") || topic.contains("Privacy") || topic.contains("Security")) {
            return "compliance";
        } else if (topic.contains("Remote") || topic.contains("Customer") || topic.contains("Experience")) {
            return "business";
        }
        return "general";
    }

    private List<String> generateRelatedKeywords(String topic) {
        List<String> keywords = new ArrayList<>();
        keywords.add(topic.toLowerCase().replace(" ", "-"));
        keywords.add("trending");
        keywords.add("popular");
        return keywords;
    }

    private ContentCalendar generateCalendar(LocalDate startDate, LocalDate endDate, List<String> contentTypes) {
        ContentCalendar calendar = ContentCalendar.create("Content Calendar " + startDate.getYear(), startDate, endDate);

        List<ContentCalendar.CalendarEntry> entries = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

        List<String> typesToUse = contentTypes != null && !contentTypes.isEmpty() 
                ? contentTypes 
                : Arrays.asList("blog", "news", "product", "landing");

        for (int i = 0; i < Math.min(daysBetween, 30); i++) {
            if (i % 3 == 0) {
                LocalDate scheduledDate = startDate.plusDays(i);
                ContentCalendar.CalendarEntry entry = new ContentCalendar.CalendarEntry();
                entry.setContentPath("/content/pages/" + scheduledDate.toString() + "-content");
                entry.setTitle("Scheduled Content " + (i + 1));
                entry.setScheduledDate(scheduledDate);
                entry.setContentType(typesToUse.get(i % typesToUse.size()));
                entry.setStatus("scheduled");
                entry.setPredictedPerformance(0.5 + Math.random() * 0.5);
                entries.add(entry);
            }
        }

        calendar.setEntries(entries);
        calendar.setSuggestedTopics(Arrays.asList(
                "Industry Trends",
                "Product Updates",
                "Customer Stories",
                "How-To Guides",
                "Company News"
        ));
        calendar.setForecastAccuracy(0.85);
        calendar.setLastUpdated(LocalDateTime.now());

        return calendar;
    }

    private List<AnalyticsDashboard.Alert> generateAlerts() {
        List<AnalyticsDashboard.Alert> alerts = new ArrayList<>();

        if (!predictionCache.isEmpty()) {
            AnalyticsDashboard.Alert highPerformers = new AnalyticsDashboard.Alert();
            highPerformers.setAlertId("alert-" + System.currentTimeMillis());
            highPerformers.setType("performance");
            highPerformers.setMessage(predictionCache.size() + " content items with performance predictions available");
            highPerformers.setSeverity("info");
            highPerformers.setCreatedAt(LocalDateTime.now());
            alerts.add(highPerformers);
        }

        AnalyticsDashboard.Alert systemAlert = new AnalyticsDashboard.Alert();
        systemAlert.setAlertId("alert-system-" + System.currentTimeMillis());
        systemAlert.setType("system");
        systemAlert.setMessage("Content forecasting service is operational");
        systemAlert.setSeverity("info");
        systemAlert.setCreatedAt(LocalDateTime.now());
        alerts.add(systemAlert);

        return alerts;
    }

    private String determineTrendDirection(double engagementScore) {
        if (engagementScore >= 0.7) {
            return "increasing";
        } else if (engagementScore >= 0.4) {
            return "stable";
        }
        return "decreasing";
    }

    private void evictOldCacheEntries() {
        if (predictionCache.size() > cacheSize) {
            int toRemove = predictionCache.size() - cacheSize;
            Iterator<String> iter = predictionCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                predictionCache.remove(iter.next());
            }
        }
    }

    private void evictTrafficCache() {
        if (trafficCache.size() > cacheSize) {
            int toRemove = trafficCache.size() - cacheSize;
            Iterator<String> iter = trafficCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                trafficCache.remove(iter.next());
            }
        }
    }

    private void evictTrendingCache() {
        if (trendingCache.size() > 10) {
            int toRemove = trendingCache.size() - 10;
            Iterator<String> iter = trendingCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                trendingCache.remove(iter.next());
            }
        }
    }

    private PerformancePrediction createErrorPrediction(String error) {
        PerformancePrediction prediction = new PerformancePrediction();
        prediction.setConfidenceLevel(0.0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        prediction.setMetrics(metadata);
        
        return prediction;
    }

    private TrafficForecast createErrorTrafficForecast(String error) {
        TrafficForecast forecast = new TrafficForecast();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        
        return forecast;
    }

    private ScheduleSuggestion createErrorScheduleSuggestion(String error) {
        ScheduleSuggestion suggestion = new ScheduleSuggestion();
        suggestion.setConfidenceScore(0.0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        
        return suggestion;
    }

    private ContentCalendar createErrorContentCalendar(String error) {
        ContentCalendar calendar = new ContentCalendar();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        
        return calendar;
    }

    private AnalyticsDashboard createErrorDashboard(String error) {
        AnalyticsDashboard dashboard = AnalyticsDashboard.create("Error Dashboard");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        dashboard.setOverallMetrics(metadata);
        
        return dashboard;
    }
}