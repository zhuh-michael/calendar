<template>
  <!-- copied from frontend/kid/src/views/KidLogin.vue -->
  <div class="login-page">
    <!-- 童趣背景 -->
    <div class="background-decoration">
      <div class="star star-1">⭐</div>
      <div class="star star-2">🌟</div>
      <div class="star star-3">✨</div>
      <div class="star star-4">⭐</div>
      <div class="star star-5">🌟</div>
      <div class="cloud cloud-1">☁️</div>
      <div class="cloud cloud-2">☁️</div>
      <div class="cloud cloud-3">☁️</div>
      <div class="rainbow">🌈</div>
      <div class="butterfly butterfly-1">🦋</div>
      <div class="butterfly butterfly-2">🐝</div>
      <div class="butterfly butterfly-3">🦋</div>
    </div>

    <div class="login-container">
      <div class="welcome-section">
        <h1 class="welcome-title animate__animated animate__bounceIn">🌟 StarQuest 🌟</h1>
        <p class="welcome-subtitle">选择你的超级英雄身份，开始奇妙冒险吧！🚀</p>
        <div class="welcome-emoji">🎈🎪🎠</div>
      </div>

      <div class="avatar-selection" v-if="!selectedKid">
        <h2 class="section-title">🎭 选择你的超级英雄！</h2>
        <div class="avatar-grid">
          <div
            v-for="kid in availableKids"
            :key="kid.id"
            class="avatar-card animate__animated animate__zoomIn"
            :class="{ selected: selectedKidId === kid.id }"
            @click="selectKid(kid)"
          >
          <div class="avatar-wrapper">
            <van-image
              :src="kid.avatar || defaultAvatar"
              round
              width="80"
              height="80"
              class="avatar-image"
            />
            <div class="avatar-glow" v-if="selectedKidId === kid.id"></div>
            <div class="decoration-stars">
              <span class="decoration-star star-1">⭐</span>
              <span class="decoration-star star-2">✨</span>
            </div>
          </div>
            <div class="kid-name">{{ kid.nickname }}</div>
            <div class="kid-stars">
              <van-icon name="star" color="#ffd700" />
              {{ kid.starBalance }}
            </div>
          </div>
        </div>
      </div>

      <div class="password-section" v-if="selectedKid && !showPasswordInput">
        <div class="selected-avatar">
          <van-image
            :src="selectedKid.avatar || defaultAvatar"
            round
            width="100"
            height="100"
            class="selected-avatar-image"
          />
          <h3>{{ selectedKid.nickname }}</h3>
        </div>
        <van-button
          type="primary"
          size="large"
          round
          @click="showPasswordInput = true"
          class="enter-button animate__animated animate__pulse"
        >
          点击进入冒险
        </van-button>
      </div>

      <div class="password-form" v-if="showPasswordInput">
        <div class="password-avatar">
          <van-image
            :src="selectedKid.avatar || defaultAvatar"
            round
            width="80"
            height="80"
          />
          <div class="kid-name">{{ selectedKid.nickname }}</div>
        </div>

        <van-form @submit="handleLogin">
          <van-field
            v-model="password"
            type="password"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
            class="password-input"
          />
          <van-button
            type="primary"
            size="large"
            round
            native-type="submit"
            :loading="loading"
            class="login-button"
            style="background: linear-gradient(45deg, #ff6b35, #f7931e); border: none;"
          >
            开始冒险 🚀
          </van-button>
        </van-form>

        <van-button
          type="default"
          size="small"
          @click="backToSelection"
          class="back-button"
        >
          选择其他角色
        </van-button>
      </div>
    </div>

    <!-- 心情打卡弹窗 -->
    <van-dialog
      v-model:show="showMoodDialog"
      :show-cancel-button="false"
      :show-confirm-button="false"
      :close-on-click-overlay="false"
      class="mood-dialog"
    >
      <div class="mood-content">
        <div class="mood-header">
          <h2 class="mood-title">🌟 今天心情怎么样？</h2>
          <p class="mood-subtitle">选择你的心情，让我们一起记录美好的一天</p>
        </div>

        <div class="mood-options">
          <div
            v-for="mood in moodOptions"
            :key="mood.type"
            class="mood-option animate__animated animate__zoomIn"
            :class="{ selected: selectedMood === mood.type }"
            @click="selectMood(mood)"
          >
            <div class="mood-emoji">{{ mood.emoji }}</div>
            <div class="mood-text">{{ mood.text }}</div>
            <div class="mood-glow" v-if="selectedMood === mood.type"></div>
          </div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { auth, mood } from '@/utils/api.js'

const router = useRouter()
const availableKids = ref([])
const selectedKid = ref(null)
const selectedKidId = ref(null)
const password = ref('')
const showPasswordInput = ref(false)
const loading = ref(false)
const defaultAvatar = '/src/assets/default-avatar.svg'

const loadKids = async () => {
  try {
    const response = await auth.getKids()
    availableKids.value = response.data.map(kid => ({
      id: kid.id,
      username: kid.username,
      nickname: kid.nickname,
      avatar: kid.avatar || '',
      starBalance: kid.starBalance
    }))
  } catch (error) {
    showToast('加载角色失败，请稍后重试')
    console.error('加载儿童用户失败:', error)
  }
}

const selectKid = (kid) => {
  selectedKidId.value = kid.id
  selectedKid.value = kid
}

const backToSelection = () => {
  selectedKid.value = null
  selectedKidId.value = null
  showPasswordInput.value = false
  password.value = ''
}

const handleLogin = async () => {
  if (!password.value) {
    showToast('请输入密码')
    return
  }

  loading.value = true
  try {
    const response = await auth.login(selectedKid.value.username, password.value)
    const user = response.data
    // Store token for authentication
    localStorage.setItem('token', user.token)
    // Store user info for display
    localStorage.setItem('kidUser', JSON.stringify(user))
    showToast({ message: `欢迎回来，${user.nickname}！`, icon: 'success' })

    // Show mood check-in dialog only if there's no mood log today
    try {
      const today = new Date().toISOString().split('T')[0]
      const start = `${today}T00:00:00`
      const end = `${today}T23:59:59`
      const resp = await mood.historyRange(user.userId, start, end)
      const history = resp.data || []
      if (history.length === 0) {
        showMoodDialog.value = true
      } else {
        // already checked in today, go straight to dashboard
        router.push('/kid/dashboard')
      }
    } catch (e) {
      // if any error, default to showing dialog so user can still check in
      console.error('Failed to check mood history:', e)
      showMoodDialog.value = true
    }
  } catch (error) {
    showToast({ message: error.response?.data || '密码错误，请重试', icon: 'fail' })
  } finally {
    loading.value = false
  }
}

// Mood check-in functionality
const showMoodDialog = ref(false)
const selectedMood = ref('')
const moodOptions = [
  { emoji: '😄', text: '开心', type: 'HAPPY' },
  { emoji: '😐', text: '一般', type: 'NEUTRAL' },
  { emoji: '😡', text: '生气', type: 'ANGRY' },
  { emoji: '😭', text: '难过', type: 'SAD' }
]

const selectMood = async (mood) => {
  selectedMood.value = mood.type
  try {
    await mood.log(selectedKid.value.id, mood.type, '')
    showMoodDialog.value = false
    router.push('/kid/dashboard')
  } catch (error) {
    console.error('Failed to log mood:', error)
    // Even if mood logging fails, proceed to dashboard
    showMoodDialog.value = false
    router.push('/kid/dashboard')
  }
}

onMounted(() => {
  loadKids()
})
</script>

<style scoped>
.login-page {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #87CEEB 0%, #98D8E8 50%, #F0E68C 100%);
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.star {
  position: absolute;
  font-size: 24px;
  animation: twinkle 3s ease-in-out infinite;
  opacity: 0.8;
}

.star-1 { top: 10%; left: 10%; animation-delay: 0s; }
.star-2 { top: 20%; right: 15%; animation-delay: 1s; }
.star-3 { top: 60%; left: 20%; animation-delay: 2s; }
.star-4 { bottom: 20%; right: 10%; animation-delay: 0.5s; }
.star-5 { top: 40%; left: 70%; animation-delay: 1.5s; }

.cloud {
  position: absolute;
  font-size: 40px;
  animation: float 8s ease-in-out infinite;
  opacity: 0.6;
}

.cloud-1 { top: 15%; left: 5%; animation-delay: 0s; }
.cloud-2 { top: 25%; right: 8%; animation-delay: 3s; }
.cloud-3 { bottom: 15%; left: 15%; animation-delay: 6s; }

.rainbow {
  position: absolute;
  top: 5%;
  left: 50%;
  transform: translateX(-50%);
  font-size: 60px;
  animation: rainbow-glow 4s ease-in-out infinite;
}

.butterfly {
  position: absolute;
  font-size: 28px;
  animation: flutter 6s ease-in-out infinite;
}

.butterfly-1 { top: 30%; left: 80%; animation-delay: 0s; }
.butterfly-2 { top: 50%; right: 80%; animation-delay: 2s; }
.butterfly-3 { bottom: 30%; left: 85%; animation-delay: 4s; }

@keyframes twinkle {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; }
}

@keyframes float {
  0%, 100% { transform: translateY(0px) translateX(0px); }
  33% { transform: translateY(-10px) translateX(5px); }
  66% { transform: translateY(5px) translateX(-5px); }
}

@keyframes rainbow-glow {
  0%, 100% { transform: translateX(-50%) scale(1); opacity: 0.7; }
  50% { transform: translateX(-50%) scale(1.1); opacity: 1; }
}

@keyframes flutter {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  25% { transform: translateY(-5px) rotate(2deg); }
  50% { transform: translateY(-10px) rotate(0deg); }
  75% { transform: translateY(-5px) rotate(-2deg); }
}

.login-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 30px;
  padding: 40px 30px;
  max-width: 450px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
  border: 3px solid #FFD700;
  background-image:
    radial-gradient(circle at 20% 80%, rgba(255, 215, 0, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 105, 53, 0.1) 0%, transparent 50%);
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.welcome-title {
  font-size: 36px;
  margin-bottom: 10px;
  background: linear-gradient(45deg, #ff6b35, #f7931e, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  animation: title-bounce 2s ease-in-out infinite;
}

.welcome-subtitle {
  color: #333;
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 15px;
}

.welcome-emoji {
  font-size: 24px;
  margin-top: 10px;
  animation: emoji-float 3s ease-in-out infinite;
}

@keyframes title-bounce {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
}

@keyframes emoji-float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-3px); }
}

.section-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  font-weight: bold;
}

.avatar-selection {
  margin-bottom: 30px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.avatar-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25px 20px;
  border-radius: 25px;
  background: linear-gradient(135deg, #FFF 0%, #F8F9FA 100%);
  border: 3px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.avatar-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.avatar-card.selected {
  border-color: #ff6b35;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(247, 147, 30, 0.1));
  transform: scale(1.05);
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 10px;
}

.avatar-image {
  border: 3px solid #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.avatar-glow {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 50%;
  background: linear-gradient(45deg, #ff6b35, #f7931e);
  opacity: 0.3;
  animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
  0% {
    opacity: 0.3;
    transform: scale(1);
  }
  100% {
    opacity: 0.6;
    transform: scale(1.1);
  }
}

.decoration-stars {
  position: absolute;
  top: -5px;
  right: -5px;
  pointer-events: none;
}

.decoration-star {
  position: absolute;
  font-size: 16px;
  animation: sparkle 2s ease-in-out infinite;
}

.star-1 {
  top: 5px;
  right: 5px;
  animation-delay: 0s;
}

.star-2 {
  top: 15px;
  right: 15px;
  animation-delay: 1s;
}

@keyframes sparkle {
  0%, 100% { transform: scale(0.8) rotate(0deg); opacity: 0.7; }
  50% { transform: scale(1.2) rotate(180deg); opacity: 1; }
}

.kid-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.kid-stars {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.password-section {
  text-align: center;
}

.selected-avatar {
  margin-bottom: 30px;
}

.selected-avatar-image {
  margin-bottom: 15px;
  border: 4px solid #ff6b35;
}

.enter-button {
  background: linear-gradient(45deg, #ff6b35, #f7931e);
  border: none;
  padding: 15px 40px;
  font-size: 18px;
}

.password-form {
  text-align: center;
}

.password-avatar {
  margin-bottom: 30px;
}

.password-input {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  margin-bottom: 15px;
  padding: 15px;
  font-size: 18px;
}

.back-button {
  border: none;
  color: #666;
}

/* 心情打卡弹窗样式 */
.mood-dialog {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(30px);
  border-radius: 30px;
  border: 3px solid #FFD700;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.mood-content {
  padding: 30px 20px;
  text-align: center;
}

.mood-header {
  margin-bottom: 30px;
}

.mood-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  background: linear-gradient(45deg, #FF6B35, #F7931E, #FFD700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.mood-subtitle {
  font-size: 16px;
  color: #666;
}

.mood-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.mood-option {
  background: linear-gradient(135deg, #FFF 0%, #F8F9FA 100%);
  border-radius: 20px;
  padding: 25px 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.mood-option:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
}

.mood-option.selected {
  border-color: #FF6B35;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(247, 147, 30, 0.1));
  transform: scale(1.05);
}

.mood-emoji {
  font-size: 48px;
  margin-bottom: 10px;
}

.mood-text {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.mood-glow {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 20px;
  background: linear-gradient(45deg, #FF6B35, #F7931E);
  opacity: 0.3;
  animation: mood-glow 2s ease-in-out infinite alternate;
}

@keyframes mood-glow {
  from { opacity: 0.3; transform: scale(1); }
  to { opacity: 0.6; transform: scale(1.02); }
}

@media (max-width: 480px) {
  .login-container {
    padding: 30px 20px;
    margin: 10px;
  }

  .avatar-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .avatar-card {
    padding: 15px 10px;
  }

  .mood-content {
    padding: 20px 15px;
  }

  .mood-title {
    font-size: 24px;
  }

  .mood-options {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .mood-option {
    padding: 20px 10px;
  }

  .mood-emoji {
    font-size: 40px;
  }
}

</style>


