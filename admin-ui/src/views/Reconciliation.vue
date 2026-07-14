<template>
  <div class="page-container">
    <!-- 操作栏 -->
    <el-card class="search-card" shadow="never">
      <el-form inline>
        <el-form-item label="流水日期">
          <el-date-picker
            v-model="queryDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" @click="handleGenerate" :loading="generating">
            <el-icon style="margin-right:4px"><Refresh /></el-icon>生成流水
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 流水数据展示 -->
    <div v-if="flowData && flowData.flowDate" class="flow-content">
      <!-- 概览卡片 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">订单总数</div>
            <div class="stat-value">{{ flowData.totalOrderCount ?? '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card paid" shadow="hover">
            <div class="stat-label">已支付笔数</div>
            <div class="stat-value">{{ flowData.paidOrderCount ?? '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card paid" shadow="hover">
            <div class="stat-label">已支付金额</div>
            <div class="stat-value">{{ fmtAmount(flowData.paidAmount) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">退款笔数 / 金额</div>
            <div class="stat-value small">{{ flowData.refundCount ?? 0 }} 笔 / {{ fmtAmount(flowData.refundAmount) }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="stat-row">
        <el-col :span="6">
          <el-card class="stat-card success" shadow="hover">
            <div class="stat-label">充值成功笔数</div>
            <div class="stat-value">{{ flowData.rechargeSuccessCount ?? '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card success" shadow="hover">
            <div class="stat-label">充值成功金额</div>
            <div class="stat-value">{{ fmtAmount(flowData.rechargeSuccessAmount) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card fail" shadow="hover">
            <div class="stat-label">充值失败笔数</div>
            <div class="stat-value">{{ flowData.rechargeFailedCount ?? '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card fail" shadow="hover">
            <div class="stat-label">充值失败金额</div>
            <div class="stat-value">{{ fmtAmount(flowData.rechargeFailedAmount) }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 流水详情表 -->
      <el-card class="table-card" shadow="never">
        <div class="toolbar">
          <span class="toolbar-title">流水确认单 — {{ flowData.flowDate }}</span>
          <el-tag v-if="flowData.remark" type="info">{{ flowData.remark }}</el-tag>
        </div>
        <el-table :data="[flowData]" border stripe style="width:100%">
          <el-table-column prop="flowDate" label="日期" width="120" />
          <el-table-column prop="totalOrderCount" label="订单总数" width="100" />
          <el-table-column prop="paidOrderCount" label="已支付笔数" width="110" />
          <el-table-column label="已支付金额(元)" width="130">
            <template #default="{ row }">{{ row.paidAmount }}</template>
          </el-table-column>
          <el-table-column prop="rechargeSuccessCount" label="充值成功笔数" width="130" />
          <el-table-column label="充值成功金额(元)" width="150">
            <template #default="{ row }">{{ row.rechargeSuccessAmount }}</template>
          </el-table-column>
          <el-table-column prop="rechargeFailedCount" label="充值失败笔数" width="130" />
          <el-table-column label="充值失败金额(元)" width="150">
            <template #default="{ row }">{{ row.rechargeFailedAmount }}</template>
          </el-table-column>
          <el-table-column prop="refundCount" label="退款笔数" width="100" />
          <el-table-column label="退款金额(元)" width="120">
            <template #default="{ row }">{{ row.refundAmount }}</template>
          </el-table-column>
          <el-table-column label="生成时间" min-width="160">
            <template #default="{ row }">{{ row.createTime }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-card v-else class="empty-card" shadow="never">
      <el-empty description="请选择日期并点击「查询」或「生成流水」">
        <el-button type="primary" @click="handleGenerate" :loading="generating">生成流水</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getDailyFlow, generateDailyFlow } from '@/api'

const queryDate = ref(today())
const flowData = ref(null)
const generating = ref(false)

// 禁用未来日期
function disabledDate(time) {
  return time.getTime() > Date.now()
}

// 获取今天日期字符串
function today() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

// 格式化金额
function fmtAmount(val) {
  if (val === null || val === undefined || val === '') return '-'
  return '¥ ' + Number(val).toFixed(2)
}

// 查询流水
async function handleQuery() {
  if (!queryDate.value) {
    ElMessage.warning('请选择日期')
    return
  }
  try {
    const res = await getDailyFlow(queryDate.value)
    flowData.value = res.data
  } catch {
    flowData.value = null
  }
}

// 生成流水
async function handleGenerate() {
  if (!queryDate.value) {
    ElMessage.warning('请选择日期')
    return
  }
  generating.value = true
  try {
    await generateDailyFlow(queryDate.value)
    ElMessage.success('流水生成成功')
    await handleQuery()
  } finally {
    generating.value = false
  }
}
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
.flow-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.stat-row {
  row-gap: 16px;
}
.stat-card {
  border-radius: 8px;
  text-align: center;
  border-left: 4px solid #909399;
}
.stat-card.paid {
  border-left-color: #409EFF;
}
.stat-card.success {
  border-left-color: #67C23A;
}
.stat-card.fail {
  border-left-color: #F56C6C;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-value.small {
  font-size: 16px;
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
.empty-card {
  border-radius: 8px;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
