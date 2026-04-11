package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIGenerationOptions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContentScoringServiceImplTest {

    @Mock
    private AIService aiService;

    @InjectMocks
    private ContentScoringServiceImpl contentScoringService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
                .thenReturn(AIService.AIGenerationResult.success("AI analysis text", null));
    }

    @Test
    void testScoreContent() {
        ContentScore score = contentScoringService.scoreContent("/content/page1");
        
        assertNotNull(score);
        assertEquals("/content/page1", score.getContentPath());
        assertTrue(score.getOverallScore() >= 0 && score.getOverallScore() <= 100);
        assertTrue(score.getQualityScore() >= 0 && score.getQualityScore() <= 100);
        assertTrue(score.getSeoScore() >= 0 && score.getSeoScore() <= 100);
        assertTrue(score.getEngagementScore() >= 0 && score.getEngagementScore() <= 100);
    }

    @Test
    void testAnalyzeQuality() {
        ContentScore.QualityDetails quality = contentScoringService.analyzeQuality("/content/page1");
        
        assertNotNull(quality);
        assertTrue(quality.getReadabilityScore() >= 0 && quality.getReadabilityScore() <= 100);
        assertTrue(quality.getCompletenessScore() >= 0 && quality.getCompletenessScore() <= 100);
        assertTrue(quality.getFreshnessScore() >= 0 && quality.getFreshnessScore() <= 100);
        assertTrue(quality.getAccuracyScore() >= 0 && quality.getAccuracyScore() <= 100);
    }

    @Test
    void testAnalyzeSEO() {
        ContentScore.SEODetails seo = contentScoringService.analyzeSEO("/content/page1");
        
        assertNotNull(seo);
        assertTrue(seo.getTitleScore() >= 0 && seo.getTitleScore() <= 100);
        assertTrue(seo.getMetaDescriptionScore() >= 0 && seo.getMetaDescriptionScore() <= 100);
        assertTrue(seo.getKeywordScore() >= 0 && seo.getKeywordScore() <= 100);
        assertTrue(seo.getStructureScore() >= 0 && seo.getStructureScore() <= 100);
    }

    @Test
    void testAnalyzeEngagement() {
        ContentScore.EngagementDetails engagement = contentScoringService.analyzeEngagement("/content/page1");
        
        assertNotNull(engagement);
        assertTrue(engagement.getInteractionScore() >= 0 && engagement.getInteractionScore() <= 100);
        assertTrue(engagement.getSocialShareScore() >= 0 && engagement.getSocialShareScore() <= 100);
        assertTrue(engagement.getReturnVisitScore() >= 0 && engagement.getReturnVisitScore() <= 100);
        assertTrue(engagement.getConversionScore() >= 0 && engagement.getConversionScore() <= 100);
    }

    @Test
    void testBatchScoreContent() {
        List<String> paths = new ArrayList<>();
        paths.add("/content/page1");
        paths.add("/content/page2");
        paths.add("/content/page3");

        List<ContentScore> scores = contentScoringService.batchScoreContent(paths);
        
        assertNotNull(scores);
        assertEquals(3, scores.size());
    }

    @Test
    void testOverallScoreCalculation() {
        ContentScore score = contentScoringService.scoreContent("/content/test");
        
        double expectedOverall = (score.getQualityScore() * 0.3 + score.getSeoScore() * 0.35 + 
                               score.getEngagementScore() * 0.35);
        assertEquals(expectedOverall, score.getOverallScore(), 0.01);
    }

    @Test
    void testQualityDetailsHasSubScores() {
        ContentScore.QualityDetails details = contentScoringService.analyzeQuality("/content/page1");
        
        assertNotNull(details.getReadabilityScore());
        assertNotNull(details.getCompletenessScore());
        assertNotNull(details.getFreshnessScore());
        assertNotNull(details.getAccuracyScore());
    }
}