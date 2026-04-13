package com.aem.playground.core.services;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContentErrorTest {

    @Test
    void testContentErrorCreate() {
        long timestamp = System.currentTimeMillis();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("url", "/content/page");

        ContentError error = ContentError.create(
            "err_001",
            ErrorType.BROKEN_LINK,
            ErrorSeverity.WARNING,
            "Broken link detected",
            "/content/home",
            "paragraph:2",
            "Update link to valid URL",
            metadata,
            timestamp
        );

        assertEquals("err_001", error.getErrorId());
        assertEquals(ErrorType.BROKEN_LINK, error.getType());
        assertEquals(ErrorSeverity.WARNING, error.getSeverity());
        assertEquals("Broken link detected", error.getMessage());
        assertEquals("/content/home", error.getContentPath());
        assertEquals("paragraph:2", error.getLocation());
        assertEquals("Update link to valid URL", error.getSuggestedFix());
        assertEquals(metadata, error.getMetadata());
        assertEquals(timestamp, error.getDetectedAt());
    }

    @Test
    void testContentErrorBuilder() {
        ContentError error = ContentError.builder()
            .errorId("err_002")
            .type(ErrorType.MISSING_ASSET)
            .severity(ErrorSeverity.CRITICAL)
            .message("Missing asset reference")
            .contentPath("/content/dam/image")
            .location("image:1")
            .suggestedFix("Upload asset")
            .build();

        assertEquals("err_002", error.getErrorId());
        assertEquals(ErrorType.MISSING_ASSET, error.getType());
        assertEquals(ErrorSeverity.CRITICAL, error.getSeverity());
    }

    @Test
    void testContentErrorBuilderGeneratesId() {
        ContentError error = ContentError.builder()
            .type(ErrorType.AUTHORING_ERROR)
            .message("Test error")
            .build();

        assertNotNull(error.getErrorId());
        assertTrue(error.getErrorId().startsWith("err_"));
    }

    @Test
    void testContentErrorMetadata() {
        ContentError error = ContentError.builder()
            .type(ErrorType.BROKEN_LINK)
            .message("Test")
            .metadata(createTestMetadata())
            .build();

        assertNotNull(error.getMetadata());
        assertEquals("value1", error.getMetadata().get("key1"));
        assertEquals(42, error.getMetadata().get("key2"));
    }

    private Map<String, Object> createTestMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 42);
        return metadata;
    }
}