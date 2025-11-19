package com.attil.inventory.data.model.bulk

import com.google.gson.annotations.SerializedName

// Import result data class
data class ImportResult(
    val success: Boolean,
    val totalRecords: Int,
    val successfulRecords: Int,
    val failedRecords: Int,
    val skippedDuplicates: Int = 0,
    val errors: List<String> = emptyList(),
    val message: String
)

// Bulk Import Requests for Categories
data class BulkCategoriesImport(
    val categories: List<CategoryImportRow>
)

data class CategoryImportRow(
    val name: String,
    val description: String? = null
)

// Bulk Import Requests for Racks
data class BulkRacksImport(
    val racks: List<RackImportRow>
)

data class RackImportRow(
    val name: String,
    val description: String? = null,
    @SerializedName("godown_id")
    val godownId: String
)

// Bulk Import Requests for Items
data class BulkItemsImport(
    val items: List<ItemImportRow>
)

data class ItemImportRow(
    val name: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("godown_id")
    val godownId: String? = null,
    @SerializedName("rack_id")
    val rackId: String? = null,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double,
    @SerializedName("is_active")
    val isActive: Boolean = true
)

// Bulk Import Requests for Inward Transactions
data class BulkInwardImport(
    val inwardItems: List<InwardImportRow>
)

data class InwardImportRow(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("vendor_name")
    val vendorName: String,
    @SerializedName("vendor_contact")
    val vendorContact: String? = null,
    @SerializedName("vendor_address")
    val vendorAddress: String? = null,
    @SerializedName("purchase_date")
    val purchaseDate: String,
    @SerializedName("inward_quantity")
    val inwardQuantity: Double,
    @SerializedName("price_per_unit")
    val pricePerUnit: Double,
    @SerializedName("price_without_gst")
    val priceWithoutGst: Double? = null,
    @SerializedName("gst_percentage")
    val gstPercentage: Double? = null,
    @SerializedName("price_with_gst")
    val priceWithGst: Double? = null,
    @SerializedName("bill_number")
    val billNumber: String? = null,
    @SerializedName("expiry_date")
    val expiryDate: String? = null,
    @SerializedName("cuisine_id")
    val cuisineId: String? = null,
    @SerializedName("created_by")
    val createdBy: String? = null
)

// Bulk Import Requests for Godowns
data class BulkGodownsImport(
    val godowns: List<GodownImportRow>
)

data class GodownImportRow(
    val name: String,
    val description: String? = null,
    val location: String? = null
)

// Bulk Import Requests for Cuisines
data class BulkCuisinesImport(
    val cuisines: List<CuisineImportRow>
)

data class CuisineImportRow(
    val name: String,
    val description: String? = null
)

// Bulk Import Requests for Vendors
data class BulkVendorsImport(
    val vendors: List<VendorImportRow>
)

data class VendorImportRow(
    val name: String,
    val address: String? = null,
    @SerializedName("contact_number")
    val contactNumber: String? = null,
    val email: String? = null
)

// Bulk Import Requests for Usage
data class BulkUsageImport(
    val usages: List<UsageImportRow>
)

data class UsageImportRow(
    val name: String,
    val description: String? = null
)

// Export data models
enum class ExportType {
    GODOWNS,
    CATEGORIES,
    CUISINES,
    VENDORS,
    USAGE,
    RACKS,
    ITEMS,
    CURRENT_STOCK,
    INWARD_TRANSACTIONS,
    OUTWARD_TRANSACTIONS,
    INDENT_REPORT
}

data class ExportRequest(
    val exportType: ExportType,
    val startDate: String? = null,
    val endDate: String? = null,
    val filterStatus: String? = null
)

// Import template types
enum class ImportTemplateType {
    GODOWNS,
    CATEGORIES,
    CUISINES,
    VENDORS,
    USAGE,
    RACKS,
    ITEMS,
    INITIAL_STOCK,
    INWARD_TRANSACTIONS
}
