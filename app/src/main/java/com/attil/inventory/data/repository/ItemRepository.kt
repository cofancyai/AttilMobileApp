package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.management.CreateItemRequest
import com.attil.inventory.data.model.management.Item
import com.attil.inventory.data.model.management.ItemCuisine
import com.attil.inventory.data.model.management.UpdateItemRequest
import com.attil.inventory.data.remote.ItemApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val apiService: ItemApiService
) {

    suspend fun getAllItems(): Flow<Result<List<Item>>> = flow {
        try {
            Log.d("ItemRepo", "Fetching all items...")
            val response = apiService.getAllItems()
            Log.d("ItemRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ItemRepo", "Error fetching items: $errorBody")
                emit(Result.failure(Exception("Failed to fetch items: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("ItemRepo", "Exception fetching items", e)
            emit(Result.failure(e))
        }
    }

    suspend fun createItem(request: CreateItemRequest): Flow<Result<Item>> = flow {
        try {
            Log.d("ItemRepo", "Creating item: $request")

            val itemData = hashMapOf<String, Any>()
            itemData["name"] = request.name
            itemData["category_id"] = request.categoryId
            itemData["unit_of_measure"] = request.unitOfMeasure
            itemData["minimum_stock_level"] = request.minimumStockLevel

            request.godownId?.let { itemData["godown_id"] = it }
            request.rackId?.let { itemData["rack_id"] = it }

            val response = apiService.createItem(itemData)

            if (response.isSuccessful) {
                val items = response.body()
                if (!items.isNullOrEmpty()) {
                    val createdItem = items.first()
                    request.cuisineIds?.let { cuisineIds ->
                        if (cuisineIds.isNotEmpty()) {
                            createItemCuisineAssociations(createdItem.id!!, cuisineIds)
                        }
                    }
                    emit(Result.success(createdItem))
                } else {
                    emit(Result.failure(Exception("Created successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Result.failure(Exception("Failed to create item: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("ItemRepo", "Exception creating item", e)
            emit(Result.failure(e))
        }
    }

    suspend fun updateItem(id: String, request: UpdateItemRequest): Flow<Result<Item>> = flow {
        try {
            val itemData = hashMapOf<String, Any>()

            request.name?.let { itemData["name"] = it }
            request.categoryId?.let { itemData["category_id"] = it }
            request.unitOfMeasure?.let { itemData["unit_of_measure"] = it }
            request.minimumStockLevel?.let { itemData["minimum_stock_level"] = it }
            request.godownId?.let { itemData["godown_id"] = it }
            request.rackId?.let { itemData["rack_id"] = it }
            request.isActive?.let { itemData["is_active"] = it }

            val response = apiService.updateItem("eq.$id", itemData)

            if (response.isSuccessful) {
                val items = response.body()
                if (!items.isNullOrEmpty()) {
                    val updatedItem = items.first()
                    request.cuisineIds?.let { cuisineIds ->
                        deleteItemCuisineAssociations(id)
                        if (cuisineIds.isNotEmpty()) {
                            createItemCuisineAssociations(id, cuisineIds)
                        }
                    }
                    emit(Result.success(updatedItem))
                } else {
                    emit(Result.failure(Exception("Updated successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Result.failure(Exception("Failed to update item: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun deleteItem(id: String): Flow<Result<Unit>> = flow {
        try {
            deleteItemCuisineAssociations(id)
            val response = apiService.deleteItem("eq.$id")

            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Result.failure(Exception("Failed to delete item: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private suspend fun createItemCuisineAssociations(itemId: String, cuisineIds: List<String>) {
        try {
            val itemCuisines = cuisineIds.map { cuisineId ->
                mapOf("item_id" to itemId, "cuisine_id" to cuisineId)
            }
            apiService.createItemCuisines(itemCuisines)
        } catch (e: Exception) {
            Log.e("ItemRepo", "Error creating item cuisine associations", e)
        }
    }

    private suspend fun deleteItemCuisineAssociations(itemId: String) {
        try {
            apiService.deleteItemCuisines("eq.$itemId")
        } catch (e: Exception) {
            Log.e("ItemRepo", "Error deleting item cuisine associations", e)
        }
    }
}