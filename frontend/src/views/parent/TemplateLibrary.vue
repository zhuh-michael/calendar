<template>
  <div class="template-library">
    <div class="header">
      <h2>任务模板库</h2>
    </div>
    <ParentNav />

    <div class="controls">
      <van-field v-model="selectedKidId" label="孩子" is-link placeholder="选择孩子" @click="showKidPicker = true" readonly />
      <van-field v-model="startDate" type="date" label="起始日期" placeholder="开始日期" />
      <van-field v-model="endDate" type="date" label="结束日期" placeholder="结束日期" />
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="templates.length === 0" class="empty">暂无模板</div>
      <div class="template-list">
        <div v-for="t in templates" :key="t.id" class="template-card">
          <div class="title">{{ t.title }}</div>
          <div class="meta">奖励: {{ t.rewardStars }} · 每日: {{ t.startTime ? (new Date(t.startTime).toLocaleTimeString()) : '-' }}</div>
          <div class="desc">{{ t.description }}</div>
          <div class="actions">
            <van-button size="small" type="primary" @click="importTemplate(t.id)">一键导入</van-button>
          </div>
        </div>
      </div>
    </div>

    <van-popup v-model:show="showKidPicker" position="bottom">
      <van-list>
        <van-cell v-for="kid in kids" :key="kid.id" :title="kid.nickname || kid.username" @click="selectKid(kid)" />
      </van-list>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { tasks, auth } from '@/utils/api.js'
import { showToast } from 'vant'

const templates = ref([])
const kids = ref([])
const loading = ref(false)
const showKidPicker = ref(false)
const selectedKidId = ref(null)
const startDate = ref(new Date().toISOString().split('T')[0])
const endDate = ref(new Date().toISOString().split('T')[0])

const loadTemplates = async () => {
  loading.value = true
  try {
    const resp = await tasks.templates()
    templates.value = resp.data || []
  } catch (e) {
    console.error(e)
    showToast('加载模板失败')
  } finally {
    loading.value = false
  }
}

const loadKids = async () => {
  try {
    const r = await auth.getKids()
    kids.value = r.data || []
  } catch (e) {}
}

const selectKid = (kid) => {
  selectedKidId.value = kid.id
  showKidPicker.value = false
}

const importTemplate = async (templateId) => {
  if (!selectedKidId.value) { showToast('请选择孩子'); return }
  try {
    await tasks.fromTemplate(templateId, selectedKidId.value, startDate.value, endDate.value)
    showToast('导入成功')
  } catch (e) {
    console.error(e)
    showToast('导入失败')
  }
}

onMounted(() => {
  loadTemplates()
  loadKids()
})
</script>

<style scoped>
.template-library { padding:20px; }
.controls { display:flex; gap:12px; align-items:center; margin-bottom:12px; }
.template-list { display:grid; grid-template-columns: repeat(auto-fill,minmax(260px,1fr)); gap:12px; }
.template-card { background:rgba(255,255,255,0.95); padding:12px; border-radius:8px; }
.title { font-weight:700; }
.meta { color:#777; font-size:13px; margin-top:6px; }
.desc { margin-top:8px; color:#555; }
.actions { margin-top:10px; }
.empty { text-align:center; color:#999; padding:20px 0; }
</style>


