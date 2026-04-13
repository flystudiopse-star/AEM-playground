package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.WorkflowAutomationService;
import com.aem.playground.core.services.WorkflowAutomationServiceConfig;
import com.aem.playground.core.services.dto.*;
import com.aem.playground.core.services.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class WorkflowAutomationServiceTest {

    @Mock
    private AIService aiService;

    private WorkflowAutomationService workflowService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        workflowService = new WorkflowAutomationServiceImpl();
        
        WorkflowAutomationServiceConfig config = new TestWorkflowAutomationServiceConfig();
        
        workflowService.activate(config);
    }

    @Test
    void testSuggestWorkflowActionsWithValidContent() {
        List<WorkflowAction> actions = workflowService.suggestWorkflowActions("/content/pages/home");

        assertNotNull(actions);
    }

    @Test
    void testSuggestWorkflowActionsWithNullPath() {
        List<WorkflowAction> actions = workflowService.suggestWorkflowActions(null);

        assertNotNull(actions);
        assertTrue(actions.isEmpty());
    }

    @Test
    void testAnalyzeContentForWorkflow() {
        ContentAnalysisResult analysis = workflowService.analyzeContentForWorkflow("/content/pages/home");

        assertNotNull(analysis);
        assertEquals("/content/pages/home", analysis.getContentPath());
    }

    @Test
    void testAnalyzeContentForWorkflowWithNullPath() {
        ContentAnalysisResult analysis = workflowService.analyzeContentForWorkflow(null);

        assertNotNull(analysis);
    }

    @Test
    void testCanAutoApprove() {
        boolean canApprove = workflowService.canAutoApprove("/content/pages/simple");

        assertNotNull(canApprove);
    }

    @Test
    void testAutoApproveContent() {
        boolean approved = workflowService.autoApproveContent("/content/pages/simple");

        assertNotNull(approved);
    }

    @Test
    void testRouteContentForReview() {
        WorkflowRouteDecision decision = workflowService.routeContentForReview("/content/pages/home");

        assertNotNull(decision);
        assertEquals("/content/pages/home", decision.getContentPath());
    }

    @Test
    void testRouteContentForReviewWithNullPath() {
        WorkflowRouteDecision decision = workflowService.routeContentForReview(null);

        assertNotNull(decision);
    }

    @Test
    void testDetectContentIssues() {
        List<ContentIssue> issues = workflowService.detectContentIssues("/content/pages/home");

        assertNotNull(issues);
    }

    @Test
    void testDetectContentIssuesWithNullPath() {
        List<ContentIssue> issues = workflowService.detectContentIssues(null);

        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void testTriggerCorrectionWorkflowWithAutoCorrectable() {
        ContentIssue issue = ContentIssue.builder()
                .issueId("issue-1")
                .contentPath("/content/pages/home")
                .autoCorrectable(true)
                .build();

        boolean triggered = workflowService.triggerCorrectionWorkflow(issue);

        assertNotNull(triggered);
    }

    @Test
    void testTriggerCorrectionWorkflowWithNonCorrectable() {
        ContentIssue issue = ContentIssue.builder()
                .issueId("issue-1")
                .contentPath("/content/pages/home")
                .autoCorrectable(false)
                .build();

        boolean triggered = workflowService.triggerCorrectionWorkflow(issue);

        assertFalse(triggered);
    }

    @Test
    void testGetWorkflowPerformanceAnalytics() {
        List<WorkflowPerformanceMetrics> metrics = workflowService.getWorkflowPerformanceAnalytics("workflow-1");

        assertNotNull(metrics);
    }

    @Test
    void testGetAggregatePerformanceMetrics() {
        WorkflowPerformanceMetrics metrics = workflowService.getAggregatePerformanceMetrics();

        assertNotNull(metrics);
    }

    @Test
    void testLaunchWorkflow() {
        String workflowId = workflowService.launchWorkflow("models/content-approval", "/content/pages/home");

        assertNotNull(workflowId);
    }

    @Test
    void testLaunchWorkflowWithNullInputs() {
        String workflowId = workflowService.launchWorkflow(null, null);

        assertNull(workflowId);
    }

    @Test
    void testWorkflowActionBuilder() {
        WorkflowAction action = WorkflowAction.builder()
                .actionId("action-1")
                .actionType("approve")
                .actionName("Approve Content")
                .description("Approve the content")
                .confidenceScore(0.9)
                .workflowModelId("models/content-approval")
                .autoExecute(true)
                .priority(1)
                .build();

        assertNotNull(action);
        assertEquals("action-1", action.getActionId());
        assertEquals("approve", action.getActionType());
        assertEquals("Approve Content", action.getActionName());
        assertEquals(0.9, action.getConfidenceScore());
        assertEquals("models/content-approval", action.getWorkflowModelId());
        assertTrue(action.isAutoExecute());
        assertEquals(1, action.getPriority());
    }

    @Test
    void testContentAnalysisResultBuilder() {
        ContentAnalysisResult analysis = ContentAnalysisResult.builder()
                .contentPath("/content/pages/home")
                .contentType("page")
                .analysisId("analysis-1")
                .complexityScore(0.3)
                .isSimpleChange(true)
                .riskScore(0.2)
                .riskLevel("low")
                .build();

        assertNotNull(analysis);
        assertEquals("/content/pages/home", analysis.getContentPath());
        assertEquals("page", analysis.getContentType());
        assertEquals("analysis-1", analysis.getAnalysisId());
        assertEquals(0.3, analysis.getComplexityScore());
        assertTrue(analysis.isSimpleChange());
        assertEquals(0.2, analysis.getRiskScore());
        assertEquals("low", analysis.getRiskLevel());
    }

    @Test
    void testWorkflowRouteDecisionBuilder() {
        WorkflowRouteDecision decision = WorkflowRouteDecision.builder()
                .decisionId("route-1")
                .contentPath("/content/pages/home")
                .targetWorkflow("models/content-review")
                .routeReason("Medium risk")
                .confidenceScore(0.7)
                .urgencyLevel("normal")
                .estimatedReviewTime(240)
                .requiresEscalation(false)
                .build();

        assertNotNull(decision);
        assertEquals("route-1", decision.getDecisionId());
        assertEquals("/content/pages/home", decision.getContentPath());
        assertEquals("models/content-review", decision.getTargetWorkflow());
        assertEquals("Medium risk", decision.getRouteReason());
        assertEquals(0.7, decision.getConfidenceScore());
        assertEquals("normal", decision.getUrgencyLevel());
        assertEquals(240, decision.getEstimatedReviewTime());
        assertFalse(decision.isRequiresEscalation());
    }

    @Test
    void testContentIssueBuilder() {
        ContentIssue issue = ContentIssue.builder()
                .issueId("issue-1")
                .issueType("deprecated")
                .severity("medium")
                .description("Component is deprecated")
                .contentPath("/content/pages/home")
                .suggestedFix("Replace with new component")
                .autoCorrectable(true)
                .correctAction("replace:old-component")
                .detectedAt(System.currentTimeMillis())
                .build();

        assertNotNull(issue);
        assertEquals("issue-1", issue.getIssueId());
        assertEquals("deprecated", issue.getIssueType());
        assertEquals("medium", issue.getSeverity());
        assertEquals("Component is deprecated", issue.getDescription());
        assertEquals("/content/pages/home", issue.getContentPath());
        assertEquals("Replace with new component", issue.getSuggestedFix());
        assertTrue(issue.isAutoCorrectable());
        assertEquals("replace:old-component", issue.getCorrectAction());
    }

    @Test
    void testWorkflowPerformanceMetricsBuilder() {
        WorkflowPerformanceMetrics metrics = WorkflowPerformanceMetrics.builder()
                .workflowId("wf-1")
                .workflowName("Content Approval")
                .startTime(System.currentTimeMillis())
                .completionTime(System.currentTimeMillis() + 60000)
                .duration(60000)
                .status("completed")
                .stepsCompleted(5)
                .totalSteps(5)
                .completionRate(1.0)
                .averageStepTime(12000)
                .approvalCount(1)
                .rejectionCount(0)
                .build();

        assertNotNull(metrics);
        assertEquals("wf-1", metrics.getWorkflowId());
        assertEquals("Content Approval", metrics.getWorkflowName());
        assertEquals("completed", metrics.getStatus());
        assertEquals(5, metrics.getStepsCompleted());
        assertEquals(5, metrics.getTotalSteps());
        assertEquals(1.0, metrics.getCompletionRate());
        assertEquals(1, metrics.getApprovalCount());
        assertEquals(0, metrics.getRejectionCount());
    }

    TestWorkflowAutomationServiceConfig implements WorkflowAutomationServiceConfig {
        
        public Class<? extends java.lang.annotation.Annotation> annotationType() { return WorkflowAutomationServiceConfig.class; }
        @Override
        public String apiKey() {
            return "test-key";
        }

        @Override
        public String serviceUrl() {
            return "https://api.openai.com/v1/chat/completions";
        }

        @Override
        public String defaultModel() {
            return "gpt-4";
        }

        @Override
        public float temperature() {
            return 0.7f;
        }

        @Override
        public int maxTokens() {
            return 2000;
        }

        @Override
        public boolean enableCache() {
            return true;
        }

        @Override
        public int cacheSize() {
            return 100;
        }

        @Override
        public double simpleChangeThreshold() {
            return 0.3;
        }

        @Override
        public boolean autoApproveEnabled() {
            return true;
        }

        @Override
        public String defaultApprovalWorkflow() {
            return "models/content-approval";
        }

        @Override
        public String defaultReviewWorkflow() {
            return "models/content-review";
        }
    }
}