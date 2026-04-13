package com.aem.playground.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.aem.playground.core.services.ComponentBuilderService;
import com.aem.playground.core.services.dto.ComponentDescriptor;

@Component(service = ComponentBuilderService.class)
@Designate(ocd = ComponentBuilderServiceImpl.Config.class)
public class ComponentBuilderServiceImpl implements ComponentBuilderService {

    @ObjectClassDefinition(name = "Component Builder Configuration",
            description = "Configuration for AI-powered Component Builder")
    public @interface Config {

        @AttributeDefinition(name = "Default Component Group", description = "Default group for generated components")
        String default_component_group() default "AEM Playground - Content";

        @AttributeDefinition(name = "Enable Responsive by Default", description = "Enable responsive styles by default")
        boolean enable_responsive_default() default false;

        @AttributeDefinition(name = "Enable CRUD by Default", description = "Enable CRUD scaffolding by default")
        boolean enable_crud_default() default false;
    }

    @Override
    public ComponentDescriptor buildComponent(String componentName, String description, boolean responsive, boolean includeCrud) {
        ComponentDescriptor descriptor = new ComponentDescriptor();
        descriptor.setComponentName(componentName);
        descriptor.setDescription(description);
        descriptor.setResponsive(responsive);
        descriptor.setIncludeCrud(includeCrud);

        List<String> fields = generateFields(description);
        descriptor.setFields(fields);

        descriptor.setSlingModel(generateSlingModel(componentName, fields, includeCrud));
        descriptor.setHtlTemplate(generateHtTemplate(componentName, fields, responsive));
        descriptor.setDialogXml(generateDialogXml(fields));
        descriptor.setContentXml(generateContentXml(componentName, formatComponentTitle(componentName), "AEM Playground - Content"));
        descriptor.setCss(generateCss(componentName, responsive));

        return descriptor;
    }

    @Override
    public List<String> generateFields(String description) {
        List<String> fields = new ArrayList<>();
        
        description = description.toLowerCase();
        
        if (description.contains("title") || description.contains("heading")) {
            fields.add("title");
        }
        if (description.contains("text") || description.contains("content") || description.contains("description")) {
            fields.add("text");
        }
        if (description.contains("image") || description.contains("picture") || description.contains("photo")) {
            fields.add("image");
        }
        if (description.contains("link") || description.contains("url") || description.contains("href")) {
            fields.add("link");
        }
        if (description.contains("subtitle")) {
            fields.add("subtitle");
        }
        if (description.contains("author")) {
            fields.add("author");
        }
        if (description.contains("date")) {
            fields.add("date");
        }
        if (description.contains("summary") || description.contains("excerpt")) {
            fields.add("summary");
        }
        
        if (fields.isEmpty()) {
            fields.add("title");
            fields.add("text");
        }
        
        return fields;
    }

    @Override
    public String generateDialogXml(List<String> fields) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jcr:root xmlns:sling=\"http://sling.apache.org/jcr/sling/1.0\" ");
        xml.append("xmlns:cq=\"http://www.day.com/jcr/cq/1.0\" ");
        xml.append("xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" ");
        xml.append("xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"\n");
        xml.append("    jcr:primaryType=\"nt:unstructured\"\n");
        xml.append("    jcr:title=\"Properties\"\n");
        xml.append("    sling:resourceType=\"cq/gui/components/authoring/dialog\">\n");
        xml.append("    <content\n");
        xml.append("        jcr:primaryType=\"nt:unstructured\"\n");
        xml.append("        sling:resourceType=\"granite/ui/components/coral/foundation/fixedcolumns\">\n");
        xml.append("        <items jcr:primaryType=\"nt:unstructured\">\n");
        xml.append("            <column\n");
        xml.append("                jcr:primaryType=\"nt:unstructured\"\n");
        xml.append("                sling:resourceType=\"granite/ui/components/coral/foundation/container\">\n");
        xml.append("                <items jcr:primaryType=\"nt:unstructured\">\n");
        
        for (String field : fields) {
            xml.append(generateFieldXml(field));
        }
        
        xml.append("                </items>\n");
        xml.append("            </column>\n");
        xml.append("        </items>\n");
        xml.append("    </content>\n");
        xml.append("</jcr:root>\n");
        
        return xml.toString();
    }

    private String generateFieldXml(String fieldName) {
        StringBuilder xml = new StringBuilder();
        
        switch (fieldName) {
            case "title":
            case "subtitle":
                xml.append("                    <").append(fieldName).append("\n");
                xml.append("                        jcr:primaryType=\"nt:unstructured\"\n");
                xml.append("                        sling:resourceType=\"granite/ui/components/coral/foundation/form/textfield\"\n");
                xml.append("                        fieldLabel=\"").append(capitalize(fieldName)).append("\"\n");
                xml.append("                        name=\"./").append(fieldName).append("\"/>\n");
                break;
            case "text":
            case "summary":
                xml.append("                    <").append(fieldName).append("\n");
                xml.append("                        jcr:primaryType=\"nt:unstructured\"\n");
                xml.append("                        sling:resourceType=\"granite/ui/components/coral/foundation/form/textarea\"\n");
                xml.append("                        fieldLabel=\"").append(capitalize(fieldName)).append("\"\n");
                xml.append("                        name=\"./").append(fieldName).append("\"/>\n");
                break;
            case "image":
                xml.append("                    <image\n");
                xml.append("                        jcr:primaryType=\"nt:unstructured\"\n");
                xml.append("                        sling:resourceType=\"granite/ui/components/coral/foundation/form/imagepath\"\n");
                xml.append("                        fieldLabel=\"Image\"\n");
                xml.append("                        name=\"./image\"/>\n");
                break;
            case "link":
                xml.append("                    <link\n");
                xml.append("                        jcr:primaryType=\"nt:unstructured\"\n");
                xml.append("                        sling:resourceType=\"granite/ui/components/coral/foundation/form/pathbrowser\"\n");
                xml.append("                        fieldLabel=\"Link\"\n");
                xml.append("                        name=\"./link\"/>\n");
                break;
            case "author":
                xml.append("                    <author\n");
                xml.append("                        jcr:primaryType=\"nt:unstructured\"\n");
                xml.append("                        sling:resourceType=\"granite/ui/components/coral/foundation/form/textfield\"\n");
                xml.append("                        fieldLabel=\"Author\"\n");
                xml.append("                        name=\"./author\"/>\n");
                break;
            case "date":
                xml.append("                    <date\n");
                xml.append("                        jcr:primaryType=\"nt:unstructured\"\n");
                xml.append("                        sling:resourceType=\"granite/ui/components/coral/foundation/form/datepicker\"\n");
                xml.append("                        fieldLabel=\"Date\"\n");
                xml.append("                        name=\"./date\"\n");
                xml.append("                        type=\"datetime\"/>\n");
                break;
            default:
                xml.append("                    <").append(fieldName).append("\n");
                xml.append("                        jcr:primaryType=\"nt:unstructured\"\n");
                xml.append("                        sling:resourceType=\"granite/ui/components/coral/foundation/form/textfield\"\n");
                xml.append("                        fieldLabel=\"").append(capitalize(fieldName)).append("\"\n");
                xml.append("                        name=\"./").append(fieldName).append("\"/>\n");
                break;
        }
        
        return xml.toString();
    }

    @Override
    public String generateContentXml(String componentName, String title, String group) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jcr:root xmlns:cq=\"http://www.day.com/jcr/cq/1.0\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"\n");
        xml.append("    jcr:primaryType=\"cq:Component\"\n");
        xml.append("    jcr:title=\"").append(title).append("\"\n");
        xml.append("    componentGroup=\"").append(group).append("\"/>\n");
        
        return xml.toString();
    }

    @Override
    public String generateHtTemplate(String componentName, List<String> fields, boolean responsive) {
        StringBuilder html = new StringBuilder();
        String cssClass = toCssClass(componentName);
        
        html.append("<!--*/\n");
        html.append("    Copyright 2024 AEM Playground\n");
        html.append("    Component: ").append(componentName).append("\n");
        html.append("*/-->\n");
        html.append("<div class=\"cmp-").append(cssClass);
        if (responsive) {
            html.append(" cmp-").append(cssClass).append("--responsive");
        }
        html.append("\" data-cmp-is=\"").append(componentName).append("\">\n");
        
        for (String field : fields) {
            html.append(generateFieldHtml(field, cssClass));
        }
        
        html.append("</div>\n");
        
        return html.toString();
    }

    private String generateFieldHtml(String fieldName, String cssClass) {
        StringBuilder html = new StringBuilder();
        
        switch (fieldName) {
            case "title":
                html.append("    <h2 class=\"cmp-").append(cssClass).append("__title\" ");
                html.append("data-sly-test=\"${properties.title}\">${properties.title}</h2>\n");
                break;
            case "subtitle":
                html.append("    <h3 class=\"cmp-").append(cssClass).append("__subtitle\" ");
                html.append("data-sly-test=\"${properties.subtitle}\">${properties.subtitle}</h3>\n");
                break;
            case "text":
            case "summary":
                html.append("    <div class=\"cmp-").append(cssClass).append("__").append(fieldName);
                html.append("\" data-sly-test=\"${properties.").append(fieldName).append("}\">\n");
                html.append("        <p data-sly-use.model=\"com.aem.playground.core.models.").append(capitalize(toJavaClassName(fieldName))).append("Model\">${model.processedText}</p>\n");
                html.append("    </div>\n");
                break;
            case "image":
                html.append("    <div class=\"cmp-").append(cssClass).append("__image\" ");
                html.append("data-sly-test=\"${properties.image}\">\n");
                html.append("        <img src=\"${properties.image.path}\" alt=\"${properties.title}\" />\n");
                html.append("    </div>\n");
                break;
            case "link":
                html.append("    <a class=\"cmp-").append(cssClass).append("__link\" ");
                html.append("data-sly-test=\"${properties.link}\" href=\"${properties.link}\">Learn More</a>\n");
                break;
            case "author":
                html.append("    <span class=\"cmp-").append(cssClass).append("__author\" ");
                html.append("data-sly-test=\"${properties.author}\">By ${properties.author}</span>\n");
                break;
            case "date":
                html.append("    <time class=\"cmp-").append(cssClass).append("__date\" ");
                html.append("data-sly-test=\"${properties.date}\" datetime=\"${properties.date}\">${properties.date}</time>\n");
                break;
            default:
                html.append("    <div class=\"cmp-").append(cssClass).append("__").append(fieldName);
                html.append("\" data-sly-test=\"${properties.").append(fieldName).append("}\">\n");
                html.append("        ${properties.").append(fieldName).append("}\n");
                html.append("    </div>\n");
                break;
        }
        
        return html.toString();
    }

    @Override
    public String generateSlingModel(String componentName, List<String> fields, boolean includeCrud) {
        StringBuilder java = new StringBuilder();
        String className = capitalize(componentName) + "Model";
        
        java.append("package com.aem.playground.core.models;\n\n");
        java.append("import java.util.Optional;\n\n");
        java.append("import javax.annotation.PostConstruct;\n\n");
        java.append("import org.apache.sling.api.resource.Resource;\n");
        java.append("import org.apache.sling.models.annotations.Model;\n");
        java.append("import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;\n");
        java.append("import org.apache.sling.models.annotations.injectorspecific.SlingObject;\n");
        java.append("import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;\n\n");
        java.append("import com.day.cq.wcm.api.Page;\n");
        java.append("import com.day.cq.wcm.api.PageManager;\n\n");
        java.append("@Model(adaptables = Resource.class)\n");
        java.append("public class ").append(className).append(" {\n\n");
        
        java.append("    @SlingObject\n");
        java.append("    private Resource currentResource;\n\n");
        
        for (String field : fields) {
            java.append("    @ValueMapValue(name=\"./").append(field);
            java.append("\", injectionStrategy=InjectionStrategy.OPTIONAL)\n");
            java.append("    private String ").append(field).append(";\n\n");
        }
        
        java.append("    private String processedText;\n");
        java.append("    private boolean isEmpty;\n\n");
        
        java.append("    @PostConstruct\n");
        java.append("    protected void init() {\n");
        java.append("        isEmpty = ");
        
        if (fields.isEmpty()) {
            java.append("true;\n");
        } else {
            java.append(fields.stream()
                .map(f -> f + " == null")
                .collect(Collectors.joining(" && ")));
            java.append(";\n");
        }
        
        java.append("        processedText = processText(text);\n");
        java.append("    }\n\n");
        
        java.append("    private String processText(String text) {\n");
        java.append("        if (text == null || text.isEmpty()) {\n");
        java.append("            return \"\";\n");
        java.append("        }\n");
        java.append("        return text.replaceAll(\"\\\\n\", \"<br/>\");\n");
        java.append("    }\n\n");
        
        if (includeCrud) {
            java.append(generateCrudMethods(className, fields));
        }
        
        for (String field : fields) {
            java.append("    public String get").append(capitalize(field)).append("() {\n");
            java.append("        return ").append(field).append(";\n");
            java.append("    }\n\n");
            
            if (includeCrud) {
                java.append("    public void set").append(capitalize(field)).append("(String ").append(field).append(") {\n");
                java.append("        this.").append(field).append(" = ").append(field).append(";\n");
                java.append("    }\n\n");
            }
        }
        
        java.append("    public String getProcessedText() {\n");
        java.append("        return processedText;\n");
        java.append("    }\n\n");
        
        java.append("    public boolean isEmpty() {\n");
        java.append("        return isEmpty;\n");
        java.append("    }\n\n");
        
        java.append("    public String getResourceType() {\n");
        java.append("        return Optional.ofNullable(currentResource)\n");
        java.append("            .map(Resource::getResourceType)\n");
        java.append("            .orElse(\"\");\n");
        java.append("    }\n\n");
        
        java.append("}\n");
        
        return java.toString();
    }

    private String generateCrudMethods(String className, List<String> fields) {
        StringBuilder methods = new StringBuilder();
        
        methods.append("    public void create(Resource resource) {\n");
        methods.append("        // Implementation for create operation\n");
        methods.append("    }\n\n");
        
        methods.append("    public void read() {\n");
        methods.append("        // Implementation for read operation\n");
        methods.append("    }\n\n");
        
        methods.append("    public void update() {\n");
        methods.append("        // Implementation for update operation\n");
        methods.append("    }\n\n");
        
        methods.append("    public void delete() {\n");
        methods.append("        // Implementation for delete operation\n");
        methods.append("    }\n\n");
        
        return methods.toString();
    }

    @Override
    public String generateCss(String componentName, boolean responsive) {
        StringBuilder css = new StringBuilder();
        String cssClass = toCssClass(componentName);
        
        css.append("/* ").append(capitalize(componentName)).append(" Component Styles */\n\n");
        
        css.append(".cmp-").append(cssClass).append(" {\n");
        css.append("    /* Base styles */\n");
        css.append("    display: block;\n");
        css.append("    margin: 0;\n");
        css.append("    padding: 0;\n");
        css.append("}\n\n");
        
        css.append(".cmp-").append(cssClass).append("__title {\n");
        css.append("    font-size: 1.5rem;\n");
        css.append("    font-weight: bold;\n");
        css.append("    margin-bottom: 1rem;\n");
        css.append("}\n\n");
        
        css.append(".cmp-").append(cssClass).append("__subtitle {\n");
        css.append("    font-size: 1.25rem;\n");
        css.append("    font-weight: 600;\n");
        css.append("    margin-bottom: 0.75rem;\n");
        css.append("}\n\n");
        
        css.append(".cmp-").append(cssClass).append("__text,\n");
        css.append(".cmp-").append(cssClass).append("__summary {\n");
        css.append("    line-height: 1.6;\n");
        css.append("    margin-bottom: 1rem;\n");
        css.append("}\n\n");
        
        css.append(".cmp-").append(cssClass).append("__image img {\n");
        css.append("    max-width: 100%;\n");
        css.append("    height: auto;\n");
        css.append("    display: block;\n");
        css.append("}\n\n");
        
        css.append(".cmp-").append(cssClass).append("__link {\n");
        css.append("    color: #0069ba;\n");
        css.append("    text-decoration: none;\n");
        css.append("}\n\n");
        
        css.append(".cmp-").append(cssClass).append("__link:hover {\n");
        css.append("    text-decoration: underline;\n");
        css.append("}\n\n");
        
        css.append(".cmp-").append(cssClass).append("__author,\n");
        css.append(".cmp-").append(cssClass).append("__date {\n");
        css.append("    font-size: 0.875rem;\n");
        css.append("    color: #666;\n");
        css.append("}\n\n");
        
        if (responsive) {
            css.append("/* Responsive Styles */\n");
            css.append(".cmp-").append(cssClass).append("--responsive {\n");
            css.append("    width: 100%;\n");
            css.append("}\n\n");
            css.append("@media (min-width: 768px) {\n");
            css.append("    .cmp-").append(cssClass).append("--responsive {\n");
            css.append("        max-width: 750px;\n");
            css.append("        margin: 0 auto;\n");
            css.append("    }\n");
            css.append("}\n\n");
            css.append("@media (min-width: 992px) {\n");
            css.append("    .cmp-").append(cssClass).append("--responsive {\n");
            css.append("        max-width: 970px;\n");
            css.append("    }\n");
            css.append("}\n\n");
            css.append("@media (min-width: 1200px) {\n");
            css.append("    .cmp-").append(cssClass).append("--responsive {\n");
            css.append("        max-width: 1170px;\n");
            css.append("    }\n");
            css.append("}\n");
        }
        
        return css.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String toCssClass(String componentName) {
        if (componentName == null || componentName.isEmpty()) {
            return "";
        }
        return componentName.replaceAll("([A-Z])", "-$1").toLowerCase();
    }

    private String toJavaClassName(String name) {
        return capitalize(name);
    }

    private String formatComponentTitle(String componentName) {
        StringBuilder title = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : componentName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (title.length() > 0) {
                    title.append(" ");
                }
                capitalizeNext = true;
            }
            if (capitalizeNext) {
                title.append(c);
                capitalizeNext = false;
            } else {
                title.append(c);
            }
        }
        
        return title.toString();
    }
}