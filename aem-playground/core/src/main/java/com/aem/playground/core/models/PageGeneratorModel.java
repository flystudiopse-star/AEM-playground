/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.playground.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.Reference;
import org.apache.sling.models.annotations.Default;
import org.apache.jackrabbit.JcrConstants;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIGenerationOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Model(adaptables = Resource.class)
public class PageGeneratorModel {

    private static final String PN_AI_ENABLED = "aiEnabled";
    private static final String PN_PAGE_DESCRIPTION = "pageDescription";
    private static final String PN_AI_SERVICE_URL = "aiServiceUrl";
    private static final String PN_REGENERATE = "regenerate";

    @ValueMapValue(name = PN_AI_ENABLED, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean aiEnabled;

    @ValueMapValue(name = PN_PAGE_DESCRIPTION, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String pageDescription;

    @ValueMapValue(name = PN_AI_SERVICE_URL, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    @Default(values = "https://api.openai.com/v1/chat/completions")
    private String aiServiceUrl;

    @ValueMapValue(name = PN_REGENERATE, injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private boolean regenerate;

    @ValueMapValue(name = "pageTitle", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String pageTitle;

    @ValueMapValue(name = "pageDescription", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String generatedPageDescription;

    @ValueMapValue(name = "navigationStructure", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String navigationStructure;

    @ValueMapValue(name = "seoKeywords", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String seoKeywords;

    @ValueMapValue(name = "seoOgTitle", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String seoOgTitle;

    @ValueMapValue(name = "seoOgDescription", injectionStrategy = org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL)
    private String seoOgDescription;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private Resource currentResource;

    @Reference
    private AIService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<ContentSection> contentSections;
    private boolean hasGeneratedContent;

    @PostConstruct
    protected void init() {
        contentSections = new ArrayList<>();
        if (aiEnabled && pageDescription != null && !pageDescription.isEmpty()) {
            if (pageTitle != null && !pageTitle.isEmpty() && !regenerate) {
                loadFromCache();
                return;
            }
            generatePageContent();
            saveToJcr();
        }
    }

    private void loadFromCache() {
        if (navigationStructure != null && !navigationStructure.isEmpty()) {
            parseNavigationStructure(navigationStructure);
        }
        hasGeneratedContent = pageTitle != null && !pageTitle.isEmpty();
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public String getPageDescription() {
        return pageDescription;
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getGeneratedPageDescription() {
        return generatedPageDescription;
    }

    public String getNavigationStructure() {
        return navigationStructure;
    }

    public String getSeoKeywords() {
        return seoKeywords;
    }

    public String getSeoOgTitle() {
        return seoOgTitle;
    }

    public String getSeoOgDescription() {
        return seoOgDescription;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public boolean hasGeneratedContent() {
        return hasGeneratedContent;
    }

    public List<ContentSection> getContentSections() {
        return contentSections;
    }

    public static class ContentSection {
        private String title;
        private String content;
        private String fragmentPath;

        public ContentSection(String title, String content, String fragmentPath) {
            this.title = title;
            this.content = content;
            this.fragmentPath = fragmentPath;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getFragmentPath() {
            return fragmentPath;
        }
    }

    private void generatePageContent() {
        String prompt = buildPageGenerationPrompt();
        AIGenerationOptions options = AIGenerationOptions.builder()
            .setMaxTokens(2000)
            .setTemperature(0.7f)
            .build();
        
        AIService.AIGenerationResult result = aiService.generateText(prompt, options);
        if (result.isSuccess()) {
            parsePageResponse(result.getContent());
        }
    }

    private String buildPageGenerationPrompt() {
        return "Generate a complete page structure for an AEM page based on this description: \"" + pageDescription + "\". " +
               "Provide the response as a JSON object with the following structure (no additional text): " +
               "{\"pageTitle\": \"string\", \"description\": \"string\", \"navigation\": [{\"label\": \"string\", \"path\": \"string\"}], " +
               "\"sections\": [{\"title\": \"string\", \"content\": \"string\"}], " +
               "\"seo\": {\"keywords\": \"string\", \"ogTitle\": \"string\", \"ogDescription\": \"string\"}}";
    }

    private void parsePageResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            
            pageTitle = getTextValue(rootNode, "pageTitle");
            generatedPageDescription = getTextValue(rootNode, "description");
            
            JsonNode navigation = rootNode.get("navigation");
            if (navigation != null && navigation.isArray()) {
                StringBuilder navBuilder = new StringBuilder();
                for (int i = 0; i < navigation.size(); i++) {
                    JsonNode navItem = navigation.get(i);
                    if (i > 0) navBuilder.append("|");
                    navBuilder.append(navItem.get("label").asText()).append(";")
                              .append(navItem.get("path").asText());
                }
                navigationStructure = navBuilder.toString();
            }

            JsonNode sections = rootNode.get("sections");
            if (sections != null && sections.isArray()) {
                for (int i = 0; i < sections.size(); i++) {
                    JsonNode section = sections.get(i);
                    String title = getTextValue(section, "title");
                    String content = getTextValue(section, "content");
                    contentSections.add(new ContentSection(title, content, null));
                }
            }

            JsonNode seo = rootNode.get("seo");
            if (seo != null) {
                seoKeywords = getTextValue(seo, "keywords");
                seoOgTitle = getTextValue(seo, "ogTitle");
                seoOgDescription = getTextValue(seo, "ogDescription");
            }

            hasGeneratedContent = true;
        } catch (Exception e) {
            hasGeneratedContent = false;
        }
    }

    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null ? fieldNode.asText() : "";
    }

    private void parseNavigationStructure(String navStructure) {
        if (navStructure == null || navStructure.isEmpty()) return;
        
        String[] items = navStructure.split("\\|");
        for (String item : items) {
            String[] parts = item.split(";");
            if (parts.length == 2) {
                contentSections.add(new ContentSection(parts[0], "", parts[1]));
            }
        }
    }

    private void saveToJcr() {
        if (currentResource == null || !hasGeneratedContent) return;
        
        try {
            Resource parent = currentResource.getParent();
            if (parent != null) {
                String childName = currentResource.getName() + "_generated";
                Map<String, Object> props = new HashMap<>();
                props.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
                props.put("pageTitle", pageTitle != null ? pageTitle : "");
                props.put("pageDescription", generatedPageDescription != null ? generatedPageDescription : "");
                props.put("navigationStructure", navigationStructure != null ? navigationStructure : "");
                props.put("seoKeywords", seoKeywords != null ? seoKeywords : "");
                props.put("seoOgTitle", seoOgTitle != null ? seoOgTitle : "");
                props.put("seoOgDescription", seoOgDescription != null ? seoOgDescription : "");
                
                Resource generatedResource = resourceResolver.create(parent, childName, props);
                
                for (int i = 0; i < contentSections.size(); i++) {
                    ContentSection section = contentSections.get(i);
                    Map<String, Object> sectionProps = new HashMap<>();
                    sectionProps.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
                    sectionProps.put("title", section.getTitle());
                    sectionProps.put("content", section.getContent());
                    
                    String sectionName = "section_" + i;
                    Resource sectionResource = resourceResolver.create(generatedResource, sectionName, sectionProps);
                    
                    section = new ContentSection(section.getTitle(), section.getContent(), sectionResource.getPath());
                    contentSections.set(i, section);
                }
                
                resourceResolver.commit();
            }
        } catch (Exception e) {
        }
    }
}