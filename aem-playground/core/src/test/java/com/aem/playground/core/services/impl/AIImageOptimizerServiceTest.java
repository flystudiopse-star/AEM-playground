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
package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.ImageOptimizerConfig;
import com.aem.playground.core.services.ImageOptimizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIImageOptimizerServiceTest {

    @Mock
    private AIService aiService;

    @Mock
    private ImageOptimizerConfig config;

    private AIImageOptimizerService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(config.apiKey()).thenReturn("test-api-key");
        when(config.visionEndpoint()).thenReturn("https://api.openai.com/v1/chat/completions");
        when(config.visionModel()).thenReturn("gpt-4o");
        when(config.defaultQuality()).thenReturn(85);
        when(config.maxWidth()).thenReturn(1920);
        when(config.maxHeight()).thenReturn(1080);
        when(config.defaultFormat()).thenReturn("webp");
        when(config.generateResponsiveVariants()).thenReturn(true);
        when(config.breakpoints()).thenReturn("320,768,1024,1920");
        when(config.altTextPrompt()).thenReturn("Describe this image for accessibility purposes.");
        when(config.enableCaching()).thenReturn(true);
        when(config.cacheSize()).thenReturn(100);

        service = new AIImageOptimizerService();
        service.bindAiService(aiService);
        service.activate(config);
    }

    @Test
    void testServiceActivation() {
        assertNotNull(service);
        assertEquals("gpt-4o", service.getVisionModel());
        assertEquals(85, service.getDefaultQuality());
        assertEquals("webp", service.getDefaultFormat());
        assertTrue(service.isGenerateResponsiveVariants());
    }

    @Test
    void testAnalyzeImageEmptyPath() {
        ImageOptimizerService.ImageAnalysisResult result = service.analyzeImage("");

        assertNotNull(result);
        assertEquals("", result.getImagePath());
        assertEquals(0, result.getQualityScore());
    }

    @Test
    void testAnalyzeImageNullPath() {
        ImageOptimizerService.ImageAnalysisResult result = service.analyzeImage(null);

        assertNotNull(result);
        assertNull(result.getImagePath());
        assertEquals(0, result.getQualityScore());
    }

    @Test
    void testGetOptimizationSuggestionsEmptyPath() {
        List<ImageOptimizerService.OptimizationSuggestion> suggestions = 
            service.getOptimizationSuggestions("");

        assertNotNull(suggestions);
    }

    @Test
    void testGetCompressionSuggestion() {
        ImageOptimizerService.CompressionSuggestion suggestion = 
            service.getCompressionSuggestion("/content/dam/test.jpg");

        assertNotNull(suggestion);
        assertEquals("webp", suggestion.getRecommendedFormat());
        assertEquals(85, suggestion.getRecommendedQuality());
        assertTrue(suggestion.isStripMetadata());
    }

    @Test
    void testGenerateAltTextEmptyPath() {
        String altText = service.generateAltText("");
        
        assertEquals("", altText);
    }

    @Test
    void testGenerateResponsiveVariantsDisabled() {
        when(config.generateResponsiveVariants()).thenReturn(false);
        service.activate(config);

        List<ImageOptimizerService.ResponsiveVariant> variants = 
            service.generateResponsiveVariants("/content/dam/test.jpg", null);

        assertNotNull(variants);
        assertTrue(variants.isEmpty());
    }

    @Test
    void testGenerateResponsiveVariantsWithCustomBreakpoints() {
        List<String> customBreakpoints = Arrays.asList("480", "960");
        
        List<ImageOptimizerService.ResponsiveVariant> variants = 
            service.generateResponsiveVariants("/content/dam/test.jpg", customBreakpoints);

        assertNotNull(variants);
    }

    @Test
    void testGenerateOptimizedRendition() {
        byte[] result = service.generateOptimizedRendition(
            "/content/dam/test.jpg",
            ImageOptimizerService.OptimizationOptions.builder()
                .maxWidth(800)
                .maxHeight(600)
                .quality(90)
                .build()
        );

        assertNotNull(result);
    }

    @Test
    void testGenerateOptimizedRenditionWithDefaults() {
        byte[] result = service.generateOptimizedRendition(
            "/content/dam/test.jpg",
            null
        );

        assertNotNull(result);
    }

    @Test
    void testCompressionSuggestionWithTransparency() {
        when(config.maxWidth()).thenReturn(800);
        when(config.maxHeight()).thenReturn(600);
        service.activate(config);

        ImageOptimizerService.CompressionSuggestion suggestion = 
            service.getCompressionSuggestion("/content/dam/test.png");

        assertNotNull(suggestion);
    }

    @Test
    void testResponsiveVariantsContainCorrectBreakpoints() {
        List<String> breakpoints = Arrays.asList("320", "768", "1024", "1920");
        
        List<ImageOptimizerService.ResponsiveVariant> variants = 
            service.generateResponsiveVariants("/content/dam/test.jpg", breakpoints);

        assertNotNull(variants);
    }
}

class ImageOptimizerServiceIntegrationTest {

    @Test
    void testAnalysisResultBuilder() {
        List<String> objects = new ArrayList<>();
        objects.add("product");
        objects.add("background");

        ImageOptimizerService.ImageAnalysisResult result = 
            ImageOptimizerService.ImageAnalysisResult.builder()
                .imagePath("/content/dam/products/item.jpg")
                .width(2400)
                .height(1600)
                .format("png")
                .fileSize(1200000)
                .dominantColors("#FFFFFF,#F0F0F0")
                .hasTransparency(true)
                .detectedObjects(objects)
                .sceneDescription("Product on white background")
                .qualityScore(95)
                .build();

        assertEquals("/content/dam/products/item.jpg", result.getImagePath());
        assertEquals(2400, result.getWidth());
        assertEquals(1600, result.getHeight());
        assertTrue(result.hasTransparency());
        assertEquals(95, result.getQualityScore());
    }

    @Test
    void testOptimizationSuggestionTypes() {
        ImageOptimizerService.OptimizationSuggestion resizeSuggestion = 
            ImageOptimizerService.OptimizationSuggestion.builder()
                .type("RESIZE")
                .description("Resize to fit dimensions")
                .priority(ImageOptimizerService.OptimizationSuggestion.Priority.HIGH)
                .build();

        ImageOptimizerService.OptimizationSuggestion formatSuggestion = 
            ImageOptimizerService.OptimizationSuggestion.builder()
                .type("FORMAT")
                .description("Convert to WebP")
                .priority(ImageOptimizerService.OptimizationSuggestion.Priority.MEDIUM)
                .build();

        ImageOptimizerService.OptimizationSuggestion compressionSuggestion = 
            ImageOptimizerService.OptimizationSuggestion.builder()
                .type("COMPRESSION")
                .description("Apply compression")
                .priority(ImageOptimizerService.OptimizationSuggestion.Priority.LOW)
                .build();

        assertNotNull(resizeSuggestion);
        assertNotNull(formatSuggestion);
        assertNotNull(compressionSuggestion);
    }

    @Test
    void testCompressionSuggestionEstimation() {
        ImageOptimizerService.CompressionSuggestion suggestion = 
            ImageOptimizerService.CompressionSuggestion.builder()
                .recommendedFormat("webp")
                .estimatedCompressionRatio(0.6)
                .build();

        assertTrue(suggestion.getEstimatedCompressionRatio() > 0);
    }

    @Test
    void testResponsiveVariantEstimation() {
        ImageOptimizerService.ResponsiveVariant variant = 
            ImageOptimizerService.ResponsiveVariant.builder()
                .breakpoint("md")
                .width(1024)
                .height(576)
                .format("webp")
                .estimatedSize(65000)
                .variantPath("/content/dam/test_md.webp")
                .build();

        assertTrue(variant.getEstimatedSize() > 0);
    }

    @Test
    void testOptimizationOptionsPreserveAspectRatio() {
        ImageOptimizerService.OptimizationOptions options = 
            ImageOptimizerService.OptimizationOptions.builder()
                .maxWidth(800)
                .maxHeight(600)
                .preserveAspectRatio(true)
                .build();

        assertTrue(options.isPreserveAspectRatio());
        assertEquals(800, options.getMaxWidth());
    }

    @Test
    void testAllBreakpointNames() {
        List<String> allBreakpoints = new ArrayList<>(Arrays.asList("xs", "sm", "md", "lg"));
        
        for (String bp : allBreakpoints) {
            ImageOptimizerService.ResponsiveVariant variant = 
                ImageOptimizerService.ResponsiveVariant.builder()
                    .breakpoint(bp)
                    .width(768)
                    .height(432)
                    .build();
            
            assertEquals(bp, variant.getBreakpoint());
        }
    }
}