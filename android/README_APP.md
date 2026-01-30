# 简易账本 Android 应用开发框架

## 项目概述

简易账本是一款基于 Android 平台的个人财务管理应用，使用 Kotlin 和 Jetpack Compose 开发，采用 MVVM 架构模式。该应用主要功能包括支出记录、报销管理、数据统计、Excel 导出等，为用户提供便捷的个人财务管理工具。

## 技术栈

| 类别 | 技术/框架 | 版本 |
|------|-----------|------|
| 开发语言 | Kotlin | 最新稳定版 |
| UI 框架 | Jetpack Compose | 最新稳定版 |
| 网络请求 | Retrofit | 最新稳定版 |
| 状态管理 | ViewModel + StateFlow | 最新稳定版 |
| 导航 | Navigation Compose | 最新稳定版 |
| 认证 | JWT (Bearer Token) | - |
| 存储 | SharedPreferences (CredentialManager) | - |
| 日期处理 | java.time | - |
| 图标 | Material Icons | - |
| 构建工具 | Gradle | 8.14.3 |

## 项目结构

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/miniledger/app/
│   │   │   │   ├── data/            # 数据层
│   │   │   │   │   ├── ApiService.kt       # API 接口定义
│   │   │   │   │   ├── AuthViewModel.kt     # 认证相关状态管理
│   │   │   │   │   ├── CredentialManager.kt # 凭证管理
│   │   │   │   │   ├── ExpenseViewModel.kt  # 支出相关状态管理
│   │   │   │   │   └── NetworkModule.kt     # 网络模块配置
│   │   │   │   ├── ui/              # UI 层
│   │   │   │   │   ├── navigation/  # 导航相关
│   │   │   │   │   │   └── AppNavigation.kt
│   │   │   │   │   ├── screens/     # 屏幕组件
│   │   │   │   │   │   ├── AddExpenseScreen.kt    # 添加支出
│   │   │   │   │   │   ├── ChangePasswordScreen.kt # 修改密码
│   │   │   │   │   │   ├── ExpenseListScreen.kt   # 支出列表
│   │   │   │   │   │   ├── HomeScreen.kt          # 主页
│   │   │   │   │   │   ├── LoginScreen.kt         # 登录
│   │   │   │   │   │   ├── PayTypesScreen.kt      # 支付类型管理
│   │   │   │   │   │   ├── RegisterScreen.kt      # 注册
│   │   │   │   │   │   ├── ReimburseTypesScreen.kt # 报销类型管理
│   │   │   │   │   │   └── SettingsScreen.kt      # 设置
│   │   │   │   │   └── theme/       # 主题相关
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   └── MainActivity.kt   # 应用入口
│   │   │   ├── res/                 # 资源文件
│   │   │   └── AndroidManifest.xml  # 应用配置
│   └── build.gradle                 # 模块构建配置
├── build.gradle                     # 项目构建配置
└── gradle/                          # Gradle 包装器
```

## 架构设计

### MVVM 架构

```
┌─────────────────┐
│   UI 层 (Compose) │
└────────┬────────┘
         │ 状态观察
┌────────▼────────┐
│  ViewModel 层    │
└────────┬────────┘
         │ 数据请求
┌────────▼────────┐
│  数据层 (Repository) │
└────────┬────────┘
         │ API 调用
┌────────▼────────┐
│   网络层 (Retrofit) │
└─────────────────┘
```

### 核心组件说明

1. **ViewModel**：负责管理 UI 状态，处理业务逻辑，与数据层交互
2. **StateFlow**：用于在 ViewModel 和 UI 之间传递状态
3. **ApiService**：定义所有 API 接口
4. **NetworkModule**：配置 Retrofit 实例，处理网络请求
5. **CredentialManager**：管理用户认证凭证（JWT Token）

## 主要功能模块

### 1. 认证模块

- **登录**：用户登录功能，验证用户名密码
- **注册**：新用户注册功能
- **密码修改**：用户修改密码功能
- **凭证管理**：JWT Token 存储和管理

### 2. 支出管理模块

- **添加支出**：记录新的支出信息
- **支出列表**：查看历史支出记录
- **支出详情**：查看单个支出的详细信息
- **Excel 导出**：将支出数据导出为 Excel 文件

### 3. 配置管理模块

- **报销类型管理**：管理报销类型列表
- **支付类型管理**：管理支付类型列表
- **设置**：应用相关设置

### 4. 统计模块

- **支出统计**：统计支出金额和分类
- **报销统计**：统计报销金额和状态

## API 接口

### 认证相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/change-password` | POST | 修改密码 |

### 支出相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/expenses` | GET | 获取支出列表 |
| `/api/expenses` | POST | 添加支出 |
| `/api/expenses/:id` | PUT | 更新支出 |
| `/api/expenses/:id` | DELETE | 删除支出 |
| `/api/expenses/export` | GET | 导出支出为 Excel |

### 配置相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/config` | GET | 获取配置信息 |
| `/api/config` | PUT | 更新配置信息 |

## 开发环境配置

### 前提条件

- Android Studio 最新版本
- JDK 11 或更高版本
- Android SDK 最新版本
- Kotlin 插件最新版本

### 项目设置

1. **克隆项目**
   ```bash
   git clone <项目地址>
   cd MiniLedger/android
   ```

2. **打开项目**
   - 在 Android Studio 中打开 `android` 目录
   - 等待 Gradle 同步完成

3. **配置 API 基础地址**
   - 在 `NetworkModule.kt` 中配置 `BASE_URL`

4. **构建和运行**
   - 选择目标设备或模拟器
   - 点击运行按钮构建并安装应用

## 常见问题与解决方案

### 1. 网络请求失败

**症状**：API 请求返回错误

**解决方案**：
- 检查网络连接
- 确认 API 基础地址配置正确
- 检查 Token 是否有效（401 错误）
- 检查请求参数是否正确（400 错误）

### 2. Excel 导出失败

**症状**：点击导出按钮无反应或提示错误

**解决方案**：
- 检查存储权限是否已授予
- 确认设备有足够的存储空间
- 检查 API 接口是否正常

### 3. 日期选择器问题

**症状**：自定义日期范围选择不工作

**解决方案**：
- 确认日期格式正确
- 检查日期选择逻辑

### 4. 编译错误

**症状**：构建失败，显示编译错误

**解决方案**：
- 检查依赖版本是否兼容
- 确认所有导入语句正确
- 检查代码语法错误

## 后续维护建议

### 1. 代码规范

- 遵循 Kotlin 代码规范
- 使用 Jetpack Compose 最佳实践
- 保持代码结构清晰，注释完善

### 2. 性能优化

- 使用 `remember` 和 `rememberSaveable` 优化 Composable 性能
- 避免在 Composable 中执行耗时操作
- 使用 `LaunchedEffect` 和 `ViewModelScope` 处理异步操作

### 3. 安全性

- 保护 JWT Token，避免明文存储
- 加密敏感数据
- 验证 API 请求参数

### 4. 测试

- 添加单元测试覆盖核心业务逻辑
- 添加 UI 测试确保界面功能正常
- 定期进行集成测试

### 5. 版本管理

- 遵循语义化版本规范
- 记录版本变更日志
- 定期备份代码

## 依赖管理

### 核心依赖

- **Jetpack Compose**：现代 UI 工具包
- **Retrofit**：网络请求框架
- **ViewModel**：状态管理
- **Navigation Compose**：应用导航
- **Material 3**：设计组件

### 依赖版本管理

依赖版本在 `build.gradle` 文件中管理，建议定期更新到最新稳定版本，以获得更好的性能和安全性。

## 总结

简易账本应用采用现代 Android 开发技术栈，使用 Kotlin 和 Jetpack Compose 构建，遵循 MVVM 架构模式，具有良好的可维护性和扩展性。通过本文档的说明，开发者可以快速了解项目结构和功能，便于后续的维护和扩展。

## 联系方式

如有问题或建议，请联系项目维护者。

---

*最后更新：2026-01-30*
