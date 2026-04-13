package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;

import java.util.List;

public interface ContentRecommenderService {

    UserBehaviorProfile analyzeUserBehavior(String userId, String sessionId);

    List<ContentRecommendation> getPersonalizedRecommendations(String userId, int maxRecommendations);

    ContentAffinityScore calculateContentAffinity(String userId, String contentPath);

    RelatedContentRecommendation suggestRelatedContent(String contentPath, int maxResults);

    CollaborativeFilterResult performCollaborativeFiltering(String userId, int maxResults);

    RecommendationDashboard generateRecommendationDashboard();
}