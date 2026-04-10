package com.aem.playground.core.services;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;
import java.util.Map;

public interface TranslationService {

    TranslationResult translateContent(String content, String sourceLanguage, String targetLanguage);

    TranslationResult translatePage(Resource pageResource, String sourceLanguage, String targetLanguage);

    List<TranslationResult> translatePageToMultipleLanguages(Resource pageResource, String sourceLanguage, List<String> targetLanguages);

    Map<String, String> translateMetadata(Map<String, String> metadata, String sourceLanguage, String targetLanguage);

    List<TranslationResult> translateExperienceFragment(Resource fragment, String sourceLanguage, String targetLanguage);

    Resource createLanguageCopy(Resource sourcePage, String targetLanguage, ResourceResolver resolver);

    List<TranslationResult> translateComponentContent(Resource component, String sourceLanguage, String targetLanguage);

    List<Language> getSupportedLanguages();

    static class TranslationResult {
        private final String originalContent;
        private final String translatedContent;
        private final String sourceLanguage;
        private final String targetLanguage;
        private final boolean success;
        private final String error;
        private final Map<String, Object> metadata;

        private TranslationResult(String originalContent, String translatedContent, String sourceLanguage,
                                  String targetLanguage, boolean success, String error, Map<String, Object> metadata) {
            this.originalContent = originalContent;
            this.translatedContent = translatedContent;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
            this.success = success;
            this.error = error;
            this.metadata = metadata;
        }

        public static TranslationResult success(String original, String translated, String source, String target, Map<String, Object> metadata) {
            return new TranslationResult(original, translated, source, target, true, null, metadata);
        }

        public static TranslationResult error(String original, String source, String target, String error) {
            return new TranslationResult(original, null, source, target, false, error, null);
        }

        public String getOriginalContent() { return originalContent; }
        public String getTranslatedContent() { return translatedContent; }
        public String getSourceLanguage() { return sourceLanguage; }
        public String getTargetLanguage() { return targetLanguage; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    class Language {
        private final String code;
        private final String name;
        private final String nativeName;

        public Language(String code, String name, String nativeName) {
            this.code = code;
            this.name = name;
            this.nativeName = nativeName;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getNativeName() { return nativeName; }

        public static Language fromCode(String code) {
            return SUPPORTED_LANGUAGES.getOrDefault(code.toLowerCase(), new Language(code, code, code));
        }

        private static final Map<String, Language> SUPPORTED_LANGUAGES = Map.ofEntries(
            Map.entry("en", new Language("en", "English", "English")),
            Map.entry("de", new Language("de", "German", "Deutsch")),
            Map.entry("fr", new Language("fr", "French", "Français")),
            Map.entry("es", new Language("es", "Spanish", "Español")),
            Map.entry("pl", new Language("pl", "Polish", "Polski")),
            Map.entry("it", new Language("it", "Italian", "Italiano")),
            Map.entry("pt", new Language("pt", "Portuguese", "Português")),
            Map.entry("nl", new Language("nl", "Dutch", "Nederlands")),
            Map.entry("ru", new Language("ru", "Russian", "Русский")),
            Map.entry("ja", new Language("ja", "Japanese", "日本語")),
            Map.entry("ko", new Language("ko", "Korean", "한국어")),
            Map.entry("zh", new Language("zh", "Chinese", "中文")),
            Map.entry("ar", new Language("ar", "Arabic", "العربية")),
            Map.entry("hi", new Language("hi", "Hindi", "हिन्दी")),
            Map.entry("sv", new Language("sv", "Swedish", "Svenska")),
            Map.entry("da", new Language("da", "Danish", "Dansk")),
            Map.entry("fi", new Language("fi", "Finnish", "Suomi")),
            Map.entry("no", new Language("no", "Norwegian", "Norsk")),
            Map.entry("cs", new Language("cs", "Czech", "Čeština")),
            Map.entry("hu", new Language("hu", "Hungarian", "Magyar"))
        );
    }
}