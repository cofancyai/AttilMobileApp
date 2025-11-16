package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.management.CreateRackRequest
import com.attil.inventory.data.model.management.Rack
import com.attil.inventory.data.model.management.RackWithGodown
import com.attil.inventory.data.model.management.UpdateRackRequest
import com.attil.inventory.data.remote.RackApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RackRepository @Inject constructor(
    private val apiService: RackApiService
) {

    suspend fun getAllRacks(): Flow<Result<List<RackWithGodown>>> = flow {
        try {
            Log.d("RackRepo", "Fetching all racks with godowns...")
            val response = apiService.getAllRacks()
            Log.d("RackRepo", "Response code: ${response.code()}")
            Log.d("RackRepo", "Response body: ${response.body()}")

            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RackRepo", "Error fetching racks: $errorBody")
                emit(Result.failure(Exception("Failed to fetch racks: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RackRepo", "Exception fetching racks", e)
            emit(Result.failure(e))
        }
    }

    suspend fun createRack(request: CreateRackRequest): Flow<Result<Rack>> = flow {
        try {
            Log.d("RackRepo", "Creating rack: $request")
            val response = apiService.createRack(request)
            Log.d("RackRepo", "Create response code: ${response.code()}")
            Log.d("RackRepo", "Create response body: ${response.body()}")

            if (response.isSuccessful) {
                val racks = response.body()
                if (!racks.isNullOrEmpty()) {
                    emit(Result.success(racks.first()))
                } else {
                    Log.e("RackRepo", "Empty response from create")
                    emit(Result.failure(Exception("Created successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RackRepo", "Error creating rack: $errorBody")
                emit(Result.failure(Exception("Failed to create rack: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RackRepo", "Exception creating rack", e)
            emit(Result.failure(e))
        }
    }

    suspend fun updateRack(id: String, request: UpdateRackRequest): Flow<Result<Rack>> = flow {
        try {
            Log.d("RackRepo", "Updating rack $id: $request")
            val response = apiService.updateRack("eq.$id", request)
            Log.d("RackRepo", "Update response code: ${response.code()}")
            Log.d("RackRepo", "Update response body: ${response.body()}")

            if (response.isSuccessful) {
                val racks = response.body()
                if (!racks.isNullOrEmpty()) {
                    emit(Result.success(racks.first()))
                } else {
                    Log.e("RackRepo", "Empty response from update")
                    emit(Result.failure(Exception("Updated successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RackRepo", "Error updating rack: $errorBody")
                emit(Result.failure(Exception("Failed to update rack: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RackRepo", "Exception updating rack", e)
            emit(Result.failure(e))
        }
    }

    suspend fun deleteRack(id: String): Flow<Result<Unit>> = flow {
        try {
            Log.d("RackRepo", "Deleting rack: $id")
            val response = apiService.deleteRack("eq.$id")
            Log.d("RackRepo", "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RackRepo", "Error deleting rack: $errorBody")
                emit(Result.failure(Exception("Failed to delete rack: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RackRepo", "Exception deleting rack", e)
            emit(Result.failure(e))
        }
    }
}