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
public class AITextModel {

    private static final Logger log = LoggerFactory.getLogger(AITextModel.class);

    private static final String PN_AI_ENABLED = "aiEnabled";
    private static final String PN_AI_PROMPT = "aiPrompt";
    private static final String PN_GENERATED_TEXT = "generatedText";
    private static final String PN_REGENERATE = "regenerate";

    @Self
    private Resource resource;

    @OSGiService
    private AIService aiService;

    @ValueMapValue(name = PN_AI_ENABLED, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean aiEnabled;

    @ValueMapValue(name = PN_AI_PROMPT, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String aiPrompt;

    @ValueMapValue(name = "generatedText", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String generatedText;

    @ValueMapValue(name = "regenerate", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean regenerate;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private Resource currentResource;

    @PostConstruct
    protected void init() {
        if (aiService == null) {
            log.warn("AIService not available - AI text generation disabled");
            return;
        }
        if (aiEnabled && aiPrompt != null && !aiPrompt.isEmpty()) {
            if (generatedText != null && !generatedText.isEmpty() && !regenerate) {
                return;
            }
            generateText();
            saveToJcr();
        }
    }

    private void generateText() {
        try {
            AIGenerationOptions options = AIGenerationOptions.builder()
                .enableCache(!regenerate)
                .build();

            AIService.AIGenerationResult result = aiService.generateText(aiPrompt, options);

            if (result.isSuccess()) {
                generatedText = result.getContent();
            } else {
                log.error("AI text generation failed: {}", result.getError());
                generatedText = null;
            }
        } catch (Exception e) {
            log.error("Error generating text", e);
            generatedText = null;
        }
    }

    private void saveToJcr() {
        if (currentResource != null && generatedText != null && !generatedText.isEmpty()) {
            try {
                Resource parent = currentResource.getParent();
                if (parent != null) {
                    String childName = currentResource.getName() + "_generated";
                    Map<String, Object> props = new HashMap<>();
                    props.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
                    props.put("generatedText", generatedText);
                    resourceResolver.create(parent, childName, props);
                    resourceResolver.commit();
                }
            } catch (Exception e) {
                log.error("Error saving generated text to JCR", e);
            }
        }
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public String getAiPrompt() {
        return aiPrompt;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public boolean isUseGeneratedText() {
        return aiEnabled && generatedText != null && !generatedText.isEmpty();
    }

    public boolean isRegenerate() {
        return regenerate;
    }
}
