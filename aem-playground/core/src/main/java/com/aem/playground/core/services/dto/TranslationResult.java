package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.Map;

public class TranslationResult {

    private boolean success;
    private String translatedContent;
    private String sourceLanguage;
    private String targetLanguage;
    private String contentPath;
    private String error;
    private Map<String, String> metadata;
    private Map<String, String> componentTranslations;
    private Map<String, String> metadataTranslations;
    private int tokensUsed;
    private long translationTimeMs;

    private TranslationResult() {
    }

    public static TranslationResult success(String translatedContent) {
        TranslationResult result = new TranslationResult();
        result.success = true;
        result.translatedContent = translatedContent;
        result.metadata = new HashMap<>();
        result.componentTranslations = new HashMap<>();
        result.metadataTranslations = new HashMap<>();
        return result;
    }

    public static TranslationResult error(String errorMessage) {
        TranslationResult result = new TranslationResult();
        result.success = false;
        result.error = errorMessage;
        return result;
    }

    public static TranslationResult partialSuccess(String translatedContent, String error) {
        TranslationResult result = new TranslationResult();
        result.success = true;
        result.translatedContent = translatedContent;
        result.error = error;
        result.metadata = new HashMap<>();
        result.componentTranslations = new HashMap<>();
        result.metadataTranslations = new HashMap<>();
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getContentPath() {
        return contentPath;
    }

    public String getError() {
        return error;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Map<String, String> getComponentTranslations() {
        return componentTranslations;
    }

    public Map<String, String> getMetadataTranslations() {
        return metadataTranslations;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public long getTranslationTimeMs() {
        return translationTimeMs;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public void setComponentTranslations(Map<String, String> componentTranslations) {
        this.componentTranslations = componentTranslations;
    }

    public void addComponentTranslation(String componentPath, String translatedContent) {
        if (this.componentTranslations == null) {
            this.componentTranslations = new HashMap<>();
        }
        this.componentTranslations.put(componentPath, translatedContent);
    }

    public void setMetadataTranslations(Map<String, String> metadataTranslations) {
        this.metadataTranslations = metadataTranslations;
    }

    public void addMetadataTranslation(String metadataKey, String translatedValue) {
        if (this.metadataTranslations == null) {
            this.metadataTranslations = new HashMap<>();
        }
        this.metadataTranslations.put(metadataKey, translatedValue);
    }

    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public void setTranslationTimeMs(long translationTimeMs) {
        this.translationTimeMs = translationTimeMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String translatedContent;
        private String sourceLanguage;
        private String targetLanguage;
        private String contentPath;
        private String error;
        private Map<String, String> metadata;
        private Map<String, String> componentTranslations;
        private Map<String, String> metadataTranslations;
        private int tokensUsed;
        private long translationTimeMs;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder translatedContent(String translatedContent) {
            this.translatedContent = translatedContent;
            return this;
        }

        public Builder sourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
            return this;
        }

        public Builder targetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
            return this;
        }

        public Builder contentPath(String contentPath) {
            this.contentPath = contentPath;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder componentTranslations(Map<String, String> componentTranslations) {
            this.componentTranslations = componentTranslations;
            return this;
        }

        public Builder metadataTranslations(Map<String, String> metadataTranslations) {
            this.metadataTranslations = metadataTranslations;
            return this;
        }

        public Builder tokensUsed(int tokensUsed) {
            this.tokensUsed = tokensUsed;
            return this;
        }

        public Builder translationTimeMs(long translationTimeMs) {
            this.translationTimeMs = translationTimeMs;
            return this;
        }

        public TranslationResult build() {
            TranslationResult result = new TranslationResult();
            result.success = this.success;
            result.translatedContent = this.translatedContent;
            result.sourceLanguage = this.sourceLanguage;
            result.targetLanguage = this.targetLanguage;
            result.contentPath = this.contentPath;
            result.error = this.error;
            result.metadata = this.metadata != null ? this.metadata : new HashMap<>();
            result.componentTranslations = this.componentTranslations != null ? this.componentTranslations : new HashMap<>();
            result.metadataTranslations = this.metadataTranslations != null ? this.metadataTranslations : new HashMap<>();
            result.tokensUsed = this.tokensUsed;
            result.translationTimeMs = this.translationTimeMs;
            return result;
        }
    }
}