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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContentCategoryTest {

    @Test
    void testCreateWithAllFields() {
        List<String> subcats = Arrays.asList("sub1", "sub2");
        List<ContentTag> tags = Arrays.asList(
            ContentTag.create("tag1", "cat", 0.8, "ai"),
            ContentTag.create("tag2", "cat", 0.7, "ai")
        );

        ContentCategory category = ContentCategory.create("Technology", "Science", subcats, 0.9, tags);

        assertEquals("Technology", category.getName());
        assertEquals("Science", category.getParentCategory());
        assertEquals(subcats, category.getSubcategories());
        assertEquals(0.9, category.getRelevanceScore());
        assertEquals(tags, category.getAssociatedTags());
    }

    @Test
    void testSimple() {
        ContentCategory category = ContentCategory.simple("Music", 0.85);

        assertEquals("Music", category.getName());
        assertEquals(0.85, category.getRelevanceScore());
        assertNull(category.getParentCategory());
        assertNull(category.getSubcategories());
        assertNull(category.getAssociatedTags());
    }

    @Test
    void testHasChildren() {
        ContentCategory withChildren = ContentCategory.create("Parent", null, Arrays.asList("child1"), 0.8, null);
        assertTrue(withChildren.hasChildren());

        ContentCategory noChildren = ContentCategory.simple("NoChildren", 0.5);
        assertFalse(noChildren.hasChildren());
    }

    @Test
    void testHasChildrenWithNullSubcategories() {
        ContentCategory category = ContentCategory.simple("Test", 0.5);
        assertFalse(category.hasChildren());
    }

    @Test
    void testGetters() {
        ContentCategory category = ContentCategory.create("TestCat", "Parent", null, 0.7, null);

        assertEquals("TestCat", category.getName());
        assertEquals("Parent", category.getParentCategory());
        assertEquals(0.7, category.getRelevanceScore());
    }
}