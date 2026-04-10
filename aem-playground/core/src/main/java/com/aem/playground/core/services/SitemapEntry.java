package com.aem.playground.core.services;

import java.time.ZonedDateTime;

public class SitemapEntry {

    private String loc;
    private ZonedDateTime lastmod;
    private String changefreq;
    private String priority;
    private String alternates;

    public SitemapEntry() {
    }

    public SitemapEntry(String loc) {
        this.loc = loc;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public ZonedDateTime getLastmod() {
        return lastmod;
    }

    public void setLastmod(ZonedDateTime lastmod) {
        this.lastmod = lastmod;
    }

    public String getChangefreq() {
        return changefreq;
    }

    public void setChangefreq(String changefreq) {
        this.changefreq = changefreq;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAlternates() {
        return alternates;
    }

    public void setAlternates(String alternates) {
        this.alternates = alternates;
    }

    public static class Builder {
        private final SitemapEntry entry = new SitemapEntry();

        public Builder loc(String loc) {
            entry.loc = loc;
            return this;
        }

        public Builder lastmod(ZonedDateTime lastmod) {
            entry.lastmod = lastmod;
            return this;
        }

        public Builder changefreq(String changefreq) {
            entry.changefreq = changefreq;
            return this;
        }

        public Builder priority(String priority) {
            entry.priority = priority;
            return this;
        }

        public Builder alternates(String alternates) {
            entry.alternates = alternates;
            return this;
        }

        public SitemapEntry build() {
            return entry;
        }
    }
}