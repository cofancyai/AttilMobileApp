package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.Item
import com.attil.inventory.data.model.management.ItemCuisine
import retrofit2.Response
import retrofit2.http.*

interface ItemApiService {

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @GET("items")
    suspend fun getAllItems(
        @Query("select") select: String = "*,categories(*),godowns(*),racks(*),item_cuisines(cuisines(*))",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Item>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("items")
    suspend fun createItem(@Body item: HashMap<String, Any>): Response<List<Item>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("items")
    suspend fun updateItem(
        @Query("id") id: String,
        @Body updateRequest: HashMap<String, Any>
    ): Response<List<Item>>

    @Headers("Content-Type: application/json")
    @DELETE("items")
    suspend fun deleteItem(@Query("id") id: String): Response<Unit>

    @Headers("Content-Type: application/json")
    @GET("item_cuisines")
    suspend fun getItemCuisines(
        @Query("item_id") itemId: String,
        @Query("select") select: String = "*,cuisines(*)"
    ): Response<List<ItemCuisine>>

    @Headers("Content-Type: application/json")
    @POST("item_cuisines")
    suspend fun createItemCuisines(@Body itemCuisines: List<Map<String, String>>): Response<List<ItemCuisine>>

    @Headers("Content-Type: application/json")
    @DELETE("item_cuisines")
    suspend fun deleteItemCuisines(@Query("item_id") itemId: String): Response<Unit>
}