package com.attil.inventory.presentation.management.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.Category
import com.attil.inventory.data.model.management.CreateCategoryRequest
import com.attil.inventory.data.model.management.UpdateCategoryRequest
import com.attil.inventory.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllCategories().collectLatest { result ->
                result.onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
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

    fun createCategory(name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateCategoryRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() }
            )

            repository.createCategory(request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    loadCategories()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = exception.message ?: "Failed to create category"
                    )
                }
            }
        }
    }

    fun updateCategory(id: String, name: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateCategoryRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() }
            )

            repository.updateCategory(id, request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadCategories()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update category"
                    )
                }
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            repository.deleteCategory(id).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    loadCategories()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message ?: "Failed to delete category"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}