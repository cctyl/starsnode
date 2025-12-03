// WebSocket连接管理
let ws = null;
let reconnectTimer = null;
let reconnectDelay = 3000; // 3秒后重连
const WS_URL = "ws://10.0.8.1:6080/?token=abcdef&type=view&endpointName=web-access";

// 更新时间戳
function updateTimestamp() {
    const updateTimeElement = document.getElementById('updateTime');
    if (!updateTimeElement) return;

    const now = new Date();
    const timeString = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    updateTimeElement.textContent = timeString;
}

// 更新连接状态
function updateConnectionStatus(status, message) {
    const statusElement = document.querySelector('.update-time');
    if (!statusElement) return;

    const statusIcons = {
        connecting: '🔄',
        connected: '✅',
        disconnected: '❌',
        error: '⚠️'
    };

    const icon = statusIcons[status] || '📡';
    statusElement.innerHTML = `${icon} ${message}`;
}

// 连接WebSocket
function connectWebSocket() {
    try {
        updateConnectionStatus('connecting', '正在连接...');

        ws = new WebSocket(WS_URL);

        ws.onopen = function (event) {
            console.log('WebSocket连接已建立');
            updateConnectionStatus('connected', '已连接');

            // 清除重连定时器
            if (reconnectTimer) {
                clearTimeout(reconnectTimer);
                reconnectTimer = null;
            }
        };

        ws.onmessage = function (event) {
            try {
                const data = JSON.parse(event.data);
                console.log('收到数据:', data);

                // 更新时间戳
                updateTimestamp();

                // 检查数据格式
                if (Array.isArray(data)) {
                    // 数据是设备数组
                    processDevicesData(data);
                } else {
                    console.warn('数据格式不正确，期望数组格式');
                }
            } catch (error) {
                console.error('数据解析失败:', error);
                updateConnectionStatus('error', '数据解析错误');
            }
        };

        ws.onerror = function (error) {
            console.error('WebSocket错误:', error);
            updateConnectionStatus('error', '连接错误');
        };

        ws.onclose = function (event) {
            console.log('WebSocket连接已关闭');
            updateConnectionStatus('disconnected', '连接已断开，准备重连...');

            // 自动重连
            reconnectTimer = setTimeout(() => {
                console.log('尝试重新连接...');
                connectWebSocket();
            }, reconnectDelay);
        };

    } catch (error) {
        console.error('WebSocket连接失败:', error);
        updateConnectionStatus('error', '连接失败');

        // 重连
        reconnectTimer = setTimeout(() => {
            connectWebSocket();
        }, reconnectDelay);
    }
}

// 处理接收到的设备数据
function processDevicesData(devices) {
    if (devices.length === 0) {
        const devicesList = document.getElementById('devicesList');
        if (devicesList) {
            devicesList.innerHTML = '<div class="loading"><div>📭 暂无设备数据</div></div>';
        }
        return;
    }

    // 计算并显示汇总数据
    const summary = calculateSummary(devices);
    updateSummaryDisplay(summary);

    // 创建或更新图表
    createMemoryChart(summary);
    createDiskChart(summary);

    // 渲染设备列表
    renderDevices(devices);
}

// 计算汇总数据
function calculateSummary(devices) {
    const summary = {
        total: devices.length,
        windows: 0,
        linux: 0,
        other: 0,
        totalCpuCores: 0,
        totalCpuUsage: 0,
        totalMemory: 0,
        usedMemory: 0,
        totalDisk: 0,
        usedDisk: 0
    };

    devices.forEach(device => {
        // 统计操作系统
        const platform = device.osInfo?.platform?.toLowerCase() || '';
        if (platform.includes('windows')) {
            summary.windows++;
        } else if (platform.includes('linux')) {
            summary.linux++;
        } else {
            summary.other++;
        }

        // CPU统计
        summary.totalCpuCores += device.cpuInfo?.cpuCount || 0;
        summary.totalCpuUsage += device.cpuInfo?.cpuUsage || 0;

        // 内存统计
        const totalMem = device.memInfo?.totalMemMb || 0;
        const usedMem = device.memInfo?.usedMemMb || 0;
        summary.totalMemory += totalMem;
        summary.usedMemory += usedMem;

        // 磁盘统计
        let totalDisk = device.driveInfo?.totalGb || 0;
        let usedDisk = device.driveInfo?.usedGb || 0;

        // 处理可能是字符串的情况
        if (typeof totalDisk === 'string') totalDisk = parseFloat(totalDisk) || 0;
        if (typeof usedDisk === 'string') usedDisk = parseFloat(usedDisk) || 0;

        summary.totalDisk += totalDisk;
        summary.usedDisk += usedDisk;
    });

    // 计算平均CPU使用率
    if (summary.total > 0) {
        summary.avgCpuUsage = (summary.totalCpuUsage / summary.total).toFixed(2);
    }

    return summary;
}

// 更新汇总显示
function updateSummaryDisplay(summary) {
    const elements = {
        totalDevices: document.getElementById('totalDevices'),
        windowsDevices: document.getElementById('windowsDevices'),
        linuxDevices: document.getElementById('linuxDevices'),
        otherDevices: document.getElementById('otherDevices'),
        totalCpuCores: document.getElementById('totalCpuCores'),
        avgCpuUsage: document.getElementById('avgCpuUsage'),
        totalMemory: document.getElementById('totalMemory'),
        totalDisk: document.getElementById('totalDisk')
    };

    if (elements.totalDevices) elements.totalDevices.textContent = summary.total;
    if (elements.windowsDevices) elements.windowsDevices.textContent = summary.windows;
    if (elements.linuxDevices) elements.linuxDevices.textContent = summary.linux;
    if (elements.otherDevices) elements.otherDevices.textContent = summary.other;

    if (elements.totalCpuCores) elements.totalCpuCores.textContent = `${summary.totalCpuCores} 核心`;
    if (elements.avgCpuUsage) elements.avgCpuUsage.textContent = `${summary.avgCpuUsage}%`;

    if (elements.totalMemory) elements.totalMemory.textContent = `${(summary.totalMemory / 1024).toFixed(2)} GB`;
    if (elements.totalDisk) elements.totalDisk.textContent = `${summary.totalDisk.toFixed(2)} GB`;
}

// 图表实例缓存
let memoryChartInstance = null;
let diskChartInstance = null;

// 创建内存图表
function createMemoryChart(summary) {
    const ctx = document.getElementById('memoryChart');
    if (!ctx) return;

    const usedMem = summary.usedMemory / 1024;
    const freeMem = (summary.totalMemory - summary.usedMemory) / 1024;

    // 如果图表已存在，更新数据
    if (memoryChartInstance) {
        memoryChartInstance.data.datasets[0].data = [usedMem, freeMem];
        memoryChartInstance.update();
        return;
    }

    // 创建新图表
    memoryChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['已使用', '空闲'],
            datasets: [{
                data: [usedMem, freeMem],
                backgroundColor: [
                    'rgba(102, 126, 234, 0.8)',
                    'rgba(72, 187, 120, 0.8)'
                ],
                borderColor: [
                    'rgba(102, 126, 234, 1)',
                    'rgba(72, 187, 120, 1)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#cbd5e1',
                        font: { size: 11 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.label + ': ' + context.parsed.toFixed(2) + ' GB';
                        }
                    }
                }
            }
        }
    });
}

// 创建磁盘图表
function createDiskChart(summary) {
    const ctx = document.getElementById('diskChart');
    if (!ctx) return;

    // 如果图表已存在，更新数据
    if (diskChartInstance) {
        diskChartInstance.data.datasets[0].data = [summary.usedDisk, summary.totalDisk - summary.usedDisk];
        diskChartInstance.update();
        return;
    }

    // 创建新图表
    diskChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['已使用', '空闲'],
            datasets: [{
                data: [summary.usedDisk, summary.totalDisk - summary.usedDisk],
                backgroundColor: [
                    'rgba(245, 101, 101, 0.8)',
                    'rgba(66, 153, 225, 0.8)'
                ],
                borderColor: [
                    'rgba(245, 101, 101, 1)',
                    'rgba(66, 153, 225, 1)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#cbd5e1',
                        font: { size: 11 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.label + ': ' + context.parsed.toFixed(2) + ' GB';
                        }
                    }
                }
            }
        }
    });
}

// 获取操作系统标识
function getOSBadge(platform) {
    const p = platform?.toLowerCase() || '';
    if (p.includes('windows')) {
        return '<span class="device-os-badge os-windows">🪟 Windows</span>';
    } else if (p.includes('linux')) {
        return '<span class="device-os-badge os-linux">🐧 Linux</span>';
    } else {
        return '<span class="device-os-badge os-other">💻 其他</span>';
    }
}

// 获取进度条类别
function getProgressClass(percentage) {
    if (percentage >= 80) return 'high';
    if (percentage >= 50) return 'medium';
    return '';
}

// 格式化网络速度
function formatSpeed(mb) {
    if (mb === 0) return '0 MB/s';
    if (mb < 0.01) return '<0.01 MB/s';
    return `${mb.toFixed(2)} MB/s`;
}

// 格式化运行时间
function formatUptime(seconds) {
    const days = Math.floor(seconds / 86400);
    const hours = Math.floor((seconds % 86400) / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (days > 0) {
        return `${days}天 ${hours}小时`;
    } else if (hours > 0) {
        return `${hours}小时 ${minutes}分钟`;
    } else {
        return `${minutes}分钟`;
    }
}

// 渲染设备卡片
function renderDevices(devices) {
    const container = document.getElementById('devicesList');
    if (!container) return;

    container.innerHTML = '';

    devices.forEach((device, index) => {
        const osInfo = device.osInfo || {};
        const cpuInfo = device.cpuInfo || {};
        const memInfo = device.memInfo || {};
        const driveInfo = device.driveInfo || {};
        const netstatInfo = device.netstatInfo || {};
        const netInterface = device.netInterface || {};
        const ipInfo = device.ipInfo || null;

        // 处理磁盘数据（可能是字符串）
        let totalDisk = driveInfo.totalGb || 0;
        let usedDisk = driveInfo.usedGb || 0;
        let diskPercentage = driveInfo.usedPercentage || 0;

        if (typeof totalDisk === 'string') totalDisk = parseFloat(totalDisk) || 0;
        if (typeof usedDisk === 'string') usedDisk = parseFloat(usedDisk) || 0;
        if (typeof diskPercentage === 'string') diskPercentage = parseFloat(diskPercentage) || 0;

        // 网络总速度
        const totalSpeed = netstatInfo.total || { inputMb: 0, outputMb: 0 };

        // 网卡列表
        const interfacesList = Object.entries(netInterface).map(([name, addresses]) => {
            const stats = netstatInfo[name] || netstatInfo[name + ':'] || { inputMb: 0, outputMb: 0 };
            const ipv4 = addresses.find(addr => addr.family === 'IPv4');

            return `
                <div class="interface-card">
                    <div class="interface-name">🔌 ${name}</div>
                    ${ipv4 ? `
                        <div class="interface-details">📍 IP: ${ipv4.address}</div>
                        <div class="interface-details">🎭 子网掩码: ${ipv4.netmask}</div>
                        ${ipv4.mac ? `<div class="interface-details">🏷️ MAC: ${ipv4.mac}</div>` : ''}
                    ` : '<div class="interface-details">暂无IPv4地址</div>'}
                    <div class="interface-speed">
                        <span class="speed-stat">↓ 下载: <span class="value">${formatSpeed(stats.inputMb)}</span></span>
                        <span class="speed-stat">↑ 上传: <span class="value">${formatSpeed(stats.outputMb)}</span></span>
                    </div>
                </div>
            `;
        }).join('');

        // IP信息卡片
        const ipInfoCard = ipInfo ? `
            <div class="ip-info">
                <div class="ip-title">🌍 外网IP信息</div>
                <div class="ip-details">
                    <div>📍 IP: ${ipInfo.query || 'N/A'}</div>
                    <div>🏳️ 国家: ${ipInfo.country || 'N/A'}</div>
                    <div>🏙️ 城市: ${ipInfo.city || 'N/A'}</div>
                    <div>🌐 ISP: ${ipInfo.isp || 'N/A'}</div>
                </div>
            </div>
        ` : '';

        const deviceCard = `
            <div class="device-card" style="animation-delay: ${index * 0.1}s">
                <div class="device-header">
                    <div class="device-name">
                        ${getOSBadge(osInfo.platform)}
                        <span>${osInfo.hostname || '未知设备'}</span>
                    </div>
                    <div class="device-status">
                        <span class="status-dot"></span>
                        <span>在线</span>
                    </div>
                </div>

                <div class="device-info-grid">
                    <div class="info-item">
                        <div class="info-label">💻 CPU</div>
                        <div class="info-value">${cpuInfo.cpuModel || 'N/A'}</div>
                        <div class="info-value">${cpuInfo.cpuCount || 0} 核心 | 使用率: ${(cpuInfo.cpuUsage || 0).toFixed(2)}%</div>
                        <div class="progress-bar">
                            <div class="progress-fill ${getProgressClass(cpuInfo.cpuUsage || 0)}" style="width: ${cpuInfo.cpuUsage || 0}%"></div>
                        </div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">🧠 内存</div>
                        <div class="info-value">${((memInfo.totalMemMb || 0) / 1024).toFixed(2)} GB</div>
                        <div class="info-value">已用: ${((memInfo.usedMemMb || 0) / 1024).toFixed(2)} GB | ${(memInfo.usedMemPercentage || 0).toFixed(2)}%</div>
                        <div class="progress-bar">
                            <div class="progress-fill ${getProgressClass(memInfo.usedMemPercentage || 0)}" style="width: ${memInfo.usedMemPercentage || 0}%"></div>
                        </div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">💾 磁盘</div>
                        <div class="info-value">${totalDisk.toFixed(2)} GB</div>
                        <div class="info-value">已用: ${usedDisk.toFixed(2)} GB | ${diskPercentage.toFixed(2)}%</div>
                        <div class="progress-bar">
                            <div class="progress-fill ${getProgressClass(diskPercentage)}" style="width: ${diskPercentage}%"></div>
                        </div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">⏱️ 运行时间</div>
                        <div class="info-value">${formatUptime(osInfo.uptime || 0)}</div>
                        <div class="info-value">架构: ${osInfo.arch || 'N/A'}</div>
                    </div>
                </div>

                <div class="network-speed">
                    <div class="speed-item">
                        <div class="speed-label">↓ 总下载速度</div>
                        <div class="speed-value">${formatSpeed(totalSpeed.inputMb)}</div>
                    </div>
                    <div class="speed-item">
                        <div class="speed-label">↑ 总上传速度</div>
                        <div class="speed-value">${formatSpeed(totalSpeed.outputMb)}</div>
                    </div>
                </div>

                ${ipInfoCard}

                <div class="network-interfaces">
                    <div class="interfaces-title">🔌 网卡信息</div>
                    ${interfacesList}
                </div>
            </div>
        `;

        container.innerHTML += deviceCard;
    });
}

// 初始化应用
async function init() {
    // 显示加载状态
    const devicesList = document.getElementById('devicesList');
    if (devicesList) {
        devicesList.innerHTML = '<div class="loading"><div class="spinner"></div><div>正在建立连接...</div></div>';
    }

    // 连接WebSocket
    connectWebSocket();

    // 定期更新本地时间显示（每秒）
    setInterval(updateTimestamp, 1000);
}

// 页面加载完成后初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}

// 页面卸载时关闭WebSocket连接
window.addEventListener('beforeunload', function () {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close();
    }
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
    }
});
