<template>
  <div class="reward-shop">
    <!-- 顶部栏 -->
    <div class="header">
      <div class="user-info">
        <van-image
          :src="`${apiBaseUrl}/${userInfo.avatar}`"
          round
          width="50"
          height="50"
          class="user-avatar"
        />
        <div class="user-details">
          <h3 class="user-name">{{ userInfo.nickname }}</h3>
          <div class="star-balance">
            <van-icon name="star" color="#FFD700" size="18" />
            <span class="star-count">{{ userInfo.starBalance }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品货架 -->
    <div class="shop-section">
      <h2 class="shop-title">🛍️ 星星商店</h2>
      <p class="shop-subtitle">用你的星星兑换心仪的奖励吧！</p>

      <div v-if="loading" class="loading-state">
        <van-loading size="40px" color="#FFD700">加载商品中...</van-loading>
      </div>

      <div v-else-if="rewards.length === 0" class="empty-state">
        <van-icon name="shop" size="60" color="#ccc" />
        <p>商店正在补货中...</p>
      </div>

      <div v-else class="product-grid">
        <div
          v-for="reward in rewardList"
          :key="reward.id"
          class="product-card animate__animated animate__zoomIn"
          :class="{
            'can-afford': userInfo.starBalance >= reward.cost,
            'cannot-afford': userInfo.starBalance < reward.cost
          }"
        >
          <div class="product-image">
            <van-image
              :src="`${apiBaseUrl}/${reward.imageUrl}`"
              width="100%"
              height="120"
              fit="cover"
              class="product-img"
            />
            <div v-if="userInfo.starBalance < reward.cost" class="insufficient-overlay">
              <van-icon name="lock" size="30" color="#fff" />
            </div>
          </div>

          <div class="product-info">
            <h4 class="product-name">{{ reward.name }}</h4>
            <p class="product-description">{{ reward.description || '精彩的系统奖励，等你来兑换！' }}</p>

            <div class="product-price">
              <van-icon name="star" color="#FFD700" size="16" />
              <span class="price-text">{{ reward.cost }}</span>
            </div>

            <!-- 进度条（余额不足时显示） -->
            <div v-if="userInfo.starBalance < reward.cost" class="progress-section">
              <van-progress
                :percentage="(userInfo.starBalance / reward.cost) * 100"
                color="#FFD700"
                track-color="#eee"
                :show-pivot="false"
                stroke-width="8"
              />
              <p class="progress-text">还差 {{ reward.cost - userInfo.starBalance }} 颗星星</p>
            </div>
          </div>

          <div class="product-actions">
            <van-button
              :type="userInfo.starBalance >= reward.cost ? 'primary' : 'default'"
              round
              size="small"
              :disabled="userInfo.starBalance < reward.cost || purchasing === reward.id"
              :loading="purchasing === reward.id"
              @click="purchaseReward(reward)"
              class="purchase-btn"
            >
              <template v-if="purchasing === reward.id">
                兑换中...
              </template>
              <template v-else>
                {{ userInfo.starBalance >= reward.cost ? '立即兑换' : '星星不足' }}
              </template>
            </van-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 购买确认弹窗 -->
    <van-dialog
      v-model:show="showConfirmDialog"
      title="确认兑换"
      :show-cancel-button="true"
      confirm-button-text="确认兑换"
      cancel-button-text="再想想"
      @confirm="confirmPurchase"
      @cancel="cancelPurchase"
    >
      <div class="confirm-content">
        <div class="selected-reward">
          <van-image
            :src="selectedReward?.imageUrl || defaultProductImage"
            width="80"
            height="80"
            fit="cover"
            round
          />
          <div class="reward-details">
            <h4>{{ selectedReward?.name }}</h4>
            <p>需要 {{ selectedReward?.cost }} 颗星星</p>
          </div>
        </div>
        <div class="balance-info">
          <p>兑换后余额：{{ userInfo.starBalance - (selectedReward?.cost || 0) }} 颗星星</p>
        </div>
        <div class="warning-text">
          <van-icon name="info" color="#FF9800" />
          <span>兑换成功后，系统会通过快递的方式发放奖励，请注意查收哦！</span>
        </div>
      </div>
    </van-dialog>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="$router.push('/kid/dashboard')">
        <van-icon name="wap-home" size="24" />
        <span>首页</span>
      </div>
      <div class="nav-item active">
        <van-icon name="shop" size="24" />
        <span>商店</span>
      </div>
      <div class="nav-item" @click="$router.push('/kid/lucky-house')">
        <van-icon name="gem" size="24" />
        <span>幸运屋</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { showToast, Dialog } from 'vant'
import { rewards } from '@/utils/api.js'
import { useUserStore } from '@/stores/user.js'
import { getApiBaseUrl } from '@/utils/config.js' 

const userStore = useUserStore()
const userInfo = computed(() => userStore.currentUser)
const rewardsList = ref([])
const loading = ref(false)
const purchasing = ref(null)
const showConfirmDialog = ref(false)
const selectedReward = ref(null)
const defaultAvatar = '/default-avatar.svg'
const defaultProductImage = 'https://via.placeholder.com/150x120?text=🎁'

const rewardList = computed(() => rewardsList.value)

// API基础URL
const apiBaseUrl = getApiBaseUrl()
// 加载用户信息
const loadUserInfo = async () => {
  await userStore.loadUserInfo(true) // 强制刷新以获取最新数据
}

// 加载商品列表
const loadRewards = async () => {
  loading.value = true
  try {
    const response = await rewards.list()
    rewardsList.value = response.data.filter(reward => reward.active) // 只显示激活的商品
  } catch (error) {
    console.error('Failed to load rewards:', error)
    showToast('加载商品失败')
  } finally {
    loading.value = false
  }
}

// 购买商品
const purchaseReward = (reward) => {
  if (userInfo.value.starBalance < reward.cost) {
    showToast('星星不足，无法兑换')
    return
  }

  selectedReward.value = reward
  showConfirmDialog.value = true
}

// 确认购买
const confirmPurchase = async () => {
  if (!selectedReward.value) return

  purchasing.value = selectedReward.value.id

  try {
    await rewards.purchase(selectedReward.value.id, userInfo.value.userId)

    // 更新本地星星余额
    userStore.deductStars(selectedReward.value.cost)

    showToast({
      message: `🎉 兑换成功！系统会通过快递的方式发放 ${selectedReward.value.name}，请注意查收哦！`,
      icon: 'success'
    })

    showConfirmDialog.value = false
    selectedReward.value = null

  } catch (error) {
    console.error('Failed to purchase reward:', error)
    showToast('兑换失败，请重试')
  } finally {
    purchasing.value = null
  }
}

// 取消购买
const cancelPurchase = () => {
  selectedReward.value = null
}

onMounted(() => {
  loadUserInfo()
  loadRewards()
})
</script>

<style scoped>
.reward-shop {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFE4B5 0%, #FFA07A 50%, #FF6347 100%);
  padding: 20px;
  padding-bottom: 100px;
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 15px 20px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 2px solid #FFD700;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  border: 3px solid #FFD700;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.3);
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.star-balance {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #FF9800;
  font-weight: bold;
  font-size: 16px;
}

.shop-section {
  text-align: center;
}

.shop-title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.shop-subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.product-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 2px solid #E0E0E0;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.product-card.can-afford {
  border-color: #4CAF50;
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.2);
}

.product-card.cannot-afford {
  border-color: #ccc;
  opacity: 0.8;
}

.product-image {
  position: relative;
  height: 120px;
  overflow: hidden;
}

.product-img {
  border-radius: 18px 18px 0 0;
}

.insufficient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px 18px 0 0;
}

.product-info {
  padding: 15px;
  text-align: left;
}

.product-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.product-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  font-weight: bold;
  color: #FF9800;
  margin-bottom: 12px;
}

.progress-section {
  margin-top: 10px;
}

.progress-text {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.product-actions {
  padding: 0 15px 15px;
}

.purchase-btn {
  width: 100%;
  background: linear-gradient(45deg, #4CAF50, #45a049);
  border: none;
  font-weight: bold;
}

.purchase-btn:disabled {
  background: #ccc;
}

.empty-state, .loading-state {
  padding: 60px 20px;
  text-align: center;
  color: #999;
}

.empty-state p, .loading-state {
  margin-top: 15px;
  font-size: 16px;
}

/* 确认弹窗样式 */
.confirm-content {
  padding: 20px;
}

.selected-reward {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 20px;
}

.reward-details h4 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #333;
}

.reward-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.balance-info {
  text-align: center;
  margin-bottom: 15px;
  font-weight: bold;
  color: #FF9800;
}

.warning-text {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: #FFF3CD;
  border-radius: 8px;
  font-size: 14px;
  color: #856404;
}

@media (max-width: 480px) {
  .reward-shop {
    padding: 15px;
  }

  .product-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .product-card {
    margin: 0 auto;
    max-width: 300px;
  }

  .shop-title {
    font-size: 28px;
  }
}

/* 底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-top: 2px solid #FFD700;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  min-width: 70px;
}

.nav-item.active {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: #8B4513;
}

.nav-item.active span {
  color: #8B4513;
  font-weight: bold;
}

.nav-item:not(.active):hover {
  background: rgba(255, 215, 0, 0.1);
  transform: translateY(-2px);
}

.nav-item span {
  font-size: 12px;
  margin-top: 4px;
  color: #666;
  transition: color 0.3s ease;
}

.nav-item.active .van-icon {
  color: #8B4513;
}

.nav-item:not(.active) .van-icon {
  color: #999;
}
</style>


