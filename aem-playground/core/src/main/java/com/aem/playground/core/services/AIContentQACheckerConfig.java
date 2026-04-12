package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AI Content QA Checker Configuration",
    description = "Configuration for AI-powered content QA checker service"
)
public @interface AIContentQACheckerConfig {

    @AttributeDefinition(
        name = "AI Enabled",
        description = "Enable AI-powered content analysis"
    )
    boolean aiEnabled() default true;

    @AttributeDefinition(
        name = "Allowed Brand Colors",
        description = "Comma-separated list of approved brand colors (hex codes)"
    )
    String allowedBrandColors() default "#000000,#FFFFFF,#FF0000,#00FF00,#0000FF";

    @AttributeDefinition(
        name = "Allowed Fonts",
        description = "Comma-separated list of approved brand fonts"
    )
    String allowedFonts() default "Arial,Helvetica,Roboto,Open Sans";

    @AttributeDefinition(
        name = "Minimum Content Length",
        description = "Minimum recommended content length in characters"
    )
    int minContentLength() default 100;

    @AttributeDefinition(
        name = "Maximum Content Length",
        description = "Maximum recommended content length in characters"
    )
    int maxContentLength() default 10000;
}