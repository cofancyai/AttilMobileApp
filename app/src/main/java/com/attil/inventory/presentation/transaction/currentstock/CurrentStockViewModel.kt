package com.attil.inventory.presentation.transaction.currentstock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.CurrentStock
import com.attil.inventory.data.repository.CurrentStockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrentStockUiState(
    val currentStocks: List<CurrentStock> = emptyList(),
    val filteredStocks: List<CurrentStock> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategoryFilter: String = "",
    val selectedGodownFilter: String = "",
    val showLowStockOnly: Boolean = false,
    val searchQuery: String = "",
    val lowStockCount: Int = 0,
    val totalItems: Int = 0
)

@HiltViewModel
class CurrentStockViewModel @Inject constructor(
    private val repository: CurrentStockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrentStockUiState())
    val uiState: StateFlow<CurrentStockUiState> = _uiState.asStateFlow()

    init {
        loadCurrentStocks()
    }

    fun loadCurrentStocks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllCurrentStocks().collectLatest { result ->
                result.onSuccess { stocks ->
                    val lowStockCount = stocks.count { it.isLowStock }
                    _uiState.value = _uiState.value.copy(
                        currentStocks = stocks,
                        isLoading = false,
                        error = null,
                        lowStockCount = lowStockCount,
                        totalItems = stocks.size
                    )
                    applyFilters()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load current stock"
                    )
                }
            }
        }
    }

    fun filterByCategory(categoryName: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = categoryName)
        applyFilters()
    }

    fun filterByGodown(godownName: String) {
        _uiState.value = _uiState.value.copy(selectedGodownFilter = godownName)
        applyFilters()
    }

    fun toggleLowStockFilter() {
        _uiState.value = _uiState.value.copy(showLowStockOnly = !_uiState.value.showLowStockOnly)
        applyFilters()
    }

    fun searchItems(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCategoryFilter = "",
            selectedGodownFilter = "",
            showLowStockOnly = false,
            searchQuery = ""
        )
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.currentStocks

        // Apply category filter
        if (currentState.selectedCategoryFilter.isNotEmpty()) {
            filtered = filtered.filter { it.categoryName == currentState.selectedCategoryFilter }
        }

        // Apply godown filter
        if (currentState.selectedGodownFilter.isNotEmpty()) {
            filtered = filtered.filter { it.godownName == currentState.selectedGodownFilter }
        }

        // Apply low stock filter
        if (currentState.showLowStockOnly) {
            filtered = filtered.filter { it.isLowStock }
        }

        // Apply search filter
        if (currentState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { 
                it.itemName.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        _uiState.value = currentState.copy(filteredStocks = filtered)
    }

    fun getUniqueCategories(): List<String> {
        return _uiState.value.currentStocks
            .map { it.categoryName }
            .distinct()
            .sorted()
    }

    fun getUniqueGodowns(): List<String> {
        return _uiState.value.currentStocks
            .mapNotNull { it.godownName }
            .distinct()
            .sorted()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshData() {
        loadCurrentStocks()
    }
}
