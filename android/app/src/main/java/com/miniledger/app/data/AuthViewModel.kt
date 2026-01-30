package com.miniledger.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(username: String, password: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = NetworkModule.apiService.login(
                    LoginRequest(username, password)
                )
                if (response.error == null && response.token != null) {
                    _token.value = response.token
                    _userId.value = response.userId
                    _username.value = username
                    _isAuthenticated.value = true
                    onSuccess?.invoke()
                } else {
                    _error.value = "登录失败: ${response.error ?: "未知错误"}"
                }
            } catch (e: retrofit2.HttpException) {
                _error.value = "登录失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                _error.value = "登录失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                _error.value = "登录失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                _error.value = "登录失败: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(username: String, password: String, email: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = NetworkModule.apiService.register(
                    RegisterRequest(username, password, email)
                )
                if (response.error == null) {
                    // 注册成功后需要重新登录获取token
                    _error.value = "注册成功，请使用用户名和密码登录"
                } else {
                    _error.value = "注册失败: ${response.error ?: "未知错误"}"
                }
            } catch (e: Exception) {
                _error.value = "注册失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _token.value = null
        _userId.value = null
        _username.value = null
        _isAuthenticated.value = false
    }

    fun changePassword(oldPassword: String, newPassword: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = _token.value
                if (token == null) {
                    _error.value = "请先登录"
                    return@launch
                }
                val bearerToken = "Bearer $token"
                val response = NetworkModule.apiService.changePassword(
                    bearerToken,
                    ChangePasswordRequest(currentPassword = oldPassword, newPassword = newPassword)
                )
                if (response.error == null) {
                    _error.value = response.message
                    onSuccess?.invoke()
                } else {
                    _error.value = "密码修改失败: ${response.error}"
                }
            } catch (e: retrofit2.HttpException) {
                _error.value = "密码修改失败: HTTP错误 ${e.code()} - ${e.message()}"
            } catch (e: java.net.SocketTimeoutException) {
                _error.value = "密码修改失败: 网络超时，请检查网络连接"
            } catch (e: java.net.UnknownHostException) {
                _error.value = "密码修改失败: 无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                _error.value = "密码修改失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
