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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = AIService.class)
@Designate(ocd = OpenAIServiceConfig.class)
public class OpenAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);

    private static final String DEFAULT_TEXT_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_IMAGE_ENDPOINT = "https://api.openai.com/v1/images/generations";
    private static final String DEFAULT_MODEL = "gpt-4";
    private static final String DEFAULT_IMAGE_MODEL = "dall-e-3";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedResult> textCache = new ConcurrentHashMap<>();
    private final Map<String, CachedResult> imageCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String textEndpoint;
    private String imageEndpoint;
    private String defaultModel;
    private String defaultImageModel;
    private int cacheMaxSize;
    private boolean cachingEnabled;

    @Activate
    @Modified
    protected void activate(OpenAIServiceConfig config) {
        this.apiKey = config.apiKey();
        this.textEndpoint = PropertiesUtil.toString(config.textEndpoint(), DEFAULT_TEXT_ENDPOINT);
        this.imageEndpoint = PropertiesUtil.toString(config.imageEndpoint(), DEFAULT_IMAGE_ENDPOINT);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.defaultImageModel = PropertiesUtil.toString(config.defaultImageModel(), DEFAULT_IMAGE_MODEL);
        this.cacheMaxSize = config.cacheMaxSize();
        this.cachingEnabled = config.cachingEnabled();
        log.info("OpenAIService activated with endpoint: {}", textEndpoint);
    }

    @Deactivate
    protected void deactivate() {
        textCache.clear();
        imageCache.clear();
    }

    @Override
    public AIGenerationResult generateText(String prompt, AIGenerationOptions options) {
        if (StringUtils.isBlank(prompt)) {
            return AIGenerationResult.error("Prompt cannot be empty");
        }

        AIGenerationOptions opts = options != null ? options : AIGenerationOptions.builder().build();

        if (opts.isEnableCache() && cachingEnabled) {
            String cacheKey = generateCacheKey(prompt, opts);
            CachedResult cached = textCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for text generation: {}", cacheKey);
                return AIGenerationResult.success(cached.content, cached.metadata);
            }
        }

        try {
            String model = StringUtils.isNotBlank(opts.getModel()) ? opts.getModel() : defaultModel;
            String requestBody = buildTextRequestBody(prompt, opts, model);

            String response = executePostRequest(textEndpoint, requestBody, true);

            if (StringUtils.isBlank(response)) {
                return AIGenerationResult.error("Empty response from API");
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choices = rootNode.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                return AIGenerationResult.error("Invalid response format");
            }

            String content = choices.get(0).get("message").get("content").asText();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", model);
            metadata.put("prompt", prompt);
            metadata.put("tokens", rootNode.get("usage").get("total_tokens").asInt());

            AIGenerationResult result = AIGenerationResult.success(content, metadata);

            if (opts.isEnableCache() && cachingEnabled) {
                String cacheKey = generateCacheKey(prompt, opts);
                textCache.put(cacheKey, new CachedResult(content, metadata));
                evictOldCacheEntries(textCache);
            }

            return result;

        } catch (Exception e) {
            log.error("Error generating text: {}", e.getMessage());
            return AIGenerationResult.error(e.getMessage());
        }
    }

    @Override
    public AIGenerationResult generateImage(String prompt, AIGenerationOptions options) {
        if (StringUtils.isBlank(prompt)) {
            return AIGenerationResult.error("Prompt cannot be empty");
        }

        AIGenerationOptions opts = options != null ? options : AIGenerationOptions.builder().build();

        if (opts.isEnableCache() && cachingEnabled) {
            String cacheKey = generateCacheKey(prompt, opts);
            CachedResult cached = imageCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for image generation: {}", cacheKey);
                return AIGenerationResult.success(cached.content, cached.metadata);
            }
        }

        try {
            String model = StringUtils.isNotBlank(opts.getModel()) ? opts.getModel() : defaultImageModel;
            String requestBody = buildImageRequestBody(prompt, opts, model);

            String response = executePostRequest(imageEndpoint, requestBody, false);

            if (StringUtils.isBlank(response)) {
                return AIGenerationResult.error("Empty response from API");
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataArray = rootNode.get("data");
            if (dataArray == null || !dataArray.isArray() || dataArray.size() == 0) {
                return AIGenerationResult.error("Invalid response format");
            }

            String imageUrl = dataArray.get(0).get("url").asText();
            String revisedPrompt = dataArray.get(0).has("revised_prompt") 
                ? dataArray.get(0).get("revised_prompt").asText() 
                : null;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", model);
            metadata.put("prompt", prompt);
            if (revisedPrompt != null) {
                metadata.put("revisedPrompt", revisedPrompt);
            }

            AIGenerationResult result = AIGenerationResult.success(imageUrl, metadata);

            if (opts.isEnableCache() && cachingEnabled) {
                String cacheKey = generateCacheKey(prompt, opts);
                imageCache.put(cacheKey, new CachedResult(imageUrl, metadata));
                evictOldCacheEntries(imageCache);
            }

            return result;

        } catch (Exception e) {
            log.error("Error generating image: {}", e.getMessage());
            return AIGenerationResult.error(e.getMessage());
        }
    }

    @Override
    public void clearCache() {
        textCache.clear();
        imageCache.clear();
        log.info("AI cache cleared");
    }

    private String buildTextRequestBody(String prompt, AIGenerationOptions opts, String model) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("temperature", opts.getTemperature());
        request.put("max_tokens", opts.getMaxTokens());

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        if (StringUtils.isNotBlank(opts.getCustomSystemPrompt())) {
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", opts.getCustomSystemPrompt());
            request.put("messages", new Object[]{systemMessage, message});
        } else {
            request.put("messages", new Object[]{message});
        }

        return objectMapper.writeValueAsString(request);
    }

    private String buildImageRequestBody(String prompt, AIGenerationOptions opts, String model) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", prompt);
        request.put("n", opts.getImageCount());
        request.put("size", opts.getImageSize());
        request.put("model", model);

        return objectMapper.writeValueAsString(request);
    }

    private String executePostRequest(String endpoint, String body, boolean isTextRequest) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    log.error("API error: {} - {}", statusCode, responseBody);
                    throw new IOException("API returned status code: " + statusCode);
                }

                return responseBody;
            }
        }
    }

    private String generateCacheKey(String prompt, AIGenerationOptions opts) {
        try {
            String keyMaterial = prompt + opts.getCacheKey();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return prompt.hashCode() + ":" + opts.getCacheKey();
        }
    }

    private void evictOldCacheEntries(Map<String, CachedResult> cache) {
        if (cache.size() > cacheMaxSize) {
            int toRemove = cache.size() - cacheMaxSize;
            Iterator<String> iterator = cache.keySet().iterator();
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getTextEndpoint() {
        return textEndpoint;
    }

    public String getImageEndpoint() {
        return imageEndpoint;
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public int getCacheMaxSize() {
        return cacheMaxSize;
    }

    public int getTextCacheSize() {
        return textCache.size();
    }

    public int getImageCacheSize() {
        return imageCache.size();
    }

    private static class CachedResult {
        final String content;
        final Map<String, Object> metadata;

        CachedResult(String content, Map<String, Object> metadata) {
            this.content = content;
            this.metadata = metadata;
        }
    }
}