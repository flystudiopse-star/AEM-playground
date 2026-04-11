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

@ObjectClassDefinition(name = "AI DAM Metadata Manager")
public interface DAMMetadataManagerConfig {

    @AttributeDefinition(name = "AI Service URL", description = "OpenAI API endpoint for text generation")
    String ai_service_url() default "https://api.openai.com/v1/chat/completions";

    @AttributeDefinition(name = "API Key", description = "OpenAI API key")
    String api_key();

    @AttributeDefinition(name = "Model", description = "AI model to use for metadata extraction")
    String model() default "gpt-4";

    @AttributeDefinition(name = "Max Keywords", description = "Maximum number of keywords to generate per asset")
    int max_keywords() default 10;

    @AttributeDefinition(name = "Min Confidence", description = "Minimum confidence threshold for keywords (0.0-1.0)")
    double min_confidence() default 0.5;

    @AttributeDefinition(name = "Max Tokens", description = "Maximum tokens for AI response")
    int max_tokens() default 2000;

    @AttributeDefinition(name = "Temperature", description = "AI temperature setting (0.0-2.0)")
    double temperature() default 0.7;

    @AttributeDefinition(name = "Enable Smart Keywords", description = "Enable AI-powered smart keyword generation")
    boolean enable_smart_keywords() default true;

    @AttributeDefinition(name = "Enable Categories", description = "Enable content-based asset categorization")
    boolean enable_categories() default true;

    @AttributeDefinition(name = "Enable Relationships", description = "Enable asset relationship suggestions")
    boolean enable_relationships() default true;

    @AttributeDefinition(name = "Enable Intelligent Tagging", description = "Enable intelligent asset tagging")
    boolean enable_intelligent_tagging() default true;

    @AttributeDefinition(name = "Enable Metadata Extraction", description = "Enable AI-powered metadata extraction")
    boolean enable_metadata_extraction() default true;

    @AttributeDefinition(name = "Cache Enabled", description = "Enable caching for AI responses")
    boolean cache_enabled() default true;

    @AttributeDefinition(name = "Cache Size", description = "Maximum number of cached entries")
    int cache_size() default 100;
}