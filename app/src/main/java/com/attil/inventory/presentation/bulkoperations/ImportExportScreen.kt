package com.attil.inventory.presentation.bulkoperations

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    onBackClick: () -> Unit = {},
    viewModel: ImportExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showImportOptions by remember { mutableStateOf(false) }
    var showExportOptions by remember { mutableStateOf(false) }

    // File picker for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.handleFileSelected(context, it)
        }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                        text = "Import / Export",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = "Bulk import or export inventory data",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        // Import and Export Action Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Import Section
            item {
                ActionCard(
                    title = "Import Data",
                    description = "Import categories, racks, items, or initial stock from Excel files",
                    icon = Icons.Default.Upload,
                    color = Color(0xFF4CAF50),
                    onClick = { showImportOptions = true }
                )
            }

            // Export Section
            item {
                ActionCard(
                    title = "Export Data",
                    description = "Export items, stock, transactions to Excel format",
                    icon = Icons.Default.Download,
                    color = Color(0xFF2196F3),
                    onClick = { showExportOptions = true }
                )
            }

            // Download Import Templates
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Download Import Templates",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Download pre-formatted Excel templates for importing data",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        listOf(
                            "Categories Template" to "categories_import.xlsx",
                            "Racks Template" to "racks_import.xlsx",
                            "Items Template" to "items_import.xlsx",
                            "Initial Stock Template" to "initial_stock_inward.xlsx"
                        ).forEach { (title, filename) ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.downloadTemplate(context, filename)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Download",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(title)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Status/Result Display
            if (uiState.isProcessing || uiState.lastResult != null) {
                item {
                    ResultCard(uiState = uiState)
                }
            }
        }
    }

    // Import Options Dialog
    if (showImportOptions) {
        ImportOptionsDialog(
            onDismiss = { showImportOptions = false },
            onOptionSelected = { importType ->
                showImportOptions = false
                viewModel.setImportType(importType)
                filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            }
        )
    }

    // Export Options Dialog
    if (showExportOptions) {
        ExportOptionsDialog(
            onDismiss = { showExportOptions = false },
            onExport = { exportType ->
                showExportOptions = false
                viewModel.exportData(context, exportType)
            }
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFF999999),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ResultCard(uiState: ImportExportUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                uiState.isProcessing -> Color(0xFFFFF9C4)
                uiState.lastResult?.success == true -> Color(0xFFE8F5E9)
                else -> Color(0xFFFFEBEE)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            if (uiState.isProcessing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Processing...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (uiState.lastResult != null) {
                val result = uiState.lastResult
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = if (result.success) "Success" else "Error",
                        tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = result.message,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (result.totalRecords > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Total: ${result.totalRecords} | Success: ${result.successfulRecords} | Failed: ${result.failedRecords}",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        if (result.errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Errors:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE53935)
                            )
                            result.errors.take(5).forEach { error ->
                                Text(
                                    text = "• $error",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE53935)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportOptionsDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Import Type") },
        text = {
            Column {
                listOf(
                    "Categories" to "CATEGORIES",
                    "Racks" to "RACKS",
                    "Items" to "ITEMS",
                    "Initial Stock (Inward)" to "INITIAL_STOCK"
                ).forEach { (label, type) ->
                    TextButton(
                        onClick = { onOptionSelected(type) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExportOptionsDialog(
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Export Type") },
        text = {
            Column {
                listOf(
                    "Categories" to "CATEGORIES",
                    "Racks" to "RACKS",
                    "Items" to "ITEMS",
                    "Current Stock" to "CURRENT_STOCK",
                    "Inward Transactions" to "INWARD_TRANSACTIONS",
                    "Outward Transactions" to "OUTWARD_TRANSACTIONS"
                ).forEach { (label, type) ->
                    TextButton(
                        onClick = { onExport(type) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
