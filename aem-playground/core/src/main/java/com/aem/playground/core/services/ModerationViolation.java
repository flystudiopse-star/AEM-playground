package com.aem.playground.core.services;

public class ModerationViolation {

    private ModerationCategory category;
    private double confidence;
    private String description;
    private String matchedContent;
    private int startIndex;
    private int endIndex;
    private boolean autoCensored;
    private String severity;
    private String policyReference;

    public ModerationViolation() {}

    public static ModerationViolationBuilder builder() {
        return new ModerationViolationBuilder();
    }

    public ModerationCategory getCategory() {
        return category;
    }

    public void setCategory(ModerationCategory category) {
        this.category = category;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMatchedContent() {
        return matchedContent;
    }

    public void setMatchedContent(String matchedContent) {
        this.matchedContent = matchedContent;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public boolean isAutoCensored() {
        return autoCensored;
    }

    public void setAutoCensored(boolean autoCensored) {
        this.autoCensored = autoCensored;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getPolicyReference() {
        return policyReference;
    }

    public void setPolicyReference(String policyReference) {
        this.policyReference = policyReference;
    }

    public static class ModerationViolationBuilder {
        private final ModerationViolation violation = new ModerationViolation();

        public ModerationViolationBuilder category(ModerationCategory category) {
            violation.category = category;
            return this;
        }

        public ModerationViolationBuilder confidence(double confidence) {
            violation.confidence = confidence;
            return this;
        }

        public ModerationViolationBuilder description(String description) {
            violation.description = description;
            return this;
        }

        public ModerationViolationBuilder matchedContent(String matchedContent) {
            violation.matchedContent = matchedContent;
            return this;
        }

        public ModerationViolationBuilder startIndex(int startIndex) {
            violation.startIndex = startIndex;
            return this;
        }

        public ModerationViolationBuilder endIndex(int endIndex) {
            violation.endIndex = endIndex;
            return this;
        }

        public ModerationViolationBuilder autoCensored(boolean autoCensored) {
            violation.autoCensored = autoCensored;
            return this;
        }

        public ModerationViolationBuilder severity(String severity) {
            violation.severity = severity;
            return this;
        }

        public ModerationViolationBuilder policyReference(String policyReference) {
            violation.policyReference = policyReference;
            return this;
        }

        public ModerationViolation build() {
            return violation;
        }
    }
}