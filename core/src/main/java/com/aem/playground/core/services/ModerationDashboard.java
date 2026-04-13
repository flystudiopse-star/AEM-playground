package com.aem.playground.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModerationDashboard {

    private long generatedAt;
    private int totalContentReviewedToday;
    private int totalPendingApproval;
    private int totalApprovedToday;
    private int totalRejectedToday;
    private int totalCensoredToday;
    private double approvalRate;
    private double rejectionRate;
    private List<CategoryCount> categoryBreakdown;
    private List<RecentActivity> recentActivity;
    private Map<String, Object> additionalMetrics;

    public ModerationDashboard() {
        this.generatedAt = System.currentTimeMillis();
        this.categoryBreakdown = new ArrayList<>();
        this.recentActivity = new ArrayList<>();
        this.additionalMetrics = new HashMap<>();
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public int getTotalContentReviewedToday() {
        return totalContentReviewedToday;
    }

    public void setTotalContentReviewedToday(int totalContentReviewedToday) {
        this.totalContentReviewedToday = totalContentReviewedToday;
    }

    public int getTotalPendingApproval() {
        return totalPendingApproval;
    }

    public void setTotalPendingApproval(int totalPendingApproval) {
        this.totalPendingApproval = totalPendingApproval;
    }

    public int getTotalApprovedToday() {
        return totalApprovedToday;
    }

    public void setTotalApprovedToday(int totalApprovedToday) {
        this.totalApprovedToday = totalApprovedToday;
    }

    public int getTotalRejectedToday() {
        return totalRejectedToday;
    }

    public void setTotalRejectedToday(int totalRejectedToday) {
        this.totalRejectedToday = totalRejectedToday;
    }

    public int getTotalCensoredToday() {
        return totalCensoredToday;
    }

    public void setTotalCensoredToday(int totalCensoredToday) {
        this.totalCensoredToday = totalCensoredToday;
    }

    public double getApprovalRate() {
        return approvalRate;
    }

    public void setApprovalRate(double approvalRate) {
        this.approvalRate = approvalRate;
    }

    public double getRejectionRate() {
        return rejectionRate;
    }

    public void setRejectionRate(double rejectionRate) {
        this.rejectionRate = rejectionRate;
    }

    public List<CategoryCount> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategoryCount> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public List<RecentActivity> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<RecentActivity> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }

    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }

    public static class CategoryCount {
        private ModerationCategory category;
        private int count;
        private double percentage;

        public ModerationCategory getCategory() {
            return category;
        }

        public void setCategory(ModerationCategory category) {
            this.category = category;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    public static class RecentActivity {
        private String contentId;
        private String action;
        private String performedBy;
        private long timestamp;

        public String getContentId() {
            return contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getPerformedBy() {
            return performedBy;
        }

        public void setPerformedBy(String performedBy) {
            this.performedBy = performedBy;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}