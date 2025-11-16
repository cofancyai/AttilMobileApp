package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.Vendor
import com.attil.inventory.data.model.management.CreateVendorRequest
import com.attil.inventory.data.model.management.UpdateVendorRequest
import retrofit2.Response
import retrofit2.http.*

interface VendorApiService {

    @Headers("Content-Type: application/json")
    @GET("vendors")
    suspend fun getAllVendors(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Vendor>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("vendors")
    suspend fun createVendor(@Body vendor: CreateVendorRequest): Response<List<Vendor>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("vendors")
    suspend fun updateVendor(
        @Query("id") id: String,
        @Body updateRequest: UpdateVendorRequest
    ): Response<List<Vendor>>

    @Headers("Content-Type: application/json")
    @DELETE("vendors")
    suspend fun deleteVendor(@Query("id") id: String): Response<Unit>
}