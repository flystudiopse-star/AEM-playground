package com.aem.playground.core.services.dto;

import java.util.HashMap;
import java.util.Map;

public class ContentQAIssue {

    public enum Severity {
        CRITICAL("Critical"),
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");

        private final String displayName;

        Severity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum IssueType {
        BROKEN_LINK("Broken Link"),
        ACCESSIBILITY("Accessibility"),
        CONTENT_QUALITY("Content Quality"),
        BRAND_CONSISTENCY("Brand Consistency"),
        STRUCTURE("Structure"),
        SEO("SEO"),
        READABILITY("Readability");

        private final String displayName;

        IssueType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final IssueType type;
    private final Severity severity;
    private final String title;
    private final String description;
    private final String location;
    private final String suggestion;
    private final Map<String, Object> metadata;

    private ContentQAIssue(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.title = builder.title;
        this.description = builder.description;
        this.location = builder.location;
        this.suggestion = builder.suggestion;
        this.metadata = builder.metadata != null ? new HashMap<>(builder.metadata) : new HashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public IssueType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static class Builder {
        private IssueType type;
        private Severity severity = Severity.MEDIUM;
        private String title;
        private String description;
        private String location;
        private String suggestion;
        private Map<String, Object> metadata;

        public Builder type(IssueType type) {
            this.type = type;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ContentQAIssue build() {
            return new ContentQAIssue(this);
        }
    }
}