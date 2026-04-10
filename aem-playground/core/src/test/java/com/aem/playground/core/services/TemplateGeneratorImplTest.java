package com.aem.playground.core.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TemplateGeneratorImplTest {

    private TemplateGeneratorImpl templateGenerator;

    @Before
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

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGenerateTemplateWithContactType() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType("contact");
        request.setPageTitle("Contact Us");

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGenerateTemplateWithNullType() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType(null);
        request.setPageTitle("Test");

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().size() > 0);
    }

    @Test
    public void testGenerateContentSections() throws Exception {
        List<TemplateGenerator.TemplateSection> sections = 
            templateGenerator.generateContentSections("landing", "Test page");

        assertNotNull(sections);
        assertEquals(5, sections.size());
        
        TemplateGenerator.TemplateSection heroSection = sections.get(0);
        assertEquals("hero", heroSection.getSectionName());
        assertNotNull(heroSection.getComponentType());
        assertNotNull(heroSection.getContent());
    }

    @Test
    public void testGeneratePolicyMappingsForLanding() {
        Map<String, String> policies = templateGenerator.generatePolicyMappings("landing");

        assertNotNull(policies);
        assertTrue(policies.size() > 0);
        assertTrue(policies.containsKey("root"));
    }

    @Test
    public void testGeneratePolicyMappingsForBlog() {
        Map<String, String> policies = templateGenerator.generatePolicyMappings("blog");

        assertNotNull(policies);
        assertTrue(policies.size() > 0);
    }

    @Test
    public void testGeneratePolicyMappingsForProduct() {
        Map<String, String> policies = templateGenerator.generatePolicyMappings("product");

        assertNotNull(policies);
        assertTrue(policies.size() > 0);
    }

    @Test
    public void testGeneratePolicyMappingsForContact() {
        Map<String, String> policies = templateGenerator.generatePolicyMappings("contact");

        assertNotNull(policies);
        assertTrue(policies.size() > 0);
    }

    @Test
    public void testGeneratePolicyMappingsForUnknown() {
        Map<String, String> policies = templateGenerator.generatePolicyMappings("unknown");

        assertNotNull(policies);
        assertTrue(policies.isEmpty());
    }

    @Test
    public void testGeneratePreviewThumbnail() throws Exception {
        byte[] thumbnail = templateGenerator.generatePreviewThumbnail("landing");

        assertNotNull(thumbnail);
        String svgContent = new String(thumbnail);
        assertTrue(svgContent.contains("<?xml"));
        assertTrue(svgContent.contains("svg"));
        assertTrue(svgContent.contains("Landing Template"));
    }

    @Test
    public void testGeneratePreviewThumbnailForAllTypes() throws Exception {
        String[] types = {"landing", "blog", "product", "contact"};
        
        for (String type : types) {
            byte[] thumbnail = templateGenerator.generatePreviewThumbnail(type);
            assertNotNull("Thumbnail should not be null for type: " + type, thumbnail);
        }
    }

    @Test
    public void testSectionProperties() throws Exception {
        List<TemplateGenerator.TemplateSection> sections = 
            templateGenerator.generateContentSections("landing", null);

        for (TemplateGenerator.TemplateSection section : sections) {
            assertNotNull(section.getProperties());
        }
    }

    @Test
    public void testSectionWithNullDescription() throws Exception {
        TemplateGenerator.TemplateRequest request = new TemplateGenerator.TemplateRequest();
        request.setTemplateType("landing");
        request.setPageTitle("Test");
        request.setPageDescription(null);

        TemplateGenerator.TemplateResult result = templateGenerator.generateTemplate(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}