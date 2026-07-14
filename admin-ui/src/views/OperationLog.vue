<template>
  <div class="operation-log">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="订单号">
          <el-input
            v-model="queryParams.businessNo"
            placeholder="输入订单号搜索"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryParams.businessType" placeholder="全部" clearable style="width: 140px">
            <el-option label="订单" value="ORDER" />
            <el-option label="套餐" value="PACKAGE_PLAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="日志级别">
          <el-select v-model="queryParams.logLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="INFO" value="INFO" />
            <el-option label="WARN" value="WARN" />
            <el-option label="ERROR" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>操作日志列表</span>
          <el-tag type="info" size="small">
            日志保留至少 6 个月，当前总数 {{ total }} 条
          </el-tag>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
        :max-height="tableHeight"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="businessNo" label="订单号" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button
              v-if="row.businessNo"
              link
              type="primary"
              size="small"
              @click="handleTrace(row.businessNo)"
            >
              {{ row.businessNo }}
            </el-button>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.businessType === 'ORDER' ? 'primary' : 'success'" size="small">
              {{ row.businessType === 'ORDER' ? '订单' : row.businessType === 'PACKAGE_PLAN' ? '套餐' : row.businessType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessName" label="业务名称" width="140" show-overflow-tooltip />
        <el-table-column prop="logTypeText" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.logTypeText || row.logType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="日志标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="logLevel" label="级别" width="70" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.logLevel === 'ERROR' ? 'danger' : row.logLevel === 'WARN' ? 'warning' : 'info'"
              size="small"
            >
              {{ row.logLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.operator || '系统' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 链路追踪弹窗 -->
    <el-dialog
      v-model="traceDialogVisible"
      :title="'链路追踪 - ' + traceOrderNo"
      width="900px"
      top="5vh"
      destroy-on-close
    >
      <div v-if="traceData.length === 0" class="trace-empty">
        <el-empty description="未找到该订单的操作日志" />
      </div>
      <el-timeline v-else class="trace-timeline">
        <el-timeline-item
          v-for="item in traceData"
          :key="item.id"
          :timestamp="item.createTime"
          :color="item.logLevel === 'ERROR' ? '#F56C6C' : item.logLevel === 'WARN' ? '#E6A23C' : '#409EFF'"
          placement="top"
          :icon="getTraceIcon(item.logType)"
        >
          <el-card shadow="hover" class="trace-card">
            <div class="trace-header">
              <el-tag
                :type="item.logLevel === 'ERROR' ? 'danger' : item.logLevel === 'WARN' ? 'warning' : 'info'"
                size="small"
                effect="plain"
              >
                {{ item.logLevel }}
              </el-tag>
              <span class="trace-type">{{ item.logTypeText || item.logType }}</span>
              <span class="trace-operator">{{ item.operator || '系统' }}</span>
            </div>
            <div class="trace-title">{{ item.title }}</div>
            <div v-if="item.content" class="trace-content">
              <pre>{{ formatContent(item.content) }}</pre>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="traceDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 日志详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="650px"
      top="5vh"
      destroy-on-close
    >
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">
          <el-tag :type="currentDetail.businessType === 'ORDER' ? 'primary' : 'success'" size="small">
            {{ currentDetail.businessType === 'ORDER' ? '订单' : currentDetail.businessType === 'PACKAGE_PLAN' ? '套餐' : currentDetail.businessType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentDetail.businessNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务名称">{{ currentDetail.businessName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentDetail.logTypeText || currentDetail.logType }}</el-descriptions-item>
        <el-descriptions-item label="日志级别">
          <el-tag :type="currentDetail.logLevel === 'ERROR' ? 'danger' : currentDetail.logLevel === 'WARN' ? 'warning' : 'info'" size="small">
            {{ currentDetail.logLevel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="日志标题" :span="2">{{ currentDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentDetail.operator || '系统' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="日志内容" :span="2" v-if="currentDetail.content">
          <pre class="detail-content">{{ formatContent(currentDetail.content) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOperationLogList, traceOperationLog } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableHeight = ref(window.innerHeight - 420)
const total = ref(0)
const tableData = ref([])

const dateRange = ref([])
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  businessNo: '',
  businessType: '',
  logLevel: '',
  startTime: '',
  endTime: ''
})

// 链路追踪
const traceDialogVisible = ref(false)
const traceOrderNo = ref('')
const traceData = ref([])

// 详情
const detailDialogVisible = ref(false)
const currentDetail = ref({})

function buildParams() {
  const params = { ...queryParams }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  } else {
    params.startTime = ''
    params.endTime = ''
  }
  return params
}

function handleSearch() {
  loading.value = true
  getOperationLogList(buildParams())
    .then(res => {
      const data = res.data
      tableData.value = data.records || []
      total.value = data.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function handleReset() {
  queryParams.pageNum = 1
  queryParams.businessNo = ''
  queryParams.businessType = ''
  queryParams.logLevel = ''
  dateRange.value = []
  queryParams.startTime = ''
  queryParams.endTime = ''
  handleSearch()
}

function handleTrace(businessNo) {
  traceOrderNo.value = businessNo
  traceDialogVisible.value = true
  traceData.value = []
  traceOperationLog(businessNo)
    .then(res => {
      traceData.value = res.data || []
    })
}

function handleViewDetail(row) {
  currentDetail.value = row
  detailDialogVisible.value = true
}

function getTraceIcon(logType) {
  if (!logType) return undefined
  if (logType.includes('CREATE')) return 'Plus'
  if (logType.includes('PAY')) return 'CreditCard'
  if (logType.includes('RECHARGE')) return 'Connection'
  if (logType.includes('NOTIFY')) return 'Message'
  if (logType.includes('RETRY')) return 'Refresh'
  if (logType.includes('ERROR') || logType.includes('FAILED')) return 'CloseBold'
  return 'MoreFilled'
}

function formatContent(content) {
  if (!content) return ''
  try {
    const obj = JSON.parse(content)
    return JSON.stringify(obj, null, 2)
  } catch {
    return content
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.operation-log {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card .search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.table-card {
  flex: 1;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 链路追踪 */
.trace-timeline {
  padding: 20px 12px;
}
.trace-card {
  margin-bottom: 8px;
}
.trace-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.trace-type {
  font-weight: 600;
  color: #303133;
}
.trace-operator {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}
.trace-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}
.trace-content pre {
  margin: 0;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
.trace-empty {
  padding: 40px 0;
}

/* 详情 */
.detail-content {
  margin: 0;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 10px 14px;
  border-radius: 4px;
  max-height: 260px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
