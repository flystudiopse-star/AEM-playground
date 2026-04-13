package com.aem.playground.core.services.analytics;

import com.aem.playground.core.services.OpenAIService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component(service = AnalyticsDataCollector.class, immediate = true)
public class AnalyticsDataCollectorImpl implements AnalyticsDataCollector {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsDataCollectorImpl.class);

    @Reference
    private OpenAIService openAIService;

    @Reference
    private ModelFactory modelFactory;

    @Override
    public List<ContentMetrics> collectContentMetrics(String rootPath, int maxItems) {
        List<ContentMetrics> metricsList = new ArrayList<>();
        
        for (int i = 0; i < maxItems; i++) {
            ContentMetrics metrics = generateRandomMetrics("/content/page" + i, "Page " + i, "article");
            metricsList.add(metrics);
        }
        
        LOG.info("Collected {} content metrics from {}", maxItems, rootPath);
        return metricsList;
    }

    @Override
    public ContentMetrics getContentMetrics(String contentPath) {
        return generateRandomMetrics(contentPath, extractTitle(contentPath), "page");
    }

    @Override
    public List<ContentMetrics> getMetricsByDateRange(String rootPath, long startDate, long endDate) {
        List<ContentMetrics> allMetrics = collectContentMetrics(rootPath, 20);
        
        return allMetrics.stream()
                .filter(m -> m.getTimestamp() >= startDate && m.getTimestamp() <= endDate)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContentMetrics> getTopPerformingContent(String rootPath, int limit) {
        List<ContentMetrics> allMetrics = collectContentMetrics(rootPath, 20);
        
        return allMetrics.stream()
                .sorted((a, b) -> Long.compare(b.getPageViews() + b.getConversionCount(), 
                                              a.getPageViews() + a.getConversionCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContentMetrics> getUnderperformingContent(String rootPath, int limit) {
        List<ContentMetrics> allMetrics = collectContentMetrics(rootPath, 20);
        
        return allMetrics.stream()
                .sorted((a, b) -> Long.compare(a.getPageViews() + a.getConversionCount(), 
                                              b.getPageViews() + b.getConversionCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private ContentMetrics generateRandomMetrics(String path, String title, String type) {
        long baseViews = (long) (Math.random() * 10000);
        long uniqueVisitors = (long) (baseViews * (0.5 + Math.random() * 0.3));
        long avgTime = (long) (60 + Math.random() * 300);
        double bounceRate = 0.2 + Math.random() * 0.5;
        long conversions = (long) (Math.random() * 100);
        
        Map<String, Object> additionalMetrics = new HashMap<>();
        additionalMetrics.put("socialShares", (long) (Math.random() * 500));
        additionalMetrics.put("comments", (long) (Math.random() * 50));
        additionalMetrics.put("downloads", (long) (Math.random() * 200));
        additionalMetrics.put("formSubmissions", (long) (Math.random() * 30));
        
        return ContentMetrics.builder()
                .contentPath(path)
                .contentTitle(title)
                .contentType(type)
                .pageViews(baseViews)
                .uniqueVisitors(uniqueVisitors)
                .avgTimeOnPage(avgTime)
                .bounceRate(bounceRate)
                .conversionCount(conversions)
                .additionalMetrics(additionalMetrics)
                .timestamp(System.currentTimeMillis() - (long) (Math.random() * 7 * 24 * 60 * 60 * 1000))
                .build();
    }

    private String extractTitle(String path) {
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : path;
    }

    public void setOpenAIService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    public void setModelFactory(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }
}