package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.management.CreateCuisineRequest
import com.attil.inventory.data.model.management.UpdateCuisineRequest
import retrofit2.Response
import retrofit2.http.*

interface CuisineApiService {

    @Headers("Content-Type: application/json")
    @GET("cuisines")
    suspend fun getAllCuisines(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Cuisine>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("cuisines")
    suspend fun createCuisine(@Body cuisine: CreateCuisineRequest): Response<List<Cuisine>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("cuisines")
    suspend fun updateCuisine(
        @Query("id") id: String,
        @Body updateRequest: UpdateCuisineRequest
    ): Response<List<Cuisine>>

    @Headers("Content-Type: application/json")
    @DELETE("cuisines")
    suspend fun deleteCuisine(@Query("id") id: String): Response<Unit>
}