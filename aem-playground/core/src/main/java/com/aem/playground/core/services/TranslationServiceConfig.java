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
    name = "Translation Service Configuration",
    description = "Configuration for the AI Content Translation Service"
)
public @interface TranslationServiceConfig {

    @AttributeDefinition(
        name = "Default Target Language",
        description = "Default target language for translation",
        type = AttributeDefinition.STRING
    )
    String defaultTargetLanguage() default "en";

    @AttributeDefinition(
        name = "Cache Enabled",
        description = "Enable caching for translated content",
        type = AttributeDefinition.BOOLEAN
    )
    boolean cachingEnabled() default true;

    @AttributeDefinition(
        name = "Cache Max Size",
        description = "Maximum number of cached translation entries",
        type = AttributeDefinition.INTEGER
    )
    int cacheMaxSize() default 500;

    @AttributeDefinition(
        name = "Cache TTL (minutes)",
        description = "Time to live for cached translations in minutes",
        type = AttributeDefinition.INTEGER
    )
    int cacheTtlMinutes() default 60;

    @AttributeDefinition(
        name = "API Key",
        description = "OpenAI API key for translation",
        type = AttributeDefinition.PASSWORD
    )
    String apiKey() default "";

    @AttributeDefinition(
        name = "Translation Model",
        description = "Model to use for translation",
        type = AttributeDefinition.STRING
    )
    String translationModel() default "gpt-4";

    @AttributeDefinition(
        name = "Temperature",
        description = "Temperature for translation generation",
        type = AttributeDefinition.DOUBLE
    )
    double temperature() default 0.3;

    @AttributeDefinition(
        name = "Max Tokens",
        description = "Maximum tokens for translation response",
        type = AttributeDefinition.INTEGER
    )
    int maxTokens() default 4000;

    @AttributeDefinition(
        name = "Batch Size",
        description = "Maximum number of items to translate in a single batch",
        type = AttributeDefinition.INTEGER
    )
    int batchSize() default 10;
}