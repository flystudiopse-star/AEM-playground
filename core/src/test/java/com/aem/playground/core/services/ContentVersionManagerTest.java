package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentVersionManagerTest {

    @Mock
    private AIService aiService;

    private ContentVersionManager service;

    @BeforeEach
    void setUp() {
        service = new ContentVersionManagerImpl();
    }

    @Test
    void testCompareVersions() {
        VersionComparison comparison = service.compareVersions("/content/page", "1.0", "2.0");

        assertNotNull(comparison);
        assertEquals("/content/page", comparison.getContentPath());
        assertEquals("1.0", comparison.getVersionId1());
        assertEquals("2.0", comparison.getVersionId2());
    }

    @Test
    void testCompareVersionsWithNullContentPath() {
        VersionComparison comparison = service.compareVersions(null, "1.0", "2.0");

        assertNotNull(comparison);
        assertTrue(comparison.getSummary().contains("Error"));
    }

    @Test
    void testCompareVersionsWithNullVersionId1() {
        VersionComparison comparison = service.compareVersions("/content/page", null, "2.0");

        assertNotNull(comparison);
        assertTrue(comparison.getSummary().contains("Error"));
    }

    @Test
    void testCompareVersionsWithNullVersionId2() {
        VersionComparison comparison = service.compareVersions("/content/page", "1.0", null);

        assertNotNull(comparison);
        assertTrue(comparison.getSummary().contains("Error"));
    }

    @Test
    void testCompareVersionsWithBlankContentPath() {
        VersionComparison comparison = service.compareVersions("", "1.0", "2.0");

        assertNotNull(comparison);
        assertTrue(comparison.getSummary().contains("Error"));
    }

    @Test
    void testGenerateVersionSummary() {
        VersionSummary summary = service.generateVersionSummary("/content/page", "1.0");

        assertNotNull(summary);
        assertEquals("/content/page", summary.getContentPath());
        assertEquals("1.0", summary.getVersionId());
    }

    @Test
    void testGenerateVersionSummaryWithDetails() {
        VersionSummary summary = service.generateVersionSummary("/content/page", "1.0", true);

        assertNotNull(summary);
        assertEquals("/content/page", summary.getContentPath());
        assertEquals("1.0", summary.getVersionId());
    }

    @Test
    void testGenerateVersionSummaryWithoutDetails() {
        VersionSummary summary = service.generateVersionSummary("/content/page", "1.0", false);

        assertNotNull(summary);
        assertEquals("/content/page", summary.getContentPath());
        assertEquals("1.0", summary.getVersionId());
    }

    @Test
    void testGenerateVersionSummaryWithNullContentPath() {
        VersionSummary summary = service.generateVersionSummary(null, "1.0");

        assertNotNull(summary);
        assertTrue(summary.getSummary().contains("Error"));
    }

    @Test
    void testGenerateVersionSummaryWithNullVersionId() {
        VersionSummary summary = service.generateVersionSummary("/content/page", null);

        assertNotNull(summary);
        assertTrue(summary.getSummary().contains("Error"));
    }

    @Test
    void testSuggestVersionActions() {
        List<String> versionIds = Arrays.asList("1.0", "2.0", "3.0");
        List<VersionRecommendation> recommendations = service.suggestVersionActions("/content/page", versionIds);

        assertNotNull(recommendations);
        assertEquals(3, recommendations.size());
    }

    @Test
    void testSuggestVersionActionsWithEmptyList() {
        List<String> versionIds = new ArrayList<>();
        List<VersionRecommendation> recommendations = service.suggestVersionActions("/content/page", versionIds);

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testSuggestVersionActionsWithNullList() {
        List<VersionRecommendation> recommendations = service.suggestVersionActions("/content/page", null);

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testSuggestVersionActionsWithSingleVersion() {
        List<String> versionIds = Arrays.asList("1.0");
        List<VersionRecommendation> recommendations = service.suggestVersionActions("/content/page", versionIds);

        assertNotNull(recommendations);
        assertEquals(1, recommendations.size());
    }

    @Test
    void testDetectContentDrift() {
        ContentDrift drift = service.detectContentDrift("/content/page", "1.0", "2.0");

        assertNotNull(drift);
        assertEquals("/content/page", drift.getContentPath());
        assertEquals("1.0", drift.getBaseVersionId());
        assertEquals("2.0", drift.getCurrentVersionId());
    }

    @Test
    void testDetectContentDriftWithThreshold() {
        ContentDrift drift = service.detectContentDrift("/content/page", "1.0", "2.0", 0.5);

        assertNotNull(drift);
        assertEquals("/content/page", drift.getContentPath());
        assertNotNull(drift.getStatus());
    }

    @Test
    void testDetectContentDriftWithNullContentPath() {
        ContentDrift drift = service.detectContentDrift(null, "1.0", "2.0");

        assertNotNull(drift);
        assertTrue(drift.getDriftReason().contains("Error"));
    }

    @Test
    void testDetectContentDriftWithNullBaseVersion() {
        ContentDrift drift = service.detectContentDrift("/content/page", null, "2.0");

        assertNotNull(drift);
        assertTrue(drift.getDriftReason().contains("Error"));
    }

    @Test
    void testDetectContentDriftWithNullCurrentVersion() {
        ContentDrift drift = service.detectContentDrift("/content/page", "1.0", null);

        assertNotNull(drift);
        assertTrue(drift.getDriftReason().contains("Error"));
    }

    @Test
    void testCreateRestorePoint() {
        VersionRestorePoint restorePoint = service.createRestorePoint("/content/page", "1.0");

        assertNotNull(restorePoint);
        assertEquals("/content/page", restorePoint.getContentPath());
        assertEquals("1.0", restorePoint.getVersionId());
        assertEquals(VersionRestorePoint.RestorePointType.MANUAL, restorePoint.getType());
    }

    @Test
    void testCreateRestorePointWithNullContentPath() {
        VersionRestorePoint restorePoint = service.createRestorePoint(null, "1.0");

        assertNull(restorePoint);
    }

    @Test
    void testCreateRestorePointWithNullVersionId() {
        VersionRestorePoint restorePoint = service.createRestorePoint("/content/page", null);

        assertNull(restorePoint);
    }

    @Test
    void testCreateIntelligentRestorePoint() {
        VersionRestorePoint restorePoint = service.createIntelligentRestorePoint("/content/page", "1.0");

        assertNotNull(restorePoint);
        assertEquals("/content/page", restorePoint.getContentPath());
        assertEquals(VersionRestorePoint.RestorePointType.INTELLIGENT_AUTO, restorePoint.getType());
        assertTrue(restorePoint.isIntelligent());
    }

    @Test
    void testCreateIntelligentRestorePointWithNullContentPath() {
        VersionRestorePoint restorePoint = service.createIntelligentRestorePoint(null, "1.0");

        assertNull(restorePoint);
    }

    @Test
    void testCreateIntelligentRestorePointWithNullVersionId() {
        VersionRestorePoint restorePoint = service.createIntelligentRestorePoint("/content/page", null);

        assertNull(restorePoint);
    }

    @Test
    void testListRestorePoints() {
        service.createRestorePoint("/content/page", "1.0");
        service.createRestorePoint("/content/page", "2.0");
        service.createRestorePoint("/content/page", "3.0");

        List<VersionRestorePoint> restorePoints = service.listRestorePoints("/content/page");

        assertNotNull(restorePoints);
        assertEquals(3, restorePoints.size());
    }

    @Test
    void testListRestorePointsForNonExistentContent() {
        List<VersionRestorePoint> restorePoints = service.listRestorePoints("/content/nonexistent");

        assertNotNull(restorePoints);
        assertTrue(restorePoints.isEmpty());
    }

    @Test
    void testRestoreFromPoint_Success() {
        VersionRestorePoint created = service.createRestorePoint("/content/page", "1.0");
        boolean result = service.restoreFromPoint("/content/page", created.getRestorePointId());

        assertTrue(result);
    }

    @Test
    void testRestoreFromPoint_WrongContentPath() {
        VersionRestorePoint created = service.createRestorePoint("/content/page", "1.0");
        boolean result = service.restoreFromPoint("/content/other", created.getRestorePointId());

        assertFalse(result);
    }

    @Test
    void testRestoreFromPoint_NonExistentRestorePoint() {
        boolean result = service.restoreFromPoint("/content/page", "non-existent");

        assertFalse(result);
    }

    @Test
    void testContentDifferenceGetters() {
        ContentDifference diff = new ContentDifference();
        diff.setPath("/content/page/jcr:content");
        diff.setType(ContentDifference.DifferenceType.MODIFIED);
        diff.setOldValue("old text");
        diff.setNewValue("new text");
        diff.setDescription("Text updated");
        diff.setSeverity(0.5);

        assertEquals("/content/page/jcr:content", diff.getPath());
        assertEquals(ContentDifference.DifferenceType.MODIFIED, diff.getType());
        assertEquals("old text", diff.getOldValue());
        assertEquals("new text", diff.getNewValue());
        assertEquals("Text updated", diff.getDescription());
        assertEquals(0.5, diff.getSeverity(), 0.001);
    }

    @Test
    void testContentDifferenceTypes() {
        ContentDifference added = new ContentDifference();
        added.setType(ContentDifference.DifferenceType.ADDED);
        assertEquals(ContentDifference.DifferenceType.ADDED, added.getType());

        ContentDifference removed = new ContentDifference();
        removed.setType(ContentDifference.DifferenceType.REMOVED);
        assertEquals(ContentDifference.DifferenceType.REMOVED, removed.getType());

        ContentDifference moved = new ContentDifference();
        moved.setType(ContentDifference.DifferenceType.MOVED);
        assertEquals(ContentDifference.DifferenceType.MOVED, moved.getType());

        ContentDifference formatChange = new ContentDifference();
        formatChange.setType(ContentDifference.DifferenceType.FORMAT_CHANGE);
        assertEquals(ContentDifference.DifferenceType.FORMAT_CHANGE, formatChange.getType());
    }

    @Test
    void testVersionRecommendationGetters() {
        VersionRecommendation rec = new VersionRecommendation();
        rec.setVersionId("1.0");
        rec.setRecommendation(VersionRecommendation.RecommendationType.KEEP);
        rec.setReason("Latest version");
        rec.setConfidenceScore(0.9);
        rec.setSuggestedActions(Arrays.asList("Keep", "Archive older"));

        assertEquals("1.0", rec.getVersionId());
        assertEquals(VersionRecommendation.RecommendationType.KEEP, rec.getRecommendation());
        assertEquals("Latest version", rec.getReason());
        assertEquals(0.9, rec.getConfidenceScore(), 0.001);
    }

    @Test
    void testVersionRecommendationTypes() {
        VersionRecommendation keep = new VersionRecommendation();
        keep.setRecommendation(VersionRecommendation.RecommendationType.KEEP);
        assertEquals(VersionRecommendation.RecommendationType.KEEP, keep.getRecommendation());

        VersionRecommendation merge = new VersionRecommendation();
        merge.setRecommendation(VersionRecommendation.RecommendationType.MERGE);
        assertEquals(VersionRecommendation.RecommendationType.MERGE, merge.getRecommendation());

        VersionRecommendation delete = new VersionRecommendation();
        delete.setRecommendation(VersionRecommendation.RecommendationType.DELETE);
        assertEquals(VersionRecommendation.RecommendationType.DELETE, delete.getRecommendation());

        VersionRecommendation restore = new VersionRecommendation();
        restore.setRecommendation(VersionRecommendation.RecommendationType.RESTORE);
        assertEquals(VersionRecommendation.RecommendationType.RESTORE, restore.getRecommendation());

        VersionRecommendation review = new VersionRecommendation();
        review.setRecommendation(VersionRecommendation.RecommendationType.REVIEW);
        assertEquals(VersionRecommendation.RecommendationType.REVIEW, review.getRecommendation());
    }

    @Test
    void testContentDriftGetters() {
        ContentDrift drift = new ContentDrift();
        drift.setContentPath("/content/page");
        drift.setBaseVersionId("1.0");
        drift.setCurrentVersionId("2.0");
        drift.setDriftScore(0.5);
        drift.setStatus(ContentDrift.DriftStatus.MINOR_DRIFT);
        drift.setDriftReason("Minor changes detected");

        assertEquals("/content/page", drift.getContentPath());
        assertEquals("1.0", drift.getBaseVersionId());
        assertEquals("2.0", drift.getCurrentVersionId());
        assertEquals(0.5, drift.getDriftScore(), 0.001);
        assertEquals(ContentDrift.DriftStatus.MINOR_DRIFT, drift.getStatus());
        assertEquals("Minor changes detected", drift.getDriftReason());
    }

    @Test
    void testContentDriftStatuses() {
        ContentDrift stable = new ContentDrift();
        stable.setStatus(ContentDrift.DriftStatus.STABLE);
        assertEquals(ContentDrift.DriftStatus.STABLE, stable.getStatus());

        ContentDrift minor = new ContentDrift();
        minor.setStatus(ContentDrift.DriftStatus.MINOR_DRIFT);
        assertEquals(ContentDrift.DriftStatus.MINOR_DRIFT, minor.getStatus());

        ContentDrift moderate = new ContentDrift();
        moderate.setStatus(ContentDrift.DriftStatus.MODERATE_DRIFT);
        assertEquals(ContentDrift.DriftStatus.MODERATE_DRIFT, moderate.getStatus());

        ContentDrift major = new ContentDrift();
        major.setStatus(ContentDrift.DriftStatus.MAJOR_DRIFT);
        assertEquals(ContentDrift.DriftStatus.MAJOR_DRIFT, major.getStatus());

        ContentDrift critical = new ContentDrift();
        critical.setStatus(ContentDrift.DriftStatus.CRITICAL_DRIFT);
        assertEquals(ContentDrift.DriftStatus.CRITICAL_DRIFT, critical.getStatus());
    }

    @Test
    void testDriftDetailGetters() {
        DriftDetail detail = new DriftDetail();
        detail.setPath("/content/page/jcr:content");
        detail.setChangeType("modified");
        detail.setImpactScore(0.5);
        detail.setDescription("Content modified");

        assertEquals("/content/page/jcr:content", detail.getPath());
        assertEquals("modified", detail.getChangeType());
        assertEquals(0.5, detail.getImpactScore(), 0.001);
        assertEquals("Content modified", detail.getDescription());
    }

    @Test
    void testVersionRestorePointGetters() {
        VersionRestorePoint restorePoint = new VersionRestorePoint();
        restorePoint.setRestorePointId("rp-1");
        restorePoint.setContentPath("/content/page");
        restorePoint.setVersionId("1.0");
        restorePoint.setLabel("Test Restore Point");
        restorePoint.setDescription("Test description");
        restorePoint.setTimestamp(System.currentTimeMillis());
        restorePoint.setAuthor("admin");
        restorePoint.setType(VersionRestorePoint.RestorePointType.MANUAL);

        assertEquals("rp-1", restorePoint.getRestorePointId());
        assertEquals("/content/page", restorePoint.getContentPath());
        assertEquals("1.0", restorePoint.getVersionId());
        assertEquals("Test Restore Point", restorePoint.getLabel());
        assertEquals("Test description", restorePoint.getDescription());
        assertEquals("admin", restorePoint.getAuthor());
        assertEquals(VersionRestorePoint.RestorePointType.MANUAL, restorePoint.getType());
    }

    @Test
    void testVersionRestorePointTypes() {
        VersionRestorePoint manual = new VersionRestorePoint();
        manual.setType(VersionRestorePoint.RestorePointType.MANUAL);
        assertEquals(VersionRestorePoint.RestorePointType.MANUAL, manual.getType());

        VersionRestorePoint scheduled = new VersionRestorePoint();
        scheduled.setType(VersionRestorePoint.RestorePointType.SCHEDULED);
        assertEquals(VersionRestorePoint.RestorePointType.SCHEDULED, scheduled.getType());

        VersionRestorePoint intelligentAuto = new VersionRestorePoint();
        intelligentAuto.setType(VersionRestorePoint.RestorePointType.INTELLIGENT_AUTO);
        assertEquals(VersionRestorePoint.RestorePointType.INTELLIGENT_AUTO, intelligentAuto.getType());

        VersionRestorePoint emergency = new VersionRestorePoint();
        emergency.setType(VersionRestorePoint.RestorePointType.EMERGENCY);
        assertEquals(VersionRestorePoint.RestorePointType.EMERGENCY, emergency.getType());
    }

    @Test
    void testVersionComparisonGetters() {
        VersionComparison comparison = new VersionComparison();
        comparison.setContentPath("/content/page");
        comparison.setVersionId1("1.0");
        comparison.setVersionId2("2.0");
        comparison.setSimilarityScore(0.8);
        comparison.setSummary("Test comparison");

        assertEquals("/content/page", comparison.getContentPath());
        assertEquals("1.0", comparison.getVersionId1());
        assertEquals("2.0", comparison.getVersionId2());
        assertEquals(0.8, comparison.getSimilarityScore(), 0.001);
        assertEquals("Test comparison", comparison.getSummary());
    }

    @Test
    void testVersionComparisonWithDifferences() {
        VersionComparison comparison = new VersionComparison();
        
        ContentDifference diff = new ContentDifference();
        diff.setPath("/content/page/paragraph");
        diff.setType(ContentDifference.DifferenceType.MODIFIED);
        diff.setOldValue("old");
        diff.setNewValue("new");

        comparison.setDifferences(Arrays.asList(diff));

        assertEquals(1, comparison.getDifferences().size());
        assertEquals("/content/page/paragraph", comparison.getDifferences().get(0).getPath());
    }

    @Test
    void testVersionSummaryGetters() {
        VersionSummary summary = new VersionSummary();
        summary.setVersionId("1.0");
        summary.setContentPath("/content/page");
        summary.setSummary("Test summary");
        summary.setTimestamp(System.currentTimeMillis());
        summary.setAuthor("admin");
        summary.setAiConfidence(0.85);
        summary.setVersionLabel("v1.0");

        assertEquals("1.0", summary.getVersionId());
        assertEquals("/content/page", summary.getContentPath());
        assertEquals("Test summary", summary.getSummary());
        assertEquals("admin", summary.getAuthor());
        assertEquals(0.85, summary.getAiConfidence(), 0.001);
        assertEquals("v1.0", summary.getVersionLabel());
    }

    @Test
    void testVersionSummaryWithKeyChanges() {
        VersionSummary summary = new VersionSummary();
        summary.setKeyChanges(Arrays.asList("Added heading", "Updated text", "Changed image"));

        assertEquals(3, summary.getKeyChanges().size());
        assertTrue(summary.getKeyChanges().contains("Added heading"));
    }

    @Test
    void testCompareVersionsMultipleTimes() {
        VersionComparison comparison1 = service.compareVersions("/content/page", "1.0", "2.0");
        VersionComparison comparison2 = service.compareVersions("/content/page", "1.0", "2.0");

        assertNotNull(comparison1);
        assertNotNull(comparison2);
    }

    @Test
    void testGenerateVersionSummaryMultipleTimes() {
        VersionSummary summary1 = service.generateVersionSummary("/content/page", "1.0");
        VersionSummary summary2 = service.generateVersionSummary("/content/page", "1.0");

        assertNotNull(summary1);
        assertNotNull(summary2);
    }

    @Test
    void testDetectContentDriftMultipleTimes() {
        ContentDrift drift1 = service.detectContentDrift("/content/page", "1.0", "2.0");
        ContentDrift drift2 = service.detectContentDrift("/content/page", "1.0", "2.0");

        assertNotNull(drift1);
        assertNotNull(drift2);
    }
}