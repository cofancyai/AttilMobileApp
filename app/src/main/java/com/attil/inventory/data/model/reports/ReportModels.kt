package com.attil.inventory.data.model.reports

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// Simplified Report Filter - only date range needed
data class ReportFilter(
    val startDate: String,
    val endDate: String
)

// Report Types
enum class ReportType {
    INWARD,
    OUTWARD,
    CUISINE_WISE
}

// INWARD REPORT MODELS
data class InwardReport(
    val reportDate: String,
    val filter: ReportFilter,
    val totalTransactions: Int,
    val totalQuantity: Double,
    val totalValue: BigDecimal,
    val items: List<InwardReportItem>
)

data class InwardReportItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("category_name")
    val categoryName: String?,
    @SerializedName("vendor_name")
    val vendorName: String,
    @SerializedName("purchase_date")
    val purchaseDate: String,
    @SerializedName("inward_quantity")
    val inwardQuantity: Double,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    @SerializedName("price_per_unit")
    val pricePerUnit: Double,
    @SerializedName("total_value")
    val totalValue: Double,
    @SerializedName("price_with_gst")
    val priceWithGst: Double?,
    @SerializedName("gst_percentage")
    val gstPercentage: Double?,
    @SerializedName("bill_number")
    val billNumber: String?,
    @SerializedName("expiry_date")
    val expiryDate: String?,
    @SerializedName("cuisine_name")
    val cuisineName: String?
)

// OUTWARD REPORT MODELS - Updated with calculated cost
data class OutwardReport(
    val reportDate: String,
    val filter: ReportFilter,
    val totalTransactions: Int,
    val totalQuantity: Double,
    val totalValue: BigDecimal,
    val items: List<OutwardReportItem>
)

data class OutwardReportItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("category_name")
    val categoryName: String?,
    @SerializedName("outward_quantity")
    val outwardQuantity: Double,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    @SerializedName("usage_date")
    val usageDate: String,
    @SerializedName("cuisine_name")
    val cuisineName: String?,
    @SerializedName("purpose")
    val purpose: String?,
    @SerializedName("estimated_value")
    val estimatedValue: Double?,
    @SerializedName("source_type")
    val sourceType: String?,
    @SerializedName("indent_id")
    val indentId: String?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("created_by")
    val createdBy: String?,
    @SerializedName("chef_name")
    val chefName: String? = null,
    // NEW: Calculated cost fields
    val calculatedCostPerUnit: Double = 0.0,
    val calculatedTotalCost: Double = 0.0,
    val costCalculationMethod: String = "N/A" // "15-day avg", "60-day avg", "last purchase", etc.
)

// Summary models for quick statistics
data class InwardSummary(
    val totalPurchases: Int,
    val totalValue: BigDecimal,
    val topVendor: String?,
    val topCategory: String?,
    val averageOrderValue: BigDecimal
)

data class OutwardSummary(
    val totalConsumptions: Int,
    val totalValue: BigDecimal,
    val topCuisine: String?,
    val topCategory: String?,
    val averageConsumptionValue: BigDecimal
)

// CUISINE-WISE REPORT MODELS
data class CuisineWiseReport(
    val reportDate: String,
    val filter: ReportFilter,
    val cuisineName: String?, // null means "All Cuisines"
    val totalTransactions: Int,
    val totalQuantity: Double,
    val totalValue: BigDecimal,
    val items: List<OutwardReportItem>, // Reuse OutwardReportItem
    val cuisineBreakdown: List<CuisineBreakdownItem> // Summary by cuisine
)

data class CuisineBreakdownItem(
    val cuisineName: String,
    val totalTransactions: Int,
    val totalQuantity: Double,
    val totalCost: Double,
    val percentageOfTotal: Double
)

// Moving average cost response model
data class MovingAverageCost(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("average_cost")
    val averageCost: Double,
    @SerializedName("calculation_method")
    val calculationMethod: String,
    @SerializedName("data_points")
    val dataPoints: Int,
    @SerializedName("date_range")
    val dateRange: String
)

// EXISTING MODELS (keeping all the original models)
data class StockLevelReportItem(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("cuisine_name")
    val cuisineName: String?,
    @SerializedName("godown_name")
    val godownName: String?,
    @SerializedName("rack_name")
    val rackName: String?,
    @SerializedName("current_quantity")
    val currentQuantity: Double,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double?,
    @SerializedName("unit_cost")
    val unitCost: Double?,
    @SerializedName("total_value")
    val totalValue: Double?,
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("stock_status")
    val stockStatus: String
)

data class StockLevelReport(
    val reportDate: String,
    val filters: ReportFilter,
    val totalItems: Int,
    val totalValue: Double,
    val lowStockItems: Int,
    val items: List<StockLevelReportItem>
)

data class LowStockAlertItem(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("current_quantity")
    val currentQuantity: Double,
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double,
    @SerializedName("shortage")
    val shortage: Double,
    @SerializedName("godown_name")
    val godownName: String?,
    @SerializedName("rack_name")
    val rackName: String?,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("urgency_level")
    val urgencyLevel: String
)

data class StockMovementItem(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("transaction_date")
    val transactionDate: String,
    @SerializedName("transaction_type")
    val transactionType: String,
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("reference_number")
    val referenceNumber: String?,
    @SerializedName("vendor_name")
    val vendorName: String?,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("notes")
    val notes: String?
)

// TRANSACTION REPORTS
data class TransactionSummaryItem(
    @SerializedName("transaction_date")
    val transactionDate: String,
    @SerializedName("inward_transactions")
    val inwardTransactions: Int,
    @SerializedName("outward_transactions")
    val outwardTransactions: Int,
    @SerializedName("inward_quantity")
    val inwardQuantity: Double,
    @SerializedName("outward_quantity")
    val outwardQuantity: Double,
    @SerializedName("inward_value")
    val inwardValue: BigDecimal,
    @SerializedName("outward_value")
    val outwardValue: BigDecimal
)

data class VendorPerformanceItem(
    @SerializedName("vendor_id")
    val vendorId: String,
    @SerializedName("vendor_name")
    val vendorName: String,
    @SerializedName("total_orders")
    val totalOrders: Int,
    @SerializedName("total_quantity")
    val totalQuantity: Double,
    @SerializedName("total_value")
    val totalValue: BigDecimal,
    @SerializedName("average_order_value")
    val averageOrderValue: BigDecimal,
    @SerializedName("last_order_date")
    val lastOrderDate: String?,
    @SerializedName("most_supplied_item")
    val mostSuppliedItem: String?
)

// INDENT REPORTS
data class IndentStatusItem(
    @SerializedName("indent_id")
    val indentId: String,
    @SerializedName("indent_number")
    val indentNumber: String?,
    @SerializedName("chef_name")
    val chefName: String,
    @SerializedName("created_date")
    val createdDate: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("total_items")
    val totalItems: Int,
    @SerializedName("fulfilled_items")
    val fulfilledItems: Int,
    @SerializedName("pending_items")
    val pendingItems: Int,
    @SerializedName("priority")
    val priority: String?
)

data class ChefRequisitionItem(
    @SerializedName("chef_id")
    val chefId: String,
    @SerializedName("chef_name")
    val chefName: String,
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("total_requests")
    val totalRequests: Int,
    @SerializedName("total_quantity")
    val totalQuantity: Double,
    @SerializedName("fulfilled_requests")
    val fulfilledRequests: Int,
    @SerializedName("fulfillment_rate")
    val fulfillmentRate: Double,
    @SerializedName("last_request_date")
    val lastRequestDate: String?
)

// FINANCIAL REPORTS
data class PurchaseCostItem(
    @SerializedName("group_key")
    val groupKey: String,
    @SerializedName("group_name")
    val groupName: String,
    @SerializedName("total_cost")
    val totalCost: BigDecimal,
    @SerializedName("total_quantity")
    val totalQuantity: Double,
    @SerializedName("average_cost")
    val averageCost: BigDecimal,
    @SerializedName("transaction_count")
    val transactionCount: Int
)

data class InventoryCostAnalysisItem(
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("total_cost")
    val totalCost: BigDecimal,
    @SerializedName("total_quantity")
    val totalQuantity: Double,
    @SerializedName("percentage_of_total")
    val percentageOfTotal: Double,
    @SerializedName("item_count")
    val itemCount: Int
)