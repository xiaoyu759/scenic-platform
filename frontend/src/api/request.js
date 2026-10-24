import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加token
request.interceptors.request.use(
  config => {
    const visitorToken = localStorage.getItem('visitorToken')
    const adminToken = localStorage.getItem('adminToken')
    
    const isAdminApi = config.url && config.url.startsWith('/admin/')
    const isVisitorApi = config.url && config.url.startsWith('/visitor/')
    
    if (isAdminApi && adminToken) {
      config.headers['Authorization'] = `Bearer ${adminToken}`
    }
    if (isVisitorApi && visitorToken) {
      config.headers['Authorization'] = `Bearer ${visitorToken}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 401 未登录或 token 过期
      if (res.code === 401) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('visitorToken')
        localStorage.removeItem('adminUser')
        localStorage.removeItem('visitorUser')
        // 判断当前是否在管理端页面
        if (window.location.pathname.startsWith('/admin')) {
          window.location.href = '/admin'
        } else {
          window.location.href = '/login'
        }
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      // 401 未登录
      if (error.response.status === 401) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('visitorToken')
        localStorage.removeItem('adminUser')
        localStorage.removeItem('visitorUser')
        if (window.location.pathname.startsWith('/admin')) {
          window.location.href = '/admin'
        } else {
          window.location.href = '/login'
        }
      }
      ElMessage.error(error.response.data?.message || '请求失败')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request