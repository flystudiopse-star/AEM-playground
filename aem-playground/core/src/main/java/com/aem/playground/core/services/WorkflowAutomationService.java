package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;

import java.util.List;

public interface WorkflowAutomationService {

    List<WorkflowAction> suggestWorkflowActions(String contentPath);

    ContentAnalysisResult analyzeContentForWorkflow(String contentPath);

    boolean canAutoApprove(String contentPath);

    boolean autoApproveContent(String contentPath);

    WorkflowRouteDecision routeContentForReview(String contentPath);

    List<ContentIssue> detectContentIssues(String contentPath);

    boolean triggerCorrectionWorkflow(ContentIssue issue);

    List<WorkflowPerformanceMetrics> getWorkflowPerformanceAnalytics(String workflowId);

    List<WorkflowPerformanceMetrics> getWorkflowPerformanceAnalytics(String workflowId, long startDate, long endDate);

    WorkflowPerformanceMetrics getAggregatePerformanceMetrics();

    String launchWorkflow(String workflowModelId, String contentPath);
}