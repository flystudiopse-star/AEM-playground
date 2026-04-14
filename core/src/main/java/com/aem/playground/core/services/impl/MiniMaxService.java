package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.MiniMaxServiceConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = AIService.class)
@Designate(ocd = MiniMaxServiceConfig.class)
public class MiniMaxService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(MiniMaxService.class);

    private static final String DEFAULT_TEXT_ENDPOINT = "https://api.minimax.chat/v1/text/chatcompletion_pro";
    private static final String DEFAULT_IMAGE_ENDPOINT = "https://api.minimax.chat/v1/image/gen";
    private static final String DEFAULT_MODEL = "MiniMax-Text-01";
    private static final String DEFAULT_IMAGE_MODEL = "gamepainter";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedResult> textCache = new ConcurrentHashMap<>();
    private final Map<String, CachedResult> imageCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String textEndpoint;
    private String imageEndpoint;
    private String defaultModel;
    private String defaultImageModel;
    private String groupId;
    private int cacheMaxSize;
    private boolean cachingEnabled;

    @Activate
    @Modified
    protected void activate(MiniMaxServiceConfig config) {
        this.apiKey = resolveApiKey(config);
        this.textEndpoint = PropertiesUtil.toString(config.textEndpoint(), DEFAULT_TEXT_ENDPOINT);
        this.imageEndpoint = PropertiesUtil.toString(config.imageEndpoint(), DEFAULT_IMAGE_ENDPOINT);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.defaultImageModel = PropertiesUtil.toString(config.defaultImageModel(), DEFAULT_IMAGE_MODEL);
        this.groupId = config.groupId();
        this.cacheMaxSize = config.cacheMaxSize();
        this.cachingEnabled = config.cachingEnabled();
        log.info("MiniMaxService activated with endpoint: {}, model: {}", textEndpoint, defaultModel);
    }

    private String resolveApiKey(MiniMaxServiceConfig config) {
        // First try direct API key
        String key = config.apiKey();
        if (StringUtils.isNotBlank(key)) {
            // If it starts with $, resolve from environment
            if (key.startsWith("$")) {
                String envVar = key.substring(1);
                String envValue = System.getenv(envVar);
                if (StringUtils.isNotBlank(envValue)) {
                    return envValue;
                }
                log.warn("Environment variable {} for API key not found", envVar);
                return null;
            }
            return key;
        }
        // Try environment variable name from config
        String envVar = config.apiKeyEnvVar();
        if (StringUtils.isNotBlank(envVar)) {
            String envValue = System.getenv(envVar);
            if (StringUtils.isNotBlank(envValue)) {
                return envValue;
            }
            log.warn("Environment variable {} for API key not found", envVar);
        }
        return null;
    }

    @Deactivate
    protected void deactivate() {
        textCache.clear();
        imageCache.clear();
    }

    @Override
    public AIService.AIGenerationResult generateText(String prompt, AIGenerationOptions options) {
        if (StringUtils.isBlank(prompt)) {
            return AIService.AIGenerationResult.error("Prompt cannot be empty");
        }
        if (StringUtils.isBlank(apiKey)) {
            return AIService.AIGenerationResult.error("API key not configured");
        }

        AIGenerationOptions opts = options != null ? options : AIGenerationOptions.builder().build();

        if (opts.isEnableCache() && cachingEnabled) {
            String cacheKey = generateCacheKey(prompt, opts);
            CachedResult cached = textCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for text generation: {}", cacheKey);
                return AIService.AIGenerationResult.success(cached.content, cached.metadata);
            }
        }

        try {
            String model = StringUtils.isNotBlank(opts.getModel()) ? opts.getModel() : defaultModel;
            String requestBody = buildTextRequestBody(prompt, opts, model);

            String response = executePostRequest(textEndpoint, requestBody, true);

            if (StringUtils.isBlank(response)) {
                return AIService.AIGenerationResult.error("Empty response from API");
            }

            JsonNode rootNode = objectMapper.readTree(response);

            // Check for API errors
            if (rootNode.has("base_resp") && rootNode.get("base_resp").has("status_msg")) {
                String errorMsg = rootNode.get("base_resp").get("status_msg").asText();
                int statusCode = rootNode.get("base_resp").has("status_code")
                    ? rootNode.get("base_resp").get("status_code").asInt() : -1;
                if (statusCode != 0) {
                    return AIService.AIGenerationResult.error("API error: " + errorMsg);
                }
            }

            // Parse MiniMax response format
            JsonNode choices = rootNode.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                return AIService.AIGenerationResult.error("Invalid response format from MiniMax API");
            }

            String content = choices.get(0).get("messages").get(0).get("text").asText();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", model);
            metadata.put("prompt", prompt);

            // Extract usage if available
            if (rootNode.has("usage")) {
                metadata.put("tokens", rootNode.get("usage").asInt());
            }

            AIService.AIGenerationResult result = AIService.AIGenerationResult.success(content, metadata);

            if (opts.isEnableCache() && cachingEnabled) {
                String cacheKey = generateCacheKey(prompt, opts);
                textCache.put(cacheKey, new CachedResult(content, metadata));
                evictOldCacheEntries(textCache);
            }

            return result;

        } catch (Exception e) {
            log.error("Error generating text: {}", e.getMessage());
            return AIService.AIGenerationResult.error(e.getMessage());
        }
    }

    @Override
    public AIService.AIGenerationResult generateImage(String prompt, AIGenerationOptions options) {
        if (StringUtils.isBlank(prompt)) {
            return AIService.AIGenerationResult.error("Prompt cannot be empty");
        }
        if (StringUtils.isBlank(apiKey)) {
            return AIService.AIGenerationResult.error("API key not configured");
        }

        AIGenerationOptions opts = options != null ? options : AIGenerationOptions.builder().build();

        if (opts.isEnableCache() && cachingEnabled) {
            String cacheKey = generateCacheKey(prompt, opts);
            CachedResult cached = imageCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for image generation: {}", cacheKey);
                return AIService.AIGenerationResult.success(cached.content, cached.metadata);
            }
        }

        try {
            String model = StringUtils.isNotBlank(opts.getModel()) ? opts.getModel() : defaultImageModel;
            String requestBody = buildImageRequestBody(prompt, opts, model);

            String response = executePostRequest(imageEndpoint, requestBody, false);

            if (StringUtils.isBlank(response)) {
                return AIService.AIGenerationResult.error("Empty response from API");
            }

            JsonNode rootNode = objectMapper.readTree(response);

            // Check for API errors
            if (rootNode.has("base_resp") && rootNode.get("base_resp").has("status_msg")) {
                String errorMsg = rootNode.get("base_resp").get("status_msg").asText();
                int statusCode = rootNode.get("base_resp").has("status_code")
                    ? rootNode.get("base_resp").get("status_code").asInt() : -1;
                if (statusCode != 0) {
                    return AIService.AIGenerationResult.error("API error: " + errorMsg);
                }
            }

            JsonNode dataArray = rootNode.get("data");
            if (dataArray == null || !dataArray.isArray() || dataArray.size() == 0) {
                return AIService.AIGenerationResult.error("Invalid response format from MiniMax API");
            }

            String imageUrl = dataArray.get(0).get("b64_json").asText();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", model);
            metadata.put("prompt", prompt);

            AIService.AIGenerationResult result = AIService.AIGenerationResult.success(imageUrl, metadata);

            if (opts.isEnableCache() && cachingEnabled) {
                String cacheKey = generateCacheKey(prompt, opts);
                imageCache.put(cacheKey, new CachedResult(imageUrl, metadata));
                evictOldCacheEntries(imageCache);
            }

            return result;

        } catch (Exception e) {
            log.error("Error generating image: {}", e.getMessage());
            return AIService.AIGenerationResult.error(e.getMessage());
        }
    }

    @Override
    public void clearCache() {
        textCache.clear();
        imageCache.clear();
        log.info("MiniMax cache cleared");
    }

    private String buildTextRequestBody(String prompt, AIGenerationOptions opts, String model) throws IOException {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", model);
        request.put("tokens_to_generate", opts.getMaxTokens() > 0 ? opts.getMaxTokens() : 1000);
        request.put("temperature", (float) opts.getTemperature());

        ObjectNode messages = objectMapper.createObjectNode();
        messages.put("role", "USER");
        messages.put("text", prompt);

        request.putArray("messages").add(messages);

        // Add bot_setting for system prompt if provided
        if (StringUtils.isNotBlank(opts.getCustomSystemPrompt())) {
            ObjectNode botSetting = objectMapper.createObjectNode();
            botSetting.put("bot_name", "AI Assistant");
            botSetting.put("bot_description", opts.getCustomSystemPrompt());
            request.putArray("bot_setting").add(botSetting);
        }

        return objectMapper.writeValueAsString(request);
    }

    private String buildImageRequestBody(String prompt, AIGenerationOptions opts, String model) throws IOException {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("prompt", prompt);
        request.put("model", model);
        request.put("num_images", opts.getImageCount());

        // Parse image size (e.g. "1024x1024" -> "1K")
        String size = opts.getImageSize();
        String sizeMapping = sizeMap(size);
        request.put("resolution", sizeMapping);

        return objectMapper.writeValueAsString(request);
    }

    private String sizeMap(String size) {
        switch (size) {
            case "1024x1024": return "1K";
            case "512x512": return "512";
            case "256x256": return "256";
            case "1024x1792": return "1K-H";
            case "1792x1024": return "1K-W";
            default: return "1K";
        }
    }

    private String executePostRequest(String endpoint, String body, boolean isTextRequest) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            if (StringUtils.isNotBlank(groupId)) {
                httpPost.setHeader("GroupId", groupId);
            }
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200 && statusCode != 201) {
                    log.error("MiniMax API error: {} - {}", statusCode, responseBody);
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

    private static class CachedResult {
        final String content;
        final Map<String, Object> metadata;

        CachedResult(String content, Map<String, Object> metadata) {
            this.content = content;
            this.metadata = metadata;
        }
    }
}
