# EasyExp - 简易支出追踪应用

<div align="center">
  <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=A%20clean%20modern%20finance%20tracking%20app%20dashboard%20with%20statistics%20cards%20and%20expense%20list&image_size=landscape_16_9" alt="EasyExp Dashboard" width="800" />
</div>

## 📋 项目简介

EasyExp 是一款基于 Next.js 和 MongoDB 开发的简易支出追踪应用，帮助用户轻松管理个人支出、追踪报销状态并生成统计报表。

## ✨ 功能特性

### 核心功能
- **用户认证**：注册、登录、密码修改
- **支出管理**：添加、编辑、删除支出记录
- **报销追踪**：支持多种报销状态，记录报销金额
- **统计分析**：支出总额、待报销金额、已报销金额、收支差额
- **数据筛选**：按日期范围、报销类型、支付类型筛选
- **Excel 导出**：导出支出记录为 Excel 文件
- **自定义类型**：支持自定义报销类型和支付类型

### 用户体验
- **响应式设计**：适配桌面和移动设备
- **实时数据**：自动更新统计数据
- **分页加载**：优化性能，支持大量数据
- **直观界面**：清晰的卡片式布局和状态标签

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Next.js | 16.1.5 |
| 样式方案 | Tailwind CSS | - |
| 数据库 | MongoDB | - |
| 认证方案 | JWT | - |
| 密码加密 | bcrypt | - |
| Excel 处理 | xlsx | - |
| 部署平台 | Vercel / Netlify / Cloudflare Pages | - |

## 🚀 快速开始

### 开发环境

1. **克隆仓库**
   ```bash
   git clone <your-repository-url>
   cd MiniLedger
   ```

2. **安装依赖**
   ```bash
   npm install
   ```

3. **配置环境变量**
   创建 `.env.local` 文件并添加以下内容：
   ```env
   MONGODB_URI=<your-mongodb-connection-string>
   JWT_SECRET=<your-jwt-secret-key>
   ```

4. **启动开发服务器**
   ```bash
   npm run dev
   ```

5. **访问应用**
   打开浏览器访问 `http://localhost:3000`

### 生产构建

```bash
npm run build
npm start
```

## 📦 部署指南

### Vercel 部署

1. **创建 Vercel 账号**
   访问 [Vercel 官网](https://vercel.com/) 注册/登录

2. **导入项目**
   - 点击 "New Project"
   - 选择 "Import from Git Repository"
   - 连接 GitHub 账号并选择 EasyExp 仓库

3. **配置项目**
   - Framework Preset: Next.js
   - 构建命令: 默认 (`next build`)
   - 输出目录: 默认 (`.next`)

4. **设置环境变量**
   - `MONGODB_URI`: MongoDB Atlas 连接字符串
   - `JWT_SECRET`: 自定义 JWT 密钥

5. **部署项目**
   - 点击 "Deploy" 按钮
   - 等待部署完成

### Netlify 部署

1. **创建 Netlify 账号**
   访问 [Netlify 官网](https://www.netlify.com/) 注册/登录

2. **导入项目**
   - 点击 "Add new site" → "Import an existing project"
   - 连接 GitHub 账号并选择 EasyExp 仓库

3. **配置项目**
   - 构建命令: `npm run build`
   - 发布目录: `.next`

4. **设置环境变量**
   - `MONGODB_URI`: MongoDB Atlas 连接字符串
   - `JWT_SECRET`: 自定义 JWT 密钥

5. **部署项目**
   - 点击 "Deploy site" 按钮
   - 等待部署完成

### Cloudflare Pages 部署

1. **创建 Cloudflare 账号**
   访问 [Cloudflare 官网](https://www.cloudflare.com/) 注册/登录

2. **导入项目**
   - 点击 "Workers & Pages" → "Create application" → "Pages"
   - 连接 GitHub 账号并选择 EasyExp 仓库

3. **配置项目**
   - 构建命令: `npm run build`
   - 构建输出目录: `.next`

4. **设置环境变量**
   - `MONGODB_URI`: MongoDB Atlas 连接字符串
   - `JWT_SECRET`: 自定义 JWT 密钥

5. **部署项目**
   - 点击 "Save and Deploy" 按钮
   - 等待部署完成

## 📁 项目结构

```
MiniLedger/
├── app/
│   ├── api/
│   │   ├── auth/           # 认证相关 API
│   │   ├── config/         # 配置管理 API
│   │   └── expenses/       # 支出管理 API
│   ├── add-expense/        # 添加支出页面
│   ├── edit-expense/       # 编辑支出页面
│   ├── settings/           # 设置页面
│   ├── page.tsx            # 首页（统计和支出列表）
│   └── layout.tsx          # 全局布局
├── lib/
│   ├── auth.ts             # 认证中间件
│   ├── authContext.tsx     # 认证上下文
│   ├── jwt.ts              # JWT 工具
│   └── mongodb.ts          # MongoDB 连接
├── public/                 # 静态资源
├── package.json            # 项目配置
├── tsconfig.json           # TypeScript 配置
└── README.md               # 项目文档
```

## 🔧 环境变量

| 变量名 | 描述 | 示例值 |
|--------|------|--------|
| `MONGODB_URI` | MongoDB Atlas 连接字符串 | `mongodb+srv://username:password@cluster0.mongodb.net/EasyExp?retryWrites=true&w=majority` |
| `JWT_SECRET` | JWT 签名密钥 | `your-secret-key-here` |

## 🎨 界面预览

### 首页统计
<div align="center">
  <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Finance%20dashboard%20with%20statistics%20cards%20showing%20total%20expense%2C%20pending%20reimburse%2C%20reimbursed%2C%20and%20balance&image_size=landscape_16_9" alt="Statistics Dashboard" width="600" />
</div>

### 支出记录
<div align="center">
  <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Expense%20list%20with%20cards%20showing%20amount%2C%20date%2C%20reimburse%20status%2C%20and%20payment%20type&image_size=landscape_16_9" alt="Expense List" width="600" />
</div>

### 添加支出
<div align="center">
  <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Add%20expense%20form%20with%20amount%2C%20reimburse%20type%2C%20payment%20type%2C%20date%2C%20and%20notes%20fields&image_size=portrait_4_3" alt="Add Expense Form" width="400" />
</div>

## 🔒 安全性

- **密码加密**：使用 bcrypt 对密码进行加密存储
- **JWT 认证**：使用 JSON Web Token 进行无状态认证
- **API 保护**：所有 API 端点都有认证中间件保护
- **输入验证**：对所有用户输入进行验证
- **CORS 配置**：正确配置跨域资源共享

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📞 联系

如果您有任何问题或建议，请随时联系我们。

## 📖 API 调用方法

### 基础信息

#### API 基础 URL
- 生产环境: `https://your-api-base-url.com`
- 开发环境: `http://localhost:3000`

#### 认证方式
- 使用 JWT (JSON Web Token) 进行认证
- 所有需要认证的 API 端点都需要在请求头中添加 `Authorization` 字段
- 格式: `Authorization: Bearer <token>`

### 认证相关 API

#### 1. 登录
- **端点**: `/api/auth/login`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- **响应**:
  ```json
  {
    "token": "JWT 令牌",
    "userId": "用户 ID",
    "error": null
  }
  ```

#### 2. 注册
- **端点**: `/api/auth/register`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "username": "用户名",
    "password": "密码",
    "email": "邮箱（可选）"
  }
  ```
- **响应**:
  ```json
  {
    "message": "注册成功",
    "userId": "用户 ID",
    "error": null
  }
  ```

#### 3. 修改密码
- **端点**: `/api/auth/change-password`
- **方法**: `POST`
- **请求头**: `Authorization: Bearer <token>`
- **请求体**:
  ```json
  {
    "currentPassword": "当前密码",
    "newPassword": "新密码"
  }
  ```
- **响应**:
  ```json
  {
    "message": "密码修改成功",
    "error": null
  }
  ```

### 配置管理 API

#### 1. 获取配置
- **端点**: `/api/config`
- **方法**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **响应**:
  ```json
  {
    "reimburseTypes": ["待报销", "报销中", "已报销"],
    "payTypes": ["微信", "支付宝", "现金", "网银"]
  }
  ```

#### 2. 更新配置
- **端点**: `/api/config`
- **方法**: `PUT`
- **请求头**: `Authorization: Bearer <token>`
- **请求体**:
  ```json
  {
    "type": "reimburseType", // 或 "payType"
    "options": ["待报销", "报销中", "已报销", "新增类型"]
  }
  ```
- **响应**:
  ```json
  {
    "reimburseTypes": ["待报销", "报销中", "已报销", "新增类型"],
    "payTypes": ["微信", "支付宝", "现金", "网银"]
  }
  ```

### 支出管理 API

#### 1. 获取支出列表
- **端点**: `/api/expenses`
- **方法**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **查询参数**:
  - `startDate`: 开始日期 (格式: YYYY-MM-DD)
  - `endDate`: 结束日期 (格式: YYYY-MM-DD)
  - `reimburseType`: 报销类型
  - `payType`: 支付类型
  - `page`: 页码 (默认: 1)
  - `limit`: 每页数量 (默认: 10)
- **响应**:
  ```json
  {
    "expenses": [
      {
        "_id": "支出 ID",
        "amount": 100.0,
        "reimburseType": "待报销",
        "payType": "微信",
        "date": "2024-01-01",
        "other": "备注信息",
        "reimburseAmount": 50.0
      }
    ],
    "total": 1
  }
  ```

#### 2. 添加支出
- **端点**: `/api/expenses`
- **方法**: `POST`
- **请求头**: `Authorization: Bearer <token>`
- **请求体**:
  ```json
  {
    "_id": "", // 新增时为空，由服务器生成
    "amount": 100.0,
    "reimburseType": "待报销",
    "payType": "微信",
    "date": "2024-01-01",
    "other": "备注信息",
    "reimburseAmount": 50.0
  }
  ```
- **响应**:
  ```json
  {
    "_id": "生成的支出 ID",
    "amount": 100.0,
    "reimburseType": "待报销",
    "payType": "微信",
    "date": "2024-01-01",
    "other": "备注信息",
    "reimburseAmount": 50.0
  }
  ```

#### 3. 更新支出
- **端点**: `/api/expenses/{id}`
- **方法**: `PUT`
- **请求头**: `Authorization: Bearer <token>`
- **请求体**:
  ```json
  {
    "_id": "支出 ID",
    "amount": 150.0,
    "reimburseType": "报销中",
    "payType": "微信",
    "date": "2024-01-01",
    "other": "更新后的备注",
    "reimburseAmount": 100.0
  }
  ```
- **响应**:
  ```json
  {
    "_id": "支出 ID",
    "amount": 150.0,
    "reimburseType": "报销中",
    "payType": "微信",
    "date": "2024-01-01",
    "other": "更新后的备注",
    "reimburseAmount": 100.0
  }
  ```

#### 4. 删除支出
- **端点**: `/api/expenses/{id}`
- **方法**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`
- **响应**: 无内容 (204 No Content)

#### 5. 获取支出统计
- **端点**: `/api/expenses/stats`
- **方法**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **查询参数**:
  - `startDate`: 开始日期 (格式: YYYY-MM-DD)
  - `endDate`: 结束日期 (格式: YYYY-MM-DD)
  - `reimburseType`: 报销类型
  - `payType`: 支付类型
- **响应**:
  ```json
  {
    "totalExpense": 1000.0,
    "pendingReimburse": 500.0,
    "reimbursed": 300.0,
    "balance": -200.0
  }
  ```

### 客户端代码示例 (Kotlin/Android)

#### 1. API 服务定义

```kotlin
interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("/api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse
    
    @POST("/api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): ChangePasswordResponse
    
    @GET("/api/expenses/stats")
    suspend fun getExpenseStats(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): ExpenseStatsResponse
    
    @GET("/api/expenses")
    suspend fun getExpenses(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): ExpenseListResponse
    
    @GET("/api/config")
    suspend fun getConfig(
        @Header("Authorization") token: String
    ): ConfigResponse
    
    @PUT("/api/config")
    suspend fun updateConfig(
        @Header("Authorization") token: String,
        @Body configRequest: ConfigRequest
    ): ConfigResponse
    
    @POST("/api/expenses")
    suspend fun addExpense(
        @Header("Authorization") token: String,
        @Body expense: Expense
    ): Expense
    
    @PUT("/api/expenses/{id}")
    suspend fun updateExpense(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body expense: Expense
    ): Expense
    
    @DELETE("/api/expenses/{id}")
    suspend fun deleteExpense(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Unit
}

// 请求和响应数据类
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String,
    val error: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val message: String,
    val error: String? = null
)

data class ConfigRequest(
    val type: String,
    val options: List<String>
)

data class ConfigResponse(
    val reimburseTypes: List<String>,
    val payTypes: List<String>
)
```

#### 2. 网络模块配置

```kotlin
object NetworkModule {
    private const val BASE_URL = "https://your-api-base-url.com"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
```

#### 3. 使用示例

```kotlin
// 修改密码
fun changePassword(oldPassword: String, newPassword: String) {
    viewModelScope.launch {
        val token = _token.value
        if (token == null) {
            _error.value = "请先登录"
            return@launch
        }
        val bearerToken = "Bearer $token"
        try {
            val response = NetworkModule.apiService.changePassword(
                bearerToken,
                ChangePasswordRequest(currentPassword = oldPassword, newPassword = newPassword)
            )
            if (response.error == null) {
                _error.value = response.message
            } else {
                _error.value = "密码修改失败: ${response.error}"
            }
        } catch (e: HttpException) {
            _error.value = "密码修改失败: HTTP错误 ${e.code()} - ${e.message()}"
        } catch (e: Exception) {
            _error.value = "密码修改失败: ${e.message}"
        }
    }
}

// 添加报销类型
fun addReimburseType(typeName: String) {
    viewModelScope.launch {
        val token = authViewModel.token.value
        if (token == null) {
            _error.value = "添加报销类型失败: 未登录"
            return@launch
        }
        val bearerToken = "Bearer $token"
        try {
            // 获取当前配置
            val currentConfig = _config.value
            // 创建新的报销类型列表
            val newReimburseTypes = currentConfig.reimburseTypes.toMutableList()
            newReimburseTypes.add(typeName)
            // 更新配置
            val response = NetworkModule.apiService.updateConfig(
                bearerToken,
                ConfigRequest("reimburseType", newReimburseTypes)
            )
            // 更新本地配置
            _config.value = response
        } catch (e: HttpException) {
            _error.value = "添加报销类型失败: HTTP错误 ${e.code()} - ${e.message()}"
        } catch (e: Exception) {
            _error.value = "添加报销类型失败: ${e.message}"
        }
    }
}
```

### 常见错误和解决方案

#### 1. HTTP 401 Unauthorized
- **原因**: 未提供认证令牌或令牌无效
- **解决方案**:
  - 确保在请求头中添加了 `Authorization: Bearer <token>`
  - 确保令牌未过期
  - 确保令牌格式正确

#### 2. HTTP 400 Bad Request
- **原因**: 请求参数错误或格式不正确
- **解决方案**:
  - 检查请求体格式是否正确
  - 检查字段名称是否与API文档一致
  - 确保所有必填字段都已提供

#### 3. HTTP 405 Method Not Allowed
- **原因**: 使用了错误的HTTP方法
- **解决方案**:
  - 检查API端点的HTTP方法是否正确
  - 例如: 配置更新应该使用 `PUT` 方法，而不是 `POST` 方法

#### 4. HTTP 500 Internal Server Error
- **原因**: 服务器内部错误
- **解决方案**:
  - 检查服务器日志以获取详细错误信息
  - 确保请求格式正确
  - 联系服务器管理员

### 最佳实践

1. **认证令牌管理**:
   - 安全存储JWT令牌
   - 实现令牌过期处理
   - 在令牌过期时自动重新登录

2. **错误处理**:
   - 对所有网络请求进行异常捕获
   - 提供清晰的错误信息给用户
   - 实现网络连接状态检测

3. **请求优化**:
   - 使用适当的HTTP方法
   - 合理设置请求超时
   - 实现请求缓存

4. **安全性**:
   - 不要在客户端存储敏感信息
   - 实现HTTPS通信
   - 对用户输入进行验证

---

<div align="center">
  <p>Made with ❤️ using Next.js and MongoDB</p>
</div>
