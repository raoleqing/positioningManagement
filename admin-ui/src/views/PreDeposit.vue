<template>
  <div class="page-container">
    <!-- 账户信息 -->
    <el-card class="info-card" shadow="never">
      <template #header><span class="card-title">预存款账户</span></template>
      <div v-if="account" class="account-info">
        <div class="account-item">
          <span class="label">账户名称</span>
          <span class="value">{{ account.accountName }}</span>
        </div>
        <div class="account-item">
          <span class="label">当前余额</span>
          <span class="value highlight" :class="{ 'low-balance': account.status === 'LOW_BALANCE' }">
            ¥{{ account.balance?.toFixed(2) }}
          </span>
        </div>
        <div class="account-item">
          <span class="label">累计充值</span>
          <span class="value">¥{{ account.totalDeposit?.toFixed(2) }}</span>
        </div>
        <div class="account-item">
          <span class="label">累计消费</span>
          <span class="value">¥{{ account.totalConsume?.toFixed(2) }}</span>
        </div>
        <div class="account-item">
          <span class="label">账户状态</span>
          <el-tag :type="account.status === 'LOW_BALANCE' ? 'danger' : 'success'">{{ account.status === 'LOW_BALANCE' ? '余额不足' : '正常' }}</el-tag>
        </div>
      </div>
      <el-empty v-else description="暂无账户数据" />
    </el-card>

    <!-- 告警配置 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span class="card-title">告警配置</span>
        <el-button type="primary" size="small" style="float: right" @click="showAlertDialog(true)">编辑配置</el-button>
      </template>
      <el-descriptions v-if="alertConfig" :column="2" border>
        <el-descriptions-item label="告警阈值">¥{{ alertConfig.alertThreshold?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="启用状态">
          <el-switch :model-value="alertConfig.alertEnabled === 1" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="告警手机号">{{ alertConfig.alertPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="告警邮箱">{{ alertConfig.alertEmail || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 消耗明细 -->
    <el-card class="record-card" shadow="never">
      <template #header><span class="card-title">消耗明细</span></template>
      <el-table :data="records" border stripe v-loading="recordLoading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type === 'CONSUME' ? '消费' : row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="100" />
        <el-table-column prop="balanceBefore" label="变动前余额" width="120">
          <template #default="{ row }">¥{{ row.balanceBefore?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后余额" width="120">
          <template #default="{ row }">¥{{ row.balanceAfter?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="recordPage"
          :page-size="recordPageSize"
          :total="recordTotal"
          layout="total, prev, pager, next"
          @current-change="loadRecords"
        />
      </div>
    </el-card>

    <!-- 告警配置对话框 -->
    <el-dialog v-model="alertDialogVisible" title="告警配置" width="480px" destroy-on-close>
      <el-form :model="alertForm" :rules="alertRules" ref="alertFormRef" label-width="100px">
        <el-form-item label="告警阈值" prop="alertThreshold">
          <el-input-number v-model="alertForm.alertThreshold" :precision="2" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="启用告警" prop="alertEnabled">
          <el-switch v-model="alertForm.alertEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="告警手机号">
          <el-input v-model="alertForm.alertPhone" placeholder="手机号，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="告警邮箱">
          <el-input v-model="alertForm.alertEmail" placeholder="邮箱，多个用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="alertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAlertConfig" :loading="alertSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPredepositAccount, getPredepositRecords, getAlertConfig, updateAlertConfig } from '@/api'

const account = ref(null)
const records = ref([])
const recordLoading = ref(false)
const recordPage = ref(1)
const recordPageSize = ref(20)
const recordTotal = ref(0)
const alertConfig = ref(null)

// 告警配置弹窗
const alertDialogVisible = ref(false)
const alertSaving = ref(false)
const alertFormRef = ref(null)
const alertForm = reactive({
  accountId: 1,
  alertThreshold: 500,
  alertEnabled: 1,
  alertPhone: '',
  alertEmail: ''
})
const alertRules = {
  alertThreshold: [{ required: true, message: '请输入告警阈值', trigger: 'blur' }]
}

async function loadAccount() {
  try {
    const res = await getPredepositAccount()
    account.value = res.data
    if (res.data?.id) {
      await loadAlertConfig(res.data.id)
    }
  } catch { /* ignore */ }
}

async function loadAlertConfig(accountId) {
  try {
    const res = await getAlertConfig(accountId)
    alertConfig.value = res.data
    Object.assign(alertForm, res.data)
  } catch { /* ignore */ }
}

async function loadRecords() {
  recordLoading.value = true
  try {
    const res = await getPredepositRecords({
      accountId: account.value?.id || 1,
      pageNum: recordPage.value,
      pageSize: recordPageSize.value
    })
    // 响应可能是分页或直接list
    records.value = res.data?.records || res.data || []
    recordTotal.value = res.data?.total || records.value.length
  } finally {
    recordLoading.value = false
  }
}

function showAlertDialog() {
  Object.assign(alertForm, {
    accountId: account.value?.id || 1,
    alertThreshold: alertConfig.value?.alertThreshold || 500,
    alertEnabled: alertConfig.value?.alertEnabled ?? 1,
    alertPhone: alertConfig.value?.alertPhone || '',
    alertEmail: alertConfig.value?.alertEmail || ''
  })
  alertDialogVisible.value = true
}

async function submitAlertConfig() {
  const valid = await alertFormRef.value.validate().catch(() => false)
  if (!valid) return
  alertSaving.value = true
  try {
    await updateAlertConfig(alertForm)
    ElMessage.success('告警配置已保存')
    alertDialogVisible.value = false
    loadAlertConfig(account.value?.id || 1)
  } finally {
    alertSaving.value = false
  }
}

onMounted(() => {
  loadAccount().then(() => loadRecords())
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-card, .config-card, .record-card {
  border-radius: 8px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.account-info {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}
.account-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.account-item .label {
  font-size: 13px;
  color: #909399;
}
.account-item .value {
  font-size: 20px;
  font-weight: 600;
}
.account-item .highlight {
  color: #409EFF;
}
.account-item .low-balance {
  color: #F56C6C;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
