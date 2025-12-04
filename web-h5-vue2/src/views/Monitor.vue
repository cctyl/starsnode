<template>
  <div class="container">
    <!-- 设置按钮 -->
    <button class="settings-btn" @click="showSettings">
      ⚙️
    </button>

    <!-- 消息提示 -->
    <div v-if="notification.show" :class="['notification', notification.type]" @click="hideNotification">
      {{ notification.message }}
      <button class="notification-close" @click.stop="hideNotification">×</button>
    </div>

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

    <!-- 设置模态框 -->
    <SettingsModal
      :is-visible="isSettingsVisible"
      :is-required="isSettingsRequired"
      @close="hideSettings"
      @settings-updated="onSettingsUpdated"
    />
  </div>
</template>

<script>
import SummaryCards from '../components/SummaryCards.vue';
import HardwareCharts from '../components/HardwareCharts.vue';
import DeviceList from '../components/DeviceList.vue';
import SettingsModal from '../components/SettingsModal.vue';
import webSocketService from '../services/websocket.js';

export default {
  name: 'MonitorView',
  components: {
    SummaryCards,
    HardwareCharts,
    DeviceList,
    SettingsModal
  },
  data() {
    return {
      devices: [],
      connectionStatus: 'disconnected',
      connectionMessage: '未连接',
      lastUpdateTime: new Date(),
      loadingMessage: '正在建立连接...',
      timeUpdateInterval: null,
      isSettingsVisible: false,
      isSettingsRequired: false,
      notification: {
        show: false,
        message: '',
        type: 'info', // 'success', 'error', 'info', 'warning'
      }
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
    // 显示设置模态框
    showSettings() {
      this.isSettingsVisible = true;
    },

    // 隐藏设置模态框
    hideSettings() {
      this.isSettingsVisible = false;
    },

    // 显示通知消息
    showNotification(message, type = 'info', duration = 3000) {
      this.notification = {
        show: true,
        message: message,
        type: type
      };

      if (duration > 0) {
        setTimeout(() => {
          this.hideNotification();
        }, duration);
      }
    },

    // 隐藏通知消息
    hideNotification() {
      this.notification.show = false;
    },

    // 显示设置模态框（必需模式）
    showRequiredSettings() {
      this.isSettingsRequired = true;
      this.isSettingsVisible = true;
    },

    // 隐藏设置模态框（必需模式）
    hideRequiredSettings() {
      this.isSettingsRequired = false;
      this.isSettingsVisible = false;
    },

    // 加载用户设置并连接WebSocket
    loadAndConnect() {
      // 从localStorage加载设置
      let settings = {};
      const savedSettings = localStorage.getItem('monitorSettings');

      if (savedSettings) {
        try {
          settings = JSON.parse(savedSettings);
        } catch (error) {
          console.error('Failed to load settings:', error);
          settings = {};
        }
      }

      // 如果没有WebSocket地址，显示设置界面
      if (!settings.wsUrl || !settings.wsUrl.trim()) {
        this.showNotification('请先配置WebSocket服务器地址', 'warning');
        this.showRequiredSettings();
        return;
      }

      // 使用设置连接WebSocket
      this.showNotification('正在连接WebSocket服务器...', 'info');
      webSocketService.connect(settings);
    },

    // 处理设置更新
    onSettingsUpdated(settings) {
      // 重新连接WebSocket使用新的设置
      webSocketService.disconnect();
      this.showNotification('正在重新连接WebSocket服务器...', 'info');
      setTimeout(() => {
        webSocketService.connect(settings);
      }, 100);
    },

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
        this.showNotification('WebSocket服务器连接成功！', 'success');
      } else if (status === 'disconnected') {
        this.loadingMessage = '连接已断开，准备重连...';
        this.showNotification('WebSocket连接已断开', 'warning');
      } else if (status === 'error') {
        this.loadingMessage = '连接出错，请检查网络连接';
        this.showNotification('WebSocket连接失败，请检查服务器地址和网络连接', 'error');
      }
    }
  },
  mounted() {
    // 设置WebSocket回调
    webSocketService.on('onData', this.processDevicesData);
    webSocketService.on('onStatusChange', this.updateConnectionStatus);

    // 加载用户设置并连接WebSocket
    this.loadAndConnect();

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