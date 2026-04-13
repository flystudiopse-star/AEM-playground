package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class ComponentSuggestion {

    private String componentResourceType;
    private String componentName;
    private int position;
    private String container;
    private Map<String, Object> properties;
    private List<String> requiredContent;
    private double relevanceScore;

    public String getComponentResourceType() {
        return componentResourceType;
    }

    public void setComponentResourceType(String componentResourceType) {
        this.componentResourceType = componentResourceType;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<String> getRequiredContent() {
        return requiredContent;
    }

    public void setRequiredContent(List<String> requiredContent) {
        this.requiredContent = requiredContent;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
}