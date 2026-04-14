package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "MiniMax Service Configuration",
    description = "Configuration for MiniMax AI API (text and image generation)"
)
public @interface MiniMaxServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "MiniMax API key (or environment variable name prefixed with $)"
    )
    String apiKey() default "";

    @AttributeDefinition(
        name = "API Key Env Variable",
        description = "Environment variable name containing the API key (e.g. MINIMAX_API_KEY)"
    )
    String apiKeyEnvVar() default "MINIMAX_API_KEY";

    @AttributeDefinition(
        name = "Text API Endpoint",
        description = "Endpoint for text generation API"
    )
    String textEndpoint() default "https://api.minimax.chat/v1/text/chatcompletion_pro";

    @AttributeDefinition(
        name = "Image API Endpoint",
        description = "Endpoint for image generation API"
    )
    String imageEndpoint() default "https://api.minimax.chat/v1/image/gen";

    @AttributeDefinition(
        name = "Default Text Model",
        description = "Default model for text generation (e.g. MiniMax-Text-01)"
    )
    String defaultModel() default "MiniMax-Text-01";

    @AttributeDefinition(
        name = "Default Image Model",
        description = "Default model for image generation (e.g. gamepainter)"
    )
    String defaultImageModel() default "gamepainter";

    @AttributeDefinition(
        name = "Group ID",
        description = "MiniMax Group ID for API authentication"
    )
    String groupId() default "";

    @AttributeDefinition(
        name = "Cache Enabled",
        description = "Enable caching for API responses"
    )
    boolean cachingEnabled() default true;

    @AttributeDefinition(
        name = "Cache Max Size",
        description = "Maximum number of cached entries"
    )
    int cacheMaxSize() default 100;
}
