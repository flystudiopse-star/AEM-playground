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
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.Default;
import org.apache.jackrabbit.JcrConstants;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Model(adaptables = Resource.class)
public class AITextModel {

    private static final String PN_AI_ENABLED = "aiEnabled";
    private static final String PN_AI_PROMPT = "aiPrompt";
    private static final String PN_AI_SERVICE_URL = "aiServiceUrl";

    @ValueMapValue(name = PN_AI_ENABLED, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    @Default(booleanValue = false)
    private boolean aiEnabled;

    @ValueMapValue(name = PN_AI_PROMPT, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String aiPrompt;

    @ValueMapValue(name = PN_AI_SERVICE_URL, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    @Default(values = "https://api.openai.com/v1/chat/completions")
    private String aiServiceUrl;

    @ValueMapValue(name = "generatedText", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String generatedText;

    @ValueMapValue(name = "regenerate", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    @Default(booleanValue = false)
    private boolean regenerate;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private Resource currentResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    protected void init() {
        if (aiEnabled && aiPrompt != null && !aiPrompt.isEmpty()) {
            if (generatedText != null && !generatedText.isEmpty() && !regenerate) {
                return;
            }
            generateText();
            saveToJcr();
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

    public String getGeneratedText() {
        return generatedText;
    }

    public boolean isUseGeneratedText() {
        return aiEnabled && generatedText != null && !generatedText.isEmpty();
    }

    public boolean isRegenerate() {
        return regenerate;
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
            }
        }
    }

    private void generateText() {
        try {
            URL url = new URL(aiServiceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format(
                "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 500}",
                escapeJson(aiPrompt)
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream responseStream = connection.getInputStream()) {
                    JsonNode rootNode = objectMapper.readTree(responseStream);
                    JsonNode choices = rootNode.get("choices");
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        generatedText = choices.get(0).get("message").get("content").asText();
                    }
                }
            }
        } catch (Exception e) {
            generatedText = null;
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