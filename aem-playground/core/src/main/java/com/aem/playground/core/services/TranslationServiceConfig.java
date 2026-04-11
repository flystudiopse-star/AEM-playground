package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeOption;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content Translator Service",
    description = "Configuration for AI-powered content translation service using OpenAI"
)
public @interface TranslationServiceConfig {

    @AttributeDefinition(
        name = "Enable Translation Service",
        description = "Enable the AI content translation service",
        defaultValue = "true"
    )
    boolean enableService() default true;

    @AttributeDefinition(
        name = "OpenAI API Key",
        description = "OpenAI API key for translation calls"
    )
    String apiKey();

    @AttributeDefinition(
        name = "Translation Model",
        description = "OpenAI model to use for translations",
        defaultValue = "gpt-4"
    )
    @AttributeOption(values = {"gpt-4", "gpt-4-turbo", "gpt-3.5-turbo"})
    String translationModel() default "gpt-4";

    @AttributeDefinition(
        name = "Temperature",
        description = "Temperature parameter for OpenAI API (0.0-1.0)",
        defaultValue = "0.3"
    )
    double temperature() default 0.3;

    @AttributeDefinition(
        name = "Max Tokens",
        description = "Maximum number of tokens for translation response",
        defaultValue = "4000"
    )
    int maxTokens() default 4000;

    @AttributeDefinition(
        name = "Supported Languages",
        description = "Comma-separated list of supported language codes",
        defaultValue = "en,de,fr,es,pl,it,pt,nl,ja,zh,ko"
    )
    String supportedLanguages() default "en,de,fr,es,pl,it,pt,nl,ja,zh,ko";

    @AttributeDefinition(
        name = "Default Source Language",
        description = "Default source language for translations",
        defaultValue = "en"
    )
    String defaultSourceLanguage() default "en";

    @AttributeDefinition(
        name = "Enable Translation Caching",
        description = "Cache translation results to reduce API calls",
        defaultValue = "true"
    )
    boolean enableCaching() default true;

    @AttributeDefinition(
        name = "Cache Maximum Size",
        description = "Maximum number of cached translations",
        defaultValue = "500"
    )
    int cacheMaxSize() default 500;

    @AttributeDefinition(
        name = "Use MSM Live Copies",
        description = "Create MSM live copies instead of language branches for translated content",
        defaultValue = "false"
    )
    boolean useMSMLiveCopies() default false;

    @AttributeDefinition(
        name = "Translation Prompt Template",
        description = "Custom prompt template for translation. Use {source_lang}, {target_lang}, {content} as placeholders",
        defaultValue = "Translate the following content from {source_lang} to {target_lang}. Preserve HTML tags, formatting, and structure. Only translate the text content."
    )
    String translationPromptTemplate();
}