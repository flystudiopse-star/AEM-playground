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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.aem.playground.core.testcontext.AppAemContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(AemContextExtension.class)
class PageGeneratorModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private PageGeneratorModel pageGeneratorModel;

    private Resource resource;

    @BeforeEach
    public void setup() throws Exception {
        resource = context.create().resource("/content/pagegenerator",
            "sling:resourceType", "aem-playground/components/page-generator");
        pageGeneratorModel = resource.adaptTo(PageGeneratorModel.class);
    }

    @Test
    void testAiEnabledDefaultsToFalse() throws Exception {
        assertFalse(pageGeneratorModel.isAiEnabled());
    }

    @Test
    void testPageDescriptionIsNullWhenNotSet() throws Exception {
        assertNull(pageGeneratorModel.getPageDescription());
    }

    @Test
    void testAiServiceUrlHasDefaultValue() throws Exception {
        assertNotNull(pageGeneratorModel.getAiServiceUrl());
    }

    @Test
    void testPageTitleIsNullWhenNotGenerated() throws Exception {
        assertNull(pageGeneratorModel.getPageTitle());
    }

    @Test
    void testHasGeneratedContentIsFalseWhenNotEnabled() throws Exception {
        assertFalse(pageGeneratorModel.hasGeneratedContent());
    }

    @Test
    void testGetAiServiceUrlReturnsExpectedDefault() throws Exception {
        String expected = "https://api.openai.com/v1/chat/completions";
        assertTrue(pageGeneratorModel.getAiServiceUrl().equals(expected));
    }

    @Test
    void testRegenerateDefaultsToFalse() throws Exception {
        assertFalse(pageGeneratorModel.isRegenerate());
    }

    @Test
    void testContentSectionsListIsEmptyWhenNotGenerated() throws Exception {
        assertTrue(pageGeneratorModel.getContentSections().isEmpty());
    }

    @Test
    void testSeoKeywordsIsNullWhenNotSet() throws Exception {
        assertNull(pageGeneratorModel.getSeoKeywords());
    }

    @Test
    void testSeoOgTitleIsNullWhenNotSet() throws Exception {
        assertNull(pageGeneratorModel.getSeoOgTitle());
    }

    @Test
    void testSeoOgDescriptionIsNullWhenNotSet() throws Exception {
        assertNull(pageGeneratorModel.getSeoOgDescription());
    }

    @Test
    void testNavigationStructureIsNullWhenNotGenerated() throws Exception {
        assertNull(pageGeneratorModel.getNavigationStructure());
    }

    @Test
    void testGeneratedPageDescriptionIsNullWhenNotGenerated() throws Exception {
        assertNull(pageGeneratorModel.getGeneratedPageDescription());
    }
}