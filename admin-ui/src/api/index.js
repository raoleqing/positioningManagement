import request from '@/utils/request'

// ============ 套餐管理 ============

/** 分页查询套餐列表 */
export function getPackagePlanList(params) {
  return request({ url: '/package-plan/list', method: 'get', params })
}

/** 查询所有启用的套餐 */
export function getEnabledPackagePlans() {
  return request({ url: '/package-plan/enabled', method: 'get' })
}

/** 新增套餐 */
export function createPackagePlan(data, operator) {
  return request({ url: '/package-plan', method: 'post', params: { operator }, data })
}

/** 修改套餐 */
export function updatePackagePlan(id, data, operator) {
  return request({ url: `/package-plan/${id}`, method: 'put', params: { operator }, data })
}

/** 删除套餐 */
export function deletePackagePlan(id, operator) {
  return request({ url: `/package-plan/${id}`, method: 'delete', params: { operator } })
}

/** 修改套餐状态 */
export function updatePackagePlanStatus(id, status, operator) {
  return request({ url: `/package-plan/${id}/status`, method: 'put', params: { status, operator } })
}

/** 查看套餐操作日志 */
export function getPackagePlanLogs(planId) {
  return request({ url: `/package-plan/${planId}/logs`, method: 'get' })
}

// ============ 订单管理 ============

/** 分页查询订单 */
export function getOrderList(params) {
  return request({ url: '/order/list', method: 'get', params })
}

/** 查看订单日志 */
export function getOrderLogs(orderId) {
  return request({ url: `/order/${orderId}/logs`, method: 'get' })
}

/** 手动重试单个订单 */
export function retryOrder(orderId, operator) {
  return request({ url: `/order/${orderId}/retry`, method: 'post', params: { operator } })
}

/** 一键重试所有失败订单 */
export function retryAllOrders(operator) {
  return request({ url: '/order/retry-all', method: 'post', params: { operator } })
}


