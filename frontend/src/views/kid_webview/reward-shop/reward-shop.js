// RewardShop 页面逻辑

const api = require('../utils/api.js')

Page({
  data: {
    apiBaseUrl: '',
    userInfo: {},
    rewardList: [],
    loading: false,
    purchasing: null,
    showConfirmDialog: false,
    selectedReward: null,
    defaultProductImage: 'https://via.placeholder.com/150x150?text=🎁'
  },

  onLoad() {
    this.setData({
      apiBaseUrl: api.getApiBaseUrl()
    })
    this.loadUserInfo()
    // 注意：不在 onLoad 时调用 loadRewards，因为 userId 可能还没有
    // loadRewards 会在 refreshUserInfo 成功后被调用
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
      this.setData({ userInfo: user })
    }

    // 使用封装好的 API 获取最新用户信息
    api.auth.getCurrentUser().then(function(res) {
      // /auth/me 返回的是 id 字段，需要同步到 userId 字段（保持与登录接口一致）
      if (res.id && !res.userId) {
        res.userId = res.id
      }
      wx.setStorageSync('kidUser', JSON.stringify(res))
      that.setData({ userInfo: res })
      // 用户信息更新后再加载商品列表
      that.loadRewards()
    }).catch(function(err) {
      console.error('Failed to refresh user info:', err)
      var userStr = wx.getStorageSync('kidUser')
      if (userStr) {
        var user = JSON.parse(userStr)
        that.setData({ userInfo: user })
        that.loadRewards()
      }
    })
  },

  // 获取用户信息
  loadUserInfo() {
    const userStr = wx.getStorageSync('kidUser')
    if (userStr) {
      const user = JSON.parse(userStr)
      this.setData({ userInfo: user })
    }
  },

  // 加载商品列表
  async loadRewards() {
    this.setData({ loading: true })
    try {
      const rewards = await api.rewards.list()
      var activeRewards = []
      for (var i = 0; i < rewards.length; i++) {
        if (rewards[i].active) {
          activeRewards.push(rewards[i])
        }
      }
      this.setData({ rewardList: activeRewards })
    } catch (error) {
      console.error('Failed to load rewards:', error)
      wx.showToast({ title: '加载商品失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 购买商品
  purchaseReward(e) {
    const rewardId = e.currentTarget.dataset.id
    var reward = null
    for (var i = 0; i < this.data.rewardList.length; i++) {
      if (this.data.rewardList[i].id === rewardId) {
        reward = this.data.rewardList[i]
        break
      }
    }

    if (!reward) return

    if (this.data.userInfo.starBalance < reward.cost) {
      wx.showToast({ title: '星星不足，无法兑换', icon: 'none' })
      return
    }

    this.setData({
      selectedReward: reward,
      showConfirmDialog: true
    })
  },

  // 确认购买
  async confirmPurchase() {
    if (!this.data.selectedReward) return

    this.setData({ purchasing: this.data.selectedReward.id })

    var that = this
    api.rewards.purchase(
      this.data.selectedReward.id,
      this.data.userInfo.userId
    ).then(function() {
      // 更新本地星星余额
      const newBalance = that.data.userInfo.starBalance - that.data.selectedReward.cost
      const updatedUserInfo = that.data.userInfo
      updatedUserInfo.starBalance = newBalance
      that.setData({
        userInfo: updatedUserInfo
      })

      wx.showToast({
        title: '🎉 兑换成功！',
        icon: 'success'
      })

      that.setData({
        showConfirmDialog: false,
        selectedReward: null,
        purchasing: null
      })
    }).catch(function(error) {
      console.error('Failed to purchase reward:', error)
      wx.showToast({ title: '兑换失败，请重试', icon: 'none' })
      that.setData({ purchasing: null })
    })
  },

  // 取消购买
  cancelPurchase() {
    this.setData({
      showConfirmDialog: false,
      selectedReward: null
    })
  },

  // 跳转到首页
  goToDashboard() {
    wx.navigateTo({
      url: '/pages/webview/kid-dashboard/kid-dashboard'
    })
  },

  // 跳转到幸运屋
  goToLuckyHouse() {
    wx.navigateTo({
      url: '/pages/webview/lucky-house/lucky-house'
    })
  }
})
