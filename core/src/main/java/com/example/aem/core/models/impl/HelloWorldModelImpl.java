package com.example.aem.core.models.impl;

import com.example.aem.core.models.HelloWorldModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.factory.ModelFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Model(adaptables = {SlingHttpServletRequest.class, org.apache.sling.api.resource.Resource.class})
public class HelloWorldModelImpl implements HelloWorldModel {

    @Inject
    @Self
    private HelloWorldModel self;

    @Inject
    private org.apache.sling.api.resource.Resource resource;

    @Inject
    @Optional
    private String name;

    private String message;

    @Override
    public String getName() {
        return name != null ? name : "World";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @PostConstruct
    protected void init() {
        message = "Hello, " + getName() + "! Welcome to AEM.";
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getAsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", getName());
        map.put("message", message);
        return map;
    }
}