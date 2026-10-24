<template>
  <div>
    <VisitorHeader />
    <div class="orders-container">
      <div class="orders-wrapper">
        <h1>我的订单</h1>
        
        <el-tabs v-model="activeTab" @tab-change="fetchOrders">
          <el-tab-pane label="全部订单" name="all" />
          <el-tab-pane label="待使用" name="pending" />
          <el-tab-pane label="已支付" name="paid" />
          <el-tab-pane label="已使用" name="used" />
          <el-tab-pane label="已取消" name="cancelled" />
        </el-tabs>

        <div v-if="loading" class="loading">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="orders.length === 0" class="empty">
          <el-empty description="暂无订单" />
          <el-button type="primary" @click="$router.push('/ticket')">去购票</el-button>
        </div>
        
        <div v-else class="orders-list">
          <div v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">订单号：{{ order.orderNo }}</span>
                <span class="order-date">{{ formatDate(order.createTime) }}</span>
              </div>
              <div class="order-status" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </div>
            </div>
            
            <div class="order-content">
              <div class="ticket-info">
                <div class="ticket-name">{{ order.ticketName }}</div>
                <div class="ticket-count">x{{ order.quantity }}</div>
              </div>
              <div class="order-detail">
                <div>游玩日期：{{ order.visitDate }}</div>
                <div>购票人：{{ order.name }}</div>
              </div>
              <div class="order-amount">
                实付：¥{{ order.totalAmount }}
              </div>
            </div>
            
            <div class="order-footer" v-if="order.status === 'pending'">
              <el-button size="small" type="danger" plain @click="cancelOrderHandler(order.id)">取消订单</el-button>
              <el-button size="small" type="primary" @click="viewOrderDetail(order)">查看详情</el-button>
            </div>
            <div class="order-footer" v-else>
              <el-button size="small" type="primary" plain @click="viewOrderDetail(order)">查看详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <VisitorFooter />

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <div v-if="currentOrder" class="order-detail-dialog">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ getStatusText(currentOrder.status) }}</el-descriptions-item>
          <el-descriptions-item label="票种">{{ currentOrder.ticketName }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ currentOrder.quantity }}张</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="游玩日期">{{ currentOrder.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="购票人">{{ currentOrder.name }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentOrder.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(currentOrder.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentOrder?.status === 'pending'" type="primary" @click="useTicket">使用门票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import VisitorHeader from '@/components/VisitorHeader.vue'
import VisitorFooter from '@/components/VisitorFooter.vue'
import { getOrderList, cancelOrder as cancelOrderApi } from '@/api/visitor'
import { useTicket as useTicketApi } from '@/api/visitor'


const activeTab = ref('all')
const orders = ref([])
const loading = ref(false)
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

const getStatusClass = (status) => {
  const map = { 
    pending: 'status-pending', 
    paid: 'status-paid', 
    used: 'status-used', 
    cancelled: 'status-cancelled' 
  }
  return map[status]
}

// 格式化日期
const formatDate = (dateStr) => {
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

// 获取订单列表 - 调用真实后端接口
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {}
    if (activeTab.value !== 'all') {
      params.status = activeTab.value
    }
    
    const res = await getOrderList(params)
    if (res.code === 200) {
      // 后端返回的数据直接赋值，字段名已在接口中匹配
      orders.value = res.data || []
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

// 取消订单 - 调用真实后端接口
const cancelOrderHandler = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', { type: 'warning' })
    const res = await cancelOrderApi(id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      await fetchOrders()
    } else {
      ElMessage.error(res.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '取消订单失败')
    }
  }
}

const viewOrderDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

// 使用门票（核销）
const useTicket = async () => {
  if (!currentOrder.value) return
  
  // 检查订单状态
  if (currentOrder.value.status !== 'pending' && currentOrder.value.status !== 'paid') {
    ElMessage.warning('该订单无法使用')
    return
  }
  
  try {
    const res = await useTicketApi(currentOrder.value.id)
    if (res.code === 200) {
      ElMessage.success('门票已核销，祝您游玩愉快！')
      detailVisible.value = false
      await fetchOrders()
    } else {
      ElMessage.error(res.message || '使用门票失败')
    }
  } catch (error) {
    console.error('使用门票失败:', error)
    ElMessage.error(error.response?.data?.message || '使用门票失败')
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
  min-height: calc(100vh - 200px);
  background: #f5f7fa;
  padding: 40px 20px;
}

.orders-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 30px;
}

.orders-wrapper h1 {
  margin-bottom: 20px;
}

.loading, .empty {
  padding: 40px;
  text-align: center;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
}

.order-header {
  background: #f5f7fa;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
}

.order-no {
  font-weight: bold;
  color: #333;
}

.order-date {
  font-size: 12px;
  color: #999;
  margin-left: 16px;
}

.order-status {
  font-size: 14px;
  font-weight: bold;
}

.status-pending { color: #e6a23c; }
.status-paid { color: #409eff; }
.status-used { color: #67c23a; }
.status-cancelled { color: #f56c6c; }

.order-content {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.ticket-info {
  display: flex;
  gap: 16px;
  align-items: baseline;
}

.ticket-name {
  font-weight: bold;
  font-size: 16px;
}

.ticket-count {
  color: #666;
}

.order-detail {
  color: #666;
  font-size: 13px;
}

.order-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.order-footer {
  padding: 12px 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #fafafa;
}

.order-detail-dialog {
  padding: 10px 0;
}
</style>