package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "AI Error Detection Service", description = "AI-powered error detection service for AEM content")
public @interface ErrorDetectionServiceConfig {

    @AttributeDefinition(name = "AI Service URL", description = "URL of the AI service endpoint")
    String ai_service_url() default "https://api.openai.com/v1/chat/completions";

    @AttributeDefinition(name = "API Key", description = "API key for the AI service")
    String api_key();

    @AttributeDefinition(name = "Model", description = "AI model to use for error detection")
    String model() default "gpt-4";

    @AttributeDefinition(name = "Max Tokens", description = "Maximum tokens for AI response")
    int max_tokens() default 2000;

    @AttributeDefinition(name = "Temperature", description = "Temperature for AI generation")
    double temperature() default 0.3;

    @AttributeDefinition(name = "Enable Broken Link Detection", description = "Enable AI-powered broken link detection")
    boolean enable_broken_link_detection() default true;

    @AttributeDefinition(name = "Enable Missing Asset Detection", description = "Enable missing asset detection")
    boolean enable_missing_asset_detection() default true;

    @AttributeDefinition(name = "Enable Structure Issue Detection", description = "Enable content structure issue detection")
    boolean enable_structure_issue_detection() default true;

    @AttributeDefinition(name = "Enable Authoring Error Detection", description = "Enable authoring error detection")
    boolean enable_authoring_error_detection() default true;

    @AttributeDefinition(name = "Enable Auto Fix Suggestions", description = "Enable automatic fix suggestions")
    boolean enable_auto_fix_suggestions() default true;

    @AttributeDefinition(name = "Enable Error Dashboard", description = "Enable error monitoring dashboard")
    boolean enable_error_dashboard() default true;

    @AttributeDefinition(name = "Cache Enabled", description = "Enable caching for error detection results")
    boolean cache_enabled() default true;

    @AttributeDefinition(name = "Cache Size", description = "Maximum number of cached error detection results")
    int cache_size() default 100;

    @AttributeDefinition(name = "Error History Days", description = "Number of days to keep error history")
    int error_history_days() default 30;
}