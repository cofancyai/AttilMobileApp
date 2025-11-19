package com.attil.inventory.presentation.bulkoperations

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.bulk.ImportResult
import com.attil.inventory.data.model.management.CreateCategoryRequest
import com.attil.inventory.data.model.management.CreateRackRequest
import com.attil.inventory.data.model.management.CreateItemRequest
import com.attil.inventory.data.model.management.CreateGodownRequest
import com.attil.inventory.data.model.management.CreateCuisineRequest
import com.attil.inventory.data.model.management.CreateVendorRequest
import com.attil.inventory.data.model.management.CreateUsageRequest
import com.attil.inventory.data.model.transaction.CreateInwardItemRequest
import com.attil.inventory.data.repository.CategoryRepository
import com.attil.inventory.data.repository.ItemRepository
import com.attil.inventory.data.repository.RackRepository
import com.attil.inventory.data.repository.InwardRepository
import com.attil.inventory.data.repository.CurrentStockRepository
import com.attil.inventory.data.repository.GodownRepository
import com.attil.inventory.data.repository.CuisineRepository
import com.attil.inventory.data.repository.VendorRepository
import com.attil.inventory.data.repository.UsageRepository
import com.attil.inventory.utils.ExcelUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ImportExportUiState(
    val isProcessing: Boolean = false,
    val lastResult: ImportResult? = null,
    val selectedImportType: String? = null
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val godownRepository: GodownRepository,
    private val categoryRepository: CategoryRepository,
    private val cuisineRepository: CuisineRepository,
    private val vendorRepository: VendorRepository,
    private val usageRepository: UsageRepository,
    private val rackRepository: RackRepository,
    private val itemRepository: ItemRepository,
    private val inwardRepository: InwardRepository,
    private val currentStockRepository: CurrentStockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()

    fun setImportType(importType: String) {
        _uiState.value = _uiState.value.copy(selectedImportType = importType)
    }

    fun handleFileSelected(context: Context, uri: Uri) {
        val importType = _uiState.value.selectedImportType ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)

            try {
                // Validate file structure first
                val expectedHeaders = getExpectedHeaders(importType)
                val validation = ExcelUtils.validateExcelStructure(context, uri, expectedHeaders)

                if (!validation.isValid) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        lastResult = ImportResult(
                            success = false,
                            totalRecords = 0,
                            successfulRecords = 0,
                            failedRecords = 0,
                            errors = listOf(validation.message),
                            message = "Excel validation failed"
                        )
                    )
                    return@launch
                }

                // Read Excel data
                val excelData = ExcelUtils.readExcelFile(context, uri)

                // Process import based on type
                val result = when (importType) {
                    "GODOWNS" -> importGodowns(excelData)
                    "CATEGORIES" -> importCategories(excelData)
                    "CUISINES" -> importCuisines(excelData)
                    "VENDORS" -> importVendors(excelData)
                    "USAGE" -> importUsage(excelData)
                    "RACKS" -> importRacks(excelData)
                    "ITEMS" -> importItems(excelData)
                    "INITIAL_STOCK" -> importInitialStock(excelData)
                    "INWARD_TRANSACTIONS" -> importInwardTransactions(excelData)
                    else -> ImportResult(
                        success = false,
                        totalRecords = 0,
                        successfulRecords = 0,
                        failedRecords = 0,
                        message = "Unknown import type"
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastResult = ImportResult(
                        success = false,
                        totalRecords = 0,
                        successfulRecords = 0,
                        failedRecords = 0,
                        errors = listOf(e.message ?: "Unknown error"),
                        message = "Import failed"
                    )
                )
            }
        }
    }

    private fun getExpectedHeaders(importType: String): List<String> {
        return when (importType) {
            "GODOWNS" -> listOf("name", "description", "location")
            "CATEGORIES" -> listOf("name", "description")
            "CUISINES" -> listOf("name", "description")
            "VENDORS" -> listOf("name", "address", "contact_number", "email")
            "USAGE" -> listOf("name", "description")
            "RACKS" -> listOf("name", "description", "godown_name")
            "ITEMS" -> listOf("name", "category_name", "godown_name", "rack_name", "unit_of_measure", "minimum_stock_level")
            "INITIAL_STOCK" -> listOf("item_name", "vendor_name", "purchase_date", "inward_quantity", "price_per_unit", "price_without_gst", "gst_percentage", "price_with_gst", "bill_number")
            "INWARD_TRANSACTIONS" -> listOf("item_name", "vendor_name", "purchase_date", "inward_quantity", "price_per_unit", "price_without_gst", "gst_percentage", "price_with_gst", "bill_number")
            else -> emptyList()
        }
    }

    // IMPORT FUNCTIONS WITH UPPERCASE NORMALIZATION AND DUPLICATE CHECKING

    private suspend fun importGodowns(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch existing godowns to check for duplicates
        val existingResult = godownRepository.getAllGodowns().first()
        val existing = existingResult.getOrNull() ?: emptyList()
        val existingNames = existing.map { it.name.uppercase() }.toSet()

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"]?.trim() ?: throw Exception("Missing name")
                val nameUpper = name.uppercase()

                // Check for duplicate
                if (existingNames.contains(nameUpper)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val description = row["description"]?.trim()
                val location = row["location"]?.trim()

                val request = CreateGodownRequest(nameUpper, description, location)
                val result = godownRepository.createGodown(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount godowns"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount godowns, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} godowns, skipped $skippedCount duplicates"
            }
        )
    }

    private suspend fun importCategories(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch existing categories to check for duplicates
        val existingResult = categoryRepository.getAllCategories().first()
        val existing = existingResult.getOrNull() ?: emptyList()
        val existingNames = existing.map { it.name.uppercase() }.toSet()

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"]?.trim() ?: throw Exception("Missing name")
                val nameUpper = name.uppercase()

                // Check for duplicate
                if (existingNames.contains(nameUpper)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val description = row["description"]?.trim()

                val request = CreateCategoryRequest(nameUpper, description)
                val result = categoryRepository.createCategory(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount categories"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount categories, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} categories, skipped $skippedCount duplicates"
            }
        )
    }

    private suspend fun importCuisines(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch existing cuisines to check for duplicates
        val existingResult = cuisineRepository.getAllCuisines().first()
        val existing = existingResult.getOrNull() ?: emptyList()
        val existingNames = existing.map { it.name.uppercase() }.toSet()

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"]?.trim() ?: throw Exception("Missing name")
                val nameUpper = name.uppercase()

                // Check for duplicate
                if (existingNames.contains(nameUpper)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val description = row["description"]?.trim()

                val request = CreateCuisineRequest(nameUpper, description)
                val result = cuisineRepository.createCuisine(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount cuisines"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount cuisines, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} cuisines, skipped $skippedCount duplicates"
            }
        )
    }

    private suspend fun importVendors(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch existing vendors to check for duplicates
        val existingResult = vendorRepository.getAllVendors().first()
        val existing = existingResult.getOrNull() ?: emptyList()
        val existingNames = existing.map { it.name.uppercase() }.toSet()

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"]?.trim() ?: throw Exception("Missing name")
                val nameUpper = name.uppercase()

                // Check for duplicate
                if (existingNames.contains(nameUpper)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val address = row["address"]?.trim()
                val contactNumber = row["contact_number"]?.trim()
                val email = row["email"]?.trim()

                val request = CreateVendorRequest(nameUpper, address, contactNumber, email)
                val result = vendorRepository.createVendor(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount vendors"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount vendors, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} vendors, skipped $skippedCount duplicates"
            }
        )
    }

    private suspend fun importUsage(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch existing usages to check for duplicates
        val existingResult = usageRepository.getAllUsages().first()
        val existing = existingResult.getOrNull() ?: emptyList()
        val existingNames = existing.map { it.name.uppercase() }.toSet()

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"]?.trim() ?: throw Exception("Missing name")
                val nameUpper = name.uppercase()

                // Check for duplicate
                if (existingNames.contains(nameUpper)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val description = row["description"]?.trim()

                val request = CreateUsageRequest(nameUpper, description)
                val result = usageRepository.createUsage(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount usage types"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount usage types, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} usage types, skipped $skippedCount duplicates"
            }
        )
    }

    private suspend fun importRacks(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch all racks and godowns
        val racksResult = rackRepository.getAllRacks().first()
        if (racksResult.isFailure) {
            return ImportResult(
                success = false,
                totalRecords = excelData.size,
                successfulRecords = 0,
                failedRecords = excelData.size,
                errors = listOf("Failed to fetch existing racks: ${racksResult.exceptionOrNull()?.message}"),
                message = "Import failed - could not fetch existing data"
            )
        }

        val racks = racksResult.getOrNull() ?: emptyList()
        // Create duplicate check map: "RACKNAME_GODOWNID" to detect duplicates
        val existingRackKeys = racks.map { "${it.name.uppercase()}_${it.godownId}" }.toSet()

        val godowns = racks.mapNotNull { it.godowns }.distinctBy { it.id }
        val godownMap = godowns.associate { it.name.uppercase() to (it.id ?: "") }

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"]?.trim() ?: throw Exception("Missing name")
                val nameUpper = name.uppercase()
                val description = row["description"]?.trim()
                val godownName = row["godown_name"]?.trim() ?: throw Exception("Missing godown_name")

                // Lookup godown ID by name (case-insensitive)
                val godownId = godownMap[godownName.uppercase()]
                    ?: throw Exception("Godown '$godownName' not found. Please create it first.")

                // Check for duplicate (rack name + godown combination)
                val rackKey = "${nameUpper}_${godownId}"
                if (existingRackKeys.contains(rackKey)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val request = CreateRackRequest(nameUpper, description, godownId)
                val result = rackRepository.createRack(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount racks"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount racks, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} racks, skipped $skippedCount duplicates"
            }
        )
    }

    private suspend fun importItems(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch existing items
        val existingItemsResult = itemRepository.getAllItems().first()
        val existingItems = existingItemsResult.getOrNull() ?: emptyList()
        val existingItemNames = existingItems.map { it.name.uppercase() }.toSet()

        // Fetch all categories, racks, godowns and create name-to-ID mappings
        val categoriesResult = categoryRepository.getAllCategories().first()
        if (categoriesResult.isFailure) {
            return ImportResult(
                success = false,
                totalRecords = excelData.size,
                successfulRecords = 0,
                failedRecords = excelData.size,
                errors = listOf("Failed to fetch categories: ${categoriesResult.exceptionOrNull()?.message}"),
                message = "Import failed - could not fetch existing data"
            )
        }

        val categories = categoriesResult.getOrNull() ?: emptyList()
        val categoryMap = categories.associate { it.name.uppercase() to (it.id ?: "") }

        val racksResult = rackRepository.getAllRacks().first()
        if (racksResult.isFailure) {
            return ImportResult(
                success = false,
                totalRecords = excelData.size,
                successfulRecords = 0,
                failedRecords = excelData.size,
                errors = listOf("Failed to fetch racks: ${racksResult.exceptionOrNull()?.message}"),
                message = "Import failed - could not fetch existing data"
            )
        }

        val racks = racksResult.getOrNull() ?: emptyList()
        val rackMap = racks.associate { it.name.uppercase() to (it.id ?: "") }

        val godowns = racks.mapNotNull { it.godowns }.distinctBy { it.id }
        val godownMap = godowns.associate { it.name.uppercase() to (it.id ?: "") }

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"]?.trim() ?: throw Exception("Missing name")
                val nameUpper = name.uppercase()

                // Check for duplicate
                if (existingItemNames.contains(nameUpper)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val categoryName = row["category_name"]?.trim() ?: throw Exception("Missing category_name")
                val unitOfMeasure = row["unit_of_measure"]?.trim()?.uppercase() ?: throw Exception("Missing unit_of_measure")
                val minimumStockLevel = row["minimum_stock_level"]?.toDoubleOrNull() ?: 0.0
                val godownName = row["godown_name"]?.trim()
                val rackName = row["rack_name"]?.trim()

                // Lookup category ID by name (required, case-insensitive)
                val categoryId = categoryMap[categoryName.uppercase()]
                    ?: throw Exception("Category '$categoryName' not found. Please import categories first.")

                // Lookup godown ID by name (optional, case-insensitive)
                val godownId = godownName?.let {
                    if (it.isNotBlank()) {
                        godownMap[it.uppercase()]
                            ?: throw Exception("Godown '$it' not found. Please create it first.")
                    } else null
                }

                // Lookup rack ID by name (optional, case-insensitive)
                val rackId = rackName?.let {
                    if (it.isNotBlank()) {
                        rackMap[it.uppercase()]
                            ?: throw Exception("Rack '$it' not found. Please import racks first.")
                    } else null
                }

                val request = CreateItemRequest(
                    name = nameUpper,
                    categoryId = categoryId,
                    godownId = godownId,
                    rackId = rackId,
                    unitOfMeasure = unitOfMeasure,
                    minimumStockLevel = minimumStockLevel,
                    cuisineIds = emptyList()
                )
                val result = itemRepository.createItem(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount items"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount items, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} items, skipped $skippedCount duplicates"
            }
        )
    }

    private suspend fun importInitialStock(excelData: List<Map<String, String>>): ImportResult {
        return importInwardTransactions(excelData)
    }

    private suspend fun importInwardTransactions(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0
        var skippedCount = 0

        // Fetch existing inward items to check for duplicates
        val existingInwardResult = inwardRepository.getAllInwardItems().first()
        val existingInward = existingInwardResult.getOrNull() ?: emptyList()
        // Create duplicate key: "ITEMID_BILLNUMBER_DATE" (bill number can be null, so use "NONE")
        val existingKeys = existingInward.map {
            "${it.itemId}_${it.billNumber?.uppercase() ?: "NONE"}_${it.purchaseDate}"
        }.toSet()

        // Fetch all items and create name-to-ID mapping
        val itemsResult = itemRepository.getAllItems().first()
        if (itemsResult.isFailure) {
            return ImportResult(
                success = false,
                totalRecords = excelData.size,
                successfulRecords = 0,
                failedRecords = excelData.size,
                errors = listOf("Failed to fetch items: ${itemsResult.exceptionOrNull()?.message}"),
                message = "Import failed - could not fetch existing data"
            )
        }

        val items = itemsResult.getOrNull() ?: emptyList()
        val itemMap = items.associate { it.name.uppercase() to (it.id ?: "") }

        // Fetch all cuisines and create name-to-ID mapping
        val cuisinesResult = cuisineRepository.getAllCuisines().first()
        val cuisines = cuisinesResult.getOrNull() ?: emptyList()
        val cuisineMap = cuisines.associate { it.name.uppercase() to (it.id ?: "") }

        excelData.forEachIndexed { index, row ->
            try {
                val itemName = row["item_name"]?.trim() ?: throw Exception("Missing item_name")
                val vendorName = row["vendor_name"]?.trim()?.uppercase() ?: "INITIAL STOCK IMPORT"
                val purchaseDate = row["purchase_date"]?.trim() ?: throw Exception("Missing purchase_date")
                val inwardQuantity = row["inward_quantity"]?.toDoubleOrNull() ?: throw Exception("Invalid inward_quantity")
                val pricePerUnit = row["price_per_unit"]?.toDoubleOrNull() ?: 0.0
                val priceWithoutGst = row["price_without_gst"]?.toDoubleOrNull()
                val gstPercentage = row["gst_percentage"]?.toDoubleOrNull()
                val priceWithGst = row["price_with_gst"]?.toDoubleOrNull()
                val billNumber = row["bill_number"]?.trim()?.uppercase()
                val cuisineName = row["cuisine_name"]?.trim()

                // Lookup item ID by name (case-insensitive)
                val itemId = itemMap[itemName.uppercase()]
                    ?: throw Exception("Item '$itemName' not found. Please import items first.")

                // Lookup cuisine ID by name (optional, case-insensitive)
                val cuisineId = cuisineName?.let {
                    if (it.isNotBlank()) {
                        cuisineMap[it.uppercase()]
                            ?: throw Exception("Cuisine '$it' not found. Please import cuisines first.")
                    } else null
                }

                // Check for duplicate
                val duplicateKey = "${itemId}_${billNumber ?: "NONE"}_${purchaseDate}"
                if (existingKeys.contains(duplicateKey)) {
                    skippedCount++
                    return@forEachIndexed
                }

                val request = CreateInwardItemRequest(
                    itemId = itemId,
                    vendorName = vendorName,
                    vendorContact = null,
                    vendorAddress = null,
                    purchaseDate = purchaseDate,
                    inwardQuantity = inwardQuantity,
                    pricePerUnit = pricePerUnit,
                    priceWithoutGst = priceWithoutGst,
                    gstPercentage = gstPercentage,
                    priceWithGst = priceWithGst,
                    billNumber = billNumber,
                    expiryDate = null,
                    cuisineId = cuisineId,
                    createdBy = null
                )
                val result = inwardRepository.createInwardItem(request).first()

                if (result.isFailure) {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

                successCount++
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }

        return ImportResult(
            success = errors.isEmpty(),
            totalRecords = excelData.size,
            successfulRecords = successCount,
            failedRecords = errors.size,
            skippedDuplicates = skippedCount,
            errors = errors,
            message = when {
                errors.isEmpty() && skippedCount == 0 -> "Successfully imported $successCount inward transactions"
                errors.isEmpty() && skippedCount > 0 -> "Imported $successCount inward transactions, skipped $skippedCount duplicates"
                else -> "Imported $successCount out of ${excelData.size} inward transactions, skipped $skippedCount duplicates"
            }
        )
    }

    fun exportData(context: Context, exportType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)

            try {
                val fileName = "${exportType.lowercase()}_export_${System.currentTimeMillis()}.xlsx"

                // Create file and get URI based on Android version
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ (Scoped Storage) - save to Downloads
                    saveToDownloads(context, fileName, exportType)
                } else {
                    // Android 9 and below - save to external storage
                    saveToExternalStorage(context, fileName, exportType)
                }

                // Open the exported file directly
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // If no app can open Excel, just show success message
                    // File is already saved to Downloads
                }

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastResult = ImportResult(
                        success = true,
                        totalRecords = 0,
                        successfulRecords = 0,
                        failedRecords = 0,
                        message = "File saved to Downloads folder: $fileName"
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastResult = ImportResult(
                        success = false,
                        totalRecords = 0,
                        successfulRecords = 0,
                        failedRecords = 0,
                        errors = listOf(e.message ?: "Unknown error"),
                        message = "Export failed"
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveToDownloads(context: Context, fileName: String, exportType: String): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw Exception("Failed to create file in Downloads")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            try {
                when (exportType) {
                    "GODOWNS" -> {
                        val result = godownRepository.getAllGodowns().first()
                        val godowns = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Description", "Location", "Created At")
                        val data = godowns.map { godown ->
                            listOf(godown.id ?: "", godown.name, godown.description ?: "", godown.location ?: "", godown.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Godowns", headers, data)
                    }
                    "CATEGORIES" -> {
                        val result = categoryRepository.getAllCategories().first()
                        val categories = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Description", "Created At")
                        val data = categories.map { category ->
                            listOf(category.id ?: "", category.name, category.description ?: "", category.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Categories", headers, data)
                    }
                    "CUISINES" -> {
                        val result = cuisineRepository.getAllCuisines().first()
                        val cuisines = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Description", "Created At")
                        val data = cuisines.map { cuisine ->
                            listOf(cuisine.id ?: "", cuisine.name, cuisine.description ?: "", cuisine.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Cuisines", headers, data)
                    }
                    "VENDORS" -> {
                        val result = vendorRepository.getAllVendors().first()
                        val vendors = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Address", "Contact Number", "Email", "Created At")
                        val data = vendors.map { vendor ->
                            listOf(vendor.id ?: "", vendor.name, vendor.address ?: "", vendor.contactNumber ?: "", vendor.email ?: "", vendor.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Vendors", headers, data)
                    }
                    "USAGE" -> {
                        val result = usageRepository.getAllUsages().first()
                        val usages = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Description", "Created At")
                        val data = usages.map { usage ->
                            listOf(usage.id ?: "", usage.name, usage.description ?: "", usage.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Usage", headers, data)
                    }
                    "RACKS" -> {
                        val result = rackRepository.getAllRacks().first()
                        val racks = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Description", "Godown ID", "Is Active", "Created At")
                        val data = racks.map { rack ->
                            listOf(rack.id ?: "", rack.name, rack.description ?: "", rack.godownId, rack.isActive, rack.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Racks", headers, data)
                    }
                    "ITEMS" -> {
                        val result = itemRepository.getAllItems().first()
                        val items = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Category ID", "Godown ID", "Rack ID", "Unit", "Min Stock", "Is Active", "Created At")
                        val data = items.map { item ->
                            listOf(item.id ?: "", item.name, item.categoryId, item.godownId ?: "", item.rackId ?: "", item.unitOfMeasure, item.minimumStockLevel, item.isActive, item.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Items", headers, data)
                    }
                    "CURRENT_STOCK" -> {
                        val result = currentStockRepository.getAllCurrentStocks().first()
                        val stocks = result.getOrThrow()
                        val headers = listOf("Item", "Category", "Godown", "Rack", "Unit", "Min Stock", "Inward", "Outward", "Current Stock", "Low Stock")
                        val data = stocks.map { stock ->
                            listOf(stock.itemName, stock.categoryName, stock.godownName ?: "", stock.rackName ?: "", stock.unitOfMeasure, stock.minimumStockLevel, stock.totalInward, stock.totalOutward, stock.currentStock, stock.isLowStock)
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Current Stock", headers, data)
                    }
                    "ITEMS_BY_RACK" -> {
                        // For Android 10+, we need to use a temporary file approach
                        val tempFile = File.createTempFile("items_by_rack_", ".xlsx", context.cacheDir)
                        exportItemsByRack(context, tempFile)
                        tempFile.inputStream().use { input ->
                            input.copyTo(outputStream)
                        }
                        tempFile.delete()
                    }
                    "INWARD_TRANSACTIONS" -> {
                        val result = inwardRepository.getAllInwardItems().first()
                        val inwardItems = result.getOrThrow()
                        val headers = listOf("ID", "Item ID", "Vendor", "Date", "Quantity", "Price/Unit", "Price-GST", "GST%", "Price+GST", "Bill", "Created")
                        val data = inwardItems.map { inward ->
                            listOf(inward.id ?: "", inward.itemId, inward.vendorName, inward.purchaseDate, inward.inwardQuantity, inward.pricePerUnit, inward.priceWithoutGst ?: 0.0, inward.gstPercentage ?: 0.0, inward.priceWithGst ?: 0.0, inward.billNumber ?: "", inward.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Inward Transactions", headers, data)
                    }
                    else -> {
                        ExcelUtils.createExcelFile(context, outputStream, "Export", listOf("Message"), listOf(listOf("Not implemented yet")))
                    }
                }
            } catch (e: Exception) {
                // Re-throw with clearer message
                throw Exception("Failed to export $exportType: ${e.message}", e)
            }
        }

        return uri
    }

    private suspend fun saveToExternalStorage(context: Context, fileName: String, exportType: String): Uri {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        file.outputStream().use { outputStream ->
            when (exportType) {
                "GODOWNS" -> exportGodowns(context, file)
                "CATEGORIES" -> exportCategories(context, file)
                "CUISINES" -> exportCuisines(context, file)
                "VENDORS" -> exportVendors(context, file)
                "USAGE" -> exportUsage(context, file)
                "RACKS" -> exportRacks(context, file)
                "ITEMS" -> exportItems(context, file)
                "CURRENT_STOCK" -> exportCurrentStock(context, file)
                "ITEMS_BY_RACK" -> exportItemsByRack(context, file)
                "INWARD_TRANSACTIONS" -> exportInwardTransactions(context, file)
                else -> {
                    ExcelUtils.createExcelFile(context, outputStream, "Export", listOf("Message"), listOf(listOf("Not implemented yet")))
                }
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private suspend fun exportGodowns(context: Context, file: File) {
        val godowns = godownRepository.getAllGodowns().first().getOrThrow()
        val headers = listOf("ID", "Name", "Description", "Location", "Created At")
        val data = godowns.map { godown ->
            listOf(
                godown.id ?: "",
                godown.name,
                godown.description ?: "",
                godown.location ?: "",
                godown.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Godowns",
                headers,
                data
            )
        }
    }

    private suspend fun exportCategories(context: Context, file: File) {
        val categories = categoryRepository.getAllCategories().first().getOrThrow()
        val headers = listOf("ID", "Name", "Description", "Created At")
        val data = categories.map { category ->
            listOf(
                category.id ?: "",
                category.name,
                category.description ?: "",
                category.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Categories",
                headers,
                data
            )
        }
    }

    private suspend fun exportCuisines(context: Context, file: File) {
        val cuisines = cuisineRepository.getAllCuisines().first().getOrThrow()
        val headers = listOf("ID", "Name", "Description", "Created At")
        val data = cuisines.map { cuisine ->
            listOf(
                cuisine.id ?: "",
                cuisine.name,
                cuisine.description ?: "",
                cuisine.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Cuisines",
                headers,
                data
            )
        }
    }

    private suspend fun exportVendors(context: Context, file: File) {
        val vendors = vendorRepository.getAllVendors().first().getOrThrow()
        val headers = listOf("ID", "Name", "Address", "Contact Number", "Email", "Created At")
        val data = vendors.map { vendor ->
            listOf(
                vendor.id ?: "",
                vendor.name,
                vendor.address ?: "",
                vendor.contactNumber ?: "",
                vendor.email ?: "",
                vendor.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Vendors",
                headers,
                data
            )
        }
    }

    private suspend fun exportUsage(context: Context, file: File) {
        val usages = usageRepository.getAllUsages().first().getOrThrow()
        val headers = listOf("ID", "Name", "Description", "Created At")
        val data = usages.map { usage ->
            listOf(
                usage.id ?: "",
                usage.name,
                usage.description ?: "",
                usage.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Usage",
                headers,
                data
            )
        }
    }

    private suspend fun exportRacks(context: Context, file: File) {
        val racks = rackRepository.getAllRacks().first().getOrThrow()

        // Fetch all godowns to map IDs to names
        val godowns = godownRepository.getAllGodowns().first().getOrThrow()
        val godownMap = godowns.associate { it.id to it.name }

        val headers = listOf("ID", "Name", "Description", "Godown Name", "Is Active", "Created At")
        val data = racks.map { rack ->
            listOf(
                rack.id ?: "",
                rack.name,
                rack.description ?: "",
                godownMap[rack.godownId] ?: rack.godownId,  // Show name, fallback to ID
                rack.isActive,
                rack.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Racks",
                headers,
                data
            )
        }
    }

    private suspend fun exportItems(context: Context, file: File) {
        val items = itemRepository.getAllItems().first().getOrThrow()

        // Fetch all related entities to map IDs to names
        val categories = categoryRepository.getAllCategories().first().getOrThrow()
        val godowns = godownRepository.getAllGodowns().first().getOrThrow()
        val racks = rackRepository.getAllRacks().first().getOrThrow()

        val categoryMap = categories.associate { it.id to it.name }
        val godownMap = godowns.associate { it.id to it.name }
        val rackMap = racks.associate { it.id to it.name }

        val headers = listOf("ID", "Name", "Category Name", "Godown Name", "Rack Name", "Unit of Measure", "Minimum Stock Level", "Is Active", "Created At")
        val data = items.map { item ->
            listOf(
                item.id ?: "",
                item.name,
                categoryMap[item.categoryId] ?: item.categoryId,  // Show name, fallback to ID
                if (item.godownId != null) godownMap[item.godownId] ?: item.godownId else "",
                if (item.rackId != null) rackMap[item.rackId] ?: item.rackId else "",
                item.unitOfMeasure,
                item.minimumStockLevel,
                item.isActive,
                item.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Items",
                headers,
                data
            )
        }
    }

    private suspend fun exportCurrentStock(context: Context, file: File) {
        val stocks = currentStockRepository.getAllCurrentStocks().first().getOrThrow()
        val headers = listOf("Item Name", "Category", "Godown", "Rack", "Unit", "Min Stock", "Total Inward", "Total Outward", "Current Stock", "Is Low Stock")
        val data = stocks.map { stock ->
            listOf(
                stock.itemName,
                stock.categoryName,
                stock.godownName ?: "",
                stock.rackName ?: "",
                stock.unitOfMeasure,
                stock.minimumStockLevel,
                stock.totalInward,
                stock.totalOutward,
                stock.currentStock,
                stock.isLowStock
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Current Stock",
                headers,
                data
            )
        }
    }

    private suspend fun exportInwardTransactions(context: Context, file: File) {
        val inwardItems = inwardRepository.getAllInwardItems().first().getOrThrow()
        val headers = listOf("ID", "Item ID", "Vendor Name", "Purchase Date", "Quantity", "Price Per Unit", "Price Without GST", "GST %", "Price With GST", "Bill Number", "Created At")
        val data = inwardItems.map { inward ->
            listOf(
                inward.id ?: "",
                inward.itemId,
                inward.vendorName,
                inward.purchaseDate,
                inward.inwardQuantity,
                inward.pricePerUnit,
                inward.priceWithoutGst ?: 0.0,
                inward.gstPercentage ?: 0.0,
                inward.priceWithGst ?: 0.0,
                inward.billNumber ?: "",
                inward.createdAt ?: ""
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Inward Transactions",
                headers,
                data
            )
        }
    }

    private suspend fun exportOutwardTransactions(context: Context, file: File) {
        // TODO: Implement outward transactions export
        // Similar to inward but using OutwardRepository
        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Outward Transactions",
                listOf("Not implemented yet"),
                emptyList()
            )
        }
    }

    private suspend fun exportItemsByRack(context: Context, file: File) {
        // Fetch all necessary data
        val racks = rackRepository.getAllRacks().first().getOrThrow()
        val items = itemRepository.getAllItems().first().getOrThrow()
        val categories = categoryRepository.getAllCategories().first().getOrThrow()
        val godowns = godownRepository.getAllGodowns().first().getOrThrow()
        val currentStocks = currentStockRepository.getAllCurrentStocks().first().getOrThrow()

        // Create mapping for lookups
        val categoryMap = categories.associate { it.id to it.name }
        val godownMap = godowns.associate { it.id to it.name }
        val rackMap = racks.associate { it.id to it.name }
        val stockMap = currentStocks.associate { it.itemName to it.currentStock }

        // Headers for the export
        val headers = listOf(
            "Rack Name",
            "Godown Name",
            "Item Name",
            "Category Name",
            "Unit of Measure",
            "Minimum Stock Level",
            "Current Stock",
            "Is Active"
        )

        // Group items by rack and create data rows
        val data = mutableListOf<List<Any>>()

        // Sort racks by godown and then by rack name for better organization
        val sortedRacks = racks.sortedWith(compareBy({ godownMap[it.godownId] ?: "" }, { it.name }))

        sortedRacks.forEach { rack ->
            // Find all items for this rack
            val rackItems = items.filter { it.rackId == rack.id }.sortedBy { it.name }

            rackItems.forEach { item ->
                data.add(
                    listOf(
                        rack.name,
                        godownMap[rack.godownId] ?: "",
                        item.name,
                        categoryMap[item.categoryId] ?: "",
                        item.unitOfMeasure,
                        item.minimumStockLevel,
                        stockMap[item.name] ?: 0.0,
                        item.isActive
                    )
                )
            }
        }

        // Also include items without a rack at the end
        val unassignedItems = items.filter { it.rackId == null }.sortedBy { it.name }
        unassignedItems.forEach { item ->
            data.add(
                listOf(
                    "UNASSIGNED",
                    if (item.godownId != null) godownMap[item.godownId] ?: "" else "",
                    item.name,
                    categoryMap[item.categoryId] ?: "",
                    item.unitOfMeasure,
                    item.minimumStockLevel,
                    stockMap[item.name] ?: 0.0,
                    item.isActive
                )
            )
        }

        file.outputStream().use { outputStream ->
            ExcelUtils.createExcelFile(
                context,
                outputStream,
                "Items by Rack",
                headers,
                data
            )
        }
    }

    fun downloadTemplate(context: Context, templateType: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)

                Log.d("ImportExportVM", "Starting template download for type: $templateType")
                val fileName = "${templateType.lowercase()}_template.xlsx"
                val headers = getTemplateHeaders(templateType)
                Log.d("ImportExportVM", "Template headers: $headers")

                val uri = withContext(Dispatchers.IO) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Log.d("ImportExportVM", "Using MediaStore API (Android 10+)")
                            saveTemplateToDownloads(context, fileName, templateType, headers)
                        } else {
                            Log.d("ImportExportVM", "Using legacy file storage (Android 9-)")
                            saveTemplateToExternalStorage(context, fileName, templateType, headers)
                        }
                    } catch (e: Exception) {
                        Log.e("ImportExportVM", "Error saving template file: ${e.message}", e)
                        throw e
                    }
                }

                Log.d("ImportExportVM", "Template saved successfully at: $uri")

                // Try to open the file, but don't crash if no app is available
                withContext(Dispatchers.Main) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        // Use chooser to avoid crash
                        val chooser = Intent.createChooser(intent, "Open template with")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                        Log.d("ImportExportVM", "Template opened successfully")
                    } catch (e: Exception) {
                        Log.e("ImportExportVM", "Could not open file, but it was saved: ${e.message}")
                        // File is still saved, just can't open it
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastResult = ImportResult(
                        success = true,
                        totalRecords = 0,
                        successfulRecords = 0,
                        failedRecords = 0,
                        message = "Template downloaded to Downloads folder: $fileName"
                    )
                )
            } catch (e: Exception) {
                Log.e("ImportExportVM", "Failed to download template: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastResult = ImportResult(
                        success = false,
                        totalRecords = 0,
                        successfulRecords = 0,
                        failedRecords = 0,
                        message = "Failed to download template: ${e.message}"
                    )
                )
            }
        }
    }

    private fun getTemplateHeaders(templateType: String): List<String> {
        return when (templateType) {
            "GODOWNS" -> listOf("name", "description", "location")
            "CATEGORIES" -> listOf("name", "description")
            "CUISINES" -> listOf("name", "description")
            "VENDORS" -> listOf("name", "address", "contact_number", "email")
            "USAGE" -> listOf("name", "description")
            "RACKS" -> listOf("name", "description", "godown_name")
            "ITEMS" -> listOf("name", "category_name", "godown_name", "rack_name", "unit_of_measure", "minimum_stock_level", "is_active")
            "INITIAL_STOCK" -> listOf("item_name", "vendor_name", "vendor_contact", "vendor_address", "purchase_date", "inward_quantity", "price_per_unit", "price_without_gst", "gst_percentage", "price_with_gst", "bill_number", "expiry_date", "cuisine_name")
            "INWARD_TRANSACTIONS" -> listOf("item_name", "vendor_name", "vendor_contact", "vendor_address", "purchase_date", "inward_quantity", "price_per_unit", "price_without_gst", "gst_percentage", "price_with_gst", "bill_number", "expiry_date", "cuisine_name")
            else -> listOf("Invalid template type")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveTemplateToDownloads(context: Context, fileName: String, templateType: String, headers: List<String>): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to create file in Downloads")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val sheetName = templateType.split("_").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
            ExcelUtils.createExcelFile(context, outputStream, sheetName, headers, emptyList())
        } ?: throw Exception("Failed to open output stream")

        return uri
    }

    private fun saveTemplateToExternalStorage(context: Context, fileName: String, templateType: String, headers: List<String>): Uri {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)

        file.outputStream().use { outputStream ->
            val sheetName = templateType.split("_").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
            ExcelUtils.createExcelFile(context, outputStream, sheetName, headers, emptyList())
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
