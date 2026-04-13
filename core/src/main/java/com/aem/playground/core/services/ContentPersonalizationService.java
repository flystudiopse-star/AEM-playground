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

import java.util.List;
import java.util.Map;

public interface ContentPersonalizationService {

    List<UserSegment> analyzeUserBehavior(String userId, Map<String, Object> behaviorData);

    List<UserSegment> getAvailableSegments();

    UserSegment getSegmentById(String segmentId);

    List<ContentVariant> generatePersonalizedVariants(String contentPath, UserSegment targetSegment,
                                                      Map<String, Object> generationOptions);

    List<PersonalizationRule> createPersonalizationRules(String contentPath, List<ContentVariant> variants,
                                                         List<UserSegment> segments);

    PersonalizationRule getMatchingRule(String contentPath, UserSegment userSegment);

    ContentVariant getPersonalizedContent(String contentPath, UserSegment userSegment);

    ABTest createABTest(String contentPath, String controlVariantId, String testVariantId,
                        double trafficPercentage);

    ABTest getABTest(String testId);

    List<ABTest> getActiveABTests();

    ABTest updateABTestStatus(String testId, ABTest.Status newStatus);

    String getVariantForUser(String testId, String userId);

    Map<String, Object> getABTestMetrics(String testId);

    PersonalizationPreview generatePreview(String contentPath, String segmentId, String variantId);

    void syncWithAdobeTarget(String configurationId);

    boolean isAdobeTargetEnabled();

    String exportPersonalizationConfig();

    void importPersonalizationConfig(String configJson);
}