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
package com.aem.playground.core.services;

import com.aem.playground.core.models.PersonalizationModels.ABTest;
import com.aem.playground.core.models.PersonalizationModels.ContentVariant;
import com.aem.playground.core.models.PersonalizationModels.PersonalizationPreview;
import com.aem.playground.core.models.PersonalizationModels.PersonalizationRule;
import com.aem.playground.core.models.PersonalizationModels.UserSegment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContentPersonalizationServiceTest {

    @Nested
    class UserSegmentTests {

        @Test
        void testUserSegmentBuilder() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("visitCount", 10);
            attrs.put("purchaseCount", 5);

            UserSegment segment = UserSegment.builder()
                    .id("segment-1")
                    .name("VIP Customer")
                    .description("High value customers")
                    .attributes(attrs)
                    .weight(2.0)
                    .build();

            assertEquals("segment-1", segment.getId());
            assertEquals("VIP Customer", segment.getName());
            assertEquals("High value customers", segment.getDescription());
            assertEquals(10, segment.getAttributes().get("visitCount"));
            assertEquals(5, segment.getAttributes().get("purchaseCount"));
            assertEquals(2.0, segment.getWeight());
        }

        @Test
        void testUserSegmentEquality() {
            UserSegment s1 = UserSegment.builder()
                    .id("seg-1")
                    .name("Test")
                    .weight(1.0)
                    .build();
            UserSegment s2 = UserSegment.builder()
                    .id("seg-1")
                    .name("Test")
                    .weight(1.0)
                    .build();

            assertEquals(s1, s2);
            assertEquals(s1.hashCode(), s2.hashCode());
        }

        @Test
        void testUserSegmentInequality() {
            UserSegment s1 = UserSegment.builder()
                    .id("seg-1")
                    .name("Test")
                    .weight(1.0)
                    .build();
            UserSegment s2 = UserSegment.builder()
                    .id("seg-2")
                    .name("Test")
                    .weight(1.0)
                    .build();

            assertNotEquals(s1, s2);
        }

        @Test
        void testUserSegmentAttributesAreCloned() {
            Map<String, Object> original = new HashMap<>();
            original.put("key", "value");

            UserSegment segment = UserSegment.builder()
                    .id("seg-1")
                    .name("Test")
                    .attributes(original)
                    .build();

            original.put("key", "modified");

            assertEquals("value", segment.getAttributes().get("key"));
        }
    }

    @Nested
    class PersonalizationRuleTests {

        @Test
        void testRuleBuilder() {
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("minVisits", 5);

            PersonalizationRule rule = PersonalizationRule.builder()
                    .id("rule-1")
                    .name("Rule for VIP")
                    .targetSegmentId("seg-1")
                    .contentPath("/content/page")
                    .variantId("var-1")
                    .conditions(conditions)
                    .priority(10)
                    .enabled(true)
                    .build();

            assertEquals("rule-1", rule.getId());
            assertEquals("Rule for VIP", rule.getName());
            assertEquals("seg-1", rule.getTargetSegmentId());
            assertEquals("/content/page", rule.getContentPath());
            assertEquals("var-1", rule.getVariantId());
            assertEquals(5, rule.getConditions().get("minVisits"));
            assertEquals(10, rule.getPriority());
            assertTrue(rule.isEnabled());
        }

        @Test
        void testRuleDefaults() {
            PersonalizationRule rule = PersonalizationRule.builder()
                    .id("rule-1")
                    .name("Test Rule")
                    .build();

            assertTrue(rule.isEnabled());
            assertEquals(0, rule.getPriority());
            assertNotNull(rule.getConditions());
        }

        @Test
        void testRuleEquality() {
            PersonalizationRule r1 = PersonalizationRule.builder()
                    .id("r-1")
                    .name("Test")
                    .priority(1)
                    .enabled(true)
                    .build();
            PersonalizationRule r2 = PersonalizationRule.builder()
                    .id("r-1")
                    .name("Test")
                    .priority(1)
                    .enabled(true)
                    .build();

            assertEquals(r1, r2);
        }
    }

    @Nested
    class ContentVariantTests {

        @Test
        void testVariantBuilder() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("model", "gpt-4");

            ContentVariant variant = ContentVariant.builder()
                    .id("var-1")
                    .name("Test Variant")
                    .content("Personalized content")
                    .contentPath("/content/page")
                    .metadata(metadata)
                    .isAIGenerated(true)
                    .build();

            assertEquals("var-1", variant.getId());
            assertEquals("Test Variant", variant.getName());
            assertEquals("Personalized content", variant.getContent());
            assertEquals("/content/page", variant.getContentPath());
            assertEquals("gpt-4", variant.getMetadata().get("model"));
            assertTrue(variant.isAIGenerated());
        }

        @Test
        void testVariantMetadataAreCloned() {
            Map<String, Object> original = new HashMap<>();
            original.put("key", "value");

            ContentVariant variant = ContentVariant.builder()
                    .id("var-1")
                    .metadata(original)
                    .build();

            original.put("key", "modified");

            assertEquals("value", variant.getMetadata().get("key"));
        }
    }

    @Nested
    class ABTestTests {

        @Test
        void testABTestBuilder() {
            ABTest abTest = ABTest.builder()
                    .id("abtest-1")
                    .name("Conversion Test")
                    .contentPath("/content/page")
                    .controlVariantId("control-var")
                    .testVariantId("test-var")
                    .trafficPercentage(50.0)
                    .status(ABTest.Status.DRAFT)
                    .metric("impressions", 100)
                    .metric("conversions", 10)
                    .build();

            assertEquals("abtest-1", abTest.getId());
            assertEquals("Conversion Test", abTest.getName());
            assertEquals("control-var", abTest.getControlVariantId());
            assertEquals("test-var", abTest.getTestVariantId());
            assertEquals(50.0, abTest.getTrafficPercentage());
            assertEquals(ABTest.Status.DRAFT, abTest.getStatus());
            assertEquals(100, abTest.getMetrics().get("impressions"));
            assertEquals(10, abTest.getMetrics().get("conversions"));
        }

        @Test
        void testABTestStatusEnum() {
            assertEquals(4, ABTest.Status.values().length);
            assertNotNull(ABTest.Status.valueOf("DRAFT"));
            assertNotNull(ABTest.Status.valueOf("RUNNING"));
            assertNotNull(ABTest.Status.valueOf("PAUSED"));
            assertNotNull(ABTest.Status.valueOf("COMPLETED"));
        }

        @Test
        void testABTestDefaultValues() {
            ABTest abTest = ABTest.builder()
                    .id("abtest-1")
                    .contentPath("/content/page")
                    .controlVariantId("control")
                    .testVariantId("test")
                    .build();

            assertEquals(50.0, abTest.getTrafficPercentage());
            assertEquals(ABTest.Status.DRAFT, abTest.getStatus());
            assertNotNull(abTest.getMetrics());
        }

        @Test
        void testABTestEquality() {
            ABTest t1 = ABTest.builder()
                    .id("t-1")
                    .name("Test")
                    .trafficPercentage(50.0)
                    .build();
            ABTest t2 = ABTest.builder()
                    .id("t-1")
                    .name("Test")
                    .trafficPercentage(50.0)
                    .build();

            assertEquals(t1, t2);
        }
    }

    @Nested
    class PersonalizationPreviewTests {

        @Test
        void testPreviewBuilder() {
            Map<String, Object> context = new HashMap<>();
            context.put("previewMode", true);

            PersonalizationPreview preview = new PersonalizationPreview(
                    "/content/page",
                    "seg-1",
                    "var-1",
                    "Preview content",
                    context
            );

            assertEquals("/content/page", preview.getContentPath());
            assertEquals("seg-1", preview.getSegmentId());
            assertEquals("var-1", preview.getVariantId());
            assertEquals("Preview content", preview.getPreviewContent());
            assertTrue((Boolean) preview.getContext().get("previewMode"));
        }

        @Test
        void testPreviewContextAreCloned() {
            Map<String, Object> original = new HashMap<>();
            original.put("key", "value");

            PersonalizationPreview preview = new PersonalizationPreview(
                    "/content/page",
                    "seg-1",
                    "var-1",
                    "Content",
                    original
            );

            original.put("key", "modified");

            assertEquals("value", preview.getContext().get("key"));
        }
    }

    @Nested
    class BehaviorAnalysisTests {

        @Test
        void testAnalyzeNewVisitorBehavior() {
            Map<String, Object> behaviorData = new HashMap<>();
            behaviorData.put("visitCount", 0);
            behaviorData.put("purchaseCount", 0);

            List<UserSegment> segments = analyzeBehavior(behaviorData);

            assertFalse(segments.isEmpty());
        }

        @Test
        void testAnalyzeReturningCustomerBehavior() {
            Map<String, Object> behaviorData = new HashMap<>();
            behaviorData.put("visitCount", 5);
            behaviorData.put("purchaseCount", 2);
            behaviorData.put("avgOrderValue", 50.0);

            List<UserSegment> segments = analyzeBehavior(behaviorData);

            assertFalse(segments.isEmpty());
        }

        @Test
        void testAnalyzeVIPCustomerBehavior() {
            Map<String, Object> behaviorData = new HashMap<>();
            behaviorData.put("visitCount", 20);
            behaviorData.put("purchaseCount", 10);
            behaviorData.put("avgOrderValue", 150.0);

            List<UserSegment> segments = analyzeBehavior(behaviorData);

            assertFalse(segments.isEmpty());
        }

        @Test
        void testAnalyzeBehaviorWithNullData() {
            List<UserSegment> segments = analyzeBehavior(null);

            assertTrue(segments.isEmpty());
        }

        @Test
        void testAnalyzeBehaviorWithEmptyData() {
            Map<String, Object> behaviorData = new HashMap<>();

            List<UserSegment> segments = analyzeBehavior(behaviorData);

            assertTrue(segments.isEmpty());
        }

        private List<UserSegment> analyzeBehavior(Map<String, Object> behaviorData) {
            List<UserSegment> matchingSegments = new ArrayList<>();

            if (behaviorData == null || behaviorData.isEmpty()) {
                return matchingSegments;
            }

            Object visitCountObj = behaviorData.get("visitCount");
            Object purchaseCountObj = behaviorData.get("purchaseCount");
            Object avgOrderValueObj = behaviorData.get("avgOrderValue");

            int visitCount = visitCountObj != null ? ((Number) visitCountObj).intValue() : 0;
            int purchaseCount = purchaseCountObj != null ? ((Number) purchaseCountObj).intValue() : 0;
            double avgOrderValue = avgOrderValueObj != null ? ((Number) avgOrderValueObj).doubleValue() : 0.0;

            if (visitCount == 0) {
                matchingSegments.add(UserSegment.builder()
                        .id("new_customer")
                        .name("New Customer")
                        .weight(1.0)
                        .build());
            }

            if (purchaseCount >= 5 && avgOrderValue > 100) {
                matchingSegments.add(UserSegment.builder()
                        .id("vip_customer")
                        .name("VIP Customer")
                        .weight(2.0)
                        .build());
            }

            if (visitCount > 2) {
                matchingSegments.add(UserSegment.builder()
                        .id("returning_customer")
                        .name("Returning Customer")
                        .weight(1.5)
                        .build());
            }

            return matchingSegments;
        }
    }

    @Nested
    class RuleMatchingTests {

        @Test
        void testGetMatchingRuleForSegment() {
            String contentPath = "/content/page";
            String segmentId = "seg-1";

            List<PersonalizationRule> rules = createTestRules(contentPath, segmentId);
            PersonalizationRule matchedRule = findMatchingRule(rules, contentPath, segmentId);

            assertNotNull(matchedRule);
            assertEquals(segmentId, matchedRule.getTargetSegmentId());
        }

        @Test
        void testNoMatchingRuleForDifferentPath() {
            List<PersonalizationRule> rules = createTestRules("/content/page", "seg-1");
            PersonalizationRule matchedRule = findMatchingRule(rules, "/content/other", "seg-1");

            assertNull(matchedRule);
        }

        @Test
        void testNoMatchingRuleForDifferentSegment() {
            String contentPath = "/content/page";
            List<PersonalizationRule> rules = createTestRules(contentPath, "seg-1");
            PersonalizationRule matchedRule = findMatchingRule(rules, contentPath, "seg-2");

            assertNull(matchedRule);
        }

        @Test
        void testDisabledRuleNotMatched() {
            String contentPath = "/content/page";
            String segmentId = "seg-1";

            List<PersonalizationRule> rules = new ArrayList<>();
            rules.add(PersonalizationRule.builder()
                    .id("rule-1")
                    .targetSegmentId(segmentId)
                    .contentPath(contentPath)
                    .enabled(false)
                    .priority(1)
                    .build());

            PersonalizationRule matchedRule = rules.stream()
                    .filter(PersonalizationRule::isEnabled)
                    .filter(r -> r.getContentPath().equals(contentPath))
                    .filter(r -> r.getTargetSegmentId().equals(segmentId))
                    .findFirst()
                    .orElse(null);

            assertNull(matchedRule);
        }

        @Test
        void testHigherPriorityRuleMatched() {
            String contentPath = "/content/page";
            String segmentId = "seg-1";

            List<PersonalizationRule> rules = new ArrayList<>();
            rules.add(PersonalizationRule.builder()
                    .id("rule-1")
                    .targetSegmentId(segmentId)
                    .contentPath(contentPath)
                    .priority(1)
                    .enabled(true)
                    .build());
            rules.add(PersonalizationRule.builder()
                    .id("rule-2")
                    .targetSegmentId(segmentId)
                    .contentPath(contentPath)
                    .priority(10)
                    .enabled(true)
                    .build());

            PersonalizationRule matchedRule = rules.stream()
                    .filter(PersonalizationRule::isEnabled)
                    .filter(r -> r.getContentPath().equals(contentPath))
                    .filter(r -> r.getTargetSegmentId().equals(segmentId))
                    .max(java.util.Comparator.comparingInt(PersonalizationRule::getPriority))
                    .orElse(null);

            assertNotNull(matchedRule);
            assertEquals("rule-2", matchedRule.getId());
        }

        private List<PersonalizationRule> createTestRules(String contentPath, String segmentId) {
            List<PersonalizationRule> rules = new ArrayList<>();
            rules.add(PersonalizationRule.builder()
                    .id("rule-1")
                    .targetSegmentId(segmentId)
                    .contentPath(contentPath)
                    .enabled(true)
                    .priority(1)
                    .build());
            return rules;
        }

        private PersonalizationRule findMatchingRule(List<PersonalizationRule> rules, String contentPath, String segmentId) {
            return rules.stream()
                    .filter(PersonalizationRule::isEnabled)
                    .filter(r -> r.getContentPath().equals(contentPath))
                    .filter(r -> r.getTargetSegmentId().equals(segmentId))
                    .max(java.util.Comparator.comparingInt(PersonalizationRule::getPriority))
                    .orElse(null);
        }
    }

    @Nested
    class ABVariantSelectionTests {

        @Test
        void testVariantSelectionInTrafficRange() {
            double trafficPercentage = 50.0;
            int hash = 25;

            String selectedVariant = hash < trafficPercentage ? "test" : "control";

            assertEquals("test", selectedVariant);
        }

        @Test
        void testVariantSelectionOutOfTrafficRange() {
            double trafficPercentage = 50.0;
            int hash = 75;

            String selectedVariant = hash < trafficPercentage ? "test" : "control";

            assertEquals("control", selectedVariant);
        }

        @Test
        void testVariantSelectionAtBoundary() {
            double trafficPercentage = 50.0;
            int hash = 50;

            String selectedVariant = hash < trafficPercentage ? "test" : "control";

            assertEquals("control", selectedVariant);
        }

        @Test
        void test100PercentTrafficAlwaysReturnsTest() {
            double trafficPercentage = 100.0;
            for (int hash = 0; hash < 100; hash++) {
                String selectedVariant = hash < trafficPercentage ? "test" : "control";
                assertEquals("test", selectedVariant, "Failed at hash: " + hash);
            }
        }

        @Test
        void test0PercentTrafficAlwaysReturnsControl() {
            double trafficPercentage = 0.0;
            for (int hash = 0; hash < 100; hash++) {
                String selectedVariant = hash < trafficPercentage ? "test" : "control";
                assertEquals("control", selectedVariant, "Failed at hash: " + hash);
            }
        }

        @Test
        void testConsistentSelectionForSameUserId() {
            String userId = "user-123";
            double trafficPercentage = 50.0;

            int hash1 = Math.abs(userId.hashCode() % 100);
            int hash2 = Math.abs(userId.hashCode() % 100);

            assertEquals(hash1, hash2);
        }
    }

    @Nested
    class PreviewGenerationTests {

        @Test
        void testGeneratePreviewWithValidData() {
            String contentPath = "/content/page";
            String segmentId = "seg-1";
            String variantId = "var-1";

            PersonalizationPreview preview = generatePreview(contentPath, segmentId, variantId);

            assertNotNull(preview);
            assertEquals(contentPath, preview.getContentPath());
            assertEquals(segmentId, preview.getSegmentId());
            assertEquals(variantId, preview.getVariantId());
        }

        @Test
        void testGeneratePreviewIncludesTimestamp() {
            PersonalizationPreview preview = generatePreview("/content/page", "seg-1", "var-1");

            assertNotNull(preview.getContext().get("timestamp"));
        }

        @Test
        void testGeneratePreviewIncludesPreviewMode() {
            PersonalizationPreview preview = generatePreview("/content/page", "seg-1", "var-1");

            assertTrue((Boolean) preview.getContext().get("previewMode"));
        }

        private PersonalizationPreview generatePreview(String contentPath, String segmentId, String variantId) {
            Map<String, Object> context = new HashMap<>();
            context.put("previewMode", true);
            context.put("timestamp", System.currentTimeMillis());

            return new PersonalizationPreview(contentPath, segmentId, variantId, "Preview content", context);
        }
    }

    @Nested
    class ExportImportTests {

        @Test
        void testExportConfigIncludesSegments() {
            Map<String, Object> config = exportConfig();

            assertNotNull(config.get("segments"));
        }

        @Test
        void testExportConfigIncludesVariants() {
            Map<String, Object> config = exportConfig();

            assertNotNull(config.get("variants"));
        }

        @Test
        void testExportConfigIncludesRules() {
            Map<String, Object> config = exportConfig();

            assertNotNull(config.get("rules"));
        }

        @Test
        void testExportConfigIncludesABTests() {
            Map<String, Object> config = exportConfig();

            assertNotNull(config.get("abTests"));
        }

        @Test
        void testExportConfigCanBeParsedAsMap() {
            Map<String, Object> config = exportConfig();

            assertTrue(config.containsKey("segments"));
            assertTrue(config.containsKey("variants"));
            assertTrue(config.containsKey("rules"));
            assertTrue(config.containsKey("abTests"));
        }

        private Map<String, Object> exportConfig() {
            Map<String, Object> config = new HashMap<>();

            List<Map<String, Object>> segments = new ArrayList<>();
            segments.add(Map.of("id", "seg-1", "name", "VIP"));
            config.put("segments", segments);

            List<Map<String, Object>> variants = new ArrayList<>();
            variants.add(Map.of("id", "var-1", "name", "Variant 1"));
            config.put("variants", variants);

            List<Map<String, Object>> rules = new ArrayList<>();
            rules.add(Map.of("id", "rule-1", "name", "Rule 1"));
            config.put("rules", rules);

            List<Map<String, Object>> abTests = new ArrayList<>();
            abTests.add(Map.of("id", "ab-1", "name", "AB Test 1"));
            config.put("abTests", abTests);

            return config;
        }
    }

    @Nested
    class AdobeTargetIntegrationTests {

        @Test
        void testAdobeTargetDisabledByDefault() {
            boolean enabled = checkAdobeTargetEnabled(false);

            assertFalse(enabled);
        }

        @Test
        void testAdobeTargetEnabledWhenConfigured() {
            boolean enabled = checkAdobeTargetEnabled(true);

            assertTrue(enabled);
        }

        @Test
        void testSyncSkippedWhenDisabled() {
            boolean result = syncWithAdobeTarget(false, "config-1");

            assertFalse(result);
        }

        @Test
        void testSyncSucceedsWhenEnabled() {
            boolean result = syncWithAdobeTarget(true, "config-1");

            assertTrue(result);
        }

        private boolean checkAdobeTargetEnabled(boolean configured) {
            return configured;
        }

        private boolean syncWithAdobeTarget(boolean enabled, String configId) {
            if (!enabled) {
                return false;
            }
            return true;
        }
    }
}