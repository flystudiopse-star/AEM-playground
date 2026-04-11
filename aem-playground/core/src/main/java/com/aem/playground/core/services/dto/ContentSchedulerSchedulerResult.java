package com.aem.playground.core.services.dto;

import java.time.ZonedDateTime;
import java.util.Map;

public class ContentSchedulerSchedulerResult {

    private String contentPath;
    private ZonedDateTime scheduledPublishTime;
    private ZonedDateTime scheduledUnpublishTime;
    private String status;
    private String schedulerJobId;
    private double predictedEngagement;
    private Map<String, Object> metadata = new java.util.HashMap<>();
    private ZonedDateTime createdAt;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public ZonedDateTime getScheduledPublishTime() {
        return scheduledPublishTime;
    }

    public void setScheduledPublishTime(ZonedDateTime scheduledPublishTime) {
        this.scheduledPublishTime = scheduledPublishTime;
    }

    public ZonedDateTime getScheduledUnpublishTime() {
        return scheduledUnpublishTime;
    }

    public void setScheduledUnpublishTime(ZonedDateTime scheduledUnpublishTime) {
        this.scheduledUnpublishTime = scheduledUnpublishTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSchedulerJobId() {
        return schedulerJobId;
    }

    public void setSchedulerJobId(String schedulerJobId) {
        this.schedulerJobId = schedulerJobId;
    }

    public double getPredictedEngagement() {
        return predictedEngagement;
    }

    public void setPredictedEngagement(double predictedEngagement) {
        this.predictedEngagement = predictedEngagement;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static ContentSchedulerSchedulerResult create(String contentPath, ZonedDateTime publishTime) {
        ContentSchedulerSchedulerResult result = new ContentSchedulerSchedulerResult();
        result.setContentPath(contentPath);
        result.setScheduledPublishTime(publishTime);
        result.setStatus("scheduled");
        result.setCreatedAt(ZonedDateTime.now());
        return result;
    }
}