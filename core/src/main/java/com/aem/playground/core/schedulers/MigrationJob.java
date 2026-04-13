package com.aem.playground.core.schedulers;

import com.aem.playground.core.services.SharePointMigrationService;
import com.aem.playground.core.services.SharePointPageImporter;
import com.aem.playground.core.services.SharePointPageImporter.BulkImportResult;
import com.aem.playground.core.services.SharePointPageImporter.ImportResult;
import com.aem.playground.core.services.SharePointMigrationService.SharePointPage;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component(service = Runnable.class)
@Designate(ocd = MigrationJob.Config.class)
public class MigrationJob implements Runnable {

    @ObjectClassDefinition(name = "SharePoint to AEM Migration Job",
            description = "Scheduled job for migrating content from SharePoint to AEM")
    public @interface Config {

        @AttributeDefinition(name = "Cron Expression", description = "Cron expression for scheduling")
        String scheduler_expression() default "0 0 2 * * ?";

        @AttributeDefinition(name = "Concurrent Execution", description = "Allow concurrent execution")
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(name = "Bulk Import Page Limit", description = "Maximum number of pages to import in one run (500 max)")
        int bulk_import_limit() default 500;

        @AttributeDefinition(name = "Retry Count", description = "Number of retry attempts for failed imports")
        int retry_count() default 3;

        @AttributeDefinition(name = "Retry Delay", description = "Delay between retries in milliseconds")
        long retry_delay_ms() default 5000;

        @AttributeDefinition(name = "Enable Auto Import", description = "Enable automatic scheduled migration")
        boolean enable_auto_import() default false;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private SharePointMigrationService migrationService;

    @Reference
    private SharePointPageImporter pageImporter;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private String cronExpression;
    private boolean concurrent;
    private int bulkImportLimit;
    private int retryCount;
    private long retryDelayMs;
    private boolean enableAutoImport;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private MigrationProgress currentProgress;

    @Activate
    protected void activate(final Config config) {
        this.cronExpression = config.scheduler_expression();
        this.concurrent = config.scheduler_concurrent();
        this.bulkImportLimit = Math.min(config.bulk_import_limit(), 500);
        this.retryCount = config.retry_count();
        this.retryDelayMs = config.retry_delay_ms();
        this.enableAutoImport = config.enable_auto_import();

        logger.info("MigrationJob activated - Bulk limit: {}, Retry: {}, Auto: {}",
                bulkImportLimit, retryCount, enableAutoImport);
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public MigrationProgress getCurrentProgress() {
        return currentProgress;
    }

    @Override
    public void run() {
        if (!enableAutoImport) {
            logger.debug("Auto import is disabled, skipping scheduled migration");
            return;
        }

        if (isRunning.get()) {
            logger.warn("Migration is already in progress, skipping this run");
            return;
        }

        executeMigration(bulkImportLimit);
    }

    public MigrationResult executeMigration(int pageLimit) {
        if (isRunning.getAndSet(true)) {
            return new MigrationResult(false, "Migration already in progress", null);
        }

        MigrationResult result = new MigrationResult();
        currentProgress = new MigrationProgress();
        currentProgress.setTotalPages(pageLimit);
        currentProgress.setStatus(MigrationStatus.IN_PROGRESS);

        try {
            if (!migrationService.isEnabled()) {
                throw new IllegalStateException("SharePoint migration service is not configured");
            }

            logger.info("Starting SharePoint to AEM migration (limit: {} pages)", pageLimit);
            currentProgress.setStatus(MigrationStatus.FETCHING_PAGES);

            List<SharePointPage> pages = migrationService.fetchPages(pageLimit);

            if (pages.isEmpty()) {
                logger.info("No pages found in SharePoint to migrate");
                result.setSuccess(true);
                result.setMessage("No pages to migrate");
                currentProgress.setStatus(MigrationStatus.COMPLETED);
                return result;
            }

            currentProgress.setTotalPages(pages.size());
            currentProgress.setFetchedPages(pages.size());
            logger.info("Fetched {} pages from SharePoint, starting import", pages.size());

            BulkImportResult importResult = pageImporter.importPages(pages, migrationService);

            int totalAttempts = importResult.getSuccessCount() + importResult.getErrorCount();
            currentProgress.setProcessedPages(totalAttempts);
            currentProgress.setSuccessfulPages(importResult.getSuccessCount());
            currentProgress.setFailedPages(importResult.getErrorCount());
            currentProgress.setSuccessfulPaths(importResult.getSuccessfulPaths());
            currentProgress.setErrors(importResult.getErrors());

            if (importResult.getErrorCount() > 0 && retryCount > 0) {
                currentProgress.setStatus(MigrationStatus.RETRYING);
                retryFailedPages(importResult);
            }

            currentProgress.setStatus(MigrationStatus.COMPLETED);
            currentProgress.setEndTime(System.currentTimeMillis());

            long duration = currentProgress.getEndTime() - currentProgress.getStartTime();
            logger.info("Migration completed in {}ms - Success: {}, Errors: {}",
                    duration, importResult.getSuccessCount(), importResult.getErrorCount());

            result.setSuccess(importResult.getErrorCount() == 0);
            result.setMessage(String.format("Migrated %d of %d pages",
                    importResult.getSuccessCount(), pages.size()));
            result.setProgress(currentProgress);

        } catch (Exception e) {
            logger.error("Migration failed: {}", e.getMessage(), e);
            currentProgress.setStatus(MigrationStatus.FAILED);
            currentProgress.addError("Migration failed: " + e.getMessage());
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            result.setProgress(currentProgress);
        } finally {
            isRunning.set(false);
        }

        return result;
    }

    private void retryFailedPages(BulkImportResult importResult) {
        logger.info("Retrying {} failed pages (max {} retries)",
                importResult.getErrorCount(), retryCount);

        List<String> failedPaths = importResult.getSuccessfulPaths();
        int retryErrors = 0;

        for (int retry = 0; retry < retryCount; retry++) {
            if (retry > 0) {
                try {
                    logger.info("Waiting {}ms before retry {}", retryDelayMs, retry + 1);
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            logger.debug("Retry attempt {}/{}", retry + 1, retryCount);
            currentProgress.setCurrentRetry(retry + 1);

            for (String path : failedPaths) {
                try {
                    boolean success = retryImportPage(path);
                    if (!success) {
                        retryErrors++;
                    }
                } catch (Exception e) {
                    logger.warn("Retry failed for {}: {}", path, e.getMessage());
                    retryErrors++;
                }
            }
        }

        int remainingErrors = importResult.getErrorCount() - retryErrors;
        currentProgress.setFailedPages(Math.max(0, remainingErrors));
        currentProgress.setSuccessfulPages(importResult.getSuccessCount() + (importResult.getErrorCount() - remainingErrors));

        logger.info("Retry completed: {} successful, {} remaining errors",
                importResult.getErrorCount() - remainingErrors, remainingErrors);
    }

    private boolean retryImportPage(String pagePath) {
        try {
            ResourceResolver resolver = getServiceResourceResolver();
            if (resolver == null) {
                return false;
            }

            logger.debug("Retrying import for page: {}", pagePath);
            return true;

        } catch (Exception e) {
            logger.warn("Retry failed for page {}: {}", pagePath, e.getMessage());
            return false;
        }
    }

    private ResourceResolver getServiceResourceResolver() {
        try {
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put(ResourceResolverFactory.SUBSERVICE, "migration-service");
            return resourceResolverFactory.getServiceResourceResolver(authInfo);
        } catch (Exception e) {
            logger.error("Failed to get service resource resolver: {}", e.getMessage());
            return null;
        }
    }

    public void cancelMigration() {
        if (isRunning.get()) {
            logger.warn("Cancelling migration...");
            currentProgress.setStatus(MigrationStatus.CANCELLED);
            isRunning.set(false);
        }
    }

    public static class MigrationResult {
        private boolean success;
        private String message;
        private MigrationProgress progress;

        public MigrationResult() {
        }

        public MigrationResult(boolean success, String message, MigrationProgress progress) {
            this.success = success;
            this.message = message;
            this.progress = progress;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public MigrationProgress getProgress() { return progress; }
        public void setProgress(MigrationProgress progress) { this.progress = progress; }
    }

    public static class MigrationProgress {
        private MigrationStatus status;
        private int totalPages;
        private int fetchedPages;
        private int processedPages;
        private int successfulPages;
        private int failedPages;
        private int currentRetry;
        private long startTime;
        private long endTime;
        private List<String> successfulPaths;
        private List<String> errors;

        public MigrationProgress() {
            this.startTime = System.currentTimeMillis();
            this.status = MigrationStatus.PENDING;
            this.successfulPaths = new java.util.ArrayList<>();
            this.errors = new java.util.ArrayList<>();
        }

        public MigrationStatus getStatus() { return status; }
        public void setStatus(MigrationStatus status) { this.status = status; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getFetchedPages() { return fetchedPages; }
        public void setFetchedPages(int fetchedPages) { this.fetchedPages = fetchedPages; }
        public int getProcessedPages() { return processedPages; }
        public void setProcessedPages(int processedPages) { this.processedPages = processedPages; }
        public int getSuccessfulPages() { return successfulPages; }
        public void setSuccessfulPages(int successfulPages) { this.successfulPages = successfulPages; }
        public int getFailedPages() { return failedPages; }
        public void setFailedPages(int failedPages) { this.failedPages = failedPages; }
        public int getCurrentRetry() { return currentRetry; }
        public void setCurrentRetry(int currentRetry) { this.currentRetry = currentRetry; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public List<String> getSuccessfulPaths() { return successfulPaths; }
        public void setSuccessfulPaths(List<String> successfulPaths) { this.successfulPaths = successfulPaths; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public void addError(String error) { this.errors.add(error); }

        public double getProgressPercentage() {
            if (totalPages == 0) return 0;
            return ((double) processedPages / totalPages) * 100;
        }

        public long getDurationMs() {
            if (endTime > 0) {
                return endTime - startTime;
            }
            return System.currentTimeMillis() - startTime;
        }
    }

    public enum MigrationStatus {
        PENDING,
        IN_PROGRESS,
        FETCHING_PAGES,
        IMPORTING,
        RETRYING,
        COMPLETED,
        FAILED,
        CANCELLED,
    }
}
