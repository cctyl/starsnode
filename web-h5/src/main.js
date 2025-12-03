import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'

// 等待DOM完全加载的函数
function waitForDOMReady() {
  return new Promise((resolve) => {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', resolve)
    } else {
      resolve()
    }
  })
}

// 等待#app元素存在的函数
function waitForAppElement(maxRetries = 50, interval = 100) {
  return new Promise((resolve, reject) => {
    let retries = 0

    function checkElement() {
      const appElement = document.getElementById('app')
      if (appElement) {
        resolve(appElement)
      } else if (retries >= maxRetries) {
        reject(new Error(`#app 元素在 ${maxRetries} 次尝试后仍未找到`))
      } else {
        retries++
        console.log(`🔍 等待 #app 元素... (${retries}/${maxRetries})`)
        setTimeout(checkElement, interval)
      }
    }

    checkElement()
  })
}

// 添加页面加载就绪的调试信息
console.log('📄 main.js 开始执行')
console.log('🆔 DOM ready state:', document.readyState)

// 检查Vue是否正确加载
if (typeof createApp !== 'undefined') {
  console.log('✅ Vue 3 createApp 函数已加载')
} else {
  console.error('❌ Vue 3 createApp 函数未加载')
}

// 检查App组件是否正确加载
console.log('📱 App component:', typeof App !== 'undefined' ? '✅ 已加载' : '❌ 未加载')

// 在DOM上添加初始化标记
document.addEventListener('DOMContentLoaded', () => {
  console.log('🎯 DOMContentLoaded 事件触发')
  //dom加载显示弹窗，暂时关闭
  // const initDiv = document.createElement('div')
  // initDiv.id = 'init-debug'
  // initDiv.style.cssText = `
  //   position: fixed;
  //   top: 60px;
  //   left: 10px;
  //   background: rgba(0, 255, 0, 0.8);
  //   color: #000;
  //   padding: 8px;
  //   border-radius: 5px;
  //   font-size: 11px;
  //   z-index: 9998;
  //   border: 1px solid #00ff00;
  // `
  // initDiv.innerHTML = 'DOM已加载，正在初始化Vue...'
  // document.body.appendChild(initDiv)
})

// 引入VConsole调试工具
// import VConsole from 'vconsole'
// const vconsole = new VConsole({
//   defaultPlugins: ['system', 'network', 'element', 'storage'],
//   onReady: () => {
//     console.log('✅ VConsole已初始化')
//
//     // 在VConsole就绪后显示信息，暂时关闭
//     // const vconsoleDiv = document.createElement('div')
//     // vconsoleDiv.style.cssText = `
//     //   position: fixed;
//     //   top: 100px;
//     //   left: 10px;
//     //   background: rgba(0, 100, 255, 0.8);
//     //   color: #fff;
//     //   padding: 8px;
//     //   border-radius: 5px;
//     //   font-size: 11px;
//     //   z-index: 9997;
//     // `
//     // vconsoleDiv.innerHTML = 'VConsole已就绪'
//     // document.body.appendChild(vconsoleDiv)
//     //
//     // setTimeout(() => {
//     //   if (vconsoleDiv.parentNode) {
//     //     vconsoleDiv.parentNode.removeChild(vconsoleDiv)
//     //   }
//     // }, 3000)
//   }
// })

const app = createApp(App)

// 将vconsole挂载到全局，方便其他地方使用
// window.vconsole = vconsole

// 添加全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('❌ Vue 错误:', err, info)
  console.error('❌ 错误详情:', {
    message: err.message,
    stack: err.stack,
    info: info
  })

  // 显示错误信息
  const errorDiv = document.createElement('div')
  errorDiv.style.cssText = `
    position: fixed;
    top: 140px;
    left: 10px;
    background: rgba(255, 0, 0, 0.9);
    color: #fff;
    padding: 10px;
    border-radius: 5px;
    font-size: 12px;
    z-index: 9999;
    max-width: 300px;
  `
  errorDiv.innerHTML = `Vue错误: ${err.message}`
  document.body.appendChild(errorDiv)
}

// 异步挂载应用，确保DOM和#app元素都已就绪
async function mountVueApp() {
  try {
    console.log('🚀 开始异步挂载Vue应用...')

    // 1. 等待DOM完全加载
    await waitForDOMReady()
    console.log('✅ DOM已完全加载')

    // 2. 等待#app元素存在
    let appElement = await waitForAppElement()
    console.log('✅ 找到 #app 元素')

    // 3. 挂载Vue应用
    const vm = app.mount('#app')
    console.log('✅ Vue应用挂载成功')
    console.log('📊 Vue实例:', vm)

    // 移除初始化调试信息
    // setTimeout(() => {
    //   const initDebug = document.getElementById('init-debug')
    //   if (initDebug) {
    //     initDebug.remove()
    //   }
    // }, 2000)

    return vm

  } catch (error) {
    console.error('❌ Vue应用挂载失败:', error)

    // 如果找不到#app元素，创建一个
    if (error.message.includes('#app 元素')) {
      console.log('🔧 自动创建 #app 元素...')
      const newAppElement = document.createElement('div')
      newAppElement.id = 'app'
      newAppElement.style.cssText = `
        min-height: 100vh;
        background: linear-gradient(135deg, #ff6b6b, #4ecdc4);
        padding: 20px;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Oxygen", "Ubuntu", "Cantarell", sans-serif;
      `

      // 添加错误信息显示
      newAppElement.innerHTML = `
        <div style="color: white; text-align: center; padding: 40px;">
          <h2>🔧 正在自动修复...</h2>
          <p>已创建 #app 元素，正在重新挂载Vue应用</p>
        </div>
      `

      document.body.appendChild(newAppElement)

      // 重新尝试挂载
      setTimeout(() => {
        console.log('🔄 使用创建的元素重新挂载...')
        try {
          const vm = app.mount('#app')
          console.log('✅ 重新挂载成功!')
        } catch (retryError) {
          console.error('❌ 重新挂载也失败了:', retryError)
        }
      }, 1000)
    }

    // 显示挂载失败信息
    const failDiv = document.createElement('div')
    failDiv.style.cssText = `
      position: fixed;
      top: 180px;
      left: 10px;
      background: rgba(255, 0, 0, 0.9);
      color: #fff;
      padding: 15px;
      border-radius: 5px;
      font-size: 14px;
      z-index: 9999;
      max-width: 350px;
    `
    failDiv.innerHTML = `
      <div>❌ Vue应用挂载失败</div>
      <div>错误: ${error.message}</div>
    `
    document.body.appendChild(failDiv)

    throw error
  }
}

// 启动应用
mountVueApp()
