package com.aem.playground.core.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class)
public class BilingualComparisonModel {

    private static final Logger log = LoggerFactory.getLogger(BilingualComparisonModel.class);

    private static final String PN_SOURCE_LANGUAGE = "sourceLanguage";
    private static final String PN_TARGET_LANGUAGE = "targetLanguage";
    private static final String PN_SOURCE_PATH = "sourcePath";
    private static final String PN_TARGET_PATH = "targetPath";
    private static final String PN_COMPARE_TITLE = "compareTitle";
    private static final String PN_COMPARE_DESCRIPTION = "compareDescription";
    private static final String PN_COMPARE_CONTENT = "compareContent";

    @ValueMapValue(name = PN_SOURCE_LANGUAGE, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String sourceLanguage;

    @ValueMapValue(name = PN_TARGET_LANGUAGE, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String targetLanguage;

    @ValueMapValue(name = PN_SOURCE_PATH, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String sourcePath;

    @ValueMapValue(name = PN_TARGET_PATH, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String targetPath;

    @ValueMapValue(name = PN_COMPARE_TITLE, injectionStrategy = InjectionStrategy.OPTIONAL)
    private boolean compareTitle;

    @ValueMapValue(name = PN_COMPARE_DESCRIPTION, injectionStrategy = InjectionStrategy.OPTIONAL)
    private boolean compareDescription;

    @ValueMapValue(name = PN_COMPARE_CONTENT, injectionStrategy = InjectionStrategy.OPTIONAL)
    private boolean compareContent;

    @SlingObject
    private Resource resource;

    private List<ComparisonItem> comparisonItems;
    private Map<String, ComparisonStats> stats;
    private boolean hasComparisonData;

    @PostConstruct
    protected void init() {
        comparisonItems = new ArrayList<>();
        stats = new HashMap<>();
        loadComparisonData();
    }

    private void loadComparisonData() {
        if (resource == null) {
            return;
        }

        Resource sourceResource = resource;
        Resource targetResource = resource;

        if (StringUtils.isNotBlank(sourcePath)) {
            sourceResource = resource.getResourceResolver().getResource(sourcePath);
        }
        if (StringUtils.isNotBlank(targetPath)) {
            targetResource = resource.getResourceResolver().getResource(targetPath);
        }

        if (sourceResource != null && targetResource != null) {
            compareContent(sourceResource, targetResource);
            hasComparisonData = !comparisonItems.isEmpty();
        }
    }

    private void compareContent(Resource source, Resource target) {
        Map<String, Object> sourceProps = new HashMap<>();
        Map<String, Object> targetProps = new HashMap<>();

        Resource sourceContent = getContentNode(source);
        Resource targetContent = getContentNode(target);

        if (sourceContent != null) {
            sourceProps = new HashMap<>(sourceContent.getValueMap());
        }
        if (targetContent != null) {
            targetProps = new HashMap<>(targetContent.getValueMap());
        }

        if (compareTitle) {
            String sourceTitle = getStringProperty(sourceProps, "jcr:title");
            String targetTitle = getStringProperty(targetProps, "jcr:title");
            if (sourceTitle != null && targetTitle != null) {
                addComparisonItem("Title", "jcr:title", sourceTitle, targetTitle);
            }
        }

        if (compareDescription) {
            String sourceDesc = getStringProperty(sourceProps, "jcr:description");
            String targetDesc = getStringProperty(targetProps, "jcr:description");
            if (sourceDesc != null && targetDesc != null) {
                addComparisonItem("Description", "jcr:description", sourceDesc, targetDesc);
            }
        }

        for (Map.Entry<String, Object> entry : sourceProps.entrySet()) {
            if (isTranslatableProperty(entry.getKey())) {
                String sourceValue = entry.getValue() != null ? entry.getValue().toString() : "";
                String targetValue = getStringProperty(targetProps, entry.getKey());
                if (targetValue == null) {
                    targetValue = "";
                }
                if (compareContent || (!"jcr:title".equals(entry.getKey()) && !"jcr:description".equals(entry.getKey()))) {
                    addComparisonItem(entry.getKey(), entry.getKey(), sourceValue, targetValue);
                }
            }
        }

        calculateStats();
    }

    private Resource getContentNode(Resource page) {
        if (page == null) {
            return null;
        }
        Resource content = page.getChild("jcr:content");
        return content != null ? content : page;
    }

    private String getStringProperty(Map<String, Object> props, String key) {
        Object value = props.get(key);
        return value != null ? value.toString() : null;
    }

    private boolean isTranslatableProperty(String key) {
        List<String> translatableKeys = java.util.Arrays.asList(
                "jcr:title", "jcr:description", "pageTitle", "pageDescription",
                "navTitle", "subtitle", "text", "content", "alt", "caption"
        );
        return translatableKeys.contains(key);
    }

    private void addComparisonItem(String label, String key, String sourceValue, String targetValue) {
        ComparisonItem item = new ComparisonItem();
        item.label = label;
        item.key = key;
        item.sourceValue = sourceValue;
        item.targetValue = targetValue;

        if (StringUtils.isNotBlank(sourceValue) && StringUtils.isNotBlank(targetValue)) {
            item.similarity = calculateSimilarity(sourceValue, targetValue);
            item.isTranslated = calculateSimilarity(sourceValue, targetValue) > 0.7;
        } else {
            item.similarity = 0.0;
            item.isTranslated = false;
        }

        comparisonItems.add(item);
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }

        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        if (s1.equals(s2)) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - ((double) distance / maxLength);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private void calculateStats() {
        int total = comparisonItems.size();
        int translated = 0;
        int partial = 0;
        int missing = 0;
        int exact = 0;

        for (ComparisonItem item : comparisonItems) {
            if (item.targetValue.isEmpty()) {
                missing++;
            } else if (item.isTranslated) {
                translated++;
            }
            if (item.similarity > 0.9) {
                exact++;
            } else if (item.similarity > 0.5) {
                partial++;
            }
        }

        stats.put("total", new ComparisonStats("Total Items", total));
        stats.put("translated", new ComparisonStats("Translated", translated));
        stats.put("partial", new ComparisonStats("Partially Translated", partial));
        stats.put("missing", new ComparisonStats("Missing Translation", missing));
        stats.put("exact", new ComparisonStats("Exact Match", exact));
    }

    public List<ComparisonItem> getComparisonItems() {
        return comparisonItems;
    }

    public Map<String, ComparisonStats> getStats() {
        return stats;
    }

    public boolean isHasComparisonData() {
        return hasComparisonData;
    }

    public String getSourceLanguage() {
        return StringUtils.defaultString(sourceLanguage, "en");
    }

    public String getTargetLanguage() {
        return StringUtils.defaultString(targetLanguage, "de");
    }

    public static class ComparisonItem {
        public String label;
        public String key;
        public String sourceValue;
        public String targetValue;
        public double similarity;
        public boolean isTranslated;
    }

    public static class ComparisonStats {
        public String label;
        public int value;

        public ComparisonStats(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() { return label; }
        public int getValue() { return value; }
    }
}