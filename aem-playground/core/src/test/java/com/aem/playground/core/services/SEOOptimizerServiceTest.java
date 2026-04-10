package com.aem.playground.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SEOOptimizerServiceTest {

    @Mock
    private AIService aiService;

    private SEOOptimizerServiceImpl seoOptimizerService;

    @BeforeEach
    void setUp() {
        seoOptimizerService = new SEOOptimizerServiceImpl();
        setField(seoOptimizerService, "aiService", aiService);
        setField(seoOptimizerService, "defaultLanguage", "en");
        setField(seoOptimizerService, "generateSchemaEnabled", true);
        setField(seoOptimizerService, "openGraphEnabled", true);
        setField(seoOptimizerService, "twitterCardsEnabled", true);
        setField(seoOptimizerService, "cacheMaxSize", 100);
    }

    @Test
    void testGenerateMetadataWithValidAIResponse() {
        String pageContent = "This is a test page about renewable energy and solar panels.";
        String pageTitle = "Renewable Energy Solutions";
        String pagePath = "/content/test/renewable-energy";

        String aiResponse = "{\"metaTitle\": \"Renewable Energy Solutions - Eco Power\", " +
                "\"metaDescription\": \"Discover the best renewable energy solutions for your home or business. " +
                "Solar panels and wind energy experts.\", " +
                "\"keywords\": [\"renewable energy\", \"solar panels\", \"wind energy\", \"eco power\"], " +
                "\"ogType\": \"website\"}";

        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
                .thenReturn(AIService.AIGenerationResult.success(aiResponse, null));

        SEOMetadata metadata = seoOptimizerService.generateMetadata(pageContent, pageTitle, pagePath);

        assertNotNull(metadata);
        assertEquals("Renewable Energy Solutions - Eco Power", metadata.getMetaTitle());
        assertNotNull(metadata.getMetaDescription());
        assertTrue(metadata.getKeywords().size() >= 3);
    }

    @Test
    void testGenerateMetadataWithEmptyContent() {
        SEOMetadata metadata = seoOptimizerService.generateMetadata("", "", "/content/test");

        assertNotNull(metadata);
        assertNull(metadata.getMetaTitle());
        assertNull(metadata.getMetaDescription());
    }

    @Test
    void testGenerateMetadataWithNullContent() {
        SEOMetadata metadata = seoOptimizerService.generateMetadata(null, null, null);

        assertNotNull(metadata);
        assertNull(metadata.getMetaTitle());
    }

    @Test
    void testGenerateMetadataWithFallback() {
        String pageContent = "This is a test page about renewable energy and solar panels for homes.";
        String pageTitle = "Renewable Energy Solutions";
        String pagePath = "/content/test/renewable-energy";

        when(aiService.generateText(anyString(), any(AIGenerationOptions.class)))
                .thenReturn(AIService.AIGenerationResult.error("API Error"));

        SEOMetadata metadata = seoOptimizerService.generateMetadata(pageContent, pageTitle, pagePath);

        assertNotNull(metadata);
        assertNotNull(metadata.getMetaTitle());
        assertNotNull(metadata.getMetaDescription());
    }

    @Test
    void testCalculateSeoScoreWithCompleteMetadata() {
        SEOMetadata metadata = new SEOMetadata.Builder()
                .metaTitle("Test Title for SEO Optimization")
                .metaDescription("This is a comprehensive test description for SEO optimization purposes.")
                .keywords(Arrays.asList("test", "seo", "optimization", "metadata"))
                .ogTitle("Test Title for SEO Optimization")
                .ogDescription("This is a comprehensive test description for SEO optimization purposes.")
                .twitterCard("summary_large_image")
                .schemaOrgJsonLd("{\"@context\":\"https://schema.org\",\"@type\":\"WebPage\"}")
                .build();

        SEOMetadata result = seoOptimizerService.calculateSeoScore(metadata);

        assertTrue(result.getSeoScore() >= 70);
        assertFalse(result.getSeoRecommendations().isEmpty());
    }

    @Test
    void testCalculateSeoScoreWithMissingMetadata() {
        SEOMetadata metadata = new SEOMetadata();

        SEOMetadata result = seoOptimizerService.calculateSeoScore(metadata);

        assertEquals(0, result.getSeoScore());
        assertTrue(result.getSeoRecommendations().size() >= 5);
    }

    @Test
    void testCalculateSeoScoreWithShortTitle() {
        SEOMetadata metadata = new SEOMetadata.Builder()
                .metaTitle("Short")
                .build();

        SEOMetadata result = seoOptimizerService.calculateSeoScore(metadata);

        assertTrue(result.getSeoRecommendations().stream()
                .anyMatch(r -> r.contains("too short")));
    }

    @Test
    void testCalculateSeoScoreWithLongDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("word");
        }
        
        SEOMetadata metadata = new SEOMetadata.Builder()
                .metaTitle("Test Title for SEO Optimization")
                .metaDescription(sb.toString())
                .keywords(Arrays.asList("test", "seo", "optimization"))
                .build();

        SEOMetadata result = seoOptimizerService.calculateSeoScore(metadata);

        assertTrue(result.getSeoRecommendations().stream()
                .anyMatch(r -> r.contains("too long")));
    }

    @Test
    void testGenerateSchemaOrgJsonLd() {
        String jsonLd = seoOptimizerService.generateSchemaOrgJsonLd(
                "Test Page Title",
                "Test page description",
                "https://example.com/test",
                "WebPage"
        );

        assertNotNull(jsonLd);
        assertTrue(jsonLd.contains("schema.org"));
        assertTrue(jsonLd.contains("Test Page Title"));
        assertTrue(jsonLd.contains("Test page description"));
    }

    @Test
    void testGenerateSchemaOrgJsonLdWithNullValues() {
        String jsonLd = seoOptimizerService.generateSchemaOrgJsonLd(null, null, null, null);

        assertNotNull(jsonLd);
        assertTrue(jsonLd.contains("@type"));
    }

    @Test
    void testGenerateOpenGraphMetadata() {
        String pageContent = "<title>Test Page</title><meta name=\"description\" content=\"Test description\">";
        String pageTitle = "Test Page";
        String pageUrl = "https://example.com/test";

        SEOMetadata metadata = seoOptimizerService.generateOpenGraphMetadata(pageContent, pageTitle, pageUrl);

        assertNotNull(metadata);
        assertEquals("Test Page", metadata.getOgTitle());
        assertEquals("Test description", metadata.getOgDescription());
        assertEquals("https://example.com/test", metadata.getOgUrl());
        assertEquals("website", metadata.getOgType());
    }

    @Test
    void testGenerateOpenGraphMetadataWithEmptyContent() {
        SEOMetadata metadata = seoOptimizerService.generateOpenGraphMetadata("", "Test Page", "https://example.com/test");

        assertNotNull(metadata);
        assertEquals("Test Page", metadata.getOgTitle());
    }

    @Test
    void testGenerateTwitterCardMetadata() {
        String pageContent = "<meta name=\"description\" content=\"Test description\">";
        String pageTitle = "Test Page";
        String pageUrl = "https://example.com/test";

        SEOMetadata metadata = seoOptimizerService.generateTwitterCardMetadata(pageContent, pageTitle, pageUrl);

        assertNotNull(metadata);
        assertEquals("summary_large_image", metadata.getTwitterCard());
        assertEquals("Test Page", metadata.getTwitterTitle());
        assertEquals("Test description", metadata.getTwitterDescription());
    }

    @Test
    void testGenerateSitemapXml() {
        List<SitemapEntry> entries = Arrays.asList(
                new SitemapEntry.Builder()
                        .loc("https://example.com/page1")
                        .lastmod(ZonedDateTime.now())
                        .changefreq("daily")
                        .priority("0.9")
                        .build(),
                new SitemapEntry.Builder()
                        .loc("https://example.com/page2")
                        .lastmod(ZonedDateTime.now())
                        .changefreq("weekly")
                        .priority("0.8")
                        .build()
        );

        String xml = seoOptimizerService.generateSitemapXml(entries);

        assertNotNull(xml);
        assertTrue(xml.contains("<?xml version"));
        assertTrue(xml.contains("urlset"));
        assertTrue(xml.contains("https://example.com/page1"));
        assertTrue(xml.contains("https://example.com/page2"));
        assertTrue(xml.contains("daily"));
        assertTrue(xml.contains("weekly"));
    }

    @Test
    void testGenerateSitemapXmlWithBaseUrl() {
        List<SitemapEntry> entries = Arrays.asList(
                new SitemapEntry.Builder()
                        .loc("/content/page1")
                        .build()
        );

        String xml = seoOptimizerService.generateSitemapXml(entries, "https://example.com");

        assertTrue(xml.contains("https://example.com/content/page1"));
    }

    @Test
    void testGenerateSitemapXmlWithEmptyEntries() {
        String xml = seoOptimizerService.generateSitemapXml(Arrays.asList());

        assertTrue(xml.contains("</urlset>"));
    }

    @Test
    void testGenerateSitemapXmlWithNullEntries() {
        String xml = seoOptimizerService.generateSitemapXml(null);

        assertTrue(xml.contains("</urlset>"));
    }

    @Test
    void testGenerateSitemapXmlDefaultValues() {
        List<SitemapEntry> entries = Arrays.asList(
                new SitemapEntry.Builder()
                        .loc("https://example.com/page1")
                        .build()
        );

        String xml = seoOptimizerService.generateSitemapXml(entries);

        assertTrue(xml.contains("changefreq>weekly"));
        assertTrue(xml.contains("priority>0.5"));
    }

    @Test
    void testSEOMetadataBuilder() {
        SEOMetadata metadata = new SEOMetadata.Builder()
                .metaTitle("Test Title")
                .metaDescription("Test Description")
                .keywords(Arrays.asList("keyword1", "keyword2"))
                .ogTitle("OG Title")
                .ogDescription("OG Description")
                .ogUrl("https://example.com")
                .ogType("article")
                .twitterCard("summary")
                .twitterTitle("Twitter Title")
                .twitterDescription("Twitter Description")
                .twitterImage("https://example.com/image.jpg")
                .schemaOrgJsonLd("{}")
                .seoScore(85)
                .seoRecommendations(Arrays.asList("Recommendation 1"))
                .build();

        assertEquals("Test Title", metadata.getMetaTitle());
        assertEquals("Test Description", metadata.getMetaDescription());
        assertEquals(2, metadata.getKeywords().size());
        assertEquals("OG Title", metadata.getOgTitle());
        assertEquals("article", metadata.getOgType());
        assertEquals("summary", metadata.getTwitterCard());
        assertEquals(85, metadata.getSeoScore());
    }

    @Test
    void testSEOMetadataBuilderDefaults() {
        SEOMetadata metadata = new SEOMetadata();

        assertNull(metadata.getMetaTitle());
        assertNull(metadata.getMetaDescription());
        assertTrue(metadata.getKeywords().isEmpty());
    }

    @Test
    void testSEOMetadataSetters() {
        SEOMetadata metadata = new SEOMetadata();
        metadata.setMetaTitle("Test Title");
        metadata.setMetaDescription("Test Description");
        metadata.addKeyword("keyword1");
        metadata.addKeyword("keyword2");
        metadata.setSeoScore(75);
        metadata.addSeoRecommendation("Test Recommendation");

        assertEquals("Test Title", metadata.getMetaTitle());
        assertEquals("Test Description", metadata.getMetaDescription());
        assertEquals(2, metadata.getKeywords().size());
        assertEquals(1, metadata.getSeoRecommendations().size());
    }

    @Test
    void testSEOMetadataScoreClamping() {
        SEOMetadata metadata = new SEOMetadata();

        metadata.setSeoScore(150);
        assertEquals(100, metadata.getSeoScore());

        metadata.setSeoScore(-50);
        assertEquals(0, metadata.getSeoScore());
    }

    @Test
    void testSitemapEntryBuilder() {
        ZonedDateTime now = ZonedDateTime.now();
        
        SitemapEntry entry = new SitemapEntry.Builder()
                .loc("https://example.com/test")
                .lastmod(now)
                .changefreq("daily")
                .priority("0.9")
                .alternates("en:https://example.com/en/test")
                .build();

        assertEquals("https://example.com/test", entry.getLoc());
        assertNotNull(entry.getLastmod());
        assertEquals("daily", entry.getChangefreq());
        assertEquals("0.9", entry.getPriority());
    }

    @Test
    void testSitemapEntryDefaults() {
        SitemapEntry entry = new SitemapEntry("https://example.com/test");

        assertEquals("https://example.com/test", entry.getLoc());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}