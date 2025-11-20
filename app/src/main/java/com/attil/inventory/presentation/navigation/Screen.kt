package com.attil.inventory.presentation.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object BasicLogin : Screen("basic_login")
    object ChangePassword : Screen("change_password")

    // Dashboard
    object Dashboard : Screen("dashboard")
    object MainDashboard : Screen("main_dashboard")

    // Management Screens
    object GodownManagement : Screen("godown_management")
    object RackManagement : Screen("rack_management")
    object CategoryManagement : Screen("category_management")
    object CuisineManagement : Screen("cuisine_management")
    object VendorManagement : Screen("vendor_management")
    object ItemManagement : Screen("item_management")
    object UsageManagement : Screen("usage_management")
    object RoleManagement : Screen("role_management")
    object UserManagement : Screen("user_management")

    // Transaction Screens
    object InwardManagement : Screen("inward_management")
    object OutwardManagement : Screen("outward_management")
    object CurrentStock : Screen("current_stock")

    // Indent Screens
    object IndentManagement : Screen("indent_management")
    object IndentCreation : Screen("indent_creation")
    object IndentFulfillment : Screen("indent_fulfillment")

    // Report Screens
    object InventoryReports : Screen("inventory_reports")
    object TransactionReports : Screen("transaction_reports")
    object UsageReports : Screen("usage_reports")
    object FinancialReports : Screen("financial_reports")

    // Bulk Operations
    object ImportExport : Screen("import_export")
}