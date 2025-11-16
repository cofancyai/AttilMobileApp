package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.CurrentStock
import retrofit2.Response
import retrofit2.http.*

interface CurrentStockApiService {

    @Headers("Content-Type: application/json")
    @GET("current_stock")
    suspend fun getAllCurrentStocks(
        @Query("select") select: String = "*",
        @Query("order") order: String = "item_name.asc"
    ): Response<List<CurrentStock>>

    @Headers("Content-Type: application/json")
    @GET("current_stock")
    suspend fun getCurrentStocksByCategory(
        @Query("category_name") categoryName: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "item_name.asc"
    ): Response<List<CurrentStock>>

    @Headers("Content-Type: application/json")
    @GET("current_stock")
    suspend fun getCurrentStocksByGodown(
        @Query("godown_name") godownName: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "item_name.asc"
    ): Response<List<CurrentStock>>

    @Headers("Content-Type: application/json")
    @GET("current_stock")
    suspend fun getLowStockItems(
        @Query("is_low_stock") isLowStock: String = "eq.true",
        @Query("select") select: String = "*",
        @Query("order") order: String = "current_stock.asc"
    ): Response<List<CurrentStock>>
}