package com.aem.playground.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aem.playground.core.services.dto.ComponentDescriptor;

public class ComponentBuilderServiceTest {

    private ComponentBuilderService service;

    @BeforeEach
    void setUp() {
        service = new com.aem.playground.core.services.impl.ComponentBuilderServiceImpl();
    }

    @Test
    void testBuildComponentBasic() {
        ComponentDescriptor descriptor = service.buildComponent("TestComponent", "title and text", false, false);
        
        assertNotNull(descriptor);
        assertEquals("TestComponent", descriptor.getComponentName());
        assertNotNull(descriptor.getSlingModel());
        assertNotNull(descriptor.getHtlTemplate());
        assertNotNull(descriptor.getDialogXml());
        assertNotNull(descriptor.getContentXml());
        assertNotNull(descriptor.getCss());
    }

    @Test
    void testBuildComponentWithFields() {
        ComponentDescriptor descriptor = service.buildComponent("HeroBanner", "title subtitle image", false, false);
        
        assertNotNull(descriptor);
        assertTrue(descriptor.getFields().contains("title"));
    }

    @Test
    void testBuildComponentResponsive() {
        ComponentDescriptor descriptor = service.buildComponent("Card", "title text", true, false);
        
        assertTrue(descriptor.isResponsive());
        assertTrue(descriptor.getCss().contains("responsive"));
    }

    @Test
    void testBuildComponentWithCrud() {
        ComponentDescriptor descriptor = service.buildComponent("DataEntry", "title text", false, true);
        
        assertTrue(descriptor.isIncludeCrud());
        assertTrue(descriptor.getSlingModel().contains("public void create"));
        assertTrue(descriptor.getSlingModel().contains("public void update"));
        assertTrue(descriptor.getSlingModel().contains("public void delete"));
    }

    @Test
    void testGenerateFieldsDefault() {
        List<String> fields = service.generateFields("generic component");
        
        assertNotNull(fields);
        assertTrue(fields.contains("title"));
        assertTrue(fields.contains("text"));
    }

    @Test
    void testGenerateFieldsWithImage() {
        List<String> fields = service.generateFields("image gallery with title");
        
        assertNotNull(fields);
        assertTrue(fields.contains("title"));
        assertTrue(fields.contains("image"));
    }

    @Test
    void testGenerateDialogXml() {
        List<String> fields = List.of("title", "text");
        String xml = service.generateDialogXml(fields);
        
        assertNotNull(xml);
        assertTrue(xml.contains("jcr:root"));
        assertTrue(xml.contains("granite/ui/components/coral/foundation"));
        assertTrue(xml.contains("title"));
        assertTrue(xml.contains("text"));
    }

    @Test
    void testGenerateContentXml() {
        String xml = service.generateContentXml("TestComponent", "Test Component", "Test Group");
        
        assertNotNull(xml);
        assertTrue(xml.contains("jcr:primaryType=\"cq:Component\""));
        assertTrue(xml.contains("jcr:title=\"Test Component\""));
        assertTrue(xml.contains("componentGroup=\"Test Group\""));
    }

    @Test
    void testGenerateHtTemplate() {
        List<String> fields = List.of("title", "text");
        String html = service.generateHtTemplate("Hero", fields, false);
        
        assertNotNull(html);
        assertTrue(html.contains("cmp-hero"));
        assertTrue(html.contains("data-sly-test"));
    }

    @Test
    void testGenerateHtTemplateResponsive() {
        List<String> fields = List.of("title");
        String html = service.generateHtTemplate("Card", fields, true);
        
        assertTrue(html.contains("cmp-card--responsive"));
    }

    @Test
    void testGenerateSlingModel() {
        List<String> fields = List.of("title", "text");
        String java = service.generateSlingModel("Hero", fields, false);
        
        assertNotNull(java);
        assertTrue(java.contains("@Model(adaptables = Resource.class)"));
        assertTrue(java.contains("public class HeroModel"));
        assertTrue(java.contains("@ValueMapValue"));
        assertTrue(java.contains("getTitle()"));
        assertTrue(java.contains("getText()"));
        assertTrue(java.contains("getProcessedText()"));
    }

    @Test
    void testGenerateSlingModelWithCrud() {
        List<String> fields = List.of("title");
        String java = service.generateSlingModel("DataItem", fields, true);
        
        assertTrue(java.contains("create(Resource"));
        assertTrue(java.contains("read()"));
        assertTrue(java.contains("update()"));
        assertTrue(java.contains("delete()"));
    }

    @Test
    void testGenerateCss() {
        String css = service.generateCss("Card", false);
        
        assertNotNull(css);
        assertTrue(css.contains(".cmp-card"));
        assertTrue(css.contains("__title"));
        assertTrue(css.contains("__text"));
    }

    @Test
    void testGenerateCssResponsive() {
        String css = service.generateCss("Hero", true);
        
        assertTrue(css.contains("@media"));
        assertTrue(css.contains("max-width"));
    }
}