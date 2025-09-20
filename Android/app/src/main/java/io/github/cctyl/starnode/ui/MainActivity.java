package io.github.cctyl.starnode.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private Spinner deviceSpinner;
    private Button clearFilterButton;
    
    private StatsAdapter statsAdapter;
    private DeviceAdapter deviceAdapter;
    private List<DeviceInfo> allDevices;
    private List<DeviceInfo> filteredDevices;
    private WebSocketManager webSocketManager;
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> deviceNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerViews();
        initWebSocket();
        setupDeviceFilter();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        statusText = findViewById(R.id.statusText);
        statsRecyclerView = findViewById(R.id.statsRecyclerView);
        deviceRecyclerView = findViewById(R.id.deviceRecyclerView);
        deviceSpinner = findViewById(R.id.deviceSpinner);
        clearFilterButton = findViewById(R.id.clearFilterButton);
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
        // 更新状态文本（显示所有设备数量）
        statusText.setText(allDevices.size() + " 在线");
        
        // 更新统计数据（基于所有设备）
        statsAdapter.updateStats(createStatsData());
        
        // 应用当前的筛选条件来更新设备列表
        applyCurrentFilter();
    }
    
    private void applyCurrentFilter() {
        int currentSelection = deviceSpinner.getSelectedItemPosition();
        if (currentSelection == 0) {
            // 显示所有设备
            showAllDevices();
        } else {
            // 显示特定设备
            filterToDevice(currentSelection - 1);
        }
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
                if ("windows".equalsIgnoreCase(osType) || "Windows_NT".equalsIgnoreCase(osType)) {
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

    private void setupDeviceFilter() {
        // 初始化设备名称列表
        deviceNameList = new ArrayList<>();
        deviceNameList.add("所有设备"); // 默认选项
        
        // 创建Spinner适配器
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceNameList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(spinnerAdapter);
        
        // 设置Spinner选择监听器
        deviceSpinner.setOnItemSelectedListener(createSpinnerListener());
        
        // 设置清除筛选按钮监听器
        clearFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceSpinner.setSelection(0); // 选择"所有设备"
                showAllDevices();
            }
        });
    }

    private AdapterView.OnItemSelectedListener createSpinnerListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // 选择"所有设备"
                    showAllDevices();
                } else {
                    // 选择特定设备
                    filterToDevice(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showAllDevices();
            }
        };
    }

    private void showAllDevices() {
        filteredDevices.clear();
        filteredDevices.addAll(allDevices);
        deviceAdapter.updateDevices(filteredDevices);
    }

    private void filterToDevice(int deviceIndex) {
        filteredDevices.clear();
        if (deviceIndex >= 0 && deviceIndex < allDevices.size()) {
            filteredDevices.add(allDevices.get(deviceIndex));
        }
        deviceAdapter.updateDevices(filteredDevices);
    }

    private void updateDeviceSpinner() {
        // 保存当前选中的设备索引
        int currentSelection = deviceSpinner.getSelectedItemPosition();
        String currentSelectedDevice = null;
        if (currentSelection > 0 && currentSelection <= allDevices.size()) {
            DeviceInfo currentDevice = allDevices.get(currentSelection - 1);
            currentSelectedDevice = getDeviceDisplayName(currentDevice);
        }
        
        // 清空并重新填充设备列表
        deviceNameList.clear();
        deviceNameList.add("所有设备");
        
        for (DeviceInfo device : allDevices) {
            deviceNameList.add(getDeviceDisplayName(device));
        }
        
        spinnerAdapter.notifyDataSetChanged();
        
        // 尝试恢复之前的选择
        if (currentSelectedDevice != null) {
            int newIndex = deviceNameList.indexOf(currentSelectedDevice);
            if (newIndex > 0) {
                // 临时禁用监听器，避免触发不必要的筛选
                deviceSpinner.setOnItemSelectedListener(null);
                deviceSpinner.setSelection(newIndex);
                deviceSpinner.setOnItemSelectedListener(createSpinnerListener());
                // 手动应用筛选
                filterToDevice(newIndex - 1);
                return;
            }
        }
        
        // 如果无法恢复之前的选择，默认选择"所有设备"
        deviceSpinner.setOnItemSelectedListener(null);
        deviceSpinner.setSelection(0);
        deviceSpinner.setOnItemSelectedListener(createSpinnerListener());
        showAllDevices();
    }

    private String getDeviceDisplayName(DeviceInfo device) {
        StringBuilder name = new StringBuilder();
        if (device.getOsInfo() != null && device.getOsInfo().getHostname() != null) {
            name.append(device.getOsInfo().getHostname());
        } else {
            name.append("未知设备");
        }
        
        if (device.getOsInfo() != null && device.getOsInfo().getIp() != null) {
            name.append(" (").append(device.getOsInfo().getIp()).append(")");
        }
        
        return name.toString();
    }

    // WebSocket监听器回调方法
    @Override
    public void onDataReceived(List<DeviceInfo> devices) {
        if (devices != null) {
            allDevices.clear();
            allDevices.addAll(devices);
            updateUI();
            updateDeviceSpinner();
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