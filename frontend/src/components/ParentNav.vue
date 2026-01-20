<template>
  <nav class="parent-nav">
    <router-link to="/parent/dashboard" class="nav-item" :class="{ active: isActive('/parent/dashboard') }">仪表盘</router-link>
    <router-link to="/parent/tasks" class="nav-item" :class="{ active: isActive('/parent/tasks') }">任务管理</router-link>
    <router-link to="/parent/rewards" class="nav-item" :class="{ active: isActive('/parent/rewards') }">商品管理</router-link>
    <router-link to="/parent/rewards/approvals" class="nav-item" :class="{ active: isActive('/parent/rewards/approvals') }">兑换审批</router-link>
    <router-link to="/parent/kids" class="nav-item" :class="{ active: isActive('/parent/kids') }">小朋友管理</router-link>
    <router-link to="/parent/templates" class="nav-item" :class="{ active: isActive('/parent/templates') }">模板库</router-link>
    <router-link to="/parent/analytics" class="nav-item" :class="{ active: isActive('/parent/analytics') }">报表</router-link>
  </nav>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
const route = useRoute()
const isActive = (path) => route.path === path
</script>

<style scoped>
.parent-nav {
  display:flex;
  gap:12px;
  background: rgba(255,255,255,0.95);
  padding:10px;
  border-radius:8px;
  margin-bottom:12px;
}
.nav-item { padding:6px 12px; color:#333; text-decoration:none; border-radius:6px; }
.nav-item.active { background: linear-gradient(45deg,#FFD700,#FFA500); color:#8B4513; font-weight:700; }
</style>


