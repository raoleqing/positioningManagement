<template>
  <div class="page-container">
    <!-- 生成对账报告 -->
    <el-card class="search-card" shadow="never">
      <el-form inline>
        <el-form-item label="对账日期">
          <el-date-picker
            v-model="generateDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doGenerate" :loading="generateLoading">生成对账报告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报告列表 -->
    <el-card class="table-card" shadow="never">
      <template #header><span class="card-title">对账报告列表</span></template>
      <el-form :model="reportQuery" inline class="report-filter">
        <el-form-item label="报告日期">
          <el-date-picker
            v-model="reportDateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="reportQuery.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReports">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="reportList" border stripe v-loading="reportLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="reportNo" label="报告编号" width="200" show-overflow-tooltip />
        <el-table-column prop="reportDate" label="对账日期" width="110" />
        <el-table-column prop="totalCount" label="总笔数" width="80" />
        <el-table-column prop="matchCount" label="匹配数" width="80" />
        <el-table-column label="差异数" width="80">
          <template #default="{ row }">
            <span :style="{ color: row.diffCount > 0 ? '#F56C6C' : '#67C23A', fontWeight: 'bold' }">
              {{ row.diffCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额(元)" width="110" />
        <el-table-column label="差异金额(元)" width="110">
          <template #default="{ row }">
            <span :style="{ color: row.diffAmount > 0 ? '#F56C6C' : '#333' }">¥{{ row.diffAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RESOLVED' ? 'success' : row.status === 'CLOSED' ? 'info' : 'warning'" size="small">
              {{ reportStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetails(row)">差异明细</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              link
              @click="doResolveReport(row)"
              :loading="row._resolving"
            >标记已处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="reportQuery.pageNum"
          v-model:page-size="reportQuery.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="reportTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="loadReports"
          @current-change="loadReports"
        />
      </div>
    </el-card>

    <!-- 差异明细对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="'差异明细 - ' + currentReport?.reportNo" width="1000px" destroy-on-close>
      <el-form :model="detailQuery" inline class="detail-filter">
        <el-form-item label="差异类型">
          <el-select v-model="detailQuery.diffType" placeholder="全部" clearable style="width:140px">
            <el-option label="金额差异" value="AMOUNT_DIFF" />
            <el-option label="我方缺失" value="MISSING_OUR" />
            <el-option label="运营商缺失" value="MISSING_EXTERNAL" />
            <el-option label="状态差异" value="STATUS_DIFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="detailQuery.processStatus" placeholder="全部" clearable style="width:120px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDetails">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="detailList" border stripe v-loading="detailLoading" max-height="400">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="我方订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="externalOrderNo" label="运营商订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="tradeTime" label="交易时间" width="160" />
        <el-table-column prop="amount" label="金额" width="90" />
        <el-table-column prop="ourStatus" label="我方状态" width="80" />
        <el-table-column prop="externalStatus" label="运营商状态" width="90" />
        <el-table-column label="差异类型" width="100">
          <template #default="{ row }">
            <el-tag :type="diffTypeTag(row.diffType)" size="small">{{ diffTypeText(row.diffType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diffDesc" label="差异描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="处理状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.processStatus === 'RESOLVED' ? 'success' : row.processStatus === 'IGNORED' ? 'info' : 'warning'" size="small">
              {{ processStatusText(row.processStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.processStatus !== 'RESOLVED'"
              type="primary"
              link
              @click="showProcessDialog(row)"
            >标记处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="detailQuery.pageNum"
          v-model:page-size="detailQuery.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="detailTotal"
          layout="total, sizes, prev, pager, next"
          small
          @size-change="loadDetails"
          @current-change="loadDetails"
        />
      </div>
    </el-dialog>

    <!-- 标记处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="标记差异处理" width="450px" destroy-on-close>
      <el-form :model="processForm" ref="processFormRef" label-width="100px">
        <el-form-item label="处理状态" prop="processStatus">
          <el-radio-group v-model="processForm.processStatus">
            <el-radio value="RESOLVED">已解决</el-radio>
            <el-radio value="IGNORED">已忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="processForm.processRemark" type="textarea" :rows="2" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="processSaving">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  generateReconciliation,
  getReconciliationReports,
  getReconciliationDetails,
  updateDetailStatus,
  resolveReport
} from '@/api'

// 生成报告
const generateDate = ref('')
const generateLoading = ref(false)

// 报告列表
const reportQuery = reactive({ reportDateStart: '', reportDateEnd: '', status: '', pageNum: 1, pageSize: 10 })
const reportDateRange = ref([])
const reportList = ref([])
const reportTotal = ref(0)
const reportLoading = ref(false)

// 差异明细
const detailDialogVisible = ref(false)
const currentReport = ref(null)
const detailQuery = reactive({ diffType: '', processStatus: '', pageNum: 1, pageSize: 10 })
const detailList = ref([])
const detailTotal = ref(0)
const detailLoading = ref(false)

// 处理状态弹窗
const processDialogVisible = ref(false)
const processSaving = ref(false)
const processFormRef = ref(null)
const processForm = reactive({ detailId: null, processStatus: 'RESOLVED', processRemark: '' })

// 文本映射
const reportStatusText = (s) => ({ PENDING: '待处理', RESOLVED: '已处理', CLOSED: '已关闭' })[s] || s
const diffTypeMap = { AMOUNT_DIFF: '金额差异', MISSING_OUR: '我方缺失', MISSING_EXTERNAL: '运营商缺失', STATUS_DIFF: '状态差异' }
const diffTypeTagMap = { AMOUNT_DIFF: 'danger', MISSING_OUR: 'warning', MISSING_EXTERNAL: 'warning', STATUS_DIFF: 'info' }
const diffTypeText = (t) => diffTypeMap[t] || t
const diffTypeTag = (t) => diffTypeTagMap[t] || 'info'
const processStatusText = (s) => ({ PENDING: '待处理', RESOLVED: '已解决', IGNORED: '已忽略' })[s] || s

async function doGenerate() {
  if (!generateDate.value) { ElMessage.warning('请选择对账日期'); return }
  generateLoading.value = true
  try {
    await generateReconciliation({ reportDate: generateDate.value }, 'admin')
    ElMessage.success('对账报告生成成功')
    loadReports()
  } finally {
    generateLoading.value = false
  }
}

async function loadReports() {
  reportLoading.value = true
  try {
    if (reportDateRange.value?.length === 2) {
      reportQuery.reportDateStart = reportDateRange.value[0]
      reportQuery.reportDateEnd = reportDateRange.value[1]
    } else {
      reportQuery.reportDateStart = ''
      reportQuery.reportDateEnd = ''
    }
    const res = await getReconciliationReports(reportQuery)
    reportList.value = (res.data?.records || []).map(row => ({ ...row, _resolving: false }))
    reportTotal.value = res.data?.total || 0
  } finally {
    reportLoading.value = false
  }
}

async function doResolveReport(row) {
  try {
    await ElMessageBox.confirm('确认标记该报告为已处理？', '提示', { type: 'info' })
  } catch { return }
  row._resolving = true
  try {
    await resolveReport(row.id, 'admin')
    ElMessage.success('报告已标记为已处理')
    loadReports()
  } finally {
    row._resolving = false
  }
}

async function viewDetails(row) {
  currentReport.value = row
  Object.assign(detailQuery, { diffType: '', processStatus: '', pageNum: 1, pageSize: 10 })
  detailDialogVisible.value = true
  loadDetails()
}

async function loadDetails() {
  if (!currentReport.value) return
  detailLoading.value = true
  try {
    const res = await getReconciliationDetails(currentReport.value.id, detailQuery)
    detailList.value = res.data?.records || []
    detailTotal.value = res.data?.total || 0
  } finally {
    detailLoading.value = false
  }
}

function showProcessDialog(row) {
  processForm.detailId = row.id
  processForm.processStatus = 'RESOLVED'
  processForm.processRemark = ''
  processDialogVisible.value = true
}

async function submitProcess() {
  processSaving.value = true
  try {
    await updateDetailStatus(processForm.detailId, {
      processStatus: processForm.processStatus,
      processRemark: processForm.processRemark,
      operator: 'admin'
    })
    ElMessage.success('处理状态已更新')
    processDialogVisible.value = false
    loadDetails()
    loadReports()
  } finally {
    processSaving.value = false
  }
}

onMounted(() => loadReports())
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card, .table-card {
  border-radius: 8px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.report-filter, .detail-filter {
  margin-bottom: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
