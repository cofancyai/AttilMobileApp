package com.attil.inventory.presentation.screen.master

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.attil.inventory.data.model.master.User
import com.attil.inventory.presentation.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBackClick: () -> Unit = {},
    viewModel: UserViewModel = hiltViewModel()
) {
    val users by viewModel.filteredUsers.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val cuisines by viewModel.cuisines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    LaunchedEffect(errorMessage, successMessage) {
        if (errorMessage != null || successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "User Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchUsers(it) },
            label = { Text("Search users...", color = Color.Black) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Success/Error Messages
        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        successMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Green.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Loading Indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user,
                        onEdit = {
                            viewModel.selectUser(user)
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.selectUser(user)
                            showDeleteDialog = true
                        },
                        onResetPassword = {
                            viewModel.selectUser(user)
                            showPasswordResetDialog = true
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        UserDialog(
            title = "Create User",
            user = null,
            cuisines = cuisines,
            onDismiss = { showCreateDialog = false },
            onConfirm = { username, password, fullName, phone, cuisineIds ->
                // Pass all cuisine IDs to ViewModel for junction table management
                viewModel.createUser(username, "$username@restaurant.com", password, fullName, phone, null, cuisineIds.firstOrNull(), true, cuisineIds)
                showCreateDialog = false
            }
        )
    }

    if (showEditDialog && selectedUser != null) {
        UserDialog(
            title = "Edit User",
            user = selectedUser,
            cuisines = cuisines,
            onDismiss = { showEditDialog = false },
            onConfirm = { username, _, fullName, phone, cuisineIds ->
                selectedUser?.let { user ->
                    // Pass all cuisine IDs to ViewModel for junction table management
                    viewModel.updateUser(user.id, username, user.email, fullName, phone, user.roleId, cuisineIds.firstOrNull(), user.isActive, cuisineIds)
                }
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && selectedUser != null) {
        DeleteConfirmDialog(
            userName = selectedUser?.fullName ?: "Unknown User",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                selectedUser?.let { user ->
                    viewModel.deleteUser(user.id)
                }
                showDeleteDialog = false
            }
        )
    }

    if (showPasswordResetDialog && selectedUser != null) {
        PasswordResetDialog(
            userName = selectedUser?.fullName ?: "Unknown User",
            onDismiss = { showPasswordResetDialog = false },
            onConfirm = { newPassword ->
                selectedUser?.let { user ->
                    viewModel.resetUserPassword(user.id, newPassword)
                }
                showPasswordResetDialog = false
            }
        )
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    user.phone?.let { phone ->
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Status and Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (user.isActive) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = if (user.isActive) "Active" else "Inactive",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (user.isActive) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f)
                        )
                    }

                    // Action Buttons
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Blue
                        )
                    }

                    IconButton(onClick = onResetPassword) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Reset Password",
                            tint = Color(0xFFFF9800)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete User",
                color = Color.Black
            )
        },
        text = {
            Text(
                "Are you sure you want to delete user '$userName'? This action cannot be undone.",
                color = Color.Black
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Black)
            }
        }
    )
}

@Composable
fun UserDialog(
    title: String,
    user: User?,
    cuisines: List<com.attil.inventory.data.model.management.Cuisine>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?, List<String>) -> Unit
) {
    var username by remember { mutableStateOf(user?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var selectedCuisineIds by remember { mutableStateOf(user?.getCuisineIds() ?: emptyList()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                color = Color.Black
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                // Password fields (only for new users)
                if (user == null) {
                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.Black) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    // Confirm Password (only for new users)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = Color.Black) },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    // Password mismatch warning
                    if (password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword) {
                        Text(
                            "Passwords do not match",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", color = Color.Black) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                // Assign Cuisines (Multiple Selection with Checkboxes)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Assign Cuisines (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            if (cuisines.isEmpty()) {
                                Text(
                                    text = "No cuisines available",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else {
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
                                                cuisine.id?.let { id ->
                                                    selectedCuisineIds = if (isChecked) {
                                                        selectedCuisineIds + id
                                                    } else {
                                                        selectedCuisineIds - id
                                                    }
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF1976D2),
                                                uncheckedColor = Color.Gray
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cuisine.name,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val isValidForCreate = user == null && username.isNotBlank() && fullName.isNotBlank() &&
                            password.isNotBlank() && password == confirmPassword
                    val isValidForEdit = user != null && username.isNotBlank() && fullName.isNotBlank()

                    if (isValidForCreate || isValidForEdit) {
                        onConfirm(username, password, fullName, phone.takeIf { it.isNotBlank() }, selectedCuisineIds)
                    }
                },
                enabled = if (user == null) {
                    username.isNotBlank() && fullName.isNotBlank() &&
                            password.isNotBlank() && password == confirmPassword
                } else {
                    username.isNotBlank() && fullName.isNotBlank()
                }
            ) {
                Text("Save", color = Color.Blue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Black)
            }
        }
    )
}

@Composable
fun PasswordResetDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Reset Password",
                color = Color.Black
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Reset password for $userName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password", color = Color.Black) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color.Black) },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                    Text(
                        "Passwords do not match",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword.isNotBlank() && newPassword == confirmPassword) {
                        onConfirm(newPassword)
                    }
                },
                enabled = newPassword.isNotBlank() && newPassword == confirmPassword
            ) {
                Text("Reset", color = Color.Blue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Black)
            }
        }
    )
}