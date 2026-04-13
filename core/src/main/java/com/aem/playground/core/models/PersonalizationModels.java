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
package com.aem.playground.core.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PersonalizationModels {

    public static class UserSegment {
        private final String id;
        private final String name;
        private final String description;
        private final Map<String, Object> attributes;
        private final double weight;

        public UserSegment(String id, String name, String description, Map<String, Object> attributes, double weight) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
            this.weight = weight;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public double getWeight() {
            return weight;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String description;
            private Map<String, Object> attributes = new HashMap<>();
            private double weight = 1.0;

            public Builder id(String id) {
                this.id = id;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder attribute(String key, Object value) {
                this.attributes.put(key, value);
                return this;
            }

            public Builder attributes(Map<String, Object> attributes) {
                this.attributes = attributes;
                return this;
            }

            public Builder weight(double weight) {
                this.weight = weight;
                return this;
            }

            public UserSegment build() {
                return new UserSegment(id, name, description, attributes, weight);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserSegment that = (UserSegment) o;
            return Double.compare(that.weight, weight) == 0 &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, weight);
        }
    }

    public static class PersonalizationRule {
        private final String id;
        private final String name;
        private final String targetSegmentId;
        private final String contentPath;
        private final String variantId;
        private final Map<String, Object> conditions;
        private final int priority;
        private final boolean enabled;

        public PersonalizationRule(String id, String name, String targetSegmentId, String contentPath,
                                  String variantId, Map<String, Object> conditions, int priority, boolean enabled) {
            this.id = id;
            this.name = name;
            this.targetSegmentId = targetSegmentId;
            this.contentPath = contentPath;
            this.variantId = variantId;
            this.conditions = conditions != null ? new HashMap<>(conditions) : new HashMap<>();
            this.priority = priority;
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getTargetSegmentId() {
            return targetSegmentId;
        }

        public String getContentPath() {
            return contentPath;
        }

        public String getVariantId() {
            return variantId;
        }

        public Map<String, Object> getConditions() {
            return conditions;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String targetSegmentId;
            private String contentPath;
            private String variantId;
            private Map<String, Object> conditions = new HashMap<>();
            private int priority = 0;
            private boolean enabled = true;

            public Builder id(String id) {
                this.id = id;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder targetSegmentId(String targetSegmentId) {
                this.targetSegmentId = targetSegmentId;
                return this;
            }

            public Builder contentPath(String contentPath) {
                this.contentPath = contentPath;
                return this;
            }

            public Builder variantId(String variantId) {
                this.variantId = variantId;
                return this;
            }

            public Builder condition(String key, Object value) {
                this.conditions.put(key, value);
                return this;
            }

            public Builder conditions(Map<String, Object> conditions) {
                this.conditions = conditions;
                return this;
            }

            public Builder priority(int priority) {
                this.priority = priority;
                return this;
            }

            public Builder enabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }

            public PersonalizationRule build() {
                return new PersonalizationRule(id, name, targetSegmentId, contentPath, variantId, conditions, priority, enabled);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonalizationRule that = (PersonalizationRule) o;
            return priority == that.priority &&
                    enabled == that.enabled &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, priority, enabled);
        }
    }

    public static class ContentVariant {
        private final String id;
        private final String name;
        private final String content;
        private final String contentPath;
        private final Map<String, Object> metadata;
        private final boolean isAIGenerated;

        public ContentVariant(String id, String name, String content, String contentPath,
                           Map<String, Object> metadata, boolean isAIGenerated) {
            this.id = id;
            this.name = name;
            this.content = content;
            this.contentPath = contentPath;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.isAIGenerated = isAIGenerated;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

        public String getContentPath() {
            return contentPath;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public boolean isAIGenerated() {
            return isAIGenerated;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String content;
            private String contentPath;
            private Map<String, Object> metadata = new HashMap<>();
            private boolean isAIGenerated = false;

            public Builder id(String id) {
                this.id = id;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder content(String content) {
                this.content = content;
                return this;
            }

            public Builder contentPath(String contentPath) {
                this.contentPath = contentPath;
                return this;
            }

            public Builder metadata(String key, Object value) {
                this.metadata.put(key, value);
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            public Builder isAIGenerated(boolean isAIGenerated) {
                this.isAIGenerated = isAIGenerated;
                return this;
            }

            public ContentVariant build() {
                return new ContentVariant(id, name, content, contentPath, metadata, isAIGenerated);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContentVariant that = (ContentVariant) o;
            return isAIGenerated == that.isAIGenerated &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, isAIGenerated);
        }
    }

    public static class ABTest {
        public enum Status {
            DRAFT, RUNNING, PAUSED, COMPLETED
        }

        private final String id;
        private final String name;
        private final String contentPath;
        private final String controlVariantId;
        private final String testVariantId;
        private final double trafficPercentage;
        private final Status status;
        private final long startTime;
        private final long endTime;
        private final Map<String, Object> metrics;

        public ABTest(String id, String name, String contentPath, String controlVariantId, String testVariantId,
                   double trafficPercentage, Status status, long startTime, long endTime, Map<String, Object> metrics) {
            this.id = id;
            this.name = name;
            this.contentPath = contentPath;
            this.controlVariantId = controlVariantId;
            this.testVariantId = testVariantId;
            this.trafficPercentage = trafficPercentage;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.metrics = metrics != null ? new HashMap<>(metrics) : new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getContentPath() {
            return contentPath;
        }

        public String getControlVariantId() {
            return controlVariantId;
        }

        public String getTestVariantId() {
            return testVariantId;
        }

        public double getTrafficPercentage() {
            return trafficPercentage;
        }

        public Status getStatus() {
            return status;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public Map<String, Object> getMetrics() {
            return metrics;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String contentPath;
            private String controlVariantId;
            private String testVariantId;
            private double trafficPercentage = 50.0;
            private Status status = Status.DRAFT;
            private long startTime;
            private long endTime;
            private Map<String, Object> metrics = new HashMap<>();

            public Builder id(String id) {
                this.id = id;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder contentPath(String contentPath) {
                this.contentPath = contentPath;
                return this;
            }

            public Builder controlVariantId(String controlVariantId) {
                this.controlVariantId = controlVariantId;
                return this;
            }

            public Builder testVariantId(String testVariantId) {
                this.testVariantId = testVariantId;
                return this;
            }

            public Builder trafficPercentage(double trafficPercentage) {
                this.trafficPercentage = trafficPercentage;
                return this;
            }

            public Builder status(Status status) {
                this.status = status;
                return this;
            }

            public Builder startTime(long startTime) {
                this.startTime = startTime;
                return this;
            }

            public Builder endTime(long endTime) {
                this.endTime = endTime;
                return this;
            }

            public Builder metric(String key, Object value) {
                this.metrics.put(key, value);
                return this;
            }

            public Builder metrics(Map<String, Object> metrics) {
                this.metrics = metrics;
                return this;
            }

            public ABTest build() {
                return new ABTest(id, name, contentPath, controlVariantId, testVariantId,
                        trafficPercentage, status, startTime, endTime, metrics);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ABTest abTest = (ABTest) o;
            return Double.compare(abTest.trafficPercentage, trafficPercentage) == 0 &&
                    Objects.equals(id, abTest.id) &&
                    Objects.equals(name, abTest.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, trafficPercentage);
        }
    }

    public static class PersonalizationPreview {
        private final String contentPath;
        private final String segmentId;
        private final String variantId;
        private final String previewContent;
        private final Map<String, Object> context;

        public PersonalizationPreview(String contentPath, String segmentId, String variantId,
                                 String previewContent, Map<String, Object> context) {
            this.contentPath = contentPath;
            this.segmentId = segmentId;
            this.variantId = variantId;
            this.previewContent = previewContent;
            this.context = context != null ? new HashMap<>(context) : new HashMap<>();
        }

        public String getContentPath() {
            return contentPath;
        }

        public String getSegmentId() {
            return segmentId;
        }

        public String getVariantId() {
            return variantId;
        }

        public String getPreviewContent() {
            return previewContent;
        }

        public Map<String, Object> getContext() {
            return context;
        }
    }
}