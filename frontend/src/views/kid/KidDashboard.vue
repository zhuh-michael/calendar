<template>
  <div class="kid-dashboard">
    <!-- 顶部栏 -->
    <div class="header">
      <div class="user-info">
        <van-image
          :src="userInfo.avatar || defaultAvatar"
          round
          width="60"
          height="60"
          class="user-avatar"
        />
        <div class="user-details">
          <h2 class="user-name">{{ userInfo.nickname }}</h2>
          <div class="star-balance">
            <van-icon name="star" color="#FFD700" size="20" />
            <span class="star-count animate__animated animate__pulse">{{ userInfo.starBalance }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 今日任务流 -->
    <div class="task-section">
      <h3 class="section-title">📅 今日任务</h3>

      <!-- 待办任务 -->
      <div class="todo-tasks">
        <h4 class="subsection-title">⏳ 待完成</h4>
        <div v-if="pendingTasks.length === 0" class="empty-state">
          <van-icon name="smile" size="40" color="#ccc" />
          <p>今天没有待完成的任务！</p>
        </div>
        <div v-else class="task-list">
          <div
            v-for="task in pendingTasks"
            :key="task.id"
            class="task-card animate__animated animate__fadeInUp"
            :class="{ completing: completingTasks.includes(task.id) }"
          >
            <div class="task-content">
              <div class="task-info">
                <h4 class="task-title">{{ task.title }}</h4>
                <div class="task-reward">
                  <van-icon name="star" color="#FFD700" size="16" />
                  <span>+{{ task.rewardStars }}</span>
                </div>
              </div>
              <div class="task-actions">
                <van-button
                  round
                  :loading="completingTasks.includes(task.id)"
                  @click="completeTask(task)"
                  class="complete-btn"
                  :disabled="task.status !== 'TODO'"
                >
                  <template v-if="!completingTasks.includes(task.id)">
                    <van-icon name="success" />
                  </template>
                </van-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 已完成任务 -->
      <div class="completed-tasks">
        <h4 class="subsection-title">✅ 已完成</h4>
        <div v-if="completedTasks.length === 0" class="empty-state">
          <van-icon name="flag" size="40" color="#ccc" />
          <p>还没有完成的任务</p>
        </div>
        <div v-else class="completed-list">
          <div
            v-for="task in completedTasks"
            :key="task.id"
            class="completed-task-item animate__animated animate__fadeIn"
          >
            <van-icon name="success" color="#4CAF50" />
            <span class="task-title">{{ task.title }}</span>
            <div class="task-reward">
              <van-icon name="star" color="#FFD700" size="14" />
              <span>+{{ task.rewardStars }}</span>
            </div>
          </div>
          <div class="today-total">
            <van-icon name="star" color="#FFD700" />
            <span>今日获得：{{ todayTotalStars }} 星星</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 烟花特效 -->
    <div v-if="showFireworks" class="fireworks" @animationend="hideFireworks">
      <div class="firework firework-1">🎆</div>
      <div class="firework firework-2">🎇</div>
      <div class="firework firework-3">✨</div>
      <div class="firework firework-4">🎆</div>
      <div class="firework firework-5">🎇</div>
    </div>

    <!-- 幸运屋悬浮按钮 -->
    <div class="floating-lucky-btn" @click="$router.push('/kid/lucky-house')">
      <div class="lucky-icon">🎁</div>
      <div class="lucky-text">幸运屋</div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item active">
        <van-icon name="wap-home" size="24" />
        <span>首页</span>
      </div>
      <div class="nav-item" @click="$router.push('/kid/shop')">
        <van-icon name="shop" size="24" />
        <span>商店</span>
      </div>
      <div class="nav-item" @click="$router.push('/kid/lucky-house')">
        <van-icon name="gem" size="24" />
        <span>幸运屋</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { showToast } from 'vant'
import { tasks } from '@/utils/api.js'
import { playSuccessSound, playCoinSound, initAudio } from '@/utils/audioManager.js'

const userInfo = ref({})
const pendingTasks = ref([])
const completedTasks = ref([])
const completingTasks = ref([])
const showFireworks = ref(false)
const defaultAvatar = '/default-avatar.svg'

// 计算今日获得星星总数
const todayTotalStars = computed(() => {
  return completedTasks.value.reduce((total, task) => total + task.rewardStars, 0)
})

// 加载用户信息
const loadUserInfo = () => {
  try {
    const storedUser = localStorage.getItem('kidUser')
    if (storedUser) {
      userInfo.value = JSON.parse(storedUser)
    }
  } catch (error) {
    console.error('Failed to load user info:', error)
  }
}

// 加载今日任务
const loadTodayTasks = async () => {
  try {
    const today = new Date().toISOString().split('T')[0] // YYYY-MM-DD format
    const response = await tasks.getByKidAndDate(userInfo.value.userId, today)
    const allTasks = response.data

    // 分类任务 - 支持后端返回数字或字符串状态
    const isTodo = (s) => s === 'TODO' || s === 0 || s === '0'
    const isPending = (s) => s === 'PENDING' || s === 1 || s === '1'
    const isDone = (s) => s === 'DONE' || s === 2 || s === '2'

    pendingTasks.value = allTasks.filter(task => isTodo(task.status))
    completedTasks.value = allTasks.filter(task => isDone(task.status))
  } catch (error) {
    console.error('Failed to load tasks:', error)
    showToast('加载任务失败')
  }
}

// 完成任务
const completeTask = async (task) => {
  if (completingTasks.value.includes(task.id)) return

  completingTasks.value.push(task.id)

  try {
    await tasks.complete(task.id, userInfo.value.userId)

    // 播放音效
    playSuccessSound()
    playCoinSound()

    // 显示烟花特效
    showFireworks.value = true

    // 更新星星余额
    userInfo.value.starBalance += task.rewardStars

    // 从待办移动到已完成
    const taskIndex = pendingTasks.value.findIndex(t => t.id === task.id)
    if (taskIndex !== -1) {
      const completedTask = { ...pendingTasks.value[taskIndex], status: 'DONE' }
      pendingTasks.value.splice(taskIndex, 1)
      completedTasks.value.unshift(completedTask)
    }

    // 更新localStorage
    localStorage.setItem('kidUser', JSON.stringify(userInfo.value))

    showToast({ message: `获得 ${task.rewardStars} 颗星星！`, icon: 'success' })

  } catch (error) {
    console.error('Failed to complete task:', error)
    showToast('任务完成失败，请重试')
  } finally {
    completingTasks.value = completingTasks.value.filter(id => id !== task.id)
  }
}

// 隐藏烟花特效
const hideFireworks = () => {
  showFireworks.value = false
}

onMounted(() => {
  initAudio()
  loadUserInfo()
  loadTodayTasks()
})
</script>

<style scoped>
.kid-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #87CEEB 0%, #98D8E8 50%, #F0E68C 100%);
  padding: 20px;
  padding-bottom: 100px; /* 为底部导航留空间 */
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 25px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 3px solid #FFD700;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  border: 4px solid #FFD700;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.3);
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.star-balance {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(45deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 18px;
  font-weight: bold;
}

.task-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 30px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.subsection-title {
  font-size: 20px;
  font-weight: bold;
  color: #555;
  margin-bottom: 15px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.task-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border: 2px solid #E0E0E0;
  transition: all 0.3s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
}

.task-card.completing {
  animation: completing 0.5s ease-in-out;
}

@keyframes completing {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.task-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.task-info {
  flex: 1;
}

.task-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.task-reward {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #FF9800;
  font-weight: bold;
}

.complete-btn {
  width: 50px;
  height: 50px;
  background: linear-gradient(45deg, #4CAF50, #45a049);
  border: none;
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
}

.complete-btn:hover {
  transform: scale(1.1);
}

.completed-list {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.completed-task-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.completed-task-item:last-child {
  border-bottom: none;
}

.today-total {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 2px solid #FFD700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #FF9800;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-state p {
  margin-top: 10px;
  font-size: 16px;
}

/* 烟花特效 */
.fireworks {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
  animation: fireworks-show 2s ease-out;
}

.firework {
  position: absolute;
  font-size: 40px;
  animation: firework-explode 2s ease-out infinite;
}

.firework-1 { top: 20%; left: 20%; animation-delay: 0s; }
.firework-2 { top: 30%; right: 25%; animation-delay: 0.3s; }
.firework-3 { top: 60%; left: 30%; animation-delay: 0.6s; }
.firework-4 { bottom: 25%; right: 20%; animation-delay: 0.9s; }
.firework-5 { top: 50%; left: 70%; animation-delay: 1.2s; }

@keyframes fireworks-show {
  0% { opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; }
}

@keyframes firework-explode {
  0% {
    transform: scale(0) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: scale(1.5) rotate(180deg);
    opacity: 0.8;
  }
  100% {
    transform: scale(2) rotate(360deg);
    opacity: 0;
  }
}

/* 幸运屋悬浮按钮 */
.floating-lucky-btn {
  position: fixed;
  bottom: 100px;
  right: 20px;
  width: 70px;
  height: 70px;
  background: linear-gradient(45deg, #FFD700, #FFA500);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4);
  transition: all 0.3s ease;
  z-index: 100;
  border: 3px solid #FFF;
}

.floating-lucky-btn:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: 0 12px 35px rgba(255, 215, 0, 0.6);
  animation: lucky-bounce 1s ease-in-out infinite;
}

@keyframes lucky-bounce {
  0%, 100% { transform: scale(1.1) translateY(-2px); }
  50% { transform: scale(1.15) translateY(-5px); }
}

.lucky-icon {
  font-size: 28px;
  margin-bottom: 2px;
  animation: lucky-shine 2s ease-in-out infinite alternate;
}

@keyframes lucky-shine {
  from { filter: brightness(1); }
  to { filter: brightness(1.2) drop-shadow(0 0 8px rgba(255, 215, 0, 0.8)); }
}

.lucky-text {
  font-size: 10px;
  font-weight: bold;
  color: #8B4513;
  text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.8);
}

@media (max-width: 480px) {
  .kid-dashboard {
    padding: 15px;
  }

  .header {
    padding: 15px;
  }

  .user-info {
    gap: 10px;
  }

  .user-name {
    font-size: 20px;
  }

  .task-card {
    padding: 15px;
  }

  .task-title {
    font-size: 16px;
  }

  .floating-lucky-btn {
    bottom: 90px;
    right: 15px;
    width: 60px;
    height: 60px;
  }

  .lucky-icon {
    font-size: 24px;
  }

  .lucky-text {
    font-size: 9px;
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


