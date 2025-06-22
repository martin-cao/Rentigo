# Rentigo

基于 Spring Boot 和 Vue.js 构建的现代化汽车租赁平台。

## 概述

Rentigo 是一个功能完备的汽车租赁管理系统，具备以下功能：
- 浏览和租赁车辆
- 使用 Stripe 处理支付
- 跟踪租赁状态
- 管理押金和超时费用
- 处理用户认证和授权

## 技术栈

### 后端
- Java 21
- Spring Boot 3.x
- MySQL 8.0
- Stripe 支付 API
- JWT 认证

### 前端
- Vue.js 3
- Vite
- TypeScript
- Tailwind CSS

## 构建说明

### 环境要求

1. JDK 21 或以上
2. Maven 3.8+
3. MySQL 8.0
4. Node.js 16+
5. Stripe 账号和 API 密钥

### 数据库配置

1. 安装 MySQL 8.0
2. 创建新数据库：
```sql
CREATE DATABASE rentigo;
```
然后运行 `init_db.sql`
3. 更新 `backend/rentigo-backend/src/main/resources/application.properties` 中的数据库配置：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rentigo
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```

### Stripe 配置

1. 在 https://stripe.com 注册 Stripe 账号
2. 从 Stripe 控制面板获取 API 密钥
3. 在 `application.properties` 中配置 Stripe 密钥：
```properties
stripe.secret-key=你的_stripe_密钥
stripe.webhook.secret=你的_stripe_webhook_密钥
```

> [!WARNING]
> 请先创建 Stripe Sandbox 进行测试，请勿直接使用生产环境！


### 构建后端

1. 进入后端目录：
```bash
cd backend/rentigo-backend
```

2. 使用 Maven 构建：
```bash
./mvnw clean package
```

3. 运行应用：
```bash
./mvnw spring-boot:run
```

后端 API 将在 `http://localhost:8080` 可用

### 构建前端

1. 进入前端目录：
```bash
cd frontend/rentigo-frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端将在 `http://localhost:5173` 可用

## 配置说明

### 应用配置

主要应用配置（`application.properties`）：

```properties
# 服务器
server.port=8080

# 数据库
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=你的_jwt_密钥
jwt.expiration=86400000

# Stripe
stripe.secret-key=你的_stripe_密钥
stripe.webhook.secret=你的_stripe_webhook_密钥
app.frontend.success-url=http://localhost:5173/payment/success
app.frontend.cancel-url=http://localhost:5173/payment/cancel
```