package com.attil.inventory.presentation.bulkoperations

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.bulk.ImportResult
import com.attil.inventory.data.model.management.CreateCategoryRequest
import com.attil.inventory.data.model.management.CreateRackRequest
import com.attil.inventory.data.model.management.CreateItemRequest
import com.attil.inventory.data.model.transaction.CreateInwardItemRequest
import com.attil.inventory.data.repository.CategoryRepository
import com.attil.inventory.data.repository.ItemRepository
import com.attil.inventory.data.repository.RackRepository
import com.attil.inventory.data.repository.InwardRepository
import com.attil.inventory.data.repository.CurrentStockRepository
import com.attil.inventory.utils.ExcelUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ImportExportUiState(
    val isProcessing: Boolean = false,
    val lastResult: ImportResult? = null,
    val selectedImportType: String? = null
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
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
                    "CATEGORIES" -> importCategories(excelData)
                    "RACKS" -> importRacks(excelData)
                    "ITEMS" -> importItems(excelData)
                    "INITIAL_STOCK" -> importInitialStock(excelData)
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
            "CATEGORIES" -> listOf("name", "description")
            "RACKS" -> listOf("name", "description", "godown_name")
            "ITEMS" -> listOf("name", "category_name", "godown_name", "rack_name", "unit_of_measure", "minimum_stock_level")
            "INITIAL_STOCK" -> listOf("item_name", "vendor_name", "purchase_date", "inward_quantity", "price_per_unit", "price_without_gst", "gst_percentage", "price_with_gst")
            else -> emptyList()
        }
    }

    private suspend fun importCategories(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"] ?: throw Exception("Missing name")
                val description = row["description"]

                val request = CreateCategoryRequest(name, description)
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
            errors = errors,
            message = if (errors.isEmpty()) "Successfully imported $successCount categories"
            else "Imported $successCount out of ${excelData.size} categories"
        )
    }

    private suspend fun importRacks(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0

        // Fetch all racks with godowns and create name-to-ID mapping
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
        val godowns = racks.mapNotNull { it.godowns }.distinctBy { it.id }
        val godownMap = godowns.associate { it.name.trim().lowercase() to (it.id ?: "") }

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"] ?: throw Exception("Missing name")
                val description = row["description"]
                val godownName = row["godown_name"] ?: throw Exception("Missing godown_name")

                // Lookup godown ID by name
                val godownId = godownMap[godownName.trim().lowercase()]
                    ?: throw Exception("Godown '$godownName' not found. Please create it first.")

                val request = CreateRackRequest(name, description, godownId)
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
            errors = errors,
            message = if (errors.isEmpty()) "Successfully imported $successCount racks"
            else "Imported $successCount out of ${excelData.size} racks"
        )
    }

    private suspend fun importItems(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0

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
        val categoryMap = categories.associate { it.name.trim().lowercase() to (it.id ?: "") }

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
        val rackMap = racks.associate { it.name.trim().lowercase() to (it.id ?: "") }

        val godowns = racks.mapNotNull { it.godowns }.distinctBy { it.id }
        val godownMap = godowns.associate { it.name.trim().lowercase() to (it.id ?: "") }

        excelData.forEachIndexed { index, row ->
            try {
                val name = row["name"] ?: throw Exception("Missing name")
                val categoryName = row["category_name"] ?: throw Exception("Missing category_name")
                val unitOfMeasure = row["unit_of_measure"] ?: throw Exception("Missing unit_of_measure")
                val minimumStockLevel = row["minimum_stock_level"]?.toDoubleOrNull() ?: 0.0
                val godownName = row["godown_name"]
                val rackName = row["rack_name"]

                // Lookup category ID by name (required)
                val categoryId = categoryMap[categoryName.trim().lowercase()]
                    ?: throw Exception("Category '$categoryName' not found. Please import categories first.")

                // Lookup godown ID by name (optional)
                val godownId = godownName?.let {
                    if (it.isNotBlank()) {
                        godownMap[it.trim().lowercase()]
                            ?: throw Exception("Godown '$it' not found. Please create it first.")
                    } else null
                }

                // Lookup rack ID by name (optional)
                val rackId = rackName?.let {
                    if (it.isNotBlank()) {
                        rackMap[it.trim().lowercase()]
                            ?: throw Exception("Rack '$it' not found. Please import racks first.")
                    } else null
                }

                val request = CreateItemRequest(
                    name = name,
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
            errors = errors,
            message = if (errors.isEmpty()) "Successfully imported $successCount items"
            else "Imported $successCount out of ${excelData.size} items"
        )
    }

    private suspend fun importInitialStock(excelData: List<Map<String, String>>): ImportResult {
        val errors = mutableListOf<String>()
        var successCount = 0

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
        val itemMap = items.associate { it.name.trim().lowercase() to (it.id ?: "") }

        excelData.forEachIndexed { index, row ->
            try {
                val itemName = row["item_name"] ?: throw Exception("Missing item_name")
                val vendorName = row["vendor_name"] ?: "Initial Stock Import"
                val purchaseDate = row["purchase_date"] ?: throw Exception("Missing purchase_date")
                val inwardQuantity = row["inward_quantity"]?.toDoubleOrNull() ?: throw Exception("Invalid inward_quantity")
                val pricePerUnit = row["price_per_unit"]?.toDoubleOrNull() ?: 0.0
                val priceWithoutGst = row["price_without_gst"]?.toDoubleOrNull()
                val gstPercentage = row["gst_percentage"]?.toDoubleOrNull()
                val priceWithGst = row["price_with_gst"]?.toDoubleOrNull()

                // Lookup item ID by name
                val itemId = itemMap[itemName.trim().lowercase()]
                    ?: throw Exception("Item '$itemName' not found. Please import items first.")

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
                    billNumber = row["bill_number"],
                    expiryDate = null,
                    cuisineId = null,
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
            errors = errors,
            message = if (errors.isEmpty()) "Successfully imported $successCount inward transactions"
            else "Imported $successCount out of ${excelData.size} inward transactions"
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
                    "CATEGORIES" -> {
                        val result = categoryRepository.getAllCategories().first()
                        val categories = result.getOrThrow()
                        val headers = listOf("ID", "Name", "Description", "Created At")
                        val data = categories.map { category ->
                            listOf(category.id ?: "", category.name, category.description ?: "", category.createdAt ?: "")
                        }
                        ExcelUtils.createExcelFile(context, outputStream, "Categories", headers, data)
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
                "CATEGORIES" -> exportCategories(context, file)
                "RACKS" -> exportRacks(context, file)
                "ITEMS" -> exportItems(context, file)
                "CURRENT_STOCK" -> exportCurrentStock(context, file)
                "INWARD_TRANSACTIONS" -> exportInwardTransactions(context, file)
                else -> {
                    ExcelUtils.createExcelFile(context, outputStream, "Export", listOf("Message"), listOf(listOf("Not implemented yet")))
                }
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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

    private suspend fun exportRacks(context: Context, file: File) {
        val racks = rackRepository.getAllRacks().first().getOrThrow()
        val headers = listOf("ID", "Name", "Description", "Godown ID", "Is Active", "Created At")
        val data = racks.map { rack ->
            listOf(
                rack.id ?: "",
                rack.name,
                rack.description ?: "",
                rack.godownId,
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
        val headers = listOf("ID", "Name", "Category ID", "Godown ID", "Rack ID", "Unit of Measure", "Minimum Stock Level", "Is Active", "Created At")
        val data = items.map { item ->
            listOf(
                item.id ?: "",
                item.name,
                item.categoryId,
                item.godownId ?: "",
                item.rackId ?: "",
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

    fun downloadTemplate(context: Context, filename: String) {
        // TODO: Implement template download from assets
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                lastResult = ImportResult(
                    success = false,
                    totalRecords = 0,
                    successfulRecords = 0,
                    failedRecords = 0,
                    message = "Template download not yet implemented. Please use the templates in the Excel folder."
                )
            )
        }
    }
}
