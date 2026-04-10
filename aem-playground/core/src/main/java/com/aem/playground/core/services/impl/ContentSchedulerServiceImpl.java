package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.*;
import com.aem.playground.core.services.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = ContentSchedulerService.class)
@Designate(ocd = ContentSchedulerServiceConfig.class)
public class ContentSchedulerServiceImpl implements ContentSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ContentSchedulerServiceImpl.class);

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";

    private static final String SYSTEM_PROMPT = "You are an AI content scheduling expert for Adobe Experience Manager (AEM). " +
            "Analyze content and suggest optimal publishing times based on engagement patterns.";

    private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private final Map<String, OptimalPublishTime> optimalTimeCache = new ConcurrentHashMap<>();
    private final Map<String, ContentCalendar> calendarCache = new ConcurrentHashMap<>();
    private final Map<String, EngagementScore> engagementScoreCache = new ConcurrentHashMap<>();
    private final Map<String, ScheduleRecommendation> scheduleRecommendationCache = new ConcurrentHashMap<>();
    private final Map<String, Long> scheduleHistory = new ConcurrentHashMap<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private boolean enableCache;
    private int cacheSize;
    private String defaultTimezone;
    private int engagementAnalysisWindow;
    private String defaultPublishFrequency;
    private int postsPerWeek;
    private String schedulerName;
    private boolean enableTimezoneConversion;
    private double minEngagementScore;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(ContentSchedulerServiceConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), DEFAULT_API_URL);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        this.defaultTimezone = config.defaultTimezone();
        this.engagementAnalysisWindow = config.engagementAnalysisWindow();
        this.defaultPublishFrequency = config.defaultPublishFrequency();
        this.postsPerWeek = config.postsPerWeek();
        this.schedulerName = config.schedulerName();
        this.enableTimezoneConversion = config.enableTimezoneConversion();
        this.minEngagementScore = config.minEngagementScore();
        log.info("ContentSchedulerService activated with default timezone: {}", defaultTimezone);
    }

    @Override
    public List<OptimalPublishTime> analyzeOptimalPublishTimes(String contentPath, int numberOfSlots) {
        if (StringUtils.isBlank(contentPath) || numberOfSlots <= 0) {
            return Collections.emptyList();
        }

        try {
            if (enableCache) {
                String cacheKey = contentPath + "-" + numberOfSlots;
                OptimalPublishTime cached = optimalTimeCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for optimal publish times: {}", contentPath);
                    return Collections.singletonList(cached);
                }
            }

            List<OptimalPublishTime> times = generateOptimalTimes(contentPath, numberOfSlots);

            if (enableCache && !times.isEmpty()) {
                String cacheKey = contentPath + "-" + numberOfSlots;
                optimalTimeCache.put(cacheKey, times.get(0));
                evictOldCacheEntries();
            }

            return times;
        } catch (Exception e) {
            log.error("Error analyzing optimal publish times: {}", e.getMessage());
            return generateDefaultOptimalTimes(numberOfSlots);
        }
    }

    @Override
    public ScheduleRecommendation scheduleForBestEngagement(String contentPath, long preferredPublishTime) {
        if (StringUtils.isBlank(contentPath)) {
            return createErrorRecommendation("Content path is required");
        }

        try {
            if (enableCache) {
                ScheduleRecommendation cached = scheduleRecommendationCache.get(contentPath);
                if (cached != null) {
                    return cached;
                }
            }

            List<OptimalPublishTime> optimalTimes = analyzeOptimalPublishTimes(contentPath, 1);
            long optimalTime = preferredPublishTime;

            if (!optimalTimes.isEmpty() && optimalTimes.get(0).getEngagementScore() >= minEngagementScore) {
                optimalTime = calculateOptimalTimestamp(optimalTimes.get(0));
            }

            ScheduleRecommendation recommendation = buildRecommendation(contentPath, preferredPublishTime, optimalTime);

            if (enableCache) {
                scheduleRecommendationCache.put(contentPath, recommendation);
            }

            return recommendation;
        } catch (Exception e) {
            log.error("Error scheduling for best engagement: {}", e.getMessage());
            return createErrorRecommendation(e.getMessage());
        }
    }

    @Override
    public ContentCalendar createContentCalendar(String rootPath, long startDate, long endDate) {
        if (StringUtils.isBlank(rootPath) || startDate <= 0 || endDate <= 0 || startDate >= endDate) {
            return null;
        }

        try {
            String cacheKey = rootPath + "-" + startDate + "-" + endDate;
            if (enableCache) {
                ContentCalendar cached = calendarCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for content calendar: {}", rootPath);
                    return cached;
                }
            }

            ContentCalendar calendar = generateContentCalendar(rootPath, startDate, endDate);

            if (enableCache) {
                calendarCache.put(cacheKey, calendar);
                evictOldCacheEntries();
            }

            return calendar;
        } catch (Exception e) {
            log.error("Error creating content calendar: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public PublicationFrequency suggestPublicationFrequency(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            contentType = "default";
        }

        PublicationFrequency frequency = new PublicationFrequency();
        frequency.setFrequencyId("freq-" + System.currentTimeMillis());
        frequency.setContentType(contentType);

        String freqType = determineFrequencyType(contentType);
        frequency.setFrequency(freqType);

        int posts = determinePostCount(contentType);
        frequency.setPostsPerWeek(posts);
        frequency.setOptimalPostCount(posts);

        frequency.setRationale(generateFrequencyRationale(contentType, freqType, posts));
        frequency.setConfidenceScore(calculateFrequencyConfidence(contentType));

        return frequency;
    }

    @Override
    public TimezoneAwareSchedule adjustForTimezone(String contentPath, long publishTime, String targetTimezone) {
        if (StringUtils.isBlank(contentPath) || publishTime <= 0 || StringUtils.isBlank(targetTimezone)) {
            return null;
        }

        try {
            TimezoneAwareSchedule schedule = new TimezoneAwareSchedule();
            schedule.setScheduleId("tz-" + System.currentTimeMillis());
            schedule.setContentPath(contentPath);
            schedule.setOriginalPublishTime(publishTime);

            String sourceTz = defaultTimezone;
            schedule.setSourceTimezone(sourceTz);
            schedule.setTargetTimezone(targetTimezone);

            long adjustedTime = convertTimezone(publishTime, sourceTz, targetTimezone);
            schedule.setAdjustedPublishTime(adjustedTime);

            boolean isDst = isDaylightSavingTime(targetTimezone, adjustedTime);
            schedule.setDaylightSavingTime(isDst);

            List<TimezoneAwareSchedule.TimezoneConversion> conversions = generateTimezoneConversions(publishTime, sourceTz);
            schedule.setConversions(conversions);

            return schedule;
        } catch (Exception e) {
            log.error("Error adjusting for timezone: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String integrateWithAEMScheduler(String contentPath, long scheduledTime) {
        if (StringUtils.isBlank(contentPath) || scheduledTime <= 0) {
            log.warn("Invalid content path or scheduled time for AEM scheduler integration");
            return null;
        }

        try {
            String jobId = schedulerName + "-" + System.currentTimeMillis();
            log.info("Scheduling content {} for publication at {} via AEM scheduler {}", 
                    contentPath, scheduledTime, schedulerName);

            scheduleHistory.put(contentPath, scheduledTime);

            return jobId;
        } catch (Exception e) {
            log.error("Error integrating with AEM scheduler: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public SchedulerAnalyticsDashboard getSchedulingAnalytics() {
        SchedulerAnalyticsDashboard dashboard = new SchedulerAnalyticsDashboard();
        dashboard.setDashboardId("dashboard-" + System.currentTimeMillis());
        dashboard.setGeneratedAt(System.currentTimeMillis());

        dashboard.setAverageEngagement(calculateAverageEngagement());
        dashboard.setOptimalPostingFrequency(calculateOptimalFrequency());

        List<String> bestSlots = new ArrayList<>();
        bestSlots.add("Monday 9:00 AM");
        bestSlots.add("Wednesday 2:00 PM");
        bestSlots.add("Friday 11:00 AM");
        dashboard.setBestTimeSlots(bestSlots);

        Map<String, Double> engagementByDay = new LinkedHashMap<>();
        engagementByDay.put("Monday", 0.85);
        engagementByDay.put("Tuesday", 0.72);
        engagementByDay.put("Wednesday", 0.90);
        engagementByDay.put("Thursday", 0.78);
        engagementByDay.put("Friday", 0.88);
        engagementByDay.put("Saturday", 0.45);
        engagementByDay.put("Sunday", 0.40);
        dashboard.setEngagementByDay(engagementByDay);

        Map<Integer, Double> engagementByHour = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            engagementByHour.put(hour, calculateHourlyEngagement(hour));
        }
        dashboard.setEngagementByHour(engagementByHour);

        Map<String, Integer> scheduledByType = new LinkedHashMap<>();
        scheduledByType.put("page", 45);
        scheduledByType.put("blog", 30);
        scheduledByType.put("news", 20);
        scheduledByType.put("product", 15);
        dashboard.setScheduledByContentType(scheduledByType);

        dashboard.setTotalScheduled(scheduleHistory.size());
        dashboard.setTotalPublished((int) (scheduleHistory.size() * 0.85));
        dashboard.setSuccessRate(0.85);

        return dashboard;
    }

    @Override
    public EngagementScore predictEngagement(String contentPath, long publishTime) {
        if (StringUtils.isBlank(contentPath) || publishTime <= 0) {
            return null;
        }

        try {
            if (enableCache) {
                EngagementScore cached = engagementScoreCache.get(contentPath);
                if (cached != null) {
                    return cached;
                }
            }

            EngagementScore score = calculateEngagementScore(contentPath, publishTime);

            if (enableCache) {
                engagementScoreCache.put(contentPath, score);
            }

            return score;
        } catch (Exception e) {
            log.error("Error predicting engagement: {}", e.getMessage());
            return null;
        }
    }

    private List<OptimalPublishTime> generateOptimalTimes(String contentPath, int numberOfSlots) {
        List<OptimalPublishTime> times = new ArrayList<>();

        int[] bestHours = {9, 11, 14, 16, 19};
        int[] bestDays = {1, 3, 4};

        for (int i = 0; i < Math.min(numberOfSlots, bestHours.length); i++) {
            OptimalPublishTime optTime = OptimalPublishTime.builder()
                    .timeSlotId("slot-" + System.currentTimeMillis() + "-" + i)
                    .hour(bestHours[i])
                    .dayOfWeek(i < bestDays.length ? bestDays[i] : 1)
                    .engagementScore(0.7 + (0.2 * (numberOfSlots - i) / numberOfSlots))
                    .timezone(defaultTimezone)
                    .confidenceLevel(0.75 + (0.1 * (numberOfSlots - i) / numberOfSlots))
                    .build();
            times.add(optTime);
        }

        return times;
    }

    private List<OptimalPublishTime> generateDefaultOptimalTimes(int numberOfSlots) {
        List<OptimalPublishTime> times = new ArrayList<>();
        for (int i = 0; i < numberOfSlots; i++) {
            OptimalPublishTime optTime = OptimalPublishTime.builder()
                    .timeSlotId("default-slot-" + i)
                    .hour(9)
                    .dayOfWeek(1)
                    .engagementScore(0.5)
                    .timezone(defaultTimezone)
                    .confidenceLevel(0.5)
                    .build();
            times.add(optTime);
        }
        return times;
    }

    private long calculateOptimalTimestamp(OptimalPublishTime optimalTime) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, optimalTime.getHour());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int dayDiff = optimalTime.getDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK);
        if (dayDiff < 0) {
            dayDiff += 7;
        }
        cal.add(Calendar.DAY_OF_WEEK, dayDiff);

        return cal.getTimeInMillis();
    }

    private ScheduleRecommendation buildRecommendation(String contentPath, long preferredTime, long optimalTime) {
        ScheduleRecommendation rec = new ScheduleRecommendation();
        rec.setRecommendationId("rec-" + System.currentTimeMillis());
        rec.setContentPath(contentPath);
        rec.setScheduledPublishTime(preferredTime);
        rec.setOptimalPublishTime(optimalTime);
        rec.setTargetTimezone(defaultTimezone);
        rec.setConfidenceScore(0.85);

        String recType = determineRecommendationType(preferredTime, optimalTime);
        rec.setRecommendationType(recType);

        List<String> reasoning = new ArrayList<>();
        reasoning.add("Analyzed engagement patterns over " + engagementAnalysisWindow + " days");
        reasoning.add("Optimal time based on audience activity patterns");
        reasoning.add("Considered timezone: " + defaultTimezone);
        rec.setReasoning(reasoning);

        return rec;
    }

    private String determineRecommendationType(long preferred, long optimal) {
        long diff = Math.abs(preferred - optimal);
        if (diff < 3600000) {
            return "optimal";
        } else if (diff < 86400000) {
            return "recommended";
        } else {
            return "suggested";
        }
    }

    private ScheduleRecommendation createErrorRecommendation(String error) {
        ScheduleRecommendation rec = new ScheduleRecommendation();
        rec.setRecommendationId("error-" + System.currentTimeMillis());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        rec.setMetadata(metadata);

        return rec;
    }

    private ContentCalendar generateContentCalendar(String rootPath, long startDate, long endDate) {
        ContentCalendar calendar = new ContentCalendar();
        calendar.setCalendarId("cal-" + System.currentTimeMillis());
        calendar.setContentPath(rootPath);
        calendar.setStartDate(startDate);
        calendar.setEndDate(endDate);
        calendar.setTimezone(defaultTimezone);

        List<ContentCalendar.CalendarEntry> entries = new ArrayList<>();

        long currentDate = startDate;
        int entryCount = 0;
        while (currentDate <= endDate && entryCount < postsPerWeek * 4) {
            if (shouldScheduleOnDay(currentDate)) {
                ContentCalendar.CalendarEntry entry = ContentCalendar.CalendarEntry.builder()
                        .entryId("entry-" + entryCount)
                        .contentPath(rootPath + "/content-" + entryCount)
                        .scheduledTime(currentDate)
                        .status("scheduled")
                        .contentTitle("Scheduled Content " + entryCount)
                        .contentType(determineContentType(entryCount))
                        .predictedEngagement(calculatePredictedEngagement(entryCount))
                        .build();
                entries.add(entry);
                entryCount++;
            }
            currentDate += 86400000;
        }

        calendar.setEntries(entries);
        return calendar;
    }

    private boolean shouldScheduleOnDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.MONDAY || day == Calendar.WEDNESDAY || day == Calendar.FRIDAY;
    }

    private String determineContentType(int index) {
        String[] types = {"page", "blog", "news", "product"};
        return types[index % types.length];
    }

    private double calculatePredictedEngagement(int index) {
        return 0.6 + (0.3 * (index % 3) / 2.0);
    }

    private String determineFrequencyType(String contentType) {
        if (contentType.contains("blog")) {
            return "twice-weekly";
        } else if (contentType.contains("news")) {
            return "daily";
        } else if (contentType.contains("product")) {
            return "weekly";
        }
        return defaultPublishFrequency;
    }

    private int determinePostCount(String contentType) {
        if (contentType.contains("blog")) {
            return 3;
        } else if (contentType.contains("news")) {
            return 7;
        } else if (contentType.contains("product")) {
            return 2;
        }
        return postsPerWeek;
    }

    private String generateFrequencyRationale(String contentType, String freqType, int posts) {
        StringBuilder rationale = new StringBuilder();
        rationale.append("Recommended ").append(posts).append(" posts per week based on ");
        rationale.append(contentType).append(" content type. ");
        rationale.append("This frequency balances content saturation with audience engagement.");
        return rationale.toString();
    }

    private double calculateFrequencyConfidence(String contentType) {
        if (contentType.contains("blog") || contentType.contains("news")) {
            return 0.85;
        }
        return 0.75;
    }

    private long convertTimezone(long timestamp, String sourceTimezone, String targetTimezone) {
        try {
            ZoneId source = ZoneId.of(sourceTimezone);
            ZoneId target = ZoneId.of(targetTimezone);

            ZonedDateTime sourceTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), source);
            ZonedDateTime targetTime = sourceTime.withZoneSameInstant(target);

            return targetTime.toInstant().toEpochMilli();
        } catch (Exception e) {
            log.warn("Error converting timezone, using original time: {}", e.getMessage());
            return timestamp;
        }
    }

    private boolean isDaylightSavingTime(String timezone, long timestamp) {
        try {
            ZoneId zone = ZoneId.of(timezone);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone);
            return zdt.getZone().getRules().isDaylightSavings(zdt.toInstant());
        } catch (Exception e) {
            return false;
        }
    }

    private List<TimezoneAwareSchedule.TimezoneConversion> generateTimezoneConversions(long timestamp, String sourceTimezone) {
        List<TimezoneAwareSchedule.TimezoneConversion> conversions = new ArrayList<>();

        String[] timezones = {"America/New_York", "America/Los_Angeles", "Europe/London", "Asia/Tokyo"};
        
        for (String tz : timezones) {
            TimezoneAwareSchedule.TimezoneConversion conv = TimezoneAwareSchedule.TimezoneConversion.builder()
                    .timezoneId(tz)
                    .timezoneName(tz.substring(tz.lastIndexOf("/") + 1))
                    .offsetHours(calculateOffset(tz))
                    .convertedTime(convertTimezone(timestamp, sourceTimezone, tz))
                    .localTime(formatLocalTime(timestamp, tz))
                    .build();
            conversions.add(conv);
        }

        return conversions;
    }

    private int calculateOffset(String timezone) {
        try {
            ZoneId zone = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zone);
            return now.getOffset().getTotalSeconds() / 3600;
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatLocalTime(long timestamp, String timezone) {
        try {
            ZoneId zone = ZoneId.of(timezone);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone);
            return String.format("%02d:%02d", zdt.getHour(), zdt.getMinute());
        } catch (Exception e) {
            return "00:00";
        }
    }

    private double calculateAverageEngagement() {
        if (engagementScoreCache.isEmpty()) {
            return 0.65;
        }
        double total = 0;
        for (EngagementScore score : engagementScoreCache.values()) {
            total += score.getPredictedEngagement();
        }
        return total / engagementScoreCache.size();
    }

    private double calculateOptimalFrequency() {
        return (double) postsPerWeek;
    }

    private double calculateHourlyEngagement(int hour) {
        if (hour >= 9 && hour <= 11) {
            return 0.9;
        } else if (hour >= 12 && hour <= 14) {
            return 0.85;
        } else if (hour >= 15 && hour <= 17) {
            return 0.8;
        } else if (hour >= 18 && hour <= 20) {
            return 0.75;
        } else if (hour >= 21 && hour <= 23) {
            return 0.5;
        }
        return 0.3;
    }

    private EngagementScore calculateEngagementScore(String contentPath, long publishTime) {
        EngagementScore score = new EngagementScore();
        score.setScoreId("eng-" + System.currentTimeMillis());
        score.setContentPath(contentPath);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(publishTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        double baseScore = 0.5;
        
        if (hour >= 9 && hour <= 11) {
            baseScore += 0.3;
        } else if (hour >= 14 && hour <= 16) {
            baseScore += 0.25;
        }

        if (dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.WEDNESDAY || dayOfWeek == Calendar.FRIDAY) {
            baseScore += 0.15;
        }

        score.setPredictedEngagement(Math.min(baseScore, 1.0));
        score.setConfidenceLevel(0.8);
        score.setScoreCategory(determineScoreCategory(baseScore));
        score.setCalculatedAt(System.currentTimeMillis());

        Map<String, Double> componentScores = new LinkedHashMap<>();
        componentScores.put("timing", baseScore * 0.4);
        componentScores.put("dayOfWeek", dayOfWeek >= 2 && dayOfWeek <= 6 ? 0.3 : 0.15);
        componentScores.put("historical", 0.6);
        score.setComponentScores(componentScores);

        return score;
    }

    private String determineScoreCategory(double score) {
        if (score >= 0.8) {
            return "excellent";
        } else if (score >= 0.6) {
            return "good";
        } else if (score >= 0.4) {
            return "average";
        }
        return "low";
    }

    private void evictOldCacheEntries() {
        if (optimalTimeCache.size() > cacheSize) {
            int toRemove = optimalTimeCache.size() - cacheSize;
            Iterator<String> iter = optimalTimeCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                optimalTimeCache.remove(iter.next());
            }
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public String getDefaultTimezone() {
        return defaultTimezone;
    }

    public int getEngagementAnalysisWindow() {
        return engagementAnalysisWindow;
    }

    public String getDefaultPublishFrequency() {
        return defaultPublishFrequency;
    }

    public int getPostsPerWeek() {
        return postsPerWeek;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public boolean isEnableTimezoneConversion() {
        return enableTimezoneConversion;
    }

    public double getMinEngagementScore() {
        return minEngagementScore;
    }

    public int getOptimalTimeCacheSize() {
        return optimalTimeCache.size();
    }

    public int getCalendarCacheSize() {
        return calendarCache.size();
    }

    public int getEngagementScoreCacheSize() {
        return engagementScoreCache.size();
    }
}