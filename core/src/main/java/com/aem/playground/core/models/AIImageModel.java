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
public class AIImageModel {

    private static final Logger log = LoggerFactory.getLogger(AIImageModel.class);

    private static final String PN_AI_ENABLED = "aiEnabled";
    private static final String PN_AI_PROMPT = "aiPrompt";
    private static final String PN_GENERATED_IMAGE_URL = "generatedImageUrl";
    private static final String PN_GENERATED_ALT_TEXT = "generatedAltText";
    private static final String PN_REGENERATE = "regenerate";

    @Self
    private Resource resource;

    @OSGiService
    private AIService aiService;

    @ValueMapValue(name = PN_AI_ENABLED, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean aiEnabled;

    @ValueMapValue(name = PN_AI_PROMPT, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String aiPrompt;

    @ValueMapValue(name = "generatedImageUrl", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String generatedImageUrl;

    @ValueMapValue(name = "generatedAltText", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String generatedAltText;

    @ValueMapValue(name = "regenerate", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean regenerate;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private Resource currentResource;

    @PostConstruct
    protected void init() {
        if (aiService == null) {
            log.warn("AIService not available - AI image generation disabled");
            return;
        }
        if (aiEnabled && aiPrompt != null && !aiPrompt.isEmpty()) {
            if (generatedImageUrl != null && !generatedImageUrl.isEmpty() && !regenerate) {
                return;
            }
            generateImage();
            saveToJcr();
        }
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public String getAiPrompt() {
        return aiPrompt;
    }

    public String getGeneratedImageUrl() {
        return generatedImageUrl;
    }

    public String getGeneratedAltText() {
        return generatedAltText;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public boolean isUseGeneratedImage() {
        return aiEnabled && generatedImageUrl != null && !generatedImageUrl.isEmpty();
    }

    private void saveToJcr() {
        if (currentResource != null && generatedImageUrl != null && !generatedImageUrl.isEmpty()) {
            try {
                Resource parent = currentResource.getParent();
                if (parent != null) {
                    String childName = currentResource.getName() + "_generated";
                    Map<String, Object> props = new HashMap<>();
                    props.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
                    props.put("generatedImageUrl", generatedImageUrl);
                    if (generatedAltText != null) {
                        props.put("generatedAltText", generatedAltText);
                    }
                    resourceResolver.create(parent, childName, props);
                    resourceResolver.commit();
                }
            } catch (Exception e) {
                log.error("Error saving generated image to JCR", e);
            }
        }
    }

    private void generateImage() {
        try {
            AIGenerationOptions options = AIGenerationOptions.builder()
                .enableCache(!regenerate)
                .imageCount(1)
                .imageSize("1024x1024")
                .build();

            AIService.AIGenerationResult result = aiService.generateImage(aiPrompt, options);

            if (result.isSuccess()) {
                generatedImageUrl = result.getContent();
                Map<String, Object> metadata = result.getMetadata();
                if (metadata != null && metadata.containsKey("revisedPrompt")) {
                    generatedAltText = (String) metadata.get("revisedPrompt");
                } else {
                    generatedAltText = aiPrompt;
                }
            } else {
                log.error("AI image generation failed: {}", result.getError());
                generatedImageUrl = null;
            }
        } catch (Exception e) {
            log.error("Error generating image", e);
            generatedImageUrl = null;
        }
    }
}
