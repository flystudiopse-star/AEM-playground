package com.aem.playground.core.services;

import java.util.List;
import java.util.Map;

public interface ContentTransformer {

    String transformContent(String htmlContent, TransformOptions options);
    
    String generateImageAltText(byte[] imageData, String imageName);
    
    Map<String, String> generateMetadata(String content, String title);
    
    List<ComponentMapping> suggestComponentMappings(String htmlContent);
    
    String cleanupAndOptimize(String htmlContent, CleanupOptions options);
    
    List<RedirectMapping> generateRedirectMappings(List<String> sourceUrls, String targetBaseUrl);

    interface TransformOptions {
        String getTargetFormat();
        boolean isPreserveImages();
        boolean isCleanupHtml();
        Map<String, String> getCustomMappings();
    }

    interface CleanupOptions {
        boolean isRemoveEmptyParagraphs();
        boolean isFixEncoding();
        boolean isNormalizeWhitespace();
        boolean isRemoveInvalidTags();
    }

    class ComponentMapping {
        private final String sourceElement;
        private final String targetComponent;
        private final double confidence;
        private final String reason;

        public ComponentMapping(String sourceElement, String targetComponent, double confidence, String reason) {
            this.sourceElement = sourceElement;
            this.targetComponent = targetComponent;
            this.confidence = confidence;
            this.reason = reason;
        }

        public String getSourceElement() { return sourceElement; }
        public String getTargetComponent() { return targetComponent; }
        public double getConfidence() { return confidence; }
        public String getReason() { return reason; }
    }

    class RedirectMapping {
        private final String sourceUrl;
        private final String targetUrl;
        private final int priority;

        public RedirectMapping(String sourceUrl, String targetUrl, int priority) {
            this.sourceUrl = sourceUrl;
            this.targetUrl = targetUrl;
            this.priority = priority;
        }

        public String getSourceUrl() { return sourceUrl; }
        public String getTargetUrl() { return targetUrl; }
        public int getPriority() { return priority; }
    }
}