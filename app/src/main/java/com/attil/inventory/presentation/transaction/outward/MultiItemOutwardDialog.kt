package com.attil.inventory.presentation.transaction.outward

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.transaction.ItemWithStock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiItemOutwardDialog(
    availableItems: List<ItemWithStock>,
    selectedItems: List<ItemWithStock>,
    cuisines: List<Cuisine>,
    selectedCuisine: Cuisine?,
    categories: List<String>,
    selectedCategory: String,
    cuisineType: String,
    isLoadingItems: Boolean,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onCuisineSelected: (Cuisine) -> Unit,
    onCuisineTypeChange: (String) -> Unit,
    onItemToggle: (ItemWithStock) -> Unit,
    onQuantityChange: (String, Double) -> Unit,
    onSave: (String) -> Unit,
    isLoading: Boolean
) {
    var currentStep by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF667eea)),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Add Outward Items",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Step $currentStep of 3",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Progress Indicator
                LinearProgressIndicator(
                    progress = currentStep / 3f,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF667eea),
                    trackColor = Color(0xFFE0E0E0)
                )

                // Content based on current step
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp)
                ) {
                    when (currentStep) {
                        1 -> Step1CategoryAndCuisine(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            cuisines = cuisines,
                            selectedCuisine = selectedCuisine,
                            cuisineType = cuisineType,
                            onCategorySelected = onCategorySelected,
                            onCuisineSelected = onCuisineSelected,
                            onCuisineTypeChange = onCuisineTypeChange
                        )
                        2 -> Step2ItemSelection(
                            availableItems = availableItems,
                            selectedItems = selectedItems,
                            isLoadingItems = isLoadingItems,
                            onItemToggle = onItemToggle,
                            onQuantityChange = onQuantityChange
                        )
                        3 -> Step3AdditionalDetails(
                            notes = notes,
                            onNotesChange = { notes = it }
                        )
                    }
                }

                // Navigation Buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.width(100.dp)
                            ) {
                                Text("Previous")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(100.dp))
                        }

                        if (currentStep < 3) {
                            Button(
                                onClick = {
                                    if (canProceedToNextStep(currentStep, selectedItems)) {
                                        currentStep++
                                    }
                                },
                                enabled = canProceedToNextStep(currentStep, selectedItems),
                                modifier = Modifier.width(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667eea))
                            ) {
                                Text("Next", color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onSave(notes)
                                },
                                enabled = !isLoading && selectedItems.any { it.outwardQuantity > 0 },
                                modifier = Modifier.width(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Save", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1CategoryAndCuisine(
    categories: List<String>,
    selectedCategory: String,
    cuisines: List<Cuisine>,
    selectedCuisine: Cuisine?,
    cuisineType: String,
    onCategorySelected: (String) -> Unit,
    onCuisineSelected: (Cuisine) -> Unit,
    onCuisineTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Step 1: Select Category & Cuisine",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        // Category Filter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Filter by Category (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedTextField(
                        value = selectedCategory.ifEmpty { "All Categories" },
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Dropdown"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea)
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                onCategorySelected("")
                                expanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    onCategorySelected(category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Cuisine Type
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Cuisine Type (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cuisineType,
                    onValueChange = onCuisineTypeChange,
                    placeholder = { Text("e.g., Breakfast, Lunch, Dinner") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea)
                    )
                )
            }
        }

        // Cuisine Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Cuisine (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onCuisineSelected(Cuisine("", "None", null, true, null, null)) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCuisine == null)
                                    Color(0xFF667eea).copy(alpha = 0.1f) else Color.White
                            ),
                            border = if (selectedCuisine == null)
                                androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF667eea)) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCuisine == null,
                                    onClick = { onCuisineSelected(Cuisine("", "None", null, true, null, null)) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF667eea)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "None",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    }

                    items(count = cuisines.size) { index ->
                        val cuisine = cuisines[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onCuisineSelected(cuisine) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCuisine?.id == cuisine.id)
                                    Color(0xFF667eea).copy(alpha = 0.1f) else Color.White
                            ),
                            border = if (selectedCuisine?.id == cuisine.id)
                                androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF667eea)) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCuisine?.id == cuisine.id,
                                    onClick = { onCuisineSelected(cuisine) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF667eea)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = cuisine.name,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF333333)
                                    )
                                    cuisine.description?.let { desc ->
                                        Text(
                                            text = desc,
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2ItemSelection(
    availableItems: List<ItemWithStock>,
    selectedItems: List<ItemWithStock>,
    isLoadingItems: Boolean,
    onItemToggle: (ItemWithStock) -> Unit,
    onQuantityChange: (String, Double) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step 2: Select Items",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                text = "${selectedItems.size} selected",
                fontSize = 14.sp,
                color = Color(0xFF667eea),
                fontWeight = FontWeight.Medium
            )
        }

        if (isLoadingItems) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF667eea))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(count = availableItems.size) { index ->
                    val itemWithStock = availableItems[index]
                    ItemSelectionCard(
                        itemWithStock = itemWithStock,
                        onToggle = { onItemToggle(itemWithStock) },
                        onQuantityChange = { quantity ->
                            onQuantityChange(itemWithStock.item.id, quantity)
                        }
                    )
                }

                if (availableItems.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
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
                                    text = "No items available",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = "All items are out of stock",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectionCard(
    itemWithStock: ItemWithStock,
    onToggle: () -> Unit,
    onQuantityChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (itemWithStock.isSelected)
                Color(0xFF667eea).copy(alpha = 0.1f) else Color.White
        ),
        border = if (itemWithStock.isSelected)
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF667eea)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = itemWithStock.isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF667eea)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemWithStock.item.name,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "${itemWithStock.item.categories?.name ?: "Unknown"} • Available: ${itemWithStock.currentStock.toInt()} ${itemWithStock.item.unitOfMeasure}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            if (itemWithStock.isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity:",
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.width(80.dp)
                    )
                    OutlinedTextField(
                        value = if (itemWithStock.outwardQuantity > 0) itemWithStock.outwardQuantity.toString() else "",
                        onValueChange = { value ->
                            val quantity = value.toDoubleOrNull() ?: 0.0
                            onQuantityChange(quantity)
                        },
                        placeholder = { Text("Enter quantity", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea)
                        ),
                        suffix = { Text(itemWithStock.item.unitOfMeasure, fontSize = 12.sp) }
                    )
                }

                if (itemWithStock.outwardQuantity > itemWithStock.currentStock) {
                    Text(
                        text = "⚠️ Quantity exceeds available stock",
                        fontSize = 12.sp,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3AdditionalDetails(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Step 3: Additional Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea)
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ℹ️ Note",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1976D2)
                )
                Text(
                    text = "All selected items will be processed with the same cuisine and details. The current stock will be automatically updated after saving.",
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun canProceedToNextStep(
    currentStep: Int,
    selectedItems: List<ItemWithStock>
): Boolean {
    return when (currentStep) {
        1 -> true
        2 -> selectedItems.any { it.outwardQuantity > 0 && it.outwardQuantity <= it.currentStock }
        else -> true
    }
}