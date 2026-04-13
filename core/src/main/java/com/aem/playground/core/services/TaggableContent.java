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

import java.util.List;

public class TaggableContent {
    private final String contentId;
    private final String title;
    private final String text;
    private final List<String> existingTags;
    private final String contentType;
    private final String path;

    private TaggableContent(String contentId, String title, String text, List<String> existingTags, 
                          String contentType, String path) {
        this.contentId = contentId;
        this.title = title;
        this.text = text;
        this.existingTags = existingTags;
        this.contentType = contentType;
        this.path = path;
    }

    public static TaggableContent create(String contentId, String title, String text, List<String> existingTags,
                                        String contentType, String path) {
        return new TaggableContent(contentId, title, text, existingTags, contentType, path);
    }

    public static TaggableContent fromText(String contentId, String title, String text) {
        return new TaggableContent(contentId, title, text, null, "text", null);
    }

    public String getContentId() {
        return contentId;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<String> getExistingTags() {
        return existingTags;
    }

    public String getContentType() {
        return contentType;
    }

    public String getPath() {
        return path;
    }
}