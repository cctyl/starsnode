import { Chart, ArcElement, Tooltip, Legend, DoughnutController } from 'chart.js';

// 注册Chart.js组件
Chart.register(ArcElement, Tooltip, Legend, DoughnutController);

export default {
  data() {
    return {
      memoryChartInstance: null,
      diskChartInstance: null,
      cpuUsageChartInstance: null
    };
  },
  methods: {
    // 创建CPU使用率图表
    createCpuUsageChart(summary) {
      const canvas = this.$refs.cpuUsageChart;
      if (!canvas) return;

      const avgUsage = summary.avgCpuUsage || 0;
      const idlePercentage = Math.max(0, 100 - avgUsage);

      // 如果图表已存在，只更新数据
      if (this.cpuUsageChartInstance) {
        this.updateCpuUsageChartData(summary);
        return;
      }

      // 确保canvas是干净的
      const ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      this.cpuUsageChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['使用率', '空闲'],
          datasets: [{
            data: [avgUsage, idlePercentage],
            backgroundColor: [
              'rgba(245, 101, 101, 0.8)',
              'rgba(72, 187, 120, 0.8)'
            ],
            borderColor: [
              'rgba(245, 101, 101, 1)',
              'rgba(72, 187, 120, 1)'
            ],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          plugins: {
            legend: {
              position: 'bottom',
              align: 'center',
              labels: {
                color: '#cbd5e1',
                font: { size: 10 },
                boxWidth: 12,
                padding: 8,
                textAlign: 'center'
              }
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return context.label + ': ' + context.parsed.toFixed(1) + '%';
                }
              }
            }
          }
        }
      });
    },

    // 更新CPU使用率图表数据
    updateCpuUsageChartData(summary) {
      if (!this.cpuUsageChartInstance) return;

      const avgUsage = summary.avgCpuUsage || 0;
      const idlePercentage = Math.max(0, 100 - avgUsage);

      this.cpuUsageChartInstance.data.datasets[0].data = [avgUsage, idlePercentage];
      this.cpuUsageChartInstance.update('none'); // 使用 'none' 模式避免动画
    },

    // 创建内存图表
    createMemoryChart(summary) {
      const canvas = this.$refs.memoryChart;
      if (!canvas) return;

      const usedMem = summary.usedMemory / 1024;
      const freeMem = (summary.totalMemory - summary.usedMemory) / 1024;

      // 如果图表已存在，只更新数据
      if (this.memoryChartInstance) {
        this.updateMemoryChartData(summary);
        return;
      }

      // 确保canvas是干净的
      const ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      this.memoryChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['已使用', '空闲'],
          datasets: [{
            data: [usedMem, freeMem],
            backgroundColor: [
              'rgba(102, 126, 234, 0.8)',
              'rgba(72, 187, 120, 0.8)'
            ],
            borderColor: [
              'rgba(102, 126, 234, 1)',
              'rgba(72, 187, 120, 1)'
            ],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          plugins: {
            legend: {
              position: 'bottom',
              align: 'center',
              labels: {
                color: '#cbd5e1',
                font: { size: 10 },
                boxWidth: 12,
                padding: 8,
                textAlign: 'center'
              }
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return context.label + ': ' + context.parsed.toFixed(2) + ' GB';
                }
              }
            }
          }
        }
      });
    },

    // 更新内存图表数据
    updateMemoryChartData(summary) {
      if (!this.memoryChartInstance) return;

      const usedMem = summary.usedMemory / 1024;
      const freeMem = (summary.totalMemory - summary.usedMemory) / 1024;

      this.memoryChartInstance.data.datasets[0].data = [usedMem, freeMem];
      this.memoryChartInstance.update('none'); // 使用 'none' 模式避免动画
    },

    // 创建磁盘图表
    createDiskChart(summary) {
      const canvas = this.$refs.diskChart;
      if (!canvas) return;

      // 如果图表已存在，只更新数据
      if (this.diskChartInstance) {
        this.updateDiskChartData(summary);
        return;
      }

      // 确保canvas是干净的
      const ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      this.diskChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['已使用', '空闲'],
          datasets: [{
            data: [summary.usedDisk, summary.totalDisk - summary.usedDisk],
            backgroundColor: [
              'rgba(245, 101, 101, 0.8)',
              'rgba(66, 153, 225, 0.8)'
            ],
            borderColor: [
              'rgba(245, 101, 101, 1)',
              'rgba(66, 153, 225, 1)'
            ],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          plugins: {
            legend: {
              position: 'bottom',
              align: 'center',
              labels: {
                color: '#cbd5e1',
                font: { size: 10 },
                boxWidth: 12,
                padding: 8,
                textAlign: 'center'
              }
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return context.label + ': ' + context.parsed.toFixed(2) + ' GB';
                }
              }
            }
          }
        }
      });
    },

    // 更新磁盘图表数据
    updateDiskChartData(summary) {
      if (!this.diskChartInstance) return;

      this.diskChartInstance.data.datasets[0].data = [summary.usedDisk, summary.totalDisk - summary.usedDisk];
      this.diskChartInstance.update('none'); // 使用 'none' 模式避免动画
    },

    // 销毁图表
    destroyCharts() {
      if (this.memoryChartInstance) {
        this.memoryChartInstance.destroy();
        this.memoryChartInstance = null;
      }
      if (this.diskChartInstance) {
        this.diskChartInstance.destroy();
        this.diskChartInstance = null;
      }
    }
  },
  beforeDestroy() {
    this.destroyCharts();
  }
};