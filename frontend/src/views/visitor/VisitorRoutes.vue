<template>
  <div>
    <VisitorHeader />
    <div class="routes-container">
      <div class="routes-wrapper">
        <h1>我的路线</h1>
        
        <div v-if="loading" class="loading">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="routes.length === 0" class="empty">
          <el-empty description="暂无保存的路线" />
          <el-button type="primary" @click="$router.push('/route-plan')">去规划路线</el-button>
        </div>
        
        <div v-else class="routes-list">
          <div v-for="route in routes" :key="route.id" class="route-card">
            <div class="route-header">
              <div class="route-info">
                <span class="route-name">{{ route.name }}</span>
                <span class="route-date">{{ route.createTime }}</span>
              </div>
              <el-button type="danger" size="small" plain @click="deleteRoute(route.id)">删除</el-button>
            </div>
            <div class="route-content">
              <div class="route-stats">
                <span>⏱️ {{ route.totalDuration }}分钟</span>
                <span>📏 {{ route.totalDistance }}米</span>
                <span>📍 {{ route.spotCount }}个景点</span>
              </div>
              <div class="route-spots">
                <span v-for="(spot, idx) in route.spots" :key="idx" class="spot-tag">
                  {{ idx + 1 }}. {{ spot.name }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <VisitorFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import VisitorHeader from '@/components/VisitorHeader.vue'
import VisitorFooter from '@/components/VisitorFooter.vue'
import { getRouteList, deleteRoute as deleteRouteApi } from '@/api/visitor'

const loading = ref(false)
const routes = ref([])

const fetchRoutes = async () => {
  loading.value = true
  try {
    const res = await getRouteList()
    console.log('路线列表原始响应:', res)  // 调试日志
    if (res.code === 200) {
      routes.value = (res.data || []).map(item => ({
        id: item.id,
        name: item.name,
        createTime: item.createdAt || item.createTime,
        totalDuration: item.totalTime || item.totalDuration,  // ⭐ 关键映射
        totalDistance: item.totalDistance,
        spotCount: item.spotCount || (item.spots ? item.spots.length : 0),
        spots: item.spots || []
      }))
      console.log('映射后的路线列表:', routes.value)  // 调试日志
    } else {
      ElMessage.error(res.message || '获取路线列表失败')
    }
  } catch (error) {
    console.error('获取路线列表失败:', error)
    ElMessage.error('获取路线列表失败')
  } finally {
    loading.value = false
  }
}

const deleteRoute = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条路线吗？', '提示', { type: 'warning' })
    const res = await deleteRouteApi(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await fetchRoutes()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchRoutes()
})
</script>

<style scoped>
.routes-container {
  min-height: calc(100vh - 200px);
  background: #f5f7fa;
  padding: 40px 20px;
}

.routes-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 30px;
}

.routes-wrapper h1 {
  margin-bottom: 20px;
}

.loading, .empty {
  padding: 40px;
  text-align: center;
}

.routes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.route-card {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
}

.route-header {
  background: #f5f7fa;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
}

.route-name {
  font-weight: bold;
  color: #333;
}

.route-date {
  font-size: 12px;
  color: #999;
  margin-left: 16px;
}

.route-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #666;
}

.route-spots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spot-tag {
  font-size: 12px;
  color: #333;
  background: #f0f4f9;
  padding: 4px 10px;
  border-radius: 12px;
}
</style>