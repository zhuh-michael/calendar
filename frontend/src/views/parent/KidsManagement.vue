<template>
  <div class="kids-management">
    <h2>小朋友管理</h2>
    <div class="kids-list">
      <div v-for="kid in kids" :key="kid.id" class="kid-item">
        <div class="kid-info">
          <div class="kid-avatar" v-if="kid.avatar">
            <img :src="`${apiBaseUrl}/${kid.avatar}`" style="width:48px;height:48px;border-radius:8px;object-fit:cover" />
          </div>
          <div class="kid-name">{{ kid.nickname || kid.username }}</div>
          <div class="kid-balance"><van-icon name="star" color="#FFD700" /> {{ kid.starBalance }}</div>
        </div>
        <div class="kid-actions">
          <van-button size="small" type="primary" @click="openAdjustDialog(kid)">调整星星</van-button>
          <van-button size="small" type="info" @click="openEditDialog(kid)">编辑</van-button>
          <van-button size="small" type="danger" @click="deleteKid(kid)">删除</van-button>
          <van-button size="small" @click="exportTransactions(kid)">导出流水</van-button>
        </div>
      </div>
    </div>
    <div style="margin-top:12px;">
      <van-button type="primary" @click="openCreateKid">创建小朋友</van-button>
    </div>
    <ParentNav />

    <van-dialog v-model:show="showAdjust" title="人工调整星星" :style="{ minWidth: '400px' }" close-on-click-overlay>
      <div class="adjust-form">
        <van-field v-model="adjustAmount" type="number" label="数量" placeholder="正数加星，负数减星" />
        <van-field v-model="adjustNote" label="备注" placeholder="请输入备注" />
      </div>
      <template #footer>
        <div style="display: flex; justify-content: center; gap: 10px; padding-bottom: 16px;">
        <van-button plain @click="showAdjust=false">取消</van-button>
        <van-button type="primary" @click="submitAdjust">确认</van-button>
        </div>
      </template>
    </van-dialog>
  
  <van-dialog v-model:show="showCreate" title="创建小朋友账号" :style="{ minWidth: '400px' }" close-on-click-overlay>
    <van-field v-model="newKid.username" label="用户名" placeholder="username" />
    <van-field v-model="newKid.password" label="密码" placeholder="password" />
    <van-field v-model="newKid.nickname" label="昵称" placeholder="nickname" />
    <div class="image-upload-section" style="padding:12px 0;">
      <div class="upload-label">头像 <span class="upload-tip">（推荐尺寸：正方形或宽图，如 50×50）</span></div>
      <div class="image-preview" v-if="(uploaderModelCreate && uploaderModelCreate.length) || newKidAvatarPreview">
        <van-image :src="newKidAvatarPreview || (uploaderModelCreate[0] && uploaderModelCreate[0].url)" width="80" height="80" fit="cover" radius="8" />
        <van-button size="small" type="default" @click.stop="removeCreateImage" class="remove-btn">移除</van-button>
      </div>
      <van-uploader
        v-else
        v-model="uploaderModelCreate"
        :after-read="handleCreateImageRead"
        accept="image/*"
        :max-count="1"
        @delete="handleCreateImageDelete"
      >
        <div class="upload-placeholder">
          <van-icon name="plus" size="32" color="#999" />
          <div style="font-size:12px;color:#999;margin-top:4px;">上传图片</div>
        </div>
      </van-uploader>
    </div>
    <template #footer>
      <div style="display: flex; justify-content: center; gap: 10px; padding-bottom: 16px;">
      <van-button plain @click="showCreate=false">取消</van-button>
      <van-button type="primary" @click="submitCreateKid">创建</van-button>
      </div>
    </template>
  </van-dialog>

  <van-dialog v-model:show="showEdit" title="编辑小朋友账号" :style="{ minWidth: '400px' }" close-on-click-overlay>
    <van-field v-model="editKid.username" label="用户名" readonly />
    <van-field v-model="editKid.password" label="新密码" placeholder="留空则不修改" />
    <van-field v-model="editKid.nickname" label="昵称" placeholder="nickname" />
    <van-field v-model.number="editKid.starBalance" type="number" label="星星余额" placeholder="star balance" />
    <div class="image-upload-section" style="padding:12px 0;">
      <div class="upload-label">头像 <span class="upload-tip">（推荐尺寸：正方形或宽图，如 50×50）</span></div>
      <div class="image-preview" v-if="((uploaderModelEdit && uploaderModelEdit.length) || editKidAvatarPreview || currentKid?.avatar) && !editRemoveExisting">
        <van-image :src="editKidAvatarPreview || (uploaderModelEdit[0] && uploaderModelEdit[0].url) || currentKid.avatar" width="80" height="80" fit="cover" radius="8" />
        <van-button size="small" type="default" @click.stop="removeEditImage" class="remove-btn">移除</van-button>
      </div>
      <van-uploader
        v-else
        v-model="uploaderModelEdit"
        :after-read="handleEditImageRead"
        accept="image/*"
        :max-count="1"
        @delete="handleEditImageDelete"
      >
        <div class="upload-placeholder">
          <van-icon name="plus" size="32" color="#999" />
          <div style="font-size:12px;color:#999;margin-top:4px;">上传图片</div>
        </div>
      </van-uploader>
    </div>
    <template #footer>
      <div style="display: flex; justify-content: center; gap: 10px; padding-bottom: 16px;">
        <van-button plain @click="showEdit=false">取消</van-button>
        <van-button type="primary" @click="submitEditKid">保存</van-button>
      </div>
    </template>
  </van-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { parents, auth } from '@/utils/api.js'
import { showToast } from 'vant'
import { getApiBaseUrl } from '@/utils/config.js'

const kids = ref([])
const showAdjust = ref(false)
const showEdit = ref(false)
const currentKid = ref(null)
const editKid = ref({ username: '', password: '', nickname: '', starBalance: 0 })
const adjustAmount = ref(0)
const adjustNote = ref('')

// API基础URL
const apiBaseUrl = getApiBaseUrl()

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
const newKidAvatarFile = ref(null)
const newKidAvatarPreview = ref('')
const uploaderModelCreate = ref([])

const openCreateKid = () => {
  newKid.value = { username:'', password:'', nickname:'' }
  showCreate.value = true
}

const submitCreateKid = async () => {
  try {
    // construct FormData to include avatar if present
    // always submit as multipart/form-data to the unified endpoint
    const payload = new FormData()
    payload.append('username', newKid.value.username)
    payload.append('password', newKid.value.password)
    if (newKid.value.nickname) payload.append('nickname', newKid.value.nickname)
    if (newKidAvatarFile.value) payload.append('imageFile', newKidAvatarFile.value)
    else payload.append('keepExistingImage', 'true')
    await parents.createKid(payload)
    showToast('创建成功')
    showCreate.value = false
    loadKids()
  } catch (e) {
    console.error(e)
    showToast('创建失败')
  }
}

// Edit kid account
const openEditDialog = (kid) => {
  editKid.value = {
    username: kid.username,
    password: '',
    nickname: kid.nickname || '',
    starBalance: kid.starBalance
  }
  currentKid.value = kid
  showEdit.value = true
  editKidAvatarPreview.value = ''
  editKidAvatarFile.value = null
  editRemoveExisting.value = false
}

const submitEditKid = async () => {
  try {
    const updateData = {
      nickname: editKid.value.nickname,
      starBalance: editKid.value.starBalance
    }
    if (editKid.value.password && editKid.value.password.trim()) {
      updateData.password = editKid.value.password
    }
    // always send multipart/form-data
    const fd = new FormData()
    if (updateData.nickname !== undefined) fd.append('nickname', updateData.nickname)
    if (updateData.password) fd.append('password', updateData.password)
    if (updateData.starBalance !== undefined) fd.append('starBalance', updateData.starBalance)
    // handle image / removal flags
    if (editKidAvatarFile.value) {
      fd.append('imageFile', editKidAvatarFile.value)
    } else if (editRemoveExisting.value) {
      fd.append('removeExistingImage', 'true')
    } else {
      fd.append('keepExistingImage', 'true')
    }
    await parents.updateKid(currentKid.value.id, fd)
    showToast('修改成功')
    showEdit.value = false
    loadKids()
  } catch (e) {
    console.error(e)
    showToast('修改失败')
  }
}

// avatar handlers
const onCreateAvatarChange = (e) => {
  const f = e.target.files && e.target.files[0]
  if (!f) return
  newKidAvatarFile.value = f
  const reader = new FileReader()
  reader.onload = (ev) => { newKidAvatarPreview.value = ev.target.result }
  reader.readAsDataURL(f)
}

const editKidAvatarFile = ref(null)
const editKidAvatarPreview = ref('')
const uploaderModelEdit = ref([])
const onEditAvatarChange = (e) => {
  const f = e.target.files && e.target.files[0]
  if (!f) return
  editKidAvatarFile.value = f
  const reader = new FileReader()
  reader.onload = (ev) => { editKidAvatarPreview.value = ev.target.result }
  reader.readAsDataURL(f)
}
const editRemoveExisting = ref(false)

// uploader handlers for create
const handleCreateImageRead = (file) => {
  // create an object URL for preview and keep file for upload
  const objUrl = URL.createObjectURL(file.file)
  newKidAvatarFile.value = file.file
  newKidAvatarPreview.value = objUrl
  uploaderModelCreate.value = [{ url: objUrl, isImage: true }]
}
const handleCreateImageDelete = () => {
  console.log('[KidsManagement] handleCreateImageDelete called')
  if (newKidAvatarPreview.value) {
    try { URL.revokeObjectURL(newKidAvatarPreview.value) } catch (e) {}
  }
  newKidAvatarFile.value = null
  newKidAvatarPreview.value = ''
  uploaderModelCreate.value = []
}
const removeCreateImage = () => {
  console.log('[KidsManagement] removeCreateImage called')
  if (newKidAvatarPreview.value) {
    try { URL.revokeObjectURL(newKidAvatarPreview.value) } catch (e) {}
  }
  newKidAvatarFile.value = null
  newKidAvatarPreview.value = ''
  uploaderModelCreate.value = []
  // also clear any native file inputs inside the create dialog to reset control
  try {
    const dlg = document.querySelector('.kids-management .van-dialog[style*="display: block"]')
    if (dlg) {
      const input = dlg.querySelector('input[type="file"]')
      if (input) input.value = ''
    }
  } catch (e) {}
}

// uploader handlers for edit
const handleEditImageRead = (file) => {
  const objUrl = URL.createObjectURL(file.file)
  editKidAvatarFile.value = file.file
  editKidAvatarPreview.value = objUrl
  uploaderModelEdit.value = [{ url: objUrl, isImage: true }]
}
const handleEditImageDelete = () => {
  console.log('[KidsManagement] handleEditImageDelete called')
  if (editKidAvatarPreview.value) {
    try { URL.revokeObjectURL(editKidAvatarPreview.value) } catch (e) {}
  }
  editKidAvatarFile.value = null
  editKidAvatarPreview.value = ''
  uploaderModelEdit.value = []
}
const removeEditImage = () => {
  console.log('[KidsManagement] removeEditImage called')
  if (editKidAvatarPreview.value) {
    try { URL.revokeObjectURL(editKidAvatarPreview.value) } catch (e) {}
  }
  editKidAvatarFile.value = null
  editKidAvatarPreview.value = ''
  uploaderModelEdit.value = []
  // clear native file inputs in edit dialog if present
  try {
    const dlg = document.querySelector('.kids-management .van-dialog[style*="display: block"]')
    if (dlg) {
      const input = dlg.querySelector('input[type="file"]')
      if (input) input.value = ''
    }
  } catch (e) {}
  // mark that existing avatar should be removed on submit
  editRemoveExisting.value = true
}

// Delete kid account
const deleteKid = async (kid) => {
  try {
    await parents.deleteKid(kid.id)
    showToast('删除成功')
    loadKids()
  } catch (e) {
    console.error(e)
    showToast('删除失败')
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

// helper to build avatar src safely (handles server relative paths and SSR)
const getAvatarSrc = (kid) => {
  try {
    const avatar = kid && kid.avatar
    if (!avatar) return '/default-avatar.svg'
    if (avatar.startsWith('http')) return avatar
    const origin = (typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : ''
    return origin + '/' + avatar
  } catch (e) {
    return '/default-avatar.svg'
  }
}
</script>

<style scoped>
.kids-management { padding: 20px; }
.kid-item { display:flex; justify-content:space-between; align-items:center; padding:12px 8px; background:rgba(255,255,255,0.95); border-radius:8px; margin-bottom:8px; }
.kid-info { display:flex; gap:12px; align-items:center;}
.kid-name { font-weight:600; }
.kid-balance { color:#FF9800; }

/* Ensure van-field labels are vertically centered inside dialogs/forms in this view */
 /* 图片上传样式 */
.image-upload-section { padding: 12px 16px; }
.upload-label { font-size: 14px; color: #64626a; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.upload-tip { font-size: 12px; color: #969799; font-weight: normal; }
.image-preview { display: flex; align-items: center; gap: 12px; }
.remove-btn { margin-left: auto; }
.upload-placeholder {
  width: 80px;
  height: 80px;
  border: 1px dashed #dcdee0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f7f8fa;
}
:deep(.van-uploader) { display: block; }
:deep(.van-uploader__wrapper) { display: block; }
:deep(.van-uploader__input-wrapper) { width: 100%; }
</style>


