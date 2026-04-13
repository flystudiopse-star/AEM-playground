package com.aem.playground.core.services.dto;

import java.util.List;

public class ComponentDescriptor {
    private String componentName;
    private String description;
    private String slingModel;
    private String htlTemplate;
    private String dialogXml;
    private String contentXml;
    private String css;
    private boolean responsive;
    private boolean includeCrud;
    private List<String> fields;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlingModel() {
        return slingModel;
    }

    public void setSlingModel(String slingModel) {
        this.slingModel = slingModel;
    }

    public String getHtlTemplate() {
        return htlTemplate;
    }

    public void setHtlTemplate(String htlTemplate) {
        this.htlTemplate = htlTemplate;
    }

    public String getDialogXml() {
        return dialogXml;
    }

    public void setDialogXml(String dialogXml) {
        this.dialogXml = dialogXml;
    }

    public String getContentXml() {
        return contentXml;
    }

    public void setContentXml(String contentXml) {
        this.contentXml = contentXml;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public boolean isResponsive() {
        return responsive;
    }

    public void setResponsive(boolean responsive) {
        this.responsive = responsive;
    }

    public boolean isIncludeCrud() {
        return includeCrud;
    }

    public void setIncludeCrud(boolean includeCrud) {
        this.includeCrud = includeCrud;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}