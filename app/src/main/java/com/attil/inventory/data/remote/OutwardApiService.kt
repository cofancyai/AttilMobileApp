package com.attil.inventory.data.remote

import com.attil.inventory.data.model.transaction.OutwardItem
import com.attil.inventory.data.model.transaction.CreateOutwardItemRequest
import com.attil.inventory.data.model.transaction.UpdateOutwardItemRequest
import retrofit2.Response
import retrofit2.http.*

interface OutwardApiService {

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @GET("outward_items")
    suspend fun getAllOutwardItems(
        @Query("select") select: String = "*,items:item_id(id,name,unit_of_measure,categories:category_id(id,name)),categories:category_id(id,name),cuisines:cuisine_id(id,name,description)",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<OutwardItem>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("outward_items")
    suspend fun createOutwardItem(@Body item: CreateOutwardItemRequest): Response<List<OutwardItem>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("outward_items")
    suspend fun updateOutwardItem(
        @Query("id") id: String,
        @Body updateRequest: UpdateOutwardItemRequest
    ): Response<List<OutwardItem>>

    @Headers("Content-Type: application/json")
    @DELETE("outward_items")
    suspend fun deleteOutwardItem(@Query("id") id: String): Response<Unit>

    // Get items with current stock for selection
    @Headers("Content-Type: application/json")
    @GET("current_stock")
    suspend fun getItemsWithStock(
        @Query("select") select: String = "item_id,item_name,category_name,current_stock,unit_of_measure",
        @Query("current_stock") stock: String = "gt.0",
        @Query("order") order: String = "item_name.asc"
    ): Response<List<Map<String, Any>>>

    // Get items filtered by category with stock
    @Headers("Content-Type: application/json")
    @GET("current_stock")
    suspend fun getItemsWithStockByCategory(
        @Query("category_name") categoryName: String,
        @Query("select") select: String = "item_id,item_name,category_name,current_stock,unit_of_measure",
        @Query("current_stock") stock: String = "gt.0",
        @Query("order") order: String = "item_name.asc"
    ): Response<List<Map<String, Any>>>
}