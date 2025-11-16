package com.attil.inventory.presentation.transaction.indent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.attil.inventory.data.model.transaction.Indent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentFulfillmentScreen(
    onBackClick: () -> Unit,
    currentUserId: String,
    viewModel: IndentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedIndent by remember { mutableStateOf<Indent?>(null) }
    var fulfillmentItems by remember { mutableStateOf<List<FulfillmentItem>>(emptyList()) }

    val premiumGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
    )

    LaunchedEffect(Unit) {
        viewModel.loadIndents()
    }

    Box(modifier = Modifier.fillMaxSize().background(premiumGradient)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedIndent == null) "Indent Fulfillment" else "Fulfill Items",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedIndent != null) {
                            selectedIndent = null
                            fulfillmentItems = emptyList()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Card(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF667eea))
                        }
                    }
                    selectedIndent != null -> {
                        LaunchedEffect(selectedIndent!!.indentItems) {
                            selectedIndent!!.indentItems?.let { items ->
                                viewModel.getItemsWithStock(items).collect { result ->
                                    result.onSuccess { stockItems ->
                                        fulfillmentItems = stockItems
                                    }
                                }
                            }
                        }

                        FulfillmentItemsView(
                            indent = selectedIndent!!,
                            fulfillmentItems = fulfillmentItems,
                            onItemSelectionChange = { item, selected ->
                                fulfillmentItems = fulfillmentItems.map {
                                    if (it.indentItem.id == item.indentItem.id) it.copy(isSelected = selected) else it
                                }
                            },
                            onQuantityChange = { item, quantity ->
                                fulfillmentItems = fulfillmentItems.map {
                                    if (it.indentItem.id == item.indentItem.id) it.copy(fulfillQuantity = quantity) else it
                                }
                            },
                            onAutoFill = { item ->
                                fulfillmentItems = fulfillmentItems.map {
                                    if (it.indentItem.id == item.indentItem.id) {
                                        it.copy(fulfillQuantity = it.indentItem.requestedQuantity)
                                    } else it
                                }
                            },
                            onCompleteFulfillment = {
                                viewModel.fulfillIndent(selectedIndent!!.id!!, fulfillmentItems, currentUserId)
                                selectedIndent = null
                                fulfillmentItems = emptyList()
                            }
                        )
                    }
                    else -> {
                        IndentListView(
                            indents = uiState.indents.filter { it.status in listOf("Submitted", "Approved") },
                            onIndentClick = { indent ->
                                selectedIndent = indent
                                fulfillmentItems = emptyList()
                            }
                        )
                    }
                }
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error", color = Color(0xFF333333)) },
            text = { Text(error, color = Color(0xFF666666)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun IndentListView(
    indents: List<Indent>,
    onIndentClick: (Indent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Pending Fulfillment (${indents.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }

        if (indents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "No Pending",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All Indents Fulfilled",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(indents) { indent ->
                IndentCard(indent = indent, onClick = { onIndentClick(indent) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndentCard(
    indent: Indent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = indent.cuisines?.name ?: "Unknown Cuisine",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Purpose: ${indent.purpose}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Required: ${indent.requiredDate} at ${indent.requiredTime}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                Surface(
                    color = when (indent.priority) {
                        "High" -> Color(0xFFE91E63)
                        "Medium" -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = indent.priority,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Items: ${indent.indentItems?.size ?: 0}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "Status: ${indent.status}",
                    fontSize = 12.sp,
                    color = Color(0xFF667eea),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FulfillmentItemsView(
    indent: Indent,
    fulfillmentItems: List<FulfillmentItem>,
    onItemSelectionChange: (FulfillmentItem, Boolean) -> Unit,
    onQuantityChange: (FulfillmentItem, Double) -> Unit,
    onAutoFill: (FulfillmentItem) -> Unit,
    onCompleteFulfillment: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF667eea).copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = indent.cuisines?.name ?: "Unknown Cuisine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Purpose: ${indent.purpose}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "Required: ${indent.requiredDate} at ${indent.requiredTime}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        // Items List
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fulfillmentItems) { item ->
                FulfillmentItemCard(
                    item = item,
                    onSelectionChange = { selected -> onItemSelectionChange(item, selected) },
                    onQuantityChange = { quantity -> onQuantityChange(item, quantity) },
                    onAutoFill = { onAutoFill(item) }
                )
            }
        }

        // Complete Button
        Button(
            onClick = onCompleteFulfillment,
            enabled = fulfillmentItems.any { it.isSelected && it.fulfillQuantity > 0 },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Complete Fulfillment", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FulfillmentItemCard(
    item: FulfillmentItem,
    onSelectionChange: (Boolean) -> Unit,
    onQuantityChange: (Double) -> Unit,
    onAutoFill: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) Color(0xFF667eea).copy(alpha = 0.1f) else Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isSelected,
                    onCheckedChange = onSelectionChange,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF667eea))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.indentItem.items?.name ?: "Unknown Item",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Requested: ${item.indentItem.requestedQuantity} ${item.indentItem.unitOfMeasure}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Available: ${item.availableStock} ${item.indentItem.unitOfMeasure}",
                        fontSize = 12.sp,
                        color = if (item.availableStock >= item.indentItem.requestedQuantity) Color(0xFF4CAF50) else Color(0xFFE91E63)
                    )
                }
            }

            if (item.isSelected) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (item.fulfillQuantity == 0.0) "" else item.fulfillQuantity.toString(),
                        onValueChange = { value ->
                            val quantity = value.toDoubleOrNull() ?: 0.0
                            onQuantityChange(quantity)
                        },
                        label = { Text("Fulfill Quantity", color = Color(0xFF666666)) },
                        suffix = { Text(item.indentItem.unitOfMeasure, color = Color(0xFF666666)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        ),
                        isError = item.fulfillQuantity > item.availableStock
                    )

                    Button(
                        onClick = onAutoFill,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667eea)),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Fill", color = Color.White)
                    }
                }

                if (item.fulfillQuantity > item.availableStock) {
                    Text(
                        text = "Quantity exceeds available stock",
                        color = Color(0xFFE91E63),
                        fontSize = 12.sp
                    )
                }

                if (item.fulfillQuantity > item.indentItem.requestedQuantity) {
                    Text(
                        text = "Quantity exceeds requested amount",
                        color = Color(0xFFFF9800),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}