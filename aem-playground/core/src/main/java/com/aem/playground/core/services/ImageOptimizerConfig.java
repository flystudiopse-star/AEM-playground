package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Image Optimizer Configuration",
    description = "Configuration for AI-powered image optimization service"
)
public @interface ImageOptimizerConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for vision model access"
    )
    String apiKey();

    @AttributeDefinition(
        name = "Vision Endpoint",
        description = "OpenAI vision API endpoint",
        defaultValue = "https://api.openai.com/v1/chat/completions"
    )
    String visionEndpoint();

    @AttributeDefinition(
        name = "Vision Model",
        description = "Model to use for image analysis",
        defaultValue = "gpt-4o"
    )
    String visionModel();

    @AttributeDefinition(
        name = "Default Quality",
        description = "Default quality for optimized images",
        defaultValue = "85"
    )
    int defaultQuality();

    @AttributeDefinition(
        name = "Max Image Width",
        description = "Maximum width for optimized images",
        defaultValue = "1920"
    )
    int maxWidth();

    @AttributeDefinition(
        name = "Max Image Height",
        description = "Maximum height for optimized images",
        defaultValue = "1080"
    )
    int maxHeight();

    @AttributeDefinition(
        name = "Default Format",
        description = "Default output format (webp, jpeg, png)",
        defaultValue = "webp"
    )
    String defaultFormat();

    @AttributeDefinition(
        name = "Generate Responsive Variants",
        description = "Enable responsive variant generation",
        defaultValue = "true"
    )
    boolean generateResponsiveVariants();

    @AttributeDefinition(
        name = "Breakpoints",
        description = "Comma-separated breakpoint definitions (e.g., 320,768,1024,1920)",
        defaultValue = "320,768,1024,1920"
    )
    String breakpoints();

    @AttributeDefinition(
        name = "Default Alt Text Prompt",
        description = "Prompt template for alt text generation",
        defaultValue = "Describe this image for accessibility purposes in one concise sentence."
    )
    String altTextPrompt();

    @AttributeDefinition(
        name = "Enable Analysis Caching",
        description = "Cache image analysis results",
        defaultValue = "true"
    )
    boolean enableCaching();

    @AttributeDefinition(
        name = "Cache Size",
        description = "Maximum number of cached analysis results",
        defaultValue = "100"
    )
    int cacheSize();
}