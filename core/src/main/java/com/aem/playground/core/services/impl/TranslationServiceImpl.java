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
package com.aem.playground.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.TranslationService;
import com.aem.playground.core.services.TranslationServiceConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component(service = TranslationService.class)
@Designate(ocd = TranslationServiceConfig.class)
public class TranslationServiceImpl implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationServiceImpl.class);

    private static final String DEFAULT_TRANSLATION_ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final ConcurrentMap<String, CachedTranslation> translationCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String endpoint;
    private String defaultTargetLanguage;
    private String model;
    private double temperature;
    private int maxTokens;
    private int cacheMaxSize;
    private int cacheTtlMinutes;
    private boolean cachingEnabled;
    private int batchSize;

    @Reference
    private AIService aiService;

    @Activate
    @Modified
    protected void activate(TranslationServiceConfig config) {
        this.apiKey = config.apiKey();
        this.endpoint = DEFAULT_TRANSLATION_ENDPOINT;
        this.defaultTargetLanguage = config.defaultTargetLanguage();
        this.model = config.translationModel();
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.cacheMaxSize = config.cacheMaxSize();
        this.cacheTtlMinutes = config.cacheTtlMinutes();
        this.cachingEnabled = config.cachingEnabled();
        this.batchSize = config.batchSize();
        log.info("TranslationService activated with default target language: {}", defaultTargetLanguage);
    }

    @Deactivate
    protected void deactivate() {
        translationCache.clear();
    }

    @Override
    public TranslationResult translatePage(String content, String sourceLanguage, TargetLanguage targetLanguage) {
        if (StringUtils.isBlank(content)) {
            return TranslationResult.error("Content cannot be empty");
        }
        if (StringUtils.isBlank(sourceLanguage)) {
            return TranslationResult.error("Source language cannot be empty");
        }
        if (targetLanguage == null) {
            targetLanguage = getDefaultTargetLanguageEnum();
        }

        return translate(content, sourceLanguage, targetLanguage.getCode());
    }

    @Override
    public TranslationResult translateContentFragment(String content, String sourceLanguage, TargetLanguage targetLanguage) {
        if (StringUtils.isBlank(content)) {
            return TranslationResult.error("Content cannot be empty");
        }
        if (StringUtils.isBlank(sourceLanguage)) {
            return TranslationResult.error("Source language cannot be empty");
        }
        if (targetLanguage == null) {
            targetLanguage = getDefaultTargetLanguageEnum();
        }

        return translate(content, sourceLanguage, targetLanguage.getCode());
    }

    @Override
    public BatchTranslationResult batchTranslate(List<TranslationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return BatchTranslationResult.error("Request list cannot be empty");
        }

        if (requests.size() > batchSize) {
            return BatchTranslationResult.error("Batch size exceeds maximum: " + batchSize);
        }

        List<TranslationResult> results = new ArrayList<>();
        for (TranslationRequest request : requests) {
            TranslationResult result = translate(
                request.getContent(),
                request.getSourceLanguage(),
                request.getTargetLanguage() != null ? request.getTargetLanguage().getCode() : defaultTargetLanguage
            );
            results.add(result);
        }

        return BatchTranslationResult.success(results);
    }

    @Override
    public LanguageDetectionResult detectLanguage(String content) {
        if (StringUtils.isBlank(content)) {
            return LanguageDetectionResult.error("Content cannot be empty");
        }

        try {
            String prompt = "Detect the language of the following text and return only the language code (en, es, fr, de, pl, zh, ja, ar or other). Text: " 
                + content.substring(0, Math.min(content.length(), 500));

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", model);
            metadata.put("temperature", temperature);

            AIService.AIGenerationResult result = aiService.generateText(prompt, null);

            if (result.isSuccess()) {
                String detectedLang = result.getContent().trim().toLowerCase();
                double confidence = 0.9;
                return LanguageDetectionResult.success(detectedLang, confidence);
            } else {
                return LanguageDetectionResult.error(result.getError());
            }
        } catch (Exception e) {
            log.error("Error detecting language: {}", e.getMessage());
            return LanguageDetectionResult.error(e.getMessage());
        }
    }

    @Override
    public Set<TargetLanguage> getSupportedLanguages() {
        return Arrays.stream(TargetLanguage.values()).collect(Collectors.toSet());
    }

    @Override
    public void clearCache() {
        translationCache.clear();
        log.info("Translation cache cleared");
    }

    private TranslationResult translate(String content, String sourceLanguage, String targetLanguageCode) {
        if (cachingEnabled) {
            String cacheKey = generateCacheKey(content, sourceLanguage, targetLanguageCode);
            CachedTranslation cached = translationCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                log.debug("Cache hit for translation: {} -> {}", sourceLanguage, targetLanguageCode);
                return TranslationResult.success(cached.translatedContent, sourceLanguage, targetLanguageCode, cached.metadata);
            }
        }

        try {
            TargetLanguage targetLang = Arrays.stream(TargetLanguage.values())
                .filter(tl -> tl.getCode().equals(targetLanguageCode))
                .findFirst()
                .orElse(TargetLanguage.ENGLISH);

            String prompt = buildTranslationPrompt(content, sourceLanguage, targetLang);
            Map<String, Object> options = new HashMap<>();
            options.put("model", model);
            options.put("temperature", temperature);
            options.put("maxTokens", maxTokens);

            AIService.AIGenerationResult result = aiService.generateText(prompt, null);

            if (result.isSuccess()) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("model", model);
                metadata.put("sourceLanguage", sourceLanguage);
                metadata.put("targetLanguage", targetLanguageCode);

                TranslationResult translationResult = TranslationResult.success(
                    result.getContent(),
                    sourceLanguage,
                    targetLanguageCode,
                    metadata
                );

                if (cachingEnabled) {
                    String cacheKey = generateCacheKey(content, sourceLanguage, targetLanguageCode);
                    translationCache.put(cacheKey, new CachedTranslation(result.getContent(), metadata));
                    evictOldCacheEntries();
                }

                return translationResult;
            } else {
                return TranslationResult.error(result.getError());
            }
        } catch (Exception e) {
            log.error("Error translating content: {}", e.getMessage());
            return TranslationResult.error(e.getMessage());
        }
    }

    private String buildTranslationPrompt(String content, String sourceLanguage, TargetLanguage targetLanguage) {
        return String.format(
            "Translate the following text from %s to %s. Preserve the formatting and tone. Only return the translation, no explanations.%s%s",
            sourceLanguage,
            targetLanguage.getDisplayName(),
            System.lineSeparator(),
            content
        );
    }

    private String generateCacheKey(String content, String sourceLanguage, String targetLanguage) {
        try {
            String keyMaterial = content + "|" + sourceLanguage + "|" + targetLanguage;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return (content.hashCode() + "|" + sourceLanguage + "|" + targetLanguage);
        }
    }

    private void evictOldCacheEntries() {
        if (translationCache.size() > cacheMaxSize) {
            int toRemove = translationCache.size() - cacheMaxSize;
            List<String> keysToRemove = new ArrayList<>(translationCache.keySet());
            for (int i = 0; i < toRemove && i < keysToRemove.size(); i++) {
                translationCache.remove(keysToRemove.get(i));
            }
        }
    }

    private TargetLanguage getDefaultTargetLanguageEnum() {
        try {
            return Arrays.stream(TargetLanguage.values())
                .filter(tl -> tl.getCode().equalsIgnoreCase(defaultTargetLanguage))
                .findFirst()
                .orElse(TargetLanguage.ENGLISH);
        } catch (Exception e) {
            return TargetLanguage.ENGLISH;
        }
    }

    public String getDefaultTargetLanguage() {
        return defaultTargetLanguage;
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public int getCacheMaxSize() {
        return cacheMaxSize;
    }

    public int getCacheSize() {
        return translationCache.size();
    }

    public int getBatchSize() {
        return batchSize;
    }

    private class CachedTranslation {
        private final String translatedContent;
        private final Map<String, Object> metadata;
        private final long timestamp;

        CachedTranslation(String translatedContent, Map<String, Object> metadata) {
            this.translatedContent = translatedContent;
            this.metadata = metadata;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            long expiryTime = cacheTtlMinutes * 60 * 1000L;
            return System.currentTimeMillis() - timestamp > expiryTime;
        }
    }
}