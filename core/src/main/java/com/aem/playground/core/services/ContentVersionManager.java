package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.ContentDrift;
import com.aem.playground.core.services.dto.VersionComparison;
import com.aem.playground.core.services.dto.VersionRecommendation;
import com.aem.playground.core.services.dto.VersionRestorePoint;
import com.aem.playground.core.services.dto.VersionSummary;

import java.util.List;

public interface ContentVersionManager {

    VersionComparison compareVersions(String contentPath, String versionId1, String versionId2);

    VersionSummary generateVersionSummary(String contentPath, String versionId);

    VersionSummary generateVersionSummary(String contentPath, String versionId, boolean includeDetails);

    List<VersionRecommendation> suggestVersionActions(String contentPath, List<String> versionIds);

    ContentDrift detectContentDrift(String contentPath, String baseVersionId, String currentVersionId);

    ContentDrift detectContentDrift(String contentPath, String baseVersionId, String currentVersionId, double threshold);

    VersionRestorePoint createRestorePoint(String contentPath, String versionId);

    VersionRestorePoint createIntelligentRestorePoint(String contentPath, String currentVersionId);

    List<VersionRestorePoint> listRestorePoints(String contentPath);

    boolean restoreFromPoint(String contentPath, String restorePointId);
}