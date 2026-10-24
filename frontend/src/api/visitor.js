// import request from './request'

// // 首页
// export const getHomeInfo = () => request.get('/visitor/home/info')
// export const getAnnouncements = () => request.get('/visitor/home/announcements')

// // 景点
// export const getSpotList = () => request.get('/visitor/spot/list')
// export const getSpotDetail = (id) => request.get(`/visitor/spot/${id}`)
// export const getFacilities = () => request.get('/visitor/spot/facilities')

// // 购票
// export const getTicketTypes = () => request.get('/visitor/ticket/types')
// export const createOrder = (data) => request.post('/visitor/ticket/create', data)
// export const getOrderList = (params) => request.get('/visitor/ticket/list', { params })
// export const cancelOrder = (id) => request.put(`/visitor/ticket/cancel/${id}`)

// // 路线规划
// export const generateRoute = (data) => request.post('/visitor/route/generate', data)
// export const saveRoute = (data) => request.post('/visitor/route/save', data)
// export const getRouteList = () => request.get('/visitor/route/list')
// export const deleteRoute = (id) => request.delete(`/visitor/route/${id}`)

// // 用户
// export const login = (data) => request.post('/visitor/login', data)
// export const register = (data) => request.post('/visitor/register', data)
// export const getUserInfo = () => request.get('/visitor/user/info')
// export const updateUserInfo = (data) => request.put('/visitor/user/info', data)
// export const logout = () => request.post('/visitor/logout')

import request from './request'

// 首页
export const getHomeInfo = () => request.get('/visitor/home/info')
export const getAnnouncements = () => request.get('/visitor/home/announcements')

// 景点
export const getSpotList = () => request.get('/visitor/spot/list')
export const getSpotDetail = (id) => request.get(`/visitor/spot/${id}`)
export const getFacilities = () => request.get('/visitor/spot/facilities')

// 购票
export const getTicketTypes = () => request.get('/visitor/ticket/types')
export const createOrder = (data) => request.post('/visitor/ticket/create', data)
export const getOrderList = (params) => request.get('/visitor/ticket/list', { params })
export const cancelOrder = (id) => request.put(`/visitor/ticket/cancel/${id}`)


// 使用门票（核销）
export const useTicket = (id) => request.put(`/visitor/ticket/use/${id}`)

// 路线规划
export const generateRoute = (data) => request.post('/visitor/route/generate', data)
export const saveRoute = (data) => request.post('/visitor/route/save', data)
export const getRouteList = () => request.get('/visitor/route/list')
export const deleteRoute = (id) => request.delete(`/visitor/route/${id}`)

// 用户
export const login = (data) => request.post('/visitor/login', data)
export const register = (data) => request.post('/visitor/register', data)
export const getUserInfo = () => request.get('/visitor/user/info')
export const updateUserInfo = (data) => request.put('/visitor/user/info', data)
export const logout = () => request.post('/visitor/logout')
// 在文件末尾添加
export const changePassword = (data) => request.put('/visitor/user/password', data)