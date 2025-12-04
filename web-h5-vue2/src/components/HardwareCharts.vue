<template>
  <div class="hardware-summary">
    <div class="chart-card">
      <h3 class="chart-title">CPU 核心数汇总</h3>
      <div class="stat-value">{{ summary.totalCpuCores }} 核心</div>
      <div class="stat-desc">平均使用率: {{ summary.avgCpuUsage }}%</div>
    </div>

    <div class="chart-card">
      <h3 class="chart-title">内存汇总</h3>
      <div class="stat-value">{{ (summary.totalMemory / 1024).toFixed(2) }} GB</div>
      <canvas ref="memoryChart"></canvas>
    </div>

    <div class="chart-card">
      <h3 class="chart-title">磁盘汇总</h3>
      <div class="stat-value">{{ summary.totalDisk.toFixed(2) }} GB</div>
      <canvas ref="diskChart"></canvas>
    </div>
  </div>
</template>

<script>
import chartMixin from '../mixins/chartMixin';

export default {
  name: 'HardwareCharts',
  mixins: [chartMixin],
  props: {
    summary: {
      type: Object,
      required: true,
      default: () => ({
        totalCpuCores: 0,
        avgCpuUsage: 0,
        totalMemory: 0,
        usedMemory: 0,
        totalDisk: 0,
        usedDisk: 0
      })
    }
  },
  watch: {
    summary: {
      handler(newSummary) {
        this.updateCharts(newSummary);
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    updateCharts(summary) {
      this.$nextTick(() => {
        // 调用create方法，内部会智能判断是创建新图表还是更新数据
        this.createMemoryChart(summary);
        this.createDiskChart(summary);
      });
    }
  }
};
</script>