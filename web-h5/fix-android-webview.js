#!/usr/bin/env node

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

/**
 * 修复Android WebView兼容性的构建后脚本
 * 自动移除HTML中的type="module"和crossorigin属性
 */

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const androidAssetsPath = path.join(__dirname, '../android-h5/app/src/main/assets');
const indexHtmlPath = path.join(androidAssetsPath, 'index.html');

console.log('🔧 正在修复Android WebView兼容性...');

try {
  // 检查文件是否存在
  if (!fs.existsSync(indexHtmlPath)) {
    console.error('❌ 找不到index.html文件:', indexHtmlPath);
    process.exit(1);
  }

  // 读取HTML文件
  let htmlContent = fs.readFileSync(indexHtmlPath, 'utf8');
  console.log('📄 已读取index.html文件');

  // 记录修改前的状态
  const originalHtml = htmlContent;
  let hasChanges = false;

  // 修复script标签 - 移除type="module"和crossorigin属性，但保留src
  if (htmlContent.includes('type="module"')) {
    htmlContent = htmlContent.replace(/<script([^>]*?)type=["']module["']([^>]*?)>/g, '<script$1$2>');
    console.log('✅ 已移除type="module"属性');
    hasChanges = true;
  }

  // 移除crossorigin属性
  if (htmlContent.includes('crossorigin')) {
    htmlContent = htmlContent.replace(/\s+crossorigin(=["'][^"']*["'])?/g, '');
    console.log('✅ 已移除crossorigin属性');
    hasChanges = true;
  }

  // 更新HTML语言和标题
  if (htmlContent.includes('html lang=""')) {
    htmlContent = htmlContent.replace('html lang=""', 'html lang="zh-CN"');
    console.log('✅ 已更新HTML语言设置');
    hasChanges = true;
  }

  if (htmlContent.includes('<title>Vite App</title>')) {
    htmlContent = htmlContent.replace('<title>Vite App</title>', '<title>设备监控平台</title>');
    console.log('✅ 已更新页面标题');
    hasChanges = true;
  }

  // 如果有修改，则写入文件
  if (hasChanges) {
    fs.writeFileSync(indexHtmlPath, htmlContent, 'utf8');
    console.log('✅ 修复完成，已保存到:', indexHtmlPath);

    // 显示修改差异
    console.log('\n📝 修改内容:');
    console.log('- 移除了type="module"属性（解决MIME类型错误）');
    console.log('- 移除了crossorigin属性');
    console.log('- 设置了正确的语言和标题');
  } else {
    console.log('✅ HTML文件已经是正确的格式，无需修改');
  }

  // 验证JavaScript文件是否存在
  const jsPath = path.join(androidAssetsPath, 'assets/index.js');
  if (fs.existsSync(jsPath)) {
    const jsStats = fs.statSync(jsPath);
    console.log(`✅ JavaScript文件已就绪: ${jsStats.size} bytes`);
  } else {
    console.warn('⚠️ JavaScript文件不存在:', jsPath);
  }

  console.log('🎉 Android WebView兼容性修复完成！');

} catch (error) {
  console.error('❌ 修复过程中出现错误:', error.message);
  process.exit(1);
}
