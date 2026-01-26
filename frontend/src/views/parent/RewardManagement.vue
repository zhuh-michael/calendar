<template>
  <div class="reward-management">
    <ParentNav />
    <div class="header">
      <h2>商品管理</h2>
      <van-button type="primary" @click="openCreate">新增商品</van-button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="rewards.length===0" class="empty">暂无商品</div>
      <div class="grid">
        <div v-for="r in rewards" :key="r.id" class="card">
          <van-image :src="`${apiBaseUrl}/${r.imageUrl}` || defaultImage" width="100%" height="120" fit="cover" />
          <div class="card-body">
            <div class="title">{{ r.name }}</div>
            <div class="desc">{{ r.description }}</div>
            <div class="meta">
              <span class="cost"><van-icon name="star" color="#FFD700" /> {{ r.cost }}</span>
              <span class="stock">库存: {{ r.stock }}</span>
            </div>
            <div class="actions">
              <van-button size="small" @click="openEdit(r)">编辑</van-button>
              <van-button size="small" type="danger" @click="remove(r.id)">删除</van-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <van-dialog v-model:show="showDialog" title="商品" :style="{ minWidth: '400px' }" close-on-click-overlay>
      <van-field v-model="form.name" label="名称" placeholder="商品名称" />
      <van-field v-model="form.description" label="描述" placeholder="商品描述" />
      <van-field v-model.number="form.cost" type="number" label="价格(星)" placeholder="例如 20" />
      <van-field v-model.number="form.stock" type="number" label="库存" placeholder="例如 10" />

      <!-- 图片上传 -->
      <div class="image-upload-section">
        <div class="upload-label">商品图片 <span class="upload-tip">（推荐尺寸：正方形或宽图，如 200×120）</span></div>
        <div class="image-preview" v-if="form.imageUrl && !previewImageFile">
          <van-image :src="`${apiBaseUrl}/${form.imageUrl}`" width="80" height="80" fit="cover" radius="8" />
          <van-button size="small" type="default" @click="removeImage" class="remove-btn">移除</van-button>
        </div>
        <van-uploader
          v-else
          v-model="uploaderModel"
          :after-read="handleImageRead"
          accept="image/*"
          :max-count="1"
          @delete="handleImageDelete"
        >
          <div class="upload-placeholder">
            <van-icon name="plus" size="32" color="#999" />
            <div style="font-size:12px;color:#999;margin-top:4px;">上传图片</div>
          </div>
        </van-uploader>
      </div>

      <van-select v-model="form.type" :options="typeOptions" placeholder="选择类型" />
      <div style="margin-top:12px;">
        <van-checkbox v-model="form.active">上架</van-checkbox>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: center; gap: 10px; padding-bottom: 16px;">
          <van-button plain @click="showDialog=false">取消</van-button>
          <van-button type="primary" @click="submit">保存</van-button>
        </div>
      </template>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { rewards as rewardsApi } from '@/utils/api.js'
import { showToast } from 'vant'
import { getApiBaseUrl } from '@/utils/config.js'

const rewards = ref([])
const loading = ref(false)
const defaultImage = '/default-avatar.svg'
const showDialog = ref(false)
const uploaderModel = ref([])
const previewImageFile = ref(null)
const form = ref({ id: null, name:'', description:'', cost:0, stock:0, imageUrl:'', type:'ITEM', active:true })
const typeOptions = [
  { text:'实物', value:'ITEM' },
  { text:'虚拟', value:'VIRTUAL' },
  { text:'盲盒券', value:'BLINDBOX' }
]

// API基础URL
const apiBaseUrl = getApiBaseUrl()

// 监听对话框关闭，清空表单
watch(showDialog, (val) => {
  if (!val) {
    resetForm()
  }
})

const resetForm = () => {
  form.value = { id:null, name:'', description:'', cost:0, stock:0, imageUrl:'', type:'ITEM', active:true }
  uploaderModel.value = []
  previewImageFile.value = null
}

const load = async () => {
  loading.value = true
  try {
    const resp = await rewardsApi.list()
    rewards.value = resp.data || []
  } catch (e) {
    console.error(e)
    showToast('加载商品失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  showDialog.value = true
}

const openEdit = async (r) => {
  form.value = { ...r }
  previewImageFile.value = null
  uploaderModel.value = []
  if (r.imageUrl) {
    // 设置预览图片显示
    uploaderModel.value = [{ url: r.imageUrl, isImage: true }]
  }
  showDialog.value = true
}

const handleImageRead = (file) => {
  // 保存文件对象，用于提交时上传
  previewImageFile.value = file.file
}

const handleImageDelete = () => {
  previewImageFile.value = null
}

const removeImage = () => {
  form.value.imageUrl = ''
  previewImageFile.value = null
  uploaderModel.value = []
}

const submit = async () => {
  try {
    // 构建 FormData
    const formData = new FormData()
    formData.append('name', form.value.name)
    formData.append('cost', String(form.value.cost))
    formData.append('stock', String(form.value.stock))
    formData.append('type', form.value.type)
    if (form.value.description) {
      formData.append('description', form.value.description)
    }
    formData.append('active', String(form.value.active))

    // 添加图片文件（如果选择了新图片）
    if (previewImageFile.value) {
      formData.append('imageFile', previewImageFile.value)
    } else if (form.value.id && form.value.imageUrl) {
      // 编辑时保留现有图片
      formData.append('keepExistingImage', 'true')
    }

    if (form.value.id) {
      await rewardsApi.update(form.value.id, formData)
      showToast('更新成功')
    } else {
      await rewardsApi.create(formData)
      showToast('创建成功')
    }
    showDialog.value = false
    load()
  } catch (e) {
    console.error(e)
    showToast('保存失败')
  }
}

const remove = async (id) => {
  try {
    await rewardsApi.delete(id)
    showToast('已下架')
    load()
  } catch (e) {
    console.error(e)
    showToast('操作失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.reward-management { padding:20px; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.grid { display:grid; grid-template-columns: repeat(auto-fill,minmax(220px,1fr)); gap:16px; }
.card { background:rgba(255,255,255,0.95); border-radius:10px; overflow:hidden; box-shadow:0 8px 18px rgba(0,0,0,0.08); }
.card-body { padding:12px; }
.title { font-weight:700; }
.meta { display:flex; justify-content:space-between; margin-top:8px; color:#777; }
.actions { margin-top:10px; display:flex; gap:8px; }
.empty { text-align:center; color:#999; padding:30px; }

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
 
