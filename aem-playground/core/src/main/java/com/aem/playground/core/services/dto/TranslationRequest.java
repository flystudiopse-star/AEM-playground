package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.Map;

public class TranslationRequest {

    private String contentPath;
    private String sourceLanguage;
    private String targetLanguage;
    private TranslationType translationType;
    private boolean preserveFormatting;
    private boolean translateMetadata;
    private Map<String, Object> options;

    public enum TranslationType {
        PAGE_CONTENT,
        COMPONENT_CONTENT,
        METADATA,
        EXPERIENCE_FRAGMENT,
        FULL_PAGE
    }

    private TranslationRequest(Builder builder) {
        this.contentPath = builder.contentPath;
        this.sourceLanguage = builder.sourceLanguage;
        this.targetLanguage = builder.targetLanguage;
        this.translationType = builder.translationType;
        this.preserveFormatting = builder.preserveFormatting;
        this.translateMetadata = builder.translateMetadata;
        this.options = builder.options != null ? builder.options : new HashMap<>();
    }

    public String getContentPath() {
        return contentPath;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public TranslationType getTranslationType() {
        return translationType;
    }

    public boolean isPreserveFormatting() {
        return preserveFormatting;
    }

    public boolean isTranslateMetadata() {
        return translateMetadata;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String contentPath;
        private String sourceLanguage;
        private String targetLanguage;
        private TranslationType translationType = TranslationType.FULL_PAGE;
        private boolean preserveFormatting = true;
        private boolean translateMetadata = true;
        private Map<String, Object> options;

        public Builder contentPath(String contentPath) {
            this.contentPath = contentPath;
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

        public Builder translationType(TranslationType translationType) {
            this.translationType = translationType;
            return this;
        }

        public Builder preserveFormatting(boolean preserveFormatting) {
            this.preserveFormatting = preserveFormatting;
            return this;
        }

        public Builder translateMetadata(boolean translateMetadata) {
            this.translateMetadata = translateMetadata;
            return this;
        }

        public Builder options(Map<String, Object> options) {
            this.options = options;
            return this;
        }

        public TranslationRequest build() {
            return new TranslationRequest(this);
        }
    }
}