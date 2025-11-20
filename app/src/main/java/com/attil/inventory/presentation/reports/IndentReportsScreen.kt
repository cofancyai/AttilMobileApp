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
import com.attil.inventory.data.model.reports.IndentReportSummary
import com.attil.inventory.data.model.reports.IndentReportItemDetail
import com.attil.inventory.presentation.transaction.indent.IndentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentReportsScreen(
    onBackClick: () -> Unit = {},
    viewModel: IndentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // State variables
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_DATE)) }
    var endDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_DATE)) }
    var selectedStatus by remember { mutableStateOf<String?>(null) } // null = All
    var selectedChef by remember { mutableStateOf<String?>(null) } // null = All

    var showDatePicker by remember { mutableStateOf(false) }
    var isDatePickerForStart by remember { mutableStateOf(true) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var expandedIndentId by remember { mutableStateOf<String?>(null) }

    // Status options
    val statusOptions = listOf("All", "Submitted", "Approved", "Fulfilled", "Received")

    // Load report on date/filter change
    LaunchedEffect(startDate, endDate, selectedStatus, selectedChef) {
        viewModel.loadIndentReport(startDate, endDate, selectedStatus, selectedChef)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header Card with Filters
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Indent Report",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "Track indent fulfillment and verification status",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Filter
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedStatus ?: "All",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Filter", color = Color(0xFF666666)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color(0xFF333333),
                            focusedTextColor = Color(0xFF333333)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, color = Color(0xFF333333)) },
                                onClick = {
                                    selectedStatus = if (status == "All") null else status
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Export Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.exportIndentReportToPdf(context)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.indentReport != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)
                        )
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.exportIndentReportToCsv(context)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.indentReport != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047)
                        )
                    ) {
                        Icon(
                            Icons.Default.TableChart,
                            contentDescription = "CSV",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 12.sp)
                    }
                }
            }
        }

        // Report Content
        if (uiState.isLoadingReport) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.reportError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = uiState.reportError!!,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFD32F2F)
                )
            }
        } else if (uiState.indentReport == null || uiState.indentReport!!.indents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No indents found for the selected period",
                    color = Color(0xFF666666),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.indentReport!!.indents) { indent ->
                    IndentReportCard(
                        indent = indent,
                        isExpanded = expandedIndentId == indent.indentId,
                        onExpandToggle = {
                            expandedIndentId = if (expandedIndentId == indent.indentId) null else indent.indentId
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun IndentReportCard(
    indent: IndentReportSummary,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = indent.chefName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = "${indent.cuisineName} • ${indent.purpose}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Required: ${indent.requiredDate} ${indent.requiredTime}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }

                // Status Badge
                Surface(
                    color = getStatusColor(indent.status),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = indent.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "Total",
                    value = indent.totalItems.toString(),
                    color = Color(0xFF2196F3)
                )
                StatChip(
                    label = "Fulfilled",
                    value = indent.fulfilledItems.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatChip(
                    label = "Verified",
                    value = indent.verifiedItems.toString(),
                    color = Color(0xFF9C27B0)
                )
                if (indent.rejectedItems > 0) {
                    StatChip(
                        label = "Rejected",
                        value = indent.rejectedItems.toString(),
                        color = Color(0xFFE53935)
                    )
                }
            }

            // Fulfillment Progress
            if (indent.totalItems > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                val fulfillmentProgress = indent.fulfilledItems.toFloat() / indent.totalItems.toFloat()
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fulfillment Progress",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${(fulfillmentProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = fulfillmentProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }

            // Fulfilled By
            if (indent.fulfilledBy != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fulfilled by: ${indent.fulfilledBy}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Expand/Collapse Button
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onExpandToggle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isExpanded) "Hide Items" else "Show Items (${indent.totalItems})",
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            // Expanded Items List
            if (isExpanded) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE0E0E0)
                )

                indent.items.forEach { item ->
                    IndentItemRow(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun IndentItemRow(item: IndentReportItemDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    item.isRejected -> Color(0xFFFFEBEE)
                    item.isVerified -> Color(0xFFE8F5E9)
                    item.isFulfilled -> Color(0xFFFFF9C4)
                    else -> Color(0xFFF5F5F5)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.itemName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = buildString {
                    append("Requested: ${item.requestedQuantity} ${item.unitOfMeasure}")
                    if (item.fulfilledQuantity != null && item.fulfilledQuantity > 0) {
                        append(" • Fulfilled: ${item.fulfilledQuantity} ${item.unitOfMeasure}")
                    }
                },
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }

        // Status Icon
        Icon(
            imageVector = when {
                item.isRejected -> Icons.Default.Cancel
                item.isVerified -> Icons.Default.CheckCircle
                item.isFulfilled -> Icons.Default.LocalShipping
                else -> Icons.Default.Schedule
            },
            contentDescription = "Status",
            tint = when {
                item.isRejected -> Color(0xFFE53935)
                item.isVerified -> Color(0xFF4CAF50)
                item.isFulfilled -> Color(0xFFFFC107)
                else -> Color(0xFF9E9E9E)
            },
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = color
            )
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Submitted" -> Color(0xFF2196F3)
        "Approved" -> Color(0xFFFFC107)
        "Fulfilled" -> Color(0xFF4CAF50)
        "Received" -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }
}
