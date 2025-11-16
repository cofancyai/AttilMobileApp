package com.attil.inventory.presentation.transaction.indent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.transaction.ItemForIndentSelection
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentCreationScreen(
    onBackClick: () -> Unit,
    chefId: String,
    viewModel: IndentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                1 -> IndentDetailsAndItemSelectionStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNext = { viewModel.proceedToStep(2) }
                )
                2 -> QuantityEntryStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    onBack = { viewModel.proceedToStep(1) },
                    onNext = { viewModel.proceedToStep(3) }
                )
                3 -> ReviewAndSubmitStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    chefId = chefId,
                    onBack = { viewModel.proceedToStep(2) },
                    onSubmit = { 
                        viewModel.createIndent(chefId)
                        viewModel.proceedToStep(4)
                    }
                )
                4 -> ConfirmationStep(
                    uiState = uiState,
                    onCreateAnother = { 
                        viewModel.proceedToStep(1)
                    },
                    onFinish = onBackClick
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndentDetailsAndItemSelectionStep(
    uiState: IndentUiState,
    viewModel: IndentViewModel,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Step Progress
            StepProgressIndicator(currentStep = 1, totalSteps = 4)
        }

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

                    // Required Date with Date Picker
                    var showDatePicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState()

                    OutlinedTextField(
                        value = uiState.requiredDate,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Required Date", color = Color(0xFF666666)) },
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
                                    Text("OK", color = Color(0xFF667eea))
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
                                    todayDateBorderColor = Color(0xFF667eea)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Required Time
                    OutlinedTextField(
                        value = uiState.requiredTime,
                        onValueChange = viewModel::setRequiredTime,
                        label = { Text("Required Time", color = Color(0xFF666666)) },
                        placeholder = { Text("HH:MM", color = Color(0xFF999999)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Priority Selection
                    var priorityExpanded by remember { mutableStateOf(false) }
                    val priorities = listOf("High", "Medium", "Low")
                    
                    Box {
                        OutlinedTextField(
                            value = uiState.priority,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Priority", color = Color(0xFF666666)) },
                            trailingIcon = {
                                IconButton(onClick = { priorityExpanded = !priorityExpanded }) {
                                    Icon(
                                        if (priorityExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Priority",
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
                        
                        DropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            priorities.forEach { priority ->
                                DropdownMenuItem(
                                    text = { Text(priority, color = Color(0xFF333333)) },
                                    onClick = {
                                        viewModel.setPriority(priority)
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cuisine Selection
                    var cuisineExpanded by remember { mutableStateOf(false) }
                    
                    Box {
                        OutlinedTextField(
                            value = uiState.selectedCuisine?.name ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Cuisine", color = Color(0xFF666666)) },
                            trailingIcon = {
                                IconButton(onClick = { cuisineExpanded = !cuisineExpanded }) {
                                    Icon(
                                        if (cuisineExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Cuisine",
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
                        
                        DropdownMenu(
                            expanded = cuisineExpanded,
                            onDismissRequest = { cuisineExpanded = false }
                        ) {
                            uiState.cuisines.forEach { cuisine ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(cuisine.name, color = Color(0xFF333333))
                                            cuisine.description?.let { desc ->
                                                Text(
                                                    text = desc,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF666666)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectCuisine(cuisine)
                                        cuisineExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Purpose
                    OutlinedTextField(
                        value = uiState.purpose,
                        onValueChange = viewModel::setPurpose,
                        label = { Text("Purpose", color = Color(0xFF666666)) },
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

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::searchItems,
                        label = { Text("Search items...", color = Color(0xFF666666)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF667eea))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter
                    val categories = viewModel.getUniqueCategories()
                    if (categories.isNotEmpty()) {
                        var categoryExpanded by remember { mutableStateOf(false) }
                        
                        Box {
                            OutlinedTextField(
                                value = uiState.selectedCategoryFilter.ifEmpty { "All Categories" },
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Filter by Category", color = Color(0xFF666666)) },
                                trailingIcon = {
                                    IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                                        Icon(
                                            if (categoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Category",
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
                            
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Categories", color = Color(0xFF333333)) },
                                    onClick = {
                                        viewModel.filterByCategory("")
                                        categoryExpanded = false
                                    }
                                )
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category, color = Color(0xFF333333)) },
                                        onClick = {
                                            viewModel.filterByCategory(category)
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
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
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
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
            // Next Button
            Button(
                onClick = onNext,
                enabled = uiState.totalSelectedItems > 0 && 
                         uiState.requiredDate.isNotEmpty() && 
                         uiState.requiredTime.isNotEmpty() && 
                         uiState.priority.isNotEmpty() && 
                         uiState.purpose.isNotEmpty() && 
                         uiState.selectedCuisine != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Next: Set Quantities", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuantityEntryStep(
    uiState: IndentUiState,
    viewModel: IndentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepProgressIndicator(currentStep = 2, totalSteps = 4)
        }

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
                        text = "Set Quantities",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Enter required quantities for selected items",
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
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back", color = Color(0xFF667eea))
                }
                
                Button(
                    onClick = onNext,
                    enabled = uiState.selectedItems.all { it.requestedQuantity > 0 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Next: Review", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewAndSubmitStep(
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
            StepProgressIndicator(currentStep = 3, totalSteps = 4)
        }

        item {
            // Indent Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Review Indent",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Indent Details
                    IndentDetailRow("Cuisine", uiState.selectedCuisine?.name ?: "")
                    IndentDetailRow("Required Date", uiState.requiredDate)
                    IndentDetailRow("Required Time", uiState.requiredTime)
                    IndentDetailRow("Priority", uiState.priority)
                    IndentDetailRow("Purpose", uiState.purpose)
                    if (uiState.notes.isNotEmpty()) {
                        IndentDetailRow("Notes", uiState.notes)
                    }
                    IndentDetailRow("Total Items", "${uiState.selectedItems.size}")
                }
            }
        }

        item {
            // Items Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Items Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        items(uiState.selectedItems) { item ->
            ReviewItemCard(item = item)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back", color = Color(0xFF667eea))
                }
                
                Button(
                    onClick = onSubmit,
                    enabled = !uiState.isCreating,
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
private fun ConfirmationStep(
    uiState: IndentUiState,
    onCreateAnother: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Indent Created Successfully!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
                
                uiState.createdIndent?.let { indent ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Indent ID: ${indent.id?.take(12)}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Status: ${indent.status}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCreateAnother,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Another", color = Color(0xFF667eea))
                    }
                    
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Finish", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepProgressIndicator(currentStep: Int, totalSteps: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { step ->
                val stepNumber = step + 1
                val isCompleted = stepNumber < currentStep
                val isCurrent = stepNumber == currentStep
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = when {
                                    isCompleted -> Color(0xFF4CAF50)
                                    isCurrent -> Color(0xFF667eea)
                                    else -> Color(0xFFE0E0E0)
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = stepNumber.toString(),
                                color = if (isCurrent) Color.White else Color(0xFF666666),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    if (step < totalSteps - 1) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(
                                    color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                                )
                        )
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
    onQuantityChange: (String, Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = if (item.requestedQuantity == 0.0) "" else item.requestedQuantity.toString(),
                onValueChange = { value ->
                    val quantity = value.toDoubleOrNull() ?: 0.0
                    onQuantityChange(item.item.id, quantity)
                },
                label = { Text("Required Quantity", color = Color(0xFF666666)) },
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
                Text(
                    text = "Requested quantity exceeds available stock",
                    color = Color(0xFFE91E63),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewItemCard(item: ItemForIndentSelection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.requestedQuantity} ${item.item.unitOfMeasure}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                val status = when {
                    item.requestedQuantity <= item.availableStock -> "✅ Available"
                    item.availableStock > 0 -> "⚠️ Partial"
                    else -> "❌ Out of Stock"
                }
                
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = when {
                        item.requestedQuantity <= item.availableStock -> Color(0xFF4CAF50)
                        item.availableStock > 0 -> Color(0xFFFF9800)
                        else -> Color(0xFFE91E63)
                    }
                )
            }
        }
    }
}

@Composable
private fun IndentDetailRow(label: String, value: String) {
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