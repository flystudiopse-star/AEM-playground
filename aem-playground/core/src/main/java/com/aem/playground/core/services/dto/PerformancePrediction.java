package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PerformancePrediction {

    private String contentPath;
    private String contentType;
    private double predictedEngagementScore;
    private double predictedConversionRate;
    private double predictedPageViews;
    private double predictedBounceRate;
    private List<String> predictedTrends;
    private Map<String, Object> metrics;
    private LocalDateTime predictionDate;
    private LocalDateTime validUntil;
    private double confidenceLevel;

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

    public double getPredictedEngagementScore() {
        return predictedEngagementScore;
    }

    public void setPredictedEngagementScore(double predictedEngagementScore) {
        this.predictedEngagementScore = predictedEngagementScore;
    }

    public double getPredictedConversionRate() {
        return predictedConversionRate;
    }

    public void setPredictedConversionRate(double predictedConversionRate) {
        this.predictedConversionRate = predictedConversionRate;
    }

    public double getPredictedPageViews() {
        return predictedPageViews;
    }

    public void setPredictedPageViews(double predictedPageViews) {
        this.predictedPageViews = predictedPageViews;
    }

    public double getPredictedBounceRate() {
        return predictedBounceRate;
    }

    public void setPredictedBounceRate(double predictedBounceRate) {
        this.predictedBounceRate = predictedBounceRate;
    }

    public List<String> getPredictedTrends() {
        return predictedTrends;
    }

    public void setPredictedTrends(List<String> predictedTrends) {
        this.predictedTrends = predictedTrends;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public LocalDateTime getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(LocalDateTime predictionDate) {
        this.predictionDate = predictionDate;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public static PerformancePrediction create(String contentPath, String contentType) {
        PerformancePrediction prediction = new PerformancePrediction();
        prediction.setContentPath(contentPath);
        prediction.setContentType(contentType);
        prediction.setPredictionDate(LocalDateTime.now());
        return prediction;
    }
}