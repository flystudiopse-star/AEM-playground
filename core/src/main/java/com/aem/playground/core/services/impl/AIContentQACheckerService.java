package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.ContentQACheckerService;
import com.aem.playground.core.services.dto.ContentQAIssue;
import com.aem.playground.core.services.dto.ContentQAReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.playground.core.services.AIContentQACheckerConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component(service = ContentQACheckerService.class)
@Designate(ocd = AIContentQACheckerConfig.class)
public class AIContentQACheckerService implements ContentQACheckerService {

    private static final Logger log = LoggerFactory.getLogger(AIContentQACheckerService.class);

    private static final String BRAND_COLORS_PATTERN = "^#[0-9A-Fa-f]{6}$";
    private static final String ACCESSIBLE_CONTRAST_RATIO = "4.5:1";
    private static final int MIN_CONTENT_LENGTH = 100;
    private static final int MAX_CONTENT_LENGTH = 10000;

    private static final String ACTION_ANALYZE_QUALITY = "Analyze the following AEM content for quality issues such as spelling, grammar, clarity, and completeness.";
    private static final String ACTION_CHECK_BROKEN_LINKS = "Analyze the following content and identify any broken or invalid links.";
    private static final String ACTION_VALIDATE_STRUCTURE = "Validate the content structure and identify issues with heading hierarchy.";
    private static final String ACTION_CHECK_ACCESSIBILITY = "Analyze for accessibility issues including missing alt text and improper heading levels.";
    private static final String ACTION_CHECK_BRAND = "Check for brand consistency issues including incorrect colors and fonts.";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AIContentQACheckerConfig config;
    private AIService aiService;

    private List<String> allowedBrandColors;
    private List<String> allowedFonts;
    private boolean aiEnabled;

    @Reference
    public void setAiService(AIService aiService) {
        this.aiService = aiService;
    }

    @Activate
    @Modified
    public void activate(AIContentQACheckerConfig config) {
        this.config = config;
        this.aiEnabled = config.aiEnabled();
        this.allowedBrandColors = parseStringList(config.allowedBrandColors());
        this.allowedFonts = parseStringList(config.allowedFonts());
        log.info("AIContentQACheckerService activated, AI enabled: {}", aiEnabled);
    }

    @Override
    public ContentQAReport analyzeContent(String content, String contentPath) {
        if (StringUtils.isBlank(content)) {
            return ContentQAReport.builder()
                    .contentPath(contentPath)
                    .status(ContentQAReport.OverallStatus.FAIL)
                    .overallScore(0)
                    .addIssue(ContentQAIssue.builder()
                            .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                            .severity(ContentQAIssue.Severity.CRITICAL)
                            .title("Empty Content")
                            .description("Content is empty or null")
                            .location(contentPath)
                            .suggestion("Add content to analyze")
                            .build())
                    .build();
        }

        List<ContentQAIssue> issues = new ArrayList<>();

        issues.addAll(checkContentLength(content, contentPath));
        issues.addAll(checkFormatting(content, contentPath));
        issues.addAll(checkPlaceholderContent(content, contentPath));

        if (aiEnabled && aiService != null) {
            List<ContentQAIssue> aiIssues = getAIAnalysis(content, contentPath, ACTION_ANALYZE_QUALITY, ContentQAIssue.IssueType.CONTENT_QUALITY);
            issues.addAll(aiIssues);
        }

        return buildReport(content, contentPath, issues);
    }

    @Override
    public ContentQAReport checkBrokenLinks(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        issues.addAll(findBrokenLinks(content, contentPath));

        if (aiEnabled && aiService != null) {
            List<ContentQAIssue> aiIssues = getAIAnalysis(content, contentPath, ACTION_CHECK_BROKEN_LINKS, ContentQAIssue.IssueType.BROKEN_LINK);
            issues.addAll(aiIssues);
        }

        return buildReport(content, contentPath, issues);
    }

    @Override
    public ContentQAReport validateStructure(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        issues.addAll(checkHeadingHierarchy(content, contentPath));
        issues.addAll(checkParagraphFlow(content, contentPath));

        if (aiEnabled && aiService != null) {
            List<ContentQAIssue> aiIssues = getAIAnalysis(content, contentPath, ACTION_VALIDATE_STRUCTURE, ContentQAIssue.IssueType.STRUCTURE);
            issues.addAll(aiIssues);
        }

        return buildReport(content, contentPath, issues);
    }

    @Override
    public ContentQAReport checkAccessibility(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        issues.addAll(checkAltText(content, contentPath));
        issues.addAll(checkHeadingLevels(content, contentPath));
        issues.addAll(checkLinkText(content, contentPath));

        if (aiEnabled && aiService != null) {
            List<ContentQAIssue> aiIssues = getAIAnalysis(content, contentPath, ACTION_CHECK_ACCESSIBILITY, ContentQAIssue.IssueType.ACCESSIBILITY);
            issues.addAll(aiIssues);
        }

        return buildReport(content, contentPath, issues);
    }

    @Override
    public ContentQAReport checkBrandConsistency(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        issues.addAll(checkBrandColors(content, contentPath));
        issues.addAll(checkBrandFonts(content, contentPath));
        issues.addAll(checkBrandTone(content, contentPath));

        if (aiEnabled && aiService != null) {
            List<ContentQAIssue> aiIssues = getAIAnalysis(content, contentPath, ACTION_CHECK_BRAND, ContentQAIssue.IssueType.BRAND_CONSISTENCY);
            issues.addAll(aiIssues);
        }

        return buildReport(content, contentPath, issues);
    }

    @Override
    public ContentQAReport generateFullReport(String content, String contentPath) {
        List<ContentQAIssue> allIssues = new ArrayList<>();

        ContentQAReport qualityReport = analyzeContent(content, contentPath);
        allIssues.addAll(qualityReport.getIssues());

        ContentQAReport linkReport = checkBrokenLinks(content, contentPath);
        allIssues.addAll(linkReport.getIssues());

        ContentQAReport structureReport = validateStructure(content, contentPath);
        allIssues.addAll(structureReport.getIssues());

        ContentQAReport accessibilityReport = checkAccessibility(content, contentPath);
        allIssues.addAll(accessibilityReport.getIssues());

        ContentQAReport brandReport = checkBrandConsistency(content, contentPath);
        allIssues.addAll(brandReport.getIssues());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("qualityIssues", qualityReport.getTotalIssues());
        metadata.put("linkIssues", linkReport.getTotalIssues());
        metadata.put("structureIssues", structureReport.getTotalIssues());
        metadata.put("accessibilityIssues", accessibilityReport.getTotalIssues());
        metadata.put("brandIssues", brandReport.getTotalIssues());

        List<String> recommendations = generateRecommendations(allIssues);

        return ContentQAReport.builder()
                .contentPath(contentPath)
                .contentTitle(extractTitle(content))
                .issues(allIssues)
                .metadata(metadata)
                .recommendations(recommendations)
                .build();
    }

    private List<ContentQAIssue> checkContentLength(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        if (content.length() < MIN_CONTENT_LENGTH) {
            issues.add(ContentQAIssue.builder()
                    .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                    .severity(ContentQAIssue.Severity.MEDIUM)
                    .title("Content Too Short")
                    .description("Content length is below minimum recommended length of " + MIN_CONTENT_LENGTH + " characters")
                    .location(contentPath)
                    .suggestion("Add more content to improve SEO and user engagement")
                    .build());
        } else if (content.length() > MAX_CONTENT_LENGTH) {
            issues.add(ContentQAIssue.builder()
                    .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                    .severity(ContentQAIssue.Severity.LOW)
                    .title("Content Too Long")
                    .description("Content length exceeds maximum recommended length of " + MAX_CONTENT_LENGTH + " characters")
                    .location(contentPath)
                    .suggestion("Consider splitting content into multiple pages")
                    .build());
        }

        return issues;
    }

    private List<ContentQAIssue> checkFormatting(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        if (StringUtils.countMatches(content, "\n\n") < 1) {
            issues.add(ContentQAIssue.builder()
                    .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                    .severity(ContentQAIssue.Severity.LOW)
                    .title("Poor Formatting")
                    .description("Content lacks paragraph breaks")
                    .location(contentPath)
                    .suggestion("Add paragraph breaks to improve readability")
                    .build());
        }

        return issues;
    }

    private List<ContentQAIssue> checkPlaceholderContent(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        String[] placeholders = {"lorem ipsum", "placeholder", "write content here", "add text"};
        String lowerContent = content.toLowerCase();

        for (String placeholder : placeholders) {
            if (lowerContent.contains(placeholder)) {
                issues.add(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.CONTENT_QUALITY)
                        .severity(ContentQAIssue.Severity.HIGH)
                        .title("Placeholder Content Detected")
                        .description("Found placeholder text: '" + placeholder + "'")
                        .location(contentPath)
                        .suggestion("Replace placeholder content with actual content")
                        .metadata(Map.of("placeholder", placeholder))
                        .build());
            }
        }

        return issues;
    }

    private List<ContentQAIssue> findBrokenLinks(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        Pattern linkPattern = Pattern.compile("href=[\"']([^\"']+)[\"']");
        Matcher matcher = linkPattern.matcher(content);

        while (matcher.find()) {
            String url = matcher.group(1);

            if (url.startsWith("/") && !url.contains(" ")) {
                continue;
            }

            if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("#")) {
                continue;
            }

            if (url.contains(" ") || url.contains("<") || url.contains(">")) {
                issues.add(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.BROKEN_LINK)
                        .severity(ContentQAIssue.Severity.HIGH)
                        .title("Malformed Link")
                        .description("Link appears to be malformed: " + url)
                        .location(contentPath)
                        .suggestion("Fix the link URL")
                        .metadata(Map.of("url", url))
                        .build());
            }
        }

        return issues;
    }

    private List<ContentQAIssue> checkHeadingHierarchy(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        boolean hasH1 = content.contains("<h1") || content.contains("# ");
        boolean hasH2 = content.contains("<h2") || content.contains("## ");
        boolean hasH3 = content.contains("<h3") || content.contains("### ");

        if (!hasH1 && (hasH2 || hasH3)) {
            issues.add(ContentQAIssue.builder()
                    .type(ContentQAIssue.IssueType.STRUCTURE)
                    .severity(ContentQAIssue.Severity.MEDIUM)
                    .title("Missing Heading h1")
                    .description("Content has h2 or h3 headings but no h1 heading")
                    .location(contentPath)
                    .suggestion("Add a main h1 heading to the content")
                    .build());
        }

        if (hasH1 && hasH3 && !hasH2) {
            issues.add(ContentQAIssue.builder()
                    .type(ContentQAIssue.IssueType.STRUCTURE)
                    .severity(ContentQAIssue.Severity.LOW)
                    .title("Skipped Heading Level")
                    .description("Content has h1 and h3 headings but skips h2")
                    .location(contentPath)
                    .suggestion("Add h2 headings between h1 and h3")
                    .build());
        }

        return issues;
    }

    private List<ContentQAIssue> checkParagraphFlow(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        String[] paragraphs = content.split("\n\n");
        if (paragraphs.length > 1) {
            for (int i = 0; i < paragraphs.length; i++) {
                if (paragraphs[i].length() > 500) {
                    issues.add(ContentQAIssue.builder()
                            .type(ContentQAIssue.IssueType.STRUCTURE)
                            .severity(ContentQAIssue.Severity.LOW)
                            .title("Long Paragraph")
                            .description("Paragraph " + (i + 1) + " exceeds 500 characters")
                            .location(contentPath + " - Paragraph " + (i + 1))
                            .suggestion("Break this paragraph into smaller chunks")
                            .build());
                }
            }
        }

        return issues;
    }

    private List<ContentQAIssue> checkAltText(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        Pattern imgPattern = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher imgMatcher = imgPattern.matcher(content);

        while (imgMatcher.find()) {
            String imgTag = imgMatcher.group();
            if (!imgTag.toLowerCase().contains("alt=")) {
                issues.add(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.ACCESSIBILITY)
                        .severity(ContentQAIssue.Severity.HIGH)
                        .title("Missing Alt Text")
                        .description("Image is missing alt attribute")
                        .location(contentPath)
                        .suggestion("Add alt text to describe the image")
                        .metadata(Map.of("imageTag", imgTag))
                        .build());
            }
        }

        return issues;
    }

    private List<ContentQAIssue> checkHeadingLevels(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        int maxHeadingLevel = 0;
        for (int i = 1; i <= 6; i++) {
            if (content.contains("<h" + i) || content.contains("##".repeat(i))) {
                maxHeadingLevel = i;
            }
        }

        if (maxHeadingLevel > 4) {
            issues.add(ContentQAIssue.builder()
                    .type(ContentQAIssue.IssueType.ACCESSIBILITY)
                    .severity(ContentQAIssue.Severity.LOW)
                    .title("Deep Heading Nesting")
                    .description("Content uses heading level h" + maxHeadingLevel + ", which may be too deep")
                    .location(contentPath)
                    .suggestion("Consider restructuring with shallower heading levels")
                    .build());
        }

        return issues;
    }

    private List<ContentQAIssue> checkLinkText(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        Pattern linkPattern = Pattern.compile("<a[^>]*href=[\"']([^\"']+)[\"'][^>]*>([^<]*)</a>", Pattern.CASE_INSENSITIVE);
        Matcher linkMatcher = linkPattern.matcher(content);

        while (linkMatcher.find()) {
            String linkText = linkMatcher.group(2).trim().toLowerCase();
            if (linkText.equals("click here") || linkText.equals("link") || linkText.isEmpty()) {
                issues.add(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.ACCESSIBILITY)
                        .severity(ContentQAIssue.Severity.MEDIUM)
                        .title("Non-Descriptive Link Text")
                        .description("Link text '" + linkMatcher.group(2) + "' is not descriptive")
                        .location(contentPath)
                        .suggestion("Use descriptive link text that indicates the destination")
                        .build());
            }
        }

        return issues;
    }

    private List<ContentQAIssue> checkBrandColors(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        if (allowedBrandColors == null || allowedBrandColors.isEmpty()) {
            return issues;
        }

        Pattern colorPattern = Pattern.compile("#[0-9A-Fa-f]{6}");
        Matcher colorMatcher = colorPattern.matcher(content);

        while (colorMatcher.find()) {
            String color = colorMatcher.group();
            boolean isAllowed = allowedBrandColors.stream()
                    .anyMatch(c -> c.equalsIgnoreCase(color));

            if (!isAllowed) {
                issues.add(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.BRAND_CONSISTENCY)
                        .severity(ContentQAIssue.Severity.MEDIUM)
                        .title("Non-Brand Color")
                        .description("Found color " + color + " which is not in approved brand colors")
                        .location(contentPath)
                        .suggestion("Use brand-approved colors: " + String.join(", ", allowedBrandColors))
                        .metadata(Map.of("color", color))
                        .build());
            }
        }

        return issues;
    }

    private List<ContentQAIssue> checkBrandFonts(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        if (allowedFonts == null || allowedFonts.isEmpty()) {
            return issues;
        }

        Pattern fontPattern = Pattern.compile("font-family:[^;]+", Pattern.CASE_INSENSITIVE);
        Matcher fontMatcher = fontPattern.matcher(content);

        while (fontMatcher.find()) {
            String fontBlock = fontMatcher.group();
            boolean isAllowed = allowedFonts.stream()
                    .anyMatch(f -> fontBlock.contains(f));

            if (!isAllowed) {
                issues.add(ContentQAIssue.builder()
                        .type(ContentQAIssue.IssueType.BRAND_CONSISTENCY)
                        .severity(ContentQAIssue.Severity.MEDIUM)
                        .title("Non-Brand Font")
                        .description("Found non-approved font in: " + fontBlock)
                        .location(contentPath)
                        .suggestion("Use brand-approved fonts: " + String.join(", ", allowedFonts))
                        .build());
            }
        }

        return issues;
    }

    private List<ContentQAIssue> checkBrandTone(String content, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        String lowerContent = content.toLowerCase();

        if (lowerContent.contains("sorry") && lowerContent.contains("for")) {
            issues.add(ContentQAIssue.builder()
                    .type(ContentQAIssue.IssueType.BRAND_CONSISTENCY)
                    .severity(ContentQAIssue.Severity.LOW)
                    .title("Apologetic Tone")
                    .description("Content may contain overly apologetic language")
                    .location(contentPath)
                    .suggestion("Use confident, positive language instead")
                    .build());
        }

        return issues;
    }

    private List<ContentQAIssue> getAIAnalysis(String content, String contentPath, String action, ContentQAIssue.IssueType issueType) {
        List<ContentQAIssue> issues = new ArrayList<>();

        try {
            String prompt = action + "\n\nContent:\n" + content;

            AIGenerationOptions options = AIGenerationOptions.builder()
                    .maxTokens(2000)
                    .temperature(0.3f)
                    .build();

            AIService.AIGenerationResult result = aiService.generateText(prompt, options);

            if (result.isSuccess()) {
                String response = result.getContent();
                issues.addAll(parseAIResponse(response, issueType, contentPath));
            }
        } catch (Exception e) {
            log.error("AI analysis failed for {}: {}", contentPath, e.getMessage());
        }

        return issues;
    }

    private List<ContentQAIssue> parseAIResponse(String response, ContentQAIssue.IssueType defaultType, String contentPath) {
        List<ContentQAIssue> issues = new ArrayList<>();

        try {
            if (response == null || response.isBlank()) {
                return issues;
            }

            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.length() > 10 && !line.startsWith("Here's")) {
                    issues.add(ContentQAIssue.builder()
                            .type(defaultType)
                            .severity(ContentQAIssue.Severity.MEDIUM)
                            .title("AI Detected Issue")
                            .description(line)
                            .location(contentPath)
                            .suggestion("Review and address this issue")
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
        }

        return issues;
    }

    private ContentQAReport buildReport(String content, String contentPath, List<ContentQAIssue> issues) {
        return ContentQAReport.builder()
                .contentPath(contentPath)
                .contentTitle(extractTitle(content))
                .issues(issues)
                .build();
    }

    private List<String> generateRecommendations(List<ContentQAIssue> issues) {
        List<String> recommendations = new ArrayList<>();

        long criticalCount = issues.stream()
                .filter(i -> i.getSeverity() == ContentQAIssue.Severity.CRITICAL)
                .count();
        long highCount = issues.stream()
                .filter(i -> i.getSeverity() == ContentQAIssue.Severity.HIGH)
                .count();

        if (criticalCount > 0) {
            recommendations.add("Address " + criticalCount + " critical issues before publishing");
        }
        if (highCount > 0) {
            recommendations.add("Review " + highCount + " high priority issues");
        }
        if (issues.isEmpty()) {
            recommendations.add("Content passes all QA checks - ready for review");
        }

        return recommendations;
    }

    private String extractTitle(String content) {
        if (content == null) {
            return "Untitled";
        }

        Pattern titlePattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
        Matcher titleMatcher = titlePattern.matcher(content);
        if (titleMatcher.find()) {
            return titleMatcher.group(1);
        }

        String[] lines = content.split("\n");
        if (lines.length > 0 && lines[0].length() > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.length() <= 100) {
                return firstLine;
            }
        }

        return "Content";
    }

    private List<String> parseStringList(String value) {
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}