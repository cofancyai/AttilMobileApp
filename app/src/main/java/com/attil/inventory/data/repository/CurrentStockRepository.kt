package com.attil.inventory.data.repository

import com.attil.inventory.data.model.management.CurrentStock
import com.attil.inventory.data.remote.CurrentStockApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentStockRepository @Inject constructor(
    private val apiService: CurrentStockApiService
) {
    fun getAllCurrentStocks(): Flow<Result<List<CurrentStock>>> = flow {
        try {
            println("CurrentStockRepository: Making API call to get all current stocks")
            val response = apiService.getAllCurrentStocks()
            println("CurrentStockRepository: Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val stocks = response.body() ?: emptyList()
                println("CurrentStockRepository: Success - Got ${stocks.size} stock records")
                emit(Result.success(stocks))
            } else {
                val errorBody = response.errorBody()?.string()
                println("CurrentStockRepository: API Error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("API Error ${response.code()}: $errorBody")))
            }
        } catch (e: Exception) {
            println("CurrentStockRepository: Exception in getAllCurrentStocks - ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    fun getCurrentStocksByCategory(categoryName: String): Flow<Result<List<CurrentStock>>> = flow {
        try {
            println("CurrentStockRepository: Getting stocks for category: $categoryName")
            val response = apiService.getCurrentStocksByCategory(categoryName)
            
            if (response.isSuccessful) {
                val stocks = response.body() ?: emptyList()
                println("CurrentStockRepository: Success - Got ${stocks.size} stocks for category $categoryName")
                emit(Result.success(stocks))
            } else {
                val errorBody = response.errorBody()?.string()
                println("CurrentStockRepository: Category filter error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("Failed to filter by category: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            println("CurrentStockRepository: Exception in getCurrentStocksByCategory - ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getCurrentStocksByGodown(godownName: String): Flow<Result<List<CurrentStock>>> = flow {
        try {
            println("CurrentStockRepository: Getting stocks for godown: $godownName")
            val response = apiService.getCurrentStocksByGodown(godownName)
            
            if (response.isSuccessful) {
                val stocks = response.body() ?: emptyList()
                println("CurrentStockRepository: Success - Got ${stocks.size} stocks for godown $godownName")
                emit(Result.success(stocks))
            } else {
                val errorBody = response.errorBody()?.string()
                println("CurrentStockRepository: Godown filter error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("Failed to filter by godown: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            println("CurrentStockRepository: Exception in getCurrentStocksByGodown - ${e.message}")
            emit(Result.failure(e))
        }
    }

    fun getLowStockItems(): Flow<Result<List<CurrentStock>>> = flow {
        try {
            println("CurrentStockRepository: Getting low stock items")
            val response = apiService.getLowStockItems()
            
            if (response.isSuccessful) {
                val stocks = response.body() ?: emptyList()
                println("CurrentStockRepository: Success - Got ${stocks.size} low stock items")
                emit(Result.success(stocks))
            } else {
                val errorBody = response.errorBody()?.string()
                println("CurrentStockRepository: Low stock filter error - Code: ${response.code()}, Body: $errorBody")
                emit(Result.failure(Exception("Failed to get low stock items: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            println("CurrentStockRepository: Exception in getLowStockItems - ${e.message}")
            emit(Result.failure(e))
        }
    }
}

