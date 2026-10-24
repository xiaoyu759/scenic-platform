<template>
  <div>
    <VisitorHeader />
    <div class="profile-container">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar">
            <el-avatar :size="80" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
          </div>
          <h2>{{ userInfo.nickname || userInfo.username }}</h2>
          <p>{{ userInfo.phone }}</p>
        </div>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="个人信息" name="info">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="form.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="changing">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    <VisitorFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import VisitorHeader from '@/components/VisitorHeader.vue'
import VisitorFooter from '@/components/VisitorFooter.vue'
import { getUserInfo, updateUserInfo, changePassword as changePasswordApi } from '@/api/visitor'
const activeTab = ref('info')
const formRef = ref()
const pwdFormRef = ref()
const saving = ref(false)
const changing = ref(false)

const userInfo = ref({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  avatar: ''
})

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
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

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    console.log('getUserInfo 响应:', res)  // 调试日志
    
    if (res.code === 200) {
      const data = res.data
      console.log('用户数据:', data)  // 调试日志
      
      // ⭐ 直接赋值给 userInfo
      userInfo.value.username = data.username || ''
      userInfo.value.nickname = data.nickname || data.username || '用户'
      userInfo.value.phone = data.phone || ''
      userInfo.value.email = data.email || ''
      userInfo.value.avatar = data.avatar || ''
      
      // ⭐ 直接赋值给 form
      form.username = data.username || ''
      form.nickname = data.nickname || data.username || '用户'
      form.phone = data.phone || ''
      form.email = data.email || ''
      
      // 保存到 localStorage
      localStorage.setItem('visitorUser', JSON.stringify(userInfo.value))
      
      console.log('更新后 form:', form)  // 调试日志
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 降级：从 localStorage 读取
    const stored = localStorage.getItem('visitorUser')
    if (stored) {
      const data = JSON.parse(stored)
      userInfo.value.username = data.username || ''
      userInfo.value.nickname = data.nickname || data.username || '用户'
      userInfo.value.phone = data.phone || ''
      userInfo.value.email = data.email || ''
      userInfo.value.avatar = data.avatar || ''
      
      form.username = data.username || ''
      form.nickname = data.nickname || data.username || '用户'
      form.phone = data.phone || ''
      form.email = data.email || ''
    }
  }
}

const saveProfile = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    const res = await updateUserInfo({
      nickname: form.nickname,
      phone: form.phone,
      email: form.email
    })
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      await fetchUserInfo()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  try {
    await pwdFormRef.value.validate()
    changing.value = true
    
    const res = await changePasswordApi({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        localStorage.removeItem('visitorToken')
        localStorage.removeItem('visitorUser')
        window.location.href = '/login'
      }, 1500)
    } else {
      ElMessage.error(res.message || '修改密码失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '修改密码失败')
    }
  } finally {
    changing.value = false
  }
}
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-container {
  min-height: calc(100vh - 200px);
  background: #f5f7fa;
  padding: 40px 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  overflow: hidden;
}

.profile-header {
  text-align: center;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.profile-header h2 {
  margin: 15px 0 5px;
}

.profile-header p {
  opacity: 0.8;
}

:deep(.el-tabs__header) {
  padding: 0 20px;
  margin: 0;
}

:deep(.el-tabs__content) {
  padding: 20px;
}
</style>