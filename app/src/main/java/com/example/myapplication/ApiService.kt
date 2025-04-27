package com.example.myapplication

import com.example.myapplication.ui.login.LoginRequest
import com.example.myapplication.ui.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/users/register")
    suspend fun registerUser(@Body user: UserCredentials): Response<Unit>

    @POST("auth")
    suspend fun login(@Body credentials: LoginRequest): LoginResponse


    @DELETE("/api/tables/{tableName}")
    suspend fun deleteItem(
        @Path("tableName") tableName: String,
        @Query("id") id: Int
    ): Response<Unit>

    @GET("tables")
    suspend fun getTables(): List<String>

    @GET("tables/{tableName}/data")
    suspend fun getTableData(@Path("tableName") tableName: String): List<Map<String, Any>>

    @POST("/api/tables/{tableName}/update")
    suspend fun updateTable(
        @Path("tableName") tableName: String,
        @Body items: List<DataItem>
    ): Response<Unit>

    @POST("/api/tables/{tableName}")
    suspend fun addItem(
        @Path("tableName") tableName: String,
        @Body item: DataItem
    ): Response<Unit>
}