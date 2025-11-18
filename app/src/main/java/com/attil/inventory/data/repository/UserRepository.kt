package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.master.User
import com.attil.inventory.data.model.master.CreateUserRequest
import com.attil.inventory.data.model.master.UpdateUserRequest
import com.attil.inventory.data.model.master.ChangePasswordRequest
import com.attil.inventory.data.model.master.ResetPasswordRequest
import com.attil.inventory.data.remote.UserApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    suspend fun getAllUsers(): Flow<Result<List<User>>> = flow {
        try {
            Log.d("UserRepo", "Fetching all users...")
            val response = userApiService.getAllUsers()
            Log.d("UserRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                Log.d("UserRepo", "Successfully fetched ${users.size} users")
                emit(Result.success(users))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error fetching users: $errorBody")
                emit(Result.failure(Exception("Failed to fetch users: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception fetching users", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getUserById(id: String): Flow<Result<User?>> = flow {
        try {
            Log.d("UserRepo", "Fetching user by id: $id")
            val response = userApiService.getUserById("eq.$id")
            Log.d("UserRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                emit(Result.success(users.firstOrNull()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error fetching user: $errorBody")
                emit(Result.failure(Exception("Failed to fetch user: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception fetching user", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getUserByUsername(username: String): Flow<Result<User?>> = flow {
        try {
            Log.d("UserRepo", "Fetching user by username: $username")
            val response = userApiService.getUserByUsername("eq.$username")
            Log.d("UserRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                emit(Result.success(users.firstOrNull()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error fetching user: $errorBody")
                emit(Result.failure(Exception("Failed to fetch user: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception fetching user", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getUserByEmail(email: String): Flow<Result<User?>> = flow {
        try {
            Log.d("UserRepo", "Fetching user by email: $email")
            val response = userApiService.getUserByEmail("eq.$email")
            Log.d("UserRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                emit(Result.success(users.firstOrNull()))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error fetching user: $errorBody")
                emit(Result.failure(Exception("Failed to fetch user: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception fetching user", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getUsersByRole(roleId: String): Flow<Result<List<User>>> = flow {
        try {
            Log.d("UserRepo", "Fetching users by role: $roleId")
            val response = userApiService.getUsersByRole("eq.$roleId")
            Log.d("UserRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                emit(Result.success(users))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error fetching users by role: $errorBody")
                emit(Result.failure(Exception("Failed to fetch users by role: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception fetching users by role", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getActiveUsers(): Flow<Result<List<User>>> = flow {
        try {
            Log.d("UserRepo", "Fetching active users...")
            val response = userApiService.getActiveUsers()
            Log.d("UserRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                emit(Result.success(users))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error fetching active users: $errorBody")
                emit(Result.failure(Exception("Failed to fetch active users: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception fetching active users", e)
            emit(Result.failure(e))
        }
    }

    suspend fun createUser(createUserRequest: CreateUserRequest): Flow<Result<User>> = flow {
        try {
            Log.d("UserRepo", "Creating user: $createUserRequest")
            val response = userApiService.createUser(createUserRequest)
            Log.d("UserRepo", "Create response code: ${response.code()}")

            if (response.isSuccessful) {
                val createdUsers = response.body() ?: emptyList()
                if (createdUsers.isNotEmpty()) {
                    emit(Result.success(createdUsers.first()))
                } else {
                    emit(Result.failure(Exception("Created successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error creating user: $errorBody")
                emit(Result.failure(Exception("Failed to create user: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception creating user", e)
            emit(Result.failure(e))
        }
    }

    suspend fun updateUser(id: String, updateUserRequest: UpdateUserRequest): Flow<Result<User>> = flow {
        try {
            Log.d("UserRepo", "Updating user $id: $updateUserRequest")
            val response = userApiService.updateUser("eq.$id", updateUserRequest)
            Log.d("UserRepo", "Update response code: ${response.code()}")

            if (response.isSuccessful) {
                val updatedUsers = response.body() ?: emptyList()
                if (updatedUsers.isNotEmpty()) {
                    emit(Result.success(updatedUsers.first()))
                } else {
                    emit(Result.failure(Exception("Updated successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error updating user: $errorBody")
                emit(Result.failure(Exception("Failed to update user: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception updating user", e)
            emit(Result.failure(e))
        }
    }

    suspend fun updateUserScreenPermissions(userId: String, screenPermissions: List<String>): Flow<Result<User>> = flow {
        try {
            Log.d("UserRepo", "Updating screen permissions for user $userId: $screenPermissions")
            val updateRequest = UpdateUserRequest(screenPermissions = screenPermissions)
            val response = userApiService.updateUser("eq.$userId", updateRequest)
            Log.d("UserRepo", "Update permissions response code: ${response.code()}")

            if (response.isSuccessful) {
                val updatedUsers = response.body() ?: emptyList()
                if (updatedUsers.isNotEmpty()) {
                    emit(Result.success(updatedUsers.first()))
                } else {
                    emit(Result.failure(Exception("Permissions updated successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error updating permissions: $errorBody")
                emit(Result.failure(Exception("Failed to update permissions: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception updating permissions", e)
            emit(Result.failure(e))
        }
    }

    suspend fun changePassword(id: String, changePasswordRequest: ChangePasswordRequest): Flow<Result<User>> = flow {
        try {
            Log.d("UserRepo", "Changing password for user: $id")
            val response = userApiService.changePassword("eq.$id", changePasswordRequest)
            Log.d("UserRepo", "Change password response code: ${response.code()}")

            if (response.isSuccessful) {
                val updatedUsers = response.body() ?: emptyList()
                if (updatedUsers.isNotEmpty()) {
                    emit(Result.success(updatedUsers.first()))
                } else {
                    emit(Result.failure(Exception("Password changed successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error changing password: $errorBody")
                emit(Result.failure(Exception("Failed to change password: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception changing password", e)
            emit(Result.failure(e))
        }
    }

    suspend fun resetPassword(id: String, resetPasswordRequest: ResetPasswordRequest): Flow<Result<User>> = flow {
        try {
            Log.d("UserRepo", "Resetting password for user: $id")
            val response = userApiService.resetPassword("eq.$id", resetPasswordRequest)
            Log.d("UserRepo", "Reset password response code: ${response.code()}")

            if (response.isSuccessful) {
                val updatedUsers = response.body() ?: emptyList()
                if (updatedUsers.isNotEmpty()) {
                    emit(Result.success(updatedUsers.first()))
                } else {
                    emit(Result.failure(Exception("Password reset successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error resetting password: $errorBody")
                emit(Result.failure(Exception("Failed to reset password: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception resetting password", e)
            emit(Result.failure(e))
        }
    }

    suspend fun toggleUserStatus(id: String, isActive: Boolean): Flow<Result<User>> = flow {
        try {
            Log.d("UserRepo", "Toggling user status for $id to $isActive")
            val statusUpdate = mapOf("is_active" to isActive)
            val response = userApiService.toggleUserStatus("eq.$id", statusUpdate)
            Log.d("UserRepo", "Toggle status response code: ${response.code()}")

            if (response.isSuccessful) {
                val updatedUsers = response.body() ?: emptyList()
                if (updatedUsers.isNotEmpty()) {
                    emit(Result.success(updatedUsers.first()))
                } else {
                    emit(Result.failure(Exception("Status updated successfully but no data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error toggling user status: $errorBody")
                emit(Result.failure(Exception("Failed to toggle user status: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception toggling user status", e)
            emit(Result.failure(e))
        }
    }

    suspend fun deleteUser(id: String): Flow<Result<Unit>> = flow {
        try {
            Log.d("UserRepo", "Deleting user: $id")
            val response = userApiService.deleteUser("eq.$id")
            Log.d("UserRepo", "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error deleting user: $errorBody")
                emit(Result.failure(Exception("Failed to delete user: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception deleting user", e)
            emit(Result.failure(e))
        }
    }

    suspend fun searchUsers(query: String): Flow<Result<List<User>>> = flow {
        try {
            Log.d("UserRepo", "Searching users with query: $query")
            val searchQuery = "(username.ilike.*$query*,email.ilike.*$query*,full_name.ilike.*$query*,phone.ilike.*$query*)"
            val response = userApiService.searchUsers(searchQuery)
            Log.d("UserRepo", "Search response code: ${response.code()}")

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                emit(Result.success(users))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepo", "Error searching users: $errorBody")
                emit(Result.failure(Exception("Failed to search users: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception searching users", e)
            emit(Result.failure(e))
        }
    }

    // User Cuisines Management
    suspend fun updateUserCuisines(userId: String, cuisineIds: List<String>): Flow<Result<Unit>> = flow {
        try {
            Log.d("UserRepo", "Updating user cuisines for user $userId with ${cuisineIds.size} cuisines")

            // First, remove all existing cuisine assignments
            val deleteResponse = userApiService.removeAllCuisinesFromUser("eq.$userId")
            if (!deleteResponse.isSuccessful) {
                val errorBody = deleteResponse.errorBody()?.string()
                Log.e("UserRepo", "Error removing existing cuisines: $errorBody")
                emit(Result.failure(Exception("Failed to remove existing cuisines: ${deleteResponse.code()} - $errorBody")))
                return@flow
            }

            // Then, add new cuisine assignments
            for (cuisineId in cuisineIds) {
                val assignment = mapOf("user_id" to userId, "cuisine_id" to cuisineId)
                val addResponse = userApiService.assignCuisineToUser(assignment)
                if (!addResponse.isSuccessful) {
                    val errorBody = addResponse.errorBody()?.string()
                    Log.e("UserRepo", "Error assigning cuisine $cuisineId: $errorBody")
                    emit(Result.failure(Exception("Failed to assign cuisine: ${addResponse.code()} - $errorBody")))
                    return@flow
                }
            }

            Log.d("UserRepo", "Successfully updated user cuisines")
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("UserRepo", "Exception updating user cuisines", e)
            emit(Result.failure(e))
        }
    }
}

