#!/bin/bash

echo "🧪 StarQuest 应用功能测试脚本"
echo "================================"

# 检查服务是否运行
check_service() {
    local url=$1
    local name=$2
    if curl -s --max-time 5 "$url" > /dev/null; then
        echo "✅ $name 服务运行正常"
        return 0
    else
        echo "❌ $name 服务未运行或无响应"
        return 1
    fi
}

echo ""
echo "📡 检查服务状态..."
check_service "http://localhost:8080/api/auth/login" "后端API"
check_service "http://localhost:5173" "统一前端应用 (kid/parent 路由)"

echo ""
echo "🎯 测试账号信息:"
echo "家长端: admin / password"
echo "学员端: testkid / 123456 (测试小朋友)"

echo ""
echo "🔗 访问地址:"
echo "统一前端应用 (kid/parent 路由): http://localhost:5173"
echo "API文档: http://localhost:8080"

echo ""
echo "📋 推荐测试流程:"
echo "1. 打开家长端，登录admin/password"
echo "2. 在仪表盘中查看统计信息"
echo "3. 创建一个测试孩子账号"
echo "4. 创建任务并分配给孩子"
echo "5. 打开学员端，选择测试孩子登录"
echo "6. 体验任务完成、补给站购物、幸运屋抽奖"
echo "7. 返回家长端查看报表数据"

echo ""
echo "✨ 享受测试体验！"
