import Vue from 'vue';
import VueRouter from 'vue-router';
import Monitor from '../views/Monitor.vue';

Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    name: 'Monitor',
    component: Monitor
  },
  {
    path: '*',
    redirect: '/'
  }
];

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
});

export default router;