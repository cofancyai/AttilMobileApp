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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.attil.inventory.data.model.transaction.ItemForIndentSelection
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentCreationScreen(
    onBackClick: () -> Unit,
    chefId: String,
    viewModel: IndentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val premiumGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2)
        )
    )

    // Handle successful indent creation
    LaunchedEffect(uiState.createdIndent) {
        if (uiState.createdIndent != null && !uiState.isCreating) {
            snackbarHostState.showSnackbar(
                message = "Indent created successfully",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(1000)
            onBackClick() // Redirect to dashboard
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(premiumGradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Create Indent",
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                when (uiState.currentStep) {
                    1 -> ItemSelectionStep(
                        uiState = uiState,
                        viewModel = viewModel,
                        chefId = chefId,
                        onNext = { viewModel.proceedToStep(2) }
                    )
                    2 -> QuantityEntryStep(
                        uiState = uiState,
                        viewModel = viewModel,
                        chefId = chefId,
                        onBack = { viewModel.proceedToStep(1) },
                        onSubmit = { viewModel.createIndent(chefId) }
                    )
                }
            }
        }

        // Error Dialog
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemSelectionStep(
    uiState: IndentUiState,
    viewModel: IndentViewModel,
    chefId: String,
    onNext: () -> Unit
) {
    // Get current date
    val currentDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Indent Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Indent Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Date (Display Only)
                    OutlinedTextField(
                        value = currentDate,
                        onValueChange = { },
                        readOnly = true,
                        enabled = false,
                        label = { Text("Current Date", color = Color(0xFF666666)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Current Date",
                                tint = Color(0xFF667eea)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color(0xFFE0E0E0),
                            disabledTextColor = Color(0xFF333333),
                            disabledLabelColor = Color(0xFF666666)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Required Date with Date Picker
                    var showDatePicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState()

                    OutlinedTextField(
                        value = uiState.requiredDate,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Required Date *", color = Color(0xFF666666)) },
                        placeholder = { Text("Select Date", color = Color(0xFF999999)) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Select Date",
                                    tint = Color(0xFF667eea)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        )
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = Instant.ofEpochMilli(millis)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                            viewModel.setRequiredDate(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                        }
                                        showDatePicker = false
                                    }
                                ) {
                                    Text("OK", color = Color(0xFF667eea), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel", color = Color(0xFF666666))
                                }
                            },
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.White
                            )
                        ) {
                            DatePicker(
                                state = datePickerState,
                                colors = DatePickerDefaults.colors(
                                    containerColor = Color.White,
                                    titleContentColor = Color(0xFF333333),
                                    headlineContentColor = Color(0xFF333333),
                                    weekdayContentColor = Color(0xFF666666),
                                    subheadContentColor = Color(0xFF666666),
                                    yearContentColor = Color(0xFF333333),
                                    currentYearContentColor = Color(0xFF667eea),
                                    selectedYearContentColor = Color.White,
                                    selectedYearContainerColor = Color(0xFF667eea),
                                    dayContentColor = Color(0xFF333333),
                                    selectedDayContentColor = Color.White,
                                    selectedDayContainerColor = Color(0xFF667eea),
                                    todayContentColor = Color(0xFF667eea),
                                    todayDateBorderColor = Color(0xFF667eea),
                                    disabledDayContentColor = Color(0xFFBDBDBD)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Purpose
                    OutlinedTextField(
                        value = uiState.purpose,
                        onValueChange = viewModel::setPurpose,
                        label = { Text("Purpose *", color = Color(0xFF666666)) },
                        placeholder = { Text("Purpose of this indent", color = Color(0xFF999999)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notes
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::setNotes,
                        label = { Text("Notes (Optional)", color = Color(0xFF666666)) },
                        placeholder = { Text("Additional notes...", color = Color(0xFF999999)) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        )
                    )
                }
            }
        }

        item {
            // Item Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Text(
                        text = "Selected: ${uiState.totalSelectedItems} items",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Filter
                    var categoryDropdownExpanded by remember { mutableStateOf(false) }
                    val availableCategories = remember(uiState.availableItems) {
                        uiState.availableItems
                            .mapNotNull { it.item.categories?.name }
                            .distinct()
                            .sorted()
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (uiState.selectedCategoryFilter.isEmpty()) "All Categories" else uiState.selectedCategoryFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category Filter", color = Color(0xFF666666)) },
                            leadingIcon = {
                                Icon(Icons.Default.FilterList, contentDescription = "Category", tint = Color(0xFF667eea))
                            },
                            trailingIcon = {
                                Row {
                                    if (uiState.selectedCategoryFilter.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.filterByCategory("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear filter", tint = Color(0xFF666666))
                                        }
                                    }
                                    IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                        Icon(
                                            if (categoryDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = Color(0xFF667eea)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF667eea),
                                focusedTextColor = Color(0xFF333333),
                                unfocusedTextColor = Color(0xFF333333)
                            )
                        )

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Categories", color = Color(0xFF333333)) },
                                onClick = {
                                    viewModel.filterByCategory("")
                                    categoryDropdownExpanded = false
                                }
                            )
                            availableCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = Color(0xFF333333)) },
                                    onClick = {
                                        viewModel.filterByCategory(category)
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::searchItems,
                        label = { Text("Search items...", color = Color(0xFF666666)) },
                        placeholder = { Text("Type to search", color = Color(0xFF999999)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF667eea))
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchItems("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF666666))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        )
                    )
                }
            }
        }

        // Items List
        val itemsToShow = if (uiState.searchQuery.isNotEmpty()) {
            uiState.filteredItems
        } else {
            uiState.availableItems
        }

        if (uiState.isLoadingItems) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        } else if (itemsToShow.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "No items",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No items found",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
        } else {
            items(itemsToShow) { item ->
                ItemSelectionCard(
                    item = item,
                    onToggleSelection = { viewModel.toggleItemSelection(it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Next Button
            Button(
                onClick = onNext,
                enabled = uiState.totalSelectedItems > 0 &&
                         uiState.requiredDate.isNotEmpty() &&
                         uiState.purpose.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Next: Enter Quantities", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuantityEntryStep(
    uiState: IndentUiState,
    viewModel: IndentViewModel,
    chefId: String,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Enter Quantities",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Enter required quantities for ${uiState.selectedItems.size} selected items",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        items(uiState.selectedItems) { item ->
            QuantityEntryCard(
                item = item,
                onQuantityChange = { itemId, quantity ->
                    viewModel.updateItemQuantity(itemId, quantity)
                },
                onRemove = {
                    viewModel.removeSelectedItem(item.item.id)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back", color = Color.White)
                }

                Button(
                    onClick = onSubmit,
                    enabled = uiState.selectedItems.all { it.requestedQuantity > 0 } && !uiState.isCreating,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit Indent", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemSelectionCard(
    item: ItemForIndentSelection,
    onToggleSelection: (ItemForIndentSelection) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onToggleSelection(item) },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected)
                Color(0xFF667eea).copy(alpha = 0.1f) else Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggleSelection(item) },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF667eea))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.item.name,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Category: ${item.item.categories?.name ?: "N/A"}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "Available: ${item.availableStock} ${item.item.unitOfMeasure}",
                    fontSize = 12.sp,
                    color = if (item.availableStock > 0) Color(0xFF4CAF50) else Color(0xFFE91E63)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuantityEntryCard(
    item: ItemForIndentSelection,
    onQuantityChange: (String, Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.item.name,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Available: ${item.availableStock} ${item.item.unitOfMeasure}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFE91E63)
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color(0xFFE91E63)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = if (item.requestedQuantity == 0.0) "" else item.requestedQuantity.toString(),
                onValueChange = { value ->
                    val quantity = value.toDoubleOrNull() ?: 0.0
                    onQuantityChange(item.item.id, quantity)
                },
                label = { Text("Required Quantity *", color = Color(0xFF666666)) },
                suffix = { Text(item.item.unitOfMeasure, color = Color(0xFF666666)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF667eea),
                    focusedTextColor = Color(0xFF333333),
                    unfocusedTextColor = Color(0xFF333333)
                ),
                isError = item.requestedQuantity > item.availableStock
            )

            if (item.requestedQuantity > item.availableStock) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ Requested quantity exceeds available stock",
                    color = Color(0xFFE91E63),
                    fontSize = 12.sp
                )
            }
        }
    }
}
