<template>
  <div class="device-card" :style="{ animationDelay: index * 0.1 + 's' }">
    <!-- 折叠状态的简洁视图 -->
    <div class="device-summary" @click="toggleDeviceExpanded">
      <div class="device-summary-left">
        <span class="device-os-badge" :class="osBadgeClass">{{ osBadgeText }}</span>
      </div>
      <div class="device-summary-center">
        <div class="device-name-row">
          <span class="device-name">{{ device.osInfo?.hostname || '未知设备' }}</span>
        </div>
        <div class="cpu-info-row">
          <div class="cpu-usage-summary">
            <span class="cpu-label">CPU</span>
            <span class="cpu-value">{{ (device.cpuInfo?.cpuUsage || 0).toFixed(1) }}%</span>
            <div class="mini-progress-bar">
              <div class="mini-progress-fill" :class="getProgressClass(device.cpuInfo?.cpuUsage || 0)" :style="{ width: (device.cpuInfo?.cpuUsage || 0) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="device-summary-right">
        <span class="expand-toggle-icon" :class="{ 'expanded': isDeviceExpanded }">▼</span>
      </div>
    </div>

    <!-- 展开状态的完整信息 -->
    <div v-if="isDeviceExpanded" class="device-details">
      <div class="device-info-grid">
        <div class="info-item">
          <div class="info-label">💻 CPU</div>
          <div class="info-value">{{ device.cpuInfo?.cpuModel || 'N/A' }}</div>
          <div class="info-value">{{ device.cpuInfo?.cpuCount || 0 }} 核心 | 使用率: {{ (device.cpuInfo?.cpuUsage || 0).toFixed(2) }}%</div>
          <div class="progress-bar">
            <div class="progress-fill" :class="getProgressClass(device.cpuInfo?.cpuUsage || 0)" :style="{ width: (device.cpuInfo?.cpuUsage || 0) + '%' }"></div>
          </div>
        </div>

        <div class="info-item">
          <div class="info-label">🧠 内存</div>
          <div class="info-value">{{ ((device.memInfo?.totalMemMb || 0) / 1024).toFixed(2) }} GB</div>
          <div class="info-value">已用: {{ ((device.memInfo?.usedMemMb || 0) / 1024).toFixed(2) }} GB | {{ (device.memInfo?.usedMemPercentage || 0).toFixed(2) }}%</div>
          <div class="progress-bar">
            <div class="progress-fill" :class="getProgressClass(device.memInfo?.usedMemPercentage || 0)" :style="{ width: (device.memInfo?.usedMemPercentage || 0) + '%' }"></div>
          </div>
        </div>

        <div class="info-item">
          <div class="info-label">💾 磁盘</div>
          <div class="info-value">{{ totalDisk.toFixed(2) }} GB</div>
          <div class="info-value">已用: {{ usedDisk.toFixed(2) }} GB | {{ diskPercentage.toFixed(2) }}%</div>
          <div class="progress-bar">
            <div class="progress-fill" :class="getProgressClass(diskPercentage)" :style="{ width: diskPercentage + '%' }"></div>
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
        <div class="interfaces-title" @click="toggleNetworkInterfaces">
          <span>🔌 网卡信息</span>
          <span class="toggle-icon" :class="{ 'expanded': isNetworkExpanded }">▼</span>
        </div>
        <div v-if="isNetworkExpanded" class="interfaces-content">
          <div v-for="(addresses, name) in device.netInterface" :key="name" class="interface-card">
            <div class="interface-name">🔌 {{ name }}</div>
            <div v-if="getIPv4Address(addresses)" class="interface-details">📍 IP: {{ getIPv4Address(addresses).address }}</div>
            <div v-if="getIPv4Address(addresses)" class="interface-details">🎭 子网掩码: {{ getIPv4Address(addresses).netmask }}</div>
            <div v-if="getIPv4Address(addresses)?.mac" class="interface-details">🏷️ MAC: {{ getIPv4Address(addresses).mac }}</div>
            <div v-else class="interface-details">暂无IPv4地址</div>
            <div class="interface-speed">
              <span class="speed-stat">↓ 下载: <span class="value">{{ formatSpeed(getInterfaceStats(name).inputMb) }}</span></span>
              <span class="speed-stat">↑ 上传: <span class="value">{{ formatSpeed(getInterfaceStats(name).outputMb) }}</span></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DeviceCard',
  props: {
    device: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      isDeviceExpanded: false,    // 设备信息默认折叠
      isNetworkExpanded: false    // 网卡信息默认折叠
    };
  },
  computed: {
    osBadgeClass() {
      const platform = this.device.osInfo?.platform?.toLowerCase() || '';
      if (platform.includes('windows')) return 'os-windows';
      if (platform.includes('linux')) return 'os-linux';
      return 'os-other';
    },
    osBadgeText() {
      const platform = this.device.osInfo?.platform?.toLowerCase() || '';
      if (platform.includes('windows')) return '🪟';
      if (platform.includes('linux')) return '🐧';
      return '💻';
    },
    totalDisk() {
      let total = this.device.driveInfo?.totalGb || 0;
      return typeof total === 'string' ? parseFloat(total) || 0 : total;
    },
    usedDisk() {
      let used = this.device.driveInfo?.usedGb || 0;
      return typeof used === 'string' ? parseFloat(used) || 0 : used;
    },
    diskPercentage() {
      let percentage = this.device.driveInfo?.usedPercentage || 0;
      return typeof percentage === 'string' ? parseFloat(percentage) || 0 : percentage;
    },
    totalSpeed() {
      return this.device.netstatInfo?.total || { inputMb: 0, outputMb: 0 };
    }
  },
  methods: {
    toggleDeviceExpanded() {
      this.isDeviceExpanded = !this.isDeviceExpanded;
    },
    toggleNetworkInterfaces(event) {
      // 阻止事件冒泡，避免触发设备折叠
      event.stopPropagation();
      this.isNetworkExpanded = !this.isNetworkExpanded;
    },
    getProgressClass(percentage) {
      if (percentage >= 80) return 'high';
      if (percentage >= 50) return 'medium';
      return '';
    },
    formatSpeed(mb) {
      if (mb === 0) return '0 MB/s';
      if (mb < 0.01) return '<0.01 MB/s';
      return `${mb.toFixed(2)} MB/s`;
    },
    formatUptime(seconds) {
      const days = Math.floor(seconds / 86400);
      const hours = Math.floor((seconds % 86400) / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);

      if (days > 0) {
        return `${days}天 ${hours}小时`;
      } else if (hours > 0) {
        return `${hours}小时 ${minutes}分钟`;
      } else {
        return `${minutes}分钟`;
      }
    },
    getIPv4Address(addresses) {
      return addresses.find(addr => addr.family === 'IPv4');
    },
    getInterfaceStats(name) {
      return this.device.netstatInfo?.[name] ||
             this.device.netstatInfo?.[name + ':'] ||
             { inputMb: 0, outputMb: 0 };
    }
  }
};
</script>