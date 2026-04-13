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

import java.util.Map;

public interface AIService {

    AIGenerationResult generateText(String prompt, AIGenerationOptions options);

    AIGenerationResult generateImage(String prompt, AIGenerationOptions options);

    void clearCache();

    class AIGenerationResult {
        private final String content;
        private final Map<String, Object> metadata;
        private final boolean success;
        private final String error;

        private AIGenerationResult(String content, Map<String, Object> metadata, boolean success, String error) {
            this.content = content;
            this.metadata = metadata;
            this.success = success;
            this.error = error;
        }

        public static AIGenerationResult success(String content, Map<String, Object> metadata) {
            return new AIGenerationResult(content, metadata, true, null);
        }

        public static AIGenerationResult error(String error) {
            return new AIGenerationResult(null, null, false, error);
        }

        public String getContent() {
            return content;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }
}
