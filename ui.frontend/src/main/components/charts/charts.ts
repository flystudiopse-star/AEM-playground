// Charts Component - Highcharts integration
import './scss/index';

import { Chart } from './chart-options';

export class Charts {
    private container: string;
    private type: string;
    private title: string;
    private labels: string[];
    private values: number[];

    constructor(container: string, type: string, title: string, labels: string[], values: number[]) {
        this.container = container;
        this.type = type;
        this.title = title;
        this.labels = labels;
        this.values = values;
    }

    public render(): void {
        const config = Chart.getChartConfig(this.type, this.title, this.labels, this.values);
        (window as any).Highcharts.chart(this.container, config);
    }
}

// Initialize charts when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const elements = document.querySelectorAll('.chart-component');
    
    elements.forEach((el) => {
        const type = el.getAttribute('data-chart-type') || 'line';
        const title = el.getAttribute('data-chart-title') || 'Chart';
        const labelsStr = el.getAttribute('data-manual-labels') || '';
        const valuesStr = el.getAttribute('data-manual-values') || '';
        
        const container = el.querySelector('.chart-container');
        if (!container) return;
        
        const labels = labelsStr ? labelsStr.split(',').map((l: string) => l.trim()) : [];
        const values = valuesStr ? valuesStr.split(',').map((v: string) => {
            const n = parseFloat(v.trim());
            return isNaN(n) ? 0 : n;
        }) : [];
        
        if (labels.length === 0) return;
        
        const chart = new Charts((container as HTMLElement).id, type, title, labels, values);
        chart.render();
    });
});
