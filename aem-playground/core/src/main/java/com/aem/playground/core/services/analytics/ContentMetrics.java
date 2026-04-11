package com.aem.playground.core.services.analytics;

import java.util.HashMap;
import java.util.Map;

public class ContentMetrics {
    private final String contentPath;
    private final String contentTitle;
    private final String contentType;
    private final long pageViews;
    private final long uniqueVisitors;
    private final long avgTimeOnPage;
    private final double bounceRate;
    private final long conversionCount;
    private final Map<String, Object> additionalMetrics;
    private final long timestamp;

    private ContentMetrics(Builder builder) {
        this.contentPath = builder.contentPath;
        this.contentTitle = builder.contentTitle;
        this.contentType = builder.contentType;
        this.pageViews = builder.pageViews;
        this.uniqueVisitors = builder.uniqueVisitors;
        this.avgTimeOnPage = builder.avgTimeOnPage;
        this.bounceRate = builder.bounceRate;
        this.conversionCount = builder.conversionCount;
        this.additionalMetrics = builder.additionalMetrics != null ? builder.additionalMetrics : new HashMap<>();
        this.timestamp = builder.timestamp;
    }

    public String getContentPath() {
        return contentPath;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentType() {
        return contentType;
    }

    public long getPageViews() {
        return pageViews;
    }

    public long getUniqueVisitors() {
        return uniqueVisitors;
    }

    public long getAvgTimeOnPage() {
        return avgTimeOnPage;
    }

    public double getBounceRate() {
        return bounceRate;
    }

    public long getConversionCount() {
        return conversionCount;
    }

    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String contentPath;
        private String contentTitle;
        private String contentType;
        private long pageViews;
        private long uniqueVisitors;
        private long avgTimeOnPage;
        private double bounceRate;
        private long conversionCount;
        private Map<String, Object> additionalMetrics;
        private long timestamp = System.currentTimeMillis();

        public Builder contentPath(String contentPath) {
            this.contentPath = contentPath;
            return this;
        }

        public Builder contentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder pageViews(long pageViews) {
            this.pageViews = pageViews;
            return this;
        }

        public Builder uniqueVisitors(long uniqueVisitors) {
            this.uniqueVisitors = uniqueVisitors;
            return this;
        }

        public Builder avgTimeOnPage(long avgTimeOnPage) {
            this.avgTimeOnPage = avgTimeOnPage;
            return this;
        }

        public Builder bounceRate(double bounceRate) {
            this.bounceRate = bounceRate;
            return this;
        }

        public Builder conversionCount(long conversionCount) {
            this.conversionCount = conversionCount;
            return this;
        }

        public Builder additionalMetrics(Map<String, Object> additionalMetrics) {
            this.additionalMetrics = additionalMetrics;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ContentMetrics build() {
            return new ContentMetrics(this);
        }
    }
}