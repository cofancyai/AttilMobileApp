package com.attil.inventory.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.attil.inventory.data.model.AuthState
import com.attil.inventory.data.model.LoginRequest
import com.attil.inventory.data.model.master.User
import com.attil.inventory.data.remote.AuthApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_preferences")

class AuthRepository(
    private val apiService: AuthApiService,
    private val context: Context,
    private val gson: Gson
) {

    companion object {
        private val USER_KEY = stringPreferencesKey("user_data")
    }

    suspend fun login(username: String, password: String): Flow<AuthState> = flow {
        Log.d("AuthRepository", "=== LOGIN ATTEMPT STARTED ===")
        Log.d("AuthRepository", "Username: '$username', Password: '$password'")

        // Clear any existing auth data first
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        Log.d("AuthRepository", "Cleared existing auth data")

        emit(AuthState(isLoading = true))

        try {
            val loginRequest = LoginRequest(username, password)
            Log.d("AuthRepository", "Sending API request to authenticate_user")

            val response = apiService.login(loginRequest)
            Log.d("AuthRepository", "API Response - Code: ${response.code()}, Success: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("AuthRepository", "Raw Response Body: $responseBody")
                Log.d("AuthRepository", "Response Body Type: ${responseBody?.javaClass}")
                Log.d("AuthRepository", "Users count: ${responseBody?.size ?: 0}")

                if (!responseBody.isNullOrEmpty()) {
                    try {
                        val user = responseBody.first()
                        Log.d("AuthRepository", "✅ USER PARSED SUCCESSFULLY")
                        Log.d("AuthRepository", "User ID: ${user.id}")
                        Log.d("AuthRepository", "Username: ${user.username}")
                        Log.d("AuthRepository", "Email: ${user.email}")
                        Log.d("AuthRepository", "Full Name: ${user.fullName}")
                        Log.d("AuthRepository", "Phone: ${user.phone}")
                        Log.d("AuthRepository", "Role ID: ${user.roleId}")
                        Log.d("AuthRepository", "Is Active: ${user.isActive}")
                        Log.d("AuthRepository", "Screen Permissions: ${user.screenPermissions}")

                        saveUserData(user)
                        emit(AuthState(isLoading = false, isLoggedIn = true, user = user))
                    } catch (e: Exception) {
                        Log.e("AuthRepository", "❌ ERROR PARSING USER DATA: ${e.message}", e)
                        Log.e("AuthRepository", "Raw user data: ${responseBody.first()}")
                        emit(AuthState(isLoading = false, error = "Error parsing user data: ${e.message}"))
                    }
                } else {
                    Log.d("AuthRepository", "❌ LOGIN FAILED - No users returned (Invalid credentials)")
                    emit(AuthState(isLoading = false, error = "Invalid credentials"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "❌ API CALL FAILED - Code: ${response.code()}")
                Log.e("AuthRepository", "Error body: $errorBody")
                emit(AuthState(isLoading = false, error = "Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ EXCEPTION during login: ${e.message}", e)
            emit(AuthState(isLoading = false, error = e.message))
        }
        Log.d("AuthRepository", "=== LOGIN ATTEMPT FINISHED ===")
    }

    suspend fun changePassword(userId: String, currentPassword: String, newPassword: String): Flow<Result<String>> = flow {
        try {
            val requestBody = mapOf(
                "user_id_input" to userId,
                "current_password_input" to currentPassword,
                "new_password_input" to newPassword
            )

            val response = apiService.changePassword(requestBody)

            if (response.isSuccessful) {
                val responseBody = response.body()
                val success = responseBody?.get("success") as? Boolean ?: false
                val message = responseBody?.get("message") as? String ?: "Unknown response"

                if (success) {
                    emit(Result.success(message))
                } else {
                    emit(Result.failure(Exception(message)))
                }
            } else {
                emit(Result.failure(Exception("Failed to change password: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private suspend fun saveUserData(user: User) {
        Log.d("AuthRepository", "Saving user data to DataStore: ${user.username}")
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = gson.toJson(user)
        }
    }

    fun getCurrentUser(): Flow<User?> {
        return context.dataStore.data.map { preferences ->
            val userJson = preferences[USER_KEY]
            val user = if (userJson != null) {
                try {
                    gson.fromJson(userJson, User::class.java)
                } catch (e: Exception) { null }
            } else null
            Log.d("AuthRepository", "getCurrentUser: ${user?.username ?: "null"}")
            user
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            val isLoggedIn = preferences[USER_KEY] != null
            Log.d("AuthRepository", "isLoggedIn check: $isLoggedIn")
            isLoggedIn
        }
    }

    suspend fun logout() {
        Log.d("AuthRepository", "Logging out - clearing DataStore")
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}