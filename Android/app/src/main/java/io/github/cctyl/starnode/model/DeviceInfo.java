package io.github.cctyl.starnode.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class DeviceInfo {
    @SerializedName("cpuInfo")
    private CpuInfo cpuInfo;
    
    @SerializedName("memInfo")
    private MemInfo memInfo;
    
    @SerializedName("driveInfo")
    private DriveInfo driveInfo;
    
    @SerializedName("netstatInfo")
    private NetstatInfo netstatInfo;
    
    @SerializedName("osInfo")
    private OsInfo osInfo;
    
    @SerializedName("ipInfo")
    private IpInfo ipInfo;
    
    @SerializedName("netInterface")
    private Map<String, List<NetInterface>> netInterface;
    
    @SerializedName("openedCount")
    private int openedCount;

    // Getters and Setters
    public CpuInfo getCpuInfo() { return cpuInfo; }
    public void setCpuInfo(CpuInfo cpuInfo) { this.cpuInfo = cpuInfo; }
    
    public MemInfo getMemInfo() { return memInfo; }
    public void setMemInfo(MemInfo memInfo) { this.memInfo = memInfo; }
    
    public DriveInfo getDriveInfo() { return driveInfo; }
    public void setDriveInfo(DriveInfo driveInfo) { this.driveInfo = driveInfo; }
    
    public NetstatInfo getNetstatInfo() { return netstatInfo; }
    public void setNetstatInfo(NetstatInfo netstatInfo) { this.netstatInfo = netstatInfo; }
    
    public OsInfo getOsInfo() { return osInfo; }
    public void setOsInfo(OsInfo osInfo) { this.osInfo = osInfo; }
    
    public IpInfo getIpInfo() { return ipInfo; }
    public void setIpInfo(IpInfo ipInfo) { this.ipInfo = ipInfo; }
    
    public Map<String, List<NetInterface>> getNetInterface() { return netInterface; }
    public void setNetInterface(Map<String, List<NetInterface>> netInterface) { this.netInterface = netInterface; }
    
    public int getOpenedCount() { return openedCount; }
    public void setOpenedCount(int openedCount) { this.openedCount = openedCount; }

    public static class CpuInfo {
        @SerializedName("cpuCount")
        private int cpuCount;
        
        @SerializedName("cpuModel")
        private String cpuModel;
        
        @SerializedName("cpuUsage")
        private double cpuUsage;

        public int getCpuCount() { return cpuCount; }
        public void setCpuCount(int cpuCount) { this.cpuCount = cpuCount; }
        
        public String getCpuModel() { return cpuModel; }
        public void setCpuModel(String cpuModel) { this.cpuModel = cpuModel; }
        
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
    }

    public static class MemInfo {
        @SerializedName("freeMemMb")
        private double freeMemMb;
        
        @SerializedName("freeMemPercentage")
        private double freeMemPercentage;
        
        @SerializedName("totalMemMb")
        private double totalMemMb;
        
        @SerializedName("usedMemMb")
        private double usedMemMb;
        
        @SerializedName("usedMemPercentage")
        private double usedMemPercentage;

        public double getFreeMemMb() { return freeMemMb; }
        public void setFreeMemMb(double freeMemMb) { this.freeMemMb = freeMemMb; }
        
        public double getFreeMemPercentage() { return freeMemPercentage; }
        public void setFreeMemPercentage(double freeMemPercentage) { this.freeMemPercentage = freeMemPercentage; }
        
        public double getTotalMemMb() { return totalMemMb; }
        public void setTotalMemMb(double totalMemMb) { this.totalMemMb = totalMemMb; }
        
        public double getUsedMemMb() { return usedMemMb; }
        public void setUsedMemMb(double usedMemMb) { this.usedMemMb = usedMemMb; }
        
        public double getUsedMemPercentage() { return usedMemPercentage; }
        public void setUsedMemPercentage(double usedMemPercentage) { this.usedMemPercentage = usedMemPercentage; }
    }

    public static class DriveInfo {
        @SerializedName("freeGb")
        private double freeGb;
        
        @SerializedName("freePercentage")
        private double freePercentage;
        
        @SerializedName("totalGb")
        private double totalGb;
        
        @SerializedName("usedGb")
        private double usedGb;
        
        @SerializedName("usedPercentage")
        private double usedPercentage;

        public double getFreeGb() { return freeGb; }
        public void setFreeGb(double freeGb) { this.freeGb = freeGb; }
        
        public double getFreePercentage() { return freePercentage; }
        public void setFreePercentage(double freePercentage) { this.freePercentage = freePercentage; }
        
        public double getTotalGb() { return totalGb; }
        public void setTotalGb(double totalGb) { this.totalGb = totalGb; }
        
        public double getUsedGb() { return usedGb; }
        public void setUsedGb(double usedGb) { this.usedGb = usedGb; }
        
        public double getUsedPercentage() { return usedPercentage; }
        public void setUsedPercentage(double usedPercentage) { this.usedPercentage = usedPercentage; }
    }

    public static class NetstatInfo {
        @SerializedName("total")
        private NetStat total;
        
        // 其他网络接口统计信息会作为动态字段
        private Map<String, NetStat> interfaces;

        public NetStat getTotal() { return total; }
        public void setTotal(NetStat total) { this.total = total; }
        
        public Map<String, NetStat> getInterfaces() { return interfaces; }
        public void setInterfaces(Map<String, NetStat> interfaces) { this.interfaces = interfaces; }
    }

    public static class NetStat {
        @SerializedName("inputMb")
        private double inputMb;
        
        @SerializedName("outputMb")
        private double outputMb;

        public double getInputMb() { return inputMb; }
        public void setInputMb(double inputMb) { this.inputMb = inputMb; }
        
        public double getOutputMb() { return outputMb; }
        public void setOutputMb(double outputMb) { this.outputMb = outputMb; }
    }

    public static class OsInfo {
        @SerializedName("arch")
        private String arch;
        
        @SerializedName("hostname")
        private String hostname;
        
        @SerializedName("ip")
        private String ip;
        
        @SerializedName("platform")
        private String platform;
        
        @SerializedName("release")
        private String release;
        
        @SerializedName("type")
        private String type;
        
        @SerializedName("uptime")
        private double uptime;

        public String getArch() { return arch; }
        public void setArch(String arch) { this.arch = arch; }
        
        public String getHostname() { return hostname; }
        public void setHostname(String hostname) { this.hostname = hostname; }
        
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        
        public String getRelease() { return release; }
        public void setRelease(String release) { this.release = release; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public double getUptime() { return uptime; }
        public void setUptime(double uptime) { this.uptime = uptime; }
    }

    public static class IpInfo {
        @SerializedName("status")
        private String status;
        
        @SerializedName("country")
        private String country;
        
        @SerializedName("countryCode")
        private String countryCode;
        
        @SerializedName("region")
        private String region;
        
        @SerializedName("regionName")
        private String regionName;
        
        @SerializedName("city")
        private String city;
        
        @SerializedName("zip")
        private String zip;
        
        @SerializedName("lat")
        private double lat;
        
        @SerializedName("lon")
        private double lon;
        
        @SerializedName("timezone")
        private String timezone;
        
        @SerializedName("isp")
        private String isp;
        
        @SerializedName("org")
        private String org;
        
        @SerializedName("as")
        private String as;
        
        @SerializedName("query")
        private String query;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }
        
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        
        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }
        
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        
        public String getIsp() { return isp; }
        public void setIsp(String isp) { this.isp = isp; }
        
        public String getOrg() { return org; }
        public void setOrg(String org) { this.org = org; }
        
        public String getAs() { return as; }
        public void setAs(String as) { this.as = as; }
        
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }

    public static class NetInterface {
        @SerializedName("address")
        private String address;
        
        @SerializedName("boardcast")
        private String broadcast;
        
        @SerializedName("family")
        private String family;
        
        @SerializedName("internal")
        private boolean internal;
        
        @SerializedName("mac")
        private String mac;
        
        @SerializedName("netmask")
        private String netmask;
        
        @SerializedName("cidr")
        private String cidr;
        
        @SerializedName("scopeid")
        private Integer scopeid;

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getBroadcast() { return broadcast; }
        public void setBroadcast(String broadcast) { this.broadcast = broadcast; }
        
        public String getFamily() { return family; }
        public void setFamily(String family) { this.family = family; }
        
        public boolean isInternal() { return internal; }
        public void setInternal(boolean internal) { this.internal = internal; }
        
        public String getMac() { return mac; }
        public void setMac(String mac) { this.mac = mac; }
        
        public String getNetmask() { return netmask; }
        public void setNetmask(String netmask) { this.netmask = netmask; }
        
        public String getCidr() { return cidr; }
        public void setCidr(String cidr) { this.cidr = cidr; }
        
        public Integer getScopeid() { return scopeid; }
        public void setScopeid(Integer scopeid) { this.scopeid = scopeid; }
    }
}