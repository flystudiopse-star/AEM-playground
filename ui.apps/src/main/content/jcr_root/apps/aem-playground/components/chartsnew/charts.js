'use strict';

use(function () {
    var chartType = this.properties.chartType || 'line';
    var chartTitle = this.properties.chartTitle || 'Chart';
    var manualLabels = this.properties.chartLabels || '';
    var manualValues = this.properties.chartValues || '';
    var csvPath = this.properties.csvFileReference || '';
    
    return {
        chartType: chartType,
        chartTitle: chartTitle,
        manualLabels: manualLabels,
        manualValues: manualValues,
        csvPath: csvPath
    };
});
