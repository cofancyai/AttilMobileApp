package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.management.Category
import com.attil.inventory.data.model.management.CreateCategoryRequest
import com.attil.inventory.data.model.management.UpdateCategoryRequest
import com.attil.inventory.data.remote.CategoryApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: CategoryApiService
) {

    suspend fun getAllCategories(): Flow<Result<List<Category>>> = flow {
        try {
            Log.d("CategoryRepo", "Fetching all categories...")
            val response = apiService.getAllCategories()
            Log.d("CategoryRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CategoryRepo", "Error fetching categories: $errorBody")
                emit(Result.failure(Exception("Failed to fetch categories: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Exception fetching categories", e)
            emit(Result.failure(e))
        }
    }

    suspend fun createCategory(request: CreateCategoryRequest): Flow<Result<Category>> = flow {
        try {
            Log.d("CategoryRepo", "Creating category: $request")
            val response = apiService.createCategory(request)
            Log.d("CategoryRepo", "Create response code: ${response.code()}")

            if (response.isSuccessful) {
                val categories = response.body()
                if (!categories.isNullOrEmpty()) {
                    emit(Result.success(categories.first()))
                } else {
                    emit(Result.failure(Exception("Created successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CategoryRepo", "Error creating category: $errorBody")
                emit(Result.failure(Exception("Failed to create category: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Exception creating category", e)
            emit(Result.failure(e))
        }
    }

    suspend fun updateCategory(id: String, request: UpdateCategoryRequest): Flow<Result<Category>> = flow {
        try {
            Log.d("CategoryRepo", "Updating category $id: $request")
            val response = apiService.updateCategory("eq.$id", request)
            Log.d("CategoryRepo", "Update response code: ${response.code()}")

            if (response.isSuccessful) {
                val categories = response.body()
                if (!categories.isNullOrEmpty()) {
                    emit(Result.success(categories.first()))
                } else {
                    emit(Result.failure(Exception("Updated successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CategoryRepo", "Error updating category: $errorBody")
                emit(Result.failure(Exception("Failed to update category: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Exception updating category", e)
            emit(Result.failure(e))
        }
    }

    suspend fun deleteCategory(id: String): Flow<Result<Unit>> = flow {
        try {
            Log.d("CategoryRepo", "Deleting category: $id")
            val response = apiService.deleteCategory("eq.$id")
            Log.d("CategoryRepo", "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CategoryRepo", "Error deleting category: $errorBody")
                emit(Result.failure(Exception("Failed to delete category: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Exception deleting category", e)
            emit(Result.failure(e))
        }
    }
}