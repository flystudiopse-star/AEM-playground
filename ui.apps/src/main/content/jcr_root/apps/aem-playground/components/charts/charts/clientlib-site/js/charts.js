/**
 * Charts Component JavaScript
 * Uses Highcharts library for rendering charts
 */

(function () {
    'use strict';

    // Load Highcharts from CDN
    function loadHighcharts(callback) {
        if (window.Highcharts) {
            callback();
            return;
        }
        
        var script = document.createElement('script');
        script.src = 'https://code.highcharts.com/highcharts.js';
        script.onload = callback;
        script.onerror = function() {
            console.error('Failed to load Highcharts');
        };
        document.head.appendChild(script);
    }

    // Parse CSV data
    function parseCSV(csvText) {
        var lines = csvText.split('\n');
        var labels = [];
        var values = [];
        
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i].trim();
            if (!line || line.startsWith('#')) continue;
            
            var parts = line.split(',');
            if (parts.length >= 2) {
                labels.push(parts[0].trim());
                values.push(parseFloat(parts[1].trim()) || 0);
            }
        }
        
        return { labels: labels, values: values };
    }

    // Get chart configuration based on type
    function getChartConfig(type, title, labels, values) {
        var chartTypeMap = {
            'line': 'line',
            'column': 'column',
            'bar': 'bar',
            'pie': 'pie',
            'area': 'area'
        };
        
        var chartType = chartTypeMap[type] || 'line';
        
        var config = {
            chart: {
                type: chartType
            },
            title: {
                text: title
            },
            xAxis: {
                categories: labels
            },
            yAxis: {
                title: {
                    text: 'Values'
                }
            },
            series: [{
                name: title,
                data: values
            }],
            credits: {
                enabled: false
            }
        };
        
        // Pie chart specific config
        if (type === 'pie') {
            config.series = [{
                type: 'pie',
                name: title,
                data: labels.map(function(label, i) {
                    return [label, values[i]];
                })
            }];
            delete config.xAxis;
            delete config.yAxis;
        }
        
        return config;
    }

    // Initialize all chart components on the page
    function initCharts() {
        var chartElements = document.querySelectorAll('.chart-component');
        
        chartElements.forEach(function(el) {
            var chartType = el.getAttribute('data-chart-type') || 'line';
            var chartTitle = el.getAttribute('data-chart-title') || 'Chart';
            var manualLabels = el.getAttribute('data-manual-labels') || '';
            var manualValues = el.getAttribute('data-manual-values') || '';
            var csvPath = el.getAttribute('data-csv-path') || '';
            var containerId = el.querySelector('.chart-container').id;
            
            var labels = [];
            var values = [];
            
            // Use manual data if provided
            if (manualLabels && manualValues) {
                labels = manualLabels.split(',').map(function(l) { return l.trim(); });
                values = manualValues.split(',').map(function(v) { 
                    var num = parseFloat(v.trim());
                    return isNaN(num) ? 0 : num; 
                });
            }
            
            // If CSV path provided, fetch and parse CSV
            if (csvPath && labels.length === 0) {
                // For now, use manual data if CSV not loaded yet
                console.log('CSV file path set, but needs server-side parsing: ' + csvPath);
            }
            
            // Only render if we have data
            if (labels.length > 0 && values.length > 0) {
                var chartConfig = getChartConfig(chartType, chartTitle, labels, values);
                Highcharts.chart(containerId, chartConfig);
            } else {
                document.getElementById(containerId).innerHTML = 
                    '<p style="text-align:center;color:#999;">No data available. Please configure chart data in component dialog.</p>';
            }
        });
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            loadHighcharts(initCharts);
        });
    } else {
        loadHighcharts(initCharts);
    }

})();
