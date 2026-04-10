package com.aem.playground.core.services;

import java.util.List;

public interface ErrorDetectionService {

    ErrorReport detectErrors(String contentPath, String content);

    List<ContentError> detectBrokenLinks(String contentPath, String content);

    List<ContentError> detectMissingAssets(String contentPath, String content);

    List<ContentError> detectContentStructureIssues(String contentPath, String content);

    List<ContentError> detectAuthoringErrors(String contentPath, String content);

    ErrorFix suggestFix(ContentError error);

    ErrorDashboard getErrorDashboard(String startDate, String endDate);

    class ErrorReport {
        private final String contentPath;
        private final List<ContentError> errors;
        private final int totalErrors;
        private final int criticalErrors;
        private final int warningErrors;
        private final int infoErrors;
        private final long processingTimeMs;

        private ErrorReport(String contentPath, List<ContentError> errors, long processingTimeMs) {
            this.contentPath = contentPath;
            this.errors = errors;
            this.processingTimeMs = processingTimeMs;
            this.totalErrors = errors.size();
            this.criticalErrors = (int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.CRITICAL).count();
            this.warningErrors = (int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.WARNING).count();
            this.infoErrors = (int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.INFO).count();
        }

        public static ErrorReport create(String contentPath, List<ContentError> errors, long processingTimeMs) {
            return new ErrorReport(contentPath, errors, processingTimeMs);
        }

        public String getContentPath() {
            return contentPath;
        }

        public List<ContentError> getErrors() {
            return errors;
        }

        public int getTotalErrors() {
            return totalErrors;
        }

        public int getCriticalErrors() {
            return criticalErrors;
        }

        public int getWarningErrors() {
            return warningErrors;
        }

        public int getInfoErrors() {
            return infoErrors;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }

    class ErrorDashboard {
        private final String periodStart;
        private final String periodEnd;
        private final int totalErrors;
        private final int resolvedErrors;
        private final int openErrors;
        private final List<ErrorSummary> errorsByType;
        private final List<ErrorTrend> errorTrends;
        private final List<ContentError> recentErrors;

        private ErrorDashboard(String periodStart, String periodEnd, int totalErrors, int resolvedErrors, 
                               int openErrors, List<ErrorSummary> errorsByType, 
                               List<ErrorTrend> errorTrends, List<ContentError> recentErrors) {
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.totalErrors = totalErrors;
            this.resolvedErrors = resolvedErrors;
            this.openErrors = openErrors;
            this.errorsByType = errorsByType;
            this.errorTrends = errorTrends;
            this.recentErrors = recentErrors;
        }

        public static ErrorDashboard create(String periodStart, String periodEnd, int totalErrors, 
                                            int resolvedErrors, int openErrors,
                                            List<ErrorSummary> errorsByType, 
                                            List<ErrorTrend> errorTrends,
                                            List<ContentError> recentErrors) {
            return new ErrorDashboard(periodStart, periodEnd, totalErrors, resolvedErrors, 
                                      openErrors, errorsByType, errorTrends, recentErrors);
        }

        public String getPeriodStart() {
            return periodStart;
        }

        public String getPeriodEnd() {
            return periodEnd;
        }

        public int getTotalErrors() {
            return totalErrors;
        }

        public int getResolvedErrors() {
            return resolvedErrors;
        }

        public int getOpenErrors() {
            return openErrors;
        }

        public List<ErrorSummary> getErrorsByType() {
            return errorsByType;
        }

        public List<ErrorTrend> getErrorTrends() {
            return errorTrends;
        }

        public List<ContentError> getRecentErrors() {
            return recentErrors;
        }
    }

    class ErrorSummary {
        private final ErrorType type;
        private final int count;

        private ErrorSummary(ErrorType type, int count) {
            this.type = type;
            this.count = count;
        }

        public static ErrorSummary create(ErrorType type, int count) {
            return new ErrorSummary(type, count);
        }

        public ErrorType getType() {
            return type;
        }

        public int getCount() {
            return count;
        }
    }

    class ErrorTrend {
        private final String date;
        private final int errorCount;

        private ErrorTrend(String date, int errorCount) {
            this.date = date;
            this.errorCount = errorCount;
        }

        public static ErrorTrend create(String date, int errorCount) {
            return new ErrorTrend(date, errorCount);
        }

        public String getDate() {
            return date;
        }

        public int getErrorCount() {
            return errorCount;
        }
    }
}