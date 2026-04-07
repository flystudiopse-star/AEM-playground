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

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ResourceResolverAnnotated;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.Default;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Model(adaptables = Resource.class)
public class AIImageModel {

    private static final String PN_AI_ENABLED = "aiEnabled";
    private static final String PN_AI_PROMPT = "aiPrompt";
    private static final String PN_AI_SERVICE_URL = "aiServiceUrl";
    private static final String PN_GENERATED_IMAGE_URL = "generatedImageUrl";
    private static final String PN_GENERATED_ALT_TEXT = "generatedAltText";
    private static final String PN_REGENERATE = "regenerate";

    @ResourceResolverAnnotated
    private ResourceResolver resourceResolver;

    @Self
    private Resource resource;

    @ValueMapValue(name = PN_AI_ENABLED, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    @Default(booleanValue = false)
    private boolean aiEnabled;

    @ValueMapValue(name = PN_AI_PROMPT, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String aiPrompt;

    @ValueMapValue(name = PN_AI_SERVICE_URL, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    @Default(values = "https://api.openai.com/v1/images/generations")
    private String aiServiceUrl;

    @ValueMapValue(name = PN_REGENERATE, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    @Default(booleanValue = false)
    private boolean regenerate;

    private String generatedImageUrl;
    private String generatedAltText;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    protected void init() {
        if (aiEnabled && aiPrompt != null && !aiPrompt.isEmpty()) {
            if (!regenerate) {
                String[] cached = getCachedGeneratedImage();
                if (cached != null) {
                    generatedImageUrl = cached[0];
                    generatedAltText = cached[1];
                    return;
                }
            }
            generateImage();
            saveGeneratedImageToJcr();
        }
    }

    private String[] getCachedGeneratedImage() {
        if (resource != null) {
            String url = resource.getValueMap().get(PN_GENERATED_IMAGE_URL, String.class);
            String alt = resource.getValueMap().get(PN_GENERATED_ALT_TEXT, String.class);
            if (url != null) {
                return new String[]{url, alt};
            }
        }
        return null;
    }

    private void saveGeneratedImageToJcr() {
        if (generatedImageUrl != null && resourceResolver != null && resource != null) {
            try {
                ModifiableValueMap map = resource.adaptTo(ModifiableValueMap.class);
                if (map != null) {
                    map.put(PN_GENERATED_IMAGE_URL, generatedImageUrl);
                    if (generatedAltText != null) {
                        map.put(PN_GENERATED_ALT_TEXT, generatedAltText);
                    }
                    resourceResolver.commit();
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public String getAiPrompt() {
        return aiPrompt;
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public String getGeneratedImageUrl() {
        return generatedImageUrl;
    }

    public String getGeneratedAltText() {
        return generatedAltText;
    }

    public boolean isUseGeneratedImage() {
        return aiEnabled && generatedImageUrl != null && !generatedImageUrl.isEmpty();
    }

    private void generateImage() {
        try {
            URL url = new URL(aiServiceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format(
                "{\"prompt\": \"%s\", \"n\": 1, \"size\": \"1024x1024\"}",
                escapeJson(aiPrompt)
            );

            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream responseStream = connection.getInputStream()) {
                    JsonNode rootNode = objectMapper.readTree(responseStream);
                    JsonNode dataArray = rootNode.get("data");
                    if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                        generatedImageUrl = dataArray.get(0).get("url").asText();
                    }
                }
            }
        } catch (Exception e) {
            generatedImageUrl = null;
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
