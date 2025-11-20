package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.management.CreateGodownRequest
import com.attil.inventory.data.model.management.Godown
import com.attil.inventory.data.model.management.UpdateGodownRequest
import com.attil.inventory.data.remote.GodownApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GodownRepository @Inject constructor(
    private val apiService: GodownApiService
) {

    suspend fun getAllGodowns(): Flow<Result<List<Godown>>> = flow {
        val result = try {
            Log.d("GodownRepo", "Fetching all godowns...")
            val response = apiService.getAllGodowns()
            Log.d("GodownRepo", "Response code: ${response.code()}")
            Log.d("GodownRepo", "Response body: ${response.body()}")

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GodownRepo", "Error fetching godowns: $errorBody")
                Result.failure(Exception("Failed to fetch godowns: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("GodownRepo", "Exception fetching godowns", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun createGodown(request: CreateGodownRequest): Flow<Result<Godown>> = flow {
        val result = try {
            Log.d("GodownRepo", "Creating godown: $request")
            val response = apiService.createGodown(request)
            Log.d("GodownRepo", "Create response code: ${response.code()}")
            Log.d("GodownRepo", "Create response body: ${response.body()}")

            if (response.isSuccessful) {
                val godowns = response.body()
                if (!godowns.isNullOrEmpty()) {
                    Result.success(godowns.first())
                } else {
                    Log.e("GodownRepo", "Empty response from create")
                    Result.failure(Exception("Created successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GodownRepo", "Error creating godown: $errorBody")
                Result.failure(Exception("Failed to create godown: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("GodownRepo", "Exception creating godown", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun updateGodown(id: String, request: UpdateGodownRequest): Flow<Result<Godown>> = flow {
        val result = try {
            Log.d("GodownRepo", "Updating godown $id: $request")
            val response = apiService.updateGodown("eq.$id", request)
            Log.d("GodownRepo", "Update response code: ${response.code()}")
            Log.d("GodownRepo", "Update response body: ${response.body()}")

            if (response.isSuccessful) {
                val godowns = response.body()
                if (!godowns.isNullOrEmpty()) {
                    Result.success(godowns.first())
                } else {
                    Log.e("GodownRepo", "Empty response from update")
                    Result.failure(Exception("Updated successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GodownRepo", "Error updating godown: $errorBody")
                Result.failure(Exception("Failed to update godown: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("GodownRepo", "Exception updating godown", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun deleteGodown(id: String): Flow<Result<Unit>> = flow {
        val result = try {
            Log.d("GodownRepo", "Deleting godown: $id")
            val response = apiService.deleteGodown("eq.$id")
            Log.d("GodownRepo", "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GodownRepo", "Error deleting godown: $errorBody")
                Result.failure(Exception("Failed to delete godown: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("GodownRepo", "Exception deleting godown", e)
            Result.failure(e)
        }
        emit(result)
    }
}