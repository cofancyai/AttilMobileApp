package com.attil.inventory.presentation.management.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDialog(
    item: Item?,
    categories: List<Category>,
    godowns: List<Godown>,
    racks: List<Rack>,
    cuisines: List<Cuisine>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?, String, Double, List<String>) -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var selectedCategoryId by remember { mutableStateOf(item?.categoryId ?: "") }
    var selectedGodownId by remember { mutableStateOf(item?.godownId ?: "") }
    var selectedRackId by remember { mutableStateOf(item?.rackId ?: "") }
    var unitOfMeasure by remember { mutableStateOf(item?.unitOfMeasure ?: "kg") }
    var minimumStockLevel by remember { mutableStateOf(item?.minimumStockLevel?.toString() ?: "10") }
    var selectedCuisineIds by remember { mutableStateOf<List<String>>(
        item?.itemCuisines?.map { it.cuisineId } ?: emptyList()
    ) }

    val unitOptions = listOf("kg", "liters", "pieces", "packets", "grams")
    val filteredRacks = if (selectedGodownId.isNotEmpty()) {
        racks.filter { it.godownId == selectedGodownId }
    } else {
        emptyList()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
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
                    text = if (item == null) "Add New Item" else "Edit Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
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

                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category",
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
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name, color = Color(0xFF333333)) },
                                onClick = {
                                    selectedCategoryId = category.id!!
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Godown Dropdown
                var godownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = godownExpanded,
                    onExpandedChange = { godownExpanded = !godownExpanded }
                ) {
                    OutlinedTextField(
                        value = godowns.find { it.id == selectedGodownId }?.name ?: "Select Godown (Optional)",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Godown") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = godownExpanded) },
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
                        expanded = godownExpanded,
                        onDismissRequest = { godownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None", color = Color(0xFF333333)) },
                            onClick = {
                                selectedGodownId = ""
                                selectedRackId = ""
                                godownExpanded = false
                            }
                        )
                        godowns.forEach { godown ->
                            DropdownMenuItem(
                                text = { Text(godown.name, color = Color(0xFF333333)) },
                                onClick = {
                                    selectedGodownId = godown.id!!
                                    selectedRackId = ""
                                    godownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rack Dropdown
                if (filteredRacks.isNotEmpty()) {
                    var rackExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = rackExpanded,
                        onExpandedChange = { rackExpanded = !rackExpanded }
                    ) {
                        OutlinedTextField(
                            value = filteredRacks.find { it.id == selectedRackId }?.name ?: "Select Rack (Optional)",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Rack") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rackExpanded) },
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
                            expanded = rackExpanded,
                            onDismissRequest = { rackExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None", color = Color(0xFF333333)) },
                                onClick = {
                                    selectedRackId = ""
                                    rackExpanded = false
                                }
                            )
                            filteredRacks.forEach { rack ->
                                DropdownMenuItem(
                                    text = { Text(rack.name, color = Color(0xFF333333)) },
                                    onClick = {
                                        selectedRackId = rack.id!!
                                        rackExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Unit of Measure Dropdown
                var unitExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = unitOfMeasure,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Unit of Measure") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
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
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        unitOptions.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit, color = Color(0xFF333333)) },
                                onClick = {
                                    unitOfMeasure = unit
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Minimum Stock Level
                OutlinedTextField(
                    value = minimumStockLevel,
                    onValueChange = { minimumStockLevel = it },
                    label = { Text("Minimum Stock Level") },
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

                // Cuisines Selection
                Text(
                    text = "Select Cuisines (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    cuisines.forEach { cuisine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedCuisineIds.contains(cuisine.id),
                                onCheckedChange = { isChecked ->
                                    selectedCuisineIds = if (isChecked) {
                                        selectedCuisineIds + cuisine.id!!
                                    } else {
                                        selectedCuisineIds - cuisine.id!!
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF667eea)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cuisine.name,
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
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
                            if (name.isNotBlank() && selectedCategoryId.isNotEmpty()) {
                                val stockLevel = minimumStockLevel.toDoubleOrNull() ?: 10.0
                                onSave(
                                    name,
                                    selectedCategoryId,
                                    selectedGodownId.takeIf { it.isNotEmpty() },
                                    selectedRackId.takeIf { it.isNotEmpty() },
                                    unitOfMeasure,
                                    stockLevel,
                                    selectedCuisineIds
                                )
                            }
                        },
                        enabled = name.isNotBlank() && selectedCategoryId.isNotEmpty() && !isLoading,
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
}