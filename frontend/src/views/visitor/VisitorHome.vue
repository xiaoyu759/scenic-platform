<template>
  <div>
    <VisitorHeader />
    <div class="home-container">
      <div class="home-wrapper">
        <!-- 图片轮播区 -->
        <div class="carousel-section fade-in">
          <el-carousel :interval="4000" height="450px" trigger="click" arrow="always">
            <el-carousel-item v-for="(item, index) in banners" :key="index">
              <div class="carousel-bg" :style="{ backgroundImage: `url(${item.image})` }">
                <div class="carousel-overlay"></div>
                <div class="carousel-text">
                  <h2 class="slide-up">{{ item.title }}</h2>
                  <p class="slide-up delay-1">{{ item.subtitle }}</p>
                  <el-button type="primary" round size="large" @click="goTo('/ticket')" class="carousel-btn slide-up delay-2">
                    立即购票
                  </el-button>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <!-- 主内容区：三列布局 -->
        <div class="main-content">
          <!-- 左侧：实时景点热力图 -->
          <div class="heatmap-section fade-in-up">
            <div class="section-header">
              <h3>
                <el-icon><Location /></el-icon>
                特呈岛实时热力图
                <span class="pulse-dot"></span>
              </h3>
              <span class="update-time">数据实时更新</span>
            </div>
            
            <div class="heatmap-stats">
              <div class="stat-item" v-for="(stat, idx) in statsCards" :key="idx" :style="{ animationDelay: `${idx * 0.1}s` }">
                <div class="stat-value">
                  <span class="count-up">{{ stat.value }}</span>
                  <span class="stat-unit">{{ stat.unit }}</span>
                </div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-trend" :class="stat.trendClass">{{ stat.trend }}</div>
                <div class="stat-chart">
                  <div class="spark-line" :style="{ width: stat.sparkWidth }"></div>
                </div>
              </div>
            </div>

            <div class="heatmap-map">
              <div id="heatmapMiniMap" class="mini-map"></div>
              <div class="crowd-legend">
                <div class="legend-item" v-for="legend in crowdLegend" :key="legend.level">
                  <span class="legend-color" :class="legend.colorClass"></span>
                  <span>{{ legend.label }}</span>
                </div>
              </div>
            </div>

            <!-- 特呈岛景点拥挤度列表 -->
            <div class="crowd-list">
              <div v-for="(spot, idx) in crowdSpots" :key="spot.id" class="crowd-item" :style="{ animationDelay: `${idx * 0.05}s` }">
                <span class="spot-name">
                  {{ spot.name }}
                  <span class="spot-status" :class="getStatusClass(spot.crowd)"></span>
                </span>
                <div class="crowd-bar">
                  <div class="crowd-fill" :style="{ width: spot.crowd + '%', backgroundColor: getCrowdColor(spot.crowd) }">
                    <div class="crowd-wave"></div>
                  </div>
                </div>
                <span class="crowd-percent">
                  {{ spot.crowd.toFixed(1) }}%
                  <span class="trend-icon" :class="getTrendIcon(spot)">↗</span>
                </span>
              </div>
            </div>
          </div>

          <!-- 中间：特呈岛景点推荐卡片 -->
          <div class="recommend-section fade-in-up">
            <div class="section-header">
              <h3><el-icon><Star /></el-icon>特呈岛热门推荐</h3>
              <el-button link type="primary" @click="goTo('/guide')">查看更多 ></el-button>
            </div>
            <div class="recommend-cards">
              <div v-for="(spot, idx) in recommendSpots" :key="spot.id" class="recommend-card" @click="goToSpot(spot)">
                <div class="card-image">
                  <img :src="spot.image" :alt="spot.name" class="card-img">
                  <div class="card-tag">{{ spot.tag }}</div>
                  <div class="card-overlay">
                    <span class="view-detail">查看详情 →</span>
                  </div>
                </div>
                <div class="card-content">
                  <h4>{{ spot.name }}</h4>
                  <div class="card-rating">
                    <el-rate v-model="spot.rating" disabled show-score text-color="#ff9900" score-template="{value}" />
                    <span class="distance">| {{ spot.distance }}</span>
                  </div>
                  <p class="card-reason">{{ spot.reason }}</p>
                  <div class="card-hover-effect"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：实时数据模块（湛江/特呈岛） -->
          <div class="live-section">
            <!-- 湛江今日天气 -->
            <div class="weather-card fade-in-up">
              <div class="section-header">
                <h3><el-icon><Sunny /></el-icon>湛江今日天气</h3>
              </div>
              <div class="weather-content">
                <div class="weather-icon">
                  <div class="weather-animation">
                    <span class="sun">☀️</span>
                    <span class="cloud">⛅</span>
                  </div>
                </div>
                <div class="weather-info">
                  <div class="temp">
                    <span class="temp-value">{{ weather.temp }}</span>
                    <span class="temp-unit">°C</span>
                  </div>
                  <div class="desc">{{ weather.desc }}</div>
                  <div class="air-quality">
                    空气质量：{{ weather.airQuality }}
                    <span class="air-badge" :class="getAirClass()"></span>
                  </div>
                </div>
              </div>
              <div class="weather-forecast">
                <div class="forecast-item" v-for="day in forecast" :key="day.day">
                  <span>{{ day.day }}</span>
                  <span>{{ day.temp }}°</span>
                  <span>{{ day.icon }}</span>
                </div>
              </div>
            </div>

            <!-- 特呈岛入园排队 -->
            <div class="queue-card fade-in-up">
              <div class="section-header">
                <h3><el-icon><User /></el-icon>特呈岛入园排队</h3>
              </div>
              <div class="queue-content">
                <div class="queue-icon">🚢</div>
                <div class="queue-info">
                  <div class="queue-time">当前轮渡等待 <span class="highlight">{{ queue.waitTime }}</span> 分钟</div>
                  <div class="queue-people">排队人数：{{ queue.peopleCount }} 人</div>
                  <div class="queue-progress">
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: queueProgress + '%' }"></div>
                    </div>
                    <span class="progress-label">预计{{ queue.estimateTime }}分钟后登船</span>
                  </div>
                  <div class="queue-tip">💡 建议：{{ queue.tip }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部：游客实时评价 -->
        <div class="reviews-section fade-in-up">
          <div class="section-header">
            <h3><el-icon><ChatDotRound /></el-icon>游客真实评价</h3>
            <span class="review-count">共 {{ originalReviews.length }} 条评价</span>
          </div>
          <div class="reviews-scroll">
            <div class="reviews-track">
              <div v-for="(review, index) in duplicatedReviews" :key="index" class="review-card">
                <div class="review-avatar">
                  <el-avatar :size="40">{{ review.user.charAt(0) }}</el-avatar>
                </div>
                <div class="review-content">
                  <div class="review-user">
                    <span class="username">{{ review.user }}</span>
                    <el-rate v-model="review.rating" disabled size="small" />
                  </div>
                  <p class="review-text">“{{ review.content }}”</p>
                  <span class="review-time">{{ review.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <VisitorFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Star, Sunny, User, ChatDotRound } from '@element-plus/icons-vue'
import VisitorHeader from '@/components/VisitorHeader.vue'
import VisitorFooter from '@/components/VisitorFooter.vue'

import img8 from '@/components/images/8.png'
import img5 from '@/components/images/5.png'
import img9 from '@/components/images/9.png'

import recommendImg1 from '@/components/images/5.png'
import recommendImg2 from '@/components/images/8.png'
import recommendImg3 from '@/components/images/9.png'
import recommendImg4 from '@/components/images/11.png'
import recommendImg5 from '@/components/images/4.png'

const router = useRouter()

// 特呈岛轮播图图片
const banners = ref([
   { 
    image: img8, 
    title: '欢迎来到特呈岛', 
    subtitle: '体验海岛慢生活' 
  },
  { 
    image: img5, 
    title: '红树林奇观', 
    subtitle: '500年古红树林，白鹭飞舞' 
  },
  { 
    image: img9, 
    title: '天然温泉', 
    subtitle: '放松身心，养生度假' 
  }
])

// 特呈岛统计数据（基于实际承载量）
const todayVisitors = ref(1846)
const currentVisitors = ref(847)
const capacityRate = ref(42)

// 统计卡片数据
const statsCards = computed(() => [
  { value: formatNumber(todayVisitors.value), unit: '', label: '今日登岛', trend: '↑ 8%', trendClass: 'trend-up', sparkWidth: '75%' },
  { value: formatNumber(currentVisitors.value), unit: '', label: '当前在岛', trend: '较昨日 +3%', trendClass: 'trend-up', sparkWidth: '55%' },
  { value: capacityRate.value.toFixed(1), unit: '%', label: '承载率', trend: '舒适游览', trendClass: 'trend-steady', sparkWidth: '42%' }
])

// 特呈岛景点拥挤度数据
const crowdSpots = ref([
  { id: 1, name: '红树林栈道', crowd: 85.3, prevCrowd: 82.1 },
  { id: 2, name: '温泉度假村', crowd: 72.8, prevCrowd: 70.5 },
  { id: 3, name: '红姜石海滩', crowd: 68.2, prevCrowd: 65.8 },
  { id: 4, name: '渔村文化村', crowd: 45.6, prevCrowd: 48.2 },
  { id: 5, name: '冼太庙', crowd: 38.4, prevCrowd: 36.9 },
  { id: 6, name: '观海长廊', crowd: 32.1, prevCrowd: 30.5 },
  { id: 7, name: '八音桥', crowd: 28.5, prevCrowd: 26.8 }
])

// 拥挤度图例
const crowdLegend = [
  { level: 'extreme', label: '拥挤 (>80%)', colorClass: 'extreme' },
  { level: 'high', label: '较挤 (60%-80%)', colorClass: 'high' },
  { level: 'medium', label: '正常 (30%-60%)', colorClass: 'medium' },
  { level: 'low', label: '舒适 (<30%)', colorClass: 'low' }
]

// 特呈岛推荐景点
const recommendSpots = ref([
  {
    id: 1,
    name: '红树林栈道',
    image: recommendImg1,
    rating: 4.9,
    distance: '步行0.5km',
    reason: '红树林奇观，白鹭飞舞',
    tag: '🔥 必去'
  },
  {
    id: 2,
    name: '温泉度假村',
    image: recommendImg2,
    rating: 4.8,
    distance: '步行1.2km',
    reason: '天然温泉，放松身心',
    tag: '♨️ 推荐'
  },
  {
    id: 3,
    name: '渔村文化村',
    image: recommendImg3,
    rating: 4.7,
    distance: '骑行0.8km',
    reason: '疍家文化，渔家体验',
    tag: '🏮 人文'
  },
  {
    id: 4,
    name: '红姜石海滩',
    image: recommendImg4,
    rating: 4.8,
    distance: '步行0.3km',
    reason: '赶海挖蚝，乐趣无穷',
    tag: '🌊 体验'
  },
  {
    id: 5,
    name: '观海长廊',
    image: recommendImg5,
    rating: 4.6,
    distance: '骑行1.0km',
    reason: '日出日落，绝美观景',
    tag: '🌅 观景'
  }
])

// 湛江天气数据（实时更新）
const weather = ref({ 
  type: 'sunny', 
  temp: 26, 
  desc: '晴转多云', 
  airQuality: '优' 
})

const forecast = ref([
  { day: '周一', temp: 27, icon: '☀️' }, 
  { day: '周二', temp: 28, icon: '☀️' }, 
  { day: '周三', temp: 26, icon: '⛅' }
])

// 特呈岛轮渡排队数据
const queue = ref({ 
  waitTime: 15, 
  peopleCount: 48, 
  tip: '避开10:00-12:00和14:00-16:00高峰时段', 
  estimateTime: 10 
})
const queueProgress = computed(() => Math.min(100, (queue.value.waitTime / 30) * 100))

// 游客真实评价
const originalReviews = ref([
  { user: '广州李小姐', rating: 5, content: '红树林栈道太美了，白鹭成群，拍照超出片！', time: '2分钟前' },
  { user: '深圳陈先生', rating: 5, content: '温泉很舒服，海水温泉第一次体验，值得再来', time: '5分钟前' },
  { user: '佛山王女士', rating: 4.5, content: '带孩子赶海挖蚝，玩得不亦乐乎，强烈推荐', time: '10分钟前' },
  { user: '珠海张先生', rating: 5, content: '岛上的海鲜很新鲜，渔村文化体验很有意思', time: '15分钟前' },
  { user: '中山刘小姐', rating: 4.5, content: '观海长廊看日落绝美，情侣约会圣地', time: '20分钟前' },
  { user: '东莞黄先生', rating: 5, content: '从霞山码头坐船15分钟就到，很方便', time: '25分钟前' }
])

// 复制多份评价数据让滚动衔接更流畅
const duplicatedReviews = computed(() => {
  return [...originalReviews.value, ...originalReviews.value, ...originalReviews.value]
})

// 工具函数
const formatNumber = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  return num.toString()
}

const getCrowdColor = (crowd) => {
  if (crowd >= 80) return '#f56c6c'
  if (crowd >= 60) return '#e6a23c'
  if (crowd >= 30) return '#409eff'
  return '#67c23a'
}

const getStatusClass = (crowd) => {
  if (crowd >= 80) return 'status-critical'
  if (crowd >= 60) return 'status-warning'
  if (crowd >= 30) return 'status-normal'
  return 'status-good'
}

const getTrendIcon = (spot) => {
  return spot.crowd > spot.prevCrowd ? 'trend-up' : 'trend-down'
}

const getAirClass = () => {
  if (weather.value.airQuality === '优') return 'air-good'
  if (weather.value.airQuality === '良') return 'air-normal'
  return 'air-bad'
}

const goTo = (path) => { router.push(path) }
const goToSpot = (spot) => { router.push('/guide') }

// 热力图初始化
let timer = null
let mapInterval = null

const initMiniMap = () => {
  const container = document.getElementById('heatmapMiniMap')
  if (!container) return
  container.innerHTML = ''
  
  const canvas = document.createElement('canvas')
  const width = container.clientWidth
  const height = 200
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  
  ctx.fillStyle = '#e8f4f8'
  ctx.fillRect(0, 0, width, height)
  
  ctx.strokeStyle = '#c8dce8'
  ctx.lineWidth = 0.5
  for (let i = 0; i <= 4; i++) {
    ctx.beginPath()
    ctx.moveTo(0, i * height / 4)
    ctx.lineTo(width, i * height / 4)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(i * width / 4, 0)
    ctx.lineTo(i * width / 4, height)
    ctx.stroke()
  }
  
  // 特呈岛景点位置（相对坐标）
  const spotsPos = [
    { x: 0.25, y: 0.35, crowd: crowdSpots.value[0].crowd },
    { x: 0.55, y: 0.25, crowd: crowdSpots.value[1].crowd },
    { x: 0.40, y: 0.55, crowd: crowdSpots.value[2].crowd },
    { x: 0.20, y: 0.70, crowd: crowdSpots.value[3].crowd },
    { x: 0.35, y: 0.80, crowd: crowdSpots.value[4].crowd },
    { x: 0.70, y: 0.45, crowd: crowdSpots.value[5].crowd },
    { x: 0.50, y: 0.60, crowd: crowdSpots.value[6].crowd }
  ]
  
  spotsPos.forEach((spot) => {
    const x = spot.x * width
    const y = spot.y * height
    const crowd = spot.crowd
    const radius = 20 + (crowd / 10)
    
    let color
    if (crowd >= 80) color = 'rgba(245, 108, 108, 0.55)'
    else if (crowd >= 60) color = 'rgba(230, 162, 60, 0.55)'
    else if (crowd >= 30) color = 'rgba(64, 158, 255, 0.55)'
    else color = 'rgba(103, 194, 58, 0.55)'
    
    for (let i = 3; i >= 1; i--) {
      ctx.beginPath()
      ctx.arc(x, y, radius + i * 3, 0, Math.PI * 2)
      ctx.fillStyle = color.replace('0.55', 0.15 / i)
      ctx.fill()
    }
    
    ctx.beginPath()
    ctx.arc(x, y, radius, 0, Math.PI * 2)
    ctx.fillStyle = color
    ctx.fill()
    
    ctx.beginPath()
    ctx.arc(x - 5, y - 5, 5, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255,255,255,0.4)'
    ctx.fill()
  })
  
  const names = ['红树林栈道', '温泉度假村', '红姜石海滩', '渔村文化村', '冼太庙', '观海长廊', '八音桥']
  spotsPos.forEach((spot, idx) => {
    const x = spot.x * width
    const y = spot.y * height - 22
    ctx.font = '10px "Microsoft YaHei"'
    ctx.fillStyle = '#333'
    ctx.fillText(names[idx], x - 20, y)
    
    ctx.font = '10px "Microsoft YaHei"'
    ctx.fillStyle = getCrowdColor(spot.crowd)
    ctx.fillText(spot.crowd.toFixed(1) + '%', x - 15, y + 12)
  })
  
  container.appendChild(canvas)
  canvas.style.cursor = 'pointer'
  canvas.onclick = () => router.push('/route-plan')
}

// 模拟实时数据更新
const startDataUpdate = () => {
  mapInterval = setInterval(() => {
    crowdSpots.value.forEach(spot => {
      spot.prevCrowd = spot.crowd
      let newCrowd = spot.crowd + (Math.random() - 0.5) * 6
      newCrowd = Math.min(95, Math.max(15, newCrowd))
      spot.crowd = parseFloat(newCrowd.toFixed(1))
    })
    initMiniMap()
  }, 15000)
  
  timer = setInterval(() => {
    currentVisitors.value = Math.floor(Math.random() * 600) + 500
    capacityRate.value = parseFloat((currentVisitors.value / 2000 * 100).toFixed(1))
    queue.value.waitTime = Math.floor(Math.random() * 15) + 8
    queue.value.peopleCount = queue.value.waitTime * 3
    queue.value.estimateTime = Math.max(5, Math.floor(queue.value.waitTime * 0.7))
  }, 10000)
}

const handleResize = () => setTimeout(() => initMiniMap(), 200)

onMounted(() => {
  setTimeout(() => initMiniMap(), 300)
  startDataUpdate()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (mapInterval) clearInterval(mapInterval)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.home-container {
  background: linear-gradient(135deg, #e8f4f8 0%, #d4eaf0 50%, #c5e3ea 100%);
  min-height: 100vh;
}

.home-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 入场动画 */
.fade-in { animation: fadeIn 0.6s ease forwards; }
.fade-in-up { animation: fadeInUp 0.6s ease forwards; opacity: 0; animation-fill-mode: forwards; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
.slide-up { animation: slideUp 0.5s ease forwards; opacity: 0; }
.delay-1 { animation-delay: 0.2s; }
.delay-2 { animation-delay: 0.4s; }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

/* 轮播区 */
.carousel-section {
  margin-bottom: 30px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}
.carousel-bg {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}
.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
}
.carousel-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
  z-index: 2;
  width: 100%;
}
.carousel-text h2 { font-size: 48px; margin-bottom: 16px; text-shadow: 2px 2px 4px rgba(0,0,0,0.3); }
.carousel-text p { font-size: 20px; margin-bottom: 24px; text-shadow: 1px 1px 2px rgba(0,0,0,0.3); }
.carousel-btn { background: rgba(255,255,255,0.9); color: #667eea; border: none; font-weight: bold; transition: all 0.3s ease; }
.carousel-btn:hover { background: white; transform: translateY(-2px); }

/* 主内容区三列布局 */
.main-content { display: grid; grid-template-columns: 1.2fr 1.5fr 0.9fr; gap: 24px; margin-bottom: 30px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid rgba(102, 126, 234, 0.3); }
.section-header h3 { display: flex; align-items: center; gap: 8px; font-size: 18px; color: #2c3e50; margin: 0; }

/* 热力图区域 */
.heatmap-section {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}
.heatmap-section:hover { transform: translateY(-5px); box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12); }
.pulse-dot { width: 8px; height: 8px; background: #67c23a; border-radius: 50%; display: inline-block; margin-left: 8px; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.5; transform: scale(1.2); } }

.heatmap-stats { display: flex; gap: 12px; margin-bottom: 20px; }
.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px 8px;
  background: linear-gradient(135deg, #f8f9fc 0%, #f0f4f9 100%);
  border-radius: 16px;
  transition: all 0.3s ease;
  animation: fadeInUp 0.4s ease forwards;
  opacity: 0;
}
.stat-item:hover { transform: translateY(-3px); background: linear-gradient(135deg, #f0f4f9 0%, #e8ecf3 100%); }
.stat-value { font-size: 24px; font-weight: bold; color: #667eea; }
.stat-unit { font-size: 14px; }
.stat-label { font-size: 12px; color: #666; margin-top: 4px; }
.stat-trend { font-size: 10px; margin-top: 2px; }
.trend-up { color: #67c23a; }
.trend-steady { color: #409eff; }
.stat-chart { margin-top: 8px; height: 2px; background: #e8e8e8; border-radius: 2px; overflow: hidden; }
.spark-line { height: 100%; background: linear-gradient(90deg, #667eea, #764ba2); border-radius: 2px; transition: width 0.5s ease; }

.heatmap-map { background: #f0f7fa; border-radius: 16px; overflow: hidden; margin-bottom: 16px; }
.mini-map { width: 100%; height: 200px; }
.mini-map canvas { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease; }
.mini-map canvas:hover { transform: scale(1.02); }
.crowd-legend { display: flex; justify-content: space-around; padding: 12px; background: #f8f9fc; border-radius: 12px; margin-top: 12px; flex-wrap: wrap; gap: 10px; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #666; }
.legend-color { width: 16px; height: 16px; border-radius: 4px; }
.legend-color.extreme { background: #f56c6c; }
.legend-color.high { background: #e6a23c; }
.legend-color.medium { background: #409eff; }
.legend-color.low { background: #67c23a; }

.crowd-list { margin-top: 16px; }
.crowd-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  animation: fadeInUp 0.3s ease forwards;
  opacity: 0;
}
.crowd-item:hover { background: #f8f9fc; transform: translateX(5px); }
.crowd-item .spot-name { width: 95px; font-size: 13px; color: #333; display: flex; align-items: center; gap: 5px; }
.spot-status { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.status-critical { background: #f56c6c; box-shadow: 0 0 5px #f56c6c; animation: blink 1s infinite; }
.status-warning { background: #e6a23c; }
.status-normal { background: #409eff; }
.status-good { background: #67c23a; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

.crowd-bar { flex: 1; height: 8px; background: #e8e8e8; border-radius: 4px; overflow: hidden; }
.crowd-fill { height: 100%; border-radius: 4px; transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden; }
.crowd-wave { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent); animation: wave 2s infinite; }
@keyframes wave { 0% { transform: translateX(-100%); } 100% { transform: translateX(100%); } }
.crowd-percent { width: 55px; font-size: 12px; color: #666; text-align: right; }
.trend-icon { display: inline-block; margin-left: 3px; font-size: 10px; }
.trend-up { color: #f56c6c; transform: rotate(0deg); }
.trend-down { color: #67c23a; transform: rotate(180deg); }

/* 推荐卡片区域 */
.recommend-section {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}
.recommend-section:hover { transform: translateY(-5px); box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12); }

.recommend-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-card {
  display: flex;
  background: #f8f9fc;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}
.recommend-card:hover {
  transform: translateX(8px) scale(1.01);
  background: #f0f4f9;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}
.card-image {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.recommend-card:hover .card-img { transform: scale(1.1); }
.card-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0,0,0,0.7);
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 12px;
  z-index: 1;
}
.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(102, 126, 234, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}
.recommend-card:hover .card-overlay { opacity: 1; }
.view-detail { color: white; font-size: 12px; font-weight: bold; transform: translateY(10px); transition: transform 0.3s ease; }
.recommend-card:hover .view-detail { transform: translateY(0); }
.card-content {
  flex: 1;
  padding: 12px;
  position: relative;
  overflow: hidden;
}
.card-content h4 { margin: 0 0 6px 0; font-size: 15px; color: #333; transition: color 0.3s ease; }
.recommend-card:hover .card-content h4 { color: #667eea; }
.card-rating { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; flex-wrap: wrap; }
.card-rating :deep(.el-rate) { height: 16px; }
.card-rating :deep(.el-rate__star) { font-size: 12px; }
.distance { font-size: 11px; color: #999; }
.card-reason { font-size: 12px; color: #667eea; margin: 0; }
.card-hover-effect {
  position: absolute;
  bottom: 0;
  left: -100%;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  transition: left 0.4s ease;
}
.recommend-card:hover .card-hover-effect { left: 0; }

/* 右侧实时数据 */
.live-section { display: flex; flex-direction: column; gap: 20px; }
.weather-card, .queue-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}
.weather-card:hover, .queue-card:hover { transform: translateY(-5px); box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12); }

.weather-content, .queue-content { display: flex; align-items: center; gap: 20px; }
.weather-icon { font-size: 56px; }
.weather-animation { position: relative; width: 60px; height: 60px; }
.sun { position: absolute; animation: rotate 10s linear infinite; }
.cloud { position: absolute; animation: float 3s ease-in-out infinite; }
@keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }

.weather-info .temp { font-size: 32px; font-weight: bold; color: #333; }
.air-quality { font-size: 12px; margin-top: 4px; }
.air-good { color: #67c23a; }
.weather-forecast { display: flex; justify-content: space-between; margin-top: 16px; padding-top: 12px; border-top: 1px solid #eee; }
.forecast-item { display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 12px; color: #666; }

.queue-time { font-size: 14px; color: #666; }
.queue-time .highlight { font-size: 28px; font-weight: bold; color: #e6a23c; }
.queue-progress { margin: 8px 0; }
.progress-bar { height: 6px; background: #e8e8e8; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #e6a23c, #f0a64a); border-radius: 3px; transition: width 0.5s ease; position: relative; overflow: hidden; }
.progress-fill::after { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent); animation: shimmer 1.5s infinite; }
@keyframes shimmer { 0% { transform: translateX(-100%); } 100% { transform: translateX(100%); } }
.progress-label { font-size: 10px; color: #999; margin-top: 4px; display: block; }
.queue-tip { font-size: 12px; color: #999; margin-top: 8px; padding: 6px 10px; background: #f8f9fc; border-radius: 8px; }

/* 评价区域 */
.reviews-section {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}
.review-count { font-size: 12px; color: #999; }
.reviews-scroll { overflow: hidden; position: relative; }
.reviews-track {
  display: flex;
  gap: 20px;
  animation: scrollReviews 40s linear infinite;
  width: fit-content;
}
.reviews-track:hover { animation-play-state: paused; }
@keyframes scrollReviews {
  0% { transform: translateX(0); }
  100% { transform: translateX(-33.33%); }
}
.review-card {
  min-width: 280px;
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 16px;
  transition: all 0.3s ease;
}
.review-card:hover { transform: translateY(-3px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); background: #f0f4f9; }
.review-user { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }
.username { font-weight: bold; color: #333; font-size: 14px; }
.review-text { font-size: 14px; color: #555; margin: 8px 0; line-height: 1.4; }
.review-time { font-size: 11px; color: #999; }

/* 响应式 */
@media (max-width: 1100px) {
  .main-content { grid-template-columns: 1fr; gap: 20px; }
  .recommend-cards {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  .recommend-card { flex-direction: column; }
  .card-image { width: 100%; height: 140px; }
}
@media (max-width: 700px) {
  .recommend-cards { grid-template-columns: 1fr; }
  .carousel-text h2 { font-size: 28px; }
  .carousel-text p { font-size: 14px; }
  .heatmap-stats { flex-wrap: wrap; }
  .stat-item { min-width: 100px; }
}
</style>