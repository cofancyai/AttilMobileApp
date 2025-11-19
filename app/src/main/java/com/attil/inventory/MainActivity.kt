package com.attil.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.attil.inventory.presentation.auth.BasicLoginScreen
import com.attil.inventory.presentation.dashboard.MainDashboardWithDrawer
import com.attil.inventory.presentation.management.category.CategoryManagementScreen
import com.attil.inventory.presentation.management.cuisine.CuisineManagementScreen
import com.attil.inventory.presentation.management.godown.GodownManagementScreen
import com.attil.inventory.presentation.management.item.ItemManagementScreen
import com.attil.inventory.presentation.management.rack.RackManagementScreen
import com.attil.inventory.presentation.management.usage.UsageManagementScreen
import com.attil.inventory.presentation.management.vendor.VendorManagementScreen
import com.attil.inventory.presentation.reports.InventoryReportsScreen
import com.attil.inventory.presentation.reports.ReportsMainScreen
import com.attil.inventory.presentation.reports.IndentReportsScreen
import com.attil.inventory.presentation.screen.master.RoleManagementScreen
import com.attil.inventory.presentation.screen.master.UserManagementScreen
import com.attil.inventory.presentation.theme.AttilInventoryTheme
import com.attil.inventory.presentation.transaction.currentstock.CurrentStockScreen
import com.attil.inventory.presentation.transaction.indent.IndentCreationScreen
import com.attil.inventory.presentation.transaction.indent.IndentFulfillmentScreen
import com.attil.inventory.presentation.transaction.indent.IndentManagementScreen
import com.attil.inventory.presentation.transaction.inward.InwardManagementScreen
import com.attil.inventory.presentation.transaction.outward.OutwardManagementScreen
import com.attil.inventory.data.model.reports.ReportType
import dagger.hilt.android.AndroidEntryPoint

// Global password storage
object PasswordManager {
    var currentPassword by mutableStateOf("attil")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AttilInventoryTheme {
                var currentScreen by remember { mutableStateOf("login") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "login" -> {
                            BasicLoginScreen(
                                onLoginSuccess = {
                                    currentScreen = "dashboard"
                                }
                            )
                        }
                        "dashboard" -> {
                            MainDashboardWithDrawer(
                                onLogout = {
                                    currentScreen = "login"
                                }
                            )
                        }
                        "category_management" -> {
                            CategoryManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "cuisine_management" -> {
                            CuisineManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "item_management" -> {
                            ItemManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "godown_management" -> {
                            GodownManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "rack_management" -> {
                            RackManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "usage_management" -> {
                            UsageManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "vendor_management" -> {
                            VendorManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "current_stock" -> {
                            CurrentStockScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "inward_management" -> {
                            InwardManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "outward_management" -> {
                            OutwardManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "indent_management" -> {
                            IndentManagementScreen(
                                onBackClick = { currentScreen = "dashboard" },
                                onCreateIndent = { currentScreen = "indent_creation" },
                                onFulfillIndent = { currentScreen = "indent_fulfillment" },
                                currentUserId = "550e8400-e29b-41d4-a716-446655440000", // Default user ID
                                userRole = "chef" // Default role
                            )
                        }
                        "indent_creation" -> {
                            IndentCreationScreen(
                                onBackClick = { currentScreen = "indent_management" },
                                chefId = "550e8400-e29b-41d4-a716-446655440000"
                            )
                        }
                        "indent_fulfillment" -> {
                            IndentFulfillmentScreen(
                                onBackClick = { currentScreen = "dashboard" },
                                currentUserId = "550e8400-e29b-41d4-a716-446655440000"
                            )
                        }
                        "role_management" -> {
                            RoleManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "user_management" -> {
                            UserManagementScreen(
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                        "inventory_reports" -> {
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
                        }
                        "inward_report" -> {
                            InventoryReportsScreen(
                                reportType = ReportType.INWARD,
                                onBackClick = { currentScreen = "inventory_reports" }
                            )
                        }
                        "outward_report" -> {
                            InventoryReportsScreen(
                                reportType = ReportType.OUTWARD,
                                onBackClick = { currentScreen = "inventory_reports" }
                            )
                        }
                        "indent_report" -> {
                            IndentReportsScreen(
                                onBackClick = { currentScreen = "inventory_reports" }
                            )
                        }

                    }
                }
            }
        }
    }
}

// Placeholder screens for remaining reports