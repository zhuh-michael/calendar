<template>
  <div class="task-management">
    <ParentNav />
    <div class="controls">
      <van-field v-model="selectedKidId" label="孩子" is-link placeholder="选择孩子" @click="showKidPicker = true" readonly />
      <van-field v-model="selectedDate" type="date" label="日期" placeholder="选择日期" />
      <van-button type="primary" @click="loadTasks">加载任务</van-button>
      <van-button type="success" @click="openCreate">新建任务</van-button>
    </div>

    <div v-if="tasksList.length === 0" class="empty">没有任务</div>
    <div v-else class="task-list">
      <div v-for="task in tasksList" :key="task.id" class="task-item">
        <div class="main">
          <div class="title">{{ task.title }}</div>
          <div class="meta">时间: {{ formatTime(task.startTime) }} · 星星: {{ task.rewardStars }} · 审核: {{ (task.status === 'PENDING' || task.status === 1 || task.status === '1') ? '是' : '否' }}</div>
          <div class="desc">{{ task.description }}</div>
        </div>
        <div class="actions">
          <van-button size="small" @click="openEdit(task)">编辑</van-button>
          <van-button size="small" type="danger" @click="deleteTask(task.id)">删除</van-button>
          <van-button v-if="task.status === 'PENDING' || task.status === 1 || task.status === '1'" size="small" type="primary" @click="approve(task.id)">通过</van-button>
          <van-button v-if="task.status === 'PENDING' || task.status === 1 || task.status === '1'" size="small" type="warning" @click="reject(task.id)">拒绝</van-button>
        </div>
      </div>
    </div>

    <!-- kid picker -->
    <van-popup v-model:show="showKidPicker" position="bottom">
      <van-list>
        <van-cell v-for="kid in kids" :key="kid.id" :title="kid.nickname || kid.username" @click="selectKid(kid)" />
      </van-list>
    </van-popup>

    <!-- create/edit dialog -->
    <van-dialog v-model:show="showDialog" title="任务">
      <van-field v-model="form.title" label="标题" placeholder="任务标题" />
      <van-field v-model="form.startTime" label="时间" placeholder="YYYY-MM-DDTHH:mm" />
      <div class="field-row reward-row">
        <van-field class="reward-field" v-model.number="form.rewardStars" type="number" label="星星" placeholder="奖励星星" />
        <div class="switch-wrapper">
          <van-switch v-model="form.needsReview" active-color="#ff6b35" />
          <div class="switch-label">需要审核</div>
        </div>
      </div>
      <van-field v-model="form.description" label="描述" placeholder="任务描述" />
      <template #footer>
        <van-button plain @click="showDialog=false">取消</van-button>
        <van-button type="primary" @click="submitForm">保存</van-button>
      </template>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { tasks, auth, parents } from '@/utils/api.js'
import { showToast } from 'vant'
import ParentNav from '@/components/ParentNav.vue'

const kids = ref([])
const selectedKidId = ref(null)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const tasksList = ref([])
const tasksCount = ref(0)
const tasksLoaded = ref(false)
const showKidPicker = ref(false)
const showDialog = ref(false)
const form = ref({
  id: null,
  title: '',
  startTime: '',
  rewardStars: 0,
  needsReview: false,
  description: '',
  kidId: null
})

const loadKids = async () => {
  try {
    const resp = await auth.getKids()
    kids.value = resp.data || []
  } catch (e) {
    console.error(e)
    showToast('加载孩子失败')
  }
}

const loadTasks = async () => {
  if (!selectedKidId.value) { showToast('请选择孩子'); return }
  try {
    const resp = await tasks.getByKidAndDate(selectedKidId.value, selectedDate.value)
    tasksList.value = resp.data || []
    tasksLoaded.value = true
  } catch (e) {
    console.error(e)
    showToast('加载任务失败')
  }
}

const selectKid = (kid) => {
  selectedKidId.value = kid.id
  showKidPicker.value = false
}

const openCreate = () => {
  form.value = { id: null, title:'', startTime: selectedDate.value + 'T08:00', rewardStars: 0, needsReview: false, description:'', kidId: selectedKidId.value }
  showDialog.value = true
}

const openEdit = (task) => {
  form.value = { ...task }
  showDialog.value = true
}

const submitForm = async () => {
  try {
    if (!form.value.kidId && !selectedKidId.value) { showToast('请选择孩子'); return }
    form.value.kidId = form.value.kidId || selectedKidId.value
    if (form.value.id) {
      await tasks.update(form.value.id, form.value)
      showToast('更新成功')
    } else {
      await tasks.create(form.value)
      showToast('创建成功')
    }
    showDialog.value = false
    loadTasks()
  } catch (e) {
    console.error(e)
    showToast('保存失败')
  }
}

const deleteTask = async (id) => {
  try {
    await tasks.delete(id)
    showToast('删除成功')
    loadTasks()
  } catch (e) {
    console.error(e)
    showToast('删除失败')
  }
}

const approve = async (id) => {
  try {
    await tasks.approve(id)
    showToast('已通过')
    loadTasks()
  } catch (e) {
    console.error(e)
    showToast('操作失败')
  }
}

const reject = async (id) => {
  try {
    await tasks.reject(id)
    showToast('已拒绝')
    loadTasks()
  } catch (e) {
    console.error(e)
    showToast('操作失败')
  }
}

const formatTime = (iso) => {
  return iso ? new Date(iso).toLocaleString() : ''
}

onMounted(() => {
  loadKids()
})
</script>

<style scoped>
.task-management { padding:20px; }
.controls { display:flex; gap:12px; align-items:center; margin-bottom:12px; }
.task-list { display:flex; flex-direction:column; gap:10px; }
.task-item { display:flex; justify-content:space-between; background:rgba(255,255,255,0.95); padding:12px; border-radius:8px; }
.title { font-weight:600; }
.meta { color:#777; font-size:12px; margin-top:6px; }
.desc { color:#555; margin-top:8px; }
.empty { text-align:center; color:#999; padding:36px 0; font-size:16px; }
</style>
