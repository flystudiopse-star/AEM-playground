package com.aem.playground.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aem.playground.core.services.SharePointPageImporter.AemComponent;
import com.aem.playground.core.services.SharePointPageImporter.ImportResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SharePointPageImporterTest {

    private SharePointPageImporter fixture;

    @BeforeEach
    void setup() {
        fixture = new SharePointPageImporter();
    }

    @Test
    void testGetTargetPathBase() {
        assertEquals("/content/aem-playground", fixture.getTargetPathBase());
    }

    @Test
    void testParseHtmlToComponentsWithSimpleText() {
        String html = "<p>Simple paragraph text</p>";
        
        List<AemComponent> components = fixture.parseHtmlToComponents(html);
        
        assertNotNull(components);
        assertFalse(components.isEmpty());
    }

    @Test
    void testParseHtmlToComponentsWithHeadings() {
        String html = "<h1>Main Heading</h1><h2>Sub Heading</h2>";
        
        List<AemComponent> components = fixture.parseHtmlToComponents(html);
        
        assertNotNull(components);
        assertTrue(components.size() >= 2);
        assertEquals("aem-playground/components/title", components.get(0).getResourceType());
    }

    @Test
    void testParseHtmlToComponentsWithLinks() {
        String html = "<p>Check out <a href=\"https://example.com\">this link</a> for more info.</p>";
        
        List<AemComponent> components = fixture.parseHtmlToComponents(html);
        
        assertNotNull(components);
        assertTrue(components.size() >= 2);
    }

    @Test
    void testParseHtmlToComponentsWithEmptyHtml() {
        String html = "";
        
        List<AemComponent> components = fixture.parseHtmlToComponents(html);
        
        assertNotNull(components);
        assertTrue(components.isEmpty());
    }

    @Test
    void testParseHtmlToComponentsWithNullHtml() {
        String html = null;
        
        List<AemComponent> components = fixture.parseHtmlToComponents(html);
        
        assertNotNull(components);
        assertTrue(components.isEmpty());
    }

    @Test
    void testParseHtmlToComponentsWithMixedContent() {
        String html = "<h1>Title</h1><p>First paragraph with <a href=\"http://test.com\">link</a> inside.</p><p>Second paragraph.</p>";
        
        List<AemComponent> components = fixture.parseHtmlToComponents(html);
        
        assertNotNull(components);
        assertTrue(components.size() >= 3);
    }

    @Test
    void testParseHtmlToComponentsCreatesTextComponentForPlainContent() {
        String html = "<p>Just plain text</p>";
        
        List<AemComponent> components = fixture.parseHtmlToComponents(html);
        
        assertTrue(components.stream().anyMatch(c -> 
            "aem-playground/components/text".equals(c.getResourceType())));
    }

    @Test
    void testAemComponentProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("text", "Hello");
        AemComponent component = new AemComponent("test/resource/type", props);
        
        assertEquals("test/resource/type", component.getResourceType());
        assertEquals("Hello", component.getProperties().get("text"));
    }

    @Test
    void testImportResultDefaultValues() {
        ImportResult result = new ImportResult();
        
        assertFalse(result.isSuccess());
        assertNull(result.getTargetPath());
        assertEquals(0, result.getComponentCount());
        assertNull(result.getErrorMessage());
        assertFalse(result.isExistingPage());
    }

    @Test
    void testImportResultSetters() {
        ImportResult result = new ImportResult();
        result.setSuccess(true);
        result.setTargetPath("/content/aem-playground/test-page");
        result.setComponentCount(5);
        result.setErrorMessage("Error message");
        result.setExistingPage(true);
        
        assertTrue(result.isSuccess());
        assertEquals("/content/aem-playground/test-page", result.getTargetPath());
        assertEquals(5, result.getComponentCount());
        assertEquals("Error message", result.getErrorMessage());
        assertTrue(result.isExistingPage());
    }
}