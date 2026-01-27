<template>
  <div class="lucky-house">
    <!-- 顶部栏 -->
    <div class="header">
      <div class="user-info">
        <van-image
          :src="`${apiBaseUrl}/${userInfo.avatar}`"
          round
          width="50"
          height="50"
          class="user-avatar"
        />
        <div class="user-details">
          <h3 class="user-name">{{ userInfo.nickname }}</h3>
          <div class="star-balance">
            <van-icon name="star" color="#FFD700" size="18" />
            <span class="star-count">{{ userInfo.starBalance }}</span>
          </div>
        </div>
      </div>
      <div class="back-btn">
        <van-button
          type="default"
          icon="arrow-left"
          @click="$router.go(-1)"
          round
        >
          返回
        </van-button>
      </div>
    </div>

    <!-- 幸运屋主体 -->
    <div class="lucky-section">
      <h1 class="lucky-title">🎰 幸运屋</h1>
      <p class="lucky-subtitle">每抽一次消耗 {{ drawCost }} 颗星星，试试你的运气吧！</p>
      <p class="lucky-probabilities">奖池概率：大吉（5%） · 中吉（20%） · 小吉（45%） · 鼓励（30%）</p>

      <!-- 宝箱区域 -->
      <div class="chest-area">
        <!-- 粒子层（开箱时用于展示光点/碎片） -->
        <div ref="particlesContainer" class="particle-layer"></div>

        <div
          ref="treasureChest"
          class="treasure-chest"
          :class="{ shaking: isDrawing, opened: showResult }"
          @click="performDraw"
        >
          <div ref="chestLid" class="chest-lid" :class="{ open: showResult }">
            <span class="chest-emoji">💎</span>
          </div>
          <div ref="chestBody" class="chest-body">
            <span class="chest-emoji">🎁</span>
          </div>
          <div class="chest-glow" v-if="!isDrawing && !showResult && canDraw"></div>
        </div>

        <div v-if="!canDraw && !isDrawing" class="insufficient-stars">
          <van-icon name="star" size="40" color="#FFD700" />
          <p>星星不足</p>
          <p class="need-more">还需 {{ drawCost - userInfo.starBalance }} 颗星星</p>
        </div>

        <div v-if="isDrawing" class="drawing-animation">
          <div class="sparkles">
            <span class="sparkle">✨</span>
            <span class="sparkle">⭐</span>
            <span class="sparkle">🌟</span>
            <span class="sparkle">✨</span>
          </div>
          <p class="drawing-text">正在抽奖中...</p>
        </div>
      </div>

      <!-- 抽奖结果 -->
      <div v-if="showResult" class="draw-result animate__animated animate__bounceIn">
        <div class="result-card" :class="resultCardClass">
          <div class="result-icon">
            <span class="result-emoji">{{ drawResult?.emoji }}</span>
          </div>
          <h3 class="result-title">{{ drawResult?.title }}</h3>
          <p class="result-description">{{ drawResult?.description }}</p>
          <div v-if="drawResult?.stars > 0" class="result-stars">
            <van-icon name="star" color="#FFD700" />
            <span>+{{ drawResult.stars }}</span>
          </div>
        </div>

        <van-button
          type="primary"
          round
          size="large"
          @click="continueDrawing"
          class="continue-btn"
        >
          继续抽奖 ({{ drawCost }} 星星)
        </van-button>
      </div>

      <!-- 抽奖历史 -->
      <div class="history-section">
        <h3 class="history-title">📜 抽奖记录</h3>
        <div v-if="drawHistory.length === 0" class="empty-history">
          <van-icon name="records" size="40" color="#ccc" />
          <p>还没有抽奖记录</p>
        </div>
        <div v-else class="history-list">
          <div
            v-for="record in drawHistory.slice(0, 10)"
            :key="record.id"
            class="history-item"
          >
            <div class="history-emoji">{{ getResultEmoji(record.resultType) }}</div>
            <div class="history-info">
              <span class="history-result">{{ getResultText(record.resultType) }}</span>
              <span class="history-time">{{ formatTime(record.createTime) }}</span>
            </div>
            <div v-if="record.starsEarned > 0" class="history-stars">
              <van-icon name="star" size="14" color="#FFD700" />
              <span>+{{ record.starsEarned }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="$router.push('/kid/dashboard')">
        <van-icon name="wap-home" size="24" />
        <span>首页</span>
      </div>
      <div class="nav-item" @click="$router.push('/kid/shop')">
        <van-icon name="shop" size="24" />
        <span>商店</span>
      </div>
      <div class="nav-item active">
        <van-icon name="gem" size="24" />
        <span>幸运屋</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { lucky } from '@/utils/api.js'
import { playGachaSound, initAudio } from '@/utils/audioManager.js'
import { nextTick } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { getApiBaseUrl } from '@/utils/config.js'
// API基础URL
const apiBaseUrl = getApiBaseUrl()
const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.currentUser)
const drawHistory = ref([])
const isDrawing = ref(false)
const showResult = ref(false)
const drawResult = ref(null)
const drawCost = 20 // 每次抽奖消耗的星星数
const defaultAvatar = '/default-avatar.svg'
const particlesContainer = ref(null)
const treasureChest = ref(null)
const chestLid = ref(null)
const chestBody = ref(null)

const canDraw = computed(() => userInfo.value.starBalance >= drawCost)

// 结果类型映射
const resultTypes = {
  'GRAND_PRIZE': { emoji: '🎊', title: '大吉（5%）', description: '恭喜获得100星星大礼包奖励！', stars: 100 },
  'GOOD_PRIZE': { emoji: '🎉', title: '中吉（20%）', description: '恭喜获得免做卡奖励！', stars: 30 },
  'SMALL_PRIZE': { emoji: '⭐', title: '小吉（45%）', description: '恭喜获得10星星回血奖励！', stars: 10 },
  'ENCOURAGEMENT': { emoji: '💝', title: '鼓励（30%）', description: '谢谢参与！继续加油！', stars: 0 }
}

// 加载用户信息
const loadUserInfo = async () => {
  await userStore.loadUserInfo(true) // 强制刷新以获取最新数据
}

// 加载抽奖历史
const loadDrawHistory = async () => {
  try {
    const response = await lucky.history(userInfo.value.userId)
    drawHistory.value = response.data
  } catch (error) {
    console.error('Failed to load draw history:', error)
  }
}

// 粒子特效：在 particlesContainer 内生成彩色小球并自动移除
const spawnParticles = (count, colors) => {
  const container = particlesContainer.value
  if (!container) return
  const rect = container.getBoundingClientRect()
  for (let i = 0; i < count; i++) {
    const el = document.createElement('div')
    el.className = 'particle'
    const color = colors[Math.floor(Math.random() * colors.length)]
    el.style.background = color
    // 随机起点在箱体中间附近
    el.style.left = `${50 + (Math.random()*40-20)}%`
    el.style.top = `${40 + (Math.random()*20-10)}%`
    el.style.opacity = `${0.9 + Math.random()*0.1}`
    const size = 6 + Math.floor(Math.random()*10)
    el.style.width = `${size}px`
    el.style.height = `${size}px`
    container.appendChild(el)
    // 触发动画：向外飘动并消失
    const dx = (Math.random() * 160 - 80)
    const dy = - (80 + Math.random() * 160)
    el.animate([
      { transform: 'translate(0,0) scale(1)', opacity: 1 },
      { transform: `translate(${dx}px, ${dy}px) scale(0.6)`, opacity: 0 }
    ], {
      duration: 900 + Math.random()*600,
      easing: 'cubic-bezier(.2,.9,.3,1)'
    })
    // 清理
    setTimeout(() => {
      try { container.removeChild(el) } catch(e) {}
    }, 1800)
  }
}

// 执行抽奖（单一宝箱模式）
const performDraw = async () => {
  if (!canDraw.value) {
    showToast('星星不足，无法抽奖')
    return
  }

  if (isDrawing.value) return

  isDrawing.value = true
  showResult.value = false
  drawResult.value = null

  // 播放抽奖音效
  playGachaSound()

  try {
    // 扣除星星
    userStore.deductStars(drawCost)

    // 调用后端抽奖API
    const response = await lucky.draw(userInfo.value.userId)
    const result = response.data

    // 保证有短动画：宝箱抖动 1.5 秒，然后开箱显示结果
    setTimeout(() => {
      // 更新本地星星余额（若有额外奖励）
      if (result.starsEarned > 0) {
        userStore.addStars(result.starsEarned)
      }

      drawResult.value = {
        ...resultTypes[result.resultType],
        stars: result.starsEarned
      }
      // 播放开箱粒子特效（根据奖品类型微调颜色/数量）
      nextTick(() => {
        const type = result.resultType
        if (particlesContainer.value) {
          if (type === 'GRAND_PRIZE') spawnParticles(30, ['#FFD700','#FFF59D','#FFD54F'])
          else if (type === 'GOOD_PRIZE') spawnParticles(20, ['#C5CAE9','#E8EAF6','#9FA8DA'])
          else if (type === 'SMALL_PRIZE') spawnParticles(14, ['#FFE082','#FFECB3','#FFD54F'])
          else spawnParticles(8, ['#FFE0B2','#FFCC80'])
        }
      })
      showResult.value = true
      isDrawing.value = false

      // 重新加载历史记录
      loadDrawHistory()
    }, 1500)

  } catch (error) {
    console.error('Failed to perform draw:', error)
    showToast('抽奖失败，请重试')

    // 退还星星
    userStore.addStars(drawCost)

    isDrawing.value = false
  }
}

// 清理粒子层
const clearParticles = () => {
  const container = particlesContainer.value
  if (!container) return
  while (container.firstChild) container.removeChild(container.firstChild)
}

// 重置宝箱视觉（确保盖子闭合、移除残留样式）
const resetChestVisuals = () => {
  try {
    if (chestLid.value) {
      chestLid.value.classList.remove('open')
      chestLid.value.style.transform = ''
    }
    if (treasureChest.value) {
      treasureChest.value.classList.remove('opened', 'shaking')
      treasureChest.value.style.transform = ''
    }
  } catch (e) {}
}

// 继续抽奖 / 关闭结果并重置视觉
const continueDrawing = () => {
  showResult.value = false
  drawResult.value = null
  clearParticles()
  // slight delay to allow DOM updates before forcing visual reset
  setTimeout(() => {
    resetChestVisuals()
    isDrawing.value = false
  }, 80)
}

// 获取结果表情符号
const getResultEmoji = (resultType) => {
  return resultTypes[resultType]?.emoji || '❓'
}

// 获取结果文本
const getResultText = (resultType) => {
  return resultTypes[resultType]?.title || '未知结果'
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

// 计算结果卡片样式类
const resultCardClass = computed(() => {
  if (!drawResult.value) return ''
  const stars = drawResult.value.stars
  if (stars >= 100) return 'grand-prize'
  if (stars >= 30) return 'good-prize'
  if (stars >= 10) return 'small-prize'
  return 'encouragement'
})

onMounted(() => {
  initAudio()
  loadUserInfo()
  loadDrawHistory()
})
</script>

<style scoped>
.lucky-house {
  min-height: 100vh;
  background: linear-gradient(135deg, #8B4513 0%, #A0522D 30%, #D2691E 70%, #CD853F 100%);
  padding: 20px;
  padding-bottom: 100px;
  position: relative;
  overflow: hidden;
}

.lucky-house::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 20% 20%, rgba(255, 215, 0, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 69, 0, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 60%, rgba(255, 140, 0, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 15px 20px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 2px solid #FFD700;
  position: relative;
  z-index: 10;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  border: 3px solid #FFD700;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.3);
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.star-balance {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #FF9800;
  font-weight: bold;
  font-size: 16px;
}

.lucky-section {
  position: relative;
  z-index: 10;
  text-align: center;
}

.lucky-title {
  font-size: 36px;
  font-weight: bold;
  color: #FFD700;
  margin-bottom: 10px;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.5);
  animation: title-glow 2s ease-in-out infinite alternate;
}

.lucky-subtitle {
  font-size: 16px;
  color: #FFF;
  margin-bottom: 40px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.lucky-probabilities {
  font-size: 14px;
  color: #FFF9E6;
  margin-top: -20px;
  margin-bottom: 20px;
  opacity: 0.95;
}

@keyframes title-glow {
  from { text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.5); }
  to { text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.5), 0 0 20px rgba(255, 215, 0, 0.8); }
}

.chest-area {
  position: relative;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
}

.treasure-chest {
  position: relative;
  width: 220px;
  height: 170px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.treasure-chest:hover {
  transform: scale(1.05);
}

.treasure-chest.shaking {
  animation: chest-shake 0.5s ease-in-out infinite;
}

.treasure-chest.opened {
  animation: chest-open 1s ease-out;
}

@keyframes chest-shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px) rotate(-2deg); }
  75% { transform: translateX(5px) rotate(2deg); }
}

@keyframes chest-open {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.chest-lid {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 188px;
  height: 100px;
  background: linear-gradient(45deg, #8B4513, #A0522D);
  border-radius: 18px 18px 6px 6px;
  border: 3px solid rgba(101,67,33,0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  transition: transform 0.45s cubic-bezier(.2,.9,.3,1);
  transform-origin: center top;
  z-index: 4;
}

.chest-lid.open {
  transform: translateX(-50%) rotateX(-120deg);
}

.chest-body {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 208px;
  height: 120px;
  background: linear-gradient(180deg, #6b3e25, #7a4a2c);
  border-radius: 0 0 14px 14px;
  border: 3px solid rgba(101,67,33,0.95);
  border-top: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 46px;
  z-index: 2;
  box-shadow: inset 0 -18px 40px rgba(0,0,0,0.25);
}

.chest-glow {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%);
  width: 220px;
  height: 120px;
  border-radius: 18px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.20), transparent 40%);
  filter: blur(6px);
  animation: glow-pulse 2s ease-in-out infinite alternate;
  z-index: 1;
}

@keyframes glow-pulse {
  from { opacity: 0.3; transform: scale(1); }
  to { opacity: 0.8; transform: scale(1.05); }
}

.insufficient-stars {
  text-align: center;
  color: #FFF;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.insufficient-stars p:first-child {
  font-size: 18px;
  margin-bottom: 5px;
}

.need-more {
  font-size: 14px;
  opacity: 0.8;
}

.drawing-animation {
  text-align: center;
}

.sparkles {
  margin-bottom: 20px;
}

.sparkle {
  display: inline-block;
  font-size: 30px;
  margin: 0 5px;
  animation: sparkle-float 1s ease-in-out infinite alternate;
}

.sparkle:nth-child(1) { animation-delay: 0s; }
.sparkle:nth-child(2) { animation-delay: 0.2s; }
.sparkle:nth-child(3) { animation-delay: 0.4s; }
.sparkle:nth-child(4) { animation-delay: 0.6s; }

@keyframes sparkle-float {
  from { transform: translateY(0) rotate(0deg); }
  to { transform: translateY(-10px) rotate(360deg); }
}

.drawing-text {
  color: #FFD700;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}
.chest-emoji {
  display: inline-block;
  filter: drop-shadow(0 6px 12px rgba(0,0,0,0.35));
}

.chest-lid .chest-emoji { font-size: 48px; transform: translateY(-8px); }
.chest-body .chest-emoji { font-size: 46px; transform: translateY(6px); }

/* 粒子层 */
.particle-layer {
  position: absolute;
  left: 50%;
  top: 35%;
  transform: translateX(-50%);
  width: 260px;
  height: 220px;
  pointer-events: none;
  overflow: visible;
  z-index: 5;
}
.particle {
  position: absolute;
  border-radius: 50%;
  will-change: transform, opacity;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  pointer-events: none;
}

.draw-result {
  margin-bottom: 40px;
}

.result-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 25px;
  padding: 30px 20px;
  margin-bottom: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  border: 3px solid #FFD700;
}

.result-card.grand-prize {
  border-color: #FFD700;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), rgba(255, 215, 0, 0.05));
  animation: grand-prize-glow 1s ease-in-out infinite alternate;
}

.result-card.good-prize {
  border-color: #2196F3;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.1), rgba(33, 150, 243, 0.05));
}

.result-card.small-prize {
  border-color: #4CAF50;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(76, 175, 80, 0.05));
}

.result-card.encouragement {
  border-color: #FF9800;
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1), rgba(255, 152, 0, 0.05));
}

@keyframes grand-prize-glow {
  from { box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2); }
  to { box-shadow: 0 15px 35px rgba(255, 215, 0, 0.5), 0 0 30px rgba(255, 215, 0, 0.3); }
}

.result-icon {
  margin-bottom: 15px;
}

.result-emoji {
  font-size: 60px;
  display: block;
  text-align: center;
}

.result-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.result-description {
  font-size: 16px;
  color: #666;
  margin-bottom: 15px;
}

.result-stars {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 18px;
  font-weight: bold;
  color: #FF9800;
}

.continue-btn {
  background: linear-gradient(45deg, #FF6B35, #F7931E);
  border: none;
  padding: 12px 30px;
  font-size: 16px;
  font-weight: bold;
}

.history-section {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.history-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  text-align: center;
}

.empty-history {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-history p {
  margin-top: 10px;
  font-size: 16px;
}

.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.history-item:last-child {
  border-bottom: none;
}

.history-emoji {
  font-size: 24px;
  min-width: 30px;
}

.history-info {
  flex: 1;
}

.history-result {
  display: block;
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

.history-time {
  display: block;
  color: #999;
  font-size: 12px;
}

.history-stars {
  display: flex;
  align-items: center;
  gap: 3px;
  color: #FF9800;
  font-weight: bold;
  font-size: 14px;
}

@media (max-width: 480px) {
  .lucky-house {
    padding: 15px;
  }

  .lucky-title {
    font-size: 28px;
  }

  .treasure-chest {
    width: 160px;
    height: 144px;
  }

  .chest-lid {
    width: 140px;
    height: 96px;
    left: 50%;
    transform: translateX(-50%);
  }

  .chest-body {
    width: 140px;
    height: 80px;
    left: 50%;
    transform: translateX(-50%);
  }

  .result-card {
    padding: 20px 15px;
  }

  .result-emoji {
    font-size: 40px;
  }
}

/* 底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-top: 2px solid #FFD700;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  min-width: 70px;
}

.nav-item.active {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: #8B4513;
}

.nav-item.active span {
  color: #8B4513;
  font-weight: bold;
}

.nav-item:not(.active):hover {
  background: rgba(255, 215, 0, 0.1);
  transform: translateY(-2px);
}

.nav-item span {
  font-size: 12px;
  margin-top: 4px;
  color: #666;
  transition: color 0.3s ease;
}

.nav-item.active .van-icon {
  color: #8B4513;
}

.nav-item:not(.active) .van-icon {
  color: #999;
}
</style>


