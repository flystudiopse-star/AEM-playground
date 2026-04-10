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

public class TagManagerResult {
    private final boolean success;
    private final String message;
    private final List<ContentTag> tags;

    private TagManagerResult(boolean success, String message, List<ContentTag> tags) {
        this.success = success;
        this.message = message;
        this.tags = tags;
    }

    public static TagManagerResult success(String message, List<ContentTag> tags) {
        return new TagManagerResult(true, message, tags);
    }

    public static TagManagerResult failure(String message) {
        return new TagManagerResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<ContentTag> getTags() {
        return tags;
    }
}