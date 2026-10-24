<template>
  <div class="header">
    <div class="header-container">
      <div class="logo" @click="goHome">
        <span class="icon">🏞️</span>
        <span class="logo-text">智慧游客云平台</span>
      </div>
      <div class="nav">
        <router-link to="/" class="nav-item" active-class="active">
          <span class="nav-icon">🏠</span>
          <span class="nav-text">首页</span>
          <span class="nav-indicator"></span>
        </router-link>
        <router-link to="/ticket" class="nav-item" active-class="active">
          <span class="nav-icon">🎫</span>
          <span class="nav-text">在线购票</span>
          <span class="nav-indicator"></span>
        </router-link>
        <router-link to="/route-plan" class="nav-item" active-class="active">
          <span class="nav-icon">🗺️</span>
          <span class="nav-text">路线规划</span>
          <span class="nav-indicator"></span>
        </router-link>
        <router-link to="/guide" class="nav-item" active-class="active">
          <span class="nav-icon">📍</span>
          <span class="nav-text">景区导览</span>
          <span class="nav-indicator"></span>
        </router-link>
        <template v-if="isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
            <div class="nav-item user-btn">
              <span class="nav-icon">👤</span>
              <span class="nav-text">{{ userInfo?.nickname || userInfo?.username || '个人中心' }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              <span class="nav-indicator"></span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
                <el-dropdown-item command="orders"><el-icon><Tickets /></el-icon>我的订单</el-dropdown-item>
                <el-dropdown-item command="routes"><el-icon><Location /></el-icon>我的路线</el-dropdown-item>
                <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-item login-btn" active-class="active">
            <span class="nav-icon">🔑</span>
            <span class="nav-text">登录</span>
            <span class="nav-indicator"></span>
          </router-link>
          <router-link to="/register" class="nav-item register-btn" active-class="active">
            <span class="nav-icon">📝</span>
            <span class="nav-text">注册</span>
            <span class="nav-indicator"></span>
          </router-link>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Tickets, Location, SwitchButton, ArrowDown } from '@element-plus/icons-vue'
import { getUserInfo, logout } from '@/api/visitor'

const router = useRouter()
const userInfo = ref(null)

// 检查是否已登录：token 存在且不是 mock 值
const isLoggedIn = computed(() => {
  const token = localStorage.getItem('visitorToken')
  return token && !token.startsWith('mock-')
})

const goHome = () => router.push('/')

const fetchUserInfo = async () => {
  const token = localStorage.getItem('visitorToken')
  
  // 如果没有 token 或 token 是 mock 的，直接返回
  if (!token || token.startsWith('mock-')) {
    // 清除无效的 mock 数据
    if (token && token.startsWith('mock-')) {
      localStorage.removeItem('visitorToken')
      localStorage.removeItem('visitorUser')
    }
    return
  }

  // 先从 localStorage 读取缓存的用户信息
  const stored = localStorage.getItem('visitorUser')
  if (stored) {
    try {
      userInfo.value = JSON.parse(stored)
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }

  // 调用后端获取最新用户信息
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
      localStorage.setItem('visitorUser', JSON.stringify(res.data))
    } else {
      // 如果返回不是 200，清除 token
      localStorage.removeItem('visitorToken')
      localStorage.removeItem('visitorUser')
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    // 如果是 401 未登录，清除 token
    if (error.response?.status === 401) {
      localStorage.removeItem('visitorToken')
      localStorage.removeItem('visitorUser')
      userInfo.value = null
    }
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await logout()
    } catch (error) {
      console.error('退出登录失败', error)
    }
    localStorage.removeItem('visitorToken')
    localStorage.removeItem('visitorUser')
    userInfo.value = null
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'orders') {
    router.push('/orders')
  } else if (command === 'routes') {
    router.push('/routes')
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.95) 0%, rgba(22, 30, 62, 0.95) 50%, rgba(15, 52, 96, 0.95) 100%);
  backdrop-filter: blur(10px);
  color: white;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 22px;
  font-weight: bold;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.logo .icon {
  font-size: 32px;
  animation: pulse 2s ease-in-out infinite;
}

.logo-text {
  background: linear-gradient(135deg, #fff, #a8c0ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 40px;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  cursor: pointer;
}

.nav-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.nav-text {
  transition: transform 0.3s ease;
}

.nav-item:hover {
  color: white;
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.nav-item:hover .nav-icon {
  transform: scale(1.15) rotate(5deg);
}

.nav-item:hover .nav-text {
  transform: translateX(2px);
}

.nav-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  border-radius: 3px;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.nav-item:hover .nav-indicator,
.nav-item.active .nav-indicator {
  width: 60%;
}

.nav-item.active {
  color: white;
  background: rgba(102, 126, 234, 0.3);
  box-shadow: 0 0 15px rgba(102, 126, 234, 0.3);
}

.login-btn, .register-btn {
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-btn:hover, .register-btn:hover {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.2);
}

.register-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.user-btn {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 40px;
}

.arrow-icon {
  transition: transform 0.3s ease;
}

.user-btn:hover .arrow-icon {
  transform: rotate(180deg);
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}

@media (max-width: 900px) {
  .header-container { padding: 0 15px; }
  .nav-text { display: none; }
  .nav-item { padding: 10px 14px; }
  .nav-icon { font-size: 20px; }
}
</style>