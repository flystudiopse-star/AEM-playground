package com.aem.playground.core.services;

import com.aem.playground.core.services.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LayoutSuggestionServiceTest {

    @Mock
    private AIService aiService;

    private LayoutSuggestionService service;

    @BeforeEach
    void setUp() {
        service = new LayoutSuggestionServiceImpl();
    }

    @Test
    void testAnalyzePageContent() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        PageContentAnalysis analysis = service.analyzePageContent("/content/page");

        assertNotNull(analysis);
        assertEquals("/content/page", analysis.getPagePath());
    }

    @Test
    void testAnalyzePageContentWithChildren() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        PageContentAnalysis analysis = service.analyzePageContent("/content/page", true);

        assertNotNull(analysis);
        assertEquals("/content/page", analysis.getPagePath());
        assertNotNull(analysis.getHeadings());
    }

    @Test
    void testAnalyzePageContentWithNullPath() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        PageContentAnalysis analysis = service.analyzePageContent(null);

        assertNull(analysis);
    }

    @Test
    void testAnalyzePageContentWithBlankPath() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        PageContentAnalysis analysis = service.analyzePageContent("");

        assertNull(analysis);
    }

    @Test
    void testLayoutSuggestionGetters() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");
        suggestion.setLayoutType("hero");
        suggestion.setConfidenceScore(0.85);

        assertEquals("/content/page", suggestion.getPagePath());
        assertEquals("hero", suggestion.getLayoutType());
        assertEquals(0.85, suggestion.getConfidenceScore());
    }

    @Test
    void testLayoutSuggestionWithComponents() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");

        ComponentSuggestion comp = new ComponentSuggestion();
        comp.setComponentResourceType("aem-playground/components/hero");
        comp.setComponentName("Hero");
        comp.setPosition(0);
        comp.setRelevanceScore(0.95);

        suggestion.setComponents(Arrays.asList(comp));

        assertEquals(1, suggestion.getComponents().size());
        assertEquals("aem-playground/components/hero", suggestion.getComponents().get(0).getComponentResourceType());
    }

    @Test
    void testLayoutSuggestionWithResponsiveLayouts() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");

        ResponsiveLayout mobile = new ResponsiveLayout();
        mobile.setBreakpoint("mobile");
        mobile.setMinWidth(0);
        mobile.setMaxWidth(767);
        mobile.setGridColumns("1");

        suggestion.setResponsiveLayouts(Arrays.asList(mobile));

        assertEquals(1, suggestion.getResponsiveLayouts().size());
        assertEquals("mobile", suggestion.getResponsiveLayouts().get(0).getBreakpoint());
    }

    @Test
    void testLayoutSuggestionWithABTests() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");

        ABTestSuggestion abTest = new ABTestSuggestion();
        abTest.setTestName("Layout Test");
        abTest.setTestId("test-1");
        abTest.setVariants(Arrays.asList("variant-a", "variant-b"));
        abTest.setMetricToTrack("conversion-rate");
        abTest.setEstimatedTrafficPercentage(50.0);
        abTest.setSuggestedDurationDays(14);

        suggestion.setAbTestSuggestions(Arrays.asList(abTest));

        assertEquals(1, suggestion.getAbTestSuggestions().size());
        assertEquals("conversion-rate", suggestion.getAbTestSuggestions().get(0).getMetricToTrack());
    }

    @Test
    void testLayoutSuggestionWithExperienceFragments() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");

        ExperienceFragmentSuggestion ef = new ExperienceFragmentSuggestion();
        ef.setFragmentPath("/etc/experience-fragments/header");
        ef.setFragmentName("Header");
        ef.setFragmentGroup("navigation");
        ef.setVariation("master");

        suggestion.setExperienceFragmentSuggestions(Arrays.asList(ef));

        assertEquals(1, suggestion.getExperienceFragmentSuggestions().size());
        assertEquals("navigation", suggestion.getExperienceFragmentSuggestions().get(0).getFragmentGroup());
    }

    @Test
    void testComponentSuggestionGetters() {
        ComponentSuggestion comp = new ComponentSuggestion();
        comp.setComponentResourceType("aem-playground/components/text");
        comp.setComponentName("Text");
        comp.setPosition(1);
        comp.setContainer("main");
        comp.setRelevanceScore(0.8);

        Map<String, Object> props = new HashMap<>();
        props.put("text", "Sample text");
        comp.setProperties(props);

        assertEquals("aem-playground/components/text", comp.getComponentResourceType());
        assertEquals("Text", comp.getComponentName());
        assertEquals(1, comp.getPosition());
        assertEquals("main", comp.getContainer());
        assertEquals(0.8, comp.getRelevanceScore());
        assertEquals("Sample text", comp.getProperties().get("text"));
    }

    @Test
    void testResponsiveLayoutGetters() {
        ResponsiveLayout layout = new ResponsiveLayout();
        layout.setBreakpoint("tablet");
        layout.setMinWidth(768);
        layout.setMaxWidth(1199);
        layout.setGridColumns("2");
        layout.setSpacing("16px");

        assertEquals("tablet", layout.getBreakpoint());
        assertEquals(768, layout.getMinWidth());
        assertEquals(1199, layout.getMaxWidth());
        assertEquals("2", layout.getGridColumns());
        assertEquals("16px", layout.getSpacing());
    }

    @Test
    void testABTestSuggestionGetters() {
        ABTestSuggestion abTest = new ABTestSuggestion();
        abTest.setTestName("CTA Test");
        abTest.setTestId("cta-1");
        abTest.setVariants(Arrays.asList("control", "variant-a"));
        abTest.setMetricToTrack("click-through-rate");
        abTest.setEstimatedTrafficPercentage(33.0);
        abTest.setSuggestedDurationDays(7);
        abTest.setMinimumSampleSize(500);

        assertEquals("CTA Test", abTest.getTestName());
        assertEquals("cta-1", abTest.getTestId());
        assertEquals(2, abTest.getVariants().size());
        assertEquals("click-through-rate", abTest.getMetricToTrack());
        assertEquals(33.0, abTest.getEstimatedTrafficPercentage());
        assertEquals(7, abTest.getSuggestedDurationDays());
        assertEquals(500, abTest.getMinimumSampleSize(), 0.001);
    }

    @Test
    void testExperienceFragmentSuggestionGetters() {
        ExperienceFragmentSuggestion ef = new ExperienceFragmentSuggestion();
        ef.setFragmentPath("/etc/experience-fragments/footer");
        ef.setFragmentName("Footer");
        ef.setFragmentGroup("navigation");
        ef.setVariation("master");
        ef.setApplicablePages(Arrays.asList("/content/page1", "/content/page2"));

        assertEquals("/etc/experience-fragments/footer", ef.getFragmentPath());
        assertEquals("Footer", ef.getFragmentName());
        assertEquals("navigation", ef.getFragmentGroup());
        assertEquals("master", ef.getVariation());
        assertEquals(2, ef.getApplicablePages().size());
    }

    @Test
    void testPageContentAnalysisGetters() {
        PageContentAnalysis analysis = new PageContentAnalysis();
        analysis.setPagePath("/content/page");
        analysis.setPageTitle("Test Page");
        analysis.setPageDescription("Test description");
        analysis.setHeadings(Arrays.asList("Heading 1", "Heading 2"));
        analysis.setParagraphs(Arrays.asList("Paragraph 1"));
        analysis.setKeywords(Arrays.asList("AEM", "Test"));
        analysis.setImages(Arrays.asList("/content/image.jpg"));
        analysis.setLinks(Arrays.asList("/content/link"));
        analysis.setContentType("landing");
        analysis.setTargetAudience("general");

        assertEquals("/content/page", analysis.getPagePath());
        assertEquals("Test Page", analysis.getPageTitle());
        assertEquals("Test description", analysis.getPageDescription());
        assertEquals(2, analysis.getHeadings().size());
        assertEquals(1, analysis.getParagraphs().size());
        assertEquals(2, analysis.getKeywords().size());
        assertEquals("landing", analysis.getContentType());
        assertEquals("general", analysis.getTargetAudience());
    }

    @Test
    void testValidateLayoutSuggestion_ValidSuggestion() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");
        suggestion.setLayoutType("hero");

        ComponentSuggestion comp = new ComponentSuggestion();
        comp.setComponentResourceType("aem-playground/components/hero");
        comp.setComponentName("Hero");
        suggestion.setComponents(Arrays.asList(comp));

        assertTrue(service.validateLayoutSuggestion(suggestion));
    }

    @Test
    void testValidateLayoutSuggestion_NullSuggestion() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        assertFalse(service.validateLayoutSuggestion(null));
    }

    @Test
    void testValidateLayoutSuggestion_NullPagePath() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setLayoutType("hero");
        suggestion.setComponents(Arrays.asList(new ComponentSuggestion()));

        assertFalse(service.validateLayoutSuggestion(suggestion));
    }

    @Test
    void testValidateLayoutSuggestion_NullLayoutType() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");
        suggestion.setComponents(Arrays.asList(new ComponentSuggestion()));

        assertFalse(service.validateLayoutSuggestion(suggestion));
    }

    @Test
    void testValidateLayoutSuggestion_NullComponents() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");
        suggestion.setLayoutType("hero");

        assertFalse(service.validateLayoutSuggestion(suggestion));
    }

    @Test
    void testValidateLayoutSuggestion_EmptyComponents() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");
        suggestion.setLayoutType("hero");
        suggestion.setComponents(new ArrayList<>());

        assertFalse(service.validateLayoutSuggestion(suggestion));
    }

    @Test
    void testValidateLayoutSuggestion_NullComponentResourceType() {
        LayoutSuggestionService service = new LayoutSuggestionServiceImpl();
        
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");
        suggestion.setLayoutType("hero");

        ComponentSuggestion comp = new ComponentSuggestion();
        comp.setComponentName("Hero");
        suggestion.setComponents(Arrays.asList(comp));

        assertFalse(service.validateLayoutSuggestion(suggestion));
    }

    @Test
    void testLayoutSuggestionMetadata() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generatedBy", "AI");
        metadata.put("timestamp", System.currentTimeMillis());
        suggestion.setMetadata(metadata);

        assertNotNull(suggestion.getMetadata());
        assertEquals("AI", suggestion.getMetadata().get("generatedBy"));
    }

    @Test
    void testComponentSuggestionWithRequiredContent() {
        ComponentSuggestion comp = new ComponentSuggestion();
        comp.setComponentResourceType("aem-playground/components/image");
        comp.setRequiredContent(Arrays.asList("alt text", "image file"));

        assertEquals(2, comp.getRequiredContent().size());
        assertTrue(comp.getRequiredContent().contains("alt text"));
    }

    @Test
    void testABTestWithVariantProperties() {
        ABTestSuggestion abTest = new ABTestSuggestion();
        Map<String, Object> variantProps = new HashMap<>();
        variantProps.put("variant-a", "blue-button");
        variantProps.put("variant-b", "green-button");
        abTest.setVariantProperties(variantProps);

        assertEquals("blue-button", abTest.getVariantProperties().get("variant-a"));
    }

    @Test
    void testExperienceFragmentWithCustomProperties() {
        ExperienceFragmentSuggestion ef = new ExperienceFragmentSuggestion();
        Map<String, Object> props = new HashMap<>();
        props.put("showSearch", true);
        props.put("showLanguageSwitcher", false);
        ef.setFragmentProperties(props);

        assertEquals(true, ef.getFragmentProperties().get("showSearch"));
    }

    @Test
    void testResponsiveLayoutColumnWidths() {
        ResponsiveLayout layout = new ResponsiveLayout();
        Map<String, String> columnWidths = new LinkedHashMap<>();
        columnWidths.put("col1", "25%");
        columnWidths.put("col2", "50%");
        columnWidths.put("col3", "25%");
        layout.setColumnWidths(columnWidths);

        assertEquals("25%", layout.getColumnWidths().get("col1"));
        assertEquals("50%", layout.getColumnWidths().get("col2"));
    }

    @Test
    void testMultipleComponentsInLayout() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/blog/article");
        suggestion.setLayoutType("two-column");

        List<ComponentSuggestion> components = new ArrayList<>();
        
        ComponentSuggestion title = new ComponentSuggestion();
        title.setComponentResourceType("aem-playground/components/title");
        title.setPosition(0);
        components.add(title);

        ComponentSuggestion text1 = new ComponentSuggestion();
        text1.setComponentResourceType("aem-playground/components/text");
        text1.setPosition(1);
        components.add(text1);

        ComponentSuggestion image = new ComponentSuggestion();
        image.setComponentResourceType("aem-playground/components/image");
        image.setPosition(2);
        components.add(image);

        ComponentSuggestion text2 = new ComponentSuggestion();
        text2.setComponentResourceType("aem-playground/components/text");
        text2.setPosition(3);
        components.add(text2);

        suggestion.setComponents(components);

        assertEquals(4, suggestion.getComponents().size());
        assertEquals("title", suggestion.getComponents().get(0).getComponentResourceType());
        assertEquals("image", suggestion.getComponents().get(2).getComponentResourceType());
    }

    @Test
    void testFullResponsiveLayoutBreakpoints() {
        LayoutSuggestion suggestion = new LayoutSuggestion();
        suggestion.setPagePath("/content/page");

        List<ResponsiveLayout> layouts = new ArrayList<>();

        ResponsiveLayout mobile = new ResponsiveLayout();
        mobile.setBreakpoint("mobile");
        mobile.setMinWidth(0);
        mobile.setMaxWidth(767);
        mobile.setGridColumns("1");
        layouts.add(mobile);

        ResponsiveLayout tablet = new ResponsiveLayout();
        tablet.setBreakpoint("tablet");
        tablet.setMinWidth(768);
        tablet.setMaxWidth(1024);
        tablet.setGridColumns("2");
        layouts.add(tablet);

        ResponsiveLayout desktop = new ResponsiveLayout();
        desktop.setBreakpoint("desktop");
        desktop.setMinWidth(1025);
        desktop.setMaxWidth(Integer.MAX_VALUE);
        desktop.setGridColumns("12");
        layouts.add(desktop);

        suggestion.setResponsiveLayouts(layouts);

        assertEquals(3, suggestion.getResponsiveLayouts().size());
        assertEquals("1", suggestion.getResponsiveLayouts().get(0).getGridColumns());
        assertEquals("2", suggestion.getResponsiveLayouts().get(1).getGridColumns());
        assertEquals("12", suggestion.getResponsiveLayouts().get(2).getGridColumns());
    }

    @Test
    void testPageContentAnalysisWithAllFields() {
        PageContentAnalysis analysis = new PageContentAnalysis();
        analysis.setPagePath("/content/products");
        analysis.setPageTitle("Our Products");
        analysis.setPageDescription("Browse our wide range of products");
        analysis.setHeadings(Arrays.asList("Featured Products", "New Arrivals", "Special Offers"));
        analysis.setParagraphs(Arrays.asList("Lorem ipsum", "Dolor sit amet", "Consectetur"));
        analysis.setKeywords(Arrays.asList("products", "shopping", "e-commerce", "online store"));
        analysis.setImages(Arrays.asList("/content/dam/product1.jpg", "/content/dam/product2.jpg"));
        analysis.setLinks(Arrays.asList("/content/category1", "/content/category2", "/content/cart"));
        analysis.setContentType("product");
        analysis.setTargetAudience("consumer");

        Map<String, Object> custom = new HashMap<>();
        custom.put("category", "retail");
        custom.put("pricing", "dynamic");
        analysis.setCustomProperties(custom);

        assertEquals("product", analysis.getContentType());
        assertEquals("consumer", analysis.getTargetAudience());
        assertEquals(3, analysis.getHeadings().size());
        assertEquals(3, analysis.getKeywords().size());
        assertEquals("retail", analysis.getCustomProperties().get("category"));
    }
}