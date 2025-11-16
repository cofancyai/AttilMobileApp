package com.attil.inventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.attil.inventory.data.model.master.Role
import com.attil.inventory.data.model.master.CreateRoleRequest
import com.attil.inventory.data.model.master.UpdateRoleRequest
import com.attil.inventory.data.repository.RoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class RoleViewModel @Inject constructor(
    private val roleRepository: RoleRepository
) : ViewModel() {

    private val _roles = MutableStateFlow<List<Role>>(emptyList())
    val roles: StateFlow<List<Role>> = _roles.asStateFlow()

    private val _filteredRoles = MutableStateFlow<List<Role>>(emptyList())
    val filteredRoles: StateFlow<List<Role>> = _filteredRoles.asStateFlow()

    private val _selectedRole = MutableStateFlow<Role?>(null)
    val selectedRole: StateFlow<Role?> = _selectedRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        Log.d("RoleViewModel", "RoleViewModel initialized")
        loadRoles()
    }

    fun loadRoles() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                delay(100) // Small delay to show loading state
                roleRepository.getAllRoles().collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { roleList ->
                            Log.d("RoleViewModel", "Successfully loaded ${roleList.size} roles")
                            _roles.value = roleList
                            filterRoles(_searchQuery.value)
                        },
                        onFailure = { exception ->
                            Log.e("RoleViewModel", "Error loading roles", exception)
                            _errorMessage.value = exception.message ?: "Unknown error occurred"
                            _roles.value = emptyList()
                            _filteredRoles.value = emptyList()
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RoleViewModel", "Exception in loadRoles", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun createRole(name: String, displayName: String, description: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                // Use displayName as the role name since our model only has 'name' field
                val createRequest = CreateRoleRequest(
                    name = displayName,
                    description = description
                )

                roleRepository.createRole(createRequest).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { createdRole ->
                            Log.d("RoleViewModel", "Successfully created role: ${createdRole.name}")
                            _successMessage.value = "Role created successfully"
                            loadRoles() // Refresh the list
                        },
                        onFailure = { exception ->
                            Log.e("RoleViewModel", "Error creating role", exception)
                            _errorMessage.value = exception.message ?: "Failed to create role"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RoleViewModel", "Exception in createRole", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun updateRole(roleId: String, name: String, displayName: String, description: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                // Use displayName as the role name since our model only has 'name' field
                val updateRequest = UpdateRoleRequest(
                    name = displayName,
                    description = description
                )

                roleRepository.updateRole(roleId, updateRequest).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { updatedRole ->
                            Log.d("RoleViewModel", "Successfully updated role: ${updatedRole.name}")
                            _successMessage.value = "Role updated successfully"
                            loadRoles() // Refresh the list
                        },
                        onFailure = { exception ->
                            Log.e("RoleViewModel", "Error updating role", exception)
                            _errorMessage.value = exception.message ?: "Failed to update role"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RoleViewModel", "Exception in updateRole", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun deleteRole(roleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                roleRepository.deleteRole(roleId).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = {
                            Log.d("RoleViewModel", "Successfully deleted role")
                            _successMessage.value = "Role deleted successfully"
                            loadRoles() // Refresh the list
                        },
                        onFailure = { exception ->
                            Log.e("RoleViewModel", "Error deleting role", exception)
                            _errorMessage.value = exception.message ?: "Failed to delete role"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RoleViewModel", "Exception in deleteRole", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun toggleRoleStatus(roleId: String, isActive: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val updateRequest = UpdateRoleRequest(isActive = isActive)

                roleRepository.updateRole(roleId, updateRequest).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { updatedRole ->
                            Log.d("RoleViewModel", "Successfully toggled role status")
                            _successMessage.value = "Role status updated successfully"
                            loadRoles() // Refresh the list
                        },
                        onFailure = { exception ->
                            Log.e("RoleViewModel", "Error toggling role status", exception)
                            _errorMessage.value = exception.message ?: "Failed to update role status"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RoleViewModel", "Exception in toggleRoleStatus", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun searchRoles(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            filterRoles(query)
        } else {
            performServerSearch(query)
        }
    }

    private fun performServerSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                roleRepository.searchRoles(query).collectLatest { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { roleList ->
                            Log.d("RoleViewModel", "Successfully searched ${roleList.size} roles")
                            _filteredRoles.value = roleList
                        },
                        onFailure = { exception ->
                            Log.e("RoleViewModel", "Error searching roles", exception)
                            _errorMessage.value = exception.message ?: "Search failed"
                            filterRoles(query) // Fallback to local filtering
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RoleViewModel", "Exception in performServerSearch", e)
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                filterRoles(query) // Fallback to local filtering
            }
        }
    }

    private fun filterRoles(query: String) {
        val currentRoles = _roles.value
        _filteredRoles.value = if (query.isBlank()) {
            currentRoles
        } else {
            currentRoles.filter { role ->
                role.name.contains(query, ignoreCase = true) ||
                role.description?.contains(query, ignoreCase = true) == true
            }
        }
    }

    fun selectRole(role: Role?) {
        _selectedRole.value = role
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun getRoleById(roleId: String) {
        viewModelScope.launch {
            try {
                roleRepository.getRoleById(roleId).collectLatest { result ->
                    result.fold(
                        onSuccess = { role ->
                            _selectedRole.value = role
                        },
                        onFailure = { exception ->
                            Log.e("RoleViewModel", "Error fetching role by ID", exception)
                            _errorMessage.value = exception.message ?: "Failed to fetch role"
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RoleViewModel", "Exception in getRoleById", e)
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun getActiveRoles() {
        // Filter from existing roles since there's no specific getActiveRoles method in repository
        val currentRoles = _roles.value
        _filteredRoles.value = currentRoles.filter { it.isActive }
    }
}