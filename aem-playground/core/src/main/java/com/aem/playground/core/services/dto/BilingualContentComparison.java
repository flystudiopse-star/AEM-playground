package com.aem.playground.core.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BilingualContentComparison {

    private String sourcePath;
    private String sourceLanguage;
    private String targetLanguage;
    private List<ContentDifference> differences;
    private Map<String, SourceTargetPair> componentPairs;
    private Map<String, SourceTargetPair> metadataPairs;
    private boolean hasDifferences;

    public BilingualContentComparison() {
        this.differences = new ArrayList<>();
        this.componentPairs = new HashMap<>();
        this.metadataPairs = new HashMap<>();
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public List<ContentDifference> getDifferences() {
        return differences;
    }

    public void addDifference(ContentDifference difference) {
        this.differences.add(difference);
        if (difference.getDifferenceType() != DifferenceType.EQUAL) {
            this.hasDifferences = true;
        }
    }

    public Map<String, SourceTargetPair> getComponentPairs() {
        return componentPairs;
    }

    public void addComponentPair(String componentPath, String sourceContent, String targetContent) {
        this.componentPairs.put(componentPath, new SourceTargetPair(sourceContent, targetContent));
    }

    public Map<String, SourceTargetPair> getMetadataPairs() {
        return metadataPairs;
    }

    public void addMetadataPair(String metadataKey, String sourceValue, String targetValue) {
        this.metadataPairs.put(metadataKey, new SourceTargetPair(sourceValue, targetValue));
    }

    public boolean hasDifferences() {
        return hasDifferences;
    }

    public static class ContentDifference {
        private String path;
        private String sourceContent;
        private String targetContent;
        private DifferenceType differenceType;
        private double similarity;

        public ContentDifference(String path, String sourceContent, String targetContent, DifferenceType differenceType) {
            this.path = path;
            this.sourceContent = sourceContent;
            this.targetContent = targetContent;
            this.differenceType = differenceType;
            this.similarity = calculateSimilarity(sourceContent, targetContent);
        }

        private double calculateSimilarity(String s1, String s2) {
            if (s1 == null && s2 == null) return 1.0;
            if (s1 == null || s2 == null) return 0.0;
            if (s1.equals(s2)) return 1.0;
            
            int maxLen = Math.max(s1.length(), s2.length());
            if (maxLen == 0) return 1.0;
            
            int distance = levenshteinDistance(s1, s2);
            return 1.0 - ((double) distance / maxLen);
        }

        private int levenshteinDistance(String s1, String s2) {
            int[][] dp = new int[s1.length() + 1][s2.length() + 1];
            for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
            for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
            
            for (int i = 1; i <= s1.length(); i++) {
                for (int j = 1; j <= s2.length(); j++) {
                    if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1];
                    } else {
                        dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                    }
                }
            }
            return dp[s1.length()][s2.length()];
        }

        public String getPath() {
            return path;
        }

        public String getSourceContent() {
            return sourceContent;
        }

        public String getTargetContent() {
            return targetContent;
        }

        public DifferenceType getDifferenceType() {
            return differenceType;
        }

        public double getSimilarity() {
            return similarity;
        }
    }

    public enum DifferenceType {
        EQUAL,
        MODIFIED,
        MISSING_IN_TARGET,
        ADDED_IN_TARGET
    }

    public static class SourceTargetPair {
        private String source;
        private String target;

        public SourceTargetPair(String source, String target) {
            this.source = source;
            this.target = target;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }
    }
}