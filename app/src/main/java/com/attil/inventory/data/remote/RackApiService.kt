package com.attil.inventory.data.remote

import com.attil.inventory.data.model.management.CreateRackRequest
import com.attil.inventory.data.model.management.Rack
import com.attil.inventory.data.model.management.RackWithGodown
import com.attil.inventory.data.model.management.UpdateRackRequest
import retrofit2.Response
import retrofit2.http.*

interface RackApiService {

    @Headers("Content-Type: application/json")
    @GET("racks")
    suspend fun getAllRacks(
        @Query("select") select: String = "*,godowns(id,name)",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<RackWithGodown>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("racks")
    suspend fun createRack(@Body rack: CreateRackRequest): Response<List<Rack>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("racks")
    suspend fun updateRack(
        @Query("id") id: String,
        @Body updateRequest: UpdateRackRequest
    ): Response<List<Rack>>

    @Headers("Content-Type: application/json")
    @DELETE("racks")
    suspend fun deleteRack(@Query("id") id: String): Response<Unit>
}