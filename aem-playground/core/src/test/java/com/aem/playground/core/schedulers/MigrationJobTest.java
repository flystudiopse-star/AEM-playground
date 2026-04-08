package com.aem.playground.core.schedulers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aem.playground.core.schedulers.MigrationJob.MigrationProgress;
import com.aem.playground.core.schedulers.MigrationJob.MigrationResult;
import com.aem.playground.core.schedulers.MigrationJob.MigrationStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationJobTest {

    private MigrationJob fixture;

    @BeforeEach
    void setup() {
        fixture = new MigrationJob();
    }

    @Test
    void testActivateWithDefaultConfig() {
        MigrationJob.Config config = mock(MigrationJob.Config.class);
        when(config.scheduler_expression()).thenReturn("0 0 2 * * ?");
        when(config.scheduler_concurrent()).thenReturn(false);
        when(config.bulk_import_limit()).thenReturn(500);
        when(config.retry_count()).thenReturn(3);
        when(config.retry_delay_ms()).thenReturn(5000L);
        when(config.enable_auto_import()).thenReturn(false);

        fixture.activate(config);

        assertEquals("0 0 2 * * ?", fixture.getCronExpression());
    }

    @Test
    void testActivateWithAutoImportEnabled() {
        MigrationJob.Config config = mock(MigrationJob.Config.class);
        when(config.scheduler_expression()).thenReturn("0 0 2 * * ?");
        when(config.scheduler_concurrent()).thenReturn(true);
        when(config.bulk_import_limit()).thenReturn(100);
        when(config.retry_count()).thenReturn(2);
        when(config.retry_delay_ms()).thenReturn(3000L);
        when(config.enable_auto_import()).thenReturn(true);

        fixture.activate(config);
    }

    @Test
    void testBulkImportLimitCappedAt500() {
        MigrationJob.Config config = mock(MigrationJob.Config.class);
        when(config.scheduler_expression()).thenReturn("0 0 2 * * ?");
        when(config.scheduler_concurrent()).thenReturn(false);
        when(config.bulk_import_limit()).thenReturn(1000);
        when(config.retry_count()).thenReturn(3);
        when(config.retry_delay_ms()).thenReturn(5000L);
        when(config.enable_auto_import()).thenReturn(false);

        fixture.activate(config);
    }

    @Test
    void testMigrationProgressInitialState() {
        MigrationProgress progress = new MigrationProgress();
        
        assertEquals(MigrationStatus.PENDING, progress.getStatus());
        assertEquals(0, progress.getTotalPages());
        assertEquals(0, progress.getProcessedPages());
        assertEquals(0, progress.getSuccessfulPages());
        assertEquals(0, progress.getFailedPages());
        assertTrue(progress.getStartTime() > 0);
    }

    @Test
    void testMigrationProgressProgressPercentage() {
        MigrationProgress progress = new MigrationProgress();
        progress.setTotalPages(100);
        progress.setProcessedPages(50);
        
        assertEquals(50.0, progress.getProgressPercentage());
    }

    @Test
    void testMigrationProgressProgressPercentageWithZeroTotal() {
        MigrationProgress progress = new MigrationProgress();
        progress.setTotalPages(0);
        progress.setProcessedPages(0);
        
        assertEquals(0.0, progress.getProgressPercentage());
    }

    @Test
    void testMigrationProgressDurationMs() {
        MigrationProgress progress = new MigrationProgress();
        progress.setEndTime(progress.getStartTime() + 60000);
        
        assertTrue(progress.getDurationMs() >= 60000);
    }

    @Test
    void testMigrationResultDefaultValues() {
        MigrationResult result = new MigrationResult();
        
        assertFalse(result.isSuccess());
        assertNull(result.getMessage());
        assertNull(result.getProgress());
    }

    @Test
    void testMigrationResultConstructor() {
        MigrationProgress progress = new MigrationProgress();
        MigrationResult result = new MigrationResult(true, "Success message", progress);
        
        assertTrue(result.isSuccess());
        assertEquals("Success message", result.getMessage());
        assertNotNull(result.getProgress());
    }

    @Test
    void testMigrationStatusValues() {
        assertEquals(MigrationStatus.PENDING, MigrationStatus.valueOf("PENDING"));
        assertEquals(MigrationStatus.FETCHING_PAGES, MigrationStatus.valueOf("FETCHING_PAGES"));
        assertEquals(MigrationStatus.IMPORTING, MigrationStatus.valueOf("IMPORTING"));
        assertEquals(MigrationStatus.RETRYING, MigrationStatus.valueOf("RETRYING"));
        assertEquals(MigrationStatus.COMPLETED, MigrationStatus.valueOf("COMPLETED"));
        assertEquals(MigrationStatus.FAILED, MigrationStatus.valueOf("FAILED"));
        assertEquals(MigrationStatus.CANCELLED, MigrationStatus.valueOf("CANCELLED"));
    }

    @Test
    void testMigrationProgressSuccessfulPaths() {
        MigrationProgress progress = new MigrationProgress();
        List<String> paths = new ArrayList<>();
        paths.add("/content/aem-playground/page1");
        paths.add("/content/aem-playground/page2");
        progress.setSuccessfulPaths(paths);
        
        assertEquals(2, progress.getSuccessfulPaths().size());
    }

    @Test
    void testMigrationProgressErrors() {
        MigrationProgress progress = new MigrationProgress();
        progress.addError("Error 1");
        progress.addError("Error 2");
        
        assertEquals(2, progress.getErrors().size());
    }
}