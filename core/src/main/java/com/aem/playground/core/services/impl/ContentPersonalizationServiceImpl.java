/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.playground.core.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.aem.playground.core.models.PersonalizationModels.ABTest;
import com.aem.playground.core.models.PersonalizationModels.ContentVariant;
import com.aem.playground.core.models.PersonalizationModels.PersonalizationPreview;
import com.aem.playground.core.models.PersonalizationModels.PersonalizationRule;
import com.aem.playground.core.models.PersonalizationModels.UserSegment;
import com.aem.playground.core.services.AIService;
import com.aem.playground.core.services.AIGenerationOptions;
import com.aem.playground.core.services.ContentPersonalizationService;

@Component(service = ContentPersonalizationService.class)
@Designate(ocd = ContentPersonalizationServiceImpl.Config.class)
public class ContentPersonalizationServiceImpl implements ContentPersonalizationService {

    @ObjectClassDefinition(name = "Content Personalization Configuration",
            description = "Configuration for AI-powered content personalization")
    public @interface Config {

        @AttributeDefinition(name = "AI Service URL", description = "URL for the AI generation service")
        String aiServiceUrl() default "https://api.openai.com/v1";

        @AttributeDefinition(name = "Default Segments", description = "Comma-separated list of default user segments")
        String defaultSegments() default "new_customer,returning_customer,vip_customer";

        @AttributeDefinition(name = "Variants per Segment", description = "Number of content variants to generate per segment")
        int variantsPerSegment() default 3;

        @AttributeDefinition(name = "Enable Adobe Target Integration", description = "Enable Adobe Target integration")
        boolean enableAdobeTarget() default false;

        @AttributeDefinition(name = "Adobe Target Client Code", description = "Adobe Target client code")
        String adobeTargetClientCode() default "";

        @AttributeDefinition(name = "Default Traffic Percentage", description = "Default traffic percentage for A/B tests")
        double defaultTrafficPercentage() default 50.0;
    }

    private Config config;
    private final Map<String, UserSegment> segments = new ConcurrentHashMap<>();
    private final Map<String, ContentVariant> variants = new ConcurrentHashMap<>();
    private final Map<String, PersonalizationRule> rules = new ConcurrentHashMap<>();
    private final Map<String, ABTest> abTests = new ConcurrentHashMap<>();

    @Reference
    private AIService aiService;

    @Activate
    public void activate(Config config) {
        this.config = config;
        initializeDefaultSegments();
    }

    private void initializeDefaultSegments() {
        String[] segmentNames = config.defaultSegments().split(",");
        for (int i = 0; i < segmentNames.length; i++) {
            String name = segmentNames[i].trim();
            UserSegment segment = UserSegment.builder()
                    .id("segment-" + i)
                    .name(name.replace("_", " "))
                    .description("Default segment for " + name)
                    .attribute("type", name)
                    .weight(1.0)
                    .build();
            segments.put(segment.getId(), segment);
        }
    }

    @Override
    public List<UserSegment> analyzeUserBehavior(String userId, Map<String, Object> behaviorData) {
        List<UserSegment> matchingSegments = new ArrayList<>();

        if (behaviorData == null || behaviorData.isEmpty()) {
            return matchingSegments;
        }

        Object visitCountObj = behaviorData.get("visitCount");
        Object purchaseCountObj = behaviorData.get("purchaseCount");
        Object avgOrderValueObj = behaviorData.get("avgOrderValue");
        Object lastVisitObj = behaviorData.get("lastVisitDaysAgo");

        int visitCount = visitCountObj != null ? ((Number) visitCountObj).intValue() : 0;
        int purchaseCount = purchaseCountObj != null ? ((Number) purchaseCountObj).intValue() : 0;
        double avgOrderValue = avgOrderValueObj != null ? ((Number) avgOrderValueObj).doubleValue() : 0.0;
        int lastVisitDaysAgo = lastVisitObj != null ? ((Number) lastVisitObj).intValue() : 0;

        if (visitCount == 0) {
            matchingSegments.add(findSegmentByType("new_customer"));
        } else if (purchaseCount >= 5 && avgOrderValue > 100) {
            matchingSegments.add(findSegmentByType("vip_customer"));
        } else if (visitCount > 2) {
            matchingSegments.add(findSegmentByType("returning_customer"));
        }

        segments.values().stream()
                .filter(s -> calculateSegmentMatch(s, behaviorData) > 0.5)
                .sorted(Comparator.comparingDouble(UserSegment::getWeight).reversed())
                .limit(3)
                .forEach(matchingSegments::add);

        return matchingSegments;
    }

    private UserSegment findSegmentByType(String type) {
        return segments.values().stream()
                .filter(s -> s.getAttributes().get("type") != null)
                .filter(s -> s.getAttributes().get("type").equals(type))
                .findFirst()
                .orElse(segments.values().iterator().next());
    }

    private double calculateSegmentMatch(UserSegment segment, Map<String, Object> behaviorData) {
        double score = 0.0;
        Map<String, Object> attrs = segment.getAttributes();

        if (attrs.containsKey("minVisits")) {
            Object minVisits = attrs.get("minVisits");
            if (minVisits != null && behaviorData.containsKey("visitCount")) {
                int visits = ((Number) behaviorData.get("visitCount")).intValue();
                if (visits >= ((Number) minVisits).intValue()) {
                    score += 0.3;
                }
            }
        }

        if (attrs.containsKey("minPurchases")) {
            Object minPurchases = attrs.get("minPurchases");
            if (minPurchases != null && behaviorData.containsKey("purchaseCount")) {
                int purchases = ((Number) behaviorData.get("purchaseCount")).intValue();
                if (purchases >= ((Number) minPurchases).intValue()) {
                    score += 0.4;
                }
            }
        }

        if (attrs.containsKey("minAvgOrderValue")) {
            Object minOrderValue = attrs.get("minAvgOrderValue");
            if (minOrderValue != null && behaviorData.containsKey("avgOrderValue")) {
                double orderValue = ((Number) behaviorData.get("avgOrderValue")).doubleValue();
                if (orderValue >= ((Number) minOrderValue).doubleValue()) {
                    score += 0.3;
                }
            }
        }

        return Math.min(score, 1.0);
    }

    @Override
    public List<UserSegment> getAvailableSegments() {
        return new ArrayList<>(segments.values());
    }

    @Override
    public UserSegment getSegmentById(String segmentId) {
        return segments.get(segmentId);
    }

    @Override
    public List<ContentVariant> generatePersonalizedVariants(String contentPath, UserSegment targetSegment,
                                                            Map<String, Object> generationOptions) {
        List<ContentVariant> generatedVariants = new ArrayList<>();
        int variantCount = config.variantsPerSegment();

        if (generationOptions != null && generationOptions.containsKey("variantCount")) {
            variantCount = ((Number) generationOptions.get("variantCount")).intValue();
        }

        for (int i = 0; i < variantCount; i++) {
            String variantName = targetSegment.getName() + " - Variant " + (i + 1);

            String prompt = String.format(
                    "Generate personalized content for segment '%s'. Create variant %d with content tailored to user attributes: %s",
                    targetSegment.getName(), i + 1, targetSegment.getAttributes()
            );

            AIGenerationOptions options = AIGenerationOptions.builder()
                .model("gpt-4")
                .maxTokens(500)
                .temperature(0.7)
                .build();

            AIService.AIGenerationResult result = aiService.generateText(prompt, options);

            String content = result.isSuccess() ? result.getContent() :
                    "Personalized content for " + targetSegment.getName() + " - Variant " + (i + 1);

            ContentVariant variant = ContentVariant.builder()
                    .id("variant-" + UUID.randomUUID().toString().substring(0, 8))
                    .name(variantName)
                    .content(content)
                    .contentPath(contentPath)
                    .metadata(Map.of(
                            "segmentId", targetSegment.getId(),
                            "generationPrompt", prompt,
                            "variantIndex", i
                    ))
                    .isAIGenerated(true)
                    .build();

            variants.put(variant.getId(), variant);
            generatedVariants.add(variant);
        }

        return generatedVariants;
    }

    @Override
    public List<PersonalizationRule> createPersonalizationRules(String contentPath,
                                                                   List<ContentVariant> variants,
                                                                   List<UserSegment> segments) {
        List<PersonalizationRule> createdRules = new ArrayList<>();

        for (ContentVariant variant : variants) {
            UserSegment matchingSegment = findSegmentForVariant(variant, segments);

            if (matchingSegment != null) {
                PersonalizationRule rule = PersonalizationRule.builder()
                        .id("rule-" + UUID.randomUUID().toString().substring(0, 8))
                        .name("Rule for " + variant.getName())
                        .targetSegmentId(matchingSegment.getId())
                        .contentPath(contentPath)
                        .variantId(variant.getId())
                        .condition("segmentMatch", true)
                        .priority(createdRules.size())
                        .enabled(true)
                        .build();

                rules.put(rule.getId(), rule);
                createdRules.add(rule);
            }
        }

        return createdRules;
    }

    private UserSegment findSegmentForVariant(ContentVariant variant, List<UserSegment> segments) {
        Map<String, Object> metadata = variant.getMetadata();
        if (metadata != null && metadata.containsKey("segmentId")) {
            String segmentId = (String) metadata.get("segmentId");
            return segments.stream()
                    .filter(s -> s.getId().equals(segmentId))
                    .findFirst()
                    .orElse(segments.isEmpty() ? null : segments.get(0));
        }
        return segments.isEmpty() ? null : segments.get(0);
    }

    @Override
    public PersonalizationRule getMatchingRule(String contentPath, UserSegment userSegment) {
        return rules.values().stream()
                .filter(r -> r.isEnabled())
                .filter(r -> r.getContentPath().equals(contentPath))
                .filter(r -> r.getTargetSegmentId().equals(userSegment.getId()))
                .sorted(Comparator.comparingInt(PersonalizationRule::getPriority).reversed())
                .findFirst()
                .orElse(null);
    }

    @Override
    public ContentVariant getPersonalizedContent(String contentPath, UserSegment userSegment) {
        PersonalizationRule matchingRule = getMatchingRule(contentPath, userSegment);

        if (matchingRule != null) {
            return variants.get(matchingRule.getVariantId());
        }

        return variants.values().stream()
                .filter(v -> v.getContentPath().equals(contentPath))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ABTest createABTest(String contentPath, String controlVariantId, String testVariantId,
                               double trafficPercentage) {
        ABTest abTest = ABTest.builder()
                .id("abtest-" + UUID.randomUUID().toString().substring(0, 8))
                .name("A/B Test for " + contentPath)
                .contentPath(contentPath)
                .controlVariantId(controlVariantId)
                .testVariantId(testVariantId)
                .trafficPercentage(trafficPercentage)
                .status(ABTest.Status.DRAFT)
                .metric("impressions", 0)
                .metric("conversions", 0)
                .build();

        abTests.put(abTest.getId(), abTest);
        return abTest;
    }

    @Override
    public ABTest getABTest(String testId) {
        return abTests.get(testId);
    }

    @Override
    public List<ABTest> getActiveABTests() {
        return abTests.values().stream()
                .filter(t -> t.getStatus() == ABTest.Status.RUNNING)
                .collect(Collectors.toList());
    }

    @Override
    public ABTest updateABTestStatus(String testId, ABTest.Status newStatus) {
        ABTest existingTest = abTests.get(testId);
        if (existingTest == null) {
            return null;
        }

        ABTest updatedTest = ABTest.builder()
                .id(existingTest.getId())
                .name(existingTest.getName())
                .contentPath(existingTest.getContentPath())
                .controlVariantId(existingTest.getControlVariantId())
                .testVariantId(existingTest.getTestVariantId())
                .trafficPercentage(existingTest.getTrafficPercentage())
                .status(newStatus)
                .startTime(newStatus == ABTest.Status.RUNNING ? System.currentTimeMillis() : existingTest.getStartTime())
                .endTime(newStatus == ABTest.Status.COMPLETED ? System.currentTimeMillis() : existingTest.getEndTime())
                .metrics(existingTest.getMetrics())
                .build();

        abTests.put(testId, updatedTest);
        return updatedTest;
    }

    @Override
    public String getVariantForUser(String testId, String userId) {
        ABTest test = abTests.get(testId);
        if (test == null || test.getStatus() != ABTest.Status.RUNNING) {
            return null;
        }

        int hash = Math.abs(userId.hashCode() % 100);
        return hash < test.getTrafficPercentage() ?
                test.getTestVariantId() : test.getControlVariantId();
    }

    @Override
    public Map<String, Object> getABTestMetrics(String testId) {
        ABTest test = abTests.get(testId);
        return test != null ? new HashMap<>(test.getMetrics()) : new HashMap<>();
    }

    @Override
    public PersonalizationPreview generatePreview(String contentPath, String segmentId, String variantId) {
        UserSegment segment = segments.get(segmentId);
        ContentVariant variant = variants.get(variantId);

        if (variant == null) {
            return null;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("previewMode", true);
        context.put("timestamp", System.currentTimeMillis());

        if (segment != null) {
            context.put("segmentAttributes", segment.getAttributes());
        }

        return new PersonalizationPreview(
                contentPath,
                segmentId,
                variantId,
                variant.getContent(),
                context
        );
    }

    @Override
    public void syncWithAdobeTarget(String configurationId) {
        if (!config.enableAdobeTarget()) {
            return;
        }
    }

    @Override
    public boolean isAdobeTargetEnabled() {
        return config != null && config.enableAdobeTarget();
    }

    @Override
    public String exportPersonalizationConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put("segments", segments.values().stream()
                .map(s -> Map.of(
                        "id", s.getId(),
                        "name", s.getName(),
                        "description", s.getDescription(),
                        "attributes", s.getAttributes(),
                        "weight", s.getWeight()
                ))
                .collect(Collectors.toList()));

        config.put("variants", variants.values().stream()
                .map(v -> Map.of(
                        "id", v.getId(),
                        "name", v.getName(),
                        "contentPath", v.getContentPath(),
                        "isAIGenerated", v.isAIGenerated()
                ))
                .collect(Collectors.toList()));

        config.put("rules", rules.values().stream()
                .map(r -> Map.of(
                        "id", r.getId(),
                        "name", r.getName(),
                        "targetSegmentId", r.getTargetSegmentId(),
                        "contentPath", r.getContentPath(),
                        "variantId", r.getVariantId(),
                        "enabled", r.isEnabled()
                ))
                .collect(Collectors.toList()));

        config.put("abTests", abTests.values().stream()
                .map(t -> Map.of(
                        "id", t.getId(),
                        "name", t.getName(),
                        "contentPath", t.getContentPath(),
                        "status", t.getStatus().name(),
                        "trafficPercentage", t.getTrafficPercentage()
                ))
                .collect(Collectors.toList()));

        return config.toString();
    }

    @Override
    public void importPersonalizationConfig(String configJson) {
    }
}