package com.attil.inventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.content.Context
import android.widget.Toast
import android.os.Environment
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.attil.inventory.data.model.reports.*
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.management.Category
import com.attil.inventory.data.model.management.Usage
import com.attil.inventory.data.repository.ReportRepository
import com.attil.inventory.data.repository.CuisineRepository
import com.attil.inventory.data.repository.CategoryRepository
import com.attil.inventory.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val cuisineRepository: CuisineRepository,
    private val categoryRepository: CategoryRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    // Common state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Current report type
    private val _currentReportType = MutableStateFlow(ReportType.INWARD)
    val currentReportType: StateFlow<ReportType> = _currentReportType.asStateFlow()

    // Master data for filtering
    private val _cuisines = MutableStateFlow<List<Cuisine>>(emptyList())
    val cuisines: StateFlow<List<Cuisine>> = _cuisines.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _usages = MutableStateFlow<List<Usage>>(emptyList())
    val usages: StateFlow<List<Usage>> = _usages.asStateFlow()

    // Outward Report Filter Type
    enum class OutwardFilterType {
        CUISINE, CATEGORY, PURPOSE
    }

    private val _outwardFilterType = MutableStateFlow<OutwardFilterType?>(null)
    val outwardFilterType: StateFlow<OutwardFilterType?> = _outwardFilterType.asStateFlow()

    private val _outwardFilterValue = MutableStateFlow<String?>(null)
    val outwardFilterValue: StateFlow<String?> = _outwardFilterValue.asStateFlow()

    // Date filter state
    private val _startDate = MutableStateFlow(getDefaultStartDate())
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(getDefaultEndDate())
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    // Outward Report Filters
    private val _selectedCuisineId = MutableStateFlow<String?>(null)
    val selectedCuisineId: StateFlow<String?> = _selectedCuisineId.asStateFlow()

    private val _selectedCategoryName = MutableStateFlow<String?>(null)
    val selectedCategoryName: StateFlow<String?> = _selectedCategoryName.asStateFlow()

    private val _selectedUsageName = MutableStateFlow<String?>(null)
    val selectedUsageName: StateFlow<String?> = _selectedUsageName.asStateFlow()

    // INWARD REPORT STATE
    private val _inwardReport = MutableStateFlow<InwardReport?>(null)
    val inwardReport: StateFlow<InwardReport?> = _inwardReport.asStateFlow()

    private val _inwardSummary = MutableStateFlow<InwardSummary?>(null)
    val inwardSummary: StateFlow<InwardSummary?> = _inwardSummary.asStateFlow()

    // OUTWARD REPORT STATE
    private val _outwardReport = MutableStateFlow<OutwardReport?>(null)
    val outwardReport: StateFlow<OutwardReport?> = _outwardReport.asStateFlow()

    private val _outwardSummary = MutableStateFlow<OutwardSummary?>(null)
    val outwardSummary: StateFlow<OutwardSummary?> = _outwardSummary.asStateFlow()

    init {
        // Load default report on initialization
        loadInwardReport()
        loadCuisines()
        loadCategories()
        loadUsages()
    }

    private fun loadCuisines() {
        viewModelScope.launch {
            try {
                cuisineRepository.getAllCuisines().collect { result ->
                    result.fold(
                        onSuccess = { cuisineList ->
                            _cuisines.value = cuisineList
                            Log.d("ReportViewModel", "Successfully loaded ${cuisineList.size} cuisines")
                        },
                        onFailure = { error ->
                            Log.e("ReportViewModel", "Error loading cuisines: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Exception loading cuisines", e)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                categoryRepository.getAllCategories().collect { result ->
                    result.fold(
                        onSuccess = { categoryList ->
                            _categories.value = categoryList
                            Log.d("ReportViewModel", "Successfully loaded ${categoryList.size} categories")
                        },
                        onFailure = { error ->
                            Log.e("ReportViewModel", "Error loading categories: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Exception loading categories", e)
            }
        }
    }

    private fun loadUsages() {
        viewModelScope.launch {
            try {
                usageRepository.getAllUsages().collect { result ->
                    result.fold(
                        onSuccess = { usageList ->
                            _usages.value = usageList.filter { it.isActive }
                            Log.d("ReportViewModel", "Successfully loaded ${usageList.size} usages")
                        },
                        onFailure = { error ->
                            Log.e("ReportViewModel", "Error loading usages: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Exception loading usages", e)
            }
        }
    }

    // REPORT TYPE MANAGEMENT
    fun setReportType(reportType: ReportType) {
        _currentReportType.value = reportType
        when (reportType) {
            ReportType.INWARD -> loadInwardReport()
            ReportType.OUTWARD -> loadOutwardReport()
            ReportType.INDENT -> {} // Not handled by this ViewModel - use IndentViewModel
        }
    }

    // OUTWARD FILTER MANAGEMENT
    fun setSelectedCuisineId(cuisineId: String?) {
        _selectedCuisineId.value = cuisineId
        loadOutwardReport()
    }

    fun setSelectedCategoryName(categoryName: String?) {
        _selectedCategoryName.value = categoryName
        loadOutwardReport()
    }

    fun setSelectedUsageName(usageName: String?) {
        _selectedUsageName.value = usageName
        loadOutwardReport()
    }

    // New filter type methods
    fun setOutwardFilterType(filterType: OutwardFilterType?) {
        _outwardFilterType.value = filterType
        _outwardFilterValue.value = null // Reset filter value when type changes

        // Clear all filters
        _selectedCuisineId.value = null
        _selectedCategoryName.value = null
        _selectedUsageName.value = null

        loadOutwardReport()
    }

    fun setOutwardFilterValue(value: String?) {
        _outwardFilterValue.value = value

        // Set the appropriate filter based on filter type
        when (_outwardFilterType.value) {
            OutwardFilterType.CUISINE -> _selectedCuisineId.value = value
            OutwardFilterType.CATEGORY -> _selectedCategoryName.value = value
            OutwardFilterType.PURPOSE -> _selectedUsageName.value = value
            null -> {
                // Clear all filters
                _selectedCuisineId.value = null
                _selectedCategoryName.value = null
                _selectedUsageName.value = null
            }
        }

        loadOutwardReport()
    }

    // DATE MANAGEMENT
    fun setStartDate(date: String) {
        _startDate.value = date
        refreshCurrentReport()
    }

    fun setEndDate(date: String) {
        _endDate.value = date
        refreshCurrentReport()
    }

    fun setDateRange(startDate: String, endDate: String) {
        _startDate.value = startDate
        _endDate.value = endDate
        refreshCurrentReport()
    }

    fun refreshCurrentReport() {
        when (_currentReportType.value) {
            ReportType.INWARD -> loadInwardReport()
            ReportType.OUTWARD -> loadOutwardReport()
            ReportType.INDENT -> {} // Not handled by this ViewModel - use IndentViewModel
        }
    }

    // INWARD REPORT METHODS
    fun loadInwardReport() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val filter = ReportFilter(
                startDate = _startDate.value,
                endDate = _endDate.value
            )

            try {
                reportRepository.getInwardReport(filter).collect { result ->
                    result.fold(
                        onSuccess = { report ->
                            _inwardReport.value = report
                            _isLoading.value = false
                            Log.d("ReportViewModel", "Inward report loaded successfully: ${report.items.size} items")
                        },
                        onFailure = { error ->
                            _errorMessage.value = error.message ?: "Failed to load inward report"
                            _isLoading.value = false
                            Log.e("ReportViewModel", "Error loading inward report: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unexpected error occurred"
                _isLoading.value = false
                Log.e("ReportViewModel", "Exception loading inward report", e)
            }
        }
    }

    fun loadInwardSummary() {
        viewModelScope.launch {
            val filter = ReportFilter(
                startDate = _startDate.value,
                endDate = _endDate.value
            )

            try {
                reportRepository.getInwardSummary(filter).collect { result ->
                    result.fold(
                        onSuccess = { summary ->
                            _inwardSummary.value = summary
                            Log.d("ReportViewModel", "Inward summary loaded successfully")
                        },
                        onFailure = { error ->
                            Log.e("ReportViewModel", "Error loading inward summary: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Exception loading inward summary", e)
            }
        }
    }

    // OUTWARD REPORT METHODS WITH FILTERS
    fun loadOutwardReport() {
        viewModelScope.launch {
            Log.d("ReportViewModel", "=== Starting loadOutwardReport ===")
            Log.d("ReportViewModel", "Start Date: ${_startDate.value}")
            Log.d("ReportViewModel", "End Date: ${_endDate.value}")
            Log.d("ReportViewModel", "Cuisine Filter: ${_selectedCuisineId.value}")
            Log.d("ReportViewModel", "Category Filter: ${_selectedCategoryName.value}")
            Log.d("ReportViewModel", "Purpose Filter: ${_selectedUsageName.value}")

            _isLoading.value = true
            _errorMessage.value = null

            val filter = ReportFilter(
                startDate = _startDate.value,
                endDate = _endDate.value
            )

            try {
                reportRepository.getOutwardReport(
                    filter = filter,
                    cuisineId = _selectedCuisineId.value,
                    categoryName = _selectedCategoryName.value,
                    purposeName = _selectedUsageName.value
                ).collect { result ->
                    result.fold(
                        onSuccess = { report ->
                            _outwardReport.value = report
                            _isLoading.value = false
                            Log.d("ReportViewModel", "✅ Outward report loaded successfully: ${report.items.size} items (filters: cuisine=${_selectedCuisineId.value}, category=${_selectedCategoryName.value}, purpose=${_selectedUsageName.value})")
                        },
                        onFailure = { error ->
                            _errorMessage.value = error.message ?: "Failed to load outward report"
                            _isLoading.value = false
                            Log.e("ReportViewModel", "❌ Error loading outward report: ${error.message}")
                            Log.e("ReportViewModel", "Error stack trace:", error)
                        }
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unexpected error occurred"
                _isLoading.value = false
                Log.e("ReportViewModel", "❌ Exception loading outward report: ${e.message}")
                Log.e("ReportViewModel", "Exception stack trace:", e)
            }
        }
    }

    fun loadOutwardSummary() {
        viewModelScope.launch {
            val filter = ReportFilter(
                startDate = _startDate.value,
                endDate = _endDate.value
            )

            try {
                reportRepository.getOutwardSummary(filter).collect { result ->
                    result.fold(
                        onSuccess = { summary ->
                            _outwardSummary.value = summary
                            Log.d("ReportViewModel", "Outward summary loaded successfully")
                        },
                        onFailure = { error ->
                            Log.e("ReportViewModel", "Error loading outward summary: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Exception loading outward summary", e)
            }
        }
    }

    // EXPORT METHODS WITH DOWNLOADS FOLDER
    fun exportInwardReportToPdf(context: Context, report: InwardReport) {
        viewModelScope.launch {
            try {
                Log.d("ReportViewModel", "Starting PDF export for inward report")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Creating PDF...", Toast.LENGTH_SHORT).show()
                }

                // Load summary data first
                loadInwardSummary()
                // Wait a moment for summary to load
                kotlinx.coroutines.delay(500)
                val summary = _inwardSummary.value

                val fileName = "Inward_Report_${report.filter.startDate}_to_${report.filter.endDate}.pdf"
                val file = createInwardPdfReport(context, report, summary, fileName)

                withContext(Dispatchers.Main) {
                    if (file != null) {
                        Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        openPdfFile(context, file)
                    } else {
                        Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error exporting PDF", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun exportInwardReportToCsv(context: Context, report: InwardReport) {
        viewModelScope.launch {
            try {
                Log.d("ReportViewModel", "Starting CSV export for inward report")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Creating CSV...", Toast.LENGTH_SHORT).show()
                }

                val fileName = "Inward_Report_${report.filter.startDate}_to_${report.filter.endDate}.csv"
                val file = createInwardCsvReport(context, report, fileName)

                withContext(Dispatchers.Main) {
                    if (file != null) {
                        Toast.makeText(context, "CSV saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        openCsvFile(context, file)
                    } else {
                        Toast.makeText(context, "Failed to create CSV", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error exporting CSV", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun exportOutwardReportToPdf(context: Context, report: OutwardReport) {
        viewModelScope.launch {
            try {
                Log.d("ReportViewModel", "Starting PDF export for outward report")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Creating PDF...", Toast.LENGTH_SHORT).show()
                }

                // Load summary data first
                loadOutwardSummary()
                // Wait a moment for summary to load
                kotlinx.coroutines.delay(500)
                val summary = _outwardSummary.value

                val fileName = "Outward_Report_${report.filter.startDate}_to_${report.filter.endDate}.pdf"
                val file = createOutwardPdfReport(context, report, summary, fileName)

                withContext(Dispatchers.Main) {
                    if (file != null) {
                        Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        openPdfFile(context, file)
                    } else {
                        Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error exporting PDF", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun exportOutwardReportToCsv(context: Context, report: OutwardReport) {
        viewModelScope.launch {
            try {
                Log.d("ReportViewModel", "Starting CSV export for outward report")

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Creating CSV...", Toast.LENGTH_SHORT).show()
                }

                val fileName = "Outward_Report_${report.filter.startDate}_to_${report.filter.endDate}.csv"
                val file = createOutwardCsvReport(context, report, fileName)

                withContext(Dispatchers.Main) {
                    if (file != null) {
                        Toast.makeText(context, "CSV saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        openCsvFile(context, file)
                    } else {
                        Toast.makeText(context, "Failed to create CSV", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error exporting CSV", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    // PRIVATE HELPER METHODS
    private fun getDefaultStartDate(): String {
        return LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    private fun getDefaultEndDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    // COMPREHENSIVE AUTO-FIT PDF CREATION WITH ENHANCED SUMMARY
    private suspend fun createInwardPdfReport(context: Context, report: InwardReport, summary: InwardSummary?, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ReportViewModel", "Creating comprehensive inward PDF with ${report.items.size} items")

                // Save to Downloads folder on all Android versions
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)

                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 Landscape
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()

                // Page margins
                val leftMargin = 25f
                val rightMargin = 817f
                val topMargin = 25f
                val availableWidth = rightMargin - leftMargin

                var yPosition = topMargin

                // Title
                paint.textSize = 18f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.color = android.graphics.Color.rgb(34, 45, 50)
                canvas.drawText("INWARD REPORT", leftMargin, yPosition, paint)
                paint.color = android.graphics.Color.BLACK
                yPosition += 28f

                // Date range and generation info
                paint.textSize = 10f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Period: ${report.filter.startDate} to ${report.filter.endDate}", leftMargin, yPosition, paint)
                canvas.drawText("Generated: ${getCurrentDateTime()}", rightMargin - 150f, yPosition, paint)
                yPosition += 22f

                // COMPREHENSIVE SUMMARY BOX
                paint.textSize = 9f
                val summaryHeight = 110f
                paint.color = android.graphics.Color.rgb(245, 245, 245)
                canvas.drawRect(leftMargin, yPosition, rightMargin, yPosition + summaryHeight, paint)
                paint.color = android.graphics.Color.BLACK
                paint.strokeWidth = 1f
                canvas.drawRect(leftMargin, yPosition, rightMargin, yPosition + summaryHeight, paint)

                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textSize = 11f
                canvas.drawText("COMPREHENSIVE SUMMARY", leftMargin + 10f, yPosition + 15f, paint)

                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8f

                // Calculate comprehensive statistics - FIXED sumOf calls
                val uniqueVendors = report.items.map { it.vendorName }.distinct().size
                val uniqueCategories = report.items.mapNotNull { it.categoryName }.distinct().size
                val uniqueItems = report.items.map { it.itemName }.distinct().size
                val avgTransactionValue = if(report.totalTransactions > 0) report.totalValue.toDouble() / report.totalTransactions else 0.0
                val avgQuantityPerTransaction = if(report.totalTransactions > 0) report.totalQuantity / report.totalTransactions else 0.0
                val avgCostPerUnit = if(report.totalQuantity > 0) report.totalValue.toDouble() / report.totalQuantity else 0.0
                val totalWithGst = report.items.sumOf { (it.priceWithGst ?: it.totalValue) }
                val reportPeriod = calculateDateDifference(report.filter.startDate, report.filter.endDate)
                val topVendor = report.items.groupBy { it.vendorName }.maxByOrNull { it.value.sumOf { item -> item.totalValue } }?.key ?: "N/A"
                val topCategory = report.items.groupBy { it.categoryName }.maxByOrNull { it.value.sumOf { item -> item.totalValue } }?.key ?: "N/A"

                // Summary Row 1
                canvas.drawText("Total Transactions: ${report.totalTransactions}", leftMargin + 10f, yPosition + 28f, paint)
                canvas.drawText("Unique Items: $uniqueItems", leftMargin + 170f, yPosition + 28f, paint)
                canvas.drawText("Unique Vendors: $uniqueVendors", leftMargin + 300f, yPosition + 28f, paint)
                canvas.drawText("Unique Categories: $uniqueCategories", leftMargin + 450f, yPosition + 28f, paint)
                canvas.drawText("Report Days: $reportPeriod", leftMargin + 600f, yPosition + 28f, paint)

                // Summary Row 2
                canvas.drawText("Total Quantity: ${String.format("%.2f", report.totalQuantity)} units", leftMargin + 10f, yPosition + 42f, paint)
                canvas.drawText("Avg Qty/Transaction: ${String.format("%.2f", avgQuantityPerTransaction)}", leftMargin + 170f, yPosition + 42f, paint)
                canvas.drawText("Avg Cost/Unit: ₹${String.format("%.2f", avgCostPerUnit)}", leftMargin + 320f, yPosition + 42f, paint)
                canvas.drawText("Daily Transactions: ${String.format("%.1f", if(reportPeriod > 0) report.totalTransactions.toDouble() / reportPeriod else 0.0)}", leftMargin + 480f, yPosition + 42f, paint)

                // Summary Row 3
                canvas.drawText("Total Value (Ex-GST): ₹${String.format("%,.2f", report.totalValue.toDouble())}", leftMargin + 10f, yPosition + 56f, paint)
                canvas.drawText("Total Value (Inc-GST): ₹${String.format("%,.2f", totalWithGst)}", leftMargin + 200f, yPosition + 56f, paint)
                canvas.drawText("Avg Transaction Value: ₹${String.format("%.2f", avgTransactionValue)}", leftMargin + 400f, yPosition + 56f, paint)

                // Summary Row 4
                canvas.drawText("Top Vendor (by Value): ${autoFitText(paint, topVendor, 140f)}", leftMargin + 10f, yPosition + 70f, paint)
                canvas.drawText("Top Category (by Value): ${autoFitText(paint, topCategory, 140f)}", leftMargin + 250f, yPosition + 70f, paint)
                canvas.drawText("Daily Avg Value: ₹${String.format("%.0f", if(reportPeriod > 0) report.totalValue.toDouble() / reportPeriod else 0.0)}", leftMargin + 450f, yPosition + 70f, paint)

                // Summary Row 5 - GST Analysis
                val itemsWithGst = report.items.count { it.gstPercentage != null && it.gstPercentage!! > 0 }
                val totalGstAmount = report.items.sumOf { ((it.priceWithGst ?: it.totalValue) - it.totalValue) }
                canvas.drawText("Items with GST: $itemsWithGst/${report.items.size}", leftMargin + 10f, yPosition + 84f, paint)
                canvas.drawText("Total GST Amount: ₹${String.format("%.2f", totalGstAmount)}", leftMargin + 170f, yPosition + 84f, paint)
                canvas.drawText("Avg GST%: ${String.format("%.1f", report.items.filter { it.gstPercentage != null }.map { it.gstPercentage!! }.average().takeIf { !it.isNaN() } ?: 0.0)}%", leftMargin + 320f, yPosition + 84f, paint)

                // Summary Row 6 - Additional Insights
                val maxOrderValue = report.items.maxOfOrNull { it.totalValue } ?: 0.0
                val minOrderValue = report.items.filter { it.totalValue > 0 }.minOfOrNull { it.totalValue } ?: 0.0
                canvas.drawText("Largest Order: ₹${String.format("%.0f", maxOrderValue)}", leftMargin + 10f, yPosition + 98f, paint)
                canvas.drawText("Smallest Order: ₹${String.format("%.0f", minOrderValue)}", leftMargin + 170f, yPosition + 98f, paint)
                canvas.drawText("Value Range: ₹${String.format("%.0f", maxOrderValue - minOrderValue)}", leftMargin + 320f, yPosition + 98f, paint)

                yPosition += summaryHeight + 8f

                // API SUMMARY SECTION
                if (summary != null) {
                    paint.textSize = 10f
                    paint.typeface = Typeface.DEFAULT_BOLD
                    canvas.drawText("API SUMMARY INSIGHTS", leftMargin, yPosition, paint)
                    yPosition += 15f

                    paint.textSize = 8f
                    paint.typeface = Typeface.DEFAULT
                    canvas.drawText("Top Vendor: ${summary.topVendor ?: "N/A"}", leftMargin, yPosition, paint)
                    canvas.drawText("Top Category: ${summary.topCategory ?: "N/A"}", leftMargin + 200f, yPosition, paint)
                    canvas.drawText("Avg Order Value: ₹${summary.averageOrderValue}", leftMargin + 400f, yPosition, paint)
                    yPosition += 15f
                } else {
                    yPosition += 10f
                }

                // Auto-fit table setup
                val rowHeight = 18f
                val headerHeight = 22f

                // Optimized column definitions with auto-width calculation
                val totalTableWidth = availableWidth - 10f
                val columns = arrayOf(
                    Pair("No", 30f),
                    Pair("Item Name", 130f),
                    Pair("Category", 70f),
                    Pair("Vendor", 90f),
                    Pair("Date", 65f),
                    Pair("Qty", 40f),
                    Pair("Unit", 45f),
                    Pair("Rate", 50f),
                    Pair("GST%", 40f),
                    Pair("Total", 60f)
                )

                // Auto-adjust column widths to fit available space
                val totalDefinedWidth = columns.map { it.second }.sum()
                val scaleFactor = if (totalDefinedWidth > totalTableWidth) totalTableWidth / totalDefinedWidth else 1.0f
                val adjustedColumns = columns.map { Pair(it.first, it.second * scaleFactor) }.toTypedArray()

                // Calculate column positions
                val columnPositions = mutableListOf<Float>()
                var currentX = leftMargin + 5f
                columnPositions.add(currentX)

                adjustedColumns.forEach { (_, width) ->
                    currentX += width
                    columnPositions.add(currentX)
                }

                // Draw table headers
                paint.textSize = 7f
                paint.typeface = Typeface.DEFAULT_BOLD

                // Header background
                paint.color = android.graphics.Color.rgb(200, 200, 200)
                canvas.drawRect(columnPositions.first(), yPosition, columnPositions.last(), yPosition + headerHeight, paint)

                paint.color = android.graphics.Color.BLACK

                // Header text with center alignment
                adjustedColumns.forEachIndexed { index, (header, width) ->
                    val textX = columnPositions[index] + (width / 2) - (paint.measureText(header) / 2)
                    canvas.drawText(header, textX, yPosition + 14f, paint)
                }

                // Draw header borders
                drawEnhancedTableBorders(canvas, paint, columnPositions, yPosition, headerHeight)

                yPosition += headerHeight

                // Table data
                paint.textSize = 6.5f
                paint.typeface = Typeface.DEFAULT
                paint.color = android.graphics.Color.BLACK

                report.items.forEachIndexed { index, item ->
                    if (yPosition + rowHeight > 570f) {
                        // Add "...continued" indicator
                        paint.typeface = Typeface.DEFAULT_BOLD
                        canvas.drawText("... ${report.items.size - index} more items (continued in full CSV export)", leftMargin + 5f, yPosition + 15f, paint)
                        return@forEachIndexed
                    }

                    // Row data
                    val rowData = arrayOf(
                        (index + 1).toString(),
                        autoFitText(paint, item.itemName, adjustedColumns[1].second - 8f),
                        autoFitText(paint, item.categoryName ?: "", adjustedColumns[2].second - 8f),
                        autoFitText(paint, item.vendorName, adjustedColumns[3].second - 8f),
                        autoFitText(paint, item.purchaseDate, adjustedColumns[4].second - 8f),
                        String.format("%.1f", item.inwardQuantity),
                        autoFitText(paint, item.unitOfMeasure, adjustedColumns[6].second - 8f),
                        String.format("%.0f", item.pricePerUnit),
                        if (item.gstPercentage != null) "${String.format("%.0f", item.gstPercentage)}%" else "",
                        String.format("%.0f", item.totalValue)
                    )

                    // Alternate row background
                    if (index % 2 == 0) {
                        paint.color = android.graphics.Color.rgb(248, 248, 248)
                        canvas.drawRect(columnPositions.first(), yPosition, columnPositions.last(), yPosition + rowHeight, paint)
                        paint.color = android.graphics.Color.BLACK
                    }

                    // Draw row data with center alignment
                    rowData.forEachIndexed { colIndex, data ->
                        val textX = columnPositions[colIndex] + (adjustedColumns[colIndex].second / 2) - (paint.measureText(data) / 2)
                        canvas.drawText(data, textX, yPosition + 12f, paint)
                    }

                    // Draw row borders
                    drawEnhancedTableBorders(canvas, paint, columnPositions, yPosition, rowHeight)

                    yPosition += rowHeight
                }

                // Footer
                paint.textSize = 7f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Report Generated by Attil Inventory Management System", leftMargin, 585f, paint)
                canvas.drawText("Page 1 of 1", rightMargin - 60f, 585f, paint)

                pdfDocument.finishPage(page)

                val fileOutputStream = FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                fileOutputStream.close()
                pdfDocument.close()

                Log.d("ReportViewModel", "Comprehensive inward PDF saved to: ${file.absolutePath}")
                file
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error creating comprehensive inward PDF", e)
                null
            }
        }
    }

    private suspend fun createOutwardPdfReport(context: Context, report: OutwardReport, summary: OutwardSummary?, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ReportViewModel", "Creating comprehensive outward PDF with ${report.items.size} items")

                // Save to Downloads folder on all Android versions
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)

                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 Landscape
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()

                // Page margins
                val leftMargin = 25f
                val rightMargin = 817f
                val topMargin = 25f
                val availableWidth = rightMargin - leftMargin

                var yPosition = topMargin

                // Title
                paint.textSize = 18f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.color = android.graphics.Color.rgb(34, 45, 50)
                canvas.drawText("OUTWARD REPORT", leftMargin, yPosition, paint)
                paint.color = android.graphics.Color.BLACK
                yPosition += 28f

                // Date range and generation info
                paint.textSize = 10f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Period: ${report.filter.startDate} to ${report.filter.endDate}", leftMargin, yPosition, paint)
                canvas.drawText("Generated: ${getCurrentDateTime()}", rightMargin - 150f, yPosition, paint)
                yPosition += 22f

                // COMPREHENSIVE SUMMARY BOX FOR OUTWARD
                paint.textSize = 9f
                val summaryHeight = 110f
                paint.color = android.graphics.Color.rgb(245, 245, 245)
                canvas.drawRect(leftMargin, yPosition, rightMargin, yPosition + summaryHeight, paint)
                paint.color = android.graphics.Color.BLACK
                paint.strokeWidth = 1f
                canvas.drawRect(leftMargin, yPosition, rightMargin, yPosition + summaryHeight, paint)

                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textSize = 11f
                canvas.drawText("COMPREHENSIVE SUMMARY", leftMargin + 10f, yPosition + 15f, paint)

                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8f

                // Calculate comprehensive statistics for outward
                val uniqueCuisines = report.items.mapNotNull { it.cuisineName }.distinct().size
                val uniqueCategories = report.items.mapNotNull { it.categoryName }.distinct().size
                val uniqueItems = report.items.map { it.itemName }.distinct().size
                val avgTransactionValue = if(report.totalTransactions > 0) report.totalValue.toDouble() / report.totalTransactions else 0.0
                val avgQuantityPerTransaction = if(report.totalTransactions > 0) report.totalQuantity / report.totalTransactions else 0.0
                val avgCostPerUnit = if(report.totalQuantity > 0) report.totalValue.toDouble() / report.totalQuantity else 0.0
                val reportPeriod = calculateDateDifference(report.filter.startDate, report.filter.endDate)
                val itemsWithCalculatedCost = report.items.count { it.calculatedCostPerUnit > 0 }
                val topCuisine = report.items.groupBy { it.cuisineName }.maxByOrNull { it.value.sumOf { item -> item.calculatedTotalCost } }?.key ?: "N/A"
                val topCategory = report.items.groupBy { it.categoryName }.maxByOrNull { it.value.sumOf { item -> item.calculatedTotalCost } }?.key ?: "N/A"

                // Summary Row 1
                canvas.drawText("Total Transactions: ${report.totalTransactions}", leftMargin + 10f, yPosition + 28f, paint)
                canvas.drawText("Unique Items: $uniqueItems", leftMargin + 170f, yPosition + 28f, paint)
                canvas.drawText("Unique Cuisines: $uniqueCuisines", leftMargin + 300f, yPosition + 28f, paint)
                canvas.drawText("Unique Categories: $uniqueCategories", leftMargin + 450f, yPosition + 28f, paint)
                canvas.drawText("Report Days: $reportPeriod", leftMargin + 600f, yPosition + 28f, paint)

                // Summary Row 2
                canvas.drawText("Total Quantity: ${String.format("%.2f", report.totalQuantity)} units", leftMargin + 10f, yPosition + 42f, paint)
                canvas.drawText("Avg Qty/Transaction: ${String.format("%.2f", avgQuantityPerTransaction)}", leftMargin + 170f, yPosition + 42f, paint)
                canvas.drawText("Avg Cost/Unit: ₹${String.format("%.2f", avgCostPerUnit)}", leftMargin + 320f, yPosition + 42f, paint)
                canvas.drawText("Daily Transactions: ${String.format("%.1f", if(reportPeriod > 0) report.totalTransactions.toDouble() / reportPeriod else 0.0)}", leftMargin + 480f, yPosition + 42f, paint)

                // Summary Row 3
                canvas.drawText("Total Calculated Cost: ₹${String.format("%,.2f", report.totalValue.toDouble())}", leftMargin + 10f, yPosition + 56f, paint)
                canvas.drawText("Avg Transaction Cost: ₹${String.format("%.2f", avgTransactionValue)}", leftMargin + 220f, yPosition + 56f, paint)
                canvas.drawText("Cost Coverage: $itemsWithCalculatedCost/${report.items.size} items", leftMargin + 400f, yPosition + 56f, paint)

                // Summary Row 4
                canvas.drawText("Top Cuisine (by Cost): ${autoFitText(paint, topCuisine, 140f)}", leftMargin + 10f, yPosition + 70f, paint)
                canvas.drawText("Top Category (by Cost): ${autoFitText(paint, topCategory, 140f)}", leftMargin + 250f, yPosition + 70f, paint)
                canvas.drawText("Daily Avg Consumption: ₹${String.format("%.0f", if(reportPeriod > 0) report.totalValue.toDouble() / reportPeriod else 0.0)}", leftMargin + 450f, yPosition + 70f, paint)

                // Summary Row 5 - Cost calculation methods analysis
                val costMethods = report.items.groupBy { it.costCalculationMethod }.mapValues { it.value.size }
                val movingAvgItems = costMethods["15-day avg"] ?: 0
                val fallbackItems = costMethods.values.sum() - movingAvgItems
                canvas.drawText("15-day Avg Costs: $movingAvgItems items", leftMargin + 10f, yPosition + 84f, paint)
                canvas.drawText("Fallback Costs: $fallbackItems items", leftMargin + 170f, yPosition + 84f, paint)

                // Summary Row 6 - Additional insights
                val maxConsumptionValue = report.items.maxOfOrNull { it.calculatedTotalCost } ?: 0.0
                val minConsumptionValue = report.items.filter { it.calculatedTotalCost > 0 }.minOfOrNull { it.calculatedTotalCost } ?: 0.0
                canvas.drawText("Largest Consumption: ₹${String.format("%.0f", maxConsumptionValue)}", leftMargin + 10f, yPosition + 98f, paint)
                canvas.drawText("Smallest Consumption: ₹${String.format("%.0f", minConsumptionValue)}", leftMargin + 180f, yPosition + 98f, paint)
                canvas.drawText("Cost Range: ₹${String.format("%.0f", maxConsumptionValue - minConsumptionValue)}", leftMargin + 350f, yPosition + 98f, paint)

                yPosition += summaryHeight + 8f

                // API SUMMARY SECTION
                if (summary != null) {
                    paint.textSize = 10f
                    paint.typeface = Typeface.DEFAULT_BOLD
                    canvas.drawText("API SUMMARY INSIGHTS", leftMargin, yPosition, paint)
                    yPosition += 15f

                    paint.textSize = 8f
                    paint.typeface = Typeface.DEFAULT
                    canvas.drawText("Top Cuisine: ${summary.topCuisine ?: "N/A"}", leftMargin, yPosition, paint)
                    canvas.drawText("Top Category: ${summary.topCategory ?: "N/A"}", leftMargin + 200f, yPosition, paint)
                    canvas.drawText("Avg Consumption Value: ₹${summary.averageConsumptionValue}", leftMargin + 400f, yPosition, paint)
                    yPosition += 15f
                } else {
                    yPosition += 10f
                }

                // Auto-fit table setup for Outward Report
                val rowHeight = 18f
                val headerHeight = 22f

                // Optimized column definitions with cost information and user
                val totalTableWidth = availableWidth - 10f
                val columns = arrayOf(
                    Pair("No", 30f),
                    Pair("Item Name", 100f),
                    Pair("Category", 60f),
                    Pair("Cuisine", 60f),
                    Pair("Date & Time", 70f),
                    Pair("Qty", 35f),
                    Pair("Unit", 40f),
                    Pair("Total Cost", 55f),
                    Pair("Method", 60f),
                    Pair("User", 70f)
                )

                // Auto-adjust column widths to fit available space
                val totalDefinedWidth = columns.map { it.second }.sum()
                val scaleFactor = if (totalDefinedWidth > totalTableWidth) totalTableWidth / totalDefinedWidth else 1.0f
                val adjustedColumns = columns.map { Pair(it.first, it.second * scaleFactor) }.toTypedArray()

                // Calculate column positions
                val columnPositions = mutableListOf<Float>()
                var currentX = leftMargin + 5f
                columnPositions.add(currentX)

                adjustedColumns.forEach { (_, width) ->
                    currentX += width
                    columnPositions.add(currentX)
                }

                // Draw table headers
                paint.textSize = 7f
                paint.typeface = Typeface.DEFAULT_BOLD

                // Header background
                paint.color = android.graphics.Color.rgb(200, 200, 200)
                canvas.drawRect(columnPositions.first(), yPosition, columnPositions.last(), yPosition + headerHeight, paint)

                paint.color = android.graphics.Color.BLACK

                // Header text with center alignment
                adjustedColumns.forEachIndexed { index, (header, width) ->
                    val textX = columnPositions[index] + (width / 2) - (paint.measureText(header) / 2)
                    canvas.drawText(header, textX, yPosition + 14f, paint)
                }

                // Draw header borders
                drawEnhancedTableBorders(canvas, paint, columnPositions, yPosition, headerHeight)

                yPosition += headerHeight

                // Table data
                paint.textSize = 6.5f
                paint.typeface = Typeface.DEFAULT
                paint.color = android.graphics.Color.BLACK

                report.items.forEachIndexed { index, item ->
                    if (yPosition + rowHeight > 570f) {
                        // Add "...continued" indicator
                        paint.typeface = Typeface.DEFAULT_BOLD
                        canvas.drawText("... ${report.items.size - index} more items (continued in full CSV export)", leftMargin + 5f, yPosition + 15f, paint)
                        return@forEachIndexed
                    }

                    // Row data with calculated costs and user name
                    val rowData = arrayOf(
                        (index + 1).toString(),
                        autoFitText(paint, item.itemName, adjustedColumns[1].second - 8f),
                        autoFitText(paint, item.categoryName ?: "", adjustedColumns[2].second - 8f),
                        autoFitText(paint, item.cuisineName ?: "", adjustedColumns[3].second - 8f),
                        autoFitText(paint, item.usageDate, adjustedColumns[4].second - 8f),
                        String.format("%.1f", item.outwardQuantity),
                        autoFitText(paint, item.unitOfMeasure, adjustedColumns[6].second - 8f),
                        String.format("%.2f", item.calculatedTotalCost),
                        autoFitText(paint, item.costCalculationMethod, adjustedColumns[8].second - 8f),
                        autoFitText(paint, item.chefName ?: "N/A", adjustedColumns[9].second - 8f)
                    )

                    // Alternate row background
                    if (index % 2 == 0) {
                        paint.color = android.graphics.Color.rgb(248, 248, 248)
                        canvas.drawRect(columnPositions.first(), yPosition, columnPositions.last(), yPosition + rowHeight, paint)
                        paint.color = android.graphics.Color.BLACK
                    }

                    // Draw row data with center alignment
                    rowData.forEachIndexed { colIndex, data ->
                        val textX = columnPositions[colIndex] + (adjustedColumns[colIndex].second / 2) - (paint.measureText(data) / 2)
                        canvas.drawText(data, textX, yPosition + 12f, paint)
                    }

                    // Draw row borders
                    drawEnhancedTableBorders(canvas, paint, columnPositions, yPosition, rowHeight)

                    yPosition += rowHeight
                }

                // Footer
                paint.textSize = 7f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Report Generated by Attil Inventory Management System", leftMargin, 585f, paint)
                canvas.drawText("Page 1 of 1", rightMargin - 60f, 585f, paint)

                pdfDocument.finishPage(page)

                val fileOutputStream = FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                fileOutputStream.close()
                pdfDocument.close()

                Log.d("ReportViewModel", "Comprehensive outward PDF saved to: ${file.absolutePath}")
                file
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error creating comprehensive outward PDF", e)
                null
            }
        }
    }

    // ENHANCED CSV CREATION METHODS
    private suspend fun createInwardCsvReport(context: Context, report: InwardReport, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val csvContent = buildString {
                    // Headers
                    appendLine("S.No,Item Name,Category,Vendor,Purchase Date,Quantity,Unit,Price Per Unit,Total Value,GST%,Price with GST,Bill Number,Expiry Date,Cuisine")

                    // Data rows
                    report.items.forEachIndexed { index, item ->
                        appendLine("${index + 1},\"${item.itemName}\",\"${item.categoryName ?: ""}\",\"${item.vendorName}\",\"${item.purchaseDate}\",${item.inwardQuantity},\"${item.unitOfMeasure}\",${item.pricePerUnit},${item.totalValue},${item.gstPercentage ?: ""},${item.priceWithGst ?: ""},\"${item.billNumber ?: ""}\",\"${item.expiryDate ?: ""}\",\"${item.cuisineName ?: ""}\"")
                    }

                    // Comprehensive Summary
                    appendLine()
                    appendLine("COMPREHENSIVE SUMMARY")
                    appendLine("Total Transactions,${report.totalTransactions}")
                    appendLine("Total Quantity,${report.totalQuantity}")
                    appendLine("Total Value (Ex-GST),${report.totalValue}")
                    appendLine("Total Value (Inc-GST),${report.items.sumOf { (it.priceWithGst ?: it.totalValue) }}")
                    appendLine("Unique Items,${report.items.map { it.itemName }.distinct().size}")
                    appendLine("Unique Vendors,${report.items.map { it.vendorName }.distinct().size}")
                    appendLine("Unique Categories,${report.items.mapNotNull { it.categoryName }.distinct().size}")
                    appendLine("Average Transaction Value,${if(report.totalTransactions > 0) report.totalValue.toDouble() / report.totalTransactions else 0.0}")
                    appendLine("Average Cost Per Unit,${if(report.totalQuantity > 0) report.totalValue.toDouble() / report.totalQuantity else 0.0}")
                    appendLine("Date Range,${report.filter.startDate} to ${report.filter.endDate}")
                    appendLine("Report Generated,${getCurrentDateTime()}")
                }

                // Save to Downloads folder on all Android versions
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)

                file.writeText(csvContent)

                Log.d("ReportViewModel", "Comprehensive CSV saved to: ${file.absolutePath}")
                file
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error creating CSV", e)
                null
            }
        }
    }

    private suspend fun createOutwardCsvReport(context: Context, report: OutwardReport, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val csvContent = buildString {
                    // Headers
                    appendLine("S.No,Item Name,Category,Cuisine,Date & Time,Quantity,Unit,Total Cost,Cost Calculation Method,User (Chef Name)")

                    // Data rows
                    report.items.forEachIndexed { index, item ->
                        appendLine("${index + 1},\"${item.itemName}\",\"${item.categoryName ?: ""}\",\"${item.cuisineName ?: ""}\",\"${item.usageDate}\",${item.outwardQuantity},\"${item.unitOfMeasure}\",${item.calculatedTotalCost},\"${item.costCalculationMethod}\",\"${item.chefName ?: "N/A"}\"")
                    }

                    // Comprehensive Summary
                    appendLine()
                    appendLine("COMPREHENSIVE SUMMARY")
                    appendLine("Total Transactions,${report.totalTransactions}")
                    appendLine("Total Quantity,${report.totalQuantity}")
                    appendLine("Total Calculated Cost,${report.totalValue}")
                    appendLine("Unique Items,${report.items.map { it.itemName }.distinct().size}")
                    appendLine("Unique Cuisines,${report.items.mapNotNull { it.cuisineName }.distinct().size}")
                    appendLine("Unique Categories,${report.items.mapNotNull { it.categoryName }.distinct().size}")
                    appendLine("Items with Calculated Costs,${report.items.count { it.calculatedCostPerUnit > 0 }}")
                    appendLine("Average Transaction Cost,${if(report.totalTransactions > 0) report.totalValue.toDouble() / report.totalTransactions else 0.0}")
                    appendLine("Average Cost Per Unit,${if(report.totalQuantity > 0) report.totalValue.toDouble() / report.totalQuantity else 0.0}")

                    // Cost method breakdown
                    val costMethods = report.items.groupBy { it.costCalculationMethod }.mapValues { it.value.size }
                    costMethods.forEach { (method, count) ->
                        appendLine("$method Items,$count")
                    }

                    appendLine("Date Range,${report.filter.startDate} to ${report.filter.endDate}")
                    appendLine("Report Generated,${getCurrentDateTime()}")
                }

                // Save to Downloads folder on all Android versions
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)

                file.writeText(csvContent)

                Log.d("ReportViewModel", "Comprehensive outward CSV saved to: ${file.absolutePath}")
                file
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Error creating outward CSV", e)
                null
            }
        }
    }

    // FILE OPENING METHODS
    private fun openPdfFile(context: Context, file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error opening PDF file", e)
            Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCsvFile(context: Context, file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Error opening CSV file", e)
            Toast.makeText(context, "No CSV/Excel viewer app found", Toast.LENGTH_SHORT).show()
        }
    }

    // UTILITY METHODS
    private fun autoFitText(paint: Paint, text: String, maxWidth: Float): String {
        val textWidth = paint.measureText(text)
        return if (textWidth <= maxWidth) {
            text
        } else {
            val ratio = maxWidth / textWidth
            val maxChars = (text.length * ratio).toInt() - 3
            if (maxChars <= 0) "..." else "${text.take(maxChars)}..."
        }
    }

    private fun drawEnhancedTableBorders(canvas: android.graphics.Canvas, paint: Paint, columnPositions: List<Float>, yStart: Float, height: Float) {
        val originalStrokeWidth = paint.strokeWidth
        paint.strokeWidth = 1f

        // Vertical lines
        columnPositions.forEach { xPos ->
            canvas.drawLine(xPos, yStart, xPos, yStart + height, paint)
        }

        // Horizontal lines
        canvas.drawLine(columnPositions.first(), yStart, columnPositions.last(), yStart, paint)
        canvas.drawLine(columnPositions.first(), yStart + height, columnPositions.last(), yStart + height, paint)

        paint.strokeWidth = originalStrokeWidth
    }

    private fun calculateDateDifference(startDate: String, endDate: String): Long {
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1
        } catch (e: Exception) {
            1
        }
    }

    private fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}