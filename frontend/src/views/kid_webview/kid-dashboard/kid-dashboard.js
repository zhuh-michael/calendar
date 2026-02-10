// KidDashboard 页面逻辑

const api = require('../utils/api.js')

Page({
  data: {
    apiBaseUrl: '',
    userInfo: {},
    levelProgress: 0,
    xpToNextLevel: 50,
    pendingTasks: [],
    reviewingTasks: [],
    completedTasks: [],
    overdueTasks: [],
    overdueCount: 0,
    completingTasks: [],
    isLoadingTasks: false,
    selectedDate: Date.now(), // 使用时间戳
    todayTotalStars: 0,

    // 证据上传相关
    showEvidenceDialog: false,
    showViewEvidenceDialog: false,
    selectedTaskForEvidence: null,
    currentTaskEvidence: [],
    evidenceFiles: [],
    evidencePreviews: [],
    uploadingEvidence: false,

    // 打卡相关
    showCheckInDialog: false,
    checkedIn: false,
    selectedMood: null,
    checkInResult: null,
    streakDays: 0,
    streakBonusMessage: '',
    checkInLoading: false,

    // 升级弹窗
    showLevelUpDialog: false,
    newLevel: 1,
    newTitle: '',

    // 心情选项（固定数据，不需要响应式）
    moodsData: [
      { type: 'HAPPY', emoji: '😄', label: '开心' },
      { type: 'NEUTRAL', emoji: '😐', label: '一般' },
      { type: 'SAD', emoji: '😢', label: '难过' },
      { type: 'ANGRY', emoji: '😡', label: '生气' }
    ]
  },

  onLoad() {
    this.setData({
      apiBaseUrl: api.getApiBaseUrl()
    })
    this.loadUserInfo()
    // 注意：不在 onLoad 时调用 loadTodayTasks 和 loadOverdueTasks
    // 因为 userId 可能还没有，它们会在 refreshUserInfo 成功后被调用
  },

  onShow() {
    // 每次页面显示时刷新用户信息（从服务器获取最新数据）
    this.refreshUserInfo()
  },

  onPullDownRefresh() {
    this.loadUserInfo()
    this.loadTodayTasks()
    this.loadOverdueTasks()
    wx.stopPullDownRefresh()
  },

  // 刷新用户信息（从服务器获取最新数据）
  refreshUserInfo() {
    var that = this
    // 先从本地读取，确保有初始数据
    var userStr = wx.getStorageSync('kidUser')
    if (userStr) {
      var user = JSON.parse(userStr)
      this.setData({ userInfo: user })
    }

    // 使用封装好的 API 获取最新用户信息
    api.auth.getCurrentUser().then(function(res) {
      // /auth/me 返回的是 id 字段，需要同步到 userId 字段（保持与登录接口一致）
      if (res.id && !res.userId) {
        res.userId = res.id
      }
      // 保存到本地存储
      wx.setStorageSync('kidUser', JSON.stringify(res))
      that.setData({ userInfo: res })
      that.calculateLevelProgress()
      // 用户信息更新后再加载任务列表
      that.loadTodayTasks()
      that.loadOverdueTasks()
      // 检查是否需要打卡
      that.checkCheckinStatus()
    }).catch(function(err) {
      console.error('Failed to refresh user info:', err)
      // 失败时从本地读取并尝试加载任务
      var userStr = wx.getStorageSync('kidUser')
      if (userStr) {
        var user = JSON.parse(userStr)
        that.setData({ userInfo: user })
        that.calculateLevelProgress()
        that.loadTodayTasks()
        that.loadOverdueTasks()
        // 检查是否需要打卡
        that.checkCheckinStatus()
      }
    })
  },

  // 获取用户信息
  loadUserInfo() {
    var userStr = wx.getStorageSync('kidUser')
    if (userStr) {
      var user = JSON.parse(userStr)
      this.setData({ userInfo: user })
      this.calculateLevelProgress()
    }
  },

  // 加载今日任务
  async loadTodayTasks() {
    if (!this.data.userInfo.userId) return

    this.setData({ isLoadingTasks: true })

    try {
      const date = new Date(this.data.selectedDate)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const dateStr = year + '-' + month + '-' + day

      const allTasks = await api.tasks.getByKidAndDate(this.data.userInfo.userId, dateStr) || []
      console.log('loadTodayTasks - allTasks:', allTasks)
      if (allTasks.length > 0) {
        console.log('First task keys:', Object.keys(allTasks[0]))
        console.log('First task startTime:', allTasks[0].startTime)
      }

      // 分类任务
      const isTodo = function(s) { return s === 'TODO' || s === 0 || s === '0' }
      const isPending = function(s) { return s === 'PENDING' || s === 1 || s === '1' }
      const isDone = function(s) { return s === 'DONE' || s === 2 || s === '2' }

      this.setData({
        pendingTasks: allTasks.filter(function(task) { return isTodo(task.status) }),
        reviewingTasks: allTasks.filter(function(task) { return isPending(task.status) }),
        completedTasks: allTasks.filter(function(task) { return isDone(task.status) })
      })

      // 预处理任务时间格式化（供 WXML 直接使用）
      ;['pendingTasks', 'reviewingTasks', 'completedTasks'].forEach(key => {
        const tasks = this.data[key]
        tasks.forEach(task => {
          task.formattedTime = this.formatTaskTime(task.startTime)
        })
        this.setData({ [key]: tasks })
      })

      // 计算今日获得星星总数
      let totalStars = 0
      for (var i = 0; i < allTasks.length; i++) {
        var task = allTasks[i]
        if (isDone(task.status)) {
          totalStars = totalStars + task.rewardStars
        }
      }
      this.setData({ todayTotalStars: totalStars })

    } catch (error) {
      console.error('Failed to load tasks:', error)
      wx.showToast({ title: '加载任务失败', icon: 'none' })
    } finally {
      this.setData({ isLoadingTasks: false })
    }
  },

  // 加载延期任务
  async loadOverdueTasks() {
    if (!this.data.userInfo.userId) return

    try {
      const overdueTasks = await api.tasks.getOverdue(this.data.userInfo.userId, 0, 20)
      // 预处理任务时间格式化
      overdueTasks.forEach(task => {
        task.formattedDateTime = this.formatTaskDateTime(task.startTime)
      })
      this.setData({
        overdueTasks: overdueTasks || [],
        overdueCount: overdueTasks ? overdueTasks.length : 0
      })
    } catch (error) {
      console.error('Failed to load overdue tasks:', error)
    }
  },

  // 完成任务
  async handleComplete(e) {
    const task = e.currentTarget.dataset.task
    if (!task) return

    const completingTasks = this.data.completingTasks
    if (completingTasks.indexOf(task.id) !== -1) return

    completingTasks.push(task.id)
    this.setData({ completingTasks: completingTasks })

    try {
      if (task.needsReview) {
        // 需要上传结果
        this.setData({ selectedTaskForEvidence: task, showEvidenceDialog: true })
      } else {
        // 直接完成任务
        await api.tasks.complete(task.id, this.data.userInfo.userId)
        this.updateTaskStatus(task.id, 2)
        wx.showToast({ title: '任务完成！', icon: 'success' })
      }
    } catch (error) {
      console.error('Failed to complete task:', error)
      wx.showToast({ title: '任务完成失败', icon: 'none' })
    } finally {
      const newCompletingTasks = this.data.completingTasks.filter(function(id) { return id !== task.id })
      this.setData({ completingTasks: newCompletingTasks })
    }
  },

  // 更新任务状态
  updateTaskStatus(taskId, newStatus) {
    const pendingTasks = this.data.pendingTasks
    const index = pendingTasks.findIndex(function(t) { return t.id === taskId })

    if (index !== -1) {
      const task = pendingTasks[index]
      task.status = newStatus
      pendingTasks.splice(index, 1)

      if (newStatus === 2) {
        // 移到已完成
        const completedTasks = [task].concat(this.data.completedTasks)
        this.setData({
          pendingTasks: pendingTasks,
          completedTasks: completedTasks,
          todayTotalStars: this.data.todayTotalStars + task.rewardStars
        })
      } else if (newStatus === 1) {
        // 移到审核中
        const reviewingTasks = [task].concat(this.data.reviewingTasks)
        this.setData({ pendingTasks: pendingTasks, reviewingTasks: reviewingTasks })
      }
    }
  },

  // 完成任务（简化）
  completeTask(e) {
    const taskId = e.currentTarget.dataset.id
    const task = this.data.pendingTasks.find(function(t) { return t.id === taskId })
    if (task) {
      this.handleComplete({ currentTarget: { dataset: { task: task } } })
    }
  },

  // 查看任务结果
  viewTaskEvidence(e) {
    var that = this
    var task = e.currentTarget.dataset.task
    if (task) {
      // 先加载证据
      api.tasks.getEvidence(task.id).then(function(response) {
        that.setData({
          selectedTaskForEvidence: task,
          currentTaskEvidence: response || [],
          showViewEvidenceDialog: true
        })
      }).catch(function(error) {
        console.error('Failed to load task evidence:', error)
        that.setData({
          selectedTaskForEvidence: task,
          currentTaskEvidence: [],
          showViewEvidenceDialog: true
        })
      })
    }
  },

  // 日期选择器变化
  onDateChange(e) {
    var dateStr = e.detail.value
    var date = new Date(dateStr)
    this.setData({
      selectedDate: date.getTime()
    })
    this.loadTodayTasks()
  },

  // 格式化选中的日期
  get formattedSelectedDate() {
    const date = new Date(this.data.selectedDate)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekDay = weekDays[date.getDay()]
    return month + '/' + day + ' ' + weekDay
  },

  // 是否是今日
  get isToday() {
    const today = new Date()
    const selected = new Date(this.data.selectedDate)
    return today.toDateString() === selected.toDateString()
  },

  // 任务列表标题
  get selectedDateDisplay() {
    const date = new Date(this.data.selectedDate)
    if (this.isToday) return '今日任务'
    const month = date.getMonth() + 1
    const day = date.getDate()
    return month + '月' + day + '日任务'
  },

  // 头像框样式
  get avatarFrameClass() {
    const level = this.data.userInfo.level || 1
    if (level >= 10) return 'diamond'
    if (level >= 7) return 'gold'
    if (level >= 4) return 'silver'
    return 'bronze'
  },

  // 计算等级进度
  calculateLevelProgress() {
    var userInfo = this.data.userInfo
    if (!userInfo) {
      this.setData({
        levelProgress: 0,
        xpToNextLevel: 50
      })
      return
    }

    var exp = userInfo.exp || 0
    var level = userInfo.level || 1

    var baseExp = [0, 50, 150, 300, 500, 800, 1200, 1700, 2300, 3000, 4000]
    var currentLevelExp = baseExp[level - 1] || 0
    var nextLevelExpVal = baseExp[level] || baseExp[baseExp.length - 1] + 1000

    var progress = (exp - currentLevelExp) / (nextLevelExpVal - currentLevelExp)
    progress = Math.min(100, Math.max(0, Math.round(progress * 100)))

    var xpNeeded = Math.max(0, nextLevelExpVal - exp)

    this.setData({
      levelProgress: progress,
      xpToNextLevel: xpNeeded
    })
  },

  // 格式化任务时间
  formatTaskTime(isoTime) {
    if (!isoTime) return ''
    var date
    if (typeof isoTime === 'string') {
      // 处理 ISO 8601 格式，兼容 iOS
      var formatted = isoTime
      if (formatted.includes('T')) {
        formatted = formatted.replace('T', ' ')
      }
      formatted = formatted.replace(/\.\d{3}/, '')
      // iOS 必须用 / 分隔日期
      formatted = formatted.replace(/-/g, '/')
      date = new Date(formatted)
    } else {
      date = new Date(isoTime)
    }
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.warn('Invalid date:', isoTime)
      return ''
    }
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return hours + ':' + minutes
  },

  // 格式化任务日期时间
  formatTaskDateTime(isoTime) {
    if (!isoTime) return ''
    var date
    if (typeof isoTime === 'string') {
      // 处理 ISO 8601 格式，兼容 iOS
      var formatted = isoTime
      if (formatted.includes('T')) {
        formatted = formatted.replace('T', ' ')
      }
      formatted = formatted.replace(/\.\d{3}/, '')
      // iOS 必须用 / 分隔日期
      formatted = formatted.replace(/-/g, '/')
      date = new Date(formatted)
    } else {
      date = new Date(isoTime)
    }
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.warn('Invalid date:', isoTime)
      return ''
    }
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return month + '月' + day + '日 ' + hours + ':' + minutes
  },

  // 跳转到商店
  goToShop() {
    wx.navigateTo({
      url: '/pages/webview/reward-shop/reward-shop'
    })
  },

  // 跳转到幸运屋
  goToLuckyHouse() {
    wx.navigateTo({
      url: '/pages/webview/lucky-house/lucky-house'
    })
  },

  // ========== 证据上传相关方法 ==========

  // 拍照
  takePhoto() {
    var that = this
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['camera'],
      success: function(res) {
        console.log('takePhoto response:', res)
        var tempFilePath = res.tempFiles[0].tempFilePath
        console.log('tempFilePath:', tempFilePath)
        var evidenceFiles = that.data.evidenceFiles
        var evidencePreviews = that.data.evidencePreviews

        evidenceFiles.push(tempFilePath)
        evidencePreviews.push(tempFilePath)

        that.setData({
          evidenceFiles: evidenceFiles,
          evidencePreviews: evidencePreviews
        })
        console.log('evidenceFiles after takePhoto:', evidenceFiles)
      }
    })
  },

  // 从相册选择
  chooseFromGallery() {
    var that = this
    wx.chooseMedia({
      count: 5,
      mediaType: ['image'],
      sourceType: ['album'],
      success: function(res) {
        console.log('chooseFromGallery response:', res)
        var tempFiles = res.tempFiles
        var evidenceFiles = that.data.evidenceFiles
        var evidencePreviews = that.data.evidencePreviews

        for (var i = 0; i < tempFiles.length; i++) {
          console.log('File ' + i + ':', tempFiles[i].tempFilePath)
          evidenceFiles.push(tempFiles[i].tempFilePath)
          evidencePreviews.push(tempFiles[i].tempFilePath)
        }

        that.setData({
          evidenceFiles: evidenceFiles,
          evidencePreviews: evidencePreviews
        })
        console.log('evidenceFiles after chooseFromGallery:', evidenceFiles)
      }
    })
  },

  // 提交证据 - 直接实现上传逻辑，避免 api.js 封装问题
  submitWithEvidence() {
    var that = this

    if (this.data.evidenceFiles.length === 0) {
      wx.showToast({ title: '请先上传照片', icon: 'none' })
      return
    }

    this.setData({ uploadingEvidence: true })

    var taskId = this.data.selectedTaskForEvidence.id
    var kidId = this.data.userInfo.userId
    var files = this.data.evidenceFiles

    console.log('submitWithEvidence - files:', files)
    console.log('submitWithEvidence - taskId:', taskId)

    // 直接实现上传逻辑
    var token = wx.getStorageSync('token')
    var API_BASE_URL = api.getApiBaseUrl() + '/tasks/' + taskId + '/evidence'

    var uploadTasks = files.map(function(filePath, index) {
      console.log('Uploading file ' + index + ':', filePath)
      return new Promise(function(resolve, reject) {
        if (!filePath) {
          console.error('File path is undefined at index:', index)
          reject(new Error('File path is undefined at index: ' + index))
          return
        }
        wx.uploadFile({
          url: API_BASE_URL,
          filePath: filePath,
          name: 'files',
          header: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'multipart/form-data'
          },
          success: function(res) {
            console.log('Upload success for file ' + index, res)
            try {
              var data = JSON.parse(res.data)
              resolve(data)
            } catch (e) {
              resolve(res.data)
            }
          },
          fail: function(err) {
            console.error('Upload fail for file ' + index, err)
            reject(err)
          }
        })
      })
    })

    Promise.all(uploadTasks)
      .then(function() {
        // 上传成功后完成任务
        return api.tasks.complete(taskId, kidId)
      })
      .then(function() {
        // 任务状态变为审核中
        that.updateTaskStatus(taskId, 1)

        // 关闭对话框
        that.setData({
          showEvidenceDialog: false,
          selectedTaskForEvidence: null,
          evidenceFiles: [],
          evidencePreviews: [],
          uploadingEvidence: false
        })

        wx.showToast({ title: '任务已提交审核！', icon: 'success' })
      })
      .catch(function(error) {
        console.error('Failed to submit task with evidence:', error)
        that.setData({ uploadingEvidence: false })
        wx.showToast({ title: '提交失败，请重试', icon: 'none' })
      })
  },

  // 关闭证据上传对话框
  closeEvidenceDialog() {
    this.setData({
      showEvidenceDialog: false,
      selectedTaskForEvidence: null,
      evidenceFiles: [],
      evidencePreviews: []
    })
  },

  // 关闭查看证据对话框
  closeViewEvidenceDialog() {
    this.setData({
      showViewEvidenceDialog: false,
      selectedTaskForEvidence: null,
      currentTaskEvidence: []
    })
  },

  // 加载任务证据
  loadTaskEvidence(taskId) {
    var that = this
    api.tasks.getEvidence(taskId).then(function(response) {
      that.setData({ currentTaskEvidence: response || [] })
    }).catch(function(error) {
      console.error('Failed to load task evidence:', error)
    })
  },

  // 预览证据图片
  previewEvidenceImage(e) {
    var that = this
    var src = e.currentTarget.dataset.src
    var images = this.data.currentTaskEvidence.map(function(item) {
      return that.data.apiBaseUrl + '/' + item.imagePath
    })
    var current = images.indexOf(src)

    wx.previewImage({
      urls: images,
      current: src
    })
  },

  // 替换证据
  replaceEvidence() {
    this.setData({
      showViewEvidenceDialog: false,
      showEvidenceDialog: true,
      evidenceFiles: [],
      evidencePreviews: []
    })
  },

  // 判断任务状态是否为等待审核
  isPendingStatus(status) {
    if (status === null || status === undefined) return false
    return status === 1 || status === '1' || status === 'PENDING' || status === 'pending'
  },

  // 格式化时间
  formatTime(iso) {
    if (!iso) return ''
    // 处理 ISO 8601 格式字符串或时间戳
    var date
    if (typeof iso === 'string' && iso.includes('T')) {
      date = new Date(iso.replace('T', ' ').replace(/-/g, '/'))
    } else {
      date = new Date(iso)
    }
    return date.toLocaleString()
  },

  // ========== 打卡相关方法 ==========

  // 选择心情
  selectMood(e) {
    this.setData({ selectedMood: e.currentTarget.dataset.mood })
  },

  // 更新连签奖励提示
  updateStreakBonusMessage(days) {
    var messages = {
      0: '好的开始！',
      1: '继续保持！',
      2: '渐入佳境！',
      3: '坚持就是胜利！',
      4: '你太棒了！',
      5: '明天有大奖！',
      6: '周冠王！奖励翻倍！',
      7: '新的周期开始！'
    }
    this.setData({ streakBonusMessage: messages[days % 7] || '继续加油！' })
  },

  // 检查是否需要打卡
  checkCheckinStatus() {
    var that = this
    var user = this.data.userInfo

    // 只有 kid 角色需要打卡
    if (user.role !== 'KID') return

    // 获取最新的 RPG 信息（包含连签天数）
    api.auth.getRpgInfo().then(function(rpgInfo) {
      // 更新连签奖励提示
      that.updateStreakBonusMessage(rpgInfo.streakDays || 0)

      // 检查今天是否已打卡
      var today = new Date().toISOString().split('T')[0]
      var lastCheckinDate = rpgInfo.lastCheckinDate

      if (lastCheckinDate !== today) {
        // 今天还没打卡，显示打卡弹窗（延迟显示，让页面先渲染完成）
        setTimeout(function() {
          that.setData({
            showCheckInDialog: true,
            checkedIn: false,
            streakDays: rpgInfo.streakDays || 0,
            selectedMood: null,
            checkInResult: null
          })
        }, 1500)
      } else {
        // 今天已打卡
        that.setData({ checkedIn: true })
      }
    }).catch(function(err) {
      console.error('Failed to check checkin status:', err)
    })
  },

  // 确认打卡
  confirmCheckIn() {
    var that = this

    if (!this.data.selectedMood) {
      wx.showToast({ title: '请先选择今天的心情', icon: 'none' })
      return
    }

    this.setData({ checkInLoading: true })

    // 调用后端打卡接口
    api.auth.checkIn().then(function(res) {
      // 更新本地用户信息
      var user = that.data.userInfo
      user.lastCheckinDate = new Date().toISOString().split('T')[0]
      user.streakDays = res.streakDays
      user.exp = (user.exp || 0) + res.xpGain
      user.starBalance = (user.starBalance || 0) + res.starGain
      user.level = res.newLevel
      user.levelTitle = res.newTitle

      wx.setStorageSync('kidUser', JSON.stringify(user))

      that.setData({
        userInfo: user,
        checkedIn: true,
        streakDays: res.streakDays,
        checkInLoading: false,
        checkInResult: {
          xpGain: res.xpGain,
          starGain: res.starGain,
          leveledUp: res.leveledUp,
          newLevel: res.newLevel,
          newTitle: res.newTitle
        }
      })
      that.calculateLevelProgress()

      // 如果升级了，显示升级弹窗
      if (res.leveledUp) {
        that.setData({
          showCheckInDialog: false,
          showLevelUpDialog: true,
          newLevel: res.newLevel,
          newTitle: res.newTitle
        })
      } else {
        // 如果没有升级，直接关闭打卡弹窗
        that.setData({ showCheckInDialog: false })
      }
    }).catch(function(err) {
      that.setData({ checkInLoading: false })
      console.error('Checkin failed:', err)
      wx.showToast({ title: '打卡失败，请重试', icon: 'none' })
    })
  },

  // 关闭打卡弹窗
  closeCheckInDialog() {
    this.setData({ showCheckInDialog: false })
  },

  // 关闭升级弹窗
  closeLevelUpDialog() {
    this.setData({ showLevelUpDialog: false })
  }
})
