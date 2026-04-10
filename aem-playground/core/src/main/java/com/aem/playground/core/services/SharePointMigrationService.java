package com.aem.playground.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(service = SharePointMigrationService.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SharePointMigrationService.Config.class)
public class SharePointMigrationService {

    @Reference
    private ContentTransformer contentTransformer;

    @ObjectClassDefinition(name = "SharePoint Migration Configuration",
            description = "Configuration for SharePoint to AEM migration")
    public @interface Config {

        @AttributeDefinition(name = "SharePoint Site URL", description = "Base URL of SharePoint site")
        String sharepoint_site_url() default "";

        @AttributeDefinition(name = "Client ID", description = "SharePoint API client ID")
        String sharepoint_client_id() default "";

        @AttributeDefinition(name = "Client Secret", description = "SharePoint API client secret")
        String sharepoint_client_secret() default "";

        @AttributeDefinition(name = "Tenant ID", description = "Azure AD tenant ID")
        String sharepoint_tenant_id() default "";

        @AttributeDefinition(name = "Site Name", description = "SharePoint site name")
        String sharepoint_site_name() default "";

        @AttributeDefinition(name = "API Timeout", description = "API request timeout in milliseconds")
        int api_timeout() default 30000;

        @AttributeDefinition(name = "Enable Debug Logging", description = "Enable debug logging")
        boolean enable_debug() default false;

        @AttributeDefinition(name = "Enable AI Transformation", description = "Enable AI-powered content transformation")
        boolean enable_ai_transformation() default true;

        @AttributeDefinition(name = "Enable AI Metadata", description = "Enable AI-generated metadata")
        boolean enable_ai_metadata() default true;

        @AttributeDefinition(name = "Enable AI Image Alt Text", description = "Enable AI-generated image alt text")
        boolean enable_ai_image_alt() default true;

        @AttributeDefinition(name = "Enable AI Cleanup", description = "Enable AI-powered content cleanup")
        boolean enable_ai_cleanup() default true;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String siteUrl;
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String siteName;
    private int apiTimeout;
    private boolean enableDebug;
    private boolean enableAiTransformation;
    private boolean enableAiMetadata;
    private boolean enableAiImageAlt;
    private boolean enableAiCleanup;

    private String accessToken;
    private long tokenExpiry;

    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "<img[^>]+src=['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    @PostConstruct
    protected void init() {
        logger.info("SharePointMigrationService initialized");
    }

    @Activate
    protected void activate(final Config config) {
        this.siteUrl = config.sharepoint_site_url();
        this.clientId = config.sharepoint_client_id();
        this.clientSecret = config.sharepoint_client_secret();
        this.tenantId = config.sharepoint_tenant_id();
        this.siteName = config.sharepoint_site_name();
        this.apiTimeout = config.api_timeout();
        this.enableDebug = config.enable_debug();
        this.enableAiTransformation = config.enable_ai_transformation();
        this.enableAiMetadata = config.enable_ai_metadata();
        this.enableAiImageAlt = config.enable_ai_image_alt();
        this.enableAiCleanup = config.enable_ai_cleanup();

        logger.info("SharePoint migration service activated with site: {}, AI: transformation={}, metadata={}, imageAlt={}, cleanup={}",
                siteUrl, enableAiTransformation, enableAiMetadata, enableAiImageAlt, enableAiCleanup);
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public String getSiteName() {
        return siteName;
    }

    public boolean isEnabled() {
        return siteUrl != null && !siteUrl.isEmpty();
    }

    private void authenticate() throws IOException {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return;
        }

        String tokenUrl = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId);

        String postData = String.format(
                "client_id=%s&client_secret=%s&scope=%s/.default&grant_type=client_credentials",
                clientId, clientSecret, clientId
        );

        URL url = new URL(tokenUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.getOutputStream().write(postData.getBytes(StandardCharsets.UTF_8));

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseStr = response.toString();
            int tokenStart = responseStr.indexOf("\"access_token\":\"") + 16;
            int tokenEnd = responseStr.indexOf("\"", tokenStart);
            if (tokenStart > 15 && tokenEnd > tokenStart) {
                accessToken = responseStr.substring(tokenStart, tokenEnd);
                tokenExpiry = System.currentTimeMillis() + 3500000;
            }
        } else {
            throw new IOException("Authentication failed with response code: " + responseCode);
        }
    }

    public List<SharePointPage> fetchPages(int maxPages) throws IOException {
        List<SharePointPage> pages = new ArrayList<>();
        authenticate();

        String apiUrl = String.format("%s/_api/web/lists/GetByTitle('Site Pages')/items?$top=%d&$select=Title,Id,FileRef,Created,Modified",
                siteUrl, maxPages);

        if (enableDebug) {
            logger.debug("Fetching pages from: {}", apiUrl);
        }

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(apiTimeout);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String json = response.toString();
            pages = parsePagesFromJson(json);
            logger.info("Fetched {} pages from SharePoint", pages.size());
        } else {
            throw new IOException("Failed to fetch pages, response code: " + responseCode);
        }

        return pages;
    }

    List<SharePointPage> parsePagesFromJson(String json) {
        List<SharePointPage> pages = new ArrayList<>();
        int resultsStart = json.indexOf("\"results\":[");
        if (resultsStart == -1) {
            resultsStart = json.indexOf("\"value\":[");
        }

        if (resultsStart != -1) {
            int arrayStart = json.indexOf("[", resultsStart);
            int arrayEnd = json.lastIndexOf("]");
            if (arrayStart != -1 && arrayEnd > arrayStart) {
                String arrayContent = json.substring(arrayStart + 1, arrayEnd);
                String[] items = arrayContent.split("\\},\\{");
                for (String item : items) {
                    if (item.trim().isEmpty()) continue;
                    if (!item.startsWith("{")) item = "{" + item;
                    if (!item.endsWith("}")) item = item + "}";

                    SharePointPage page = parsePageFromJson(item);
                    if (page != null) {
                        pages.add(page);
                    }
                }
            }
        }
        return pages;
    }

    private SharePointPage parsePageFromJson(String json) {
        try {
            String id = extractJsonValue(json, "Id");
            String title = extractJsonValue(json, "Title");
            String fileRef = extractJsonValue(json, "FileRef");
            String created = extractJsonValue(json, "Created");
            String modified = extractJsonValue(json, "Modified");

            if (id == null) return null;

            return new SharePointPage(id, title, fileRef, created, modified);
        } catch (Exception e) {
            logger.warn("Failed to parse page from JSON: {}", e.getMessage());
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\"')) {
            valueStart++;
        }

        int valueEnd = valueStart;
        boolean inString = json.charAt(valueStart - 1) == '\"';
        while (valueEnd < json.length()) {
            if (inString && json.charAt(valueEnd) == '\"') break;
            if (!inString && (json.charAt(valueEnd) == ',' || json.charAt(valueEnd) == '}')) break;
            valueEnd++;
        }

        String value = json.substring(valueStart, valueEnd);
        return value.isEmpty() ? null : value;
    }

    public SharePointPageContent getPageContent(String pageId) throws IOException {
        authenticate();

        String apiUrl = String.format("%s/_api/web/lists/GetByTitle('Site Pages')/items(%s)/FieldValuesAsHtml",
                siteUrl, pageId);

        if (enableDebug) {
            logger.debug("Fetching page content from: {}", apiUrl);
        }

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(apiTimeout);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return parsePageContentFromJson(response.toString());
        } else {
            throw new IOException("Failed to fetch page content, response code: " + responseCode);
        }
    }

    public List<SharePointAsset> fetchAssets(int maxAssets) throws IOException {
        List<SharePointAsset> assets = new ArrayList<>();
        authenticate();

        String apiUrl = String.format("%s/_api/web/lists/GetByTitle('Documents')/items?$top=%d&$select=Name,Id,FileRef,Created,Modified,FileSize",
                siteUrl, maxAssets);

        if (enableDebug) {
            logger.debug("Fetching assets from: {}", apiUrl);
        }

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(apiTimeout);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            assets = parseAssetsFromJson(response.toString());
            logger.info("Fetched {} assets from SharePoint", assets.size());
        } else {
            throw new IOException("Failed to fetch assets, response code: " + responseCode);
        }

        return assets;
    }

    List<SharePointAsset> parseAssetsFromJson(String json) {
        List<SharePointAsset> assets = new ArrayList<>();
        int resultsStart = json.indexOf("\"results\":[");
        if (resultsStart == -1) {
            resultsStart = json.indexOf("\"value\":[");
        }

        if (resultsStart != -1) {
            int arrayStart = json.indexOf("[", resultsStart);
            int arrayEnd = json.lastIndexOf("]");
            if (arrayStart != -1 && arrayEnd > arrayStart) {
                String arrayContent = json.substring(arrayStart + 1, arrayEnd);
                String[] items = arrayContent.split("\\},\\{");
                for (String item : items) {
                    if (item.trim().isEmpty()) continue;
                    if (!item.startsWith("{")) item = "{" + item;
                    if (!item.endsWith("}")) item = item + "}";

                    SharePointAsset asset = parseAssetFromJson(item);
                    if (asset != null) {
                        assets.add(asset);
                    }
                }
            }
        }
        return assets;
    }

    private SharePointAsset parseAssetFromJson(String json) {
        try {
            String id = extractJsonValue(json, "Id");
            String name = extractJsonValue(json, "Name");
            String fileRef = extractJsonValue(json, "FileRef");
            String created = extractJsonValue(json, "Created");
            String modified = extractJsonValue(json, "Modified");
            String fileSize = extractJsonValue(json, "FileSize");

            if (id == null || name == null) return null;

            return new SharePointAsset(id, name, fileRef, created, modified, fileSize);
        } catch (Exception e) {
            logger.warn("Failed to parse asset from JSON: {}", e.getMessage());
            return null;
        }
    }

    private SharePointPageContent parsePageContentFromJson(String json) {
        Map<String, String> metadata = new HashMap<>();
        String htmlContent = "";

        String[] keys = {"Title", "Editor", "Author", "Created", "Modified", "Description"};
        for (String key : keys) {
            String value = extractJsonValue(json, key);
            if (value != null) {
                metadata.put(key, value);
            }
        }

        String wikiField = extractJsonValue(json, "WikiField");
        if (wikiField != null) {
            htmlContent = wikiField;
        }

        return new SharePointPageContent(htmlContent, metadata);
    }

    public byte[] downloadAsset(String fileRef) throws IOException {
        authenticate();

        String apiUrl = siteUrl + "/_api/web/GetFileByServerRelativeUrl('" + fileRef + "')/$value";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setConnectTimeout(apiTimeout);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            java.io.InputStream inputStream = conn.getInputStream();
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            inputStream.close();
            return buffer.toByteArray();
        } else {
            throw new IOException("Failed to download asset, response code: " + responseCode);
        }
    }

    public void setContentTransformer(ContentTransformer contentTransformer) {
    }

    public boolean isAiTransformationEnabled() {
        return enableAiTransformation && contentTransformer != null;
    }

    public boolean isAiMetadataEnabled() {
        return enableAiMetadata && contentTransformer != null;
    }

    public boolean isAiImageAltEnabled() {
        return enableAiImageAlt && contentTransformer != null;
    }

    public boolean isAiCleanupEnabled() {
        return enableAiCleanup && contentTransformer != null;
    }

    public TransformationResult transformContentWithAi(SharePointPageContent content) {
        TransformationResult result = new TransformationResult();
        result.setOriginalContent(content.getHtmlContent());

        if (!isAiTransformationEnabled()) {
            result.setTransformedContent(content.getHtmlContent());
            return result;
        }

        try {
            ContentTransformer.TransformOptions options = createTransformOptions();
            String transformed = contentTransformer.transformContent(content.getHtmlContent(), options);
            result.setTransformedContent(transformed);
            result.setTransformationSuccess(true);

            List<String> imageUrls = extractImageUrls(content.getHtmlContent());
            result.setImageUrls(imageUrls);

            if (enableAiMetadata) {
                Map<String, String> metadata = contentTransformer.generateMetadata(
                        content.getHtmlContent(),
                        content.getMetadata().get("Title")
                );
                result.setGeneratedMetadata(metadata);
            }

            if (enableAiCleanup) {
                ContentTransformer.CleanupOptions cleanupOptions = createCleanupOptions();
                String cleaned = contentTransformer.cleanupAndOptimize(transformed, cleanupOptions);
                if (!cleaned.equals(transformed)) {
                    result.setTransformedContent(cleaned);
                    result.setCleanupApplied(true);
                }
            }

            if (enableDebug) {
                logger.debug("AI transformation completed for content length: {}", content.getHtmlContent().length());
            }

        } catch (Exception e) {
            logger.error("AI transformation failed: {}", e.getMessage());
            result.setTransformedContent(content.getHtmlContent());
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    public ImageAltTextResult generateImageAltText(byte[] imageData, String imageName) {
        ImageAltTextResult result = new ImageAltTextResult();
        result.setImageName(imageName);

        if (!isAiImageAltEnabled()) {
            result.setAltText(generateDefaultAltText(imageName));
            return result;
        }

        try {
            String altText = contentTransformer.generateImageAltText(imageData, imageName);
            result.setAltText(altText);
            result.setGenerationSuccess(true);

            if (enableDebug) {
                logger.debug("Generated alt text for image {}: {}", imageName, altText);
            }

        } catch (Exception e) {
            logger.error("Failed to generate alt text for {}: {}", imageName, e.getMessage());
            result.setAltText(generateDefaultAltText(imageName));
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    public ComponentMappingResult suggestComponentMappings(String htmlContent) {
        ComponentMappingResult result = new ComponentMappingResult();

        if (!isAiTransformationEnabled()) {
            return result;
        }

        try {
            List<ContentTransformer.ComponentMapping> mappings =
                    contentTransformer.suggestComponentMappings(htmlContent);
            result.setMappings(mappings);
            result.setMappingSuccess(true);

            if (enableDebug) {
                logger.debug("Suggested {} component mappings", mappings.size());
            }

        } catch (Exception e) {
            logger.error("Component mapping failed: {}", e.getMessage());
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    public RedirectResult generateRedirectMappings(List<SharePointPage> pages, String targetBaseUrl) {
        RedirectResult result = new RedirectResult();

        if (!isAiTransformationEnabled()) {
            return result;
        }

        try {
            List<String> sourceUrls = new ArrayList<>();
            for (SharePointPage page : pages) {
                sourceUrls.add(page.getFileRef());
            }

            List<ContentTransformer.RedirectMapping> redirects =
                    contentTransformer.generateRedirectMappings(sourceUrls, targetBaseUrl);
            result.setRedirects(redirects);
            result.setGenerationSuccess(true);

            if (enableDebug) {
                logger.debug("Generated {} redirect mappings", redirects.size());
            }

        } catch (Exception e) {
            logger.error("Redirect mapping failed: {}", e.getMessage());
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    private List<String> extractImageUrls(String htmlContent) {
        List<String> urls = new ArrayList<>();
        if (htmlContent == null || htmlContent.isEmpty()) {
            return urls;
        }

        Matcher matcher = IMAGE_PATTERN.matcher(htmlContent);
        while (matcher.find()) {
            String src = matcher.group(1);
            if (src != null && !src.isEmpty()) {
                urls.add(src);
            }
        }

        return urls;
    }

    private ContentTransformer.TransformOptions createTransformOptions() {
        return new ContentTransformer.TransformOptions() {
            @Override
            public String getTargetFormat() {
                return "AEM HTL";
            }

            @Override
            public boolean isPreserveImages() {
                return true;
            }

            @Override
            public boolean isCleanupHtml() {
                return enableAiCleanup;
            }

            @Override
            public Map<String, String> getCustomMappings() {
                return new HashMap<>();
            }
        };
    }

    private ContentTransformer.CleanupOptions createCleanupOptions() {
        return new ContentTransformer.CleanupOptions() {
            @Override
            public boolean isRemoveEmptyParagraphs() {
                return true;
            }

            @Override
            public boolean isFixEncoding() {
                return true;
            }

            @Override
            public boolean isNormalizeWhitespace() {
                return true;
            }

            @Override
            public boolean isRemoveInvalidTags() {
                return true;
            }
        };
    }

    private String generateDefaultAltText(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return "Image";
        }
        String name = imageName.substring(0, imageName.lastIndexOf('.'));
        String camelCase = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        return camelCase.replaceAll("[-_]", " ");
    }

    public static class TransformationResult {
        private String originalContent;
        private String transformedContent;
        private boolean transformationSuccess;
        private boolean cleanupApplied;
        private List<String> imageUrls = new ArrayList<>();
        private Map<String, String> generatedMetadata;
        private String errorMessage;

        public String getOriginalContent() { return originalContent; }
        public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
        public String getTransformedContent() { return transformedContent; }
        public void setTransformedContent(String transformedContent) { this.transformedContent = transformedContent; }
        public boolean isTransformationSuccess() { return transformationSuccess; }
        public void setTransformationSuccess(boolean transformationSuccess) { this.transformationSuccess = transformationSuccess; }
        public boolean isCleanupApplied() { return cleanupApplied; }
        public void setCleanupApplied(boolean cleanupApplied) { this.cleanupApplied = cleanupApplied; }
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
        public Map<String, String> getGeneratedMetadata() { return generatedMetadata; }
        public void setGeneratedMetadata(Map<String, String> generatedMetadata) { this.generatedMetadata = generatedMetadata; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ImageAltTextResult {
        private String imageName;
        private String altText;
        private boolean generationSuccess;
        private String errorMessage;

        public String getImageName() { return imageName; }
        public void setImageName(String imageName) { this.imageName = imageName; }
        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }
        public boolean isGenerationSuccess() { return generationSuccess; }
        public void setGenerationSuccess(boolean generationSuccess) { this.generationSuccess = generationSuccess; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ComponentMappingResult {
        private List<ContentTransformer.ComponentMapping> mappings = new ArrayList<>();
        private boolean mappingSuccess;
        private String errorMessage;

        public List<ContentTransformer.ComponentMapping> getMappings() { return mappings; }
        public void setMappings(List<ContentTransformer.ComponentMapping> mappings) { this.mappings = mappings; }
        public boolean isMappingSuccess() { return mappingSuccess; }
        public void setMappingSuccess(boolean mappingSuccess) { this.mappingSuccess = mappingSuccess; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class RedirectResult {
        private List<ContentTransformer.RedirectMapping> redirects = new ArrayList<>();
        private boolean generationSuccess;
        private String errorMessage;

        public List<ContentTransformer.RedirectMapping> getRedirects() { return redirects; }
        public void setRedirects(List<ContentTransformer.RedirectMapping> redirects) { this.redirects = redirects; }
        public boolean isGenerationSuccess() { return generationSuccess; }
        public void setGenerationSuccess(boolean generationSuccess) { this.generationSuccess = generationSuccess; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class SharePointPage {
        private final String id;
        private final String title;
        private final String fileRef;
        private final String created;
        private final String modified;

        public SharePointPage(String id, String title, String fileRef, String created, String modified) {
            this.id = id;
            this.title = title;
            this.fileRef = fileRef;
            this.created = created;
            this.modified = modified;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getFileRef() { return fileRef; }
        public String getCreated() { return created; }
        public String getModified() { return modified; }

        public String getPageName() {
            if (fileRef != null && fileRef.contains("/")) {
                int lastSlash = fileRef.lastIndexOf("/");
                return fileRef.substring(lastSlash + 1);
            }
            return title != null ? title : "page-" + id;
        }
    }

    public static class SharePointPageContent {
        private final String htmlContent;
        private final Map<String, String> metadata;

        public SharePointPageContent(String htmlContent, Map<String, String> metadata) {
            this.htmlContent = htmlContent;
            this.metadata = metadata;
        }

        public String getHtmlContent() { return htmlContent; }
        public Map<String, String> getMetadata() { return metadata; }
    }

    public static class SharePointAsset {
        private final String id;
        private final String name;
        private final String fileRef;
        private final String created;
        private final String modified;
        private final String fileSize;

        public SharePointAsset(String id, String name, String fileRef, String created, String modified, String fileSize) {
            this.id = id;
            this.name = name;
            this.fileRef = fileRef;
            this.created = created;
            this.modified = modified;
            this.fileSize = fileSize;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getFileRef() { return fileRef; }
        public String getCreated() { return created; }
        public String getModified() { return modified; }
        public String getFileSize() { return fileSize; }
    }
}
