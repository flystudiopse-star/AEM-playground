package com.example.aem.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
    service = Servlet.class,
    property = {
        "service.description=Example AEM Servlet",
        "sling.servlet.resourceTypes=sling/servlet/default",
        "sling.servlet.methods=GET",
        "sling.servlet.selectors=example",
        "sling.servlet.extensions=json"
    }
)
public class ExampleServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String json = "{\n" +
                "  \"status\": \"success\",\n" +
                "  \"message\": \"AEM Servlet is working!\",\n" +
                "  \"timestamp\": " + System.currentTimeMillis() + "\n" +
                "}";
        
        response.getWriter().write(json);
    }
}