package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class LayoutSuggestion {

    private String pagePath;
    private String layoutType;
    private List<ComponentSuggestion> components;
    private List<ResponsiveLayout> responsiveLayouts;
    private List<ABTestSuggestion> abTestSuggestions;
    private List<ExperienceFragmentSuggestion> experienceFragmentSuggestions;
    private double confidenceScore;
    private Map<String, Object> metadata;

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    public List<ComponentSuggestion> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentSuggestion> components) {
        this.components = components;
    }

    public List<ResponsiveLayout> getResponsiveLayouts() {
        return responsiveLayouts;
    }

    public void setResponsiveLayouts(List<ResponsiveLayout> responsiveLayouts) {
        this.responsiveLayouts = responsiveLayouts;
    }

    public List<ABTestSuggestion> getAbTestSuggestions() {
        return abTestSuggestions;
    }

    public void setAbTestSuggestions(List<ABTestSuggestion> abTestSuggestions) {
        this.abTestSuggestions = abTestSuggestions;
    }

    public List<ExperienceFragmentSuggestion> getExperienceFragmentSuggestions() {
        return experienceFragmentSuggestions;
    }

    public void setExperienceFragmentSuggestions(List<ExperienceFragmentSuggestion> experienceFragmentSuggestions) {
        this.experienceFragmentSuggestions = experienceFragmentSuggestions;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}