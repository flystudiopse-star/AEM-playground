package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content Scheduler Service",
    description = "AI-powered content scheduling service for AEM"
)
public @interface ContentSchedulerServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for content scheduling"
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
        description = "Default model to use for scheduling",
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
        description = "Enable caching of AI responses",
        defaultValue = "true"
    )
    boolean enableCache();

    @AttributeDefinition(
        name = "Cache Size",
        description = "Maximum number of cached responses",
        defaultValue = "100"
    )
    int cacheSize();

    @AttributeDefinition(
        name = "Default Timezone",
        description = "Default timezone for scheduling",
        defaultValue = "America/New_York"
    )
    String defaultTimezone();

    @AttributeDefinition(
        name = "Engagement Analysis Window Days",
        description = "Number of days to analyze for engagement patterns",
        defaultValue = "30"
    )
    int engagementAnalysisWindow();

    @AttributeDefinition(
        name = "Default Publish Frequency",
        description = "Default publish frequency (daily, weekly, etc.)",
        defaultValue = "weekly"
    )
    String defaultPublishFrequency();

    @AttributeDefinition(
        name = "Posts Per Week",
        description = "Default number of posts per week",
        defaultValue = "5"
    )
    int postsPerWeek();

    @AttributeDefinition(
        name = "AEM Scheduler Name",
        description = "Name of the AEM scheduler to integrate with",
        defaultValue = "aem-content-scheduler"
    )
    String schedulerName();

    @AttributeDefinition(
        name = "Enable Timezone Conversion",
        description = "Enable automatic timezone conversion for scheduling",
        defaultValue = "true"
    )
    boolean enableTimezoneConversion();

    @AttributeDefinition(
        name = "Minimum Engagement Score",
        description = "Minimum engagement score threshold for optimal times",
        defaultValue = "0.5"
    )
    double minEngagementScore();
}