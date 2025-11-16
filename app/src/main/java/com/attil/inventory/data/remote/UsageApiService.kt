package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.Usage
import retrofit2.Response
import retrofit2.http.*

interface UsageApiService {

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @GET("usages")
    suspend fun getAllUsages(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Usage>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("usages")
    suspend fun createUsage(@Body usage: HashMap<String, Any>): Response<List<Usage>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("usages")
    suspend fun updateUsage(
        @Query("id") id: String,
        @Body updateRequest: HashMap<String, Any>
    ): Response<List<Usage>>

    @Headers("Content-Type: application/json")
    @DELETE("usages")
    suspend fun deleteUsage(@Query("id") id: String): Response<Unit>
}