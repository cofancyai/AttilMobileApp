package com.attil.inventory.data.model.transaction

import com.google.gson.annotations.SerializedName
import com.attil.inventory.data.model.management.Item
import com.attil.inventory.data.model.management.Cuisine

data class InwardItem(
    val id: String? = null,
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("vendor_name")
    val vendorName: String,
    @SerializedName("vendor_contact")
    val vendorContact: String? = null,
    @SerializedName("vendor_address")
    val vendorAddress: String? = null,
    @SerializedName("purchase_date")
    val purchaseDate: String,
    @SerializedName("inward_quantity")
    val inwardQuantity: Double,
    @SerializedName("price_per_unit")
    val pricePerUnit: Double,
    @SerializedName("price_without_gst")
    val priceWithoutGst: Double? = null,
    @SerializedName("price_with_gst")
    val priceWithGst: Double? = null,
    @SerializedName("gst_percentage")
    val gstPercentage: Double? = null,
    @SerializedName("bill_number")
    val billNumber: String? = null,
    @SerializedName("expiry_date")
    val expiryDate: String? = null,
    @SerializedName("cuisine_id")
    val cuisineId: String? = null,
    @SerializedName("created_by")
    val createdBy: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    // Related entities for display
    val items: Item? = null,
    val cuisines: Cuisine? = null
)

data class CreateInwardItemRequest(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("vendor_name")
    val vendorName: String,
    @SerializedName("vendor_contact")
    val vendorContact: String? = null,
    @SerializedName("vendor_address")
    val vendorAddress: String? = null,
    @SerializedName("purchase_date")
    val purchaseDate: String,
    @SerializedName("inward_quantity")
    val inwardQuantity: Double,
    @SerializedName("price_per_unit")
    val pricePerUnit: Double,
    @SerializedName("price_without_gst")
    val priceWithoutGst: Double? = null,
    @SerializedName("price_with_gst")
    val priceWithGst: Double? = null,
    @SerializedName("gst_percentage")
    val gstPercentage: Double? = null,
    @SerializedName("bill_number")
    val billNumber: String? = null,
    @SerializedName("expiry_date")
    val expiryDate: String? = null,
    @SerializedName("cuisine_id")
    val cuisineId: String? = null,
    @SerializedName("created_by")
    val createdBy: String? = null
)

data class UpdateInwardItemRequest(
    @SerializedName("item_id")
    val itemId: String? = null,
    @SerializedName("vendor_name")
    val vendorName: String? = null,
    @SerializedName("vendor_contact")
    val vendorContact: String? = null,
    @SerializedName("vendor_address")
    val vendorAddress: String? = null,
    @SerializedName("purchase_date")
    val purchaseDate: String? = null,
    @SerializedName("inward_quantity")
    val inwardQuantity: Double? = null,
    @SerializedName("price_per_unit")
    val pricePerUnit: Double? = null,
    @SerializedName("price_without_gst")
    val priceWithoutGst: Double? = null,
    @SerializedName("price_with_gst")
    val priceWithGst: Double? = null,
    @SerializedName("gst_percentage")
    val gstPercentage: Double? = null,
    @SerializedName("bill_number")
    val billNumber: String? = null,
    @SerializedName("expiry_date")
    val expiryDate: String? = null,
    @SerializedName("cuisine_id")
    val cuisineId: String? = null
)