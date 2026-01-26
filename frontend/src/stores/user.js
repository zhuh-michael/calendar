import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { auth } from '@/utils/api.js'

export const useUserStore = defineStore('user', () => {
  // 状态
  const currentUser = ref(null)
  const isLoading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!currentUser.value)
  const isKid = computed(() => currentUser.value?.role === 'KID')
  const isParent = computed(() => currentUser.value?.role === 'PARENT')

  // RPG 相关计算属性
  const userLevel = computed(() => currentUser.value?.level || 1)
  const userExp = computed(() => currentUser.value?.exp || 0)
  const userTitle = computed(() => currentUser.value?.levelTitle || '星际见习生')
  const userStreak = computed(() => currentUser.value?.streakDays || 0)
  const userAvatarFrame = computed(() => currentUser.value?.avatarFrame || 'bronze')

  // 加载用户信息
  const loadUserInfo = async (forceRefresh = false) => {
    // 如果不是强制刷新且已有用户信息，直接返回
    if (!forceRefresh && currentUser.value) {
      return currentUser.value
    }

    isLoading.value = true

    try {
      // 优先从后端获取最新用户信息
      const response = await auth.getCurrentUser()
      const userData = {
        userId: response.data.id,
        username: response.data.username,
        nickname: response.data.nickname,
        starBalance: response.data.starBalance,
        avatar: response.data.avatar,
        role: response.data.role,
        // RPG 字段
        exp: response.data.exp || 0,
        level: response.data.level || 1,
        levelTitle: response.data.levelTitle || '星际见习生',
        streakDays: response.data.streakDays || 0,
        avatarFrame: response.data.avatarFrame || 'bronze'
      }

      currentUser.value = userData

      // 更新本地缓存
      localStorage.setItem('kidUser', JSON.stringify(userData))

      return userData
    } catch (error) {
      console.error('Failed to load user info from server:', error)

      // 如果后端获取失败，回退到本地缓存
      try {
        const storedUser = localStorage.getItem('kidUser')
        if (storedUser) {
          const cachedUser = JSON.parse(storedUser)
          currentUser.value = cachedUser
          return cachedUser
        }
      } catch (cacheError) {
        console.error('Failed to load user info from cache:', cacheError)
      }

      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 更新星星余额
  const updateStarBalance = (newBalance) => {
    if (currentUser.value) {
      currentUser.value.starBalance = newBalance
      localStorage.setItem('kidUser', JSON.stringify(currentUser.value))
    }
  }

  // 增加星星
  const addStars = (amount) => {
    if (currentUser.value) {
      currentUser.value.starBalance += amount
      localStorage.setItem('kidUser', JSON.stringify(currentUser.value))
    }
  }

  // 减少星星
  const deductStars = (amount) => {
    if (currentUser.value) {
      currentUser.value.starBalance -= amount
      localStorage.setItem('kidUser', JSON.stringify(currentUser.value))
    }
  }

  // 更新 RPG 信息
  const updateRpgInfo = (rpgData) => {
    if (currentUser.value) {
      currentUser.value.exp = rpgData.exp
      currentUser.value.level = rpgData.level
      currentUser.value.levelTitle = rpgData.levelTitle
      currentUser.value.streakDays = rpgData.streakDays
      currentUser.value.avatarFrame = rpgData.avatarFrame
      localStorage.setItem('kidUser', JSON.stringify(currentUser.value))
    }
  }

  // 清除用户信息（登出时使用）
  const clearUserInfo = () => {
    currentUser.value = null
    localStorage.removeItem('kidUser')
    localStorage.removeItem('token')
  }

  // 从缓存初始化（应用启动时使用）
  const initializeFromCache = () => {
    try {
      const storedUser = localStorage.getItem('kidUser')
      if (storedUser) {
        currentUser.value = JSON.parse(storedUser)
      }
    } catch (error) {
      console.error('Failed to initialize user from cache:', error)
    }
  }

  return {
    // 状态
    currentUser,
    isLoading,

    // 计算属性
    isLoggedIn,
    isKid,
    isParent,
    userLevel,
    userExp,
    userTitle,
    userStreak,
    userAvatarFrame,

    // 方法
    loadUserInfo,
    updateStarBalance,
    addStars,
    deductStars,
    updateRpgInfo,
    clearUserInfo,
    initializeFromCache
  }
})
