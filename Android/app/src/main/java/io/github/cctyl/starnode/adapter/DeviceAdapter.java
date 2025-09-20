package io.github.cctyl.starnode.adapter;

import android.graphics.Color;
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
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<DeviceInfo> devices;

    public DeviceAdapter(List<DeviceInfo> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_card, parent, false);
        return new DeviceViewHolder(view);
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

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceAvatar;
        private TextView deviceName;
        private TextView deviceLocation;
        private View statusIndicator;
        private RecyclerView metricsRecyclerView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceAvatar = itemView.findViewById(R.id.deviceAvatar);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceLocation = itemView.findViewById(R.id.deviceLocation);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            metricsRecyclerView = itemView.findViewById(R.id.metricsRecyclerView);

            // 设置横向滑动的RecyclerView
            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext(), 
                    LinearLayoutManager.HORIZONTAL, false);
            metricsRecyclerView.setLayoutManager(layoutManager);
        }

        public void bind(DeviceInfo device) {
            // 设置设备信息
            if (device.getOsInfo() != null) {
                deviceName.setText(device.getOsInfo().getHostname() != null ? 
                        device.getOsInfo().getHostname() : "未知设备");
                
                String location = "";
                if (device.getOsInfo().getIp() != null) {
                    location += device.getOsInfo().getIp();
                }
                if (device.getIpInfo() != null && device.getIpInfo().getCity() != null) {
                    location += " • " + device.getIpInfo().getCity();
                }
                deviceLocation.setText(location);

                // 设置设备头像
                String osType = device.getOsInfo().getType();
                if ("Linux".equalsIgnoreCase(osType)) {
                    deviceAvatar.setText("L");
                    deviceAvatar.setBackgroundColor(Color.parseColor("#FF9800"));
                } else if ("Windows_NT".equalsIgnoreCase(osType)) {
                    deviceAvatar.setText("W");
                    deviceAvatar.setBackgroundColor(Color.parseColor("#2196F3"));
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
                metrics.add(new MetricItem("🖥️", String.format("%.0f%%", cpuUsage), 
                        "CPU", cpuDetail, cpuUsage, getColorForUsage(cpuUsage)));
            }

            // 内存指标
            if (device.getMemInfo() != null) {
                double memUsage = device.getMemInfo().getUsedMemPercentage();
                String memDetail = String.format("%.1f/%.1fGB", 
                        device.getMemInfo().getUsedMemMb() / 1024.0,
                        device.getMemInfo().getTotalMemMb() / 1024.0);
                metrics.add(new MetricItem("💾", String.format("%.0f%%", memUsage), 
                        "内存", memDetail, memUsage, getColorForUsage(memUsage)));
            }

            // 磁盘指标
            if (device.getDriveInfo() != null) {
                double diskUsage = device.getDriveInfo().getUsedPercentage();
                String diskDetail = String.format("%.1f/%.1fGB", 
                        device.getDriveInfo().getUsedGb(),
                        device.getDriveInfo().getTotalGb());
                metrics.add(new MetricItem("💿", String.format("%.0f%%", diskUsage), 
                        "磁盘", diskDetail, diskUsage, getColorForUsage(diskUsage)));
            }

            // 网络指标
            if (device.getNetstatInfo() != null && device.getNetstatInfo().getTotal() != null) {
                double totalInput = device.getNetstatInfo().getTotal().getInputMb();
                double totalOutput = device.getNetstatInfo().getTotal().getOutputMb();
                String networkDetail = String.format("↓%.1fMB ↑%.1fMB", totalInput, totalOutput);
                metrics.add(new MetricItem("🌐", "网络", "流量", networkDetail, 0, "#45a0ff"));
            }

            // 设置指标适配器
            MetricsAdapter metricsAdapter = new MetricsAdapter(metrics);
            metricsRecyclerView.setAdapter(metricsAdapter);

            // 设置状态指示器颜色
            setStatusIndicator(device);
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