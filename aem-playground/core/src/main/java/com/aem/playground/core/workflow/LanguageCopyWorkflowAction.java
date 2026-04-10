package com.aem.playground.core.workflow;

import com.aem.playground.core.services.TranslationService;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.model.WorkflowData;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.payload.Payload;
import com.day.cq.workflow.payload.URLPayload;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(service = WorkflowProcess.class, property = {
        "process.label=AI Language Copy"
})
public class LanguageCopyWorkflowAction implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(LanguageCopyWorkflowAction.class);

    private static final String SOURCE_LANGUAGE_PARAM = "sourceLanguage";
    private static final String TARGET_LANGUAGES_PARAM = "targetLanguages";
    private static final String TRANSLATE_METADATA = "translateMetadata";
    private static final String TRANSLATE_COMPONENTS = "translateComponents";
    private static final String CREATE_LANGUAGE_COPY = "createLanguageCopy";

    @Reference
    private TranslationService translationService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, Map<String, Object> args) throws WorkflowException {
        log.info("Starting AI Language Copy workflow execution");

        WorkflowData workflowData = workItem.getWorkflowData();
        Payload payload = workflowData.getPayload();
        if (payload == null) {
            throw new WorkflowException("Workflow payload is null");
        }

        String payloadPath = getPayloadPath(payload);
        if (payloadPath == null) {
            throw new WorkflowException("Could not resolve payload path");
        }

        String sourceLanguage = getArgValue(args, SOURCE_LANGUAGE_PARAM, "en");
        String targetLanguagesArg = getArgValue(args, TARGET_LANGUAGES_PARAM, "de,fr,es");
        List<String> targetLanguages = parseTargetLanguages(targetLanguagesArg);

        boolean translateMetadata = getBooleanArgValue(args, TRANSLATE_METADATA, true);
        boolean translateComponents = getBooleanArgValue(args, TRANSLATE_COMPONENTS, true);
        boolean createLanguageCopy = getBooleanArgValue(args, CREATE_LANGUAGE_COPY, true);

        ResourceResolver resolver = workflowSession.getSession().getResourceResolver();
        Resource payloadResource = resolver.getResource(payloadPath);

        if (payloadResource == null) {
            throw new WorkflowException("Payload resource not found: " + payloadPath);
        }

        for (String targetLanguage : targetLanguages) {
            try {
                processLanguageTranslation(workItem, workflowSession, payloadResource, sourceLanguage, targetLanguage,
                        translateMetadata, translateComponents, createLanguageCopy, resolver);
            } catch (Exception e) {
                log.error("Failed to translate to {}: {}", targetLanguage, e.getMessage());
            }
        }

        log.info("AI Language Copy workflow completed");
    }

    private void processLanguageTranslation(WorkItem workItem, WorkflowSession workflowSession,
            Resource sourceResource, String sourceLanguage, String targetLanguage,
            boolean translateMetadata, boolean translateComponents, boolean createLanguageCopy,
            ResourceResolver resolver) throws WorkflowException {

        if (createLanguageCopy) {
            Resource languageCopy = translationService.createLanguageCopy(sourceResource, targetLanguage, resolver);
            if (languageCopy != null) {
                log.info("Created language copy for {} at {}", targetLanguage, languageCopy.getPath());
                addWorkflowData(workItem, "languageCopy_" + targetLanguage, languageCopy.getPath());
            }
        }

        if (translateMetadata) {
            Map<String, String> metadata = extractMetadata(sourceResource);
            Map<String, String> translatedMetadata = translationService.translateMetadata(metadata, sourceLanguage, targetLanguage);
            log.info("Translated metadata to {}", targetLanguage);
            addWorkflowData(workItem, "metadata_" + targetLanguage, translatedMetadata.toString());
        }

        if (translateComponents) {
            List<TranslationService.TranslationResult> results = translateComponentContentRecursive(sourceResource, sourceLanguage, targetLanguage);
            long successCount = results.stream().filter(TranslationService.TranslationResult::isSuccess).count();
            log.info("Translated {} components to {}", successCount, targetLanguage);
            addWorkflowData(workItem, "components_" + targetLanguage, String.valueOf(successCount));
        }
    }

    private List<TranslationService.TranslationResult> translateComponentContentRecursive(Resource resource, String sourceLanguage, String targetLanguage) {
        List<TranslationService.TranslationResult> results = new ArrayList<>();

        if (resource == null) {
            return results;
        }

        List<Resource> children = resource.getChildren();
        for (Resource child : children) {
            List<TranslationService.TranslationResult> componentResults =
                    translationService.translateComponentContent(child, sourceLanguage, targetLanguage);
            results.addAll(componentResults);

            results.addAll(translateComponentContentRecursive(child, sourceLanguage, targetLanguage));
        }

        return results;
    }

    private Map<String, String> extractMetadata(Resource resource) {
        Map<String, String> metadata = new HashMap<>();
        Resource contentResource = resource.getChild("jcr:content");
        if (contentResource == null) {
            contentResource = resource;
        }

        Map<String, Object> props = contentResource.getValueMap();
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            if (entry.getValue() instanceof String) {
                metadata.put(entry.getKey(), (String) entry.getValue());
            }
        }

        return metadata;
    }

    private String getPayloadPath(Payload payload) {
        if (payload instanceof URLPayload) {
            return ((URLPayload) payload).getPath();
        }
        return payload.getPath();
    }

    private String getArgValue(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }

    private boolean getBooleanArgValue(Map<String, Object> args, String key, boolean defaultValue) {
        Object value = args.get(key);
        if (value != null) {
            return Boolean.parseBoolean(value.toString());
        }
        return defaultValue;
    }

    private List<String> parseTargetLanguages(String languagesArg) {
        if (StringUtils.isBlank(languagesArg)) {
            return Arrays.asList("de");
        }
        return Arrays.asList(languagesArg.split(","));
    }

    private void addWorkflowData(WorkItem workItem, String key, String value) {
    }

    public void setTranslationService(TranslationService service) {
        this.translationService = service;
    }
}