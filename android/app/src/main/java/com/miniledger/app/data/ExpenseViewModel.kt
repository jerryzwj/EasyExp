package com.miniledger.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import android.util.Log
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ExpenseViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _stats = MutableStateFlow(
        ExpenseStatsResponse(
            totalExpense = 0.0,
            pendingReimburse = 0.0,
            reimbursed = 0.0,
            balance = 0.0
        )
    )
    val stats: StateFlow<ExpenseStatsResponse> = _stats

    private val _expenses = MutableStateFlow(emptyList<Expense>())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _config = MutableStateFlow(
        ConfigResponse(
            reimburseTypes = listOf("待报销", "报销中", "已报销"),
            payTypes = listOf("微信", "支付宝", "现金", "网银")
        )
    )
    val config: StateFlow<ConfigResponse> = _config

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _filters = MutableStateFlow(
        ExpenseFilters(
            dateRange = "本年度",
            startDate = "",
            endDate = "",
            reimburseType = "全部",
            payType = "全部",
            page = 1,
            limit = 10
        )
    )
    val filters: StateFlow<ExpenseFilters> = _filters

    fun updateFilters(newFilters: ExpenseFilters) {
        // 处理日期范围，转换为实际的startDate和endDate
        val processedFilters = processDateRange(newFilters)
        _filters.value = processedFilters
        loadExpenses()
        loadStats()
    }

    private fun processDateRange(filters: ExpenseFilters): ExpenseFilters {
        val startDateCalendar = java.util.Calendar.getInstance()
        val endDateCalendar = java.util.Calendar.getInstance()
        endDateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        endDateCalendar.set(java.util.Calendar.MINUTE, 59)
        endDateCalendar.set(java.util.Calendar.SECOND, 59)
        endDateCalendar.set(java.util.Calendar.MILLISECOND, 999)

        val startDate: String
        val endDate: String

        when (filters.dateRange) {
            "本年度" -> {
                startDateCalendar.set(java.util.Calendar.MONTH, 0)
                startDateCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                startDateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startDateCalendar.set(java.util.Calendar.MINUTE, 0)
                startDateCalendar.set(java.util.Calendar.SECOND, 0)
                startDateCalendar.set(java.util.Calendar.MILLISECOND, 0)
                startDate = formatDate(startDateCalendar.time)
                endDate = formatDate(endDateCalendar.time)
            }
            "本月" -> {
                startDateCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                startDateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startDateCalendar.set(java.util.Calendar.MINUTE, 0)
                startDateCalendar.set(java.util.Calendar.SECOND, 0)
                startDateCalendar.set(java.util.Calendar.MILLISECOND, 0)
                startDate = formatDate(startDateCalendar.time)
                endDate = formatDate(endDateCalendar.time)
            }
            "本周" -> {
                val dayOfWeek = startDateCalendar.get(java.util.Calendar.DAY_OF_WEEK)
                val daysToSubtract = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else dayOfWeek - 2
                startDateCalendar.add(java.util.Calendar.DAY_OF_MONTH, -daysToSubtract)
                startDateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startDateCalendar.set(java.util.Calendar.MINUTE, 0)
                startDateCalendar.set(java.util.Calendar.SECOND, 0)
                startDateCalendar.set(java.util.Calendar.MILLISECOND, 0)
                startDate = formatDate(startDateCalendar.time)
                endDate = formatDate(endDateCalendar.time)
            }
            "上月" -> {
                startDateCalendar.add(java.util.Calendar.MONTH, -1)
                startDateCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                startDateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startDateCalendar.set(java.util.Calendar.MINUTE, 0)
                startDateCalendar.set(java.util.Calendar.SECOND, 0)
                startDateCalendar.set(java.util.Calendar.MILLISECOND, 0)
                
                val lastDayOfMonth = startDateCalendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                endDateCalendar.add(java.util.Calendar.MONTH, -1)
                endDateCalendar.set(java.util.Calendar.DAY_OF_MONTH, lastDayOfMonth)
                endDateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                endDateCalendar.set(java.util.Calendar.MINUTE, 59)
                endDateCalendar.set(java.util.Calendar.SECOND, 59)
                endDateCalendar.set(java.util.Calendar.MILLISECOND, 999)
                
                startDate = formatDate(startDateCalendar.time)
                endDate = formatDate(endDateCalendar.time)
            }
            else -> {
                // 全部或自定义，使用原始的startDate和endDate
                startDate = filters.startDate
                endDate = filters.endDate
            }
        }

        return filters.copy(
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun formatDate(date: java.util.Date): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(date)
    }

    fun loadExpenses() {
        GlobalScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "获取支出列表失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                val params = buildParams()
                println("开始获取支出列表，token: $token, params: $params")
                val response = NetworkModule.apiService.getExpenses(bearerToken, params)
                println("获取支出列表成功，支出数量: ${response.expenses.size}")
                _expenses.value = response.expenses
            } catch (e: retrofit2.HttpException) {
                println("获取支出列表失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "获取支出列表失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("获取支出列表失败: 网络超时，请检查网络连接")
                _error.value = "获取支出列表失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("获取支出列表失败: 无法连接到服务器，请检查网络连接")
                _error.value = "获取支出列表失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("获取支出列表失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "获取支出列表失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "获取统计数据失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                val params = buildParams().filterKeys { it != "page" && it != "limit" }
                println("开始获取支出统计，token: $token, params: $params")
                val response = NetworkModule.apiService.getExpenseStats(bearerToken, params)
                println("获取支出统计成功，总支出: ${response.totalExpense}")
                _stats.value = response
            } catch (e: retrofit2.HttpException) {
                println("获取统计数据失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "获取统计数据失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("获取统计数据失败: 网络超时，请检查网络连接")
                _error.value = "获取统计数据失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("获取统计数据失败: 无法连接到服务器，请检查网络连接")
                _error.value = "获取统计数据失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("获取统计数据失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "获取统计数据失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadConfig() {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "获取配置失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始获取配置，token: $token")
                val response = NetworkModule.apiService.getConfig(bearerToken)
                println("获取配置成功，报销类型数量: ${response.reimburseTypes.size}, 支付类型数量: ${response.payTypes.size}")
                _config.value = response
            } catch (e: retrofit2.HttpException) {
                println("获取配置失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "获取配置失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("获取配置失败: 网络超时，请检查网络连接")
                _error.value = "获取配置失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("获取配置失败: 无法连接到服务器，请检查网络连接")
                _error.value = "获取配置失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("获取配置失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "获取配置失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildParams(): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val filters = _filters.value

        // 使用已经转换好的startDate和endDate
        if (filters.startDate.isNotEmpty()) {
            params["startDate"] = filters.startDate
        }
        if (filters.endDate.isNotEmpty()) {
            params["endDate"] = filters.endDate
        }
        if (filters.reimburseType != "全部") {
            params["reimburseType"] = filters.reimburseType
        }
        if (filters.payType != "全部") {
            params["payType"] = filters.payType
        }
        params["page"] = filters.page.toString()
        params["limit"] = filters.limit.toString()

        return params
    }

    fun addExpense(
        amount: Double,
        reimburseType: String,
        reimburseAmount: Double?,
        payType: String,
        date: String,
        other: String?
    ) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "添加支出失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                val expense = com.miniledger.app.data.Expense(
                    _id = "", // 新增支出时ID由服务器生成
                    amount = amount,
                    reimburseType = reimburseType,
                    reimburseAmount = reimburseAmount,
                    payType = payType,
                    date = date,
                    other = other
                )
                println("开始添加支出，token: $token, expense: $expense")
                val response = NetworkModule.apiService.addExpense(bearerToken, expense)
                println("添加支出成功，expenseId: ${response._id}")
                // 添加成功后重新加载支出列表和统计数据
                loadExpenses()
                loadStats()
            } catch (e: retrofit2.HttpException) {
                println("添加支出失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "添加支出失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("添加支出失败: 网络超时，请检查网络连接")
                _error.value = "添加支出失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("添加支出失败: 无法连接到服务器，请检查网络连接")
                _error.value = "添加支出失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("添加支出失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "添加支出失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExpense(
        expenseId: String,
        amount: Double,
        reimburseType: String,
        reimburseAmount: Double?,
        payType: String,
        date: String,
        other: String?
    ) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "更新支出失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                val expense = com.miniledger.app.data.Expense(
                    _id = expenseId,
                    amount = amount,
                    reimburseType = reimburseType,
                    reimburseAmount = reimburseAmount,
                    payType = payType,
                    date = date,
                    other = other
                )
                println("开始更新支出，token: $token, expenseId: $expenseId, expense: $expense")
                val response = NetworkModule.apiService.updateExpense(bearerToken, expenseId, expense)
                println("更新支出成功，expenseId: ${response._id}")
                // 更新成功后重新加载支出列表和统计数据
                loadExpenses()
                loadStats()
            } catch (e: retrofit2.HttpException) {
                println("更新支出失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "更新支出失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("更新支出失败: 网络超时，请检查网络连接")
                _error.value = "更新支出失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("更新支出失败: 无法连接到服务器，请检查网络连接")
                _error.value = "更新支出失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("更新支出失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "更新支出失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "删除支出失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始删除支出，token: $token, expenseId: $expenseId")
                NetworkModule.apiService.deleteExpense(bearerToken, expenseId)
                println("删除支出成功，expenseId: $expenseId")
                // 删除成功后重新加载支出列表和统计数据
                loadExpenses()
                loadStats()
            } catch (e: retrofit2.HttpException) {
                println("删除支出失败: HTTP错误 ${e.code()} - ${e.message}")
                _error.value = "删除支出失败: HTTP错误 ${e.code()} - ${e.message}"
            } catch (e: java.net.SocketTimeoutException) {
                println("删除支出失败: 网络超时，请检查网络连接")
                _error.value = "删除支出失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("删除支出失败: 无法连接到服务器，请检查网络连接")
                _error.value = "删除支出失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("删除支出失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "删除支出失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 报销类型管理
    fun addReimburseType(typeName: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "添加报销类型失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始添加报销类型，token: $token, typeName: $typeName")
                // 获取当前配置
                val currentConfig = _config.value
                // 创建新的报销类型列表
                val newReimburseTypes = currentConfig.reimburseTypes.toMutableList()
                newReimburseTypes.add(typeName)
                // 更新配置
                NetworkModule.apiService.updateConfig(
                    bearerToken,
                    ConfigRequest("reimburseType", newReimburseTypes)
                )
                println("添加报销类型成功")
                // 添加成功后重新加载配置
                loadConfig()
                onSuccess?.invoke()
            } catch (e: retrofit2.HttpException) {
                println("添加报销类型失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "添加报销类型失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("添加报销类型失败: 网络超时，请检查网络连接")
                _error.value = "添加报销类型失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("添加报销类型失败: 无法连接到服务器，请检查网络连接")
                _error.value = "添加报销类型失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("添加报销类型失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "添加报销类型失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReimburseType(oldTypeName: String, newTypeName: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "更新报销类型失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始更新报销类型，token: $token, oldTypeName: $oldTypeName, newTypeName: $newTypeName")
                // 获取当前配置
                val currentConfig = _config.value
                // 创建新的报销类型列表
                val newReimburseTypes = currentConfig.reimburseTypes.toMutableList()
                val index = newReimburseTypes.indexOf(oldTypeName)
                if (index != -1) {
                    newReimburseTypes[index] = newTypeName
                    // 更新配置
                    NetworkModule.apiService.updateConfig(
                        bearerToken,
                        ConfigRequest("reimburseType", newReimburseTypes)
                    )
                    println("更新报销类型成功")
                    // 更新成功后重新加载配置
                    loadConfig()
                    onSuccess?.invoke()
                } else {
                    _error.value = "更新报销类型失败: 类型不存在"
                }
            } catch (e: retrofit2.HttpException) {
                println("更新报销类型失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "更新报销类型失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("更新报销类型失败: 网络超时，请检查网络连接")
                _error.value = "更新报销类型失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("更新报销类型失败: 无法连接到服务器，请检查网络连接")
                _error.value = "更新报销类型失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("更新报销类型失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "更新报销类型失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteReimburseType(typeName: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "删除报销类型失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始删除报销类型，token: $token, typeName: $typeName")
                // 获取当前配置
                val currentConfig = _config.value
                // 创建新的报销类型列表
                val newReimburseTypes = currentConfig.reimburseTypes.toMutableList()
                if (newReimburseTypes.remove(typeName)) {
                    // 更新配置
                    NetworkModule.apiService.updateConfig(
                        bearerToken,
                        ConfigRequest("reimburseType", newReimburseTypes)
                    )
                    println("删除报销类型成功")
                    // 删除成功后重新加载配置
                    loadConfig()
                    onSuccess?.invoke()
                } else {
                    _error.value = "删除报销类型失败: 类型不存在"
                }
            } catch (e: retrofit2.HttpException) {
                println("删除报销类型失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "删除报销类型失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("删除报销类型失败: 网络超时，请检查网络连接")
                _error.value = "删除报销类型失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("删除报销类型失败: 无法连接到服务器，请检查网络连接")
                _error.value = "删除报销类型失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("删除报销类型失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "删除报销类型失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 支付类型管理
    fun addPayType(typeName: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "添加支付类型失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始添加支付类型，token: $token, typeName: $typeName")
                // 获取当前配置
                val currentConfig = _config.value
                // 创建新的支付类型列表
                val newPayTypes = currentConfig.payTypes.toMutableList()
                newPayTypes.add(typeName)
                // 更新配置
                NetworkModule.apiService.updateConfig(
                    bearerToken,
                    ConfigRequest("payType", newPayTypes)
                )
                println("添加支付类型成功")
                // 添加成功后重新加载配置
                loadConfig()
                onSuccess?.invoke()
            } catch (e: retrofit2.HttpException) {
                println("添加支付类型失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "添加支付类型失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("添加支付类型失败: 网络超时，请检查网络连接")
                _error.value = "添加支付类型失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("添加支付类型失败: 无法连接到服务器，请检查网络连接")
                _error.value = "添加支付类型失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("添加支付类型失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "添加支付类型失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePayType(oldTypeName: String, newTypeName: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "更新支付类型失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始更新支付类型，token: $token, oldTypeName: $oldTypeName, newTypeName: $newTypeName")
                // 获取当前配置
                val currentConfig = _config.value
                // 创建新的支付类型列表
                val newPayTypes = currentConfig.payTypes.toMutableList()
                val index = newPayTypes.indexOf(oldTypeName)
                if (index != -1) {
                    newPayTypes[index] = newTypeName
                    // 更新配置
                    NetworkModule.apiService.updateConfig(
                        bearerToken,
                        ConfigRequest("payType", newPayTypes)
                    )
                    println("更新支付类型成功")
                    // 更新成功后重新加载配置
                    loadConfig()
                    onSuccess?.invoke()
                } else {
                    _error.value = "更新支付类型失败: 类型不存在"
                }
            } catch (e: retrofit2.HttpException) {
                println("更新支付类型失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "更新支付类型失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("更新支付类型失败: 网络超时，请检查网络连接")
                _error.value = "更新支付类型失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("更新支付类型失败: 无法连接到服务器，请检查网络连接")
                _error.value = "更新支付类型失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("更新支付类型失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "更新支付类型失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePayType(typeName: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val token = authViewModel.token.value
            if (token == null) {
                _error.value = "删除支付类型失败: 未登录"
                return@launch
            }
            val bearerToken = "Bearer $token"
            _isLoading.value = true
            _error.value = null
            try {
                println("开始删除支付类型，token: $token, typeName: $typeName")
                // 获取当前配置
                val currentConfig = _config.value
                // 创建新的支付类型列表
                val newPayTypes = currentConfig.payTypes.toMutableList()
                if (newPayTypes.remove(typeName)) {
                    // 更新配置
                    NetworkModule.apiService.updateConfig(
                        bearerToken,
                        ConfigRequest("payType", newPayTypes)
                    )
                    println("删除支付类型成功")
                    // 删除成功后重新加载配置
                    loadConfig()
                    onSuccess?.invoke()
                } else {
                    _error.value = "删除支付类型失败: 类型不存在"
                }
            } catch (e: retrofit2.HttpException) {
                println("删除支付类型失败: HTTP错误 ${e.code()} - ${e.message()}")
                _error.value = "删除支付类型失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                println("删除支付类型失败: 网络超时，请检查网络连接")
                _error.value = "删除支付类型失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                println("删除支付类型失败: 无法连接到服务器，请检查网络连接")
                _error.value = "删除支付类型失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                println("删除支付类型失败: ${e.javaClass.simpleName} - ${e.message}")
                _error.value = "删除支付类型失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 导出Excel
    fun exportExcel(context: android.content.Context, onSuccess: ((String) -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        Log.i("ExpenseViewModel", "===== 开始导出Excel流程 =====")
        Log.i("ExpenseViewModel", "Context: ${context.javaClass.simpleName}")
        Log.i("ExpenseViewModel", "onSuccess回调: $onSuccess")
        Log.i("ExpenseViewModel", "onError回调: $onError")
        
        // 检查权限
        val hasWritePermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasReadPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        Log.i("ExpenseViewModel", "===== 检查权限 =====")
        Log.i("ExpenseViewModel", "===== 写入权限: $hasWritePermission =====")
        Log.i("ExpenseViewModel", "===== 读取权限: $hasReadPermission =====")
        
        viewModelScope.launch {
            Log.i("ExpenseViewModel", "===== 进入协程 =====")
            val token = authViewModel.token.value
            Log.i("ExpenseViewModel", "===== 获取token: $token =====")
            if (token == null) {
                Log.e("ExpenseViewModel", "===== 导出Excel失败: 未登录 =====")
                _error.value = "导出Excel失败: 未登录"
                onError?.invoke("导出Excel失败: 未登录")
                return@launch
            }
            val bearerToken = "Bearer $token"
            Log.i("ExpenseViewModel", "===== 构建bearerToken: $bearerToken =====")
            _isLoading.value = true
            _error.value = null
            try {
                Log.i("ExpenseViewModel", "===== 开始构建参数 =====")
                // 构建参数，不包含分页信息
                val params = buildParams().filterKeys { it != "page" && it != "limit" }
                Log.i("ExpenseViewModel", "===== 导出Excel参数: $params =====")
                
                Log.i("ExpenseViewModel", "===== 准备调用API =====")
                // 调用API
                val response = NetworkModule.apiService.exportExpenses(bearerToken, params)
                Log.i("ExpenseViewModel", "===== API调用完成，响应码: ${response.code()} =====")
                
                if (response.isSuccessful) {
                    Log.i("ExpenseViewModel", "===== 响应成功 =====")
                    val body = response.body()
                    Log.i("ExpenseViewModel", "===== 响应体: $body =====")
                    if (body != null) {
                        Log.i("ExpenseViewModel", "===== 响应体不为空 =====")
                        // 保存文件到外部存储的下载目录
                        val filename = "简易账本_${System.currentTimeMillis()}.xlsx"
                        
                        // 首先尝试使用公共下载目录
                        val publicDownloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                        Log.i("ExpenseViewModel", "===== 公共下载目录: ${publicDownloadsDir?.absolutePath} =====")
                        
                        // 作为备选，使用应用的外部文件目录
                        val appExternalDir = context.getExternalFilesDir(null)
                        Log.i("ExpenseViewModel", "===== 应用外部文件目录: ${appExternalDir?.absolutePath} =====")
                        
                        // 选择一个可用的目录
                        val targetDir = if (publicDownloadsDir != null && publicDownloadsDir.exists()) {
                            publicDownloadsDir
                        } else if (appExternalDir != null) {
                            appExternalDir
                        } else {
                            // 最后的备选方案：使用缓存目录
                            context.cacheDir
                        }
                        
                        Log.i("ExpenseViewModel", "===== 最终选择的目录: ${targetDir.absolutePath} =====")
                        
                        // 确保目录存在
                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                            Log.i("ExpenseViewModel", "===== 创建目录成功 =====")
                        }
                        
                        val file = java.io.File(targetDir, filename)
                        Log.i("ExpenseViewModel", "===== 准备保存Excel文件到: ${file.absolutePath} =====")
                        
                        try {
                            Log.i("ExpenseViewModel", "===== 开始创建文件输出流 =====")
                            file.outputStream().use {
                                Log.i("ExpenseViewModel", "===== 文件输出流创建成功 =====")
                                val inputStream = body.byteStream()
                                Log.i("ExpenseViewModel", "===== 输入流创建成功 =====")
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                var totalBytesRead = 0L
                                Log.i("ExpenseViewModel", "===== 开始读取数据 =====")
                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    it.write(buffer, 0, bytesRead)
                                    totalBytesRead += bytesRead
                                    Log.i("ExpenseViewModel", "===== 已读取: $totalBytesRead 字节 =====")
                                }
                                Log.i("ExpenseViewModel", "===== 文件写入完成，总字节数: $totalBytesRead =====")
                            }
                            
                            // 为文件添加媒体扫描，使其在文件管理器中可见
                            val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            val contentUri = android.net.Uri.fromFile(file)
                            mediaScanIntent.setData(contentUri)
                            context.sendBroadcast(mediaScanIntent)
                            
                            Log.i("ExpenseViewModel", "===== 导出Excel成功，文件保存至: ${file.absolutePath} =====")
                            onSuccess?.invoke(file.absolutePath)
                        } catch (fileError: Exception) {
                            Log.e("ExpenseViewModel", "===== 保存文件失败: ${fileError.javaClass.simpleName} - ${fileError.message} =====")
                            fileError.printStackTrace()
                            _error.value = "保存文件失败: ${fileError.message}"
                            onError?.invoke("保存文件失败: ${fileError.message}")
                        }
                    } else {
                        Log.e("ExpenseViewModel", "===== 导出Excel失败: 响应体为空 =====")
                        _error.value = "导出Excel失败: 响应体为空"
                        onError?.invoke("导出Excel失败: 响应体为空")
                    }
                } else {
                    Log.e("ExpenseViewModel", "===== 导出Excel失败: HTTP错误 ${response.code()} - ${response.message()} =====")
                    _error.value = "导出Excel失败: HTTP错误 ${response.code()} - ${response.message()}"
                    onError?.invoke("导出Excel失败: HTTP错误 ${response.code()} - ${response.message()}")
                }
            } catch (e: retrofit2.HttpException) {
                Log.e("ExpenseViewModel", "===== 导出Excel失败: HTTP错误 ${e.code()} - ${e.message()} =====")
                e.printStackTrace()
                _error.value = "导出Excel失败: HTTP错误 ${e.code()} - ${e.message()}"
                onError?.invoke("导出Excel失败: HTTP错误 ${e.code()} - ${e.message()}")
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("ExpenseViewModel", "===== 导出Excel失败: 网络超时，请检查网络连接 =====")
                e.printStackTrace()
                _error.value = "导出Excel失败: 网络超时，请检查网络连接"
                onError?.invoke("导出Excel失败: 网络超时，请检查网络连接")
            } catch (e: java.net.UnknownHostException) {
                Log.e("ExpenseViewModel", "===== 导出Excel失败: 无法连接到服务器，请检查网络连接 =====")
                e.printStackTrace()
                _error.value = "导出Excel失败: 无法连接到服务器，请检查网络连接"
                onError?.invoke("导出Excel失败: 无法连接到服务器，请检查网络连接")
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "===== 导出Excel失败: ${e.javaClass.simpleName} - ${e.message} =====")
                e.printStackTrace()
                _error.value = "导出Excel失败: ${e.javaClass.simpleName} - ${e.message}"
                onError?.invoke("导出Excel失败: ${e.javaClass.simpleName} - ${e.message}")
            } finally {
                Log.i("ExpenseViewModel", "===== 导出Excel流程结束 =====")
                _isLoading.value = false
            }
        }
    }
}

data class ExpenseFilters(
    val dateRange: String,
    val startDate: String,
    val endDate: String,
    val reimburseType: String,
    val payType: String,
    val page: Int,
    val limit: Int
)


