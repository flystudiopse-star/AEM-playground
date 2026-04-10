package com.aem.playground.core.services;

import com.aem.playground.core.services.impl.ContentModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentModerationServiceTest {

    @Mock
    private AIService aiService;

    private ContentModerationService service;

    @BeforeEach
    void setUp() {
        service = new ContentModerationServiceImpl();
    }

    @Test
    void testDetectInappropriateContent() {
        ModerationResult result = service.detectInappropriateContent("/content/page");

        assertNotNull(result);
        assertEquals("/content/page", result.getContentPath());
    }

    @Test
    void testDetectInappropriateContentWithSensitivity() {
        ModerationResult result = service.detectInappropriateContent("/content/page", 0.8);

        assertNotNull(result);
        assertEquals("/content/page", result.getContentPath());
    }

    @Test
    void testDetectInappropriateContentWithBlankPath() {
        ModerationResult result = service.detectInappropriateContent("");

        assertNull(result);
    }

    @Test
    void testDetectInappropriateContentWithNullPath() {
        ModerationResult result = service.detectInappropriateContent(null);

        assertNull(result);
    }

    @Test
    void testFlagPolicyViolations() {
        List<ModerationViolation> violations = service.flagPolicyViolations("/content/page");

        assertNotNull(violations);
    }

    @Test
    void testFlagPolicyViolationsWithCategory() {
        List<ModerationViolation> violations = service.flagPolicyViolations(
                "/content/page", ModerationCategory.HATE_SPEECH);

        assertNotNull(violations);
    }

    @Test
    void testFlagPolicyViolationsWithBlankPath() {
        List<ModerationViolation> violations = service.flagPolicyViolations("");

        assertNotNull(violations);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testAutoCensorContent() {
        ContentCensorResult result = service.autoCensorContent("/content/page");

        assertNotNull(result);
        assertNotNull(result.getOriginalContent());
    }

    @Test
    void testAutoCensorContentWithCustomChar() {
        ContentCensorResult result = service.autoCensorContent("/content/page", "#");

        assertNotNull(result);
    }

    @Test
    void testAutoCensorContentWithBlankPath() {
        ContentCensorResult result = service.autoCensorContent("");

        assertNull(result);
    }

    @Test
    void testAutoCensorContentWithNullPath() {
        ContentCensorResult result = service.autoCensorContent(null);

        assertNull(result);
    }

    @Test
    void testTriggerModerationWorkflow() {
        boolean result = service.triggerModerationWorkflow("/content/page");

        assertTrue(result);
    }

    @Test
    void testTriggerModerationWorkflowWithTrigger() {
        ModerationWorkflowTrigger trigger = ModerationWorkflowTrigger.builder()
                .contentPath("/content/page")
                .workflowModelId("models/content-moderation")
                .triggerReason("Test trigger")
                .priority(1)
                .build();

        boolean result = service.triggerModerationWorkflow(trigger);

        assertTrue(result);
    }

    @Test
    void testGetApprovalQueue() {
        List<ApprovalQueueItem> queue = service.getApprovalQueue();

        assertNotNull(queue);
    }

    @Test
    void testGetApprovalQueueWithStatus() {
        List<ApprovalQueueItem> queue = service.getApprovalQueue(
                ApprovalQueueItem.ApprovalStatus.PENDING);

        assertNotNull(queue);
    }

    @Test
    void testAddToApprovalQueue() {
        boolean result = service.addToApprovalQueue("/content/page");

        assertTrue(result);
    }

    @Test
    void testAddToApprovalQueueWithBlankPath() {
        boolean result = service.addToApprovalQueue("");

        assertFalse(result);
    }

    @Test
    void testApproveContent() {
        service.addToApprovalQueue("/content/page");
        List<ApprovalQueueItem> queue = service.getApprovalQueue();
        
        if (!queue.isEmpty()) {
            String itemId = queue.get(0).getItemId();
            boolean result = service.approveContent(itemId, "Approved");
            
            assertTrue(result);
        }
    }

    @Test
    void testRejectContent() {
        service.addToApprovalQueue("/content/page2");
        List<ApprovalQueueItem> queue = service.getApprovalQueue();
        
        if (!queue.isEmpty()) {
            String itemId = queue.get(0).getItemId();
            boolean result = service.rejectContent(itemId, "Policy violation", "Rejected due to content");
            
            assertTrue(result);
        }
    }

    @Test
    void testRemoveFromApprovalQueue() {
        service.addToApprovalQueue("/content/page3");
        List<ApprovalQueueItem> queue = service.getApprovalQueue();
        
        if (!queue.isEmpty()) {
            String itemId = queue.get(0).getItemId();
            boolean result = service.removeFromApprovalQueue(itemId);
            
            assertTrue(result);
        }
    }

    @Test
    void testGenerateModerationReport() {
        ModerationReport report = service.generateModerationReport(new Date(), new Date());

        assertNotNull(report);
    }

    @Test
    void testGenerateModerationReportWithCategory() {
        ModerationReport report = service.generateModerationReport(
                new Date(), new Date(), ModerationCategory.HATE_SPEECH);

        assertNotNull(report);
    }

    @Test
    void testGetModerationDashboard() {
        ModerationDashboard dashboard = service.getModerationDashboard();

        assertNotNull(dashboard);
    }

    @Test
    void testGetModerationDashboardWithTimeRange() {
        ModerationDashboard dashboard = service.getModerationDashboard("week");

        assertNotNull(dashboard);
    }

    @Test
    void testModerationResultBuilder() {
        ModerationResult result = ModerationResultBuilder.builder()
                .contentPath("/content/page")
                .isApproved(true)
                .overallScore(95.0)
                .recommendation("APPROVE")
                .build();

        assertEquals("/content/page", result.getContentPath());
        assertTrue(result.isApproved());
        assertEquals(95.0, result.getOverallScore());
    }

    @Test
    void testModerationViolationBuilder() {
        ModerationViolation violation = ModerationViolationBuilder.builder()
                .category(ModerationCategory.HATE_SPEECH)
                .confidence(0.9)
                .description("Hate speech detected")
                .matchedContent("badword")
                .startIndex(0)
                .endIndex(7)
                .autoCensored(true)
                .severity("high")
                .build();

        assertEquals(ModerationCategory.HATE_SPEECH, violation.getCategory());
        assertEquals(0.9, violation.getConfidence(), 0.001);
        assertEquals("badword", violation.getMatchedContent());
        assertTrue(violation.isAutoCensored());
    }

    @Test
    void testModerationCategoryEnum() {
        assertEquals("Hate Speech", ModerationCategory.HATE_SPEECH.getDisplayName());
        assertEquals("Violence", ModerationCategory.VIOLENCE.getDisplayName());
        assertEquals("Sexual Content", ModerationCategory.SEXUAL_CONTENT.getDisplayName());
    }

    @Test
    void testContentCensorResultBuilder() {
        List<ContentCensorResult.CensoredSegment> segments = new ArrayList<>();
        
        ContentCensorResult.CensoredSegment segment = new ContentCensorResult.CensoredSegment();
        segment.setStartIndex(0);
        segment.setEndIndex(10);
        segment.setOriginalText("badword");
        segment.setCensoredText("*******");
        segment.setCategory(ModerationCategory.PROHIBITED_WORDS);
        segment.setReason("Prohibited word");
        segments.add(segment);

        ContentCensorResult result = ContentCensorResultBuilder.builder()
                .originalContent("This contains badword here")
                .censoredContent("This contains ******* here")
                .censoredSegments(segments)
                .totalCensoredCount(1)
                .processingTimeMs(50)
                .build();

        assertEquals(1, result.getTotalCensoredCount());
        assertEquals("*******", result.getCensoredContent().substring(15, 22));
    }

    @Test
    void testModerationWorkflowTriggerBuilder() {
        List<ModerationViolation> violations = new ArrayList<>();
        violations.add(ModerationViolationBuilder.builder()
                .category(ModerationCategory.SPAM)
                .confidence(0.8)
                .build());

        ModerationWorkflowTrigger trigger = ModerationWorkflowTriggerBuilder.builder()
                .contentPath("/content/page")
                .workflowModelId("models/content-moderation")
                .triggerReason("Spam detected")
                .triggeredViolations(violations)
                .assignedReviewer("admin")
                .priority(1)
                .build();

        assertEquals("/content/page", trigger.getContentPath());
        assertEquals(1, trigger.getPriority());
    }

    @Test
    void testApprovalQueueItemBuilder() {
        List<ModerationViolation> violations = new ArrayList<>();
        
        ApprovalQueueItem item = ApprovalQueueItemBuilder.builder()
                .itemId("item-123")
                .contentPath("/content/page")
                .contentTitle("Test Page")
                .contentType("text")
                .submittedBy("author")
                .status(ApprovalQueueItem.ApprovalStatus.PENDING)
                .pendingViolations(violations)
                .build();

        assertEquals("item-123", item.getItemId());
        assertEquals("Test Page", item.getContentTitle());
        assertEquals(ApprovalQueueItem.ApprovalStatus.PENDING, item.getStatus());
    }

    @Test
    void testApprovalQueueItemStatusEnum() {
        assertEquals(ApprovalQueueItem.ApprovalStatus.PENDING, ApprovalQueueItem.ApprovalStatus.valueOf("PENDING"));
        assertEquals(ApprovalQueueItem.ApprovalStatus.IN_REVIEW, ApprovalQueueItem.ApprovalStatus.valueOf("IN_REVIEW"));
        assertEquals(ApprovalQueueItem.ApprovalStatus.APPROVED, ApprovalQueueItem.ApprovalStatus.valueOf("APPROVED"));
        assertEquals(ApprovalQueueItem.ApprovalStatus.REJECTED, ApprovalQueueItem.ApprovalStatus.valueOf("REJECTED"));
    }

    @Test
    void testModerationReportBuilder() {
        Map<ModerationCategory, ModerationReport.CategoryStats> categoryStats = new HashMap<>();
        ModerationReport.CategoryStats stats = new ModerationReport.CategoryStats();
        stats.setCount(5);
        stats.setPercentage(25.0);
        categoryStats.put(ModerationCategory.SPAM, stats);

        ModerationReport report = ModerationReportBuilder.builder()
                .reportId("report-123")
                .startDate(System.currentTimeMillis() - 86400000)
                .endDate(System.currentTimeMillis())
                .totalContentReviewed(100)
                .totalApproved(80)
                .totalRejected(10)
                .totalCensored(10)
                .categoryStats(categoryStats)
                .build();

        assertEquals("report-123", report.getReportId());
        assertEquals(100, report.getTotalContentReviewed());
        assertEquals(80, report.getTotalApproved());
    }

    @Test
    void testModerationReportCategoryStats() {
        ModerationReport.CategoryStats stats = new ModerationReport.CategoryStats();
        stats.setCount(10);
        stats.setPercentage(50.0);
        stats.setAutoCensoredCount(5);
        stats.setRejectedCount(3);
        stats.setAverageConfidence(0.85);

        assertEquals(10, stats.getCount());
        assertEquals(50.0, stats.getPercentage(), 0.001);
        assertEquals(5, stats.getAutoCensoredCount());
        assertEquals(3, stats.getRejectedCount());
        assertEquals(0.85, stats.getAverageConfidence(), 0.001);
    }

    @Test
    void testModerationReportTrendData() {
        ModerationReport.TrendData trend = new ModerationReport.TrendData();
        trend.setDate(System.currentTimeMillis());
        trend.setReviewed(50);
        trend.setApproved(40);
        trend.setRejected(5);
        trend.setCensored(5);

        assertEquals(50, trend.getReviewed());
        assertEquals(40, trend.getApproved());
        assertEquals(5, trend.getRejected());
        assertEquals(5, trend.getCensored());
    }

    @Test
    void testModerationReportTopViolation() {
        ModerationReport.TopViolation topViolation = new ModerationReport.TopViolation();
        topViolation.setCategory(ModerationCategory.SPAM);
        topViolation.setCount(25);
        topViolation.setAvgConfidence(0.92);

        assertEquals(ModerationCategory.SPAM, topViolation.getCategory());
        assertEquals(25, topViolation.getCount());
        assertEquals(0.92, topViolation.getAvgConfidence(), 0.001);
    }

    @Test
    void testModerationDashboard() {
        ModerationDashboard dashboard = new ModerationDashboard();
        dashboard.setTotalContentReviewedToday(50);
        dashboard.setTotalPendingApproval(10);
        dashboard.setTotalApprovedToday(35);
        dashboard.setTotalRejectedToday(5);
        dashboard.setApprovalRate(87.5);
        dashboard.setRejectionRate(12.5);

        List<ModerationDashboard.CategoryCount> categoryBreakdown = new ArrayList<>();
        ModerationDashboard.CategoryCount catCount = new ModerationDashboard.CategoryCount();
        catCount.setCategory(ModerationCategory.SPAM);
        catCount.setCount(15);
        catCount.setPercentage(30.0);
        categoryBreakdown.add(catCount);
        dashboard.setCategoryBreakdown(categoryBreakdown);

        assertEquals(50, dashboard.getTotalContentReviewedToday());
        assertEquals(10, dashboard.getTotalPendingApproval());
        assertEquals(35, dashboard.getTotalApprovedToday());
        assertEquals(87.5, dashboard.getApprovalRate(), 0.001);
    }

    @Test
    void testModerationDashboardRecentActivity() {
        ModerationDashboard dashboard = new ModerationDashboard();
        
        List<ModerationDashboard.RecentActivity> activities = new ArrayList<>();
        ModerationDashboard.RecentActivity activity = new ModerationDashboard.RecentActivity();
        activity.setContentId("/content/page");
        activity.setAction("APPROVED");
        activity.setPerformedBy("admin");
        activity.setTimestamp(System.currentTimeMillis());
        activities.add(activity);
        
        dashboard.setRecentActivity(activities);

        assertEquals(1, dashboard.getRecentActivity().size());
        assertEquals("APPROVED", dashboard.getRecentActivity().get(0).getAction());
    }

    @Test
    void testMultipleModerationCategories() {
        for (ModerationCategory category : ModerationCategory.values()) {
            assertNotNull(category.getDisplayName());
            assertFalse(category.getDisplayName().isEmpty());
        }
    }

    @Test
    void testModerationViolationAllCategories() {
        for (ModerationCategory category : ModerationCategory.values()) {
            ModerationViolation violation = ModerationViolationBuilder.builder()
                    .category(category)
                    .confidence(0.5)
                    .build();
            
            assertEquals(category, violation.getCategory());
        }
    }

    @Test
    void testApprovalQueueWithMultipleItems() {
        service.addToApprovalQueue("/content/page1");
        service.addToApprovalQueue("/content/page2");
        service.addToApprovalQueue("/content/page3");

        List<ApprovalQueueItem> queue = service.getApprovalQueue();
        
        assertTrue(queue.size() >= 3);
    }

    @Test
    void testApprovalQueueFiltersByStatus() {
        service.addToApprovalQueue("/content/page1");
        
        List<ApprovalQueueItem> pendingQueue = service.getApprovalQueue(ApprovalQueueItem.ApprovalStatus.PENDING);
        
        assertTrue(pendingQueue.stream().allMatch(item -> item.getStatus() == ApprovalQueueItem.ApprovalStatus.PENDING));
    }

    @Test
    void testModerationResultWithViolations() {
        List<ModerationViolation> violations = new ArrayList<>();
        violations.add(ModerationViolationBuilder.builder()
                .category(ModerationCategory.SPAM)
                .confidence(0.7)
                .build());
        
        ModerationResult result = ModerationResultBuilder.builder()
                .contentPath("/content/page")
                .isApproved(false)
                .violations(violations)
                .overallScore(30.0)
                .recommendation("REJECT")
                .build();

        assertEquals(1, result.getViolations().size());
        assertFalse(result.isApproved());
        assertEquals("REJECT", result.getRecommendation());
    }

    @Test
    void testAutoCensorResultTracksSegments() {
        ContentCensorResult result = service.autoCensorContent("/content/test");
        
        assertNotNull(result.getCensoredSegments());
    }

    @Test
    void testModerationReportWithTrends() {
        List<ModerationReport.TrendData> trends = new ArrayList<>();
        ModerationReport.TrendData trend = new ModerationReport.TrendData();
        trend.setDate(System.currentTimeMillis());
        trend.setReviewed(100);
        trend.setApproved(90);
        trend.setRejected(5);
        trend.setCensored(5);
        trends.add(trend);

        ModerationReport report = ModerationReportBuilder.builder()
                .trends(trends)
                .build();

        assertEquals(1, report.getTrends().size());
    }

    @Test
    void testDashboardCategoryBreakdown() {
        ModerationDashboard dashboard = new ModerationDashboard();
        
        List<ModerationDashboard.CategoryCount> breakdown = new ArrayList<>();
        
        ModerationDashboard.CategoryCount spamCount = new ModerationDashboard.CategoryCount();
        spamCount.setCategory(ModerationCategory.SPAM);
        spamCount.setCount(20);
        spamCount.setPercentage(40.0);
        breakdown.add(spamCount);
        
        ModerationDashboard.CategoryCount hateCount = new ModerationDashboard.CategoryCount();
        hateCount.setCategory(ModerationCategory.HATE_SPEECH);
        hateCount.setCount(10);
        hateCount.setPercentage(20.0);
        breakdown.add(hateCount);
        
        dashboard.setCategoryBreakdown(breakdown);
        
        assertEquals(2, dashboard.getCategoryBreakdown().size());
        assertEquals(40.0, dashboard.getCategoryBreakdown().get(0).getPercentage(), 0.001);
    }

    @Test
    void testCensoredSegmentDetails() {
        ContentCensorResult.CensoredSegment segment = new ContentCensorResult.CensoredSegment();
        segment.setStartIndex(10);
        segment.setEndIndex(20);
        segment.setOriginalText("offensive");
        segment.setCensoredText("*********");
        segment.setCategory(ModerationCategory.HATE_SPEECH);
        segment.setReason("Hate speech content");

        assertEquals(10, segment.getStartIndex());
        assertEquals(20, segment.getEndIndex());
        assertEquals("offensive", segment.getOriginalText());
        assertEquals(ModerationCategory.HATE_SPEECH, segment.getCategory());
    }
}