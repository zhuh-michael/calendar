import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/kid/login', name: 'KidLogin', component: () => import('@/views/kid/KidLogin.vue') },
  { path: '/kid/dashboard', name: 'KidDashboard', component: () => import('@/views/kid/KidDashboard.vue'), meta: { requiresAuth: true } },
  { path: '/kid/shop', name: 'KidRewardShop', component: () => import('@/views/kid/RewardShop.vue'), meta: { requiresAuth: true } },
  { path: '/kid/lucky-house', name: 'KidLuckyHouse', component: () => import('@/views/kid/LuckyHouse.vue'), meta: { requiresAuth: true } },

  { path: '/parent/login', name: 'ParentLogin', component: () => import('@/views/parent/Login.vue') },
  { path: '/parent/dashboard', name: 'ParentDashboard', component: () => import('@/views/parent/Dashboard.vue'), meta: { requiresAuth: true } },
  { path: '/parent/kids', name: 'ParentKids', component: () => import('@/views/parent/KidsManagement.vue'), meta: { requiresAuth: true } },
  { path: '/parent/rewards/approvals', name: 'ParentApprovals', component: () => import('@/views/parent/RewardApprovals.vue'), meta: { requiresAuth: true } },
  { path: '/parent/tasks', name: 'ParentTasks', component: () => import('@/views/parent/TaskManagement.vue'), meta: { requiresAuth: true } },
  { path: '/parent/rewards', name: 'ParentRewards', component: () => import('@/views/parent/RewardManagement.vue'), meta: { requiresAuth: true } },
  { path: '/parent/templates', name: 'ParentTemplates', component: () => import('@/views/parent/TemplateLibrary.vue'), meta: { requiresAuth: true } },
  { path: '/parent/analytics', name: 'ParentAnalytics', component: () => import('@/views/parent/AnalyticsPage.vue'), meta: { requiresAuth: true } },

  { path: '/', redirect: '/kid/login' },
  { path: '/:pathMatch(.*)*', redirect: '/kid/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// auth guard: require token for routes with meta.requiresAuth
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    // redirect to the appropriate login based on path
    if (to.path.startsWith('/parent')) return next('/parent/login')
    return next('/kid/login')
  }
  next()
})

export default router


