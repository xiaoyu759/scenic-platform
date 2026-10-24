<template>
  <div>
    <VisitorHeader />
    <div class="route-container">
      <div class="route-wrapper">
        <!-- 左侧百度地图 -->
        <div class="route-left">
          <div ref="mapContainer" class="map-container"></div>
          <div v-if="mapLoading" class="map-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            地图加载中...
          </div>
          <div v-if="mapError" class="map-error">
            <el-alert type="error" :title="mapError" show-icon :closable="false" />
            <el-button size="small" @click="retryInitMap" style="margin-top: 10px;">重试加载</el-button>
          </div>
          <div v-if="mapLoaded" class="map-tip">
            <span>📍 点击地图上的景点标记添加到路线</span>
          </div>
        </div>
        
        <!-- 右侧面板 -->
        <div class="route-right">
          <div class="route-header">
            <h3>🗺️ 游览路线规划</h3>
            <el-button type="primary" size="small" @click="generateRecommendRoute" :loading="generating">
              <el-icon><MagicStick /></el-icon>
              一键生成推荐路线
            </el-button>
          </div>

          <div class="selected-spots">
            <div class="section-title">
              已选景点
              <span style="font-size: 12px; color: #999;">（共 {{ selectedSpots.length }} 个）</span>
              <div class="section-buttons">
                <el-button v-if="selectedSpots.length > 1" type="primary" link size="small" :loading="sorting" @click="sortByRecommend">
                  智能排序
                </el-button>
                <el-button v-if="selectedSpots.length > 0" type="danger" link size="small" @click="clearRoute">
                  清空全部
                </el-button>
              </div>
            </div>
            
            <div v-if="selectedSpots.length === 0" class="empty-tip">
              👈 点击左侧地图上的景点标记添加到路线
            </div>
            
            <div v-else class="spot-list">
              <div v-for="(spot, index) in selectedSpots" :key="spot.id" class="spot-item">
                <div class="spot-order">{{ index + 1 }}</div>
                <div class="spot-info">
                  <div class="spot-name">{{ spot.name }}</div>
                  <div class="spot-desc">{{ spot.duration }}分钟 | {{ spot.description }}</div>
                </div>
                <div class="spot-actions">
                  <el-button v-if="index > 0" size="small" text @click="moveUp(index)" title="上移">
                    <el-icon><ArrowUp /></el-icon>
                  </el-button>
                  <el-button v-if="index < selectedSpots.length - 1" size="small" text @click="moveDown(index)" title="下移">
                    <el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <el-button type="danger" size="small" circle @click="removeSpot(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="selectedSpots.length > 0" class="route-info">
            <div class="info-item">
              <div class="label">总时长</div>
              <div class="value">{{ totalDuration }}分钟</div>
            </div>
            <div class="info-item">
              <div class="label">步行距离</div>
              <div class="value">{{ totalDistance }}公里</div>
            </div>
            <div class="info-item">
              <div class="label">景点数量</div>
              <div class="value">{{ selectedSpots.length }}个</div>
            </div>
            <div class="info-item">
              <el-button type="primary" size="small" @click="showRouteOnMap">
                <el-icon><Location /></el-icon>
                显示路线
              </el-button>
            </div>
          </div>

          <div class="route-actions">
            <el-button type="primary" @click="saveRoute" :disabled="selectedSpots.length === 0">
              <el-icon><Document /></el-icon> 保存路线
            </el-button>
            <el-button type="success" @click="printRoute" :disabled="selectedSpots.length === 0">
              <el-icon><Printer /></el-icon> 打印路线
            </el-button>
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
import { MagicStick, Delete, Document, Printer, ArrowUp, ArrowDown, Loading, Location } from '@element-plus/icons-vue'
import VisitorHeader from '@/components/VisitorHeader.vue'
import VisitorFooter from '@/components/VisitorFooter.vue'
import { saveRoute as saveRouteApi, getSpotList as getSpotListApi, generateRoute as generateRouteApi } from '@/api/visitor'

// 百度地图相关
let map = null
let markers = []
let markerLabels = []
let routePolyline = null
let geocoder = null

const mapContainer = ref(null)
const selectedSpots = ref([])
const generating = ref(false)
const sorting = ref(false)
const mapLoading = ref(true)
const mapLoaded = ref(false)
const mapError = ref('')

// 经典推荐路线的时间预算（分钟），请求后端推荐路线时使用
const CLASSIC_ROUTE_DURATION = 240
// 后端推荐接口不可用时的降级推荐顺序（原硬编码数据）
const FALLBACK_RECOMMEND_ORDER = [1, 6, 8, 2, 3, 4, 5, 7, 9, 10]

// ========== 特呈岛景点数据 ==========
// 注意：该数组仅作为 GET /api/visitor/spot/list 请求失败时的降级数据，
// 页面加载时会尝试用后端返回的真实景点数据覆盖它
const allSpots = ref([
  { id: 1, name: '特呈岛码头', address: '湛江市霞山区特呈岛码头', lat: null, lng: null, duration: 20, description: '景区主入口，乘船登岛点，游客服务中心' },
  { id: 2, name: '渔村文化村', address: '湛江市霞山区特呈岛渔村', lat: null, lng: null, duration: 30, description: '体验晒鱼干、做门鳝干，感受疍家文化' },
  { id: 3, name: '冼太庙', address: '湛江市霞山区特呈岛冼太庙', lat: null, lng: null, duration: 30, description: '400多年历史，市级非物质文化遗产' },
  { id: 4, name: '八音桥', address: '湛江市霞山区特呈岛八音桥', lat: null, lng: null, duration: 20, description: '历史人文景点，桥上可欣赏海岛风光' },
  { id: 5, name: '解缙雕像', address: '湛江市霞山区特呈岛解缙广场', lat: null, lng: null, duration: 15, description: '明代大儒登岛赋诗处，文化地标' },
  { id: 6, name: '红树林栈道', address: '湛江市霞山区特呈岛红树林保护区', lat: null, lng: null, duration: 60, description: '500年古红树林，乘小船穿梭其中，观赏白鹭飞舞' },
  { id: 7, name: '太邱学校', address: '湛江市霞山区特呈岛太邱学校', lat: null, lng: null, duration: 15, description: '人文打卡点，百年老校' },
  { id: 8, name: '红姜石海滩', address: '湛江市霞山区特呈岛东海岸', lat: null, lng: null, duration: 45, description: '天然蚝仔生长环境，体验赶海挖蚝乐趣' },
  { id: 9, name: '观海长廊', address: '湛江市霞山区特呈岛观海长廊', lat: null, lng: null, duration: 30, description: '海边栈道，观赏日出日落最佳地点' },
  { id: 10, name: '温泉度假村', address: '湛江市霞山区特呈岛温泉度假村', lat: null, lng: null, duration: 90, description: '天然海水温泉，富含矿物质，放松身心' }
])

const totalDuration = computed(() => {
  return selectedSpots.value.reduce((sum, spot) => sum + (spot.duration || 30), 0)
})

// 坐标有效性校验：后端 latitude/longitude 是 BigDecimal，可能为 null
const hasValidCoord = (spot) => {
  const lat = Number(spot?.lat)
  const lng = Number(spot?.lng)
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return false
  // null 经 Number() 会变成 0，因此 0 一律视为缺失坐标
  return lat !== 0 && lng !== 0
}

// Haversine 公式：按两个经纬度坐标计算球面距离，单位公里
const haversineDistance = (lat1, lng1, lat2, lng2) => {
  const toRad = (deg) => (deg * Math.PI) / 180
  const earthRadius = 6371 // 地球平均半径（公里）
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * earthRadius * Math.asin(Math.sqrt(a))
}

const totalDistance = computed(() => {
  if (selectedSpots.value.length <= 1) return 0
  let distance = 0
  let measured = false
  for (let i = 0; i < selectedSpots.value.length - 1; i++) {
    const from = selectedSpots.value[i]
    const to = selectedSpots.value[i + 1]
    // 任一景点坐标缺失就跳过这一段，不能按 0 计算，否则总距离会偏小
    if (!hasValidCoord(from) || !hasValidCoord(to)) continue
    const segment = haversineDistance(Number(from.lat), Number(from.lng), Number(to.lat), Number(to.lng))
    if (Number.isFinite(segment)) {
      distance += segment
      measured = true
    }
  }
  return measured ? Number(distance.toFixed(1)) : 0
})

// ========== 加载后端真实景点数据 ==========
const loadSpots = async () => {
  try {
    const res = await getSpotListApi()
    const list = Array.isArray(res?.data) ? res.data : []

    if (list.length === 0) {
      ElMessage.error('景点数据为空，已使用本地降级数据')
      return false
    }

    // 字段映射：stayTime -> duration，latitude/longitude -> lat/lng
    allSpots.value = list
      .filter(item => item && item.id !== null && item.id !== undefined)
      .map(item => {
        const latitude = (item.latitude === null || item.latitude === undefined) ? null : Number(item.latitude)
        const longitude = (item.longitude === null || item.longitude === undefined) ? null : Number(item.longitude)
        return {
          id: item.id,
          name: item.name || '未命名景点',
          address: item.address || '',
          description: item.description || '',
          duration: item.stayTime || 30,
          // 坐标为 null / 非数字时统一置为 null，地图与距离计算都会跳过该景点
          lat: Number.isFinite(latitude) ? latitude : null,
          lng: Number.isFinite(longitude) ? longitude : null
        }
      })

    console.log(`景点数据加载完成：${allSpots.value.length} 个`)
    return true
  } catch (error) {
    // 接口失败时保留硬编码降级数据，保证页面仍然可用
    console.error('景点列表加载失败，已使用本地降级数据:', error)
    ElMessage.error('景点数据加载失败，已使用本地降级数据')
    return false
  }
}

// 移动景点
const moveUp = (index) => {
  if (index > 0) {
    const temp = selectedSpots.value[index]
    selectedSpots.value[index] = selectedSpots.value[index - 1]
    selectedSpots.value[index - 1] = temp
    ElMessage.success('顺序已调整')
    drawRouteLine()
  }
}

const moveDown = (index) => {
  if (index < selectedSpots.value.length - 1) {
    const temp = selectedSpots.value[index]
    selectedSpots.value[index] = selectedSpots.value[index + 1]
    selectedSpots.value[index + 1] = temp
    ElMessage.success('顺序已调整')
    drawRouteLine()
  }
}

// 智能排序：由后端按时间预算返回推荐顺序
const sortByRecommend = async () => {
  if (selectedSpots.value.length <= 1) return

  const candidates = [...selectedSpots.value]
  sorting.value = true
  const loadingMsg = ElMessage({
    message: '正在按时间预算获取推荐顺序...',
    duration: 0,
    type: 'info'
  })

  try {
    // 以当前已选景点的总游览时长作为时间预算
    const res = await generateRouteApi({ duration: totalDuration.value })
    const order = Array.isArray(res?.data) ? res.data : []

    if (order.length === 0) {
      ElMessage.warning('后端未返回推荐顺序，已保持当前顺序')
      return
    }

    const orderMap = new Map(order.map((id, index) => [id, index]))
    // 只对后端返回的景点排序，未返回的追加在后面
    const recommended = candidates
      .filter(spot => orderMap.has(spot.id))
      .sort((a, b) => orderMap.get(a.id) - orderMap.get(b.id))
    const rest = candidates.filter(spot => !orderMap.has(spot.id))

    selectedSpots.value = [...recommended, ...rest]

    if (rest.length > 0) {
      ElMessage.warning(`已按时间预算优化，部分景点未纳入：${rest.map(spot => spot.name).join('、')}`)
    } else {
      ElMessage.success('已按推荐顺序排序')
    }
    drawRouteLine()
    updateMarkersStyle()
  } catch (error) {
    console.error('获取推荐顺序失败:', error)
    ElMessage.error('推荐顺序获取失败，已保持当前顺序')
  } finally {
    loadingMsg.close()
    sorting.value = false
  }
}

// 显示路线
const showRouteOnMap = () => {
  drawRouteLine()
  ElMessage.success('路线已显示在地图上')
}

// 加载百度地图
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

// 使用百度地理编码服务获取景点坐标
const geocodeSpot = (spot) => {
  return new Promise((resolve) => {
    if (!geocoder || !window.BMapGL) {
      resolve(null)
      return
    }
    
    geocoder.getPoint(spot.address, (point) => {
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

// 获取所有景点坐标
const geocodeAllSpots = async (BMapGL) => {
  geocoder = new BMapGL.Geocoder()
  let successCount = 0
  
  for (const spot of allSpots.value) {
    // 后端已返回真实经纬度的景点直接使用，避免被地理编码结果覆盖
    if (hasValidCoord(spot)) {
      successCount++
      continue
    }
    
    const point = await geocodeSpot(spot)
    if (point) {
      successCount++
    }
  }
  
  console.log(`地理编码完成：${successCount}/${allSpots.value.length} 个景点定位成功`)
  return successCount
}

// 初始化地图
const initMap = async () => {
  try {
    mapLoading.value = true
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
    
    // 创建地图
    map = new BMapGL.Map(mapContainer.value)
    const centerPoint = new BMapGL.Point(110.4345, 21.149)
    map.centerAndZoom(centerPoint, 14)
    map.enableScrollWheelZoom(true)
    map.enableContinuousZoom(true)
    
    // 添加控件
    map.addControl(new BMapGL.NavigationControl())
    map.addControl(new BMapGL.ScaleControl())
    map.addControl(new BMapGL.ZoomControl())
    
    console.log('地图创建成功')
    
    // 获取景点坐标
    ElMessage.info('正在获取景点位置...')
    await geocodeAllSpots(BMapGL)
    
    // 添加标记
    addMarkers(BMapGL)
    
    mapLoaded.value = true
    mapLoading.value = false
    
  } catch (error) {
    console.error('地图初始化失败:', error)
    mapError.value = error.message || '地图加载失败'
    mapLoading.value = false
    ElMessage.error(`地图加载失败: ${error.message}`)
  }
}

// 添加标记点（使用百度地图默认图标，避免CORS）
const addMarkers = (BMapGL) => {
  if (!map) return
  
  // 清除旧标记
  markers.forEach(marker => map.removeOverlay(marker))
  markerLabels.forEach(label => map.removeOverlay(label))
  markers = []
  markerLabels = []
  
  allSpots.value.forEach(spot => {
    if (!spot.lat || !spot.lng) return
    
    const point = new BMapGL.Point(spot.lng, spot.lat)
    const isSelected = selectedSpots.value.some(s => s.id === spot.id)
    
    // 使用百度地图默认图标（不依赖外部图片）
    const marker = new BMapGL.Marker(point)
    
    // 添加文字标签
    const label = new BMapGL.Label(spot.name, {
      position: point,
      offset: new BMapGL.Size(15, -25)
    })
    label.setStyle({
      color: isSelected ? '#67c23a' : '#333',
      fontSize: '11px',
      fontWeight: 'bold',
      background: 'rgba(255,255,255,0.9)',
      padding: '2px 6px',
      borderRadius: '12px',
      border: isSelected ? '1px solid #67c23a' : '1px solid #667eea',
      whiteSpace: 'nowrap',
      boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
    })
    
    // 添加点击事件
    marker.addEventListener('click', () => {
      addSpotToRoute(spot.id)
    })
    
    map.addOverlay(marker)
    map.addOverlay(label)
    markers.push(marker)
    markerLabels.push(label)
  })
  
  console.log(`已添加 ${markers.length} 个景点标记`)
}

// 更新标记样式
const updateMarkersStyle = () => {
  if (!map || !window.BMapGL) return
  
  allSpots.value.forEach((spot, idx) => {
    const isSelected = selectedSpots.value.some(s => s.id === spot.id)
    const label = markerLabels[idx]
    if (label) {
      label.setStyle({
        color: isSelected ? '#67c23a' : '#333',
        border: isSelected ? '1px solid #67c23a' : '1px solid #667eea'
      })
    }
  })
}

// 绘制路线
const drawRouteLine = () => {
  if (!map || !window.BMapGL || selectedSpots.value.length < 2) {
    if (routePolyline && map) {
      map.removeOverlay(routePolyline)
      routePolyline = null
    }
    return
  }
  
  // 清除旧路线
  if (routePolyline) {
    map.removeOverlay(routePolyline)
    routePolyline = null
  }
  
  // 获取路线点集
  const points = []
  for (const spot of selectedSpots.value) {
    if (spot.lat && spot.lng) {
      points.push(new window.BMapGL.Point(spot.lng, spot.lat))
    }
  }
  
  if (points.length < 2) return
  
  // 绘制路线
  routePolyline = new window.BMapGL.Polyline(points, {
    strokeColor: '#e6a23c',
    strokeWeight: 4,
    strokeOpacity: 0.8,
    strokeStyle: 'solid'
  })
  map.addOverlay(routePolyline)
  
  // 调整视野以包含所有点
  if (points.length > 0) {
    const viewport = map.getViewport(points)
    map.centerAndZoom(viewport.center, Math.min(viewport.zoom, 15))
  }
}

// 重试初始化
const retryInitMap = () => {
  initMap()
}

// 添加到路线
const addSpotToRoute = (spotId) => {
  const spot = allSpots.value.find(s => s.id === spotId)
  if (!spot) return
  
  if (selectedSpots.value.find(s => s.id === spotId)) {
    ElMessage.warning(`${spot.name} 已在路线中`)
    return
  }
  
  selectedSpots.value.push({ ...spot })
  ElMessage.success(`已添加：${spot.name}`)
  drawRouteLine()
  updateMarkersStyle()
}

// 移除景点
const removeSpot = (index) => {
  const spot = selectedSpots.value[index]
  selectedSpots.value.splice(index, 1)
  ElMessage.info(`已移除：${spot.name}`)
  drawRouteLine()
  updateMarkersStyle()
}

// 一键生成推荐路线（改为调用后端推荐算法）
const generateRecommendRoute = async () => {
  generating.value = true
  try {
    const res = await generateRouteApi({ duration: CLASSIC_ROUTE_DURATION })
    const order = Array.isArray(res?.data) ? res.data : []

    if (order.length === 0) {
      console.warn('推荐路线接口未返回数据，使用本地降级推荐顺序')
      ElMessage.warning('推荐路线接口未返回结果，已使用本地推荐顺序')
      applyRecommendOrder(FALLBACK_RECOMMEND_ORDER)
      return
    }

    applyRecommendOrder(order)
    ElMessage.success(`已生成推荐路线，共 ${selectedSpots.value.length} 个景点`)
  } catch (error) {
    // 接口异常时保留原有硬编码推荐顺序作为降级
    console.error('生成推荐路线失败，已使用本地降级推荐顺序:', error)
    ElMessage.error('推荐路线生成失败，已使用本地推荐顺序')
    applyRecommendOrder(FALLBACK_RECOMMEND_ORDER)
  } finally {
    generating.value = false
  }
}

// 按后端返回的 id 顺序重排已选景点
const applyRecommendOrder = (orderIds) => {
  const spots = orderIds
    .map(id => allSpots.value.find(spot => spot.id === id))
    .filter(Boolean)
    .map(spot => ({ ...spot }))

  if (spots.length === 0) {
    ElMessage.warning('没有可推荐的景点')
    return
  }

  selectedSpots.value = spots
  drawRouteLine()
  updateMarkersStyle()
}

// 保存路线
const saveRoute = async () => {
  // 1. 检查是否登录
  const token = localStorage.getItem('visitorToken')
  if (!token) {
    ElMessage.warning('请先登录再保存路线')
    return
  }

  // 2. 检查是否有景点
  if (selectedSpots.value.length === 0) {
    ElMessage.warning('请先添加景点')
    return
  }
  
  // 3. 构建请求数据
  const requestData = {
    name: `特呈岛游览路线_${new Date().toLocaleDateString()}`,
    spotIds: selectedSpots.value.map(s => s.id),
    stayTimes: selectedSpots.value.map(s => s.duration || 30),
    totalTime: totalDuration.value,
    // totalDistance 已按公里保留一位小数，无需再除以 1000
    totalDistance: totalDistance.value
  }
  
  console.log('保存路线请求数据:', requestData)  // 调试日志
  
  try {
    const res = await saveRouteApi(requestData)
    console.log('保存路线响应:', res)  // 调试日志
    
    if (res.code === 200) {
      // ⭐ 修复：更明确的成功提示
      ElMessage.success({
        message: `✅ 路线已保存成功！共 ${selectedSpots.value.length} 个景点`,
        duration: 3000
      })
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存路线失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(error.response?.data?.message || '保存失败，请稍后重试')
    }
  }
}

// 打印路线
const printRoute = () => {
  const printContent = `
    <!DOCTYPE html>
    <html>
    <head><meta charset="UTF-8"><title>特呈岛游览路线</title>
    <style>
      body { font-family: 'Microsoft YaHei', sans-serif; padding: 30px; max-width: 800px; margin: 0 auto; }
      h1 { color: #333; border-bottom: 2px solid #667eea; }
      .info { background: #f5f7fa; padding: 15px; border-radius: 8px; margin: 20px 0; }
      table { width: 100%; border-collapse: collapse; }
      th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
      th { background: #667eea; color: white; }
    </style>
    </head>
    <body>
      <h1>🏝️ 特呈岛游览路线</h1>
      <div class="info">
        <p>打印时间：${new Date().toLocaleString()}</p>
        <p>总时长：${totalDuration.value} 分钟 | 步行距离：约 ${totalDistance.value} 公里 | 景点数量：${selectedSpots.value.length} 个</p>
      </div>
      <table>
        <thead>
          <tr><th>序号</th><th>景点名称</th><th>简介</th><th>游览时间</th></tr>
        </thead>
        <tbody>
          ${selectedSpots.value.map((spot, i) => `
            <tr>
              <td style="text-align:center">${i + 1}</td>
              <td><strong>${spot.name}</strong></td>
              <td>${spot.description || ''}</td>
              <td style="text-align:center">${spot.duration || 30}分钟</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
      <p style="text-align:center;color:#999;margin-top:30px;">智慧游客管理平台 - 让旅行更简单</p>
    </body>
    </html>
  `
  const printWindow = window.open('', '_blank')
  printWindow.document.write(printContent)
  printWindow.document.close()
  printWindow.print()
}

// 清空路线
const clearRoute = () => {
  if (selectedSpots.value.length > 0) {
    selectedSpots.value = []
    ElMessage.info('已清空路线')
    if (routePolyline && map) {
      map.removeOverlay(routePolyline)
      routePolyline = null
    }
    updateMarkersStyle()
  }
}

// 窗口大小变化时刷新地图
const handleResize = () => {
  if (map) {
    setTimeout(() => {
      map.centerAndZoom(map.getCenter(), map.getZoom())
    }, 100)
  }
}

onMounted(async () => {
  // 先加载后端真实景点数据；失败时 loadSpots 内部已保留硬编码降级数据
  await loadSpots()
  
  nextTick(() => {
    setTimeout(() => initMap(), 500)
  })
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
.route-container {
  min-height: calc(100vh - 130px);
  background: #f5f7fa;
  padding: 20px;
  overflow-x: hidden;
}

.route-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 170px);
  overflow-x: hidden;
}

.route-left {
  flex: 2;
  background: #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  min-height: 500px;
}

.map-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.map-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 12px 20px;
  border-radius: 30px;
  font-size: 14px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80%;
  max-width: 320px;
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

.route-right {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.route-header h3 {
  margin: 0;
  color: #333;
}

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

.section-buttons {
  display: flex;
  gap: 8px;
}

.selected-spots {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 200px;
  max-height: 400px;
}

.spot-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.spot-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.2s;
  overflow-x: hidden;
}

.spot-item:hover {
  background: #eef2ff;
  transform: translateX(2px);
}

.spot-order {
  width: 32px;
  height: 32px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.spot-info {
  flex: 1;
  min-width: 0;
}

.spot-name {
  font-weight: bold;
  font-size: 14px;
  color: #333;
  word-break: break-word;
}

.spot-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  word-break: break-word;
}

.spot-actions {
  display: flex;
  gap: 4px;
  align-items: center;
  flex-shrink: 0;
}

.route-info {
  background: #f0f9eb;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.info-item {
  flex: 1;
  text-align: center;
  min-width: 80px;
}

.info-item .label {
  font-size: 12px;
  color: #666;
  display: block;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.route-actions {
  display: flex;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  flex-wrap: wrap;
}

.route-actions .el-button {
  flex: 1;
}

.empty-tip {
  text-align: center;
  padding: 50px 20px;
  color: #999;
  font-size: 14px;
  background: #fafafa;
  border-radius: 8px;
}

@media (max-width: 900px) {
  .route-wrapper {
    flex-direction: column;
  }
  
  .route-left {
    min-height: 400px;
  }
  
  .map-container {
    min-height: 400px;
  }
}
</style>