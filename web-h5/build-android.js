import { readFileSync, writeFileSync } from 'fs';
import { execSync } from 'child_process';

console.log('🚀 开始为Android WebView构建项目...');

// 输出目录路径
const outputPath = '../starsnode/android-h5/app/src/main/assets';

// 1. 执行标准构建
console.log('📦 执行Vite构建...');
execSync('npm run build', { stdio: 'inherit' });

// 2. 修改HTML文件，移除type="module"和crossorigin属性，并添加VConsole支持
console.log('🔧 修改HTML文件以兼容Android WebView...');
const htmlPath = `${outputPath}/index.html`;
let html = readFileSync(htmlPath, 'utf8');

// 移除type="module"和crossorigin属性
html = html.replace(/<script[^>]*type="module"[^>]*src="([^"]+)"[^>]*>/g, '<script src="$1">');
html = html.replace(/<link[^>]*crossorigin[^>]*href="([^"]+)"[^>]*>/g, '<link rel="stylesheet" href="$1">');
html = html.replace(/<script[^>]*crossorigin[^>]*src="([^"]+)"[^>]*>/g, '<script src="$1">');

// 更新标题和添加VConsole支持
html = html.replace('<title>Vite App</title>', '<title>设备监控平台</title>');
html = html.replace('</head>', `    <style>
      /* VConsole启动提示样式 */
      .vconsole-tips {
        position: fixed;
        top: 10px;
        right: 10px;
        background: rgba(0, 0, 0, 0.7);
        color: white;
        padding: 5px 10px;
        border-radius: 3px;
        font-size: 12px;
        z-index: 9999;
        opacity: 0;
        animation: fadeInOut 3s ease-in-out;
      }
      @keyframes fadeInOut {
        0% { opacity: 0; }
        20% { opacity: 1; }
        80% { opacity: 1; }
        100% { opacity: 0; }
      }
    </style>
  </head>`);

// 添加VConsole初始化脚本
html = html.replace('</body>', `    <script>
      // VConsole初始化提示
      setTimeout(function() {
        if (typeof window.vconsole !== 'undefined') {
          const tip = document.createElement('div');
          tip.className = 'vconsole-tips';
          tip.textContent = '🔧 VConsole已就绪，点击右下角按钮查看调试信息';
          document.body.appendChild(tip);

          setTimeout(() => {
            if (tip.parentNode) {
              tip.parentNode.removeChild(tip);
            }
          }, 3000);
        }
      }, 1000);
    </script>
  </body>`);

writeFileSync(htmlPath, html);

console.log('✅ 构建完成！');
console.log(`📁 文件已直接输出到 Android 项目: ${outputPath}`);
console.log('🎯 现在可以直接运行Android应用，无需手动复制文件');