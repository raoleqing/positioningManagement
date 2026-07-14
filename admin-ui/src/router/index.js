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
      },
      {
        path: 'reconciliation',
        name: 'Reconciliation',
        component: () => import('@/views/Reconciliation.vue'),
        meta: { title: '对账管理' }
      },
      {
        path: 'task-management',
        name: 'TaskManagement',
        component: () => import('@/views/TaskManagement.vue'),
        meta: { title: '定时任务管理' }
      },
      {
        path: 'alert-config',
        name: 'AlertConfig',
        component: () => import('@/views/AlertConfig.vue'),
        meta: { title: '告警管理' }
      },
      {
        path: 'operation-log',
        name: 'OperationLog',
        component: () => import('@/views/OperationLog.vue'),
        meta: { title: '日志管理' }
      }

    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
