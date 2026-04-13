package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Workflow Automation Service",
    description = "AI-powered workflow automation service for AEM"
)
public @interface WorkflowAutomationServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for workflow automation"
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
        description = "Default model to use for workflow automation",
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
        name = "Simple Change Threshold",
        description = "Complexity threshold below which changes are auto-approved",
        defaultValue = "0.3"
    )
    double simpleChangeThreshold();

    @AttributeDefinition(
        name = "Auto-Approve Enabled",
        description = "Enable automatic approval of simple content changes",
        defaultValue = "true"
    )
    boolean autoApproveEnabled();

    @AttributeDefinition(
        name = "Default Approval Workflow",
        description = "Default workflow model for content approval",
        defaultValue = "models/content-approval"
    )
    String defaultApprovalWorkflow();

    @AttributeDefinition(
        name = "Default Review Workflow",
        description = "Default workflow model for content review",
        defaultValue = "models/content-review"
    )
    String defaultReviewWorkflow();
}