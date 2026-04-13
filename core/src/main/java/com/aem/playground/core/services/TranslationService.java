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

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TranslationService {

    enum TargetLanguage {
        ENGLISH("en", "English"),
        SPANISH("es", "Spanish"),
        FRENCH("fr", "French"),
        GERMAN("de", "German"),
        POLISH("pl", "Polish"),
        CHINESE("zh", "Chinese"),
        JAPANESE("ja", "Japanese"),
        ARABIC("ar", "Arabic");

        private final String code;
        private final String displayName;

        TargetLanguage(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    TranslationResult translatePage(String content, String sourceLanguage, TargetLanguage targetLanguage);

    TranslationResult translateContentFragment(String content, String sourceLanguage, TargetLanguage targetLanguage);

    BatchTranslationResult batchTranslate(List<TranslationRequest> requests);

    LanguageDetectionResult detectLanguage(String content);

    Set<TargetLanguage> getSupportedLanguages();

    void clearCache();

    class TranslationResult {
        private final String translatedContent;
        private final String sourceLanguage;
        private final String targetLanguage;
        private final boolean success;
        private final String error;
        private final Map<String, Object> metadata;

        private TranslationResult(String translatedContent, String sourceLanguage, String targetLanguage, 
                            boolean success, String error, Map<String, Object> metadata) {
            this.translatedContent = translatedContent;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
            this.success = success;
            this.error = error;
            this.metadata = metadata;
        }

        public static TranslationResult success(String translatedContent, String sourceLanguage, 
                                         String targetLanguage, Map<String, Object> metadata) {
            return new TranslationResult(translatedContent, sourceLanguage, targetLanguage, true, null, metadata);
        }

        public static TranslationResult error(String error) {
            return new TranslationResult(null, null, null, false, error, null);
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

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }

    class BatchTranslationResult {
        private final List<TranslationResult> results;
        private final int totalCount;
        private final int successCount;
        private final int failureCount;
        private final boolean success;
        private final String error;

        private BatchTranslationResult(List<TranslationResult> results, int totalCount, 
                                     int successCount, int failureCount, boolean success, String error) {
            this.results = results;
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.success = success;
            this.error = error;
        }

        public static BatchTranslationResult success(List<TranslationResult> results) {
            int successCount = (int) results.stream().filter(TranslationResult::isSuccess).count();
            int failureCount = results.size() - successCount;
            return new BatchTranslationResult(results, results.size(), successCount, failureCount, true, null);
        }

        public static BatchTranslationResult error(String error) {
            return new BatchTranslationResult(null, 0, 0, 0, false, error);
        }

        public List<TranslationResult> getResults() {
            return results;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }

    class LanguageDetectionResult {
        private final String detectedLanguage;
        private final double confidence;
        private final boolean success;
        private final String error;

        private LanguageDetectionResult(String detectedLanguage, double confidence, boolean success, String error) {
            this.detectedLanguage = detectedLanguage;
            this.confidence = confidence;
            this.success = success;
            this.error = error;
        }

        public static LanguageDetectionResult success(String detectedLanguage, double confidence) {
            return new LanguageDetectionResult(detectedLanguage, confidence, true, null);
        }

        public static LanguageDetectionResult error(String error) {
            return new LanguageDetectionResult(null, 0, false, error);
        }

        public String getDetectedLanguage() {
            return detectedLanguage;
        }

        public double getConfidence() {
            return confidence;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }

    class TranslationRequest {
        private final String content;
        private final String sourceLanguage;
        private final TargetLanguage targetLanguage;

        public TranslationRequest(String content, String sourceLanguage, TargetLanguage targetLanguage) {
            this.content = content;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
        }

        public String getContent() {
            return content;
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public TargetLanguage getTargetLanguage() {
            return targetLanguage;
        }
    }
}