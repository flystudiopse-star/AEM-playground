package com.aem.playground.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TemplateGeneratorImplTest {

    private TemplateGeneratorImpl templateGenerator;

    @BeforeEach
    public void setUp() {
        templateGenerator = new TemplateGeneratorImpl();
    }

    @Test
    public void testGenerateTemplateWithLandingType() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType("landing");
        request.setPageTitle("Home Page");
        request.setPageDescription("A landing page for our business");
        request.setGenerateAiContent(false);

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("landing", result.getTemplateName().split("-")[0]);
        assertNotNull(result.getTemplatePath());
        assertNotNull(result.getStructure());
        assertNotNull(result.getSections());
        assertEquals(5, result.getSections().size());
    }

    @Test
    public void testGenerateTemplateWithBlogType() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType("blog");
        request.setPageTitle("My Blog");
        request.setGenerateAiContent(false);

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getSections());
        assertEquals(5, result.getSections().size());
    }

    @Test
    public void testGenerateTemplateWithProductType() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType("product");
        request.setPageTitle("Product Page");
        request.setPageDescription("A product page");
        request.setGenerateAiContent(false);

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getSections());
        assertEquals(5, result.getSections().size());
    }

    @Test
    public void testGenerateTemplateWithContactType() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType("contact");
        request.setPageTitle("Contact Page");
        request.setGenerateAiContent(false);

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getSections());
        assertEquals(4, result.getSections().size());
    }

    @Test
    public void testGenerateContentSections() throws Exception {
        List<TemplateGenerator.TemplateSection> sections =
            templateGenerator.generateContentSections("landing", "Test description");

        assertNotNull(sections);
        assertFalse(sections.isEmpty());
    }

    @Test
    public void testGeneratePolicyMappings() {
        var mappings = templateGenerator.generatePolicyMappings("contact");

        assertNotNull(mappings);
        assertTrue(mappings.containsKey("root"));
    }

    @Test
    public void testGeneratePolicyMappingsEmpty() {
        var mappings = templateGenerator.generatePolicyMappings("unknown");

        assertNotNull(mappings);
        assertTrue(mappings.isEmpty());
    }

    @Test
    public void testGenerateTemplateReturnsValidTemplateName() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType("landing");
        request.setPageTitle("Test Page");
        request.setGenerateAiContent(false);

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result.getTemplateName());
        assertTrue(result.getTemplateName().startsWith("landing-"));
    }
}
