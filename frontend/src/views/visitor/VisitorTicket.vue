<template>
  <div>
    <VisitorHeader />
    <div class="ticket-container">
      <div class="background-layer"></div>
      
      <div class="ticket-wrapper">
        <!-- 左侧景区介绍区 -->
        <div class="intro-section fade-in-left">
          <div class="glass-card">
            <h2 class="section-title">
              <span class="title-icon">🏝️</span>
              特呈岛景区介绍
            </h2>
            
            <div class="image-gallery">
              <div class="main-image">
                <img :src="mainImage" alt="特呈岛主图">
              </div>
              <div class="sub-images">
                <div class="sub-image">
                  <img :src="subImage1" alt="红树林栈道">
                </div>
                <div class="sub-image">
                  <img :src="subImage2" alt="特呈岛温泉">
                </div>
              </div>
            </div>
            
            <div class="scenic-info">
              <h3 class="scenic-name">{{ scenicInfo.name }}</h3>
              <div class="scenic-tags">
                <span class="tag tag-level">🏆 国家3A级旅游景区</span>
                <span class="tag tag-level">🌊 全国乡村旅游重点村</span>
                <span class="tag tag-nature">🌿 红树林保护区</span>
                <span class="tag tag-nature">♨️ 天然温泉海岛</span>
                <span class="tag tag-nature">🐚 赶海挖蚝体验</span>
              </div>
              <p class="scenic-desc">{{ scenicInfo.description }}</p>
            </div>
          </div>
        </div>

        <!-- 右侧票务选择区 -->
        <div class="ticket-section fade-in-right">
          <div class="glass-card">
            <div class="ticket-header">
              <div class="real-time-stock">
                <span class="stock-icon">🎫</span>
                <span>今日余票：单次登岛票剩余 <strong>{{ singleTicketStock }}</strong> 张 | 套票剩余 <strong>{{ packageTicketStock }}</strong> 张</span>
              </div>
              <div class="warning-bar">
                <span class="warning-icon">⚠️</span>
                <span>登岛须知：请携带本人身份证 | 轮渡时间：8:00-18:00（每30分钟一班）</span>
              </div>
            </div>

            <!-- 购票表单 -->
            <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="100px">
              <el-form-item label="票种" prop="ticketType">
                <el-radio-group v-model="orderForm.ticketType">
                  <el-radio value="single">单次登岛票 ¥60</el-radio>
                  <el-radio value="combo">特呈岛套票 ¥168</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="数量" prop="quantity">
                <el-input-number v-model="orderForm.quantity" :min="1" :max="10" />
              </el-form-item>

              <el-form-item label="游玩日期" prop="visitDate">
                <el-date-picker v-model="orderForm.visitDate" type="date" placeholder="选择游玩日期" />
              </el-form-item>

              <el-form-item label="购票人" prop="name">
                <el-input v-model="orderForm.name" placeholder="请输入购票人姓名" />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="orderForm.phone" placeholder="请输入手机号" />
              </el-form-item>

              <el-form-item label="身份证" prop="idCard">
                <el-input v-model="orderForm.idCard" placeholder="请输入身份证号" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" size="large" class="buy-btn" @click="submitOrder" :loading="submitting">
                  立即购票
                  <span class="btn-arrow">→</span>
                </el-button>
              </el-form-item>
            </el-form>

            <div class="ticket-footer">
              <div class="secure-payment">
                <span class="secure-icon">✓</span>
                <span>安全支付</span>
              </div>
              <div class="customer-service">
                <span class="service-icon">📞</span>
                <span>景区热线：0759-1234567</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部评价/推荐区 -->
      <div class="bottom-section fade-in-up">
        <div class="bottom-glass">
          <div class="reviews-panel">
            <h3 class="panel-title">游客评价</h3>
            <div class="review-list">
              <div class="review-item" v-for="(review, idx) in reviews" :key="idx">
                <div class="review-avatar">{{ review.user.charAt(0) }}</div>
                <div class="review-content">
                  <div class="review-user">{{ review.user }}</div>
                  <div class="review-text">“{{ review.content }}”</div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="recommend-panel">
            <h3 class="panel-title">热门推荐</h3>
            <div class="recommend-list">
              <div v-for="(spot, idx) in hotSpots" :key="idx" class="recommend-item" @click="goToSpot(spot)">
                <img :src="spot.image" :alt="spot.name">
                <div class="recommend-info">
                  <span class="recommend-name">{{ spot.name }}</span>
                  <span class="recommend-rating">⭐ {{ spot.rating }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快速咨询按钮 -->
      <div class="quick-consult" @click="showConsult">
        <div class="consult-btn">
          <span class="consult-icon">💬</span>
          <span class="consult-text">快速咨询</span>
        </div>
      </div>
    </div>
    <VisitorFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import VisitorHeader from '@/components/VisitorHeader.vue'
import VisitorFooter from '@/components/VisitorFooter.vue'
import { createOrder, getTicketTypes } from '@/api/visitor'

import mainImg from '@/components/images/8.png'
import subImg1 from '@/components/images/5.png'
import subImg2 from '@/components/images/9.png'

import bgImage from '@/components/images/3.png'

const router = useRouter()
const orderFormRef = ref()
const submitting = ref(false)

// ========== 景区图片 ==========
const mainImage = ref(mainImg)
const subImage1 = ref(subImg1)
const subImage2 = ref(subImg2)

// ========== 景区信息 ==========
const scenicInfo = ref({
  name: '特呈岛生态景区',
  description: '特呈岛位于湛江湾内，距离市区仅3海里，乘船15-20分钟可达。是国家3A级旅游景区、全国乡村旅游重点村。岛上拥有500年古红树林、天然海水温泉、红姜石海滩等独特资源，疍家渔村文化浓厚。这里依山傍海，四季如春，被誉为"湛江明珠"、"海上桃源"，是休闲度假、亲子游玩、康养健身的理想胜地。'
})

// ========== 票务数据 ==========
const singleTicketPrice = ref(60)
const packageTicketPrice = ref(168)
const singleTicketStock = ref(384)
const packageTicketStock = ref(156)
const singleTicketSold = ref(1246)
const packageTicketSold = ref(534)

// ========== 订单表单 ==========
const orderForm = reactive({
  ticketType: 'single',
  quantity: 1,
  visitDate: '',
  name: '',
  phone: '',
  idCard: ''
})

const orderRules = {
  ticketType: [{ required: true, message: '请选择票种', trigger: 'change' }],
  quantity: [{ required: true, message: '请选择数量', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择游玩日期', trigger: 'change' }],
  name: [{ required: true, message: '请输入购票人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

// ========== 评价数据 ==========
const reviews = ref([
  { user: '广州李小姐', content: '红树林栈道太美了，白鹭成群，拍照超出片！' },
  { user: '深圳陈先生', content: '温泉很舒服，海水温泉第一次体验，值得再来' }
])
import recommendImg1 from '@/components/images/8.png'
import recommendImg2 from '@/components/images/5.png'
import recommendImg3 from '@/components/images/9.png'
// ========== 推荐景点 ==========
const hotSpots = ref([
  { name: '红树林栈道', rating: 4.9, image: recommendImg2 },
  { name: '温泉度假村', rating: 4.8, image: recommendImg1 },
  { name: '渔村文化村', rating: 4.7, image: recommendImg3 }
])

// ========== 提交订单 ==========
const submitOrder = async () => {
  try {
    await orderFormRef.value.validate()
    
    // 检查是否登录
    const token = localStorage.getItem('visitorToken')
    if (!token) {
      ElMessage.warning('请先登录再购票')
      router.push('/login')
      return
    }
    
    submitting.value = true
    
    const res = await createOrder({
      ticketType: orderForm.ticketType,
      quantity: orderForm.quantity,
      visitDate: orderForm.visitDate,
      name: orderForm.name,
      phone: orderForm.phone,
      idCard: orderForm.idCard
    })
    
    if (res.code === 200) {
      ElMessage.success(`订单创建成功，订单号：${res.data.orderNo}`)
      // 清空表单
      orderFormRef.value.resetFields()
      // 跳转到订单列表
      setTimeout(() => {
        router.push('/orders')
      }, 1500)
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '创建订单失败，请检查表单')
    }
  } finally {
    submitting.value = false
  }
}

// ========== 跳转 ==========
const goToSpot = (spot) => {
  router.push('/guide')
}

const showConsult = () => {
  ElMessage.info('客服在线时间：8:00-20:00，请拨打 0759-1234567')
}

// ========== 获取票种信息 ==========
const fetchTicketTypes = async () => {
  try {
    const res = await getTicketTypes()
    if (res.code === 200) {
      // 更新价格信息
      if (res.data.single) {
        singleTicketPrice.value = res.data.single.price || 60
      }
      if (res.data.combo) {
        packageTicketPrice.value = res.data.combo.price || 168
      }
    }
  } catch (error) {
    console.error('获取票种信息失败:', error)
  }
}

onMounted(() => {
  fetchTicketTypes()
  // 设置背景图
  document.querySelector('.background-layer').style.backgroundImage = `url(${bgImage})`
})
</script>

<style scoped>
/* 保持原有样式不变 */
.ticket-container {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(135deg, #0f4c81 0%, #1a6fb5 30%, #5ba3d9 70%, #c5e4f7 100%);
  overflow-x: hidden;
  padding-bottom: 20px;
}

.background-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--bg-image);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 0;
}

.background-layer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(15, 76, 129, 0.3) 0%, rgba(91, 163, 217, 0.4) 100%);
}

.ticket-wrapper,
.bottom-section,
.quick-consult {
  position: relative;
  z-index: 2;
}

.ticket-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 100px 20px 40px;
  display: grid;
  grid-template-columns: 1fr 0.9fr;
  gap: 30px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 28px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.glass-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.9);
}

@keyframes fadeInLeft {
  from { opacity: 0; transform: translateX(-50px); }
  to { opacity: 1; transform: translateX(0); }
}
@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(50px); }
  to { opacity: 1; transform: translateX(0); }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(50px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
@keyframes slideIn {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-in-left { animation: fadeInLeft 0.8s ease forwards; }
.fade-in-right { animation: fadeInRight 0.8s ease forwards; }
.fade-in-up { animation: fadeInUp 0.8s ease forwards; }

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  color: #2c3e50;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f5a623;
}

.title-icon {
  font-size: 28px;
  animation: float 3s ease-in-out infinite;
}

.image-gallery {
  margin-bottom: 20px;
}

.main-image {
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 12px;
}
.main-image img {
  width: 100%;
  height: 250px;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.main-image:hover img { transform: scale(1.03); }

.sub-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.sub-image {
  border-radius: 12px;
  overflow: hidden;
}
.sub-image img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.sub-image:hover img { transform: scale(1.03); }

.scenic-info { margin-bottom: 20px; }
.scenic-name {
  font-size: 22px;
  color: #e67e22;
  margin-bottom: 12px;
  display: inline-block;
  position: relative;
}
.scenic-name::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #f5a623, #f7c948);
  transition: width 0.4s ease;
}
.scenic-info:hover .scenic-name::after { width: 100%; }

.scenic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
.tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.tag-level { background: linear-gradient(135deg, #f5a623, #f7c948); color: white; }
.tag-nature { background: rgba(93, 173, 226, 0.2); color: #2e86c1; }
.scenic-desc {
  font-size: 13px;
  line-height: 1.6;
  color: #555;
  text-align: justify;
}

.ticket-header { margin-bottom: 20px; }
.real-time-stock {
  background: rgba(245, 166, 35, 0.15);
  padding: 10px 15px;
  border-radius: 12px;
  font-size: 13px;
  color: #2c3e50;
  margin-bottom: 12px;
}
.real-time-stock strong { color: #e67e22; font-size: 16px; }
.warning-bar {
  background: rgba(245, 166, 35, 0.25);
  padding: 10px 15px;
  border-radius: 12px;
  font-size: 13px;
  color: #d35400;
  display: flex;
  align-items: center;
  gap: 8px;
  border-left: 3px solid #e67e22;
}

.ticket-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.4s ease;
  border: 1px solid #eee;
}
.ticket-card:hover {
  transform: translateX(5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #f5a623;
}

.buy-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #f5a623, #f7c948);
  border: none;
  color: white;
  font-weight: bold;
  border-radius: 40px;
  transition: all 0.3s ease;
}
.buy-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(245, 166, 35, 0.4);
}
.btn-arrow {
  margin-left: 5px;
  transition: transform 0.3s ease;
  display: inline-block;
}
.buy-btn:hover .btn-arrow { transform: translateX(5px); }

.ticket-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
  margin-top: 10px;
}
.secure-payment,
.customer-service {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}
.secure-payment:hover,
.customer-service:hover { color: #e67e22; transform: translateY(-2px); }
.secure-icon {
  width: 18px;
  height: 18px;
  background: #67c23a;
  color: white;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.bottom-section {
  position: relative;
  z-index: 2;
  max-width: 1400px;
  margin: 0 auto 40px;
  padding: 0 20px;
}
.bottom-glass {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 28px;
  padding: 24px 30px;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 40px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}
.bottom-glass:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-5px);
}

.reviews-panel { border-right: 1px solid rgba(0, 0, 0, 0.1); padding-right: 30px; }
.panel-title {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f5a623;
  display: inline-block;
}
.review-list { display: flex; flex-direction: column; gap: 15px; }
.review-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 10px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  transition: all 0.3s ease;
  animation: slideIn 0.5s ease backwards;
}
.review-item:hover { transform: translateX(5px); background: white; }
.review-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #f5a623, #f7c948);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
}
.review-user { font-weight: bold; font-size: 13px; color: #333; margin-bottom: 4px; }
.review-text { font-size: 12px; color: #666; line-height: 1.4; }

.recommend-list { display: flex; gap: 25px; }
.recommend-item {
  flex: 1;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.5);
}
.recommend-item:hover {
  transform: translateY(-5px);
  background: white;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}
.recommend-item img {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  object-fit: cover;
  margin-bottom: 8px;
  transition: transform 0.3s ease;
}
.recommend-item:hover img { transform: scale(1.05); }
.recommend-name { display: block; font-size: 13px; font-weight: bold; color: #333; }
.recommend-rating { display: block; font-size: 11px; color: #f5a623; margin-top: 4px; }

.quick-consult {
  position: fixed;
  bottom: 80px;
  right: 30px;
  z-index: 100;
}
.consult-btn {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #f5a623, #f7c948);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 5px 20px rgba(245, 166, 35, 0.4);
  transition: all 0.3s ease;
  animation: pulse 2s infinite;
}
.consult-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 30px rgba(245, 166, 35, 0.6);
}
.consult-icon { font-size: 24px; }
.consult-text { font-size: 10px; margin-top: 4px; }

:deep(.el-form-item__label) { color: #333; font-weight: 500; }
:deep(.el-input__wrapper) { border-radius: 12px; }
:deep(.el-input-number .el-input__wrapper) { border-radius: 12px; }

@media (max-width: 1100px) {
  .ticket-wrapper { grid-template-columns: 1fr; padding-top: 80px; }
  .bottom-glass { grid-template-columns: 1fr; gap: 30px; }
  .reviews-panel { border-right: none; padding-right: 0; border-bottom: 1px solid rgba(0,0,0,0.1); padding-bottom: 20px; }
  .ticket-footer { flex-direction: column; gap: 10px; }
  .recommend-list { justify-content: center; }
}
@media (max-width: 768px) {
  .ticket-wrapper { padding: 60px 15px 20px; }
  .glass-card { padding: 16px; }
  .sub-images { grid-template-columns: 1fr; }
  .recommend-list { flex-direction: column; align-items: center; }
  .recommend-item { width: 100%; max-width: 200px; }
  .quick-consult { bottom: 20px; right: 20px; }
  .consult-btn { width: 55px; height: 55px; }
  .consult-icon { font-size: 20px; }
  .consult-text { font-size: 9px; }
}
</style>