package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content Moderation Service",
    description = "AI-powered content moderation service for AEM"
)
public @interface ContentModerationServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for content moderation"
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
        description = "Default model to use for content moderation",
        defaultValue = "gpt-4"
    )
    String defaultModel();

    @AttributeDefinition(
        name = "Temperature",
        description = "Temperature for text generation",
        defaultValue = "0.3"
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
        name = "Auto-Censor Enabled",
        description = "Enable automatic censorship of sensitive content",
        defaultValue = "true"
    )
    boolean autoCensorEnabled();

    @AttributeDefinition(
        name = "Moderation Sensitivity",
        description = "Sensitivity level for content moderation (0.0-1.0)",
        defaultValue = "0.5"
    )
    double moderationSensitivity();

    @AttributeDefinition(
        name = "Workflow Trigger Enabled",
        description = "Enable workflow triggers for flagged content",
        defaultValue = "true"
    )
    boolean workflowTriggerEnabled();

    @AttributeDefinition(
        name = "Moderation Workflow Model",
        description = "Workflow model for content moderation",
        defaultValue = "models/content-moderation"
    )
    String moderationWorkflowModel();

    @AttributeDefinition(
        name = "Approval Queue Enabled",
        description = "Enable content approval queue",
        defaultValue = "true"
    )
    boolean approvalQueueEnabled();

    @AttributeDefinition(
        name = "Report Generation Enabled",
        description = "Enable moderation report generation",
        defaultValue = "true"
    )
    boolean reportGenerationEnabled();

    @AttributeDefinition(
        name = "Censorship Character",
        description = "Character to use for censorship",
        defaultValue = "*"
    )
    String censorshipCharacter();
}
