// WebSocket 服务封装
class WebSocketService {
    constructor() {
        this.ws = null;
        this.reconnectTimer = null;
        this.reconnectDelay = 3000;
        this.url = this.getStoredUrl() || "ws://10.0.8.1:6080/?token=abcdef&type=view&endpointName=web-access";
        this.onMessageCallback = null;
        this.onStatusChangeCallback = null;
    }

    getStoredUrl() {
        return localStorage.getItem('ws_url');
    }

    saveUrl(url) {
        localStorage.setItem('ws_url', url);
        this.url = url;
    }

    updateConfig(newUrl) {
        if (this.url === newUrl && this.ws?.readyState === WebSocket.OPEN) return;

        this.saveUrl(newUrl);
        this.disconnect();
        this.connect();
    }

    connect() {
        try {
            this.updateStatus('connecting', '正在连接...');

            this.ws = new WebSocket(this.url);

            this.ws.onopen = () => {
                console.log('WebSocket连接已建立');
                this.updateStatus('connected', '已连接');

                if (this.reconnectTimer) {
                    clearTimeout(this.reconnectTimer);
                    this.reconnectTimer = null;
                }
            };

            this.ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    // console.log('收到数据:', data);

                    if (this.onMessageCallback && Array.isArray(data)) {
                        this.onMessageCallback(data);
                    } else if (!Array.isArray(data)) {
                        console.warn('数据格式不正确，期望数组格式');
                    }
                } catch (error) {
                    console.error('数据解析失败:', error);
                    this.updateStatus('error', '数据解析错误');
                }
            };

            this.ws.onerror = (error) => {
                console.error('WebSocket错误:', error);
                this.updateStatus('error', '连接错误');
            };

            this.ws.onclose = () => {
                console.log('WebSocket连接已关闭');
                this.updateStatus('disconnected', '连接已断开，准备重连...');

                this.reconnectTimer = setTimeout(() => {
                    console.log('尝试重新连接...');
                    this.connect();
                }, this.reconnectDelay);
            };

        } catch (error) {
            console.error('WebSocket连接失败:', error);
            this.updateStatus('error', '连接失败');

            this.reconnectTimer = setTimeout(() => {
                this.connect();
            }, this.reconnectDelay);
        }
    }

    updateStatus(status, message) {
        if (this.onStatusChangeCallback) {
            this.onStatusChangeCallback({ status, message });
        }
    }

    onMessage(callback) {
        this.onMessageCallback = callback;
    }

    onStatusChange(callback) {
        this.onStatusChangeCallback = callback;
    }

    disconnect() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
        if (this.ws) {
            // 移除监听器防止触发重连逻辑
            this.ws.onclose = null;
            this.ws.close();
            this.ws = null;
        }
    }
}

export default new WebSocketService();
