
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
import com.attil.inventory.data.model.reports.IndentReport
import com.attil.inventory.data.model.reports.IndentReportFilter
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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
    val usages: List<com.attil.inventory.data.model.management.Usage> = emptyList(),
    val selectedUsage: com.attil.inventory.data.model.management.Usage? = null,

    // Item selection state
    val availableItems: List<ItemForIndentSelection> = emptyList(),
    val selectedItems: List<ItemForIndentSelection> = emptyList(),
    val filteredItems: List<ItemForIndentSelection> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: String = "",
    val totalSelectedItems: Int = 0,
    val showVerificationDialog: Boolean = false,
    val verificationItems: List<VerificationItem> = emptyList(),
    val currentIndentForVerification: Indent? = null,
    val isVerifying: Boolean = false,
    val verificationError: String? = null,
    val verificationCompleted: Boolean = false,

    // Indent Report state
    val indentReport: IndentReport? = null,
    val isLoadingReport: Boolean = false,
    val reportError: String? = null,

    // Reason selection dialog state
    val showReasonDialog: Boolean = false,
    val untickedItems: List<VerificationItem> = emptyList(),
    val itemReasons: Map<String, String> = emptyMap(), // itemId -> reason
    val itemPartialQuantities: Map<String, String> = emptyMap(), // itemId -> quantity
    val isSubmittingReasons: Boolean = false
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
    private val outwardRepository: OutwardRepository,
    private val userRepository: com.attil.inventory.data.repository.UserRepository,
    private val usageRepository: com.attil.inventory.data.repository.UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndentUiState())
    val uiState: StateFlow<IndentUiState> = _uiState.asStateFlow()

    init {
        loadCuisines()
        loadUsages()
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

    private fun loadUsages() {
        viewModelScope.launch {
            usageRepository.getAllUsages().collect { result ->
                result.fold(
                    onSuccess = { usages ->
                        val activeUsages = usages.filter { it.isActive }
                        _uiState.value = _uiState.value.copy(usages = activeUsages)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
                )
            }
        }
    }

    fun loadChefCuisines(chefId: String) {
        viewModelScope.launch {
            userRepository.getUserById(chefId).collect { result ->
                result.fold(
                    onSuccess = { user ->
                        // Get assigned cuisines from user_cuisines junction table
                        val assignedCuisines = user?.getAssignedCuisines() ?: emptyList()
                        _uiState.value = _uiState.value.copy(cuisines = assignedCuisines)

                        // Auto-select first cuisine if only one assigned
                        if (assignedCuisines.size == 1) {
                            _uiState.value = _uiState.value.copy(selectedCuisine = assignedCuisines.first())
                        }
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

    fun selectUsage(usage: com.attil.inventory.data.model.management.Usage) {
        _uiState.value = _uiState.value.copy(
            selectedUsage = usage,
            purpose = usage.name // Update purpose text as well
        )
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

    fun removeSelectedItem(itemId: String) {
        val currentItems = _uiState.value.availableItems.toMutableList()
        val index = currentItems.indexOfFirst { it.item.id == itemId }

        if (index != -1) {
            // Unselect the item
            val updatedItem = currentItems[index].copy(
                isSelected = false,
                requestedQuantity = 0.0
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
                // Fetch chef's cuisine from user profile
                var chefCuisineId: String? = null
                userRepository.getUserById(chefId).collect { userResult ->
                    userResult.onSuccess { user ->
                        chefCuisineId = user?.cuisineId
                    }
                }

                // Use chef's assigned cuisine, or fall back to first available cuisine
                val cuisineId = chefCuisineId
                    ?: _uiState.value.selectedCuisine?.id
                    ?: _uiState.value.cuisines.firstOrNull()?.id
                    ?: ""

                if (cuisineId.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = "No cuisine assigned to chef. Please contact administrator."
                    )
                    return@launch
                }

                val indentRequest = CreateIndentRequest(
                    chefId = chefId,
                    cuisineId = cuisineId,
                    requiredDate = _uiState.value.requiredDate,
                    requiredTime = getCurrentTime(), // Auto-set to current time
                    priority = "Medium", // Default priority
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
                            // Reset form for next creation
                            resetForNewIndent()
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

    private fun getCurrentTime(): String {
        return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
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
        println("DEBUG - indent chefId: ${indent.chefId}, cuisineId: ${indent.cuisineId}")

        val verificationItems = indent.indentItems?.map { item ->
            println("DEBUG - Item: ${item.items?.name}, fulfilled: ${item.fulfilledQuantity}")
            VerificationItem(
                indentItem = item,
                isFulfilled = item.fulfilledQuantity != null && item.fulfilledQuantity!! > 0,
                isReceived = false
            )
        } ?: emptyList()

        println("DEBUG - verificationItems size: ${verificationItems.size}")
        println("DEBUG - fulfilled items: ${verificationItems.count { it.isFulfilled }}")

        _uiState.value = _uiState.value.copy(
            showVerificationDialog = true,
            verificationItems = verificationItems,
            currentIndentForVerification = indent
        )
    }

    fun hideVerificationDialog() {
        _uiState.value = _uiState.value.copy(
            showVerificationDialog = false,
            verificationItems = emptyList(),
            currentIndentForVerification = null,
            verificationError = null,
            verificationCompleted = false
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

    fun showReasonDialog() {
        val untickedItems = _uiState.value.verificationItems.filter {
            it.isFulfilled && !it.isReceived
        }
        _uiState.value = _uiState.value.copy(
            showReasonDialog = true,
            untickedItems = untickedItems,
            itemReasons = emptyMap(),
            itemPartialQuantities = emptyMap()
        )
    }

    fun hideReasonDialog() {
        _uiState.value = _uiState.value.copy(
            showReasonDialog = false,
            untickedItems = emptyList(),
            itemReasons = emptyMap(),
            itemPartialQuantities = emptyMap()
        )
    }

    fun updateItemReason(itemId: String, reason: String) {
        val updatedReasons = _uiState.value.itemReasons.toMutableMap()
        updatedReasons[itemId] = reason
        _uiState.value = _uiState.value.copy(itemReasons = updatedReasons)
    }

    fun updateItemPartialQuantity(itemId: String, quantity: String) {
        val updatedQuantities = _uiState.value.itemPartialQuantities.toMutableMap()
        updatedQuantities[itemId] = quantity
        _uiState.value = _uiState.value.copy(itemPartialQuantities = updatedQuantities)
    }

    fun verifyIndentItems(indentId: String, userId: String) {
        // Check if there are unticked items
        val untickedItems = _uiState.value.verificationItems.filter {
            it.isFulfilled && !it.isReceived
        }

        if (untickedItems.isNotEmpty()) {
            // Show reason selection dialog for unticked items
            showReasonDialog()
            return
        }

        // Proceed with normal verification if all items are ticked
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
                            // Create outward transactions for verified items to update stock
                            // This is where stock is actually updated - only after chef verification
                            val currentIndent = _uiState.value.currentIndentForVerification
                            receivedItems.forEach { verificationItem ->
                                val indentItem = verificationItem.indentItem
                                indentItem.fulfilledQuantity?.let { fulfilledQty ->
                                    createOutwardTransactionForVerification(
                                        itemId = indentItem.itemId,
                                        quantity = fulfilledQty,
                                        indentId = indentId,
                                        chefId = currentIndent?.chefId,
                                        cuisineId = currentIndent?.cuisineId
                                    )
                                }
                            }

                            // Calculate new indent status - all items verified (since unticked items show reason dialog)
                            val newStatus = "Received"

                            // Update indent status
                            val updateRequest = UpdateIndentRequest(
                                status = newStatus,
                                receivedAt = getCurrentTimestamp()
                            )

                            repository.updateIndent(indentId, updateRequest).collect { indentResult ->
                                indentResult.fold(
                                    onSuccess = {
                                        _uiState.value = _uiState.value.copy(
                                            isVerifying = false,
                                            showVerificationDialog = false,
                                            verificationItems = emptyList(),
                                            currentIndentForVerification = null,
                                            verificationCompleted = true
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

    fun submitPartialVerification(indentId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReasons = true, verificationError = null)

            try {
                // Get ticked items (to be verified as received)
                val tickedItems = _uiState.value.verificationItems.filter { it.isReceived && it.isFulfilled }

                // Process ticked items - mark as received
                if (tickedItems.isNotEmpty()) {
                    val verifyRequest = VerifyIndentItemRequest(
                        isReceived = true,
                        receivedBy = userId,
                        receivedAt = getCurrentTimestamp()
                    )

                    val tickedItemIds = tickedItems.map { it.indentItem.id!! }

                    repository.verifyMultipleIndentItems(tickedItemIds, verifyRequest).collect { result ->
                        result.onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isSubmittingReasons = false,
                                verificationError = "Failed to verify items: ${error.message}"
                            )
                            return@collect
                        }
                    }

                    // Create outward transactions for ticked items to update stock
                    val currentIndent = _uiState.value.currentIndentForVerification
                    tickedItems.forEach { verificationItem ->
                        val indentItem = verificationItem.indentItem
                        indentItem.fulfilledQuantity?.let { fulfilledQty ->
                            createOutwardTransactionForVerification(
                                itemId = indentItem.itemId,
                                quantity = fulfilledQty,
                                indentId = indentId,
                                chefId = currentIndent?.chefId,
                                cuisineId = currentIndent?.cuisineId
                            )
                        }
                    }
                }

                // Process unticked items based on their reasons
                _uiState.value.untickedItems.forEach { item ->
                    val reason = _uiState.value.itemReasons[item.indentItem.id!!] ?: "Not Received"
                    val partialQty = _uiState.value.itemPartialQuantities[item.indentItem.id!!]

                    when (reason) {
                        "Not Received" -> {
                            // Mark as not received (isReceived = false)
                            val updateRequest = UpdateIndentItemRequest(
                                isReceived = false
                            )
                            repository.updateIndentItem(item.indentItem.id!!, updateRequest).collect { result ->
                                result.onFailure { error ->
                                    println("Failed to update item: ${error.message}")
                                }
                            }
                        }
                        "Partially Received" -> {
                            // Update fulfilled quantity to partial quantity
                            val quantity = partialQty?.toDoubleOrNull() ?: 0.0
                            val updateRequest = UpdateIndentItemRequest(
                                fulfilledQuantity = quantity,
                                isReceived = true,
                                receivedBy = userId,
                                receivedAt = getCurrentTimestamp()
                            )
                            repository.updateIndentItem(item.indentItem.id!!, updateRequest).collect { result ->
                                result.onFailure { error ->
                                    println("Failed to update item: ${error.message}")
                                }
                            }

                            // Create outward transaction for partial quantity
                            if (quantity > 0) {
                                val currentIndent = _uiState.value.currentIndentForVerification
                                createOutwardTransactionForVerification(
                                    itemId = item.indentItem.itemId,
                                    quantity = quantity,
                                    indentId = indentId,
                                    chefId = currentIndent?.chefId,
                                    cuisineId = currentIndent?.cuisineId
                                )
                            }
                        }
                    }
                }

                // Update indent status to "Partially Received"
                val updateRequest = UpdateIndentRequest(
                    status = "Partially Received",
                    receivedAt = getCurrentTimestamp()
                )

                repository.updateIndent(indentId, updateRequest).collect { indentResult ->
                    indentResult.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isSubmittingReasons = false,
                                showReasonDialog = false,
                                showVerificationDialog = false,
                                verificationItems = emptyList(),
                                untickedItems = emptyList(),
                                itemReasons = emptyMap(),
                                itemPartialQuantities = emptyMap(),
                                verificationCompleted = true
                            )
                            // Reload indents to show updated status
                            loadIndents()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isSubmittingReasons = false,
                                verificationError = error.message
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingReasons = false,
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

                // NOTE: Outward transactions (stock updates) are now created during verification,
                // not during fulfillment. Stock is only updated when chef verifies receipt.

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

    private suspend fun createOutwardTransactionForVerification(
        itemId: String,
        quantity: Double,
        indentId: String,
        chefId: String?,
        cuisineId: String?
    ) {
        try {
            val outwardRequest = CreateOutwardItemRequest(
                itemId = itemId,
                categoryId = null,
                outwardQuantity = quantity,
                cuisineType = null,
                usageDate = getCurrentDate(),
                notes = "Verified and received from indent: $indentId",
                createdBy = chefId, // Set chef ID from indent
                cuisineId = cuisineId, // Set cuisine ID from indent
                indentId = indentId,
                sourceType = "indent"
            )

            println("DEBUG - Creating outward transaction for verified item: $outwardRequest")
            outwardRepository.createOutwardItem(outwardRequest).collect { result ->
                result.fold(
                    onSuccess = {
                        println("DEBUG - Outward transaction created successfully for verified item")
                    },
                    onFailure = { error ->
                        println("DEBUG - Failed to create outward transaction for verified item: ${error.message}")
                    }
                )
            }
        } catch (e: Exception) {
            println("DEBUG - Exception creating outward transaction for verified item: ${e.message}")
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
        val currentCreatedIndent = _uiState.value.createdIndent // Preserve for toast

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
            createdIndent = currentCreatedIndent, // Keep for toast notification
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

    private fun getCurrentTime(): String {
        return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    }

    private fun loadUsages() {
        viewModelScope.launch {
            usageRepository.getAllUsages().collect { result ->
                result.fold(
                    onSuccess = { usages ->
                        val activeUsages = usages.filter { it.isActive }
                        _uiState.value = _uiState.value.copy(usages = activeUsages)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
                )
            }
        }
    }

    fun selectUsage(usage: com.attil.inventory.data.model.management.Usage) {
        _uiState.value = _uiState.value.copy(
            selectedUsage = usage,
            purpose = usage.name
        )
    }

    fun removeSelectedItem(itemId: String) {
        val currentItems = _uiState.value.availableItems.toMutableList()
        val index = currentItems.indexOfFirst { it.item.id == itemId }

        if (index != -1) {
            val updatedItem = currentItems[index].copy(
                isSelected = false,
                requestedQuantity = 0.0
            )
            currentItems[index] = updatedItem

            val selectedItems = currentItems.filter { it.isSelected }

            _uiState.value = _uiState.value.copy(
                availableItems = currentItems,
                selectedItems = selectedItems,
                totalSelectedItems = selectedItems.size
            )

            if (_uiState.value.searchQuery.isNotEmpty()) {
                filterItems()
            }
        }
    }

    fun loadChefCuisines(chefId: String) {
        viewModelScope.launch {
            userRepository.getUserById(chefId).collect { result ->
                result.fold(
                    onSuccess = { user ->
                        val assignedCuisines = user?.getAssignedCuisines() ?: emptyList()
                        _uiState.value = _uiState.value.copy(cuisines = assignedCuisines)

                        if (assignedCuisines.size == 1) {
                            _uiState.value = _uiState.value.copy(selectedCuisine = assignedCuisines.first())
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
                )
            }
        }
    }

    fun loadIndentReport(startDate: String, endDate: String, status: String?, chefId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReport = true, reportError = null)

            try {
                val filter = IndentReportFilter(
                    startDate = startDate,
                    endDate = endDate,
                    status = status,
                    chefId = chefId
                )

                repository.getIndentsForReport(filter).collect { result ->
                    result.fold(
                        onSuccess = { report ->
                            _uiState.value = _uiState.value.copy(
                                indentReport = report,
                                isLoadingReport = false,
                                reportError = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoadingReport = false,
                                reportError = error.message ?: "Failed to load indent report"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingReport = false,
                    reportError = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun exportIndentReportToPdf(context: Context) {
        viewModelScope.launch {
            try {
                val report = _uiState.value.indentReport ?: return@launch

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Creating PDF...", Toast.LENGTH_SHORT).show()
                }

                val fileName = "Indent_Report_${report.filter.startDate}_to_${report.filter.endDate}.pdf"
                val file = createIndentPdfReport(context, report, fileName)

                withContext(Dispatchers.Main) {
                    if (file != null) {
                        Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        openPdfFile(context, file)
                    } else {
                        Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("IndentViewModel", "Error exporting PDF", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun createIndentPdfReport(context: Context, report: IndentReport, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)

                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                var page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas
                val paint = Paint()

                val leftMargin = 30f
                val rightMargin = 565f
                val topMargin = 40f

                var yPosition = topMargin

                paint.textSize = 20f
                paint.isFakeBoldText = true
                canvas.drawText("Indent Report", leftMargin, yPosition, paint)
                yPosition += 30f

                paint.textSize = 12f
                paint.isFakeBoldText = false
                canvas.drawText("Period: ${report.filter.startDate} to ${report.filter.endDate}", leftMargin, yPosition, paint)
                yPosition += 20f

                paint.textSize = 10f
                canvas.drawText("Total Indents: ${report.totalIndents}", leftMargin, yPosition, paint)
                yPosition += 40f

                report.indents.forEach { indent ->
                    if (yPosition > 750f) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = topMargin
                    }

                    paint.textSize = 12f
                    paint.isFakeBoldText = true
                    canvas.drawText("Chef: ${indent.chefName}", leftMargin, yPosition, paint)
                    yPosition += 18f

                    paint.textSize = 10f
                    paint.isFakeBoldText = false
                    canvas.drawText("Cuisine: ${indent.cuisineName} | Purpose: ${indent.purpose}", leftMargin, yPosition, paint)
                    yPosition += 15f

                    canvas.drawText("Required: ${indent.requiredDate} ${indent.requiredTime}", leftMargin, yPosition, paint)
                    yPosition += 15f

                    canvas.drawText("Status: ${indent.status} | Priority: ${indent.priority}", leftMargin, yPosition, paint)
                    yPosition += 15f

                    val stats = "Items: ${indent.totalItems} | Fulfilled: ${indent.fulfilledItems} | Verified: ${indent.verifiedItems} | Rejected: ${indent.rejectedItems}"
                    canvas.drawText(stats, leftMargin, yPosition, paint)
                    yPosition += 15f

                    if (indent.fulfilledBy != null) {
                        canvas.drawText("Fulfilled by: ${indent.fulfilledBy}", leftMargin, yPosition, paint)
                        yPosition += 15f
                    }

                    yPosition += 10f

                    paint.textSize = 8f
                    paint.isFakeBoldText = true

                    val col1X = leftMargin
                    val col2X = leftMargin + 250f
                    val col3X = leftMargin + 340f
                    val col4X = leftMargin + 430f

                    if (yPosition > 720f) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = topMargin
                    }

                    canvas.drawText("Item Name", col1X, yPosition, paint)
                    canvas.drawText("Requested", col2X, yPosition, paint)
                    canvas.drawText("Fulfilled", col3X, yPosition, paint)
                    canvas.drawText("Status", col4X, yPosition, paint)
                    yPosition += 15f

                    canvas.drawLine(leftMargin, yPosition - 5f, rightMargin, yPosition - 5f, paint)
                    yPosition += 2f

                    paint.isFakeBoldText = false
                    indent.items.forEach { item ->
                        if (yPosition > 750f) {
                            pdfDocument.finishPage(page)
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            yPosition = topMargin

                            paint.isFakeBoldText = true
                            canvas.drawText("Item Name", col1X, yPosition, paint)
                            canvas.drawText("Requested", col2X, yPosition, paint)
                            canvas.drawText("Fulfilled", col3X, yPosition, paint)
                            canvas.drawText("Status", col4X, yPosition, paint)
                            yPosition += 15f
                            canvas.drawLine(leftMargin, yPosition - 5f, rightMargin, yPosition - 5f, paint)
                            yPosition += 2f
                            paint.isFakeBoldText = false
                        }

                        val status = when {
                            item.isRejected -> "Rejected"
                            item.isVerified -> "Verified"
                            item.isFulfilled -> "Fulfilled"
                            else -> "Pending"
                        }

                        val itemName = if (item.itemName.length > 30) {
                            item.itemName.substring(0, 27) + "..."
                        } else {
                            item.itemName
                        }

                        canvas.drawText(itemName, col1X, yPosition, paint)
                        canvas.drawText("${item.requestedQuantity} ${item.unitOfMeasure}", col2X, yPosition, paint)
                        canvas.drawText("${item.fulfilledQuantity ?: 0.0} ${item.unitOfMeasure}", col3X, yPosition, paint)
                        canvas.drawText(status, col4X, yPosition, paint)
                        yPosition += 14f
                    }

                    yPosition += 10f
                }

                pdfDocument.finishPage(page)
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                file
            } catch (e: Exception) {
                Log.e("IndentViewModel", "Error creating PDF", e)
                null
            }
        }
    }

    fun exportIndentReportToCsv(context: Context) {
        viewModelScope.launch {
            try {
                val report = _uiState.value.indentReport ?: return@launch

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Creating CSV...", Toast.LENGTH_SHORT).show()
                }

                val fileName = "Indent_Report_${report.filter.startDate}_to_${report.filter.endDate}.csv"
                val file = createIndentCsvReport(context, report, fileName)

                withContext(Dispatchers.Main) {
                    if (file != null) {
                        Toast.makeText(context, "CSV saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        openCsvFile(context, file)
                    } else {
                        Toast.makeText(context, "Failed to create CSV", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("IndentViewModel", "Error exporting CSV", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun createIndentCsvReport(context: Context, report: IndentReport, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { fos ->
                    val header = "Indent ID,Chef Name,Cuisine,Required Date,Required Time,Priority,Purpose,Status," +
                            "Total Items,Fulfilled Items,Verified Items,Rejected Items,Fulfilled By," +
                            "Item Name,Requested Qty,Fulfilled Qty,Unit,Item Status,Is Fulfilled,Is Verified,Is Rejected\n"
                    fos.write(header.toByteArray())

                    report.indents.forEach { indent ->
                        indent.items.forEach { item ->
                            val row = "${escapeCsv(indent.indentId)}," +
                                    "${escapeCsv(indent.chefName)}," +
                                    "${escapeCsv(indent.cuisineName)}," +
                                    "${escapeCsv(indent.requiredDate)}," +
                                    "${escapeCsv(indent.requiredTime)}," +
                                    "${escapeCsv(indent.priority)}," +
                                    "${escapeCsv(indent.purpose)}," +
                                    "${escapeCsv(indent.status)}," +
                                    "${indent.totalItems}," +
                                    "${indent.fulfilledItems}," +
                                    "${indent.verifiedItems}," +
                                    "${indent.rejectedItems}," +
                                    "${escapeCsv(indent.fulfilledBy ?: "N/A")}," +
                                    "${escapeCsv(item.itemName)}," +
                                    "${item.requestedQuantity}," +
                                    "${item.fulfilledQuantity ?: 0.0}," +
                                    "${escapeCsv(item.unitOfMeasure)}," +
                                    "${escapeCsv(item.itemStatus)}," +
                                    "${item.isFulfilled}," +
                                    "${item.isVerified}," +
                                    "${item.isRejected}\n"
                            fos.write(row.toByteArray())
                        }
                    }
                }

                file
            } catch (e: Exception) {
                Log.e("IndentViewModel", "Error creating CSV", e)
                null
            }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun openPdfFile(context: Context, file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                android.net.Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("IndentViewModel", "Error opening PDF", e)
            Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCsvFile(context: Context, file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                android.net.Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("IndentViewModel", "Error opening CSV", e)
            Toast.makeText(context, "No CSV/Excel viewer app found", Toast.LENGTH_SHORT).show()
        }
    }
}