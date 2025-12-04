<template>
  <header class="header">
    <h1>📊 设备监控平台</h1>
    <div class="update-time">
      <span>{{ connectionIcon }}</span>
      <span>{{ connectionMessage }}</span>
      <span class="separator">|</span>
      <span>最后更新: {{ formattedUpdateTime }}</span>
    </div>
  </header>
</template>

<script>
export default {
  name: 'MonitorHeader',
  props: {
    connectionStatus: {
      type: String,
      default: 'disconnected'
    },
    connectionMessage: {
      type: String,
      default: '未连接'
    },
    lastUpdateTime: {
      type: Date,
      default: () => new Date()
    }
  },
  computed: {
    connectionIcon() {
      const icons = {
        connecting: '🔄',
        connected: '✅',
        disconnected: '❌',
        error: '⚠️'
      };
      return icons[this.connectionStatus] || '📡';
    },
    formattedUpdateTime() {
      return this.lastUpdateTime.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    }
  }
};
</script>