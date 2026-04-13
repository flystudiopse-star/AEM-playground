package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.LayoutSuggestion;
import com.aem.playground.core.services.dto.PageContentAnalysis;

public interface LayoutSuggestionService {

    LayoutSuggestion suggestLayout(PageContentAnalysis analysis);

    LayoutSuggestion suggestLayout(PageContentAnalysis analysis, String templateType);

    LayoutSuggestion suggestLayoutFromPrompt(PageContentAnalysis analysis, String userPrompt);

    PageContentAnalysis analyzePageContent(String pagePath);

    PageContentAnalysis analyzePageContent(String pagePath, boolean includeChildren);

    boolean validateLayoutSuggestion(LayoutSuggestion suggestion);
}