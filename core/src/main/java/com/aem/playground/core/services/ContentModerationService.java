package com.aem.playground.core.services;

import java.util.Date;
import java.util.List;

public interface ContentModerationService {

    ModerationResult detectInappropriateContent(String contentPath);

    ModerationResult detectInappropriateContent(String contentPath, double sensitivityThreshold);

    List<ModerationViolation> flagPolicyViolations(String contentPath);

    List<ModerationViolation> flagPolicyViolations(String contentPath, ModerationCategory category);

    ContentCensorResult autoCensorContent(String contentPath);

    ContentCensorResult autoCensorContent(String contentPath, String censorshipChar);

    boolean triggerModerationWorkflow(String contentPath);

    boolean triggerModerationWorkflow(ModerationWorkflowTrigger trigger);

    List<ApprovalQueueItem> getApprovalQueue();

    List<ApprovalQueueItem> getApprovalQueue(ApprovalQueueItem.ApprovalStatus status);

    boolean approveContent(String itemId);

    boolean approveContent(String itemId, String reviewNotes);

    boolean rejectContent(String itemId, String reason);

    boolean rejectContent(String itemId, String reason, String reviewNotes);

    ModerationReport generateModerationReport(Date startDate, Date endDate);

    ModerationReport generateModerationReport(Date startDate, Date endDate, boolean includeTrends);

    ModerationReport generateModerationReport(Date startDate, Date endDate, ModerationCategory filterCategory);

    ModerationDashboard getModerationDashboard();

    ModerationDashboard getModerationDashboard(String timeRange);

    boolean addToApprovalQueue(String contentPath);

    boolean removeFromApprovalQueue(String itemId);
}