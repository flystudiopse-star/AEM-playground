// Chart configuration options using Highcharts

export class Chart {
    public static getChartConfig(type: string, title: string, labels: string[], values: number[]): any {
        const chartType = this.getChartType(type);
        
        const config: any = {
            chart: { type: chartType },
            title: { text: title },
            xAxis: { categories: labels },
            yAxis: { title: { text: 'Value' } },
            series: [{ name: title, data: values }],
            credits: { enabled: false }
        };
        
        if (type === 'pie') {
            config.series = [{
                type: 'pie',
                name: title,
                data: labels.map((l: string, i: number) => [l, values[i]])
            }];
            delete config.xAxis;
            delete config.yAxis;
        }
        
        return config;
    }
    
    private static getChartType(type: string): string {
        const typeMap: { [key: string]: string } = {
            'line': 'line',
            'column': 'column',
            'bar': 'bar',
            'pie': 'pie',
            'area': 'area'
        };
        return typeMap[type] || 'line';
    }
}

// Load Highcharts from CDN if not already loaded
(function loadHighcharts(): void {
    if ((window as any).Highcharts) return;
    
    const script = document.createElement('script');
    script.src = 'https://code.highcharts.com/highcharts.js';
    document.head.appendChild(script);
})();
