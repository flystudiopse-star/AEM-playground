package com.aem.playground.core.services;

import java.util.ArrayList;
import java.util.List;

public class SEOMetadata {

    private String metaTitle;
    private String metaDescription;
    private List<String> keywords = new ArrayList<>();

    private String ogTitle;
    private String ogDescription;
    private String ogImage;
    private String ogUrl;
    private String ogType;

    private String twitterCard;
    private String twitterTitle;
    private String twitterDescription;
    private String twitterImage;

    private String schemaOrgJsonLd;

    private int seoScore;
    private List<String> seoRecommendations = new ArrayList<>();

    public SEOMetadata() {
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void addKeyword(String keyword) {
        this.keywords.add(keyword);
    }

    public String getOgTitle() {
        return ogTitle;
    }

    public void setOgTitle(String ogTitle) {
        this.ogTitle = ogTitle;
    }

    public String getOgDescription() {
        return ogDescription;
    }

    public void setOgDescription(String ogDescription) {
        this.ogDescription = ogDescription;
    }

    public String getOgImage() {
        return ogImage;
    }

    public void setOgImage(String ogImage) {
        this.ogImage = ogImage;
    }

    public String getOgUrl() {
        return ogUrl;
    }

    public void setOgUrl(String ogUrl) {
        this.ogUrl = ogUrl;
    }

    public String getOgType() {
        return ogType;
    }

    public void setOgType(String ogType) {
        this.ogType = ogType;
    }

    public String getTwitterCard() {
        return twitterCard;
    }

    public void setTwitterCard(String twitterCard) {
        this.twitterCard = twitterCard;
    }

    public String getTwitterTitle() {
        return twitterTitle;
    }

    public void setTwitterTitle(String twitterTitle) {
        this.twitterTitle = twitterTitle;
    }

    public String getTwitterDescription() {
        return twitterDescription;
    }

    public void setTwitterDescription(String twitterDescription) {
        this.twitterDescription = twitterDescription;
    }

    public String getTwitterImage() {
        return twitterImage;
    }

    public void setTwitterImage(String twitterImage) {
        this.twitterImage = twitterImage;
    }

    public String getSchemaOrgJsonLd() {
        return schemaOrgJsonLd;
    }

    public void setSchemaOrgJsonLd(String schemaOrgJsonLd) {
        this.schemaOrgJsonLd = schemaOrgJsonLd;
    }

    public int getSeoScore() {
        return seoScore;
    }

    public void setSeoScore(int seoScore) {
        this.seoScore = Math.max(0, Math.min(100, seoScore));
    }

    public List<String> getSeoRecommendations() {
        return seoRecommendations;
    }

    public void setSeoRecommendations(List<String> seoRecommendations) {
        this.seoRecommendations = seoRecommendations;
    }

    public void addSeoRecommendation(String recommendation) {
        this.seoRecommendations.add(recommendation);
    }

    public static class Builder {
        private final SEOMetadata metadata = new SEOMetadata();

        public Builder metaTitle(String title) {
            metadata.metaTitle = title;
            return this;
        }

        public Builder metaDescription(String description) {
            metadata.metaDescription = description;
            return this;
        }

        public Builder keywords(List<String> keywords) {
            metadata.keywords = keywords;
            return this;
        }

        public Builder ogTitle(String title) {
            metadata.ogTitle = title;
            return this;
        }

        public Builder ogDescription(String description) {
            metadata.ogDescription = description;
            return this;
        }

        public Builder ogImage(String image) {
            metadata.ogImage = image;
            return this;
        }

        public Builder ogUrl(String url) {
            metadata.ogUrl = url;
            return this;
        }

        public Builder ogType(String type) {
            metadata.ogType = type;
            return this;
        }

        public Builder twitterCard(String card) {
            metadata.twitterCard = card;
            return this;
        }

        public Builder twitterTitle(String title) {
            metadata.twitterTitle = title;
            return this;
        }

        public Builder twitterDescription(String description) {
            metadata.twitterDescription = description;
            return this;
        }

        public Builder twitterImage(String image) {
            metadata.twitterImage = image;
            return this;
        }

        public Builder schemaOrgJsonLd(String jsonLd) {
            metadata.schemaOrgJsonLd = jsonLd;
            return this;
        }

        public Builder seoScore(int score) {
            metadata.seoScore = Math.max(0, Math.min(100, score));
            return this;
        }

        public Builder seoRecommendations(List<String> recommendations) {
            metadata.seoRecommendations = recommendations;
            return this;
        }

        public SEOMetadata build() {
            return metadata;
        }
    }
}