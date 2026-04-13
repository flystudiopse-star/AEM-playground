package com.aem.playground.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContentCensorResult {

    private String originalContent;
    private String censoredContent;
    private List<CensoredSegment> censoredSegments;
    private int totalCensoredCount;
    private long processingTimeMs;
    private Map<String, Object> metadata;

    public ContentCensorResult() {
        this.censoredSegments = new ArrayList<>();
    }

    public static ContentCensorResultBuilder builder() {
        return new ContentCensorResultBuilder();
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getCensoredContent() {
        return censoredContent;
    }

    public void setCensoredContent(String censoredContent) {
        this.censoredContent = censoredContent;
    }

    public List<CensoredSegment> getCensoredSegments() {
        return censoredSegments;
    }

    public void setCensoredSegments(List<CensoredSegment> censoredSegments) {
        this.censoredSegments = censoredSegments;
    }

    public int getTotalCensoredCount() {
        return totalCensoredCount;
    }

    public void setTotalCensoredCount(int totalCensoredCount) {
        this.totalCensoredCount = totalCensoredCount;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static class ContentCensorResultBuilder {
        private final ContentCensorResult result = new ContentCensorResult();

        public ContentCensorResultBuilder originalContent(String originalContent) {
            result.originalContent = originalContent;
            return this;
        }

        public ContentCensorResultBuilder censoredContent(String censoredContent) {
            result.censoredContent = censoredContent;
            return this;
        }

        public ContentCensorResultBuilder censoredSegments(List<CensoredSegment> censoredSegments) {
            result.censoredSegments = censoredSegments;
            return this;
        }

        public ContentCensorResultBuilder totalCensoredCount(int totalCensoredCount) {
            result.totalCensoredCount = totalCensoredCount;
            return this;
        }

        public ContentCensorResultBuilder processingTimeMs(long processingTimeMs) {
            result.processingTimeMs = processingTimeMs;
            return this;
        }

        public ContentCensorResultBuilder metadata(Map<String, Object> metadata) {
            result.metadata = metadata;
            return this;
        }

        public ContentCensorResult build() {
            return result;
        }
    }

    public static class CensoredSegment {
        private int startIndex;
        private int endIndex;
        private String originalText;
        private String censoredText;
        private ModerationCategory category;
        private String reason;

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

        public String getOriginalText() {
            return originalText;
        }

        public void setOriginalText(String originalText) {
            this.originalText = originalText;
        }

        public String getCensoredText() {
            return censoredText;
        }

        public void setCensoredText(String censoredText) {
            this.censoredText = censoredText;
        }

        public ModerationCategory getCategory() {
            return category;
        }

        public void setCategory(ModerationCategory category) {
            this.category = category;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}