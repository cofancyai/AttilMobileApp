package com.attil.inventory.data.remote

import com.attil.inventory.data.model.LoginRequest
import com.attil.inventory.data.model.master.User
import com.attil.inventory.data.model.master.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @Headers("Content-Type: application/json")
    @POST("rpc/authenticate_user")
    suspend fun login(@Body loginRequest: LoginRequest): Response<List<User>>

    @Headers("Content-Type: application/json")
    @PATCH("users")
    suspend fun updateLastLogin(
        @Query("id") userId: String,
        @Body updateData: Map<String, String>
    ): Response<Unit>

    @Headers("Content-Type: application/json")
    @POST("rpc/change_user_password")
    suspend fun changePassword(@Body changePasswordRequest: Map<String, String>): Response<Map<String, Any>>
}