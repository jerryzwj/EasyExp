package com.miniledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import android.widget.Toast
import com.miniledger.app.data.AuthViewModel
import com.miniledger.app.data.ExpenseViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToExpenseList: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(authViewModel) as T
        }
    })
) {
    Log.i("HomeScreen", "HomeScreen组件被加载")
    val username by authViewModel.username.collectAsState()
    val token by authViewModel.token.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val stats by expenseViewModel.stats.collectAsState()
    val config by expenseViewModel.config.collectAsState()
    val filters by expenseViewModel.filters.collectAsState()
    Log.i("HomeScreen", "获取状态成功，username: $username, token: $token, isAuthenticated: $isAuthenticated")

    // 日期范围下拉菜单状态
    var dateRangeExpanded by remember { mutableStateOf(false) }
    var reimburseTypeExpanded by remember { mutableStateOf(false) }
    var payTypeExpanded by remember { mutableStateOf(false) }
    
    // 自定义日期选择状态
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var selectedStartDate by remember { mutableStateOf<java.util.Date?>(null) }
    var selectedEndDate by remember { mutableStateOf<java.util.Date?>(null) }

    // 初始化数据
    LaunchedEffect(Unit) {
        expenseViewModel.loadConfig()
        expenseViewModel.loadStats()
        expenseViewModel.loadExpenses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部导航栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "简易账本",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "欢迎，$username",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                TextButton(onClick = {
                    println("点击刷新数据按钮，token: $token, isAuthenticated: $isAuthenticated")
                    expenseViewModel.loadConfig()
                    expenseViewModel.loadStats()
                    expenseViewModel.loadExpenses()
                }) {
                    Text("刷新")
                }
                TextButton(onClick = onNavigateToSettings) {
                    Text("设置")
                }
                TextButton(onClick = onLogout) {
                    Text("登出")
                }
            }
        }
        
        // 调试信息


        Spacer(modifier = Modifier.height(16.dp))

        // 筛选条件
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "筛选条件",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 日期范围
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "日期范围",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            TextButton(
                                onClick = { dateRangeExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (filters.dateRange == "自定义" && filters.startDate.isNotEmpty() && filters.endDate.isNotEmpty()) {
                                        "${filters.startDate} 至 ${filters.endDate}"
                                    } else {
                                        filters.dateRange
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = dateRangeExpanded,
                                onDismissRequest = { dateRangeExpanded = false }
                            ) {
                                listOf("全部", "本年度", "本月", "本周", "上月").forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range) },
                                        onClick = {
                                            expenseViewModel.updateFilters(
                                                filters.copy(dateRange = range)
                                            )
                                            dateRangeExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("自定义") },
                                    onClick = {
                                        dateRangeExpanded = false
                                        // 显示日期选择器
                                        showDatePicker = true
                                    }
                                )
                            }
                        }
                    }

                    // 报销类型
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "报销类型",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            TextButton(
                                onClick = { reimburseTypeExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = filters.reimburseType)
                            }
                            DropdownMenu(
                                expanded = reimburseTypeExpanded,
                                onDismissRequest = { reimburseTypeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("全部") },
                                    onClick = {
                                        expenseViewModel.updateFilters(
                                            filters.copy(reimburseType = "全部")
                                        )
                                        reimburseTypeExpanded = false
                                    }
                                )
                                config.reimburseTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            expenseViewModel.updateFilters(
                                                filters.copy(reimburseType = type)
                                            )
                                            reimburseTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 支付类型
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "支付类型",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            TextButton(
                                onClick = { payTypeExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = filters.payType)
                            }
                            DropdownMenu(
                                expanded = payTypeExpanded,
                                onDismissRequest = { payTypeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("全部") },
                                    onClick = {
                                        expenseViewModel.updateFilters(
                                            filters.copy(payType = "全部")
                                        )
                                        payTypeExpanded = false
                                    }
                                )
                                config.payTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            expenseViewModel.updateFilters(
                                                filters.copy(payType = type)
                                            )
                                            payTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }


            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 统计卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(
                title = "支出总额",
                value = stats.totalExpense,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                title = "收支差额",
                value = stats.balance,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(
                title = "待报销金额",
                value = stats.pendingReimburse,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                title = "已报销金额",
                value = stats.reimbursed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 最近支出提示
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "查看完整支出记录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "点击下方按钮，查看完整的支出记录列表",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = onNavigateToExpenseList) {
                        Text("前往支出记录")
                    }
                }
            }
        }
        
        // 日期选择器 - 使用DatePickerDialog，支持开始和结束日期选择
        if (showDatePicker) {
            val datePickerState = androidx.compose.material3.rememberDatePickerState()
            androidx.compose.material3.DatePickerDialog(
                onDismissRequest = { 
                    showDatePicker = false
                    // 重置选择状态
                    isSelectingStartDate = true
                    selectedStartDate = null
                    selectedEndDate = null
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { timestamp ->
                                val selectedDate = java.util.Date(timestamp)
                                
                                if (isSelectingStartDate) {
                                    // 保存开始日期，切换到结束日期选择
                                    selectedStartDate = selectedDate
                                    isSelectingStartDate = false
                                } else {
                                    // 保存结束日期，应用自定义日期范围
                                    selectedEndDate = selectedDate
                                    
                                    // 确保两个日期都已选择
                                    if (selectedStartDate != null && selectedEndDate != null) {
                                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        val startDateStr = dateFormat.format(selectedStartDate!!)
                                        val endDateStr = dateFormat.format(selectedEndDate!!)
                                        
                                        expenseViewModel.updateFilters(
                                            filters.copy(
                                                dateRange = "自定义",
                                                startDate = startDateStr,
                                                endDate = endDateStr
                                            )
                                        )
                                    }
                                    
                                    // 完成选择，关闭对话框
                                    showDatePicker = false
                                    isSelectingStartDate = true
                                    selectedStartDate = null
                                    selectedEndDate = null
                                }
                            }
                        }
                    ) {
                        androidx.compose.material3.Text(if (isSelectingStartDate) "下一步" else "确定")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showDatePicker = false
                            // 重置选择状态
                            isSelectingStartDate = true
                            selectedStartDate = null
                            selectedEndDate = null
                        }
                    ) {
                        androidx.compose.material3.Text("取消")
                    }
                }
            ) {
                androidx.compose.material3.DatePicker(
                    state = datePickerState
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "¥${value.formatAsCurrency(2)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 扩展函数，用于格式化数字
fun Double.formatAsCurrency(digits: Int): String {
    val format = "%.${digits}f"
    return String.format(format, this)
}
