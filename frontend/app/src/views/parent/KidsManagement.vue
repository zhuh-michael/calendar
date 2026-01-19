<template>
  <div class="kids-management">
    <h2>小朋友管理</h2>
    <div class="kids-list">
      <div v-for="kid in kids" :key="kid.id" class="kid-item">
        <div class="kid-info">
          <div class="kid-name">{{ kid.nickname || kid.username }}</div>
          <div class="kid-balance"><van-icon name="star" color="#FFD700" /> {{ kid.starBalance }}</div>
        </div>
        <div class="kid-actions">
          <van-button size="small" type="primary" @click="openAdjustDialog(kid)">调整星星</van-button>
          <van-button size="small" @click="exportTransactions(kid)">导出流水</van-button>
        </div>
      </div>
    </div>
    <div style="margin-top:12px;">
      <van-button type="primary" @click="openCreateKid">创建小朋友</van-button>
    </div>
    <ParentNav />

    <van-dialog v-model:show="showAdjust" title="人工调整星星">
      <div class="adjust-form">
        <van-field v-model="adjustAmount" type="number" label="数量" placeholder="正数加星，负数减星" />
        <van-field v-model="adjustNote" label="备注" placeholder="请输入备注" />
      </div>
      <template #footer>
        <van-button plain @click="showAdjust=false">取消</van-button>
        <van-button type="primary" @click="submitAdjust">确认</van-button>
      </template>
    </van-dialog>
  
  <van-dialog v-model:show="showCreate" title="创建小朋友账号">
    <van-field v-model="newKid.username" label="用户名" placeholder="username" />
    <van-field v-model="newKid.password" label="密码" placeholder="password" />
    <van-field v-model="newKid.nickname" label="昵称" placeholder="nickname" />
    <template #footer>
      <van-button plain @click="showCreate=false">取消</van-button>
      <van-button type="primary" @click="submitCreateKid">创建</van-button>
    </template>
  </van-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { parents, auth } from '@/utils/api.js'
import { showToast } from 'vant'

const kids = ref([])
const showAdjust = ref(false)
const currentKid = ref(null)
const adjustAmount = ref(0)
const adjustNote = ref('')

const loadKids = async () => {
  try {
    const resp = await auth.getKids()
    kids.value = resp.data || []
  } catch (e) {
    console.error(e)
    showToast('加载孩子列表失败')
  }
}

const openAdjustDialog = (kid) => {
  currentKid.value = kid
  adjustAmount.value = 0
  adjustNote.value = ''
  showAdjust.value = true
}

// Create/edit kid account
const showCreate = ref(false)
const newKid = ref({ username:'', password:'', nickname:'' })

const openCreateKid = () => {
  newKid.value = { username:'', password:'', nickname:'' }
  showCreate.value = true
}

const submitCreateKid = async () => {
  try {
    await parents.createKid(newKid.value)
    showToast('创建成功')
    showCreate.value = false
    loadKids()
  } catch (e) {
    console.error(e)
    showToast('创建失败')
  }
}

// Export transactions CSV
const exportTransactions = async (kid) => {
  try {
    const resp = await fetch(`${location.origin.replace(/:\\d+$/,'')}/api/transactions/user/${kid.id}`)
    const data = await resp.json()
    // convert to CSV
    const rows = [['id','amount','reason','createTime']]
    data.forEach(r => rows.push([r.id, r.amount, `"${r.reason}"`, r.createTime]))
    const csv = rows.map(r => r.join(',')).join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `transactions_kid_${kid.id}.csv`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
    showToast('导出失败')
  }
}

const submitAdjust = async () => {
  if (!currentKid.value) return
  try {
    await parents.adjustKid(currentKid.value.id, Number(adjustAmount.value), adjustNote.value)
    showToast('调整成功')
    showAdjust.value = false
    loadKids()
  } catch (e) {
    console.error(e)
    showToast('调整失败')
  }
}

onMounted(() => loadKids())
</script>

<style scoped>
.kids-management { padding: 20px; }
.kid-item { display:flex; justify-content:space-between; align-items:center; padding:12px 8px; background:rgba(255,255,255,0.95); border-radius:8px; margin-bottom:8px; }
.kid-info { display:flex; gap:12px; align-items:center;}
.kid-name { font-weight:600; }
.kid-balance { color:#FF9800; }
.kid-actions {}
</style>


