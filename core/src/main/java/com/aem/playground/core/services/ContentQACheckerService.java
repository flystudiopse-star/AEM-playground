package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.ContentQAReport;

public interface ContentQACheckerService {

    ContentQAReport analyzeContent(String content, String contentPath);

    ContentQAReport checkBrokenLinks(String content, String contentPath);

    ContentQAReport validateStructure(String content, String contentPath);

    ContentQAReport checkAccessibility(String content, String contentPath);

    ContentQAReport checkBrandConsistency(String content, String contentPath);

    ContentQAReport generateFullReport(String content, String contentPath);
}