<template>
  <div class="page-container">
    <!-- 搜索面板 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="输入订单号" clearable />
        </el-form-item>
        <el-form-item label="设备ID">
          <el-input v-model="queryForm.deviceId" placeholder="输入设备ID" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="queryForm.payStatus" placeholder="全部" clearable style="width:130px">
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="支付失败" value="PAY_FAILED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="充值状态">
          <el-select v-model="queryForm.rechargeStatus" placeholder="全部" clearable style="width:130px">
            <el-option label="待充值" value="PENDING" />
            <el-option label="充值成功" value="SUCCESS" />
            <el-option label="充值失败" value="FAILED" />
            <el-option label="重试中" value="RETRYING" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <span class="toolbar-title">订单列表</span>
        <el-button type="warning" :icon="RefreshRight" @click="retryAll" :loading="retryAllLoading">
          一键重试失败订单
        </el-button>
      </div>

      <!-- 订单表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="deviceId" label="设备ID" width="80" />
        <el-table-column prop="deviceNo" label="设备编号" width="140" show-overflow-tooltip />
        <el-table-column prop="simCardNo" label="SIM卡号" width="140" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="amount" label="金额(元)" width="100" />
        <el-table-column prop="years" label="年限" width="70" />
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="payStatusTag(row.payStatus)" size="small">{{ payStatusText(row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="充值状态" width="100">
          <template #default="{ row }">
            <el-tag :type="rechargeStatusTag(row.rechargeStatus)" size="small">{{ rechargeStatusText(row.rechargeStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通知状态" width="100">
          <template #default="{ row }">
            <el-tag :type="notifyStatusTag(row.notifyStatus)" size="small">{{ notifyStatusText(row.notifyStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="160" show-overflow-tooltip />
        <el-table-column prop="rechargeTime" label="充值时间" width="160" show-overflow-tooltip />
        <el-table-column prop="retryCount" label="重试次数" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewLogs(row)">日志</el-button>
            <el-button
              v-if="row.rechargeStatus === 'FAILED'"
              type="warning"
              link
              @click="retrySingle(row)"
              :loading="row._retrying"
            >重试</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="search"
          @current-change="search"
        />
      </div>
    </el-card>

    <!-- 订单日志对话框 -->
    <el-dialog v-model="logDialogVisible" title="订单操作日志" width="800px" destroy-on-close>
      <el-timeline v-if="logList.length">
        <el-timeline-item
          v-for="log in logList"
          :key="log.id"
          :timestamp="log.createTime"
          :color="log.logLevel === 'ERROR' ? '#F56C6C' : '#409EFF'"
        >
          <div class="log-item">
            <el-tag :type="log.logLevel === 'ERROR' ? 'danger' : ''" size="small">{{ log.title || log.logType }}</el-tag>
            <span style="margin-left: 8px; color: #909399;">[{{ log.operator }}]</span>
          </div>
          <div class="log-content">{{ log.content }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无日志记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getOrderList, getOrderLogs, retryOrder, retryAllOrders } from '@/api'

const queryForm = reactive({
  orderNo: '',
  deviceId: '',
  userId: '',
  payStatus: '',
  rechargeStatus: '',
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10
})
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const retryAllLoading = ref(false)

// 日志弹窗
const logDialogVisible = ref(false)
const logList = ref([])

// 支付状态映射
const payStatusMap = { PENDING: '待支付', PAID: '已支付', PAY_FAILED: '支付失败', REFUNDED: '已退款' }
const payStatusTagMap = { PENDING: 'info', PAID: 'success', PAY_FAILED: 'danger', REFUNDED: 'warning' }
const rechargeStatusMap = { PENDING: '待充值', SUCCESS: '充值成功', FAILED: '充值失败', RETRYING: '重试中' }
const rechargeStatusTagMap = { PENDING: 'info', SUCCESS: 'success', FAILED: 'danger', RETRYING: 'warning' }
const notifyStatusMap = { PENDING: '待通知', SUCCESS: '通知成功', FAILED: '通知失败' }
const notifyStatusTagMap = { PENDING: 'info', SUCCESS: 'success', FAILED: 'danger' }

const payStatusText = (s) => payStatusMap[s] || s
const payStatusTag = (s) => payStatusTagMap[s] || 'info'
const rechargeStatusText = (s) => rechargeStatusMap[s] || s
const rechargeStatusTag = (s) => rechargeStatusTagMap[s] || 'info'
const notifyStatusText = (s) => notifyStatusMap[s] || s
const notifyStatusTag = (s) => notifyStatusTagMap[s] || 'info'

// 查询
async function search() {
  loading.value = true
  try {
    if (dateRange.value?.length === 2) {
      queryForm.startTime = dateRange.value[0]
      queryForm.endTime = dateRange.value[1]
    } else {
      queryForm.startTime = ''
      queryForm.endTime = ''
    }
    const res = await getOrderList(queryForm)
    tableData.value = (res.data?.records || []).map(row => ({ ...row, _retrying: false }))
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(queryForm, {
    orderNo: '', deviceId: '', userId: '', payStatus: '', rechargeStatus: '',
    startTime: '', endTime: '', pageNum: 1, pageSize: 10
  })
  dateRange.value = []
  search()
}

// 单个重试
async function retrySingle(row) {
  try {
    await ElMessageBox.confirm(`确认重试订单 ${row.orderNo}？`, '提示', { type: 'warning' })
  } catch { return }
  row._retrying = true
  try {
    await retryOrder(row.id, 'admin')
    ElMessage.success('重试成功')
    search()
  } finally {
    row._retrying = false
  }
}

// 一键重试
async function retryAll() {
  try {
    await ElMessageBox.confirm('确认重试所有失败订单？', '提示', { type: 'warning' })
  } catch { return }
  retryAllLoading.value = true
  try {
    const res = await retryAllOrders('admin')
    ElMessage.success(`已触发重试，成功处理 ${res.data} 条`)
    search()
  } finally {
    retryAllLoading.value = false
  }
}

// 查看日志
async function viewLogs(row) {
  logDialogVisible.value = true
  try {
    const res = await getOrderLogs(row.id)
    logList.value = res.data || []
  } catch {
    logList.value = []
  }
}

onMounted(() => search())
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card {
  border-radius: 8px;
}
.table-card {
  border-radius: 8px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar-title {
  font-size: 16px;
  font-weight: 600;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.log-item {
  display: flex;
  align-items: center;
}
.log-content {
  margin-top: 4px;
  color: #606266;
}
</style>
