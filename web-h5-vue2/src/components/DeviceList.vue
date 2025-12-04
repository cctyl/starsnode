<template>
  <div class="devices-section">
    <h2 class="section-title">🖥️ 设备详情</h2>

    <div v-if="devices.length === 0" class="loading">
      <div class="spinner"></div>
      <div>{{ loadingMessage }}</div>
    </div>

    <DeviceCard
      v-for="(device, index) in devices"
      :key="device.osInfo?.hostname || index"
      :device="device"
      :index="index"
    />
  </div>
</template>

<script>
import DeviceCard from './DeviceCard.vue';

export default {
  name: 'DeviceList',
  components: {
    DeviceCard
  },
  props: {
    devices: {
      type: Array,
      default: () => []
    },
    loadingMessage: {
      type: String,
      default: '暂无设备数据'
    }
  }
};
</script>