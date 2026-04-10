package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "SEO Optimizer Service Configuration")
public @interface SEOOptimizerServiceConfig {

    @AttributeDefinition(name = "Default Language", description = "Default language code for SEO metadata generation (e.g., en, de, fr)")
    String defaultLanguage() default "en";

    @AttributeDefinition(name = "Generate Schema.org", description = "Enable Schema.org JSON-LD structured data generation")
    boolean generateSchemaEnabled() default true;

    @AttributeDefinition(name = "Enable OpenGraph", description = "Enable OpenGraph metadata generation")
    boolean openGraphEnabled() default true;

    @AttributeDefinition(name = "Enable Twitter Cards", description = "Enable Twitter Card metadata generation")
    boolean twitterCardsEnabled() default true;

    @AttributeDefinition(name = "Cache Max Size", description = "Maximum number of cached SEO results")
    int cacheMaxSize() default 100;
}