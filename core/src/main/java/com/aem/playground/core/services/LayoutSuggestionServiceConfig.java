package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Layout Suggestion Service",
    description = "AI-powered layout suggestion service for AEM"
)
public @interface LayoutSuggestionServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for layout suggestions"
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
        description = "Default model to use for layout suggestions",
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
        description = "Enable caching of layout suggestions",
        defaultValue = "true"
    )
    boolean enableCache();

    @AttributeDefinition(
        name = "Cache Size",
        description = "Maximum number of cached suggestions",
        defaultValue = "100"
    )
    int cacheSize();
}