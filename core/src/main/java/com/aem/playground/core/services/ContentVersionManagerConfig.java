package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content Version Manager",
    description = "AI-powered content version manager for AEM"
)
public @interface ContentVersionManagerConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for content version analysis"
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
        description = "Default model to use for version analysis",
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
        description = "Enable caching of version analysis results",
        defaultValue = "true"
    )
    boolean enableCache();

    @AttributeDefinition(
        name = "Cache Size",
        description = "Maximum number of cached results",
        defaultValue = "100"
    )
    int cacheSize();

    @AttributeDefinition(
        name = "Drift Threshold",
        description = "Default threshold for content drift detection",
        defaultValue = "0.3"
    )
    float driftThreshold();

    @AttributeDefinition(
        name = "Auto Restore Points",
        description = "Enable automatic intelligent restore point creation",
        defaultValue = "true"
    )
    boolean autoRestorePoints();
}