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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaggableContentTest {

    @Test
    void testCreateWithAllFields() {
        List<String> existingTags = Arrays.asList("java", "programming");
        TaggableContent content = TaggableContent.create("content-1", "Java Guide", 
            "Learn Java programming", existingTags, "article", "/content/articles/java");

        assertEquals("content-1", content.getContentId());
        assertEquals("Java Guide", content.getTitle());
        assertEquals("Learn Java programming", content.getText());
        assertEquals("article", content.getContentType());
        assertEquals("/content/articles/java", content.getPath());
        assertEquals(existingTags, content.getExistingTags());
    }

    @Test
    void testFromText() {
        TaggableContent content = TaggableContent.fromText("content-1", "Title", "Some text content");

        assertEquals("content-1", content.getContentId());
        assertEquals("Title", content.getTitle());
        assertEquals("Some text content", content.getText());
        assertEquals("text", content.getContentType());
        assertNull(content.getPath());
        assertNull(content.getExistingTags());
    }

    @Test
    void testContentIdGetter() {
        TaggableContent content = TaggableContent.fromText("id-123", "Title", "Text");
        assertEquals("id-123", content.getContentId());
    }

    @Test
    void testTitleGetter() {
        TaggableContent content = TaggableContent.fromText("id", "My Title", "Text");
        assertEquals("My Title", content.getTitle());
    }

    @Test
    void testTextGetter() {
        TaggableContent content = TaggableContent.fromText("id", "Title", "My text content");
        assertEquals("My text content", content.getText());
    }

    @Test
    void testContentTypeDefault() {
        TaggableContent content = TaggableContent.fromText("id", "Title", "Text");
        assertEquals("text", content.getContentType());
    }

    @Test
    void testExistingTagsCanBeNull() {
        TaggableContent content = TaggableContent.create("id", "Title", "Text", null, "article", null);
        assertNull(content.getExistingTags());
    }
}