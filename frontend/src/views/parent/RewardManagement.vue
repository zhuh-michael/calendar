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
          <van-image :src="r.imageUrl || defaultImage" width="100%" height="120" fit="cover" />
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
      <van-field v-model="form.imageUrl" label="图片URL" placeholder="可选" />
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
import { ref, onMounted } from 'vue'
import { rewards as rewardsApi } from '@/utils/api.js'
import { showToast } from 'vant'

const rewards = ref([])
const loading = ref(false)
const defaultImage = '/default-avatar.svg'
const showDialog = ref(false)
const form = ref({ id: null, name:'', description:'', cost:0, stock:0, imageUrl:'', type:'ITEM', active:true })
const typeOptions = [
  { text:'实物', value:'ITEM' },
  { text:'虚拟', value:'VIRTUAL' },
  { text:'盲盒券', value:'BLINDBOX' }
]

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
  form.value = { id:null, name:'', description:'', cost:0, stock:0, imageUrl:'', type:'ITEM', active:true }
  showDialog.value = true
}

const openEdit = (r) => {
  form.value = { ...r }
  showDialog.value = true
}

const submit = async () => {
  try {
    if (form.value.id) {
      await rewardsApi.update(form.value.id, form.value)
      showToast('更新成功')
    } else {
      await rewardsApi.create(form.value)
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
</style>
 
