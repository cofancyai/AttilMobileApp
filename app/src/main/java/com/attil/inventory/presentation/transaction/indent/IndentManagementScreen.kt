package com.attil.inventory.presentation.transaction.indent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.attil.inventory.data.model.transaction.Indent
import com.attil.inventory.data.model.transaction.VerificationItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndentManagementScreen(
    onBackClick: () -> Unit,
    onCreateIndent: () -> Unit,
    onFulfillIndent: () -> Unit,
    currentUserId: String,
    userRole: String, // "chef", "manager", "storekeeper"
    viewModel: IndentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showIndentDetails by remember { mutableStateOf<Indent?>(null) }

    LaunchedEffect(userRole, currentUserId, selectedStatusFilter) {
        when {
            userRole == "chef" -> {
                viewModel.loadIndentsByChef(currentUserId)
            }
            userRole == "manager" || userRole == "storekeeper" -> {
                viewModel.loadIndents()
            }
        }
    }

    val onUpdateStatus = { indentId: String, newStatus: String ->
        viewModel.updateIndentStatus(indentId, newStatus, currentUserId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1976D2)
                            )
                        }
                        Column {
                            Text(
                                text = "Indent Management",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                            Text(
                                text = when (userRole) {
                                    "chef" -> "View your indents"
                                    "manager" -> "Manage all indents"
                                    "storekeeper" -> "Fulfill approved indents"
                                    else -> "Indent management"
                                },
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    if (userRole == "chef") {
                        Button(
                            onClick = onCreateIndent,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Indent", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Filter
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedDate ?: "Date",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Filters
                    val statusOptions = listOf("Submitted", "Fulfilled", "Received")
                    statusOptions.forEach { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == status) "All" else status
                            },
                            label = {
                                Text(
                                    text = status,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1976D2),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (selectedDate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date: $selectedDate",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { selectedDate = null },
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Clear", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1976D2))
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = Color(0xFFE91E63),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.clearError() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
            else -> {
                val filteredIndents = uiState.indents.filter { indent ->
                    val statusMatch = selectedStatusFilter == "All" || indent.status == selectedStatusFilter
                    val dateMatch = selectedDate == null || indent.requiredDate == selectedDate
                    statusMatch && dateMatch
                }

                if (filteredIndents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "No Indents",
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No indents found",
                                fontSize = 18.sp,
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
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredIndents) { indent ->
                            IndentCard(
                                indent = indent,
                                userRole = userRole,
                                onClick = { showIndentDetails = indent }
                            )
                        }
                    }
                }
            }
        }
    }

    // Indent Details Dialog
    showIndentDetails?.let { indent ->
        IndentDetailsDialog(
            indent = indent,
            userRole = userRole,
            currentUserId = currentUserId,
            onDismiss = { showIndentDetails = null },
            onUpdateStatus = onUpdateStatus,
            onFulfillIndent = onFulfillIndent,
            onShowVerificationDialog = { viewModel.showVerificationDialog(indent) }
        )
    }

    // Verification Dialog
    if (uiState.showVerificationDialog) {
        showIndentDetails?.let { indent ->
            VerificationDialog(
                indent = indent,
                verificationItems = uiState.verificationItems,
                isVerifying = uiState.isVerifying,
                verificationError = uiState.verificationError,
                currentUserId = currentUserId,
                onDismiss = { viewModel.hideVerificationDialog() },
                onItemVerificationChange = { itemId, isReceived ->
                    viewModel.updateItemVerification(itemId, isReceived)
                },
                onVerifyItems = {
                    viewModel.verifyIndentItems(indent.id!!, currentUserId)
                },
                onVerificationComplete = {
                    // Close both dialogs after successful verification
                    showIndentDetails = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndentCard(
    indent: Indent,
    userRole: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
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
                        text = "Required: ${indent.requiredDate}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                StatusChip(status = indent.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items: ${indent.indentItems?.size ?: 0}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canPerformActions(indent.status, userRole)) {
                        when {
                            userRole == "chef" && indent.status == "Fulfilled" -> {
                                OutlinedButton(
                                    onClick = onClick,
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Verify", fontSize = 12.sp, color = Color(0xFF1976D2))
                                }
                            }

                            (userRole == "manager" || userRole == "storekeeper") && indent.status == "Approved" -> {
                                OutlinedButton(
                                    onClick = onClick,
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Fulfill", fontSize = 12.sp, color = Color(0xFF1976D2))
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
private fun IndentDetailsDialog(
    indent: Indent,
    userRole: String,
    currentUserId: String,
    onDismiss: () -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onFulfillIndent: () -> Unit,
    onShowVerificationDialog: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Indent Details",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        },
        text = {
            LazyColumn {
                item {
                    Column {
                        DetailRow("Indent ID", indent.id?.take(12) ?: "Unknown")
                        DetailRow("Cuisine", indent.cuisines?.name ?: "Unknown")
                        DetailRow("Required Date", indent.requiredDate)
                        DetailRow("Required Time", indent.requiredTime)
                        DetailRow("Priority", indent.priority)
                        DetailRow("Status", indent.status)
                        DetailRow("Purpose", indent.purpose)

                        if (!indent.notes.isNullOrEmpty()) {
                            DetailRow("Notes", indent.notes)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Items (${indent.indentItems?.size ?: 0})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                indent.indentItems?.let { items ->
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8F9FA)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = item.items?.name ?: "Unknown Item",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "Requested: ${item.requestedQuantity} ${item.unitOfMeasure}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                                if (item.approvedQuantity != null) {
                                    Text(
                                        text = "Approved: ${item.approvedQuantity} ${item.unitOfMeasure}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                                if (item.fulfilledQuantity != null) {
                                    Text(
                                        text = "Fulfilled: ${item.fulfilledQuantity} ${item.unitOfMeasure}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canPerformActions(indent.status, userRole)) {
                    when {
                        userRole == "chef" && indent.status == "Fulfilled" -> {
                            Button(
                                onClick = onShowVerificationDialog,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2)
                                )
                            ) {
                                Text("Verify", color = Color.White)
                            }
                        }

                        (userRole == "manager" || userRole == "storekeeper") && indent.status == "Submitted" -> {
                            Button(
                                onClick = { onUpdateStatus(indent.id!!, "Approved") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Approve", color = Color.White)
                            }
                        }

                        (userRole == "manager" || userRole == "storekeeper") && indent.status == "Approved" -> {
                            Button(
                                onClick = { onFulfillIndent() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                )
                            ) {
                                Text("Fulfill", color = Color.White)
                            }
                        }
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFF666666))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerificationDialog(
    indent: Indent,
    verificationItems: List<VerificationItem>,
    isVerifying: Boolean,
    verificationError: String?,
    currentUserId: String,
    onDismiss: () -> Unit,
    onItemVerificationChange: (String, Boolean) -> Unit,
    onVerifyItems: () -> Unit,
    onVerificationComplete: () -> Unit
) {
    // Track verification completion
    var wasVerifying by remember { mutableStateOf(false) }

    LaunchedEffect(isVerifying) {
        if (wasVerifying && !isVerifying && verificationError == null) {
            // Verification completed successfully
            onVerificationComplete()
        }
        wasVerifying = isVerifying
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Verify Received Items",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        },
        text = {
            LazyColumn {
                item {
                    Column {
                        // Header Information
                        DetailRow("Indent ID", indent.id?.take(12) ?: "Unknown")
                        DetailRow("Cuisine", indent.cuisines?.name ?: "Unknown")
                        DetailRow("Required Date", indent.requiredDate)
                        DetailRow("Status", "Fulfilled")
                        DetailRow("Purpose", indent.purpose)

                        Spacer(modifier = Modifier.height(16.dp))

                        val fulfilledCount = verificationItems.count { it.isFulfilled }
                        val receivedCount = verificationItems.count { it.isReceived && it.isFulfilled }

                        Text(
                            text = "Items ($receivedCount of $fulfilledCount verified)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(verificationItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isReceived) Color(0xFFE8F5E8) else Color(0xFFF8F9FA)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.isFulfilled) {
                                Checkbox(
                                    checked = item.isReceived,
                                    onCheckedChange = { checked ->
                                        onItemVerificationChange(item.indentItem.id!!, checked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF4CAF50)
                                    )
                                )
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.indentItem.items?.name ?: "Unknown Item",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )

                                if (item.isFulfilled) {
                                    Text(
                                        text = "Fulfilled: ${item.indentItem.fulfilledQuantity} ${item.indentItem.unitOfMeasure}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                } else {
                                    Text(
                                        text = "(Not Fulfilled)",
                                        fontSize = 12.sp,
                                        color = Color(0xFF757575),
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }

                if (verificationError != null) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = verificationError,
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fulfilledItems = verificationItems.filter { it.isFulfilled }
                val receivedCount = verificationItems.count { it.isReceived && it.isFulfilled }

                val buttonText = when {
                    receivedCount == 0 -> "Verify Items"
                    receivedCount == fulfilledItems.size -> "All Received"
                    else -> "Partially Received"
                }

                Button(
                    onClick = onVerifyItems,
                    enabled = !isVerifying && receivedCount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(buttonText, color = Color.White)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF666666))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Draft" -> Color(0xFFF5F5F5) to Color(0xFF666666)
        "Submitted" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "Under Review" -> Color(0xFFFFF3E0) to Color(0xFFFF8F00)
        "Approved" -> Color(0xFFE8F5E8) to Color(0xFF4CAF50)
        "Rejected" -> Color(0xFFFFEBEE) to Color(0xFFE91E63)
        "Fulfilled" -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
        "Received" -> Color(0xFFE8F5E8) to Color(0xFF2E7D32)
        "Partially Received" -> Color(0xFFFFF3E0) to Color(0xFFFF8F00)
        "Not Received" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else -> Color(0xFFF5F5F5) to Color(0xFF666666)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            modifier = Modifier.weight(0.6f)
        )
    }
}

private fun canPerformActions(status: String, userRole: String): Boolean {
    return when (userRole) {
        "chef" -> status == "Fulfilled"
        "manager", "storekeeper" -> status in listOf("Submitted", "Approved")
        else -> false
    }
}