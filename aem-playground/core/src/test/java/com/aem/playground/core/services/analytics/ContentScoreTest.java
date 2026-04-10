package com.aem.playground.core.services.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentScoreTest {

    @Test
    void testBuilderCreatesInstance() {
        ContentScore score = ContentScore.builder()
                .contentPath("/content/page1")
                .overallScore(85.5)
                .qualityScore(80.0)
                .seoScore(90.0)
                .engagementScore(86.0)
                .build();

        assertNotNull(score);
        assertEquals("/content/page1", score.getContentPath());
        assertEquals(85.5, score.getOverallScore(), 0.01);
        assertEquals(80.0, score.getQualityScore(), 0.01);
        assertEquals(90.0, score.getSeoScore(), 0.01);
        assertEquals(86.0, score.getEngagementScore(), 0.01);
    }

    @Test
    void testQualityDetailsBuilder() {
        ContentScore.QualityDetails details = ContentScore.QualityDetails.builder()
                .readabilityScore(85.0)
                .completenessScore(90.0)
                .freshnessScore(75.0)
                .accuracyScore(80.0)
                .build();

        assertNotNull(details);
        assertEquals(85.0, details.getReadabilityScore(), 0.01);
        assertEquals(90.0, details.getCompletenessScore(), 0.01);
        assertEquals(75.0, details.getFreshnessScore(), 0.01);
        assertEquals(80.0, details.getAccuracyScore(), 0.01);
    }

    @Test
    void testSEODetailsBuilder() {
        ContentScore.SEODetails details = ContentScore.SEODetails.builder()
                .titleScore(90.0)
                .metaDescriptionScore(85.0)
                .keywordScore(80.0)
                .structureScore(88.0)
                .build();

        assertNotNull(details);
        assertEquals(90.0, details.getTitleScore(), 0.01);
        assertEquals(85.0, details.getMetaDescriptionScore(), 0.01);
        assertEquals(80.0, details.getKeywordScore(), 0.01);
        assertEquals(88.0, details.getStructureScore(), 0.01);
    }

    @Test
    void testEngagementDetailsBuilder() {
        ContentScore.EngagementDetails details = ContentScore.EngagementDetails.builder()
                .interactionScore(75.0)
                .socialShareScore(60.0)
                .returnVisitScore(80.0)
                .conversionScore(70.0)
                .build();

        assertNotNull(details);
        assertEquals(75.0, details.getInteractionScore(), 0.01);
        assertEquals(60.0, details.getSocialShareScore(), 0.01);
        assertEquals(80.0, details.getReturnVisitScore(), 0.01);
        assertEquals(70.0, details.getConversionScore(), 0.01);
    }

    @Test
    void testScoreWithDetails() {
        ContentScore.QualityDetails quality = ContentScore.QualityDetails.builder()
                .readabilityScore(85.0)
                .completenessScore(90.0)
                .freshnessScore(75.0)
                .accuracyScore(80.0)
                .build();

        ContentScore.SEODetails seo = ContentScore.SEODetails.builder()
                .titleScore(90.0)
                .metaDescriptionScore(85.0)
                .keywordScore(80.0)
                .structureScore(88.0)
                .build();

        ContentScore.EngagementDetails engagement = ContentScore.EngagementDetails.builder()
                .interactionScore(75.0)
                .socialShareScore(60.0)
                .returnVisitScore(80.0)
                .conversionScore(70.0)
                .build();

        ContentScore score = ContentScore.builder()
                .contentPath("/content/page1")
                .overallScore(82.0)
                .qualityScore(82.5)
                .seoScore(85.75)
                .engagementScore(71.25)
                .qualityDetails(quality)
                .seoDetails(seo)
                .engagementDetails(engagement)
                .build();

        assertNotNull(score.getQualityDetails());
        assertNotNull(score.getSeoDetails());
        assertNotNull(score.getEngagementDetails());
    }

    @Test
    void testScoreRange() {
        ContentScore score = ContentScore.builder()
                .contentPath("/content/page1")
                .overallScore(50.0)
                .qualityScore(30.0)
                .seoScore(70.0)
                .engagementScore(50.0)
                .build();

        assertTrue(score.getOverallScore() >= 0 && score.getOverallScore() <= 100);
        assertTrue(score.getQualityScore() >= 0 && score.getQualityScore() <= 100);
        assertTrue(score.getSeoScore() >= 0 && score.getSeoScore() <= 100);
        assertTrue(score.getEngagementScore() >= 0 && score.getEngagementScore() <= 100);
    }
}