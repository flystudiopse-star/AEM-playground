package com.aem.playground.core.services.dto;

import java.util.List;
import java.util.Map;

public class TimezoneAwareSchedule {

    private String scheduleId;
    private String contentPath;
    private String sourceTimezone;
    private String targetTimezone;
    private long originalPublishTime;
    private long adjustedPublishTime;
    private List<TimezoneConversion> conversions;
    private boolean isDaylightSavingTime;
    private Map<String, Object> metadata;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getSourceTimezone() {
        return sourceTimezone;
    }

    public void setSourceTimezone(String sourceTimezone) {
        this.sourceTimezone = sourceTimezone;
    }

    public String getTargetTimezone() {
        return targetTimezone;
    }

    public void setTargetTimezone(String targetTimezone) {
        this.targetTimezone = targetTimezone;
    }

    public long getOriginalPublishTime() {
        return originalPublishTime;
    }

    public void setOriginalPublishTime(long originalPublishTime) {
        this.originalPublishTime = originalPublishTime;
    }

    public long getAdjustedPublishTime() {
        return adjustedPublishTime;
    }

    public void setAdjustedPublishTime(long adjustedPublishTime) {
        this.adjustedPublishTime = adjustedPublishTime;
    }

    public List<TimezoneConversion> getConversions() {
        return conversions;
    }

    public void setConversions(List<TimezoneConversion> conversions) {
        this.conversions = conversions;
    }

    public boolean isDaylightSavingTime() {
        return isDaylightSavingTime;
    }

    public void setDaylightSavingTime(boolean daylightSavingTime) {
        isDaylightSavingTime = daylightSavingTime;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TimezoneAwareSchedule schedule = new TimezoneAwareSchedule();

        public Builder scheduleId(String scheduleId) {
            schedule.scheduleId = scheduleId;
            return this;
        }

        public Builder contentPath(String contentPath) {
            schedule.contentPath = contentPath;
            return this;
        }

        public Builder sourceTimezone(String sourceTimezone) {
            schedule.sourceTimezone = sourceTimezone;
            return this;
        }

        public Builder targetTimezone(String targetTimezone) {
            schedule.targetTimezone = targetTimezone;
            return this;
        }

        public Builder originalPublishTime(long originalPublishTime) {
            schedule.originalPublishTime = originalPublishTime;
            return this;
        }

        public Builder adjustedPublishTime(long adjustedPublishTime) {
            schedule.adjustedPublishTime = adjustedPublishTime;
            return this;
        }

        public Builder conversions(List<TimezoneConversion> conversions) {
            schedule.conversions = conversions;
            return this;
        }

        public Builder daylightSavingTime(boolean daylightSavingTime) {
            schedule.isDaylightSavingTime = daylightSavingTime;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            schedule.metadata = metadata;
            return this;
        }

        public TimezoneAwareSchedule build() {
            return schedule;
        }
    }

    public static class TimezoneConversion {
        private String timezoneId;
        private String timezoneName;
        private int offsetHours;
        private long convertedTime;
        private String localTime;

        public String getTimezoneId() {
            return timezoneId;
        }

        public void setTimezoneId(String timezoneId) {
            this.timezoneId = timezoneId;
        }

        public String getTimezoneName() {
            return timezoneName;
        }

        public void setTimezoneName(String timezoneName) {
            this.timezoneName = timezoneName;
        }

        public int getOffsetHours() {
            return offsetHours;
        }

        public void setOffsetHours(int offsetHours) {
            this.offsetHours = offsetHours;
        }

        public long getConvertedTime() {
            return convertedTime;
        }

        public void setConvertedTime(long convertedTime) {
            this.convertedTime = convertedTime;
        }

        public String getLocalTime() {
            return localTime;
        }

        public void setLocalTime(String localTime) {
            this.localTime = localTime;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private TimezoneConversion conversion = new TimezoneConversion();

            public Builder timezoneId(String timezoneId) {
                conversion.timezoneId = timezoneId;
                return this;
            }

            public Builder timezoneName(String timezoneName) {
                conversion.timezoneName = timezoneName;
                return this;
            }

            public Builder offsetHours(int offsetHours) {
                conversion.offsetHours = offsetHours;
                return this;
            }

            public Builder convertedTime(long convertedTime) {
                conversion.convertedTime = convertedTime;
                return this;
            }

            public Builder localTime(String localTime) {
                conversion.localTime = localTime;
                return this;
            }

            public TimezoneConversion build() {
                return conversion;
            }
        }
    }
}