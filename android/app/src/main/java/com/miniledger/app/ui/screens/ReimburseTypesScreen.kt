package com.miniledger.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.ListItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.ui.res.stringResource
import com.miniledger.app.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import com.miniledger.app.data.ExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@Composable
fun ReimburseTypesScreen(
    expenseViewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentTypeId by remember { mutableStateOf("") }
    var newTypeName by remember { mutableStateOf("") }
    var editTypeName by remember { mutableStateOf("") }

    val config by expenseViewModel.config.collectAsState()
    val error by expenseViewModel.error.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "报销类型管理",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                newTypeName = ""
                showAddDialog = true
            },
            modifier = Modifier
                .width(200.dp)
                .height(48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = "添加", modifier = Modifier.padding(end = 4.dp))
                Text(text = "添加类型", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (config.reimburseTypes.isEmpty()) {
            Text(
                text = "暂无报销类型",
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(config.reimburseTypes) { type ->
                    ListItem(
                        headlineContent = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = type, fontSize = 16.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    currentTypeId = type // 注意：这里简化处理，实际应该从API获取类型ID
                                    editTypeName = type
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "编辑")
                                }
                                IconButton(onClick = {
                                    currentTypeId = type // 注意：这里简化处理，实际应该从API获取类型ID
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "删除")
                                }
                            }
                        }
                    )
                    Divider()
                }
            }
        }

        if (error != null) {
            Text(
                text = error!!,
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    // 添加类型对话框
    if (showAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(text = "添加报销类型")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTypeName,
                        onValueChange = { newTypeName = it },
                        label = { Text(text = "类型名称") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTypeName.isNotEmpty()) {
                        expenseViewModel.addReimburseType(
                            typeName = newTypeName,
                            onSuccess = {
                                showAddDialog = false
                                newTypeName = ""
                            }
                        )
                    }
                }) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = "取消")
                }
            }
        )
    }

    // 编辑类型对话框
    if (showEditDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(text = "编辑报销类型")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTypeName,
                        onValueChange = { editTypeName = it },
                        label = { Text(text = "类型名称") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editTypeName.isNotEmpty()) {
                        expenseViewModel.updateReimburseType(
                            oldTypeName = currentTypeId,
                            newTypeName = editTypeName,
                            onSuccess = {
                                showEditDialog = false
                                editTypeName = ""
                            }
                        )
                    }
                }) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(text = "取消")
                }
            }
        )
    }

    // 删除类型对话框
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "确认删除") },
            text = { Text(text = "确定要删除此报销类型吗？") },
            confirmButton = {
                TextButton(onClick = {
                    expenseViewModel.deleteReimburseType(
                        typeName = currentTypeId,
                        onSuccess = {
                            showDeleteDialog = false
                        }
                    )
                }) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "取消")
                }
            }
        )
    }
}
