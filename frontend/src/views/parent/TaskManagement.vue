<template>
  <div class="task-management">
    <ParentNav />
    <div class="controls">
      <van-field v-model="selectedKidNickname" label="小朋友" is-link placeholder="选择小朋友" @click="showKidPicker = true" readonly />
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
          <van-button size="small" @click="viewEvidence(task)">查看照片</van-button>
          <van-button v-if="task.status === 'PENDING' || task.status === 1 || task.status === '1'" size="small" type="primary" @click="approve(task.id)">通过</van-button>
          <van-button v-if="task.status === 'PENDING' || task.status === 1 || task.status === '1'" size="small" type="warning" @click="openRejectDialog(task)">拒绝</van-button>
        </div>
      </div>
    </div>

    <!-- kid picker -->
    <van-popup v-model:show="showKidPicker" position="bottom">
      <van-list>
        <van-cell v-for="kid in kids" :key="kid.id" :title="`${kid.nickname}（${kid.username}）`" @click="selectKid(kid)" />
      </van-list>
    </van-popup>

    <!-- create/edit dialog -->
    <van-dialog v-model:show="showDialog" title="任务" :style="{ minWidth: '400px' }" close-on-click-overlay>
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
        <div style="display: flex; justify-content: center; gap: 10px; padding-bottom: 16px;">
        <van-button plain @click="showDialog=false">取消</van-button>
        <van-button type="primary" @click="submitForm">保存</van-button>
        </div>
      </template>
    </van-dialog>

    <!-- 结果预览对话框 -->
    <van-dialog v-model:show="showEvidenceDialog" title="任务完成结果" width="70%" :style="{ minHeight: '400px', maxWidth: '800px' }" close-on-click-overlay>
      <div class="evidence-preview">
        <div class="task-header">
          <h3>{{ currentTaskForEvidence?.title }}</h3>
          <p>{{ currentTaskForEvidence?.description }}</p>
        </div>

        <div v-if="taskEvidence.length === 0" class="no-evidence">
          <van-icon name="photo" size="40" color="#ccc" />
          <p>暂无结果图片</p>
        </div>

        <div v-else class="evidence-list">
          <div
            v-for="evidence in taskEvidence"
            :key="evidence.id"
            class="evidence-item"
          >
            <img :src="`${apiBaseUrl}/${evidence.imagePath}`" class="evidence-image" @click="previewImage(taskEvidence.map(e => `${apiBaseUrl}/${e.imagePath}`), taskEvidence.findIndex(e => e.id === evidence.id))" />
            <div class="evidence-info">
              <span class="upload-time">{{ formatTime(evidence.uploadTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </van-dialog>

    <!-- 拒绝理由对话框 -->
    <van-dialog v-model:show="showRejectDialog" title="拒绝任务" :style="{ minWidth: '400px' }" close-on-click-overlay>
      <div class="reject-dialog">
        <div class="task-info">
          <h4>{{ currentTaskForReject?.title }}</h4>
          <p>{{ currentTaskForReject?.description }}</p>
        </div>

        <div class="quick-reasons">
          <p>快捷理由：</p>
          <div class="reason-buttons">
            <van-button
              v-for="reason in quickRejectReasons"
              :key="reason"
              size="small"
              type="info"
              @click="selectQuickReason(reason)"
              style="margin: 2px;"
            >
              {{ reason.length > 10 ? reason.substring(0, 10) + '...' : reason }}
            </van-button>
          </div>
        </div>

        <van-field
          v-model="rejectReason"
          type="textarea"
          label="拒绝理由"
          placeholder="请输入拒绝理由"
          :autosize="{ minHeight: 80 }"
          required
        />
      </div>

      <template #footer>
        <div style="display: flex; justify-content: center; gap: 10px; padding-bottom: 16px;">
          <van-button plain @click="showRejectDialog=false">取消</van-button>
          <van-button type="warning" @click="reject">确认拒绝</van-button>
        </div>
      </template>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { tasks, auth, parents } from '@/utils/api.js'
import { showToast, showImagePreview } from 'vant'
import ParentNav from '@/components/ParentNav.vue'
import { getApiBaseUrl } from '@/utils/config.js'

const kids = ref([])
const selectedKidId = ref(null)
const selectedDate = ref(new Date().toISOString().split('T')[0])

// 计算属性：获取当前选中小朋友的昵称
const selectedKidNickname = computed(() => {
  if (!selectedKidId.value || !kids.value.length) return ''
  const kid = kids.value.find(k => k.id === selectedKidId.value)
  return kid ? (kid.nickname || kid.username) : ''
})
const tasksList = ref([])
const tasksCount = ref(0)
const tasksLoaded = ref(false)
const showKidPicker = ref(false)
const showDialog = ref(false)
const showEvidenceDialog = ref(false)
const showRejectDialog = ref(false)

// API基础URL
const apiBaseUrl = getApiBaseUrl()
const form = ref({
  id: null,
  title: '',
  startTime: '',
  rewardStars: 0,
  needsReview: true,
  description: '',
  kidId: null
})

// 结果和拒绝相关
const currentTaskForEvidence = ref(null)
const taskEvidence = ref([])
const rejectReason = ref('')
const currentTaskForReject = ref(null)

const loadKids = async () => {
  try {
    const resp = await auth.getKids()
    kids.value = resp.data || []
    // 如果有小朋友列表且没有选中任何小朋友，默认选中第一个并加载任务
    if (kids.value.length > 0 && !selectedKidId.value) {
      selectKid(kids.value[0])
    }
  } catch (e) {
    console.error(e)
    showToast('加载小朋友失败')
  }
}

const loadTasks = async () => {
  if (!selectedKidId.value) { showToast('请选择小朋友'); return }
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
  // 自动加载选中小朋友的任务
  loadTasks()
}

const openCreate = () => {
  form.value = { id: null, title:'', startTime: selectedDate.value + 'T08:00', rewardStars: 0, needsReview: true, description:'', kidId: selectedKidId.value }
  showDialog.value = true
}

const openEdit = (task) => {
  form.value = { ...task }
  showDialog.value = true
}

const submitForm = async () => {
  try {
    if (!form.value.kidId && !selectedKidId.value) { showToast('请选择小朋友'); return }
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

const viewEvidence = async (task) => {
  currentTaskForEvidence.value = task
  try {
    const response = await tasks.getEvidence(task.id)
    taskEvidence.value = response.data || []
    showEvidenceDialog.value = true
  } catch (e) {
    console.error(e)
    showToast('加载结果失败')
  }
}

const previewImage = (images, startPosition = 0) => {
  showImagePreview({
    images,
    startPosition,
    closeable: true,
  })
}

const openRejectDialog = (task) => {
  currentTaskForReject.value = task
  rejectReason.value = ''
  showRejectDialog.value = true
}

const reject = async () => {
  if (!rejectReason.value.trim()) {
    showToast('请输入拒绝理由')
    return
  }

  try {
    await tasks.reject(currentTaskForReject.value.id, rejectReason.value)
    showToast('已拒绝')
    showRejectDialog.value = false
    currentTaskForReject.value = null
    rejectReason.value = ''
    loadTasks()
  } catch (e) {
    console.error(e)
    showToast('操作失败')
  }
}

// 快捷拒绝理由
const quickRejectReasons = [
  '作业完成质量不够，请认真检查后重新提交',
  '照片不清楚，请重新拍照上传',
  '作业内容不完整，请补全后重新提交',
  '书写不够工整，请认真书写',
  '其他问题：'
]

const selectQuickReason = (reason) => {
  if (reason === '其他问题：') {
    rejectReason.value = reason
  } else {
    rejectReason.value = reason
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

/* 结果预览样式 */
.evidence-preview {
  padding: 20px 0;
}

.task-header {
  text-align: center;
  margin-bottom: 20px;
}

.task-header h3 {
  color: #333;
  margin-bottom: 8px;
}

.task-header p {
  color: #666;
  font-size: 14px;
}

.no-evidence {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.evidence-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.evidence-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 10px;
}

.evidence-image {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.evidence-info {
  text-align: center;
}

.upload-time {
  color: #999;
  font-size: 12px;
}

/* 拒绝对话框样式 */
.reject-dialog {
  padding: 20px 0;
}

.task-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.task-info h4 {
  color: #333;
  margin-bottom: 8px;
}

.task-info p {
  color: #666;
  font-size: 14px;
}

.quick-reasons {
  margin-bottom: 15px;
}

.quick-reasons p {
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.reason-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}
</style>
