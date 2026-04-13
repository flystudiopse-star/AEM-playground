//package com.aem.playground.core.models;
//
//import org.apache.sling.api.resource.Resource;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import io.wcm.testing.mock.aem.junit5.AemContext;
//import io.wcm.testing.mock.aem.junit5.AemContextExtension;
//import com.aem.playground.core.testcontext.AppAemContext;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.util.List;
//
//@ExtendWith(AemContextExtension.class)
//class ChartsModelTest {
//
//    private final AemContext context = AppAemContext.newAemContext();
//
//    private Resource resource;
//
//    @BeforeEach
//    void setup() {
//        resource = context.create().resource("/content/charts",
//            "chartType", "pie",
//            "title", "Test Chart",
//            "width", "50%",
//            "height", "300px",
//            "chartData", "[{\"label\":\"A\",\"value\":10,\"color\":\"#ff0000\"},{\"label\":\"B\",\"value\":20,\"color\":\"#00ff00\"}]");
//    }
//
//    @Test
//    void testDefaultValues() {
//        Resource emptyResource = context.create().resource("/content/empty");
//        ChartsModel model = emptyResource.adaptTo(ChartsModel.class);
//
//        assertEquals("pie", model.getChartType());
//        assertEquals("100%", model.getWidth());
//        assertEquals("400px", model.getHeight());
//        assertNull(model.getTitle());
//    }
//
//    @Test
//    void testChartTypePie() {
//        ChartsModel model = resource.adaptTo(ChartsModel.class);
//
//        assertEquals("pie", model.getChartType());
//        assertTrue(model.isPieChart());
//        assertFalse(model.isBarChart());
//    }
//
//    @Test
//    void testChartTypeBar() {
//        Resource barResource = context.create().resource("/content/bar",
//            "chartType", "bar",
//            "title", "Bar Chart",
//            "chartData", "[{\"label\":\"X\",\"value\":5,\"color\":\"#0000ff\"}]");
//
//        ChartsModel model = barResource.adaptTo(ChartsModel.class);
//
//        assertEquals("bar", model.getChartType());
//        assertTrue(model.isBarChart());
//        assertFalse(model.isPieChart());
//    }
//
//    @Test
//    void testGetTitle() {
//        ChartsModel model = resource.adaptTo(ChartsModel.class);
//
//        assertEquals("Test Chart", model.getTitle());
//    }
//
//    @Test
//    void testGetWidth() {
//        ChartsModel model = resource.adaptTo(ChartsModel.class);
//
//        assertEquals("50%", model.getWidth());
//    }
//
//    @Test
//    void testGetHeight() {
//        ChartsModel model = resource.adaptTo(ChartsModel.class);
//
//        assertEquals("300px", model.getHeight());
//    }
//
//    @Test
//    void testGetChartData() {
//        ChartsModel model = resource.adaptTo(ChartsModel.class);
//
//        List<ChartsModel.ChartData> data = model.getChartData();
//        assertNotNull(data);
//        assertEquals(2, data.size());
//
//        ChartsModel.ChartData first = data.get(0);
//        assertEquals("A", first.getLabel());
//        assertEquals(10.0, first.getValue());
//        assertEquals("#ff0000", first.getColor());
//    }
//
//    @Test
//    void testGetChartDataJson() {
//        ChartsModel model = resource.adaptTo(ChartsModel.class);
//
//        String json = model.getChartDataJson();
//        assertNotNull(json);
//        assertTrue(json.contains("\"label\":\"A\""));
//        assertTrue(json.contains("\"value\":10"));
//    }
//
//    @Test
//    void testEmptyChartData() {
//        Resource emptyDataResource = context.create().resource("/content/empty-data",
//            "chartType", "pie");
//
//        ChartsModel model = emptyDataResource.adaptTo(ChartsModel.class);
//
//        assertNotNull(model.getChartData());
//        assertTrue(model.getChartData().isEmpty());
//    }
//
//    @Test
//    void testInvalidChartDataJson() {
//        Resource invalidDataResource = context.create().resource("/content/invalid-data",
//            "chartType", "pie",
//            "chartData", "invalid json");
//
//        ChartsModel model = invalidDataResource.adaptTo(ChartsModel.class);
//
//        assertNotNull(model.getChartData());
//        assertTrue(model.getChartData().isEmpty());
//    }
//}