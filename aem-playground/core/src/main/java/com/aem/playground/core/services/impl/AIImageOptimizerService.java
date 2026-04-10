package com.aem.playground.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.ImageOptimizerConfig;
import com.aem.playground.core.services.ImageOptimizerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = ImageOptimizerService.class)
@Designate(ocd = ImageOptimizerConfig.class)
public class AIImageOptimizerService implements ImageOptimizerService {

    private static final Logger log = LoggerFactory.getLogger(AIImageOptimizerService.class);

    private static final String DEFAULT_VISION_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_VISION_MODEL = "gpt-4o";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedAnalysis> analysisCache = new ConcurrentHashMap<>();

    private String apiKey;
    private String visionEndpoint;
    private String visionModel;
    private int defaultQuality;
    private int maxWidth;
    private int maxHeight;
    private String defaultFormat;
    private boolean generateResponsiveVariants;
    private List<Integer> breakpoints;
    private String altTextPrompt;
    private boolean enableCaching;
    private int cacheSize;

    @Reference
    private AIService aiService;

    public void bindAiService(AIService aiService) {
        this.aiService = aiService;
    }

    @Activate
    @Modified
    protected void activate(ImageOptimizerConfig config) {
        this.apiKey = config.apiKey();
        this.visionEndpoint = PropertiesUtil.toString(config.visionEndpoint(), DEFAULT_VISION_ENDPOINT);
        this.visionModel = PropertiesUtil.toString(config.visionModel(), DEFAULT_VISION_MODEL);
        this.defaultQuality = config.defaultQuality();
        this.maxWidth = config.maxWidth();
        this.maxHeight = config.maxHeight();
        this.defaultFormat = config.defaultFormat();
        this.generateResponsiveVariants = config.generateResponsiveVariants();
        this.breakpoints = parseBreakpoints(config.breakpoints());
        this.altTextPrompt = config.altTextPrompt();
        this.enableCaching = config.enableCaching();
        this.cacheSize = config.cacheSize();
        log.info("AIImageOptimizerService activated with model: {}", visionModel);
    }

    private List<Integer> parseBreakpoints(String breakpointsStr) {
        List<Integer> result = new ArrayList<>();
        if (StringUtils.isNotBlank(breakpointsStr)) {
            String[] parts = breakpointsStr.split(",");
            for (String part : parts) {
                try {
                    result.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid breakpoint value: {}", part);
                }
            }
        }
        if (result.isEmpty()) {
            result.addAll(Arrays.asList(320, 768, 1024, 1920));
        }
        return result;
    }

    @Override
    public ImageAnalysisResult analyzeImage(String imagePath) {
        if (StringUtils.isBlank(imagePath)) {
            return ImageAnalysisResult.builder()
                .imagePath(imagePath)
                .qualityScore(0)
                .build();
        }

        if (enableCaching) {
            CachedAnalysis cached = analysisCache.get(imagePath);
            if (cached != null) {
                log.debug("Cache hit for image analysis: {}", imagePath);
                return cached.result;
            }
        }

        try {
            String analysisPrompt = "Analyze this image and provide details: width, height, format, file size estimate, " +
                "dominant colors, whether it has transparency, list of detected objects, scene description, " +
                "and a quality score from 0-100. Respond in JSON format.";

            String response = callVisionApi(imagePath, analysisPrompt);

            ImageAnalysisResult result = parseAnalysisResponse(imagePath, response);

            if (enableCaching) {
                analysisCache.put(imagePath, new CachedAnalysis(result));
                evictOldCacheEntries();
            }

            return result;

        } catch (Exception e) {
            log.error("Error analyzing image: {}", e.getMessage());
            return ImageAnalysisResult.builder()
                .imagePath(imagePath)
                .qualityScore(0)
                .build();
        }
    }

    @Override
    public List<OptimizationSuggestion> getOptimizationSuggestions(String imagePath) {
        List<OptimizationSuggestion> suggestions = new ArrayList<>();
        ImageAnalysisResult analysis = analyzeImage(imagePath);

        if (analysis.getWidth() > maxWidth) {
            suggestions.add(OptimizationSuggestion.builder()
                .type("RESIZE")
                .description("Image width " + analysis.getWidth() + "px exceeds recommended max " + maxWidth + "px")
                .action("Resize to " + maxWidth + "px width")
                .estimatedSavings(calculateResizeSavings(analysis.getWidth(), maxWidth))
                .priority(OptimizationSuggestion.Priority.HIGH)
                .build());
        }

        if (analysis.getHeight() > maxHeight) {
            suggestions.add(OptimizationSuggestion.builder()
                .type("RESIZE")
                .description("Image height " + analysis.getHeight() + "px exceeds recommended max " + maxHeight + "px")
                .action("Resize to " + maxHeight + "px height")
                .estimatedSavings(calculateResizeSavings(analysis.getHeight(), maxHeight))
                .priority(OptimizationSuggestion.Priority.HIGH)
                .build());
        }

        String format = analysis.getFormat();
        if (format != null && !format.equalsIgnoreCase("webp")) {
            suggestions.add(OptimizationSuggestion.builder()
                .type("FORMAT")
                .description("Convert from " + format + " to WebP for better compression")
                .action("Convert to WebP format")
                .estimatedSavings(0.35)
                .priority(OptimizationSuggestion.Priority.MEDIUM)
                .build());
        }

        if (analysis.getFileSize() > 500000) {
            suggestions.add(OptimizationSuggestion.builder()
                .type("COMPRESSION")
                .description("File size " + analysis.getFileSize() + " bytes is large, apply compression")
                .action("Apply lossy compression at " + defaultQuality + "% quality")
                .estimatedSavings(0.40)
                .priority(OptimizationSuggestion.Priority.MEDIUM)
                .build());
        }

        if (analysis.hasTransparency() && "png".equalsIgnoreCase(format)) {
            suggestions.add(OptimizationSuggestion.builder()
                .type("TRANSPARENCY")
                .description("PNG with transparency, consider WebP for better compression")
                .action("Convert to WebP with alpha channel")
                .estimatedSavings(0.25)
                .priority(OptimizationSuggestion.Priority.LOW)
                .build());
        }

        return suggestions;
    }

    @Override
    public String generateAltText(String imagePath) {
        if (StringUtils.isBlank(imagePath)) {
            return "";
        }

        try {
            String prompt = altTextPrompt != null ? altTextPrompt :
                "Describe this image for accessibility purposes in one concise sentence.";
            return callVisionApi(imagePath, prompt);
        } catch (Exception e) {
            log.error("Error generating alt text: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public CompressionSuggestion getCompressionSuggestion(String imagePath) {
        ImageAnalysisResult analysis = analyzeImage(imagePath);

        String recommendedFormat = "webp";
        if (analysis.hasTransparency()) {
            recommendedFormat = "png";
        } else if (analysis.getFormat() != null && analysis.getFormat().equalsIgnoreCase("jpeg")) {
            double ratio = analysis.getFileSize() / (double)(analysis.getWidth() * analysis.getHeight());
            if (ratio > 0.5) {
                recommendedFormat = "jpeg";
            }
        }

        int suggestedWidth = Math.min(analysis.getWidth(), maxWidth);
        int suggestedHeight = Math.min(analysis.getHeight(), maxHeight);

        return CompressionSuggestion.builder()
            .recommendedFormat(recommendedFormat)
            .recommendedQuality(defaultQuality)
            .suggestedMaxWidth(suggestedWidth)
            .suggestedMaxHeight(suggestedHeight)
            .useProgressive(!analysis.hasTransparency())
            .estimatedCompressionRatio(0.5)
            .stripMetadata(true)
            .build();
    }

    @Override
    public List<ResponsiveVariant> generateResponsiveVariants(String imagePath, List<String> breakpointNames) {
        List<ResponsiveVariant> variants = new ArrayList<>();
        ImageAnalysisResult analysis = analyzeImage(imagePath);

        if (!generateResponsiveVariants) {
            return variants;
        }

        List<Integer> bps = breakpoints;
        if (breakpointNames != null && !breakpointNames.isEmpty()) {
            bps = new ArrayList<>();
            for (String name : breakpointNames) {
                try {
                    bps.add(Integer.parseInt(name));
                } catch (NumberFormatException e) {
                    bps.add(768);
                }
            }
        }

        int originalWidth = analysis.getWidth();
        int originalHeight = analysis.getHeight();
        double aspectRatio = originalHeight / (double) originalWidth;

        for (int i = 0; i < bps.size(); i++) {
            int bp = bps.get(i);
            if (bp >= originalWidth) {
                continue;
            }

            int newWidth = bp;
            int newHeight = (int) (newWidth * aspectRatio);

            String breakpointName = "sm";
            switch (i) {
                case 0: breakpointName = "xs"; break;
                case 1: breakpointName = "sm"; break;
                case 2: breakpointName = "md"; break;
                case 3: breakpointName = "lg"; break;
                default: breakpointName = "xl" + i; break;
            }

            long estimatedSize = estimateVariantSize(analysis.getFileSize(), newWidth, originalWidth);

            variants.add(ResponsiveVariant.builder()
                .breakpoint(breakpointName)
                .width(newWidth)
                .height(newHeight)
                .format(defaultFormat)
                .estimatedSize(estimatedSize)
                .variantPath(imagePath + "_" + breakpointName + "." + defaultFormat)
                .build());
        }

        return variants;
    }

    @Override
    public byte[] generateOptimizedRendition(String imagePath, OptimizationOptions options) {
        ImageAnalysisResult analysis = analyzeImage(imagePath);
        OptimizationOptions opts = options != null ? options : OptimizationOptions.builder().build();

        int targetWidth = Math.min(analysis.getWidth(), opts.getMaxWidth());
        int targetHeight = (int) (targetWidth * (analysis.getHeight() / (double) analysis.getWidth()));
        if (targetHeight > opts.getMaxHeight()) {
            targetHeight = opts.getMaxHeight();
            targetWidth = (int) (targetHeight / (analysis.getHeight() / (double) analysis.getWidth()));
        }

        log.info("Generating optimized rendition: {} -> {}x{}", imagePath, targetWidth, targetHeight);

        return new byte[0];
    }

    private String callVisionApi(String imagePath, String prompt) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(visionEndpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);

            Map<String, Object> request = new HashMap<>();
            request.put("model", visionModel);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            Map<String, Object> content = new HashMap<>();
            content.put("type", "text");
            content.put("text", prompt);

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, Object> imageUrl = new HashMap<>();
            imageUrl.put("url", "file://" + imagePath);
            imageContent.put("image_url", imageUrl);

            message.put("content", new Object[]{content, imageContent});

            request.put("messages", new Object[]{message});
            request.put("max_tokens", 1000);

            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("Vision API error: {}", responseBody);
                    return "";
                }

                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode choices = rootNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    return choices.get(0).get("message").get("content").asText();
                }

                return "";
            }
        }
    }

    private ImageAnalysisResult parseAnalysisResponse(String imagePath, String response) {
        int width = 1920;
        int height = 1080;
        String format = "jpeg";
        long fileSize = 0L;
        String dominantColors = "#FFFFFF";
        boolean hasTransparency = false;
        List<String> detectedObjects = new ArrayList<>();
        String sceneDescription = "";
        int qualityScore = 80;

        if (StringUtils.isNotBlank(response)) {
            try {
                JsonNode json = objectMapper.readTree(response);
                width = json.has("width") ? json.get("width").asInt() : width;
                height = json.has("height") ? json.get("height").asInt() : height;
                format = json.has("format") ? json.get("format").asText() : format;
                fileSize = json.has("fileSize") ? json.get("fileSize").asLong() : fileSize;
                dominantColors = json.has("dominantColors") ? json.get("dominantColors").asText() : dominantColors;
                hasTransparency = json.has("hasTransparency") && json.get("hasTransparency").asBoolean();
                sceneDescription = json.has("sceneDescription") ? json.get("sceneDescription").asText() : sceneDescription;
                qualityScore = json.has("qualityScore") ? json.get("qualityScore").asInt() : qualityScore;

                if (json.has("detectedObjects") && json.get("detectedObjects").isArray()) {
                    for (JsonNode obj : json.get("detectedObjects")) {
                        detectedObjects.add(obj.asText());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse analysis response: {}", e.getMessage());
            }
        }

        return ImageAnalysisResult.builder()
            .imagePath(imagePath)
            .width(width)
            .height(height)
            .format(format)
            .fileSize(fileSize)
            .dominantColors(dominantColors)
            .hasTransparency(hasTransparency)
            .detectedObjects(detectedObjects)
            .sceneDescription(sceneDescription)
            .qualityScore(qualityScore)
            .build();
    }

    private double calculateResizeSavings(int original, int target) {
        if (original <= target) return 0;
        return 1.0 - ((target * target) / (double) (original * original));
    }

    private long estimateVariantSize(long originalSize, int newWidth, int originalWidth) {
        return (long) (originalSize * ((newWidth * newWidth) / (double) (originalWidth * originalWidth)));
    }

    private void evictOldCacheEntries() {
        if (analysisCache.size() > cacheSize) {
            int toRemove = analysisCache.size() - cacheSize;
            for (int i = 0; i < toRemove && !analysisCache.isEmpty(); i++) {
                String key = analysisCache.keys().nextElement();
                analysisCache.remove(key);
            }
        }
    }

    public AIService getAiService() {
        return aiService;
    }

    public String getVisionEndpoint() {
        return visionEndpoint;
    }

    public String getVisionModel() {
        return visionModel;
    }

    public int getDefaultQuality() {
        return defaultQuality;
    }

    public String getDefaultFormat() {
        return defaultFormat;
    }

    public boolean isGenerateResponsiveVariants() {
        return generateResponsiveVariants;
    }

    private static class CachedAnalysis {
        final ImageAnalysisResult result;

        CachedAnalysis(ImageAnalysisResult result) {
            this.result = result;
        }
    }
}