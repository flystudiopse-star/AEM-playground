package com.aem.playground.core.services;

public class FixStep {
    private final int stepNumber;
    private final String action;
    private final String target;
    private final String description;
    private final boolean isAutomatic;

    private FixStep(int stepNumber, String action, String target, String description, boolean isAutomatic) {
        this.stepNumber = stepNumber;
        this.action = action;
        this.target = target;
        this.description = description;
        this.isAutomatic = isAutomatic;
    }

    public static FixStep create(int stepNumber, String action, String target, String description, boolean isAutomatic) {
        return new FixStep(stepNumber, action, target, description, isAutomatic);
    }

    public static FixStepBuilder builder() {
        return new FixStepBuilder();
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getAction() {
        return action;
    }

    public String getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public static class FixStepBuilder {
        private int stepNumber;
        private String action;
        private String target;
        private String description;
        private boolean isAutomatic = true;

        public FixStepBuilder stepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
            return this;
        }

        public FixStepBuilder action(String action) {
            this.action = action;
            return this;
        }

        public FixStepBuilder target(String target) {
            this.target = target;
            return this;
        }

        public FixStepBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FixStepBuilder isAutomatic(boolean isAutomatic) {
            this.isAutomatic = isAutomatic;
            return this;
        }

        public FixStep build() {
            return FixStep.create(stepNumber, action, target, description, isAutomatic);
        }
    }
}