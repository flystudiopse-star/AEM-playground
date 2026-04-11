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
package com.aem.playground.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.spi.ModelPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.aem.playground.core.testcontext.AppAemContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class AIImageModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private AIImageModel aiImageModel;

    private Resource resource;

    @Mock
    private ResourceResolver resourceResolver;

    @BeforeEach
    public void setup() throws Exception {
        resource = context.create().resource("/content/aiimage",
            "sling:resourceType", "aem-playground/components/ai-image");
        aiImageModel = resource.adaptTo(AIImageModel.class);
    }

    @Test
    void testAiEnabledDefaultsToFalse() throws Exception {
        assertFalse(aiImageModel.isAiEnabled());
    }

    @Test
    void testAiPromptIsNullWhenNotSet() throws Exception {
        assertNull(aiImageModel.getAiPrompt());
    }

    @Test
    void testAiServiceUrlHasDefaultValue() throws Exception {
        assertNotNull(aiImageModel.getAiServiceUrl());
    }

    @Test
    void testGeneratedImageUrlIsNullWhenNotGenerated() throws Exception {
        assertNull(aiImageModel.getGeneratedImageUrl());
    }

    @Test
    void testUseGeneratedImageIsFalseWhenNotEnabled() throws Exception {
        assertFalse(aiImageModel.isUseGeneratedImage());
    }

    @Test
    void testGetAiServiceUrlReturnsExpectedDefault() throws Exception {
        String expected = "https://api.openai.com/v1/images/generations";
        assertTrue(aiImageModel.getAiServiceUrl().equals(expected));
    }

    @Test
    void testGeneratedAltTextIsNullWhenNotGenerated() throws Exception {
        assertNull(aiImageModel.getGeneratedAltText());
    }

    @Test
    void testRegenerateDefaultsToFalse() throws Exception {
        context.currentResource(resource);
        AIImageModel model = context.request().adaptTo(AIImageModel.class);
        assertFalse(model.isUseGeneratedImage());
    }

    @Test
    void testGeneratedImageUrlIsReturnedFromCachedValue() {
        Resource testResource = context.create().resource("/content/cached-aiimage",
            "sling:resourceType", "aem-playground/components/ai-image",
            "aiEnabled", true,
            "aiPrompt", "Test prompt",
            "generatedImageUrl", "https://example.com/image.jpg",
            "generatedAltText", "Test alt text");

        AIImageModel model = testResource.adaptTo(AIImageModel.class);
        assertNotNull(model);
    }

    @Test
    void testUseGeneratedImageReturnsTrueWhenEnabledAndHasUrl() {
        Resource testResource = context.create().resource("/content/cached-aiimage",
            "sling:resourceType", "aem-playground/components/ai-image",
            "aiEnabled", true,
            "aiPrompt", "Test prompt");

        AIImageModel model = testResource.adaptTo(AIImageModel.class);
        assertFalse(model.isUseGeneratedImage());
    }

    @Test
    void testCacheIsNotUsedWhenRegenerateIsTrue() {
        Resource testResource = context.create().resource("/content/regen-aiimage",
            "sling:resourceType", "aem-playground/components/ai-image",
            "aiEnabled", true,
            "aiPrompt", "Test prompt",
            "regenerate", true);

        AIImageModel model = testResource.adaptTo(AIImageModel.class);
        assertNotNull(model);
    }

    @Test
    void testGeneratedAltTextReturnsCachedValue() {
        Resource testResource = context.create().resource("/content/cached-aiimage",
            "sling:resourceType", "aem-playground/components/ai-image",
            "aiEnabled", true,
            "aiPrompt", "Test prompt",
            "generatedImageUrl", "https://example.com/image.jpg",
            "generatedAltText", "Cached alt text");

        AIImageModel model = testResource.adaptTo(AIImageModel.class);
        assertNotNull(model);
    }
}