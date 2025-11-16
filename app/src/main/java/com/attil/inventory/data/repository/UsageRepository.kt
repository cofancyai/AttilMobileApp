package com.attil.inventory.data.repository

import com.attil.inventory.data.model.management.CreateUsageRequest
import com.attil.inventory.data.model.management.Usage
import com.attil.inventory.data.model.management.UpdateUsageRequest
import com.attil.inventory.data.remote.UsageApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    private val apiService: UsageApiService
) {
    fun getAllUsages(): Flow<Result<List<Usage>>> = flow {
        try {
            println("UsageRepository: Making API call to get all usages")
            val response = apiService.getAllUsages()
            println("UsageRepository: Response code: ${response.code()}")

            if (response.isSuccessful) {
                val usages = response.body() ?: emptyList()
                println("UsageRepository: Success - Got ${usages.size} usages")
                emit(Result.success(usages))
            } else {
                val errorBody = response.errorBody()?.string()
                println("UsageRepository: API Error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("UsageRepository: Exception in getAllUsages - ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    fun createUsage(request: CreateUsageRequest): Flow<Result<Usage>> = flow {
        try {
            println("UsageRepository: Creating usage - ${request.name}")
            val requestMap = hashMapOf<String, Any>(
                "name" to request.name,
                "is_active" to request.isActive
            )
            request.description?.let { requestMap["description"] = it }

            println("UsageRepository: Request body: $requestMap")

            val response = apiService.createUsage(requestMap)
            println("UsageRepository: Create response code: ${response.code()}")

            if (response.isSuccessful) {
                val createdUsage = response.body()?.firstOrNull()
                if (createdUsage != null) {
                    println("UsageRepository: Successfully created usage: ${createdUsage.name}")
                    emit(Result.success(createdUsage))
                } else {
                    println("UsageRepository: Response body was empty")
                    emit(Result.failure(Exception("Empty response from server")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("UsageRepository: Create API Error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("Create API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("UsageRepository: Exception in createUsage - ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    fun updateUsage(id: String, request: UpdateUsageRequest): Flow<Result<Usage>> = flow {
        try {
            println("UsageRepository: Updating usage with id: $id")
            val requestMap = hashMapOf<String, Any>(
                "name" to request.name,
                "is_active" to request.isActive
            )
            request.description?.let { requestMap["description"] = it }

            val response = apiService.updateUsage("eq.$id", requestMap)
            println("UsageRepository: Update response code: ${response.code()}")

            if (response.isSuccessful) {
                val updatedUsage = response.body()?.firstOrNull()
                if (updatedUsage != null) {
                    println("UsageRepository: Successfully updated usage: ${updatedUsage.name}")
                    emit(Result.success(updatedUsage))
                } else {
                    println("UsageRepository: Update response body was empty")
                    emit(Result.failure(Exception("Failed to update usage")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("UsageRepository: Update API Error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("Failed to update usage: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            println("UsageRepository: Exception in updateUsage - ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    fun deleteUsage(id: String): Flow<Result<Unit>> = flow {
        try {
            println("UsageRepository: Deleting usage with id: $id")
            val response = apiService.deleteUsage("eq.$id")
            println("UsageRepository: Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                println("UsageRepository: Successfully deleted usage")
                emit(Result.success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                println("UsageRepository: Delete API Error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("Failed to delete usage: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            println("UsageRepository: Exception in deleteUsage - ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }
}