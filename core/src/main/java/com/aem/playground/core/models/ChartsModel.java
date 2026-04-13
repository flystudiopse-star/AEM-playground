package com.aem.playground.core.models;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Model(adaptables = Resource.class)
public class ChartsModel {

    private static final String PROP_CHART_TYPE = "chartType";
    private static final String PROP_TITLE = "title";
    private static final String PROP_WIDTH = "width";
    private static final String PROP_HEIGHT = "height";
    private static final String PROP_CHART_DATA = "chartData";

    @ValueMapValue(name = PROP_CHART_TYPE)
    private String chartType = "pie";

    @ValueMapValue(name = PROP_TITLE)
    private String title;

    @ValueMapValue(name = PROP_WIDTH)
    private String width = "100%";

    @ValueMapValue(name = PROP_HEIGHT)
    private String height = "400px";

    @ValueMapValue(name = PROP_CHART_DATA)
    private String chartDataJson;

    private List<ChartData> chartData;
    private String chartDataJsonOutput;

    @PostConstruct
    protected void init() {
        parseChartData();
    }

    private void parseChartData() {
        chartData = new ArrayList<>();
        if (chartDataJson == null || chartDataJson.isEmpty()) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<ChartData> parsed = mapper.readValue(chartDataJson,
                mapper.getTypeFactory().constructCollectionType(List.class, ChartData.class));
            chartData = parsed;
        } catch (JsonProcessingException e) {
            chartData = new ArrayList<>();
        }
    }

    public String getChartType() {
        return chartType;
    }

    public String getTitle() {
        return title;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public List<ChartData> getChartData() {
        return chartData;
    }

    public String getChartDataJson() {
        if (chartDataJsonOutput == null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                chartDataJsonOutput = mapper.writeValueAsString(chartData);
            } catch (JsonProcessingException e) {
                chartDataJsonOutput = "[]";
            }
        }
        return chartDataJsonOutput;
    }

    public boolean isPieChart() {
        return "pie".equals(chartType);
    }

    public boolean isBarChart() {
        return "bar".equals(chartType);
    }

    public static class ChartData {
        private String label;
        private double value;
        private String color;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}