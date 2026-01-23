<template>
  <div class="parent-dashboard">
    <ParentNav />
    <div class="cards">
      <div class="card clickable" @click="goToTasks">
        <h3>今日任务完成率</h3>
        <div class="metric">{{ completionRate }}%</div>
        <div class="sub">今日任务完成率（按小朋友平均）</div>
      </div>

      <div class="card clickable" @click="goToKids">
        <h3>小朋友星星余额</h3>
        <div class="metric">
          <van-icon name="star" color="#FFD700" /> 共 {{ kidsStats.length }} 位小朋友
        </div>
        <div class="sub">点击查看小朋友详情</div>
      </div>

      <div class="card clickable" @click="goToApprovals">
        <h3>待审核任务</h3>
        <div class="metric">{{ pendingApprovals }}</div>
        <div class="sub">需要家长审批的任务数量</div>
      </div>
    </div>

    <div class="quick-actions">
      <van-button type="primary" @click="$router.push('/parent/tasks')">发布任务</van-button>
      <van-button type="info" @click="$router.push('/parent/rewards')">上架商品</van-button>
      <van-button type="warning" @click="$router.push('/parent/templates')">使用模板</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { analytics, tasks, parents } from '@/utils/api.js'
import { showToast } from 'vant'
import ParentNav from '@/components/ParentNav.vue'
import { useRouter, useRoute } from 'vue-router'

const completionRate = ref(0)
const kidsStats = ref([])
const pendingApprovals = ref(0)

const loadDashboard = async () => {
  try {
    // Try to get kids stats from analytics endpoint first
    const kidsResp = await analytics.kidsStats().catch(() => ({ data: [] }))
    let kids = kidsResp.data || []

    // If analytics.kidsStats is not implemented (empty), fall back to parents.listKids()
    if (!kids || kids.length === 0) {
      const resp = await parents.listKids()
      // parents.listKids expected to return kids with id, username, nickname, starBalance
      kids = (resp.data || []).map(k => ({
        id: k.id,
        kidName: k.nickname || k.username,
        starBalance: k.starBalance || 0
      }))
    }

    // Populate kidsStats for display
    kidsStats.value = kids.map(k => ({
      id: k.id || k.kidId || k.kidId,
      nickname: k.kidName || k.nickname || k.kidName,
      starBalance: k.starBalance || k.starBalance === 0 ? k.starBalance : (k.starBalance || 0)
    }))

    // Compute aggregate completion rate as average of per-kid completion rates
    if (kidsStats.value.length > 0) {
      const rates = await Promise.all(kidsStats.value.map(async (kid) => {
        try {
          const r = await analytics.completionRate(kid.id)
          // backend returns object with completionRate or percentage
          return r.data?.completionRate || r.data?.percentage || 0
        } catch (er) {
          return 0
        }
      }))
      const avg = rates.reduce((s, v) => s + Number(v || 0), 0) / rates.length
      completionRate.value = Math.round((avg || 0) * 100) / 100
    } else {
      completionRate.value = 0
    }

    // Count pending approvals across all kids
    const pendingCounts = await Promise.all(kidsStats.value.map(async (kid) => {
      try {
        const r = await tasks.getPending(kid.id)
        return (r.data || []).length
      } catch (er) {
        return 0
      }
    }))
    pendingApprovals.value = pendingCounts.reduce((s, n) => s + n, 0)
  } catch (e) {
    console.error('Failed to load parent dashboard:', e)
    showToast('加载仪表盘数据失败')
  }
}

onMounted(() => {
  loadDashboard()
})
// Also refresh when route changes or window regains focus to keep data up-to-date
const router = useRouter()
const route = useRoute()

const onVisibilityChange = () => {
  if (document.visibilityState === 'visible') {
    loadDashboard()
  }
}
const onWindowFocus = () => loadDashboard()

window.addEventListener('visibilitychange', onVisibilityChange)
window.addEventListener('focus', onWindowFocus)

watch(() => route.fullPath, () => {
  loadDashboard()
})

onBeforeUnmount(() => {
  window.removeEventListener('visibilitychange', onVisibilityChange)
  window.removeEventListener('focus', onWindowFocus)
})

const goToTasks = () => router.push('/parent/tasks')
const goToKids = () => router.push('/parent/kids')
const goToApprovals = () => router.push('/parent/tasks')
</script>

<style scoped>
.parent-dashboard { padding: 20px; }
.cards { display:flex; gap:20px; flex-wrap:wrap; margin-bottom:20px; }
.card { background: rgba(255,255,255,0.95); padding:18px; border-radius:12px; min-width:220px; box-shadow:0 6px 18px rgba(0,0,0,0.08); }
.metric { font-size:28px; font-weight:bold; margin-top:8px; color:#333; }
.sub { color:#777; font-size:12px; margin-top:6px; }
.kids-list { list-style:none; padding:0; margin:10px 0 0 0; max-height:160px; overflow:auto; }
.kid-name { margin-right:8px; font-weight:600; }
.kid-stars { color:#FF9800; }
.quick-actions { display:flex; gap:12px; margin-top:10px; }
.clickable { cursor: pointer; }
.clickable:hover { transform: translateY(-3px); box-shadow:0 8px 22px rgba(0,0,0,0.12); }
</style>


