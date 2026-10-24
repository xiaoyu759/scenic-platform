<template>
  <div class="dashboard">
    <AdminHeader />
    
    <div class="dashboard-main">
      <div class="admin-header">
        <h2>订单管理</h2>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar glass-card fade-in-up">
        <el-input
          v-model="searchKeyword"
          placeholder="订单号/购票人/手机号"
          prefix-icon="Search"
          clearable
          class="custom-input"
          style="width: 220px"
          @clear="fetchOrders"
          @keyup.enter="fetchOrders"
        />
        <el-select v-model="statusFilter" placeholder="订单状态" clearable class="custom-input" style="width: 120px" @change="fetchOrders">
          <el-option label="全部" value="" />
          <el-option label="待使用" value="pending" />
          <el-option label="已支付" value="paid" />
          <el-option label="已使用" value="used" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="custom-input"
          @change="fetchOrders"
        />
        <el-button type="primary" @click="fetchOrders" class="modern-btn">查询</el-button>
        <el-button @click="resetFilters" class="modern-btn">重置</el-button>
      </div>

      <!-- 订单列表 -->
      <div class="glass-card fade-in-up" style="animation-delay: 0.1s; padding: 0; overflow: hidden;">
        <el-table 
          :data="orders" 
          v-loading="loading"
          class="modern-table"
          :row-style="rowStyle"
          :cell-style="cellStyle"
          @cell-mouse-enter="handleCellMouseEnter"
          @cell-mouse-leave="handleCellMouseLeave"
        >
          <el-table-column prop="orderNo" label="订单号" width="180" sortable>
            <template #default="{ row }">
              <span class="order-no">{{ row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="ticketName" label="票种" width="120" />
          <el-table-column prop="quantity" label="数量" width="80" sortable>
            <template #default="{ row }">
              <span class="stat-number">{{ row.quantity }}张</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalPrice" label="金额" width="100" sortable>
            <template #default="{ row }">
              <span class="stat-number revenue">¥{{ row.totalPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="visitorName" label="购票人" width="100" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column prop="visitDate" label="游玩日期" width="120" sortable />
          <el-table-column prop="createdAt" label="下单时间" width="160" sortable>
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span class="status-badge" :class="row.status">{{ getStatusText(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="viewDetail(row)" class="action-btn">详情</el-button>
              <el-button 
                v-if="row.status === 'pending' || row.status === 'paid'"
                type="success" 
                link 
                size="small" 
                @click="confirmUse(row)"
                class="action-btn success"
              >核销</el-button>
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchOrders"
          @current-change="fetchOrders"
        />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="500px" class="glass-dialog">
      <div v-if="currentOrder" class="order-detail" v-loading="detailLoading">
        <div class="detail-item">
          <span class="detail-label">订单号</span>
          <span class="detail-value">{{ currentOrder.orderNo }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">订单状态</span>
          <span class="status-badge" :class="currentOrder.status">{{ getStatusText(currentOrder.status) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">票种</span>
          <span class="detail-value">{{ currentOrder.ticketName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">数量</span>
          <span class="detail-value">{{ currentOrder.quantity }}张</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">总金额</span>
          <span class="detail-value revenue">¥{{ currentOrder.totalPrice }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">游玩日期</span>
          <span class="detail-value">{{ currentOrder.visitDate }}</span>
        </div>
        <div class="detail-item" v-if="currentOrder.visitorName">
          <span class="detail-label">购票人</span>
          <span class="detail-value">{{ currentOrder.visitorName }}</span>
        </div>
        <div class="detail-item" v-if="currentOrder.idCard">
          <span class="detail-label">身份证号</span>
          <span class="detail-value">{{ currentOrder.idCard }}</span>
        </div>
        <div class="detail-item" v-if="currentOrder.phone">
          <span class="detail-label">手机号</span>
          <span class="detail-value">{{ currentOrder.phone }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">下单时间</span>
          <span class="detail-value">{{ formatDateTime(currentOrder.createdAt) }}</span>
        </div>
        <div class="detail-item" v-if="currentOrder.visitors && currentOrder.visitors.length > 0">
          <span class="detail-label">游客信息</span>
          <div class="detail-value" style="text-align: right;">
            <div v-for="(v, idx) in currentOrder.visitors" :key="idx" style="font-size: 13px; margin-bottom: 4px;">
              {{ v.name }} ({{ v.phone }})
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false" class="modern-btn">关闭</el-button>
        <el-button 
          v-if="currentOrder?.status === 'pending' || currentOrder?.status === 'paid'" 
          type="primary" 
          @click="useTicket" 
          class="modern-btn"
          :loading="updating"
        >确认核销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminHeader from '@/components/AdminHeader.vue'
import { getOrderManageList, updateOrderStatus, getOrderDetail } from '@/api/admin'

const loading = ref(false)
const detailLoading = ref(false)
const updating = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const dateRange = ref([])
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orders = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)

// 状态映射
const statusMap = {
  pending: '待使用',
  paid: '已支付',
  used: '已使用',
  cancelled: '已取消'
}

const getStatusText = (status) => statusMap[status] || status

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 行样式
const rowStyle = ({ row, rowIndex }) => {
  return {
    transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
    cursor: 'pointer'
  }
}

// 单元格样式
const cellStyle = ({ row, column, rowIndex, columnIndex }) => {
  return {
    padding: '14px 0',
    transition: 'all 0.2s ease'
  }
}

// 单元格悬停效果
const handleCellMouseEnter = (row, column, cell, event) => {
  cell.style.backgroundColor = 'rgba(64, 158, 255, 0.08)'
}

const handleCellMouseLeave = (row, column, cell, event) => {
  cell.style.backgroundColor = 'transparent'
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  dateRange.value = []
  pageNo.value = 1
  fetchOrders()
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (statusFilter.value) params.status = statusFilter.value
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = formatDate(dateRange.value[0])
      params.endDate = formatDate(dateRange.value[1])
    }
    
    const res = await getOrderManageList(params)
    if (res.code === 200) {
      orders.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error(error.response?.data?.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 查看订单详情
const viewDetail = async (order) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getOrderDetail(order.id)
    if (res.code === 200) {
      currentOrder.value = res.data
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error(error.response?.data?.message || '获取订单详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 确认核销
const confirmUse = async (order) => {
  try {
    await ElMessageBox.confirm(`确认核销订单 ${order.orderNo} 吗？`, '核销确认', { 
      type: 'info',
      confirmButtonText: '确认核销',
      cancelButtonText: '取消'
    })
    await doUseTicket(order.id)
  } catch (error) {
    // 用户取消
  }
}

// 执行核销
const doUseTicket = async (orderId) => {
  updating.value = true
  try {
    const res = await updateOrderStatus(orderId, 'used')
    if (res.code === 200) {
      ElMessage.success('核销成功')
      detailVisible.value = false
      await fetchOrders()
    } else {
      ElMessage.error(res.message || '核销失败')
    }
  } catch (error) {
    console.error('核销失败:', error)
    ElMessage.error(error.response?.data?.message || '核销失败')
  } finally {
    updating.value = false
  }
}

// 弹窗中的核销按钮
const useTicket = async () => {
  if (currentOrder.value) {
    await doUseTicket(currentOrder.value.id)
  }
}

onMounted(() => {
  fetchOrders()
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

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
  padding: 16px 20px;
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

/* 订单号样式 */
.order-no {
  font-family: monospace;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 统计数字样式 */
.stat-number {
  font-weight: 500;
}

.stat-number.revenue {
  color: #f5a623;
  font-weight: 600;
}

/* 状态标签 */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  transition: all 0.2s ease;
}

.status-badge.pending {
  background: rgba(230, 162, 60, 0.2);
  color: #e6a23c;
}

.status-badge.paid {
  background: rgba(64, 158, 255, 0.2);
  color: #409eff;
}

.status-badge.used {
  background: rgba(103, 194, 58, 0.2);
  color: #67c23a;
}

.status-badge.cancelled {
  background: rgba(245, 108, 108, 0.2);
  color: #f56c6c;
}

/* 操作按钮 */
.action-btn {
  transition: all 0.2s ease !important;
}

.action-btn:hover {
  transform: translateX(2px);
}

.action-btn.success:hover {
  color: #67c23a !important;
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

/* 订单详情样式 */
.order-detail {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 600;
  color: #409eff;
  width: 100px;
  flex-shrink: 0;
}

.detail-value {
  color: rgba(255, 255, 255, 0.85);
  flex: 1;
  text-align: right;
}

.detail-value.revenue {
  color: #f5a623;
  font-weight: 600;
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