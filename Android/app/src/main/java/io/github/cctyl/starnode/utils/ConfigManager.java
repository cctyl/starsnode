package io.github.cctyl.starnode.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {
    
    private static final String PREF_NAME = "starnode_config";
    private static final String KEY_WEBSOCKET_URL = "websocket_url";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_DEVICE_NAME = "device_name";
    
    // 默认配置值
    public static final String DEFAULT_WEBSOCKET_URL = "ws://10.0.8.1:6080";
    public static final String DEFAULT_TOKEN = "abcdef";
    public static final String DEFAULT_DEVICE_NAME = "web-access";
    
    private SharedPreferences preferences;
    
    public ConfigManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    // WebSocket URL
    public void saveWebSocketUrl(String url) {
        preferences.edit().putString(KEY_WEBSOCKET_URL, url).apply();
    }
    
    public String getWebSocketUrl() {
        return preferences.getString(KEY_WEBSOCKET_URL, DEFAULT_WEBSOCKET_URL);
    }
    
    // Token
    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }
    
    public String getToken() {
        return preferences.getString(KEY_TOKEN, DEFAULT_TOKEN);
    }
    
    // Device Name
    public void saveDeviceName(String deviceName) {
        preferences.edit().putString(KEY_DEVICE_NAME, deviceName).apply();
    }
    
    public String getDeviceName() {
        return preferences.getString(KEY_DEVICE_NAME, DEFAULT_DEVICE_NAME);
    }
    
    // 构建完整的WebSocket连接URL
    public String buildWebSocketUrl() {
        String baseUrl = getWebSocketUrl();
        String token = getToken();
        String deviceName = getDeviceName();
        
        // 确保基础URL以正确格式结尾
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        
        // 构建完整URL
        return String.format("%s?token=%s&type=view&endpointName=%s", 
                baseUrl, token, deviceName);
    }
    
    // 重置所有配置为默认值
    public void resetToDefaults() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_WEBSOCKET_URL, DEFAULT_WEBSOCKET_URL);
        editor.putString(KEY_TOKEN, DEFAULT_TOKEN);
        editor.putString(KEY_DEVICE_NAME, DEFAULT_DEVICE_NAME);
        editor.apply();
    }
    
    // 检查配置是否完整
    public boolean isConfigComplete() {
        String url = getWebSocketUrl();
        String token = getToken();
        String deviceName = getDeviceName();
        
        return url != null && !url.trim().isEmpty() &&
               token != null && !token.trim().isEmpty() &&
               deviceName != null && !deviceName.trim().isEmpty();
    }
}