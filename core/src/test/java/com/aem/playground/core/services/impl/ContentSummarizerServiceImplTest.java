package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.ContentSummarizerService;
import com.aem.playground.core.services.ContentSummarizerServiceConfig;
import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentSummarizerServiceImplTest {

    @Mock
    private com.aem.playground.core.services.AIService aiService;

    private ContentSummarizerServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ContentSummarizerServiceImpl();

        ContentSummarizerServiceTestConfig config = new ContentSummarizerServiceTestConfig() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        };
        service.activate(config);
        service.aiService = aiService;
    }

    @Test
    void testGenerateSummaryWithValidInput() {
        String contentPath = "/content/page/article";
        String contentText = "This is a test article about artificial intelligence and machine learning. " +
                "It covers the latest trends in AI technology and provides insights into future developments. " +
                "Key topics include neural networks, deep learning, and natural language processing.";

        ContentSummary summary = service.generateSummary(contentPath, contentText, 100);

        assertNotNull(summary);
        assertEquals(contentPath, summary.getContentPath());
        assertNotNull(summary.getSummaryText());
        assertTrue(summary.getWordCount() > 0);
        assertTrue(summary.getConfidenceScore() >= 0.0);
    }

    @Test
    void testGenerateSummaryWithEmptyContentPath() {
        ContentSummary summary = service.generateSummary("", "Some content", 100);

        assertNotNull(summary);
        assertEquals(0.0, summary.getConfidenceScore());
    }

    @Test
    void testGenerateSummaryWithEmptyContentText() {
        ContentSummary summary = service.generateSummary("/content/page/test", "", 100);

        assertNotNull(summary);
        assertEquals(0.0, summary.getConfidenceScore());
    }

    @Test
    void testGenerateSummaryWithCustomMaxLength() {
        String contentText = "This is a longer article that should be summarized. " +
                "It contains multiple sentences and paragraphs. " +
                "The summary should capture the main points concisely.";

        ContentSummary summary = service.generateSummary("/content/page/test", contentText, 50);

        assertNotNull(summary);
        assertNotNull(summary.getSummaryText());
    }

    @Test
    void testGenerateSummaryCaching() {
        String contentPath = "/content/page/test";
        String contentText = "Test content for caching";

        ContentSummary summary1 = service.generateSummary(contentPath, contentText, 100);
        ContentSummary summary2 = service.generateSummary(contentPath, contentText, 100);

        assertNotNull(summary1);
        assertNotNull(summary2);
    }

    @Test
    void testGenerateSummaryDetectsContentType() {
        String contentText = "Breaking news about important events happening today.";

        ContentSummary summary = service.generateSummary("/content/page/news", contentText, 100);

        assertNotNull(summary);
        assertEquals("news", summary.getContentType());
    }

    @Test
    void testGenerateSummaryExtractsMainTopics() {
        String contentText = "Artificial intelligence and machine learning are transforming the technology industry. " +
                "Deep learning and neural networks are key components of modern AI systems.";

        ContentSummary summary = service.generateSummary("/content/page/test", contentText, 100);

        assertNotNull(summary.getMainTopics());
        assertFalse(summary.getMainTopics().isEmpty());
    }

    @Test
    void testGenerateExecutiveSummaryWithValidInput() {
        String contentPath = "/content/page/article";
        String contentText = "This is a test article about business strategy. " +
                "It discusses market trends and competitive analysis. " +
                "Key findings show significant opportunities for growth. " +
                "Recommendations include investing in new technologies.";

        ExecutiveSummary summary = service.generateExecutiveSummary(contentPath, contentText, 200);

        assertNotNull(summary);
        assertEquals(contentPath, summary.getContentPath());
        assertNotNull(summary.getBriefOverview());
        assertNotNull(summary.getKeyPoints());
        assertFalse(summary.getKeyPoints().isEmpty());
    }

    @Test
    void testGenerateExecutiveSummaryWithEmptyContentPath() {
        ExecutiveSummary summary = service.generateExecutiveSummary("", "Some content", 200);

        assertNotNull(summary);
        assertNotNull(summary.getMetadata());
    }

    @Test
    void testGenerateExecutiveSummaryExtractsBusinessImpact() {
        String contentText = "Important business analysis with significant impact on revenue.";

        ExecutiveSummary summary = service.generateExecutiveSummary("/content/page/test", contentText, 200);

        assertNotNull(summary.getBusinessImpact());
    }

    @Test
    void testGenerateExecutiveSummaryExtractsStakeholders() {
        String contentText = "Content requiring stakeholder review and approval.";

        ExecutiveSummary summary = service.generateExecutiveSummary("/content/page/test", contentText, 200);

        assertNotNull(summary.getStakeholders());
        assertFalse(summary.getStakeholders().isEmpty());
    }

    @Test
    void testExtractKeyTakeawaysWithValidInput() {
        String contentPath = "/content/page/article";
        String contentText = "Key finding one: AI is transforming industries. " +
                "Important point: Machine learning improves efficiency. " +
                "Critical recommendation: Invest in AI infrastructure. " +
                "Significant trend: Automation is growing rapidly. " +
                "Key insight: Data quality matters for AI success.";

        List<KeyTakeaway> takeaways = service.extractKeyTakeaways(contentPath, contentText, 3);

        assertNotNull(takeaways);
        assertFalse(takeaways.isEmpty());
        assertTrue(takeaways.size() <= 3);
    }

    @Test
    void testExtractKeyTakeawaysWithEmptyContentPath() {
        List<KeyTakeaway> takeaways = service.extractKeyTakeaways("", "Some content", 5);

        assertTrue(takeaways.isEmpty());
    }

    @Test
    void testExtractKeyTakeawaysWithCustomCount() {
        String contentText = "First important point about technology. " +
                "Second key finding about innovation. " +
                "Third significant recommendation. " +
                "Fourth critical insight. " +
                "Fifth notable trend.";

        List<KeyTakeaway> takeaways = service.extractKeyTakeaways("/content/page/test", contentText, 2);

        assertNotNull(takeaways);
        assertTrue(takeaways.size() <= 2);
    }

    @Test
    void testExtractKeyTakeawaysHasRelevanceScores() {
        String contentText = "Important information about key topics. " +
                "Significant findings in recent research. " +
                "Critical recommendations for improvement.";

        List<KeyTakeaway> takeaways = service.extractKeyTakeaways("/content/page/test", contentText, 3);

        for (KeyTakeaway takeaway : takeaways) {
            assertTrue(takeaway.getRelevanceScore() >= 0.0);
        }
    }

    @Test
    void testExtractKeyTakeawaysHasPriorities() {
        String contentText = "First key point. Second important point. Third critical point.";

        List<KeyTakeaway> takeaways = service.extractKeyTakeaways("/content/page/test", contentText, 3);

        int priority = 1;
        for (KeyTakeaway takeaway : takeaways) {
            assertEquals(priority++, takeaway.getPriority());
        }
    }

    @Test
    void testExtractHighlightsWithValidInput() {
        String contentPath = "/content/page/article";
        String contentText = "This is an important paragraph with key information. " +
                "Another section with significant data points. " +
                "A third paragraph with critical details.";

        List<ContentHighlight> highlights = service.extractHighlights(contentPath, contentText, 3);

        assertNotNull(highlights);
    }

    @Test
    void testExtractHighlightsWithEmptyContentPath() {
        List<ContentHighlight> highlights = service.extractHighlights("", "Some content", 5);

        assertTrue(highlights.isEmpty());
    }

    @Test
    void testExtractHighlightsHasImportanceScores() {
        String contentText = "Important key information that should be highlighted. " +
                "Another significant point worth noting.";

        List<ContentHighlight> highlights = service.extractHighlights("/content/page/test", contentText, 2);

        for (ContentHighlight highlight : highlights) {
            assertTrue(highlight.getImportanceScore() >= 0.0);
        }
    }

    @Test
    void testExtractHighlightsHasTypes() {
        String contentText = "This is a key point about the topic. It contains important information.";

        List<ContentHighlight> highlights = service.extractHighlights("/content/page/test", contentText, 2);

        for (ContentHighlight highlight : highlights) {
            assertNotNull(highlight.getHighlightType());
        }
    }

    @Test
    void testSummarizeContentFragmentWithValidInput() {
        String fragmentPath = "/content/fragments/my-fragment";
        String modelName = "text-model";
        String contentData = "This is content fragment data with multiple elements. " +
                "It includes text, images, and structured data.";

        ContentFragmentSummary summary = service.summarizeContentFragment(fragmentPath, modelName, contentData);

        assertNotNull(summary);
        assertEquals(fragmentPath, summary.getFragmentPath());
        assertEquals(modelName, summary.getModelName());
        assertNotNull(summary.getSummaryText());
        assertNotNull(summary.getElements());
    }

    @Test
    void testSummarizeContentFragmentWithEmptyPath() {
        ContentFragmentSummary summary = service.summarizeContentFragment("", "text-model", "content");

        assertNotNull(summary);
        assertNotNull(summary.getMetadata());
    }

    @Test
    void testSummarizeContentFragmentDeterminesFormat() {
        String contentData = "Content fragment data";

        ContentFragmentSummary summary = service.summarizeContentFragment("/content/fragments/test", "text", contentData);

        assertNotNull(summary.getFormat());
    }

    @Test
    void testSummarizeContentFragmentExtractsTags() {
        String contentData = "AI machine learning deep learning neural networks";

        ContentFragmentSummary summary = service.summarizeContentFragment("/content/fragments/test", "text", contentData);

        assertNotNull(summary.getTags());
    }

    @Test
    void testSummarizeContentFragmentHasElementSummaries() {
        String contentData = "This is the main text content of the fragment. " +
                "It contains multiple paragraphs and important information.";

        ContentFragmentSummary summary = service.summarizeContentFragment("/content/fragments/test", "text", contentData);

        assertNotNull(summary.getElementSummaries());
        assertTrue(summary.getElementSummaries().containsKey("text"));
    }

    @Test
    void testGetDashboard() {
        service.generateSummary("/content/page/test1", "Test content one", 100);
        service.generateSummary("/content/page/test2", "Test content two", 100);
        service.extractKeyTakeaways("/content/page/test1", "Important key takeaways", 3);

        SummarizationDashboard dashboard = service.getDashboard();

        assertNotNull(dashboard);
        assertNotNull(dashboard.getDashboardId());
        assertNotNull(dashboard.getName());
        assertTrue(dashboard.getTotalSummariesGenerated() >= 0);
        assertNotNull(dashboard.getSummariesByContentType());
        assertNotNull(dashboard.getLastRefreshed());
    }

    @Test
    void testGetDashboardWithNoData() {
        SummarizationDashboard dashboard = service.getDashboard();

        assertNotNull(dashboard);
        assertEquals(0, dashboard.getTotalSummariesGenerated());
    }

    @Test
    void testGetDashboardTracksHighlightsByType() {
        service.extractHighlights("/content/page/test", "Important key information here", 3);

        SummarizationDashboard dashboard = service.getDashboard();

        assertNotNull(dashboard.getHighlightsByType());
    }

    @Test
    void testContentSummaryStaticCreate() {
        ContentSummary summary = ContentSummary.create("/content/page/test", "article");

        assertEquals("/content/page/test", summary.getContentPath());
        assertEquals("article", summary.getContentType());
        assertNotNull(summary.getGeneratedAt());
    }

    @Test
    void testExecutiveSummaryStaticCreate() {
        ExecutiveSummary summary = ExecutiveSummary.create("/content/page/test", "Test Title");

        assertEquals("/content/page/test", summary.getContentPath());
        assertEquals("Test Title", summary.getTitle());
        assertNotNull(summary.getGeneratedAt());
    }

    @Test
    void testKeyTakeawayStaticCreate() {
        KeyTakeaway takeaway = KeyTakeaway.create("Important Finding", "This is the description", "finding");

        assertEquals("Important Finding", takeaway.getTitle());
        assertEquals("This is the description", takeaway.getDescription());
        assertEquals("finding", takeaway.getCategory());
        assertNotNull(takeaway.getDetectedAt());
    }

    @Test
    void testContentHighlightStaticCreate() {
        ContentHighlight highlight = ContentHighlight.create("/content/page/test", "Key highlight", "quote");

        assertEquals("/content/page/test", highlight.getContentPath());
        assertEquals("Key highlight", highlight.getHighlightText());
        assertEquals("quote", highlight.getHighlightType());
        assertNotNull(highlight.getExtractedAt());
    }

    @Test
    void testContentFragmentSummaryStaticCreate() {
        ContentFragmentSummary summary = ContentFragmentSummary.create("/content/fragments/test", "text-model");

        assertEquals("/content/fragments/test", summary.getFragmentPath());
        assertEquals("text-model", summary.getModelName());
        assertNotNull(summary.getCreatedAt());
    }

    @Test
    void testSummarizationDashboardStaticCreate() {
        SummarizationDashboard dashboard = SummarizationDashboard.create("Test Dashboard");

        assertNotNull(dashboard.getDashboardId());
        assertEquals("Test Dashboard", dashboard.getName());
        assertNotNull(dashboard.getLastRefreshed());
    }

    @Test
    void testSummaryStatistics() {
        SummarizationDashboard.SummaryStatistics stats = new SummarizationDashboard.SummaryStatistics();
        stats.setContentPath("/content/page/test");
        stats.setSummaryType("summary");
        stats.setGeneratedAt(LocalDateTime.now());
        stats.setConfidenceScore(0.85);

        assertEquals("/content/page/test", stats.getContentPath());
        assertEquals("summary", stats.getSummaryType());
        assertEquals(0.85, stats.getConfidenceScore());
    }

    @Test
    void testContentSummaryHasMetadata() {
        ContentSummary summary = service.generateSummary("/content/page/test", "Test content with important information", 100);

        assertNotNull(summary.getMetadata());
    }

    @Test
    void testExecutiveSummaryHasMetadata() {
        ExecutiveSummary summary = service.generateExecutiveSummary("/content/page/test", "Test content", 200);

        assertNotNull(summary.getMetadata());
    }

    static class ContentSummarizerServiceTestConfig implements ContentSummarizerServiceConfig {
        @Override
        public String apiKey() {
            return "test-api-key";
        }

        @Override
        public String serviceUrl() {
            return "https://api.openai.com/v1/chat/completions";
        }

        @Override
        public String defaultModel() {
            return "gpt-4";
        }

        @Override
        public float temperature() {
            return 0.5f;
        }

        @Override
        public int maxTokens() {
            return 2000;
        }

        @Override
        public boolean enableCache() {
            return true;
        }

        @Override
        public int cacheSize() {
            return 100;
        }

        @Override
        public int defaultSummaryLength() {
            return 500;
        }

        @Override
        public int defaultExecutiveSummaryLength() {
            return 300;
        }

        @Override
        public int defaultKeyTakeaways() {
            return 5;
        }

        @Override
        public int defaultHighlights() {
            return 10;
        }

        @Override
        public boolean enableAIIntegration() {
            return true;
        }
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() { return ContentSummarizerServiceConfig.class; }
    }
}