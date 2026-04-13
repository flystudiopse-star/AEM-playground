package com.aem.playground.core.services.analytics;

public class ContentScore {
    private final String contentPath;
    private final double overallScore;
    private final double qualityScore;
    private final double seoScore;
    private final double engagementScore;
    private final QualityDetails qualityDetails;
    private final SEODetails seoDetails;
    private final EngagementDetails engagementDetails;
    private final long timestamp;

    private ContentScore(Builder builder) {
        this.contentPath = builder.contentPath;
        this.overallScore = builder.overallScore;
        this.qualityScore = builder.qualityScore;
        this.seoScore = builder.seoScore;
        this.engagementScore = builder.engagementScore;
        this.qualityDetails = builder.qualityDetails;
        this.seoDetails = builder.seoDetails;
        this.engagementDetails = builder.engagementDetails;
        this.timestamp = builder.timestamp;
    }

    public String getContentPath() {
        return contentPath;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public double getSeoScore() {
        return seoScore;
    }

    public double getEngagementScore() {
        return engagementScore;
    }

    public QualityDetails getQualityDetails() {
        return qualityDetails;
    }

    public SEODetails getSeoDetails() {
        return seoDetails;
    }

    public EngagementDetails getEngagementDetails() {
        return engagementDetails;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String contentPath;
        private double overallScore;
        private double qualityScore;
        private double seoScore;
        private double engagementScore;
        private QualityDetails qualityDetails;
        private SEODetails seoDetails;
        private EngagementDetails engagementDetails;
        private long timestamp = System.currentTimeMillis();

        public Builder contentPath(String contentPath) {
            this.contentPath = contentPath;
            return this;
        }

        public Builder overallScore(double overallScore) {
            this.overallScore = overallScore;
            return this;
        }

        public Builder qualityScore(double qualityScore) {
            this.qualityScore = qualityScore;
            return this;
        }

        public Builder seoScore(double seoScore) {
            this.seoScore = seoScore;
            return this;
        }

        public Builder engagementScore(double engagementScore) {
            this.engagementScore = engagementScore;
            return this;
        }

        public Builder qualityDetails(QualityDetails qualityDetails) {
            this.qualityDetails = qualityDetails;
            return this;
        }

        public Builder seoDetails(SEODetails seoDetails) {
            this.seoDetails = seoDetails;
            return this;
        }

        public Builder engagementDetails(EngagementDetails engagementDetails) {
            this.engagementDetails = engagementDetails;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ContentScore build() {
            return new ContentScore(this);
        }
    }

    public static class QualityDetails {
        private final double readabilityScore;
        private final double completenessScore;
        private final double freshnessScore;
        private final double accuracyScore;

        public QualityDetails(double readabilityScore, double completenessScore, double freshnessScore, double accuracyScore) {
            this.readabilityScore = readabilityScore;
            this.completenessScore = completenessScore;
            this.freshnessScore = freshnessScore;
            this.accuracyScore = accuracyScore;
        }

        public double getReadabilityScore() {
            return readabilityScore;
        }

        public double getCompletenessScore() {
            return completenessScore;
        }

        public double getFreshnessScore() {
            return freshnessScore;
        }

        public double getAccuracyScore() {
            return accuracyScore;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private double readabilityScore;
            private double completenessScore;
            private double freshnessScore;
            private double accuracyScore;

            public Builder readabilityScore(double readabilityScore) {
                this.readabilityScore = readabilityScore;
                return this;
            }

            public Builder completenessScore(double completenessScore) {
                this.completenessScore = completenessScore;
                return this;
            }

            public Builder freshnessScore(double freshnessScore) {
                this.freshnessScore = freshnessScore;
                return this;
            }

            public Builder accuracyScore(double accuracyScore) {
                this.accuracyScore = accuracyScore;
                return this;
            }

            public QualityDetails build() {
                return new QualityDetails(readabilityScore, completenessScore, freshnessScore, accuracyScore);
            }
        }
    }

    public static class SEODetails {
        private final double titleScore;
        private final double metaDescriptionScore;
        private final double keywordScore;
        private final double structureScore;

        public SEODetails(double titleScore, double metaDescriptionScore, double keywordScore, double structureScore) {
            this.titleScore = titleScore;
            this.metaDescriptionScore = metaDescriptionScore;
            this.keywordScore = keywordScore;
            this.structureScore = structureScore;
        }

        public double getTitleScore() {
            return titleScore;
        }

        public double getMetaDescriptionScore() {
            return metaDescriptionScore;
        }

        public double getKeywordScore() {
            return keywordScore;
        }

        public double getStructureScore() {
            return structureScore;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private double titleScore;
            private double metaDescriptionScore;
            private double keywordScore;
            private double structureScore;

            public Builder titleScore(double titleScore) {
                this.titleScore = titleScore;
                return this;
            }

            public Builder metaDescriptionScore(double metaDescriptionScore) {
                this.metaDescriptionScore = metaDescriptionScore;
                return this;
            }

            public Builder keywordScore(double keywordScore) {
                this.keywordScore = keywordScore;
                return this;
            }

            public Builder structureScore(double structureScore) {
                this.structureScore = structureScore;
                return this;
            }

            public SEODetails build() {
                return new SEODetails(titleScore, metaDescriptionScore, keywordScore, structureScore);
            }
        }
    }

    public static class EngagementDetails {
        private final double interactionScore;
        private final double socialShareScore;
        private final double returnVisitScore;
        private final double conversionScore;

        public EngagementDetails(double interactionScore, double socialShareScore, double returnVisitScore, double conversionScore) {
            this.interactionScore = interactionScore;
            this.socialShareScore = socialShareScore;
            this.returnVisitScore = returnVisitScore;
            this.conversionScore = conversionScore;
        }

        public double getInteractionScore() {
            return interactionScore;
        }

        public double getSocialShareScore() {
            return socialShareScore;
        }

        public double getReturnVisitScore() {
            return returnVisitScore;
        }

        public double getConversionScore() {
            return conversionScore;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private double interactionScore;
            private double socialShareScore;
            private double returnVisitScore;
            private double conversionScore;

            public Builder interactionScore(double interactionScore) {
                this.interactionScore = interactionScore;
                return this;
            }

            public Builder socialShareScore(double socialShareScore) {
                this.socialShareScore = socialShareScore;
                return this;
            }

            public Builder returnVisitScore(double returnVisitScore) {
                this.returnVisitScore = returnVisitScore;
                return this;
            }

            public Builder conversionScore(double conversionScore) {
                this.conversionScore = conversionScore;
                return this;
            }

            public EngagementDetails build() {
                return new EngagementDetails(interactionScore, socialShareScore, returnVisitScore, conversionScore);
            }
        }
    }
}