<template>
  <div class="page-container">
    <!-- 查询设备有效期 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="设备ID">
          <el-input v-model="queryForm.deviceId" placeholder="输入设备ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="queryValidity">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备有效期信息 -->
    <el-card v-if="validity" class="info-card" shadow="never">
      <template #header>
        <span class="card-title">设备有效期信息</span>
        <el-button type="primary" style="float: right" @click="showUpdateDialog">手动修正有效期</el-button>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="设备ID">{{ validity.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ validity.deviceNo }}</el-descriptions-item>
        <el-descriptions-item label="SIM卡号">{{ validity.simCardNo }}</el-descriptions-item>
        <el-descriptions-item label="有效期起始">{{ validity.validFrom || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效期截止">
          <el-tag :type="validity.remainingDays < 30 ? 'danger' : validity.remainingDays < 90 ? 'warning' : 'success'">
            {{ validity.validTo || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="剩余天数">
          <span :style="{ color: validity.remainingDays < 30 ? '#F56C6C' : validity.remainingDays < 90 ? '#E6A23C' : '#67C23A', fontWeight: 'bold' }">
            {{ validity.remainingDays }} 天
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">{{ validity.status }}</el-descriptions-item>
        <el-descriptions-item label="最近充值时间">{{ validity.lastRechargeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="累计充值金额">{{ validity.totalRechargeAmount }} 元</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-empty v-else-if="searched" description="请输入设备ID查询" />

    <!-- 修正日志 -->
    <el-card v-if="validity" class="log-card" shadow="never">
      <template #header>
        <span class="card-title">有效期修正日志</span>
        <el-button style="float: right" @click="loadLogs">刷新</el-button>
      </template>
      <el-table :data="logList" border stripe v-loading="logLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="修正前有效期" width="200">
          <template #default="{ row }">{{ row.oldValidFrom || '-' }} ~ {{ row.oldValidTo || '-' }}</template>
        </el-table-column>
        <el-table-column label="修正后有效期" width="200">
          <template #default="{ row }">{{ row.newValidFrom || '-' }} ~ {{ row.newValidTo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="修正原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="160" />
      </el-table>
      <el-empty v-if="!logLoading && logList.length === 0" description="暂无修正记录" />
    </el-card>

    <!-- 修正有效期对话框 -->
    <el-dialog v-model="updateDialogVisible" title="手动修正有效期" width="520px" destroy-on-close>
      <el-form :model="updateForm" :rules="updateRules" ref="updateFormRef" label-width="110px">
        <el-form-item label="设备ID">
          <el-input :model-value="validity?.deviceId" disabled />
        </el-form-item>
        <el-form-item label="有效期起始" prop="validFrom">
          <el-date-picker v-model="updateForm.validFrom" type="date" placeholder="选择起始日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="有效期截止" prop="validTo">
          <el-date-picker v-model="updateForm.validTo" type="date" placeholder="选择截止日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="修正原因" prop="reason">
          <el-input v-model="updateForm.reason" type="textarea" :rows="3" placeholder="请输入修正原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdate" :loading="updateLoading">确认修正</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDeviceValidity, getValidityUpdateLogs, updateValidity } from '@/api'

const queryForm = reactive({ deviceId: '' })
const validity = ref(null)
const searched = ref(false)
const logList = ref([])
const logLoading = ref(false)

// 修正弹窗
const updateDialogVisible = ref(false)
const updateLoading = ref(false)
const updateFormRef = ref(null)
const updateForm = reactive({
  validFrom: '',
  validTo: '',
  reason: ''
})
const updateRules = {
  validTo: [{ required: true, message: '请选择有效期截止日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入修正原因', trigger: 'blur' }]
}

async function queryValidity() {
  if (!queryForm.deviceId) { ElMessage.warning('请输入设备ID'); return }
  try {
    const res = await getDeviceValidity(queryForm.deviceId)
    validity.value = res.data
    searched.value = true
    loadLogs()
  } catch {
    validity.value = null
    searched.value = true
  }
}

async function loadLogs() {
  if (!validity.value?.deviceId) return
  logLoading.value = true
  try {
    const res = await getValidityUpdateLogs(validity.value.deviceId)
    logList.value = res.data || []
  } finally {
    logLoading.value = false
  }
}

function showUpdateDialog() {
  updateForm.validFrom = ''
  updateForm.validTo = ''
  updateForm.reason = ''
  updateDialogVisible.value = true
}

async function submitUpdate() {
  const valid = await updateFormRef.value.validate().catch(() => false)
  if (!valid) return
  updateLoading.value = true
  try {
    await updateValidity({
      deviceId: validity.value.deviceId,
      validFrom: updateForm.validFrom || null,
      validTo: updateForm.validTo,
      reason: updateForm.reason
    }, 'admin')
    ElMessage.success('有效期修正成功')
    updateDialogVisible.value = false
    queryValidity()
  } finally {
    updateLoading.value = false
  }
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card, .info-card, .log-card {
  border-radius: 8px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
