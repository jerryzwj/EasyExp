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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import com.miniledger.app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.miniledger.app.data.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val error by authViewModel.error.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

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
                text = "修改密码",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 旧密码输入框
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text(text = "旧密码") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(bottom = 16.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )

            // 新密码输入框
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(text = "新密码") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(bottom = 16.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )

            // 确认新密码输入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(text = "确认新密码") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(bottom = 32.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )

            Button(
                onClick = {
                    if (validatePasswords(oldPassword, newPassword, confirmPassword)) {
                        authViewModel.changePassword(
                            oldPassword = oldPassword,
                            newPassword = newPassword,
                            onSuccess = {
                                showSuccessMessage = true
                                // 重置输入
                                oldPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        )
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp)
            ) {
                Text(text = "确认修改", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateBack) {
                Text(text = "取消")
            }
        }

        if (error != null) {
            Text(
                text = error!!,
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        if (showSuccessMessage) {
            Snackbar(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "密码修改成功")
            }
        }
    }
}

private fun validatePasswords(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
    if (oldPassword.isEmpty()) {
        return false
    }
    if (newPassword.isEmpty()) {
        return false
    }
    if (newPassword.length < 6) {
        return false
    }
    if (newPassword != confirmPassword) {
        return false
    }
    return true
}
