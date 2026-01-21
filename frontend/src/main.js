import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import Vant from 'vant'
import 'vant/lib/index.css'
import 'animate.css'
import '@/styles/index.css'
import '@/styles/modals.css'
import '@/styles/buttons.css'
import { useUserStore } from '@/stores/user.js'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(Vant)

// 初始化用户store，从缓存加载用户信息
const userStore = useUserStore()
userStore.initializeFromCache()
 
// register ElementPlus icons globally
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')


