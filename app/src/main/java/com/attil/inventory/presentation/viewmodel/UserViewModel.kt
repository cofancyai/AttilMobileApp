package com.attil.inventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.attil.inventory.data.model.master.User
import com.attil.inventory.data.model.master.Role
import com.attil.inventory.data.model.master.CreateUserRequest
import com.attil.inventory.data.model.master.UpdateUserRequest
import com.attil.inventory.data.model.master.ResetPasswordRequest
import com.attil.inventory.data.repository.UserRepository
import com.attil.inventory.data.repository.RoleRepository
import com.attil.inventory.data.repository.CuisineRepository
import com.attil.inventory.data.model.management.Cuisine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val cuisineRepository: CuisineRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    private val _roles = MutableStateFlow<List<Role>>(emptyList())
    val roles: StateFlow<List<Role>> = _roles.asStateFlow()

    private val _cuisines = MutableStateFlow<List<Cuisine>>(emptyList())
    val cuisines: StateFlow<List<Cuisine>> = _cuisines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRoleFilter = MutableStateFlow<String?>(null)
    val selectedRoleFilter: StateFlow<String?> = _selectedRoleFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow<Boolean?>(null)
    val statusFilter: StateFlow<Boolean?> = _statusFilter.asStateFlow()

    init {
        Log.d("UserViewModel", "UserViewModel initialized")
        loadUsers()
        loadRoles()
        loadCuisines()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                userRepository.getAllUsers().collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { userList ->
                            Log.d("UserViewModel", "Successfully loaded ${userList.size} users")
                            _users.value = userList
                            _filteredUsers.value = userList
                            _successMessage.value = "Users loaded successfully"
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error loading users", exception)
                            _errorMessage.value = exception.message ?: "Failed to load users"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception loading users", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun loadRoles() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                roleRepository.getAllRoles().collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { roleList ->
                            Log.d("UserViewModel", "Successfully loaded ${roleList.size} roles")
                            _roles.value = roleList
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error loading roles", exception)
                            _errorMessage.value = exception.message ?: "Failed to load roles"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception loading roles", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun loadCuisines() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                cuisineRepository.getAllCuisines().collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { cuisineList ->
                            Log.d("UserViewModel", "Successfully loaded ${cuisineList.size} cuisines")
                            _cuisines.value = cuisineList
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error loading cuisines", exception)
                            _errorMessage.value = exception.message ?: "Failed to load cuisines"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception loading cuisines", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun createUser(
        username: String,
        email: String,
        password: String,
        fullName: String,
        phone: String?,
        roleId: String?,
        cuisineId: String?,
        isActive: Boolean,
        cuisineIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val createUserRequest = CreateUserRequest(
                    username = username,
                    email = email,
                    fullName = fullName,
                    phone = phone,
                    roleId = roleId,
                    cuisineId = cuisineId,
                    password = password,
                    isActive = isActive
                )

                userRepository.createUser(createUserRequest).collectLatest { result ->
                    result.fold(
                        onSuccess = { createdUser ->
                            Log.d("UserViewModel", "Successfully created user: ${createdUser.username}")

                            // Update user cuisines in junction table if cuisineIds provided
                            if (cuisineIds.isNotEmpty()) {
                                userRepository.updateUserCuisines(createdUser.id, cuisineIds).collectLatest { cuisineResult ->
                                    cuisineResult.fold(
                                        onSuccess = {
                                            Log.d("UserViewModel", "Successfully assigned ${cuisineIds.size} cuisines to user")
                                            _successMessage.value = "User created successfully with ${cuisineIds.size} cuisine(s)"
                                            _isLoading.value = false
                                            loadUsers() // Refresh the list
                                        },
                                        onFailure = { exception ->
                                            Log.e("UserViewModel", "Error assigning cuisines", exception)
                                            _errorMessage.value = "User created but failed to assign cuisines: ${exception.message}"
                                            _isLoading.value = false
                                            loadUsers()
                                        }
                                    )
                                }
                            } else {
                                _successMessage.value = "User created successfully"
                                _isLoading.value = false
                                loadUsers()
                            }
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error creating user", exception)
                            _errorMessage.value = exception.message ?: "Failed to create user"
                            _isLoading.value = false
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception in createUser", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun updateUser(
        userId: String,
        username: String?,
        email: String?,
        fullName: String?,
        phone: String?,
        roleId: String?,
        cuisineId: String?,
        isActive: Boolean?,
        cuisineIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val updateUserRequest = UpdateUserRequest(
                    username = username,
                    email = email,
                    fullName = fullName,
                    phone = phone,
                    roleId = roleId,
                    cuisineId = cuisineId,
                    isActive = isActive
                )

                userRepository.updateUser(userId, updateUserRequest).collectLatest { result ->
                    result.fold(
                        onSuccess = { updatedUser ->
                            Log.d("UserViewModel", "Successfully updated user: ${updatedUser.username}")

                            // Update user cuisines in junction table if cuisineIds provided
                            if (cuisineIds.isNotEmpty()) {
                                userRepository.updateUserCuisines(userId, cuisineIds).collectLatest { cuisineResult ->
                                    cuisineResult.fold(
                                        onSuccess = {
                                            Log.d("UserViewModel", "Successfully updated ${cuisineIds.size} cuisines for user")
                                            _successMessage.value = "User updated successfully with ${cuisineIds.size} cuisine(s)"
                                            _isLoading.value = false
                                            loadUsers() // Refresh the list
                                        },
                                        onFailure = { exception ->
                                            Log.e("UserViewModel", "Error updating cuisines", exception)
                                            _errorMessage.value = "User updated but failed to update cuisines: ${exception.message}"
                                            _isLoading.value = false
                                            loadUsers()
                                        }
                                    )
                                }
                            } else {
                                _successMessage.value = "User updated successfully"
                                _isLoading.value = false
                                loadUsers()
                            }
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error updating user", exception)
                            _errorMessage.value = exception.message ?: "Failed to update user"
                            _isLoading.value = false
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception in updateUser", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun resetUserPassword(userId: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val resetPasswordRequest = ResetPasswordRequest(newPassword)

                userRepository.resetPassword(userId, resetPasswordRequest).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { updatedUser ->
                            Log.d("UserViewModel", "Successfully reset password for user: ${updatedUser.username}")
                            _successMessage.value = "Password reset successfully"
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error resetting password", exception)
                            _errorMessage.value = exception.message ?: "Failed to reset password"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception in resetPassword", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                userRepository.deleteUser(userId).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = {
                            Log.d("UserViewModel", "Successfully deleted user")
                            _successMessage.value = "User deleted successfully"
                            loadUsers() // Refresh the list
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error deleting user", exception)
                            _errorMessage.value = exception.message ?: "Failed to delete user"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception in deleteUser", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun searchUsers(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _filteredUsers.value = _users.value
        } else {
            performServerSearch(query)
        }
    }

    private fun performServerSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                userRepository.searchUsers(query).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { userList ->
                            Log.d("UserViewModel", "Successfully searched ${userList.size} users")
                            _filteredUsers.value = userList
                        },
                        onFailure = { exception ->
                            Log.e("UserViewModel", "Error searching users", exception)
                            _errorMessage.value = exception.message ?: "Failed to search users"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception searching users", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun filterByRole(roleId: String?) {
        _selectedRoleFilter.value = roleId
        if (roleId == null) {
            _filteredUsers.value = _users.value
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null

                try {
                    userRepository.getUsersByRole(roleId).collectLatest { result ->
                        _isLoading.value = false
                        result.fold(
                            onSuccess = { userList ->
                                Log.d("UserViewModel", "Successfully filtered ${userList.size} users by role")
                                _filteredUsers.value = userList
                            },
                            onFailure = { exception ->
                                Log.e("UserViewModel", "Error filtering users by role", exception)
                                _errorMessage.value = exception.message ?: "Failed to filter users by role"
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Exception filtering users by role", e)
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Unknown error occurred"
                }
            }
        }
    }

    fun filterByStatus(isActive: Boolean?) {
        _statusFilter.value = isActive
        if (isActive == null) {
            _filteredUsers.value = _users.value
        } else if (isActive) {
            viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null

                try {
                    userRepository.getActiveUsers().collectLatest { result ->
                        _isLoading.value = false
                        result.fold(
                            onSuccess = { userList ->
                                Log.d("UserViewModel", "Successfully filtered ${userList.size} active users")
                                _filteredUsers.value = userList
                            },
                            onFailure = { exception ->
                                Log.e("UserViewModel", "Error filtering active users", exception)
                                _errorMessage.value = exception.message ?: "Failed to filter active users"
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Exception filtering active users", e)
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Unknown error occurred"
                }
            }
        } else {
            _filteredUsers.value = _users.value.filter { !it.isActive }
        }
    }

    fun clearFilters() {
        _selectedRoleFilter.value = null
        _statusFilter.value = null
        _searchQuery.value = ""
        _filteredUsers.value = _users.value
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun selectUser(user: User?) {
        _selectedUser.value = user
    }

    fun updateUserScreenPermissions(userId: String, screenPermissions: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                userRepository.updateUserScreenPermissions(userId, screenPermissions).collectLatest { result ->
                    _isLoading.value = false

                    result.onSuccess { updatedUser ->
                        _successMessage.value = "Screen permissions updated successfully"

                        // Update the user in the local list
                        val currentUsers = _users.value.toMutableList()
                        val index = currentUsers.indexOfFirst { it.id == userId }
                        if (index != -1) {
                            currentUsers[index] = updatedUser
                            _users.value = currentUsers
                            _filteredUsers.value = currentUsers // Update filtered list too
                        }

                        // Update selected user if it's the same one
                        if (_selectedUser.value?.id == userId) {
                            _selectedUser.value = updatedUser
                        }
                    }.onFailure { exception ->
                        Log.e("UserViewModel", "Error updating screen permissions", exception)
                        _errorMessage.value = exception.message ?: "Failed to update screen permissions"
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("UserViewModel", "Exception updating screen permissions", e)
                _errorMessage.value = e.message ?: "Failed to update screen permissions"
            }
        }
    }
}