package com.aem.playground.core.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(service = SEOOptimizerService.class)
@Designate(ocd = SEOOptimizerServiceConfig.class)
public class SEOOptimizerServiceImpl implements SEOOptimizerService {

    private static final Logger log = LoggerFactory.getLogger(SEOOptimizerServiceImpl.class);

    private static final int MIN_TITLE_LENGTH = 30;
    private static final int MAX_TITLE_LENGTH = 60;
    private static final int MIN_DESCRIPTION_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 160;
    private static final int MIN_KEYWORD_COUNT = 3;
    private static final int MAX_KEYWORD_COUNT = 10;

    private static final String DEFAULT_CHANGEFREQ = "weekly";
    private static final String DEFAULT_PRIORITY = "0.5";

    @Reference
    private AIService aiService;

    private String defaultLanguage;
    private boolean generateSchemaEnabled;
    private boolean openGraphEnabled;
    private boolean twitterCardsEnabled;
    private int cacheMaxSize;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Pattern titleExtractionPattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
    private final Pattern descriptionExtractionPattern = Pattern.compile("<meta[^>]+name=\"description\"[^>]+content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    @Activate
    protected void activate(SEOOptimizerServiceConfig config) {
        this.defaultLanguage = PropertiesUtil.toString(config.defaultLanguage(), "en");
        this.generateSchemaEnabled = config.generateSchemaEnabled();
        this.openGraphEnabled = config.openGraphEnabled();
        this.twitterCardsEnabled = config.twitterCardsEnabled();
        this.cacheMaxSize = config.cacheMaxSize();
        log.info("SEOOptimizerService activated with language: {}", defaultLanguage);
    }

    @Override
    public SEOMetadata generateMetadata(String pageContent, String pageTitle, String pagePath) {
        return generateMetadata(pageContent, pageTitle, pagePath, defaultLanguage);
    }

    @Override
    public SEOMetadata generateMetadata(String pageContent, String pageTitle, String pagePath, String language) {
        SEOMetadata metadata = new SEOMetadata();

        if (StringUtils.isBlank(pageContent) && StringUtils.isBlank(pageTitle)) {
            return metadata;
        }

        try {
            String prompt = buildSEOGenerationPrompt(pageContent, pageTitle, pagePath, language);
            AIGenerationOptions options = AIGenerationOptions.builder()
                    .maxTokens(1000)
                    .temperature(0.7f)
                    .build();

            AIService.AIGenerationResult result = aiService.generateText(prompt, options);

            if (result.isSuccess()) {
                String seoJson = result.getContent();
                parseSEOFromAIResponse(seoJson, metadata);
            } else {
                log.warn("AI generation failed: {}", result.getError());
                generateFallbackMetadata(pageContent, pageTitle, pagePath, metadata);
            }
        } catch (Exception e) {
            log.error("Error generating SEO metadata: {}", e.getMessage());
            generateFallbackMetadata(pageContent, pageTitle, pagePath, metadata);
        }

        if (StringUtils.isNotBlank(pagePath)) {
            metadata.setOgUrl(pagePath);
        }

        if (openGraphEnabled) {
            applyOpenGraphDefaults(metadata, pageTitle, pagePath);
        }

        if (twitterCardsEnabled) {
            applyTwitterCardDefaults(metadata, pageTitle, pagePath);
        }

        if (generateSchemaEnabled) {
            String schemaJsonLd = generateSchemaOrgJsonLd(
                    metadata.getMetaTitle(),
                    metadata.getMetaDescription(),
                    pagePath,
                    "WebPage"
            );
            metadata.setSchemaOrgJsonLd(schemaJsonLd);
        }

        calculateSeoScore(metadata);

        return metadata;
    }

    private String buildSEOGenerationPrompt(String pageContent, String pageTitle, String pagePath, String language) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate SEO metadata for the following web page content. ");
        prompt.append("Return ONLY a valid JSON object with these exact fields: ");
        prompt.append("metaTitle (50-60 chars), metaDescription (50-160 chars), ");
        prompt.append("keywords (array of 3-10 relevant keywords), ogType (website or article). ");
        prompt.append("Use language '").append(language).append("' for the content.\n\n");

        if (StringUtils.isNotBlank(pageTitle)) {
            prompt.append("Page Title: ").append(pageTitle).append("\n");
        }

        if (StringUtils.isNotBlank(pageContent)) {
            String truncatedContent = pageContent.length() > 2000 
                    ? pageContent.substring(0, 2000) 
                    : pageContent;
            prompt.append("Page Content: ").append(truncatedContent);
        }

        return prompt.toString();
    }

    private void parseSEOFromAIResponse(String seoJson, SEOMetadata metadata) {
        try {
            JsonNode jsonNode = objectMapper.readTree(seoJson);
            metadata.setMetaTitle(getTextValue(jsonNode, "metaTitle"));
            metadata.setMetaDescription(getTextValue(jsonNode, "metaDescription"));
            metadata.setOgType(getTextValue(jsonNode, "ogType", "website"));

            JsonNode keywordsNode = jsonNode.get("keywords");
            if (keywordsNode != null && keywordsNode.isArray()) {
                List<String> keywords = new ArrayList<>();
                for (JsonNode keyword : keywordsNode) {
                    keywords.add(keyword.asText());
                }
                metadata.setKeywords(keywords);
            }
        } catch (Exception e) {
            log.error("Error parsing SEO response: {}", e.getMessage());
        }
    }

    private String getTextValue(JsonNode node, String field) {
        return getTextValue(node, field, null);
    }

    private String getTextValue(JsonNode node, String field, String defaultValue) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null ? fieldNode.asText() : defaultValue;
    }

    private void generateFallbackMetadata(String pageContent, String pageTitle, String pagePath, SEOMetadata metadata) {
        if (StringUtils.isNotBlank(pageTitle)) {
            String title = pageTitle.length() > MAX_TITLE_LENGTH 
                    ? pageTitle.substring(0, MAX_TITLE_LENGTH) 
                    : pageTitle;
            metadata.setMetaTitle(title);
        }

        if (StringUtils.isNotBlank(pageContent)) {
            String description = extractDescription(pageContent);
            metadata.setMetaDescription(description);
        }

        List<String> keywords = extractKeywordsFromContent(pageContent);
        metadata.setKeywords(keywords);
    }

    private String extractDescription(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        String cleaned = content.replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.length() > MAX_DESCRIPTION_LENGTH) {
            return cleaned.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
        }
        return cleaned;
    }

    private List<String> extractKeywordsFromContent(String content) {
        List<String> keywords = new ArrayList<>();
        
        if (StringUtils.isBlank(content)) {
            return keywords;
        }

        String[] words = content.toLowerCase()
                .replaceAll("<[^>]+>", " ")
                .replaceAll("[^a-z\\s]", " ")
                .split("\\s+");

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            if (word.length() > 4) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        wordCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(MAX_KEYWORD_COUNT)
                .forEach(e -> keywords.add(e.getKey()));

        return keywords;
    }

    private void applyOpenGraphDefaults(SEOMetadata metadata, String pageTitle, String pageUrl) {
        if (StringUtils.isBlank(metadata.getOgTitle())) {
            metadata.setOgTitle(StringUtils.defaultString(metadata.getMetaTitle(), pageTitle));
        }
        if (StringUtils.isBlank(metadata.getOgDescription())) {
            metadata.setOgDescription(metadata.getMetaDescription());
        }
        if (StringUtils.isBlank(metadata.getOgUrl())) {
            metadata.setOgUrl(pageUrl);
        }
        if (StringUtils.isBlank(metadata.getOgType())) {
            metadata.setOgType("website");
        }
    }

    private void applyTwitterCardDefaults(SEOMetadata metadata, String pageTitle, String pageUrl) {
        metadata.setTwitterCard("summary_large_image");
        if (StringUtils.isBlank(metadata.getTwitterTitle())) {
            metadata.setTwitterTitle(StringUtils.defaultString(metadata.getMetaTitle(), pageTitle));
        }
        if (StringUtils.isBlank(metadata.getTwitterDescription())) {
            metadata.setTwitterDescription(metadata.getMetaDescription());
        }
    }

    @Override
    public SEOMetadata calculateSeoScore(SEOMetadata metadata) {
        int score = 0;

        if (StringUtils.isNotBlank(metadata.getMetaTitle())) {
            int titleLength = metadata.getMetaTitle().length();
            if (titleLength >= MIN_TITLE_LENGTH && titleLength <= MAX_TITLE_LENGTH) {
                score += 20;
                metadata.addSeoRecommendation("Title length is optimal");
            } else if (titleLength < MIN_TITLE_LENGTH) {
                metadata.addSeoRecommendation("Title is too short (minimum " + MIN_TITLE_LENGTH + " characters)");
            } else {
                metadata.addSeoRecommendation("Title is too long (maximum " + MAX_TITLE_LENGTH + " characters)");
            }
        } else {
            metadata.addSeoRecommendation("Missing meta title");
        }

        if (StringUtils.isNotBlank(metadata.getMetaDescription())) {
            int descLength = metadata.getMetaDescription().length();
            if (descLength >= MIN_DESCRIPTION_LENGTH && descLength <= MAX_DESCRIPTION_LENGTH) {
                score += 20;
                metadata.addSeoRecommendation("Description length is optimal");
            } else if (descLength < MIN_DESCRIPTION_LENGTH) {
                metadata.addSeoRecommendation("Description is too short (minimum " + MIN_DESCRIPTION_LENGTH + " characters)");
            } else {
                metadata.addSeoRecommendation("Description is too long (maximum " + MAX_DESCRIPTION_LENGTH + " characters)");
            }
        } else {
            metadata.addSeoRecommendation("Missing meta description");
        }

        List<String> keywords = metadata.getKeywords();
        if (keywords != null && keywords.size() >= MIN_KEYWORD_COUNT) {
            score += 20;
            metadata.addSeoRecommendation("Have " + keywords.size() + " keywords (recommended: " + MIN_KEYWORD_COUNT + "+)");
        } else {
            metadata.addSeoRecommendation("Insufficient keywords (minimum " + MIN_KEYWORD_COUNT + " recommended)");
        }

        if (StringUtils.isNotBlank(metadata.getOgTitle()) && StringUtils.isNotBlank(metadata.getOgDescription())) {
            score += 20;
            metadata.addSeoRecommendation("OpenGraph metadata present");
        } else {
            metadata.addSeoRecommendation("Missing OpenGraph metadata");
        }

        if (StringUtils.isNotBlank(metadata.getTwitterCard())) {
            score += 10;
            metadata.addSeoRecommendation("Twitter Card metadata present");
        } else {
            metadata.addSeoRecommendation("Missing Twitter Card metadata");
        }

        if (StringUtils.isNotBlank(metadata.getSchemaOrgJsonLd())) {
            score += 10;
            metadata.addSeoRecommendation("Schema.org structured data present");
        } else {
            metadata.addSeoRecommendation("Missing Schema.org structured data");
        }

        metadata.setSeoScore(score);
        return metadata;
    }

    @Override
    public String generateSchemaOrgJsonLd(String pageTitle, String pageDescription, String pageUrl, String pageType) {
        try {
            Map<String, Object> schema = new HashMap<>();
            schema.put("@context", "https://schema.org");
            schema.put("@type", StringUtils.defaultIfBlank(pageType, "WebPage"));
            schema.put("name", pageTitle);
            schema.put("description", pageDescription);
            if (StringUtils.isNotBlank(pageUrl)) {
                Map<String, Object> publisher = new HashMap<>();
                publisher.put("@type", "Organization");
                publisher.put("name", pageTitle);
                schema.put("publisher", publisher);
            }

            return objectMapper.writeValueAsString(schema);
        } catch (Exception e) {
            log.error("Error generating schema.org JSON-LD: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public SEOMetadata generateOpenGraphMetadata(String pageContent, String pageTitle, String pageUrl) {
        SEOMetadata metadata = new SEOMetadata();
        
        metadata.setOgTitle(StringUtils.defaultString(
                extractOGTitle(pageContent, pageTitle), 
                pageTitle));
        metadata.setOgDescription(extractOGDescription(pageContent));
        metadata.setOgUrl(pageUrl);
        metadata.setOgType("website");
        metadata.setOgImage("");

        return metadata;
    }

    private String extractOGTitle(String content, String defaultTitle) {
        if (StringUtils.isNotBlank(content)) {
            Matcher matcher = titleExtractionPattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return defaultTitle;
    }

    private String extractOGDescription(String content) {
        if (StringUtils.isNotBlank(content)) {
            Matcher matcher = descriptionExtractionPattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    @Override
    public SEOMetadata generateTwitterCardMetadata(String pageContent, String pageTitle, String pageUrl) {
        SEOMetadata metadata = new SEOMetadata();
        
        metadata.setTwitterCard("summary_large_image");
        metadata.setTwitterTitle(StringUtils.defaultString(pageTitle));
        metadata.setTwitterDescription(extractOGDescription(pageContent));
        metadata.setTwitterImage("");

        return metadata;
    }

    @Override
    public String generateSitemapXml(List<SitemapEntry> entries) {
        return generateSitemapXml(entries, "");
    }

    @Override
    public String generateSitemapXml(List<SitemapEntry> entries, String baseUrl) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        if (entries == null || entries.isEmpty()) {
            xml.append("</urlset>");
            return xml.toString();
        }

        for (SitemapEntry entry : entries) {
            xml.append("  <url>\n");
            
            String loc = entry.getLoc();
            if (StringUtils.isNotBlank(baseUrl) && !loc.startsWith("http")) {
                loc = baseUrl + loc;
            }
            xml.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");

            if (entry.getLastmod() != null) {
                String lastmod = entry.getLastmod().format(DateTimeFormatter.ISO_DATE_TIME);
                xml.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
            }

            if (StringUtils.isNotBlank(entry.getChangefreq())) {
                xml.append("    <changefreq>").append(entry.getChangefreq()).append("</changefreq>\n");
            } else {
                xml.append("    <changefreq>").append(DEFAULT_CHANGEFREQ).append("</changefreq>\n");
            }

            if (StringUtils.isNotBlank(entry.getPriority())) {
                xml.append("    <priority>").append(entry.getPriority()).append("</priority>\n");
            } else {
                xml.append("    <priority>").append(DEFAULT_PRIORITY).append("</priority>\n");
            }

            xml.append("  </url>\n");
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public boolean isGenerateSchemaEnabled() {
        return generateSchemaEnabled;
    }

    public boolean isOpenGraphEnabled() {
        return openGraphEnabled;
    }

    public boolean isTwitterCardsEnabled() {
        return twitterCardsEnabled;
    }
}