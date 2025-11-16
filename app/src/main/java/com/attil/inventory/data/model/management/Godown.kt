package com.attil.inventory.data.model.management

import com.google.gson.annotations.SerializedName

data class Godown(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val location: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CreateGodownRequest(
    val name: String,
    val description: String? = null,
    val location: String? = null
)

data class UpdateGodownRequest(
    val name: String? = null,
    val description: String? = null,
    val location: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null
)