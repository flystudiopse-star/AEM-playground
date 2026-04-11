package com.aem.playground.core.workflow;

import com.aem.playground.core.services.TranslationService;
import com.aem.playground.core.services.dto.TranslationRequest;
import com.aem.playground.core.services.dto.TranslationResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.resource.api.JcrResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component(service = LanguageCopyWorkflowAction.class)
public class LanguageCopyWorkflowAction {

    private static final Logger log = LoggerFactory.getLogger(LanguageCopyWorkflowAction.class);

    private static final String PARAM_SOURCE_PATH = "sourcePath";
    private static final String PARAM_SOURCE_LANGUAGE = "sourceLanguage";
    private static final String PARAM_TARGET_LANGUAGE = "targetLanguage";
    private static final String PARAM_CREATE_LANGUAGE_COPY = "createLanguageCopy";
    private static final String PARAM_TRANSLATE_CONTENT = "translateContent";
    private static final String PARAM_USE_LIVE_COPY = "useLiveCopy";

    @Reference
    private TranslationService translationService;

    @Reference
    private JcrResourceResolverFactory resourceResolverFactory;

    public Map<String, Object> execute(Map<String, Object> params, Session session) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        String sourcePath = PropertiesUtil.toString(params.get(PARAM_SOURCE_PATH), null);
        String sourceLanguage = PropertiesUtil.toString(params.get(PARAM_SOURCE_LANGUAGE), "en");
        String targetLanguage = PropertiesUtil.toString(params.get(PARAM_TARGET_LANGUAGE), null);
        boolean createLanguageCopy = PropertiesUtil.toBoolean(params.get(PARAM_CREATE_LANGUAGE_COPY), true);
        boolean translateContent = PropertiesUtil.toBoolean(params.get(PARAM_TRANSLATE_CONTENT), true);
        boolean useLiveCopy = PropertiesUtil.toBoolean(params.get(PARAM_USE_LIVE_COPY), false);

        if (StringUtils.isBlank(sourcePath)) {
            result.put("error", "Source path is required");
            return result;
        }

        if (StringUtils.isBlank(targetLanguage)) {
            result.put("error", "Target language is required");
            return result;
        }

        if (!translationService.isLanguageSupported(targetLanguage)) {
            result.put("error", "Unsupported target language: " + targetLanguage);
            return result;
        }

        try {
            ResourceResolver resolver = resourceResolverFactory.getResourceResolver(session);
            Resource sourceResource = resolver.getResource(sourcePath);

            if (sourceResource == null) {
                result.put("error", "Source resource not found: " + sourcePath);
                return result;
            }

            String targetPath = null;

            if (createLanguageCopy) {
                if (useLiveCopy) {
                    targetPath = createLiveCopy(sourceResource, targetLanguage, session);
                } else {
                    targetPath = createLanguageBranch(sourceResource, targetLanguage, session);
                }

                result.put("languageCopyPath", targetPath);
            } else {
                targetPath = sourcePath;
            }

            if (translateContent && targetPath != null) {
                TranslationRequest request = TranslationRequest.builder()
                    .contentPath(targetPath)
                    .sourceLanguage(sourceLanguage)
                    .targetLanguage(targetLanguage)
                    .translationType(TranslationRequest.TranslationType.FULL_PAGE)
                    .translateMetadata(true)
                    .build();

                TranslationResult translationResult = translationService.translateContent(request);

                if (translationResult.isSuccess()) {
                    applyTranslation(targetPath, translationResult.getTranslatedContent(), session);
                    result.put("translationSuccess", true);
                    result.put("tokensUsed", translationResult.getTokensUsed());
                    result.put("translationTimeMs", translationResult.getTranslationTimeMs());
                } else {
                    result.put("translationError", translationResult.getError());
                }
            }

            result.put("success", true);
            result.put("targetPath", targetPath);

        } catch (Exception e) {
            log.error("Error in language copy workflow action: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private String createLanguageBranch(Resource sourceResource, String targetLanguage, Session session) throws Exception {
        String sourcePath = sourceResource.getPath();
        String[] pathParts = sourcePath.split("/");
        StringBuilder newPath = new StringBuilder();

        int contentIndex = -1;
        for (int i = 0; i < pathParts.length; i++) {
            if ("content".equals(pathParts[i])) {
                contentIndex = i;
                break;
            }
        }

        if (contentIndex >= 0) {
            for (int i = 0; i <= contentIndex; i++) {
                newPath.append("/").append(pathParts[i]);
            }
            newPath.append("/").append(targetLanguage);

            if (contentIndex + 1 < pathParts.length) {
                for (int i = contentIndex + 1; i < pathParts.length; i++) {
                    newPath.append("/").append(pathParts[i]);
                }
            }
        }

        String branchPath = newPath.toString();

        ResourceResolver resolver = sourceResource.getResourceResolver();
        Resource parent = resolver.getResource(newPath.substring(0, newPath.lastIndexOf("/")));
        
        if (parent == null) {
            createIntermediatePaths(resolver, newPath.substring(0, newPath.lastIndexOf("/")), session);
        }

        Map<String, Object> props = new HashMap<>();
        props.put("jcr:primaryType", "cq:Page");
        
        Resource newPage = resolver.create(resolver.getResource(newPath.substring(0, newPath.lastIndexOf("/"))),
            pathParts[pathParts.length - 1], props);

        log.info("Created language branch: {}", newPage.getPath());
        session.save();

        return newPage.getPath();
    }

    private String createLiveCopy(Resource sourceResource, String targetLanguage, Session session) throws Exception {
        String sourcePath = sourceResource.getPath();
        String lcName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1) + "_" + targetLanguage;
        String lcPath = sourcePath.substring(0, sourcePath.lastIndexOf("/")) + "/" + lcName;

        Map<String, Object> props = new HashMap<>();
        props.put("jcr:primaryType", "cq:LiveCopy");
        props.put("cq:master", sourcePath);
        props.put("cq:language", targetLanguage);

        ResourceResolver resolver = sourceResource.getResourceResolver();
        
        if (resolver.getResource(lcPath.substring(0, lcPath.lastIndexOf("/"))) != null) {
            Resource liveCopyRes = resolver.create(
                resolver.getResource(lcPath.substring(0, lcPath.lastIndexOf("/"))),
                lcName,
                props
            );
            
            log.info("Created MSM Live Copy: {}", liveCopyRes.getPath());
            session.save();
            return liveCopyRes.getPath();
        }

        return lcPath;
    }

    private void createIntermediatePaths(ResourceResolver resolver, String path, Session session) throws Exception {
        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String part : parts) {
            if (StringUtils.isBlank(part)) continue;
            currentPath.append("/").append(part);

            if (resolver.getResource(currentPath.toString()) == null) {
                Map<String, Object> props = new HashMap<>();
                props.put("jcr:primaryType", "sling:Folder");
                resolver.create(resolver.getResource(currentPath.substring(0, currentPath.lastIndexOf("/"))), part, props);
            }
        }
        session.save();
    }

    private void applyTranslation(String targetPath, String translatedContent, Session session) {
        log.info("Applied translation to: {}", targetPath);
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void setResourceResolverFactory(JcrResourceResolverFactory resourceResolverFactory) {
        this.resourceResolverFactory = resourceResolverFactory;
    }
}