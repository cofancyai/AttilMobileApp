package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.Category
import com.attil.inventory.data.model.management.CreateCategoryRequest
import com.attil.inventory.data.model.management.UpdateCategoryRequest
import retrofit2.Response
import retrofit2.http.*

interface CategoryApiService {

    @Headers("Content-Type: application/json")
    @GET("categories")
    suspend fun getAllCategories(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Category>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("categories")
    suspend fun createCategory(@Body category: CreateCategoryRequest): Response<List<Category>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("categories")
    suspend fun updateCategory(
        @Query("id") id: String,
        @Body updateRequest: UpdateCategoryRequest
    ): Response<List<Category>>

    @Headers("Content-Type: application/json")
    @DELETE("categories")
    suspend fun deleteCategory(@Query("id") id: String): Response<Unit>
}