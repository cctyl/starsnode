package io.github.cctyl.starnode.model;

import java.util.List;

public class NetworkInterfaceInfo {
    private String name;
    private List<DeviceInfo.NetInterface> interfaces;
    private double inputMb;
    private double outputMb;
    private boolean isExpanded;

    public NetworkInterfaceInfo() {
        this.inputMb = 0.0;
        this.outputMb = 0.0;
        this.isExpanded = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DeviceInfo.NetInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<DeviceInfo.NetInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public double getInputMb() {
        return inputMb;
    }

    public void setInputMb(double inputMb) {
        this.inputMb = inputMb;
    }

    public double getOutputMb() {
        return outputMb;
    }

    public void setOutputMb(double outputMb) {
        this.outputMb = outputMb;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    /**
     * 获取主要的IPv4地址
     */
    public String getPrimaryIPv4() {
        if (interfaces != null) {
            for (DeviceInfo.NetInterface netInterface : interfaces) {
                if ("IPv4".equals(netInterface.getFamily()) && 
                    netInterface.getAddress() != null && 
                    !netInterface.getAddress().isEmpty()) {
                    return netInterface.getAddress();
                }
            }
        }
        return null;
    }

    /**
     * 获取主要的IPv6地址
     */
    public String getPrimaryIPv6() {
        if (interfaces != null) {
            for (DeviceInfo.NetInterface netInterface : interfaces) {
                if ("IPv6".equals(netInterface.getFamily()) && 
                    netInterface.getAddress() != null && 
                    !netInterface.getAddress().isEmpty()) {
                    return netInterface.getAddress();
                }
            }
        }
        return null;
    }

    /**
     * 获取MAC地址
     */
    public String getMacAddress() {
        if (interfaces != null) {
            for (DeviceInfo.NetInterface netInterface : interfaces) {
                if (netInterface.getMac() != null && 
                    !netInterface.getMac().isEmpty() && 
                    !"00:00:00:00:00:00".equals(netInterface.getMac())) {
                    return netInterface.getMac();
                }
            }
        }
        return null;
    }

    /**
     * 判断接口是否活跃（有有效的IP地址）
     */
    public boolean isActive() {
        return getPrimaryIPv4() != null || getPrimaryIPv6() != null;
    }

    /**
     * 判断是否为内部接口
     */
    public boolean isInternal() {
        if (interfaces != null && !interfaces.isEmpty()) {
            // 如果所有接口都是内部的，则认为是内部接口
            return interfaces.stream().allMatch(DeviceInfo.NetInterface::isInternal);
        }
        return true;
    }

    /**
     * 获取网络类型描述
     */
    public String getNetworkType() {
        String nameLower = name.toLowerCase();
        
        if (nameLower.contains("wlan") || nameLower.contains("wifi") || 
            nameLower.contains("wireless")) {
            return "无线网络";
        } else if (nameLower.contains("eth") || nameLower.contains("enp")) {
            return "有线网络";
        } else if (nameLower.contains("docker") || nameLower.contains("br-")) {
            return "虚拟网络";
        } else if (nameLower.contains("wg")) {
            return "VPN隧道";
        } else if (nameLower.contains("本地连接")) {
            return "本地连接";
        } else if (nameLower.contains("lo") || nameLower.contains("loopback")) {
            return "回环接口";
        } else {
            return "网络接口";
        }
    }

    /**
     * 获取流量统计文本
     */
    public String getTrafficText() {
        if (inputMb == 0 && outputMb == 0) {
            return "无流量";
        }
        return String.format("↓%.2fMB ↑%.2fMB", inputMb, outputMb);
    }
}