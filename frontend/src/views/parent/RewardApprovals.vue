<template>
  <div class="approvals">
    <ParentNav />
    <h2>兑换审批</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="ordersList.length === 0" class="empty">当前没有待处理的兑换申请</div>
      <div v-else class="order-list">
        <div v-for="order in ordersList" :key="order.id" class="order-item">
          <div class="order-info">
            <div><strong>订单ID:</strong> {{ order.id }}</div>
            <div><strong>小朋友:</strong> {{ order.kidNickname }} (ID: {{ order.kidId }})</div>
            <div><strong>奖励:</strong> {{ order.rewardName }} (ID: {{ order.rewardId }})</div>
            <div><strong>创建时间:</strong> {{ formatTime(order.createTime) }}</div>
          </div>
          <div class="order-actions">
            <van-button size="small" type="primary" @click="confirmDeliver(order.id)">确认发货</van-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orders } from '@/utils/api.js'
import { showToast } from 'vant'
import ParentNav from '@/components/ParentNav.vue'

const loading = ref(false)
const ordersList = ref([])

const formatTime = (iso) => {
  return iso ? new Date(iso).toLocaleString() : ''
}

const loadOrders = async () => {
  loading.value = true
  try {
    const resp = await orders.getPending()
    ordersList.value = resp.data || []
  } catch (e) {
    console.error(e)
    showToast('加载待处理订单失败')
  } finally {
    loading.value = false
  }
}

const confirmDeliver = async (orderId) => {
  try {
    await orders.deliver(orderId)
    showToast('已确认发货')
    loadOrders()
  } catch (e) {
    console.error(e)
    showToast('确认发货失败')
  }
}

onMounted(() => loadOrders())
</script>

<style scoped>
.approvals { padding:20px; }
.order-item { display:flex; justify-content:space-between; gap:12px; background:rgba(255,255,255,0.95); padding:12px; border-radius:8px; margin-bottom:8px; }
.order-info div { font-size:13px; color:#333; }
.empty { color:#999; padding:20px; text-align:center; }
</style>


