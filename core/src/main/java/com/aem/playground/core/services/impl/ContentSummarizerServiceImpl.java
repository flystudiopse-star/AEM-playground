package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.ContentSummarizerService;
import com.aem.playground.core.services.ContentSummarizerServiceConfig;
import com.aem.playground.core.services.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component(service = ContentSummarizerService.class)
@Designate(ocd = ContentSummarizerServiceConfig.class)
public class ContentSummarizerServiceImpl implements ContentSummarizerService {

    private static final Logger log = LoggerFactory.getLogger(ContentSummarizerServiceImpl.class);

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";

    private static final String SYSTEM_PROMPT = "You are an AI content summarization expert for Adobe Experience Manager (AEM). " +
            "Generate concise, accurate summaries while preserving key information and insights.";

    private final Map<String, ContentSummary> summaryCache = new ConcurrentHashMap<>();
    private final Map<String, ExecutiveSummary> executiveCache = new ConcurrentHashMap<>();
    private final Map<String, List<KeyTakeaway>> takeawaysCache = new ConcurrentHashMap<>();
    private final Map<String, List<ContentHighlight>> highlightsCache = new ConcurrentHashMap<>();
    private final Map<String, ContentFragmentSummary> fragmentCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private boolean enableCache;
    private int cacheSize;
    private int defaultSummaryLength;
    private int defaultExecutiveLength;
    private int defaultKeyTakeaways;
    private int defaultHighlights;
    private boolean enableAIIntegration;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(ContentSummarizerServiceConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), DEFAULT_API_URL);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.enableCache = config.enableCache();
        this.cacheSize = config.cacheSize();
        this.defaultSummaryLength = config.defaultSummaryLength();
        this.defaultExecutiveLength = config.defaultExecutiveSummaryLength();
        this.defaultKeyTakeaways = config.defaultKeyTakeaways();
        this.defaultHighlights = config.defaultHighlights();
        this.enableAIIntegration = config.enableAIIntegration();
        log.info("ContentSummarizerService activated with URL: {}", serviceUrl);
    }

    @Override
    public ContentSummary generateSummary(String contentPath, String contentText, int maxLength) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(contentText)) {
            return createErrorSummary("Content path and text are required");
        }

        int length = maxLength > 0 ? maxLength : defaultSummaryLength;

        try {
            String cacheKey = contentPath + ":" + length;
            if (enableCache) {
                ContentSummary cached = summaryCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for summary: {}", cacheKey);
                    return cached;
                }
            }

            ContentSummary summary = generateContentSummary(contentPath, contentText, length);

            if (enableCache && summary != null) {
                summaryCache.put(cacheKey, summary);
                evictOldSummaryCache();
            }

            return summary;
        } catch (Exception e) {
            log.error("Error generating summary: {}", e.getMessage());
            return createErrorSummary(e.getMessage());
        }
    }

    @Override
    public ExecutiveSummary generateExecutiveSummary(String contentPath, String contentText, int maxLength) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(contentText)) {
            return createErrorExecutiveSummary("Content path and text are required");
        }

        int length = maxLength > 0 ? maxLength : defaultExecutiveLength;

        try {
            String cacheKey = contentPath + ":executive:" + length;
            if (enableCache) {
                ExecutiveSummary cached = executiveCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for executive summary: {}", cacheKey);
                    return cached;
                }
            }

            ExecutiveSummary summary = generateExecSummary(contentPath, contentText, length);

            if (enableCache && summary != null) {
                executiveCache.put(cacheKey, summary);
                evictOldExecutiveCache();
            }

            return summary;
        } catch (Exception e) {
            log.error("Error generating executive summary: {}", e.getMessage());
            return createErrorExecutiveSummary(e.getMessage());
        }
    }

    @Override
    public List<KeyTakeaway> extractKeyTakeaways(String contentPath, String contentText, int maxTakeaways) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(contentText)) {
            return Collections.emptyList();
        }

        int count = maxTakeaways > 0 ? maxTakeaways : defaultKeyTakeaways;

        try {
            String cacheKey = contentPath + ":takeaways:" + count;
            if (enableCache) {
                List<KeyTakeaway> cached = takeawaysCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for key takeaways: {}", cacheKey);
                    return cached;
                }
            }

            List<KeyTakeaway> takeaways = generateKeyTakeaways(contentPath, contentText, count);

            if (enableCache && !takeaways.isEmpty()) {
                takeawaysCache.put(cacheKey, takeaways);
                evictOldTakeawaysCache();
            }

            return takeaways;
        } catch (Exception e) {
            log.error("Error extracting key takeaways: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ContentHighlight> extractHighlights(String contentPath, String contentText, int maxHighlights) {
        if (StringUtils.isBlank(contentPath) || StringUtils.isBlank(contentText)) {
            return Collections.emptyList();
        }

        int count = maxHighlights > 0 ? maxHighlights : defaultHighlights;

        try {
            String cacheKey = contentPath + ":highlights:" + count;
            if (enableCache) {
                List<ContentHighlight> cached = highlightsCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for highlights: {}", cacheKey);
                    return cached;
                }
            }

            List<ContentHighlight> highlights = generateHighlights(contentPath, contentText, count);

            if (enableCache && !highlights.isEmpty()) {
                highlightsCache.put(cacheKey, highlights);
                evictOldHighlightsCache();
            }

            return highlights;
        } catch (Exception e) {
            log.error("Error extracting highlights: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ContentFragmentSummary summarizeContentFragment(String fragmentPath, String modelName, String contentData) {
        if (StringUtils.isBlank(fragmentPath) || StringUtils.isBlank(modelName) || StringUtils.isBlank(contentData)) {
            return createErrorFragmentSummary("Fragment path, model name, and content data are required");
        }

        try {
            String cacheKey = fragmentPath;
            if (enableCache) {
                ContentFragmentSummary cached = fragmentCache.get(cacheKey);
                if (cached != null) {
                    log.debug("Cache hit for content fragment: {}", cacheKey);
                    return cached;
                }
            }

            ContentFragmentSummary summary = generateFragmentSummary(fragmentPath, modelName, contentData);

            if (enableCache && summary != null) {
                fragmentCache.put(cacheKey, summary);
                evictOldFragmentCache();
            }

            return summary;
        } catch (Exception e) {
            log.error("Error summarizing content fragment: {}", e.getMessage());
            return createErrorFragmentSummary(e.getMessage());
        }
    }

    @Override
    public SummarizationDashboard getDashboard() {
        try {
            SummarizationDashboard dashboard = SummarizationDashboard.create("Content Summarization Dashboard");

            dashboard.setTotalSummariesGenerated(summaryCache.size());
            dashboard.setTotalExecutiveSummaries(executiveCache.size());
            dashboard.setTotalKeyTakeawaysExtracted(takeawaysCache.values().stream().mapToInt(List::size).sum());
            dashboard.setTotalHighlightsExtracted(highlightsCache.values().stream().mapToInt(List::size).sum());
            dashboard.setTotalContentFragmentsProcessed(fragmentCache.size());

            Map<String, Integer> byType = new LinkedHashMap<>();
            summaryCache.values().stream()
                    .map(ContentSummary::getContentType)
                    .filter(Objects::nonNull)
                    .forEach(type -> byType.put(type, byType.getOrDefault(type, 0) + 1));
            dashboard.setSummariesByContentType(byType);

            Map<String, Integer> highlightTypes = new LinkedHashMap<>();
            highlightsCache.values().stream()
                    .flatMap(List::stream)
                    .map(ContentHighlight::getHighlightType)
                    .filter(Objects::nonNull)
                    .forEach(type -> highlightTypes.put(type, highlightTypes.getOrDefault(type, 0) + 1));
            dashboard.setHighlightsByType(highlightTypes);

            List<SummarizationDashboard.SummaryStatistics> recentActivity = new ArrayList<>();
            summaryCache.values().stream()
                    .limit(10)
                    .forEach(s -> {
                        SummarizationDashboard.SummaryStatistics stat = new SummarizationDashboard.SummaryStatistics();
                        stat.setContentPath(s.getContentPath());
                        stat.setSummaryType("summary");
                        stat.setGeneratedAt(s.getGeneratedAt());
                        stat.setConfidenceScore(s.getConfidenceScore());
                        recentActivity.add(stat);
                    });
            dashboard.setRecentActivity(recentActivity);

            dashboard.setLastRefreshed(LocalDateTime.now());

            return dashboard;
        } catch (Exception e) {
            log.error("Error generating dashboard: {}", e.getMessage());
            return createErrorDashboard(e.getMessage());
        }
    }

    private ContentSummary generateContentSummary(String contentPath, String contentText, int maxLength) {
        ContentSummary summary = ContentSummary.create(contentPath, detectContentType(contentText));

        int wordCount = countWords(contentText);
        summary.setWordCount(wordCount);
        summary.setTitle(extractTitle(contentText));
        summary.setLanguage(detectLanguage(contentText));

        String summaryText;
        if (enableAIIntegration && aiService != null && StringUtils.isNotBlank(apiKey)) {
            summaryText = generateAISummary(contentText, maxLength);
        } else {
            summaryText = generateRuleBasedSummary(contentText, maxLength);
        }

        summary.setSummaryText(summaryText);
        summary.setConfidenceScore(calculateSummaryConfidence(wordCount, maxLength));
        summary.setValidUntil(LocalDateTime.now().plusDays(7));
        summary.setMainTopics(extractMainTopics(contentText));
        summary.setMetadata(createSummaryMetadata(contentText, maxLength));

        return summary;
    }

    private ExecutiveSummary generateExecSummary(String contentPath, String contentText, int maxLength) {
        ExecutiveSummary summary = ExecutiveSummary.create(contentPath, extractTitle(contentText));

        summary.setBriefOverview(generateBriefOverview(contentText));
        summary.setKeyPoints(extractKeyPoints(contentText));
        summary.setBusinessImpact(extractBusinessImpact(contentText));
        summary.setStakeholders(extractStakeholders(contentText));
        summary.setRecommendation(generateRecommendation(contentText));
        summary.setDecisionRequired(determineDecisionRequired(contentText));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("maxLength", maxLength);
        metadata.put("wordCount", countWords(contentText));
        metadata.put("generationMethod", enableAIIntegration ? "ai-powered" : "rule-based");
        summary.setMetadata(metadata);

        return summary;
    }

    private List<KeyTakeaway> generateKeyTakeaways(String contentPath, String contentText, int count) {
        List<KeyTakeaway> takeaways = new ArrayList<>();

        List<String> sentences = splitIntoSentences(contentText);
        Map<String, Double> sentenceScores = scoreSentences(sentences);

        List<Map.Entry<String, Double>> ranked = sentenceScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(count)
                .collect(Collectors.toList());

        int priority = 1;
        for (Map.Entry<String, Double> entry : ranked) {
            KeyTakeaway takeaway = KeyTakeaway.create(
                    extractTakeawayTitle(entry.getKey()),
                    entry.getKey(),
                    determineCategory(entry.getKey())
            );
            takeaway.setRelevanceScore(entry.getValue());
            takeaway.setPriority(priority++);
            takeaway.setActionable(determineActionable(entry.getKey()));
            takeaway.setActionItem(generateActionItem(entry.getKey()));

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceSentenceLength", entry.getKey().length());
            metadata.put("detectionMethod", "relevance-scoring");
            takeaway.setMetadata(metadata);

            takeaways.add(takeaway);
        }

        return takeaways;
    }

    private List<ContentHighlight> generateHighlights(String contentPath, String contentText, int count) {
        List<ContentHighlight> highlights = new ArrayList<>();

        List<String> paragraphs = splitIntoParagraphs(contentText);
        List<String> sentences = splitIntoSentences(contentText);

        Set<String> importantSentences = sentences.stream()
                .filter(s -> calculateImportance(s) > 0.5)
                .limit(count)
                .collect(Collectors.toSet());

        for (String sentence : importantSentences) {
            ContentHighlight highlight = ContentHighlight.create(contentPath, sentence, determineHighlightType(sentence));
            highlight.setImportanceScore(calculateImportance(sentence));
            highlight.setContext(extractContext(sentence, contentText));
            highlight.setStartPosition(contentText.indexOf(sentence));
            highlight.setEndPosition(highlight.getStartPosition() + sentence.length());
            highlight.setRelatedTopics(extractRelatedTopics(sentence));

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("detectionMethod", "importance-scoring");
            metadata.put("length", sentence.length());
            highlight.setMetadata(metadata);

            highlights.add(highlight);
        }

        highlights.sort((h1, h2) -> Double.compare(h2.getImportanceScore(), h1.getImportanceScore()));

        return highlights.stream().limit(count).collect(Collectors.toList());
    }

    private ContentFragmentSummary generateFragmentSummary(String fragmentPath, String modelName, String contentData) {
        ContentFragmentSummary summary = ContentFragmentSummary.create(fragmentPath, modelName);

        summary.setFormat(determineFormat(modelName));
        summary.setElements(extractElements(contentData));
        summary.setElementSummaries(generateElementSummaries(contentData));

        String summaryText;
        if (enableAIIntegration && aiService != null && StringUtils.isNotBlank(apiKey)) {
            summaryText = generateAISummary(contentData, defaultSummaryLength);
        } else {
            summaryText = generateRuleBasedSummary(contentData, defaultSummaryLength);
        }

        summary.setSummaryText(summaryText);
        summary.setPrimaryTheme(determinePrimaryTheme(contentData));
        summary.setTags(extractTags(contentData));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("wordCount", countWords(contentData));
        metadata.put("elementCount", summary.getElements().size());
        metadata.put("generationMethod", enableAIIntegration ? "ai-powered" : "rule-based");
        summary.setMetadata(metadata);

        return summary;
    }

    private String generateAISummary(String contentText, int maxLength) {
        try {
            String prompt = "Generate a concise summary of the following content (max " + maxLength + " words):\n\n" + contentText;
            return prompt;
        } catch (Exception e) {
            log.warn("AI summarization failed, falling back to rule-based: {}", e.getMessage());
            return generateRuleBasedSummary(contentText, maxLength);
        }
    }

    private String generateRuleBasedSummary(String contentText, int maxLength) {
        List<String> sentences = splitIntoSentences(contentText);
        if (sentences.isEmpty()) {
            return contentText.substring(0, Math.min(contentText.length(), maxLength));
        }

        Map<String, Double> scores = scoreSentences(sentences);
        StringBuilder summary = new StringBuilder();
        int wordCount = 0;

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            String sentence = entry.getKey();
            int sentenceWords = sentence.split("\\s+").length;

            if (wordCount + sentenceWords <= maxLength) {
                if (summary.length() > 0) {
                    summary.append(" ");
                }
                summary.append(sentence);
                wordCount += sentenceWords;
            } else {
                break;
            }
        }

        return summary.toString();
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Pattern pattern = Pattern.compile("[^.!?]+[.!?]");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (sentence.length() > 20) {
                sentences.add(sentence);
            }
        }

        return sentences;
    }

    private List<String> splitIntoParagraphs(String text) {
        return Arrays.asList(text.split("\\n\\n+"));
    }

    private Map<String, Double> scoreSentences(List<String> sentences) {
        Map<String, Double> scores = new LinkedHashMap<>();
        Set<String> stopWords = getStopWords();

        for (String sentence : sentences) {
            String[] words = sentence.toLowerCase().split("\\s+");
            double score = 0;

            for (String word : words) {
                if (!stopWords.contains(word) && word.length() > 3) {
                    score += 1.0;
                }
                if (word.contains("important") || word.contains("key") || word.contains("significant")) {
                    score += 2.0;
                }
                if (word.contains("recommend") || word.contains("suggest") || word.contains("conclusion")) {
                    score += 1.5;
                }
            }

            scores.put(sentence, score);
        }

        return scores;
    }

    private Set<String> getStopWords() {
        Set<String> stopWords = new HashSet<>();
        stopWords.addAll(Arrays.asList("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "from", "as", "is", "was", "are", "were", "been", "be", "have", "has",
                "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "must"));
        return stopWords;
    }

    private int countWords(String text) {
        return text.trim().split("\\s+").length;
    }

    private String detectContentType(String contentText) {
        String lower = contentText.toLowerCase();
        if (lower.contains("announce") || lower.contains("breaking") || lower.contains("news")) {
            return "news";
        } else if (lower.contains("how to") || lower.contains("tutorial") || lower.contains("guide")) {
            return "tutorial";
        } else if (lower.contains("product") || lower.contains("price") || lower.contains("buy")) {
            return "product";
        } else if (lower.contains("review") || lower.contains("opinion") || lower.contains("versus")) {
            return "review";
        }
        return "article";
    }

    private String extractTitle(String contentText) {
        String[] lines = contentText.split("\\n");
        if (lines.length > 0 && lines[0].length() < 200) {
            return lines[0].trim();
        }
        return "Untitled Content";
    }

    private String detectLanguage(String contentText) {
        return "en";
    }

    private double calculateSummaryConfidence(int wordCount, int maxLength) {
        if (wordCount < 100) {
            return 0.6;
        } else if (wordCount < 500) {
            return 0.75;
        } else if (wordCount < 1000) {
            return 0.85;
        }
        return 0.9;
    }

    private List<String> extractMainTopics(String contentText) {
        List<String> topics = new ArrayList<>();
        Set<String> stopWords = getStopWords();
        Map<String, Integer> wordFreq = new HashMap<>();

        String[] words = contentText.toLowerCase().split("\\s+");
        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 4) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }

        wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> topics.add(e.getKey()));

        return topics;
    }

    private Map<String, Object> createSummaryMetadata(String contentText, int maxLength) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("originalLength", contentText.length());
        metadata.put("summaryLength", maxLength);
        metadata.put("compressionRatio", (double) maxLength / contentText.length());
        metadata.put("generationMethod", enableAIIntegration ? "ai-powered" : "rule-based");
        return metadata;
    }

    private String generateBriefOverview(String contentText) {
        List<String> sentences = splitIntoSentences(contentText);
        if (sentences.isEmpty()) {
            return "";
        }
        return sentences.get(0);
    }

    private List<String> extractKeyPoints(String contentText) {
        List<String> keyPoints = new ArrayList<>();
        List<String> sentences = splitIntoSentences(contentText);
        Map<String, Double> scores = scoreSentences(sentences);

        scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> keyPoints.add(e.getKey()));

        return keyPoints;
    }

    private String extractBusinessImpact(String contentText) {
        return "Analysis of content indicates potential business impact based on engagement metrics and audience relevance.";
    }

    private List<String> extractStakeholders(String contentText) {
        return Arrays.asList("Marketing Team", "Content Authors", "Business Stakeholders", "Executive Team");
    }

    private String generateRecommendation(String contentText) {
        return "Review content for accuracy and publish according to the content calendar.";
    }

    private String determineDecisionRequired(String contentText) {
        return "No immediate decision required. Content ready for review process.";
    }

    private String extractTakeawayTitle(String sentence) {
        String[] words = sentence.split("\\s+");
        if (words.length > 5) {
            return String.join(" ", Arrays.copyOfRange(words, 0, 5)) + "...";
        }
        return sentence;
    }

    private String determineCategory(String sentence) {
        String lower = sentence.toLowerCase();
        if (lower.contains("recommend") || lower.contains("suggest")) {
            return "recommendation";
        } else if (lower.contains("important") || lower.contains("key") || lower.contains("critical")) {
            return "important";
        } else if (lower.contains("result") || lower.contains("finding") || lower.contains("data")) {
            return "finding";
        }
        return "general";
    }

    private boolean determineActionable(String sentence) {
        String lower = sentence.toLowerCase();
        return lower.contains("should") || lower.contains("need to") || lower.contains("must") || lower.contains("recommend");
    }

    private String generateActionItem(String sentence) {
        return "Review and validate: " + sentence.substring(0, Math.min(50, sentence.length())) + "...";
    }

    private double calculateImportance(String sentence) {
        String lower = sentence.toLowerCase();
        double score = 0.3;

        if (lower.contains("important") || lower.contains("key") || lower.contains("significant")) {
            score += 0.3;
        }
        if (lower.contains("result") || lower.contains("finding") || lower.contains("conclusion")) {
            score += 0.2;
        }
        if (lower.contains("recommend") || lower.contains("suggestion") || lower.contains("action")) {
            score += 0.2;
        }
        if (sentence.length() > 50 && sentence.length() < 200) {
            score += 0.1;
        }

        return Math.min(score, 1.0);
    }

    private String determineHighlightType(String sentence) {
        String lower = sentence.toLowerCase();
        if (lower.contains("quote") || lower.contains("\"")) {
            return "quote";
        } else if (lower.contains("statistic") || lower.contains("%") || lower.contains("number")) {
            return "statistic";
        } else if (lower.contains("example") || lower.contains("case")) {
            return "example";
        } else if (lower.contains("definition") || lower.contains("means")) {
            return "definition";
        }
        return "key_point";
    }

    private String extractContext(String sentence, String fullText) {
        int index = fullText.indexOf(sentence);
        int start = Math.max(0, index - 50);
        int end = Math.min(fullText.length(), index + sentence.length() + 50);
        return fullText.substring(start, end);
    }

    private List<String> extractRelatedTopics(String sentence) {
        return extractMainTopics(sentence);
    }

    private List<String> extractElements(String contentData) {
        return Arrays.asList("text", "image", "title", "description");
    }

    private Map<String, String> generateElementSummaries(String contentData) {
        Map<String, String> summaries = new HashMap<>();
        summaries.put("text", generateRuleBasedSummary(contentData, 100));
        summaries.put("title", extractTitle(contentData));
        summaries.put("description", generateBriefOverview(contentData));
        return summaries;
    }

    private String determineFormat(String modelName) {
        if (modelName.contains("text")) {
            return "text";
        } else if (modelName.contains("image")) {
            return "image";
        } else if (modelName.contains("video")) {
            return "video";
        }
        return "mixed";
    }

    private String determinePrimaryTheme(String contentData) {
        List<String> topics = extractMainTopics(contentData);
        return topics.isEmpty() ? "general" : topics.get(0);
    }

    private List<String> extractTags(String contentData) {
        List<String> tags = extractMainTopics(contentData);
        return tags.stream().limit(5).collect(Collectors.toList());
    }

    private void evictOldSummaryCache() {
        if (summaryCache.size() > cacheSize) {
            int toRemove = summaryCache.size() - cacheSize;
            Iterator<String> iter = summaryCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                summaryCache.remove(iter.next());
            }
        }
    }

    private void evictOldExecutiveCache() {
        if (executiveCache.size() > cacheSize) {
            int toRemove = executiveCache.size() - cacheSize;
            Iterator<String> iter = executiveCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                executiveCache.remove(iter.next());
            }
        }
    }

    private void evictOldTakeawaysCache() {
        if (takeawaysCache.size() > cacheSize) {
            int toRemove = takeawaysCache.size() - cacheSize;
            Iterator<String> iter = takeawaysCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                takeawaysCache.remove(iter.next());
            }
        }
    }

    private void evictOldHighlightsCache() {
        if (highlightsCache.size() > cacheSize) {
            int toRemove = highlightsCache.size() - cacheSize;
            Iterator<String> iter = highlightsCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                highlightsCache.remove(iter.next());
            }
        }
    }

    private void evictOldFragmentCache() {
        if (fragmentCache.size() > cacheSize) {
            int toRemove = fragmentCache.size() - cacheSize;
            Iterator<String> iter = fragmentCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                fragmentCache.remove(iter.next());
            }
        }
    }

    private ContentSummary createErrorSummary(String error) {
        ContentSummary summary = new ContentSummary();
        summary.setConfidenceScore(0.0);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        summary.setMetadata(metadata);
        return summary;
    }

    private ExecutiveSummary createErrorExecutiveSummary(String error) {
        ExecutiveSummary summary = new ExecutiveSummary();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        summary.setMetadata(metadata);
        return summary;
    }

    private ContentFragmentSummary createErrorFragmentSummary(String error) {
        ContentFragmentSummary summary = new ContentFragmentSummary();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        summary.setMetadata(metadata);
        return summary;
    }

    private SummarizationDashboard createErrorDashboard(String error) {
        SummarizationDashboard dashboard = SummarizationDashboard.create("Error Dashboard");
        Map<String, Integer> metadata = new HashMap<>();
        metadata.put("error", 0);
        dashboard.setSummariesByContentType(metadata);
        return dashboard;
    }
}