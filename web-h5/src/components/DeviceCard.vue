<template>
  <div class="device-card" :class="{ 'is-expanded': isExpanded }" :style="{ animationDelay: `${index * 0.1}s` }">
    <div class="device-header" @click="toggleExpand">
      <div class="header-main">
        <span class="expand-icon">{{ isExpanded ? '▼' : '▶' }}</span>
        <div class="device-name">
          <component :is="'span'" v-html="getOSBadge(device.osInfo?.platform)"></component>
          <span>{{ device.osInfo?.hostname || '未知设备' }}</span>
        </div>
      </div>
      
      <div class="header-stats">
        <div v-if="!isExpanded" class="simple-cpu">
          CPU: {{ (device.cpuInfo?.cpuUsage || 0).toFixed(1) }}%
        </div>
        <div class="device-status">
          <span class="status-dot"></span>
          <span>在线</span>
        </div>
      </div>
    </div>

    <transition name="expand">
      <div v-show="isExpanded" class="device-details">
        <div class="device-info-grid">
          <div class="info-item">
            <div class="info-label">💻 CPU</div>
            <div class="info-value">{{ device.cpuInfo?.cpuModel || 'N/A' }}</div>
            <div class="info-value">{{ device.cpuInfo?.cpuCount || 0 }} 核心 | 使用率: {{ (device.cpuInfo?.cpuUsage || 0).toFixed(2) }}%</div>
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :class="getProgressClass(device.cpuInfo?.cpuUsage || 0)"
                :style="{ width: `${device.cpuInfo?.cpuUsage || 0}%` }"
              ></div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">🧠 内存</div>
            <div class="info-value">{{ ((device.memInfo?.totalMemMb || 0) / 1024).toFixed(2) }} GB</div>
            <div class="info-value">已用: {{ ((device.memInfo?.usedMemMb || 0) / 1024).toFixed(2) }} GB | {{ (device.memInfo?.usedMemPercentage || 0).toFixed(2) }}%</div>
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :class="getProgressClass(device.memInfo?.usedMemPercentage || 0)"
                :style="{ width: `${device.memInfo?.usedMemPercentage || 0}%` }"
              ></div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">💾 磁盘</div>
            <div class="info-value">{{ diskInfo.totalGb.toFixed(2) }} GB</div>
            <div class="info-value">已用: {{ diskInfo.usedGb.toFixed(2) }} GB | {{ diskInfo.percentage.toFixed(2) }}%</div>
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :class="getProgressClass(diskInfo.percentage)"
                :style="{ width: `${diskInfo.percentage}%` }"
              ></div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">⏱️ 运行时间</div>
            <div class="info-value">{{ formatUptime(device.osInfo?.uptime || 0) }}</div>
            <div class="info-value">架构: {{ device.osInfo?.arch || 'N/A' }}</div>
          </div>
        </div>

        <div class="network-speed">
          <div class="speed-item">
            <div class="speed-label">↓ 总下载速度</div>
            <div class="speed-value">{{ formatSpeed(totalSpeed.inputMb) }}</div>
          </div>
          <div class="speed-item">
            <div class="speed-label">↑ 总上传速度</div>
            <div class="speed-value">{{ formatSpeed(totalSpeed.outputMb) }}</div>
          </div>
        </div>

        <div v-if="device.ipInfo" class="ip-info">
          <div class="ip-title">🌍 外网IP信息</div>
          <div class="ip-details">
            <div>📍 IP: {{ device.ipInfo.query || 'N/A' }}</div>
            <div>🏳️ 国家: {{ device.ipInfo.country || 'N/A' }}</div>
            <div>🏙️ 城市: {{ device.ipInfo.city || 'N/A' }}</div>
            <div>🌐 ISP: {{ device.ipInfo.isp || 'N/A' }}</div>
          </div>
        </div>

        <div class="network-interfaces">
          <div class="interfaces-title" @click.stop="toggleInterfaces" style="cursor: pointer; user-select: none;">
            <span>{{ isInterfacesExpanded ? '▼' : '▶' }}</span>
            🔌 网卡信息 ({{ Object.keys(device.netInterface || {}).length }})
          </div>
          <transition name="expand">
            <div v-show="isInterfacesExpanded">
              <NetworkInterface
                v-for="(addresses, name) in device.netInterface"
                :key="name"
                :name="name"
                :addresses="addresses"
                :stats="getInterfaceStats(name)"
              />
            </div>
          </transition>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import NetworkInterface from './NetworkInterface.vue'

const props = defineProps({
  device: {
    type: Object,
    required: true
  },
  index: {
    type: Number,
    default: 0
  }
})

// 设备详情折叠状态
const isExpanded = ref(false)

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

// 网卡信息折叠状态
const isInterfacesExpanded = ref(false)

const toggleInterfaces = () => {
  isInterfacesExpanded.value = !isInterfacesExpanded.value
}

// 磁盘信息处理
const diskInfo = computed(() => {
  let totalGb = props.device.driveInfo?.totalGb || 0
  let usedGb = props.device.driveInfo?.usedGb || 0
  let percentage = props.device.driveInfo?.usedPercentage || 0
  
  if (typeof totalGb === 'string') totalGb = parseFloat(totalGb) || 0
  if (typeof usedGb === 'string') usedGb = parseFloat(usedGb) || 0
  if (typeof percentage === 'string') percentage = parseFloat(percentage) || 0
  
  return { totalGb, usedGb, percentage }
})

// 总网络速度
const totalSpeed = computed(() => {
  return props.device.netstatInfo?.total || { inputMb: 0, outputMb: 0 }
})

// 获取网卡统计信息
const getInterfaceStats = (name) => {
  return props.device.netstatInfo?.[name] || 
         props.device.netstatInfo?.[name + ':'] || 
         { inputMb: 0, outputMb: 0 }
}

// 工具函数
const getOSBadge = (platform) => {
  const p = platform?.toLowerCase() || ''
  if (p.includes('windows')) {
    return '<span class="device-os-badge os-windows">🪟 Windows</span>'
  } else if (p.includes('linux')) {
    return '<span class="device-os-badge os-linux">🐧 Linux</span>'
  } else {
    return '<span class="device-os-badge os-other">💻 其他</span>'
  }
}

const getProgressClass = (percentage) => {
  if (percentage >= 80) return 'high'
  if (percentage >= 50) return 'medium'
  return ''
}

const formatSpeed = (mb) => {
  if (mb === 0) return '0 MB/s'
  if (mb < 0.01) return '<0.01 MB/s'
  return `${mb.toFixed(2)} MB/s`
}

const formatUptime = (seconds) => {
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (days > 0) {
    return `${days}天 ${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时 ${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}
</script>

<style scoped>
.device-header {
  cursor: pointer;
  user-select: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.device-card.is-expanded .device-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border-color);
}

.header-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.expand-icon {
  font-size: 12px;
  color: var(--text-muted);
  width: 16px;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.simple-cpu {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
  background: rgba(255, 255, 255, 0.05);
  padding: 2px 8px;
  border-radius: 12px;
}

/* 展开/收起过渡动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 2000px;
}

.interfaces-title {
  transition: color 0.2s ease;
}

.interfaces-title:hover {
  color: var(--primary-color);
}
</style>
