package com.aem.playground.core.services;

public enum ModerationCategory {
    HATE_SPEECH("Hate Speech"),
    VIOLENCE("Violence"),
    SEXUAL_CONTENT("Sexual Content"),
    SELF_HARM("Self Harm"),
    HARASSMENT("Harassment"),
    MISINFORMATION("Misinformation"),
    SPAM("Spam"),
    PROHIBITED_WORDS("Prohibited Words"),
    SENSITIVE_TOPICS("Sensitive Topics"),
    BRAND_VIOLATION("Brand Violation"),
    LEGAL_RISK("Legal Risk"),
    CUSTOM("Custom");

    private final String displayName;

    ModerationCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}