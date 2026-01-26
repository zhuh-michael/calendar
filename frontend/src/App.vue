<template>
  <router-view />
  
  <!-- 全局打卡弹窗 -->
  <van-dialog
    v-model:show="showCheckInDialog"
    title="每日打卡"
    :show-confirm-button="true"
    :confirm-button-text="checkedIn ? '明天再来' : '打卡'"
    :close-on-click-overlay="false"
    @confirm="confirmCheckIn"
    class="checkin-dialog"
    :style="{ width: '90%', maxWidth: '420px' }"
  >
    <div class="checkin-content">
      <!-- 连签进度 - 放在顶部 -->
      <div class="streak-bar-section" v-if="streakDays > 0 || checkedIn">
        <div class="streak-bar-header">
          <van-icon name="fire" color="#FF6B35" size="18" />
          <span>连续打卡</span>
          <span class="streak-count">{{ streakDays }} 天</span>
        </div>
        <div class="streak-bar-track">
          <div
            class="streak-bar-fill"
            :style="{ width: Math.min(100, (streakDays % 7) / 7 * 100) + '%' }"
          ></div>
        </div>
        <div class="streak-bar-labels">
          <span v-for="day in 7" :key="day" :class="{ active: day <= (streakDays % 7 || 7) }">{{ day }}</span>
        </div>
        <div class="streak-bonus-hint" v-if="streakBonusMessage">
          {{ streakBonusMessage }}
        </div>
      </div>

      <!-- 心情选择 -->
      <div class="checkin-title">今天心情怎么样？</div>
      <div class="mood-selector">
        <div
          v-for="mood in moods"
          :key="mood.type"
          class="mood-item"
          :class="{ selected: selectedMood === mood.type }"
          @click="selectedMood = mood.type"
        >
          <div class="mood-emoji-wrap">{{ mood.emoji }}</div>
          <span class="mood-label">{{ mood.label }}</span>
        </div>
      </div>

      <!-- 打卡结果预览 -->
      <div v-if="checkInResult" class="checkin-result">
        <div class="result-emoji">{{ checkInResult.leveledUp ? '🎉' : '✨' }}</div>
        <div class="result-text">
          <div v-if="checkInResult.leveledUp" class="level-up-text">
            恭喜升级！{{ checkInResult.newTitle }}
          </div>
          <div>获得 {{ checkInResult.xpGain }} XP + {{ checkInResult.starGain }} 星星</div>
        </div>
      </div>
    </div>
  </van-dialog>

  <!-- 升级弹窗 -->
  <van-overlay :show="showLevelUpDialog" @click="closeLevelUpDialog" :close-on-click-overlay="true">
    <div class="level-up-overlay" @click.stop>
      <div class="level-up-content animate__animated animate__zoomIn">
        <div class="level-up-bg"></div>
        <div class="level-up-main">
          <div class="level-up-title">🎊 升级啦！🎊</div>
          <div class="level-up-badge">
            <span class="level-number">{{ newLevel }}</span>
            <span class="level-label">级</span>
          </div>
          <div class="level-up-title-new">{{ newTitle }}</div>
          <div class="level-up-reward">解锁新头像框！</div>
          <van-button type="primary" round size="large" @click="closeLevelUpDialog" class="level-up-btn">
            太棒了！
          </van-button>
        </div>
      </div>
    </div>
  </van-overlay>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { auth } from '@/utils/api.js'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()

// 打卡相关状态
const showCheckInDialog = ref(false)
const checkedIn = ref(false)
const selectedMood = ref(null)
const checkInResult = ref(null)
const streakBonusMessage = ref('')
const streakDays = computed(() => userStore.userStreak || 0)

// 升级弹窗
const showLevelUpDialog = ref(false)
const newLevel = ref(1)
const newTitle = ref('')

// 心情选项
const moods = [
  { type: 'HAPPY', emoji: '😄', label: '开心' },
  { type: 'NEUTRAL', emoji: '😐', label: '一般' },
  { type: 'SAD', emoji: '😢', label: '难过' },
  { type: 'ANGRY', emoji: '😡', label: '生气' }
]

// 更新连签奖励提示
const updateStreakBonusMessage = (days) => {
  const messages = {
    0: '好的开始！',
    1: '继续保持！',
    2: '渐入佳境！',
    3: '坚持就是胜利！',
    4: '你太棒了！',
    5: '明天有大奖！',
    6: '周冠王！奖励翻倍！',
    7: '新的周期开始！'
  }
  streakBonusMessage.value = messages[days % 7] || '继续加油！'
}

// 检查是否需要打卡
const checkNeedCheckIn = async () => {
  const currentRoute = router.currentRoute.value
  const routeName = currentRoute?.name || ''
  const routePath = currentRoute?.path || ''

  // 排除规则：
  // 1. 路由名称排除：登录页和 parent 端页面
  // 2. 路由路径排除：以 /parent/ 开头的所有页面
  const excludedRouteNames = ['KidLogin', 'ParentLogin', 'ParentDashboard', 'RewardManagement', 'TaskManagement', 'KidManagement']
  const isParentRoute = routePath.startsWith('/parent/') || routePath.startsWith('/parent')

  if (excludedRouteNames.includes(routeName) || isParentRoute) {
    return
  }

  // 只有 kid 角色需要打卡
  if (userStore.currentUser?.role !== 'KID') return

  try {
    const response = await auth.getRpgInfo()
    const rpgInfo = response.data

    // 更新连签奖励提示
    updateStreakBonusMessage(rpgInfo.streakDays || 0)

    // 更新用户信息中的连签天数
    if (rpgInfo.streakDays !== undefined) {
      userStore.updateRpgInfo({
        ...userStore.currentUser,
        streakDays: rpgInfo.streakDays
      })
    }

    // 检查今天是否已打卡
    const today = new Date().toISOString().split('T')[0]
    const lastCheckinDate = rpgInfo.lastCheckinDate

    // 如果今天还没打卡，显示打卡弹窗
    if (lastCheckinDate !== today) {
      // 延迟显示，让页面先渲染完成
      setTimeout(() => {
        showCheckInDialog.value = true
      }, 1500)
    } else {
      // 今天已打卡
      checkedIn.value = true
    }
  } catch (e) {
    console.error('Failed to check check-in status:', e)
    // API 报错说明身份有问题，前端拦截器会自动处理跳转登录
    // 这里不需要额外处理
  }
}

// 确认打卡
const confirmCheckIn = async () => {
  if (!selectedMood.value) {
    showToast('请先选择今天的心情')
    return
  }

  try {
    const response = await auth.checkIn()
    const result = response.data

    checkInResult.value = result
    checkedIn.value = true

    // 更新用户信息
    userStore.updateRpgInfo({
      ...userStore.currentUser,
      exp: (userStore.currentUser.exp || 0) + result.xpGain,
      level: result.newLevel,
      levelTitle: result.newTitle,
      streakDays: result.streakDays
    })

    // 如果升级了，显示升级弹窗
    if (result.leveledUp) {
      setTimeout(() => {
        showCheckInDialog.value = false
        newLevel.value = result.newLevel
        newTitle.value = result.newTitle
        showLevelUpDialog.value = true
      }, 1500)
    }
  } catch (error) {
    console.error('Failed to check in:', error)
    showToast('打卡失败，请重试')
  }
}

// 关闭升级弹窗
const closeLevelUpDialog = () => {
  showLevelUpDialog.value = false
}

// 初始化
onMounted(async () => {
  // 等待路由初始化完成
  await router.isReady()

  const currentRoute = router.currentRoute.value
  const routeName = currentRoute?.name || ''
  const routePath = currentRoute?.path || ''

  // 排除规则
  const excludedRouteNames = ['KidLogin', 'ParentLogin', 'ParentDashboard', 'RewardManagement', 'TaskManagement', 'KidManagement']
  const isParentRoute = routePath.startsWith('/parent/') || routePath.startsWith('/parent')

  // 登录页面直接返回
  if (excludedRouteNames.includes(routeName) || isParentRoute) {
    return
  }

  // 等待用户信息加载后再检查打卡
  if (userStore.currentUser) {
    checkNeedCheckIn()
  } else {
    // 尝试从缓存初始化
    userStore.initializeFromCache()
    setTimeout(() => {
      checkNeedCheckIn()
    }, 500)
  }
})
</script>

<style>
html, body, #app { height: 100%; margin: 0; }

/* ==================== 每日打卡弹窗样式 ==================== */
.checkin-dialog .van-dialog__content {
  padding: 16px 20px;
}

.checkin-content {
  text-align: center;
}

.checkin-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

/* 连签进度条样式 */
.streak-bar-section {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08), rgba(255, 215, 0, 0.08));
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.streak-bar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #333;
}

.streak-count {
  background: linear-gradient(45deg, #FF6B35, #FF8C00);
  color: #fff;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: bold;
}

.streak-bar-track {
  background: #e8e8e8;
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 6px;
}

.streak-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FFD700);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.streak-bar-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 2px;
}

.streak-bar-labels span {
  font-size: 11px;
  color: #999;
  width: 20px;
  text-align: center;
}

.streak-bar-labels span.active {
  color: #FF6B35;
  font-weight: bold;
}

.streak-bonus-hint {
  margin-top: 8px;
  font-size: 13px;
  color: #FF6B35;
  font-weight: 500;
}

.mood-selector {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  min-width: 60px;
}

.mood-item:hover {
  background: rgba(255, 107, 53, 0.1);
}

.mood-item.selected {
  background: rgba(255, 107, 53, 0.15);
  border-color: #FF6B35;
}

.mood-emoji-wrap {
  font-size: 32px;
  line-height: 1;
}

.mood-label {
  font-size: 12px;
  color: #666;
}

/* 打卡结果 */
.checkin-result {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 215, 0, 0.1));
  border-radius: 12px;
}

.result-emoji {
  font-size: 36px;
  margin-bottom: 8px;
}

.result-text {
  font-size: 14px;
  color: #333;
}

.level-up-text {
  font-size: 18px;
  font-weight: bold;
  color: #FF6B35;
  margin-bottom: 6px;
}

/* ==================== 升级弹窗样式 ==================== */
.level-up-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
}

.level-up-content {
  position: relative;
  width: 300px;
  text-align: center;
}

.level-up-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.3), transparent 70%);
  animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
  from { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  to { transform: translate(-50%, -50%) scale(1.2); opacity: 1; }
}

.level-up-main {
  position: relative;
  z-index: 1;
  padding: 30px;
}

.level-up-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.level-up-badge {
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #FFD700, #FF8C00);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(255, 215, 0, 0.4);
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.level-number {
  font-size: 48px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.level-label {
  font-size: 18px;
  color: #fff;
  margin-top: 8px;
}

.level-up-title-new {
  font-size: 22px;
  font-weight: bold;
  color: #FF6B35;
  margin-bottom: 10px;
}

.level-up-reward {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
}

.level-up-btn {
  background: linear-gradient(45deg, #FF6B35, #FF8C00) !important;
  border: none !important;
  font-weight: bold;
  padding: 0 40px;
}
</style>
