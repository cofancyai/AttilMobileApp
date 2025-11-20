package com.attil.inventory.presentation.transaction.indent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.transaction.Indent
import com.attil.inventory.data.model.transaction.IndentItem
import com.attil.inventory.data.model.transaction.CreateIndentRequest
import com.attil.inventory.data.model.transaction.CreateIndentItemForRequest
import com.attil.inventory.data.model.transaction.ItemForIndentSelection
import com.attil.inventory.data.model.transaction.UpdateIndentRequest
import com.attil.inventory.data.model.transaction.CreateOutwardItemRequest
import com.attil.inventory.data.repository.IndentRepository
import com.attil.inventory.data.repository.CuisineRepository
import com.attil.inventory.data.repository.OutwardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.attil.inventory.data.model.transaction.VerifyIndentItemRequest
import com.attil.inventory.data.model.transaction.VerificationItem
import com.attil.inventory.data.model.transaction.UpdateIndentItemRequest

data class IndentUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isLoadingItems: Boolean = false,
    val error: String? = null,
    val indents: List<Indent> = emptyList(),
    val createdIndent: Indent? = null,

    // Creation flow state
    val currentStep: Int = 1,
    val requiredDate: String = "",
    val requiredTime: String = "",
    val priority: String = "",
    val purpose: String = "",
    val notes: String = "",
    val selectedCuisine: Cuisine? = null,
    val cuisines: List<Cuisine> = emptyList(),

    // Item selection state
    val availableItems: List<ItemForIndentSelection> = emptyList(),
    val selectedItems: List<ItemForIndentSelection> = emptyList(),
    val filteredItems: List<ItemForIndentSelection> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: String = "",
    val totalSelectedItems: Int = 0,
    val showVerificationDialog: Boolean = false,
    val verificationItems: List<VerificationItem> = emptyList(),
    val isVerifying: Boolean = false,
    val verificationError: String? = null
)

data class FulfillmentItem(
    val indentItem: IndentItem,
    val availableStock: Double,
    var fulfillQuantity: Double = 0.0,
    var isSelected: Boolean = false
)

@HiltViewModel
class IndentViewModel @Inject constructor(
    private val repository: IndentRepository,
    private val cuisineRepository: CuisineRepository,
    private val outwardRepository: OutwardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndentUiState())
    val uiState: StateFlow<IndentUiState> = _uiState.asStateFlow()

    init {
        loadCuisines()
        loadItemsForIndent()
    }

    // Navigation
    fun proceedToStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }

    // Error handling
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Load data
    fun loadIndents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getAllIndents().collect { result ->
                result.fold(
                    onSuccess = { indents ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            indents = indents
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    fun loadIndentsByChef(chefId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getIndentsByChef(chefId).collect { result ->
                result.fold(
                    onSuccess = { indents ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            indents = indents
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    fun loadIndentsByStatus(status: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getIndentsByStatus(status).collect { result ->
                result.fold(
                    onSuccess = { indents ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            indents = indents
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    private fun loadCuisines() {
        viewModelScope.launch {
            cuisineRepository.getAllCuisines().collect { result ->
                result.fold(
                    onSuccess = { cuisines ->
                        _uiState.value = _uiState.value.copy(cuisines = cuisines)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
                )
            }
        }
    }

    private fun loadItemsForIndent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingItems = true)

            repository.getItemsForIndent().collect { result ->
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingItems = false,
                            availableItems = items,
                            filteredItems = items
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingItems = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    // Form setters
    fun setRequiredDate(date: String) {
        _uiState.value = _uiState.value.copy(requiredDate = date)
    }

    fun setRequiredTime(time: String) {
        _uiState.value = _uiState.value.copy(requiredTime = time)
    }

    fun setPriority(priority: String) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun setPurpose(purpose: String) {
        _uiState.value = _uiState.value.copy(purpose = purpose)
    }

    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun selectCuisine(cuisine: Cuisine) {
        _uiState.value = _uiState.value.copy(selectedCuisine = cuisine)
    }

    // Item selection
    fun toggleItemSelection(item: ItemForIndentSelection) {
        val currentItems = _uiState.value.availableItems.toMutableList()
        val index = currentItems.indexOfFirst { it.item.id == item.item.id }

        if (index != -1) {
            val updatedItem = currentItems[index].copy(
                isSelected = !currentItems[index].isSelected,
                requestedQuantity = if (!currentItems[index].isSelected) 0.0 else currentItems[index].requestedQuantity
            )
            currentItems[index] = updatedItem

            val selectedItems = currentItems.filter { it.isSelected }

            _uiState.value = _uiState.value.copy(
                availableItems = currentItems,
                selectedItems = selectedItems,
                totalSelectedItems = selectedItems.size
            )

            // Update filtered items if search is active
            if (_uiState.value.searchQuery.isNotEmpty()) {
                filterItems()
            }
        }
    }

    fun updateItemQuantity(itemId: String, quantity: Double) {
        val currentItems = _uiState.value.selectedItems.toMutableList()
        val index = currentItems.indexOfFirst { it.item.id == itemId }

        if (index != -1) {
            currentItems[index] = currentItems[index].copy(requestedQuantity = quantity)
            _uiState.value = _uiState.value.copy(selectedItems = currentItems)

            // Also update in available items
            val availableItems = _uiState.value.availableItems.toMutableList()
            val availableIndex = availableItems.indexOfFirst { it.item.id == itemId }
            if (availableIndex != -1) {
                availableItems[availableIndex] = availableItems[availableIndex].copy(requestedQuantity = quantity)
                _uiState.value = _uiState.value.copy(availableItems = availableItems)
            }
        }
    }

    // Search and filtering
    fun searchItems(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterItems()
    }

    fun filterByCategory(categoryName: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = categoryName)
        filterItems()
    }

    private fun filterItems() {
        val query = _uiState.value.searchQuery.lowercase()
        val categoryFilter = _uiState.value.selectedCategoryFilter

        val filtered = _uiState.value.availableItems.filter { item ->
            val matchesSearch = query.isEmpty() ||
                    item.item.name.lowercase().contains(query)

            val matchesCategory = categoryFilter.isEmpty() ||
                    item.item.categories?.name == categoryFilter

            matchesSearch && matchesCategory
        }

        _uiState.value = _uiState.value.copy(filteredItems = filtered)
    }

    fun getUniqueCategories(): List<String> {
        return _uiState.value.availableItems
            .mapNotNull { it.item.categories?.name }
            .distinct()
            .sorted()
    }

    // Indent operations
    fun createIndent(chefId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)

            try {
                val indentRequest = CreateIndentRequest(
                    chefId = chefId,
                    cuisineId = _uiState.value.selectedCuisine?.id ?: "",
                    requiredDate = _uiState.value.requiredDate,
                    requiredTime = _uiState.value.requiredTime,
                    priority = _uiState.value.priority,
                    purpose = _uiState.value.purpose,
                    notes = _uiState.value.notes.ifEmpty { null },
                    indentItems = _uiState.value.selectedItems.map { item ->
                        CreateIndentItemForRequest(
                            itemId = item.item.id,
                            requestedQuantity = item.requestedQuantity,
                            unitOfMeasure = item.item.unitOfMeasure
                        )
                    }
                )

                repository.createIndent(indentRequest).collect { result ->
                    result.fold(
                        onSuccess = { createdIndent ->
                            _uiState.value = _uiState.value.copy(
                                isCreating = false,
                                createdIndent = createdIndent,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isCreating = false,
                                error = error.message
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message
                )
            }
        }
    }

    fun updateIndentStatus(indentId: String, status: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val updateRequest = UpdateIndentRequest(
                status = status,
                approvedBy = if (status == "Approved") userId else null,
                fulfilledBy = if (status == "Fulfilled") userId else null,
                approvedAt = if (status == "Approved") getCurrentTimestamp() else null,
                fulfilledAt = if (status == "Fulfilled") getCurrentTimestamp() else null,
                receivedAt = if (status == "Received") getCurrentTimestamp() else null
            )

            repository.updateIndent(indentId, updateRequest).collect { result ->
                result.fold(
                    onSuccess = { updatedIndent ->
                        // Update the indent in the list
                        val updatedIndents = _uiState.value.indents.map { indent ->
                            if (indent.id == indentId) updatedIndent else indent
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            indents = updatedIndents
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    fun showVerificationDialog(indent: Indent) {
        println("DEBUG - showVerificationDialog called")
        println("DEBUG - indent.indentItems size: ${indent.indentItems?.size}")

        // Only include items that were actually fulfilled
        // Non-fulfilled items cannot be verified since they were never sent
        val verificationItems = indent.indentItems
            ?.filter { item ->
                item.fulfilledQuantity != null && item.fulfilledQuantity!! > 0
            }
            ?.map { item ->
                println("DEBUG - Item: ${item.items?.name}, fulfilled: ${item.fulfilledQuantity}")
                VerificationItem(
                    indentItem = item,
                    isFulfilled = true, // All filtered items are fulfilled
                    isReceived = false
                )
            } ?: emptyList()

        println("DEBUG - verificationItems size: ${verificationItems.size}")
        println("DEBUG - All items shown are fulfilled")

        _uiState.value = _uiState.value.copy(
            showVerificationDialog = true,
            verificationItems = verificationItems
        )
    }

    fun hideVerificationDialog() {
        _uiState.value = _uiState.value.copy(
            showVerificationDialog = false,
            verificationItems = emptyList(),
            verificationError = null
        )
    }

    fun updateItemVerification(itemId: String, isReceived: Boolean) {
        val updatedItems = _uiState.value.verificationItems.map { item ->
            if (item.indentItem.id == itemId) {
                item.copy(isReceived = isReceived)
            } else item
        }
        _uiState.value = _uiState.value.copy(verificationItems = updatedItems)
    }

    fun updateReceivedQuantity(itemId: String, receivedQuantity: Double) {
        val updatedItems = _uiState.value.verificationItems.map { item ->
            if (item.indentItem.id == itemId) {
                item.copy(receivedQuantity = receivedQuantity)
            } else item
        }
        _uiState.value = _uiState.value.copy(verificationItems = updatedItems)
    }

    fun verifyFullyReceived(indentId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, verificationError = null)

            try {
                val verifyRequest = VerifyIndentItemRequest(
                    isReceived = true,
                    receivedBy = userId,
                    receivedAt = getCurrentTimestamp()
                )

                val itemIds = _uiState.value.verificationItems.map { it.indentItem.id!! }

                repository.verifyMultipleIndentItems(itemIds, verifyRequest).collect { result ->
                    result.fold(
                        onSuccess = {
                            // Update each item to set verification_status and received_quantity
                            itemIds.forEach { itemId ->
                                val item = _uiState.value.verificationItems.find { it.indentItem.id == itemId }
                                if (item != null) {
                                    val updateRequest = UpdateIndentItemRequest(
                                        verificationStatus = "Fully Verified",
                                        receivedQuantity = item.indentItem.fulfilledQuantity
                                    )
                                    repository.updateIndentItem(itemId, updateRequest).collect { /* Handle result */ }
                                }
                            }

                            // Update indent status to "Received"
                            val updateRequest = UpdateIndentRequest(
                                status = "Received",
                                receivedAt = getCurrentTimestamp()
                            )

                            repository.updateIndent(indentId, updateRequest).collect { indentResult ->
                                indentResult.fold(
                                    onSuccess = {
                                        _uiState.value = _uiState.value.copy(
                                            isVerifying = false,
                                            showVerificationDialog = false,
                                            verificationItems = emptyList()
                                        )
                                        loadIndents()
                                    },
                                    onFailure = { error ->
                                        _uiState.value = _uiState.value.copy(
                                            isVerifying = false,
                                            verificationError = error.message
                                        )
                                    }
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isVerifying = false,
                                verificationError = error.message
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifying = false,
                    verificationError = e.message
                )
            }
        }
    }

    fun verifyPartiallyReceived(indentId: String, userId: String, itemsWithQuantities: List<Pair<String, Double>>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, verificationError = null)

            try {
                // Update each item with received quantity and verification status
                itemsWithQuantities.forEach { (itemId, receivedQuantity) ->
                    val item = _uiState.value.verificationItems.find { it.indentItem.id == itemId }
                    val fulfilledQuantity = item?.indentItem?.fulfilledQuantity ?: 0.0

                    val verificationStatus = if (receivedQuantity >= fulfilledQuantity) {
                        "Fully Verified"
                    } else {
                        "Partially Verified"
                    }

                    val updateRequest = UpdateIndentItemRequest(
                        isReceived = true,
                        receivedBy = userId,
                        receivedAt = getCurrentTimestamp(),
                        receivedQuantity = receivedQuantity,
                        verificationStatus = verificationStatus
                    )

                    repository.updateIndentItem(itemId, updateRequest).collect { result ->
                        result.onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isVerifying = false,
                                verificationError = "Failed to update item: ${error.message}"
                            )
                            return@collect
                        }
                    }
                }

                // Determine indent status based on verification
                val allItems = _uiState.value.verificationItems
                val verifiedCount = itemsWithQuantities.size
                val indentStatus = if (verifiedCount == allItems.size) {
                    val allFullyVerified = itemsWithQuantities.all { (itemId, receivedQty) ->
                        val item = allItems.find { it.indentItem.id == itemId }
                        receivedQty >= (item?.indentItem?.fulfilledQuantity ?: 0.0)
                    }
                    if (allFullyVerified) "Received" else "Partially Received"
                } else {
                    "Partially Received"
                }

                val updateRequest = UpdateIndentRequest(
                    status = indentStatus,
                    receivedAt = if (indentStatus == "Received") getCurrentTimestamp() else null
                )

                repository.updateIndent(indentId, updateRequest).collect { indentResult ->
                    indentResult.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isVerifying = false,
                                showVerificationDialog = false,
                                verificationItems = emptyList()
                            )
                            loadIndents()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isVerifying = false,
                                verificationError = error.message
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifying = false,
                    verificationError = e.message
                )
            }
        }
    }

    fun refulfillIndentItem(itemId: String, indentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Reset verification fields to send item back to fulfillment stage
                val updateRequest = UpdateIndentItemRequest(
                    isReceived = false,
                    receivedBy = null,
                    receivedAt = null,
                    receivedQuantity = null,
                    verificationStatus = null
                )

                repository.updateIndentItem(itemId, updateRequest).collect { result ->
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            loadIndents()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to re-fulfill item: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteIndentItem(itemId: String, indentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Soft delete by setting is_deleted = true
                val updateRequest = UpdateIndentItemRequest(
                    isDeleted = true
                )

                repository.updateIndentItem(itemId, updateRequest).collect { result ->
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            loadIndents()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to delete item: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun verifyIndentItems(indentId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, verificationError = null)

            try {
                val receivedItems = _uiState.value.verificationItems.filter { it.isReceived && it.isFulfilled }

                if (receivedItems.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isVerifying = false,
                        verificationError = "Please select at least one item"
                    )
                    return@launch
                }

                val verifyRequest = VerifyIndentItemRequest(
                    isReceived = true,
                    receivedBy = userId,
                    receivedAt = getCurrentTimestamp()
                )

                val itemIds = receivedItems.map { it.indentItem.id!! }

                repository.verifyMultipleIndentItems(itemIds, verifyRequest).collect { result ->
                    result.fold(
                        onSuccess = {
                            // Calculate new indent status
                            val allFulfilledItems = _uiState.value.verificationItems.filter { it.isFulfilled }
                            val receivedCount = _uiState.value.verificationItems.count { it.isReceived && it.isFulfilled }

                            val newStatus = if (receivedCount == allFulfilledItems.size) "Received" else "Partially Received"

                            // Update indent status
                            val updateRequest = UpdateIndentRequest(
                                status = newStatus,
                                receivedAt = if (newStatus == "Received") getCurrentTimestamp() else null
                            )

                            repository.updateIndent(indentId, updateRequest).collect { indentResult ->
                                indentResult.fold(
                                    onSuccess = {
                                        _uiState.value = _uiState.value.copy(
                                            isVerifying = false,
                                            showVerificationDialog = false,
                                            verificationItems = emptyList()
                                        )
                                        // Reload indents to show updated status
                                        loadIndents()
                                    },
                                    onFailure = { error ->
                                        _uiState.value = _uiState.value.copy(
                                            isVerifying = false,
                                            verificationError = error.message
                                        )
                                    }
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isVerifying = false,
                                verificationError = error.message
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifying = false,
                    verificationError = e.message
                )
            }
        }
    }

    // Fulfillment functions
    fun fulfillIndent(
        indentId: String,
        fulfillmentItems: List<FulfillmentItem>,
        fulfilledBy: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val selectedItems = fulfillmentItems.filter { it.isSelected && it.fulfillQuantity > 0 }

                // Update each indent item with fulfilled quantity
                selectedItems.forEach { item ->
                    val updateRequest = UpdateIndentItemRequest(
                        fulfilledQuantity = item.fulfillQuantity,
                        status = "Fulfilled"
                    )
                    repository.updateIndentItem(item.indentItem.id!!, updateRequest).collect { result ->
                        result.onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to update item: ${error.message}"
                            )
                            return@collect
                        }
                    }
                }

                // Create outward transactions for fulfilled items
                selectedItems.forEach { item ->
                    createOutwardTransaction(item, indentId)
                }

                // Update indent status
                val allItemsFulfilled = fulfillmentItems.all {
                    it.fulfillQuantity > 0 || !it.isSelected
                }
                val newStatus = if (allItemsFulfilled) "Fulfilled" else "Partial"

                val updateRequest = UpdateIndentRequest(
                    status = newStatus,
                    fulfilledBy = fulfilledBy,
                    fulfilledAt = getCurrentTimestamp()
                )

                repository.updateIndent(indentId, updateRequest).collect { result ->
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = null
                            )
                            // Reload indents
                            loadIndents()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun createOutwardTransaction(item: FulfillmentItem, indentId: String) {
        try {
            val outwardRequest = CreateOutwardItemRequest(
                itemId = item.indentItem.itemId,
                categoryId = null,
                outwardQuantity = item.fulfillQuantity,
                cuisineType = null,
                usageDate = getCurrentDate(),
                notes = "Fulfilled from indent: $indentId",
                createdBy = null,
                cuisineId = null,
                indentId = indentId,
                sourceType = "indent"
            )

            println("DEBUG - Creating outward transaction: $outwardRequest")
            outwardRepository.createOutwardItem(outwardRequest).collect { result ->
                result.fold(
                    onSuccess = {
                        println("DEBUG - Outward transaction created successfully")
                    },
                    onFailure = { error ->
                        println("DEBUG - Failed to create outward transaction: ${error.message}")
                    }
                )
            }
        } catch (e: Exception) {
            println("DEBUG - Exception creating outward transaction: ${e.message}")
        }
    }

    fun getItemsWithStock(indentItems: List<IndentItem>): Flow<Result<List<FulfillmentItem>>> = flow {
        try {
            // Get current stock for each item
            repository.getItemsForIndent().collect { result ->
                result.fold(
                    onSuccess = { stockItems ->
                        val fulfillmentItems = indentItems.map { item ->
                            val stockItem = stockItems.find { it.item.id == item.itemId }
                            FulfillmentItem(
                                indentItem = item,
                                availableStock = stockItem?.availableStock ?: 0.0,
                                fulfillQuantity = 0.0,
                                isSelected = false
                            )
                        }
                        emit(Result.success(fulfillmentItems))
                    },
                    onFailure = { error ->
                        emit(Result.failure(error))
                    }
                )
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Reset for new indent creation
    fun resetForNewIndent() {
        _uiState.value = _uiState.value.copy(
            currentStep = 1,
            requiredDate = "",
            requiredTime = "",
            priority = "",
            purpose = "",
            notes = "",
            selectedCuisine = null,
            selectedItems = emptyList(),
            totalSelectedItems = 0,
            searchQuery = "",
            selectedCategoryFilter = "",
            createdIndent = null,
            error = null,
            showVerificationDialog = false,
            verificationItems = emptyList(),
            verificationError = null
        )

        // Reset item selections
        val resetItems = _uiState.value.availableItems.map { item ->
            item.copy(isSelected = false, requestedQuantity = 0.0)
        }
        _uiState.value = _uiState.value.copy(
            availableItems = resetItems,
            filteredItems = resetItems
        )
    }

    private fun getCurrentTimestamp(): String {
        return java.time.Instant.now().toString()
    }

    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
}