package com.attil.inventory.data.model.transaction

import com.google.gson.annotations.SerializedName
import com.attil.inventory.data.model.management.Cuisine

data class Indent(
    val id: String? = null,
    @SerializedName("chef_id")
    val chefId: String,
    @SerializedName("cuisine_id")
    val cuisineId: String,
    @SerializedName("required_date")
    val requiredDate: String,
    @SerializedName("required_time")
    val requiredTime: String,
    val priority: String,
    val purpose: String,
    val notes: String? = null,
    val status: String = "Submitted",
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("approved_by")
    val approvedBy: String? = null,
    @SerializedName("approved_at")
    val approvedAt: String? = null,
    @SerializedName("fulfilled_by")
    val fulfilledBy: String? = null,
    @SerializedName("fulfilled_at")
    val fulfilledAt: String? = null,
    @SerializedName("received_at")
    val receivedAt: String? = null,

    // Related entities for display
    val cuisines: Cuisine? = null,
    @SerializedName("indent_items")
    val indentItems: List<IndentItem>? = null,

    // Chef information when joined with users table
    @SerializedName("users")
    val chef: ChefInfo? = null
)

data class IndentItem(
    val id: String? = null,
    @SerializedName("indent_id")
    val indentId: String,
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("requested_quantity")
    val requestedQuantity: Double,
    @SerializedName("approved_quantity")
    val approvedQuantity: Double? = null,
    @SerializedName("fulfilled_quantity")
    val fulfilledQuantity: Double? = null,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    val status: String = "Pending",
    @SerializedName("created_at")
    val createdAt: String? = null,

    // Verification fields
    @SerializedName("is_received")
    val isReceived: Boolean? = null,
    @SerializedName("received_by")
    val receivedBy: String? = null,
    @SerializedName("received_at")
    val receivedAt: String? = null,
    @SerializedName("received_quantity")
    val receivedQuantity: Double? = null,
    @SerializedName("verification_status")
    val verificationStatus: String? = null, // "Fully Verified", "Partially Verified", null
    @SerializedName("is_deleted")
    val isDeleted: Boolean? = null,

    // Related entities for display
    val items: ItemForIndent? = null
)

data class CreateIndentRequest(
    val chefId: String,
    val cuisineId: String,
    val requiredDate: String,
    val requiredTime: String,
    val priority: String,
    val purpose: String,
    val notes: String? = null,
    val indentItems: List<CreateIndentItemForRequest>
)

data class CreateIndentMainRequest(
    @SerializedName("chef_id")
    val chefId: String,
    @SerializedName("cuisine_id")
    val cuisineId: String,
    @SerializedName("required_date")
    val requiredDate: String,
    @SerializedName("required_time")
    val requiredTime: String,
    val priority: String,
    val purpose: String,
    val notes: String? = null
)

data class CreateIndentItemRequest(
    @SerializedName("indent_id")
    val indentId: String,
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("requested_quantity")
    val requestedQuantity: Double,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String
)

data class CreateIndentItemForRequest(
    val itemId: String,
    val requestedQuantity: Double,
    val unitOfMeasure: String
)

data class UpdateIndentRequest(
    @SerializedName("chef_id")
    val chefId: String? = null,
    @SerializedName("cuisine_id")
    val cuisineId: String? = null,
    @SerializedName("required_date")
    val requiredDate: String? = null,
    @SerializedName("required_time")
    val requiredTime: String? = null,
    val priority: String? = null,
    val purpose: String? = null,
    val notes: String? = null,
    val status: String? = null,
    @SerializedName("approved_by")
    val approvedBy: String? = null,
    @SerializedName("approved_at")
    val approvedAt: String? = null,
    @SerializedName("fulfilled_by")
    val fulfilledBy: String? = null,
    @SerializedName("fulfilled_at")
    val fulfilledAt: String? = null,
    @SerializedName("received_at")
    val receivedAt: String? = null
)

data class UpdateIndentItemRequest(
    @SerializedName("fulfilled_quantity")
    val fulfilledQuantity: Double? = null,
    val status: String? = null,
    @SerializedName("is_received")
    val isReceived: Boolean? = null,
    @SerializedName("received_by")
    val receivedBy: String? = null,
    @SerializedName("received_at")
    val receivedAt: String? = null,
    @SerializedName("received_quantity")
    val receivedQuantity: Double? = null,
    @SerializedName("verification_status")
    val verificationStatus: String? = null,
    @SerializedName("is_deleted")
    val isDeleted: Boolean? = null
)

data class VerifyIndentItemRequest(
    @SerializedName("is_received")
    val isReceived: Boolean,
    @SerializedName("received_by")
    val receivedBy: String,
    @SerializedName("received_at")
    val receivedAt: String
)

data class VerificationItem(
    val indentItem: IndentItem,
    val isFulfilled: Boolean,
    var isReceived: Boolean = false,
    var receivedQuantity: Double = 0.0
)

// Data models for item selection in indent creation
data class ItemForIndentSelection(
    val item: ItemForIndent,
    val availableStock: Double,
    var isSelected: Boolean = false,
    var requestedQuantity: Double = 0.0
)

data class ItemForIndent(
    val id: String,
    val name: String,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    val categories: CategoryForIndent? = null
)

data class CategoryForIndent(
    val id: String,
    val name: String
)

// Chef information for indent display
data class ChefInfo(
    val id: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("cuisine_id")
    val cuisineId: String?
)