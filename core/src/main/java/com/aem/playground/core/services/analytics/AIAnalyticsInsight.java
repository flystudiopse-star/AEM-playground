package com.aem.playground.core.services.analytics;

import java.util.ArrayList;
import java.util.List;

public class AIAnalyticsInsight {
    private final String insightId;
    private final String insightType;
    private final String title;
    private final String description;
    private final String recommendation;
    private final double confidence;
    private final List<String> affectedPages;
    private final String category;
    private final int priority;
    private final long timestamp;

    private AIAnalyticsInsight(Builder builder) {
        this.insightId = builder.insightId;
        this.insightType = builder.insightType;
        this.title = builder.title;
        this.description = builder.description;
        this.recommendation = builder.recommendation;
        this.confidence = builder.confidence;
        this.affectedPages = builder.affectedPages != null ? builder.affectedPages : new ArrayList<>();
        this.category = builder.category;
        this.priority = builder.priority;
        this.timestamp = builder.timestamp;
    }

    public String getInsightId() {
        return insightId;
    }

    public String getInsightType() {
        return insightType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<String> getAffectedPages() {
        return affectedPages;
    }

    public String getCategory() {
        return category;
    }

    public int getPriority() {
        return priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String insightId;
        private String insightType;
        private String title;
        private String description;
        private String recommendation;
        private double confidence;
        private List<String> affectedPages;
        private String category;
        private int priority;
        private long timestamp = System.currentTimeMillis();

        public Builder insightId(String insightId) {
            this.insightId = insightId;
            return this;
        }

        public Builder insightType(String insightType) {
            this.insightType = insightType;
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

        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder affectedPages(List<String> affectedPages) {
            this.affectedPages = affectedPages;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AIAnalyticsInsight build() {
            return new AIAnalyticsInsight(this);
        }
    }

    public static final String TYPE_PERFORMANCE = "performance";
    public static final String TYPE_TREND = "trend";
    public static final String TYPE_CONTENT_QUALITY = "content_quality";
    public static final String TYPE_SEO = "seo";
    public static final String TYPE_ENGAGEMENT = "engagement";
    public static final String TYPE_RECOMMENDATION = "recommendation";

    public static final String CATEGORY_TRENDING = "trending";
    public static final String CATEGORY_IMPROVEMENT = "improvement";
    public static final String CATEGORY_OPPORTUNITY = "opportunity";
    public static final String CATEGORY_RISK = "risk";
}