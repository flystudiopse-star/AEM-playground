package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class ExperienceFragmentSuggestion {

    private String fragmentPath;
    private String fragmentName;
    private String fragmentGroup;
    private List<String> applicablePages;
    private Map<String, Object> fragmentProperties;
    private String variation;

    public String getFragmentPath() {
        return fragmentPath;
    }

    public void setFragmentPath(String fragmentPath) {
        this.fragmentPath = fragmentPath;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    public void setFragmentName(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    public String getFragmentGroup() {
        return fragmentGroup;
    }

    public void setFragmentGroup(String fragmentGroup) {
        this.fragmentGroup = fragmentGroup;
    }

    public List<String> getApplicablePages() {
        return applicablePages;
    }

    public void setApplicablePages(List<String> applicablePages) {
        this.applicablePages = applicablePages;
    }

    public Map<String, Object> getFragmentProperties() {
        return fragmentProperties;
    }

    public void setFragmentProperties(Map<String, Object> fragmentProperties) {
        this.fragmentProperties = fragmentProperties;
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }
}