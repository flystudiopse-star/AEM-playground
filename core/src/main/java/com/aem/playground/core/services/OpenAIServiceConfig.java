/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "OpenAI Service Configuration",
    description = "Configuration for the OpenAI GPT-4 and DALL-E 3 service"
)
public @interface OpenAIServiceConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for authentication"
    )
    String apiKey() default "";

    @AttributeDefinition(
        name = "Text API Endpoint",
        description = "Endpoint for text generation API"
    )
    String textEndpoint() default "https://api.openai.com/v1/chat/completions";

    @AttributeDefinition(
        name = "Image API Endpoint",
        description = "Endpoint for image generation API"
    )
    String imageEndpoint() default "https://api.openai.com/v1/images/generations";

    @AttributeDefinition(
        name = "Default Text Model",
        description = "Default model for text generation"
    )
    String defaultModel() default "gpt-4";

    @AttributeDefinition(
        name = "Default Image Model",
        description = "Default model for image generation"
    )
    String defaultImageModel() default "dall-e-3";

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