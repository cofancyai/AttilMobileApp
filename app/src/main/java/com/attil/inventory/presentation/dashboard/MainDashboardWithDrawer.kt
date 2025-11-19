package com.attil.inventory.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import com.attil.inventory.PasswordManager
import com.attil.inventory.presentation.viewmodel.UserViewModel
import com.attil.inventory.presentation.auth.AuthViewModel
import com.attil.inventory.presentation.components.AppNavigationDrawer
import com.attil.inventory.presentation.management.category.CategoryManagementScreen
import com.attil.inventory.presentation.management.cuisine.CuisineManagementScreen
import com.attil.inventory.presentation.management.godown.GodownManagementScreen
import com.attil.inventory.presentation.management.item.ItemManagementScreen
import com.attil.inventory.presentation.management.rack.RackManagementScreen
import com.attil.inventory.presentation.management.usage.UsageManagementScreen
import com.attil.inventory.presentation.management.vendor.VendorManagementScreen
import com.attil.inventory.presentation.transaction.currentstock.CurrentStockScreen
import com.attil.inventory.presentation.transaction.inward.InwardManagementScreen
import com.attil.inventory.presentation.transaction.outward.OutwardManagementScreen
import com.attil.inventory.presentation.transaction.indent.IndentManagementScreen
import com.attil.inventory.presentation.transaction.indent.IndentCreationScreen
import com.attil.inventory.presentation.transaction.indent.IndentFulfillmentScreen
import com.attil.inventory.presentation.screen.master.RoleManagementScreen
import com.attil.inventory.presentation.screen.master.UserManagementScreen
import com.attil.inventory.presentation.reports.InventoryReportsScreen
import com.attil.inventory.presentation.reports.ReportsMainScreen
import com.attil.inventory.presentation.reports.IndentReportsScreen
import com.attil.inventory.data.model.reports.ReportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardWithDrawer(
    onLogout: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("dashboard") }

    val authState by authViewModel.authState.collectAsState()
    val currentUser = authState.user
    val userPermissions = currentUser?.getPermittedScreens() ?: listOf("dashboard")

    // Use current user's ID or default
    val currentUserId = currentUser?.id ?: "550e8400-e29b-41d4-a716-446655440000"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawer(
                selectedRoute = currentScreen,
                userPermissions = userPermissions,
                onNavigate = { screen ->
                    // Check if user has permission for the screen
                    if (userPermissions.contains(screen) || screen == "dashboard" || screen == "change_password") {
                        currentScreen = screen
                        scope.launch { drawerState.close() }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    onLogout()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inventory Management") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    "dashboard" -> {
                        DashboardScreen(
                            onMenuClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        )
                    }
                    "category_management" -> {
                        if (userPermissions.contains("category_management")) {
                            CategoryManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "cuisine_management" -> {
                        if (userPermissions.contains("cuisine_management")) {
                            CuisineManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "item_management" -> {
                        if (userPermissions.contains("item_management")) {
                            ItemManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "godown_management" -> {
                        if (userPermissions.contains("godown_management")) {
                            GodownManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "rack_management" -> {
                        if (userPermissions.contains("rack_management")) {
                            RackManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "usage_management" -> {
                        if (userPermissions.contains("usage_management")) {
                            UsageManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "vendor_management" -> {
                        if (userPermissions.contains("vendor_management")) {
                            VendorManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "inward_management" -> {
                        if (userPermissions.contains("inward_management")) {
                            InwardManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "outward_management" -> {
                        if (userPermissions.contains("outward_management")) {
                            OutwardManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "current_stock" -> {
                        if (userPermissions.contains("current_stock")) {
                            CurrentStockScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "indent_management" -> {
                        if (userPermissions.contains("indent_management")) {
                            IndentManagementScreen(
                                onBackClick = { currentScreen = "dashboard" },
                                onCreateIndent = { currentScreen = "indent_creation" },
                                onFulfillIndent = { currentScreen = "indent_fulfillment" },
                                currentUserId = currentUserId,
                                userRole = "chef" // Default role for now
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "indent_creation" -> {
                        if (userPermissions.contains("indent_creation")) {
                            IndentCreationScreen(
                                onBackClick = { currentScreen = "indent_management" },
                                chefId = currentUserId
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "indent_fulfillment" -> {
                        if (userPermissions.contains("indent_fulfillment")) {
                            IndentFulfillmentScreen(
                                onBackClick = { currentScreen = "indent_management" },
                                currentUserId = currentUserId
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "role_management" -> {
                        if (userPermissions.contains("role_management")) {
                            RoleManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "user_management" -> {
                        if (userPermissions.contains("user_management")) {
                            UserManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "inventory_reports" -> {
                        if (userPermissions.contains("inventory_reports")) {
                            ReportsMainScreen(
                                onBackClick = { currentScreen = "dashboard" },
                                onReportSelected = { reportType ->
                                    currentScreen = when(reportType) {
                                        ReportType.INWARD -> "inward_report"
                                        ReportType.OUTWARD -> "outward_report"
                                        ReportType.INDENT -> "indent_report"
                                    }
                                }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "inward_report" -> {
                        if (userPermissions.contains("inventory_reports")) {
                            InventoryReportsScreen(
                                reportType = ReportType.INWARD,
                                onBackClick = { currentScreen = "inventory_reports" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "outward_report" -> {
                        if (userPermissions.contains("inventory_reports")) {
                            InventoryReportsScreen(
                                reportType = ReportType.OUTWARD,
                                onBackClick = { currentScreen = "inventory_reports" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "indent_report" -> {
                        if (userPermissions.contains("inventory_reports")) {
                            IndentReportsScreen(
                                onBackClick = { currentScreen = "inventory_reports" }
                            )
                        } else {
                            UnauthorizedScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                    "change_password" -> {
                        ChangePasswordDialog(
                            onDismiss = { currentScreen = "dashboard" },
                            onPasswordChanged = { currentScreen = "dashboard" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnauthorizedScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = "Unauthorized",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Access Denied",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Gray
        )
        Text(
            text = "You don't have permission to access this screen",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick) {
            Text("Go Back")
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    var inputCurrentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = inputCurrentPassword,
                    onValueChange = {
                        inputCurrentPassword = it
                        showError = ""
                    },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        showError = ""
                    },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        showError = ""
                    },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = showError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        inputCurrentPassword != PasswordManager.currentPassword -> showError = "Current password is incorrect"
                        newPassword.length < 4 -> showError = "Password must be at least 4 characters"
                        newPassword != confirmPassword -> showError = "Passwords do not match"
                        else -> {
                            PasswordManager.currentPassword = newPassword
                            onPasswordChanged()
                        }
                    }
                }
            ) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Placeholder screens for remaining reports