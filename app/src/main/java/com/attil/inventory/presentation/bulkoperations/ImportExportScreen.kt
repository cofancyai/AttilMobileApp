package com.attil.inventory.presentation.bulkoperations

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    var selectedTemplateType by remember { mutableStateOf("") }
    var selectedImportType by remember { mutableStateOf("") }
    var selectedExportType by remember { mutableStateOf("") }

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

        // Main Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Download Import Template
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFFFF9800).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GetApp,
                                    contentDescription = "Download",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Download Import Template",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "Get pre-formatted Excel templates",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DropdownField(
                            label = "Select Template Type",
                            options = listOf(
                                "Godowns" to "GODOWNS",
                                "Categories" to "CATEGORIES",
                                "Cuisines" to "CUISINES",
                                "Vendors" to "VENDORS",
                                "Usage" to "USAGE",
                                "Racks" to "RACKS",
                                "Items" to "ITEMS",
                                "Initial Stock" to "INITIAL_STOCK",
                                "Inward Transactions" to "INWARD_TRANSACTIONS"
                            ),
                            selectedValue = selectedTemplateType,
                            onValueChange = { selectedTemplateType = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (selectedTemplateType.isNotEmpty()) {
                                    viewModel.downloadTemplate(context, selectedTemplateType)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            enabled = selectedTemplateType.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Template")
                        }
                    }
                }
            }

            // Card 2: Import Data
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = "Upload",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Import Data",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "Upload Excel files to import data",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DropdownField(
                            label = "Select Import Type",
                            options = listOf(
                                "Godowns" to "GODOWNS",
                                "Categories" to "CATEGORIES",
                                "Cuisines" to "CUISINES",
                                "Vendors" to "VENDORS",
                                "Usage" to "USAGE",
                                "Racks" to "RACKS",
                                "Items" to "ITEMS",
                                "Initial Stock" to "INITIAL_STOCK",
                                "Inward Transactions" to "INWARD_TRANSACTIONS"
                            ),
                            selectedValue = selectedImportType,
                            onValueChange = { selectedImportType = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (selectedImportType.isNotEmpty()) {
                                    viewModel.setImportType(selectedImportType)
                                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            enabled = selectedImportType.isNotEmpty()
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = "Select File")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select File & Import")
                        }
                    }
                }
            }

            // Card 3: Export Data
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Export",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Export Data",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "Download data as Excel files",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DropdownField(
                            label = "Select Export Type",
                            options = listOf(
                                "Godowns" to "GODOWNS",
                                "Categories" to "CATEGORIES",
                                "Cuisines" to "CUISINES",
                                "Vendors" to "VENDORS",
                                "Usage" to "USAGE",
                                "Racks" to "RACKS",
                                "Items" to "ITEMS",
                                "Current Stock" to "CURRENT_STOCK",
                                "Inward Transactions" to "INWARD_TRANSACTIONS",
                                "Outward Transactions" to "OUTWARD_TRANSACTIONS"
                            ),
                            selectedValue = selectedExportType,
                            onValueChange = { selectedExportType = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (selectedExportType.isNotEmpty()) {
                                    viewModel.exportData(context, selectedExportType)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            enabled = selectedExportType.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Export")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Data")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.second == selectedValue }?.first ?: "Select..."

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = Color(0xFF666666)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333),
                focusedLabelColor = Color(0xFF1976D2),
                unfocusedLabelColor = Color(0xFF666666)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label, color = Color(0xFF333333)) },
                    onClick = {
                        onValueChange(value)
                        expanded = false
                    }
                )
            }
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
                                text = "Total: ${result.totalRecords} | Success: ${result.successfulRecords} | Failed: ${result.failedRecords} | Skipped: ${result.skippedDuplicates}",
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
                            if (result.errors.size > 5) {
                                Text(
                                    text = "... and ${result.errors.size - 5} more errors",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
