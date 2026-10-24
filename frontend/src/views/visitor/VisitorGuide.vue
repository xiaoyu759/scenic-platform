<template>
  <div>
    <VisitorHeader />
    <div class="guide-container">
      <div class="guide-wrapper">
        <!-- 左侧百度地图 -->
        <div class="guide-left">
          <div ref="mapContainer" class="guide-map"></div>
          <div v-if="mapError" class="map-error-tip">
            <el-alert type="error" :title="mapError" show-icon :closable="false" />
            <el-button size="small" @click="retryInitMap" style="margin-top: 10px;">重试加载</el-button>
          </div>
          <div v-if="mapLoaded" class="map-tip fade-in">
            <span>📍 共 {{ markersCount }} 个景点标记 | 点击标记查看详情</span>
          </div>
          <div class="map-legend fade-in">
            <div class="legend-title">图例</div>
            <div class="legend-item"><span class="legend-color transport"></span>交通</div>
            <div class="legend-item"><span class="legend-color culture"></span>文化</div>
            <div class="legend-item"><span class="legend-color scenery"></span>景观</div>
            <div class="legend-item"><span class="legend-color nature"></span>自然</div>
            <div class="legend-item"><span class="legend-color beach"></span>海滩</div>
            <div class="legend-item"><span class="legend-color hotel"></span>温泉</div>
          </div>
        </div>

        <!-- 右侧景点列表 -->
        <div class="guide-right">
          <div class="guide-header fade-in-down">
            <h3>🏝️ 特呈岛景区导览</h3>
            <div class="search-row">
              <el-input 
                v-model="searchText" 
                placeholder="搜索景点名称" 
                prefix-icon="Search" 
                clearable
                @keyup.enter="searchSpot"
              />
              <el-button type="primary" @click="searchSpot">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="getUserLocation" :loading="locating">
                <el-icon><Location /></el-icon>
                我的位置
              </el-button>
            </div>
          </div>

          <div class="spots-list" v-loading="loading">
            <div class="section-title">
              景点列表（共 {{ filteredSpots.length }} 个）
              <el-button type="primary" link size="small" @click="resetView">重置视图</el-button>
            </div>
            <div v-for="(spot, idx) in filteredSpots" :key="spot.id" class="spot-card" @click="focusSpot(spot)" :style="{ animationDelay: `${idx * 0.05}s` }">
              <div class="spot-info">
                <div class="spot-name">
                  {{ spot.name }}
                  <span class="spot-type" :class="spot.category">{{ getCategoryText(spot.category) }}</span>
                </div>
                <div class="spot-desc">{{ spot.description }}</div>
                <div class="spot-meta">
                  <span>⏱️ {{ spot.stayTime || 30 }}分钟</span>
                  <span v-if="spot.lat && spot.lng" class="located">✓ 已定位</span>
                  <span v-else class="pending">⏳ 定位中</span>
                </div>
              </div>
              <div class="spot-locate">
                <el-icon><Location /></el-icon>
              </div>
            </div>
          </div>

          <div class="facilities fade-in-up">
            <div class="section-title">服务设施</div>
            <div class="facility-buttons">
              <el-button size="small" @click="showFacility('toilet')">🚻 卫生间</el-button>
              <el-button size="small" @click="showFacility('restaurant')">🍔 餐饮</el-button>
              <el-button size="small" @click="showFacility('parking')">🅿️ 停车场</el-button>
              <el-button size="small" @click="showFacility('ferry')">⛴️ 轮渡</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <VisitorFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Location } from '@element-plus/icons-vue'
import VisitorHeader from '@/components/VisitorHeader.vue'
import VisitorFooter from '@/components/VisitorFooter.vue'
import { getSpotList } from '@/api/visitor'

// 百度地图相关
let map = null
let markers = []
let markerLabels = []
let infoWindow = null
let geocoder = null

const mapContainer = ref(null)
const searchText = ref('')
const mapError = ref('')
const mapLoaded = ref(false)
const locating = ref(false)
const markersCount = ref(0)
const loading = ref(false)

// 景点数据 - 从后端获取
const spots = ref([])

// 分类映射
const categoryMap = {
  transport: '🚢 交通',
  culture: '🏮 文化',
  scenery: '🌉 景观',
  nature: '🌿 自然',
  beach: '🏖️ 海滩',
  hotel: '♨️ 温泉'
}

const getCategoryText = (category) => {
  return categoryMap[category] || '📍 景点'
}

// 过滤后的景点
const filteredSpots = computed(() => {
  if (!searchText.value) return spots.value
  return spots.value.filter(s => 
    s.name.includes(searchText.value) || 
    (s.address && s.address.includes(searchText.value))
  )
})

// ========== 获取景点数据 ==========
const fetchSpots = async () => {
  loading.value = true
  try {
    const res = await getSpotList()
    if (res.code === 200) {
      spots.value = (res.data || []).map(s => ({
        ...s,
        lat: s.latitude ? parseFloat(s.latitude) : null,
        lng: s.longitude ? parseFloat(s.longitude) : null,
        duration: s.stayTime || 30
      }))
    }
  } catch (error) {
    console.error('获取景点列表失败:', error)
    ElMessage.error('获取景点列表失败')
  } finally {
    loading.value = false
  }
}

const resetView = () => {
  if (map && window.BMapGL) {
    const centerPoint = new window.BMapGL.Point(110.4345, 21.149)
    map.centerAndZoom(centerPoint, 14)
    ElMessage.info('已重置地图视图')
  }
}

const searchSpot = () => {
  if (!searchText.value.trim()) {
    ElMessage.warning('请输入景点名称')
    return
  }
  
  const found = spots.value.find(s => s.name.includes(searchText.value) || (s.address && s.address.includes(searchText.value)))
  if (found && found.lat && found.lng) {
    focusSpot(found)
  } else if (found) {
    ElMessage.info(`正在获取 ${found.name} 的位置信息...`)
    geocodeSpot(found)
    setTimeout(() => {
      if (found.lat && found.lng) focusSpot(found)
    }, 2000)
  } else {
    ElMessage.warning('未找到相关景点')
  }
}

// ========== 获取用户当前位置 ==========
const getUserLocation = () => {
  if (!map || !window.BMapGL) {
    ElMessage.warning('地图尚未加载完成')
    return
  }
  
  locating.value = true
  ElMessage.info('正在获取您的位置...')
  
  const geolocation = new window.BMapGL.Geolocation()
  
  geolocation.getCurrentPosition(
    (result) => {
      locating.value = false
      if (result && result.point) {
        const point = result.point
        map.centerAndZoom(point, 16)
        
        const userMarker = new window.BMapGL.Marker(point)
        map.addOverlay(userMarker)
        
        const label = new window.BMapGL.Label('我的位置', {
          position: point,
          offset: new window.BMapGL.Size(20, -30)
        })
        label.setStyle({
          color: '#0f4c81',
          fontSize: '12px',
          fontWeight: 'bold',
          background: 'rgba(255,255,255,0.9)',
          padding: '2px 8px',
          borderRadius: '20px',
          border: '1px solid #0f4c81'
        })
        map.addOverlay(label)
        
        ElMessage.success('已定位到您的位置')
      } else {
        ElMessage.error('定位失败，请检查GPS权限')
      }
    },
    (error) => {
      locating.value = false
      console.error('定位错误:', error)
      switch(error.code) {
        case error.PERMISSION_DENIED:
          ElMessage.error('请允许定位权限后重试')
          break
        case error.POSITION_UNAVAILABLE:
          ElMessage.error('无法获取您的位置，请检查GPS信号')
          break
        case error.TIMEOUT:
          ElMessage.error('定位超时')
          break
        default:
          ElMessage.error('定位失败')
      }
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

const retryInitMap = () => {
  mapError.value = ''
  initMap()
}

const loadBaiduMap = () => {
  return new Promise((resolve, reject) => {
    if (window.BMapGL) {
      resolve(window.BMapGL)
      return
    }
    
    if (window.__baiduLoading) {
      const checkInterval = setInterval(() => {
        if (window.BMapGL) {
          clearInterval(checkInterval)
          resolve(window.BMapGL)
        }
      }, 100)
      return
    }
    
    window.__baiduLoading = true
    
    window.__baiduInitCallback = () => {
      delete window.__baiduLoading
      if (window.BMapGL) {
        resolve(window.BMapGL)
      } else {
        reject(new Error('BMapGL 未定义'))
      }
    }
    
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = 'https://api.map.baidu.com/res/webgl/10/bmap.css'
    document.head.appendChild(link)
    
    const script = document.createElement('script')
    script.src = 'https://api.map.baidu.com/api?v=1.0&type=webgl&ak=fCCdVP0al6oiJ5RhTkznI10zv7iESb2n&callback=__baiduInitCallback&s=1'
    script.onerror = () => {
      delete window.__baiduLoading
      delete window.__baiduInitCallback
      reject(new Error('百度地图脚本加载失败'))
    }
    document.head.appendChild(script)
    
    setTimeout(() => {
      if (!window.BMapGL) {
        delete window.__baiduLoading
        reject(new Error('百度地图 API 加载超时'))
      }
    }, 15000)
  })
}

const geocodeSpot = (spot) => {
  return new Promise((resolve) => {
    if (!geocoder || !window.BMapGL) {
      resolve(null)
      return
    }
    
    geocoder.getPoint(spot.address || spot.name + '特呈岛', (point) => {
      if (point) {
        spot.lat = point.lat
        spot.lng = point.lng
        resolve(point)
      } else {
        resolve(null)
      }
    }, '湛江市')
  })
}

const geocodeAllSpots = async (BMapGL) => {
  geocoder = new BMapGL.Geocoder()
  let successCount = 0
  
  for (const spot of spots.value) {
    const point = await geocodeSpot(spot)
    if (point) {
      successCount++
    }
  }
  
  console.log(`地理编码完成：${successCount}/${spots.value.length} 个景点定位成功`)
  markersCount.value = successCount
  return successCount
}

const initMap = async () => {
  try {
    mapError.value = ''
    
    const BMapGL = await loadBaiduMap()
    
    await nextTick()
    if (!mapContainer.value) {
      throw new Error('地图容器不存在')
    }
    
    if (map) {
      map.destroy()
      map = null
    }
    
    map = new BMapGL.Map(mapContainer.value)
    const centerPoint = new BMapGL.Point(110.4345, 21.149)
    map.centerAndZoom(centerPoint, 14)
    map.enableScrollWheelZoom(true)
    map.enableContinuousZoom(true)
    
    map.addControl(new BMapGL.NavigationControl())
    map.addControl(new BMapGL.ScaleControl())
    map.addControl(new BMapGL.ZoomControl())
    
    console.log('地图创建成功')
    
    ElMessage.info('正在获取景点位置...')
    await geocodeAllSpots(BMapGL)
    
    addMarkers(BMapGL)
    
    setTimeout(() => {
      if (map) {
        map.centerAndZoom(centerPoint, 14)
      }
    }, 200)
    
    mapLoaded.value = true
    
  } catch (error) {
    console.error('地图初始化失败:', error)
    mapError.value = error.message || '地图加载失败'
    ElMessage.error(`地图加载失败: ${error.message}`)
  }
}

const addMarkers = (BMapGL) => {
  if (!map) return
  
  markers.forEach(marker => map.removeOverlay(marker))
  markerLabels.forEach(label => map.removeOverlay(label))
  markers = []
  markerLabels = []
  
  spots.value.forEach(spot => {
    if (!spot.lat || !spot.lng) return
    
    const point = new BMapGL.Point(spot.lng, spot.lat)
    const marker = new BMapGL.Marker(point)
    
    const label = new BMapGL.Label(spot.name, {
      position: point,
      offset: new BMapGL.Size(15, -25)
    })
    label.setStyle({
      color: '#333',
      fontSize: '11px',
      fontWeight: 'bold',
      background: 'rgba(255,255,255,0.9)',
      padding: '2px 6px',
      borderRadius: '12px',
      border: '1px solid #667eea',
      whiteSpace: 'nowrap',
      boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
    })
    
    marker.addEventListener('click', () => {
      const content = `
        <div style="min-width: 220px; padding: 12px;">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 10px;">
            <span style="font-size: 20px;">${getCategoryText(spot.category)}</span>
            <h4 style="margin: 0; color: #0f4c81; font-size: 16px;">${spot.name}</h4>
          </div>
          <p style="margin: 8px 0; font-size: 12px; color: #666; line-height: 1.5;">${spot.description || ''}</p>
          <p style="margin: 5px 0; font-size: 12px; color: #999;">⏱️ 建议游览：${spot.stayTime || 30}分钟</p>
          <p style="margin: 5px 0; font-size: 11px; color: #aaa;">📍 ${spot.address || spot.name}</p>
        </div>
      `
      infoWindow = new BMapGL.InfoWindow({
        content: content,
        width: 260
      })
      map.openInfoWindow(infoWindow, point)
    })
    
    map.addOverlay(marker)
    map.addOverlay(label)
    markers.push(marker)
    markerLabels.push(label)
  })
  
  console.log(`已添加 ${markers.length} 个景点标记`)
}

const focusSpot = (spot) => {
  if (!map || !window.BMapGL) {
    ElMessage.warning('地图尚未加载完成')
    return
  }
  
  if (spot.lat && spot.lng) {
    const point = new window.BMapGL.Point(spot.lng, spot.lat)
    map.centerAndZoom(point, 16)
    
    const markerIndex = spots.value.findIndex(s => s.id === spot.id)
    if (markerIndex >= 0 && markers[markerIndex]) {
      const event = document.createEvent('Event')
      event.initEvent('click', true, true)
      markers[markerIndex].dispatchEvent(event)
    }
    ElMessage.success(`已定位到：${spot.name}`)
  } else {
    ElMessage.warning(`正在获取 ${spot.name} 的位置...`)
    geocodeSpot(spot)
    setTimeout(() => {
      if (spot.lat && spot.lng) {
        focusSpot(spot)
      } else {
        ElMessage.error(`无法定位 ${spot.name}`)
      }
    }, 1500)
  }
}

const showFacility = (type) => {
  const messages = {
    toilet: '🚻 卫生间位置：码头游客中心、红树林栈道入口、温泉度假村、渔村文化村',
    restaurant: '🍔 餐饮推荐：渔村文化村海鲜大排档、温泉度假村餐厅、码头美食街',
    parking: '🅿️ 停车场：码头停车场（500个车位）、温泉度假村停车场',
    ferry: '⛴️ 轮渡信息：霞山码头→特呈岛，每30分钟一班，航程15分钟，票价：单程10元'
  }
  ElMessage.info(messages[type])
}

const handleResize = () => {
  if (map) {
    setTimeout(() => {
      map.centerAndZoom(map.getCenter(), map.getZoom())
    }, 100)
  }
}

onMounted(async () => {
  await fetchSpots()
  await nextTick()
  setTimeout(() => {
    initMap()
  }, 500)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 保持原有样式不变 */
.guide-container {
  min-height: calc(100vh - 130px);
  background: #f5f7fa;
  padding: 20px;
  overflow-x: hidden;
}

.guide-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 170px);
  overflow-x: hidden;
}

.guide-left {
  flex: 2;
  background: #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  min-height: 500px;
}

.guide-map {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.map-error-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80%;
  max-width: 300px;
  z-index: 10;
  text-align: center;
}

.map-tip {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  z-index: 10;
}

.map-legend {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.95);
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 11px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  z-index: 10;
}

.legend-title {
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 3px;
}
.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}
.legend-color.transport { background: #f56c6c; }
.legend-color.culture { background: #9b59b6; }
.legend-color.scenery { background: #3498db; }
.legend-color.nature { background: #27ae60; }
.legend-color.beach { background: #e67e22; }
.legend-color.hotel { background: #e84393; }

.guide-right {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.guide-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.guide-header h3 { margin: 0; color: #333; }

.search-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.search-row .el-input { flex: 1; min-width: 150px; }

.section-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #666;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.spots-list {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
  max-height: 400px;
}

.spot-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: slideInRight 0.4s ease forwards;
  opacity: 0;
}

.spot-card:hover {
  background: #f5f7fa;
  border-color: #667eea;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

.spot-info { flex: 1; min-width: 0; }
.spot-name {
  font-weight: bold;
  margin-bottom: 4px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.spot-type {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: normal;
}
.spot-type.transport { background: #f56c6c20; color: #f56c6c; }
.spot-type.culture { background: #9b59b620; color: #9b59b6; }
.spot-type.scenery { background: #3498db20; color: #3498db; }
.spot-type.nature { background: #27ae6020; color: #27ae60; }
.spot-type.beach { background: #e67e2220; color: #e67e22; }
.spot-type.hotel { background: #e8439320; color: #e84393; }

.spot-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  word-break: break-word;
}
.spot-meta {
  font-size: 11px;
  color: #999;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.spot-meta .located { color: #67c23a; }
.spot-meta .pending { color: #e6a23c; }

.spot-locate {
  color: #667eea;
  font-size: 18px;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
  flex-shrink: 0;
}
.spot-card:hover .spot-locate {
  background: #667eea;
  color: white;
  transform: scale(1.1);
}

.facility-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.facility-buttons .el-button { transition: all 0.3s ease; }
.facility-buttons .el-button:hover { transform: translateY(-2px); }

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-in-down { animation: fadeInDown 0.5s ease forwards; }
.fade-in { animation: fadeIn 0.5s ease forwards; }
.fade-in-up { animation: fadeInUp 0.5s ease forwards; }

@media (max-width: 900px) {
  .guide-wrapper { flex-direction: column; }
  .guide-left { min-height: 400px; }
  .guide-map { min-height: 400px; }
  .guide-right { max-height: none; }
}
</style>