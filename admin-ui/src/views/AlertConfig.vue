<template>
  <div class="page-container">
    <el-card class="header-card" shadow="never">
      <div class="page-header">
        <h3>告警管理</h3>
        <span class="page-desc">配置充值失败和预存款不足的告警通知方式（钉钉 / 邮件）</span>
      </div>
    </el-card>

    <!-- 告警类型Tab -->
    <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
      <el-tab-pane label="充值失败告警" name="RECHARGE_FAIL">
        <div class="alert-content">
          <div class="alert-tip">
            <el-icon style="color:#E6A23C"><WarningFilled /></el-icon>
            当充值接口连续失败（重试次数耗尽）时触发此告警
          </div>
          <el-form :model="forms.RECHARGE_FAIL" label-width="120px" class="config-form">
            <el-divider content-position="left">钉钉通知</el-divider>
            <el-form-item label="启用钉钉">
              <el-switch v-model="forms.RECHARGE_FAIL.dingtalkEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="Webhook地址">
              <el-input
                v-model="forms.RECHARGE_FAIL.dingtalkWebhookUrl"
                placeholder="钉钉机器人Webhook地址"
                :disabled="!forms.RECHARGE_FAIL.dingtalkEnabled"
              />
            </el-form-item>

            <el-divider content-position="left">邮件通知</el-divider>
            <el-form-item label="启用邮件">
              <el-switch v-model="forms.RECHARGE_FAIL.emailEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="接收人">
              <el-input
                v-model="forms.RECHARGE_FAIL.emailRecipients"
                placeholder="多个邮箱用逗号分隔，如 a@qq.com,b@163.com"
                :disabled="!forms.RECHARGE_FAIL.emailEnabled"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave('RECHARGE_FAIL')" :loading="saving">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="预存款不足告警" name="BALANCE_LOW">
        <div class="alert-content">
          <div class="alert-tip">
            <el-icon style="color:#E6A23C"><WarningFilled /></el-icon>
            当SIM卡预存款不足导致充值失败时触发此告警
          </div>
          <el-form :model="forms.BALANCE_LOW" label-width="120px" class="config-form">
            <el-divider content-position="left">钉钉通知</el-divider>
            <el-form-item label="启用钉钉">
              <el-switch v-model="forms.BALANCE_LOW.dingtalkEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="Webhook地址">
              <el-input
                v-model="forms.BALANCE_LOW.dingtalkWebhookUrl"
                placeholder="钉钉机器人Webhook地址"
                :disabled="!forms.BALANCE_LOW.dingtalkEnabled"
              />
            </el-form-item>

            <el-divider content-position="left">邮件通知</el-divider>
            <el-form-item label="启用邮件">
              <el-switch v-model="forms.BALANCE_LOW.emailEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="接收人">
              <el-input
                v-model="forms.BALANCE_LOW.emailRecipients"
                placeholder="多个邮箱用逗号分隔"
                :disabled="!forms.BALANCE_LOW.emailEnabled"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave('BALANCE_LOW')" :loading="saving">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlertConfigList, saveAlertConfig } from '@/api'

const activeTab = ref('RECHARGE_FAIL')
const saving = ref(false)

const defaultForm = {
  dingtalkWebhookUrl: '',
  dingtalkEnabled: 0,
  emailRecipients: '',
  emailEnabled: 0
}

const forms = reactive({
  RECHARGE_FAIL: { ...defaultForm, alertType: 'RECHARGE_FAIL' },
  BALANCE_LOW: { ...defaultForm, alertType: 'BALANCE_LOW' }
})

onMounted(async () => {
  try {
    const res = await getAlertConfigList()
    if (res.data) {
      res.data.forEach(cfg => {
        if (forms[cfg.alertType]) {
          Object.assign(forms[cfg.alertType], cfg)
        }
      })
    }
  } catch {
    // 配置暂无
  }
})

function handleTabChange() {
  // tab切换无需额外处理
}

async function handleSave(alertType) {
  saving.value = true
  try {
    await saveAlertConfig(forms[alertType])
    ElMessage.success('告警配置保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header-card {
  border-radius: 8px;
}
.page-header h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #303133;
}
.page-desc {
  font-size: 13px;
  color: #909399;
}
.alert-content {
  padding: 8px 0;
}
.alert-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  font-size: 13px;
  color: #E6A23C;
  margin-bottom: 20px;
}
.config-form {
  max-width: 520px;
}
</style>
