package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentRecommenderServiceTest {

    @Mock
    private AIService aiService;

    @Mock
    private ContentRecommenderConfig config;

    private ContentRecommenderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentRecommenderServiceImpl();
        Mockito.reset(config);
        when(config.apiKey()).thenReturn("test-api-key");
        when(config.defaultModel()).thenReturn("gpt-4");
        when(config.maxRecommendations()).thenReturn(10);
        when(config.minRelevanceThreshold()).thenReturn(0.5);
        when(config.similarityThreshold()).thenReturn(0.7);
        when(config.maxSimilarUsers()).thenReturn(20);
        when(config.enableCaching()).thenReturn(true);
        when(config.cacheSize()).thenReturn(100);
        when(config.contentAnalysisPrompt()).thenReturn("Analyze content:");
        when(config.enableCollaborativeFiltering()).thenReturn(true);
        when(config.personalizationEnabled()).thenReturn(true);
        when(config.recommendationTtl()).thenReturn(3600);
        service.activate(config);
    }

    @Test
    void testAnalyzeUserBehavior() {
        UserBehaviorProfile profile = service.analyzeUserBehavior("user-123", "session-456");

        assertNotNull(profile);
        assertEquals("user-123", profile.getUserId());
        assertEquals("session-456", profile.getSessionId());
    }

    @Test
    void testAnalyzeUserBehaviorWithNullUserId() {
        UserBehaviorProfile profile = service.analyzeUserBehavior(null, "session-456");

        assertNull(profile);
    }

    @Test
    void testAnalyzeUserBehaviorWithBlankUserId() {
        UserBehaviorProfile profile = service.analyzeUserBehavior("", "session-456");

        assertNull(profile);
    }

    @Test
    void testAnalyzeUserBehaviorProperties() {
        UserBehaviorProfile profile = service.analyzeUserBehavior("user-789", "session-abc");

        assertNotNull(profile.getViewedPages());
        assertNotNull(profile.getLikedContent());
        assertNotNull(profile.getSearchQueries());
        assertNotNull(profile.getTagPreferences());
        assertNotNull(profile.getCategoryPreferences());
        assertTrue(profile.getAverageSessionDuration() >= 0);
        assertTrue(profile.getTotalPageViews() >= 0);
    }

    @Test
    void testAnalyzeUserBehaviorUserSegment() {
        UserBehaviorProfile profile = service.analyzeUserBehavior("user-seg", "session-seg");

        assertNotNull(profile.getUserSegment());
    }

    @Test
    void testGetPersonalizedRecommendations() {
        List<ContentRecommendation> recommendations = service.getPersonalizedRecommendations("user-123", 5);

        assertNotNull(recommendations);
    }

    @Test
    void testGetPersonalizedRecommendationsWithNullUserId() {
        List<ContentRecommendation> recommendations = service.getPersonalizedRecommendations(null, 5);

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testGetPersonalizedRecommendationsMaxResults() {
        List<ContentRecommendation> recommendations = service.getPersonalizedRecommendations("user-max", 3);

        assertTrue(recommendations.size() <= 3);
    }

    @Test
    void testGetPersonalizedRecommendationsReturnsValidContent() {
        List<ContentRecommendation> recommendations = service.getPersonalizedRecommendations("user-valid", 5);

        for (ContentRecommendation rec : recommendations) {
            assertNotNull(rec.getContentPath());
            assertTrue(rec.getRelevanceScore() >= 0.0);
            assertTrue(rec.getRelevanceScore() <= 1.0);
        }
    }

    @Test
    void testCalculateContentAffinity() {
        ContentAffinityScore affinity = service.calculateContentAffinity("user-123", "/content/articles/technology");

        assertNotNull(affinity);
        assertEquals("user-123", affinity.getUserId());
        assertEquals("/content/articles/technology", affinity.getContentPath());
    }

    @Test
    void testCalculateContentAffinityWithNullUserId() {
        ContentAffinityScore affinity = service.calculateContentAffinity(null, "/content/articles/tech");

        assertNull(affinity);
    }

    @Test
    void testCalculateContentAffinityWithNullContentPath() {
        ContentAffinityScore affinity = service.calculateContentAffinity("user-123", null);

        assertNull(affinity);
    }

    @Test
    void testCalculateContentAffinityAffinityScore() {
        service.analyzeUserBehavior("user-affinity", "session-affinity");
        ContentAffinityScore affinity = service.calculateContentAffinity("user-affinity", "/content/articles/test");

        assertNotNull(affinity);
        assertTrue(affinity.getAffinityScore() >= 0.0);
    }

    @Test
    void testCalculateContentAffinityConfidenceScore() {
        ContentAffinityScore affinity = service.calculateContentAffinity("user-conf", "/content/articles/conf");

        assertNotNull(affinity.getConfidenceScore());
        assertTrue(affinity.getConfidenceScore() >= 0.0);
    }

    @Test
    void testCalculateContentAffinityReason() {
        ContentAffinityScore affinity = service.calculateContentAffinity("user-reason", "/content/articles/reason");

        assertNotNull(affinity.getRecommendationReason());
    }

    @Test
    void testSuggestRelatedContent() {
        RelatedContentRecommendation result = service.suggestRelatedContent("/content/articles/tech", 5);

        assertNotNull(result);
        assertEquals("/content/articles/tech", result.getSourceContentPath());
    }

    @Test
    void testSuggestRelatedContentWithNullPath() {
        RelatedContentRecommendation result = service.suggestRelatedContent(null, 5);

        assertNull(result);
    }

    @Test
    void testSuggestRelatedContentWithBlankPath() {
        RelatedContentRecommendation result = service.suggestRelatedContent("", 5);

        assertNull(result);
    }

    @Test
    void testSuggestRelatedContentAlgorithm() {
        RelatedContentRecommendation result = service.suggestRelatedContent("/content/path", 5);

        assertNotNull(result.getAlgorithmUsed());
    }

    @Test
    void testSuggestRelatedContentRecommendations() {
        RelatedContentRecommendation result = service.suggestRelatedContent("/content/recs", 5);

        assertNotNull(result.getRecommendations());
    }

    @Test
    void testSuggestRelatedContentMaxResults() {
        RelatedContentRecommendation result = service.suggestRelatedContent("/content/max", 3);

        assertTrue(result.getRecommendations().size() <= 3);
    }

    @Test
    void testSuggestRelatedContentRelevanceThreshold() {
        RelatedContentRecommendation result = service.suggestRelatedContent("/content/threshold", 10);

        assertTrue(result.getMinimumRelevanceThreshold() >= 0.0);
    }

    @Test
    void testPerformCollaborativeFiltering() {
        CollaborativeFilterResult result = service.performCollaborativeFiltering("user-123", 5);

        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
    }

    @Test
    void testPerformCollaborativeFilteringWithNullUserId() {
        CollaborativeFilterResult result = service.performCollaborativeFiltering(null, 5);

        assertNull(result);
    }

    @Test
    void testPerformCollaborativeFilteringAlgorithmType() {
        CollaborativeFilterResult result = service.performCollaborativeFiltering("user-collab", 5);

        assertNotNull(result.getAlgorithmType());
    }

    @Test
    void testPerformCollaborativeFilteringSimilarUsers() {
        CollaborativeFilterResult result = service.performCollaborativeFiltering("user-sim", 5);

        assertNotNull(result.getSimilarUserIds());
    }

    @Test
    void testPerformCollaborativeFilteringRecommendations() {
        CollaborativeFilterResult result = service.performCollaborativeFiltering("user-recs", 5);

        assertNotNull(result.getCollaborativeRecommendations());
    }

    @Test
    void testPerformCollaborativeFilteringSimilarityScores() {
        CollaborativeFilterResult result = service.performCollaborativeFiltering("user-scores", 5);

        assertNotNull(result.getSimilarityScores());
    }

    @Test
    void testPerformCollaborativeFilteringTotalSimilarUsers() {
        CollaborativeFilterResult result = service.performCollaborativeFiltering("user-total", 5);

        assertTrue(result.getTotalSimilarUsersFound() >= 0);
    }

    @Test
    void testGenerateRecommendationDashboard() {
        RecommendationDashboard dashboard = service.generateRecommendationDashboard();

        assertNotNull(dashboard);
    }

    @Test
    void testGenerateRecommendationDashboardMetrics() {
        RecommendationDashboard dashboard = service.generateRecommendationDashboard();

        assertTrue(dashboard.getTotalRecommendationsGenerated() >= 0);
        assertTrue(dashboard.getTotalUsersServed() >= 0);
        assertTrue(dashboard.getAverageClickThroughRate() >= 0.0);
        assertTrue(dashboard.getAverageConversionRate() >= 0.0);
    }

    @Test
    void testGenerateRecommendationDashboardTopContent() {
        RecommendationDashboard dashboard = service.generateRecommendationDashboard();

        assertNotNull(dashboard.getTopPerformingContent());
    }

    @Test
    void testGenerateRecommendationDashboardSegments() {
        RecommendationDashboard dashboard = service.generateRecommendationDashboard();

        assertNotNull(dashboard.getUserSegmentBreakdowns());
    }

    @Test
    void testGenerateRecommendationDashboardTimeRange() {
        RecommendationDashboard dashboard = service.generateRecommendationDashboard();

        assertNotNull(dashboard.getTimeRange());
    }

    @Test
    void testGenerateRecommendationDashboardPersonalizationMetrics() {
        RecommendationDashboard dashboard = service.generateRecommendationDashboard();

        assertNotNull(dashboard.getPersonalizationMetrics());
    }

    @Test
    void testGenerateRecommendationDashboardCampaigns() {
        RecommendationDashboard dashboard = service.generateRecommendationDashboard();

        assertTrue(dashboard.getActivePersonalizationCampaigns() >= 0);
    }

    @Test
    void testUserBehaviorProfileCreateStatic() {
        UserBehaviorProfile profile = UserBehaviorProfile.create("user-static");

        assertEquals("user-static", profile.getUserId());
        assertNotNull(profile.getLastActiveAt());
    }

    @Test
    void testContentAffinityScoreCreateStatic() {
        ContentAffinityScore score = ContentAffinityScore.create("/content/page", "user-1", 0.85);

        assertEquals("/content/page", score.getContentPath());
        assertEquals("user-1", score.getUserId());
        assertEquals(0.85, score.getAffinityScore());
        assertNotNull(score.getCalculatedAt());
    }

    @Test
    void testRelatedContentRecommendationCreateStatic() {
        RelatedContentRecommendation rec = RelatedContentRecommendation.create("/content/source");

        assertEquals("/content/source", rec.getSourceContentPath());
        assertNotNull(rec.getGeneratedAt());
    }

    @Test
    void testContentRecommendationCreateStatic() {
        ContentRecommendation rec = ContentRecommendation.create("/content/test", "Test Content", 0.9);

        assertEquals("/content/test", rec.getContentPath());
        assertEquals("Test Content", rec.getContentTitle());
        assertEquals(0.9, rec.getRelevanceScore());
    }

    @Test
    void testCollaborativeFilterResultCreateStatic() {
        CollaborativeFilterResult result = CollaborativeFilterResult.create("user-collab");

        assertEquals("user-collab", result.getUserId());
        assertNotNull(result.getCalculatedAt());
    }

    @Test
    void testContentRecommendationGetters() {
        ContentRecommendation rec = new ContentRecommendation();
        rec.setContentPath("/content/page");
        rec.setContentTitle("Test Page");
        rec.setRelevanceScore(0.75);
        rec.setRecommendationType("content-based");
        rec.setContentType("article");
        rec.setRecommendationReason("Matches your interests");

        assertEquals("/content/page", rec.getContentPath());
        assertEquals("Test Page", rec.getContentTitle());
        assertEquals(0.75, rec.getRelevanceScore());
        assertEquals("content-based", rec.getRecommendationType());
        assertEquals("article", rec.getContentType());
        assertEquals("Matches your interests", rec.getRecommendationReason());
    }

    @Test
    void testContentRecommendationWithTags() {
        ContentRecommendation rec = new ContentRecommendation();
        rec.setMatchingTags(Arrays.asList("tech", "business"));

        assertEquals(2, rec.getMatchingTags().size());
        assertEquals("tech", rec.getMatchingTags().get(0));
    }

    @Test
    void testContentRecommendationWithCategories() {
        ContentRecommendation rec = new ContentRecommendation();
        rec.setMatchingCategories(Arrays.asList("articles", "news"));

        assertEquals(2, rec.getMatchingCategories().size());
    }

    @Test
    void testContentRecommendationWithAdditionalProperties() {
        ContentRecommendation rec = new ContentRecommendation();
        Map<String, Object> props = new HashMap<>();
        props.put("author", "Test Author");
        props.put("views", 1000);
        rec.setAdditionalProperties(props);

        assertEquals("Test Author", rec.getAdditionalProperties().get("author"));
        assertEquals(1000, rec.getAdditionalProperties().get("views"));
    }

    @Test
    void testUserBehaviorProfileWithAllProperties() {
        UserBehaviorProfile profile = new UserBehaviorProfile();
        profile.setUserId("user-1");
        profile.setSessionId("session-1");
        profile.setViewedPages(Arrays.asList("/content/page1", "/content/page2"));
        profile.setLikedContent(Arrays.asList("/content/like1"));
        profile.setSearchQueries(Arrays.asList("search query"));
        profile.setTagPreferences(Map.of("tech", 50, "business", 30));
        profile.setCategoryPreferences(Map.of("articles", 40));
        profile.setAverageSessionDuration(300.0);
        profile.setTotalPageViews(10);
        profile.setUserSegment("premium");

        assertEquals("user-1", profile.getUserId());
        assertEquals("session-1", profile.getSessionId());
        assertEquals(2, profile.getViewedPages().size());
        assertEquals(1, profile.getLikedContent().size());
        assertEquals(1, profile.getSearchQueries().size());
        assertEquals(2, profile.getTagPreferences().size());
        assertEquals(1, profile.getCategoryPreferences().size());
        assertEquals(300.0, profile.getAverageSessionDuration());
        assertEquals(10, profile.getTotalPageViews());
        assertEquals("premium", profile.getUserSegment());
    }

    @Test
    void testContentAffinityScoreWithMatchedTags() {
        ContentAffinityScore score = new ContentAffinityScore();
        score.setMatchedTags(Arrays.asList("tech", "innovation"));

        assertEquals(2, score.getMatchedTags().size());
    }

    @Test
    void testContentAffinityScoreWithMatchedCategories() {
        ContentAffinityScore score = new ContentAffinityScore();
        score.setMatchedCategories(Arrays.asList("articles"));

        assertEquals(1, score.getMatchedCategories().size());
    }

    @Test
    void testContentAffinityScoreWithSimilarUsers() {
        ContentAffinityScore score = new ContentAffinityScore();
        score.setSimilarUsersWhoViewed(Arrays.asList("user-1", "user-2"));

        assertEquals(2, score.getSimilarUsersWhoViewed().size());
    }

    @Test
    void testRelatedContentRecommendationWithRecommendations() {
        RelatedContentRecommendation rec = new RelatedContentRecommendation();
        List<ContentRecommendation> recommendations = new ArrayList<>();
        recommendations.add(ContentRecommendation.create("/content/1", "Title 1", 0.9));
        recommendations.add(ContentRecommendation.create("/content/2", "Title 2", 0.8));
        rec.setRecommendations(recommendations);

        assertEquals(2, rec.getRecommendations().size());
    }

    @Test
    void testCollaborativeFilterResultWithAllProperties() {
        CollaborativeFilterResult result = new CollaborativeFilterResult();
        result.setUserId("user-1");
        result.setSimilarUserIds(Arrays.asList("sim-1", "sim-2"));
        result.setCollaborativeRecommendations(new ArrayList<>());
        result.setSimilarityThreshold(0.5);
        result.setMaxSimilarUsers(20);
        result.setAlgorithmType("user-based");
        result.setTotalSimilarUsersFound(2);

        assertEquals("user-1", result.getUserId());
        assertEquals(2, result.getSimilarUserIds().size());
        assertEquals(0.5, result.getSimilarityThreshold());
        assertEquals(20, result.getMaxSimilarUsers());
        assertEquals("user-based", result.getAlgorithmType());
        assertEquals(2, result.getTotalSimilarUsersFound());
    }

    @Test
    void testRecommendationDashboardTopPerformingContent() {
        RecommendationDashboard dashboard = new RecommendationDashboard();
        List<RecommendationDashboard.TopPerformingContent> topContent = new ArrayList<>();

        RecommendationDashboard.TopPerformingContent top = new RecommendationDashboard.TopPerformingContent();
        top.setContentPath("/content/test");
        top.setContentTitle("Test");
        top.setRecommendationCount(100);
        top.setClickThroughRate(0.15);
        topContent.add(top);

        dashboard.setTopPerformingContent(topContent);

        assertEquals(1, dashboard.getTopPerformingContent().size());
        assertEquals("/content/test", dashboard.getTopPerformingContent().get(0).getContentPath());
    }

    @Test
    void testRecommendationDashboardUserSegmentBreakdown() {
        RecommendationDashboard dashboard = new RecommendationDashboard();
        List<RecommendationDashboard.UserSegmentBreakdown> segments = new ArrayList<>();

        RecommendationDashboard.UserSegmentBreakdown seg = new RecommendationDashboard.UserSegmentBreakdown();
        seg.setSegmentName("Premium");
        seg.setUsersCount(50);
        seg.setAvgEngagementScore(0.8);
        seg.setRecommendationsServed(200);
        segments.add(seg);

        dashboard.setUserSegmentBreakdowns(segments);

        assertEquals(1, dashboard.getUserSegmentBreakdowns().size());
        assertEquals("Premium", dashboard.getUserSegmentBreakdowns().get(0).getSegmentName());
    }

    @Test
    void testPersonalizedMultipleUsers() {
        List<ContentRecommendation> recs1 = service.getPersonalizedRecommendations("user-multi-1", 5);
        List<ContentRecommendation> recs2 = service.getPersonalizedRecommendations("user-multi-2", 5);

        assertNotNull(recs1);
        assertNotNull(recs2);
    }

    @Test
    void testContentAffinityMultipleContents() {
        service.analyzeUserBehavior("user-multi", "session-multi");

        ContentAffinityScore aff1 = service.calculateContentAffinity("user-multi", "/content/article1");
        ContentAffinityScore aff2 = service.calculateContentAffinity("user-multi", "/content/article2");

        assertNotNull(aff1);
        assertNotNull(aff2);
    }

    @Test
    void testRelatedContentMultipleSources() {
        RelatedContentRecommendation rec1 = service.suggestRelatedContent("/content/source1", 5);
        RelatedContentRecommendation rec2 = service.suggestRelatedContent("/content/source2", 5);

        assertNotNull(rec1);
        assertNotNull(rec2);
    }

    @Test
    void testCollaborativeMultipleCalls() {
        CollaborativeFilterResult result1 = service.performCollaborativeFiltering("user-call-1", 5);
        CollaborativeFilterResult result2 = service.performCollaborativeFiltering("user-call-2", 5);

        assertNotNull(result1);
        assertNotNull(result2);
    }
}