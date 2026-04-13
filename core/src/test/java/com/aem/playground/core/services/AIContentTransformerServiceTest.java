package com.aem.playground.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AIContentTransformerServiceTest {

    private AIContentTransformerService fixture;

    @BeforeEach
    void setup() {
        fixture = new AIContentTransformerService();
    }

    @Test
    void testActivateWithDefaultConfig() {
        AIContentTransformerService.Config config = mock(AIContentTransformerService.Config.class);
        when(config.ai_service_url()).thenReturn("https://api.openai.com/v1/chat/completions");
        when(config.api_key()).thenReturn("test-api-key");
        when(config.vision_model()).thenReturn("gpt-4o");
        when(config.text_model()).thenReturn("gpt-4");
        when(config.max_tokens()).thenReturn(2000);
        when(config.enable_cleanup()).thenReturn(true);
        when(config.enable_metadata()).thenReturn(true);
        when(config.enable_component_mapping()).thenReturn(true);
        when(config.enable_redirects()).thenReturn(true);

        fixture.activate(config);

        assertTrue(fixture.isEnableCleanup());
        assertTrue(fixture.isEnableMetadata());
        assertTrue(fixture.isEnableComponentMapping());
        assertTrue(fixture.isEnableRedirects());
    }

    @Test
    void testGenerateDefaultAltText() {
        String altText = fixture.generateImageAltText(new byte[0], "test-image.jpg");
        assertEquals("test image", altText);
    }

    @Test
    void testGenerateDefaultAltTextWithUnderscores() {
        String altText = fixture.generateImageAltText(new byte[0], "my_test_image.png");
        assertEquals("my test image", altText);
    }

    @Test
    void testGenerateDefaultAltTextWithEmptyName() {
        String altText = fixture.generateImageAltText(new byte[0], "");
        assertEquals("Image", altText);
    }

    @Test
    void testGenerateDefaultAltTextWithNullName() {
        String altText = fixture.generateImageAltText(new byte[0], null);
        assertEquals("Image", altText);
    }

    @Test
    void testGenerateMetadataWithoutApiKey() {
        Map<String, String> metadata = fixture.generateMetadata("<h1>Test</h1><p>Content here</p>", "Test Page");
        
        assertNotNull(metadata);
        assertTrue(metadata.containsKey("description"));
        assertTrue(metadata.containsKey("keywords"));
    }

    @Test
    void testSuggestComponentMappingsReturnsDefaults() {
        List<ContentTransformer.ComponentMapping> mappings = fixture.suggestComponentMappings("<h1>Title</h1><img src=\"test.jpg\"/>");
        
        assertNotNull(mappings);
        assertFalse(mappings.isEmpty());
    }

    @Test
    void testCleanupAndOptimizeBasic() {
        String input = "<p>  </p><p>Content</p><span></span>";
        
        ContentTransformer.CleanupOptions options = new ContentTransformer.CleanupOptions() {
            @Override
            public boolean isRemoveEmptyParagraphs() { return true; }
            @Override
            public boolean isFixEncoding() { return true; }
            @Override
            public boolean isNormalizeWhitespace() { return true; }
            @Override
            public boolean isRemoveInvalidTags() { return true; }
        };
        
        String result = fixture.cleanupAndOptimize(input, options);
        
        assertNotNull(result);
        assertTrue(result.contains("Content"));
    }

    @Test
    void testCleanupAndOptimizeWithNullInput() {
        ContentTransformer.CleanupOptions options = new ContentTransformer.CleanupOptions() {
            @Override
            public boolean isRemoveEmptyParagraphs() { return true; }
            @Override
            public boolean isFixEncoding() { return true; }
            @Override
            public boolean isNormalizeWhitespace() { return true; }
            @Override
            public boolean isRemoveInvalidTags() { return true; }
        };
        
        String result = fixture.cleanupAndOptimize(null, options);
        
        assertNull(result);
    }

    @Test
    void testGenerateRedirectMappingsBasic() {
        List<String> sourceUrls = new java.util.ArrayList<>();
        sourceUrls.add("/sites/test/SitePages/about.aspx");
        sourceUrls.add("/sites/test/SitePages/contact.aspx");
        
        List<ContentTransformer.RedirectMapping> redirects = fixture.generateRedirectMappings(sourceUrls, "/content/aem-playground");
        
        assertNotNull(redirects);
        assertEquals(2, redirects.size());
    }

    @Test
    void testGenerateRedirectMappingsWithNullInput() {
        List<ContentTransformer.RedirectMapping> redirects = fixture.generateRedirectMappings(null, "/content/aem-playground");
        
        assertNotNull(redirects);
        assertTrue(redirects.isEmpty());
    }

    @Test
    void testTransformContentReturnsOriginalWhenNoApiKey() {
        String htmlContent = "<h1>Test Content</h1>";
        
        ContentTransformer.TransformOptions options = new ContentTransformer.TransformOptions() {
            @Override
            public String getTargetFormat() { return "AEM"; }
            @Override
            public boolean isPreserveImages() { return true; }
            @Override
            public boolean isCleanupHtml() { return true; }
            @Override
            public Map<String, String> getCustomMappings() { return new HashMap<>(); }
        };
        
        String result = fixture.transformContent(htmlContent, options);
        
        assertNotNull(result);
    }
}
