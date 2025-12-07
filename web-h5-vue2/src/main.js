import Vue from 'vue'
import App from './App.vue'
import router from './router'
import './assets/monitor.css'
import VConsole from 'vconsole';



const vConsole = new VConsole();

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
