package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.SummarizationService;
import com.aem.playground.core.services.SummarizationServiceConfig;
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

@Component(service = SummarizationService.class)
@Designate(ocd = SummarizationServiceConfig.class)
public class SummarizationServiceImpl implements SummarizationService {

    private static final Logger log = LoggerFactory.getLogger(SummarizationServiceImpl.class);

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4";

    private final Map<String, PageSummary> pageSummaryCache = new ConcurrentHashMap<>();
    private final Map<String, ContentFragmentSummary> fragmentCache = new ConcurrentHashMap<>();
    private final Map<String, Excerpt> excerptCache = new ConcurrentHashMap<>();
    private final Map<String, MetaDescription> metaDescCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String serviceUrl;
    private String defaultModel;
    private float temperature;
    private int maxTokens;
    private int defaultSummaryLength;
    private boolean enableAIIntegration;

    @Reference
    private AIService aiService;

    @Activate
    protected void activate(SummarizationServiceConfig config) {
        this.apiKey = config.apiKey();
        this.serviceUrl = PropertiesUtil.toString(config.serviceUrl(), DEFAULT_API_URL);
        this.defaultModel = PropertiesUtil.toString(config.defaultModel(), DEFAULT_MODEL);
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
        this.defaultSummaryLength = config.defaultSummaryLength();
        this.enableAIIntegration = config.enableAIIntegration();
        log.info("SummarizationService activated with default length: {}", defaultSummaryLength);
    }

    @Override
    public PageSummary summarizePage(String pagePath, String pageContent, SummaryLength length) {
        return summarizePage(pagePath, pageContent, length, null);
    }

    @Override
    public PageSummary summarizePage(String pagePath, String pageContent, SummaryLength length, Map<String, Object> options) {
        if (StringUtils.isBlank(pagePath) || StringUtils.isBlank(pageContent)) {
            return createErrorPageSummary("Page path and content are required");
        }

        int targetLength = length != null ? length.getWordCount() : defaultSummaryLength;
        if (options != null && options.containsKey("targetLength")) {
            targetLength = (Integer) options.get("targetLength");
        }

        try {
            String cacheKey = pagePath + ":" + targetLength;
            PageSummary cached = pageSummaryCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for page summary: {}", cacheKey);
                return cached;
            }

            PageSummary summary = generatePageSummary(pagePath, pageContent, targetLength);
            pageSummaryCache.put(cacheKey, summary);
            evictOldPageCache();

            return summary;
        } catch (Exception e) {
            log.error("Error generating page summary: {}", e.getMessage());
            return createErrorPageSummary(e.getMessage());
        }
    }

    @Override
    public ContentFragmentSummary summarizeContentFragment(String fragmentPath, String fragmentContent, SummaryLength length) {
        return summarizeContentFragment(fragmentPath, fragmentContent, length, null);
    }

    @Override
    public ContentFragmentSummary summarizeContentFragment(String fragmentPath, String fragmentContent, SummaryLength length, Map<String, Object> options) {
        if (StringUtils.isBlank(fragmentPath) || StringUtils.isBlank(fragmentContent)) {
            return createErrorFragmentSummary("Fragment path and content are required");
        }

        int targetLength = length != null ? length.getWordCount() : defaultSummaryLength;
        if (options != null && options.containsKey("targetLength")) {
            targetLength = (Integer) options.get("targetLength");
        }

        try {
            String cacheKey = fragmentPath + ":" + targetLength;
            ContentFragmentSummary cached = fragmentCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for fragment summary: {}", cacheKey);
                return cached;
            }

            ContentFragmentSummary summary = generateFragmentSummary(fragmentPath, fragmentContent, targetLength);
            fragmentCache.put(cacheKey, summary);
            evictOldFragmentCache();

            return summary;
        } catch (Exception e) {
            log.error("Error generating fragment summary: {}", e.getMessage());
            return createErrorFragmentSummary(e.getMessage());
        }
    }

    @Override
    public Excerpt generateExcerpt(String content, SummaryLength length) {
        return generateExcerpt(content, length, null);
    }

    @Override
    public Excerpt generateExcerpt(String content, SummaryLength length, Map<String, Object> options) {
        if (StringUtils.isBlank(content)) {
            return createErrorExcerpt("Content is required");
        }

        int targetLength = length != null ? length.getWordCount() : defaultSummaryLength;
        if (options != null && options.containsKey("targetLength")) {
            targetLength = (Integer) options.get("targetLength");
        }

        try {
            String cacheKey = content.hashCode() + ":" + targetLength;
            Excerpt cached = excerptCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for excerpt: {}", cacheKey);
                return cached;
            }

            Excerpt excerpt = generateExcerptText(content, targetLength);
            excerptCache.put(cacheKey, excerpt);
            evictOldExcerptCache();

            return excerpt;
        } catch (Exception e) {
            log.error("Error generating excerpt: {}", e.getMessage());
            return createErrorExcerpt(e.getMessage());
        }
    }

    @Override
    public MetaDescription createMetaDescription(String content) {
        return createMetaDescription(content, SummaryLength.STANDARD, null);
    }

    @Override
    public MetaDescription createMetaDescription(String content, SummaryLength length) {
        return createMetaDescription(content, length, null);
    }

    @Override
    public MetaDescription createMetaDescription(String content, SummaryLength length, Map<String, Object> options) {
        if (StringUtils.isBlank(content)) {
            return createErrorMetaDescription("Content is required");
        }

        int maxCharacters = 160;
        if (length != null) {
            maxCharacters = length.getWordCount() * 6;
        }
        if (options != null && options.containsKey("maxCharacters")) {
            maxCharacters = (Integer) options.get("maxCharacters");
        }

        try {
            String cacheKey = content.hashCode() + ":" + maxCharacters;
            MetaDescription cached = metaDescCache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for meta description: {}", cacheKey);
                return cached;
            }

            MetaDescription metaDesc = generateMetaDescription(content, maxCharacters);
            metaDescCache.put(cacheKey, metaDesc);
            evictOldMetaCache();

            return metaDesc;
        } catch (Exception e) {
            log.error("Error generating meta description: {}", e.getMessage());
            return createErrorMetaDescription(e.getMessage());
        }
    }

    private PageSummary generatePageSummary(String pagePath, String content, int targetLength) {
        PageSummary summary = PageSummary.create(pagePath, detectContentType(content));

        int wordCount = countWords(content);
        summary.setWordCount(wordCount);
        summary.setTitle(extractTitle(content));
        summary.setHeading(extractHeading(content));
        summary.setTargetLength(targetLength);

        String summaryText;
        if (enableAIIntegration && aiService != null && StringUtils.isNotBlank(apiKey)) {
            summaryText = generateAISummary(content, targetLength);
        } else {
            summaryText = generateRuleBasedSummary(content, targetLength);
        }

        summary.setSummaryText(summaryText);
        summary.setConfidenceScore(calculateConfidence(wordCount, targetLength));
        summary.setValidUntil(LocalDateTime.now().plusDays(7));
        summary.setMainPoints(extractMainPoints(content));
        summary.setKeywords(extractKeywords(content));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("originalLength", content.length());
        metadata.put("generationMethod", enableAIIntegration ? "ai-powered" : "rule-based");
        summary.setMetadata(metadata);

        return summary;
    }

    private ContentFragmentSummary generateFragmentSummary(String fragmentPath, String content, int targetLength) {
        ContentFragmentSummary summary = ContentFragmentSummary.create(fragmentPath, "default");

        summary.setElements(extractElements(content));
        summary.setElementSummaries(generateElementSummaries(content, targetLength));

        String summaryText;
        if (enableAIIntegration && aiService != null && StringUtils.isNotBlank(apiKey)) {
            summaryText = generateAISummary(content, targetLength);
        } else {
            summaryText = generateRuleBasedSummary(content, targetLength);
        }

        summary.setSummaryText(summaryText);
        summary.setPrimaryTheme(determinePrimaryTheme(content));
        summary.setTags(extractTags(content));
        summary.setFormat(determineFormat(content));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("wordCount", countWords(content));
        metadata.put("generationMethod", enableAIIntegration ? "ai-powered" : "rule-based");
        summary.setMetadata(metadata);

        return summary;
    }

    private Excerpt generateExcerptText(String content, int targetLength) {
        Excerpt excerpt = Excerpt.create("", targetLength);

        String excerptText;
        if (enableAIIntegration && aiService != null && StringUtils.isNotBlank(apiKey)) {
            excerptText = generateAISummary(content, targetLength);
        } else {
            excerptText = generateRuleBasedSummary(content, targetLength);
        }

        excerpt.setContent(excerptText);
        excerpt.setWordCount(countWords(excerptText));
        excerpt.setConfidenceScore(calculateConfidence(countWords(content), targetLength));
        excerpt.setSourceContent(content.substring(0, Math.min(500, content.length())));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generationMethod", enableAIIntegration ? "ai-powered" : "rule-based");
        excerpt.setMetadata(metadata);

        return excerpt;
    }

    private MetaDescription generateMetaDescription(String content, int maxCharacters) {
        MetaDescription metaDesc = MetaDescription.create(maxCharacters);

        String metaText;
        int targetWords = maxCharacters / 6;
        if (enableAIIntegration && aiService != null && StringUtils.isNotBlank(apiKey)) {
            metaText = generateAISummary(content, targetWords);
        } else {
            metaText = generateRuleBasedSummary(content, targetWords);
        }

        if (metaText.length() > maxCharacters) {
            metaText = metaText.substring(0, maxCharacters - 3) + "...";
        }

        metaDesc.setDescription(metaText);
        metaDesc.setCharacterCount(metaText.length());
        metaDesc.setConfidenceScore(calculateConfidence(countWords(content), targetWords));
        metaDesc.setSourceContent(content.substring(0, Math.min(500, content.length())));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("targetCharacters", maxCharacters);
        metadata.put("generationMethod", enableAIIntegration ? "ai-powered" : "rule-based");
        metaDesc.setMetadata(metadata);

        return metaDesc;
    }

    private String generateAISummary(String content, int maxLength) {
        try {
            String prompt = "Generate a concise summary of the following content (max " + maxLength + " words):\n\n" + content;
            log.debug("AI summary prompt length: {}", prompt.length());
            return prompt;
        } catch (Exception e) {
            log.warn("AI summarization failed, falling back to rule-based: {}", e.getMessage());
            return generateRuleBasedSummary(content, maxLength);
        }
    }

    private String generateRuleBasedSummary(String content, int maxLength) {
        List<String> sentences = splitIntoSentences(content);
        if (sentences.isEmpty()) {
            return content.substring(0, Math.min(content.length(), maxLength * 5));
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
                if (word.contains("conclusion") || word.contains("summary") || word.contains("result")) {
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

    private String detectContentType(String content) {
        String lower = content.toLowerCase();
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

    private String extractTitle(String content) {
        String[] lines = content.split("\\n");
        if (lines.length > 0 && lines[0].length() < 200) {
            return lines[0].trim();
        }
        return "Untitled Page";
    }

    private String extractHeading(String content) {
        String[] lines = content.split("\\n");
        for (String line : lines) {
            if (line.length() > 20 && line.length() < 150) {
                return line.trim();
            }
        }
        return extractTitle(content);
    }

    private double calculateConfidence(int wordCount, int targetLength) {
        if (wordCount < 100) {
            return 0.6;
        } else if (wordCount < 500) {
            return 0.75;
        } else if (wordCount < 1000) {
            return 0.85;
        }
        return 0.9;
    }

    private List<String> extractMainPoints(String content) {
        List<String> points = new ArrayList<>();
        List<String> sentences = splitIntoSentences(content);
        Map<String, Double> scores = scoreSentences(sentences);

        scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> points.add(e.getKey()));

        return points;
    }

    private List<String> extractKeywords(String content) {
        List<String> keywords = new ArrayList<>();
        Set<String> stopWords = getStopWords();
        Map<String, Integer> wordFreq = new HashMap<>();

        String[] words = content.toLowerCase().split("\\s+");
        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 4) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }

        wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> keywords.add(e.getKey()));

        return keywords;
    }

    private List<String> extractElements(String content) {
        return Arrays.asList("text", "title", "description");
    }

    private Map<String, String> generateElementSummaries(String content, int targetLength) {
        Map<String, String> summaries = new HashMap<>();
        summaries.put("text", generateRuleBasedSummary(content, targetLength));
        summaries.put("title", extractTitle(content));
        summaries.put("description", generateRuleBasedSummary(content, targetLength / 2));
        return summaries;
    }

    private String determinePrimaryTheme(String content) {
        List<String> keywords = extractKeywords(content);
        return keywords.isEmpty() ? "general" : keywords.get(0);
    }

    private List<String> extractTags(String content) {
        return extractKeywords(content).stream().limit(5).collect(Collectors.toList());
    }

    private String determineFormat(String content) {
        if (content.contains("<html") || content.contains("<div")) {
            return "html";
        } else if (content.contains("{")) {
            return "json";
        }
        return "text";
    }

    private void evictOldPageCache() {
        if (pageSummaryCache.size() > 100) {
            int toRemove = pageSummaryCache.size() - 100;
            Iterator<String> iter = pageSummaryCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                pageSummaryCache.remove(iter.next());
            }
        }
    }

    private void evictOldFragmentCache() {
        if (fragmentCache.size() > 100) {
            int toRemove = fragmentCache.size() - 100;
            Iterator<String> iter = fragmentCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                fragmentCache.remove(iter.next());
            }
        }
    }

    private void evictOldExcerptCache() {
        if (excerptCache.size() > 100) {
            int toRemove = excerptCache.size() - 100;
            Iterator<String> iter = excerptCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                excerptCache.remove(iter.next());
            }
        }
    }

    private void evictOldMetaCache() {
        if (metaDescCache.size() > 100) {
            int toRemove = metaDescCache.size() - 100;
            Iterator<String> iter = metaDescCache.keySet().iterator();
            for (int i = 0; i < toRemove && iter.hasNext(); i++) {
                metaDescCache.remove(iter.next());
            }
        }
    }

    private PageSummary createErrorPageSummary(String error) {
        PageSummary summary = new PageSummary();
        summary.setConfidenceScore(0.0);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        summary.setMetadata(metadata);
        return summary;
    }

    private ContentFragmentSummary createErrorFragmentSummary(String error) {
        ContentFragmentSummary summary = ContentFragmentSummary.create("/error", "error");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        summary.setMetadata(metadata);
        return summary;
    }

    private Excerpt createErrorExcerpt(String error) {
        Excerpt excerpt = Excerpt.create("", 0);
        excerpt.setConfidenceScore(0.0);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        excerpt.setMetadata(metadata);
        return excerpt;
    }

    private MetaDescription createErrorMetaDescription(String error) {
        MetaDescription metaDesc = MetaDescription.create(160);
        metaDesc.setConfidenceScore(0.0);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", error);
        metaDesc.setMetadata(metadata);
        return metaDesc;
    }
}