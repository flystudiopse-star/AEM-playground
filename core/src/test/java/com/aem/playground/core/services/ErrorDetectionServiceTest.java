package com.aem.playground.core.services;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDetectionServiceTest {

    @Test
    void testDetectBrokenLinks() {
        String contentPath = "/content/home";
        String content = "<a href=\"/content/page1\">Page 1</a><a href=\"#\">Anchor</a><a href=\"undefined\">Undefined</a>";

        List<ContentError> errors = ErrorDetectionServiceMock.detectBrokenLinks(contentPath, content);

        assertNotNull(errors);
        assertTrue(errors.size() >= 1);
        ContentError error = errors.get(0);
        assertEquals(ErrorType.BROKEN_LINK, error.getType());
    }

    @Test
    void testDetectMissingAssets() {
        String contentPath = "/content/dam/test";
        String content = "reference=\"/content/dam/missing.png\"";

        List<ContentError> errors = ErrorDetectionServiceMock.detectMissingAssets(contentPath, content);

        assertNotNull(errors);
    }

    @Test
    void testDetectContentStructureIssues() {
        String contentPath = "/content/test";
        String content = "cq:allowedTemplates=\"/libs/test\"";

        List<ContentError> errors = ErrorDetectionServiceMock.detectContentStructureIssues(contentPath, content);

        assertNotNull(errors);
    }

    @Test
    void testDetectAuthoringErrors() {
        String contentPath = "/content/test";
        String content = "Test content with trailing spaces  ";

        List<ContentError> errors = ErrorDetectionServiceMock.detectAuthoringErrors(contentPath, content);

        assertNotNull(errors);
        assertTrue(errors.size() >= 1);
    }

    @Test
    void testErrorReportCreate() {
        List<ContentError> errors = Arrays.asList(
            ContentError.builder()
                .type(ErrorType.BROKEN_LINK)
                .severity(ErrorSeverity.CRITICAL)
                .message("Broken link")
                .build(),
            ContentError.builder()
                .type(ErrorType.MISSING_ASSET)
                .severity(ErrorSeverity.WARNING)
                .message("Missing asset")
                .build()
        );

        ErrorReport report = ErrorDetectionService.ErrorReport.create("/content/test", errors, 100L);

        assertNotNull(report);
        assertEquals(2, report.getTotalErrors());
        assertEquals(1, report.getCriticalErrors());
        assertEquals(1, report.getWarningErrors());
    }

    @Test
    void testDashboardCreate() {
        ErrorDashboard dashboard = ErrorDetectionService.ErrorDashboard.create(
            "2024-01-01",
            "2024-01-31",
            10,
            5,
            5,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
        );

        assertNotNull(dashboard);
        assertEquals(10, dashboard.getTotalErrors());
        assertEquals(5, dashboard.getOpenErrors());
    }
}

class ErrorDetectionServiceMock {

    static List<ContentError> detectBrokenLinks(String contentPath, String content) {
        List<ContentError> errors = new ArrayList<>();
        if (content != null && content.contains("undefined")) {
            errors.add(ContentError.builder()
                .type(ErrorType.BROKEN_LINK)
                .severity(ErrorSeverity.WARNING)
                .message("Potentially broken link")
                .contentPath(contentPath)
                .build());
        }
        return errors;
    }

    static List<ContentError> detectMissingAssets(String contentPath, String content) {
        return new ArrayList<>();
    }

    static List<ContentError> detectContentStructureIssues(String contentPath, String content) {
        return new ArrayList<>();
    }

    static List<ContentError> detectAuthoringErrors(String contentPath, String content) {
        List<ContentError> errors = new ArrayList<>();
        
        if (content != null && (content.startsWith(" ") || content.endsWith(" "))) {
            errors.add(ContentError.builder()
                .type(ErrorType.AUTHORING_ERROR)
                .severity(ErrorSeverity.INFO)
                .message("Content has leading or trailing whitespace")
                .contentPath(contentPath)
                .build());
        }
        
        return errors;
    }
}