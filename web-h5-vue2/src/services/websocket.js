// WebSocket服务类
class WebSocketService {
  constructor() {
    this.ws = null;
    this.reconnectTimer = null;
    this.reconnectDelay = 3000;
    this.wsUrl = "ws://10.0.8.1:6080/?token=abcdef&type=view&endpointName=web-access";
    this.callbacks = {};
    this.connectionStatus = 'disconnected';
  }

  // 连接WebSocket
  connect() {
    try {
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
        this.updateConnectionStatus('disconnected', '连接已断开，准备重连...');

        this.reconnectTimer = setTimeout(() => {
          console.log('尝试重新连接...');
          this.connect();
        }, this.reconnectDelay);
      };

    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.updateConnectionStatus('error', '连接失败');

      this.reconnectTimer = setTimeout(() => {
        this.connect();
      }, this.reconnectDelay);
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