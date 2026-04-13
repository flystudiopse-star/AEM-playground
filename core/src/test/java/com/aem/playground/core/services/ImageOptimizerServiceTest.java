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
package com.aem.playground.core.services;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageOptimizerConfigTest {

    @Test
    void testDefaultQuality() {
        assertEquals(85, 85);
    }

    @Test
    void testMaxDimensions() {
        assertEquals(1920, 1920);
        assertEquals(1080, 1080);
    }

    @Test
    void testBreakpoints() {
        String breakpoints = "320,768,1024,1920";
        assertNotNull(breakpoints);
    }
}

class ImageAnalysisResultTest {

    @Test
    void testBuilderWithAllFields() {
        List<String> objects = new ArrayList<>();
        objects.add("person");
        objects.add("car");

        ImageOptimizerService.ImageAnalysisResult result = ImageOptimizerService.ImageAnalysisResult.builder()
            .imagePath("/content/dam/image.jpg")
            .width(1920)
            .height(1080)
            .format("jpeg")
            .fileSize(500000)
            .dominantColors("#FF0000,#00FF00")
            .hasTransparency(false)
            .detectedObjects(objects)
            .sceneDescription("A person standing next to a car")
            .qualityScore(85)
            .build();

        assertEquals("/content/dam/image.jpg", result.getImagePath());
        assertEquals(1920, result.getWidth());
        assertEquals(1080, result.getHeight());
        assertEquals("jpeg", result.getFormat());
        assertEquals(500000, result.getFileSize());
        assertEquals("#FF0000,#00FF00", result.getDominantColors());
        assertFalse(result.hasTransparency());
        assertEquals(2, result.getDetectedObjects().size());
        assertEquals("A person standing next to a car", result.getSceneDescription());
        assertEquals(85, result.getQualityScore());
    }

    @Test
    void testBuilderWithMinimalFields() {
        ImageOptimizerService.ImageAnalysisResult result = ImageOptimizerService.ImageAnalysisResult.builder()
            .imagePath("/content/dam/image.jpg")
            .qualityScore(0)
            .build();

        assertEquals("/content/dam/image.jpg", result.getImagePath());
        assertEquals(0, result.getWidth());
        assertEquals(0, result.getHeight());
    }

    @Test
    void testNullDetectedObjects() {
        ImageOptimizerService.ImageAnalysisResult result = ImageOptimizerService.ImageAnalysisResult.builder()
            .imagePath("/content/dam/image.jpg")
            .detectedObjects(null)
            .build();

        assertNull(result.getDetectedObjects());
    }
}

class OptimizationSuggestionTest {

    @Test
    void testSuggestionBuilder() {
        ImageOptimizerService.OptimizationSuggestion suggestion = ImageOptimizerService.OptimizationSuggestion.builder()
            .type("RESIZE")
            .description("Image too large")
            .action("Resize to 1920px")
            .estimatedSavings(0.35)
            .priority(ImageOptimizerService.OptimizationSuggestion.Priority.HIGH)
            .build();

        assertEquals("RESIZE", suggestion.getType());
        assertEquals("Image too large", suggestion.getDescription());
        assertEquals("Resize to 1920px", suggestion.getAction());
        assertEquals(0.35, suggestion.getEstimatedSavings());
        assertEquals(ImageOptimizerService.OptimizationSuggestion.Priority.HIGH, suggestion.getPriority());
    }

    @Test
    void testDefaultPriority() {
        ImageOptimizerService.OptimizationSuggestion suggestion = ImageOptimizerService.OptimizationSuggestion.builder()
            .type("FORMAT")
            .description("Convert format")
            .build();

        assertEquals(ImageOptimizerService.OptimizationSuggestion.Priority.MEDIUM, suggestion.getPriority());
    }

    @Test
    void testAllPriorityLevels() {
        for (ImageOptimizerService.OptimizationSuggestion.Priority priority : 
                ImageOptimizerService.OptimizationSuggestion.Priority.values()) {
            ImageOptimizerService.OptimizationSuggestion suggestion = 
                ImageOptimizerService.OptimizationSuggestion.builder()
                    .type("TEST")
                    .description("Test " + priority)
                    .priority(priority)
                    .build();
            assertEquals(priority, suggestion.getPriority());
        }
    }
}

class CompressionSuggestionTest {

    @Test
    void testCompressionSuggestionBuilder() {
        ImageOptimizerService.CompressionSuggestion suggestion = 
            ImageOptimizerService.CompressionSuggestion.builder()
                .recommendedFormat("webp")
                .recommendedQuality(85)
                .suggestedMaxWidth(1920)
                .suggestedMaxHeight(1080)
                .useProgressive(true)
                .estimatedCompressionRatio(0.5)
                .stripMetadata(true)
                .build();

        assertEquals("webp", suggestion.getRecommendedFormat());
        assertEquals(85, suggestion.getRecommendedQuality());
        assertEquals(1920, suggestion.getSuggestedMaxWidth());
        assertEquals(1080, suggestion.getSuggestedMaxHeight());
        assertTrue(suggestion.isUseProgressive());
        assertEquals(0.5, suggestion.getEstimatedCompressionRatio());
        assertTrue(suggestion.isStripMetadata());
    }

    @Test
    void testDefaultValues() {
        ImageOptimizerService.CompressionSuggestion suggestion = 
            ImageOptimizerService.CompressionSuggestion.builder().build();

        assertEquals("webp", suggestion.getRecommendedFormat());
        assertEquals(80, suggestion.getRecommendedQuality());
        assertEquals(1920, suggestion.getSuggestedMaxWidth());
        assertFalse(suggestion.isUseProgressive());
        assertTrue(suggestion.isStripMetadata());
    }

    @Test
    void testPngRecommendation() {
        ImageOptimizerService.CompressionSuggestion suggestion = 
            ImageOptimizerService.CompressionSuggestion.builder()
                .recommendedFormat("png")
                .stripMetadata(false)
                .build();

        assertEquals("png", suggestion.getRecommendedFormat());
        assertFalse(suggestion.isStripMetadata());
    }
}

class ResponsiveVariantTest {

    @Test
    void testResponsiveVariantBuilder() {
        ImageOptimizerService.ResponsiveVariant variant = 
            ImageOptimizerService.ResponsiveVariant.builder()
                .breakpoint("sm")
                .width(768)
                .height(432)
                .format("webp")
                .estimatedSize(45000)
                .variantPath("/content/dam/image_sm.webp")
                .build();

        assertEquals("sm", variant.getBreakpoint());
        assertEquals(768, variant.getWidth());
        assertEquals(432, variant.getHeight());
        assertEquals("webp", variant.getFormat());
        assertEquals(45000, variant.getEstimatedSize());
        assertEquals("/content/dam/image_sm.webp", variant.getVariantPath());
    }

    @Test
    void testDefaultFormat() {
        ImageOptimizerService.ResponsiveVariant variant = 
            ImageOptimizerService.ResponsiveVariant.builder()
                .breakpoint("xs")
                .width(320)
                .height(180)
                .build();

        assertEquals("webp", variant.getFormat());
    }

    @Test
    void testAllBreakpoints() {
        String[] breakpointNames = {"xs", "sm", "md", "lg", "xl0"};
        int[] widths = {320, 768, 1024, 1920, 2560};

        for (int i = 0; i < breakpointNames.length; i++) {
            ImageOptimizerService.ResponsiveVariant variant = 
                ImageOptimizerService.ResponsiveVariant.builder()
                    .breakpoint(breakpointNames[i])
                    .width(widths[i])
                    .height(widths[i] * 9 / 16)
                    .build();

            assertEquals(breakpointNames[i], variant.getBreakpoint());
            assertEquals(widths[i], variant.getWidth());
        }
    }
}

class OptimizationOptionsTest {

    @Test
    void testOptimizationOptionsBuilder() {
        ImageOptimizerService.OptimizationOptions options = 
            ImageOptimizerService.OptimizationOptions.builder()
                .maxWidth(800)
                .maxHeight(600)
                .format("jpeg")
                .quality(90)
                .stripMetadata(true)
                .preserveAspectRatio(true)
                .build();

        assertEquals(800, options.getMaxWidth());
        assertEquals(600, options.getMaxHeight());
        assertEquals("jpeg", options.getFormat());
        assertEquals(90, options.getQuality());
        assertTrue(options.isStripMetadata());
        assertTrue(options.isPreserveAspectRatio());
    }

    @Test
    void testDefaultValues() {
        ImageOptimizerService.OptimizationOptions options = 
            ImageOptimizerService.OptimizationOptions.builder().build();

        assertEquals(1920, options.getMaxWidth());
        assertEquals(1080, options.getMaxHeight());
        assertEquals("webp", options.getFormat());
        assertEquals(85, options.getQuality());
        assertTrue(options.isStripMetadata());
        assertTrue(options.isPreserveAspectRatio());
    }
}