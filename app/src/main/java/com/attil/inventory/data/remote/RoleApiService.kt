package com.attil.inventory.data.remote

import com.attil.inventory.data.model.master.Role
import com.attil.inventory.data.model.master.CreateRoleRequest
import com.attil.inventory.data.model.master.UpdateRoleRequest
import retrofit2.Response
import retrofit2.http.*

interface RoleApiService {

    @Headers("Content-Type: application/json")
    @GET("roles")
    suspend fun getAllRoles(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Role>>

    @Headers("Content-Type: application/json")
    @GET("roles")
    suspend fun getRoleById(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): Response<List<Role>>

    @Headers("Content-Type: application/json")
    @GET("roles")
    suspend fun getRoleByName(
        @Query("name") name: String,
        @Query("select") select: String = "*"
    ): Response<List<Role>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("roles")
    suspend fun createRole(@Body role: CreateRoleRequest): Response<List<Role>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("roles")
    suspend fun updateRole(
        @Query("id") id: String,
        @Body updateRequest: UpdateRoleRequest
    ): Response<List<Role>>

    @Headers("Content-Type: application/json")
    @DELETE("roles")
    suspend fun deleteRole(@Query("id") id: String): Response<Unit>

    @Headers("Content-Type: application/json")
    @GET("roles")
    suspend fun searchRoles(
        @Query("or") searchQuery: String,
        @Query("select") select: String = "*"
    ): Response<List<Role>>
}