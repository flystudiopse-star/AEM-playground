// Charts Component JavaScript
// Uses Highcharts library

import './scss/_charts.scss';

// Load Highcharts from CDN
function loadHighcharts(callback) {
    if (window.Highcharts) {
        callback();
        return;
    }
    
    const script = document.createElement('script');
    script.src = 'https://code.highcharts.com/highcharts.js';
    script.onload = callback;
    document.head.appendChild(script);
}

// Parse comma-separated data
function parseData(labelsStr, valuesStr) {
    const labels = labelsStr ? labelsStr.split(',').map(l => l.trim()) : [];
    const values = valuesStr ? valuesStr.split(',').map(v => {
        const n = parseFloat(v.trim());
        return isNaN(n) ? 0 : n;
    }) : [];
    return { labels, values };
}

// Get chart type mapping
function getChartType(type) {
    const typeMap = {
        'line': 'line',
        'column': 'column', 
        'bar': 'bar',
        'pie': 'pie',
        'area': 'area'
    };
    return typeMap[type] || 'line';
}

// Render all charts on page
function renderCharts() {
    const components = document.querySelectorAll('.chart-component');
    
    components.forEach(el => {
        const type = el.getAttribute('data-chart-type') || 'line';
        const title = el.getAttribute('data-chart-title') || 'Chart';
        const labelsStr = el.getAttribute('data-manual-labels') || '';
        const valuesStr = el.getAttribute('data-manual-values') || '';
        
        const container = el.querySelector('.chart-container');
        if (!container) return;
        
        const data = parseData(labelsStr, valuesStr);
        if (data.labels.length === 0) return;
        
        const chartType = getChartType(type);
        
        const config = {
            chart: { type: chartType },
            title: { text: title },
            xAxis: { categories: data.labels },
            yAxis: { title: { text: 'Value' } },
            series: [{ name: title, data: data.values }],
            credits: { enabled: false }
        };
        
        // Pie chart needs different data format
        if (type === 'pie') {
            config.series = [{
                type: 'pie',
                name: title,
                data: data.labels.map((l, i) => [l, data.values[i]])
            }];
            delete config.xAxis;
            delete config.yAxis;
        }
        
        Highcharts.chart(container.id, config);
    });
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    loadHighcharts(renderCharts);
});

// Also check if DOM already loaded
if (document.readyState !== 'loading') {
    loadHighcharts(renderCharts);
}
