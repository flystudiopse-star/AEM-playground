package com.aem.playground.core.services;

import java.util.List;

public interface ImageOptimizerService {

    ImageAnalysisResult analyzeImage(String imagePath);

    List<OptimizationSuggestion> getOptimizationSuggestions(String imagePath);

    String generateAltText(String imagePath);

    CompressionSuggestion getCompressionSuggestion(String imagePath);

    List<ResponsiveVariant> generateResponsiveVariants(String imagePath, List<String> breakpoints);

    byte[] generateOptimizedRendition(String imagePath, OptimizationOptions options);

    class ImageAnalysisResult {
        private final String imagePath;
        private final int width;
        private final int height;
        private final String format;
        private final long fileSize;
        private final String dominantColors;
        private final boolean hasTransparency;
        private final List<String> detectedObjects;
        private final String sceneDescription;
        private final int qualityScore;

        private ImageAnalysisResult(Builder builder) {
            this.imagePath = builder.imagePath;
            this.width = builder.width;
            this.height = builder.height;
            this.format = builder.format;
            this.fileSize = builder.fileSize;
            this.dominantColors = builder.dominantColors;
            this.hasTransparency = builder.hasTransparency;
            this.detectedObjects = builder.detectedObjects;
            this.sceneDescription = builder.sceneDescription;
            this.qualityScore = builder.qualityScore;
        }

        public String getImagePath() { return imagePath; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public String getFormat() { return format; }
        public long getFileSize() { return fileSize; }
        public String getDominantColors() { return dominantColors; }
        public boolean hasTransparency() { return hasTransparency; }
        public List<String> getDetectedObjects() { return detectedObjects; }
        public String getSceneDescription() { return sceneDescription; }
        public int getQualityScore() { return qualityScore; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String imagePath;
            private int width;
            private int height;
            private String format;
            private long fileSize;
            private String dominantColors;
            private boolean hasTransparency;
            private List<String> detectedObjects;
            private String sceneDescription;
            private int qualityScore;

            public Builder imagePath(String val) { this.imagePath = val; return this; }
            public Builder width(int val) { this.width = val; return this; }
            public Builder height(int val) { this.height = val; return this; }
            public Builder format(String val) { this.format = val; return this; }
            public Builder fileSize(long val) { this.fileSize = val; return this; }
            public Builder dominantColors(String val) { this.dominantColors = val; return this; }
            public Builder hasTransparency(boolean val) { this.hasTransparency = val; return this; }
            public Builder detectedObjects(List<String> val) { this.detectedObjects = val; return this; }
            public Builder sceneDescription(String val) { this.sceneDescription = val; return this; }
            public Builder qualityScore(int val) { this.qualityScore = val; return this; }

            public ImageAnalysisResult build() { return new ImageAnalysisResult(this); }
        }
    }

    class OptimizationSuggestion {
        private final String type;
        private final String description;
        private final double estimatedSavings;
        private final String action;
        private final Priority priority;

        public enum Priority { HIGH, MEDIUM, LOW }

        private OptimizationSuggestion(Builder builder) {
            this.type = builder.type;
            this.description = builder.description;
            this.estimatedSavings = builder.estimatedSavings;
            this.action = builder.action;
            this.priority = builder.priority;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public double getEstimatedSavings() { return estimatedSavings; }
        public String getAction() { return action; }
        public Priority getPriority() { return priority; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String type;
            private String description;
            private double estimatedSavings;
            private String action;
            private Priority priority = Priority.MEDIUM;

            public Builder type(String val) { this.type = val; return this; }
            public Builder description(String val) { this.description = val; return this; }
            public Builder estimatedSavings(double val) { this.estimatedSavings = val; return this; }
            public Builder action(String val) { this.action = val; return this; }
            public Builder priority(Priority val) { this.priority = val; return this; }

            public OptimizationSuggestion build() { return new OptimizationSuggestion(this); }
        }
    }

    class CompressionSuggestion {
        private final String recommendedFormat;
        private final int recommendedQuality;
        private final int suggestedMaxWidth;
        private final int suggestedMaxHeight;
        private final boolean useProgressive;
        private final double estimatedCompressionRatio;
        private final boolean stripMetadata;

        private CompressionSuggestion(Builder builder) {
            this.recommendedFormat = builder.recommendedFormat;
            this.recommendedQuality = builder.recommendedQuality;
            this.suggestedMaxWidth = builder.suggestedMaxWidth;
            this.suggestedMaxHeight = builder.suggestedMaxHeight;
            this.useProgressive = builder.useProgressive;
            this.estimatedCompressionRatio = builder.estimatedCompressionRatio;
            this.stripMetadata = builder.stripMetadata;
        }

        public String getRecommendedFormat() { return recommendedFormat; }
        public int getRecommendedQuality() { return recommendedQuality; }
        public int getSuggestedMaxWidth() { return suggestedMaxWidth; }
        public int getSuggestedMaxHeight() { return suggestedMaxHeight; }
        public boolean isUseProgressive() { return useProgressive; }
        public double getEstimatedCompressionRatio() { return estimatedCompressionRatio; }
        public boolean isStripMetadata() { return stripMetadata; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String recommendedFormat = "webp";
            private int recommendedQuality = 80;
            private int suggestedMaxWidth = 1920;
            private int suggestedMaxHeight = 1080;
            private boolean useProgressive = true;
            private double estimatedCompressionRatio = 0.5;
            private boolean stripMetadata = true;

            public Builder recommendedFormat(String val) { this.recommendedFormat = val; return this; }
            public Builder recommendedQuality(int val) { this.recommendedQuality = val; return this; }
            public Builder suggestedMaxWidth(int val) { this.suggestedMaxWidth = val; return this; }
            public Builder suggestedMaxHeight(int val) { this.suggestedMaxHeight = val; return this; }
            public Builder useProgressive(boolean val) { this.useProgressive = val; return this; }
            public Builder estimatedCompressionRatio(double val) { this.estimatedCompressionRatio = val; return this; }
            public Builder stripMetadata(boolean val) { this.stripMetadata = val; return this; }

            public CompressionSuggestion build() { return new CompressionSuggestion(this); }
        }
    }

    class ResponsiveVariant {
        private final String breakpoint;
        private final int width;
        private final int height;
        private final String format;
        private final long estimatedSize;
        private final String variantPath;

        private ResponsiveVariant(Builder builder) {
            this.breakpoint = builder.breakpoint;
            this.width = builder.width;
            this.height = builder.height;
            this.format = builder.format;
            this.estimatedSize = builder.estimatedSize;
            this.variantPath = builder.variantPath;
        }

        public String getBreakpoint() { return breakpoint; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public String getFormat() { return format; }
        public long getEstimatedSize() { return estimatedSize; }
        public String getVariantPath() { return variantPath; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String breakpoint;
            private int width;
            private int height;
            private String format = "webp";
            private long estimatedSize;
            private String variantPath;

            public Builder breakpoint(String val) { this.breakpoint = val; return this; }
            public Builder width(int val) { this.width = val; return this; }
            public Builder height(int val) { this.height = val; return this; }
            public Builder format(String val) { this.format = val; return this; }
            public Builder estimatedSize(long val) { this.estimatedSize = val; return this; }
            public Builder variantPath(String val) { this.variantPath = val; return this; }

            public ResponsiveVariant build() { return new ResponsiveVariant(this); }
        }
    }

    class OptimizationOptions {
        private final int maxWidth;
        private final int maxHeight;
        private final String format;
        private final int quality;
        private final boolean stripMetadata;
        private final boolean preserveAspectRatio;

        private OptimizationOptions(Builder builder) {
            this.maxWidth = builder.maxWidth;
            this.maxHeight = builder.maxHeight;
            this.format = builder.format;
            this.quality = builder.quality;
            this.stripMetadata = builder.stripMetadata;
            this.preserveAspectRatio = builder.preserveAspectRatio;
        }

        public int getMaxWidth() { return maxWidth; }
        public int getMaxHeight() { return maxHeight; }
        public String getFormat() { return format; }
        public int getQuality() { return quality; }
        public boolean isStripMetadata() { return stripMetadata; }
        public boolean isPreserveAspectRatio() { return preserveAspectRatio; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private int maxWidth = 1920;
            private int maxHeight = 1080;
            private String format = "webp";
            private int quality = 85;
            private boolean stripMetadata = true;
            private boolean preserveAspectRatio = true;

            public Builder maxWidth(int val) { this.maxWidth = val; return this; }
            public Builder maxHeight(int val) { this.maxHeight = val; return this; }
            public Builder format(String val) { this.format = val; return this; }
            public Builder quality(int val) { this.quality = val; return this; }
            public Builder stripMetadata(boolean val) { this.stripMetadata = val; return this; }
            public Builder preserveAspectRatio(boolean val) { this.preserveAspectRatio = val; return this; }

            public OptimizationOptions build() { return new OptimizationOptions(this); }
        }
    }
}