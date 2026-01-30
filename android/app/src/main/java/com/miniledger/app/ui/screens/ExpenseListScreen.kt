package com.miniledger.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import android.widget.Toast
import com.miniledger.app.data.AuthViewModel
import com.miniledger.app.data.ExpenseViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel,
    expenseViewModel: ExpenseViewModel
) {
    val context = LocalContext.current
    val username by authViewModel.username.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val error by expenseViewModel.error.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()
    val filters by expenseViewModel.filters.collectAsState()
    val config by expenseViewModel.config.collectAsState()

    // 日期范围下拉菜单状态
    var dateRangeExpanded by remember { mutableStateOf(false) }
    var reimburseTypeExpanded by remember { mutableStateOf(false) }
    var payTypeExpanded by remember { mutableStateOf(false) }
    
    // 自定义日期选择状态
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var selectedStartDate by remember { mutableStateOf<java.util.Date?>(null) }
    var selectedEndDate by remember { mutableStateOf<java.util.Date?>(null) }

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
                text = "支出记录",
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
                TextButton(onClick = onNavigateToHome) {
                    Text("统计")
                }
                TextButton(onClick = onNavigateToSettings) {
                    Text("设置")
                }
                TextButton(onClick = onLogout) {
                    Text("登出")
                }
            }
        }

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
                                // 从config中获取报销类型列表
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
                                // 从config中获取支付类型列表
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

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onNavigateToAddExpense) {
                Text("新增支出")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                Log.i("ExpenseListScreen", "点击导出Excel按钮")
                try {
                    Log.i("ExpenseListScreen", "准备调用expenseViewModel.exportExcel")
                    expenseViewModel.exportExcel(
                        context = context,
                        onSuccess = {
                            Log.i("ExpenseListScreen", "Excel导出成功: $it")
                            // 显示导出成功的Toast提示
                            Toast.makeText(context, "Excel导出成功，文件已保存到下载目录", Toast.LENGTH_LONG).show()
                            // 在实际应用中，这里可以添加文件分享或打开的逻辑
                        },
                        onError = {
                            Log.e("ExpenseListScreen", "Excel导出失败: $it")
                            // 显示导出失败的Toast提示
                            Toast.makeText(context, "Excel导出失败: $it", Toast.LENGTH_LONG).show()
                        }
                    )
                    Log.i("ExpenseListScreen", "调用expenseViewModel.exportExcel完成")
                } catch (e: Exception) {
                    Log.e("ExpenseListScreen", "点击导出Excel按钮失败: ${e.javaClass.simpleName} - ${e.message}")
                    e.printStackTrace()
                    // 显示异常的Toast提示
                    Toast.makeText(context, "导出Excel时发生错误: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }) {
                Text("导出Excel")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 支出列表
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "支出记录",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error!!,
                        color = Color.Red
                    )
                }
            } else if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无支出记录")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top
                ) {
                    items(expenses) {
                        ExpenseItem(
                            expense = it,
                            onEdit = onNavigateToEditExpense
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
fun ExpenseItem(expense: com.miniledger.app.data.Expense, onEdit: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.reimburseType == "已报销") {
                Color(0xFFE8F5E8) // 浅绿色背景，与已报销的深绿色文本相呼应
            } else {
                CardDefaults.cardColors().containerColor
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp) // 进一步减少内边距
        ) {
            // 左侧主要内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 第一行：日期、报销类型、支付类型、支出金额
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.date.split('T')[0], // 只显示日期部分，不显示时间
                            fontSize = 11.sp, // 减小字体大小
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = expense.reimburseType,
                            fontSize = 10.sp, // 减小字体大小
                            color = when (expense.reimburseType) {
                                "已报销" -> Color(0xFF2E7D32) // 深绿色，更柔和
                                "待报销" -> Color(0xFFEF6C00) // 橙色，替代黄色
                                else -> Color(0xFF1565C0) // 深蓝色，更柔和
                            },
                            modifier = Modifier.padding(end = 6.dp) // 减少间距
                        )
                        Text(
                            text = expense.payType,
                            fontSize = 10.sp, // 减小字体大小
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "¥${expense.amount.formatAsCurrency(2)}",
                        fontSize = 13.sp, // 字体大小
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828), // 深红色，更柔和
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                // 第二行：备注、实际报销金额
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (expense.other != null) {
                        Text(
                            text = "备注: ${expense.other}",
                            fontSize = 10.sp, // 减小字体大小
                            color = Color.Gray
                        )
                    } else {
                        Text("", fontSize = 10.sp)
                    }
                    if (expense.reimburseAmount != null) {
                        Text(
                            text = "报销: ¥${expense.reimburseAmount.formatAsCurrency(2)}",
                            fontSize = 10.sp, // 字体大小
                            color = Color(0xFF2E7D32), // 深绿色，更柔和
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
            
            // 右侧：编辑按钮
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            ) {
                TextButton(
                    onClick = { onEdit(expense._id) },
                    modifier = Modifier.height(32.dp) // 调整按钮高度
                ) {
                    Text("编辑", fontSize = 9.sp) // 减小字体大小
                }
            }
        }
    }
}


