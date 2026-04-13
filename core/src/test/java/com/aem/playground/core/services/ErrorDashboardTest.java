package com.aem.playground.core.services;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.aem.playground.core.services.ErrorDetectionService.ErrorDashboard;
import static com.aem.playground.core.services.ErrorDetectionService.ErrorSummary;
import static com.aem.playground.core.services.ErrorDetectionService.ErrorTrend;
import static com.aem.playground.core.services.ErrorDetectionService.ErrorType;
import static com.aem.playground.core.services.ErrorDetectionService.ContentError;

class ErrorDashboardTest {

    @Test
    void testErrorDashboardCreate() {
        List<ErrorSummary> errorsByType = Arrays.asList(
            ErrorSummary.create(ErrorType.BROKEN_LINK, 5),
            ErrorSummary.create(ErrorType.MISSING_ASSET, 3)
        );

        List<ErrorTrend> trends = Arrays.asList(
            ErrorTrend.create("2024-01-01", 10),
            ErrorTrend.create("2024-01-02", 15)
        );

        List<ContentError> recentErrors = Collections.emptyList();

        ErrorDashboard dashboard = ErrorDashboard.create(
            "2024-01-01",
            "2024-01-31",
            20,
            12,
            8,
            errorsByType,
            trends,
            recentErrors
        );

        assertEquals("2024-01-01", dashboard.getPeriodStart());
        assertEquals("2024-01-31", dashboard.getPeriodEnd());
        assertEquals(20, dashboard.getTotalErrors());
        assertEquals(12, dashboard.getResolvedErrors());
        assertEquals(8, dashboard.getOpenErrors());
        assertEquals(2, dashboard.getErrorsByType().size());
        assertEquals(2, dashboard.getErrorTrends().size());
    }

    @Test
    void testErrorDashboardEmpty() {
        ErrorDashboard dashboard = ErrorDashboard.create(
            "2024-01-01",
            "2024-01-31",
            0,
            0,
            0,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
        );

        assertEquals(0, dashboard.getTotalErrors());
        assertTrue(dashboard.getErrorsByType().isEmpty());
        assertTrue(dashboard.getErrorTrends().isEmpty());
        assertTrue(dashboard.getRecentErrors().isEmpty());
    }

    @Test
    void testErrorSummaryCreate() {
        ErrorSummary summary = ErrorSummary.create(ErrorType.AUTHORING_ERROR, 7);

        assertEquals(ErrorType.AUTHORING_ERROR, summary.getType());
        assertEquals(7, summary.getCount());
    }

    @Test
    void testErrorTrendCreate() {
        ErrorTrend trend = ErrorTrend.create("2024-01-15", 25);

        assertEquals("2024-01-15", trend.getDate());
        assertEquals(25, trend.getErrorCount());
    }
}