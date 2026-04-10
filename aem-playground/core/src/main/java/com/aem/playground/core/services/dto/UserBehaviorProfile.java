package com.aem.playground.core.services.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserBehaviorProfile {

    private String userId;
    private String sessionId;
    private List<String> viewedPages;
    private List<String> likedContent;
    private List<String> sharedContent;
    private List<String> searchQueries;
    private Map<String, Integer> tagPreferences;
    private Map<String, Integer> categoryPreferences;
    private double averageSessionDuration;
    private int totalPageViews;
    private LocalDateTime lastActiveAt;
    private String userSegment;
    private Map<String, Object> customProperties;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getViewedPages() {
        return viewedPages;
    }

    public void setViewedPages(List<String> viewedPages) {
        this.viewedPages = viewedPages;
    }

    public List<String> getLikedContent() {
        return likedContent;
    }

    public void setLikedContent(List<String> likedContent) {
        this.likedContent = likedContent;
    }

    public List<String> getSharedContent() {
        return sharedContent;
    }

    public void setSharedContent(List<String> sharedContent) {
        this.sharedContent = sharedContent;
    }

    public List<String> getSearchQueries() {
        return searchQueries;
    }

    public void setSearchQueries(List<String> searchQueries) {
        this.searchQueries = searchQueries;
    }

    public Map<String, Integer> getTagPreferences() {
        return tagPreferences;
    }

    public void setTagPreferences(Map<String, Integer> tagPreferences) {
        this.tagPreferences = tagPreferences;
    }

    public Map<String, Integer> getCategoryPreferences() {
        return categoryPreferences;
    }

    public void setCategoryPreferences(Map<String, Integer> categoryPreferences) {
        this.categoryPreferences = categoryPreferences;
    }

    public double getAverageSessionDuration() {
        return averageSessionDuration;
    }

    public void setAverageSessionDuration(double averageSessionDuration) {
        this.averageSessionDuration = averageSessionDuration;
    }

    public int getTotalPageViews() {
        return totalPageViews;
    }

    public void setTotalPageViews(int totalPageViews) {
        this.totalPageViews = totalPageViews;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public String getUserSegment() {
        return userSegment;
    }

    public void setUserSegment(String userSegment) {
        this.userSegment = userSegment;
    }

    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
    }

    public static UserBehaviorProfile create(String userId) {
        UserBehaviorProfile profile = new UserBehaviorProfile();
        profile.setUserId(userId);
        profile.setLastActiveAt(LocalDateTime.now());
        return profile;
    }
}
