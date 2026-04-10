package com.aem.playground.core.services.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ContentCalendar {

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CalendarEntry> entries;
    private List<String> suggestedTopics;
    private double forecastAccuracy;
    private LocalDateTime lastUpdated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<CalendarEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CalendarEntry> entries) {
        this.entries = entries;
    }

    public List<String> getSuggestedTopics() {
        return suggestedTopics;
    }

    public void setSuggestedTopics(List<String> suggestedTopics) {
        this.suggestedTopics = suggestedTopics;
    }

    public double getForecastAccuracy() {
        return forecastAccuracy;
    }

    public void setForecastAccuracy(double forecastAccuracy) {
        this.forecastAccuracy = forecastAccuracy;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static ContentCalendar create(String name, LocalDate startDate, LocalDate endDate) {
        ContentCalendar calendar = new ContentCalendar();
        calendar.setName(name);
        calendar.setStartDate(startDate);
        calendar.setEndDate(endDate);
        calendar.setLastUpdated(LocalDateTime.now());
        return calendar;
    }

    public static class CalendarEntry {
        private String contentPath;
        private String title;
        private LocalDate scheduledDate;
        private String contentType;
        private String status;
        private double predictedPerformance;

        public String getContentPath() {
            return contentPath;
        }

        public void setContentPath(String contentPath) {
            this.contentPath = contentPath;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LocalDate getScheduledDate() {
            return scheduledDate;
        }

        public void setScheduledDate(LocalDate scheduledDate) {
            this.scheduledDate = scheduledDate;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getPredictedPerformance() {
            return predictedPerformance;
        }

        public void setPredictedPerformance(double predictedPerformance) {
            this.predictedPerformance = predictedPerformance;
        }
    }
}