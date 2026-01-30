package com.miniledger.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("/api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse
    
    @POST("/api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): ChangePasswordResponse
    
    @GET("/api/expenses/stats")
    suspend fun getExpenseStats(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): ExpenseStatsResponse
    
    @GET("/api/expenses")
    suspend fun getExpenses(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): ExpenseListResponse
    
    @GET("/api/config")
    suspend fun getConfig(
        @Header("Authorization") token: String
    ): ConfigResponse
    
    @PUT("/api/config")
    suspend fun updateConfig(
        @Header("Authorization") token: String,
        @Body configRequest: ConfigRequest
    ): ConfigResponse
    
    @POST("/api/expenses")
    suspend fun addExpense(
        @Header("Authorization") token: String,
        @Body expense: Expense
    ): Expense
    
    @PUT("/api/expenses/{id}")
    suspend fun updateExpense(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body expense: Expense
    ): Expense
    
    @retrofit2.http.DELETE("/api/expenses/{id}")
    suspend fun deleteExpense(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): kotlin.Unit
    
    @GET("/api/expenses/export")
    suspend fun exportExpenses(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): retrofit2.Response<okhttp3.ResponseBody>
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String,
    val error: String? = null
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null
)

data class RegisterResponse(
    val message: String,
    val userId: String,
    val error: String? = null
)

data class ExpenseStatsResponse(
    val totalExpense: Double,
    val pendingReimburse: Double,
    val reimbursed: Double,
    val balance: Double
)

data class Expense(
    val _id: String,
    val amount: Double,
    val reimburseType: String,
    val payType: String,
    val date: String,
    val other: String? = null,
    val reimburseAmount: Double? = null
)

data class ExpenseListResponse(
    val expenses: List<Expense>,
    val total: Int
)

data class ConfigResponse(
    val reimburseTypes: List<String>,
    val payTypes: List<String>
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val message: String,
    val error: String? = null
)

data class TypeRequest(
    val name: String
)

data class TypeResponse(
    val _id: String,
    val name: String
)

data class ConfigRequest(
    val type: String,
    val options: List<String>
)