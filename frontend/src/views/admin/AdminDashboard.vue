<template>
  <div class="dashboard">
    <!-- 顶部导航栏 -->
    <div class="dashboard-header">
      <div class="logo-area">
        <span class="logo-icon">🏝️</span>
        <span class="logo-text">特呈岛智慧景区管理平台</span>
      </div>
      <div class="nav-area">
        <router-link to="/admin/dashboard" class="nav-link" active-class="active">
          <el-icon><Monitor /></el-icon>
          <span>实时监控</span>
        </router-link>
        <router-link to="/admin/analysis" class="nav-link" active-class="active">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据分析</span>
        </router-link>
        <router-link to="/admin/spots" class="nav-link" active-class="active">
          <el-icon><Location /></el-icon>
          <span>景点管理</span>
        </router-link>
        <router-link to="/admin/orders" class="nav-link" active-class="active">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </router-link>
        <router-link to="/admin/setting" class="nav-link" active-class="active">
          <el-icon><Setting /></el-icon>
          <span>景区设置</span>
        </router-link>
      </div>
      <div class="user-area">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span>管理员</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="dashboard-main">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <div class="panel-title">
          <el-icon><User /></el-icon>
          游客数量监测
          <span class="pulse-dot"></span>
          <span v-if="loading" class="loading-text">加载中...</span>
        </div>
        
        <!-- KPI卡片区域 - 紧凑三合一 -->
        <div class="kpi-cards" v-loading="loading">
          <div class="kpi-card">
            <div class="kpi-icon">👥</div>
            <div class="kpi-content">
              <div class="kpi-value">{{ formatNumber(currentVisitors) }}</div>
              <div class="kpi-label">当前在园人数</div>
            </div>
          </div>
          <div class="kpi-card">
            <div class="kpi-icon">🎟️</div>
            <div class="kpi-content">
              <div class="kpi-value">{{ formatNumber(todayVisitors) }}</div>
              <div class="kpi-label">今日入园人数</div>
            </div>
          </div>
          <div class="kpi-card">
            <div class="kpi-icon">📊</div>
            <div class="kpi-content">
              <div class="kpi-value">{{ capacityRate.toFixed(1) }}<span class="kpi-unit">%</span></div>
              <div class="kpi-label">承载率</div>
            </div>
          </div>
        </div>

        <!-- 各区域游客量对比 -->
        <div class="chart-container">
          <div class="chart-title">
            <el-icon><PieChart /></el-icon>
            各区域游客量对比
            <span class="chart-subtitle">单位：人 | 实时数据</span>
          </div>
          <div ref="areaChartRef" class="chart" v-loading="loading"></div>
        </div>

        <!-- 游客入园趋势 -->
        <div class="chart-container">
          <div class="chart-title">
            <el-icon><DataLine /></el-icon>
            近7天入园趋势
            <span class="chart-subtitle">单位：人</span>
          </div>
          <div ref="trendChartRef" class="chart" v-loading="loading"></div>
        </div>
      </div>

      <!-- 中央地图区域 -->
      <div class="map-panel">
        <div class="map-header">
          <div class="map-title">
            <span class="pulse-dot"></span>
            特呈岛实时热力分布
          </div>
          <div class="map-controls">
            <el-button size="small" @click="refreshMap" class="modern-btn" :loading="loading">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
        
        <!-- 地图容器 -->
        <div class="map-container" v-loading="loading">
          <canvas ref="heatmapCanvasRef" class="heatmap-canvas"></canvas>
          <div v-if="!hasHeatmapData" class="map-empty-tip">
            <span>暂无热力图数据</span>
          </div>
        </div>
        
        <!-- 底部关键指标栏 -->
        <div class="bottom-metrics">
          <div class="metric-item" v-for="metric in metrics" :key="metric.label" @click="showMetricDetail(metric)">
            <div class="metric-value">
              <span class="value">{{ metric.value }}</span>
              <span class="trend" :class="metric.trendClass">
                <el-icon><ArrowUp v-if="metric.trendClass === 'up'" /><ArrowDown v-else /></el-icon>
                {{ metric.trend }}
              </span>
            </div>
            <div class="metric-label">{{ metric.label }}</div>
            <div class="metric-progress" v-if="metric.progress">
              <div class="progress-fill" :style="{ width: metric.progress + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel">
        <!-- 异常事件预警 -->
        <div class="alert-panel">
          <div class="panel-title">
            <el-icon><WarningFilled /></el-icon>
            异常事件预警
            <span class="alert-badge">实时更新</span>
          </div>
          
          <div class="alert-stats">
            <div class="alert-stat critical">
              <span class="stat-value">{{ alertCount.critical }}</span>
              <span class="stat-label">严重告警</span>
            </div>
            <div class="alert-stat warning">
              <span class="stat-value">{{ alertCount.high }}</span>
              <span class="stat-label">高危预警</span>
            </div>
            <div class="alert-stat info">
              <span class="stat-value">{{ alertCount.unhandled }}</span>
              <span class="stat-label">待处理</span>
            </div>
          </div>
          
          <!-- 预警点位分布图 -->
          <div class="building-preview">
            <div class="preview-title">
              <span class="dot-red"></span> 预警点位分布
            </div>
            <div class="preview-canvas">
              <div class="preview-point point-critical pulse-ring" style="top: 20%; left: 30%;">
                <span>红树林栈道</span>
                <span class="level-tag critical">严重</span>
              </div>
              <div class="preview-point point-warning" style="top: 50%; left: 55%;">
                <span>温泉度假村</span>
                <span class="level-tag warning">高危</span>
              </div>
              <div class="preview-point point-info" style="top: 70%; left: 20%;">
                <span>码头</span>
                <span class="level-tag info">提醒</span>
              </div>
              <div class="preview-point point-warning" style="top: 35%; left: 70%;">
                <span>观海长廊</span>
                <span class="level-tag warning">高危</span>
              </div>
            </div>
          </div>
          
          <!-- 预警滚动列表 -->
          <div class="alert-list">
            <transition-group name="alert-slide" tag="div">
              <div v-for="alert in alertList" :key="alert.id" class="alert-row" :class="alert.type">
                <div class="alert-level">
                  <span class="level-dot" :class="alert.type"></span>
                  <span class="level-text">{{ getLevelText(alert.type) }}</span>
                </div>
                <div class="alert-content">
                  <span class="alert-time">{{ alert.time }}</span>
                  <span class="alert-location">{{ alert.location }}</span>
                  <span class="alert-desc">{{ alert.desc }}</span>
                </div>
              </div>
            </transition-group>
          </div>
        </div>

        <!-- 设备运行状态 -->
        <div class="device-panel">
          <div class="panel-title">
            <el-icon><Cpu /></el-icon>
            设备运行状态
            <span class="chart-subtitle">在线率 {{ deviceOnlineRate }}%</span>
          </div>
          
          <!-- 设备状态环形图 -->
          <div class="device-chart">
            <div ref="deviceRingChartRef" class="ring-chart"></div>
            <div class="device-stats">
              <div class="device-stat online">
                <span class="stat-color"></span>
                <span class="stat-label">在线</span>
                <span class="stat-value">{{ deviceStats.online }}</span>
              </div>
              <div class="device-stat offline">
                <span class="stat-color"></span>
                <span class="stat-label">离线</span>
                <span class="stat-value">{{ deviceStats.offline }}</span>
              </div>
              <div class="device-stat warning">
                <span class="stat-color"></span>
                <span class="stat-label">预警</span>
                <span class="stat-value">{{ deviceStats.warning }}</span>
              </div>
            </div>
          </div>
          
          <!-- 设备列表网格 -->
          <div class="device-grid">
            <div v-for="device in deviceList" :key="device.name" class="device-item" :class="device.status" @click="showDeviceDetail(device)">
              <el-icon><component :is="device.icon" /></el-icon>
              <span class="device-name">{{ device.name }}</span>
              <span class="device-status">
                <span class="status-dot" :class="device.status"></span>
                {{ device.statusText }}
              </span>
            </div>
          </div>
          
          <!-- 滚动日志 -->
          <div class="log-scroll">
            <div class="log-track">
              <div v-for="(log, idx) in logs" :key="idx" class="log-item">
                <span class="log-time">{{ log.time }}</span>
                <span class="log-content">{{ log.content }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  Monitor, DataAnalysis, Location, Tickets, Setting, User, ArrowDown,
  PieChart, DataLine, Refresh, WarningFilled, TrendCharts,
  Cpu, VideoCamera, Camera, Guide, Iphone, AlarmClock, UserFilled,
  ArrowUp
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMonitorToday, getMonitorSpots, getMonitorTrend, getHeatmapData } from '@/api/admin'

const router = useRouter()

// ==================== DOM 引用 ====================
const areaChartRef = ref(null)
const trendChartRef = ref(null)
const deviceRingChartRef = ref(null)
const heatmapCanvasRef = ref(null)

// ==================== 图表实例 ====================
let areaChart = null
let trendChart = null
let deviceRingChart = null

// ==================== 响应式数据 ====================
const loading = ref(false)
const hasHeatmapData = ref(false)

// KPI 数据
const currentVisitors = ref(0)
const todayVisitors = ref(0)
const capacityRate = ref(0)

// 设备状态
const deviceOnlineRate = ref(92)
const deviceStats = ref({
  online: 138,
  offline: 6,
  warning: 4
})

// 设备列表
const deviceList = ref([
  { name: '闸机系统', icon: 'VideoCamera', status: 'online', statusText: '正常', count: 12 },
  { name: '监控摄像头', icon: 'Camera', status: 'online', statusText: '正常', count: 48 },
  { name: '信息大屏', icon: 'Monitor', status: 'online', statusText: '正常', count: 4 },
  { name: '导览设备', icon: 'Guide', status: 'warning', statusText: '部分故障', count: 20 },
  { name: 'WiFi热点', icon: 'Iphone', status: 'online', statusText: '正常', count: 32 },
  { name: '报警系统', icon: 'AlarmClock', status: 'warning', statusText: '需维护', count: 2 }
])

// 预警数据（模拟数据 - 无后端接口）
const alertCount = ref({ critical: 2, high: 5, unhandled: 8 })

const metrics = ref([
  { label: '闸机数', value: '24', trend: '+2', trendClass: 'up', progress: 100 },
  { label: '摄像头', value: '52', trend: '+3', trendClass: 'up', progress: 100 },
  { label: '在线率', value: '92', trend: '+2%', trendClass: 'up', progress: 92 },
  { label: '故障报告', value: '8', trend: '-2', trendClass: 'down', progress: 15 }
])

// 预警列表（模拟数据 - 无后端接口）
const alertList = ref([
  { id: 1, time: '10:32:15', location: '红树林栈道', desc: '人流密度超阈值 85%', type: 'critical' },
  { id: 2, time: '10:28:45', location: '码头', desc: '排队人数过多 156人', type: 'warning' },
  { id: 3, time: '10:25:10', location: '温泉度假村', desc: '闸机2号口故障', type: 'warning' },
  { id: 4, time: '10:22:33', location: '观海长廊', desc: '摄像头离线', type: 'info' },
  { id: 5, time: '10:20:18', location: '渔村文化村', desc: '人流密度偏高 62%', type: 'info' }
])

// 日志列表（模拟数据 - 无后端接口）
const logs = ref([
  { time: '10:32:15', content: '【严重】红树林栈道区域人流密度已达阈值85%' },
  { time: '10:30:22', content: '【恢复】闸机2号口通过率恢复正常' },
  { time: '10:28:45', content: '【预警】码头排队人数新增12人' },
  { time: '10:25:10', content: '【故障】温泉度假村闸机故障已上报' },
  { time: '10:22:33', content: '【离线】观海长廊摄像头离线' },
  { time: '10:20:18', content: '【巡检】系统自动巡检完成' }
])

// 存储热力图数据
const heatmapData = ref([])
const spotData = ref([])
const trendData = ref({ dates: [], counts: [] })

// ==================== 工具函数 ====================

const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}

const getLevelText = (type) => {
  const map = { critical: '严重', warning: '高危', info: '提醒' }
  return map[type] || '提醒'
}

// ==================== 数据获取 - 真实接口 ====================

// 获取今日数据
const fetchTodayData = async () => {
  try {
    const res = await getMonitorToday()
    console.log('今日数据响应:', res)
    if (res.code === 200) {
      const data = res.data || {}
      currentVisitors.value = data.currentVisitors || 0
      todayVisitors.value = data.todayVisitors || 0
      capacityRate.value = data.capacityRate || 0
    }
  } catch (error) {
    console.error('获取今日数据失败:', error)
  }
}

// 获取景点实时数据
const fetchSpotData = async () => {
  try {
    const res = await getMonitorSpots()
    console.log('景点数据响应:', res)
    if (res.code === 200) {
      spotData.value = Array.isArray(res.data) ? res.data : []
      updateAreaChart(spotData.value)
    }
  } catch (error) {
    console.error('获取景点数据失败:', error)
  }
}

// 获取入园趋势数据
const fetchTrendData = async () => {
  try {
    console.log('========== 开始获取趋势数据 ==========')
    const res = await getMonitorTrend({ type: 'daily' })
    console.log('趋势数据响应:', res)
    
    if (res.code === 200) {
      const data = res.data
      if (data && typeof data === 'object') {
        trendData.value = {
          dates: data.dates || ['暂无数据'],
          counts: data.counts || [0]
        }
      } else {
        trendData.value = { dates: ['暂无数据'], counts: [0] }
      }
      console.log('最终趋势数据:', trendData.value)
      updateTrendChart(trendData.value)
    } else {
      trendData.value = { dates: ['暂无数据'], counts: [0] }
      updateTrendChart(trendData.value)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    trendData.value = { dates: ['暂无数据'], counts: [0] }
    updateTrendChart(trendData.value)
  }
}

// 获取热力图数据
const fetchHeatmapData = async () => {
  try {
    const res = await getHeatmapData()
    console.log('热力图数据响应:', res)
    if (res.code === 200) {
      heatmapData.value = Array.isArray(res.data) ? res.data : []
      hasHeatmapData.value = heatmapData.value.length > 0
      drawHeatmap(heatmapData.value)
    }
  } catch (error) {
    console.error('获取热力图数据失败:', error)
  }
}

// 刷新所有数据
const refreshAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchTodayData(),
      fetchSpotData(),
      fetchTrendData(),
      fetchHeatmapData()
    ])
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('刷新数据失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 初始化图表 ====================

// 初始化区域图表（带占位数据）
const initAreaChart = () => {
  if (!areaChartRef.value) return
  if (areaChart) areaChart.dispose()
  areaChart = echarts.init(areaChartRef.value)

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '10%', right: '5%', top: '10%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['加载中...'],
      axisLabel: { rotate: 30, fontSize: 9, color: '#aaa', interval: 0 },
      axisLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.3)' } }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLabel: { color: '#aaa', fontSize: 9 },
      nameTextStyle: { color: '#aaa', fontSize: 10 },
      splitLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.08)' } }
    },
    series: [{
      type: 'bar',
      data: [0],
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#67c23a' }
        ]),
        shadowColor: 'rgba(64, 158, 255, 0.2)',
        shadowBlur: 8
      },
      label: { show: true, position: 'top', color: '#f5a623', fontWeight: 'bold', fontSize: 9 },
      barWidth: '45%'
    }]
  }
  areaChart.setOption(option)
}

// ⭐ 更新区域图表 - 优化横坐标显示
const updateAreaChart = (spots) => {
  if (!areaChartRef.value || !areaChart) return

  if (!spots || spots.length === 0) {
    const option = {
      xAxis: { data: ['暂无数据'] },
      series: [{ data: [0] }]
    }
    areaChart.setOption(option)
    return
  }

  // ⭐ 缩短景点名称（保留前6个字符）
  const areaNames = spots.map(s => {
    const name = s.name || '未知景点'
    return name.length > 6 ? name.substring(0, 6) + '..' : name
  })
  const visitorCounts = spots.map(s => s.currentCount || 0)

  const option = {
    tooltip: { 
      trigger: 'axis', 
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const idx = params[0].dataIndex
        const fullName = spots[idx]?.name || params[0].name
        return `<div style="font-weight:bold;">📍 ${fullName}</div>
                <div>👥 人数：<strong>${params[0].value}</strong> 人</div>
                <div style="font-size:11px;color:#999;">最大容量：${spots[idx]?.maxCapacity || '--'} 人</div>`
      }
    },
    grid: { left: '8%', right: '4%', top: '12%', bottom: '18%', containLabel: true },
    xAxis: {
      type: 'category',
      data: areaNames,
      axisLabel: { 
        rotate: 30,
        fontSize: 9,
        color: '#aaa',
        interval: 0
      },
      axisLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.3)' } }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLabel: { color: '#aaa', fontSize: 9 },
      nameTextStyle: { color: '#aaa', fontSize: 10 },
      splitLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.08)' } }
    },
    series: [{
      type: 'bar',
      data: visitorCounts,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#67c23a' }
        ]),
        shadowColor: 'rgba(64, 158, 255, 0.2)',
        shadowBlur: 8
      },
      label: {
        show: true,
        position: 'top',
        color: '#f5a623',
        fontWeight: 'bold',
        fontSize: 9,
        formatter: (params) => params.value || 0
      },
      barWidth: '45%'
    }]
  }
  areaChart.setOption(option)
}

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '12%', right: '5%', top: '10%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['暂无数据'],
      axisLabel: { fontSize: 10, color: '#aaa' },
      axisLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.3)' } }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLabel: { color: '#aaa' },
      nameTextStyle: { color: '#aaa' },
      splitLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.1)' } }
    },
    series: [{
      type: 'line',
      data: [0],
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        color: '#409eff',
        width: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      }
    }]
  }
  trendChart.setOption(option)
}

// 更新趋势图表
const updateTrendChart = (data) => {
  if (!trendChartRef.value || !trendChart) return

  const dates = data && typeof data === 'object' ? (data.dates || []) : []
  const counts = data && typeof data === 'object' ? (data.counts || []) : []

  if (dates.length === 0 || counts.length === 0) {
    const option = {
      xAxis: { data: ['暂无数据'] },
      series: [{ data: [0] }]
    }
    trendChart.setOption(option)
    return
  }

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '12%', right: '5%', top: '10%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { fontSize: 10, color: '#aaa' },
      axisLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.3)' } }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLabel: { color: '#aaa' },
      nameTextStyle: { color: '#aaa' },
      splitLine: { lineStyle: { color: 'rgba(64, 158, 255, 0.1)' } }
    },
    series: [{
      type: 'line',
      data: counts,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        color: '#409eff',
        width: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      },
      label: {
        show: true,
        position: 'top',
        color: '#f5a623',
        fontWeight: 'bold',
        fontSize: 10,
        formatter: (params) => params.value || 0
      }
    }]
  }
  trendChart.setOption(option)
}

// 初始化设备环形图
const initDeviceRingChart = () => {
  if (!deviceRingChartRef.value) return
  if (deviceRingChart) deviceRingChart.dispose()
  deviceRingChart = echarts.init(deviceRingChartRef.value)

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    series: [{
      type: 'pie',
      radius: ['60%', '80%'],
      center: ['50%', '50%'],
      data: [
        { name: '在线设备', value: 138, itemStyle: { color: '#67c23a' } },
        { name: '离线设备', value: 6, itemStyle: { color: '#909399' } },
        { name: '预警设备', value: 4, itemStyle: { color: '#e6a23c' } }
      ],
      label: { show: false },
      emphasis: { scale: true },
      itemStyle: { borderRadius: 8, borderColor: '#1a1f45', borderWidth: 2 }
    }]
  }
  deviceRingChart.setOption(option)
}

// ==================== 热力图绘制 ====================

const drawHeatmap = (points) => {
  const canvas = heatmapCanvasRef.value
  if (!canvas) return

  const container = canvas.parentElement
  const width = container?.clientWidth || 800
  const height = container?.clientHeight || 420

  canvas.width = width
  canvas.height = height

  const ctx = canvas.getContext('2d')

  // 清空画布
  ctx.clearRect(0, 0, width, height)

  // 绘制深海背景
  const gradient = ctx.createLinearGradient(0, 0, 0, height)
  gradient.addColorStop(0, '#0a2a4a')
  gradient.addColorStop(1, '#0a1a3a')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, width, height)

  // 绘制岛屿轮廓
  ctx.beginPath()
  ctx.moveTo(width * 0.2, height * 0.4)
  ctx.quadraticCurveTo(width * 0.3, height * 0.25, width * 0.45, height * 0.3)
  ctx.quadraticCurveTo(width * 0.6, height * 0.28, width * 0.75, height * 0.4)
  ctx.quadraticCurveTo(width * 0.85, height * 0.5, width * 0.78, height * 0.65)
  ctx.quadraticCurveTo(width * 0.68, height * 0.75, width * 0.55, height * 0.72)
  ctx.quadraticCurveTo(width * 0.4, height * 0.78, width * 0.25, height * 0.65)
  ctx.quadraticCurveTo(width * 0.15, height * 0.55, width * 0.2, height * 0.4)
  ctx.fillStyle = 'rgba(40, 100, 60, 0.4)'
  ctx.fill()
  ctx.strokeStyle = 'rgba(64, 158, 255, 0.4)'
  ctx.lineWidth = 1.5
  ctx.stroke()

  // 如果没有数据，显示提示
  if (!points || points.length === 0) {
    ctx.fillStyle = 'rgba(255,255,255,0.3)'
    ctx.font = '16px "Microsoft YaHei"'
    ctx.textAlign = 'center'
    ctx.fillText('暂无热力图数据', width / 2, height / 2)
    return
  }

  // 绘制热力圈
  points.forEach(point => {
    const x = point.longitude ? (point.longitude - 110.3) / 0.5 * width * 0.8 + width * 0.1 : width * (0.2 + Math.random() * 0.5)
    const y = point.latitude ? (21.3 - point.latitude) / 0.4 * height * 0.7 + height * 0.15 : height * (0.2 + Math.random() * 0.5)
    const intensity = point.intensity || 0.5
    const radius = 35 + intensity * 25

    const gradient = ctx.createRadialGradient(x, y, 0, x, y, radius)

    if (intensity > 0.7) {
      gradient.addColorStop(0, 'rgba(245, 108, 108, 0.8)')
      gradient.addColorStop(0.5, 'rgba(245, 108, 108, 0.3)')
      gradient.addColorStop(1, 'rgba(245, 108, 108, 0)')
    } else if (intensity > 0.5) {
      gradient.addColorStop(0, 'rgba(230, 162, 60, 0.7)')
      gradient.addColorStop(0.5, 'rgba(230, 162, 60, 0.3)')
      gradient.addColorStop(1, 'rgba(230, 162, 60, 0)')
    } else {
      gradient.addColorStop(0, 'rgba(64, 158, 255, 0.6)')
      gradient.addColorStop(0.5, 'rgba(64, 158, 255, 0.2)')
      gradient.addColorStop(1, 'rgba(64, 158, 255, 0)')
    }

    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(x, y, radius, 0, Math.PI * 2)
    ctx.fill()

    // 绘制标记点和标签
    ctx.beginPath()
    ctx.arc(x, y, 6, 0, Math.PI * 2)
    ctx.fillStyle = intensity > 0.7 ? '#f56c6c' : (intensity > 0.5 ? '#e6a23c' : '#409eff')
    ctx.fill()
    ctx.beginPath()
    ctx.arc(x, y, 3, 0, Math.PI * 2)
    ctx.fillStyle = 'white'
    ctx.fill()

    // 名称标签
    ctx.fillStyle = 'rgba(10, 14, 39, 0.8)'
    const nameWidth = (point.name || '').length * 12 + 10
    ctx.fillRect(x - nameWidth / 2, y - 28, nameWidth, 20)

    ctx.fillStyle = intensity > 0.7 ? '#f56c6c' : (intensity > 0.5 ? '#e6a23c' : '#409eff')
    ctx.font = 'bold 10px "Microsoft YaHei"'
    ctx.textAlign = 'center'
    ctx.fillText(point.name || '未知', x, y - 14)

    // 人数标签
    ctx.fillStyle = 'rgba(255,255,255,0.7)'
    ctx.font = '9px "Microsoft YaHei"'
    ctx.fillText((point.currentCount || 0) + '人', x, y - 2)
  })
}

// ==================== 交互事件 ====================

const refreshMap = async () => {
  await fetchHeatmapData()
  ElMessage.success('地图数据已刷新')
}

const showMetricDetail = (metric) => {
  ElMessage.info(`${metric.label}详情功能开发中`)
}

const showDeviceDetail = (device) => {
  ElMessageBox.alert(
    `设备名称: ${device.name}\n状态: ${device.statusText}\n数量: ${device.count}台`,
    `${device.name}详情`,
    { confirmButtonText: '确定' }
  )
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminUser')
    ElMessage.success('已退出登录')
    router.push('/admin')
  }
}

// ==================== 窗口大小变化处理 ====================

const handleResize = () => {
  if (areaChart) areaChart.resize()
  if (trendChart) trendChart.resize()
  if (deviceRingChart) deviceRingChart.resize()
  if (hasHeatmapData.value) {
    drawHeatmap(heatmapData.value)
  }
}

// ==================== 定时更新 ====================

let updateTimer = null

const startAutoUpdate = () => {
  updateTimer = setInterval(() => {
    fetchTodayData()
    fetchSpotData()
    fetchHeatmapData()
  }, 30000)
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await nextTick()

  setTimeout(() => {
    initAreaChart()
    initTrendChart()
    initDeviceRingChart()
  }, 100)

  loading.value = true
  try {
    await Promise.all([
      fetchTodayData(),
      fetchSpotData(),
      fetchTrendData(),
      fetchHeatmapData()
    ])
  } catch (error) {
    console.error('初始化数据加载失败:', error)
  } finally {
    loading.value = false
  }

  startAutoUpdate()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (updateTimer) clearInterval(updateTimer)
  if (areaChart) areaChart.dispose()
  if (trendChart) trendChart.dispose()
  if (deviceRingChart) deviceRingChart.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #0f1535 50%, #1a1f45 100%);
  overflow-x: hidden;
}

/* 顶部导航栏 */
.dashboard-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(10, 14, 39, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  z-index: 100;
  border-bottom: 1px solid rgba(64, 158, 255, 0.2);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 28px;
  animation: float 3s ease-in-out infinite;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #fff, #409eff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.nav-area {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background: rgba(64, 158, 255, 0.2);
  color: #409eff;
  transform: translateY(-2px);
}

.nav-link.active {
  background: rgba(64, 158, 255, 0.3);
  color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.user-area {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
}

/* 主内容区 */
.dashboard-main {
  padding-top: 80px;
  padding-left: 20px;
  padding-right: 20px;
  display: grid;
  grid-template-columns: 320px 1fr 360px;
  gap: 20px;
  min-height: calc(100vh - 60px);
}

/* ========== 左侧面板 ========== */
.left-panel {
  background: rgba(15, 21, 53, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  overflow-x: hidden;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.2);
}

.loading-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-left: auto;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: #67c23a;
  border-radius: 50%;
  display: inline-block;
  margin-left: 8px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

/* KPI卡片 - 紧凑三合一 */
.kpi-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.kpi-card {
  background: linear-gradient(135deg, rgba(15, 21, 53, 0.8), rgba(10, 14, 39, 0.6));
  border-radius: 12px;
  padding: 12px 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid rgba(64, 158, 255, 0.15);
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-2px);
  border-color: #409eff;
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.15);
}

.kpi-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.kpi-content {
  flex: 1;
  min-width: 0;
}

.kpi-value {
  font-size: 20px;
  font-weight: bold;
  color: #f5a623;
  line-height: 1.2;
}

.kpi-unit {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-left: 2px;
}

.kpi-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

/* 图表容器 */
.chart-container {
  margin-bottom: 18px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.chart-subtitle {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
  margin-left: auto;
}

.chart {
  width: 100%;
  height: 170px;
}

/* ========== 中央地图区域 ========== */
.map-panel {
  background: rgba(15, 21, 53, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.map-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: bold;
  color: #f5a623;
}

.map-container {
  position: relative;
  width: 100%;
  height: 420px;
  border-radius: 12px;
  overflow: hidden;
  background: #0a2a4a;
}

.heatmap-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.map-empty-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(255, 255, 255, 0.3);
  font-size: 14px;
}

/* 底部指标 */
.bottom-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(64, 158, 255, 0.2);
}

.metric-item {
  text-align: center;
  padding: 8px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.metric-item:hover {
  transform: translateY(-3px);
  background: rgba(64, 158, 255, 0.15);
}

.metric-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6px;
}

.metric-value .value {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.metric-value .trend {
  font-size: 10px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.trend.up { color: #67c23a; }
.trend.down { color: #f56c6c; }

.metric-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 4px;
}

.metric-progress {
  margin-top: 8px;
  height: 3px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 2px;
  transition: width 0.5s ease;
}

/* ========== 右侧面板 ========== */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.alert-panel, .device-panel {
  background: rgba(15, 21, 53, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.alert-badge {
  background: #f56c6c;
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 20px;
  margin-left: 8px;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.alert-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.alert-stat {
  flex: 1;
  text-align: center;
  padding: 10px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
}

.alert-stat .stat-value {
  font-size: 22px;
  font-weight: bold;
  display: block;
}

.alert-stat .stat-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
}

.alert-stat.critical .stat-value { color: #f56c6c; }
.alert-stat.warning .stat-value { color: #e6a23c; }
.alert-stat.info .stat-value { color: #409eff; }

/* 预警点位图 */
.building-preview {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 16px;
}

.preview-title {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot-red {
  width: 6px;
  height: 6px;
  background: #f56c6c;
  border-radius: 50%;
  display: inline-block;
  animation: pulse 1.5s infinite;
}

.preview-canvas {
  position: relative;
  height: 100px;
  background: linear-gradient(135deg, #0a0e27, #1a1f45);
  border-radius: 8px;
  overflow: hidden;
}

.preview-point {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(0, 0, 0, 0.7);
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  white-space: nowrap;
}

.preview-point:hover {
  transform: scale(1.05);
}

.preview-point span { color: white; }

.level-tag {
  font-size: 8px;
  padding: 2px 6px;
  border-radius: 10px;
}

.level-tag.critical { background: #f56c6c; }
.level-tag.warning { background: #e6a23c; }
.level-tag.info { background: #409eff; }

.point-critical { border-left: 3px solid #f56c6c; }
.point-warning { border-left: 3px solid #e6a23c; }
.point-info { border-left: 3px solid #409eff; }

.pulse-ring {
  position: relative;
}

.pulse-ring::after {
  content: '';
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border-radius: 50%;
  border: 2px solid #f56c6c;
  animation: pulseRing 2s infinite;
  pointer-events: none;
}

@keyframes pulseRing {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

/* 告警列表 */
.alert-list {
  max-height: 200px;
  overflow-y: auto;
}

.alert-slide-move,
.alert-slide-enter-active,
.alert-slide-leave-active {
  transition: all 0.3s ease;
}

.alert-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.alert-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 11px;
}

.alert-row.critical { background: rgba(245, 108, 108, 0.1); margin: 0 -8px; padding: 10px 8px; border-radius: 8px; }
.alert-row.warning { background: rgba(230, 162, 60, 0.05); margin: 0 -8px; padding: 10px 8px; border-radius: 8px; }

.alert-level {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 50px;
}

.level-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.level-dot.critical { background: #f56c6c; box-shadow: 0 0 5px #f56c6c; }
.level-dot.warning { background: #e6a23c; }
.level-dot.info { background: #409eff; }

.alert-content {
  flex: 1;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.alert-time { color: rgba(255, 255, 255, 0.4); width: 60px; }
.alert-location { color: rgba(255, 255, 255, 0.7); min-width: 80px; }
.alert-desc { color: rgba(255, 255, 255, 0.5); flex: 1; }

/* ========== 设备面板 - 所有文字改为白色 ========== */
.device-chart {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
}

.ring-chart {
  width: 100px;
  height: 100px;
}

.device-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #ffffff;
}

.stat-color {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.device-stat.online .stat-color { background: #67c23a; }
.device-stat.offline .stat-color { background: #909399; }
.device-stat.warning .stat-color { background: #e6a23c; }

.device-stat .stat-label {
  color: rgba(255, 255, 255, 0.8);
}

.device-stat .stat-value {
  margin-left: auto;
  font-weight: bold;
  color: #ffffff;
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #ffffff;
}

.device-item:hover {
  background: rgba(64, 158, 255, 0.15);
  transform: translateX(3px);
}

.device-item .el-icon {
  color: rgba(255, 255, 255, 0.8);
}

.device-item.online .device-status { 
  color: #67c23a;
}
.device-item.warning .device-status { 
  color: #e6a23c;
}

.device-name { 
  flex: 1;
  color: #ffffff;
}

.device-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot.online { background: #67c23a; }
.status-dot.warning { background: #e6a23c; }

/* 滚动日志 */
.log-scroll {
  overflow: hidden;
  position: relative;
  height: 100px;
}

.log-track {
  animation: scrollLog 20s linear infinite;
}

.log-track:hover {
  animation-play-state: paused;
}

.log-item {
  padding: 5px 0;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.log-time {
  color: rgba(64, 158, 255, 0.7);
  margin-right: 10px;
}

@keyframes scrollLog {
  0% { transform: translateY(0); }
  100% { transform: translateY(-50%); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.modern-btn {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.modern-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

/* 响应式 */
@media (max-width: 1300px) {
  .dashboard-main {
    grid-template-columns: 300px 1fr 340px;
    gap: 15px;
  }
}

@media (max-width: 1100px) {
  .dashboard-main {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .left-panel {
    order: 1;
  }
  
  .map-panel {
    order: 2;
  }
  
  .right-panel {
    order: 3;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
  
  .map-container {
    height: 350px;
  }
}

@media (max-width: 768px) {
  .right-panel {
    grid-template-columns: 1fr;
  }
  
  .dashboard-header {
    padding: 0 15px;
  }
  
  .nav-text {
    display: none;
  }
  
  .nav-link {
    padding: 8px 12px;
  }
}
</style>