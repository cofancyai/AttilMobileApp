package com.attil.inventory.presentation.transaction.outward

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
import com.attil.inventory.data.model.transaction.OutwardItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutwardManagementScreen(
    onBackClick: () -> Unit,
    viewModel: OutwardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<OutwardItem?>(null) }

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
                        text = "Outward Items (Unplanned)",
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
                    IconButton(onClick = { 
                        viewModel.showItemSelection()
                        showAddDialog = true 
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Outward Items",
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
                            text = "Outward Records",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "${uiState.outwardItems.size} records",
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
                        OutwardItemsTable(
                            items = uiState.outwardItems,
                            onEdit = { editingItem = it },
                            onDelete = { viewModel.deleteOutwardItem(it.id!!) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        MultiItemOutwardDialog(
            availableItems = uiState.availableItems,
            selectedItems = uiState.selectedItems,
            cuisines = uiState.cuisines,
            selectedCuisine = uiState.selectedCuisine,
            categories = viewModel.getUniqueCategories(),
            selectedCategory = uiState.selectedCategoryFilter,
            cuisineType = uiState.cuisineType,
            isLoadingItems = uiState.isLoadingItems,
            onDismiss = { 
                showAddDialog = false
                viewModel.hideItemSelection()
            },
            onCategorySelected = { viewModel.filterByCategory(it) },
            onCuisineSelected = { viewModel.selectCuisine(it) },
            onCuisineTypeChange = { viewModel.setCuisineType(it) },
            onItemToggle = { viewModel.toggleItemSelection(it) },
            onQuantityChange = { itemId, quantity -> viewModel.updateItemQuantity(itemId, quantity) },
            onSave = { notes ->
                viewModel.createMultipleOutwardItems(notes)
                showAddDialog = false
            },
            isLoading = uiState.isCreating
        )
    }

    editingItem?.let { item ->
        OutwardItemDialog(
            item = item,
            cuisines = uiState.cuisines,
            onDismiss = { editingItem = null },
            onSave = { itemId, outwardQuantity, usageDate, cuisineType, notes, cuisineId ->
                viewModel.updateOutwardItem(
                    item.id!!, itemId, outwardQuantity, usageDate, cuisineType, notes, cuisineId
                )
                editingItem = null
            },
            isLoading = uiState.isUpdating
        )
    }
}

@Composable
fun OutwardItemsTable(
    items: List<OutwardItem>,
    onEdit: (OutwardItem) -> Unit,
    onDelete: (OutwardItem) -> Unit
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
                        TableHeaderCell("Category", 100.dp)
                        TableHeaderCell("Qty", 60.dp)
                        TableHeaderCell("Unit", 60.dp)
                        TableHeaderCell("Date", 80.dp)
                        TableHeaderCell("Cuisine Type", 100.dp)
                        TableHeaderCell("Cuisine", 100.dp)
                        TableHeaderCell("Notes", 120.dp)
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
                            text = item.items?.name ?: "Unknown Item",
                            width = 120.dp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Category
                        TableDataCell(
                            text = item.categories?.name ?: "Unknown",
                            width = 100.dp
                        )
                        
                        // Quantity
                        TableDataCell(
                            text = item.outwardQuantity.toInt().toString(),
                            width = 60.dp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFE91E63),
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Unit
                        TableDataCell(
                            text = item.items?.unitOfMeasure ?: "",
                            width = 60.dp,
                            textAlign = TextAlign.Center
                        )
                        
                        // Date
                        TableDataCell(
                            text = try {
                                dateFormatter.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(item.usageDate))
                            } catch (e: Exception) {
                                item.usageDate
                            },
                            width = 80.dp,
                            textAlign = TextAlign.Center
                        )
                        
                        // Cuisine Type
                        TableDataCell(
                            text = item.cuisineType ?: "-",
                            width = 100.dp,
                            textAlign = TextAlign.Center
                        )
                        
                        // Cuisine
                        TableDataCell(
                            text = item.cuisines?.name ?: "-",
                            width = 100.dp,
                            textAlign = TextAlign.Center
                        )
                        
                        // Notes
                        TableDataCell(
                            text = item.notes ?: "-",
                            width = 120.dp
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
                                Icons.Default.CallMade,
                                contentDescription = null,
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No outward records found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Add your first outward record",
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