package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = ContentRecommenderService.class)
@Designate(ocd = ContentRecommenderConfig.class)
public class ContentRecommenderServiceImpl implements ContentRecommenderService {

    private static final Logger log = LoggerFactory.getLogger(ContentRecommenderServiceImpl.class);

    @Reference
    private AIService aiService;

    private String apiKey;
    private String defaultModel;
    private int maxRecommendations;
    private double minRelevanceThreshold;
    private double similarityThreshold;
    private int maxSimilarUsers;
    private boolean enableCaching;
    private int cacheSize;
    private String contentAnalysisPrompt;
    private boolean enableCollaborativeFiltering;
    private boolean personalizationEnabled;
    private int recommendationTtl;

    private final Map<String, CachedRecommendation> recommendationCache = new ConcurrentHashMap<>();
    private final Map<String, UserBehaviorProfile> userBehaviorCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> contentSimilarityIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userContentInteractionIndex = new ConcurrentHashMap<>();

    @Activate
    @Modified
    protected void activate(ContentRecommenderConfig config) {
        this.apiKey = config.apiKey();
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), "gpt-4");
        this.maxRecommendations = config.maxRecommendations();
        this.minRelevanceThreshold = config.minRelevanceThreshold();
        this.similarityThreshold = config.similarityThreshold();
        this.maxSimilarUsers = config.maxSimilarUsers();
        this.enableCaching = config.enableCaching();
        this.cacheSize = config.cacheSize();
        this.contentAnalysisPrompt = PropertiesUtil.toString(config.contentAnalysisPrompt(),
            "Analyze the following content and identify key topics and themes:");
        this.enableCollaborativeFiltering = config.enableCollaborativeFiltering();
        this.personalizationEnabled = config.personalizationEnabled();
        this.recommendationTtl = config.recommendationTtl();
        log.info("ContentRecommenderService activated with maxRecommendations: {}", maxRecommendations);
    }

    @Override
    public UserBehaviorProfile analyzeUserBehavior(String userId, String sessionId) {
        if (StringUtils.isBlank(userId)) {
            log.warn("User ID is required for behavior analysis");
            return null;
        }

        UserBehaviorProfile profile = userBehaviorCache.computeIfAbsent(userId + ":" + sessionId, key -> {
            UserBehaviorProfile newProfile = UserBehaviorProfile.create(userId);
            newProfile.setSessionId(sessionId);
            newProfile.setViewedPages(generateSampleViewedPages(userId));
            newProfile.setLikedContent(generateSampleLikedContent(userId));
            newProfile.setSharedContent(new ArrayList<>());
            newProfile.setSearchQueries(generateSampleSearchQueries(userId));
            newProfile.setTagPreferences(generateSampleTagPreferences(userId));
            newProfile.setCategoryPreferences(generateSampleCategoryPreferences(userId));
            newProfile.setAverageSessionDuration(300.0);
            newProfile.setTotalPageViews(15);
            newProfile.setUserSegment(determineUserSegment(userId));
            return newProfile;
        });

        if (StringUtils.isNotBlank(sessionId)) {
            profile.setSessionId(sessionId);
        }
        profile.setLastActiveAt(LocalDateTime.now());

        return profile;
    }

    @Override
    public List<ContentRecommendation> getPersonalizedRecommendations(String userId, int maxRecs) {
        if (StringUtils.isBlank(userId)) {
            log.warn("User ID is required for personalized recommendations");
            return Collections.emptyList();
        }

        String cacheKey = "rec:" + userId + ":" + maxRecs;
        if (enableCaching) {
            CachedRecommendation cached = recommendationCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                log.debug("Cache hit for personalized recommendations: {}", cacheKey);
                return cached.recommendations;
            }
        }

        List<ContentRecommendation> recommendations = new ArrayList<>();

        UserBehaviorProfile profile = analyzeUserBehavior(userId, null);
        if (profile != null) {
            List<ContentRecommendation> contentBased = generateContentBasedRecommendations(profile, maxRecs);
            recommendations.addAll(contentBased);

            if (enableCollaborativeFiltering) {
                CollaborativeFilterResult collabResult = performCollaborativeFiltering(userId, maxRecs);
                if (collabResult != null && collabResult.getCollaborativeRecommendations() != null) {
                    recommendations.addAll(collabResult.getCollaborativeRecommendations());
                }
            }

            recommendations = recommendations.stream()
                .filter(r -> r.getRelevanceScore() >= minRelevanceThreshold)
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .distinct()
                .limit(maxRecs > 0 ? maxRecs : maxRecommendations)
                .collect(Collectors.toList());
        }

        if (enableCaching) {
            recommendationCache.put(cacheKey, new CachedRecommendation(recommendations, recommendationTtl));
            evictOldCacheEntries();
        }

        return recommendations;
    }

    @Override
    public ContentAffinityScore calculateContentAffinity(String userId, String contentPath) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(contentPath)) {
            log.warn("User ID and content path are required for affinity calculation");
            return null;
        }

        UserBehaviorProfile profile = analyzeUserBehavior(userId, null);
        if (profile == null) {
            return null;
        }

        ContentAffinityScore affinityScore = new ContentAffinityScore();
        affinityScore.setContentPath(contentPath);
        affinityScore.setUserId(userId);
        affinityScore.setCalculatedAt(LocalDateTime.now());

        List<String> matchedTags = new ArrayList<>();
        List<String> matchedCategories = new ArrayList<>();

        if (profile.getTagPreferences() != null) {
            matchedTags.addAll(profile.getTagPreferences().keySet().stream()
                .filter(tag -> contentPath.toLowerCase().contains(tag.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList()));
        }

        if (profile.getCategoryPreferences() != null) {
            matchedCategories.addAll(profile.getCategoryPreferences().keySet().stream()
                .filter(cat -> contentPath.toLowerCase().contains(cat.toLowerCase()))
                .limit(3)
                .collect(Collectors.toList()));
        }

        affinityScore.setMatchedTags(matchedTags);
        affinityScore.setMatchedCategories(matchedCategories);

        double baseScore = 0.0;
        if (!matchedTags.isEmpty()) {
            baseScore += 0.4 * Math.min(1.0, matchedTags.size() / 5.0);
        }
        if (!matchedCategories.isEmpty()) {
            baseScore += 0.3 * Math.min(1.0, matchedCategories.size() / 3.0);
        }
        if (profile.getAverageSessionDuration() > 0) {
            baseScore += 0.2 * Math.min(1.0, profile.getAverageSessionDuration() / 600.0);
        }
        if (profile.getTotalPageViews() > 0) {
            baseScore += 0.1 * Math.min(1.0, profile.getTotalPageViews() / 50.0);
        }

        affinityScore.setAffinityScore(baseScore);
        affinityScore.setConfidenceScore(calculateConfidence(profile));
        affinityScore.setRecommendationReason(generateAffinityReason(matchedTags, matchedCategories, baseScore));

        if (enableCollaborativeFiltering) {
            List<String> similarUsers = findSimilarUsers(userId, 5);
            affinityScore.setSimilarUsersWhoViewed(similarUsers);
        }

        return affinityScore;
    }

    @Override
    public RelatedContentRecommendation suggestRelatedContent(String contentPath, int maxResults) {
        if (StringUtils.isBlank(contentPath)) {
            log.warn("Content path is required for related content suggestion");
            return null;
        }

        RelatedContentRecommendation result = RelatedContentRecommendation.create(contentPath);
        result.setAlgorithmUsed("content-based-filtering");
        result.setMaxRecommendations(maxResults > 0 ? maxResults : maxRecommendations);
        result.setMinimumRelevanceThreshold(minRelevanceThreshold);

        List<ContentRecommendation> relatedContent = new ArrayList<>();

        List<String> similarContent = contentSimilarityIndex.getOrDefault(contentPath, new ArrayList<>());
        if (similarContent.isEmpty()) {
            similarContent = generateSimilarContent(contentPath);
        }

        for (String similarPath : similarContent) {
            ContentRecommendation rec = ContentRecommendation.create(
                similarPath,
                extractTitleFromPath(similarPath),
                calculateRelevanceScore(contentPath, similarPath)
            );
            rec.setRecommendationType("related");
            rec.setMatchingTags(extractTagsFromPath(similarPath));
            rec.setMatchingCategories(extractCategoriesFromPath(similarPath));
            rec.setContentType(determineContentType(similarPath));
            rec.setRecommendationReason("Similar content based on topic and tags");
            relatedContent.add(rec);
        }

        relatedContent = relatedContent.stream()
            .filter(r -> r.getRelevanceScore() >= minRelevanceThreshold)
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(result.getMaxRecommendations())
            .collect(Collectors.toList());

        result.setRecommendations(relatedContent);
        return result;
    }

    @Override
    public CollaborativeFilterResult performCollaborativeFiltering(String userId, int maxResults) {
        if (StringUtils.isBlank(userId) || !enableCollaborativeFiltering) {
            return null;
        }

        CollaborativeFilterResult result = CollaborativeFilterResult.create(userId);
        result.setAlgorithmType("user-based-collaborative-filtering");
        result.setSimilarityThreshold(similarityThreshold);
        result.setMaxSimilarUsers(maxSimilarUsers);

        List<String> similarUsers = findSimilarUsers(userId, maxSimilarUsers);
        result.setSimilarUserIds(similarUsers);
        result.setTotalSimilarUsersFound(similarUsers.size());

        Map<String, Object> similarityScores = new HashMap<>();
        for (String simUser : similarUsers) {
            similarityScores.put(simUser, calculateUserSimilarity(userId, simUser));
        }
        result.setSimilarityScores(similarityScores);

        List<ContentRecommendation> recommendations = new ArrayList<>();
        Set<String> userContent = userContentInteractionIndex.getOrDefault(userId, new HashSet<>());

        for (String simUser : similarUsers) {
            Set<String> simUserContent = userContentInteractionIndex.getOrDefault(simUser, new HashSet<>());
            for (String content : simUserContent) {
                if (!userContent.contains(content)) {
                    ContentRecommendation rec = ContentRecommendation.create(
                        content,
                        extractTitleFromPath(content),
                        calculateCollaborativeScore(userId, simUser, content)
                    );
                    rec.setRecommendationType("collaborative");
                    rec.setRecommendationReason("Users with similar preferences also viewed this content");
                    recommendations.add(rec);
                }
            }
        }

        recommendations = recommendations.stream()
            .filter(r -> r.getRelevanceScore() >= minRelevanceThreshold)
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(maxResults > 0 ? maxResults : maxRecommendations)
            .collect(Collectors.toList());

        result.setCollaborativeRecommendations(recommendations);
        return result;
    }

    @Override
    public RecommendationDashboard generateRecommendationDashboard() {
        RecommendationDashboard dashboard = new RecommendationDashboard();
        dashboard.setGeneratedAt(LocalDateTime.now());
        dashboard.setTimeRange("last-30-days");

        int totalUsers = userBehaviorCache.size();
        dashboard.setTotalUsersServed(totalUsers);
        dashboard.setTotalRecommendationsGenerated(totalUsers * maxRecommendations);

        dashboard.setAverageClickThroughRate(0.15 + Math.random() * 0.1);
        dashboard.setAverageConversionRate(0.05 + Math.random() * 0.05);

        List<RecommendationDashboard.TopPerformingContent> topContent = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            RecommendationDashboard.TopPerformingContent top = new RecommendationDashboard.TopPerformingContent();
            top.setContentPath("/content/articles/article-" + (i + 1));
            top.setContentTitle("Top Article " + (i + 1));
            top.setRecommendationCount(100 + (int)(Math.random() * 400));
            top.setClickThroughRate(0.1 + Math.random() * 0.2);
            topContent.add(top);
        }
        dashboard.setTopPerformingContent(topContent);

        List<RecommendationDashboard.UserSegmentBreakdown> segments = new ArrayList<>();
        String[] segmentNames = {"New Visitors", "Returning", "High Value", "Casual"};
        for (String segmentName : segmentNames) {
            RecommendationDashboard.UserSegmentBreakdown seg = new RecommendationDashboard.UserSegmentBreakdown();
            seg.setSegmentName(segmentName);
            seg.setUsersCount(50 + (int)(Math.random() * 200));
            seg.setAvgEngagementScore(0.3 + Math.random() * 0.7);
            seg.setRecommendationsServed(200 + (int)(Math.random() * 800));
            segments.add(seg);
        }
        dashboard.setUserSegmentBreakdowns(segments);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("personalizationLift", 0.25 + Math.random() * 0.15);
        metrics.put("contentDiversity", 0.7 + Math.random() * 0.2);
        metrics.put("realTimeRecommendations", true);
        dashboard.setPersonalizationMetrics(metrics);
        dashboard.setActivePersonalizationCampaigns(3 + (int)(Math.random() * 5));

        return dashboard;
    }

    private List<ContentRecommendation> generateContentBasedRecommendations(UserBehaviorProfile profile, int maxRecs) {
        List<ContentRecommendation> recommendations = new ArrayList<>();

        Map<String, Integer> tagPrefs = profile.getTagPreferences();
        if (tagPrefs != null) {
            for (Map.Entry<String, Integer> entry : tagPrefs.entrySet()) {
                String contentPath = "/content/articles/" + entry.getKey().toLowerCase().replace(" ", "-");
                ContentRecommendation rec = ContentRecommendation.create(
                    contentPath,
                    "Article about " + entry.getKey(),
                    Math.min(1.0, entry.getValue() / 100.0 * 0.8 + 0.2)
                );
                rec.setRecommendationType("content-based");
                rec.setMatchingTags(Arrays.asList(entry.getKey()));
                rec.setRecommendationReason("Matches your interest in " + entry.getKey());
                recommendations.add(rec);
            }
        }

        Map<String, Integer> catPrefs = profile.getCategoryPreferences();
        if (catPrefs != null) {
            for (Map.Entry<String, Integer> entry : catPrefs.entrySet()) {
                String contentPath = "/content/" + entry.getKey().toLowerCase() + "/recommended";
                ContentRecommendation rec = ContentRecommendation.create(
                    contentPath,
                    entry.getKey() + " Content",
                    Math.min(1.0, entry.getValue() / 100.0 * 0.7 + 0.2)
                );
                rec.setRecommendationType("category-based");
                rec.setMatchingCategories(Arrays.asList(entry.getKey()));
                rec.setRecommendationReason("Popular in " + entry.getKey() + " category");
                recommendations.add(rec);
            }
        }

        return recommendations.stream()
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(maxRecs > 0 ? maxRecs : maxRecommendations)
            .collect(Collectors.toList());
    }

    private List<String> findSimilarUsers(String userId, int maxSimilar) {
        List<String> similarUsers = new ArrayList<>();
        Random random = new Random(userId.hashCode());

        for (int i = 0; i < maxSimilar; i++) {
            similarUsers.add("user-" + (Math.abs(random.nextInt()) % 1000));
        }

        return similarUsers;
    }

    private double calculateUserSimilarity(String user1, String user2) {
        UserBehaviorProfile profile1 = userBehaviorCache.get(user1);
        UserBehaviorProfile profile2 = userBehaviorCache.get(user2);

        if (profile1 == null || profile2 == null) {
            return 0.3 + Math.random() * 0.3;
        }

        double similarity = 0.0;
        if (profile1.getTagPreferences() != null && profile2.getTagPreferences() != null) {
            Set<String> commonTags = new HashSet<>(profile1.getTagPreferences().keySet());
            commonTags.retainAll(profile2.getTagPreferences().keySet());
            similarity += 0.5 * ((double) commonTags.size() / 
                Math.max(1, Math.max(profile1.getTagPreferences().size(), profile2.getTagPreferences().size())));
        }

        if (profile1.getCategoryPreferences() != null && profile2.getCategoryPreferences() != null) {
            Set<String> commonCats = new HashSet<>(profile1.getCategoryPreferences().keySet());
            commonCats.retainAll(profile2.getCategoryPreferences().keySet());
            similarity += 0.5 * ((double) commonCats.size() / 
                Math.max(1, Math.max(profile1.getCategoryPreferences().size(), profile2.getCategoryPreferences().size())));
        }

        return Math.min(1.0, similarity);
    }

    private double calculateCollaborativeScore(String targetUser, String similarUser, String content) {
        double similarity = calculateUserSimilarity(targetUser, similarUser);
        return similarity * (0.5 + Math.random() * 0.5);
    }

    private double calculateConfidence(UserBehaviorProfile profile) {
        double confidence = 0.0;
        if (profile.getTotalPageViews() > 0) {
            confidence += 0.3 * Math.min(1.0, profile.getTotalPageViews() / 20.0);
        }
        if (profile.getAverageSessionDuration() > 0) {
            confidence += 0.3 * Math.min(1.0, profile.getAverageSessionDuration() / 300.0);
        }
        if (profile.getTagPreferences() != null && !profile.getTagPreferences().isEmpty()) {
            confidence += 0.4 * Math.min(1.0, profile.getTagPreferences().size() / 10.0);
        }
        return confidence;
    }

    private String generateAffinityReason(List<String> tags, List<String> categories, double score) {
        if (score > 0.7) {
            return "Strong affinity based on your interests and behavior";
        } else if (score > 0.4) {
            if (!tags.isEmpty()) {
                return "Matches your interest in: " + String.join(", ", tags);
            }
            return "Relevant content based on your category preferences";
        } else {
            return "Popular content that may interest you";
        }
    }

    private List<String> generateSampleViewedPages(String userId) {
        Random random = new Random(userId.hashCode());
        return Arrays.asList(
            "/content/products/category-a",
            "/content/articles/technology-news",
            "/content/blog/industry-trends"
        );
    }

    private List<String> generateSampleLikedContent(String userId) {
        return Arrays.asList(
            "/content/articles/article-1",
            "/content/blog/post-2"
        );
    }

    private List<String> generateSampleSearchQueries(String userId) {
        return Arrays.asList("AEM", "content management", "digital experience");
    }

    private Map<String, Integer> generateSampleTagPreferences(String userId) {
        Map<String, Integer> prefs = new HashMap<>();
        String[] tags = {"Technology", "Business", "Innovation", "Strategy", "Analytics"};
        Random random = new Random(userId.hashCode());
        for (String tag : tags) {
            if (random.nextBoolean()) {
                prefs.put(tag, 50 + random.nextInt(50));
            }
        }
        return prefs;
    }

    private Map<String, Integer> generateSampleCategoryPreferences(String userId) {
        Map<String, Integer> prefs = new HashMap<>();
        String[] categories = {"Articles", "Products", "Blog", "Resources"};
        Random random = new Random(userId.hashCode() + 1);
        for (String cat : categories) {
            if (random.nextBoolean()) {
                prefs.put(cat, 30 + random.nextInt(70));
            }
        }
        return prefs;
    }

    private String determineUserSegment(String userId) {
        Random random = new Random(userId.hashCode());
        String[] segments = {"new-visitor", "returning", "premium", "casual"};
        return segments[random.nextInt(segments.length)];
    }

    private List<String> generateSimilarContent(String contentPath) {
        List<String> similar = new ArrayList<>();
        Random random = new Random(contentPath.hashCode());

        for (int i = 0; i < 5; i++) {
            similar.add(contentPath + "/related-" + (i + 1));
        }

        return similar;
    }

    private String extractTitleFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length > 0) {
            String last = parts[parts.length - 1];
            return last.replace("-", " ").replace("_", " ");
        }
        return "Content";
    }

    private List<String> extractTagsFromPath(String path) {
        return Arrays.asList("technology", "business", "news");
    }

    private List<String> extractCategoriesFromPath(String path) {
        return Arrays.asList("articles");
    }

    private String determineContentType(String path) {
        if (path.contains("article")) return "article";
        if (path.contains("blog")) return "blog";
        if (path.contains("product")) return "product";
        return "page";
    }

    private double calculateRelevanceScore(String source, String target) {
        return 0.5 + Math.random() * 0.5;
    }

    private void evictOldCacheEntries() {
        if (recommendationCache.size() > cacheSize) {
            int toRemove = recommendationCache.size() - cacheSize;
            Iterator<String> iterator = recommendationCache.keySet().iterator();
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                iterator.remove();
            }
        }
    }

    private static class CachedRecommendation {
        final List<ContentRecommendation> recommendations;
        final long createdAt;
        final int ttl;

        CachedRecommendation(List<ContentRecommendation> recommendations, int ttl) {
            this.recommendations = recommendations;
            this.createdAt = System.currentTimeMillis();
            this.ttl = ttl;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > ttl * 1000L;
        }
    }
}