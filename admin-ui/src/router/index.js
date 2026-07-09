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
      }

    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
