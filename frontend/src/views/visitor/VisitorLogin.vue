<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-clouds"></div>
    <div class="bg-spiral"></div>
    <div class="bg-gradient"></div>

    <!-- 登录卡片 -->
    <div class="login-container">
      <div class="login-card">
        <div class="card-header">
          <h1>智慧游客云平台</h1>
          <p>登录你的账户，开启智慧旅行</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <div class="input-wrapper">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input 
                v-model="form.username" 
                placeholder="用户名"
                size="large"
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码"
                size="large"
                show-password
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="remember">记住密码</el-checkbox>
            <el-button link type="primary" class="forgot-link">忘记密码？</el-button>
          </div>
          
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn" 
            @click="handleLogin" 
            :loading="loading"
          >
            <span>立即登录</span>
            <span class="btn-arrow">→</span>
          </el-button>
          
          <div class="register-link">
            还没有账号？
            <router-link to="/register" class="register-link-text">立即注册</router-link>
          </div>
        </el-form>
        
        <!-- 社交登录（装饰） -->
        <div class="social-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="social-icons">
            <div class="social-icon wechat">微信</div>
            <div class="social-icon weibo">微博</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 装饰元素 -->
    <div class="floating-element element-1">☁️</div>
    <div class="floating-element element-2">🌤️</div>
    <div class="floating-element element-3">✨</div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/visitor'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const remember = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 调用真实后端接口
    const res = await login({
      username: form.username,
      password: form.password
    })
    
    if (res.code === 200) {
      // 存储 token 和用户信息
      localStorage.setItem('visitorToken', res.data.token)
      localStorage.setItem('visitorUser', JSON.stringify(res.data.user))
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    // 错误已由 request.js 拦截器统一处理
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 背景渐变 */
.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0f4c81 0%, #1a6fb5 30%, #5ba3d9 60%, #c5e4f7 100%);
  z-index: -3;
}

/* 云雾背景 */
.bg-clouds {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2000 1200"><path fill="rgba(255,255,255,0.15)" d="M0,800 L200,750 L400,780 L600,720 L800,760 L1000,700 L1200,740 L1400,690 L1600,730 L1800,670 L2000,710 L2000,1200 L0,1200 Z"/><path fill="rgba(255,255,255,0.1)" d="M0,850 L250,820 L500,860 L750,800 L1000,840 L1250,790 L1500,830 L1750,780 L2000,820 L2000,1200 L0,1200 Z"/><path fill="rgba(255,255,255,0.08)" d="M0,900 L300,880 L600,920 L900,860 L1200,900 L1500,850 L1800,890 L2000,840 L2000,1200 L0,1200 Z"/></svg>');
  background-size: cover;
  background-position: bottom;
  background-repeat: no-repeat;
  z-index: -2;
}

/* 螺旋云纹 */
.bg-spiral {
  position: absolute;
  top: 0;
  right: 0;
  width: 60%;
  height: 100%;
  background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800"><path fill="none" stroke="rgba(255,255,255,0.12)" stroke-width="3" d="M400,400 Q500,300 550,400 Q600,500 500,550 Q400,600 350,500 Q300,400 400,350 Q500,300 550,380"/><path fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="2" d="M400,400 Q550,250 620,380 Q690,510 550,580 Q410,650 340,520 Q270,390 410,320 Q550,250 630,360"/><path fill="none" stroke="rgba(255,255,255,0.06)" stroke-width="1.5" d="M400,400 Q600,200 700,380 Q800,560 600,650 Q400,740 300,560 Q200,380 400,290 Q600,200 700,370"/></svg>');
  background-size: contain;
  background-position: center right;
  background-repeat: no-repeat;
  z-index: -1;
  animation: spiralFloat 20s ease-in-out infinite;
}

/* 漂浮元素 */
.floating-element {
  position: absolute;
  font-size: 32px;
  opacity: 0.3;
  pointer-events: none;
  animation: float 6s ease-in-out infinite;
}

.element-1 {
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.element-2 {
  bottom: 20%;
  right: 12%;
  animation-delay: 2s;
  font-size: 40px;
}

.element-3 {
  top: 40%;
  right: 25%;
  animation-delay: 4s;
  font-size: 24px;
}

/* 登录卡片 */
.login-container {
  width: 100%;
  max-width: 460px;
  padding: 20px;
  z-index: 5;
}

.login-card {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-radius: 32px;
  padding: 48px 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  transition: all 0.4s ease;
  animation: fadeInUp 0.6s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.3);
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.card-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: #0f4c81;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.card-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 输入框样式 */
.input-wrapper {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 18px;
  z-index: 1;
  pointer-events: none;
}

.custom-input :deep(.el-input__wrapper) {
  padding-left: 44px;
  border-radius: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: none;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #3b82f6;
  background: white;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.custom-input :deep(.el-input__inner) {
  font-size: 15px;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.form-options :deep(.el-checkbox) {
  color: #6b7280;
}

.forgot-link {
  color: #3b82f6;
  font-size: 13px;
}

.forgot-link:hover {
  color: #2563eb;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #0f4c81, #1e6cb5);
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #1e6cb5, #2d89d6);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(15, 76, 129, 0.3);
}

.login-btn:hover::before {
  left: 100%;
}

.btn-arrow {
  transition: transform 0.3s ease;
}

.login-btn:hover .btn-arrow {
  transform: translateX(5px);
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #6b7280;
}

.register-link-text {
  color: #3b82f6;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.3s ease;
}

.register-link-text:hover {
  color: #2563eb;
}

/* 社交登录 */
.social-login {
  margin-top: 32px;
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.divider span {
  background: white;
  padding: 0 15px;
  font-size: 12px;
  color: #9ca3af;
  position: relative;
  z-index: 1;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e5e7eb;
  transform: translateY(-50%);
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.social-icon {
  width: 100px;
  padding: 10px;
  text-align: center;
  background: #f9fafb;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #6b7280;
}

.social-icon.wechat:hover {
  background: #07c160;
  color: white;
  transform: translateY(-2px);
}

.social-icon.weibo:hover {
  background: #ff8200;
  color: white;
  transform: translateY(-2px);
}

/* 动画 */
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

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes spiralFloat {
  0%, 100% {
    transform: translateX(0) scale(1);
  }
  50% {
    transform: translateX(20px) scale(1.02);
  }
}

/* 响应式 */
@media (max-width: 600px) {
  .login-card {
    padding: 32px 24px;
  }
  
  .card-header h1 {
    font-size: 20px;
  }
  
  .bg-spiral {
    opacity: 0.5;
  }
}
</style>