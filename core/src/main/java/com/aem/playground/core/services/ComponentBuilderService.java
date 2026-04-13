package com.aem.playground.core.services;

import java.util.List;
import com.aem.playground.core.services.dto.ComponentDescriptor;

public interface ComponentBuilderService {

    String COMPONENT_DESCRIPTION = "service.description";

    ComponentDescriptor buildComponent(String componentName, String description, boolean responsive, boolean includeCrud);

    List<String> generateFields(String description);

    String generateDialogXml(List<String> fields);

    String generateContentXml(String componentName, String title, String group);

    String generateHtTemplate(String componentName, List<String> fields, boolean responsive);

    String generateSlingModel(String componentName, List<String> fields, boolean includeCrud);

    String generateCss(String componentName, boolean responsive);
}