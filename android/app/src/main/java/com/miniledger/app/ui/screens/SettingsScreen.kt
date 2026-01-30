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
import androidx.compose.runtime.Composable
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

@Composable
fun SettingsScreen(
    onNavigateToReimburseTypes: () -> Unit,
    onNavigateToPayTypes: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "设置",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToReimburseTypes,
                modifier = Modifier
                    .width(280.dp)
                    .height(56.dp)
            ) {
                Text(text = "报销类型管理", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToPayTypes,
                modifier = Modifier
                    .width(280.dp)
                    .height(56.dp)
            ) {
                Text(text = "支付类型管理", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToChangePassword,
                modifier = Modifier
                    .width(280.dp)
                    .height(56.dp)
            ) {
                Text(text = "修改密码", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onNavigateBack
        ) {
            Text(text = "返回", fontSize = 16.sp)
        }
    }
}
