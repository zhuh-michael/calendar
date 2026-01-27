import axios from 'axios'
import { showToast } from 'vant'

const API_BASE = (import.meta && import.meta.env && import.meta.env.VITE_API_BASE)

const client = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: false
})

client.interceptors.request.use(config => {
  try {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
  } catch (e) {}
  return config
})

client.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err && err.response && err.response.status === 401) {
      try { localStorage.removeItem('token') } catch (e) {}
      // Notify user
      try {
        // show a 5s countdown toast and then redirect
        if (typeof window !== 'undefined') {
          const path = window.location.pathname || ''
          const loginPath = path.startsWith('/parent') ? '/parent/login' : '/kid/login'
          let seconds = 5
          const id = setInterval(() => {
            try {
              showToast({ message: `身份登录失败，${seconds}秒后跳转登录`, duration: 1000 })
            } catch (e) {}
            seconds -= 1
            if (seconds <= 0) {
              clearInterval(id)
              window.location.href = loginPath
            }
          }, 1000)
        } else {
          showToast({ message: '身份登录失败，请重新登录', type: 'warning' })
        }
      } catch (e) {}
    }
    return Promise.reject(err)
  }
)

export const auth = {
  login: (username, password) => client.post('/api/auth/login', { username, password }),
  getKids: () => client.get('/api/auth/kids'),
  getCurrentUser: () => client.get('/api/auth/me'),
  checkIn: () => client.post('/api/auth/checkin'),
  getRpgInfo: () => client.get('/api/auth/rpg-info')
}

export const tasks = {
  getByKidAndDate: (kidId, date) => client.get(`/api/tasks/kid/${kidId}`, { params: { date } }),
  getPending: (kidId, page = 0, size = 20) => client.get(`/api/tasks/kid/${kidId}/pending`, { params: { page, size } }),
  getAllPending: (page = 0, size = 20) => client.get('/api/tasks/pending-all', { params: { page, size } }),
  getByKidAll: (kidId, page = 0, size = 20) => client.get(`/api/tasks/kid/${kidId}/all`, { params: { page, size } }),
  getAll: (page = 0, size = 20) => client.get('/api/tasks/all', { params: { page, size } }),
  query: (params) => client.get('/api/tasks/query', { params }),
  templates: () => client.get('/api/tasks/templates'),
  create: (task) => client.post('/api/tasks', task),
  update: (taskId, task) => client.put(`/api/tasks/${taskId}`, task),
  delete: (taskId) => client.delete(`/api/tasks/${taskId}`),
  complete: (taskId, userId) => client.post(`/api/tasks/${taskId}/complete`, null, { params: { userId } }),
  approve: (taskId) => client.post(`/api/tasks/${taskId}/approve`),
  reject: (taskId, rejectReason) => client.post(`/api/tasks/${taskId}/reject`, null, { params: { rejectReason } }),
  uploadEvidence: (taskId, files) => {
    const formData = new FormData()
    if (Array.isArray(files)) {
      files.forEach(f => formData.append('files', f))
    } else if (files) {
      formData.append('files', files)
    }
    return client.post(`/api/tasks/${taskId}/evidence`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  getEvidence: (taskId) => client.get(`/api/tasks/${taskId}/evidence`),
  deleteEvidence: (evidenceId) => client.delete(`/api/tasks/evidence/${evidenceId}`),
  fromTemplate: (templateId, kidId, startDate, endDate) =>
    client.post('/api/tasks/from-template', null, { params: { templateId, kidId, startDate, endDate } })
}

export const rewards = {
  list: () => client.get('/api/rewards'),
  getByType: (type) => client.get(`/api/rewards/type/${type}`),
  getById: (id) => client.get(`/api/rewards/${id}`),
  create: (formData) => client.post('/api/rewards', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  update: (id, formData) => client.put(`/api/rewards/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  delete: (id) => client.delete(`/api/rewards/${id}`),
  purchase: (rewardId, kidId) => client.post(`/api/rewards/${rewardId}/purchase`, null, { params: { kidId } })
}

// admin / parent APIs
export const parents = {
  listKids: () => client.get('/api/parents/kids'),
  createKid: (formData) => client.post('/api/auth/create-kid', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  updateKid: (kidId, formData) => client.put(`/api/parents/kids/${kidId}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  deleteKid: (kidId) => client.delete(`/api/parents/kids/${kidId}`),
  adjustKid: (kidId, amount, note) => client.post(`/api/parents/kids/${kidId}/adjust`, { amount, note })
}

// pending orders / admin delivery
export const orders = {
  getPending: () => client.get('/api/rewards/pending-orders'),
  deliver: (orderId) => client.post(`/api/rewards/orders/${orderId}/deliver`)
}

export const lucky = {
  draw: (kidId) => client.post('/api/lucky-draw/draw', null, { params: { kidId } }),
  history: (kidId) => client.get(`/api/lucky-draw/history/${kidId}`)
}

export const mood = {
  log: (kidId, moodType, note) => client.post('/api/mood', null, { params: { kidId, moodType, note } }),
  history: (kidId) => client.get(`/api/mood/kid/${kidId}`),
  historyRange: (kidId, startIso, endIso) => client.get(`/api/mood/kid/${kidId}/range`, { params: { start: startIso, end: endIso } })
}

export const analytics = {
  diligence: (kidId) => client.get(`/api/analytics/diligence/${kidId}`),
  wealth: (kidId) => client.get(`/api/analytics/wealth/${kidId}`),
  completionRate: (kidId) => client.get(`/api/analytics/completion-rate/${kidId}`),
  kidsStats: () => client.get('/api/analytics/kids-stats')
}

export default client


