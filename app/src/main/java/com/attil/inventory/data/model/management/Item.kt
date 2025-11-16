package com.attil.inventory.data.model.management

import com.google.gson.annotations.SerializedName

data class Item(
    val id: String? = null,
    val name: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("godown_id")
    val godownId: String? = null,
    @SerializedName("rack_id")
    val rackId: String? = null,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    // Related entities for display
    val categories: Category? = null,
    val godowns: Godown? = null,
    val racks: Rack? = null,
    @SerializedName("item_cuisines")
    val itemCuisines: List<ItemCuisine>? = null
)

data class ItemCuisine(
    val id: String? = null,
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("cuisine_id")
    val cuisineId: String,
    val cuisines: Cuisine? = null
)

data class CreateItemRequest(
    val name: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("godown_id")
    val godownId: String? = null,
    @SerializedName("rack_id")
    val rackId: String? = null,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String,
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double,
    @SerializedName("cuisine_ids")
    val cuisineIds: List<String>? = null
)

data class UpdateItemRequest(
    val name: String? = null,
    @SerializedName("category_id")
    val categoryId: String? = null,
    @SerializedName("godown_id")
    val godownId: String? = null,
    @SerializedName("rack_id")
    val rackId: String? = null,
    @SerializedName("unit_of_measure")
    val unitOfMeasure: String? = null,
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    @SerializedName("cuisine_ids")
    val cuisineIds: List<String>? = null
)