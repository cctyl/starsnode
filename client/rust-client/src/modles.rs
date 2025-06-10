use serde::{Deserialize, Serialize};
use std::{collections::HashMap};

#[derive(Debug, Serialize, Deserialize)]
pub struct Config {
   pub server: String,
   pub port: u16,
   pub token: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CpuInfo {
    pub cpu_count: u16,
    pub cpu_model: String,
    pub cpu_usage: f32,
}
#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DriveInfo {
    pub free_gb: f32,
    pub free_percentage: f32,
    pub total_gb: f32,
    pub used_gb: f32,
    pub used_percentage: f32,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MemInfo {
    pub free_mem_mb: f32,
    pub free_mem_percentage: f32,
    pub total_mem_mb: f32,
    pub used_mem_mb: f32,
    pub used_mem_percentage: f32,
}
#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct OsInfo {
    pub arch: String,
    pub hostname: Option<String>,
    pub platform: String,
    pub release: Option<String>,
    pub r#type: String, // 使用 r# 前缀因为 type 是保留字
    pub uptime: u64,
}
#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct NetstatInfo {
    pub input_mb: f32,
    pub output_mb: f32,
}

#[derive(Debug, Default,Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct NetworkInterfaceInfo {
    pub address: String,
    pub boardcast: String,
    pub family:String,
    pub mac: Option<String>,
    pub netmask: String,
}
#[derive(Debug,Default,Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DevInfo<> {
    pub cpu_info: Option<CpuInfo>,
    pub drive_info:  Option<DriveInfo>,
    pub mem_info: Option< MemInfo>,
    pub net_interface:  Option<HashMap<String, Vec<NetworkInterfaceInfo>>>,
    pub netstat_info:  Option<HashMap<String, NetstatInfo>>,
    pub opened_count: u16,
    pub os_info:  Option<OsInfo>,
}
