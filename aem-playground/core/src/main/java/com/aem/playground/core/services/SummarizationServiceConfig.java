package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content Summarization Service",
    description = "Configuration for AI-powered content summarization service"
)
public @interface SummarizationServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for content summarization"
    )
    String apiKey() default "";

    @AttributeDefinition(
        name = "Service URL",
        description = "OpenAI API endpoint URL"
    )
    String serviceUrl() default "https://api.openai.com/v1/chat/completions";

    @AttributeDefinition(
        name = "Default Model",
        description = "Default AI model to use for summarization"
    )
    String defaultModel() default "gpt-4";

    @AttributeDefinition(
        name = "Temperature",
        description = "Temperature for AI generation (0.0 to 1.0)"
    )
    float temperature() default 0.5f;

    @AttributeDefinition(
        name = "Max Tokens",
        description = "Maximum tokens for AI response"
    )
    int maxTokens() default 2000;

    @AttributeDefinition(
        name = "Default Summary Length",
        description = "Default word count for summaries (50=brief, 100=standard, 250=detailed)"
    )
    int defaultSummaryLength() default 100;

    @AttributeDefinition(
        name = "Enable AI Integration",
        description = "Enable OpenAI integration for advanced summarization"
    )
    boolean enableAIIntegration() default true;
}