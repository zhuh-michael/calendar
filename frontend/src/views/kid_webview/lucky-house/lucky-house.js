// LuckyHouse 页面逻辑

const api = require('../utils/api.js')

Page({
  data: {
    apiBaseUrl: '',
    userInfo: {},
    drawHistory: [],
    isDrawing: false,
    showResult: false,
    drawResult: null,
    drawCost: 20,
    canDraw: true
  },

  onLoad() {
    this.setData({
      apiBaseUrl: api.getApiBaseUrl()
    })
    this.loadUserInfo()
    // 注意：不在 onLoad 时调用 loadDrawHistory，因为 userId 还没有
    // loadDrawHistory 会在 refreshUserInfo 成功后被调用
  },

  onShow() {
    // 每次页面显示时刷新用户信息
    this.refreshUserInfo()
  },

  // 刷新用户信息（从服务器获取最新数据）
  refreshUserInfo() {
    var that = this
    // 先从本地读取，确保有初始数据
    var userStr = wx.getStorageSync('kidUser')
    if (userStr) {
      var user = JSON.parse(userStr)
      this.setData({
        userInfo: user,
        canDraw: user.starBalance >= this.data.drawCost
      })
    }

    // 使用封装好的 API 获取最新用户信息
    api.auth.getCurrentUser().then(function(res) {
      // /auth/me 返回的是 id 字段，需要同步到 userId 字段（保持与登录接口一致）
      if (res.id && !res.userId) {
        res.userId = res.id
      }
      wx.setStorageSync('kidUser', JSON.stringify(res))
      that.setData({
        userInfo: res,
        canDraw: res.starBalance >= that.data.drawCost
      })
      // 用户信息更新后再加载抽奖历史
      that.loadDrawHistory()
    }).catch(function(err) {
      console.error('Failed to refresh user info:', err)
      var userStr = wx.getStorageSync('kidUser')
      if (userStr) {
        var user = JSON.parse(userStr)
        that.setData({
          userInfo: user,
          canDraw: user.starBalance >= that.data.drawCost
        })
        that.loadDrawHistory()
      }
    })
  },

  // 获取用户信息
  loadUserInfo() {
    const userStr = wx.getStorageSync('kidUser')
    if (userStr) {
      const user = JSON.parse(userStr)
      this.setData({
        userInfo: user,
        canDraw: user.starBalance >= this.data.drawCost
      })
    }
  },

  // 加载抽奖历史
  async loadDrawHistory() {
    if (!this.data.userInfo.userId) return

    try {
      const history = await api.lucky.history(this.data.userInfo.userId)
      // 预处理数据，添加显示字段
      const processedHistory = this.preprocessHistory(history || [])
      this.setData({ drawHistory: processedHistory })
    } catch (error) {
      console.error('Failed to load draw history:', error)
    }
  },

  // 执行抽奖
  async performDraw() {
    if (!this.data.canDraw || this.data.isDrawing) return

    this.setData({
      isDrawing: true,
      showResult: false,
      drawResult: null
    })

    try {
      // 扣除星星
      const newStarBalance = this.data.userInfo.starBalance - this.data.drawCost
      const updatedUserInfo = this.data.userInfo
      updatedUserInfo.starBalance = newStarBalance
      this.setData({
        userInfo: updatedUserInfo
      })

      // 调用后端抽奖API
      const result = await api.lucky.draw(this.data.userInfo.userId)

      // 1.5秒后显示结果
      const that = this
      setTimeout(function() {
        // 更新星星余额
        if (result.starsEarned > 0) {
          const finalBalance = newStarBalance + result.starsEarned
          const finalUserInfo = that.data.userInfo
          finalUserInfo.starBalance = finalBalance
          that.setData({
            userInfo: finalUserInfo,
            canDraw: finalBalance >= that.data.drawCost
          })
        }

        // 设置抽奖结果
        const resultMap = {
          'GRAND_PRIZE': { emoji: '🎊', title: '大吉（5%）', description: '恭喜获得100星星大礼包奖励！', stars: 100 },
          'GOOD_PRIZE': { emoji: '🎉', title: '中吉（20%）', description: '恭喜获得免做卡奖励！', stars: 30 },
          'SMALL_PRIZE': { emoji: '⭐', title: '小吉（45%）', description: '恭喜获得10星星回血奖励！', stars: 10 },
          'ENCOURAGEMENT': { emoji: '💝', title: '鼓励（30%）', description: '谢谢参与！继续加油！', stars: 0 }
        }

        var resultData = resultMap[result.resultType]
        resultData.stars = result.starsEarned

        that.setData({
          drawResult: resultData,
          showResult: true,
          isDrawing: false
        })

        // 重新加载历史记录
        that.loadDrawHistory()
      }, 1500)

    } catch (error) {
      console.error('Failed to perform draw:', error)
      wx.showToast({ title: '抽奖失败，请重试', icon: 'none' })

      // 退还星星
      const refundBalance = this.data.userInfo.starBalance + this.data.drawCost
      const refundUserInfo = this.data.userInfo
      refundUserInfo.starBalance = refundBalance
      this.setData({
        userInfo: refundUserInfo,
        isDrawing: false
      })
    }
  },

  // 继续抽奖
  continueDrawing() {
    this.setData({
      showResult: false,
      drawResult: null
    })
    // 检查是否还能继续抽奖
    this.setData({
      canDraw: this.data.userInfo.starBalance >= this.data.drawCost
    })
  },

  // 获取结果表情
  getResultEmoji(resultType) {
    const resultMap = {
      'GRAND_PRIZE': '🎊',
      'GOOD_PRIZE': '🎉',
      'SMALL_PRIZE': '⭐',
      'ENCOURAGEMENT': '💝'
    }
    return resultMap[resultType] || '❓'
  },

  // 获取结果文本
  getResultText(resultType) {
    const resultMap = {
      'GRAND_PRIZE': '大吉（5%）',
      'GOOD_PRIZE': '中吉（20%）',
      'SMALL_PRIZE': '小吉（45%）',
      'ENCOURAGEMENT': '鼓励（30%）'
    }
    return resultMap[resultType] || '未知结果'
  },

  // 预处理抽奖记录，添加显示字段
  preprocessHistory(history) {
    if (!history || !Array.isArray(history)) return []
    var that = this
    return history.map(function(record) {
      var emojiMap = {
        'GRAND_PRIZE': '🎊',
        'GOOD_PRIZE': '🎉',
        'SMALL_PRIZE': '⭐',
        'ENCOURAGEMENT': '💝'
      }
      var textMap = {
        'GRAND_PRIZE': '大吉（5%）',
        'GOOD_PRIZE': '中吉（20%）',
        'SMALL_PRIZE': '小吉（45%）',
        'ENCOURAGEMENT': '鼓励（30%）'
      }
      record.resultEmoji = emojiMap[record.resultType] || '❓'
      record.resultText = textMap[record.resultType] || '未知结果'
      // 预处理格式化时间
      record.formattedTime = that.formatRecordTime(record.createTime)
      return record
    })
  },

  // 格式化记录时间（供 WXML 直接使用）
  formatRecordTime(iso) {
    if (!iso) return ''
    var date
    if (typeof iso === 'string') {
      // 处理 ISO 8601 格式，兼容 iOS
      var formatted = iso
      if (formatted.includes('T')) {
        formatted = formatted.replace('T', ' ')
      }
      formatted = formatted.replace(/\.\d{3}/, '')
      // iOS 必须用 / 分隔日期
      formatted = formatted.replace(/-/g, '/')
      date = new Date(formatted)
    } else {
      date = new Date(iso)
    }
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return ''
    }
    var now = new Date()
    var diff = now - date
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
    return date.toLocaleDateString()
  },

  // 格式化时间（保留备用）
  formatTime(timestamp) {
    if (!timestamp) return ''
    
    var date
    if (typeof timestamp === 'string') {
      date = new Date(timestamp.replace('T', ' ').replace(/-/g, '/'))
    } else {
      date = new Date(timestamp)
    }
    
    var now = new Date()
    var diff = now - date

    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
    return date.toLocaleDateString()
  },

  // 计算结果卡片样式类
  get resultCardClass() {
    if (!this.data.drawResult) return ''
    const stars = this.data.drawResult.stars
    if (stars >= 100) return 'grand-prize'
    if (stars >= 30) return 'good-prize'
    if (stars >= 10) return 'small-prize'
    return 'encouragement'
  },

  // 跳转到首页
  goToDashboard() {
    wx.navigateTo({
      url: '/pages/webview/kid-dashboard/kid-dashboard'
    })
  },

  // 跳转到商店
  goToShop() {
    wx.navigateTo({
      url: '/pages/webview/reward-shop/reward-shop'
    })
  }
})
