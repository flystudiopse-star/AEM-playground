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
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
    name = "AI Auto-Tagging Service Configuration",
    description = "Configuration for AI-powered auto-tagging of AEM content with custom taxonomies"
)
public @interface AutoTaggingConfig {

    @AttributeDefinition(
        name = "AI Service URL",
        description = "OpenAI API endpoint for text generation"
    )
    String ai_service_url() default "https://api.openai.com/v1/chat/completions";

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for authentication"
    )
    String api_key();

    @AttributeDefinition(
        name = "AI Model",
        description = "AI model to use for content analysis and tag generation"
    )
    String model() default "gpt-4";

    @AttributeDefinition(
        name = "Max Tags",
        description = "Maximum number of tags to generate per content item"
    )
    int max_tags() default 10;

    @AttributeDefinition(
        name = "Min Confidence",
        description = "Minimum confidence threshold for suggested tags (0.0-1.0)"
    )
    double min_confidence() default 0.5;

    @AttributeDefinition(
        name = "Temperature",
        description = "AI temperature setting for tag generation (0.0-2.0)"
    )
    double temperature() default 0.7;

    @AttributeDefinition(
        name = "Max Tokens",
        description = "Maximum tokens for AI response"
    )
    int max_tokens() default 1000;

    @AttributeDefinition(
        name = "Enable Learning",
        description = "Enable learning from author-provided tags over time"
    )
    boolean enable_learning() default true;

    @AttributeDefinition(
        name = "Learning Rate",
        description = "Rate at which the system learns from user tags (0.0-1.0)"
    )
    double learning_rate() default 0.3;

    @AttributeDefinition(
        name = "Tag Hierarchy",
        description = "JSON configuration for tag hierarchy and custom taxonomies"
    )
    String tag_hierarchy() default "[]";

    @AttributeDefinition(
        name = "Tag Categories",
        description = "JSON configuration for tag categories and allowed tags"
    )
    String tag_categories() default "[]";

    @AttributeDefinition(
        name = "Cache Enabled",
        description = "Enable caching for AI tagging responses"
    )
    boolean cache_enabled() default true;

    @AttributeDefinition(
        name = "Cache Size",
        description = "Maximum number of cached entries"
    )
    int cache_size() default 200;
}