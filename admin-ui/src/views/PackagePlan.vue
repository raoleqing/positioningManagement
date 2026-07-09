<template>
  <div class="page-container">
    <!-- 搜索面板 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="套餐名称">
          <el-input v-model="queryForm.planName" placeholder="输入套餐名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" style="width:130px">
            <el-option label="全部" value="" />
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
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
        <span class="toolbar-title">套餐列表</span>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增套餐</el-button>
      </div>

      <!-- 套餐表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="planName" label="套餐名称" width="160" />
        <el-table-column prop="price" label="金额(元)" width="110" />
        <el-table-column prop="years" label="年数" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text="启用"
              inactive-text="停用"
              @change="(val) => toggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="search"
          @current-change="search"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close @closed="resetForm">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="套餐名称" prop="planName">
          <el-input v-model="formData.planName" placeholder="请输入套餐名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="套餐金额(元)" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" :step="100" style="width:100%" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="套餐年数" prop="years">
          <el-input-number v-model="formData.years" :min="1" :max="10" :step="1" style="width:100%" placeholder="请输入年数" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" :step="1" style="width:100%" placeholder="数字越小越靠前" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getPackagePlanList, createPackagePlan, updatePackagePlan, deletePackagePlan, updatePackagePlanStatus } from '@/api'

const queryForm = reactive({
  planName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增套餐')
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const formData = reactive({
  planName: '',
  price: null,
  years: null,
  sortOrder: 0,
  remark: ''
})

const formRules = {
  planName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入套餐金额', trigger: 'blur' }],
  years: [{ required: true, message: '请输入套餐年数', trigger: 'blur' }]
}

async function search() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (params.status === '') params.status = undefined
    const res = await getPackagePlanList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(queryForm, { planName: '', status: '', pageNum: 1, pageSize: 10 })
  search()
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(formData, { planName: '', price: null, years: null, sortOrder: 0, remark: '' })
  isEdit.value = false
  editId.value = null
}

function openAdd() {
  dialogTitle.value = '新增套餐'
  isEdit.value = false
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑套餐'
  isEdit.value = true
  editId.value = row.id
  formData.planName = row.planName
  formData.price = row.price
  formData.years = row.years
  formData.sortOrder = row.sortOrder ?? 0
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePackagePlan(editId.value, { ...formData }, 'admin')
      ElMessage.success('修改成功')
    } else {
      await createPackagePlan({ ...formData }, 'admin')
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    search()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除套餐「${row.planName}」？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await deletePackagePlan(row.id, 'admin')
    ElMessage.success('删除成功')
    search()
  } catch { /* handled by interceptor */ }
}

async function toggleStatus(row, val) {
  const newStatus = val ? 1 : 0
  const text = val ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认${text}套餐「${row.planName}」？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await updatePackagePlanStatus(row.id, newStatus, 'admin')
    ElMessage.success(`${text}成功`)
    row.status = newStatus
  } catch { /* handled by interceptor */ }
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
</style>
