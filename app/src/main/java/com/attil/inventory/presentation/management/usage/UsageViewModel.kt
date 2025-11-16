package com.attil.inventory.presentation.management.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.CreateUsageRequest
import com.attil.inventory.data.model.management.Usage
import com.attil.inventory.data.model.management.UpdateUsageRequest
import com.attil.inventory.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsageUiState(
    val usages: List<Usage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class UsageViewModel @Inject constructor(
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsageUiState())
    val uiState: StateFlow<UsageUiState> = _uiState.asStateFlow()

    init {
        loadUsages()
    }

    fun loadUsages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            usageRepository.getAllUsages().collectLatest { result ->
                result.onSuccess { usages ->
                    _uiState.value = _uiState.value.copy(
                        usages = usages,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load usages"
                    )
                }
            }
        }
    }

    fun createUsage(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateUsageRequest(
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                isActive = true
            )

            try {
                usageRepository.createUsage(request).collectLatest { result ->
                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(isCreating = false, error = null)
                        loadUsages()
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            error = exception.message ?: "Failed to create usage"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message ?: "Failed to create usage"
                )
            }
        }
    }

    fun updateUsage(id: String, name: String, description: String?, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateUsageRequest(
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                isActive = isActive
            )

            try {
                usageRepository.updateUsage(id, request).collectLatest { result ->
                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(isUpdating = false, error = null)
                        loadUsages()
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            error = exception.message ?: "Failed to update usage"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = e.message ?: "Failed to update usage"
                )
            }
        }
    }

    fun deleteUsage(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            try {
                usageRepository.deleteUsage(id).collectLatest { result ->
                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(isDeleting = false, error = null)
                        loadUsages()
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            error = exception.message ?: "Failed to delete usage"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = e.message ?: "Failed to delete usage"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshData() {
        loadUsages()
    }
}