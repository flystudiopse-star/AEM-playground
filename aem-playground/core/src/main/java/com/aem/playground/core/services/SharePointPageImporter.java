package com.aem.playground.core.services;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aem.playground.core.services.SharePointMigrationService.SharePointPage;

@Component(service = SharePointPageImporter.class)
public class SharePointPageImporter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolver resourceResolver;

    private static final String TARGET_PATH_BASE = "/content/aem-playground";

    private static final String TEMPLATE_PATH = "/conf/aem-playground/settings/wcm/templates/page-content";

    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "<img[^>]+src=['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile(
            "<p[^>]*>(.*?)</p>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern HEADING_PATTERN = Pattern.compile(
            "<h([1-6])[^>]*>(.*?)</h\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern LINK_PATTERN = Pattern.compile(
            "<a[^>]+href=['\"]([^'\"]+)['\"][^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern LIST_PATTERN = Pattern.compile(
            "<(ul|ol)[^>]*>(.*?)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile(
            "<li[^>]*>(.*?)</li>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public void setResourceResolver(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public String getTargetPathBase() {
        return TARGET_PATH_BASE;
    }

    public void setTargetPathBase(String targetPathBase) {
    }

    public ImportResult importPage(SharePointMigrationService.SharePointPage sharePointPage,
                                   SharePointMigrationService.SharePointPageContent content) {
        ImportResult result = new ImportResult();

        try {
            String pageName = sanitizePageName(sharePointPage.getPageName());
            String targetPath = TARGET_PATH_BASE + "/" + pageName;

            result.setTargetPath(targetPath);

            Resource existingResource = resourceResolver.getResource(targetPath);
            if (existingResource != null) {
                logger.info("Page already exists at: {}, updating content", targetPath);
                result.setExistingPage(true);
            }

            Resource parentResource = resourceResolver.getResource(TARGET_PATH_BASE);
            if (parentResource == null) {
                createPathHierarchy(TARGET_PATH_BASE);
            }

            Map<String, Object> pageProperties = new HashMap<>();
            pageProperties.put("jcr:primaryType", "cq:Page");
            pageProperties.put("jcr:title", sharePointPage.getTitle() != null ? sharePointPage.getTitle() : pageName);

            Map<String, Object> pageContentProps = new HashMap<>();
            pageContentProps.put("jcr:primaryType", "cq:PageContent");
            pageContentProps.put("jcr:title", sharePointPage.getTitle() != null ? sharePointPage.getTitle() : pageName);
            pageContentProps.put("sling:resourceType", "aem-playground/components/page");
            pageContentProps.put("cq:template", TEMPLATE_PATH);

            Map<String, String> metadata = content.getMetadata();
            if (metadata.containsKey("Description")) {
                pageContentProps.put("jcr:description", metadata.get("Description"));
            }
            if (metadata.containsKey("Created")) {
                pageContentProps.put("pageCreated", metadata.get("Created"));
            }
            if (metadata.containsKey("Modified")) {
                pageContentProps.put("pageModified", metadata.get("Modified"));
            }

            String htmlContent = content.getHtmlContent();
            List<AemComponent> components = parseHtmlToComponents(htmlContent);

            List<Resource> createdComponents = createComponents(targetPath + "/jcr:content/root", components);

            pageContentProps.put("componentsCount", createdComponents.size());

            Resource pageResource = resourceResolver.create(
                    resourceResolver.getResource(TARGET_PATH_BASE),
                    pageName,
                    pageProperties
            );

            Resource pageContentResource = resourceResolver.create(
                    pageResource,
                    "jcr:content",
                    pageContentProps
            );

            for (Resource component : createdComponents) {
                String componentPath = component.getPath();
                String parentContainerPath = pageContentResource.getPath() + "/root";
                Resource containerResource = resourceResolver.getResource(parentContainerPath);
                if (containerResource != null) {
                    resourceResolver.commit();
                }
            }

            resourceResolver.commit();

            result.setSuccess(true);
            result.setComponentCount(createdComponents.size());
            logger.info("Successfully imported page: {} with {} components", targetPath, createdComponents.size());

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            logger.error("Failed to import page: {}", e.getMessage(), e);
        }

        return result;
    }

    List<AemComponent> parseHtmlToComponents(String htmlContent) {
        List<AemComponent> components = new ArrayList<>();

        if (htmlContent == null || htmlContent.isEmpty()) {
            return components;
        }

        int currentPosition = 0;

        Matcher headingMatcher = HEADING_PATTERN.matcher(htmlContent);
        while (headingMatcher.find()) {
            String beforeText = htmlContent.substring(currentPosition, headingMatcher.start());
            if (!beforeText.trim().isEmpty()) {
                List<AemComponent> textComponents = parseTextContent(beforeText);
                components.addAll(textComponents);
            }

            String level = headingMatcher.group(1);
            String text = stripHtmlTags(headingMatcher.group(2));

            Map<String, Object> props = new HashMap<>();
            props.put("text", text);
            props.put("headingType", "h" + level);
            components.add(new AemComponent("aem-playground/components/title", props));

            currentPosition = headingMatcher.end();
        }

        String remainingText = htmlContent.substring(currentPosition);
        if (!remainingText.trim().isEmpty()) {
            List<AemComponent> textComponents = parseTextContent(remainingText);
            components.addAll(textComponents);
        }

        if (components.isEmpty()) {
            Map<String, Object> props = new HashMap<>();
            props.put("text", stripHtmlTags(htmlContent));
            components.add(new AemComponent("aem-playground/components/text", props));
        }

        return components;
    }

    private List<AemComponent> parseTextContent(String text) {
        List<AemComponent> components = new ArrayList<>();

        Matcher linkMatcher = LINK_PATTERN.matcher(text);
        int lastEnd = 0;
        while (linkMatcher.find()) {
            String beforeLink = text.substring(lastEnd, linkMatcher.start()).trim();
            if (!beforeLink.isEmpty()) {
                Map<String, Object> textProps = new HashMap<>();
                textProps.put("text", stripHtmlTags(beforeLink));
                components.add(new AemComponent("aem-playground/components/text", textProps));
            }

            String href = linkMatcher.group(1);
            String linkText = stripHtmlTags(linkMatcher.group(2));

            Map<String, Object> linkProps = new HashMap<>();
            linkProps.put("text", linkText);
            linkProps.put("linkURL", href);
            components.add(new AemComponent("aem-playground/components/external-link", linkProps));

            lastEnd = linkMatcher.end();
        }

        String remainingText = text.substring(lastEnd).trim();
        if (!remainingText.isEmpty()) {
            Map<String, Object> props = new HashMap<>();
            props.put("text", stripHtmlTags(remainingText));
            components.add(new AemComponent("aem-playground/components/text", props));
        }

        return components;
    }

    private String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }

    private List<Resource> createComponents(String parentPath, List<AemComponent> components) {
        List<Resource> createdResources = new ArrayList<>();

        Resource parentResource = resourceResolver.getResource(parentPath);
        if (parentResource == null) {
            try {
                Map<String, Object> containerProps = new HashMap<>();
                containerProps.put("jcr:primaryType", "nt:unstructured");
                containerProps.put("sling:resourceType", "aem-playground/components/container");
                parentResource = resourceResolver.create(
                        resourceResolver.getResource(TARGET_PATH_BASE + "/jcr:content"),
                        "root",
                        containerProps
                );
            } catch (Exception e) {
                logger.error("Failed to create container: {}", e.getMessage());
                return createdResources;
            }
        }

        for (int i = 0; i < components.size(); i++) {
            try {
                AemComponent component = components.get(i);
                String componentName = "component-" + i;

                Map<String, Object> componentProps = new HashMap<>();
                componentProps.put("jcr:primaryType", "nt:unstructured");
                componentProps.put("sling:resourceType", component.getResourceType());
                componentProps.putAll(component.getProperties());

                Resource componentResource = resourceResolver.create(parentResource, componentName, componentProps);
                createdResources.add(componentResource);

            } catch (Exception e) {
                logger.warn("Failed to create component {}: {}", i, e.getMessage());
            }
        }

        return createdResources;
    }

    private void createPathHierarchy(String path) throws RepositoryException {
        String[] segments = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String segment : segments) {
            if (segment.isEmpty()) {
                currentPath.append("/");
                continue;
            }
            currentPath.append("/").append(segment);

            Resource existing = resourceResolver.getResource(currentPath.toString());
            if (existing == null) {
                Map<String, Object> props = new HashMap<>();
                props.put("jcr:primaryType", "nt:unstructured");

                Resource parent = resourceResolver.getResource(currentPath.substring(0, currentPath.lastIndexOf("/")));
                if (parent != null) {
                    resourceResolver.create(parent, segment, props);
                }
            }
        }
    }

    private String sanitizePageName(String name) {
        if (name == null || name.isEmpty()) {
            return "page-" + System.currentTimeMillis();
        }
        String sanitized = name.replaceAll("[^a-zA-Z0-9\\-_]", "-");
        sanitized = sanitized.toLowerCase();
        if (sanitized.matches("^[0-9].*")) {
            sanitized = "p-" + sanitized;
        }
        return sanitized;
    }

    public BulkImportResult importPages(List<SharePointMigrationService.SharePointPage> pages,
                                        SharePointMigrationService migrationService) {
        BulkImportResult result = new BulkImportResult();
        result.setTotalPages(pages.size());

        logger.info("Starting bulk import of {} pages", pages.size());

        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < pages.size(); i++) {
            SharePointPage page = pages.get(i);

            try {
                SharePointMigrationService.SharePointPageContent content = migrationService.getPageContent(page.getId());

                ImportResult importResult = importPage(page, content);

                if (importResult.isSuccess()) {
                    successCount++;
                    result.addSuccessfulPath(importResult.getTargetPath());
                } else {
                    errorCount++;
                    String errorMsg = "Failed to import page " + page.getTitle() + ": " + importResult.getErrorMessage();
                    errors.add(errorMsg);
                    result.addError(errorMsg);
                }

                if ((i + 1) % 10 == 0) {
                    logger.info("Progress: {}/{} pages imported", i + 1, pages.size());
                }

            } catch (Exception e) {
                errorCount++;
                String errorMsg = "Exception importing page " + page.getTitle() + ": " + e.getMessage();
                errors.add(errorMsg);
                result.addError(errorMsg);
                logger.error(errorMsg, e);
            }
        }

        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);

        logger.info("Bulk import completed: {} successful, {} errors", successCount, errorCount);

        return result;
    }

    public static class AemComponent {
        private final String resourceType;
        private final Map<String, Object> properties;

        public AemComponent(String resourceType, Map<String, Object> properties) {
            this.resourceType = resourceType;
            this.properties = properties;
        }

        public String getResourceType() {
            return resourceType;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    public static class ImportResult {
        private boolean success;
        private String targetPath;
        private int componentCount;
        private String errorMessage;
        private boolean existingPage;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getTargetPath() { return targetPath; }
        public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
        public int getComponentCount() { return componentCount; }
        public void setComponentCount(int componentCount) { this.componentCount = componentCount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public boolean isExistingPage() { return existingPage; }
        public void setExistingPage(boolean existingPage) { this.existingPage = existingPage; }
    }

    public static class BulkImportResult {
        private int totalPages;
        private int successCount;
        private int errorCount;
        private List<String> successfulPaths = new ArrayList<>();
        private List<String> errors = new ArrayList<>();

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public List<String> getSuccessfulPaths() { return successfulPaths; }
        public void addSuccessfulPath(String path) { this.successfulPaths.add(path); }
        public List<String> getErrors() { return errors; }
        public void addError(String error) { this.errors.add(error); }

        public boolean isComplete() {
            return successCount + errorCount == totalPages;
        }

        public double getProgressPercentage() {
            if (totalPages == 0) return 100;
            return ((double) (successCount + errorCount) / totalPages) * 100;
        }
    }
}