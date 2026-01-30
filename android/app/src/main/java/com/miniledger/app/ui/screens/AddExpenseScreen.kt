package com.miniledger.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.miniledger.app.data.AuthViewModel
import com.miniledger.app.data.ExpenseViewModel

@Composable
fun AddExpenseScreen(
    onNavigateToExpenseList: () -> Unit,
    expenseId: String? = null,
    authViewModel: AuthViewModel,
    expenseViewModel: ExpenseViewModel
) {
    val username by authViewModel.username.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val error by expenseViewModel.error.collectAsState()
    val config by expenseViewModel.config.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()

    // 表单状态
    var amount by remember { mutableStateOf("") }
    var reimburseType by remember { mutableStateOf("待报销") }
    var reimburseAmount by remember { mutableStateOf("") }
    var payType by remember { mutableStateOf("微信") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_DATE)) }
    var other by remember { mutableStateOf("") }

    // 加载待编辑的支出记录数据
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            // 从expenses列表中查找对应的支出记录
            val expense = expenses.find { it._id == expenseId }
            if (expense != null) {
                // 填充表单数据
                amount = expense.amount.toString()
                reimburseType = expense.reimburseType
                reimburseAmount = expense.reimburseAmount?.toString() ?: ""
                payType = expense.payType
                // 处理日期格式，确保只保留日期部分
                val expenseDate = expense.date
                date = try {
                    // 尝试解析标准日期格式 (YYYY-MM-DD)
                    LocalDate.parse(expenseDate, DateTimeFormatter.ISO_DATE).format(DateTimeFormatter.ISO_DATE)
                } catch (e: Exception) {
                    try {
                        // 尝试解析包含时间的日期格式 (YYYY-MM-DDTHH:MM:SS.SSSZ)
                        val instant = java.time.Instant.parse(expenseDate)
                        instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ISO_DATE)
                    } catch (e: Exception) {
                        // 如果都失败了，使用当前日期
                        LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    }
                }
                other = expense.other ?: ""
            }
        }
    }

    // 下拉菜单状态
    var reimburseTypeExpanded by remember { mutableStateOf(false) }
    var payTypeExpanded by remember { mutableStateOf(false) }
    var datePickerVisible by remember { mutableStateOf(false) }

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
                text = if (expenseId == null) "新增支出" else "编辑支出",
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
                TextButton(onClick = onNavigateToExpenseList) {
                    Text("返回")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 表单区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "支出信息",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 金额
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // 报销类型
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = reimburseType,
                        onValueChange = {},
                        label = { Text("报销类型") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { reimburseTypeExpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { reimburseTypeExpanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "展开菜单")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = reimburseTypeExpanded,
                        onDismissRequest = { reimburseTypeExpanded = false }
                    ) {
                        config.reimburseTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    reimburseType = type
                                    reimburseTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // 报销金额（仅当报销类型为已报销时显示）
                if (reimburseType == "已报销") {
                    OutlinedTextField(
                        value = reimburseAmount,
                        onValueChange = { reimburseAmount = it },
                        label = { Text("报销金额") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // 支付类型
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = payType,
                        onValueChange = {},
                        label = { Text("支付类型") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { payTypeExpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { payTypeExpanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "展开菜单")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = payTypeExpanded,
                        onDismissRequest = { payTypeExpanded = false }
                    ) {
                        config.payTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    payType = type
                                    payTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // 日期
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("日期 (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerVisible = true }) {
                            Text("选择")
                        }
                    }
                )
                
                // 日期选择器
                if (datePickerVisible) {
                    var selectedDate by remember {
                        mutableStateOf(
                            if (date.isNotEmpty()) {
                                try {
                                    // 尝试解析标准日期格式 (YYYY-MM-DD)
                                    LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
                                } catch (e: Exception) {
                                    try {
                                        // 尝试解析包含时间的日期格式 (YYYY-MM-DDTHH:MM:SS.SSSZ)
                                        val instant = java.time.Instant.parse(date)
                                        instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                    } catch (e: Exception) {
                                        // 如果都失败了，使用当前日期
                                        LocalDate.now()
                                    }
                                }
                            } else {
                                LocalDate.now()
                            }
                        )
                    }
                    
                    var currentMonth by remember {
                        mutableStateOf(selectedDate.monthValue)
                    }
                    
                    var currentYear by remember {
                        mutableStateOf(selectedDate.year)
                    }
                    
                    AlertDialog(
                        onDismissRequest = { datePickerVisible = false },
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { 
                                    if (currentMonth == 1) {
                                        currentMonth = 12
                                        currentYear--
                                    } else {
                                        currentMonth--
                                    }
                                }) {
                                    Text("<")
                                }
                                Text("${currentYear} ${getMonthName(currentMonth)}")
                                IconButton(onClick = { 
                                    if (currentMonth == 12) {
                                        currentMonth = 1
                                        currentYear++
                                    } else {
                                        currentMonth++
                                    }
                                }) {
                                    Text(">")
                                }
                            }
                        },
                        text = {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // 星期标题
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                                        Text(
                                            text = it,
                                            modifier = Modifier
                                                .weight(1f)
                                                .wrapContentWidth(Alignment.CenterHorizontally),
                                            color = Color.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                
                                // 日历网格
                                val daysInMonth = LocalDate.of(currentYear, currentMonth, 1).lengthOfMonth()
                                val firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1).dayOfWeek.value
                                
                                val calendarDays = mutableListOf<Int>()
                                // 添加空白日期
                                for (i in 0 until firstDayOfMonth) {
                                    calendarDays.add(0)
                                }
                                // 添加月份天数
                                for (i in 1..daysInMonth) {
                                    calendarDays.add(i)
                                }
                                // 确保日历网格完整（6行7列）
                                while (calendarDays.size < 42) {
                                    calendarDays.add(0)
                                }
                                
                                // 显示日历网格
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    for (week in 0 until 6) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            for (day in 0 until 7) {
                                                val index = week * 7 + day
                                                if (index < calendarDays.size) {
                                                    val dayValue = calendarDays[index]
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(1f)
                                                    ) {
                                                        if (dayValue > 0) {
                                                            val dateToCheck = LocalDate.of(currentYear, currentMonth, dayValue)
                                                            Surface(
                                                                onClick = { selectedDate = dateToCheck },
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .padding(4.dp),
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = if (dateToCheck == selectedDate) {
                                                                    Color.Blue
                                                                } else if (dateToCheck == LocalDate.now()) {
                                                                    Color.Green
                                                                } else {
                                                                    Color.White
                                                                }
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = dayValue.toString(),
                                                                        color = if (dateToCheck == selectedDate) {
                                                                            Color.White
                                                                        } else if (dateToCheck == LocalDate.now()) {
                                                                            Color.White
                                                                        } else {
                                                                            Color.Black
                                                                        },
                                                                        fontSize = 16.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    date = selectedDate.format(DateTimeFormatter.ISO_DATE)
                                    datePickerVisible = false
                                }
                            ) {
                                Text("确定")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { datePickerVisible = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                // 备注
                OutlinedTextField(
                    value = other,
                    onValueChange = { other = it },
                    label = { Text("备注") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // 错误信息
                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // 提交按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onNavigateToExpenseList,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("取消")
                    }
                    
                    if (expenseId != null) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                // 删除支出
                                expenseViewModel.deleteExpense(expenseId)
                                // 删除后返回支出列表页面
                                onNavigateToExpenseList()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text("删除")
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            // 实现新增或编辑支出的逻辑
                            val amountValue = amount.toDoubleOrNull()
                            val reimburseAmountValue = if (reimburseAmount.isNotEmpty()) reimburseAmount.toDoubleOrNull() else null
                            
                            if (amountValue == null) {
                                // 金额输入无效
                                return@Button
                            }
                            
                            if (expenseId == null) {
                                // 新增支出
                                expenseViewModel.addExpense(
                                    amount = amountValue,
                                    reimburseType = reimburseType,
                                    reimburseAmount = reimburseAmountValue,
                                    payType = payType,
                                    date = date,
                                    other = if (other.isNotEmpty()) other else null
                                )
                            } else {
                                // 编辑支出
                                expenseViewModel.updateExpense(
                                    expenseId = expenseId,
                                    amount = amountValue,
                                    reimburseType = reimburseType,
                                    reimburseAmount = reimburseAmountValue,
                                    payType = payType,
                                    date = date,
                                    other = if (other.isNotEmpty()) other else null
                                )
                            }
                            
                            // 提交后返回支出列表页面
                            onNavigateToExpenseList()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && amount.isNotEmpty() && date.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White
                            )
                        } else {
                            Text(if (expenseId == null) "提交" else "更新")
                        }
                    }
                }
            }
        }
    }
}

// 获取月份名称
@Composable
private fun getMonthName(month: Int): String {
    val monthNames = listOf(
        "", "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    )
    return monthNames.getOrElse(month) { "" }
}
