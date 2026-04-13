package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.ContentQAIssue;
import com.aem.playground.core.services.dto.ContentQAReport;
import com.aem.playground.core.services.impl.AIContentQACheckerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIContentQACheckerServiceTest {

    private AIContentQACheckerService fixture;

    @BeforeEach
    void setup() {
        fixture = new AIContentQACheckerService();
        AIContentQACheckerConfig config = mock(AIContentQACheckerConfig.class);
        when(config.aiEnabled()).thenReturn(false);
        when(config.allowedBrandColors()).thenReturn("#000000,#FFFFFF,#FF0000");
        when(config.allowedFonts()).thenReturn("Arial,Helvetica");
        when(config.minContentLength()).thenReturn(100);
        when(config.maxContentLength()).thenReturn(10000);
        fixture.activate(config);
    }

    @Test
    void testAnalyzeContentWithEmptyContent() {
        ContentQAReport report = fixture.analyzeContent("", "/content/page");

        assertEquals(ContentQAReport.OverallStatus.FAIL, report.getStatus());
        assertEquals(1, report.getTotalIssues());
        assertEquals(ContentQAIssue.IssueType.CONTENT_QUALITY, report.getIssues().get(0).getType());
        assertEquals(ContentQAIssue.Severity.CRITICAL, report.getIssues().get(0).getSeverity());
    }

    @Test
    void testAnalyzeContentWithNullContent() {
        ContentQAReport report = fixture.analyzeContent(null, "/content/page");

        assertEquals(ContentQAReport.OverallStatus.FAIL, report.getStatus());
        assertEquals(1, report.getTotalIssues());
    }

    @Test
    void testAnalyzeContentWithShortContent() {
        ContentQAReport report = fixture.analyzeContent("Short content", "/content/page");

        assertTrue(report.getTotalIssues() > 0);
        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Too Short")));
    }

    @Test
    void testAnalyzeContentWithPlaceholderContent() {
        ContentQAReport report = fixture.analyzeContent("Lorem ipsum dolor sit amet", "/content/page");

        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Placeholder")));
    }

    @Test
    void testAnalyzeContentWithValidContent() {
        String content = "This is a substantial piece of content that exceeds the minimum length requirement. "
                + "It contains proper paragraph breaks and real content that should pass basic quality checks. "
                + "The content is well formatted with appropriate structure and meaningful text.";

        ContentQAReport report = fixture.analyzeContent(content, "/content/page");

        assertNotNull(report);
        assertEquals("/content/page", report.getContentPath());
    }

    @Test
    void testCheckBrokenLinksWithValidLinks() {
        String content = "<a href=\"/content/page\">Internal Link</a> "
                + "<a href=\"mailto:test@example.com\">Email</a> "
                + "<a href=\"#anchor\">Anchor</a>";

        ContentQAReport report = fixture.checkBrokenLinks(content, "/content/page");

        assertEquals(0, report.getTotalIssues());
    }

    @Test
    void testCheckBrokenLinksWithMalformedLinks() {
        String content = "<a href=\"http://example.com page\">Invalid Link</a> "
                + "<a href=\"http://bad<link\">Broken</a>";

        ContentQAReport report = fixture.checkBrokenLinks(content, "/content/page");

        assertTrue(report.getTotalIssues() > 0);
        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getType() == ContentQAIssue.IssueType.BROKEN_LINK));
    }

    @Test
    void testValidateStructureWithMissingH1() {
        String content = "<h2>Section 1</h2><p>Content</p><h3>Subsection</h3>";

        ContentQAReport report = fixture.validateStructure(content, "/content/page");

        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Missing Heading")));
    }

    @Test
    void testValidateStructureWithSkippedHeadingLevel() {
        String content = "<h1>Title</h1><h3>Subsection without h2</h3>";

        ContentQAReport report = fixture.validateStructure(content, "/content/page");

        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Skipped Heading")));
    }

    @Test
    void testValidateStructureWithValidHierarchy() {
        String content = "<h1>Main Title</h1><h2>Section 1</h2><p>Content</p><h2>Section 2</h2>";

        ContentQAReport report = fixture.validateStructure(content, "/content/page");

        assertEquals(0, report.getTotalIssues());
    }

    @Test
    void testCheckAccessibilityWithMissingAltText() {
        String content = "<img src=\"image.jpg\"><img src=\"photo.png\" alt=\"Valid\">";

        ContentQAReport report = fixture.checkAccessibility(content, "/content/page");

        assertTrue(report.getTotalIssues() > 0);
        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Alt Text")));
    }

    @Test
    void testCheckAccessibilityWithNonDescriptiveLinkText() {
        String content = "<a href=\"/page\">Click here</a> <a href=\"/link\">Link</a>";

        ContentQAReport report = fixture.checkAccessibility(content, "/content/page");

        assertTrue(report.getTotalIssues() > 0);
        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Non-Descriptive")));
    }

    @Test
    void testCheckAccessibilityWithValidContent() {
        String content = "<img src=\"photo.png\" alt=\"A beautiful sunset\">"
                + "<h1>Title</h1><h2>Section</h2>"
                + "<a href=\"/page\">Learn more about our services</a>";

        ContentQAReport report = fixture.checkAccessibility(content, "/content/page");

        assertEquals(0, report.getTotalIssues());
    }

    @Test
    void testCheckBrandConsistencyWithNonBrandColor() {
        String content = "<span style=\"color: #FF5500;\">Non-brand color</span>";

        ContentQAReport report = fixture.checkBrandConsistency(content, "/content/page");

        assertTrue(report.getTotalIssues() > 0);
        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getType() == ContentQAIssue.IssueType.BRAND_CONSISTENCY));
    }

    @Test
    void testCheckBrandConsistencyWithBrandColor() {
        String content = "<span style=\"color: #FF0000;\">Brand color</span>";

        ContentQAReport report = fixture.checkBrandConsistency(content, "/content/page");

        assertEquals(0, report.getTotalIssues());
    }

    @Test
    void testCheckBrandConsistencyWithApologeticTone() {
        String content = "We are sorry for the inconvenience caused by the delay.";

        ContentQAReport report = fixture.checkBrandConsistency(content, "/content/page");

        assertTrue(report.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Apologetic")));
    }

    @Test
    void testGenerateFullReportCombinesAllChecks() {
        String content = "<h1>Page Title</h1>"
                + "<p>Short</p>"
                + "<img src=\"test.jpg\">"
                + "<a href=\"bad link\">Click</a>";

        ContentQAReport report = fixture.generateFullReport(content, "/content/page");

        assertTrue(report.getTotalIssues() > 0);
        assertNotNull(report.getMetadata());
        assertTrue(report.getMetadata().containsKey("qualityIssues"));
        assertTrue(report.getMetadata().containsKey("accessibilityIssues"));
        assertTrue(report.getRecommendations().size() > 0);
    }

    @Test
    void testReportBuilderCalculatesScore() {
        ContentQAReport report = ContentQAReport.builder()
                .contentPath("/test")
                .addIssue(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                        .severity(ContentQAIssue.Severity.CRITICAL)
                        .title("Test")
                        .description("Test")
                        .location("/test")
                        .suggestion("Fix")
                        .build())
                .addIssue(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.ACCESSIBILITY)
                        .severity(ContentQAIssue.Severity.HIGH)
                        .title("Test")
                        .description("Test")
                        .location("/test")
                        .suggestion("Fix")
                        .build())
                .build();

        assertEquals(70, report.getOverallScore());
        assertEquals(ContentQAReport.OverallStatus.FAIL, report.getStatus());
    }

    @Test
    void testReportFiltersIssuesByType() {
        ContentQAReport report = ContentQAReport.builder()
                .contentPath("/test")
                .addIssue(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                        .severity(ContentQAIssue.Severity.MEDIUM)
                        .title("Test")
                        .description("Test")
                        .location("/test")
                        .suggestion("Fix")
                        .build())
                .addIssue(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.ACCESSIBILITY)
                        .severity(ContentQAIssue.Severity.MEDIUM)
                        .title("Test")
                        .description("Test")
                        .location("/test")
                        .suggestion("Fix")
                        .build())
                .build();

        List<ContentQAIssue> qualityIssues = report.getIssuesByType(ContentQAIssue.IssueType.CONTENT_QUALITY);
        assertEquals(1, qualityIssues.size());
    }

    @Test
    void testReportFiltersIssuesBySeverity() {
        ContentQAReport report = ContentQAReport.builder()
                .contentPath("/test")
                .addIssue(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                        .severity(ContentQAIssue.Severity.CRITICAL)
                        .title("Test")
                        .description("Test")
                        .location("/test")
                        .suggestion("Fix")
                        .build())
                .addIssue(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                        .severity(ContentQAIssue.Severity.LOW)
                        .title("Test")
                        .description("Test")
                        .location("/test")
                        .suggestion("Fix")
                        .build())
                .build();

        assertEquals(1, report.getCriticalIssueCount());
        assertEquals(1, report.getLowIssueCount());
    }

    @Test
    void testIssueBuilderCreatesValidIssue() {
        ContentQAIssue issue = ContentQAIssue.builder()
                .type(ContentQAIssue.IssueType.BROKEN_LINK)
                .severity(ContentQAIssue.Severity.HIGH)
                .title("Broken Link")
                .description("Link is broken")
                .location("/content/page")
                .suggestion("Fix the link")
                .build();

        assertEquals(ContentQAIssue.IssueType.BROKEN_LINK, issue.getType());
        assertEquals(ContentQAIssue.Severity.HIGH, issue.getSeverity());
        assertEquals("Broken Link", issue.getTitle());
        assertEquals("Link is broken", issue.getDescription());
    }

    @Test
    void testReportBuilderWithEmptyIssuesReturnsPassStatus() {
        ContentQAReport report = ContentQAReport.builder()
                .contentPath("/test")
                .contentTitle("Test Page")
                .build();

        assertEquals(ContentQAReport.OverallStatus.PASS, report.getStatus());
        assertEquals(100, report.getOverallScore());
    }
}