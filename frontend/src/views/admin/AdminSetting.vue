<template>
  <div class="dashboard">
    <AdminHeader />
    
    <div class="dashboard-main">
      <div class="admin-header">
        <h2>景区设置</h2>
      </div>

      <el-tabs v-model="activeTab" class="glass-tabs">
        <!-- 景区信息 -->
        <el-tab-pane label="景区信息" name="info">
          <div class="glass-card setting-card">
            <el-form :model="scenicForm" :rules="scenicRules" ref="scenicFormRef" label-width="120px">
              <el-form-item label="景区名称" prop="name">
                <el-input v-model="scenicForm.name" placeholder="请输入景区名称" class="custom-input" />
              </el-form-item>
              <el-form-item label="景区简介" prop="description">
                <el-input type="textarea" v-model="scenicForm.description" :rows="4" placeholder="请输入景区简介" class="custom-input" />
              </el-form-item>
              <el-form-item label="景区地址" prop="address">
                <el-input v-model="scenicForm.address" placeholder="请输入景区地址" class="custom-input" />
              </el-form-item>
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="scenicForm.phone" placeholder="请输入联系电话" class="custom-input" />
              </el-form-item>
              <el-form-item label="营业时间" prop="businessHours">
                <el-input v-model="scenicForm.businessHours" placeholder="例如：08:00-18:00" class="custom-input" />
              </el-form-item>
              <el-form-item label="最大承载量" prop="maxCapacity">
                <el-input-number v-model="scenicForm.maxCapacity" :min="1000" :max="50000" class="custom-input" /> 人
              </el-form-item>
              <el-form-item label="Logo图片" prop="logo">
                <el-input v-model="scenicForm.logo" placeholder="请输入Logo图片URL" class="custom-input" />
              </el-form-item>
              <el-form-item label="轮播图" prop="bannerImages">
                <el-input v-model="scenicForm.bannerImages" placeholder="多个URL用逗号分隔" class="custom-input" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveScenicInfo" :loading="savingScenic" class="modern-btn">保存设置</el-button>
                <el-button @click="fetchScenicInfo" :loading="loading" class="modern-btn">刷新</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 管理员账号 -->
        <el-tab-pane label="管理员账号" name="admin">
          <div class="glass-card setting-card">
            <el-form :model="adminForm" :rules="adminRules" ref="adminFormRef" label-width="120px">
              <el-form-item label="管理员账号" prop="username">
                <el-input v-model="adminForm.username" disabled class="custom-input" />
              </el-form-item>
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="adminForm.oldPassword" type="password" placeholder="请输入原密码" show-password class="custom-input" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="adminForm.newPassword" type="password" placeholder="请输入新密码" show-password class="custom-input" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="adminForm.confirmPassword" type="password" placeholder="请确认新密码" show-password class="custom-input" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="changingPwd" class="modern-btn">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 关于 -->
        <el-tab-pane label="关于" name="about">
          <div class="glass-card about-card">
            <div class="about-icon">🏝️</div>
            <h3>特呈岛智慧景区管理平台</h3>
            <p>版本：v1.0.0</p>
            <p>特呈岛景区智慧管理系统</p>
            <p>为景区提供游客管理、票务系统、路线规划等一站式解决方案</p>
            <el-divider />
            <h4>技术栈</h4>
            <div class="tech-stack">
              <el-tag>Vue 3</el-tag>
              <el-tag>Element Plus</el-tag>
              <el-tag>ECharts</el-tag>
              <el-tag>百度地图</el-tag>
              <el-tag>Spring Boot</el-tag>
              <el-tag>MySQL</el-tag>
            </div>
            <el-divider />
            <p class="copyright">© 2026 特呈岛智慧景区 | 开源项目 免费使用</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AdminHeader from '@/components/AdminHeader.vue'
import { getScenicInfo, updateScenicInfo, changeAdminPassword } from '@/api/admin'

const activeTab = ref('info')
const scenicFormRef = ref()
const adminFormRef = ref()
const loading = ref(false)
const savingScenic = ref(false)
const changingPwd = ref(false)

// 景区信息表单
const scenicForm = reactive({
  id: null,
  name: '',
  description: '',
  address: '',
  phone: '',
  businessHours: '',
  maxCapacity: 30000,
  logo: '',
  bannerImages: ''
})

const scenicRules = {
  name: [{ required: true, message: '请输入景区名称', trigger: 'blur' }],
  phone: [{ pattern: /^[\d-]+$/, message: '请输入正确的电话号码', trigger: 'blur' }],
  maxCapacity: [{ required: true, message: '请输入最大承载量', trigger: 'blur' }]
}

// 管理员表单
const adminForm = reactive({
  username: 'admin',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== adminForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const adminRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

// 获取景区信息
const fetchScenicInfo = async () => {
  loading.value = true
  try {
    const res = await getScenicInfo()
    if (res.code === 200) {
      const data = res.data
      scenicForm.id = data.id
      scenicForm.name = data.name || ''
      scenicForm.description = data.description || ''
      scenicForm.address = data.address || ''
      scenicForm.phone = data.phone || ''
      scenicForm.businessHours = data.businessHours || ''
      scenicForm.maxCapacity = data.maxCapacity || 30000
      scenicForm.logo = data.logo || ''
      scenicForm.bannerImages = data.bannerImages || ''
    } else {
      ElMessage.error(res.message || '获取景区信息失败')
    }
  } catch (error) {
    console.error('获取景区信息失败:', error)
    ElMessage.error(error.response?.data?.message || '获取景区信息失败')
  } finally {
    loading.value = false
  }
}

// 保存景区信息
const saveScenicInfo = async () => {
  try {
    await scenicFormRef.value.validate()
    savingScenic.value = true
    
    const res = await updateScenicInfo({
      id: scenicForm.id,
      name: scenicForm.name,
      description: scenicForm.description,
      address: scenicForm.address,
      phone: scenicForm.phone,
      businessHours: scenicForm.businessHours,
      maxCapacity: scenicForm.maxCapacity,
      logo: scenicForm.logo,
      bannerImages: scenicForm.bannerImages
    })
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      await fetchScenicInfo()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error !== 'cancel') {
      ElMessage.error('保存失败，请检查表单')
    }
  } finally {
    savingScenic.value = false
  }
}

// 修改密码
const changePassword = async () => {
  try {
    await adminFormRef.value.validate()
    changingPwd.value = true
    
    const res = await changeAdminPassword({
      oldPassword: adminForm.oldPassword,
      newPassword: adminForm.newPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminUser')
        window.location.href = '/admin'
      }, 1500)
    } else {
      ElMessage.error(res.message || '修改密码失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '修改密码失败')
    }
  } finally {
    changingPwd.value = false
  }
}
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
  max-width: 1000px;
  margin: 0 auto;
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

.setting-card {
  max-width: 700px;
  margin: 0 auto;
}

.glass-card {
  background: rgba(15, 21, 53, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  padding: 24px;
  transition: all 0.3s ease;
}

.about-card {
  text-align: center;
  max-width: 600px;
}

.about-icon { font-size: 64px; margin-bottom: 16px; }
.about-card h3 { color: white; margin-bottom: 8px; }
.about-card p { color: rgba(255, 255, 255, 0.7); margin: 8px 0; }
.tech-stack { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; margin: 16px 0; }
.copyright { color: rgba(255, 255, 255, 0.5); font-size: 12px; }

.custom-input :deep(.el-input__wrapper) {
  background: rgba(15, 21, 53, 0.6);
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 12px;
  box-shadow: none;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #409eff;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.glass-tabs { background: transparent; }
.glass-tabs :deep(.el-tabs__header) {
  background: rgba(15, 21, 53, 0.8);
  border-radius: 12px;
  padding: 0 16px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}
.glass-tabs :deep(.el-tabs__item) { color: rgba(255, 255, 255, 0.7); }
.glass-tabs :deep(.el-tabs__item.is-active) { color: #409eff; }
.glass-tabs :deep(.el-tabs__active-bar) { background: #409eff; }

.modern-btn {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.modern-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}
</style>