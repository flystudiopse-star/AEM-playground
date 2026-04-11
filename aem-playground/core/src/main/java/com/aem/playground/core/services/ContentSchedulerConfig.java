package com.aem.playground.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "AI Content Scheduler Service", 
                     description = "AI-powered content scheduling service for AEM")
public @interface ContentSchedulerConfig {

    @AttributeDefinition(name = "Default timezone", 
                        description = "Default timezone for scheduling content")
    String defaultTimezone() default "America/New_York";

    @AttributeDefinition(name = "Enable caching", 
                        description = "Enable caching of scheduling analysis")
    boolean enableCache() default true;

    @AttributeDefinition(name = "Cache size", 
                        description = "Maximum number of caching entries")
    int cacheSize() default 100;

    @AttributeDefinition(name = "Default publish lead time (hours)", 
                        description = "Default lead time before publishing")
    int defaultPublishLeadTime() default 24;

    @AttributeDefinition(name = "Default unpublish lead time (days)", 
                        description = "Default lead time before unpublishing")
    int defaultUnpublishLeadTime() default 30;

    @AttributeDefinition(name = "Recommended slots per day", 
                        description = "Number of recommended time slots to return")
    int recommendedSlotsPerDay() default 5;

    @AttributeDefinition(name = "Confidence threshold", 
                        description = "Minimum confidence score to accept scheduling")
    double confidenceThreshold() default 0.7;

    @AttributeDefinition(name = "Scheduler cron expression", 
                        description = "Cron expression for scheduler updates")
    String schedulerCron() default "0 0 * * * ?";

    @AttributeDefinition(name = "Enable AEM scheduler integration", 
                        description = "Enable integration with AEM scheduler")
    boolean enableAemSchedulerIntegration() default true;
}