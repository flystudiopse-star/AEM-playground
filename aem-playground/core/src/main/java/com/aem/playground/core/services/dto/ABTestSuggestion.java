package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class ABTestSuggestion {

    private String testName;
    private String testId;
    private List<String> variants;
    private Map<String, Object> variantProperties;
    private String metricToTrack;
    private double estimatedTrafficPercentage;
    private int suggestedDurationDays;
    private double minimumSampleSize;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public List<String> getVariants() {
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }

    public Map<String, Object> getVariantProperties() {
        return variantProperties;
    }

    public void setVariantProperties(Map<String, Object> variantProperties) {
        this.variantProperties = variantProperties;
    }

    public String getMetricToTrack() {
        return metricToTrack;
    }

    public void setMetricToTrack(String metricToTrack) {
        this.metricToTrack = metricToTrack;
    }

    public double getEstimatedTrafficPercentage() {
        return estimatedTrafficPercentage;
    }

    public void setEstimatedTrafficPercentage(double estimatedTrafficPercentage) {
        this.estimatedTrafficPercentage = estimatedTrafficPercentage;
    }

    public int getSuggestedDurationDays() {
        return suggestedDurationDays;
    }

    public void setSuggestedDurationDays(int suggestedDurationDays) {
        this.suggestedDurationDays = suggestedDurationDays;
    }

    public double getMinimumSampleSize() {
        return minimumSampleSize;
    }

    public void setMinimumSampleSize(double minimumSampleSize) {
        this.minimumSampleSize = minimumSampleSize;
    }
}