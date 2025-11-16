package com.attil.inventory.data.model.management

import com.google.gson.annotations.SerializedName

data class CurrentStock(
    @SerializedName("item_id")
    val itemId: String,
    
    @SerializedName("item_name")
    val itemName: String,
    
    @SerializedName("category_name")
    val categoryName: String,
    
    @SerializedName("godown_name")
    val godownName: String?,
    
    @SerializedName("rack_name")
    val rackName: String?,
    
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double,
    
    @SerializedName("total_inward")
    val totalInward: Double,
    
    @SerializedName("total_outward")
    val totalOutward: Double,
    
    @SerializedName("current_stock")
    val currentStock: Double,
    
    @SerializedName("is_low_stock")
    val isLowStock: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

