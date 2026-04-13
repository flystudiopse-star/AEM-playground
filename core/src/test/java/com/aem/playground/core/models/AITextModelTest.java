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
import org.apache.sling.api.resource.ModifiableValueMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.aem.playground.core.testcontext.AppAemContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class AITextModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private AITextModel aiTextModel;

    private Resource resource;

    @Mock
    private ResourceResolver resourceResolver;

    @BeforeEach
    public void setup() throws Exception {
        resource = context.create().resource("/content/aitext",
            "sling:resourceType", "aem-playground/components/ai-text");
        aiTextModel = resource.adaptTo(AITextModel.class);
    }

    @Test
    void testAiEnabledDefaultsToFalse() throws Exception {
        assertFalse(aiTextModel.isAiEnabled());
    }

    @Test
    void testAiPromptIsNullWhenNotSet() throws Exception {
        assertNull(aiTextModel.getAiPrompt());
    }

    @Test
    void testAiServiceUrlHasDefaultValue() throws Exception {
        assertNotNull(aiTextModel.getAiServiceUrl());
    }

    @Test
    void testGeneratedTextIsNullWhenNotGenerated() throws Exception {
        assertNull(aiTextModel.getGeneratedText());
    }

    @Test
    void testUseGeneratedTextIsFalseWhenNotEnabled() throws Exception {
        assertFalse(aiTextModel.isUseGeneratedText());
    }

    @Test
    void testGetAiServiceUrlReturnsExpectedDefault() throws Exception {
        String expected = "https://api.openai.com/v1/chat/completions";
        assertTrue(aiTextModel.getAiServiceUrl().equals(expected));
    }

    @Test
    void testRegenerateDefaultsToFalse() throws Exception {
        context.currentResource(resource);
        AITextModel model = context.request().adaptTo(AITextModel.class);
        assertFalse(model.isUseGeneratedText());
    }

    @Test
    void testGeneratedTextIsReturnedFromCachedValue() {
        Resource testResource = context.create().resource("/content/cached-aitext",
            "sling:resourceType", "aem-playground/components/ai-text",
            "aiEnabled", true,
            "aiPrompt", "Test prompt",
            "generatedText", "Cached text content");

        AITextModel model = testResource.adaptTo(AITextModel.class);
        assertNotNull(model);
    }

    @Test
    void testUseGeneratedTextReturnsTrueWhenEnabledAndHasText() {
        Resource testResource = context.create().resource("/content/cached-aitext",
            "sling:resourceType", "aem-playground/components/ai-text",
            "aiEnabled", true,
            "aiPrompt", "Test prompt");

        AITextModel model = testResource.adaptTo(AITextModel.class);
        assertFalse(model.isUseGeneratedText());
    }

    @Test
    void testCacheIsNotUsedWhenRegenerateIsTrue() {
        Resource testResource = context.create().resource("/content/regen-aitext",
            "sling:resourceType", "aem-playground/components/ai-text",
            "aiEnabled", true,
            "aiPrompt", "Test prompt",
            "regenerate", true);

        AITextModel model = testResource.adaptTo(AITextModel.class);
        assertNotNull(model);
    }
}