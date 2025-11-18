package com.attil.inventory.data.remote

import com.attil.inventory.data.model.master.User
import com.attil.inventory.data.model.master.CreateUserRequest
import com.attil.inventory.data.model.master.UpdateUserRequest
import com.attil.inventory.data.model.master.ChangePasswordRequest
import com.attil.inventory.data.model.master.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun getAllUsers(
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*),user_cuisines(id,cuisine_id,cuisines(*))",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<User>>

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun getUserById(
        @Query("id") id: String,
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*),user_cuisines(id,cuisine_id,cuisines(*))"
    ): Response<List<User>>

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun getUserByUsername(
        @Query("username") username: String,
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*),user_cuisines(id,cuisine_id,cuisines(*))"
    ): Response<List<User>>

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun getUserByEmail(
        @Query("email") email: String,
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*),user_cuisines(id,cuisine_id,cuisines(*))"
    ): Response<List<User>>

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun getUsersByRole(
        @Query("role_id") roleId: String,
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*),user_cuisines(id,cuisine_id,cuisines(*))"
    ): Response<List<User>>

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun getActiveUsers(
        @Query("is_active") isActive: Boolean = true,
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*),user_cuisines(id,cuisine_id,cuisines(*))"
    ): Response<List<User>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): Response<List<User>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("users")
    suspend fun updateUser(
        @Query("id") id: String,
        @Body updateRequest: UpdateUserRequest
    ): Response<List<User>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("users")
    suspend fun changePassword(
        @Query("id") id: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<List<User>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("users")
    suspend fun resetPassword(
        @Query("id") id: String,
        @Body resetPasswordRequest: ResetPasswordRequest
    ): Response<List<User>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("users")
    suspend fun toggleUserStatus(
        @Query("id") id: String,
        @Body statusUpdate: Map<String, Boolean>
    ): Response<List<User>>

    @Headers("Content-Type: application/json")
    @DELETE("users")
    suspend fun deleteUser(@Query("id") id: String): Response<Unit>

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun searchUsers(
        @Query("or") searchQuery: String,
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*)"
    ): Response<List<User>>

    @Headers("Content-Type: application/json")
    @GET("users")
    suspend fun getUsersWithFilters(
        @Query("role_id") roleId: String? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("select") select: String = "*,roles!role_id(*),cuisines!cuisine_id(*)"
    ): Response<List<User>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("users")
    suspend fun updateUserScreenPermissions(
        @Query("id") id: String,
        @Body permissionsUpdate: Map<String, Any>
    ): Response<List<User>>

    // User Cuisines Management
    @Headers("Content-Type: application/json")
    @GET("user_cuisines")
    suspend fun getUserCuisines(
        @Query("user_id") userId: String,
        @Query("select") select: String = "*,cuisines(*)"
    ): Response<List<com.attil.inventory.data.model.master.UserCuisineRelation>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("user_cuisines")
    suspend fun assignCuisineToUser(
        @Body assignment: Map<String, String>
    ): Response<List<com.attil.inventory.data.model.master.UserCuisineRelation>>

    @Headers("Content-Type: application/json")
    @DELETE("user_cuisines")
    suspend fun removeCuisineFromUser(
        @Query("user_id") userId: String,
        @Query("cuisine_id") cuisineId: String
    ): Response<Unit>

    @Headers("Content-Type: application/json")
    @DELETE("user_cuisines")
    suspend fun removeAllCuisinesFromUser(
        @Query("user_id") userId: String
    ): Response<Unit>
}