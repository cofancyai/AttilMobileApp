package com.attil.inventory.data.repository

import com.attil.inventory.data.model.transaction.OutwardItem
import com.attil.inventory.data.model.transaction.CreateOutwardItemRequest
import com.attil.inventory.data.model.transaction.UpdateOutwardItemRequest
import com.attil.inventory.data.model.transaction.ItemWithStock
import com.attil.inventory.data.model.transaction.ItemForOutward
import com.attil.inventory.data.model.transaction.CategoryForOutward
import com.attil.inventory.data.remote.OutwardApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutwardRepository @Inject constructor(
    private val apiService: OutwardApiService
) {

    fun getAllOutwardItems(): Flow<Result<List<OutwardItem>>> = flow {
        try {
            val response = apiService.getAllOutwardItems()
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                emit(Result.failure(Exception("API Error ${response.code()}: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun createOutwardItem(request: CreateOutwardItemRequest): Flow<Result<List<OutwardItem>>> = flow {
        try {
            println("DEBUG - Sending request: $request")
            val response = apiService.createOutwardItem(request)
            println("DEBUG - Response code: ${response.code()}")
            println("DEBUG - Response message: ${response.message()}")
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG - Full error body: $errorBody")
                emit(Result.failure(Exception("Create API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("DEBUG - Exception: ${e.message}")
            println("DEBUG - Stack trace: ${e.printStackTrace()}")
            emit(Result.failure(e))
        }
    }

    fun updateOutwardItem(id: String, request: UpdateOutwardItemRequest): Flow<Result<List<OutwardItem>>> = flow {
        try {
            val response = apiService.updateOutwardItem(id, request)
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                emit(Result.failure(Exception("API Error ${response.code()}: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun deleteOutwardItem(id: String): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deleteOutwardItem(id)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("API Error ${response.code()}: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getItemsWithStock(): Flow<Result<List<ItemWithStock>>> = flow {
        try {
            val response = apiService.getItemsWithStock()
            if (response.isSuccessful) {
                val stockData = response.body() ?: emptyList()
                val itemsWithStock = stockData.mapNotNull { stockItem ->
                    try {
                        // Extract data from the Map returned by Supabase
                        val itemId = stockItem["item_id"]?.toString()
                        val itemName = stockItem["item_name"]?.toString()
                        val categoryName = stockItem["category_name"]?.toString()
                        val currentStock = (stockItem["current_stock"] as? Number)?.toDouble()
                        val unitOfMeasure = stockItem["unit_of_measure"]?.toString()

                        if (itemId != null && itemName != null && currentStock != null && unitOfMeasure != null && currentStock > 0) {
                            ItemWithStock(
                                item = ItemForOutward(
                                    id = itemId,
                                    name = itemName,
                                    unitOfMeasure = unitOfMeasure,
                                    categories = null
                                ),
                                currentStock = currentStock,
                                isSelected = false,
                                outwardQuantity = 0.0
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null // Skip invalid entries
                    }
                }
                emit(Result.success(itemsWithStock))
            } else {
                emit(Result.failure(Exception("API Error ${response.code()}: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getItemsWithStockByCategory(categoryName: String): Flow<Result<List<ItemWithStock>>> = flow {
        try {
            val response = apiService.getItemsWithStockByCategory(categoryName)
            if (response.isSuccessful) {
                val stockData = response.body() ?: emptyList()
                val itemsWithStock = stockData.mapNotNull { stockItem ->
                    try {
                        // Extract data from the Map returned by Supabase
                        val itemId = stockItem["item_id"]?.toString()
                        val itemName = stockItem["item_name"]?.toString()
                        val categoryNameFromApi = stockItem["category_name"]?.toString()
                        val currentStock = (stockItem["current_stock"] as? Number)?.toDouble()
                        val unitOfMeasure = stockItem["unit_of_measure"]?.toString()

                        if (itemId != null && itemName != null && currentStock != null && unitOfMeasure != null && currentStock > 0) {
                            ItemWithStock(
                                item = ItemForOutward(
                                    id = itemId,
                                    name = itemName,
                                    unitOfMeasure = unitOfMeasure,
                                    categories = categoryNameFromApi?.let { CategoryForOutward("", it) }
                                ),
                                currentStock = currentStock,
                                isSelected = false,
                                outwardQuantity = 0.0
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null // Skip invalid entries
                    }
                }
                emit(Result.success(itemsWithStock))
            } else {
                emit(Result.failure(Exception("API Error ${response.code()}: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}