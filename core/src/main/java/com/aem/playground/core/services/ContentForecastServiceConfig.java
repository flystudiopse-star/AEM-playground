package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content Forecasting Service",
    description = "AI-powered content forecasting service for AEM"
)
public @interface ContentForecastServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for content forecasting"
    )
    String apiKey();

    @AttributeDefinition(
        name = "Service URL",
        description = "OpenAI API endpoint URL",
        defaultValue = "https://api.openai.com/v1/chat/completions"
    )
    String serviceUrl();

    @AttributeDefinition(
        name = "Default Model",
        description = "Default model to use for forecasting",
        defaultValue = "gpt-4"
    )
    String defaultModel();

    @AttributeDefinition(
        name = "Temperature",
        description = "Temperature for text generation",
        defaultValue = "0.7"
    )
    float temperature();

    @AttributeDefinition(
        name = "Max Tokens",
        description = "Maximum number of tokens for response",
        defaultValue = "4000"
    )
    int maxTokens();

    @AttributeDefinition(
        name = "Enable Cache",
        description = "Enable caching of forecast results",
        defaultValue = "true"
    )
    boolean enableCache();

    @AttributeDefinition(
        name = "Cache Size",
        description = "Maximum number of cached forecasts",
        defaultValue = "100"
    )
    int cacheSize();

    @AttributeDefinition(
        name = "Forecast Days Ahead",
        description = "Default number of days to forecast",
        defaultValue = "30"
    )
    int forecastDaysAhead();

    @AttributeDefinition(
        name = "Trending Topics Limit",
        description = "Maximum number of trending topics to track",
        defaultValue = "20"
    )
    int trendingTopicsLimit();
}
