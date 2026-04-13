package com.aem.playground.core.models;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.aem.playground.core.testcontext.AppAemContext;
import com.aem.playground.core.services.ContentVersionManager;
import com.aem.playground.core.services.dto.ContentDrift;
import com.aem.playground.core.services.dto.VersionComparison;
import com.aem.playground.core.services.dto.VersionRecommendation;
import com.aem.playground.core.services.dto.VersionRestorePoint;
import com.aem.playground.core.services.dto.VersionSummary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(AemContextExtension.class)
class ContentVersionComparisonModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    @Mock
    private ContentVersionManager contentVersionManager;

    private Resource resource;
    private ContentVersionComparisonModel model;

    @BeforeEach
    void setup() {
        resource = context.create().resource("/content/version-test",
            "sling:resourceType", "aem-playground/components/version-comparison",
            "contentPath", "/content/page",
            "version1", "1.0",
            "version2", "2.0");

        context.getServiceTracker().registerService(contentVersionManager, ContentVersionManager.class);

        model = resource.adaptTo(ContentVersionComparisonModel.class);
    }

    @Test
    void testInitialization() {
        assertNotNull(model);
    }

    @Test
    void testGetContentPath() {
        model.initialize("/content/page", "1.0", "2.0");
        assertEquals("/content/page", model.getContentPath());
    }

    @Test
    void testGetVersion1() {
        model.initialize("/content/page", "1.0", "2.0");
        assertEquals("1.0", model.getVersion1());
    }

    @Test
    void testGetVersion2() {
        model.initialize("/content/page", "1.0", "2.0");
        assertEquals("2.0", model.getVersion2());
    }

    @Test
    void testGetComparison() {
        VersionComparison comparison = new VersionComparison();
        comparison.setContentPath("/content/page");
        comparison.setVersionId1("1.0");
        comparison.setVersionId2("2.0");
        comparison.setSimilarityScore(0.85);
        comparison.setSummary("Content is 85% similar");

        when(contentVersionManager.compareVersions("/content/page", "1.0", "2.0")).thenReturn(comparison);

        model.initialize("/content/page", "1.0", "2.0");

        VersionComparison result = model.getComparison();
        assertNotNull(result);
        assertEquals("/content/page", result.getContentPath());
        assertEquals("1.0", result.getVersionId1());
        assertEquals("2.0", result.getVersionId2());
        assertEquals(0.85, result.getSimilarityScore());
    }

    @Test
    void testGetSummary1() {
        VersionSummary summary = new VersionSummary();
        summary.setVersionId("1.0");
        summary.setSummary("Version 1 summary");
        summary.setContentPath("/content/page");

        when(contentVersionManager.generateVersionSummary("/content/page", "1.0", true)).thenReturn(summary);

        model.initialize("/content/page", "1.0", "2.0");

        VersionSummary result = model.getSummary1();
        assertNotNull(result);
        assertEquals("1.0", result.getVersionId());
        assertEquals("Version 1 summary", result.getSummary());
    }

    @Test
    void testGetSummary2() {
        VersionSummary summary = new VersionSummary();
        summary.setVersionId("2.0");
        summary.setSummary("Version 2 summary");
        summary.setContentPath("/content/page");

        when(contentVersionManager.generateVersionSummary("/content/page", "2.0", true)).thenReturn(summary);

        model.initialize("/content/page", "1.0", "2.0");

        VersionSummary result = model.getSummary2();
        assertNotNull(result);
        assertEquals("2.0", result.getVersionId());
    }

    @Test
    void testGetDrift() {
        ContentDrift drift = new ContentDrift();
        drift.setContentPath("/content/page");
        drift.setBaseVersionId("1.0");
        drift.setCurrentVersionId("2.0");
        drift.setDriftScore(0.15);
        drift.setStatus("MODERATE");

        when(contentVersionManager.detectContentDrift("/content/page", "1.0", "2.0")).thenReturn(drift);

        model.initialize("/content/page", "1.0", "2.0");

        ContentDrift result = model.getDrift();
        assertNotNull(result);
        assertEquals("MODERATE", result.getStatus());
        assertEquals(0.15, result.getDriftScore());
    }

    @Test
    void testGetRecommendations() {
        List<VersionRecommendation> recommendations = new ArrayList<>();
        VersionRecommendation rec = new VersionRecommendation();
        rec.setAction("RESTORE");
        rec.setReason("Significant drift detected");
        recommendations.add(rec);

        List<String> versionIds = new ArrayList<>();
        versionIds.add("1.0");
        versionIds.add("2.0");

        when(contentVersionManager.suggestVersionActions("/content/page", versionIds)).thenReturn(recommendations);

        model.initialize("/content/page", "1.0", "2.0");

        List<VersionRecommendation> result = model.getRecommendations();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("RESTORE", result.get(0).getAction());
    }

    @Test
    void testGetRecommendationsEmptyWhenNull() {
        model.initialize("/content/page", "1.0", "2.0");

        when(contentVersionManager.suggestVersionActions(any(), any())).thenReturn(null);

        List<VersionRecommendation> result = model.getRecommendations();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRestorePoints() {
        List<VersionRestorePoint> restorePoints = new ArrayList<>();
        VersionRestorePoint point = new VersionRestorePoint();
        point.setId("point-1");
        point.setVersionId("1.0");
        restorePoints.add(point);

        when(contentVersionManager.listRestorePoints("/content/page")).thenReturn(restorePoints);

        model.initialize("/content/page", "1.0", "2.0");

        List<VersionRestorePoint> result = model.getRestorePoints();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("point-1", result.get(0).getId());
    }

    @Test
    void testGetRestorePointsEmptyWhenNull() {
        model.initialize("/content/page", "1.0", "2.0");

        when(contentVersionManager.listRestorePoints(any())).thenReturn(null);

        List<VersionRestorePoint> result = model.getRestorePoints();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetComparisonJson() {
        VersionComparison comparison = new VersionComparison();
        comparison.setContentPath("/content/page");
        comparison.setVersionId1("1.0");
        comparison.setVersionId2("2.0");
        comparison.setSimilarityScore(0.85);
        comparison.setSummary("Test summary");
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        comparison.setMetadata(meta);

        when(contentVersionManager.compareVersions("/content/page", "1.0", "2.0")).thenReturn(comparison);

        model.initialize("/content/page", "1.0", "2.0");

        Map<String, Object> json = model.getComparisonJson();
        assertNotNull(json);
        assertEquals("/content/page", json.get("contentPath"));
        assertEquals("1.0", json.get("versionId1"));
        assertEquals("2.0", json.get("versionId2"));
        assertEquals(0.85, json.get("similarityScore"));
        assertEquals("Test summary", json.get("summary"));
    }

    @Test
    void testGetDriftJson() {
        ContentDrift drift = new ContentDrift();
        drift.setContentPath("/content/page");
        drift.setBaseVersionId("1.0");
        drift.setCurrentVersionId("2.0");
        drift.setDriftScore(0.15);
        drift.setStatus("HIGH");
        drift.setDriftReason("Significant changes detected");
        drift.setDetectedAt("2024-01-01T00:00:00Z");

        when(contentVersionManager.detectContentDrift("/content/page", "1.0", "2.0")).thenReturn(drift);

        model.initialize("/content/page", "1.0", "2.0");

        Map<String, Object> json = model.getDriftJson();
        assertNotNull(json);
        assertEquals("/content/page", json.get("contentPath"));
        assertEquals("1.0", json.get("baseVersionId"));
        assertEquals("2.0", json.get("currentVersionId"));
        assertEquals(0.15, json.get("driftScore"));
        assertEquals("HIGH", json.get("status"));
    }

    @Test
    void testGetSummaryJson() {
        VersionSummary summary = new VersionSummary();
        summary.setVersionId("1.0");
        summary.setSummary("Version summary");
        summary.setAiConfidence(0.9);
        summary.setVersionLabel("v1.0");
        summary.setAuthor("admin");
        summary.setTimestamp("2024-01-01T00:00:00Z");

        when(contentVersionManager.generateVersionSummary("/content/page", "1.0", true)).thenReturn(summary);

        model.initialize("/content/page", "1.0", "2.0");

        Map<String, Object> json = model.getSummaryJson("1.0");
        assertNotNull(json);
        assertEquals("1.0", json.get("versionId"));
        assertEquals("Version summary", json.get("summary"));
        assertEquals(0.9, json.get("aiConfidence"));
        assertEquals("v1.0", json.get("versionLabel"));
        assertEquals("admin", json.get("author"));
    }

    @Test
    void testIsInitialized() {
        model.initialize("/content/page", "1.0", "2.0");
        assertTrue(model.isInitialized());
    }

    @Test
    void testIsNotInitializedByDefault() {
        ContentVersionComparisonModel emptyModel = context.create().resource("/content/empty")
            .adaptTo(ContentVersionComparisonModel.class);
        assertFalse(emptyModel.isInitialized());
    }

    @Test
    void testIsReady() {
        assertTrue(model.isReady());
    }

    @Test
    void testCompareVersions() {
        VersionComparison comparison = new VersionComparison();
        comparison.setVersionId1("1.0");
        comparison.setVersionId2("3.0");

        when(contentVersionManager.compareVersions("/content/page", "1.0", "3.0")).thenReturn(comparison);

        model.initialize("/content/page", "1.0", "2.0");

        VersionComparison result = model.compareVersions("1.0", "3.0");
        assertNotNull(result);
    }

    @Test
    void testGenerateVersionSummary() {
        VersionSummary summary = new VersionSummary();
        summary.setVersionId("1.0");

        when(contentVersionManager.generateVersionSummary("/content/page", "1.0", true)).thenReturn(summary);

        model.initialize("/content/page", "1.0", "2.0");

        VersionSummary result = model.generateVersionSummary("1.0");
        assertNotNull(result);
    }

    @Test
    void testDetectContentDrift() {
        ContentDrift drift = new ContentDrift();
        drift.setDriftScore(0.5);

        when(contentVersionManager.detectContentDrift("/content/page", "1.0", "3.0")).thenReturn(drift);

        model.initialize("/content/page", "1.0", "2.0");

        ContentDrift result = model.detectContentDrift("1.0", "3.0");
        assertNotNull(result);
    }

    @Test
    void testCreateRestorePoint() {
        VersionRestorePoint point = new VersionRestorePoint();
        point.setId("new-point");

        when(contentVersionManager.createRestorePoint("/content/page", "1.0")).thenReturn(point);

        model.initialize("/content/page", "1.0", "2.0");

        VersionRestorePoint result = model.createRestorePoint("1.0");
        assertNotNull(result);
    }

    @Test
    void testCreateIntelligentRestorePoint() {
        VersionRestorePoint point = new VersionRestorePoint();
        point.setId("intelligent-point");

        when(contentVersionManager.createIntelligentRestorePoint("/content/page", "1.0")).thenReturn(point);

        model.initialize("/content/page", "1.0", "2.0");

        VersionRestorePoint result = model.createIntelligentRestorePoint("1.0");
        assertNotNull(result);
    }

    @Test
    void testRestoreFromPoint() {
        when(contentVersionManager.restoreFromPoint("/content/page", "point-1")).thenReturn(true);

        model.initialize("/content/page", "1.0", "2.0");

        boolean result = model.restoreFromPoint("point-1");
        assertTrue(result);
    }

    @Test
    void testRestoreFromPointReturnsFalseWhenServiceUnavailable() {
        ContentVersionComparisonModel noServiceModel = context.create().resource("/content/no-service")
            .adaptTo(ContentVersionComparisonModel.class);

        boolean result = noServiceModel.restoreFromPoint("point-1");
        assertFalse(result);
    }

    @Test
    void testGetAllVersionIds() {
        model.initialize("/content/page", "1.0", "2.0");

        List<String> ids = model.getAllVersionIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("1.0"));
        assertTrue(ids.contains("2.0"));
    }

    @Test
    void testGetAllVersionIdsSingleVersion() {
        model.initialize("/content/page", "1.0", null);

        List<String> ids = model.getAllVersionIds();
        assertEquals(1, ids.size());
        assertEquals("1.0", ids.get(0));
    }

    @Test
    void testGetComparisonJsonReturnsEmptyMapWhenComparisonNull() {
        when(contentVersionManager.compareVersions(any(), any(), any())).thenReturn(null);

        model.initialize("/content/page", "1.0", "2.0");

        Map<String, Object> json = model.getComparisonJson();
        assertNotNull(json);
        assertTrue(json.isEmpty());
    }

    @Test
    void testGetDriftJsonReturnsEmptyMapWhenDriftNull() {
        when(contentVersionManager.detectContentDrift(any(), any(), any())).thenReturn(null);

        model.initialize("/content/page", "1.0", "2.0");

        Map<String, Object> json = model.getDriftJson();
        assertNotNull(json);
        assertTrue(json.isEmpty());
    }
}