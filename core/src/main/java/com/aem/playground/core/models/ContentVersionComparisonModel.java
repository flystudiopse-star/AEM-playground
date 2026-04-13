package com.aem.playground.core.models;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.playground.core.services.ContentVersionManager;
import com.aem.playground.core.services.dto.ContentDrift;
import com.aem.playground.core.services.dto.VersionComparison;
import com.aem.playground.core.services.dto.VersionRecommendation;
import com.aem.playground.core.services.dto.VersionRestorePoint;
import com.aem.playground.core.services.dto.VersionSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Model(adaptables = Resource.class)
public class ContentVersionComparisonModel {

    private static final Logger log = LoggerFactory.getLogger(ContentVersionComparisonModel.class);

    @ValueMapValue(name = PROPERTY_RESOURCE_TYPE, injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(values = "aem-playground/components/version-comparison")
    protected String resourceType;

    @Inject
    private ContentVersionManager contentVersionManager;

    @Inject
    private Resource resource;

    private String contentPath;
    private String version1;
    private String version2;
    private VersionComparison comparison;
    private VersionSummary summary1;
    private VersionSummary summary2;
    private ContentDrift drift;
    private List<VersionRecommendation> recommendations;
    private List<VersionRestorePoint> restorePoints;
    private boolean isInitialized;

    @PostConstruct
    protected void init() {
        log.debug("Initializing ContentVersionComparisonModel");
        initialize(null, null, null);
    }

    public void initialize(String path, String v1, String v2) {
        this.contentPath = path;
        this.version1 = v1;
        this.version2 = v2;
        
        if (contentVersionManager != null && path != null && v1 != null && v2 != null) {
            this.comparison = contentVersionManager.compareVersions(path, v1, v2);
            this.summary1 = contentVersionManager.generateVersionSummary(path, v1, true);
            this.summary2 = contentVersionManager.generateVersionSummary(path, v2, true);
            this.drift = contentVersionManager.detectContentDrift(path, v1, v2);
            
            if (v1 != null) {
                List<String> versionIds = new ArrayList<>();
                versionIds.add(v1);
                if (v2 != null) {
                    versionIds.add(v2);
                }
                this.recommendations = contentVersionManager.suggestVersionActions(path, versionIds);
            }
            
            this.restorePoints = contentVersionManager.listRestorePoints(path);
            this.isInitialized = true;
        }
    }

    public String getContentPath() {
        return contentPath;
    }

    public String getVersion1() {
        return version1;
    }

    public String getVersion2() {
        return version2;
    }

    public VersionComparison getComparison() {
        if (comparison == null && contentVersionManager != null && contentPath != null && version1 != null && version2 != null) {
            comparison = contentVersionManager.compareVersions(contentPath, version1, version2);
        }
        return comparison;
    }

    public VersionSummary getSummary1() {
        if (summary1 == null && contentVersionManager != null && contentPath != null && version1 != null) {
            summary1 = contentVersionManager.generateVersionSummary(contentPath, version1, true);
        }
        return summary1;
    }

    public VersionSummary getSummary2() {
        if (summary2 == null && contentVersionManager != null && contentPath != null && version2 != null) {
            summary2 = contentVersionManager.generateVersionSummary(contentPath, version2, true);
        }
        return summary2;
    }

    public ContentDrift getDrift() {
        if (drift == null && contentVersionManager != null && contentPath != null && version1 != null && version2 != null) {
            drift = contentVersionManager.detectContentDrift(contentPath, version1, version2);
        }
        return drift;
    }

    public List<VersionRecommendation> getRecommendations() {
        if (recommendations == null && contentVersionManager != null && contentPath != null) {
            recommendations = contentVersionManager.suggestVersionActions(contentPath, getAllVersionIds());
        }
        return recommendations != null ? recommendations : Collections.emptyList();
    }

    public List<VersionRestorePoint> getRestorePoints() {
        if (restorePoints == null && contentVersionManager != null && contentPath != null) {
            restorePoints = contentVersionManager.listRestorePoints(contentPath);
        }
        return restorePoints != null ? restorePoints : Collections.emptyList();
    }

    public List<String> getAllVersionIds() {
        List<String> versionIds = new ArrayList<>();
        if (version1 != null) {
            versionIds.add(version1);
        }
        if (version2 != null) {
            versionIds.add(version2);
        }
        return versionIds;
    }

    public VersionComparison compareVersions(String v1, String v2) {
        if (contentVersionManager == null) {
            return null;
        }
        return contentVersionManager.compareVersions(contentPath, v1, v2);
    }

    public VersionSummary generateVersionSummary(String versionId) {
        if (contentVersionManager == null) {
            return null;
        }
        return contentVersionManager.generateVersionSummary(contentPath, versionId, true);
    }

    public ContentDrift detectContentDrift(String baseVersionId, String currentVersionId) {
        if (contentVersionManager == null) {
            return null;
        }
        return contentVersionManager.detectContentDrift(contentPath, baseVersionId, currentVersionId);
    }

    public VersionRestorePoint createRestorePoint(String versionId) {
        if (contentVersionManager == null) {
            return null;
        }
        return contentVersionManager.createRestorePoint(contentPath, versionId);
    }

    public VersionRestorePoint createIntelligentRestorePoint(String versionId) {
        if (contentVersionManager == null) {
            return null;
        }
        return contentVersionManager.createIntelligentRestorePoint(contentPath, versionId);
    }

    public boolean restoreFromPoint(String restorePointId) {
        if (contentVersionManager == null) {
            return false;
        }
        return contentVersionManager.restoreFromPoint(contentPath, restorePointId);
    }

    public Map<String, Object> getComparisonJson() {
        VersionComparison comp = getComparison();
        Map<String, Object> result = new HashMap<>();
        
        if (comp != null) {
            result.put("contentPath", comp.getContentPath());
            result.put("versionId1", comp.getVersionId1());
            result.put("versionId2", comp.getVersionId2());
            result.put("similarityScore", comp.getSimilarityScore());
            result.put("summary", comp.getSummary());
            result.put("differences", comp.getDifferences());
            result.put("metadata", comp.getMetadata());
        }
        
        return result;
    }

    public Map<String, Object> getDriftJson() {
        ContentDrift driftResult = getDrift();
        Map<String, Object> result = new HashMap<>();
        
        if (driftResult != null) {
            result.put("contentPath", driftResult.getContentPath());
            result.put("baseVersionId", driftResult.getBaseVersionId());
            result.put("currentVersionId", driftResult.getCurrentVersionId());
            result.put("driftScore", driftResult.getDriftScore());
            result.put("status", driftResult.getStatus());
            result.put("driftReason", driftResult.getDriftReason());
            result.put("driftDetails", driftResult.getDriftDetails());
            result.put("detectedAt", driftResult.getDetectedAt());
        }
        
        return result;
    }

    public Map<String, Object> getSummaryJson(String versionId) {
        VersionSummary summary = generateVersionSummary(versionId);
        Map<String, Object> result = new HashMap<>();
        
        if (summary != null) {
            result.put("versionId", summary.getVersionId());
            result.put("summary", summary.getSummary());
            result.put("keyChanges", summary.getKeyChanges());
            result.put("aiConfidence", summary.getAiConfidence());
            result.put("versionLabel", summary.getVersionLabel());
            result.put("timestamp", summary.getTimestamp());
            result.put("author", summary.getAuthor());
            result.put("contentMetadata", summary.getContentMetadata());
        }
        
        return result;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isReady() {
        return contentVersionManager != null;
    }
}