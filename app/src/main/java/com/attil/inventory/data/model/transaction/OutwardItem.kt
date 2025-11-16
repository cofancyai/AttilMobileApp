package com.attil.inventory.data.model.transaction

import com.google.gson.annotations.SerializedName

data class OutwardItem(
    val id: String? = null,
    
    @SerializedName("item_id")
    val itemId: String,
    
    @SerializedName("category_id")
    val categoryId: String? = null,
    
    @SerializedName("outward_quantity")
    val outwardQuantity: Double,
    
    @SerializedName("cuisine_type")
    val cuisineType: String? = null,
    
    @SerializedName("usage_date")
    val usageDate: String,
    
    val notes: String? = null,
    
    @SerializedName("created_by")
    val createdBy: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("cuisine_id")
    val cuisineId: String? = null,
    
    @SerializedName("indent_id")
    val indentId: String? = null,
    
    @SerializedName("source_type")
    val sourceType: String = "manual",
    
    // Related entities for display
    val items: ItemForOutward? = null,
    val categories: CategoryForOutward? = null,
    val cuisines: CuisineForOutward? = null
)

data class ItemForOutward(
    val id: String,
    val name: String,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    val categories: CategoryForOutward? = null
)

data class CategoryForOutward(
    val id: String,
    val name: String
)

data class CuisineForOutward(
    val id: String,
    val name: String,
    val description: String?
)

data class CreateOutwardItemRequest(
    @SerializedName("item_id")
    val itemId: String,
    
    @SerializedName("category_id")
    val categoryId: String? = null,
    
    @SerializedName("outward_quantity")
    val outwardQuantity: Double,
    
    @SerializedName("cuisine_type")
    val cuisineType: String? = null,
    
    @SerializedName("usage_date")
    val usageDate: String,
    
    val notes: String? = null,
    
    @SerializedName("created_by")
    val createdBy: String? = null,
    
    @SerializedName("cuisine_id")
    val cuisineId: String? = null,
    
    @SerializedName("indent_id")
    val indentId: String? = null,
    
    @SerializedName("source_type")
    val sourceType: String = "manual"
)

data class UpdateOutwardItemRequest(
    @SerializedName("item_id")
    val itemId: String? = null,
    
    @SerializedName("category_id")
    val categoryId: String? = null,
    
    @SerializedName("outward_quantity")
    val outwardQuantity: Double? = null,
    
    @SerializedName("cuisine_type")
    val cuisineType: String? = null,
    
    @SerializedName("usage_date")
    val usageDate: String? = null,
    
    val notes: String? = null,
    
    @SerializedName("cuisine_id")
    val cuisineId: String? = null,
    
    @SerializedName("indent_id")
    val indentId: String? = null,
    
    @SerializedName("source_type")
    val sourceType: String? = null
)

// For the multi-item selection UI
data class ItemWithStock(
    val item: ItemForOutward,
    val currentStock: Double,
    var isSelected: Boolean = false,
    var outwardQuantity: Double = 0.0
)