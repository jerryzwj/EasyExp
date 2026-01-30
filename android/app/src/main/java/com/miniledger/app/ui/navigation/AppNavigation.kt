package com.miniledger.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.miniledger.app.ui.screens.HomeScreen
import com.miniledger.app.ui.screens.LoginScreen
import com.miniledger.app.ui.screens.RegisterScreen
import com.miniledger.app.ui.screens.SettingsScreen
import com.miniledger.app.ui.screens.ReimburseTypesScreen
import com.miniledger.app.ui.screens.PayTypesScreen
import com.miniledger.app.ui.screens.ChangePasswordScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.miniledger.app.data.AuthViewModel>()
    val expenseViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.miniledger.app.data.ExpenseViewModel>(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return com.miniledger.app.data.ExpenseViewModel(authViewModel) as T
        }
    })

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            com.miniledger.app.ui.screens.LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                context = context,
                authViewModel = authViewModel
            )
        }
        composable("register") {
            com.miniledger.app.ui.screens.RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        composable("home") {
            com.miniledger.app.ui.screens.HomeScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToExpenseList = {
                    navController.navigate("expenseList")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                authViewModel = authViewModel,
                expenseViewModel = expenseViewModel
            )
        }
        composable("expenseList") {
            com.miniledger.app.ui.screens.ExpenseListScreen(
                onNavigateToAddExpense = {
                    navController.navigate("addExpense")
                },
                onNavigateToEditExpense = { expenseId ->
                    navController.navigate("editExpense/$expenseId")
                },

                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("expenseList") { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("expenseList") { inclusive = true }
                    }
                },
                authViewModel = authViewModel,
                expenseViewModel = expenseViewModel
            )
        }
        composable("addExpense") {
            com.miniledger.app.ui.screens.AddExpenseScreen(
                onNavigateToExpenseList = {
                    navController.navigate("expenseList") {
                        popUpTo("addExpense") { inclusive = true }
                    }
                },
                expenseId = null,
                authViewModel = authViewModel,
                expenseViewModel = expenseViewModel
            )
        }
        composable("editExpense/{expenseId}") {backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId")
            com.miniledger.app.ui.screens.AddExpenseScreen(
                onNavigateToExpenseList = {
                    navController.navigate("expenseList") {
                        popUpTo("editExpense/{expenseId}") { inclusive = true }
                    }
                },
                expenseId = expenseId,
                authViewModel = authViewModel,
                expenseViewModel = expenseViewModel
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateToReimburseTypes = {
                    navController.navigate("reimburseTypes")
                },
                onNavigateToPayTypes = {
                    navController.navigate("payTypes")
                },
                onNavigateToChangePassword = {
                    navController.navigate("changePassword")
                },
                onNavigateBack = {
                    navController.navigate("home") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            )
        }
        composable("reimburseTypes") {
            ReimburseTypesScreen(
                expenseViewModel = expenseViewModel,
                onNavigateBack = {
                    navController.navigate("settings") {
                        popUpTo("reimburseTypes") { inclusive = true }
                    }
                }
            )
        }
        composable("payTypes") {
            PayTypesScreen(
                expenseViewModel = expenseViewModel,
                onNavigateBack = {
                    navController.navigate("settings") {
                        popUpTo("payTypes") { inclusive = true }
                    }
                }
            )
        }
        composable("changePassword") {
            ChangePasswordScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.navigate("settings") {
                        popUpTo("changePassword") { inclusive = true }
                    }
                }
            )
        }
    }
}
