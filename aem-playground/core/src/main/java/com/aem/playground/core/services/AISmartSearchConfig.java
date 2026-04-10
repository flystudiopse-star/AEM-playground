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

@ObjectClassDefinition(name = "AI Smart Search Service Configuration")
public @interface AISmartSearchConfig {

    @AttributeDefinition(name = "API Key", description = "OpenAI API Key for embeddings")
    String apiKey();

    @AttributeDefinition(name = "Embeddings Endpoint", description = "OpenAI embeddings endpoint")
    String embeddingsEndpoint() default "https://api.openai.com/v1/embeddings";

    @AttributeDefinition(name = "Embedding Model", description = "Model to use for embeddings")
    String embeddingModel() default "text-embedding-ada-002";

    @AttributeDefinition(name = "Max Index Size", description = "Maximum number of content items to index")
    int maxIndexSize() default 10000;

    @AttributeDefinition(name = "Suggestion Count", description = "Number of suggestions to return")
    int suggestionCount() default 5;

    @AttributeDefinition(name = "Search Result Count", description = "Default number of search results")
    int defaultSearchResults() default 10;

    @AttributeDefinition(name = "Min Score Threshold", description = "Minimum relevance score for results")
    double minScoreThreshold() default 0.5;
}