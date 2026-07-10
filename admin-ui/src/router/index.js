import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/package-plan',
    children: [
      {
        path: 'package-plan',
        name: 'PackagePlanList',
        component: () => import('@/views/PackagePlan.vue'),
        meta: { title: '套餐管理' }
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/RechargeOrder.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'recharge-consumption',
        name: 'RechargeConsumption',
        component: () => import('@/views/RechargeConsumption.vue'),
        meta: { title: '预存款消耗明细' }
      }

    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
