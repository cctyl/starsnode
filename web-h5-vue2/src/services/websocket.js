// WebSocket服务类
class WebSocketService {
  constructor() {
    this.ws = null;
    this.reconnectTimer = null;
    this.reconnectDelay = 5000;
    this.wsUrl = "";
    this.callbacks = {};
    this.connectionStatus = 'disconnected';
    this.autoReconnect = true;
    this.isManualDisconnect = false;
  }

  // 连接WebSocket
  connect(settings = {}) {
    try {
      // 应用用户设置
      if (settings.wsUrl) {
        this.wsUrl = settings.wsUrl;
      }
      if (settings.reconnectInterval) {
        this.reconnectDelay = settings.reconnectInterval * 1000;
      }
      if (settings.autoReconnect !== undefined) {
        this.autoReconnect = settings.autoReconnect;
      }

      // 重置手动断开标志
      this.isManualDisconnect = false;
      this.updateConnectionStatus('connecting', '正在连接...');

      this.ws = new WebSocket(this.wsUrl);

      this.ws.onopen = () => {
        console.log('WebSocket连接已建立');
        this.updateConnectionStatus('connected', '已连接');

        if (this.reconnectTimer) {
          clearTimeout(this.reconnectTimer);
          this.reconnectTimer = null;
        }
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log('收到数据:', data);

          if (Array.isArray(data)) {
            this.processDevicesData(data);
          } else {
            console.warn('数据格式不正确，期望数组格式');
          }
        } catch (error) {
          console.error('数据解析失败:', error);
          this.updateConnectionStatus('error', '数据解析错误');
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        this.updateConnectionStatus('error', '连接错误');
      };

      this.ws.onclose = () => {
        console.log('WebSocket连接已关闭');

        // 只有在非手动断开时才显示断开连接通知
        if (!this.isManualDisconnect) {
          this.updateConnectionStatus('disconnected', '连接已断开');
        }

        // 根据用户设置决定是否自动重连
        if (this.autoReconnect) {
          if (!this.isManualDisconnect) {
            this.updateConnectionStatus('disconnected', '连接已断开，准备重连...');
          }
          this.reconnectTimer = setTimeout(() => {
            console.log('尝试重新连接...');
            this.connect();
          }, this.reconnectDelay);
        } else {
          this.updateConnectionStatus('disconnected', '连接已断开，自动重连已禁用');
        }
      };

    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.updateConnectionStatus('error', '连接失败');

      // 根据用户设置决定是否自动重连
      if (this.autoReconnect) {
        this.reconnectTimer = setTimeout(() => {
          this.connect();
        }, this.reconnectDelay);
      }
    }
  }

  // 更新连接状态
  updateConnectionStatus(status, message) {
    this.connectionStatus = status;
    if (this.callbacks.onStatusChange) {
      this.callbacks.onStatusChange(status, message);
    }
  }

  // 处理设备数据
  processDevicesData(devices) {
    if (this.callbacks.onData) {
      this.callbacks.onData(devices);
    }
  }

  // 注册回调函数
  on(eventName, callback) {
    this.callbacks[eventName] = callback;
  }

  // 关闭连接
  disconnect() {
    // 设置手动断开标志
    this.isManualDisconnect = true;

    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.close();
    }
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }
}

export default new WebSocketService();