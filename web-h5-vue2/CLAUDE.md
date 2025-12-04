# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Vue 2 的 H5 Web 项目，名为 "starnode-h5"。项目使用 Vue CLI 5 构建工具进行开发和打包。

## 常用命令

### 开发服务器
```bash
npm run serve
```
启动开发服务器，支持热重载。

### 生产构建
```bash
npm run build
```
编译并压缩代码用于生产环境。

### 代码检查
```bash
npm run lint
```
运行 ESLint 检查并修复代码风格问题。

### 依赖安装
```bash
npm install
```
安装项目所需的所有依赖包。

## 项目结构

```
├── public/                 # 静态资源目录
│   ├── index.html         # HTML 模板
│   └── favicon.ico        # 网站图标
├── src/                   # 源代码目录
│   ├── assets/           # 静态资源文件
│   ├── components/       # Vue 组件
│   ├── App.vue          # 根组件
│   └── main.js          # 应用入口文件
├── babel.config.js       # Babel 配置
├── vue.config.js         # Vue CLI 配置
└── package.json          # 项目配置和依赖
```

## 技术栈

- **前端框架**: Vue 2.6.14
- **构建工具**: Vue CLI 5.0.0
- **代码检查**: ESLint + Vue 插件
- **JavaScript 编译**: Babel
- **CSS 预处理**: 无（原生 CSS）

## 开发配置

### ESLint 配置
- 使用 Vue Essential 规则集
- 集成 ESLint 推荐规则
- 使用 Babel 解析器支持现代 JavaScript 语法

### Babel 配置
项目配置了 Babel 用于转译现代 JavaScript 语法，确保浏览器兼容性。

### Vue CLI 配置
当前的 Vue CLI 配置较为简洁，启用了 `transpileDependencies` 选项来转译依赖包。

## 浏览器支持

项目支持以下浏览器：
- > 1% 市场份额的浏览器
- 最新 2 个版本
- 不支持已停止维护的浏览器