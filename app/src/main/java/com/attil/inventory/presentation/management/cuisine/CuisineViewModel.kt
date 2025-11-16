package com.attil.inventory.presentation.management.cuisine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.management.CreateCuisineRequest
import com.attil.inventory.data.model.management.UpdateCuisineRequest
import com.attil.inventory.data.repository.CuisineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CuisineUiState(
    val cuisines: List<Cuisine> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class CuisineViewModel @Inject constructor(
    private val repository: CuisineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuisineUiState())
    val uiState: StateFlow<CuisineUiState> = _uiState.asStateFlow()

    init {
        loadCuisines()
    }

    fun loadCuisines() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllCuisines().collectLatest { result ->
                result.onSuccess { cuisines ->
                    _uiState.value = _uiState.value.copy(
                        cuisines = cuisines,
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

    fun createCuisine(name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateCuisineRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() }
            )

            repository.createCuisine(request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    loadCuisines()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = exception.message ?: "Failed to create cuisine"
                    )
                }
            }
        }
    }

    fun updateCuisine(id: String, name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateCuisineRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() }
            )

            repository.updateCuisine(id, request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadCuisines()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update cuisine"
                    )
                }
            }
        }
    }

    fun deleteCuisine(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            repository.deleteCuisine(id).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    loadCuisines()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message ?: "Failed to delete cuisine"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}