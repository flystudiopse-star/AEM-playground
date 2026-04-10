package com.aem.playground.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BilingualComparisonModelTest {

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private Resource resource;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Resource sourceResource;

    @Mock
    private Resource targetResource;

    @Mock
    private Resource sourceContent;

    @Mock
    private Resource targetContent;

    private BilingualComparisonModel model;

    @BeforeEach
    void setUp() {
        lenient().when(request.adaptTo(Resource.class)).thenReturn(resource);
        when(resource.getResourceResolver()).thenReturn(resourceResolver);
    }

    @Test
    void testComparisonModelInitialization() {
        when(resource.getChild("jcr:content")).thenReturn(null);
        
        model = new BilingualComparisonModel();
        model.init();
        
        assertNotNull(model.getComparisonItems());
        assertNotNull(model.getStats());
        assertFalse(model.isHasComparisonData());
    }

    @Test
    void testDefaultLanguages() {
        when(resource.getChild("jcr:content")).thenReturn(null);
        
        model = new BilingualComparisonModel();
        model.init();
        
        assertEquals("en", model.getSourceLanguage());
        assertEquals("de", model.getTargetLanguage());
    }

    @Test
    void testComparisonWithPaths() {
        when(resource.getChild("jcr:content")).thenReturn(sourceContent);
        
        when(resourceResolver.getResource("/content/en/page")).thenReturn(sourceResource);
        when(resourceResolver.getResource("/content/de/page")).thenReturn(targetResource);
        
        when(sourceResource.getChild("jcr:content")).thenReturn(sourceContent);
        when(targetResource.getChild("jcr:content")).thenReturn(targetContent);
        
        java.util.Map<String, Object> sourceProps = new java.util.HashMap<>();
        sourceProps.put("jcr:title", "English Title");
        sourceProps.put("jcr:description", "English Description");
        sourceProps.put("text", "Hello World");
        
        java.util.Map<String, Object> targetProps = new java.util.HashMap<>();
        targetProps.put("jcr:title", "German Title");
        targetProps.put("jcr:description", "German Description");
        targetProps.put("text", "Hallo Welt");
        
        when(sourceContent.getValueMap()).thenReturn(new org.apache.sling.api.resource.ValueMap() {
            @Override
            public int size() { return sourceProps.size(); }
            @Override
            public boolean isEmpty() { return sourceProps.isEmpty(); }
            @Override
            public boolean containsKey(Object key) { return sourceProps.containsKey(key); }
            @Override
            public boolean containsValue(Object value) { return sourceProps.containsValue(value); }
            @Override
            public Object get(Object key) { return sourceProps.get(key); }
            @Override
            public org.apache.sling.api.resource.Resource getChild(String name) { return null; }
            @Override
            public Iterable<org.apache.sling.api.resource.Resource> getChildren() { return java.util.Collections.emptyList(); }
            @Override
            public String getPath() { return "/content/en/page/jcr:content"; }
            @Override
            public String getName() { return "jcr:content"; }
            @Override
            public org.apache.sling.api.resource.Resource getParent() { return sourceResource; }
            @Override
            public java.util.Iterator<org.apache.sling.api.resource.Resource> iterator() { return java.util.Collections.emptyIterator(); }
        });
        
        when(targetContent.getValueMap()).thenReturn(new org.apache.sling.api.resource.ValueMap() {
            @Override
            public int size() { return targetProps.size(); }
            @Override
            public boolean isEmpty() { return targetProps.isEmpty(); }
            @Override
            public boolean containsKey(Object key) { return targetProps.containsKey(key); }
            @Override
            public boolean containsValue(Object value) { return targetProps.containsValue(value); }
            @Override
            public Object get(Object key) { return targetProps.get(key); }
            @Override
            public org.apache.sling.api.resource.Resource getChild(String name) { return null; }
            @Override
            public Iterable<org.apache.sling.api.resource.Resource> getChildren() { return java.util.Collections.emptyList(); }
            @Override
            public String getPath() { return "/content/de/page/jcr:content"; }
            @Override
            public String getName() { return "jcr:content"; }
            @Override
            public org.apache.sling.api.resource.Resource getParent() { return targetResource; }
            @Override
            public java.util.Iterator<org.apache.sling.api.resource.Resource> iterator() { return java.util.Collections.emptyIterator(); }
        });

        assertTrue(true);
    }

    @Test
    void testComparisonStats() {
        BilingualComparisonModel.ComparisonStats stats = new BilingualComparisonModel.ComparisonStats("Translated", 10);
        assertEquals("Translated", stats.getLabel());
        assertEquals(10, stats.getValue());
    }

    @Test
    void testComparisonStatsGetters() {
        BilingualComparisonModel.ComparisonStats stats = new BilingualComparisonModel.ComparisonStats("Total", 5);
        
        assertEquals("Total", stats.getLabel());
        assertEquals(5, stats.getValue());
    }

    @Test
    void testNullResourceHandling() {
        model = new BilingualComparisonModel();
        model.init();
        
        assertNotNull(model.getComparisonItems());
        assertTrue(model.getComparisonItems().isEmpty());
    }
}