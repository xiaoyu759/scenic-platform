<template>
  <div class="admin-sidebar">
    <div class="logo-area">
      <span class="icon">🏝️</span>
      <span>特呈岛智慧景区</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="#0a0e27"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-menu-item index="/admin/dashboard">
        <el-icon><Monitor /></el-icon>
        <span>实时监控</span>
      </el-menu-item>
      <el-menu-item index="/admin/analysis">
        <el-icon><DataAnalysis /></el-icon>
        <span>数据分析</span>
      </el-menu-item>
      <el-menu-item index="/admin/spots">
        <el-icon><Location /></el-icon>
        <span>景点管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/orders">
        <el-icon><Tickets /></el-icon>
        <span>订单管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/setting">
        <el-icon><Setting /></el-icon>
        <span>景区设置</span>
      </el-menu-item>
    </el-menu>
    <div class="logout-area">
      <el-button type="danger" plain @click="handleLogout" style="width: 100%">
        <el-icon><SwitchButton /></el-icon>
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor, DataAnalysis, Location, Tickets, Setting, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminUser')
  ElMessage.success('已退出登录')
  router.push('/admin')
}
</script>

<style scoped>
.admin-sidebar {
  width: 240px;
  height: 100vh;
  background: linear-gradient(180deg, #0a0e27 0%, #0f1535 100%);
  position: fixed;
  left: 0;
  top: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(64, 158, 255, 0.2);
  z-index: 100;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 16px;
  font-weight: bold;
  color: white;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(15, 76, 129, 0.2));
  border-bottom: 1px solid rgba(64, 158, 255, 0.3);
}

.logo-area .icon {
  font-size: 28px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  margin: 4px 12px;
  border-radius: 8px;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(64, 158, 255, 0.2);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.3), rgba(15, 76, 129, 0.3));
  border-left: 3px solid #409eff;
}

.logout-area {
  padding: 20px;
  border-top: 1px solid rgba(64, 158, 255, 0.2);
}

.logout-area .el-button {
  background: rgba(15, 21, 53, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.3);
  color: rgba(255, 255, 255, 0.8);
}

.logout-area .el-button:hover {
  background: rgba(245, 108, 108, 0.2);
  border-color: #f56c6c;
  color: #f56c6c;
}
</style>