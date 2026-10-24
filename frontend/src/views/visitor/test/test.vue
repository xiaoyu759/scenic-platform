<template>
  <div>
    <h1>百度地图测试</h1>
    <div id="testMap" style="width: 100%; height: 500px; background: #ccc;"></div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  console.log('开始加载百度地图...')
  
  // 加载百度地图 API
  const script = document.createElement('script')
  script.src = 'https://api.map.baidu.com/api?v=1.0&type=webgl&ak=fCCdVP0al6oiJ5RhTkznI10zv7iESb2n&s=1'
  script.onload = () => {
    console.log('脚本加载完成，检查 BMapGL:', typeof window.BMapGL)
    
    // 等待 BMapGL 可用
    const checkInterval = setInterval(() => {
      if (window.BMapGL) {
        clearInterval(checkInterval)
        console.log('BMapGL 可用，开始创建地图')
        
        try {
          const map = new window.BMapGL.Map('testMap')
          const point = new window.BMapGL.Point(110.435, 21.149)
          map.centerAndZoom(point, 14)
          map.enableScrollWheelZoom(true)
          console.log('地图创建成功')
        } catch (err) {
          console.error('地图创建失败:', err)
        }
      }
    }, 200)
    
    // 超时处理
    setTimeout(() => {
      if (!window.BMapGL) {
        console.error('BMapGL 加载超时')
      }
    }, 10000)
  }
  script.onerror = (err) => {
    console.error('脚本加载失败:', err)
  }
  document.head.appendChild(script)
})
</script>