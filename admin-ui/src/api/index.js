import request from '@/utils/request'

// ============ 套餐管理 ============

/** 分页查询套餐列表 */
export function getPackagePlanList(params) {
  return request({ url: '/admin/package-plan/list', method: 'get', params })
}

/** 查询所有启用的套餐 */
export function getEnabledPackagePlans() {
  return request({ url: '/admin/package-plan/enabled', method: 'get' })
}

/** 新增套餐 */
export function createPackagePlan(data, operator) {
  return request({ url: '/admin/package-plan', method: 'post', params: { operator }, data })
}

/** 修改套餐 */
export function updatePackagePlan(id, data, operator) {
  return request({ url: `/admin/package-plan/${id}`, method: 'put', params: { operator }, data })
}

/** 删除套餐 */
export function deletePackagePlan(id, operator) {
  return request({ url: `/admin/package-plan/${id}`, method: 'delete', params: { operator } })
}

/** 修改套餐状态 */
export function updatePackagePlanStatus(id, status, operator) {
  return request({ url: `/admin/package-plan/${id}/status`, method: 'put', params: { status, operator } })
}

/** 查看套餐操作日志 */
export function getPackagePlanLogs(planId) {
  return request({ url: `/admin/package-plan/${planId}/logs`, method: 'get' })
}

// ============ 订单管理 ============

/** 分页查询订单 */
export function getOrderList(params) {
  return request({ url: '/admin/order/list', method: 'get', params })
}

/** 查看订单日志 */
export function getOrderLogs(orderId) {
  return request({ url: `/admin/order/${orderId}/logs`, method: 'get' })
}

/** 手动重试单个订单 */
export function retryOrder(orderId, operator) {
  return request({ url: `/admin/order/${orderId}/retry`, method: 'post', params: { operator } })
}

/** 一键重试所有失败订单 */
export function retryAllOrders(operator) {
  return request({ url: '/admin/order/retry-all', method: 'post', params: { operator } })
}

// ============ 预存款消耗明细（充值记录） ============

/** 分页查询充值记录 */
export function getRechargeRecordList(params) {
  return request({ url: '/admin/recharge-record/list', method: 'get', params })
}

/** 重试充值 */
export function retryRechargeRecord(id, operator) {
  return request({ url: `/admin/recharge-record/${id}/retry`, method: 'post', params: { operator } })
}

/** 退款 */
export function refundRechargeRecord(id, operator) {
  return request({ url: `/admin/recharge-record/${id}/refund`, method: 'post', params: { operator } })
}

// ============ 对账管理（每日流水） ============

/** 手动生成每日流水 */
export function generateDailyFlow(date) {
  return request({ url: '/admin/daily-flow/generate', method: 'post', params: { date } })
}

/** 查询指定日期的每日流水 */
export function getDailyFlow(date) {
  return request({ url: `/admin/daily-flow/${date}`, method: 'get' })
}

/** 手动触发定时清洗任务 */
export function runDailyFlowTask() {
  return request({ url: '/admin/daily-flow/run-task', method: 'post' })
}

// ============ 告警管理 ============

/** 获取所有告警配置 */
export function getAlertConfigList() {
  return request({ url: '/admin/alert-config', method: 'get' })
}

/** 保存/更新告警配置 */
export function saveAlertConfig(data) {
  return request({ url: '/admin/alert-config', method: 'post', data })
}

/** 根据类型查询告警配置 */
export function getAlertConfigByType(alertType) {
  return request({ url: `/admin/alert-config/${alertType}`, method: 'get' })
}

// ============ 操作日志管理 ============

/** 分页查询操作日志 */
export function getOperationLogList(params) {
  return request({ url: '/admin/operation-log/list', method: 'get', params })
}

/** 根据订单号追踪完整链路 */
export function traceOperationLog(businessNo) {
  return request({ url: `/admin/operation-log/trace/${businessNo}`, method: 'get' })
}

// ============ 定时任务手动触发 ============

/** 手动触发告警检查任务 */
export function triggerAlertTask() {
  return request({ url: '/admin/task/alert/trigger', method: 'post' })
}

/** 手动触发日志清理任务 */
export function triggerLogCleanupTask() {
  return request({ url: '/admin/task/log-cleanup/trigger', method: 'post' })
}
