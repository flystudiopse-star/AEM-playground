package com.aem.playground.core.services;

import java.util.List;
import java.util.Map;

public class ApprovalQueueItem {

    private String itemId;
    private String contentPath;
    private String contentTitle;
    private String contentType;
    private String submittedBy;
    private long submittedAt;
    private ApprovalStatus status;
    private String assignedTo;
    private List<ModerationViolation> pendingViolations;
    private String reviewNotes;
    private int reviewAttempts;
    private long lastReviewedAt;
    private Map<String, Object> metadata;

    public ApprovalQueueItem() {
        this.submittedAt = System.currentTimeMillis();
        this.status = ApprovalStatus.PENDING;
        this.reviewAttempts = 0;
    }

    public static ApprovalQueueItemBuilder builder() {
        return new ApprovalQueueItemBuilder();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(long submittedAt) {
        this.submittedAt = submittedAt;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public List<ModerationViolation> getPendingViolations() {
        return pendingViolations;
    }

    public void setPendingViolations(List<ModerationViolation> pendingViolations) {
        this.pendingViolations = pendingViolations;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public int getReviewAttempts() {
        return reviewAttempts;
    }

    public void setReviewAttempts(int reviewAttempts) {
        this.reviewAttempts = reviewAttempts;
    }

    public long getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(long lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static class ApprovalQueueItemBuilder {
        private final ApprovalQueueItem item = new ApprovalQueueItem();

        public ApprovalQueueItemBuilder itemId(String itemId) {
            item.itemId = itemId;
            return this;
        }

        public ApprovalQueueItemBuilder contentPath(String contentPath) {
            item.contentPath = contentPath;
            return this;
        }

        public ApprovalQueueItemBuilder contentTitle(String contentTitle) {
            item.contentTitle = contentTitle;
            return this;
        }

        public ApprovalQueueItemBuilder contentType(String contentType) {
            item.contentType = contentType;
            return this;
        }

        public ApprovalQueueItemBuilder submittedBy(String submittedBy) {
            item.submittedBy = submittedBy;
            return this;
        }

        public ApprovalQueueItemBuilder status(ApprovalStatus status) {
            item.status = status;
            return this;
        }

        public ApprovalQueueItemBuilder assignedTo(String assignedTo) {
            item.assignedTo = assignedTo;
            return this;
        }

        public ApprovalQueueItemBuilder pendingViolations(List<ModerationViolation> pendingViolations) {
            item.pendingViolations = pendingViolations;
            return this;
        }

        public ApprovalQueueItemBuilder reviewNotes(String reviewNotes) {
            item.reviewNotes = reviewNotes;
            return this;
        }

        public ApprovalQueueItemBuilder metadata(Map<String, Object> metadata) {
            item.metadata = metadata;
            return this;
        }

        public ApprovalQueueItem build() {
            return item;
        }
    }

    public enum ApprovalStatus {
        PENDING,
        IN_REVIEW,
        APPROVED,
        REJECTED,
        REQUIRES_REVISION,
        AUTO_CENSORED
    }
}