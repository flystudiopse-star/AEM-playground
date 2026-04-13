package com.aem.playground.core.services;

import java.util.List;
import java.util.Map;

public class ErrorFix {
    private final String fixId;
    private final String errorId;
    private final FixType fixType;
    private final String description;
    private final String originalValue;
    private final String suggestedValue;
    private final List<FixStep> steps;
    private final Map<String, Object> metadata;
    private final double confidence;

    private ErrorFix(String fixId, String errorId, FixType fixType, String description,
                     String originalValue, String suggestedValue, 
                     List<FixStep> steps, Map<String, Object> metadata, double confidence) {
        this.fixId = fixId;
        this.errorId = errorId;
        this.fixType = fixType;
        this.description = description;
        this.originalValue = originalValue;
        this.suggestedValue = suggestedValue;
        this.steps = steps;
        this.metadata = metadata;
        this.confidence = confidence;
    }

    public static ErrorFix create(String fixId, String errorId, FixType fixType, String description,
                                    String originalValue, String suggestedValue,
                                    List<FixStep> steps, Map<String, Object> metadata, double confidence) {
        return new ErrorFix(fixId, errorId, fixType, description, originalValue, 
                           suggestedValue, steps, metadata, confidence);
    }

    public static ErrorFixBuilder builder() {
        return new ErrorFixBuilder();
    }

    public String getFixId() {
        return fixId;
    }

    public String getErrorId() {
        return errorId;
    }

    public FixType getFixType() {
        return fixType;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getSuggestedValue() {
        return suggestedValue;
    }

    public List<FixStep> getSteps() {
        return steps;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public double getConfidence() {
        return confidence;
    }

    public static class ErrorFixBuilder {
        private String fixId;
        private String errorId;
        private FixType fixType;
        private String description;
        private String originalValue;
        private String suggestedValue;
        private List<FixStep> steps;
        private Map<String, Object> metadata;
        private double confidence = 0.8;

        public ErrorFixBuilder fixId(String fixId) {
            this.fixId = fixId;
            return this;
        }

        public ErrorFixBuilder errorId(String errorId) {
            this.errorId = errorId;
            return this;
        }

        public ErrorFixBuilder fixType(FixType fixType) {
            this.fixType = fixType;
            return this;
        }

        public ErrorFixBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ErrorFixBuilder originalValue(String originalValue) {
            this.originalValue = originalValue;
            return this;
        }

        public ErrorFixBuilder suggestedValue(String suggestedValue) {
            this.suggestedValue = suggestedValue;
            return this;
        }

        public ErrorFixBuilder steps(List<FixStep> steps) {
            this.steps = steps;
            return this;
        }

        public ErrorFixBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ErrorFixBuilder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public ErrorFix build() {
            if (fixId == null) {
                fixId = "fix_" + System.currentTimeMillis();
            }
            return ErrorFix.create(fixId, errorId, fixType, description, originalValue, 
                                  suggestedValue, steps, metadata, confidence);
        }
    }
}