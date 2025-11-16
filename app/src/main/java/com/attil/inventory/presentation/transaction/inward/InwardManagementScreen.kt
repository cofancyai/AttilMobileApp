package com.attil.inventory.presentation.transaction.inward

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.attil.inventory.data.model.transaction.InwardItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardManagementScreen(
    onBackClick: () -> Unit,
    viewModel: InwardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InwardItem?>(null) }

    val premiumGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(premiumGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Inward Items (Purchases)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Purchase",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Purchase Records",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "${uiState.inwardItems.size} records",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    uiState.error?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF667eea)
                            )
                        }
                    } else {
                        // Synchronized Scrolling Table
                        InwardItemsTable(
                            items = uiState.inwardItems,
                            onEdit = { editingItem = it },
                            onDelete = { viewModel.deleteInwardItem(it.id!!) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        InwardItemDialog(
            item = null,
            categories = uiState.categories,
            items = uiState.items,
            cuisines = uiState.cuisines,
            vendors = uiState.vendors,
            onDismiss = { showAddDialog = false },
            onSave = { itemId, vendorName, vendorContact, vendorAddress, purchaseDate,
                       inwardQuantity, pricePerUnit, priceWithoutGst, priceWithGst,
                       gstPercentage, billNumber, expiryDate, cuisineId ->
                viewModel.createInwardItem(
                    itemId, vendorName, vendorContact, vendorAddress, purchaseDate,
                    inwardQuantity, pricePerUnit, priceWithoutGst, priceWithGst,
                    gstPercentage, billNumber, expiryDate, cuisineId, null
                )
                showAddDialog = false
            },
            isLoading = uiState.isCreating
        )
    }

    editingItem?.let { item ->
        InwardItemDialog(
            item = item,
            categories = uiState.categories,
            items = uiState.items,
            cuisines = uiState.cuisines,
            vendors = uiState.vendors,
            onDismiss = { editingItem = null },
            onSave = { itemId, vendorName, vendorContact, vendorAddress, purchaseDate,
                       inwardQuantity, pricePerUnit, priceWithoutGst, priceWithGst,
                       gstPercentage, billNumber, expiryDate, cuisineId ->
                viewModel.updateInwardItem(
                    item.id!!, itemId, vendorName, vendorContact, vendorAddress, purchaseDate,
                    inwardQuantity, pricePerUnit, priceWithoutGst, priceWithGst,
                    gstPercentage, billNumber, expiryDate, cuisineId
                )
                editingItem = null
            },
            isLoading = uiState.isUpdating
        )
    }
}

@Composable
fun InwardItemsTable(
    items: List<InwardItem>,
    onEdit: (InwardItem) -> Unit,
    onDelete: (InwardItem) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val scrollState = rememberScrollState()

    Column {
        // Table with synchronized horizontal scrolling
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Header Item
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF667eea)
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("Item", 120.dp)
                        TableHeaderCell("Vendor", 100.dp)
                        TableHeaderCell("Qty", 60.dp)
                        TableHeaderCell("Unit", 60.dp)
                        TableHeaderCell("Price/Unit", 80.dp)
                        TableHeaderCell("Total", 80.dp)
                        TableHeaderCell("GST%", 50.dp)
                        TableHeaderCell("Date", 80.dp)
                        TableHeaderCell("Bill#", 80.dp)
                        TableHeaderCell("Expiry", 80.dp)
                        TableHeaderCell("Actions", 100.dp)
                    }
                }
            }

            // Data Items
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F9FA)
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Item Name
                        TableDataCell(
                            text = item.items?.name ?: "Unknown",
                            width = 120.dp,
                            fontWeight = FontWeight.Medium
                        )

                        // Vendor
                        TableDataCell(
                            text = item.vendorName,
                            width = 100.dp
                        )

                        // Quantity
                        TableDataCell(
                            text = item.inwardQuantity.toString(),
                            width = 60.dp,
                            textAlign = TextAlign.Center
                        )

                        // Unit
                        TableDataCell(
                            text = item.items?.unitOfMeasure ?: "",
                            width = 60.dp,
                            textAlign = TextAlign.Center
                        )

                        // Price per Unit
                        TableDataCell(
                            text = "₹${String.format("%.2f", item.pricePerUnit)}",
                            width = 80.dp,
                            textAlign = TextAlign.End
                        )

                        // Total Price
                        TableDataCell(
                            text = "₹${String.format("%.2f", item.priceWithGst ?: item.pricePerUnit * item.inwardQuantity)}",
                            width = 80.dp,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )

                        // GST%
                        TableDataCell(
                            text = if (item.gstPercentage != null && item.gstPercentage > 0) {
                                "${item.gstPercentage.toInt()}%"
                            } else {
                                "0%"
                            },
                            width = 50.dp,
                            textAlign = TextAlign.Center
                        )

                        // Purchase Date
                        TableDataCell(
                            text = try {
                                dateFormatter.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(item.purchaseDate))
                            } catch (e: Exception) {
                                item.purchaseDate
                            },
                            width = 80.dp,
                            textAlign = TextAlign.Center
                        )

                        // Bill Number
                        TableDataCell(
                            text = item.billNumber ?: "-",
                            width = 80.dp,
                            textAlign = TextAlign.Center
                        )

                        // Expiry Date
                        TableDataCell(
                            text = item.expiryDate ?: "-",
                            width = 80.dp,
                            textAlign = TextAlign.Center,
                            color = if (item.expiryDate != null) Color(0xFFE91E63) else Color(0xFF666666)
                        )

                        // Actions
                        Row(
                            modifier = Modifier.width(100.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = { onEdit(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Empty state
            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Inbox,
                                contentDescription = null,
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No purchase records found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Add your first purchase record",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TableDataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color(0xFF333333)
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = when (textAlign) {
            TextAlign.Center -> Alignment.Center
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}