<template>
  <div class="analytics-page">
    <ParentNav />
    <h2>报表中心</h2>
    <div class="charts">
      <div class="chart-card">
        <h3>勤奋度曲线（近7天）</h3>
        <svg ref="diligenceSvg" :width="600" :height="200"></svg>
      </div>
      <div class="chart-card">
        <h3>财富分布</h3>
        <svg ref="wealthSvg" :width="600" :height="200"></svg>
      </div>
      <div class="chart-card">
        <h3>今日完成率</h3>
        <div class="metric">{{ completionRateDisplay }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ParentNav from '@/components/ParentNav.vue'
import { analytics } from '@/utils/api.js'
import { showToast } from 'vant'

const diligenceSvg = ref(null)
const wealthSvg = ref(null)
const completionRate = ref(0)
const completionRateDisplay = ref('0%')

const drawLineChart = (svgEl, points) => {
  if (!svgEl) return
  const svg = svgEl
  const w = svg.getAttribute('width')
  const h = svg.getAttribute('height')
  const max = Math.max(...points, 1)
  const min = Math.min(...points)
  const stepX = w / (points.length - 1)
  let path = ''
  points.forEach((p, i) => {
    const x = i * stepX
    const y = h - ((p - min) / (max - min || 1)) * h
    path += (i === 0 ? `M${x},${y}` : ` L${x},${y}`)
  })
  svg.innerHTML = `<path d="${path}" stroke="#FF6B35" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`
}

const drawBarChart = (svgEl, items) => {
  if (!svgEl) return
  const svg = svgEl
  const w = svg.getAttribute('width')
  const h = svg.getAttribute('height')
  const max = Math.max(...items.map(i=>i.value),1)
  const barW = w / items.length - 10
  let inner = ''
  items.forEach((it, idx) => {
    const x = idx * (barW + 10) + 10
    const barH = (it.value / max) * (h - 30)
    const y = h - barH
    inner += `<rect x="${x}" y="${y}" width="${barW}" height="${barH}" fill="#FFD54F"></rect>`
    inner += `<text x="${x+barW/2}" y="${h-4}" font-size="12" text-anchor="middle" fill="#333">${it.label}</text>`
  })
  svg.innerHTML = inner
}

const loadAnalytics = async () => {
  try {
    const kidsResp = await analytics.kidsStats()
    const kids = kidsResp.data || []
    // pick first kid for demo
    if (kids.length === 0) return
    const kidId = kids[0].id
    const diligenceResp = await analytics.diligence(kidId)
    const wealthResp = await analytics.wealth(kidId)
    const rateResp = await analytics.completionRate(kidId)
    const diligencePoints = (diligenceResp.data || []).map(p => p.count || 0)
    const wealthItems = (wealthResp.data && wealthResp.data.buckets) ? wealthResp.data.buckets.map(b => ({ label: b.label, value: b.value })) : []
    drawLineChart(diligenceSvg.value, diligencePoints.length ? diligencePoints : [0,0,0,0,0,0,0])
    drawBarChart(wealthSvg.value, wealthItems.length ? wealthItems : [{label:'A',value:1}])
    completionRate.value = rateResp.data?.percentage || 0
    completionRateDisplay.value = `${Math.round(completionRate.value*100)}%`
  } catch(e) {
    console.error(e)
    showToast('加载报表失败')
  }
}

onMounted(() => loadAnalytics())
</script>

<style scoped>
.analytics-page { padding:20px; }
.charts { display:flex; flex-direction:column; gap:20px; }
.chart-card { background:rgba(255,255,255,0.95); padding:12px; border-radius:8px; }
.metric { font-size:28px; font-weight:700; color:#333; margin-top:10px; }
</style>


