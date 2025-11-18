package com.attil.inventory.data.repository

import android.util.Log
import com.attil.inventory.data.model.reports.*
import com.attil.inventory.data.remote.ReportApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportApiService: ReportApiService
) {

    // INWARD REPORT METHODS
    fun getInwardReport(filter: ReportFilter): Flow<Result<InwardReport>> = flow {
        try {
            Log.d("ReportRepo", "Fetching inward report with filter: $filter")

            val dateFilter = "gte.${filter.startDate}"

            val response = reportApiService.getInwardReportByDateRange(
                dateRange = dateFilter,
                select = "*,items(name,unit_of_measure,categories(name)),cuisines(name)",
                order = "purchase_date.desc"
            )

            if (response.isSuccessful) {
                val rawData = response.body() ?: emptyList()
                val items = parseInwardReportItems(rawData)

                val report = InwardReport(
                    reportDate = getCurrentDateTime(),
                    filter = filter,
                    totalTransactions = items.size,
                    totalQuantity = items.sumOf { it.inwardQuantity },
                    totalValue = items.sumOf { it.totalValue }.toBigDecimal(),
                    items = items
                )

                Log.d("ReportRepo", "Successfully fetched inward report: ${items.size} items")
                emit(Result.success(report))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReportRepo", "Error fetching inward report: $errorBody")
                emit(Result.failure(Exception("Failed to fetch inward report: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("ReportRepo", "Exception fetching inward report", e)
            emit(Result.failure(e))
        }
    }

    fun getInwardSummary(filter: ReportFilter): Flow<Result<InwardSummary>> = flow {
        try {
            Log.d("ReportRepo", "Fetching inward summary with filter: $filter")

            val response = reportApiService.getInwardSummary(filter.startDate, filter.endDate)

            if (response.isSuccessful) {
                val summary = response.body()
                if (summary != null) {
                    Log.d("ReportRepo", "Successfully fetched inward summary")
                    emit(Result.success(summary))
                } else {
                    emit(Result.failure(Exception("No summary data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReportRepo", "Error fetching inward summary: $errorBody")
                emit(Result.failure(Exception("Failed to fetch inward summary: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("ReportRepo", "Exception fetching inward summary", e)
            emit(Result.failure(e))
        }
    }

    // OUTWARD REPORT METHODS WITH COST CALCULATION AND FILTERS
    fun getOutwardReport(
        filter: ReportFilter,
        cuisineId: String? = null,
        categoryName: String? = null,
        purposeName: String? = null
    ): Flow<Result<OutwardReport>> = flow {
        try {
            Log.d("ReportRepo", "Fetching outward report with filters - cuisine: $cuisineId, category: $categoryName, purpose: $purposeName")

            val dateFilter = "gte.${filter.startDate}"
            val cuisineFilter = if (cuisineId != null) "eq.$cuisineId" else null

            val response = reportApiService.getOutwardReportByDateRange(
                dateRange = dateFilter,
                cuisineId = cuisineFilter,
                select = "*,items(id,name,unit_of_measure,categories(name)),cuisines(id,name),indents(chef_id,users:chef_id(full_name))",
                order = "usage_date.desc"
            )

            if (response.isSuccessful) {
                val rawData = response.body() ?: emptyList()
                var items = parseOutwardReportItemsWithCosts(rawData)

                // Client-side filtering for category and purpose
                if (!categoryName.isNullOrBlank()) {
                    items = items.filter { it.categoryName.equals(categoryName, ignoreCase = true) }
                    Log.d("ReportRepo", "After category filter: ${items.size} items")
                }

                if (!purposeName.isNullOrBlank()) {
                    items = items.filter { it.purpose.equals(purposeName, ignoreCase = true) }
                    Log.d("ReportRepo", "After purpose filter: ${items.size} items")
                }

                val report = OutwardReport(
                    reportDate = getCurrentDateTime(),
                    filter = filter,
                    totalTransactions = items.size,
                    totalQuantity = items.sumOf { it.outwardQuantity },
                    totalValue = items.sumOf { it.calculatedTotalCost }.toBigDecimal(),
                    items = items
                )

                Log.d("ReportRepo", "Successfully fetched outward report with costs: ${items.size} items")
                emit(Result.success(report))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReportRepo", "Error fetching outward report: $errorBody")
                emit(Result.failure(Exception("Failed to fetch outward report: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("ReportRepo", "Exception fetching outward report", e)
            emit(Result.failure(e))
        }
    }

    fun getOutwardSummary(filter: ReportFilter): Flow<Result<OutwardSummary>> = flow {
        try {
            Log.d("ReportRepo", "Fetching outward summary with filter: $filter")

            val response = reportApiService.getOutwardSummary(filter.startDate, filter.endDate)

            if (response.isSuccessful) {
                val summary = response.body()
                if (summary != null) {
                    Log.d("ReportRepo", "Successfully fetched outward summary")
                    emit(Result.success(summary))
                } else {
                    emit(Result.failure(Exception("No summary data returned")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReportRepo", "Error fetching outward summary: $errorBody")
                emit(Result.failure(Exception("Failed to fetch outward summary: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("ReportRepo", "Exception fetching outward summary", e)
            emit(Result.failure(e))
        }
    }

    // CUISINE-WISE REPORT METHODS
    fun getCuisineWiseReport(filter: ReportFilter, cuisineId: String?): Flow<Result<CuisineWiseReport>> = flow {
        try {
            Log.d("ReportRepo", "Fetching cuisine-wise report for cuisine ID: ${cuisineId ?: "All"}")

            val dateFilter = "gte.${filter.startDate}"
            val cuisineFilter = if (cuisineId != null) "eq.$cuisineId" else null

            Log.d("ReportRepo", "API call with dateFilter: $dateFilter, cuisineFilter: $cuisineFilter")

            val response = reportApiService.getCuisineWiseReportByDateRange(
                dateRange = dateFilter,
                cuisineId = cuisineFilter,
                select = "*,items(id,name,unit_of_measure,categories(name)),cuisines(id,name),users(full_name)",
                order = "usage_date.desc"
            )

            if (response.isSuccessful) {
                val rawData = response.body() ?: emptyList()
                Log.d("ReportRepo", "Received ${rawData.size} raw items from API")

                // Parse all items first
                val allItems = parseOutwardReportItemsWithCosts(rawData)
                Log.d("ReportRepo", "Parsed ${allItems.size} items")

                // Client-side filtering by cuisine if needed (as fallback if API filter didn't work)
                val filteredItems = if (cuisineId != null) {
                    allItems.filter { item ->
                        // Extract cuisine ID from the cuisines object in raw data
                        val itemRawData = rawData.find { it["id"] == item.id }
                        val cuisines = itemRawData?.get("cuisines") as? Map<String, Any>
                        val itemCuisineId = cuisines?.get("id")?.toString()
                        Log.d("ReportRepo", "Item ${item.itemName}: cuisineId=$itemCuisineId, looking for=$cuisineId")
                        itemCuisineId == cuisineId
                    }
                } else {
                    allItems
                }

                Log.d("ReportRepo", "After cuisine filtering: ${filteredItems.size} items")

                // Calculate cuisine breakdown (from filtered items)
                val cuisineBreakdown = calculateCuisineBreakdown(filteredItems)

                val totalValue = filteredItems.sumOf { it.calculatedTotalCost }.toBigDecimal()
                val cuisineName = if (cuisineId == null) {
                    null  // All cuisines
                } else {
                    filteredItems.firstOrNull()?.cuisineName  // Get cuisine name from first item
                }

                val report = CuisineWiseReport(
                    reportDate = getCurrentDateTime(),
                    filter = filter,
                    cuisineName = cuisineName,
                    totalTransactions = filteredItems.size,
                    totalQuantity = filteredItems.sumOf { it.outwardQuantity },
                    totalValue = totalValue,
                    items = filteredItems,
                    cuisineBreakdown = cuisineBreakdown
                )

                Log.d("ReportRepo", "Successfully created cuisine-wise report: ${filteredItems.size} items, ${cuisineBreakdown.size} cuisines, total=$totalValue")
                emit(Result.success(report))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReportRepo", "Error fetching cuisine-wise report: $errorBody")
                emit(Result.failure(Exception("Failed to fetch cuisine-wise report: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e("ReportRepo", "Exception fetching cuisine-wise report", e)
            emit(Result.failure(e))
        }
    }

    private fun calculateCuisineBreakdown(items: List<OutwardReportItem>): List<CuisineBreakdownItem> {
        val totalCost = items.sumOf { it.calculatedTotalCost }

        return items
            .filter { !it.cuisineName.isNullOrBlank() }
            .groupBy { it.cuisineName!! }
            .map { (cuisineName, cuisineItems) ->
                val cuisineTotalCost = cuisineItems.sumOf { it.calculatedTotalCost }
                CuisineBreakdownItem(
                    cuisineName = cuisineName,
                    totalTransactions = cuisineItems.size,
                    totalQuantity = cuisineItems.sumOf { it.outwardQuantity },
                    totalCost = cuisineTotalCost,
                    percentageOfTotal = if (totalCost > 0) (cuisineTotalCost / totalCost) * 100 else 0.0
                )
            }
            .sortedByDescending { it.totalCost }
    }

    // PRIVATE HELPER METHODS
    private fun parseInwardReportItems(rawData: List<Map<String, Any>>): List<InwardReportItem> {
        return rawData.mapNotNull { data ->
            try {
                val items = data["items"] as? Map<String, Any>
                val categories = (items?.get("categories") as? Map<String, Any>)
                val cuisines = data["cuisines"] as? Map<String, Any>

                InwardReportItem(
                    id = data["id"]?.toString() ?: "",
                    itemName = items?.get("name")?.toString() ?: "Unknown Item",
                    categoryName = categories?.get("name")?.toString(),
                    vendorName = data["vendor_name"]?.toString() ?: "Unknown Vendor",
                    purchaseDate = data["purchase_date"]?.toString() ?: "",
                    inwardQuantity = (data["inward_quantity"] as? Number)?.toDouble() ?: 0.0,
                    unitOfMeasure = items?.get("unit_of_measure")?.toString() ?: "",
                    pricePerUnit = (data["price_per_unit"] as? Number)?.toDouble() ?: 0.0,
                    totalValue = ((data["inward_quantity"] as? Number)?.toDouble() ?: 0.0) *
                            ((data["price_per_unit"] as? Number)?.toDouble() ?: 0.0),
                    priceWithGst = (data["price_with_gst"] as? Number)?.toDouble(),
                    gstPercentage = (data["gst_percentage"] as? Number)?.toDouble(),
                    billNumber = data["bill_number"]?.toString(),
                    expiryDate = data["expiry_date"]?.toString(),
                    cuisineName = cuisines?.get("name")?.toString()
                )
            } catch (e: Exception) {
                Log.e("ReportRepo", "Error parsing inward item: ${e.message}")
                null
            }
        }
    }

    private suspend fun parseOutwardReportItemsWithCosts(rawData: List<Map<String, Any>>): List<OutwardReportItem> {
        return rawData.mapNotNull { data ->
            try {
                val items = data["items"] as? Map<String, Any>
                val categories = (items?.get("categories") as? Map<String, Any>)
                val cuisines = data["cuisines"] as? Map<String, Any>
                val indents = data["indents"] as? Map<String, Any>
                val users = indents?.get("users") as? Map<String, Any>
                val itemId = items?.get("id")?.toString() ?: ""
                val usageDate = data["usage_date"]?.toString() ?: ""
                val outwardQuantity = (data["outward_quantity"] as? Number)?.toDouble() ?: 0.0

                // Calculate moving average cost
                val costData = calculateMovingAverageCost(itemId, usageDate)

                OutwardReportItem(
                    id = data["id"]?.toString() ?: "",
                    itemId = itemId,
                    itemName = items?.get("name")?.toString() ?: "Unknown Item",
                    categoryName = categories?.get("name")?.toString(),
                    outwardQuantity = outwardQuantity,
                    unitOfMeasure = items?.get("unit_of_measure")?.toString() ?: "",
                    usageDate = usageDate,
                    cuisineName = cuisines?.get("name")?.toString(),
                    purpose = data["purpose"]?.toString(),
                    estimatedValue = (data["estimated_value"] as? Number)?.toDouble(),
                    sourceType = data["source_type"]?.toString(),
                    indentId = data["indent_id"]?.toString(),
                    notes = data["notes"]?.toString(),
                    createdBy = data["created_by"]?.toString(),
                    chefName = users?.get("full_name")?.toString(),
                    calculatedCostPerUnit = costData.first,
                    calculatedTotalCost = costData.first * outwardQuantity,
                    costCalculationMethod = costData.second
                )
            } catch (e: Exception) {
                Log.e("ReportRepo", "Error parsing outward item: ${e.message}")
                null
            }
        }
    }

    private suspend fun calculateMovingAverageCost(itemId: String, usageDate: String): Pair<Double, String> {
        return try {
            Log.d("ReportRepo", "Calculating moving average cost for item: $itemId on date: $usageDate")

            // Parse usage date and calculate date ranges for 15/30/60 days back
            val usageDateParsed = LocalDate.parse(usageDate)
            val date15DaysAgo = usageDateParsed.minusDays(15).toString()
            val date30DaysAgo = usageDateParsed.minusDays(30).toString()
            val date60DaysAgo = usageDateParsed.minusDays(60).toString()

            // Try 15-day average first
            var costData = getWeightedAverageCost(itemId, date15DaysAgo)
            if (costData.first > 0.0) {
                Log.d("ReportRepo", "Using 15-day average: ${costData.first}")
                return Pair(costData.first, "15-day avg")
            }

            // Try 30-day average if 15-day failed
            costData = getWeightedAverageCost(itemId, date30DaysAgo)
            if (costData.first > 0.0) {
                Log.d("ReportRepo", "Using 30-day average: ${costData.first}")
                return Pair(costData.first, "30-day avg")
            }

            // Try 60-day average if 30-day failed
            costData = getWeightedAverageCost(itemId, date60DaysAgo)
            if (costData.first > 0.0) {
                Log.d("ReportRepo", "Using 60-day average: ${costData.first}")
                return Pair(costData.first, "60-day avg")
            }

            // No purchase data found
            Log.w("ReportRepo", "No purchase data found for item: $itemId in last 60 days")
            Pair(0.0, "No Purchase Data")

        } catch (e: Exception) {
            Log.e("ReportRepo", "Exception calculating moving average cost for item: $itemId", e)
            Pair(0.0, "Error")
        }
    }

    private suspend fun getWeightedAverageCost(itemId: String, startDate: String): Pair<Double, Int> {
        return try {
            val dateFilter = "gte.$startDate"

            val response = reportApiService.getInwardItemsForCostCalculation(
                itemId = "eq.$itemId",
                dateRange = dateFilter
            )

            if (response.isSuccessful) {
                val purchaseData = response.body() ?: emptyList()

                if (purchaseData.isNotEmpty()) {
                    // Calculate weighted average based on quantities
                    var totalWeightedCost = 0.0
                    var totalQuantity = 0.0

                    purchaseData.forEach { purchase ->
                        val pricePerUnit = (purchase["price_per_unit"] as? Number)?.toDouble() ?: 0.0
                        val quantity = (purchase["inward_quantity"] as? Number)?.toDouble() ?: 0.0

                        totalWeightedCost += (pricePerUnit * quantity)
                        totalQuantity += quantity
                    }

                    val averageCost = if (totalQuantity > 0) totalWeightedCost / totalQuantity else 0.0

                    Log.d("ReportRepo", "Calculated weighted average for item $itemId: $averageCost from ${purchaseData.size} purchases")
                    Pair(averageCost, purchaseData.size)
                } else {
                    Pair(0.0, 0)
                }
            } else {
                Log.e("ReportRepo", "Failed to fetch purchase data for cost calculation: ${response.code()}")
                Pair(0.0, 0)
            }
        } catch (e: Exception) {
            Log.e("ReportRepo", "Exception in getWeightedAverageCost", e)
            Pair(0.0, 0)
        }
    }

    private fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}