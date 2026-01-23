<template>
  <div class="task-management">
    <ParentNav />
    <div class="controls">
      <van-field v-model="selectedKidNickname" label="小朋友" is-link placeholder="选择小朋友" @click="showKidPicker = true" readonly />
      <van-field v-model="selectedDate" type="date" label="日期" placeholder="选择日期" :clearable="true" @clear="clearDate" />
      <van-field v-model="selectedStatusLabel" label="状态" is-link placeholder="选择状态" @click="showStatusPicker = true" readonly />
      <van-button type="primary" @click="loadTasks">加载任务</van-button>
      <van-button type="success" @click="openCreate">新建任务</van-button>
    </div>

    <div class="list-container">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
      <div v-if="tasksList.length === 0 && !loading && tasksLoaded" class="empty">没有任务</div>
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
    </van-list>
    </div>

    <!-- kid picker -->
    <van-popup v-model:show="showKidPicker" position="bottom">
      <van-list>
        <van-cell title="清空选择" @click="clearKidSelection" />
        <van-cell v-for="kid in kids" :key="kid.id" :title="`${kid.nickname}（${kid.username}）`" @click="selectKid(kid)" />
      </van-list>
    </van-popup>
    <van-popup v-model:show="showStatusPicker" position="bottom">
      <van-list>
        <van-cell v-for="opt in statusOptions" :key="opt.value" :title="opt.label" @click="selectStatus(opt)" />
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
    <van-dialog v-model:show="showEvidenceDialog" title="任务完成结果" width="70%" :style="{ minHeight: '400px', maxWidth: '800px' }" close-on-click-overlay :show-cancel-button="false" :show-confirm-button="false">
      <div class="evidence-section">
        <div class="task-header">
          <h3>{{ currentTaskForEvidence?.title }}</h3>
          <p>{{ currentTaskForEvidence?.description }}</p>
        </div>

        <div v-if="taskEvidence.length === 0" class="no-evidence">
          <van-icon name="photo" size="40" color="#ccc" />
          <p>暂无结果图片</p>
        </div>

        <div v-else class="preview-section">
          <div class="preview-grid">
            <div
              v-for="evidence in taskEvidence"
              :key="evidence.id"
              class="preview-item"
            >
              <img
                :src="`${apiBaseUrl}/${evidence.imagePath}`"
                class="evidence-preview"
                @click="previewImage(taskEvidence.map(e => `${apiBaseUrl}/${e.imagePath}`), taskEvidence.findIndex(e => e.id === evidence.id))"
              />
              <div class="evidence-info">
                <span class="upload-time">{{ formatTime(evidence.uploadTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
      </template>
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
const selectedDate = ref('') // optional

// 计算属性：获取当前选中小朋友的昵称
const selectedKidNickname = computed(() => {
  if (!selectedKidId.value || !kids.value.length) return ''
  const kid = kids.value.find(k => k.id === selectedKidId.value)
  return kid ? (kid.nickname || kid.username) : ''
})
const tasksList = ref([])
const tasksCount = ref(0)
const tasksLoaded = ref(false)
const loading = ref(false)
const finished = ref(false)
const currentPage = ref(0)
const pageSize = ref(20)
const showKidPicker = ref(false)
const showDialog = ref(false)
const showEvidenceDialog = ref(false)
const showRejectDialog = ref(false)
const showStatusPicker = ref(false)

const statusOptions = [
  { value: 'TODO', label: '待办' },
  { value: 'PENDING', label: '待审核' },
  { value: 'DONE', label: '已完成' }
]
// default to PENDING
const selectedStatus = ref('PENDING')
const selectedStatusLabel = ref('待审核')

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
    // 默认不选择任何小朋友，让用户主动选择
  } catch (e) {
    console.error(e)
    showToast('加载小朋友失败')
  }
}

const loadTasks = async (reset = true) => {
  if (reset) {
    currentPage.value = 0
    finished.value = false
    tasksList.value = []
    tasksLoaded.value = false
  }

  try {
    const page = currentPage.value
    const size = pageSize.value

    // 使用统一的查询API，传递所有筛选条件
    const params = {
      page,
      size,
      kidId: selectedKidId.value || undefined,
      status: selectedStatus.value,
      date: selectedDate.value || undefined
    }

    // 如果按日期查询，不分页，直接加载所有数据
    if (selectedDate.value && selectedKidId.value) {
      // 特殊处理：按小朋友和日期查询时，加载所有数据
      const resp = await tasks.getByKidAndDate(selectedKidId.value, selectedDate.value)
      let data = resp.data || []
      // 前端筛选状态
      data = data.filter(t => String(t.status) === selectedStatus.value || (typeof t.status === 'number' && String(t.status) === selectedStatus.value))
      tasksList.value = data
      finished.value = true // 日期查询不分页，直接标记完成
      tasksLoaded.value = true
      return
    }

    // 普通分页查询
    const resp = await tasks.query(params)
    const data = resp.data || []
    tasksList.value = reset ? data : [...tasksList.value, ...data]

    tasksCount.value = tasksList.value.length
    tasksLoaded.value = true

    // Check if we have more data
    if (data.length < size) {
      finished.value = true
    }
  } catch (e) {
    console.error(e)
    showToast('加载任务失败')
    // 只有在已经加载了一些数据的情况下，才停止加载
    if (tasksList.value.length > 0) {
      finished.value = true // Stop loading on error
    }
  }
}

const onLoad = async () => {
  currentPage.value++
  await loadTasks(false)
}

const selectKid = (kid) => {
  selectedKidId.value = kid.id
  showKidPicker.value = false
  // 自动加载选中小朋友的任务
  loadTasks()
}

const clearKidSelection = () => {
  selectedKidId.value = null
  showKidPicker.value = false
  // 重新加载任务（显示所有小朋友的任务）
  loadTasks()
}

const clearDate = () => {
  selectedDate.value = ''
  // 重新加载任务（不按日期过滤）
  loadTasks()
}

const selectStatus = (opt) => {
  selectedStatus.value = opt.value
  selectedStatusLabel.value = opt.label
  showStatusPicker.value = false
  // auto reload
  loadTasks()
}

const openCreate = () => {
  const defaultTime = selectedDate.value ? selectedDate.value + 'T08:00' : new Date().toISOString().split('T')[0] + 'T08:00'
  form.value = { id: null, title:'', startTime: defaultTime, rewardStars: 0, needsReview: true, description:'', kidId: selectedKidId.value }
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
.list-container { height: 60vh; overflow: auto; }
.controls { display:flex; gap:12px; align-items:center; margin-bottom:12px; }
.task-list { display:flex; flex-direction:column; gap:10px; }
.task-item { display:flex; justify-content:space-between; background:rgba(255,255,255,0.95); padding:12px; border-radius:8px; }
.title { font-weight:600; }
.meta { color:#777; font-size:12px; margin-top:6px; }
.desc { color:#555; margin-top:8px; }
.empty { text-align:center; color:#999; padding:36px 0; font-size:16px; }

/* 结果预览样式 */
.evidence-view-dialog {
  padding: 20px 0;
}

.task-info {
  text-align: left;
  margin-bottom: 20px;
}

.task-info h3 {
  color: #333;
  margin-bottom: 8px;
}

.task-info p {
  color: #666;
  font-size: 14px;
}

.reject-reason {
  background: #ffebee;
  border: 1px solid #ffcdd2;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #c62828;
  font-size: 14px;
}

.no-evidence {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.preview-section {
  text-align: center;
  padding: 20px;
}

.evidence-preview {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 15px;
}

.preview-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.preview-grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}
.preview-item {
  width: 140px;
}
.preview-item .evidence-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
