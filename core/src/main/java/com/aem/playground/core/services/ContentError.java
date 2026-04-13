package com.aem.playground.core.services;

import java.util.List;
import java.util.Map;

public class ContentError {
    private final String errorId;
    private final ErrorType type;
    private final ErrorSeverity severity;
    private final String message;
    private final String contentPath;
    private final String location;
    private final String suggestedFix;
    private final Map<String, Object> metadata;
    private final long detectedAt;

    private ContentError(String errorId, ErrorType type, ErrorSeverity severity, String message,
                         String contentPath, String location, String suggestedFix, 
                         Map<String, Object> metadata, long detectedAt) {
        this.errorId = errorId;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.contentPath = contentPath;
        this.location = location;
        this.suggestedFix = suggestedFix;
        this.metadata = metadata;
        this.detectedAt = detectedAt;
    }

    public static ContentError create(String errorId, ErrorType type, ErrorSeverity severity, 
                                       String message, String contentPath, String location,
                                       String suggestedFix, Map<String, Object> metadata, long detectedAt) {
        return new ContentError(errorId, type, severity, message, contentPath, location, 
                               suggestedFix, metadata, detectedAt);
    }

    public static ContentErrorBuilder builder() {
        return new ContentErrorBuilder();
    }

    public String getErrorId() {
        return errorId;
    }

    public ErrorType getType() {
        return type;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getContentPath() {
        return contentPath;
    }

    public String getLocation() {
        return location;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public long getDetectedAt() {
        return detectedAt;
    }

    public static class ContentErrorBuilder {
        private String errorId;
        private ErrorType type;
        private ErrorSeverity severity = ErrorSeverity.WARNING;
        private String message;
        private String contentPath;
        private String location;
        private String suggestedFix;
        private Map<String, Object> metadata;
        private long detectedAt = System.currentTimeMillis();

        public ContentErrorBuilder errorId(String errorId) {
            this.errorId = errorId;
            return this;
        }

        public ContentErrorBuilder type(ErrorType type) {
            this.type = type;
            return this;
        }

        public ContentErrorBuilder severity(ErrorSeverity severity) {
            this.severity = severity;
            return this;
        }

        public ContentErrorBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ContentErrorBuilder contentPath(String contentPath) {
            this.contentPath = contentPath;
            return this;
        }

        public ContentErrorBuilder location(String location) {
            this.location = location;
            return this;
        }

        public ContentErrorBuilder suggestedFix(String suggestedFix) {
            this.suggestedFix = suggestedFix;
            return this;
        }

        public ContentErrorBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ContentErrorBuilder detectedAt(long detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }

        public ContentError build() {
            if (errorId == null) {
                errorId = "err_" + System.currentTimeMillis();
            }
            return ContentError.create(errorId, type, severity, message, contentPath, 
                                       location, suggestedFix, metadata, detectedAt);
        }
    }
}