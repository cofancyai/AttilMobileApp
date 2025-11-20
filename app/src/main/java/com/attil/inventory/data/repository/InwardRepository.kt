package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.transaction.CreateInwardItemRequest
import com.attil.inventory.data.model.transaction.InwardItem
import com.attil.inventory.data.model.transaction.UpdateInwardItemRequest
import com.attil.inventory.data.remote.InwardApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InwardRepository @Inject constructor(
    private val apiService: InwardApiService
) {

    suspend fun getAllInwardItems(): Flow<Result<List<InwardItem>>> = flow {
        val result = try {
            Log.d("InwardRepo", "Fetching all inward items...")
            val response = apiService.getAllInwardItems()
            Log.d("InwardRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("InwardRepo", "Error fetching inward items: $errorBody")
                Result.failure(Exception("Failed to fetch inward items: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("InwardRepo", "Exception fetching inward items", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun createInwardItem(request: CreateInwardItemRequest): Flow<Result<InwardItem>> = flow {
        val result = try {
            Log.d("InwardRepo", "Creating inward item: $request")

            val itemData = hashMapOf<String, Any>()
            itemData["item_id"] = request.itemId
            itemData["vendor_name"] = request.vendorName
            itemData["purchase_date"] = request.purchaseDate
            itemData["inward_quantity"] = request.inwardQuantity
            itemData["price_per_unit"] = request.pricePerUnit

            request.vendorContact?.let { itemData["vendor_contact"] = it }
            request.vendorAddress?.let { itemData["vendor_address"] = it }
            request.priceWithoutGst?.let { itemData["price_without_gst"] = it }
            request.priceWithGst?.let { itemData["price_with_gst"] = it }
            request.gstPercentage?.let { itemData["gst_percentage"] = it }
            request.billNumber?.let { itemData["bill_number"] = it }
            request.expiryDate?.let { itemData["expiry_date"] = it }
            request.cuisineId?.let { itemData["cuisine_id"] = it }
            request.createdBy?.let { itemData["created_by"] = it }

            val response = apiService.createInwardItem(itemData)

            if (response.isSuccessful) {
                val items = response.body()
                if (!items.isNullOrEmpty()) {
                    Result.success(items.first())
                } else {
                    Result.failure(Exception("Created successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to create inward item: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("InwardRepo", "Exception creating inward item", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun updateInwardItem(id: String, request: UpdateInwardItemRequest): Flow<Result<InwardItem>> = flow {
        val result = try {
            val itemData = hashMapOf<String, Any>()

            request.itemId?.let { itemData["item_id"] = it }
            request.vendorName?.let { itemData["vendor_name"] = it }
            request.vendorContact?.let { itemData["vendor_contact"] = it }
            request.vendorAddress?.let { itemData["vendor_address"] = it }
            request.purchaseDate?.let { itemData["purchase_date"] = it }
            request.inwardQuantity?.let { itemData["inward_quantity"] = it }
            request.pricePerUnit?.let { itemData["price_per_unit"] = it }
            request.priceWithoutGst?.let { itemData["price_without_gst"] = it }
            request.priceWithGst?.let { itemData["price_with_gst"] = it }
            request.gstPercentage?.let { itemData["gst_percentage"] = it }
            request.billNumber?.let { itemData["bill_number"] = it }
            request.expiryDate?.let { itemData["expiry_date"] = it }
            request.cuisineId?.let { itemData["cuisine_id"] = it }

            val response = apiService.updateInwardItem("eq.$id", itemData)

            if (response.isSuccessful) {
                val items = response.body()
                if (!items.isNullOrEmpty()) {
                    Result.success(items.first())
                } else {
                    Result.failure(Exception("Updated successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to update inward item: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun deleteInwardItem(id: String): Flow<Result<Unit>> = flow {
        val result = try {
            val response = apiService.deleteInwardItem("eq.$id")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to delete inward item: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }
}