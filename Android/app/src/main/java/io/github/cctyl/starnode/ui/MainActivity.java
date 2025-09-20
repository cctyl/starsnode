package io.github.cctyl.starnode.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import io.github.cctyl.starnode.R;
import io.github.cctyl.starnode.adapter.DeviceAdapter;
import io.github.cctyl.starnode.adapter.StatsAdapter;
import io.github.cctyl.starnode.model.DeviceInfo;
import io.github.cctyl.starnode.model.StatItem;
import io.github.cctyl.starnode.websocket.WebSocketManager;

public class MainActivity extends AppCompatActivity implements WebSocketManager.WebSocketListener {

    private TextView statusText;
    private RecyclerView statsRecyclerView;
    private RecyclerView deviceRecyclerView;
    private EditText searchEditText;
    
    private StatsAdapter statsAdapter;
    private DeviceAdapter deviceAdapter;
    private List<DeviceInfo> allDevices;
    private List<DeviceInfo> filteredDevices;
    private WebSocketManager webSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerViews();
        initWebSocket();
        setupSearch();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        statusText = findViewById(R.id.statusText);
        statsRecyclerView = findViewById(R.id.statsRecyclerView);
        deviceRecyclerView = findViewById(R.id.deviceRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
    }

    private void setupRecyclerViews() {
        // 设置统计卡片横向滑动
        LinearLayoutManager statsLayoutManager = new LinearLayoutManager(this, 
                LinearLayoutManager.HORIZONTAL, false);
        statsRecyclerView.setLayoutManager(statsLayoutManager);
        
        // 设置设备列表垂直滑动
        LinearLayoutManager deviceLayoutManager = new LinearLayoutManager(this);
        deviceRecyclerView.setLayoutManager(deviceLayoutManager);
        
        // 初始化适配器
        allDevices = new ArrayList<>();
        filteredDevices = new ArrayList<>();
        
        statsAdapter = new StatsAdapter(createStatsData());
        deviceAdapter = new DeviceAdapter(filteredDevices);
        
        statsRecyclerView.setAdapter(statsAdapter);
        deviceRecyclerView.setAdapter(deviceAdapter);
    }

    private void initWebSocket() {
        webSocketManager = new WebSocketManager();
        webSocketManager.setListener(this);
        
        // 初始化空数据
        allDevices = new ArrayList<>();
        updateUI();
        
        // 开始连接WebSocket
        webSocketManager.connect();
        
        // 更新状态为连接中
        statusText.setText("连接中...");
    }

    private void updateUI() {
        filteredDevices.clear();
        filteredDevices.addAll(allDevices);
        
        // 更新状态文本
        statusText.setText(allDevices.size() + " 在线");
        
        // 更新统计数据
        statsAdapter.updateStats(createStatsData());
        
        // 更新设备列表
        deviceAdapter.updateDevices(filteredDevices);
    }

    private List<StatItem> createStatsData() {
        List<StatItem> stats = new ArrayList<>();
        
        int onlineDevices = allDevices.size();
        int windowsCount = 0;
        int linuxCount = 0;
        int totalCpuCores = 0;
        double totalMemoryGB = 0;
        
        for (DeviceInfo device : allDevices) {
            if (device.getOsInfo() != null) {
                String osType = device.getOsInfo().getType();
                if ("Windows_NT".equalsIgnoreCase(osType)) {
                    windowsCount++;
                } else if ("Linux".equalsIgnoreCase(osType)) {
                    linuxCount++;
                }
            }
            
            if (device.getCpuInfo() != null) {
                totalCpuCores += device.getCpuInfo().getCpuCount();
            }
            
            if (device.getMemInfo() != null) {
                totalMemoryGB += device.getMemInfo().getTotalMemMb() / 1024.0;
            }
        }
        
        stats.add(new StatItem("📱", String.valueOf(onlineDevices), "在线设备"));
        stats.add(new StatItem("💻", String.valueOf(windowsCount), "Windows"));
        stats.add(new StatItem("🐧", String.valueOf(linuxCount), "Linux"));
        stats.add(new StatItem("🖥️", String.valueOf(totalCpuCores), "CPU核心"));
        stats.add(new StatItem("💾", String.format("%.0fGB", totalMemoryGB), "总内存"));
        
        return stats;
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDevices(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterDevices(String query) {
        filteredDevices.clear();
        
        if (query.isEmpty()) {
            filteredDevices.addAll(allDevices);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (DeviceInfo device : allDevices) {
                boolean matches = false;
                
                // 检查主机名
                if (device.getOsInfo() != null && device.getOsInfo().getHostname() != null) {
                    if (device.getOsInfo().getHostname().toLowerCase().contains(lowerCaseQuery)) {
                        matches = true;
                    }
                }
                
                // 检查IP地址
                if (device.getOsInfo() != null && device.getOsInfo().getIp() != null) {
                    if (device.getOsInfo().getIp().contains(lowerCaseQuery)) {
                        matches = true;
                    }
                }
                
                // 检查城市信息
                if (device.getIpInfo() != null && device.getIpInfo().getCity() != null) {
                    if (device.getIpInfo().getCity().toLowerCase().contains(lowerCaseQuery)) {
                        matches = true;
                    }
                }
                
                if (matches) {
                    filteredDevices.add(device);
                }
            }
        }
        
        deviceAdapter.updateDevices(filteredDevices);
    }

    // WebSocket监听器回调方法
    @Override
    public void onDataReceived(List<DeviceInfo> devices) {
        if (devices != null) {
            allDevices.clear();
            allDevices.addAll(devices);
            updateUI();
        }
    }

    @Override
    public void onConnected() {
        statusText.setText("已连接");
        Toast.makeText(this, "WebSocket连接成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        statusText.setText("连接断开");
        Toast.makeText(this, "WebSocket连接断开", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String error) {
        statusText.setText("连接错误");
        Toast.makeText(this, "连接错误: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 可以选择在暂停时断开连接以节省资源
        // if (webSocketManager != null) {
        //     webSocketManager.disconnect();
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果在onPause中断开了连接，这里重新连接
        // if (webSocketManager != null && !webSocketManager.isConnected()) {
        //     webSocketManager.connect();
        // }
    }
}