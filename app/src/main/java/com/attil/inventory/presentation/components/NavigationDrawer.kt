package com.attil.inventory.presentation.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val subItems: List<NavigationItem> = emptyList()
)

@Composable
fun AppNavigationDrawer(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    userPermissions: List<String> = emptyList(), // Add this parameter
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItem(
            title = "Dashboard",
            icon = Icons.Default.Dashboard,
            route = "dashboard"
        ),
        NavigationItem(
            title = "Management",
            icon = Icons.Default.Business,
            route = "management",
            subItems = listOf(
                NavigationItem("Godowns", Icons.Default.Warehouse, "godown_management"),
                NavigationItem("Racks", Icons.Default.ViewModule, "rack_management"),
                NavigationItem("Categories", Icons.Default.Category, "category_management"),
                NavigationItem("Cuisines", Icons.Default.Restaurant, "cuisine_management"),
                NavigationItem("Vendors", Icons.Default.Store, "vendor_management"),
                NavigationItem("Items", Icons.Default.Inventory, "item_management"),
                NavigationItem("Usage", Icons.Default.Assignment, "usage_management"),
                NavigationItem("Roles", Icons.Default.AdminPanelSettings, "role_management"),
                NavigationItem("Users", Icons.Default.People, "user_management")
            )
        ),
        NavigationItem(
            title = "Transactions",
            icon = Icons.Default.SwapHoriz,
            route = "transactions",
            subItems = listOf(
                NavigationItem("Inward", Icons.Default.CallReceived, "inward_management"),
                NavigationItem("Outward", Icons.Default.CallMade, "outward_management"),
                NavigationItem("Current Stock", Icons.Default.Inventory2, "current_stock"),
                NavigationItem("Indent Management", Icons.Default.RequestPage, "indent_management"),
                NavigationItem("Create Indent", Icons.Default.Add, "indent_creation"),
                NavigationItem("Indent Fulfillment", Icons.Default.CheckCircle, "indent_fulfillment")
            )
        ),
        NavigationItem(
            title = "Reports",
            icon = Icons.Default.Assessment,
            route = "inventory_reports"
        )
    )

    // Filter navigation items based on user permissions
    val filteredNavigationItems = navigationItems.map { item ->
        when {
            item.route == "dashboard" -> item // Dashboard always visible
            item.subItems.isNotEmpty() -> {
                // Filter sub-items based on permissions
                val filteredSubItems = item.subItems.filter { subItem ->
                    userPermissions.contains(subItem.route) || subItem.route == "dashboard"
                }
                // Only show parent if it has visible sub-items
                if (filteredSubItems.isNotEmpty()) {
                    item.copy(subItems = filteredSubItems)
                } else null
            }
            else -> {
                // Single item - check if user has permission
                if (userPermissions.contains(item.route)) item else null
            }
        }
    }.filterNotNull()

    var expandedSections by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = "Restaurant",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Attil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Inventory",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredNavigationItems) { item ->
                NavigationSection(
                    item = item,
                    selectedRoute = selectedRoute,
                    isExpanded = expandedSections.contains(item.route),
                    onToggleExpanded = { route ->
                        expandedSections = if (expandedSections.contains(route)) {
                            expandedSections - route
                        } else {
                            expandedSections + route
                        }
                    },
                    onNavigate = onNavigate
                )
            }
        }

        // Change Password
        Divider(color = Color.Gray.copy(alpha = 0.3f))

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("change_password") }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Change Password",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Change Password",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }

        // Logout Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = "Logout",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Logout",
                fontSize = 16.sp,
                color = Color.Red,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NavigationSection(
    item: NavigationItem,
    selectedRoute: String,
    isExpanded: Boolean,
    onToggleExpanded: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    Column {
        // Main navigation item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (item.subItems.isNotEmpty()) {
                        onToggleExpanded(item.route)
                    } else {
                        onNavigate(item.route)
                    }
                }
                .background(
                    color = if (selectedRoute == item.route) Color(0xFF667eea).copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = item.title,
                tint = if (selectedRoute == item.route) Color(0xFF667eea) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.title,
                fontSize = 16.sp,
                color = if (selectedRoute == item.route) Color(0xFF667eea) else Color.Black,
                fontWeight = if (selectedRoute == item.route) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            if (item.subItems.isNotEmpty()) {
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Sub items
        if (isExpanded && item.subItems.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 32.dp, top = 4.dp)
            ) {
                item.subItems.forEach { subItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(subItem.route) }
                            .background(
                                color = if (selectedRoute == subItem.route) Color(0xFF667eea).copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            subItem.icon,
                            contentDescription = subItem.title,
                            tint = if (selectedRoute == subItem.route) Color(0xFF667eea) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subItem.title,
                            fontSize = 14.sp,
                            color = if (selectedRoute == subItem.route) Color(0xFF667eea) else Color.Black,
                            fontWeight = if (selectedRoute == subItem.route) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}