package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.management.Vendor
import com.attil.inventory.data.model.management.CreateVendorRequest
import com.attil.inventory.data.model.management.UpdateVendorRequest
import com.attil.inventory.data.remote.VendorApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepository @Inject constructor(
    private val apiService: VendorApiService
) {

    suspend fun getAllVendors(): Flow<Result<List<Vendor>>> = flow {
        val result = try {
            Log.d("VendorRepo", "Fetching all vendors...")
            val response = apiService.getAllVendors()
            Log.d("VendorRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("VendorRepo", "Error fetching vendors: $errorBody")
                Result.failure(Exception("Failed to fetch vendors: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("VendorRepo", "Exception fetching vendors", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun createVendor(request: CreateVendorRequest): Flow<Result<Vendor>> = flow {
        val result = try {
            Log.d("VendorRepo", "Creating vendor: $request")
            val response = apiService.createVendor(request)
            Log.d("VendorRepo", "Create response code: ${response.code()}")

            if (response.isSuccessful) {
                val vendors = response.body()
                if (!vendors.isNullOrEmpty()) {
                    Result.success(vendors.first())
                } else {
                    Result.failure(Exception("Created successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("VendorRepo", "Error creating vendor: $errorBody")
                Result.failure(Exception("Failed to create vendor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("VendorRepo", "Exception creating vendor", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun updateVendor(id: String, request: UpdateVendorRequest): Flow<Result<Vendor>> = flow {
        val result = try {
            Log.d("VendorRepo", "Updating vendor $id: $request")
            val response = apiService.updateVendor("eq.$id", request)
            Log.d("VendorRepo", "Update response code: ${response.code()}")

            if (response.isSuccessful) {
                val vendors = response.body()
                if (!vendors.isNullOrEmpty()) {
                    Result.success(vendors.first())
                } else {
                    Result.failure(Exception("Updated successfully but no data returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("VendorRepo", "Error updating vendor: $errorBody")
                Result.failure(Exception("Failed to update vendor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("VendorRepo", "Exception updating vendor", e)
            Result.failure(e)
        }
        emit(result)
    }

    suspend fun deleteVendor(id: String): Flow<Result<Unit>> = flow {
        val result = try {
            Log.d("VendorRepo", "Deleting vendor: $id")
            val response = apiService.deleteVendor("eq.$id")
            Log.d("VendorRepo", "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("VendorRepo", "Error deleting vendor: $errorBody")
                Result.failure(Exception("Failed to delete vendor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("VendorRepo", "Exception deleting vendor", e)
            Result.failure(e)
        }
        emit(result)
    }
}