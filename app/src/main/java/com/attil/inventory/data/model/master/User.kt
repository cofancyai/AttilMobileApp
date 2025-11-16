package com.attil.inventory.data.model.master

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonArray
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

// Custom deserializer to handle screen_permissions field that can be either object, array, or string
class ScreenPermissionsDeserializer : JsonDeserializer<List<String>?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): List<String>? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> {
                // Handle comma-separated string format
                val permissionsString = json.asString
                if (permissionsString.isBlank()) emptyList()
                else permissionsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            json.isJsonArray -> {
                // Handle array format
                val array = json.asJsonArray
                array.map { it.asString.trim() }.filter { it.isNotEmpty() }
            }
            json.isJsonObject -> {
                // Handle object format (extract screens array if exists)
                val obj = json.asJsonObject
                val screensArray = obj.getAsJsonArray("screens")
                screensArray?.map { it.asString.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            }
            else -> emptyList()
        }
    }
}

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("role_id")
    val roleId: String?,

    @SerializedName("is_active")
    val isActive: Boolean,

    @SerializedName("last_sign_in_at")
    val lastLogin: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?,

    // Screen permissions for work assignment
    @SerializedName("screen_permissions")
    @JsonAdapter(ScreenPermissionsDeserializer::class)
    val screenPermissions: List<String>? = null,

    // Nested role information when joined
    @SerializedName("roles")
    val role: Role? = null
) {
    // Helper function to check if user has permission for a specific screen
    fun hasScreenPermission(screenRoute: String): Boolean {
        return screenPermissions?.contains(screenRoute) == true || screenRoute == "dashboard"
    }

    // Helper function to get all permitted screen routes
    fun getPermittedScreens(): List<String> {
        val defaultScreens = listOf("dashboard") // Dashboard always accessible
        return (screenPermissions ?: emptyList()) + defaultScreens
    }
}

data class CreateUserRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password_hash")
    val password: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("role_id")
    val roleId: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("screen_permissions")
    val screenPermissions: List<String>? = null
)

data class UpdateUserRequest(
    @SerializedName("username")
    val username: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("role_id")
    val roleId: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean? = null,

    @SerializedName("screen_permissions")
    val screenPermissions: List<String>? = null
)

// New data class specifically for updating screen permissions
data class UpdateUserPermissionsRequest(
    @SerializedName("screen_permissions")
    val screenPermissions: List<String>
)

data class ChangePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,

    @SerializedName("new_password")
    val newPassword: String
)

data class ResetPasswordRequest(
    @SerializedName("password_hash")
    val newPassword: String
)

// Data class to define available screens for assignment
object AvailableScreens {
    const val DASHBOARD = "dashboard"

    // Management screens
    const val GODOWN_MANAGEMENT = "godown_management"
    const val RACK_MANAGEMENT = "rack_management"
    const val CATEGORY_MANAGEMENT = "category_management"
    const val CUISINE_MANAGEMENT = "cuisine_management"
    const val VENDOR_MANAGEMENT = "vendor_management"
    const val ITEM_MANAGEMENT = "item_management"
    const val USAGE_MANAGEMENT = "usage_management"
    const val ROLE_MANAGEMENT = "role_management"
    const val USER_MANAGEMENT = "user_management"

    // Transaction screens
    const val INWARD_MANAGEMENT = "inward_management"
    const val OUTWARD_MANAGEMENT = "outward_management"
    const val CURRENT_STOCK = "current_stock"
    const val INDENT_MANAGEMENT = "indent_management"
    const val INDENT_CREATION = "indent_creation"
    const val INDENT_FULFILLMENT = "indent_fulfillment"

    // Reports
    const val INVENTORY_REPORTS = "inventory_reports"

    // Get all available screens for assignment
    fun getAllScreens(): Map<String, List<Pair<String, String>>> {
        return mapOf(
            "Management" to listOf(
                GODOWN_MANAGEMENT to "Godowns",
                RACK_MANAGEMENT to "Racks",
                CATEGORY_MANAGEMENT to "Categories",
                CUISINE_MANAGEMENT to "Cuisines",
                VENDOR_MANAGEMENT to "Vendors",
                ITEM_MANAGEMENT to "Items",
                USAGE_MANAGEMENT to "Usage",
                ROLE_MANAGEMENT to "Roles",
                USER_MANAGEMENT to "Users"
            ),
            "Transactions" to listOf(
                INWARD_MANAGEMENT to "Inward Management",
                OUTWARD_MANAGEMENT to "Outward Management",
                CURRENT_STOCK to "Current Stock",
                INDENT_MANAGEMENT to "Indent Management",
                INDENT_CREATION to "Create Indent",
                INDENT_FULFILLMENT to "Indent Fulfillment"
            ),
            "Reports" to listOf(
                INVENTORY_REPORTS to "Inventory Reports"
            )
        )
    }

    // Get flat list of all screen routes
    fun getAllScreenRoutes(): List<String> {
        return getAllScreens().values.flatten().map { it.first }
    }
}