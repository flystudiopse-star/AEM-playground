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

import java.util.HashMap;
import java.util.Map;

public final class AIGenerationOptions {

    private String model = "gpt-4";
    private double temperature = 0.7;
    private int maxTokens = 1000;
    private String customSystemPrompt;
    private int imageCount = 1;
    private String imageSize = "1024x1024";
    private boolean enableCache = true;
    private final Map<String, Object> additionalParams = new HashMap<>();

    private AIGenerationOptions() {
    }

    public static AIGenerationOptions builder() {
        return new AIGenerationOptions();
    }

    public AIGenerationOptions model(String model) {
        this.model = model;
        return this;
    }

    public AIGenerationOptions temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public AIGenerationOptions maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public AIGenerationOptions customSystemPrompt(String customSystemPrompt) {
        this.customSystemPrompt = customSystemPrompt;
        return this;
    }

    public AIGenerationOptions imageCount(int imageCount) {
        this.imageCount = imageCount;
        return this;
    }

    public AIGenerationOptions imageSize(String imageSize) {
        this.imageSize = imageSize;
        return this;
    }

    public AIGenerationOptions enableCache(boolean enableCache) {
        this.enableCache = enableCache;
        return this;
    }

    public AIGenerationOptions additionalParam(String key, Object value) {
        this.additionalParams.put(key, value);
        return this;
    }

    public AIGenerationOptions cacheKey(String cacheKey) {
        this.additionalParams.put("_cacheKey", cacheKey);
        return this;
    }

    public AIGenerationOptions build() {
        return this;
    }

    public String getModel() {
        return model;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public String getCustomSystemPrompt() {
        return customSystemPrompt;
    }

    public int getImageCount() {
        return imageCount;
    }

    public String getImageSize() {
        return imageSize;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public String getCacheKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(model).append(":").append(temperature).append(":").append(maxTokens);
        if (customSystemPrompt != null) {
            sb.append(":").append(customSystemPrompt.hashCode());
        }
        return sb.toString();
    }
}