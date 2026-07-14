<template>
  <div class="page-container">
    <el-card class="header-card" shadow="never">
      <div class="page-header">
        <h3>定时任务管理</h3>
        <span class="page-desc">可手动触发以下定时任务，无需等待调度时间</span>
      </div>
    </el-card>

    <!-- 任务列表 -->
    <div class="task-grid">
      <el-card v-for="task in tasks" :key="task.id" class="task-card" shadow="hover">
        <div class="task-card-body">
          <div class="task-icon" :style="{ background: task.color }">
            <el-icon :size="28"><component :is="task.icon" /></el-icon>
          </div>
          <div class="task-info">
            <div class="task-name">{{ task.name }}</div>
            <div class="task-desc">{{ task.desc }}</div>
            <div class="task-cron">
              <el-tag type="info" size="small">
                <el-icon style="margin-right: 4px"><Timer /></el-icon>
                {{ task.cron }}
              </el-tag>
            </div>
          </div>
          <div class="task-action">
            <el-button
              type="primary"
              :loading="task.running"
              @click="handleTrigger(task)"
            >
              <el-icon style="margin-right: 4px"><VideoPlay /></el-icon>手动触发
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态占位 -->
    <el-card v-if="tasks.length === 0" class="empty-card" shadow="never">
      <el-empty description="暂无定时任务" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { runDailyFlowTask, triggerAlertTask, triggerLogCleanupTask } from '@/api'

const tasks = reactive([
  {
    id: 'daily-flow',
    name: '每日流水生成',
    desc: '每天统计前一天的充值订单数据，生成每日流水确认单，用于对账管理',
    cron: '每天 00:05',
    icon: 'Finished',
    color: '#409EFF',
    running: false
  },
  {
    id: 'alert-check',
    name: '告警检查任务',
    desc: '每5分钟检查充值失败订单，发现接口连续失败或预存款不足时，通过钉钉/邮件发送告警通知',
    cron: '每 5 分钟',
    icon: 'Bell',
    color: '#E6A23C',
    running: false
  },
  {
    id: 'log-cleanup',
    name: '日志清理任务',
    desc: '每天凌晨3点自动清理185天（约6个月）之前的操作日志，确保数据库空间可控',
    cron: '每天 03:00',
    icon: 'Delete',
    color: '#909399',
    running: false
  }
])

async function handleTrigger(task) {
  task.running = true
  try {
    const handlers = {
      'daily-flow': () => runDailyFlowTask(),
      'alert-check': () => triggerAlertTask(),
      'log-cleanup': () => triggerLogCleanupTask()
    }
    const handler = handlers[task.id]
    if (handler) {
      await handler()
      ElMessage.success(`「${task.name}」已触发成功`)
    }
  } catch {
    // 错误已由请求拦截器统一处理
  } finally {
    task.running = false
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
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 16px;
}
.task-card {
  border-radius: 8px;
}
.task-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
}
.task-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.task-info {
  flex: 1;
  min-width: 0;
}
.task-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}
.task-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.5;
}
.task-cron {
  font-size: 12px;
}
.task-action {
  flex-shrink: 0;
}
.empty-card {
  border-radius: 8px;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
