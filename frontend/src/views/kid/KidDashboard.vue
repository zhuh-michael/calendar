<template>
  <div class="kid-dashboard">
    <!-- 顶部栏 -->
    <div class="header">
      <div class="user-info-row">
        <!-- 头像 + 游戏风格边框 -->
        <div class="avatar-section">
          <div class="avatar-frame" :class="avatarFrameClass">
            <van-image
              :src="`${apiBaseUrl}/${userInfo.avatar}`"
              round
              width="64"
              height="64"
              class="user-avatar"
            />
            <!-- 等级徽章 -->
            <div class="level-badge-mini">Lv.{{ userInfo.level || 1 }}</div>
          </div>
        </div>

        <!-- 用户信息 - 两行精简版 -->
        <div class="user-info-col">
          <!-- 第一行：名字 + 称号 -->
          <div class="name-row">
            <span class="user-name">{{ userInfo.nickname }}</span>
            <span class="user-title">{{ userInfo.levelTitle || '星际见习生' }}</span>
          </div>

          <!-- 第二行：星星 + XP + 进度 -->
          <div class="stats-row">
            <!-- 星星 -->
            <div class="stat-item star-item">
              <van-icon name="star" color="#FFD700" size="16" />
              <span>{{ userInfo.starBalance }}</span>
            </div>

            <!-- XP信息 -->
            <div class="xp-info">
              <span class="xp-text">{{ userInfo.exp || 0 }} XP</span>
              <span class="xp-next">距Lv.{{ (userInfo.level || 1) + 1 }}还需 {{ xpToNextLevel }} XP</span>
            </div>
          </div>

          <!-- 进度条 -->
          <div class="exp-bar-mini">
            <div class="exp-bar-fill" :style="{ width: levelProgress + '%' }"></div>
          </div>
        </div>

        <!-- 日期选择器（闹钟按钮） -->
        <div class="date-picker-section">
          <van-button
            round
            type="warning"
            class="date-picker-btn"
            @click="showDatePicker = true"
          >
            <van-icon name="clock" size="20" />
          </van-button>
          <div class="date-label">{{ formattedSelectedDate }}</div>
        </div>
      </div>
    </div>

    <!-- 今日任务流 -->
    <div class="task-section">
      <h3 class="section-title">📅 {{ isToday ? '今日任务' : selectedDateDisplay }}</h3>

      <!-- 加载状态 -->
      <div v-if="isLoadingTasks" class="loading-container">
        <van-loading type="spinner" size="24px" color="#FF9800">加载中...</van-loading>
      </div>

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
            :class="{ completing: completingTasks.includes(task.id), 'not-today': !isToday }"
          >
            <div class="task-content">
              <div class="task-info">
                <h4 class="task-title">{{ task.title }}</h4>
                <div v-if="task.rejectReason" class="task-reject-reason">
                  <van-icon name="warning" color="#ff4444" size="14" />
                  <span class="reject-text">上次被拒绝：{{ task.rejectReason }}</span>
                </div>
                <div class="task-reward">
                  <van-icon name="star" color="#FFD700" size="16" />
                  <span>+{{ task.rewardStars }}</span>
                  <span class="xp-reward">+{{ task.rewardStars }} XP</span>
                </div>
                <div v-if="!isToday" class="task-time-hint">
                  <van-icon name="clock" size="12" />
                  <span>任务时间：{{ formatTaskTime(task.startTime) }}</span>
                </div>
              </div>
              <div class="task-actions">
                <van-button
                  round
                  :loading="completingTasks.includes(task.id)"
                  @click="completeTask(task)"
                  class="complete-btn"
                  :disabled="!canCompleteTask(task)"
                >
                  <template v-if="!completingTasks.includes(task.id)">
                    <van-icon :name="isToday ? 'success' : 'eye-o'" />
                  </template>
                </van-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 审核中任务 -->
      <div class="pending-review-tasks" v-if="reviewingTasks.length > 0">
        <h4 class="subsection-title">⏳ 审核中</h4>
        <div class="task-list">
          <div
            v-for="task in reviewingTasks"
            :key="task.id"
            class="task-card animate__animated animate__fadeInUp reviewing"
          >
            <div class="task-content">
              <div class="task-info">
                <h4 class="task-title">{{ task.title }}</h4>
                <div class="task-reward">
                  <van-icon name="star" color="#FFD700" size="16" />
                  <span>待审核 +{{ task.rewardStars }}</span>
                </div>
              </div>
              <div class="task-actions">
                <van-button size="small" @click="viewTaskEvidence(task)">查看照片</van-button>
                <div class="reviewing-status">
                  <van-icon name="clock" color="#FF9800" size="20" />
                  <span class="reviewing-text">审核中</span>
                </div>
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
            <van-button size="small" type="info" @click="viewTaskEvidence(task)" style="margin-left: auto;">
              查看照片
            </van-button>
          </div>
          <div class="today-total">
            <van-icon name="star" color="#FFD700" />
            <span>今日奖励：{{ todayTotalStars }} 星星</span>
          </div>
        </div>
      </div>

      <!-- 延期任务区域 -->
      <div class="overdue-tasks" v-if="overdueTasks.length > 0 || overdueCount > 0">
        <h4 class="subsection-title overdue-title">⚠️ 延期任务 <span class="overdue-count">({{ overdueCount }}项)</span></h4>

        <van-pull-refresh v-model="isRefreshingOverdue" @refresh="onRefreshOverdue">
          <div class="task-list">
            <div
              v-for="task in overdueTasks"
              :key="task.id"
              class="task-card animate__animated animate__fadeInUp overdue"
            >
              <div class="task-content">
                <div class="task-info">
                  <h4 class="task-title">{{ task.title }}</h4>
                  <div v-if="task.rejectReason" class="task-reject-reason">
                    <van-icon name="warning" color="#ff4444" size="14" />
                    <span class="reject-text">上次被拒绝：{{ task.rejectReason }}</span>
                  </div>
                  <div class="task-reward">
                    <van-icon name="star" color="#FFD700" size="16" />
                    <span>+{{ task.rewardStars }}</span>
                  </div>
                  <div class="task-time-hint overdue-hint">
                    <van-icon name="clock" size="12" />
                    <span>任务时间：{{ formatTaskDateTime(task.startTime) }}</span>
                  </div>
                </div>
                <div class="task-actions">
                  <van-button
                    round
                    :loading="completingTasks.includes(task.id)"
                    @click="completeTask(task)"
                    class="complete-btn"
                  >
                    <van-icon :name="task.status === 'PENDING' || task.status === 1 ? 'eye-o' : 'success'" />
                  </van-button>
                </div>
              </div>
            </div>

            <!-- 加载更多 -->
            <div v-if="overdueTasks.length < overdueCount" class="load-more">
              <van-button
                plain
                type="warning"
                :loading="isLoadingMoreOverdue"
                @click="loadMoreOverdueTasks"
              >
                加载更多
              </van-button>
            </div>

            <!-- 没有更多数据提示 -->
            <div v-else-if="overdueTasks.length > 0" class="no-more-data">
              <span>已加载全部延期任务</span>
            </div>
          </div>
        </van-pull-refresh>
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

    <!-- XP 飞入动画元素 -->
    <div v-for="xpAnim in xpAnimations" :key="xpAnim.id" class="xp-animation" :style="xpAnim.style">
      <span class="xp-icon">🔰</span>
      <span class="xp-text">+{{ xpAnim.amount }} XP</span>
    </div>

    <!-- 结果上传对话框 -->
     <van-dialog
      v-model:show="showEvidenceDialog"
      title="上传完成照片"
      :show-cancel-button="false"
      :style="{ minWidth: '400px' }"
      close-on-click-overlay
    >
      <div class="evidence-dialog">
        <div class="task-info">
          <h3>{{ selectedTaskForEvidence?.title }}</h3>
          <p>{{ selectedTaskForEvidence?.description }}</p>
        </div>

        <div v-if="selectedTaskForEvidence?.rejectReason" class="reject-reason">
          <van-icon name="warning" color="#ff4444" />
          <span>上次被拒绝：{{ selectedTaskForEvidence.rejectReason }}</span>
        </div>

        <div class="evidence-section">
          <div v-if="evidencePreviews.length === 0" class="upload-options">
            <van-button type="primary" icon="camera" @click="takePhoto">拍照</van-button>
            <van-button type="info" icon="photo" @click="chooseFromGallery">从相册选择</van-button>
          </div>

          <div v-else class="preview-section">
            <div class="preview-grid">
              <div v-for="(src, idx) in evidencePreviews" :key="idx" class="preview-item">
                <img :src="src" class="evidence-preview" />
              </div>
            </div>
            <div class="preview-actions">
              <van-button size="small" @click="takePhoto">重拍</van-button>
              <van-button size="small" @click="chooseFromGallery">重新选择</van-button>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: center; gap: 10px; padding-bottom: 16px;">
          <van-button plain @click="closeEvidenceDialog">取消</van-button>
          <van-button
            type="primary"
            @click="submitWithEvidence"
            :loading="uploadingEvidence"
          >
            {{ uploadingEvidence ? '提交中...' : '提交' }}
          </van-button>
        </div>
      </template>
    </van-dialog>

    <!-- 日期选择器弹窗（使用 Calendar 组件） -->
    <van-calendar
      v-model:show="showDatePicker"
      :min-date="minDate"
      :max-date="maxDate"
      :show-confirm="false"
      @select="onDateSelect"
    />

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

    <!-- 查看结果对话框 -->
    <van-dialog
      v-model:show="showViewEvidenceDialog"
      :show-cancel-button="false"
      :show-confirm-button="false"
      title="任务完成结果" width="70%"
      :style="{ minHeight: '400px', maxWidth: '800px' }"
      close-on-click-overlay
    >
      <div class="evidence-view-dialog">
        <div class="task-info">
          <h3>{{ selectedTaskForEvidence?.title }}</h3>
          <p>{{ selectedTaskForEvidence?.description }}</p>
        </div>

        <!-- 拒绝理由显示 -->
        <div v-if="selectedTaskForEvidence?.rejectReason" class="reject-reason">
          <van-icon name="warning" color="#ff4444" />
          <span>拒绝理由：{{ selectedTaskForEvidence.rejectReason }}</span>
        </div>

        <div v-if="currentTaskEvidence.length === 0" class="no-evidence">
          <van-icon name="photo" size="40" color="#ccc" />
          <p>暂无结果图片</p>
        </div>

        <div v-else class="preview-section">
          <div class="preview-grid">
            <div
              v-for="(evidence, index) in currentTaskEvidence"
              :key="`evidence-${evidence.id || index}`"
              class="preview-item"
            >
              <img
                :src="`${apiBaseUrl}/${evidence.imagePath}`"
                class="evidence-preview"
                @click="previewImage(currentTaskEvidence.map(e => `${apiBaseUrl}/${e.imagePath}`), currentTaskEvidence.findIndex(e => (e.id || e.uploadTime) === (evidence.id || evidence.uploadTime)))"
              />
              <div class="evidence-meta">
                <span class="upload-time">{{ formatTime(evidence.uploadTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 审核中时允许替换结果 -->
        <div v-if="isPendingStatus(selectedTaskForEvidence?.status)" class="evidence-actions">
          <van-button type="primary" @click="replaceEvidence">重新上传照片</van-button>
        </div>
      </div>

      <template #footer>
      </template>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { showToast, showImagePreview } from 'vant'
import { tasks, auth } from '@/utils/api.js'
import { playSuccessSound, playCoinSound, initAudio } from '@/utils/audioManager.js'
import { useUserStore } from '@/stores/user.js'
import { getApiBaseUrl } from '@/utils/config.js'

const userStore = useUserStore()
const pendingTasks = ref([])
const reviewingTasks = ref([])
const completedTasks = ref([])
const completingTasks = ref([])
const overdueTasks = ref([])
const overdueCount = ref(0)
const overduePage = ref(0)
const isLoadingMoreOverdue = ref(false)
const isRefreshingOverdue = ref(false)
const showFireworks = ref(false)
const defaultAvatar = '/default-avatar.svg'

// 日期选择器相关
const showDatePicker = ref(false)
const selectedDate = ref(new Date())
const minDate = ref(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)) // 30天前
const maxDate = ref(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)) // 30天后
const isLoadingTasks = ref(false)

// 是否是今日
const isToday = computed(() => {
  const today = new Date()
  const selected = new Date(selectedDate.value)
  return today.toDateString() === selected.toDateString()
})

// 格式化选中的日期显示
const formattedSelectedDate = computed(() => {
  const date = new Date(selectedDate.value)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[date.getDay()]
  return `${month}/${day} ${weekDay}`
})

// 任务列表标题显示
const selectedDateDisplay = computed(() => {
  const date = new Date(selectedDate.value)
  if (isToday.value) return '今日任务'
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日任务`
})

// 选择日期（点击日期时触发）
const onDateSelect = (date) => {
  selectedDate.value = date
  showDatePicker.value = false
  loadTasksForDate()
}

// 加载指定日期的任务
const loadTasksForDate = async () => {
  if (!userInfo.value?.userId) {
    console.warn('User not loaded yet')
    return
  }

  isLoadingTasks.value = true
  try {
    // 使用本地时区格式化日期，避免时区问题
    const year = selectedDate.value.getFullYear()
    const month = String(selectedDate.value.getMonth() + 1).padStart(2, '0')
    const day = String(selectedDate.value.getDate()).padStart(2, '0')
    const dateStr = `${year}-${month}-${day}`

    console.log('Loading tasks for date:', dateStr, 'isToday:', isToday.value)

    const response = await tasks.getByKidAndDate(userInfo.value.userId, dateStr)
    const allTasks = response.data || []

    // 分类任务 - 支持后端返回数字或字符串状态
    const isTodo = (s) => s === 'TODO' || s === 0 || s === '0'
    const isPending = (s) => s === 'PENDING' || s === 1 || s === '1'
    const isDone = (s) => s === 'DONE' || s === 2 || s === '2'

    pendingTasks.value = allTasks.filter(task => isTodo(task.status))
    reviewingTasks.value = allTasks.filter(task => isPending(task.status))
    completedTasks.value = allTasks.filter(task => isDone(task.status))

    console.log('Tasks loaded:', {
      pending: pendingTasks.value.length,
      reviewing: reviewingTasks.value.length,
      completed: completedTasks.value.length
    })
  } catch (error) {
    console.error('Failed to load tasks:', error)
    showToast('加载任务失败')
  } finally {
    isLoadingTasks.value = false
  }
}

// 判断任务是否可完成（仅今日且状态为待办）
const canCompleteTask = (task) => {
  if (!isToday.value) return false
  const isTodo = (s) => s === 'TODO' || s === 0 || s === '0'
  return isTodo(task.status)
}

// 结果上传相关
const showEvidenceDialog = ref(false)
const showViewEvidenceDialog = ref(false)
const selectedTaskForEvidence = ref(null)
const currentTaskEvidence = ref([])
const evidenceFiles = ref([])
const evidencePreviews = ref([])
const evidenceObjectUrls = ref([])
const uploadingEvidence = ref(false)

// 升级弹窗
const showLevelUpDialog = ref(false)
const newLevel = ref(1)
const newTitle = ref('')

// XP 动画
const xpAnimations = ref([])
let xpAnimId = 0

// 心情选项
const moods = [
  { type: 'HAPPY', emoji: '😄', label: '开心' },
  { type: 'NEUTRAL', emoji: '😐', label: '一般' },
  { type: 'SAD', emoji: '😢', label: '难过' },
  { type: 'ANGRY', emoji: '😡', label: '生气' }
]

// 从store获取用户信息
const userInfo = computed(() => userStore.currentUser)

// 等级进度（0-100）
const levelProgress = computed(() => {
  if (!userInfo.value) return 0
  const exp = userInfo.value.exp || 0
  const level = userInfo.value.level || 1

  const baseExp = [0, 50, 150, 300, 500, 800, 1200, 1700, 2300, 3000]
  const currentLevelExp = baseExp[level - 1] || 0
  const nextLevelExpVal = baseExp[level] || baseExp[baseExp.length - 1] + 1000
  const progress = (exp - currentLevelExp) / (nextLevelExpVal - currentLevelExp)
  return Math.min(100, Math.max(0, Math.round(progress * 100)))
})

// 头像框样式 - 游戏皮肤风格
const avatarFrameClass = computed(() => {
  const level = userInfo.value?.level || 1
  if (level >= 10) return 'diamond'
  if (level >= 7) return 'gold'
  if (level >= 4) return 'silver'
  return 'bronze'
})

// 距离下一级所需 XP
const xpToNextLevel = computed(() => {
  if (!userInfo.value) return 50
  const exp = userInfo.value.exp || 0
  const level = userInfo.value.level || 1

  const baseExp = [0, 50, 150, 300, 500, 800, 1200, 1700, 2300, 3000, 4000]
  const currentLevelExp = baseExp[level - 1] || 0
  const nextLevelExpVal = baseExp[level] || baseExp[baseExp.length - 1] + 1000
  return Math.max(0, nextLevelExpVal - exp)
})

// 距离下一级所需 XP (旧变量名，保持兼容)
const nextLevelExp = computed(() => {
  if (!userInfo.value) return 50
  const exp = userInfo.value.exp || 0
  const level = userInfo.value.level || 1

  const baseExp = [0, 50, 150, 300, 500, 800, 1200, 1700, 2300, 3000]
  const currentLevelExp = baseExp[level - 1] || 0
  const nextLevelExpVal = baseExp[level] || baseExp[baseExp.length - 1] + 1000

  return nextLevelExpVal - exp
})

// 计算今日获得星星总数
const todayTotalStars = computed(() => {
  return completedTasks.value.reduce((total, task) => total + task.rewardStars, 0)
})

// API基础URL
const apiBaseUrl = getApiBaseUrl()

// 加载用户信息
const loadUserInfo = async () => {
  await userStore.loadUserInfo(true)
  // 打卡检查已在 App.vue 中全局处理
}

// 加载今日任务
const loadTodayTasks = async () => {
  await loadTasksForDate()
}

// 加载延期任务
const loadOverdueTasks = async (refresh = false) => {
  if (!userInfo.value?.userId) return

  if (refresh) {
    overduePage.value = 0
    overdueTasks.value = []
    isRefreshingOverdue.value = true
  }

  try {
    const response = await tasks.getOverdue(userInfo.value.userId, overduePage.value, 20)
    const newTasks = response.data || []

    if (refresh) {
      overdueTasks.value = newTasks
    } else {
      overdueTasks.value = [...overdueTasks.value, ...newTasks]
    }

    // 获取延期任务总数
    const countResponse = await tasks.getOverdueCount(userInfo.value.userId)
    overdueCount.value = countResponse.data || 0
  } catch (error) {
    console.error('Failed to load overdue tasks:', error)
  } finally {
    isRefreshingOverdue.value = false
  }
}

// 刷新延期任务
const onRefreshOverdue = async () => {
  overduePage.value = 0
  await loadOverdueTasks(true)
}

// 加载更多延期任务
const loadMoreOverdueTasks = async () => {
  if (isLoadingMoreOverdue.value || overdueTasks.value.length >= overdueCount.value) return

  isLoadingMoreOverdue.value = true
  try {
    overduePage.value += 1
    const response = await tasks.getOverdue(userInfo.value.userId, overduePage.value, 20)
    const newTasks = response.data || []
    overdueTasks.value = [...overdueTasks.value, ...newTasks]
  } catch (error) {
    console.error('Failed to load more overdue tasks:', error)
    overduePage.value -= 1
  } finally {
    isLoadingMoreOverdue.value = false
  }
}

// 格式化任务日期时间（用于延期任务显示完整日期）
const formatTaskDateTime = (isoTime) => {
  if (!isoTime) return ''
  const date = new Date(isoTime)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

// 格式化任务时间
const formatTaskTime = (isoTime) => {
  if (!isoTime) return ''
  const date = new Date(isoTime)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 完成任务
const completeTask = async (task) => {
  if (completingTasks.value.includes(task.id)) return

  completingTasks.value.push(task.id)

  try {
    // 如果需要审核，先让用户上传结果
    if (task.needsReview) {
      selectedTaskForEvidence.value = task
      showEvidenceDialog.value = true
    } else {
      // 不需要审核，直接完成任务
      await tasks.complete(task.id, userInfo.value.userId)

      // 播放音效和特效
      playSuccessSound()
      playCoinSound()
      showFireworks.value = true

      // 使用任务设置的 XP 奖励（如果未设置则默认等于星星数）
      const xpReward = task.rewardXp != null ? task.rewardXp : task.rewardStars

      // 播放 XP 动画
      playXpAnimation(xpReward)

      // 立即获得星星和 XP
      userStore.addStars(task.rewardStars)

      // 更新 XP 信息
      userStore.updateRpgInfo({
        ...userInfo.value,
        exp: (userInfo.value.exp || 0) + xpReward
      })

      // 从待办移动到已完成
      const taskIndex = pendingTasks.value.findIndex(t => t.id === task.id)
      if (taskIndex !== -1) {
        const completedTask = { ...pendingTasks.value[taskIndex], status: 2 }
        pendingTasks.value.splice(taskIndex, 1)
        completedTasks.value.unshift(completedTask)
      }

      showToast({ message: `任务完成！系统奖励你 ${task.rewardStars} 颗星星！`, icon: 'success' })
    }

  } catch (error) {
    console.error('Failed to complete task:', error)
    showToast('任务完成失败，请重试')
  } finally {
    completingTasks.value = completingTasks.value.filter(id => id !== task.id)
  }
}

// 播放 XP 动画
const playXpAnimation = (amount) => {
  const animId = ++xpAnimId
  const anim = {
    id: animId,
    amount,
    style: {
      left: '20%',
      top: '30%',
      opacity: 1
    }
  }
  xpAnimations.value.push(anim)

  // 动画：向头像位置移动
  setTimeout(() => {
    anim.style.left = '15%'
    anim.style.top = '15%'
    anim.style.transition = 'all 0.8s ease-out'
  }, 50)

  // 动画结束后移除
  setTimeout(() => {
    anim.style.opacity = '0'
  }, 600)

  setTimeout(() => {
    xpAnimations.value = xpAnimations.value.filter(a => a.id !== animId)
  }, 1000)
}

// 隐藏烟花特效
const hideFireworks = () => {
  showFireworks.value = false
}

// 结果上传相关函数
const handleFileSelect = (fileList) => {
  const maxFiles = 5
  let filesArray = Array.from(fileList || [])
  if (filesArray.length > maxFiles) {
    showToast(`最多上传 ${maxFiles} 张图片`)
    filesArray = filesArray.slice(0, maxFiles)
  }
  // Replace selection with chosen files
  evidenceFiles.value = filesArray
  // cleanup old object URLs
  evidenceObjectUrls.value.forEach(url => {
    try { URL.revokeObjectURL(url) } catch(e){ }
  })
  evidenceObjectUrls.value = []
  evidencePreviews.value = []
  filesArray.forEach(file => {
    // create object URL immediately for quick preview
    try {
      const objUrl = URL.createObjectURL(file)
      evidenceObjectUrls.value.push(objUrl)
      evidencePreviews.value.push(objUrl)
    } catch (e) {
      // ignore
    }
    // also read as DataURL to support platforms where object URL may not persist
    const reader = new FileReader()
    reader.onload = (e) => {
      // replace corresponding preview (by index) with data URL when ready
      const idx = evidencePreviews.value.findIndex(u => u && typeof u === 'string' && u.startsWith('blob:'))
      if (idx >= 0) {
        evidencePreviews.value[idx] = e.target.result
      } else {
        evidencePreviews.value.push(e.target.result)
      }
    }
    reader.readAsDataURL(file)
  })
}

const takePhoto = () => {
  // use file input with capture; allow multiple (camera may be single)
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.capture = 'camera'
  // camera usually provides a single file; do not set multiple to avoid platform issues
  input.multiple = false
  input.onchange = (e) => {
    if (e.target.files.length > 0) {
      handleFileSelect(e.target.files)
    }
  }
  input.click()
}

const chooseFromGallery = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.multiple = true
  input.onchange = (e) => {
    if (e.target.files.length > 0) {
      handleFileSelect(e.target.files)
    }
  }
  input.click()
}

const submitWithEvidence = async () => {
  uploadingEvidence.value = true

  try {
    // 如果有选择图片，则上传结果（覆盖式提交，后端会删除旧记录）
    if (evidenceFiles.value && evidenceFiles.value.length > 0) {
      await tasks.uploadEvidence(selectedTaskForEvidence.value.id, evidenceFiles.value)
    }

    // 再完成任务
    await tasks.complete(selectedTaskForEvidence.value.id, userInfo.value.userId)

    // 播放音效和特效
    playSuccessSound()
    playCoinSound()
    showFireworks.value = true

    // 任务状态变为审核中
    const taskIndex = pendingTasks.value.findIndex(t => t.id === selectedTaskForEvidence.value.id)
    if (taskIndex !== -1) {
      const reviewingTask = { ...pendingTasks.value[taskIndex], status: 1 }
      pendingTasks.value.splice(taskIndex, 1)
      reviewingTasks.value.unshift(reviewingTask)
    }

    // 关闭对话框
    showEvidenceDialog.value = false
    selectedTaskForEvidence.value = null
    evidenceFiles.value = []
    evidencePreviews.value = []

    showToast({ message: '任务已提交审核！请等待系统审批。', icon: 'success' })

  } catch (error) {
    console.error('Failed to submit task with evidence:', error)
    showToast('提交失败，请重试')
  } finally {
    uploadingEvidence.value = false
  }
}

const closeEvidenceDialog = () => {
  showEvidenceDialog.value = false
  selectedTaskForEvidence.value = null
    evidenceFiles.value = []
    evidencePreviews.value = []
}

// 查看任务结果
const viewTaskEvidence = async (task) => {
  try {
    const response = await tasks.getEvidence(task.id)
    currentTaskEvidence.value = response.data || []
    selectedTaskForEvidence.value = task
    showViewEvidenceDialog.value = true
  } catch (error) {
    console.error('Failed to load task evidence:', error)
    showToast('加载结果失败')
  }
}

const previewImage = (images, startPosition = 0) => {
  showImagePreview({
    images,
    startPosition,
    closeable: true,
  })
}

// 判断任务状态是否为等待审核（兼容数字/字符串/枚举名）
const isPendingStatus = (status) => {
  if (status === null || status === undefined) return false
  return status === 1 || status === '1' || status === 'PENDING' || status === 'pending'
}

// 替换结果（审核前允许替换）
const replaceEvidence = () => {
  showViewEvidenceDialog.value = false
  showEvidenceDialog.value = true
  evidenceFiles.value = []
  evidencePreviews.value = []
  // revoke object URLs
  evidenceObjectUrls.value.forEach(url => {
    try { URL.revokeObjectURL(url) } catch(e) {}
  })
  evidenceObjectUrls.value = []
}

// 格式化时间
const formatTime = (iso) => {
  return iso ? new Date(iso).toLocaleString() : ''
}

onMounted(() => {
  initAudio()
  loadUserInfo()
  loadTodayTasks()
  loadOverdueTasks(true)
})
</script>

<style scoped>
.kid-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #87CEEB 0%, #98D8E8 50%, #F0E68C 100%);
  padding: 16px;
  padding-bottom: 100px;
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
}

.user-info-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

/* 头像区域 */
.avatar-section {
  flex-shrink: 0;
}

.avatar-frame {
  position: relative;
  padding: 4px;
  border-radius: 50%;
}

/* 游戏风格头像边框 */
.avatar-frame.bronze {
  background: linear-gradient(135deg, #cd7f32, #8B4513);
  box-shadow: 0 0 0 2px rgba(205, 127, 50, 0.3);
}

.avatar-frame.silver {
  background: linear-gradient(135deg, #e8e8e8, #a0a0a0);
  box-shadow: 0 0 0 2px rgba(192, 192, 192, 0.4);
}

.avatar-frame.gold {
  background: linear-gradient(135deg, #ffd700, #daa520);
  box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.5), 0 0 15px rgba(255, 215, 0, 0.3);
}

.avatar-frame.diamond {
  background: linear-gradient(135deg, #b9f2ff, #00bcd4, #009688);
  box-shadow: 0 0 0 2px rgba(185, 242, 255, 0.6), 0 0 20px rgba(0, 199, 209, 0.4);
}

.user-avatar {
  border: 3px solid #fff;
  display: block;
}

/* 等级徽章 */
.level-badge-mini {
  position: absolute;
  bottom: -2px;
  right: -2px;
  background: linear-gradient(45deg, #FF6B35, #FF8C00);
  color: #fff;
  font-size: 11px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  border: 2px solid #fff;
  white-space: nowrap;
}

/* 用户信息列 */
.user-info-col {
  flex: 1;
  min-width: 0;
}

/* 日期选择器区域 */
.date-picker-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.date-picker-btn {
  width: 44px !important;
  height: 44px !important;
  padding: 0 !important;
  background: linear-gradient(45deg, #FF9800, #F57C00) !important;
  border: 2px solid #fff !important;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3) !important;
}

.date-picker-btn:active {
  transform: scale(0.95);
}

.date-label {
  font-size: 11px;
  color: #666;
  text-align: center;
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 第一行：名字 + 称号 */
.name-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.user-title {
  font-size: 13px;
  color: #666;
}

/* 第二行：星星 + XP + 进度 */
.stats-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.xp-info {
  flex: 1;
}

.xp-text {
  font-size: 13px;
  color: #666;
}

.xp-next {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

/* 精简版经验条 */
.exp-bar-mini {
  width: 80px;
  background: #e8e8e8;
  height: 6px;
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}

.exp-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FFD700);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.task-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  color: #FF9800;
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

.task-card.reviewing {
  border-color: #FF9800;
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1), rgba(255, 152, 0, 0.05));
}

.task-card.reviewing .task-reward {
  color: #FF9800;
}

/* 非今日任务的样式 */
.task-card.not-today {
  background: rgba(245, 245, 245, 0.9);
  border-color: #E0E0E0;
  opacity: 0.85;
}

.task-card.not-today .task-title {
  color: #888;
}

.task-card.not-today .task-reward {
  color: #aaa;
}

.task-card.not-today .complete-btn {
  background: #e0e0e0 !important;
  box-shadow: none !important;
}

.task-card.not-today .complete-btn .van-icon {
  color: #999 !important;
}

.task-card.not-today:hover {
  transform: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

/* 任务时间提示 */
.task-time-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.reviewing-text {
  font-size: 12px;
  color: #FF9800;
  font-weight: bold;
  margin-top: 4px;
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
  justify-content: space-between;
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

/* 延期任务区域 */
.overdue-tasks {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px dashed #FF9800;
}

.overdue-title {
  color: #FF6B35 !important;
}

.overdue-count {
  font-size: 14px;
  color: #FF6B35;
}

.overdue-tasks .task-card {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 152, 0, 0.05));
  border-color: #FF9800;
}

.overdue-hint {
  color: #FF6B35 !important;
}

.load-more {
  text-align: center;
  padding: 20px;
}

.no-more-data {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
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

/* 查看结果对话框样式 */
.evidence-view-dialog {
  padding: 20px 0;
}

.task-info {
  text-align: left;
  margin-bottom: 20px;
}

.task-info h3 {
  color: #333;
  margin-bottom: 8px;
}

.task-info p {
  color: #666;
  font-size: 14px;
}

.reject-reason {
  background: #ffebee;
  border: 1px solid #ffcdd2;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #c62828;
  font-size: 14px;
}

.no-evidence {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.evidence-gallery {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.evidence-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 10px;
}

.evidence-image {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.evidence-meta {
  text-align: center;
}

.upload-time {
  color: #999;
  font-size: 12px;
}

.evidence-actions {
  text-align: center;
  margin-top: 20px;
}

/* 结果上传对话框样式 */
.evidence-dialog {
  padding: 20px 0;
  max-height: 70vh;
  overflow: auto;
}

.upload-options {
  padding: 20px;
  display: flex;
  gap: 12px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}

.preview-section {
  text-align: center;
  padding: 20px;
}

.evidence-preview {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 15px;
}

.preview-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.preview-grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}
.preview-item {
  width: 140px;
}
.preview-item .evidence-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-options .van-button {
  min-width: 120px;
  padding: 8px 12px;
}

.task-reject-reason {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  color: #ff4444;
  font-size: 13px;
}
.task-reject-reason .reject-text {
  color: #ff4444;
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

  .evidence-dialog {
    padding: 15px 0;
  }

  .upload-options {
    padding: 15px;
    display: flex;
    gap: 10px;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
  }

  .preview-section {
    padding: 15px;
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

/* ==================== RPG 等级系统样式 ==================== */

/* 等级进度环 */
.level-ring-container {
  display: none;
}

/* 用户信息行 */
.user-name-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.title-text {
  font-size: 14px;
  color: #666;
}

/* 经验条 */
.exp-bar-container {
  display: none;
}

/* XP 动画 */
.xp-animation {
  position: fixed;
  pointer-events: none;
  z-index: 10000;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #FF6B35;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.xp-icon {
  font-size: 24px;
}

.xp-text {
  background: linear-gradient(45deg, #FF6B35, #FFD700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 任务奖励中的 XP 提示 */
.task-reward .xp-reward {
  margin-left: 8px;
  font-size: 12px;
  color: #FF6B35;
  background: rgba(255, 107, 53, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

/* XP 动画 */
.xp-animation {
  position: fixed;
  pointer-events: none;
  z-index: 10000;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #FF6B35;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.xp-icon {
  font-size: 24px;
}

.xp-text {
  background: linear-gradient(45deg, #FF6B35, #FFD700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 任务奖励中的 XP 提示 */
.task-reward .xp-reward {
  margin-left: 8px;
  font-size: 12px;
  color: #FF6B35;
  background: rgba(255, 107, 53, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

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

/* 连签进度条样式 - 新设计 */
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


