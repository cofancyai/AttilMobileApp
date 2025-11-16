package com.attil.inventory.presentation.transaction.currentstock

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
import com.attil.inventory.data.model.management.CurrentStock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentStockScreen(
    onBackClick: () -> Unit,
    viewModel: CurrentStockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

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
                        text = "Current Stock",
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
                    IconButton(onClick = { viewModel.clearAllFilters() }) {
                        Icon(
                            Icons.Default.FilterAlt,
                            contentDescription = "Clear Filters",
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
                    // Header with Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Stock",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "${uiState.filteredStocks.size} items • ${uiState.lowStockCount} low stock",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        
                        if (uiState.lowStockCount > 0) {
                            Text(
                                text = "⚠️ ${uiState.lowStockCount} Low Stock",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE91E63),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search and Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                viewModel.searchItems(it)
                            },
                            placeholder = { Text("Search items...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF667eea),
                                focusedLabelColor = Color(0xFF667eea),
                                cursorColor = Color(0xFF667eea)
                            )
                        )
                        
                        // Low Stock Filter
                        FilterChip(
                            onClick = { viewModel.toggleLowStockFilter() },
                            label = { Text("Low Stock", fontSize = 12.sp) },
                            selected = uiState.showLowStockOnly,
                            leadingIcon = if (uiState.showLowStockOnly) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category and Godown Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Filter
                        Box(modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            
                            OutlinedTextField(
                                value = uiState.selectedCategoryFilter.ifEmpty { "All Categories" },
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Category Filter")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF667eea),
                                    unfocusedBorderColor = Color(0xFF999999)
                                )
                            )
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Categories") },
                                    onClick = {
                                        viewModel.filterByCategory("")
                                        expanded = false
                                    }
                                )
                                viewModel.getUniqueCategories().forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            viewModel.filterByCategory(category)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Godown Filter
                        Box(modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            
                            OutlinedTextField(
                                value = uiState.selectedGodownFilter.ifEmpty { "All Godowns" },
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Godown Filter")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF667eea),
                                    unfocusedBorderColor = Color(0xFF999999)
                                )
                            )
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Godowns") },
                                    onClick = {
                                        viewModel.filterByGodown("")
                                        expanded = false
                                    }
                                )
                                viewModel.getUniqueGodowns().forEach { godown ->
                                    DropdownMenuItem(
                                        text = { Text(godown) },
                                        onClick = {
                                            viewModel.filterByGodown(godown)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message
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

                    // Loading or Table
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
                        // Synchronized Scrolling Stock Table
                        CurrentStockTable(
                            stocks = uiState.filteredStocks
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentStockTable(
    stocks: List<CurrentStock>
) {
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
                        TableHeaderCell("Item", 140.dp)
                        TableHeaderCell("Category", 100.dp)
                        TableHeaderCell("Location", 100.dp)
                        TableHeaderCell("Unit", 60.dp)
                        TableHeaderCell("Min Stock", 80.dp)
                        TableHeaderCell("Total In", 80.dp)
                        TableHeaderCell("Total Out", 80.dp)
                        TableHeaderCell("Current", 80.dp)
                        TableHeaderCell("Status", 80.dp)
                    }
                }
            }

            // Data Items
            items(stocks) { stock ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (stock.isLowStock) Color(0xFFFFF3E0) else Color(0xFFF8F9FA)
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
                            text = stock.itemName,
                            width = 140.dp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Category
                        TableDataCell(
                            text = stock.categoryName,
                            width = 100.dp
                        )
                        
                        // Location
                        TableDataCell(
                            text = buildString {
                                append(stock.godownName ?: "No Godown")
                                stock.rackName?.let { append(" / $it") }
                            },
                            width = 100.dp,
                            fontSize = 11.sp
                        )
                        
                        // Unit
                        TableDataCell(
                            text = stock.unitOfMeasure,
                            width = 60.dp,
                            textAlign = TextAlign.Center
                        )
                        
                        // Min Stock
                        TableDataCell(
                            text = "${stock.minimumStockLevel.toInt()}",
                            width = 80.dp,
                            textAlign = TextAlign.Center
                        )
                        
                        // Total In
                        TableDataCell(
                            text = "${stock.totalInward.toInt()}",
                            width = 80.dp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF4CAF50)
                        )
                        
                        // Total Out
                        TableDataCell(
                            text = "${stock.totalOutward.toInt()}",
                            width = 80.dp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFE91E63)
                        )
                        
                        // Current Stock
                        TableDataCell(
                            text = "${stock.currentStock.toInt()}",
                            width = 80.dp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (stock.isLowStock) Color(0xFFE91E63) else Color(0xFF4CAF50)
                        )
                        
                        // Status
                        Box(
                            modifier = Modifier.width(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (stock.isLowStock) "Low" else "OK",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .background(
                                        if (stock.isLowStock) Color(0xFFE91E63) else Color(0xFF4CAF50),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Empty state
            if (stocks.isEmpty()) {
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
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No stock records found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Try adjusting your filters",
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
    color: Color = Color(0xFF333333),
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp
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
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

