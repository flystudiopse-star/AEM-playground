package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class OptimalPublishTime {

    private String timeSlotId;
    private int hour;
    private int dayOfWeek;
    private double engagementScore;
    private String timezone;
    private double confidenceLevel;
    private Map<String, Object> metadata;

    public String getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(String timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public double getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(double engagementScore) {
        this.engagementScore = engagementScore;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OptimalPublishTime time = new OptimalPublishTime();

        public Builder timeSlotId(String timeSlotId) {
            time.timeSlotId = timeSlotId;
            return this;
        }

        public Builder hour(int hour) {
            time.hour = hour;
            return this;
        }

        public Builder dayOfWeek(int dayOfWeek) {
            time.dayOfWeek = dayOfWeek;
            return this;
        }

        public Builder engagementScore(double engagementScore) {
            time.engagementScore = engagementScore;
            return this;
        }

        public Builder timezone(String timezone) {
            time.timezone = timezone;
            return this;
        }

        public Builder confidenceLevel(double confidenceLevel) {
            time.confidenceLevel = confidenceLevel;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            time.metadata = metadata;
            return this;
        }

        public OptimalPublishTime build() {
            return time;
        }
    }
}
