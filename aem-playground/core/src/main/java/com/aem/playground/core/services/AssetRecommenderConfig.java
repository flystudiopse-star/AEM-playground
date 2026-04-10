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
    name = "AI Asset Recommender Configuration",
    description = "Configuration for AI-powered asset recommendation service"
)
public @interface AssetRecommenderConfig {

    @AttributeDefinition(
        name = "DAM Base Path",
        description = "Base path for DAM assets",
        type = AttributeDefinition.STRING
    )
    String damBasePath() default "/content/dam";

    @AttributeDefinition(
        name = "Embeddings Endpoint",
        description = "Endpoint for embeddings API",
        type = AttributeDefinition.STRING
    )
    String embeddingsEndpoint() default "https://api.openai.com/v1/embeddings";

    @AttributeDefinition(
        name = "Embedding Model",
        description = "Model to use for generating embeddings",
        type = AttributeDefinition.STRING
    )
    String embeddingModel() default "text-embedding-ada-002";

    @AttributeDefinition(
        name = "Max Recommendations",
        description = "Maximum number of asset recommendations",
        type = AttributeDefinition.INTEGER
    )
    int maxRecommendations() default 10;

    @AttributeDefinition(
        name = "Similarity Threshold",
        description = "Minimum similarity score for recommendations (0-1)",
        type = AttributeDefinition.DOUBLE
    )
    double similarityThreshold() default 0.5;

    @AttributeDefinition(
        name = "Enable Semantic Search",
        description = "Enable semantic search using AI embeddings",
        type = AttributeDefinition.BOOLEAN
    )
    boolean enableSemanticSearch() default true;

    @AttributeDefinition(
        name = "Collections Base Path",
        description = "Base path for asset collections",
        type = AttributeDefinition.STRING
    )
    String collectionsBasePath() default "/content/collections";
}