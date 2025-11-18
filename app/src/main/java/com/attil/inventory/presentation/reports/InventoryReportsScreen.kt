package com.attil.inventory.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.attil.inventory.data.model.reports.*
import com.attil.inventory.presentation.viewmodel.ReportViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryReportsScreen(
    reportType: ReportType,
    onBackClick: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    val inwardReport by viewModel.inwardReport.collectAsState()
    val outwardReport by viewModel.outwardReport.collectAsState()
    val cuisineWiseReport by viewModel.cuisineWiseReport.collectAsState()
    val selectedCuisineId by viewModel.selectedCuisineId.collectAsState()
    val cuisines by viewModel.cuisines.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var isDatePickerForStart by remember { mutableStateOf(true) }
    var cuisineDropdownExpanded by remember { mutableStateOf(false) }

    // Set report type when screen loads
    LaunchedEffect(reportType) {
        viewModel.setReportType(reportType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1976D2)
                        )
                    }
                    Column {
                        Text(
                            text = when (reportType) {
                                ReportType.INWARD -> "Inward Report"
                                ReportType.OUTWARD -> "Outward Report"
                                ReportType.CUISINE_WISE -> "Cuisine-Wise Report"
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = when (reportType) {
                                ReportType.INWARD -> "Track inventory purchases and receipts"
                                ReportType.OUTWARD -> "Monitor inventory consumption and usage with calculated costs"
                                ReportType.CUISINE_WISE -> "Analyze consumption patterns by cuisine with breakdown"
                            },
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            isDatePickerForStart = true
                            showDatePicker = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Start Date",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "From: $startDate",
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isDatePickerForStart = false
                            showDatePicker = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "End Date",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "To: $endDate",
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.refreshCurrentReport() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }

                // Cuisine Filter (only for CUISINE_WISE report)
                if (reportType == ReportType.CUISINE_WISE) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { cuisineDropdownExpanded = !cuisineDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = "Cuisine",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedCuisineId == null) {
                                    "All Cuisines"
                                } else {
                                    cuisines.find { it.id == selectedCuisineId }?.name ?: "Select Cuisine"
                                },
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                if (cuisineDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        }

                        DropdownMenu(
                            expanded = cuisineDropdownExpanded,
                            onDismissRequest = { cuisineDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Cuisines") },
                                onClick = {
                                    viewModel.setSelectedCuisineId(null)
                                    cuisineDropdownExpanded = false
                                }
                            )
                            cuisines.forEach { cuisine ->
                                DropdownMenuItem(
                                    text = { Text(cuisine.name) },
                                    onClick = {
                                        viewModel.setSelectedCuisineId(cuisine.id)
                                        cuisineDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Download Buttons Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download PDF Button
                Button(
                    onClick = {
                        when (reportType) {
                            ReportType.INWARD -> inwardReport?.let {
                                viewModel.exportInwardReportToPdf(context, it)
                            }
                            ReportType.OUTWARD -> outwardReport?.let {
                                viewModel.exportOutwardReportToPdf(context, it)
                            }
                            ReportType.CUISINE_WISE -> cuisineWiseReport?.let {
                                viewModel.exportCuisineWiseReportToPdf(context, it)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "Download PDF",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download PDF", color = Color.White)
                }

                // Download CSV Button
                Button(
                    onClick = {
                        when (reportType) {
                            ReportType.INWARD -> inwardReport?.let {
                                viewModel.exportInwardReportToCsv(context, it)
                            }
                            ReportType.OUTWARD -> outwardReport?.let {
                                viewModel.exportOutwardReportToCsv(context, it)
                            }
                            ReportType.CUISINE_WISE -> cuisineWiseReport?.let {
                                viewModel.exportCuisineWiseReportToCsv(context, it)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download CSV",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download CSV", color = Color.White)
                }
            }
        }

        // Content based on loading state and report type
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading ${reportType.name.lowercase()} report...",
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.refreshCurrentReport() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2)
                                )
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
            }
            else -> {
                when (reportType) {
                    ReportType.INWARD -> {
                        inwardReport?.let { report ->
                            InwardReportContent(
                                report = report,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    ReportType.OUTWARD -> {
                        outwardReport?.let { report ->
                            OutwardReportContent(
                                report = report,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    ReportType.CUISINE_WISE -> {
                        cuisineWiseReport?.let { report ->
                            CuisineWiseReportContent(
                                report = report,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            currentDate = if (isDatePickerForStart) startDate else endDate,
            onDateSelected = { selectedDate ->
                if (isDatePickerForStart) {
                    viewModel.setStartDate(selectedDate)
                } else {
                    viewModel.setEndDate(selectedDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun InwardReportContent(
    report: InwardReport,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "Inward Summary",
                items = listOf(
                    "Total Transactions" to report.totalTransactions.toString(),
                    "Total Quantity" to String.format("%.2f", report.totalQuantity),
                    "Total Value" to "₹${NumberFormat.getInstance().format(report.totalValue)}",
                    "Date Range" to "${report.filter.startDate} to ${report.filter.endDate}"
                ),
                backgroundColor = Color(0xFFE8F5E8)
            )
        }

        items(report.items) { item ->
            InwardItemCard(item = item)
        }
    }
}

@Composable
private fun OutwardReportContent(
    report: OutwardReport,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "Outward Summary",
                items = listOf(
                    "Total Transactions" to report.totalTransactions.toString(),
                    "Total Quantity" to String.format("%.2f", report.totalQuantity),
                    "Total Calculated Cost" to "₹${NumberFormat.getInstance().format(report.totalValue)}",
                    "Date Range" to "${report.filter.startDate} to ${report.filter.endDate}"
                ),
                backgroundColor = Color(0xFFFFE8E8)
            )
        }

        items(report.items) { item ->
            OutwardItemCard(item = item)
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    items: List<Pair<String, String>>,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}

@Composable
private fun InwardItemCard(item: InwardReportItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.itemName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vendor: ${item.vendorName}", fontSize = 12.sp, color = Color(0xFF666666))
                    Text("Category: ${item.categoryName ?: "N/A"}", fontSize = 12.sp, color = Color(0xFF666666))
                    Text("Date: ${item.purchaseDate}", fontSize = 12.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Qty: ${item.inwardQuantity} ${item.unitOfMeasure}", fontSize = 12.sp, color = Color(0xFF333333))
                    Text("Rate: ₹${item.pricePerUnit}", fontSize = 12.sp, color = Color(0xFF333333))
                    Text("Total: ₹${String.format("%.2f", item.totalValue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
        }
    }
}

@Composable
private fun OutwardItemCard(item: OutwardReportItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.itemName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cuisine: ${item.cuisineName ?: "N/A"}", fontSize = 12.sp, color = Color(0xFF666666))
                    Text("Category: ${item.categoryName ?: "N/A"}", fontSize = 12.sp, color = Color(0xFF666666))
                    Text("Date: ${item.usageDate}", fontSize = 12.sp, color = Color(0xFF666666))
                    if (!item.purpose.isNullOrEmpty()) {
                        Text("Purpose: ${item.purpose}", fontSize = 12.sp, color = Color(0xFF666666))
                    }
                    // Show cost calculation method
                    if (item.costCalculationMethod != "N/A" && item.costCalculationMethod != "Error") {
                        Text("Cost Method: ${item.costCalculationMethod}", fontSize = 11.sp, color = Color(0xFF2196F3))
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Qty: ${item.outwardQuantity} ${item.unitOfMeasure}", fontSize = 12.sp, color = Color(0xFF333333))

                    // Show calculated cost per unit if available
                    if (item.calculatedCostPerUnit > 0.0) {
                        Text("Cost/Unit: ₹${String.format("%.2f", item.calculatedCostPerUnit)}", fontSize = 12.sp, color = Color(0xFF333333))
                        Text("Total Cost: ₹${String.format("%.2f", item.calculatedTotalCost)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))
                    } else {
                        // Fallback to estimated value if no calculated cost
                        if (item.estimatedValue != null && item.estimatedValue > 0.0) {
                            Text("Est. Value: ₹${String.format("%.2f", item.estimatedValue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        } else {
                            Text("No Cost Data", fontSize = 12.sp, color = Color(0xFF999999))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CuisineWiseReportContent(
    report: CuisineWiseReport,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = if (report.cuisineName == null) "All Cuisines Summary" else "${report.cuisineName} Summary",
                items = listOf(
                    "Total Transactions" to report.totalTransactions.toString(),
                    "Total Quantity" to String.format("%.2f", report.totalQuantity),
                    "Total Calculated Cost" to "₹${NumberFormat.getInstance().format(report.totalValue)}",
                    "Date Range" to "${report.filter.startDate} to ${report.filter.endDate}"
                ),
                backgroundColor = Color(0xFFFFE8F5)
            )
        }

        // Cuisine Breakdown Section
        if (report.cuisineBreakdown.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Cuisine Breakdown",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        report.cuisineBreakdown.forEach { breakdown ->
                            CuisineBreakdownCard(breakdown = breakdown)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        items(report.items) { item ->
            OutwardItemCard(item = item)
        }
    }
}

@Composable
private fun CuisineBreakdownCard(breakdown: CuisineBreakdownItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = breakdown.cuisineName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EA)
                )
                Text(
                    text = "${String.format("%.1f", breakdown.percentageOfTotal)}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EA)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Transactions: ${breakdown.totalTransactions}", fontSize = 12.sp, color = Color(0xFF666666))
                Text("Qty: ${String.format("%.2f", breakdown.totalQuantity)}", fontSize = 12.sp, color = Color(0xFF666666))
                Text("Cost: ₹${String.format("%.2f", breakdown.totalCost)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        ),
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF1976D2)
                )
            ) {
                Text("OK", color = Color(0xFF1976D2))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text("Cancel", color = Color(0xFF666666))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF333333),
                headlineContentColor = Color(0xFF333333),
                weekdayContentColor = Color(0xFF666666),
                subheadContentColor = Color(0xFF333333),
                dayContentColor = Color(0xFF333333),
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = Color(0xFF1976D2),
                todayContentColor = Color(0xFF1976D2),
                todayDateBorderColor = Color(0xFF1976D2)
            )
        )
    }
}