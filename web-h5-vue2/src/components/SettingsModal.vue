<template>
  <div v-if="isVisible" class="settings-overlay" @click="closeOnOverlay">
    <div class="settings-modal" @click.stop>
      <div class="modal-header">
        <h2>⚙️ {{ isRequired ? '必需设置' : '设置' }}</h2>
        <button class="close-btn" @click="close" v-if="!isRequired">×</button>
      </div>

      <div class="modal-body">
        <div v-if="isRequired" class="required-notice">
          <strong>⚠️ 首次使用必需设置</strong>
          <p>请输入WebSocket服务器地址以开始使用监控系统</p>
        </div>

        <div class="form-group">
          <label for="ws-url">WebSocket 地址</label>
          <input
            id="ws-url"
            v-model="wsUrl"
            type="text"
            placeholder="ws://localhost:8080/ws"
            class="form-input"
          />
          <div v-if="errorMessage" class="form-error">
            {{ errorMessage }}
          </div>
          <div v-else class="form-hint">
            请输入WebSocket服务器地址，例如：ws://localhost:8080/ws
          </div>
        </div>

        <div class="form-group">
          <label for="reconnect-interval">重连间隔 (秒)</label>
          <input
            id="reconnect-interval"
            v-model="reconnectInterval"
            type="number"
            min="1"
            max="60"
            class="form-input"
          />
          <div class="form-hint">
            连接断开后自动重连的间隔时间（1-60秒）
          </div>
        </div>

        <div class="form-group">
          <label for="auto-reconnect">
            <input
              id="auto-reconnect"
              v-model="autoReconnect"
              type="checkbox"
              class="form-checkbox"
            />
            启用自动重连
          </label>
          <div class="form-hint">
            连接断开后是否自动尝试重新连接
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button
          class="btn btn-secondary"
          @click="close"
          :disabled="isRequired"
        >
          {{ isRequired ? '必须设置' : '取消' }}
        </button>
        <button class="btn btn-primary" @click="save">保存</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SettingsModal',
  props: {
    isVisible: {
      type: Boolean,
      default: false
    },
    isRequired: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      wsUrl: '',
      reconnectInterval: 5,
      autoReconnect: true,
      errorMessage: ''
    };
  },
  methods: {
    loadSettings() {
      // 从localStorage加载设置
      const savedSettings = localStorage.getItem('monitorSettings');
      if (savedSettings) {
        try {
          const settings = JSON.parse(savedSettings);
          this.wsUrl = settings.wsUrl || '';
          this.reconnectInterval = settings.reconnectInterval || 5;
          this.autoReconnect = settings.autoReconnect !== false;
        } catch (error) {
          console.error('Failed to load settings:', error);
        }
      }
    },

    validateSettings() {
      // 清除之前的错误信息
      this.errorMessage = '';

      // 如果是必需模式，必须输入WebSocket地址
      if (this.isRequired && !this.wsUrl.trim()) {
        this.errorMessage = '请输入WebSocket服务器地址才能继续';
        return false;
      }

      // 验证WebSocket地址格式
      if (this.wsUrl.trim()) {
        try {
          const url = new URL(this.wsUrl);
          if (!['ws:', 'wss:'].includes(url.protocol)) {
            this.errorMessage = 'WebSocket地址必须以 ws:// 或 wss:// 开头';
            return false;
          }
        } catch (error) {
          this.errorMessage = 'WebSocket地址格式不正确';
          return false;
        }
      }

      // 验证重连间隔
      const interval = parseInt(this.reconnectInterval);
      if (isNaN(interval) || interval < 1 || interval > 60) {
        this.errorMessage = '重连间隔必须在 1-60 秒之间';
        return false;
      }

      return true;
    },

    saveSettings() {
      if (!this.validateSettings()) {
        return false;
      }

      const settings = {
        wsUrl: this.wsUrl.trim(),
        reconnectInterval: parseInt(this.reconnectInterval),
        autoReconnect: this.autoReconnect
      };

      localStorage.setItem('monitorSettings', JSON.stringify(settings));
      this.$emit('settings-updated', settings);
      return true;
    },

    save() {
      if (this.saveSettings()) {
        this.close();
      }
    },

    close() {
      // 如果是必需模式且没有有效的WebSocket地址，不允许关闭
      if (this.isRequired && (!this.wsUrl.trim() || !this.validateSettings())) {
        this.errorMessage = '请输入有效的WebSocket服务器地址才能继续';
        return;
      }
      this.$emit('close');
    },

    closeOnOverlay(event) {
      // 如果是必需模式，阻止点击遮罩层关闭
      if (this.isRequired) {
        return;
      }
      if (event.target === event.currentTarget) {
        this.close();
      }
    }
  },

  mounted() {
    this.loadSettings();
  },

  watch: {
    isVisible(newVal) {
      if (newVal) {
        this.loadSettings();
      }
    }
  }
};
</script>

<style scoped>
.settings-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.settings-modal {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
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
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px;
}

.required-notice {
  background: rgba(246, 173, 85, 0.1);
  border: 1px solid rgba(246, 173, 85, 0.3);
  border-radius: var(--radius-sm);
  padding: 12px;
  margin-bottom: 20px;
}

.required-notice strong {
  color: var(--warning-color);
  font-size: 14px;
  display: block;
  margin-bottom: 4px;
}

.required-notice p {
  color: var(--text-secondary);
  font-size: 12px;
  margin: 0;
  line-height: 1.4;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-dark);
  color: var(--text-primary);
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.form-checkbox {
  width: auto;
  margin-right: 8px;
  margin-bottom: 0;
}

.form-error {
  margin-top: 4px;
  font-size: 12px;
  color: var(--danger-color);
  line-height: 1.4;
}

.form-hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 80px;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background: var(--secondary-color);
  transform: translateY(-1px);
}

.btn-secondary {
  background: var(--bg-card-hover);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: var(--bg-dark);
  color: var(--text-primary);
}

.btn-secondary:disabled {
  background: var(--bg-card-hover);
  color: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-secondary:disabled:hover {
  background: var(--bg-card-hover);
  color: var(--text-muted);
  transform: none;
}

/* 手机端优化 */
@media (max-width: 480px) {
  .settings-modal {
    width: 95%;
    margin: 20px;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 16px;
  }

  .modal-footer {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    order: 2;
  }

  .btn-secondary {
    order: 1;
  }
}
</style>