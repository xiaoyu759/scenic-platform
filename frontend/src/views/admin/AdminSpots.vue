<template>
  <div class="dashboard">
    <AdminHeader />
    
    <div class="dashboard-main">
      <div class="admin-header">
        <h2>景点管理</h2>
        <el-button type="primary" @click="openDialog('add')" class="modern-btn">
          <el-icon><Plus /></el-icon>
          新增景点
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar glass-card fade-in-up">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索景点名称"
          prefix-icon="Search"
          clearable
          class="custom-input"
          style="width: 250px"
          @clear="fetchSpots"
          @keyup.enter="fetchSpots"
        />
        <el-select v-model="categoryFilter" placeholder="景点分类" clearable class="custom-input" style="width: 150px" @change="fetchSpots">
          <el-option label="交通" value="transport" />
          <el-option label="文化" value="culture" />
          <el-option label="景观" value="scenery" />
          <el-option label="自然" value="nature" />
          <el-option label="海滩" value="beach" />
          <el-option label="温泉" value="hotel" />
        </el-select>
        <el-button type="primary" @click="fetchSpots" class="modern-btn">搜索</el-button>
        <el-button @click="resetSearch" class="modern-btn">重置</el-button>
      </div>

      <!-- 景点列表 -->
      <div class="glass-card fade-in-up" style="animation-delay: 0.1s; padding: 0; overflow: hidden;">
        <el-table 
          :data="spots" 
          v-loading="loading"
          class="modern-table"
          :row-style="rowStyle"
          :cell-style="cellStyle"
          @cell-mouse-enter="handleCellMouseEnter"
          @cell-mouse-leave="handleCellMouseLeave"
        >
          <el-table-column prop="id" label="ID" width="70" sortable />
          <el-table-column prop="name" label="景点名称" width="140" sortable>
            <template #default="{ row }">
              <span class="spot-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="100">
            <template #default="{ row }">
              <span class="category-tag" :class="row.category">{{ getCategoryText(row.category) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="简介" show-overflow-tooltip />
          <el-table-column prop="stayTime" label="建议时长" width="100" sortable>
            <template #default="{ row }">
              <span class="duration-badge">{{ row.stayTime || 30 }}分钟</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-switch 
                v-model="row.status" 
                active-value="open" 
                inactive-value="closed" 
                @change="updateSpotStatus(row)"
                class="modern-switch"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDialog('edit', row)" class="action-btn">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)" class="action-btn danger">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination fade-in-up" style="animation-delay: 0.2s">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchSpots"
          @current-change="fetchSpots"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" class="glass-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="景点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入景点名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" class="custom-input" style="width: 100%">
            <el-option label="🚢 交通" value="transport" />
            <el-option label="🏮 文化" value="culture" />
            <el-option label="🌉 景观" value="scenery" />
            <el-option label="🌿 自然" value="nature" />
            <el-option label="🏖️ 海滩" value="beach" />
            <el-option label="♨️ 温泉" value="hotel" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input type="textarea" v-model="form.description" placeholder="请输入景点简介" :rows="3" class="custom-input" />
        </el-form-item>
        <el-form-item label="建议时长" prop="stayTime">
          <el-input-number v-model="form.stayTime" :min="15" :max="180" class="custom-input" /> 分钟
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入景点地址" class="custom-input" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.0001" class="custom-input" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.0001" class="custom-input" />
        </el-form-item>
        <el-form-item label="最大容量" prop="maxCapacity">
          <el-input-number v-model="form.maxCapacity" :min="50" :max="10000" class="custom-input" /> 人
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="open">开放</el-radio>
            <el-radio value="closed">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="modern-btn">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting" class="modern-btn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AdminHeader from '@/components/AdminHeader.vue'
// 导入后端接口
import { getSpotManageList, addSpot, updateSpot, deleteSpot } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const searchKeyword = ref('')
const categoryFilter = ref('')
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const spots = ref([])
const formRef = ref()

// 分类映射
const categoryMap = {
  transport: '🚢 交通',
  culture: '🏮 文化',
  scenery: '🌉 景观',
  nature: '🌿 自然',
  beach: '🏖️ 海滩',
  hotel: '♨️ 温泉'
}

const getCategoryText = (category) => {
  return categoryMap[category] || category
}

// 行样式
const rowStyle = ({ row, rowIndex }) => {
  return {
    transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
    cursor: 'pointer'
  }
}

const cellStyle = ({ row, column, rowIndex, columnIndex }) => {
  return {
    padding: '14px 0',
    transition: 'all 0.2s ease'
  }
}

const handleCellMouseEnter = (row, column, cell, event) => {
  cell.style.backgroundColor = 'rgba(64, 158, 255, 0.08)'
}

const handleCellMouseLeave = (row, column, cell, event) => {
  cell.style.backgroundColor = 'transparent'
}

// 获取景点列表（调用后端接口）
const fetchSpots = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (categoryFilter.value) {
      params.category = categoryFilter.value
    }
    
    const res = await getSpotManageList(params)
    if (res.code === 200) {
      spots.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取景点列表失败:', error)
    ElMessage.error('获取景点列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  categoryFilter.value = ''
  pageNo.value = 1
  fetchSpots()
}

// 打开弹窗
const openDialog = (type, row = null) => {
  dialogType.value = type
  if (type === 'add') {
    dialogTitle.value = '新增景点'
    Object.assign(form, {
      id: null,
      name: '',
      category: '',
      description: '',
      stayTime: 30,
      address: '',
      longitude: 110.4345,
      latitude: 21.1485,
      maxCapacity: 500,
      status: 'open'
    })
  } else {
    dialogTitle.value = '编辑景点'
    Object.assign(form, {
      id: row.id,
      name: row.name,
      category: row.category,
      description: row.description || '',
      stayTime: row.stayTime || 30,
      address: row.address || '',
      longitude: row.longitude || 110.4345,
      latitude: row.latitude || 21.1485,
      maxCapacity: row.maxCapacity || 500,
      status: row.status || 'open'
    })
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const data = { ...form }
    
    if (dialogType.value === 'add') {
      const res = await addSpot(data)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        fetchSpots()
      }
    } else {
      const res = await updateSpot(data)
      if (res.code === 200) {
        ElMessage.success('编辑成功')
        dialogVisible.value = false
        fetchSpots()
      }
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 更新景点状态
const updateSpotStatus = async (row) => {
  try {
    const res = await updateSpot({
      id: row.id,
      status: row.status
    })
    if (res.code === 200) {
      ElMessage.success(`景点已${row.status === 'open' ? '开放' : '关闭'}`)
      fetchSpots()
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    // 恢复状态
    row.status = row.status === 'open' ? 'closed' : 'open'
    ElMessage.error('更新状态失败')
  }
}

// 删除景点
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除景点"${row.name}"吗？`, '提示', { type: 'warning' })
    const res = await deleteSpot(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchSpots()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const form = reactive({
  id: null,
  name: '',
  category: '',
  description: '',
  stayTime: 30,
  address: '',
  longitude: 110.4345,
  latitude: 21.1485,
  maxCapacity: 500,
  status: 'open'
})

const rules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入简介', trigger: 'blur' }]
}

const dialogTitle = ref('新增景点')

onMounted(() => {
  fetchSpots()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #0f1535 50%, #1a1f45 100%);
}

.dashboard-main {
  padding-top: 80px;
  padding-left: 30px;
  padding-right: 30px;
  padding-bottom: 30px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.2);
}

.admin-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #fff, #409eff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px 20px;
  flex-wrap: wrap;
  align-items: center;
}

.glass-card {
  background: rgba(15, 21, 53, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  border-color: rgba(64, 158, 255, 0.4);
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 入场动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  animation: fadeInUp 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

/* 现代化表格样式 */
.modern-table {
  width: 100%;
}

.modern-table :deep(.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(10, 14, 39, 0.95);
  --el-table-row-hover-bg-color: rgba(64, 158, 255, 0.08);
  --el-table-border-color: rgba(64, 158, 255, 0.08);
}

.modern-table :deep(.el-table__header-wrapper th) {
  font-weight: 600;
  font-size: 14px;
  color: #409eff;
  border-bottom: 2px solid rgba(64, 158, 255, 0.3);
  padding: 16px 0;
}

.modern-table :deep(.el-table__body-wrapper tr) {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.modern-table :deep(.el-table__body-wrapper tr:hover td) {
  background: rgba(64, 158, 255, 0.08) !important;
  transform: translateX(4px);
}

.modern-table :deep(.el-table td) {
  border-bottom: 1px solid rgba(64, 158, 255, 0.06);
  padding: 14px 0;
  color: rgba(255, 255, 255, 0.85);
}

.modern-table :deep(.el-table td:first-child),
.modern-table :deep(.el-table th:first-child) {
  padding-left: 20px;
}

.modern-table :deep(.el-table td:last-child),
.modern-table :deep(.el-table th:last-child) {
  padding-right: 20px;
}

/* 景点名称样式 */
.spot-name {
  font-weight: 500;
  transition: all 0.2s ease;
}

/* 分类标签 */
.category-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  transition: all 0.2s ease;
}

.category-tag.transport { background: rgba(64, 158, 255, 0.2); color: #409eff; }
.category-tag.culture { background: rgba(155, 89, 182, 0.2); color: #9b59b6; }
.category-tag.scenery { background: rgba(52, 152, 219, 0.2); color: #3498db; }
.category-tag.nature { background: rgba(39, 174, 96, 0.2); color: #27ae60; }
.category-tag.beach { background: rgba(230, 126, 34, 0.2); color: #e67e22; }
.category-tag.hotel { background: rgba(232, 67, 147, 0.2); color: #e84393; }

/* 时长标签 */
.duration-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(245, 166, 35, 0.15);
  border-radius: 20px;
  font-size: 12px;
  color: #f5a623;
}

/* 操作按钮 */
.action-btn {
  transition: all 0.2s ease !important;
}

.action-btn:hover {
  transform: translateX(2px);
}

.action-btn.danger:hover {
  color: #f56c6c !important;
}

/* 开关样式 */
.modern-switch :deep(.el-switch__core) {
  transition: all 0.2s ease;
}

.modern-switch.is-checked :deep(.el-switch__core) {
  background-color: #67c23a;
  border-color: #67c23a;
}

/* 输入框样式 */
.custom-input :deep(.el-input__wrapper) {
  background: rgba(15, 21, 53, 0.6);
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.2s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #409eff;
  transform: translateY(-1px);
}

/* 按钮样式 */
.modern-btn {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.modern-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

/* 分页样式 */
:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next),
:deep(.el-pagination .el-pager li) {
  background: rgba(15, 21, 53, 0.6);
  border: 1px solid rgba(64, 158, 255, 0.3);
  color: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.2s ease;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover),
:deep(.el-pagination .el-pager li:hover) {
  background: rgba(64, 158, 255, 0.2);
  transform: translateY(-1px);
}

:deep(.el-pagination .el-pager li.active) {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

/* 弹窗样式 */
.glass-dialog :deep(.el-dialog) {
  background: linear-gradient(135deg, #1a1f45, #0f1535);
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
}

.glass-dialog :deep(.el-dialog__title) {
  color: #409eff;
  font-weight: 600;
}

.glass-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s ease;
}

.glass-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #f56c6c;
  transform: rotate(90deg);
}
</style>