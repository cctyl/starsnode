<template>
  <transition name="modal">
    <div v-if="isOpen" class="modal-overlay" @click="close">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>⚙️ 设置</h3>
          <button class="close-btn" @click="close">×</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label for="ws-url">WebSocket 连接地址</label>
            <input 
              id="ws-url" 
              v-model="url" 
              type="text" 
              placeholder="ws://10.0.8.1:6080/?token=..."
              class="form-input"
            >
            <p class="form-hint">包含 IP、端口和 Token 的完整连接地址</p>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="resetDefault">恢复默认</button>
          <div class="footer-right">
            <button class="btn btn-text" @click="close">取消</button>
            <button class="btn btn-primary" @click="save">保存</button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import websocketService from '../services/websocket'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const url = ref('')
const defaultUrl = "ws://10.0.8.1:6080/?token=abcdef&type=view&endpointName=web-access"

// 当模态框打开时，获取当前 URL
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    url.value = websocketService.url
  }
})

const close = () => {
  emit('close')
}

const resetDefault = () => {
  url.value = defaultUrl
}

const save = () => {
  if (!url.value.trim()) {
    alert('请输入有效的 WebSocket 地址')
    return
  }
  
  websocketService.updateConfig(url.value.trim())
  close()
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-container {
  background: var(--bg-card);
  width: 90%;
  max-width: 500px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
  border: 1px solid var(--border-color);
  overflow: hidden;
  animation: modalSlideUp 0.3s ease-out;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.02);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: color 0.2s;
}

.close-btn:hover {
  color: var(--text-primary);
}

.modal-body {
  padding: 24px 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 12px;
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.form-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-muted);
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.02);
}

.footer-right {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  filter: brightness(1.1);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
  color: var(--text-primary);
}

.btn-text {
  background: none;
  color: var(--text-muted);
}

.btn-text:hover {
  color: var(--text-primary);
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
