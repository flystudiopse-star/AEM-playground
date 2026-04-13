package com.aem.playground.core.services;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorFixTest {

    @Test
    void testErrorFixCreate() {
        long timestamp = System.currentTimeMillis();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "replace");

        ErrorFix fix = ErrorFix.create(
            "fix_001",
            "err_001",
            FixType.REPLACE_LINK,
            "Replace broken link",
            "/broken/path",
            "/correct/path",
            Collections.emptyList(),
            metadata,
            0.95
        );

        assertEquals("fix_001", fix.getFixId());
        assertEquals("err_001", fix.getErrorId());
        assertEquals(FixType.REPLACE_LINK, fix.getFixType());
        assertEquals("Replace broken link", fix.getDescription());
        assertEquals("/broken/path", fix.getOriginalValue());
        assertEquals("/correct/path", fix.getSuggestedValue());
        assertEquals(metadata, fix.getMetadata());
        assertEquals(0.95, fix.getConfidence());
    }

    @Test
    void testErrorFixBuilder() {
        ErrorFix fix = ErrorFix.builder()
            .fixId("fix_002")
            .errorId("err_002")
            .fixType(FixType.AUTO_CORRECT)
            .description("Auto-correct typo")
            .originalValue("teh")
            .suggestedValue("the")
            .confidence(0.9)
            .build();

        assertEquals("fix_002", fix.getFixId());
        assertEquals("err_002", fix.getErrorId());
        assertEquals(FixType.AUTO_CORRECT, fix.getFixType());
        assertEquals("Auto-correct typo", fix.getDescription());
        assertEquals("teh", fix.getOriginalValue());
        assertEquals("the", fix.getSuggestedValue());
        assertEquals(0.9, fix.getConfidence());
    }

    @Test
    void testErrorFixBuilderWithSteps() {
        FixStep step1 = FixStep.builder()
            .stepNumber(1)
            .action("replace")
            .target("element:1")
            .description("Replace broken link")
            .build();

        FixStep step2 = FixStep.builder()
            .stepNumber(2)
            .action("verify")
            .target("link:1")
            .description("Verify link works")
            .build();

        ErrorFix fix = ErrorFix.builder()
            .fixType(FixType.REPLACE_LINK)
            .description("Replace broken link")
            .steps(Arrays.asList(step1, step2))
            .build();

        assertNotNull(fix.getSteps());
        assertEquals(2, fix.getSteps().size());
        assertEquals("replace", fix.getSteps().get(0).getAction());
    }

    @Test
    void testErrorFixBuilderGeneratesId() {
        ErrorFix fix = ErrorFix.builder()
            .fixType(FixType.NOTIFY_AUTHOR)
            .description("Manual review needed")
            .build();

        assertNotNull(fix.getFixId());
        assertTrue(fix.getFixId().startsWith("fix_"));
    }

    @Test
    void testErrorFixDefaultConfidence() {
        ErrorFix fix = ErrorFix.builder()
            .fixType(FixType.UPDATE_CONTENT)
            .description("Test fix")
            .build();

        assertEquals(0.8, fix.getConfidence());
    }
}