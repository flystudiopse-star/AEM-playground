package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.PageSummary;
import com.aem.playground.core.services.dto.ContentFragmentSummary;
import com.aem.playground.core.services.dto.Excerpt;
import com.aem.playground.core.services.dto.MetaDescription;

import java.util.Map;

public interface SummarizationService {

    enum SummaryLength {
        BRIEF(50),
        STANDARD(100),
        DETAILED(250);

        private final int wordCount;

        SummaryLength(int wordCount) {
            this.wordCount = wordCount;
        }

        public int getWordCount() {
            return wordCount;
        }
    }

    PageSummary summarizePage(String pagePath, String pageContent, SummaryLength length);

    PageSummary summarizePage(String pagePath, String pageContent, SummaryLength length, Map<String, Object> options);

    ContentFragmentSummary summarizeContentFragment(String fragmentPath, String fragmentContent, SummaryLength length);

    ContentFragmentSummary summarizeContentFragment(String fragmentPath, String fragmentContent, SummaryLength length, Map<String, Object> options);

    Excerpt generateExcerpt(String content, SummaryLength length);

    Excerpt generateExcerpt(String content, SummaryLength length, Map<String, Object> options);

    MetaDescription createMetaDescription(String content);

    MetaDescription createMetaDescription(String content, SummaryLength length);

    MetaDescription createMetaDescription(String content, SummaryLength length, Map<String, Object> options);
}