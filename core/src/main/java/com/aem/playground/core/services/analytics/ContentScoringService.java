package com.aem.playground.core.services.analytics;

import org.apache.sling.api.resource.Resource;

import java.util.List;

public interface ContentScoringService {

    ContentScore scoreContent(String contentPath);

    ContentScore scoreContentFromResource(Resource resource);

    List<ContentScore> batchScoreContent(List<String> contentPaths);

    ContentScore.QualityDetails analyzeQuality(String contentPath);

    ContentScore.SEODetails analyzeSEO(String contentPath);

    ContentScore.EngagementDetails analyzeEngagement(String contentPath);
}