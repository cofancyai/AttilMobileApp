package com.attil.inventory.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DashboardCard(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onMenuClick: () -> Unit
) {
    val premiumGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2)
        )
    )

    val dashboardCards = listOf(
        DashboardCard(
            title = "Inventory Overview",
            subtitle = "Current Stock & Levels",
            icon = Icons.Default.Inventory,
            color = Color(0xFF4CAF50)
        ) { /* Navigate to current stock */ },
        
        DashboardCard(
            title = "Recent Transactions",
            subtitle = "Inward & Outward Items",
            icon = Icons.Default.SwapHoriz,
            color = Color(0xFF2196F3)
        ) { /* Navigate to transactions */ },
        
        DashboardCard(
            title = "Low Stock Alert",
            subtitle = "Items Need Attention",
            icon = Icons.Default.Warning,
            color = Color(0xFFFF9800)
        ) { /* Navigate to low stock */ },
        
        DashboardCard(
            title = "Pending Indents",
            subtitle = "Chef Requests",
            icon = Icons.Default.RequestPage,
            color = Color(0xFFE91E63)
        ) { /* Navigate to indents */ },
        
        DashboardCard(
            title = "Item Management",
            subtitle = "Manage Items & Categories",
            icon = Icons.Default.Category,
            color = Color(0xFF9C27B0)
        ) { /* Navigate to items */ },
        
        DashboardCard(
            title = "Reports & Analytics",
            subtitle = "Usage & Performance",
            icon = Icons.Default.Analytics,
            color = Color(0xFF795548)
        ) { /* Navigate to reports */ }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(premiumGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Welcome Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF667eea)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Welcome to Attil Multicuisine",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Inventory Management System",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Dashboard Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dashboardCards) { card ->
                    DashboardCardItem(
                        card = card,
                        onClick = card.onClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardCardItem(
    card: DashboardCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                card.icon,
                contentDescription = card.title,
                modifier = Modifier.size(32.dp),
                tint = card.color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )
            Text(
                text = card.subtitle,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}