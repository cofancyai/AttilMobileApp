package com.attil.inventory.presentation.transaction.inward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.transaction.CreateInwardItemRequest
import com.attil.inventory.data.model.transaction.InwardItem
import com.attil.inventory.data.model.transaction.UpdateInwardItemRequest
import com.attil.inventory.data.repository.InwardRepository
import com.attil.inventory.data.repository.CategoryRepository
import com.attil.inventory.data.repository.ItemRepository
import com.attil.inventory.data.repository.CuisineRepository
import com.attil.inventory.data.repository.VendorRepository
import com.attil.inventory.data.model.management.Category
import com.attil.inventory.data.model.management.Item
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.management.Vendor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InwardUiState(
    val inwardItems: List<InwardItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val items: List<Item> = emptyList(),
    val cuisines: List<Cuisine> = emptyList(),
    val vendors: List<Vendor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class InwardViewModel @Inject constructor(
    private val inwardRepository: InwardRepository,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: ItemRepository,
    private val cuisineRepository: CuisineRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InwardUiState())
    val uiState: StateFlow<InwardUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            loadCategories()
            loadItems()
            loadCuisines()
            loadVendors()
            loadInwardItems()
        }
    }

    private fun loadInwardItems() {
        viewModelScope.launch {
            inwardRepository.getAllInwardItems().collectLatest { result ->
                result.onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        inwardItems = items,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load inward items"
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
                }.onFailure { exception ->
                    // Log error but don't show to user unless critical
                    println("Failed to load categories: ${exception.message}")
                }
            }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            itemRepository.getAllItems().collectLatest { result ->
                result.onSuccess { items ->
                    _uiState.value = _uiState.value.copy(items = items)
                }.onFailure { exception ->
                    // Log error but don't show to user unless critical
                    println("Failed to load items: ${exception.message}")
                }
            }
        }
    }

    private fun loadCuisines() {
        viewModelScope.launch {
            cuisineRepository.getAllCuisines().collectLatest { result ->
                result.onSuccess { cuisines ->
                    _uiState.value = _uiState.value.copy(cuisines = cuisines)
                }.onFailure { exception ->
                    // Log error but don't show to user unless critical
                    println("Failed to load cuisines: ${exception.message}")
                }
            }
        }
    }

    private fun loadVendors() {
        viewModelScope.launch {
            vendorRepository.getAllVendors().collectLatest { result ->
                result.onSuccess { vendors ->
                    _uiState.value = _uiState.value.copy(vendors = vendors)
                }.onFailure { exception ->
                    // Log error but don't show to user unless critical
                    println("Failed to load vendors: ${exception.message}")
                }
            }
        }
    }

    fun createInwardItem(
        itemId: String,
        vendorName: String,
        vendorContact: String?,
        vendorAddress: String?,
        purchaseDate: String,
        inwardQuantity: Double,
        pricePerUnit: Double,
        priceWithoutGst: Double?,
        priceWithGst: Double?,
        gstPercentage: Double?,
        billNumber: String?,
        expiryDate: String?,
        cuisineId: String?,
        createdBy: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateInwardItemRequest(
                itemId = itemId,
                vendorName = vendorName.trim(),
                vendorContact = vendorContact?.trim()?.takeIf { it.isNotEmpty() },
                vendorAddress = vendorAddress?.trim()?.takeIf { it.isNotEmpty() },
                purchaseDate = purchaseDate,
                inwardQuantity = inwardQuantity,
                pricePerUnit = pricePerUnit,
                priceWithoutGst = priceWithoutGst,
                priceWithGst = priceWithGst,
                gstPercentage = gstPercentage,
                billNumber = billNumber?.trim()?.takeIf { it.isNotEmpty() },
                expiryDate = expiryDate?.takeIf { it.isNotEmpty() },
                cuisineId = cuisineId?.takeIf { it.isNotEmpty() },
                createdBy = createdBy
            )

            try {
                inwardRepository.createInwardItem(request).collectLatest { result ->
                    result.onSuccess { createdItem ->
                        _uiState.value = _uiState.value.copy(isCreating = false, error = null)
                        // Force refresh the list to show new item immediately
                        refreshInwardItems()
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            error = exception.message ?: "Failed to create inward item"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message ?: "Failed to create inward item"
                )
            }
        }
    }

    fun updateInwardItem(
        id: String,
        itemId: String,
        vendorName: String,
        vendorContact: String?,
        vendorAddress: String?,
        purchaseDate: String,
        inwardQuantity: Double,
        pricePerUnit: Double,
        priceWithoutGst: Double?,
        priceWithGst: Double?,
        gstPercentage: Double?,
        billNumber: String?,
        expiryDate: String?,
        cuisineId: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateInwardItemRequest(
                itemId = itemId,
                vendorName = vendorName.trim(),
                vendorContact = vendorContact?.trim()?.takeIf { it.isNotEmpty() },
                vendorAddress = vendorAddress?.trim()?.takeIf { it.isNotEmpty() },
                purchaseDate = purchaseDate,
                inwardQuantity = inwardQuantity,
                pricePerUnit = pricePerUnit,
                priceWithoutGst = priceWithoutGst,
                priceWithGst = priceWithGst,
                gstPercentage = gstPercentage,
                billNumber = billNumber?.trim()?.takeIf { it.isNotEmpty() },
                expiryDate = expiryDate?.takeIf { it.isNotEmpty() },
                cuisineId = cuisineId?.takeIf { it.isNotEmpty() }
            )

            try {
                inwardRepository.updateInwardItem(id, request).collectLatest { result ->
                    result.onSuccess { updatedItem ->
                        _uiState.value = _uiState.value.copy(isUpdating = false, error = null)
                        // Force refresh the list to show updated item immediately
                        refreshInwardItems()
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            error = exception.message ?: "Failed to update inward item"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = e.message ?: "Failed to update inward item"
                )
            }
        }
    }

    fun deleteInwardItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            try {
                inwardRepository.deleteInwardItem(id).collectLatest { result ->
                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(isDeleting = false, error = null)
                        // Force refresh the list to remove deleted item immediately
                        refreshInwardItems()
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            error = exception.message ?: "Failed to delete inward item"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = e.message ?: "Failed to delete inward item"
                )
            }
        }
    }

    // Force refresh inward items only
    private fun refreshInwardItems() {
        viewModelScope.launch {
            try {
                inwardRepository.getAllInwardItems().collectLatest { result ->
                    result.onSuccess { items ->
                        _uiState.value = _uiState.value.copy(
                            inwardItems = items,
                            error = null
                        )
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Failed to refresh inward items"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh inward items"
                )
            }
        }
    }

    // Public method for manual refresh
    fun refreshData() {
        loadAllData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}