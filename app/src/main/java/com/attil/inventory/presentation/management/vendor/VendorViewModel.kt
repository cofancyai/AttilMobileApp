package com.attil.inventory.presentation.management.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.Vendor
import com.attil.inventory.data.model.management.CreateVendorRequest
import com.attil.inventory.data.model.management.UpdateVendorRequest
import com.attil.inventory.data.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorUiState(
    val vendors: List<Vendor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class VendorViewModel @Inject constructor(
    private val repository: VendorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorUiState())
    val uiState: StateFlow<VendorUiState> = _uiState.asStateFlow()

    init {
        loadVendors()
    }

    fun loadVendors() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllVendors().collectLatest { result ->
                result.onSuccess { vendors ->
                    _uiState.value = _uiState.value.copy(
                        vendors = vendors,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun createVendor(name: String, address: String, contactNumber: String, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateVendorRequest(
                name = name.trim(),
                address = address.trim().takeIf { it.isNotEmpty() },
                contactNumber = contactNumber.trim().takeIf { it.isNotEmpty() },
                email = email.trim().takeIf { it.isNotEmpty() }
            )

            repository.createVendor(request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    loadVendors()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = exception.message ?: "Failed to create vendor"
                    )
                }
            }
        }
    }

    fun updateVendor(id: String, name: String, address: String, contactNumber: String, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateVendorRequest(
                name = name.trim(),
                address = address.trim().takeIf { it.isNotEmpty() },
                contactNumber = contactNumber.trim().takeIf { it.isNotEmpty() },
                email = email.trim().takeIf { it.isNotEmpty() }
            )

            repository.updateVendor(id, request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadVendors()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update vendor"
                    )
                }
            }
        }
    }

    fun deleteVendor(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            repository.deleteVendor(id).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    loadVendors()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message ?: "Failed to delete vendor"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}