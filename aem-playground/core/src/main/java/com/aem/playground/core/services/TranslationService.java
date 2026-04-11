package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.TranslationRequest;
import com.aem.playground.core.services.dto.TranslationResult;

import java.util.List;
import java.util.Map;

public interface TranslationService {

    TranslationResult translateContent(TranslationRequest request);

    TranslationResult translatePage(String pagePath, String sourceLanguage, String targetLanguage);

    TranslationResult translateMetadata(String pagePath, String sourceLanguage, String targetLanguage);

    TranslationResult translateComponent(String componentPath, String sourceLanguage, String targetLanguage);

    TranslationResult translateExperienceFragment(String fragmentPath, String sourceLanguage, String targetLanguage);

    Map<String, TranslationResult> translateToMultipleLanguages(String pagePath, String sourceLanguage, List<String> targetLanguages);

    @Deprecated
    String createLanguageCopy(String sourcePath, String targetLanguage);

    @Deprecated
    String createLanguageBranch(String sourcePath, String languageBranch);

    List<String> getSupportedLanguages();

    boolean isLanguageSupported(String languageCode);

    default String getLanguageDisplayName(String languageCode) {
        return languageCode;
    }

    static final class SupportedLanguage {
        private final String code;
        private final String displayName;
        private final String nativeName;

        public SupportedLanguage(String code, String displayName, String nativeName) {
            this.code = code;
            this.displayName = displayName;
            this.nativeName = nativeName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getNativeName() {
            return nativeName;
        }
    }
}