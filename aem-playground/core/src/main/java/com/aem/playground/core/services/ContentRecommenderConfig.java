package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content Recommender Service",
    description = "AI-powered content recommendation engine for AEM"
)
public @interface ContentRecommenderConfig {

    @AttributeDefinition(
        name = "OpenAI API Key",
        description = "OpenAI API key for AI-powered recommendations"
    )
    String apiKey();

    @AttributeDefinition(
        name = "Default Model",
        description = "OpenAI model to use for recommendations"
    )
    String defaultModel() default "gpt-4";

    @AttributeDefinition(
        name = "Max Recommendations",
        description = "Maximum number of recommendations to return per request"
    )
    int maxRecommendations() default 10;

    @AttributeDefinition(
        name = "Min Relevance Threshold",
        description = "Minimum relevance score for a recommendation to be included"
    )
    double minRelevanceThreshold() default 0.3;

    @AttributeDefinition(
        name = "Similarity Threshold",
        description = "Minimum similarity score for collaborative filtering"
    )
    double similarityThreshold() default 0.5;

    @AttributeDefinition(
        name = "Max Similar Users",
        description = "Maximum number of similar users for collaborative filtering"
    )
    int maxSimilarUsers() default 20;

    @AttributeDefinition(
        name = "Enable Caching",
        description = "Enable caching of recommendation results"
    )
    boolean enableCaching() default true;

    @AttributeDefinition(
        name = "Cache Size",
        description = "Maximum number of cached recommendation results"
    )
    int cacheSize() default 100;

    @AttributeDefinition(
        name = "Content Analysis Prompt",
        description = "Custom prompt for content analysis"
    )
    String contentAnalysisPrompt() default "Analyze the following content and identify key topics and themes:";

    @AttributeDefinition(
        name = "Enable Collaborative Filtering",
        description = "Enable collaborative filtering algorithm"
    )
    boolean enableCollaborativeFiltering() default true;

    @AttributeDefinition(
        name = "Personalization Engine Enabled",
        description = "Enable integration with AEM personalization engine"
    )
    boolean personalizationEnabled() default true;

    @AttributeDefinition(
        name = "Recommendation TTL",
        description = "Time-to-live for cached recommendations in seconds"
    )
    int recommendationTtl() default 3600;
}