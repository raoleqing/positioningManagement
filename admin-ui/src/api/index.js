import request from '@/utils/request'

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

// ============ 设备有效期 ============

/** 获取设备有效期 */
export function getDeviceValidity(deviceId) {
  return request({ url: `/order/validity/${deviceId}`, method: 'get' })
}

/** 手动修正有效期 */
export function updateValidity(data, operator) {
  return request({ url: '/order/validity/update', method: 'post', params: { operator }, data })
}

/** 查看有效期修正日志 */
export function getValidityUpdateLogs(deviceId) {
  return request({ url: `/order/validity/${deviceId}/logs`, method: 'get' })
}

// ============ 预存款管理 ============

/** 获取预存款账户 */
export function getPredepositAccount() {
  return request({ url: '/predeposit/account', method: 'get' })
}

/** 获取预存款消耗明细 */
export function getPredepositRecords(params) {
  return request({ url: '/predeposit/records', method: 'get', params })
}

/** 获取告警配置 */
export function getAlertConfig(accountId) {
  return request({ url: `/predeposit/alert-config/${accountId}`, method: 'get' })
}

/** 更新告警配置 */
export function updateAlertConfig(data) {
  return request({ url: '/predeposit/alert-config', method: 'put', data })
}

// ============ 对账管理 ============

/** 生成对账报告 */
export function generateReconciliation(data, operator) {
  return request({ url: '/reconciliation/generate', method: 'post', params: { operator }, data })
}

/** 重新生成对账报告 */
export function regenerateReconciliation(reportDate, operator) {
  return request({ url: '/reconciliation/reports/regenerate', method: 'post', params: { reportDate, operator } })
}

/** 查询对账报告列表 */
export function getReconciliationReports(params) {
  return request({ url: '/reconciliation/reports', method: 'get', params })
}

/** 查看对账差异明细 */
export function getReconciliationDetails(reportId, params) {
  return request({ url: `/reconciliation/reports/${reportId}/details`, method: 'get', params })
}

/** 标记差异处理状态 */
export function updateDetailStatus(detailId, params) {
  return request({ url: `/reconciliation/details/${detailId}/status`, method: 'put', params })
}

/** 标记报告已处理 */
export function resolveReport(reportId, operator) {
  return request({ url: `/reconciliation/reports/${reportId}/resolve`, method: 'put', params: { operator } })
}
