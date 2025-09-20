package io.github.cctyl.starnode.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.cctyl.starnode.model.DeviceInfo;
import io.github.cctyl.starnode.utils.ConfigManager;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    
    private WebSocketClient webSocketClient;
    private Handler mainHandler;
    private Gson gson;
    private WebSocketListener listener;
    private boolean isConnecting = false;
    private boolean shouldReconnect = true;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 5000; // 5秒重连延迟
    private ConfigManager configManager;

    public interface WebSocketListener {
        void onDataReceived(List<DeviceInfo> devices);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public WebSocketManager(Context context) {
        mainHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
        configManager = new ConfigManager(context);
    }

    public void setListener(WebSocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        if (isConnecting || (webSocketClient != null && webSocketClient.isOpen())) {
            Log.d(TAG, "Already connecting or connected");
            return;
        }

        isConnecting = true;
        shouldReconnect = true;

        try {
            String websocketUrl = configManager.buildWebSocketUrl();
            Log.d(TAG, "Connecting to: " + websocketUrl);
            URI serverUri = new URI(websocketUrl);
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "WebSocket connected");
                    isConnecting = false;
                    reconnectAttempts = 0;
                    
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onConnected();
                        }
                    });
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received message: " + message.substring(0, Math.min(message.length(), 100)) + "...");
                    
                    try {
                        Type listType = new TypeToken<List<DeviceInfo>>(){}.getType();
                        List<DeviceInfo> devices = gson.fromJson(message, listType);
                        
                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onDataReceived(devices);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing JSON message", e);
                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onError("数据解析错误: " + e.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed: " + reason + " (code: " + code + ")");
                    isConnecting = false;
                    
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onDisconnected();
                        }
                    });

                    // 如果需要重连且未达到最大重连次数
                    if (shouldReconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                    isConnecting = false;
                    
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onError("连接错误: " + ex.getMessage());
                        }
                    });
                }
            };

            webSocketClient.connect();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create WebSocket connection", e);
            isConnecting = false;
            
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onError("创建连接失败: " + e.getMessage());
                }
            });
        }
    }

    private void scheduleReconnect() {
        reconnectAttempts++;
        Log.d(TAG, "Scheduling reconnect attempt " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);
        
        mainHandler.postDelayed(() -> {
            if (shouldReconnect) {
                connect();
            }
        }, RECONNECT_DELAY_MS);
    }

    public void disconnect() {
        shouldReconnect = false;
        
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        } else {
            Log.w(TAG, "Cannot send message: WebSocket not connected");
        }
    }
}