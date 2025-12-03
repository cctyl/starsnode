<template>
  <div class="monitor-app">
    <!-- 页面标题 -->
    <header class="header">
      <div class="header-content">
        <h1>📊 设备监控平台</h1>
        <button class="settings-btn" @click="openSettings" title="设置">⚙️</button>
      </div>
      <div class="update-time">{{ connectionStatus.message }}</div>
    </header>

    <!-- 汇总信息区域 -->
    <SummarySection :devices="devices" v-if="devices.length > 0" />

    <!-- 设备详情区域 -->
    <section class="devices-section" v-if="devices.length > 0">
      <h2 class="section-title">🖥️ 设备详情</h2>
      <DeviceCard
        v-for="(device, index) in devices"
        :key="device.osInfo?.hostname || index"
        :device="device"
        :index="index"
      />
    </section>

    <!-- 加载状态 -->
    <div v-if="devices.length === 0" class="loading">
      <div class="spinner"></div>
      <div>{{ loadingMessage }}</div>
    </div>

    <!-- 设置模态框 -->
    <SettingsModal :is-open="isSettingsOpen" @close="closeSettings" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import websocketService from './services/websocket'
import SummarySection from './components/SummarySection.vue'
import DeviceCard from './components/DeviceCard.vue'
import SettingsModal from './components/SettingsModal.vue'

const devices = ref([])
const connectionStatus = ref({ status: 'connecting', message: '正在连接...' })
const loadingMessage = ref('正在建立连接...')
const isSettingsOpen = ref(false)

const openSettings = () => {
  isSettingsOpen.value = true
}

const closeSettings = () => {
  isSettingsOpen.value = false
}

// WebSocket消息处理
websocketService.onMessage((data) => {
  devices.value = data
})

// 连接状态更新
websocketService.onStatusChange(({ status, message }) => {
  connectionStatus.value = { status, message }
  if (devices.value.length === 0) {
    loadingMessage.value = message
  }
})

onMounted(() => {
  // 添加调试信息
  console.log('🚀 设备监控应用启动')
  console.log('📱 用户代理:', navigator.userAgent)
  console.log('🌐 当前时间:', new Date().toLocaleString())
  console.log('🔧 VConsole调试工具已就绪')

  // 检查是否在WebView环境中
  const isWebView = /wv/.test(navigator.userAgent) ||
                   (typeof window !== 'undefined' && window.webkit) ||
                   navigator.userAgent.includes('Android')
  console.log('📲 是否在WebView中:', isWebView)

  // 添加明显的加载验证元素，暂时关闭
  // const debugDiv = document.createElement('div')
  // debugDiv.id = 'debug-info'
  // debugDiv.style.cssText = `
  //   position: fixed;
  //   top: 10px;
  //   left: 10px;
  //   background: rgba(255, 255, 255, 0.9);
  //   color: #000;
  //   padding: 10px;
  //   border-radius: 5px;
  //   font-size: 12px;
  //   z-index: 9999;
  //   border: 2px solid red;
  // `
  // debugDiv.innerHTML = `
  //   <div>✅ App已加载</div>
  //   <div>时间: ${new Date().toLocaleTimeString()}</div>
  //   <div>WebView: ${isWebView}</div>
  // `
  // document.body.appendChild(debugDiv)
  //
  // // 3秒后移除调试信息
  // setTimeout(() => {
  //   if (debugDiv.parentNode) {
  //     debugDiv.parentNode.removeChild(debugDiv)
  //   }
  // }, 5000)

  websocketService.connect()
})

onUnmounted(() => {
  websocketService.disconnect()
})
</script>

<style>
@import './assets/monitor.css';

.monitor-app {
  max-width: 100%;
  padding: 16px;
  margin: 0 auto;
}

.header-content {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  margin-bottom: 8px;
}

.settings-btn {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: white;
  font-size: 16px;
  transition: background 0.2s;
}

.settings-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (min-width: 768px) {
  .monitor-app {
    max-width: 768px;
    padding: 24px;
  }
}
</style>
