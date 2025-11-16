package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.CreateGodownRequest
import com.attil.inventory.data.model.management.Godown
import com.attil.inventory.data.model.management.UpdateGodownRequest
import retrofit2.Response
import retrofit2.http.*

interface GodownApiService {

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @GET("godowns")
    suspend fun getAllGodowns(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Godown>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("godowns")
    suspend fun createGodown(@Body godown: CreateGodownRequest): Response<List<Godown>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("godowns")
    suspend fun updateGodown(
        @Query("id") id: String,
        @Body updateRequest: UpdateGodownRequest
    ): Response<List<Godown>>

    @Headers("Content-Type: application/json")
    @DELETE("godowns")
    suspend fun deleteGodown(@Query("id") id: String): Response<Unit>
}