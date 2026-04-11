package com.aem.playground.core.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixStepTest {

    @Test
    void testFixStepCreate() {
        FixStep step = FixStep.create(
            1,
            "replace",
            "element:1",
            "Replace element with correct value",
            true
        );

        assertEquals(1, step.getStepNumber());
        assertEquals("replace", step.getAction());
        assertEquals("element:1", step.getTarget());
        assertEquals("Replace element with correct value", step.getDescription());
        assertTrue(step.isAutomatic());
    }

    @Test
    void testFixStepBuilder() {
        FixStep step = FixStep.builder()
            .stepNumber(2)
            .action("verify")
            .target("link:1")
            .description("Verify the fix works")
            .isAutomatic(false)
            .build();

        assertEquals(2, step.getStepNumber());
        assertEquals("verify", step.getAction());
        assertEquals("link:1", step.getTarget());
        assertFalse(step.isAutomatic());
    }

    @Test
    void testFixStepWithMultipleNumbers() {
        FixStep step1 = FixStep.builder()
            .stepNumber(1)
            .action("update")
            .target("field:1")
            .description("Update field value")
            .build();

        FixStep step2 = FixStep.builder()
            .stepNumber(2)
            .action("save")
            .target("content:1")
            .description("Save content")
            .build();

        assertTrue(step1.getStepNumber() < step2.getStepNumber());
    }
}