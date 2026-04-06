package com.aem.playground.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SharePointMigrationServiceTest {

    private SharePointMigrationService fixture;

    @BeforeEach
    void setup() {
        fixture = new SharePointMigrationService();
    }

    @Test
    void testActivateWithDefaultConfig() {
        SharePointMigrationService.Config config = mock(SharePointMigrationService.Config.class);
        when(config.sharepoint_site_url()).thenReturn("https://test.sharepoint.com/sites/test");
        when(config.sharepoint_client_id()).thenReturn("test-client-id");
        when(config.sharepoint_client_secret()).thenReturn("test-secret");
        when(config.sharepoint_tenant_id()).thenReturn("test-tenant-id");
        when(config.sharepoint_site_name()).thenReturn("TestSite");
        when(config.api_timeout()).thenReturn(30000);
        when(config.enable_debug()).thenReturn(false);

        fixture.activate(config);

        assertEquals("https://test.sharepoint.com/sites/test", fixture.getSiteUrl());
        assertEquals("TestSite", fixture.getSiteName());
        assertTrue(fixture.isEnabled());
    }

    @Test
    void testIsEnabledWithEmptyUrl() {
        SharePointMigrationService.Config config = mock(SharePointMigrationService.Config.class);
        when(config.sharepoint_site_url()).thenReturn("");
        when(config.sharepoint_client_id()).thenReturn("test-client-id");
        when(config.sharepoint_client_secret()).thenReturn("test-secret");
        when(config.sharepoint_tenant_id()).thenReturn("test-tenant-id");
        when(config.sharepoint_site_name()).thenReturn("TestSite");
        when(config.api_timeout()).thenReturn(30000);
        when(config.enable_debug()).thenReturn(false);

        fixture.activate(config);

        assertFalse(fixture.isEnabled());
    }

    @Test
    void testIsEnabledWithNullUrl() {
        SharePointMigrationService.Config config = mock(SharePointMigrationService.Config.class);
        when(config.sharepoint_site_url()).thenReturn(null);
        when(config.sharepoint_client_id()).thenReturn("test-client-id");
        when(config.sharepoint_client_secret()).thenReturn("test-secret");
        when(config.sharepoint_tenant_id()).thenReturn("test-tenant-id");
        when(config.sharepoint_site_name()).thenReturn("TestSite");
        when(config.api_timeout()).thenReturn(30000);
        when(config.enable_debug()).thenReturn(false);

        fixture.activate(config);

        assertFalse(fixture.isEnabled());
    }

    @Test
    void testParsePagesFromJsonWithValidData() {
        String json = "{\"d\":{\"results\":[{\"Id\":\"1\",\"Title\":\"Test Page\",\"FileRef\":\"/sites/test/SitePages/Test-Page.aspx\",\"Created\":\"2024-01-01T00:00:00Z\",\"Modified\":\"2024-01-02T00:00:00Z\"}]}}";
        
        List<SharePointMigrationService.SharePointPage> pages = fixture.parsePagesFromJson(json);
        
        assertNotNull(pages);
        assertEquals(1, pages.size());
        assertEquals("1", pages.get(0).getId());
        assertEquals("Test Page", pages.get(0).getTitle());
        assertEquals("/sites/test/SitePages/Test-Page.aspx", pages.get(0).getFileRef());
    }

    @Test
    void testParsePagesFromJsonWithEmptyResults() {
        String json = "{\"d\":{\"results\":[]}}";
        
        List<SharePointMigrationService.SharePointPage> pages = fixture.parsePagesFromJson(json);
        
        assertNotNull(pages);
        assertTrue(pages.isEmpty());
    }

    @Test
    void testParsePagesFromJsonWithInvalidJson() {
        String json = "invalid json";
        
        List<SharePointMigrationService.SharePointPage> pages = fixture.parsePagesFromJson(json);
        
        assertNotNull(pages);
        assertTrue(pages.isEmpty());
    }

    @Test
    void testParsePagesFromJsonWithValueArray() {
        String json = "{\"value\":[{\"Id\":\"2\",\"Title\":\"Page 2\",\"FileRef\":\"/sites/test/SitePages/Page2.aspx\",\"Created\":\"2024-01-01T00:00:00Z\",\"Modified\":\"2024-01-02T00:00:00Z\"}]}";
        
        List<SharePointMigrationService.SharePointPage> pages = fixture.parsePagesFromJson(json);
        
        assertNotNull(pages);
        assertEquals(1, pages.size());
        assertEquals("2", pages.get(0).getId());
        assertEquals("Page 2", pages.get(0).getTitle());
    }

    @Test
    void testParsePagesFromJsonWithMultiplePages() {
        String json = "{\"value\":["
                + "{\"Id\":\"1\",\"Title\":\"Page 1\",\"FileRef\":\"/sites/test/SitePages/Page1.aspx\",\"Created\":\"2024-01-01T00:00:00Z\",\"Modified\":\"2024-01-02T00:00:00Z\"},"
                + "{\"Id\":\"2\",\"Title\":\"Page 2\",\"FileRef\":\"/sites/test/SitePages/Page2.aspx\",\"Created\":\"2024-01-03T00:00:00Z\",\"Modified\":\"2024-01-04T00:00:00Z\"}"
                + "]}";
        
        List<SharePointMigrationService.SharePointPage> pages = fixture.parsePagesFromJson(json);
        
        assertNotNull(pages);
        assertEquals(2, pages.size());
        assertEquals("1", pages.get(0).getId());
        assertEquals("2", pages.get(1).getId());
    }

    @Test
    void testParseAssetsFromJsonWithValidData() {
        String json = "{\"value\":[{\"Id\":\"1\",\"Name\":\"test.pdf\",\"/sites/test/Documents/test.pdf\",\"Created\":\"2024-01-01T00:00:00Z\",\"Modified\":\"2024-01-02T00:00:00Z\",\"FileSize\":\"12345\"}]}";
        
        List<SharePointMigrationService.SharePointAsset> assets = fixture.parseAssetsFromJson(json);
        
        assertNotNull(assets);
        assertEquals(1, assets.size());
        assertEquals("1", assets.get(0).getId());
        assertEquals("test.pdf", assets.get(0).getName());
    }

    @Test
    void testSharePointPageGetPageName() {
        SharePointMigrationService.SharePointPage page = 
            new SharePointMigrationService.SharePointPage("1", "Test Page", "//sites/test/SitePages/test-page.aspx", null, null);
        
        assertEquals("test-page.aspx", page.getPageName());
    }

    @Test
    void testSharePointPageGetPageNameWithNullFileRef() {
        SharePointMigrationService.SharePointPage page = 
            new SharePointMigrationService.SharePointPage("1", "Test Page", null, null, null);
        
        assertEquals("Test Page", page.getPageName());
    }
}