package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.master.Role
import com.attil.inventory.data.model.master.CreateRoleRequest
import com.attil.inventory.data.model.master.UpdateRoleRequest
import com.attil.inventory.data.remote.RoleApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRepository @Inject constructor(
    private val roleApiService: RoleApiService
) {
    suspend fun getAllRoles(): Flow<Result<List<Role>>> = flow {
        try {
            Log.d("RoleRepo", "Fetching all roles...")
            val response = roleApiService.getAllRoles()
            Log.d("RoleRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val roles = response.body() ?: emptyList()
                Log.d("RoleRepo", "Successfully fetched ${roles.size} roles")
                emit(Result.success(roles))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RoleRepo", "Error fetching roles: $errorBody")
                emit(Result.failure(Exception("Failed to fetch roles: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RoleRepo", "Exception fetching roles", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getRoleById(id: String): Flow<Result<Role?>> = flow {
        try {
            Log.d("RoleRepo", "Fetching role by id: $id")
            val response = roleApiService.getRoleById("eq.$id")
            Log.d("RoleRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val roles = response.body() ?: emptyList()
                emit(Result.success(roles.firstOrNull()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RoleRepo", "Error fetching role: $errorBody")
                emit(Result.failure(Exception("Failed to fetch role: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RoleRepo", "Exception fetching role", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getRoleByName(name: String): Flow<Result<Role?>> = flow {
        try {
            Log.d("RoleRepo", "Fetching role by name: $name")
            val response = roleApiService.getRoleByName("eq.$name")
            Log.d("RoleRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val roles = response.body() ?: emptyList()
                emit(Result.success(roles.firstOrNull()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RoleRepo", "Error fetching role: $errorBody")
                emit(Result.failure(Exception("Failed to fetch role: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RoleRepo", "Exception fetching role", e)
            emit(Result.failure(e))
        }
    }

    suspend fun createRole(createRoleRequest: CreateRoleRequest): Flow<Result<Role>> = flow {
        try {
            Log.d("RoleRepo", "Creating role: $createRoleRequest")
            val response = roleApiService.createRole(createRoleRequest)
            Log.d("RoleRepo", "Create response code: ${response.code()}")

            if (response.isSuccessful) {
                val createdRoles = response.body() ?: emptyList()
                if (createdRoles.isNotEmpty()) {
                    emit(Result.success(createdRoles.first()))
                } else {
                    emit(Result.failure(Exception("Created successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RoleRepo", "Error creating role: $errorBody")
                emit(Result.failure(Exception("Failed to create role: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RoleRepo", "Exception creating role", e)
            emit(Result.failure(e))
        }
    }

    suspend fun updateRole(id: String, updateRoleRequest: UpdateRoleRequest): Flow<Result<Role>> = flow {
        try {
            Log.d("RoleRepo", "Updating role $id: $updateRoleRequest")
            val response = roleApiService.updateRole("eq.$id", updateRoleRequest)
            Log.d("RoleRepo", "Update response code: ${response.code()}")

            if (response.isSuccessful) {
                val updatedRoles = response.body() ?: emptyList()
                if (updatedRoles.isNotEmpty()) {
                    emit(Result.success(updatedRoles.first()))
                } else {
                    emit(Result.failure(Exception("Updated successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RoleRepo", "Error updating role: $errorBody")
                emit(Result.failure(Exception("Failed to update role: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RoleRepo", "Exception updating role", e)
            emit(Result.failure(e))
        }
    }

    suspend fun deleteRole(id: String): Flow<Result<Unit>> = flow {
        try {
            Log.d("RoleRepo", "Deleting role: $id")
            val response = roleApiService.deleteRole("eq.$id")
            Log.d("RoleRepo", "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RoleRepo", "Error deleting role: $errorBody")
                emit(Result.failure(Exception("Failed to delete role: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RoleRepo", "Exception deleting role", e)
            emit(Result.failure(e))
        }
    }

    suspend fun searchRoles(query: String): Flow<Result<List<Role>>> = flow {
        try {
            Log.d("RoleRepo", "Searching roles with query: $query")
            // Fix the search query to only use existing fields
            val searchQuery = "(name.ilike.*$query*,description.ilike.*$query*)"
            val response = roleApiService.searchRoles(searchQuery)
            Log.d("RoleRepo", "Search response code: ${response.code()}")

            if (response.isSuccessful) {
                val roles = response.body() ?: emptyList()
                emit(Result.success(roles))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RoleRepo", "Error searching roles: $errorBody")
                emit(Result.failure(Exception("Failed to search roles: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("RoleRepo", "Exception searching roles", e)
            emit(Result.failure(e))
        }
    }
}