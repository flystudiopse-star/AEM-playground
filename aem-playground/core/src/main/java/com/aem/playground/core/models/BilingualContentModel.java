package com.aem.playground.core.models;

import com.aem.playground.core.services.TranslationService;
import com.aem.playground.core.services.dto.BilingualContentComparison;
import com.aem.playground.core.services.impl.ContentTranslatorServiceImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Model(adapters = BilingualContentModel.class)
public class BilingualContentModel {

    @RequestAttribute
    @Default(values = "")
    private String pagePath;

    @RequestAttribute
    @Default(values = "en")
    private String sourceLanguage;

    @RequestAttribute
    @Default(values = "de")
    private String targetLanguage;

    private TranslationService translationService;
    private BilingualContentComparison comparison;

    public void init() {
        if (translationService != null && pagePath != null) {
            comparison = ((ContentTranslatorServiceImpl) translationService)
                .compareBilingualContent(pagePath, sourceLanguage, targetLanguage);
        }
    }

    public String getPagePath() {
        return pagePath;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public boolean hasDifferences() {
        return comparison != null && comparison.hasDifferences();
    }

    public List<BilingualContentComparison.ContentDifference> getDifferences() {
        return comparison != null ? comparison.getDifferences() : null;
    }

    public List<BilingualContentComparison.ContentDifference> getModifiedDifferences() {
        if (comparison == null) return null;
        return comparison.getDifferences().stream()
            .filter(d -> d.getDifferenceType() != BilingualContentComparison.DifferenceType.EQUAL)
            .collect(Collectors.toList());
    }

    public Map<String, BilingualContentComparison.SourceTargetPair> getComponentPairs() {
        return comparison != null ? comparison.getComponentPairs() : null;
    }

    public Map<String, BilingualContentComparison.SourceTargetPair> getMetadataPairs() {
        return comparison != null ? comparison.getMetadataPairs() : null;
    }

    public int getTotalComponents() {
        return comparison != null ? comparison.getComponentPairs().size() : 0;
    }

    public int getModifiedComponents() {
        if (comparison == null) return 0;
        return (int) comparison.getDifferences().stream()
            .filter(d -> d.getDifferenceType() == BilingualContentComparison.DifferenceType.MODIFIED)
            .count();
    }

    public int getMissingComponents() {
        if (comparison == null) return 0;
        return (int) comparison.getDifferences().stream()
            .filter(d -> d.getDifferenceType() == BilingualContentComparison.DifferenceType.MISSING_IN_TARGET)
            .count();
    }

    public String getSourceLanguageDisplayName() {
        return translationService != null ? translationService.getLanguageDisplayName(sourceLanguage) : sourceLanguage;
    }

    public String getTargetLanguageDisplayName() {
        return translationService != null ? translationService.getLanguageDisplayName(targetLanguage) : targetLanguage;
    }

    public List<String> getSupportedLanguages() {
        return translationService != null ? translationService.getSupportedLanguages() : null;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}