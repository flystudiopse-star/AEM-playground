package com.aem.playground.core.services;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.aem.playground.core.services.ErrorDetectionService.ErrorReport;

class ErrorReportTest {

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
                .build(),
            ContentError.builder()
                .type(ErrorType.AUTHORING_ERROR)
                .severity(ErrorSeverity.INFO)
                .message("Typo")
                .build()
        );

        ErrorReport report = ErrorReport.create("/content/home", errors, 150L);

        assertEquals("/content/home", report.getContentPath());
        assertEquals(3, report.getTotalErrors());
        assertEquals(1, report.getCriticalErrors());
        assertEquals(1, report.getWarningErrors());
        assertEquals(1, report.getInfoErrors());
        assertEquals(150L, report.getProcessingTimeMs());
    }

    @Test
    void testErrorReportWithEmptyErrors() {
        ErrorReport report = ErrorReport.create("/content/home", Collections.emptyList(), 50L);

        assertEquals(0, report.getTotalErrors());
        assertEquals(0, report.getCriticalErrors());
        assertEquals(0, report.getWarningErrors());
        assertEquals(0, report.getInfoErrors());
    }

    @Test
    void testErrorReportGetErrors() {
        ContentError error = ContentError.builder()
            .type(ErrorType.BROKEN_LINK)
            .message("Test error")
            .build();

        ErrorReport report = ErrorReport.create("/content/home", Arrays.asList(error), 100L);

        assertEquals(1, report.getErrors().size());
        assertEquals(error, report.getErrors().get(0));
    }
}