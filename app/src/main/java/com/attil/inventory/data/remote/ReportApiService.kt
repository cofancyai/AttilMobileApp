package com.attil.inventory.data.remote

import com.attil.inventory.data.model.reports.*
import retrofit2.Response
import retrofit2.http.*

interface ReportApiService {

    // INWARD REPORT APIs
    @GET("inward_items")
    @Headers("Content-Type: application/json")
    suspend fun getInwardReportByDateRange(
        @Query("purchase_date") dateRange: String,
        @Query("select") select: String = "*,items(name,unit_of_measure,categories(name)),cuisines(name)",
        @Query("order") order: String = "purchase_date.desc"
    ): Response<List<Map<String, Any>>>

    // OUTWARD REPORT APIs
    @GET("outward_items")
    @Headers("Content-Type: application/json")
    suspend fun getOutwardReportByDateRange(
        @Query("usage_date") dateRange: String,
        @Query("select") select: String = "*,items(name,unit_of_measure,categories(name)),cuisines(name),users(full_name)",
        @Query("order") order: String = "usage_date.desc"
    ): Response<List<Map<String, Any>>>

    // NEW: Get inward items for specific item to calculate moving average cost
    @GET("inward_items")
    @Headers("Content-Type: application/json")
    suspend fun getInwardItemsForCostCalculation(
        @Query("item_id") itemId: String,
        @Query("purchase_date") dateRange: String,
        @Query("select") select: String = "purchase_date,price_per_unit,inward_quantity",
        @Query("order") order: String = "purchase_date.desc"
    ): Response<List<Map<String, Any>>>

    // SUMMARY APIs
    @GET("rpc/get_inward_summary")
    @Headers("Content-Type: application/json")
    suspend fun getInwardSummary(
        @Query("p_start_date") startDate: String,
        @Query("p_end_date") endDate: String
    ): Response<InwardSummary>

    @GET("rpc/get_outward_summary")
    @Headers("Content-Type: application/json")
    suspend fun getOutwardSummary(
        @Query("p_start_date") startDate: String,
        @Query("p_end_date") endDate: String
    ): Response<OutwardSummary>
}