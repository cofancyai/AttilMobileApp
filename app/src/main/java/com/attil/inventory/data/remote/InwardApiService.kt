package com.attil.inventory.data.remote

import com.attil.inventory.data.model.transaction.InwardItem
import retrofit2.Response
import retrofit2.http.*

interface InwardApiService {

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @GET("inward_items")
    suspend fun getAllInwardItems(
        @Query("select") select: String = "*,items:item_id(*,categories:category_id(*),godowns:godown_id(*),racks:rack_id(*),item_cuisines(cuisines(*))),cuisines:cuisine_id(*)",
        @Query("order") order: String = "purchase_date.desc"
    ): Response<List<InwardItem>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("inward_items")
    suspend fun createInwardItem(@Body item: HashMap<String, Any>): Response<List<InwardItem>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("inward_items")
    suspend fun updateInwardItem(
        @Query("id") id: String,
        @Body updateRequest: HashMap<String, Any>
    ): Response<List<InwardItem>>

    @Headers("Content-Type: application/json")
    @DELETE("inward_items")
    suspend fun deleteInwardItem(@Query("id") id: String): Response<Unit>
}