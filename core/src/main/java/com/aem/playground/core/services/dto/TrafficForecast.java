package com.aem.playground.core.services.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TrafficForecast {

    private String contentPath;
    private String contentType;
    private List<TrafficProjection> projections;
    private double peakTrafficExpected;
    private LocalDate peakDate;
    private Map<String, Double> trafficBySource;
    private Map<String, Double> trafficByDevice;
    private double averageDailyViews;
    private double totalMonthlyViews;
    private double growthRate;

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<TrafficProjection> getProjections() {
        return projections;
    }

    public void setProjections(List<TrafficProjection> projections) {
        this.projections = projections;
    }

    public double getPeakTrafficExpected() {
        return peakTrafficExpected;
    }

    public void setPeakTrafficExpected(double peakTrafficExpected) {
        this.peakTrafficExpected = peakTrafficExpected;
    }

    public LocalDate getPeakDate() {
        return peakDate;
    }

    public void setPeakDate(LocalDate peakDate) {
        this.peakDate = peakDate;
    }

    public Map<String, Double> getTrafficBySource() {
        return trafficBySource;
    }

    public void setTrafficBySource(Map<String, Double> trafficBySource) {
        this.trafficBySource = trafficBySource;
    }

    public Map<String, Double> getTrafficByDevice() {
        return trafficByDevice;
    }

    public void setTrafficByDevice(Map<String, Double> trafficByDevice) {
        this.trafficByDevice = trafficByDevice;
    }

    public double getAverageDailyViews() {
        return averageDailyViews;
    }

    public void setAverageDailyViews(double averageDailyViews) {
        this.averageDailyViews = averageDailyViews;
    }

    public double getTotalMonthlyViews() {
        return totalMonthlyViews;
    }

    public void setTotalMonthlyViews(double totalMonthlyViews) {
        this.totalMonthlyViews = totalMonthlyViews;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    public static TrafficForecast create(String contentPath, String contentType) {
        TrafficForecast forecast = new TrafficForecast();
        forecast.setContentPath(contentPath);
        forecast.setContentType(contentType);
        return forecast;
    }

    public static class TrafficProjection {
        private LocalDate date;
        private double predictedViews;
        private double predictedUniqueVisitors;
        private double confidenceLow;
        private double confidenceHigh;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public double getPredictedViews() {
            return predictedViews;
        }

        public void setPredictedViews(double predictedViews) {
            this.predictedViews = predictedViews;
        }

        public double getPredictedUniqueVisitors() {
            return predictedUniqueVisitors;
        }

        public void setPredictedUniqueVisitors(double predictedUniqueVisitors) {
            this.predictedUniqueVisitors = predictedUniqueVisitors;
        }

        public double getConfidenceLow() {
            return confidenceLow;
        }

        public void setConfidenceLow(double confidenceLow) {
            this.confidenceLow = confidenceLow;
        }

        public double getConfidenceHigh() {
            return confidenceHigh;
        }

        public void setConfidenceHigh(double confidenceHigh) {
            this.confidenceHigh = confidenceHigh;
        }
    }
}