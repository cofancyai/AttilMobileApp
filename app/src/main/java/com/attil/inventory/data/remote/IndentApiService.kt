package com.attil.inventory.data.remote

import com.attil.inventory.data.model.transaction.Indent
import com.attil.inventory.data.model.transaction.IndentItem
import com.attil.inventory.data.model.transaction.CreateIndentMainRequest
import com.attil.inventory.data.model.transaction.CreateIndentItemRequest
import com.attil.inventory.data.model.transaction.UpdateIndentRequest
import com.attil.inventory.data.model.transaction.UpdateIndentItemRequest
import com.attil.inventory.data.model.transaction.VerifyIndentItemRequest
import retrofit2.Response
import retrofit2.http.*

interface IndentApiService {

    @GET("indents")
    suspend fun getAllIndents(@Query("select") select: String = "*,cuisines!cuisine_id(*),indent_items(*,items(*)),users!chef_id(id,username,full_name,cuisine_id)"): Response<List<Indent>>

    @GET("indents")
    suspend fun getIndentsByChef(
        @Query("chef_id") chefId: String,
        @Query("select") select: String = "*,cuisines!cuisine_id(*),indent_items(*,items(*)),users!chef_id(id,username,full_name,cuisine_id)"
    ): Response<List<Indent>>

    @GET("indents")
    suspend fun getIndentsByStatus(
        @Query("status") status: String,
        @Query("select") select: String = "*,cuisines!cuisine_id(*),indent_items(*,items(*)),users!chef_id(id,username,full_name,cuisine_id)"
    ): Response<List<Indent>>

    @GET("indents")
    suspend fun getIndentById(
        @Query("id") id: String,
        @Query("select") select: String = "*,cuisines!cuisine_id(*),indent_items(*,items(*)),users!chef_id(id,username,full_name,cuisine_id)"
    ): Response<List<Indent>>

    @POST("indents")
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    suspend fun createIndent(@Body indent: CreateIndentMainRequest): Response<List<Indent>>

    @POST("indent_items")
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    suspend fun createIndentItems(@Body items: List<CreateIndentItemRequest>): Response<List<IndentItem>>

    @PATCH("indents")
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    suspend fun updateIndent(
        @Query("id") id: String,
        @Body updateRequest: UpdateIndentRequest
    ): Response<List<Indent>>

    @DELETE("indents")
    suspend fun deleteIndent(@Query("id") id: String): Response<Unit>

    @GET("indent_items")
    suspend fun getIndentItems(
        @Query("indent_id") indentId: String,
        @Query("select") select: String = "*,items(*)"
    ): Response<List<IndentItem>>

    @PATCH("indent_items")
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    suspend fun updateIndentItem(
        @Query("id") id: String,
        @Body updateData: UpdateIndentItemRequest
    ): Response<List<IndentItem>>

    @PATCH("indent_items")
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    suspend fun verifyIndentItem(
        @Query("id") id: String,
        @Body verifyRequest: VerifyIndentItemRequest
    ): Response<List<IndentItem>>

    @PATCH("indent_items")
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    suspend fun verifyMultipleIndentItems(
        @Query("id") itemIds: String, // comma-separated IDs like "in.(id1,id2,id3)"
        @Body verifyRequest: VerifyIndentItemRequest
    ): Response<List<IndentItem>>

    @GET("current_stock")
    suspend fun getItemsForIndent(@Query("current_stock") stock: String = "gt.0"): Response<List<Map<String, Any>>>

    @GET("current_stock")
    suspend fun getItemsForIndentByCategory(
        @Query("category_name") categoryName: String,
        @Query("current_stock") stock: String = "gt.0"
    ): Response<List<Map<String, Any>>>

    // Indent Reports - with date range and filters
    @GET("indents")
    suspend fun getIndentsForReport(
        @Query("created_at") dateRange: String, // e.g., "gte.2024-01-01&created_at=lte.2024-12-31"
        @Query("chef_id") chefId: String? = null,
        @Query("status") status: String? = null,
        @Query("select") select: String = "*,cuisines!cuisine_id(name),indent_items!inner(*,items!item_id(name)),users!chef_id(full_name)",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Indent>>
}