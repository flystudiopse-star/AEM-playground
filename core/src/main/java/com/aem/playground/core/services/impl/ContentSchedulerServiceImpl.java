package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.ContentSchedulerConfig;
import com.aem.playground.core.services.ContentSchedulerService;
import com.aem.playground.core.services.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component(service = ContentSchedulerService.class)
@Designate(ocd = ContentSchedulerConfig.class)
public class ContentSchedulerServiceImpl implements ContentSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ContentSchedulerServiceImpl.class);

    private static final String DEFAULT_TIMEZONE = "America/New_York";
    private static final int DEFAULT_PUBLISH_LEAD_TIME = 24;
    private static final int DEFAULT_UNPUBLISH_LEAD_TIME = 30;

    private static final Map<String, int[]> ENGAGEMENT_HOURS = new HashMap<>();
    static {
        ENGAGEMENT_HOURS.put("blog", new int[]{9, 10, 11, 14, 15, 16, 19, 20, 21});
        ENGAGEMENT_HOURS.put("news", new int[]{7, 8, 9, 12, 13, 17, 18});
        ENGAGEMENT_HOURS.put("product", new int[]{10, 11, 12, 14, 15, 19, 20});
        ENGAGEMENT_HOURS.put("landing", new int[]{9, 10, 11, 13, 14, 15});
        ENGAGEMENT_HOURS.put("video", new int[]{12, 13, 18, 19, 20, 21, 22});
    }

    private static final Map<String, int[]> BEST_DAYS = new HashMap<>();
    static {
        BEST_DAYS.put("blog", new int[]{2, 3, 4, 5});
        BEST_DAYS.put("news", new int[]{1, 2, 3, 4, 5});
        BEST_DAYS.put("product", new int[]{2, 3, 4, 5, 6});
        BEST_DAYS.put("landing", new int[]{2, 3, 4});
    }

    private final Map<String, SchedulingAnalysis> analysisCache = new ConcurrentHashMap<>();
    private final Map<String, List<TimeSlotRecommendation>> slotsCache = new ConcurrentHashMap<>();
    private final AtomicLong jobCounter = new AtomicLong(0);

    private String defaultTimezone;
    private boolean enableCache;
    private int cacheSize;
    private int defaultPublishLeadTime;
    private int defaultUnpublishLeadTime;
    private int recommendedSlotsPerDay;
    private double confidenceThreshold;
    private boolean enableAemSchedulerIntegration;

    @Activate
    protected void activate(ContentSchedulerConfig config) {
        this.defaultTimezone = PropertiesUtil.toString(config.defaultTimezone(), DEFAULT_TIMEZONE);
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        this.defaultPublishLeadTime = config.defaultPublishLeadTime();
        this.defaultUnpublishLeadTime = config.defaultUnpublishLeadTime();
        this.recommendedSlotsPerDay = config.recommendedSlotsPerDay();
        this.confidenceThreshold = config.confidenceThreshold();
        this.enableAemSchedulerIntegration = config.enableAemSchedulerIntegration();
        
        log.info("ContentSchedulerService activated with timezone: {}", defaultTimezone);
    }

    @Override
    public SchedulingAnalysis analyzeOptimalPublishTimes(String contentPath, String contentType, String targetTimezone) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorAnalysis("Content path is required");
        }

        try {
            String timezone = StringUtils.isNotBlank(targetTimezone) ? targetTimezone : defaultTimezone;
            String cacheKey = contentPath + ":" + contentType + ":" + timezone;
            
            if (enableCache) {
                SchedulingAnalysis cached = analysisCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for scheduling analysis: {}", cacheKey);
                    return cached;
                }
            }

            SchedulingAnalysis analysis = generateSchedulingAnalysis(contentPath, contentType, timezone);

            if (enableCache) {
                analysisCache.put(cacheKey, analysis);
                evictAnalysisCache();
            }

            return analysis;
        } catch (Exception e) {
            log.error("Error analyzing optimal publish times: {}", e.getMessage());
            return createErrorAnalysis(e.getMessage());
        }
    }

    @Override
    public ScheduleSuggestion scheduleForBestEngagement(String contentPath, String contentType, String targetTimezone) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorScheduleSuggestion("Content path is required");
        }

        try {
            String timezone = StringUtils.isNotBlank(targetTimezone) ? targetTimezone : defaultTimezone;
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);

            int[] bestHours = getEngagementHours(contentType);
            int bestHour = calculateBestHour(bestHours, now.getHour());

            ZonedDateTime optimalTime = now.plusHours(defaultPublishLeadTime)
                    .withHour(bestHour)
                    .withMinute(0)
                    .withSecond(0);

            if (optimalTime.isBefore(now)) {
                optimalTime = optimalTime.plusDays(1);
            }

            ScheduleSuggestion suggestion = ScheduleSuggestion.create(contentPath);
            suggestion.setSuggestedPublishTime(optimalTime.toLocalDateTime());
            suggestion.setSuggestedUnpublishTime(optimalTime.plusDays(defaultUnpublishLeadTime).toLocalDateTime());
            suggestion.setConfidenceScore(0.85);
            suggestion.setReason("Optimal time determined by content type analysis and audience engagement patterns in " + timezone);
            suggestion.setFactors(Arrays.asList(
                    "Best engagement hours: " + Arrays.toString(bestHours),
                    "Content type: " + contentType,
                    "Timezone: " + timezone,
                    "Day of week optimization: " + getBestDaysOfWeek(contentType)
            ));

            List<LocalDateTime> alternatives = new ArrayList<>();
            for (int offset = -1; offset <= 2; offset++) {
                if (offset != 0) {
                    ZonedDateTime altTime = optimalTime.plusDays(offset);
                    alternatives.add(altTime.toLocalDateTime());
                }
            }
            suggestion.setAlternativeTimes(alternatives);

            return suggestion;
        } catch (Exception e) {
            log.error("Error scheduling for best engagement: {}", e.getMessage());
            return createErrorScheduleSuggestion(e.getMessage());
        }
    }

    @Override
    public ContentSchedulerSchedulerResult scheduleContent(String contentPath, ZonedDateTime publishTime, ZonedDateTime unpublishTime) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorScheduleResult("Content path is required");
        }

        if (publishTime == null) {
            return createErrorScheduleResult("Publish time is required");
        }

        try {
            ZonedDateTime now = ZonedDateTime.now(publishTime.getZone());
            if (publishTime.isBefore(now)) {
                return createErrorScheduleResult("Publish time must be in the future");
            }

            ContentSchedulerSchedulerResult result = ContentSchedulerSchedulerResult.create(contentPath, publishTime);
            result.setScheduledUnpublishTime(unpublishTime);

            if (unpublishTime != null && unpublishTime.isBefore(publishTime)) {
                return createErrorScheduleResult("Unpublish time must be after publish time");
            }

            String jobId = "content-scheduler-" + jobCounter.incrementAndGet();
            result.setSchedulerJobId(jobId);
            result.setPredictedEngagement(calculatePredictedEngagement(contentPath, publishTime));

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timezone", publishTime.getZone().getId());
            metadata.put("createdAt", ZonedDateTime.now().toString());
            metadata.put("aemSchedulerIntegrated", enableAemSchedulerIntegration);
            if (enableAemSchedulerIntegration) {
                metadata.put("schedulerJobName", "com.aem.playground.content-scheduler-" + contentPath.hashCode());
            }
            result.setMetadata(metadata);

            log.info("Scheduled content {} for publish at {} with job ID {}", contentPath, publishTime, jobId);
            return result;
        } catch (Exception e) {
            log.error("Error scheduling content: {}", e.getMessage());
            return createErrorScheduleResult(e.getMessage());
        }
    }

    @Override
    public ContentCalendar createContentCalendar(ZonedDateTime startDate, ZonedDateTime endDate, List<String> contentTypes) {
        if (startDate == null || endDate == null) {
            return createErrorCalendar("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            return createErrorCalendar("Start date must be before end date");
        }

        try {
            ContentCalendar calendar = ContentCalendar.create(
                    "AI Content Calendar " + startDate.getYear(),
                    startDate.toLocalDate(),
                    endDate.toLocalDate()
            );

            List<ContentCalendar.CalendarEntry> entries = new ArrayList<>();
            long daysBetween = Duration.between(startDate, endDate).toDays();
            
            List<String> types = contentTypes != null && !contentTypes.isEmpty() 
                    ? contentTypes 
                    : Arrays.asList("blog", "news", "product", "landing");

            Random random = new Random();
            for (int i = 0; i < Math.min(daysBetween, 90); i++) {
                ZonedDateTime scheduledDateTime = startDate.plusDays(i);
                int dayOfWeek = scheduledDateTime.getDayOfWeek().getValue();
                
                if (shouldPublishOnDay(dayOfWeek, types.get(0))) {
                    ContentCalendar.CalendarEntry entry = new ContentCalendar.CalendarEntry();
                    entry.setContentPath("/content/pages/" + scheduledDateTime.toLocalDate().toString() + "-content");
                    entry.setTitle("Scheduled Content " + (i + 1));
                    entry.setScheduledDate(scheduledDateTime.toLocalDate());
                    entry.setContentType(types.get(i % types.size()));
                    entry.setStatus("scheduled");
                    entry.setPredictedPerformance(0.6 + random.nextDouble() * 0.4);
                    entries.add(entry);
                }
            }

            calendar.setEntries(entries);
            calendar.setSuggestedTopics(Arrays.asList(
                    "Industry News",
                    "Product Updates",
                    "How-To Guides",
                    "Case Studies",
                    "Company Announcements"
            ));
            calendar.setForecastAccuracy(0.82);
            calendar.setLastUpdated(java.time.LocalDateTime.now());

            return calendar;
        } catch (Exception e) {
            log.error("Error creating content calendar: {}", e.getMessage());
            return createErrorCalendar(e.getMessage());
        }
    }

    @Override
    public PublicationFrequencySuggestion suggestPublicationFrequency(String contentType, int historicalContentCount) {
        try {
            PublicationFrequencySuggestion suggestion = PublicationFrequencySuggestion.create(contentType);

            int[] frequency = calculatePublicationFrequency(contentType, historicalContentCount);
            suggestion.setPostsPerWeek(frequency[0]);
            suggestion.setPostsPerMonth(frequency[1]);
            suggestion.setRecommendedFrequency(getFrequencyLabel(frequency[0]));
            suggestion.setConfidenceScore(0.80);
            suggestion.setReason("Based on content type analysis and historical publishing patterns");

            suggestion.setBestDays(getBestDaysOfWeekList(contentType));
            suggestion.setPeakHours(getPeakHoursList(contentType));

            Map<String, Object> recommendations = new HashMap<>();
            recommendations.put("optimalTimeBetweenPosts", getOptimalTimeBetweenPosts(contentType));
            recommendations.put("maxDailyPosts", getMaxDailyPosts(contentType));
            recommendations.put("recommendedContentTypes", getRecommendedContentMix(contentType));
            suggestion.setRecommendations(recommendations);

            return suggestion;
        } catch (Exception e) {
            log.error("Error suggesting publication frequency: {}", e.getMessage());
            return createErrorFrequencySuggestion(e.getMessage());
        }
    }

    @Override
    public List<TimeSlotRecommendation> getOptimalTimeSlots(String contentType, int numberOfSlots, String targetTimezone) {
        if (StringUtils.isBlank(contentType)) {
            return Collections.emptyList();
        }

        try {
            String timezone = StringUtils.isNotBlank(targetTimezone) ? targetTimezone : defaultTimezone;
            String cacheKey = contentType + ":" + numberOfSlots + ":" + timezone;
            
            if (enableCache) {
                List<TimeSlotRecommendation> cached = slotsCache.get(cacheKey);
                if (cached != null) {
                    return cached;
                }
            }

            List<TimeSlotRecommendation> slots = generateTimeSlots(contentType, numberOfSlots, timezone);

            if (enableCache) {
                slotsCache.put(cacheKey, slots);
            }

            return slots;
        } catch (Exception e) {
            log.error("Error getting optimal time slots: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private SchedulingAnalysis generateSchedulingAnalysis(String contentPath, String contentType, String timezone) {
        SchedulingAnalysis analysis = SchedulingAnalysis.create(contentPath, contentType, timezone);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        int[] engagementHours = getEngagementHours(contentType);
        int bestHour = calculateBestHour(engagementHours, now.getHour());

        ZonedDateTime optimalPublishTime = now.plusHours(defaultPublishLeadTime)
                .withHour(bestHour)
                .withMinute(0)
                .withSecond(0);

        if (optimalPublishTime.isBefore(now)) {
            optimalPublishTime = optimalPublishTime.plusDays(1);
        }

        analysis.setOptimalPublishTime(optimalPublishTime);
        analysis.setOptimalUnpublishTime(optimalPublishTime.plusDays(defaultUnpublishLeadTime));
        analysis.setConfidenceScore(0.85);
        analysis.setReason("Based on " + contentType + " content performance patterns in " + timezone);

        List<String> factors = Arrays.asList(
                "Peak engagement hours: " + Arrays.toString(engagementHours),
                "Best days: " + getBestDaysOfWeek(contentType),
                "Timezone: " + timezone,
                "Historical performance factor: " + getHistoricalPerformanceFactor(contentType)
        );
        analysis.setFactors(factors);

        List<ZonedDateTime> alternatives = new ArrayList<>();
        for (int dayOffset = -1; dayOffset <= 2; dayOffset++) {
            if (dayOffset != 0) {
                ZonedDateTime altTime = optimalPublishTime.plusDays(dayOffset);
                for (int hourOffset : Arrays.copyOfRange(engagementHours, 0, Math.min(3, engagementHours.length))) {
                    alternatives.add(altTime.withHour(hourOffset));
                }
            }
        }
        analysis.setAlternativeSlots(alternatives);

        Map<String, Double> predictions = new LinkedHashMap<>();
        predictions.put("predictedPageViews", 5000.0 + Math.random() * 5000);
        predictions.put("predictedEngagement", 0.6 + Math.random() * 0.3);
        predictions.put("predictedConversion", 0.02 + Math.random() * 0.03);
        analysis.setEngagementPredictions(predictions);

        return analysis;
    }

    private int[] getEngagementHours(String contentType) {
        if (contentType == null) {
            return new int[]{9, 10, 11, 14, 15, 16, 19, 20, 21};
        }

        String ct = contentType.toLowerCase();
        for (Map.Entry<String, int[]> entry : ENGAGEMENT_HOURS.entrySet()) {
            if (ct.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new int[]{9, 10, 11, 14, 15, 16, 19, 20, 21};
    }

    private int calculateBestHour(int[] hours, int currentHour) {
        for (int hour : hours) {
            if (hour > currentHour) {
                return hour;
            }
        }
        return hours[0];
    }

    private String getBestDaysOfWeek(String contentType) {
        int[] days = getBestDays(contentType);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(DayOfWeek.of(days[i]).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        return sb.toString();
    }

    private List<String> getBestDaysOfWeekList(String contentType) {
        int[] days = getBestDays(contentType);
        List<String> result = new ArrayList<>();
        for (int day : days) {
            result.add(DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
        return result;
    }

    private int[] getBestDays(String contentType) {
        if (contentType == null) {
            return new int[]{2, 3, 4, 5};
        }

        String ct = contentType.toLowerCase();
        for (Map.Entry<String, int[]> entry : BEST_DAYS.entrySet()) {
            if (ct.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new int[]{2, 3, 4, 5};
    }

    private List<String> getPeakHoursList(String contentType) {
        int[] hours = getEngagementHours(contentType);
        List<String> result = new ArrayList<>();
        for (int hour : hours) {
            String amPm = hour >= 12 ? "PM" : "AM";
            int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            result.add(displayHour + ":00 " + amPm);
        }
        return result;
    }

    private int[] calculatePublicationFrequency(String contentType, int historicalCount) {
        int weekly;
        int monthly;

        if (historicalCount > 0) {
            double avgPerWeek = historicalCount / 4.0;
            weekly = (int) Math.round(Math.max(1, Math.min(7, avgPerWeek)));
        } else {
            String ct = contentType != null ? contentType.toLowerCase() : "";
            if (ct.contains("blog")) {
                weekly = 3;
            } else if (ct.contains("news")) {
                weekly = 5;
            } else if (ct.contains("product")) {
                weekly = 2;
            } else if (ct.contains("landing")) {
                weekly = 1;
            } else {
                weekly = 2;
            }
        }

        monthly = weekly * 4;
        return new int[]{weekly, monthly};
    }

    private String getFrequencyLabel(int postsPerWeek) {
        if (postsPerWeek <= 1) return "Weekly";
        if (postsPerWeek <= 3) return "2-3 times per week";
        if (postsPerWeek <= 5) return "Daily";
        return "Multiple times daily";
    }

    private String getOptimalTimeBetweenPosts(String contentType) {
        String ct = contentType != null ? contentType.toLowerCase() : "";
        if (ct.contains("news")) return "1 day";
        if (ct.contains("blog")) return "2 days";
        if (ct.contains("product")) return "1 week";
        return "3 days";
    }

    private int getMaxDailyPosts(String contentType) {
        String ct = contentType != null ? contentType.toLowerCase() : "";
        if (ct.contains("news")) return 3;
        if (ct.contains("blog")) return 2;
        return 1;
    }

    private List<String> getRecommendedContentMix(String contentType) {
        List<String> mix = new ArrayList<>();
        mix.add("60% evergreen content");
        mix.add("30% timely content");
        mix.add("10% promotional content");
        return mix;
    }

    private double getHistoricalPerformanceFactor(String contentType) {
        String ct = contentType != null ? contentType.toLowerCase() : "";
        if (ct.contains("video") || ct.contains("media")) return 0.9;
        if (ct.contains("blog") || ct.contains("article")) return 0.8;
        if (ct.contains("news")) return 0.75;
        if (ct.contains("product")) return 0.7;
        return 0.65;
    }

    private boolean shouldPublishOnDay(int dayOfWeek, String contentType) {
        int[] bestDays = getBestDays(contentType);
        for (int day : bestDays) {
            if (day == dayOfWeek) return true;
        }
        return false;
    }

    private List<TimeSlotRecommendation> generateTimeSlots(String contentType, int numberOfSlots, String timezone) {
        List<TimeSlotRecommendation> slots = new ArrayList<>();
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        int[] engagementHours = getEngagementHours(contentType);
        double baseEngagement = getHistoricalPerformanceFactor(contentType);

        for (int i = 0; i < numberOfSlots; i++) {
            ZonedDateTime slotTime;
            if (i < engagementHours.length) {
                slotTime = now.plusDays(1).withHour(engagementHours[i]).withMinute(0);
            } else {
                slotTime = now.plusDays((i / engagementHours.length) + 1)
                        .withHour(engagementHours[i % engagementHours.length])
                        .withMinute(0);
            }

            TimeSlotRecommendation slot = TimeSlotRecommendation.create(contentType, slotTime);
            slot.setPredictedEngagement(baseEngagement - (i * 0.05) + (Math.random() * 0.1));
            slot.setConfidenceScore(0.90 - (i * 0.05));
            slot.setReason("Based on historical " + contentType + " performance data");
            slot.setAudienceSegments(Arrays.asList("General Audience", "Registered Users"));
            slot.setPeakTime(i < 3);
            slot.setRecurring(true);

            slots.add(slot);
        }

        return slots;
    }

    private double calculatePredictedEngagement(String contentPath, ZonedDateTime publishTime) {
        int hour = publishTime.getHour();
        DayOfWeek day = publishTime.getDayOfWeek();

        double baseScore = 0.5;
        if ((hour >= 9 && hour <= 11) || (hour >= 14 && hour <= 16) || (hour >= 19 && hour <= 21)) {
            baseScore += 0.25;
        }

        if (day == DayOfWeek.TUESDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.THURSDAY) {
            baseScore += 0.15;
        }

        return Math.min(1.0, baseScore);
    }

    private void evictAnalysisCache() {
        if (analysisCache.size() > cacheSize) {
            int toRemove = analysisCache.size() - cacheSize;
            Iterator<String> iter = analysisCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                analysisCache.remove(iter.next());
            }
        }
    }

    private SchedulingAnalysis createErrorAnalysis(String error) {
        SchedulingAnalysis analysis = new SchedulingAnalysis();
        analysis.setConfidenceScore(0.0);
        
        Map<String, Double> predictions = new HashMap<>();
        predictions.put("error", -1.0);
        analysis.setEngagementPredictions(predictions);
        
        return analysis;
    }

    private ScheduleSuggestion createErrorScheduleSuggestion(String error) {
        ScheduleSuggestion suggestion = new ScheduleSuggestion();
        suggestion.setConfidenceScore(0.0);
        return suggestion;
    }

    private ContentSchedulerSchedulerResult createErrorScheduleResult(String error) {
        ContentSchedulerSchedulerResult result = new ContentSchedulerSchedulerResult();
        result.setStatus("failed");
        result.getMetadata().put("error", error);
        return result;
    }

    private ContentCalendar createErrorCalendar(String error) {
        ContentCalendar calendar = new ContentCalendar();
        calendar.setName("Error Calendar");
        return calendar;
    }

    private PublicationFrequencySuggestion createErrorFrequencySuggestion(String error) {
        PublicationFrequencySuggestion suggestion = new PublicationFrequencySuggestion();
        suggestion.setConfidenceScore(0.0);
        suggestion.getRecommendations().put("error", error);
        return suggestion;
    }
}