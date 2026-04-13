package com.aem.playground.core.services.dto;

import java.time.ZonedDateTime;
import java.util.List;

public class TimeSlotRecommendation {

    private String contentType;
    private ZonedDateTime slotTime;
    private double predictedEngagement;
    private double confidenceScore;
    private String reason;
    private List<String> audienceSegments;
    private boolean isPeakTime;
    private boolean isRecurring;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ZonedDateTime getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(ZonedDateTime slotTime) {
        this.slotTime = slotTime;
    }

    public double getPredictedEngagement() {
        return predictedEngagement;
    }

    public void setPredictedEngagement(double predictedEngagement) {
        this.predictedEngagement = predictedEngagement;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getAudienceSegments() {
        return audienceSegments;
    }

    public void setAudienceSegments(List<String> audienceSegments) {
        this.audienceSegments = audienceSegments;
    }

    public boolean isPeakTime() {
        return isPeakTime;
    }

    public void setPeakTime(boolean isPeakTime) {
        this.isPeakTime = isPeakTime;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public static TimeSlotRecommendation create(String contentType, ZonedDateTime slotTime) {
        TimeSlotRecommendation recommendation = new TimeSlotRecommendation();
        recommendation.setContentType(contentType);
        recommendation.setSlotTime(slotTime);
        return recommendation;
    }
}