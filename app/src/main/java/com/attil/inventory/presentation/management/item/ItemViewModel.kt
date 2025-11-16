package com.attil.inventory.presentation.management.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.CreateItemRequest
import com.attil.inventory.data.model.management.Item
import com.attil.inventory.data.model.management.UpdateItemRequest
import com.attil.inventory.data.repository.ItemRepository
import com.attil.inventory.data.repository.CategoryRepository
import com.attil.inventory.data.repository.GodownRepository
import com.attil.inventory.data.repository.RackRepository
import com.attil.inventory.data.repository.CuisineRepository
import com.attil.inventory.data.model.management.Category
import com.attil.inventory.data.model.management.Godown
import com.attil.inventory.data.model.management.Rack
import com.attil.inventory.data.model.management.Cuisine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemUiState(
    val items: List<Item> = emptyList(),
    val categories: List<Category> = emptyList(),
    val godowns: List<Godown> = emptyList(),
    val racks: List<Rack> = emptyList(),
    val cuisines: List<Cuisine> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val godownRepository: GodownRepository,
    private val rackRepository: RackRepository,
    private val cuisineRepository: CuisineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemUiState())
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            loadCategories()
            loadGodowns()
            loadRacks()
            loadCuisines()
            loadItems()
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            itemRepository.getAllItems().collectLatest { result ->
                result.onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        items = items,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load items"
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collectLatest { result ->
                result.onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
            }
        }
    }

    private fun loadGodowns() {
        viewModelScope.launch {
            godownRepository.getAllGodowns().collectLatest { result ->
                result.onSuccess { godowns ->
                    _uiState.value = _uiState.value.copy(godowns = godowns)
                }
            }
        }
    }

    private fun loadRacks() {
        viewModelScope.launch {
            rackRepository.getAllRacks().collectLatest { result ->
                result.onSuccess { racksWithGodowns ->
                    val racks = racksWithGodowns.map { rackWithGodown ->
                        Rack(
                            id = rackWithGodown.id,
                            name = rackWithGodown.name,
                            description = rackWithGodown.description,
                            godownId = rackWithGodown.godownId,
                            isActive = rackWithGodown.isActive,
                            createdAt = rackWithGodown.createdAt,
                            updatedAt = rackWithGodown.updatedAt
                        )
                    }
                    _uiState.value = _uiState.value.copy(racks = racks)
                }
            }
        }
    }

    private fun loadCuisines() {
        viewModelScope.launch {
            cuisineRepository.getAllCuisines().collectLatest { result ->
                result.onSuccess { cuisines ->
                    _uiState.value = _uiState.value.copy(cuisines = cuisines)
                }
            }
        }
    }

    fun createItem(
        name: String,
        categoryId: String,
        godownId: String?,
        rackId: String?,
        unitOfMeasure: String,
        minimumStockLevel: Double,
        cuisineIds: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateItemRequest(
                name = name.trim(),
                categoryId = categoryId,
                godownId = godownId?.takeIf { it.isNotEmpty() },
                rackId = rackId?.takeIf { it.isNotEmpty() },
                unitOfMeasure = unitOfMeasure,
                minimumStockLevel = minimumStockLevel,
                cuisineIds = cuisineIds.takeIf { it.isNotEmpty() }
            )

            itemRepository.createItem(request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    loadItems()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = exception.message ?: "Failed to create item"
                    )
                }
            }
        }
    }

    fun updateItem(
        id: String,
        name: String,
        categoryId: String,
        godownId: String?,
        rackId: String?,
        unitOfMeasure: String,
        minimumStockLevel: Double,
        cuisineIds: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateItemRequest(
                name = name.trim(),
                categoryId = categoryId,
                godownId = godownId?.takeIf { it.isNotEmpty() },
                rackId = rackId?.takeIf { it.isNotEmpty() },
                unitOfMeasure = unitOfMeasure,
                minimumStockLevel = minimumStockLevel,
                cuisineIds = cuisineIds.takeIf { it.isNotEmpty() }
            )

            itemRepository.updateItem(id, request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadItems()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update item"
                    )
                }
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            itemRepository.deleteItem(id).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    loadItems()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message ?: "Failed to delete item"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}