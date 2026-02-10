// 微信小程序 API 工具类
var API_BASE_URL = 'https://www.zhuchenyi.com/api'
var FILE_BASE_URL = 'https://www.zhuchenyi.com'

// 请求封装
var request = async function(url, method, data) {
  if (method === void 0) { method = 'GET' }
  if (data === void 0) { data = {} }

  var token = wx.getStorageSync('token')

  var header = {
    'Content-Type': 'application/json'
  }

  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  return new Promise(function(resolve, reject) {
    wx.request({
      url: API_BASE_URL + url,
      method: method,
      data: data,
      header: header,
      success: function(res) {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // 401 未授权，清除登录状态并跳转到登录页
          wx.removeStorageSync('token')
          wx.removeStorageSync('kidUser')
          wx.showToast({
            title: '登录已过期',
            icon: 'none',
            duration: 1500
          })
          setTimeout(function() {
            wx.redirectTo({
              url: '/pages/webview/kid-login/kid-login'
            })
          }, 1500)
          reject({ message: '登录已过期' })
        } else {
          reject(res)
        }
      },
      fail: reject
    })
  })
}

// 认证相关 API
var auth = {
  // 获取儿童列表
  getKids: function() {
    return request('/auth/kids', 'GET')
  },

  // 获取当前登录用户信息
  getCurrentUser: function() {
    return request('/auth/me', 'GET')
  },

  // 登录
  login: function(username, password) {
    return request('/auth/login', 'POST', { username: username, password: password })
  },

  // 每日打卡
  checkIn: function() {
    return request('/auth/checkin', 'POST')
  },

  // 获取用户 RPG 信息
  getRpgInfo: function() {
    return request('/auth/rpg-info', 'GET')
  }
}

// 任务相关 API
var tasks = {
  // 获取指定日期任务
  getByKidAndDate: function(kidId, date) {
    return request('/tasks/kid/' + kidId + '?date=' + date, 'GET')
  },

  // 完成任务
  complete: function(taskId, kidId) {
    return request('/tasks/' + taskId + '/complete', 'POST', { kidId: kidId })
  },

  // 获取延期任务
  getOverdue: function(kidId, page, size) {
    if (page === void 0) { page = 0 }
    if (size === void 0) { size = 20 }
    return request('/tasks/kid/' + kidId + '/overdue?page=' + page + '&size=' + size, 'GET')
  },

  // 获取延期任务数量
  getOverdueCount: function(kidId) {
    return request('/tasks/kid/' + kidId + '/overdue/count', 'GET')
  },

  // 上传任务证据
  uploadEvidence: function(taskId, files) {
    return new Promise(function(resolve, reject) {
      var token = wx.getStorageSync('token')
      console.log('uploadEvidence called with files:', files)
      var uploadTasks = files.map(function(file, index) {
        console.log('Uploading file ' + index + ':', file)
        return new Promise(function(resolve, reject) {
          if (!file) {
            console.error('File path is undefined at index:', index)
            reject(new Error('File path is undefined'))
            return
          }
          wx.uploadFile({
            url: API_BASE_URL + '/tasks/' + taskId + '/evidence',
            filePath: file,  // 直接使用文件路径字符串
            name: 'files',  // 后端接口要求 'files' 字段名
            header: {
              'Authorization': 'Bearer ' + token,
              'Content-Type': 'multipart/form-data'
            },
            success: resolve,
            fail: reject
          })
        })
      })
      Promise.all(uploadTasks).then(resolve).catch(reject)
    })
  },

  // 获取任务证据
  getEvidence: function(taskId) {
    return request('/tasks/' + taskId + '/evidence', 'GET')
  }
}

// 幸运屋 API
var lucky = {
  // 抽奖 (后端使用 @RequestParam，所以用查询参数)
  draw: function(kidId) {
    return request('/lucky-draw/draw?kidId=' + kidId, 'POST')
  },
  // 获取抽奖历史

  // 获取抽奖历史
  history: function(kidId) {
    return request('/lucky-draw/history/' + kidId, 'GET')
  }
}

// 奖励商店 API
var rewards = {
  // 获取商品列表
  list: function() {
    return request('/rewards', 'GET')
  },

  // 购买商品 (使用查询参数 kidId)
  purchase: function(rewardId, kidId) {
    return request('/rewards/' + rewardId + '/purchase?kidId=' + kidId, 'POST', null)
  }
}

// 获取 API 基础 URL
function getApiBaseUrl() {
  return FILE_BASE_URL
}

// 导出模块
module.exports = {
  auth: auth,
  tasks: tasks,
  lucky: lucky,
  rewards: rewards,
  getApiBaseUrl: getApiBaseUrl
}
