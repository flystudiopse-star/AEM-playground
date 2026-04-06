package com.aem.playground.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
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

@Component(service = SharePointMigrationService.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SharePointMigrationService.Config.class)
public class SharePointMigrationService {

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
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String siteUrl;
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String siteName;
    private int apiTimeout;
    private boolean enableDebug;

    private String accessToken;
    private long tokenExpiry;

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

        logger.info("SharePoint migration service activated with site: {}", siteUrl);
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
