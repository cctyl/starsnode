<template>
  <div class="hardware-summary">
    <div class="charts-row cpu-row">
      <div class="chart-card cpu-card">
        <h3 class="chart-title">CPU 核心数汇总</h3>
        <div class="stat-value">{{ summary.totalCpuCores }}</div>
        <div class="stat-desc">总核心数</div>
        <div class="cpu-details">
          <div class="cpu-stat">
            <span class="cpu-stat-label">平均使用率</span>
            <span class="cpu-stat-value">{{ summary.avgCpuUsage }}%</span>
          </div>
          <div class="cpu-progress">
            <div class="cpu-progress-fill" :class="getCpuProgressClass(summary.avgCpuUsage || 0)" :style="{ width: (summary.avgCpuUsage || 0) + '%' }"></div>
          </div>
        </div>
      </div>

      <div class="chart-card cpu-usage-card">
        <h3 class="chart-title">CPU 使用率</h3>
        <div class="stat-value">{{ summary.avgCpuUsage }}%</div>
        <canvas ref="cpuUsageChart"></canvas>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card memory-card">
        <h3 class="chart-title">内存汇总</h3>
        <div class="stat-value">{{ (summary.totalMemory / 1024).toFixed(2) }} GB</div>
        <canvas ref="memoryChart"></canvas>
      </div>

      <div class="chart-card disk-card">
        <h3 class="chart-title">磁盘汇总</h3>
        <div class="stat-value">{{ summary.totalDisk.toFixed(2) }} GB</div>
        <canvas ref="diskChart"></canvas>
      </div>
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
    getCpuProgressClass(percentage) {
      if (percentage >= 80) return 'high';
      if (percentage >= 50) return 'medium';
      return '';
    },
    updateCharts(summary) {
      this.$nextTick(() => {
        // 调用create方法，内部会智能判断是创建新图表还是更新数据
        this.createCpuUsageChart(summary);
        this.createMemoryChart(summary);
        this.createDiskChart(summary);
      });
    }
  }
};
</script>