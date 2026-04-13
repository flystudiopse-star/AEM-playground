package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.SummarizationService;
import com.aem.playground.core.services.SummarizationServiceConfig;
import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummarizationServiceImplTest {

    @Mock
    private AIService aiService;

    private SummarizationServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SummarizationServiceImpl();
        service.activate(createTestConfig());
        service.aiService = aiService;
    }


    static abstract class TestSummarizationConfig implements SummarizationServiceConfig {
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() { return SummarizationServiceConfig.class; }
        @Override
        public String apiKey() { return "test-api-key"; }
        @Override
        public String serviceUrl() { return "https://api.openai.com/v1/chat/completions"; }
        @Override
        public String defaultModel() { return "gpt-4"; }
        @Override
        public float temperature() { return 0.5f; }
        @Override
        public int maxTokens() { return 2000; }
        @Override
        public int defaultSummaryLength() { return 100; }
        @Override
        public boolean enableAIIntegration() { return true; }
    }

    private SummarizationServiceConfig createTestConfig() {
        return new TestSummarizationConfig() {};
    }

    @Test
    void testSummarizePageWithValidInput() {
        String pagePath = "/content/page/article";
        String content = "This is a test article about artificial intelligence and machine learning. " +
                "It covers the latest trends in AI technology and provides insights into future developments. " +
                "Key topics include neural networks, deep learning, and natural language processing.";

        PageSummary summary = service.summarizePage(pagePath, content, SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary);
        assertEquals(pagePath, summary.getPagePath());
        assertNotNull(summary.getSummaryText());
        assertTrue(summary.getWordCount() > 0);
        assertTrue(summary.getConfidenceScore() >= 0.0);
        assertNotNull(summary.getTitle());
        assertNotNull(summary.getKeywords());
    }

    @Test
    void testSummarizePageWithBriefLength() {
        String content = "This is a brief article about technology. It discusses the latest trends in the industry.";

        PageSummary summary = service.summarizePage("/content/page/test", content, SummarizationService.SummaryLength.BRIEF);

        assertNotNull(summary);
        assertEquals(50, summary.getTargetLength());
    }

    @Test
    void testSummarizePageWithDetailedLength() {
        String content = "This is a longer article that should have a detailed summary. " +
                "It contains multiple paragraphs of content about various topics including technology, " +
                "science, and business innovations that are transforming the modern world.";

        PageSummary summary = service.summarizePage("/content/page/test", content, SummarizationService.SummaryLength.DETAILED);

        assertNotNull(summary);
        assertEquals(250, summary.getTargetLength());
    }

    @Test
    void testSummarizePageWithEmptyPath() {
        PageSummary summary = service.summarizePage("", "Some content", SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary);
        assertEquals(0.0, summary.getConfidenceScore());
    }

    @Test
    void testSummarizePageWithEmptyContent() {
        PageSummary summary = service.summarizePage("/content/page/test", "", SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary);
        assertEquals(0.0, summary.getConfidenceScore());
    }

    @Test
    void testSummarizePageDetectsContentType() {
        String content = "Breaking news about important events happening today in the technology sector.";

        PageSummary summary = service.summarizePage("/content/page/news", content, SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary);
        assertEquals("news", summary.getContentType());
    }

    @Test
    void testSummarizePageExtractsKeywords() {
        String content = "Artificial intelligence and machine learning are transforming the technology industry. " +
                "Deep learning and neural networks are key components.";

        PageSummary summary = service.summarizePage("/content/page/test", content, SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary.getKeywords());
        assertFalse(summary.getKeywords().isEmpty());
    }

    @Test
    void testSummarizePageWithOptions() {
        String content = "Test content about various topics that require custom summarization.";

        Map<String, Object> options = new HashMap<>();
        options.put("targetLength", 75);

        PageSummary summary = service.summarizePage("/content/page/test", content, SummarizationService.SummaryLength.BRIEF, options);

        assertNotNull(summary);
        assertEquals(75, summary.getTargetLength());
    }

    @Test
    void testSummarizeContentFragment() {
        String fragmentPath = "/content/fragments/my-fragment";
        String content = "This is content fragment data with multiple elements. " +
                "It includes text, structured data, and important information.";

        ContentFragmentSummary summary = service.summarizeContentFragment(fragmentPath, content, SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary);
        assertEquals(fragmentPath, summary.getFragmentPath());
        assertNotNull(summary.getSummaryText());
        assertNotNull(summary.getElements());
        assertNotNull(summary.getTags());
    }

    @Test
    void testSummarizeContentFragmentWithEmptyPath() {
        ContentFragmentSummary summary = service.summarizeContentFragment("", "content", SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary);
        assertNotNull(summary.getMetadata());
    }

    @Test
    void testSummarizeContentFragmentDeterminesFormat() {
        String content = "<div>HTML content fragment</div>";

        ContentFragmentSummary summary = service.summarizeContentFragment("/content/fragments/test", content, SummarizationService.SummaryLength.STANDARD);

        assertNotNull(summary.getFormat());
    }

    @Test
    void testGenerateExcerpt() {
        String content = "This is a longer article about technology trends. " +
                "It covers artificial intelligence, machine learning, and cloud computing. " +
                "These technologies are transforming businesses worldwide.";

        Excerpt excerpt = service.generateExcerpt(content, SummarizationService.SummaryLength.BRIEF);

        assertNotNull(excerpt);
        assertNotNull(excerpt.getContent());
        assertTrue(excerpt.getWordCount() > 0);
        assertTrue(excerpt.getConfidenceScore() >= 0.0);
    }

    @Test
    void testGenerateExcerptWithEmptyContent() {
        Excerpt excerpt = service.generateExcerpt("", SummarizationService.SummaryLength.STANDARD);

        assertNotNull(excerpt);
        assertEquals(0.0, excerpt.getConfidenceScore());
    }

    @Test
    void testGenerateExcerptWithStandardLength() {
        String content = "This is sample content for excerpt generation.";

        Excerpt excerpt = service.generateExcerpt(content, SummarizationService.SummaryLength.STANDARD);

        assertNotNull(excerpt);
        assertEquals(100, excerpt.getMaxLength());
    }

    @Test
    void testGenerateExcerptWithOptions() {
        String content = "Test content for custom excerpt options.";

        Map<String, Object> options = new HashMap<>();
        options.put("targetLength", 30);

        Excerpt excerpt = service.generateExcerpt(content, SummarizationService.SummaryLength.BRIEF, options);

        assertNotNull(excerpt);
        assertEquals(30, excerpt.getMaxLength());
    }

    @Test
    void testCreateMetaDescription() {
        String content = "This is an article about artificial intelligence and machine learning. " +
                "It discusses how AI is transforming industries and what the future holds for technology.";

        MetaDescription metaDesc = service.createMetaDescription(content);

        assertNotNull(metaDesc);
        assertNotNull(metaDesc.getDescription());
        assertTrue(metaDesc.getCharacterCount() > 0);
        assertTrue(metaDesc.getCharacterCount() <= metaDesc.getMaxCharacters());
        assertTrue(metaDesc.getConfidenceScore() >= 0.0);
    }

    @Test
    void testCreateMetaDescriptionWithDefaultLength() {
        String content = "Test content for meta description generation.";

        MetaDescription metaDesc = service.createMetaDescription(content);

        assertNotNull(metaDesc);
        assertEquals(160, metaDesc.getMaxCharacters());
    }

    @Test
    void testCreateMetaDescriptionWithCustomLength() {
        String content = "Test content for meta description.";

        MetaDescription metaDesc = service.createMetaDescription(content, SummarizationService.SummaryLength.BRIEF);

        assertNotNull(metaDesc);
        assertEquals(300, metaDesc.getMaxCharacters());
    }

    @Test
    void testCreateMetaDescriptionWithEmptyContent() {
        MetaDescription metaDesc = service.createMetaDescription("");

        assertNotNull(metaDesc);
        assertEquals(0.0, metaDesc.getConfidenceScore());
    }

    @Test
    void testCreateMetaDescriptionTruncatesLongContent() {
        String content = "This is a very long content that should be truncated to fit within the maximum character limit for meta descriptions used in search engine optimization.";

        MetaDescription metaDesc = service.createMetaDescription(content, SummarizationService.SummaryLength.STANDARD);

        assertNotNull(metaDesc);
        assertTrue(metaDesc.getCharacterCount() <= metaDesc.getMaxCharacters());
    }

    @Test
    void testCreateMetaDescriptionWithOptions() {
        String content = "Test content for custom meta description options.";

        Map<String, Object> options = new HashMap<>();
        options.put("maxCharacters", 200);

        MetaDescription metaDesc = service.createMetaDescription(content, SummarizationService.SummaryLength.STANDARD, options);

        assertNotNull(metaDesc);
        assertEquals(200, metaDesc.getMaxCharacters());
    }

    @Test
    void testSummaryLengthEnumValues() {
        assertEquals(50, SummarizationService.SummaryLength.BRIEF.getWordCount());
        assertEquals(100, SummarizationService.SummaryLength.STANDARD.getWordCount());
        assertEquals(250, SummarizationService.SummaryLength.DETAILED.getWordCount());
    }

    @Test
    void testPageSummaryStaticCreate() {
        PageSummary summary = PageSummary.create("/content/page/test", "article");

        assertEquals("/content/page/test", summary.getPagePath());
        assertEquals("article", summary.getContentType());
        assertNotNull(summary.getGeneratedAt());
    }

    @Test
    void testExcerptStaticCreate() {
        Excerpt excerpt = Excerpt.create("Test content", 100);

        assertEquals("Test content", excerpt.getContent());
        assertEquals(100, excerpt.getMaxLength());
        assertNotNull(excerpt.getGeneratedAt());
    }

    @Test
    void testMetaDescriptionStaticCreate() {
        MetaDescription metaDesc = MetaDescription.create(160);

        assertEquals(160, metaDesc.getMaxCharacters());
        assertNotNull(metaDesc.getGeneratedAt());
    }
}