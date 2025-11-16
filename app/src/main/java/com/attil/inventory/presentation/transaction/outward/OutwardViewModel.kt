package com.attil.inventory.presentation.transaction.outward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.transaction.OutwardItem
import com.attil.inventory.data.model.transaction.CreateOutwardItemRequest
import com.attil.inventory.data.model.transaction.UpdateOutwardItemRequest
import com.attil.inventory.data.model.transaction.ItemWithStock
import com.attil.inventory.data.repository.OutwardRepository
import com.attil.inventory.data.repository.CuisineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class OutwardUiState(
    val outwardItems: List<OutwardItem> = emptyList(),
    val availableItems: List<ItemWithStock> = emptyList(),
    val selectedItems: List<ItemWithStock> = emptyList(),
    val cuisines: List<Cuisine> = emptyList(),
    val selectedCuisine: Cuisine? = null,
    val selectedCategoryFilter: String = "",
    val cuisineType: String = "",
    val isLoading: Boolean = false,
    val isLoadingItems: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val totalSelectedItems: Int = 0,
    val showItemSelection: Boolean = false
)

@HiltViewModel
class OutwardViewModel @Inject constructor(
    private val outwardRepository: OutwardRepository,
    private val cuisineRepository: CuisineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutwardUiState())
    val uiState: StateFlow<OutwardUiState> = _uiState.asStateFlow()

    init {
        loadOutwardItems()
        loadCuisines()
        loadAvailableItems()
    }

    fun loadOutwardItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            outwardRepository.getAllOutwardItems().collectLatest { result ->
                result.onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        outwardItems = items,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load outward items"
                    )
                }
            }
        }
    }

    fun loadCuisines() {
        viewModelScope.launch {
            cuisineRepository.getAllCuisines().collectLatest { result ->
                result.onSuccess { cuisines ->
                    _uiState.value = _uiState.value.copy(cuisines = cuisines.filter { it.isActive })
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to load cuisines"
                    )
                }
            }
        }
    }

    fun loadAvailableItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingItems = true)
            
            val repository = if (_uiState.value.selectedCategoryFilter.isNotEmpty()) {
                outwardRepository.getItemsWithStockByCategory(_uiState.value.selectedCategoryFilter)
            } else {
                outwardRepository.getItemsWithStock()
            }
            
            repository.collectLatest { result ->
                result.onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        availableItems = items,
                        isLoadingItems = false
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingItems = false,
                        error = exception.message ?: "Failed to load available items"
                    )
                }
            }
        }
    }

    fun filterByCategory(categoryName: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = categoryName)
        loadAvailableItems()
    }

    fun selectCuisine(cuisine: Cuisine) {
        _uiState.value = _uiState.value.copy(selectedCuisine = cuisine)
    }

    fun setCuisineType(cuisineType: String) {
        _uiState.value = _uiState.value.copy(cuisineType = cuisineType)
    }

    fun toggleItemSelection(item: ItemWithStock) {
        val currentItems = _uiState.value.availableItems.toMutableList()
        val index = currentItems.indexOfFirst { it.item.id == item.item.id }
        
        if (index != -1) {
            currentItems[index] = currentItems[index].copy(
                isSelected = !currentItems[index].isSelected,
                outwardQuantity = if (!currentItems[index].isSelected) 0.0 else currentItems[index].outwardQuantity
            )
            
            _uiState.value = _uiState.value.copy(
                availableItems = currentItems,
                selectedItems = currentItems.filter { it.isSelected },
                totalSelectedItems = currentItems.count { it.isSelected }
            )
        }
    }

    fun updateItemQuantity(itemId: String, quantity: Double) {
        val currentItems = _uiState.value.availableItems.toMutableList()
        val index = currentItems.indexOfFirst { it.item.id == itemId }
        
        if (index != -1) {
            val item = currentItems[index]
            val validQuantity = quantity.coerceIn(0.0, item.currentStock)
            
            currentItems[index] = item.copy(outwardQuantity = validQuantity)
            
            _uiState.value = _uiState.value.copy(
                availableItems = currentItems,
                selectedItems = currentItems.filter { it.isSelected }
            )
        }
    }

    fun createMultipleOutwardItems(notes: String) {
        val selectedItems = _uiState.value.selectedItems.filter { it.outwardQuantity > 0 }
        val selectedCuisine = _uiState.value.selectedCuisine
        val cuisineType = _uiState.value.cuisineType
        
        if (selectedItems.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please select items with quantities")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                var successCount = 0
                var errors = mutableListOf<String>()

                selectedItems.forEach { itemWithStock ->
                    val request = CreateOutwardItemRequest(
                        itemId = itemWithStock.item.id,
                        categoryId = null,
                        outwardQuantity = itemWithStock.outwardQuantity,
                        cuisineType = cuisineType.takeIf { it.isNotEmpty() },
                        usageDate = currentDate,
                        notes = notes.takeIf { it.isNotEmpty() },
                        cuisineId = selectedCuisine?.id?.takeIf { it.isNotEmpty() },
                        sourceType = "manual"
                    )

                    outwardRepository.createOutwardItem(request).collectLatest { result ->
                        result.onSuccess {
                            successCount++
                        }.onFailure { exception ->
                            errors.add("${itemWithStock.item.name}: ${exception.message}")
                        }
                    }
                }

                if (errors.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        showItemSelection = false
                    )
                    clearSelections()
                    loadOutwardItems()
                    loadAvailableItems() // Refresh stock levels
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = "Some items failed: ${errors.joinToString(", ")}"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message ?: "Failed to create outward items"
                )
            }
        }
    }

    fun updateOutwardItem(
        id: String,
        itemId: String,
        outwardQuantity: Double,
        usageDate: String,
        cuisineType: String,
        notes: String,
        cuisineId: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            
            val request = UpdateOutwardItemRequest(
                itemId = itemId,
                outwardQuantity = outwardQuantity,
                usageDate = usageDate,
                cuisineType = cuisineType.takeIf { it.isNotEmpty() },
                notes = notes.takeIf { it.isNotEmpty() },
                cuisineId = cuisineId
            )

            outwardRepository.updateOutwardItem(id, request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadOutwardItems()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update outward item"
                    )
                }
            }
        }
    }

    fun deleteOutwardItem(id: String) {
        viewModelScope.launch {
            outwardRepository.deleteOutwardItem(id).collectLatest { result ->
                result.onSuccess {
                    loadOutwardItems()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to delete outward item"
                    )
                }
            }
        }
    }

    fun showItemSelection() {
        _uiState.value = _uiState.value.copy(showItemSelection = true)
    }

    fun hideItemSelection() {
        _uiState.value = _uiState.value.copy(showItemSelection = false)
        clearSelections()
    }

    fun clearSelections() {
        val clearedItems = _uiState.value.availableItems.map { 
            it.copy(isSelected = false, outwardQuantity = 0.0) 
        }
        _uiState.value = _uiState.value.copy(
            availableItems = clearedItems,
            selectedItems = emptyList(),
            totalSelectedItems = 0,
            selectedCuisine = null,
            selectedCategoryFilter = "",
            cuisineType = ""
        )
    }

    fun getUniqueCategories(): List<String> {
        return _uiState.value.availableItems
            .mapNotNull { it.item.categories?.name }
            .distinct()
            .sorted()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshData() {
        loadOutwardItems()
        loadAvailableItems()
        loadCuisines()
    }
}