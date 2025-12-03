<template>
  <section class="summary-section">
    <h2 class="section-title">📈 数据汇总</h2>
    
    <!-- 设备统计卡片 -->
    <div class="summary-grid">
      <div class="summary-card">
        <div class="card-icon">🖥️</div>
        <div class="card-content">
          <div class="card-label">在线设备总数</div>
          <div class="card-value">{{ summary.total }}</div>
        </div>
      </div>
      
      <div class="summary-card">
        <div class="card-icon">🪟</div>
        <div class="card-content">
          <div class="card-label">Windows 设备</div>
          <div class="card-value">{{ summary.windows }}</div>
        </div>
      </div>
      
      <div class="summary-card">
        <div class="card-icon">🐧</div>
        <div class="card-content">
          <div class="card-label">Linux 设备</div>
          <div class="card-value">{{ summary.linux }}</div>
        </div>
      </div>
      
      <div class="summary-card">
        <div class="card-icon">💻</div>
        <div class="card-content">
          <div class="card-label">其他系统</div>
          <div class="card-value">{{ summary.other }}</div>
        </div>
      </div>
    </div>

    <!-- 硬件汇总图表 -->
    <div class="hardware-summary">
      <div class="chart-card cpu-card">
        <h3 class="chart-title">CPU 核心数汇总</h3>
        <div class="stat-value">{{ summary.totalCpuCores }} 核心</div>
        <div class="stat-desc">平均使用率: {{ summary.avgCpuUsage }}%</div>
      </div>
      
      <div class="chart-card">
        <h3 class="chart-title">内存汇总</h3>
        <div class="stat-value">{{ (summary.totalMemory / 1024).toFixed(2) }} GB</div>
        <div class="chart-container">
          <DoughnutChart 
            :data="memoryChartData"
            :options="chartOptions"
          />
        </div>
      </div>
      
      <div class="chart-card">
        <h3 class="chart-title">磁盘汇总</h3>
        <div class="stat-value">{{ summary.totalDisk.toFixed(2) }} GB</div>
        <div class="chart-container">
          <DoughnutChart 
            :data="diskChartData"
            :options="chartOptions"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hardware-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%; /* 确保不超出父容器 */
}

.cpu-card {
  grid-column: span 2;
}

.chart-card {
  padding: 12px;
  min-width: 0; /* 关键：防止Grid子项被内容撑大导致溢出 */
  overflow: hidden;
}

.chart-container {
  height: 120px; /* 限制图表高度 */
  position: relative;
  width: 100%;
  display: flex;
  justify-content: center;
}

.stat-value {
  font-size: 18px; /* 稍微再减小一点 */
  margin-bottom: 4px;
}

.chart-title {
  font-size: 13px;
  margin-bottom: 8px;
}

@media (min-width: 768px) {
  .hardware-summary {
    grid-template-columns: repeat(3, 1fr);
  }
  .cpu-card {
    grid-column: span 1;
  }
  .chart-container {
    height: 160px; /* 桌面端可以高一点 */
  }
}
</style>

<script setup>
import { computed } from 'vue'
import { Doughnut as DoughnutChart } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps({
  devices: {
    type: Array,
    required: true
  }
})

// ... (summary, memoryChartData, diskChartData computed properties remain same) ...

// 计算汇总数据
const summary = computed(() => {
  const result = {
    total: props.devices.length,
    windows: 0,
    linux: 0,
    other: 0,
    totalCpuCores: 0,
    totalCpuUsage: 0,
    totalMemory: 0,
    usedMemory: 0,
    totalDisk: 0,
    usedDisk: 0,
    avgCpuUsage: 0
  }

  props.devices.forEach(device => {
    const platform = device.osInfo?.platform?.toLowerCase() || ''
    if (platform.includes('windows')) {
      result.windows++
    } else if (platform.includes('linux')) {
      result.linux++
    } else {
      result.other++
    }

    result.totalCpuCores += device.cpuInfo?.cpuCount || 0
    result.totalCpuUsage += device.cpuInfo?.cpuUsage || 0

    const totalMem = device.memInfo?.totalMemMb || 0
    const usedMem = device.memInfo?.usedMemMb || 0
    result.totalMemory += totalMem
    result.usedMemory += usedMem

    let totalDisk = device.driveInfo?.totalGb || 0
    let usedDisk = device.driveInfo?.usedGb || 0
    
    if (typeof totalDisk === 'string') totalDisk = parseFloat(totalDisk) || 0
    if (typeof usedDisk === 'string') usedDisk = parseFloat(usedDisk) || 0
    
    result.totalDisk += totalDisk
    result.usedDisk += usedDisk
  })

  if (result.total > 0) {
    result.avgCpuUsage = (result.totalCpuUsage / result.total).toFixed(2)
  }

  return result
})

// 内存图表数据
const memoryChartData = computed(() => {
  const usedMem = summary.value.usedMemory / 1024
  const freeMem = (summary.value.totalMemory - summary.value.usedMemory) / 1024

  return {
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
  }
})

// 磁盘图表数据
const diskChartData = computed(() => ({
  labels: ['已使用', '空闲'],
  datasets: [{
    data: [summary.value.usedDisk, summary.value.totalDisk - summary.value.usedDisk],
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
}))

// 图表配置
const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  layout: {
    padding: 0
  },
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        color: '#cbd5e1',
        font: { size: 10 },
        boxWidth: 10,
        padding: 8
      }
    },
    tooltip: {
      callbacks: {
        label: (context) => {
          return context.label + ': ' + context.parsed.toFixed(2) + ' GB'
        }
      }
    }
  }
}
</script>
