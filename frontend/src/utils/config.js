// 前端配置管理
export const config = {
  // API基础URL
  get apiBaseUrl() {
    return import.meta.env.VITE_API_BASE || 'http://localhost:8081'
  },

  // 其他配置可以在这里添加
  // 例如：图片上传大小限制、支持的文件类型等
  upload: {
    maxFileSize: 10 * 1024 * 1024, // 10MB
    allowedTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  }
}

// 便捷的getter函数
export const getApiBaseUrl = () => config.apiBaseUrl
