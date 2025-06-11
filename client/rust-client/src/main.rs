#![windows_subsystem = "windows"]

use clap::Parser;
use std::error::Error;
use std::fs::{self, OpenOptions};
use std::io::Write;
use tungstenite::{Message, connect};

#[derive(Parser)]
#[command(
    version,
    about = "star node",
    long_about = "star node 轻量级、跨平台的系统监控软件"
)]
struct Cli {
    #[arg(short, long)]
    config: Option<String>,
}

fn main() -> Result<(), Box<dyn Error>> {
    let cliparse = Cli::parse();

    let mut path: String = "config.json".to_string();
    match cliparse.config {
        Some(s) => {
            println!("指定的配置文件路径是：{path}");
            path = s
        }
        None => {
            println!("未指定配置文件路径，使用默认路径 ./config.json")
        }
    }

 
    starnode::services::run(path)?;

    Ok(())
}
