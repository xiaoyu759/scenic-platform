<template>
  <div class="admin-header">
    <div class="logo-area" @click="goHome">
      <span class="logo-icon">🏝️</span>
      <span class="logo-text">特呈岛智慧景区管理平台</span>
    </div>
    <div class="nav-area">
      <router-link to="/admin/dashboard" class="nav-link" active-class="active">
        <el-icon><Monitor /></el-icon>
        <span>实时监控</span>
      </router-link>
      <router-link to="/admin/analysis" class="nav-link" active-class="active">
        <el-icon><DataAnalysis /></el-icon>
        <span>数据分析</span>
      </router-link>
      <router-link to="/admin/spots" class="nav-link" active-class="active">
        <el-icon><Location /></el-icon>
        <span>景点管理</span>
      </router-link>
      <router-link to="/admin/orders" class="nav-link" active-class="active">
        <el-icon><Tickets /></el-icon>
        <span>订单管理</span>
      </router-link>
      <router-link to="/admin/setting" class="nav-link" active-class="active">
        <el-icon><Setting /></el-icon>
        <span>景区设置</span>
      </router-link>
    </div>
    <div class="user-area">
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span>管理员</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor, DataAnalysis, Location, Tickets, Setting, UserFilled, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => router.push('/admin/dashboard')

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminUser')
    ElMessage.success('已退出登录')
    router.push('/admin')
  }
}
</script>

<style scoped>
.admin-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(10, 14, 39, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  z-index: 100;
  border-bottom: 1px solid rgba(64, 158, 255, 0.2);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-icon {
  font-size: 28px;
  animation: float 3s ease-in-out infinite;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #fff, #409eff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.nav-area {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background: rgba(64, 158, 255, 0.2);
  color: #409eff;
  transform: translateY(-2px);
}

.nav-link.active {
  background: rgba(64, 158, 255, 0.3);
  color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.user-area {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}
</style>