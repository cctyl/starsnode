<template>
  <div class="interface-card">
    <div class="interface-name">🔌 {{ name }}</div>
    <template v-if="ipv4">
      <div class="interface-details">📍 IP: {{ ipv4.address }}</div>
      <div class="interface-details">🎭 子网掩码: {{ ipv4.netmask }}</div>
      <div v-if="ipv4.mac" class="interface-details">🏷️ MAC: {{ ipv4.mac }}</div>
    </template>
    <div v-else class="interface-details">暂无IPv4地址</div>
    <div class="interface-speed">
      <span class="speed-stat">↓ 下载: <span class="value">{{ formatSpeed(stats.inputMb) }}</span></span>
      <span class="speed-stat">↑ 上传: <span class="value">{{ formatSpeed(stats.outputMb) }}</span></span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  addresses: {
    type: Array,
    required: true
  },
  stats: {
    type: Object,
    default: () => ({ inputMb: 0, outputMb: 0 })
  }
})

const ipv4 = computed(() => {
  return props.addresses.find(addr => addr.family === 'IPv4')
})

const formatSpeed = (mb) => {
  if (mb === 0) return '0 MB/s'
  if (mb < 0.01) return '<0.01 MB/s'
  return `${mb.toFixed(2)} MB/s`
}
</script>
