package com.attil.inventory.presentation.transaction.inward

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.attil.inventory.data.model.management.*
import com.attil.inventory.data.model.transaction.InwardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwardItemDialog(
    item: InwardItem?,
    categories: List<Category>,
    items: List<Item>,
    cuisines: List<Cuisine>,
    vendors: List<Vendor>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?, String, Double, Double, Double?, Double?, Double?, String?, String?, String?) -> Unit,
    isLoading: Boolean
) {
    var selectedVendorId by remember { mutableStateOf(item?.vendorName ?: "") }
    var selectedCategoryId by remember { mutableStateOf(item?.items?.categoryId ?: "") }
    var selectedItemId by remember { mutableStateOf(item?.itemId ?: "") }
    var itemSearchQuery by remember { mutableStateOf("") }
    var showItemDropdown by remember { mutableStateOf(false) }
    var inwardQuantity by remember { mutableStateOf(item?.inwardQuantity?.toString() ?: "") }
    var selectedUnit by remember { mutableStateOf(item?.items?.unitOfMeasure ?: "kg") }
    var purchaseDate by remember { mutableStateOf(item?.purchaseDate ?: java.time.LocalDate.now().toString()) }
    var taxType by remember { mutableStateOf(if (item?.gstPercentage != null && item.gstPercentage > 0) "With Tax" else "Without Tax") }
    var gstPercentage by remember { mutableStateOf(item?.gstPercentage?.toString() ?: "18") }
    var pricePerUnit by remember { mutableStateOf(item?.pricePerUnit?.toString() ?: "") }
    var finalPrice by remember { mutableStateOf("") }
    var billNumber by remember { mutableStateOf(item?.billNumber ?: "") }
    var expiryDate by remember { mutableStateOf(item?.expiryDate ?: "") }
    var selectedCuisineId by remember { mutableStateOf(item?.cuisineId ?: "") }

    val filteredItems = if (selectedCategoryId.isNotEmpty()) {
        items.filter { it.categoryId == selectedCategoryId }
    } else {
        items
    }

    val selectedItem = items.find { it.id == selectedItemId }
    val itemCuisines = selectedItem?.itemCuisines?.mapNotNull { it.cuisines } ?: emptyList()
    val selectedVendor = vendors.find { it.id == selectedVendorId }

    // Update selected unit when item changes
    LaunchedEffect(selectedItemId) {
        selectedItem?.let { item ->
            selectedUnit = item.unitOfMeasure
        }
    }

    // Auto-calculate final price
    LaunchedEffect(pricePerUnit, inwardQuantity, taxType, gstPercentage) {
        if (pricePerUnit.isNotEmpty() && inwardQuantity.isNotEmpty()) {
            try {
                val baseAmount = pricePerUnit.toDouble() * inwardQuantity.toDouble()
                when (taxType) {
                    "With Tax" -> {
                        val gst = gstPercentage.toDoubleOrNull() ?: 18.0
                        val gstAmount = (baseAmount * gst) / 100
                        finalPrice = String.format("%.2f", baseAmount + gstAmount)
                    }
                    "Without Tax" -> {
                        finalPrice = String.format("%.2f", baseAmount)
                    }
                }
            } catch (e: Exception) {
                finalPrice = ""
            }
        } else {
            finalPrice = ""
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (item == null) "Add Purchase Record" else "Edit Purchase Record",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vendor Selection
                var vendorExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = vendorExpanded,
                    onExpandedChange = { vendorExpanded = !vendorExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedVendor?.name ?: "Select Vendor",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Vendor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333),
                            unfocusedBorderColor = Color(0xFF999999),
                            unfocusedLabelColor = Color(0xFF666666)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = vendorExpanded,
                        onDismissRequest = { vendorExpanded = false }
                    ) {
                        vendors.forEach { vendor ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(vendor.name, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
                                        vendor.contactNumber?.let {
                                            Text(it, color = Color(0xFF666666), fontSize = 12.sp)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedVendorId = vendor.id!!
                                    vendorExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category (Optional)",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333),
                            unfocusedBorderColor = Color(0xFF999999),
                            unfocusedLabelColor = Color(0xFF666666)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories", color = Color(0xFF333333)) },
                            onClick = {
                                selectedCategoryId = ""
                                categoryExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name, color = Color(0xFF333333)) },
                                onClick = {
                                    selectedCategoryId = category.id!!
                                    selectedItemId = ""
                                    selectedCuisineId = ""
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item Selection with Search
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = selectedItem?.name ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Selected Item") },
                            placeholder = { Text("No item selected") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50),
                                focusedTextColor = Color(0xFF333333),
                                unfocusedTextColor = Color(0xFF333333),
                                unfocusedBorderColor = Color(0xFF4CAF50),
                                unfocusedLabelColor = Color(0xFF4CAF50)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showItemDropdown = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667eea)
                            )
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search Items")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Search")
                        }
                    }

                    // Show selected item details
                    selectedItem?.let { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8F9FA)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "Category: ${item.categories?.name ?: "Unknown"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = "Default Unit: ${item.unitOfMeasure}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = "Min Stock: ${item.minimumStockLevel}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity with Unit Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inwardQuantity,
                        onValueChange = { inwardQuantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea),
                            cursorColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333),
                            unfocusedBorderColor = Color(0xFF999999),
                            unfocusedLabelColor = Color(0xFF666666)
                        )
                    )

                    // Unit selection
                    var unitExpanded by remember { mutableStateOf(false) }
                    val unitOptions = listOf("kg", "grams", "liters", "ml", "pieces", "packets", "boxes", "bottles", "dozen", "tons")

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(0.6f)
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF667eea),
                                focusedLabelColor = Color(0xFF667eea),
                                focusedTextColor = Color(0xFF333333),
                                unfocusedTextColor = Color(0xFF333333),
                                unfocusedBorderColor = Color(0xFF999999),
                                unfocusedLabelColor = Color(0xFF666666)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            unitOptions.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit, color = Color(0xFF333333)) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Purchase Date
                OutlinedTextField(
                    value = purchaseDate,
                    onValueChange = { purchaseDate = it },
                    label = { Text("Purchase Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea),
                        cursorColor = Color(0xFF667eea),
                        focusedTextColor = Color(0xFF333333),
                        unfocusedTextColor = Color(0xFF333333),
                        unfocusedBorderColor = Color(0xFF999999),
                        unfocusedLabelColor = Color(0xFF666666)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tax Information
                var taxExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = taxExpanded,
                    onExpandedChange = { taxExpanded = !taxExpanded }
                ) {
                    OutlinedTextField(
                        value = taxType,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tax Information") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taxExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333),
                            unfocusedBorderColor = Color(0xFF999999),
                            unfocusedLabelColor = Color(0xFF666666)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = taxExpanded,
                        onDismissRequest = { taxExpanded = false }
                    ) {
                        listOf("With Tax", "Without Tax").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color(0xFF333333)) },
                                onClick = {
                                    taxType = option
                                    taxExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price per Unit
                OutlinedTextField(
                    value = pricePerUnit,
                    onValueChange = { pricePerUnit = it },
                    label = { Text("Price per $selectedUnit") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea),
                        cursorColor = Color(0xFF667eea),
                        focusedTextColor = Color(0xFF333333),
                        unfocusedTextColor = Color(0xFF333333),
                        unfocusedBorderColor = Color(0xFF999999),
                        unfocusedLabelColor = Color(0xFF666666)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GST Percentage (only show if "With Tax" is selected)
                if (taxType == "With Tax") {
                    OutlinedTextField(
                        value = gstPercentage,
                        onValueChange = { gstPercentage = it },
                        label = { Text("GST Percentage") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea),
                            cursorColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333),
                            unfocusedBorderColor = Color(0xFF999999),
                            unfocusedLabelColor = Color(0xFF666666)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Final Price (Auto-calculated, read-only)
                if (finalPrice.isNotEmpty()) {
                    OutlinedTextField(
                        value = "₹$finalPrice",
                        onValueChange = { },
                        label = { Text("Total Price") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedTextColor = Color(0xFF4CAF50),
                            unfocusedTextColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF4CAF50),
                            unfocusedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Bill Number (Optional)
                OutlinedTextField(
                    value = billNumber,
                    onValueChange = { billNumber = it },
                    label = { Text("Bill Number (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea),
                        cursorColor = Color(0xFF667eea),
                        focusedTextColor = Color(0xFF333333),
                        unfocusedTextColor = Color(0xFF333333),
                        unfocusedBorderColor = Color(0xFF999999),
                        unfocusedLabelColor = Color(0xFF666666)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Expiry Date (Optional)
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea),
                        cursorColor = Color(0xFF667eea),
                        focusedTextColor = Color(0xFF333333),
                        unfocusedTextColor = Color(0xFF333333),
                        unfocusedBorderColor = Color(0xFF999999),
                        unfocusedLabelColor = Color(0xFF666666)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cuisine Selection (if item has cuisines)
                if (itemCuisines.isNotEmpty()) {
                    var cuisineExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = cuisineExpanded,
                        onExpandedChange = { cuisineExpanded = !cuisineExpanded }
                    ) {
                        OutlinedTextField(
                            value = itemCuisines.find { it.id == selectedCuisineId }?.name ?: "Select Cuisine (Optional)",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Cuisine") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cuisineExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF667eea),
                                focusedLabelColor = Color(0xFF667eea),
                                focusedTextColor = Color(0xFF333333),
                                unfocusedTextColor = Color(0xFF333333),
                                unfocusedBorderColor = Color(0xFF999999),
                                unfocusedLabelColor = Color(0xFF666666)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = cuisineExpanded,
                            onDismissRequest = { cuisineExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None", color = Color(0xFF333333)) },
                                onClick = {
                                    selectedCuisineId = ""
                                    cuisineExpanded = false
                                }
                            )
                            itemCuisines.forEach { cuisine ->
                                DropdownMenuItem(
                                    text = { Text(cuisine.name, color = Color(0xFF333333)) },
                                    onClick = {
                                        selectedCuisineId = cuisine.id!!
                                        cuisineExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF666666))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedVendorId.isNotEmpty() && selectedItemId.isNotEmpty() &&
                                inwardQuantity.isNotBlank() && pricePerUnit.isNotBlank()) {
                                val quantity = inwardQuantity.toDoubleOrNull() ?: 0.0
                                val price = pricePerUnit.toDoubleOrNull() ?: 0.0
                                val baseAmount = price * quantity

                                val (withoutGst, withGst, gst) = when (taxType) {
                                    "With Tax" -> {
                                        val gstPercent = gstPercentage.toDoubleOrNull() ?: 18.0
                                        val gstAmount = (baseAmount * gstPercent) / 100
                                        Triple(baseAmount, baseAmount + gstAmount, gstPercent)
                                    }
                                    else -> Triple(baseAmount, baseAmount, 0.0)
                                }

                                onSave(
                                    selectedItemId,
                                    selectedVendor?.name ?: "",
                                    selectedVendor?.contactNumber,
                                    selectedVendor?.address,
                                    purchaseDate,
                                    quantity,
                                    price,
                                    withoutGst,
                                    withGst,
                                    if (gst > 0) gst else null,
                                    billNumber.takeIf { it.isNotBlank() },
                                    expiryDate.takeIf { it.isNotBlank() },
                                    selectedCuisineId.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = selectedVendorId.isNotEmpty() && selectedItemId.isNotEmpty() &&
                                inwardQuantity.isNotBlank() && pricePerUnit.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (item == null) "Add" else "Update")
                        }
                    }
                }
            }
        }
    }

    // Item Search Dialog
    if (showItemDropdown) {
        Dialog(onDismissRequest = { showItemDropdown = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Search Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = itemSearchQuery,
                        onValueChange = { itemSearchQuery = it },
                        label = { Text("Type to search items...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (itemSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { itemSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF666666))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea),
                            cursorColor = Color(0xFF667eea),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333),
                            unfocusedBorderColor = Color(0xFF999999),
                            unfocusedLabelColor = Color(0xFF666666)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real-time search results
                    val displayItems = remember(itemSearchQuery, items) {
                        if (itemSearchQuery.isEmpty()) {
                            items.take(50) // Show first 50 items when no search
                        } else {
                            items.filter { item ->
                                item.name.contains(itemSearchQuery, ignoreCase = true) ||
                                        item.categories?.name?.contains(itemSearchQuery, ignoreCase = true) == true ||
                                        item.unitOfMeasure.contains(itemSearchQuery, ignoreCase = true)
                            }
                        }
                    }

                    // Search results count
                    Text(
                        text = when {
                            itemSearchQuery.isEmpty() -> "Showing ${displayItems.size} items"
                            displayItems.isEmpty() -> "No items found for '$itemSearchQuery'"
                            else -> "Found ${displayItems.size} items"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayItems) { itemObj ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    selectedItemId = itemObj.id!!
                                    selectedCuisineId = ""
                                    selectedUnit = itemObj.unitOfMeasure
                                    showItemDropdown = false
                                    itemSearchQuery = ""
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedItemId == itemObj.id) {
                                        Color(0xFF667eea).copy(alpha = 0.2f)
                                    } else {
                                        Color(0xFFF8F9FA)
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (selectedItemId == itemObj.id) 4.dp else 2.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = itemObj.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Category: ${itemObj.categories?.name ?: "Unknown"}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666)
                                        )
                                        Row {
                                            Text(
                                                text = "Unit: ${itemObj.unitOfMeasure}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF666666)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = "Min Stock: ${itemObj.minimumStockLevel}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF666666)
                                            )
                                        }
                                    }
                                    if (selectedItemId == itemObj.id) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = "Select",
                                            tint = Color(0xFF999999),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Show message if no items found
                        if (displayItems.isEmpty() && itemSearchQuery.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No items found",
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF333333)
                                        )
                                        Text(
                                            text = "Try different keywords",
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                itemSearchQuery = ""
                                showItemDropdown = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        if (selectedItemId.isNotEmpty()) {
                            Button(
                                onClick = { showItemDropdown = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Select Item")
                            }
                        }
                    }
                }
            }
        }
    }
}