# EasyExp 简易账本开发框架方案

## 1. 项目概述

EasyExp 是一款简易的个人支出跟踪应用，旨在帮助用户方便地记录、管理和分析个人支出，支持自定义报销类型和支付方式，提供统计分析和数据导出功能。

## 2. 技术栈

### 前端技术
- **框架**: Next.js 16.1.5 (App Router)
- **语言**: TypeScript
- **样式**: Tailwind CSS v4
- **状态管理**: React Context API
- **图表库**: 可选集成 Chart.js 用于统计图表
- **工具库**: 
  - `date-fns` 用于日期处理
  - `xlsx` 用于 Excel 导出
  - `bcryptjs` 用于前端密码验证（可选）

### 后端技术
- **运行环境**: Node.js 18+
- **数据库**: MongoDB Atlas
- **认证**: JWT (JSON Web Token)
- **密码加密**: bcrypt
- **API 设计**: RESTful API

### 开发工具
- **包管理**: npm
- **代码质量**: ESLint + Prettier
- **类型检查**: TypeScript Compiler
- **版本控制**: Git

## 3. 项目结构

```
├── app/                   # App Router 目录
│   ├── api/               # API 路由
│   │   ├── auth/          # 认证相关 API
│   │   │   ├── login/     # 登录
│   │   │   ├── register/  # 注册
│   │   │   └── change-password/  # 修改密码
│   │   ├── config/        # 配置管理 API
│   │   ├── expenses/      # 支出管理 API
│   │   │   ├── [id]/      # 单个支出操作
│   │   │   ├── stats/     # 统计功能
│   │   │   └── export/    # 导出功能
│   │   └── users/         # 用户管理 API
│   ├── add-expense/       # 新增支出页面
│   ├── edit-expense/      # 编辑支出页面
│   │   └── [id]/          # 动态路由
│   ├── settings/          # 设置页面
│   ├── login/             # 登录页面
│   ├── register/          # 注册页面
│   ├── page.tsx           # 首页（支出列表和统计）
│   └── layout.tsx         # 全局布局
├── components/            # 共享组件
│   ├── auth/              # 认证相关组件
│   ├── expense/           # 支出相关组件
│   ├── layout/            # 布局组件
│   └── ui/                # 通用 UI 组件
├── lib/                   # 工具库
│   ├── auth.ts            # 认证中间件
│   ├── authContext.tsx    # 认证上下文（包含 authenticatedFetch 函数）
│   ├── jwt.ts             # JWT 工具
│   ├── mongodb.ts         # MongoDB 连接
│   └── utils.ts           # 通用工具函数
├── types/                 # TypeScript 类型定义
├── public/                # 静态资源
├── package.json           # 项目配置
├── tsconfig.json          # TypeScript 配置
├── tailwind.config.js     # Tailwind 配置
└── README.md              # 项目文档
```

## 4. 核心功能模块

### 4.1 用户认证模块

#### 功能点
- 用户注册
- 用户登录
- 密码修改
- JWT 令牌管理
- 权限验证
- 登录失效自动处理

#### 实现细节
- 使用 bcrypt 进行密码加密存储
- 使用 JWT 进行无状态认证
- 实现认证中间件保护需要登录的 API
- 实现认证上下文管理前端登录状态
- 实现 `authenticatedFetch` 函数处理登录失效场景
- 当登录过期时自动跳转到登录页并提示用户

### 4.2 支出管理模块

#### 功能点
- 添加支出记录
- 编辑支出记录
- 删除支出记录
- 支出列表展示
- 支出详情查看

#### 实现细节
- 支持自定义报销类型和支付方式
- 实现表单验证
- 支持日期选择和金额输入
- 实现分页加载大量数据

### 4.3 配置管理模块

#### 功能点
- 管理报销类型
- 管理支付类型
- 用户偏好设置

#### 实现细节
- 支持添加、编辑、删除自定义类型
- 每个用户有独立的配置
- 提供默认配置作为 fallback

### 4.4 统计分析模块

#### 功能点
- 支出总额统计
- 待报销金额统计
- 已报销金额统计
- 收支差额统计
- 按时间段、类型等维度统计

#### 实现细节
- 使用 MongoDB 聚合查询实现统计
- 支持筛选条件下的统计
- 可选集成图表展示

### 4.5 数据导出模块

#### 功能点
- Excel 格式导出
- 支持导出筛选后的记录

#### 实现细节
- 使用 xlsx 库生成 Excel 文件
- 支持自定义导出字段
- 实现流式下载

### 4.6 筛选功能模块

#### 功能点
- 按日期范围筛选
- 按报销类型筛选
- 按支付类型筛选
- 重置筛选条件

#### 实现细节
- 支持预设日期范围（本周、本月、本年等）
- 支持自定义日期范围
- 筛选条件实时生效

## 5. 数据库设计

### 5.1 用户集合 (`users`)

| 字段名 | 类型 | 描述 | 索引 |
|-------|------|------|------|
| `_id` | `ObjectId` | 用户 ID | 主键 |
| `username` | `String` | 用户名 | 唯一 |
| `email` | `String` | 邮箱 | 唯一 |
| `password` | `String` | 加密后的密码 | - |
| `createdAt` | `Date` | 创建时间 | - |
| `updatedAt` | `Date` | 更新时间 | - |

### 5.2 支出集合 (`expense`)

| 字段名 | 类型 | 描述 | 索引 |
|-------|------|------|------|
| `_id` | `ObjectId` | 支出 ID | 主键 |
| `userId` | `String` | 用户 ID | 复合索引 |
| `amount` | `Number` | 支出金额 | - |
| `reimburseType` | `String` | 报销类型 | 复合索引 |
| `reimburseAmount` | `Number` | 已报销金额 | - |
| `payType` | `String` | 支付类型 | 复合索引 |
| `date` | `Date` | 支出日期 | 复合索引 |
| `other` | `String` | 备注 | - |
| `createTime` | `Date` | 创建时间 | - |
| `updateTime` | `Date` | 更新时间 | - |

### 5.3 配置集合 (`config`)

| 字段名 | 类型 | 描述 | 索引 |
|-------|------|------|------|
| `_id` | `ObjectId` | 配置 ID | 主键 |
| `userId` | `String` | 用户 ID | 唯一 |
| `reimburseTypes` | `Array<String>` | 报销类型列表 | - |
| `payTypes` | `Array<String>` | 支付类型列表 | - |
| `updatedAt` | `Date` | 更新时间 | - |

## 6. API 设计

### 6.1 认证 API

#### POST /api/auth/register
- **功能**: 用户注册
- **请求体**: `{ username, email, password }`
- **响应**: `{ success: boolean, message: string, user?: object, token?: string }`

#### POST /api/auth/login
- **功能**: 用户登录
- **请求体**: `{ username, password }`
- **响应**: `{ success: boolean, message: string, user?: object, token?: string }`

#### POST /api/auth/change-password
- **功能**: 修改密码
- **请求体**: `{ currentPassword, newPassword }`
- **响应**: `{ success: boolean, message: string }`

### 6.2 支出 API

#### GET /api/expenses
- **功能**: 获取支出列表
- **查询参数**: `page`, `limit`, `startDate`, `endDate`, `reimburseType`, `payType`
- **响应**: `{ expenses: Array, total: number, page: number, limit: number }`

#### POST /api/expenses
- **功能**: 创建支出记录
- **请求体**: `{ amount, reimburseType, reimburseAmount, payType, date, other }`
- **响应**: `{ success: boolean, message: string, expenseId?: string }`

#### GET /api/expenses/[id]
- **功能**: 获取单个支出记录
- **响应**: `{ expense: object }`

#### PUT /api/expenses/[id]
- **功能**: 更新支出记录
- **请求体**: `{ amount, reimburseType, reimburseAmount, payType, date, other }`
- **响应**: `{ success: boolean, message: string }`

#### DELETE /api/expenses/[id]
- **功能**: 删除支出记录
- **响应**: `{ success: boolean, message: string }`

#### GET /api/expenses/stats
- **功能**: 获取支出统计
- **查询参数**: `startDate`, `endDate`, `reimburseType`, `payType`
- **响应**: `{ totalExpense: number, pendingReimburse: number, reimbursed: number, balance: number }`

#### GET /api/expenses/export
- **功能**: 导出支出记录为 Excel
- **查询参数**: `startDate`, `endDate`, `reimburseType`, `payType`
- **响应**: Excel 文件下载

### 6.3 配置 API

#### GET /api/config
- **功能**: 获取用户配置
- **响应**: `{ reimburseTypes: Array, payTypes: Array }`

#### PUT /api/config
- **功能**: 更新用户配置
- **请求体**: `{ reimburseTypes, payTypes }`
- **响应**: `{ success: boolean, message: string, config?: object }`

## 7. 前端页面设计

### 7.1 首页 (`/`)

#### 功能点
- 支出统计卡片展示
- 支出列表展示
- 筛选条件设置
- 操作按钮（新增支出、导出 Excel）
- 最近支出记录
- 登录失效自动处理

#### 布局
- 顶部导航栏（包含品牌、用户信息、设置、登出）
- 筛选条件区域
- 统计卡片区域
- 操作按钮区域
- 支出列表区域
- 分页控件
- 错误提示区域（用于显示登录失效等错误信息）

### 7.2 新增支出页面 (`/add-expense`)

#### 功能点
- 支出金额输入
- 报销类型选择
- 报销金额输入（仅已报销类型）
- 支付类型选择
- 日期选择
- 备注输入
- 登录失效自动处理

#### 布局
- 顶部导航栏
- 表单区域
- 提交和取消按钮
- 错误提示区域（用于显示登录失效等错误信息）

### 7.3 编辑支出页面 (`/edit-expense/[id]`)

#### 功能点
- 支出信息编辑
- 删除支出功能
- 保存和取消按钮
- 登录失效自动处理

#### 布局
- 顶部导航栏
- 表单区域
- 操作按钮区域（删除、取消、保存）
- 错误提示区域（用于显示登录失效等错误信息）

### 7.4 设置页面 (`/settings`)

#### 功能点
- 报销类型管理（添加、编辑、删除）
- 支付类型管理（添加、编辑、删除）
- 密码修改功能
- 登录失效自动处理

#### 布局
- 顶部导航栏
- 配置管理区域
- 密码修改区域
- 错误提示区域（用于显示登录失效等错误信息）

### 7.5 登录页面 (`/login`)

#### 功能点
- 邮箱输入
- 密码输入
- 登录按钮
- 注册链接

#### 布局
- 登录表单
- 品牌展示
- 错误提示

### 7.6 注册页面 (`/register`)

#### 功能点
- 用户名输入
- 邮箱输入
- 密码输入
- 注册按钮
- 登录链接

#### 布局
- 注册表单
- 品牌展示
- 错误提示

## 8. 响应式设计

### 设计原则
- 移动优先设计
- 断点设置：
  - 移动设备: < 640px
  - 平板设备: 640px - 1024px
  - 桌面设备: > 1024px

### 具体实现
- 使用 Tailwind CSS 的响应式类
- 在移动设备上：
  - 统计卡片每行显示 2 个
  - 筛选条件垂直堆叠
  - 操作按钮垂直堆叠
- 在桌面设备上：
  - 统计卡片每行显示 4 个
  - 筛选条件水平排列
  - 操作按钮水平排列

## 9. 性能优化

### 9.1 前端优化

- 使用 Next.js 静态生成和服务器端渲染
- 组件懒加载
- 图片优化
- 代码分割
- 使用 `useCallback` 和 `useMemo` 优化渲染

### 9.2 后端优化

- 数据库索引优化
- API 响应缓存
- 批量操作优化
- 分页加载

### 9.3 数据库优化

- 合理使用索引
- 聚合查询优化
- 定期清理过期数据

## 10. 安全措施

### 10.1 前端安全

- 输入验证
- XSS 防护
- CSRF 防护
- 敏感信息保护

### 10.2 后端安全

- 密码加密存储
- JWT 令牌验证
- API 访问控制
- 防 SQL 注入
- 速率限制

### 10.3 部署安全

- HTTPS 配置
- 环境变量管理
- 依赖包安全扫描
- 定期安全更新

## 11. 部署方案

### 11.1 Vercel 部署

#### 步骤
1. 连接 GitHub 仓库到 Vercel
2. 配置环境变量（MongoDB 连接字符串、JWT 密钥等）
3. 配置构建命令和输出目录
4. 部署到 Vercel

#### 环境变量
- `MONGODB_URI`: MongoDB 连接字符串
- `JWT_SECRET`: JWT 签名密钥
- `JWT_EXPIRES_IN`: JWT 过期时间

### 11.2 Docker 部署

#### 步骤
1. 创建 Dockerfile
2. 创建 docker-compose.yml
3. 构建和运行容器

#### Dockerfile 示例
```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npm run build

EXPOSE 3000

CMD ["npm", "start"]
```

## 12. 开发流程

### 12.1 本地开发

#### 步骤
1. 克隆仓库
2. 安装依赖：`npm install`
3. 配置环境变量
4. 启动开发服务器：`npm run dev`
5. 访问 `http://localhost:3000`

### 12.2 代码规范

#### 规范要求
- 使用 TypeScript 类型定义
- 遵循 ESLint 规则
- 使用 Prettier 格式化代码
- 提交前运行类型检查和 lint

#### 脚本命令
- `npm run dev`: 启动开发服务器
- `npm run build`: 构建生产版本
- `npm run lint`: 运行 ESLint
- `npm run typecheck`: 运行 TypeScript 类型检查
- `npm run format`: 运行 Prettier 格式化

## 13. 测试策略

### 13.1 单元测试

#### 测试框架
- Jest
- React Testing Library

#### 测试范围
- 工具函数
- 组件渲染
- 表单验证

### 13.2 集成测试

#### 测试范围
- API 端点测试
- 数据库操作测试
- 认证流程测试

### 13.3 E2E 测试

#### 测试框架
- Cypress

#### 测试范围
- 用户登录流程
- 支出记录管理流程
- 筛选功能测试
- 导出功能测试

## 14. 未来扩展

### 14.1 功能扩展

- **预算管理**: 支持设置月度/年度预算
- **分类管理**: 支持支出分类
- **图表分析**: 集成更丰富的图表展示
- **多设备同步**: 支持数据云同步
- **深色模式**: 支持深色主题
- **数据导入**: 支持从其他应用导入数据

### 14.2 技术扩展

- **PWA 支持**: 实现渐进式 Web 应用
- **WebSockets**: 实现实时数据更新
- **GraphQL**: 可选迁移到 GraphQL API
- **Server Components**: 充分利用 Next.js Server Components
- **AI 分析**: 集成 AI 进行支出分析和建议

## 15. 总结

EasyExp 简易账本项目采用现代化的技术栈和架构设计，提供了完整的个人支出管理功能。通过本开发框架方案，我们可以构建一个功能完善、性能优秀、用户体验良好的支出管理应用。该方案考虑了项目的可扩展性和可维护性，为未来的功能扩展和技术升级预留了空间。

项目的核心价值在于帮助用户更好地管理个人财务，通过直观的界面和强大的功能，让用户能够轻松追踪支出、分析消费模式，并做出更明智的财务决策。