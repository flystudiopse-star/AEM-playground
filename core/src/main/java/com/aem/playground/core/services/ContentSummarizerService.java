package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;

import java.util.List;

public interface ContentSummarizerService {

    ContentSummary generateSummary(String contentPath, String contentText, int maxLength);

    ExecutiveSummary generateExecutiveSummary(String contentPath, String contentText, int maxLength);

    List<KeyTakeaway> extractKeyTakeaways(String contentPath, String contentText, int maxTakeaways);

    List<ContentHighlight> extractHighlights(String contentPath, String contentText, int maxHighlights);

    ContentFragmentSummary summarizeContentFragment(String fragmentPath, String modelName, String contentData);

    SummarizationDashboard getDashboard();
}