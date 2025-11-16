package com.attil.inventory.presentation.transaction.outward

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attil.inventory.data.model.management.Cuisine
import com.attil.inventory.data.model.transaction.OutwardItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OutwardItemDialog(
    item: OutwardItem,
    cuisines: List<Cuisine>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String, String?) -> Unit,
    isLoading: Boolean
) {
    var selectedCuisineId by remember { mutableStateOf(item.cuisineId ?: "") }
    var outwardQuantity by remember { mutableStateOf(item.outwardQuantity.toString()) }
    var usageDate by remember { mutableStateOf(item.usageDate) }
    var cuisineType by remember { mutableStateOf(item.cuisineType ?: "") }
    var notes by remember { mutableStateOf(item.notes ?: "") }

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
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
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
                                text = "Edit Outward Item",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.items?.name ?: "Unknown Item",
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

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item Info (Read-only)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Item Information",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Item Name",
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = item.items?.name ?: "Unknown",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF333333)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Unit",
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = item.items?.unitOfMeasure ?: "Unknown",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF333333)
                                    )
                                }
                            }
                        }
                    }

                    // Quantity
                    OutlinedTextField(
                        value = outwardQuantity,
                        onValueChange = { outwardQuantity = it },
                        label = { Text("Outward Quantity *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text(item.items?.unitOfMeasure ?: "", fontSize = 12.sp) },
                        isError = outwardQuantity.toDoubleOrNull() == null || outwardQuantity.toDoubleOrNull()!! <= 0,
                        supportingText = {
                            if (outwardQuantity.toDoubleOrNull() == null || outwardQuantity.toDoubleOrNull()!! <= 0) {
                                Text(
                                    text = "Please enter a valid quantity",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea)
                        )
                    )

                    // Date
                    OutlinedTextField(
                        value = usageDate,
                        onValueChange = { usageDate = it },
                        label = { Text("Usage Date *") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("YYYY-MM-DD") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea)
                        )
                    )

                    // Cuisine Type
                    OutlinedTextField(
                        value = cuisineType,
                        onValueChange = { cuisineType = it },
                        label = { Text("Cuisine Type") },
                        placeholder = { Text("e.g., Breakfast, Lunch, Dinner") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea)
                        )
                    )

                    // Cuisine Selection
                    Column {
                        Text(
                            text = "Cuisine",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                        
                        var expanded by remember { mutableStateOf(false) }
                        val selectedCuisine = cuisines.find { it.id == selectedCuisineId }
                        
                        Box {
                            OutlinedTextField(
                                value = selectedCuisine?.name ?: "None",
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
                                    text = { Text("None") },
                                    onClick = {
                                        selectedCuisineId = ""
                                        expanded = false
                                    }
                                )
                                cuisines.forEach { cuisine ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(cuisine.name)
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
                                            selectedCuisineId = cuisine.id!!
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea)
                        )
                    )
                }

                // Action Buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                val quantity = outwardQuantity.toDoubleOrNull()
                                if (quantity != null && quantity > 0 && usageDate.isNotEmpty()) {
                                    onSave(
                                        item.itemId,
                                        quantity,
                                        usageDate,
                                        cuisineType,
                                        notes,
                                        selectedCuisineId.takeIf { it.isNotEmpty() }
                                    )
                                }
                            },
                            enabled = !isLoading && 
                                     outwardQuantity.toDoubleOrNull()?.let { it > 0 } == true &&
                                     usageDate.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Update", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}