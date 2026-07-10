<template>
  <div class="page-container">
    <!-- 搜索面板 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="输入订单号" clearable />
        </el-form-item>
        <el-form-item label="扣费状态">
          <el-select v-model="queryForm.rechargeStatus" placeholder="全部" clearable style="width:130px">
            <el-option label="扣费成功" value="SUCCESS" />
            <el-option label="扣费失败" value="FAILED" />
            <el-option label="接口调用异常" value="CALL_FAILED" />
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

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <span class="toolbar-title">预存款消耗明细</span>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="deviceNo" label="设备编号" width="140" show-overflow-tooltip />
        <el-table-column prop="iccid" label="ICCID" width="180" show-overflow-tooltip />
        <el-table-column prop="planName" label="套餐名称" width="140" show-overflow-tooltip />
        <el-table-column prop="amount" label="扣费金额(元)" width="110" />
        <el-table-column label="扣费状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.rechargeStatus)" size="small">{{ statusText(row.rechargeStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="失败原因" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.errorMsg" style="color: #F56C6C;">{{ row.errorMsg }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="rechargeTime" label="扣费时间" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewResponse(row)">查看响应</el-button>
            <el-button
              v-if="isFailed(row.rechargeStatus)"
              type="warning"
              link
              :loading="row._retrying"
              @click="handleRetry(row)"
            >重试</el-button>
            <el-button
              v-if="isFailed(row.rechargeStatus)"
              type="danger"
              link
              :loading="row._refunding"
              @click="handleRefund(row)"
            >退款</el-button>
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

    <!-- 响应详情弹窗 -->
    <el-dialog v-model="responseDialogVisible" title="运营商API响应详情" width="700px" destroy-on-close>
      <div v-if="currentResponse" class="response-json">{{ formattedResponse }}</div>
      <el-empty v-else description="暂无响应数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRechargeRecordList, retryRechargeRecord, refundRechargeRecord } from '@/api'

const queryForm = reactive({
  orderNo: '',
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

// 响应弹窗
const responseDialogVisible = ref(false)
const currentResponse = ref('')

// 状态映射
const statusMap = { SUCCESS: '扣费成功', FAILED: '扣费失败', CALL_FAILED: '接口调用异常' }
const statusTagMap = { SUCCESS: 'success', FAILED: 'danger', CALL_FAILED: 'warning' }
const statusText = (s) => statusMap[s] || s
const statusTag = (s) => statusTagMap[s] || 'info'
const isFailed = (s) => s === 'FAILED' || s === 'CALL_FAILED'

const formattedResponse = computed(() => {
  if (!currentResponse.value) return ''
  try {
    return JSON.stringify(JSON.parse(currentResponse.value), null, 2)
  } catch {
    return currentResponse.value
  }
})

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
    const res = await getRechargeRecordList(queryForm)
    tableData.value = (res.data?.records || []).map(row => ({ ...row, _retrying: false, _refunding: false }))
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(queryForm, {
    orderNo: '', rechargeStatus: '', startTime: '', endTime: '',
    pageNum: 1, pageSize: 10
  })
  dateRange.value = []
  search()
}

function viewResponse(row) {
  currentResponse.value = row.responseBody || ''
  responseDialogVisible.value = true
}

// 重试
async function handleRetry(row) {
  try {
    await ElMessageBox.confirm(`确认重试该笔扣费？将重新调用运营商API进行充值。`, '重试确认', { type: 'warning' })
  } catch { return }
  row._retrying = true
  try {
    await retryRechargeRecord(row.id, 'admin')
    ElMessage.success('重试已触发')
    search()
  } catch {
    // 错误由拦截器提示
  } finally {
    row._retrying = false
  }
}

// 退款
async function handleRefund(row) {
  try {
    await ElMessageBox.confirm(`确认对该笔扣费进行退款？退款后关联订单将标记为已退款。`, '退款确认', { type: 'warning' })
  } catch { return }
  row._refunding = true
  try {
    await refundRechargeRecord(row.id, 'admin')
    ElMessage.success('退款处理成功')
    search()
  } catch {
    // 错误由拦截器提示
  } finally {
    row._refunding = false
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
.response-json {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 500px;
  overflow: auto;
}
</style>
