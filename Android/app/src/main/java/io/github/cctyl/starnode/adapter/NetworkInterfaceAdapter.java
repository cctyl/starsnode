package io.github.cctyl.starnode.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.cctyl.starnode.R;
import io.github.cctyl.starnode.model.DeviceInfo;
import io.github.cctyl.starnode.model.NetworkInterfaceInfo;

public class NetworkInterfaceAdapter extends RecyclerView.Adapter<NetworkInterfaceAdapter.NetworkInterfaceViewHolder> {
    
    private List<NetworkInterfaceInfo> interfaces;
    
    public NetworkInterfaceAdapter() {
        this.interfaces = new ArrayList<>();
    }
    
    public void updateInterfaces(Map<String, List<DeviceInfo.NetInterface>> netInterfaces, 
                               Map<String, DeviceInfo.NetStat> netStats) {
        interfaces.clear();
        
        if (netInterfaces != null) {
            for (Map.Entry<String, List<DeviceInfo.NetInterface>> entry : netInterfaces.entrySet()) {
                String interfaceName = entry.getKey();
                List<DeviceInfo.NetInterface> interfaceList = entry.getValue();
                
                // 跳过回环接口，减少界面复杂度
                if (interfaceName.toLowerCase().contains("lo") && 
                    interfaceName.toLowerCase().contains("loopback")) {
                    continue;
                }
                
                NetworkInterfaceInfo info = new NetworkInterfaceInfo();
                info.setName(interfaceName);
                info.setInterfaces(interfaceList);
                
                // 设置流量统计
                if (netStats != null) {
                    DeviceInfo.NetStat stat = netStats.get(interfaceName);
                    if (stat == null) {
                        // 尝试带冒号的键名
                        stat = netStats.get(interfaceName + ":");
                    }
                    if (stat != null) {
                        info.setInputMb(stat.getInputMb());
                        info.setOutputMb(stat.getOutputMb());
                    }
                }
                
                interfaces.add(info);
            }
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public NetworkInterfaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_network_interface, parent, false);
        return new NetworkInterfaceViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NetworkInterfaceViewHolder holder, int position) {
        NetworkInterfaceInfo info = interfaces.get(position);
        holder.bind(info);
    }
    
    @Override
    public int getItemCount() {
        return interfaces.size();
    }
    
    static class NetworkInterfaceViewHolder extends RecyclerView.ViewHolder {
        private TextView interfaceName;
        private TextView interfaceType;
        private TextView trafficStats;
        private TextView interfaceStatus;
        private ImageView interfaceIcon;
        private ImageView expandButton;
        private LinearLayout detailsContainer;
        private LinearLayout ipv4Container;
        private LinearLayout ipv6Container;
        private TextView ipv4Address;
        private TextView ipv4Netmask;
        private TextView macAddress;
        private TextView ipv6Address;
        
        private boolean isExpanded = false;
        
        public NetworkInterfaceViewHolder(@NonNull View itemView) {
            super(itemView);
            
            interfaceName = itemView.findViewById(R.id.interfaceName);
            interfaceType = itemView.findViewById(R.id.interfaceType);
            trafficStats = itemView.findViewById(R.id.trafficStats);
            interfaceStatus = itemView.findViewById(R.id.interfaceStatus);
            interfaceIcon = itemView.findViewById(R.id.interfaceIcon);
            expandButton = itemView.findViewById(R.id.expandButton);
            detailsContainer = itemView.findViewById(R.id.detailsContainer);
            ipv4Container = itemView.findViewById(R.id.ipv4Container);
            ipv6Container = itemView.findViewById(R.id.ipv6Container);
            ipv4Address = itemView.findViewById(R.id.ipv4Address);
            ipv4Netmask = itemView.findViewById(R.id.ipv4Netmask);
            macAddress = itemView.findViewById(R.id.macAddress);
            ipv6Address = itemView.findViewById(R.id.ipv6Address);
            
            // 设置点击事件
            itemView.setOnClickListener(v -> toggleExpansion());
            expandButton.setOnClickListener(v -> toggleExpansion());
        }
        
        public void bind(NetworkInterfaceInfo info) {
            interfaceName.setText(info.getName());
            
            // 设置接口类型和图标
            setInterfaceTypeAndIcon(info.getName());
            
            // 设置流量统计
            String traffic = String.format("↓%.2fMB ↑%.2fMB", 
                    info.getInputMb(), info.getOutputMb());
            trafficStats.setText(traffic);
            
            // 设置状态
            boolean hasActiveInterface = false;
            DeviceInfo.NetInterface ipv4Interface = null;
            DeviceInfo.NetInterface ipv6Interface = null;
            
            if (info.getInterfaces() != null && !info.getInterfaces().isEmpty()) {
                for (DeviceInfo.NetInterface netInterface : info.getInterfaces()) {
                    if ("IPv4".equals(netInterface.getFamily())) {
                        ipv4Interface = netInterface;
                        hasActiveInterface = true;
                    } else if ("IPv6".equals(netInterface.getFamily())) {
                        ipv6Interface = netInterface;
                    }
                }
            }
            
            interfaceStatus.setText(hasActiveInterface ? "活跃" : "未连接");
            interfaceStatus.setTextColor(hasActiveInterface ? 
                    itemView.getContext().getColor(R.color.status_online) :
                    itemView.getContext().getColor(R.color.text_hint));
            
            // 设置详细信息
            setupDetails(ipv4Interface, ipv6Interface);
        }
        
        private void setInterfaceTypeAndIcon(String name) {
            String type;
            int iconRes = R.drawable.ic_network_white;
            
            String nameLower = name.toLowerCase();
            if (nameLower.contains("wlan") || nameLower.contains("wifi") || 
                nameLower.contains("wireless")) {
                type = "无线网络";
                iconRes = R.drawable.ic_network_white;
            } else if (nameLower.contains("eth") || nameLower.contains("enp")) {
                type = "有线网络";
                iconRes = R.drawable.ic_network_white;
            } else if (nameLower.contains("docker") || nameLower.contains("br-")) {
                type = "虚拟网络";
                iconRes = R.drawable.ic_network_white;
            } else if (nameLower.contains("wg")) {
                type = "VPN隧道";
                iconRes = R.drawable.ic_network_white;
            } else if (nameLower.contains("本地连接")) {
                type = "本地连接";
                iconRes = R.drawable.ic_network_white;
            } else {
                type = "网络接口";
                iconRes = R.drawable.ic_network_white;
            }
            
            interfaceType.setText(type);
            interfaceIcon.setImageResource(iconRes);
        }
        
        private void setupDetails(DeviceInfo.NetInterface ipv4, DeviceInfo.NetInterface ipv6) {
            // 设置IPv4信息
            if (ipv4 != null) {
                ipv4Container.setVisibility(View.VISIBLE);
                ipv4Address.setText(ipv4.getAddress() != null ? ipv4.getAddress() : "未配置");
                ipv4Netmask.setText(ipv4.getNetmask() != null ? ipv4.getNetmask() : "未知");
                macAddress.setText(ipv4.getMac() != null && !ipv4.getMac().isEmpty() ? 
                        ipv4.getMac() : "未知");
            } else {
                ipv4Container.setVisibility(View.GONE);
            }
            
            // 设置IPv6信息
            if (ipv6 != null && ipv6.getAddress() != null && !ipv6.getAddress().isEmpty()) {
                ipv6Container.setVisibility(View.VISIBLE);
                ipv6Address.setText(ipv6.getAddress());
            } else {
                ipv6Container.setVisibility(View.GONE);
            }
        }
        
        private void toggleExpansion() {
            isExpanded = !isExpanded;
            
            // 动画切换详细信息显示
            if (isExpanded) {
                detailsContainer.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(expandButton, "rotation", 0f, 180f).start();
            } else {
                detailsContainer.setVisibility(View.GONE);
                ObjectAnimator.ofFloat(expandButton, "rotation", 180f, 0f).start();
            }
        }
    }
}