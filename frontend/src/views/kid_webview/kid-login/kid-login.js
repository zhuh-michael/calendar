// KidLogin 页面逻辑

const api = require('../utils/api.js')

Page({
  data: {
    apiBaseUrl: '',
    availableKids: [],
    selectedKid: null,
    selectedKidId: null,
    password: '',
    showPasswordInput: false,
    loading: false
  },

  onLoad() {
    this.setData({
      apiBaseUrl: api.getApiBaseUrl()
    })
    this.loadKids()
  },

  // 加载角色列表
  async loadKids() {
    try {
      const kids = await api.auth.getKids()
      var formattedKids = []
      for (var i = 0; i < kids.length; i++) {
        var kid = kids[i]
        formattedKids.push({
          id: kid.id,
          username: kid.username,
          nickname: kid.nickname,
          avatar: kid.avatar || '',
          starBalance: kid.starBalance
        })
      }
      this.setData({ availableKids: formattedKids })
    } catch (error) {
      wx.showToast({
        title: '加载角色失败，请稍后重试',
        icon: 'none'
      })
      console.error('加载儿童用户失败:', error)
    }
  },

  // 选择角色
  selectKid(e) {
    const id = e.currentTarget.dataset.id
    var selectedKid = null
    for (var i = 0; i < this.data.availableKids.length; i++) {
      if (this.data.availableKids[i].id === id) {
        selectedKid = this.data.availableKids[i]
        break
      }
    }
    if (selectedKid) {
      this.setData({
        selectedKidId: id,
        selectedKid: selectedKid
      })
    }
  },

  // 返回角色选择
  backToSelection() {
    this.setData({
      selectedKid: null,
      selectedKidId: null,
      showPasswordInput: false,
      password: ''
    })
  },

  // 显示密码输入框
  onShowPasswordInput() {
    this.setData({
      showPasswordInput: true
    })
  },

  // 密码输入
  onPasswordInput(e) {
    this.setData({
      password: e.detail.value
    })
  },

  // 处理登录
  async handleLogin() {
    if (!this.data.password) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      })
      return
    }

    this.setData({ loading: true })

    try {
      const user = await api.auth.login(
        this.data.selectedKid.username,
        this.data.password
      )

      // 存储 token 和用户信息
      wx.setStorageSync('token', user.token)
      wx.setStorageSync('kidUser', JSON.stringify(user))

      var welcomeText = '欢迎回来，' + user.nickname + '！'
      wx.showToast({
        title: welcomeText,
        icon: 'success'
      })

      // 跳转到首页
      wx.redirectTo({
        url: '/pages/webview/kid-dashboard/kid-dashboard'
      })
    } catch (error) {
      var errorMsg = error.data || '密码错误，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})
