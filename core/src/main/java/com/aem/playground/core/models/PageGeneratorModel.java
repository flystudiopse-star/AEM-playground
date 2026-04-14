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

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import com.day.cq.commons.jcr.JcrConstants;

@Model(adaptables = Resource.class)
public class PageGeneratorModel {

    private static final Logger log = LoggerFactory.getLogger(PageGeneratorModel.class);

    private static final String PN_AI_ENABLED = "aiEnabled";
    private static final String PN_PAGE_DESCRIPTION = "pageDescription";
    private static final String PN_GENERATED_CONTENT = "generatedContent";
    private static final String PN_REGENERATE = "regenerate";

    @Self
    private Resource resource;

    @OSGiService
    private AIService aiService;

    @ValueMapValue(name = PN_AI_ENABLED, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean aiEnabled;

    @ValueMapValue(name = PN_PAGE_DESCRIPTION, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String pageDescription;

    @ValueMapValue(name = "generatedContent", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String generatedContent;

    @ValueMapValue(name = "regenerate", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean regenerate;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private Resource currentResource;

    @PostConstruct
    protected void init() {
        if (aiService == null) {
            log.warn("AIService not available - AI page generation disabled");
            return;
        }
        if (aiEnabled && pageDescription != null && !pageDescription.isEmpty()) {
            if (generatedContent != null && !generatedContent.isEmpty() && !regenerate) {
                return;
            }
            generateContent();
            saveToJcr();
        }
    }

    private void generateContent() {
        try {
            String prompt = "Generate a structured page content description for: " + pageDescription +
                ". Return a JSON structure with sections: hero title, hero subtitle, features list (3 items), and call to action text.";

            AIGenerationOptions options = AIGenerationOptions.builder()
                .enableCache(!regenerate)
                .maxTokens(1000)
                .temperature(0.7)
                .build();

            AIService.AIGenerationResult result = aiService.generateText(prompt, options);

            if (result.isSuccess()) {
                generatedContent = result.getContent();
            } else {
                log.error("AI page generation failed: {}", result.getError());
                generatedContent = null;
            }
        } catch (Exception e) {
            log.error("Error generating page content", e);
            generatedContent = null;
        }
    }

    private void saveToJcr() {
        if (currentResource != null && generatedContent != null && !generatedContent.isEmpty()) {
            try {
                Resource parent = currentResource.getParent();
                if (parent != null) {
                    String childName = currentResource.getName() + "_generated";
                    Map<String, Object> props = new HashMap<>();
                    props.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
                    props.put("generatedContent", generatedContent);
                    resourceResolver.create(parent, childName, props);
                    resourceResolver.commit();
                }
            } catch (Exception e) {
                log.error("Error saving generated content to JCR", e);
            }
        }
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public String getPageDescription() {
        return pageDescription;
    }

    public String getGeneratedContent() {
        return generatedContent;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public boolean isUseGeneratedContent() {
        return aiEnabled && generatedContent != null && !generatedContent.isEmpty();
    }
}
