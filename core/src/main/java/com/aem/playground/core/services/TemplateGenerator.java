package com.aem.playground.core.services;

import java.util.List;
import java.util.Map;

public interface TemplateGenerator {

    TemplateResult generateTemplate(TemplateRequest request) throws Exception;

    List<TemplateSection> generateContentSections(String templateType, String pageDescription) throws Exception;

    Map<String, String> generatePolicyMappings(String templateType);

    byte[] generatePreviewThumbnail(String templateType) throws Exception;

    class TemplateRequest {
        private String templateType;
        private String pageTitle;
        private String pageDescription;
        private boolean generateAiContent;
        private String aiServiceUrl;
        private String targetPath;

        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
        public String getPageTitle() { return pageTitle; }
        public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
        public String getPageDescription() { return pageDescription; }
        public void setPageDescription(String pageDescription) { this.pageDescription = pageDescription; }
        public boolean isGenerateAiContent() { return generateAiContent; }
        public void setGenerateAiContent(boolean generateAiContent) { this.generateAiContent = generateAiContent; }
        public String getAiServiceUrl() { return aiServiceUrl; }
        public void setAiServiceUrl(String aiServiceUrl) { this.aiServiceUrl = aiServiceUrl; }
        public String getTargetPath() { return targetPath; }
        public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    }

    class TemplateResult {
        private String templateName;
        private String templatePath;
        private Map<String, Object> structure;
        private List<TemplateSection> sections;
        private Map<String, String> policyMappings;
        private byte[] previewThumbnail;
        private List<String> errors;

        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getTemplatePath() { return templatePath; }
        public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }
        public Map<String, Object> getStructure() { return structure; }
        public void setStructure(Map<String, Object> structure) { this.structure = structure; }
        public List<TemplateSection> getSections() { return sections; }
        public void setSections(List<TemplateSection> sections) { this.sections = sections; }
        public Map<String, String> getPolicyMappings() { return policyMappings; }
        public void setPolicyMappings(Map<String, String> policyMappings) { this.policyMappings = policyMappings; }
        public byte[] getPreviewThumbnail() { return previewThumbnail; }
        public void setPreviewThumbnail(byte[] previewThumbnail) { this.previewThumbnail = previewThumbnail; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public boolean isSuccess() { return errors == null || errors.isEmpty(); }
    }

    class TemplateSection {
        private String sectionName;
        private String componentType;
        private Map<String, String> properties;
        private String content;
        private List<TemplateSection> children;

        public String getSectionName() { return sectionName; }
        public void setSectionName(String sectionName) { this.sectionName = sectionName; }
        public String getComponentType() { return componentType; }
        public void setComponentType(String componentType) { this.componentType = componentType; }
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public List<TemplateSection> getChildren() { return children; }
        public void setChildren(List<TemplateSection> children) { this.children = children; }
    }
}