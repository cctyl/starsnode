<template>
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
</template>

<script>
export default {
  name: 'SummaryCards',
  props: {
    summary: {
      type: Object,
      required: true,
      default: () => ({
        total: 0,
        windows: 0,
        linux: 0,
        other: 0
      })
    }
  }
};
</script>