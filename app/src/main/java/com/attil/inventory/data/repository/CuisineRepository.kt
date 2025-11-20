package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.management.CreateCuisineRequest
import com.attil.inventory.data.model.management.UpdateCuisineRequest
import com.attil.inventory.data.remote.CuisineApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CuisineRepository @Inject constructor(
    private val apiService: CuisineApiService
) {

    fun getAllCuisines(): Flow<Result<List<Cuisine>>> = flow {
        val result = try {
            Log.d("CuisineRepo", "Fetching all cuisines...")
            val response = apiService.getAllCuisines()
            Log.d("CuisineRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val cuisines = response.body() ?: emptyList()
                Log.d("CuisineRepo", "Successfully fetched ${cuisines.size} cuisines")
                Result.success(cuisines)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CuisineRepo", "Error fetching cuisines: $errorBody")
                Result.failure(Exception("Failed to fetch cuisines: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CuisineRepo", "Exception fetching cuisines", e)
            Result.failure(e)
        }
        emit(result)
    }

    fun createCuisine(request: CreateCuisineRequest): Flow<Result<Cuisine>> = flow {
        val result = try {
            Log.d("CuisineRepo", "Creating cuisine: $request")
            val response = apiService.createCuisine(request)
            Log.d("CuisineRepo", "Create response code: ${response.code()}")

            if (response.isSuccessful) {
                val cuisines = response.body()
                if (!cuisines.isNullOrEmpty()) {
                    Result.success(cuisines.first())
                } else {
                    Result.failure(Exception("Created successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CuisineRepo", "Error creating cuisine: $errorBody")
                Result.failure(Exception("Failed to create cuisine: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CuisineRepo", "Exception creating cuisine", e)
            Result.failure(e)
        }
        emit(result)
    }

    fun updateCuisine(id: String, request: UpdateCuisineRequest): Flow<Result<Cuisine>> = flow {
        val result = try {
            Log.d("CuisineRepo", "Updating cuisine $id: $request")
            val response = apiService.updateCuisine("eq.$id", request)
            Log.d("CuisineRepo", "Update response code: ${response.code()}")

            if (response.isSuccessful) {
                val cuisines = response.body()
                if (!cuisines.isNullOrEmpty()) {
                    Result.success(cuisines.first())
                } else {
                    Result.failure(Exception("Updated successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CuisineRepo", "Error updating cuisine: $errorBody")
                Result.failure(Exception("Failed to update cuisine: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CuisineRepo", "Exception updating cuisine", e)
            Result.failure(e)
        }
        emit(result)
    }

    fun deleteCuisine(id: String): Flow<Result<Unit>> = flow {
        val result = try {
            Log.d("CuisineRepo", "Deleting cuisine: $id")
            val response = apiService.deleteCuisine("eq.$id")
            Log.d("CuisineRepo", "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CuisineRepo", "Error deleting cuisine: $errorBody")
                Result.failure(Exception("Failed to delete cuisine: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CuisineRepo", "Exception deleting cuisine", e)
            Result.failure(e)
        }
        emit(result)
    }
}