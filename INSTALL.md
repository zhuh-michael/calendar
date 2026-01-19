# 🚀 StarQuest 安装和运行指南

## 环境要求

- **操作系统**: macOS 10.15+ / Windows 10+ / Linux
- **Java**: JDK 17 或更高版本
- **Node.js**: 16.x 或更高版本 (用于前端)
- **Maven**: 3.6+ (可选，可使用 Maven Wrapper)

## 快速开始

### 1. 环境检查

运行以下命令检查环境：

```bash
# 检查Java
java -version

# 检查Node.js
node -version
npm -version

# 检查Maven (可选)
mvn -version
```

### 2. 安装Java (macOS)

如果没有安装Java，请按以下步骤安装：

```bash
# 1. 安装Homebrew (如果没有)
# 访问 https://brew.sh/ 按说明安装

# 2. 安装OpenJDK 17
brew install openjdk@17

# 3. 配置环境变量
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export CPPFLAGS="-I/usr/local/opt/openjdk@17/include"' >> ~/.zshrc

# 4. 重新加载配置
source ~/.zshrc

# 5. 验证安装
java -version
```

### 3. 启动后端服务

```bash
# 方法1: 使用启动脚本 (推荐)
./start-backend.sh

# 方法2: 手动启动
cd backend

# 如果有系统Maven
mvn spring-boot:run

# 或使用Maven Wrapper
./mvnw spring-boot:run
```

### 4. 启动前端服务

```bash
# 家长端
cd frontend-parent
npm install
npm run dev

# 学员端
cd frontend-kid
npm install
npm run dev
```

## 详细安装步骤

### Windows 用户

#### 安装Java
1. 下载并安装 JDK 17: https://adoptium.net/temurin/releases/
2. 配置环境变量：
   - 添加 `JAVA_HOME` 环境变量指向JDK安装目录
   - 将 `%JAVA_HOME%\bin` 添加到 `PATH`

#### 安装Node.js
1. 下载并安装 Node.js: https://nodejs.org/
2. 验证安装：`node -version` 和 `npm -version`

#### 安装Maven (可选)
1. 下载 Maven: https://maven.apache.org/download.cgi
2. 解压到合适目录
3. 配置环境变量：
   - 添加 `MAVEN_HOME` 环境变量
   - 将 `%MAVEN_HOME%\bin` 添加到 `PATH`

### Linux 用户

#### Ubuntu/Debian
```bash
# 安装Java
sudo apt update
sudo apt install openjdk-17-jdk

# 安装Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装Maven (可选)
sudo apt install maven
```

#### CentOS/RHEL
```bash
# 安装Java
sudo yum install java-17-openjdk-devel

# 安装Node.js
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs

# 安装Maven (可选)
sudo yum install maven
```

## 常见问题

### 1. Java版本问题
```
错误: Java版本过低
```
**解决**: 确保安装了JDK 17或更高版本
```bash
java -version  # 应该显示 17.x.x
```

### 2. Maven未找到
```
mvn: command not found
```
**解决**: 使用Maven Wrapper或安装Maven
```bash
# 使用Wrapper
cd backend && ./mvnw spring-boot:run

# 或安装Maven
brew install maven  # macOS
sudo apt install maven  # Ubuntu
```

### 3. 端口冲突
```
Port 8080 is already in use
```
**解决**: 修改application.properties中的端口
```properties
server.port=8081
```

### 4. 数据库连接问题
```
Could not connect to database
```
**解决**: 检查SQLite文件路径，项目会自动创建数据库文件

### 5. 前端依赖安装失败
```
npm install 失败
```
**解决**: 清除缓存后重试
```bash
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

## 项目结构

```
starquest/
├── backend/                 # Spring Boot后端
│   ├── src/main/java/com/starquest/backend/
│   │   ├── controller/      # REST API控制器
│   │   ├── service/         # 业务逻辑层
│   │   ├── repository/      # 数据访问层
│   │   ├── model/          # 实体模型
│   │   └── config/         # 配置类
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend-parent/         # 家长端Vue应用
├── frontend-kid/           # 学员端Vue应用
├── database/               # SQLite数据库文件
├── docs/                   # 项目文档
├── start-backend.sh        # 后端启动脚本
└── README.md               # 项目说明
```

## 服务端口

- 后端API: http://localhost:8080
- 家长端: http://localhost:5173 (开发时)
- 学员端: http://localhost:5174 (开发时)

## 访问应用

启动所有服务后：

1. **家长端**: 打开浏览器访问家长端URL
2. **学员端**: 打开浏览器访问学员端URL
3. **测试账号**:
   - **家长端管理员**: `admin` / `password`
   - **学员端预设账号**:
     - `testkid` / `123456` (测试小朋友)
     - `xiaoming` / `123456` (小明)
     - `xiaohong` / `123456` (小红)
   - 或通过家长端创建新的孩子账号

## 获取帮助

如果遇到问题，请：

1. 检查环境配置是否正确
2. 查看控制台错误信息
3. 确认所有服务都已启动
4. 查看项目README中的故障排除部分

---

**🎉 祝您使用愉快！如果有问题随时联系。**
