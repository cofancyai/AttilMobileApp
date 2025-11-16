package com.attil.inventory.data.model.management

import com.google.gson.annotations.SerializedName

data class Vendor(
    val id: String? = null,
    val name: String,
    val address: String? = null,
    @SerializedName("contact_number")
    val contactNumber: String? = null,
    val email: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CreateVendorRequest(
    val name: String,
    val address: String? = null,
    @SerializedName("contact_number")
    val contactNumber: String? = null,
    val email: String? = null
)

data class UpdateVendorRequest(
    val name: String? = null,
    val address: String? = null,
    @SerializedName("contact_number")
    val contactNumber: String? = null,
    val email: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null
)