package com.example.aem.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.inject.Inject;

@Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
public interface HelloWorldModel {

    @Inject
    @Default(values = "World")
    String getName();

    String getMessage();
}