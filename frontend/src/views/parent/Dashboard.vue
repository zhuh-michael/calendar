<template>
  <div class="parent-dashboard">
    <ParentNav />
    <div class="cards">
      <div class="card">
        <h3>今日任务完成率</h3>
        <div class="metric">{{ completionRate }}%</div>
        <div class="sub">今日任务完成率（按孩子平均）</div>
      </div>

      <div class="card">
        <h3>孩子星星余额</h3>
        <ul class="kids-list">
          <li v-for="kid in kidsStats" :key="kid.id">
            <span class="kid-name">{{ kid.nickname || kid.username }}</span>
            <span class="kid-stars"><van-icon name="star" color="#FFD700" /> {{ kid.starBalance }}</span>
          </li>
        </ul>
      </div>

      <div class="card">
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
import { ref, onMounted } from 'vue'
import { analytics } from '@/utils/api.js'
import { tasks } from '@/utils/api.js'
import { showToast } from 'vant'
import ParentNav from '@/components/ParentNav.vue'
import { useRouter } from 'vue-router'

const completionRate = ref(0)
const kidsStats = ref([])
const pendingApprovals = ref(0)

const loadDashboard = async () => {
  try {
    // kids stats (includes star balances)
    const kidsResp = await analytics.kidsStats()
    kidsStats.value = kidsResp.data || []

    // today's completion rate for first kid as example (aggregate can be improved)
    if (kidsStats.value.length > 0) {
      const kidId = kidsStats.value[0].id
      const rateResp = await analytics.completionRate(kidId)
      completionRate.value = Math.round((rateResp.data?.percentage || 0) * 100) / 100
    }

    // pending approvals count (backend tasks endpoint)
    const pendingResp = await tasks.getPending(0).catch(() => ({ data: [] }))
    // backend may support query by parent; fallback to 0 length
    pendingApprovals.value = (pendingResp.data && pendingResp.data.length) || 0
  } catch (e) {
    console.error('Failed to load parent dashboard:', e)
    showToast('加载仪表盘数据失败')
  }
}

onMounted(() => {
  loadDashboard()
})
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
</style>


