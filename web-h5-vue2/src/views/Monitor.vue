<template>
  <div class="container">
    <!-- 汇总信息区域 -->
    <section class="summary-section">
      <h2 class="section-title">📈 数据汇总</h2>

      <!-- 设备统计卡片 -->
      <SummaryCards :summary="summary" />

      <!-- 硬件汇总图表 -->
      <HardwareCharts :summary="summary" />
    </section>

    <!-- 设备详情区域 -->
    <DeviceList
      :devices="devices"
      :loading-message="loadingMessage"
    />
  </div>
</template>

<script>
import SummaryCards from '../components/SummaryCards.vue';
import HardwareCharts from '../components/HardwareCharts.vue';
import DeviceList from '../components/DeviceList.vue';
import webSocketService from '../services/websocket.js';

export default {
  name: 'MonitorView',
  components: {
    SummaryCards,
    HardwareCharts,
    DeviceList
  },
  data() {
    return {
      devices: [],
      connectionStatus: 'disconnected',
      connectionMessage: '未连接',
      lastUpdateTime: new Date(),
      loadingMessage: '正在建立连接...',
      timeUpdateInterval: null
    };
  },
  computed: {
    summary() {
      if (this.devices.length === 0) {
        return {
          total: 0,
          windows: 0,
          linux: 0,
          other: 0,
          totalCpuCores: 0,
          avgCpuUsage: 0,
          totalMemory: 0,
          usedMemory: 0,
          totalDisk: 0,
          usedDisk: 0
        };
      }

      const summary = {
        total: this.devices.length,
        windows: 0,
        linux: 0,
        other: 0,
        totalCpuCores: 0,
        totalCpuUsage: 0,
        totalMemory: 0,
        usedMemory: 0,
        totalDisk: 0,
        usedDisk: 0
      };

      this.devices.forEach(device => {
        // 统计操作系统
        const platform = device.osInfo?.platform?.toLowerCase() || '';
        if (platform.includes('windows')) {
          summary.windows++;
        } else if (platform.includes('linux')) {
          summary.linux++;
        } else {
          summary.other++;
        }

        // CPU统计
        summary.totalCpuCores += device.cpuInfo?.cpuCount || 0;
        summary.totalCpuUsage += device.cpuInfo?.cpuUsage || 0;

        // 内存统计
        const totalMem = device.memInfo?.totalMemMb || 0;
        const usedMem = device.memInfo?.usedMemMb || 0;
        summary.totalMemory += totalMem;
        summary.usedMemory += usedMem;

        // 磁盘统计
        let totalDisk = device.driveInfo?.totalGb || 0;
        let usedDisk = device.driveInfo?.usedGb || 0;

        if (typeof totalDisk === 'string') totalDisk = parseFloat(totalDisk) || 0;
        if (typeof usedDisk === 'string') usedDisk = parseFloat(usedDisk) || 0;

        summary.totalDisk += totalDisk;
        summary.usedDisk += usedDisk;
      });

      // 计算平均CPU使用率
      if (summary.total > 0) {
        summary.avgCpuUsage = (summary.totalCpuUsage / summary.total).toFixed(2);
      }

      return summary;
    }
  },
  methods: {
    // 处理接收到的设备数据
    processDevicesData(devices) {
      if (devices.length === 0) {
        this.devices = [];
        this.loadingMessage = '暂无设备数据';
        return;
      }

      this.devices = devices;
      this.updateTimestamp();
    },

    // 更新时间戳
    updateTimestamp() {
      this.lastUpdateTime = new Date();
    },

    // 更新连接状态
    updateConnectionStatus(status, message) {
      this.connectionStatus = status;
      this.connectionMessage = message;

      if (status === 'connected') {
        this.loadingMessage = '正在接收设备数据...';
      } else if (status === 'disconnected') {
        this.loadingMessage = '连接已断开，准备重连...';
      } else if (status === 'error') {
        this.loadingMessage = '连接出错，请检查网络连接';
      }
    }
  },
  mounted() {
    // 设置WebSocket回调
    webSocketService.on('onData', this.processDevicesData);
    webSocketService.on('onStatusChange', this.updateConnectionStatus);

    // 连接WebSocket
    webSocketService.connect();

    // 定期更新时间显示
    this.timeUpdateInterval = setInterval(this.updateTimestamp, 1000);
  },
  beforeDestroy() {
    // 清理定时器
    if (this.timeUpdateInterval) {
      clearInterval(this.timeUpdateInterval);
      this.timeUpdateInterval = null;
    }

    // 断开WebSocket连接
    webSocketService.disconnect();
  }
};
</script>