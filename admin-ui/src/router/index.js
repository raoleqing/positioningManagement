import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/order',
    children: [
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/RechargeOrder.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'validity',
        name: 'DeviceValidity',
        component: () => import('@/views/DeviceValidity.vue'),
        meta: { title: '设备有效期管理' }
      },
      {
        path: 'predeposit',
        name: 'PreDeposit',
        component: () => import('@/views/PreDeposit.vue'),
        meta: { title: '预存款管理' }
      },
      {
        path: 'reconciliation',
        name: 'Reconciliation',
        component: () => import('@/views/Reconciliation.vue'),
        meta: { title: '对账管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
