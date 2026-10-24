<template>
  <div class="admin-login-page">
    <div class="bg-animation">
      <div class="bg-gradient"></div>
      <div class="floating-cloud cloud-1"></div>
      <div class="floating-cloud cloud-2"></div>
      <div class="floating-cloud cloud-3"></div>
    </div>

    <div class="login-container">
      <div class="illustration-section">
        <div class="illustration-inner">
          <div class="cloud-3d">
            <div class="cloud-body">
              <div class="cloud-part cloud-1"></div>
              <div class="cloud-part cloud-2"></div>
              <div class="cloud-part cloud-3"></div>
              <div class="cloud-part cloud-4"></div>
              <div class="cloud-part cloud-5"></div>
            </div>
            <div class="play-symbol">
              <div class="play-icon">▶</div>
              <div class="pulse-ring"></div>
            </div>
            <div class="platform-base">
              <div class="base-layer layer-1"></div>
              <div class="base-layer layer-2"></div>
              <div class="base-layer layer-3"></div>
            </div>
          </div>

          <div class="building left-pavilion">
            <div class="pavilion-roof"></div>
            <div class="pavilion-body">
              <div class="pavilion-columns">
                <div class="column"></div>
                <div class="column"></div>
                <div class="column"></div>
              </div>
            </div>
            <div class="pavilion-base"></div>
          </div>

          <div class="building right-tower">
            <div class="tower-roof"></div>
            <div class="tower-body">
              <div class="tower-window"></div>
              <div class="tower-window"></div>
              <div class="tower-window"></div>
            </div>
            <div class="tower-base"></div>
          </div>

          <div class="bus">
            <div class="bus-body">
              <div class="bus-window"></div>
              <div class="bus-window"></div>
              <div class="bus-window"></div>
              <div class="bus-door"></div>
            </div>
            <div class="bus-wheels">
              <div class="wheel"></div>
              <div class="wheel"></div>
            </div>
            <div class="bus-headlight"></div>
          </div>

          <div class="floating-icons">
            <div class="float-icon location">
              <el-icon><Location /></el-icon>
              <span class="float-label">地图</span>
            </div>
            <div class="float-icon message">
              <el-icon><ChatDotRound /></el-icon>
              <span class="float-label">通知</span>
            </div>
            <div class="float-icon check">
              <el-icon><Select /></el-icon>
              <span class="float-label">审核</span>
            </div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="form-card">
          <div class="card-header">
            <div class="logo-icon">🏝️</div>
            <h1>特呈岛智慧景区管理系统</h1>
            <p class="subtitle">管理端登录</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input 
                  v-model="form.username" 
                  placeholder="管理员账号"
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
          </el-form>

          <div class="divider">
            <span>特呈岛智慧景区</span>
          </div>

          <div class="footer-tip">
            <p>© 2026 特呈岛智慧景区 | 管理平台</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Location, ChatDotRound, Select } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/admin'

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
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
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
    const res = await adminLogin({
      username: form.username,
      password: form.password
    })
    
    if (res.code === 200) {
      localStorage.setItem('adminToken', res.data.token)
      localStorage.setItem('adminUser', JSON.stringify(res.data.user))
      ElMessage.success('登录成功')
      router.replace('/admin/dashboard')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, #0a1628 0%, #0f2b4d 30%, #1a3a5c 60%, #2a5a8c 100%);
  z-index: -3;
}

.floating-cloud {
  position: absolute;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 1000px;
  backdrop-filter: blur(4px);
  animation: floatCloud 20s ease-in-out infinite;
}

.cloud-1 { width: 200px; height: 80px; top: 10%; left: -50px; }
.cloud-2 { width: 150px; height: 60px; bottom: 15%; right: -30px; animation-delay: -5s; }
.cloud-3 { width: 120px; height: 50px; top: 30%; right: 20%; animation-delay: -10s; }

@keyframes floatCloud {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(30px); }
}

.login-container {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 100vh;
}

.illustration-section {
  flex: 1.2;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.illustration-inner {
  position: relative;
  width: 100%;
  max-width: 550px;
  height: 550px;
}

.cloud-3d {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -45%);
  width: 280px;
  height: 180px;
  filter: drop-shadow(0 20px 30px rgba(0, 0, 0, 0.2));
  animation: cloudFloat 6s ease-in-out infinite;
}

@keyframes cloudFloat {
  0%, 100% { transform: translate(-50%, -45%); }
  50% { transform: translate(-50%, -50%); }
}

.cloud-body { position: relative; width: 100%; height: 100%; }
.cloud-part {
  position: absolute;
  background: linear-gradient(145deg, #4a9eff, #2d6dcf);
  border-radius: 50%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}
.cloud-part.cloud-1 { width: 120px; height: 120px; bottom: 20px; left: 20px; }
.cloud-part.cloud-2 { width: 100px; height: 100px; bottom: 40px; left: 100px; }
.cloud-part.cloud-3 { width: 90px; height: 90px; bottom: 50px; left: 170px; }
.cloud-part.cloud-4 { width: 70px; height: 70px; top: 20px; left: 60px; }
.cloud-part.cloud-5 { width: 60px; height: 60px; top: 30px; left: 150px; }

.play-symbol {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  cursor: pointer;
  box-shadow: 0 0 20px rgba(74, 158, 255, 0.5);
}
.play-icon { color: #2d6dcf; font-size: 18px; margin-left: 3px; }

.platform-base {
  position: absolute;
  bottom: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 320px;
  text-align: center;
}
.base-layer {
  height: 8px;
  background: linear-gradient(90deg, transparent, #4a9eff, #7cb8ff, #4a9eff, transparent);
  margin: 4px 0;
  border-radius: 4px;
  animation: basePulse 3s ease-in-out infinite;
}
.layer-1 { width: 100%; }
.layer-2 { width: 80%; margin: 0 auto; }
.layer-3 { width: 60%; margin: 0 auto; }

@keyframes basePulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.building { position: absolute; bottom: 80px; }
.left-pavilion { left: 20px; animation: buildingFloat 5s ease-in-out infinite; }
.right-tower { right: 20px; bottom: 80px; animation: buildingFloat 5s ease-in-out infinite 1s; }

@keyframes buildingFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.pavilion-roof {
  width: 80px;
  height: 30px;
  background: linear-gradient(135deg, #5a9eff, #3a7edf);
  clip-path: polygon(0% 100%, 50% 0%, 100% 100%);
}
.pavilion-body { width: 60px; background: rgba(74, 158, 255, 0.3); backdrop-filter: blur(4px); border-radius: 0 0 8px 8px; margin-left: 10px; }
.pavilion-columns { display: flex; justify-content: space-around; padding: 8px 4px; }
.column { width: 4px; height: 25px; background: rgba(255, 255, 255, 0.6); border-radius: 2px; }
.pavilion-base { width: 80px; height: 6px; background: linear-gradient(90deg, transparent, #4a9eff, transparent); border-radius: 3px; }

.tower-roof {
  width: 50px;
  height: 40px;
  background: linear-gradient(135deg, #5a9eff, #3a7edf);
  clip-path: polygon(0% 100%, 50% 0%, 100% 100%);
  margin-left: 15px;
}
.tower-body {
  width: 80px;
  background: rgba(74, 158, 255, 0.3);
  backdrop-filter: blur(4px);
  border-radius: 8px;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.tower-window { width: 20px; height: 20px; background: rgba(255, 255, 255, 0.4); border-radius: 4px; }
.tower-base { width: 80px; height: 6px; background: linear-gradient(90deg, transparent, #4a9eff, transparent); border-radius: 3px; }

.bus {
  position: absolute;
  bottom: 60px;
  left: 30%;
  animation: busMove 8s ease-in-out infinite;
}
@keyframes busMove {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(15px); }
}
.bus-body {
  width: 100px;
  height: 40px;
  background: linear-gradient(135deg, #3a7edf, #2d6dcf);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 8px;
}
.bus-window { width: 18px; height: 22px; background: rgba(255, 255, 255, 0.3); border-radius: 4px; }
.bus-door { width: 20px; height: 30px; background: rgba(255, 255, 255, 0.2); border-radius: 4px; margin-left: auto; }
.bus-wheels { position: absolute; bottom: -8px; width: 100%; display: flex; justify-content: space-between; padding: 0 8px; }
.wheel { width: 12px; height: 12px; background: #1a1a2e; border-radius: 50%; border: 2px solid #4a9eff; }
.bus-headlight { position: absolute; right: -4px; top: 12px; width: 6px; height: 6px; background: #ffff00; border-radius: 50%; box-shadow: 0 0 8px #ffff00; animation: headlight 1.5s ease-in-out infinite; }
@keyframes headlight { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }

.floating-icons { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; }
.float-icon {
  position: absolute;
  background: rgba(74, 158, 255, 0.2);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  pointer-events: auto;
  cursor: pointer;
  transition: all 0.3s ease;
}
.float-icon:hover { transform: scale(1.05); background: rgba(74, 158, 255, 0.4); }
.location { top: 15%; left: 0; animation: floatIcon 4s ease-in-out infinite; }
.message { top: 40%; right: 5%; animation: floatIcon 4s ease-in-out infinite 1s; }
.check { bottom: 25%; left: 5%; animation: floatIcon 4s ease-in-out infinite 2s; }
@keyframes floatIcon { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }

.form-section {
  flex: 0.9;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(10px);
}

.form-card {
  width: 100%;
  max-width: 440px;
  background: white;
  border-radius: 32px;
  padding: 48px 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  transition: all 0.4s ease;
  animation: fadeInRight 0.8s ease;
}
@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(50px); }
  to { opacity: 1; transform: translateX(0); }
}

.card-header { text-align: center; margin-bottom: 36px; }
.logo-icon { font-size: 48px; margin-bottom: 16px; animation: float 3s ease-in-out infinite; }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
.card-header h1 { font-size: 22px; font-weight: 700; color: #0f4c81; margin: 0 0 8px 0; }
.subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.input-wrapper { position: relative; width: 100%; }
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
.custom-input :deep(.el-input__wrapper:hover) { border-color: #3b82f6; background: white; }
.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-options { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; }
.forgot-link { color: #3b82f6; font-size: 13px; }
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
.login-btn:hover { background: linear-gradient(135deg, #1e6cb5, #2d89d6); transform: translateY(-2px); box-shadow: 0 8px 25px rgba(15, 76, 129, 0.3); }
.login-btn:hover::before { left: 100%; }
.btn-arrow { transition: transform 0.3s ease; }
.login-btn:hover .btn-arrow { transform: translateX(5px); }

.divider {
  position: relative;
  text-align: center;
  margin: 32px 0 24px;
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

.footer-tip { text-align: center; }
.footer-tip p { font-size: 11px; color: #9ca3af; margin: 0; }

@media (max-width: 1100px) {
  .illustration-section { display: none; }
  .form-section { flex: 1; }
}
@media (max-width: 600px) {
  .form-card { padding: 32px 24px; margin: 20px; }
  .card-header h1 { font-size: 18px; }
}
</style>