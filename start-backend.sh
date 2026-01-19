#!/bin/bash

echo "🚀 StarQuest 后端启动脚本"
echo "=========================="

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "❌ Java 未安装，请先安装 Java 17 或更高版本"
    echo ""
    echo "macOS 安装方法："
    echo "1. 安装 Homebrew (如果没有):"
    echo "   /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
    echo ""
    echo "2. 安装 OpenJDK 17:"
    echo "   brew install openjdk@17"
    echo ""
    echo "3. 配置环境变量，添加到 ~/.zshrc 或 ~/.bash_profile:"
    echo "   echo 'export PATH=\"/usr/local/opt/openjdk@17/bin:\$PATH\"' >> ~/.zshrc"
    echo "   echo 'export CPPFLAGS=\"-I/usr/local/opt/openjdk@17/include\"' >> ~/.zshrc"
    echo ""
    echo "4. 重新加载配置:"
    echo "   source ~/.zshrc"
    echo ""
    exit 1
fi

echo "✅ Java 版本信息:"
java -version
echo ""

# 检查Maven是否安装
if command -v mvn &> /dev/null; then
    echo "✅ 检测到系统 Maven:"
    mvn -version
    echo ""
    echo "🚀 使用系统 Maven 启动..."
    cd backend && mvn spring-boot:run
elif [ -f "backend/mvnw" ]; then
    echo "✅ 检测到 Maven Wrapper"
    echo "🚀 使用 Maven Wrapper 启动..."
    cd backend && chmod +x mvnw && ./mvnw spring-boot:run
else
    echo "❌ 既没有找到系统 Maven，也没有 Maven Wrapper"
    echo ""
    echo "安装 Maven 的方法："
    echo ""
    echo "方法1 - 使用 Homebrew 安装系统 Maven:"
    echo "   brew install maven"
    echo ""
    echo "方法2 - 下载并使用 Maven Wrapper:"
    echo "   cd backend"
    echo "   curl -o mvnw https://raw.githubusercontent.com/apache/maven-wrapper/master/mvnw"
    echo "   chmod +x mvnw"
    echo "   ./mvnw --version"
    echo ""
    exit 1
fi
