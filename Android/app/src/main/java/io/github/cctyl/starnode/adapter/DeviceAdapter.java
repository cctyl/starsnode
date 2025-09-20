package io.github.cctyl.starnode.adapter;

import android.graphics.Color;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.cctyl.starnode.R;
import io.github.cctyl.starnode.model.DeviceInfo;
import io.github.cctyl.starnode.model.MetricItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.animation.ObjectAnimator;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<DeviceInfo> devices;
    private Map<String, Parcelable> scrollStates = new HashMap<>();

    public DeviceAdapter(List<DeviceInfo> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_card, parent, false);
        return new DeviceViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceInfo device = devices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void updateDevices(List<DeviceInfo> newDevices) {
        this.devices = newDevices;
        notifyDataSetChanged();
    }
    
    public void saveScrollState(String deviceKey, Parcelable state) {
        scrollStates.put(deviceKey, state);
    }
    
    public Parcelable getScrollState(String deviceKey) {
        return scrollStates.get(deviceKey);
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceAvatar;
        private TextView deviceName;
        private TextView deviceLocation;
        private TextView deviceUptime;
        private View statusIndicator;
        private RecyclerView metricsRecyclerView;
        private LinearLayoutManager metricsLayoutManager;
        private DeviceAdapter parentAdapter;
        
        // 网络接口相关
        private LinearLayout networkInfoContainer;
        private LinearLayout networkHeaderContainer;
        private TextView networkInterfaceCount;
        private ImageView networkExpandIcon;
        private RecyclerView networkInterfaceRecyclerView;
        private NetworkInterfaceAdapter networkInterfaceAdapter;
        private boolean isNetworkExpanded = false;

        public DeviceViewHolder(@NonNull View itemView, DeviceAdapter adapter) {
            super(itemView);
            this.parentAdapter = adapter;
            deviceAvatar = itemView.findViewById(R.id.deviceAvatar);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceLocation = itemView.findViewById(R.id.deviceLocation);
            deviceUptime = itemView.findViewById(R.id.deviceUptime);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            metricsRecyclerView = itemView.findViewById(R.id.metricsRecyclerView);

            // 设置横向滑动的RecyclerView
            metricsLayoutManager = new LinearLayoutManager(itemView.getContext(), 
                    LinearLayoutManager.HORIZONTAL, false);
            metricsRecyclerView.setLayoutManager(metricsLayoutManager);
            
            // 初始化网络接口相关组件
            networkInfoContainer = itemView.findViewById(R.id.networkInfoContainer);
            networkHeaderContainer = itemView.findViewById(R.id.networkHeaderContainer);
            networkInterfaceCount = itemView.findViewById(R.id.networkInterfaceCount);
            networkExpandIcon = itemView.findViewById(R.id.networkExpandIcon);
            networkInterfaceRecyclerView = itemView.findViewById(R.id.networkInterfaceRecyclerView);
            
            // 设置网络接口RecyclerView
            LinearLayoutManager networkLayoutManager = new LinearLayoutManager(itemView.getContext());
            networkInterfaceRecyclerView.setLayoutManager(networkLayoutManager);
            networkInterfaceAdapter = new NetworkInterfaceAdapter();
            networkInterfaceRecyclerView.setAdapter(networkInterfaceAdapter);
            
            // 设置网络接口标题栏点击事件
            networkHeaderContainer.setOnClickListener(v -> toggleNetworkExpansion());
        }

        public void bind(DeviceInfo device) {
            // 生成设备唯一标识符
            String deviceKey = getDeviceKey(device);
            
            // 保存当前滑动状态（如果存在）
            if (metricsLayoutManager != null) {
                Parcelable currentState = metricsLayoutManager.onSaveInstanceState();
                if (currentState != null) {
                    parentAdapter.saveScrollState(deviceKey, currentState);
                }
            }
            
            // 设置设备信息
            if (device.getOsInfo() != null) {
                deviceName.setText(device.getOsInfo().getHostname() != null ? 
                        device.getOsInfo().getHostname() : "未知设备");
                
                String location = "";
                boolean hasLocation = false;
                
                // 添加IP地址
                if (device.getOsInfo().getIp() != null && !device.getOsInfo().getIp().trim().isEmpty()) {
                    location += device.getOsInfo().getIp();
                    hasLocation = true;
                }
                
                // 添加城市信息
                if (device.getIpInfo() != null && device.getIpInfo().getCity() != null && 
                    !device.getIpInfo().getCity().trim().isEmpty()) {
                    if (hasLocation) {
                        location += " • ";
                    }
                    location += device.getIpInfo().getCity();
                    hasLocation = true;
                }
                
                // 如果没有任何位置信息，显示"未知地点"
                if (!hasLocation || location.trim().isEmpty()) {
                    location = "未知地点";
                }
                
                deviceLocation.setText(location);
                
                // 设置开机时间
                double uptime = device.getOsInfo().getUptime();
                if (uptime > 0) {
                    String uptimeText = formatUptime(uptime);
                    deviceUptime.setText(uptimeText);
                } else {
                    deviceUptime.setText("运行时间未知");
                }

                // 设置设备头像
                String osType = device.getOsInfo().getType();
                if ("Linux".equalsIgnoreCase(osType)) {
                    deviceAvatar.setText("L");
                    deviceAvatar.setBackgroundResource(R.drawable.gradient_device_linux);
                } else if ("windows".equalsIgnoreCase(osType) || "Windows_NT".equalsIgnoreCase(osType)) {
                    deviceAvatar.setText("W");
                    deviceAvatar.setBackgroundResource(R.drawable.gradient_device_windows);
                } else {
                    deviceAvatar.setText("?");
                    deviceAvatar.setBackgroundColor(Color.parseColor("#9E9E9E"));
                }
            }

            // 创建指标数据
            List<MetricItem> metrics = new ArrayList<>();
            
            // CPU指标
            if (device.getCpuInfo() != null) {
                double cpuUsage = device.getCpuInfo().getCpuUsage();
                String cpuDetail = device.getCpuInfo().getCpuCount() + "核心";
                metrics.add(new MetricItem("cpu", String.format("%.0f%%", cpuUsage), 
                        "CPU", cpuDetail, cpuUsage, getColorForUsage(cpuUsage)));
            }

            // 内存指标
            if (device.getMemInfo() != null) {
                double memUsage = device.getMemInfo().getUsedMemPercentage();
                String memDetail = String.format("%.1f/%.1fGB", 
                        device.getMemInfo().getUsedMemMb() / 1024.0,
                        device.getMemInfo().getTotalMemMb() / 1024.0);
                metrics.add(new MetricItem("memory", String.format("%.0f%%", memUsage), 
                        "内存", memDetail, memUsage, getColorForUsage(memUsage)));
            }

            // 磁盘指标
            if (device.getDriveInfo() != null) {
                double diskUsage = device.getDriveInfo().getUsedPercentage();
                String diskDetail = String.format("%.1f/%.1fGB", 
                        device.getDriveInfo().getUsedGb(),
                        device.getDriveInfo().getTotalGb());
                metrics.add(new MetricItem("disk", String.format("%.0f%%", diskUsage), 
                        "磁盘", diskDetail, diskUsage, getColorForUsage(diskUsage)));
            }

            // 网络指标
            if (device.getNetstatInfo() != null && device.getNetstatInfo().getTotal() != null) {
                double totalInput = device.getNetstatInfo().getTotal().getInputMb();
                double totalOutput = device.getNetstatInfo().getTotal().getOutputMb();
                String networkDetail = String.format("↓%.1fMB ↑%.1fMB", totalInput, totalOutput);
                metrics.add(new MetricItem("network", "网络", "流量", networkDetail, 0, "#45a0ff"));
            }

            // 设置指标适配器
            MetricsAdapter metricsAdapter = new MetricsAdapter(metrics);
            metricsRecyclerView.setAdapter(metricsAdapter);

            // 恢复滑动状态
            Parcelable savedState = parentAdapter.getScrollState(deviceKey);
            if (savedState != null && metricsLayoutManager != null) {
                metricsLayoutManager.onRestoreInstanceState(savedState);
            }

            // 设置状态指示器颜色
            setStatusIndicator(device);
            
            // 设置网络接口信息
            setupNetworkInterfaces(device);
        }
        
        private void setupNetworkInterfaces(DeviceInfo device) {
            // 检查是否有网络接口数据
            Map<String, List<DeviceInfo.NetInterface>> netInterfaces = device.getNetInterface();
            Map<String, DeviceInfo.NetStat> netStats = null;
            
            // 获取网络统计数据
            if (device.getNetstatInfo() != null && device.getNetstatInfo().getInterfaces() != null) {
                netStats = device.getNetstatInfo().getInterfaces();
            }
            
            // 如果有网络接口数据，显示网络信息容器
            if (netInterfaces != null && !netInterfaces.isEmpty()) {
                // 过滤掉回环接口以减少界面复杂度
                Map<String, List<DeviceInfo.NetInterface>> filteredInterfaces = new HashMap<>();
                for (Map.Entry<String, List<DeviceInfo.NetInterface>> entry : netInterfaces.entrySet()) {
                    String interfaceName = entry.getKey().toLowerCase();
                    // 跳过回环接口，但保留其他所有接口
                    if (!interfaceName.contains("loopback") && 
                        !(interfaceName.equals("lo") && entry.getValue().size() == 2)) {
                        filteredInterfaces.put(entry.getKey(), entry.getValue());
                    }
                }
                
                if (!filteredInterfaces.isEmpty()) {
                    networkInfoContainer.setVisibility(View.VISIBLE);
                    networkInterfaceAdapter.updateInterfaces(filteredInterfaces, netStats);
                    
                    // 更新接口数量显示
                    networkInterfaceCount.setText(filteredInterfaces.size() + "个");
                    
                    // 确保默认是收起状态
                    if (isNetworkExpanded) {
                        networkInterfaceRecyclerView.setVisibility(View.VISIBLE);
                        networkExpandIcon.setRotation(180f);
                    } else {
                        networkInterfaceRecyclerView.setVisibility(View.GONE);
                        networkExpandIcon.setRotation(0f);
                    }
                } else {
                    networkInfoContainer.setVisibility(View.GONE);
                }
            } else {
                networkInfoContainer.setVisibility(View.GONE);
            }
        }
        
        private void toggleNetworkExpansion() {
            isNetworkExpanded = !isNetworkExpanded;
            
            if (isNetworkExpanded) {
                // 展开网络接口列表
                networkInterfaceRecyclerView.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(networkExpandIcon, "rotation", 0f, 180f).start();
            } else {
                // 收起网络接口列表
                networkInterfaceRecyclerView.setVisibility(View.GONE);
                ObjectAnimator.ofFloat(networkExpandIcon, "rotation", 180f, 0f).start();
            }
        }
        
        private String getDeviceKey(DeviceInfo device) {
            // 使用主机名和IP作为唯一标识符
            StringBuilder key = new StringBuilder();
            if (device.getOsInfo() != null) {
                if (device.getOsInfo().getHostname() != null) {
                    key.append(device.getOsInfo().getHostname());
                }
                if (device.getOsInfo().getIp() != null) {
                    key.append("_").append(device.getOsInfo().getIp());
                }
            }
            return key.length() > 0 ? key.toString() : "unknown_device";
        }
        
        /**
         * 格式化开机时间
         * @param uptimeSeconds 开机时间（秒）
         * @return 格式化后的时间字符串
         */
        private String formatUptime(double uptimeSeconds) {
            long totalSeconds = (long) uptimeSeconds;
            
            long days = totalSeconds / (24 * 3600);
            long hours = (totalSeconds % (24 * 3600)) / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            
            StringBuilder result = new StringBuilder("运行 ");
            
            if (days > 0) {
                result.append(days).append("天");
                if (hours > 0) {
                    result.append(" ").append(hours).append("小时");
                }
            } else if (hours > 0) {
                result.append(hours).append("小时");
                if (minutes > 0) {
                    result.append(" ").append(minutes).append("分钟");
                }
            } else if (minutes > 0) {
                result.append(minutes).append("分钟");
            } else {
                result.append("不到1分钟");
            }
            
            return result.toString();
        }

        private String getColorForUsage(double usage) {
            if (usage > 90) {
                return "#f34c4a"; // 红色 - 危险
            } else if (usage > 70) {
                return "#dfa238"; // 橙色 - 警告
            } else {
                return "#45a0ff"; // 蓝色 - 正常
            }
        }

        private void setStatusIndicator(DeviceInfo device) {
            double maxUsage = 0;
            
            if (device.getCpuInfo() != null) {
                maxUsage = Math.max(maxUsage, device.getCpuInfo().getCpuUsage());
            }
            if (device.getMemInfo() != null) {
                maxUsage = Math.max(maxUsage, device.getMemInfo().getUsedMemPercentage());
            }
            if (device.getDriveInfo() != null) {
                maxUsage = Math.max(maxUsage, device.getDriveInfo().getUsedPercentage());
            }

            int color;
            if (maxUsage > 90) {
                color = Color.parseColor("#f34c4a"); // 红色
            } else if (maxUsage > 70) {
                color = Color.parseColor("#dfa238"); // 橙色
            } else {
                color = Color.parseColor("#4caf50"); // 绿色
            }
            
            statusIndicator.setBackgroundColor(color);
        }
    }
}