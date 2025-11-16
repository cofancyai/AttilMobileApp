package com.attil.inventory.presentation.management.godown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.CreateGodownRequest
import com.attil.inventory.data.model.management.Godown
import com.attil.inventory.data.model.management.UpdateGodownRequest
import com.attil.inventory.data.repository.GodownRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GodownUiState(
    val godowns: List<Godown> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class GodownViewModel @Inject constructor(
    private val repository: GodownRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GodownUiState())
    val uiState: StateFlow<GodownUiState> = _uiState.asStateFlow()

    init {
        loadGodowns()
    }

    fun loadGodowns() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllGodowns().collectLatest { result ->
                result.onSuccess { godowns ->
                    _uiState.value = _uiState.value.copy(
                        godowns = godowns,
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

    fun createGodown(name: String, description: String, location: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateGodownRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() },
                location = location.trim().takeIf { it.isNotEmpty() }
            )

            repository.createGodown(request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    loadGodowns() // Refresh the list
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = exception.message ?: "Failed to create godown"
                    )
                }
            }
        }
    }

    fun updateGodown(id: String, name: String, description: String, location: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateGodownRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() },
                location = location.trim().takeIf { it.isNotEmpty() }
            )

            repository.updateGodown(id, request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadGodowns() // Refresh the list
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update godown"
                    )
                }
            }
        }
    }

    fun deleteGodown(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            repository.deleteGodown(id).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    loadGodowns() // Refresh the list
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message ?: "Failed to delete godown"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}