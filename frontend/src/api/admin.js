// import request from './request'

// // ==================== 登录 ====================
// export const adminLogin = (data) => request.post('/admin/login', data)

// // ==================== 监控大屏 ====================
// // 今日数据总览
// export const getMonitorToday = () => request.get('/admin/monitor/today')
// // 景点实时数据
// export const getMonitorSpots = () => request.get('/admin/monitor/spots')
// // 入园趋势
// export const getMonitorTrend = (params) => request.get('/admin/monitor/trend', { params })
// // 热力图数据
// export const getHeatmapData = () => request.get('/admin/monitor/heatmap')

// // ==================== 数据分析 ====================
// // 营收统计
// export const getRevenueStats = (params) => request.get('/admin/analysis/revenue', { params })
// // 票种销售统计
// export const getTicketSales = (params) => request.get('/admin/analysis/ticket', { params })
// // 游客画像
// export const getVisitorProfile = (params) => request.get('/admin/analysis/visitor', { params })
// // 导出Excel
// export const exportExcel = (params) => request.get('/admin/analysis/export', { params, responseType: 'blob' })

// // ==================== 景点管理 ====================
// // 获取景点列表
// export const getSpotManageList = (params) => request.get('/admin/spot/list', { params })
// // 新增景点
// export const addSpot = (data) => request.post('/admin/spot/add', data)
// // 更新景点
// export const updateSpot = (data) => request.put('/admin/spot/update', data)
// // 删除景点
// export const deleteSpot = (id) => request.delete(`/admin/spot/${id}`)

// // ==================== 订单管理 ====================
// // 获取订单列表
// export const getOrderManageList = (params) => request.get('/admin/order/list', { params })
// // 更新订单状态
// export const updateOrderStatus = (id, status) => request.put(`/admin/order/${id}/status`, { status })

// // ==================== 景区设置 ====================
// // 获取景区信息
// export const getScenicInfo = () => request.get('/admin/setting/scenic')
// // 更新景区信息
// export const updateScenicInfo = (data) => request.put('/admin/setting/scenic', data)


import request from './request'

// ==================== 登录 ====================
export const adminLogin = (data) => request.post('/admin/login', data)

// ==================== 监控大屏 ====================
export const getMonitorToday = () => request.get('/admin/monitor/today')
export const getMonitorSpots = () => request.get('/admin/monitor/spots')
export const getMonitorTrend = (params) => request.get('/admin/monitor/trend', { params })
export const getHeatmapData = () => request.get('/admin/monitor/heatmap')

// ==================== 数据分析 ====================
export const getRevenueStats = (params) => request.get('/admin/analysis/revenue', { params })
export const getTicketSales = (params) => request.get('/admin/analysis/ticket', { params })
export const getVisitorProfile = (params) => request.get('/admin/analysis/visitor', { params })
export const exportExcel = (params) => request.get('/admin/analysis/export', { params, responseType: 'blob' })

// ==================== 景点管理 ====================
export const getSpotManageList = (params) => request.get('/admin/spot/list', { params })
export const addSpot = (data) => request.post('/admin/spot/add', data)
export const updateSpot = (data) => request.put('/admin/spot/update', data)
export const deleteSpot = (id) => request.delete(`/admin/spot/${id}`)

// ==================== 订单管理 ====================
export const getOrderManageList = (params) => request.get('/admin/order/list', { params })
export const updateOrderStatus = (id, status) => request.put(`/admin/order/${id}/status`, { status })
export const getOrderDetail = (id) => request.get(`/admin/order/${id}`)

// ==================== 景区设置 ====================
export const getScenicInfo = () => request.get('/admin/setting/scenic')
export const updateScenicInfo = (data) => request.put('/admin/setting/scenic', data)

// api/admin.js 新增
export const changeAdminPassword = (data) => request.put('/admin/setting/password', data)