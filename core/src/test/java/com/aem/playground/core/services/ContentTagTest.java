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

import static org.junit.jupiter.api.Assertions.*;

class ContentTagTest {

    @Test
    void testCreateWithAllFields() {
        ContentTag tag = ContentTag.create("java", "programming", 0.9, "ai");

        assertEquals("java", tag.getName());
        assertEquals("programming", tag.getCategory());
        assertEquals(0.9, tag.getConfidence());
        assertEquals("ai", tag.getSource());
    }

    @Test
    void testAiGenerated() {
        ContentTag tag = ContentTag.aiGenerated("python", "language", 0.85);

        assertEquals("python", tag.getName());
        assertEquals("language", tag.getCategory());
        assertEquals(0.85, tag.getConfidence());
        assertEquals("ai", tag.getSource());
    }

    @Test
    void testManual() {
        ContentTag tag = ContentTag.manual("manual-tag", "custom");

        assertEquals("manual-tag", tag.getName());
        assertEquals("custom", tag.getCategory());
        assertEquals(1.0, tag.getConfidence());
        assertEquals("manual", tag.getSource());
    }

    @Test
    void testIsHighConfidenceWithHighConfidence() {
        ContentTag tag = ContentTag.create("tag", "cat", 0.9, "ai");
        assertTrue(tag.isHighConfidence());
    }

    @Test
    void testIsHighConfidenceWithLowConfidence() {
        ContentTag tag = ContentTag.create("tag", "cat", 0.5, "ai");
        assertFalse(tag.isHighConfidence());
    }

    @Test
    void testIsHighConfidenceAtThreshold() {
        ContentTag tag = ContentTag.create("tag", "cat", 0.8, "ai");
        assertTrue(tag.isHighConfidence());
    }

    @Test
    void testGetters() {
        ContentTag tag = ContentTag.create("test-tag", "test-category", 0.75, "manual");

        assertEquals("test-tag", tag.getName());
        assertEquals("test-category", tag.getCategory());
        assertEquals(0.75, tag.getConfidence());
        assertEquals("manual", tag.getSource());
    }
}