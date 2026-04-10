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

class TagManagerResultTest {

    @Test
    void testSuccessResult() {
        List<ContentTag> tags = Arrays.asList(
            ContentTag.manual("java", "programming"),
            ContentTag.manual("coding", "skill")
        );

        TagManagerResult result = TagManagerResult.success("Tags managed successfully", tags);

        assertTrue(result.isSuccess());
        assertEquals("Tags managed successfully", result.getMessage());
        assertEquals(tags, result.getTags());
    }

    @Test
    void testFailureResult() {
        TagManagerResult result = TagManagerResult.failure("Tag not found");

        assertFalse(result.isSuccess());
        assertEquals("Tag not found", result.getMessage());
        assertNull(result.getTags());
    }

    @Test
    void testSuccessWithEmptyTags() {
        TagManagerResult result = TagManagerResult.success("Operation completed", Collections.emptyList());

        assertTrue(result.isSuccess());
        assertNotNull(result.getTags());
        assertTrue(result.getTags().isEmpty());
    }

    @Test
    void testGettersOnSuccess() {
        ContentTag tag = ContentTag.manual("test", "category");
        TagManagerResult result = TagManagerResult.success("Success message", Arrays.asList(tag));

        assertEquals("Success message", result.getMessage());
        assertEquals(1, result.getTags().size());
        assertEquals("test", result.getTags().get(0).getName());
    }

    @Test
    void testGettersOnFailure() {
        TagManagerResult result = TagManagerResult.failure("Error occurred");

        assertEquals("Error occurred", result.getMessage());
        assertNull(result.getTags());
    }
}