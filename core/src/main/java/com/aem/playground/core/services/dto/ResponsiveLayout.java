package com.aem.playground.core.services.dto;

import java.util.Map;

public class ResponsiveLayout {

    private String breakpoint;
    private int minWidth;
    private int maxWidth;
    private String gridColumns;
    private Map<String, String> columnWidths;
    private String spacing;

    public String getBreakpoint() {
        return breakpoint;
    }

    public void setBreakpoint(String breakpoint) {
        this.breakpoint = breakpoint;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public String getGridColumns() {
        return gridColumns;
    }

    public void setGridColumns(String gridColumns) {
        this.gridColumns = gridColumns;
    }

    public Map<String, String> getColumnWidths() {
        return columnWidths;
    }

    public void setColumnWidths(Map<String, String> columnWidths) {
        this.columnWidths = columnWidths;
    }

    public String getSpacing() {
        return spacing;
    }

    public void setSpacing(String spacing) {
        this.spacing = spacing;
    }
}