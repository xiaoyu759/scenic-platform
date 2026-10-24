import { createRouter, createWebHistory } from 'vue-router'

// 游客端页面
const VisitorHome = () => import('@/views/visitor/VisitorHome.vue')
const VisitorTicket = () => import('@/views/visitor/VisitorTicket.vue')
const VisitorRoutePlan = () => import('@/views/visitor/VisitorRoutePlan.vue')
const VisitorGuide = () => import('@/views/visitor/VisitorGuide.vue')
const VisitorProfile = () => import('@/views/visitor/VisitorProfile.vue')
const VisitorLogin = () => import('@/views/visitor/VisitorLogin.vue')
const VisitorRegister = () => import('@/views/visitor/VisitorRegister.vue')
const VisitorOrders = () => import('@/views/visitor/VisitorOrders.vue')
const VisitorRoutes = () => import('@/views/visitor/VisitorRoutes.vue')

// 管理端页面
const AdminLogin = () => import('@/views/admin/AdminLogin.vue')
const AdminDashboard = () => import('@/views/admin/AdminDashboard.vue')
const AdminAnalysis = () => import('@/views/admin/AdminAnalysis.vue')
const AdminSpots = () => import('@/views/admin/AdminSpots.vue')
const AdminOrders = () => import('@/views/admin/AdminOrders.vue')
const AdminSetting = () => import('@/views/admin/AdminSetting.vue')

const routes = [
  // 游客端
  { path: '/', name: 'Home', component: VisitorHome },
  { path: '/ticket', name: 'Ticket', component: VisitorTicket },
  { path: '/route-plan', name: 'RoutePlan', component: VisitorRoutePlan },
  { path: '/guide', name: 'Guide', component: VisitorGuide },
  { path: '/profile', name: 'Profile', component: VisitorProfile, meta: { requiresAuth: true } },
  { path: '/login', name: 'Login', component: VisitorLogin },
  { path: '/register', name: 'Register', component: VisitorRegister },
  { path: '/orders', name: 'Orders', component: VisitorOrders, meta: { requiresAuth: true } },
  { path: '/routes', name: 'Routes', component: VisitorRoutes, meta: { requiresAuth: true } },
  
  // 管理端
  { path: '/admin', name: 'AdminLogin', component: AdminLogin },
  { path: '/admin/dashboard', name: 'AdminDashboard', component: AdminDashboard, meta: { requiresAdmin: true } },
  { path: '/admin/analysis', name: 'AdminAnalysis', component: AdminAnalysis, meta: { requiresAdmin: true } },
  { path: '/admin/spots', name: 'AdminSpots', component: AdminSpots, meta: { requiresAdmin: true } },
  { path: '/admin/orders', name: 'AdminOrders', component: AdminOrders, meta: { requiresAdmin: true } },
  { path: '/admin/setting', name: 'AdminSetting', component: AdminSetting, meta: { requiresAdmin: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 修复版
router.beforeEach((to, from, next) => {
  const visitorToken = localStorage.getItem('visitorToken')
  const adminToken = localStorage.getItem('adminToken')
  
  // 管理端路由守卫
  if (to.meta.requiresAdmin) {
    if (!adminToken) {
      // 没有 token，跳转到管理端登录页
      next('/admin')
    } else {
      next()
    }
  } 
  // 游客端路由守卫
  else if (to.meta.requiresAuth) {
    if (!visitorToken) {
      next('/login')
    } else {
      next()
    }
  } 
  else {
    next()
  }
})

export default router