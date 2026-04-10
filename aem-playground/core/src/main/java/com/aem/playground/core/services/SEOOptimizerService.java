package com.aem.playground.core.services;

import java.util.List;

public interface SEOOptimizerService {

    SEOMetadata generateMetadata(String pageContent, String pageTitle, String pagePath);

    SEOMetadata generateMetadata(String pageContent, String pageTitle, String pagePath, String language);

    String generateSitemapXml(List<SitemapEntry> entries);

    String generateSitemapXml(List<SitemapEntry> entries, String baseUrl);

    SEOMetadata calculateSeoScore(SEOMetadata metadata);

    String generateSchemaOrgJsonLd(String pageTitle, String pageDescription, String pageUrl, String pageType);

    SEOMetadata generateOpenGraphMetadata(String pageContent, String pageTitle, String pageUrl);

    SEOMetadata generateTwitterCardMetadata(String pageContent, String pageTitle, String pageUrl);
}