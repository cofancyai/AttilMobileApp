package com.attil.inventory.data.model.management

import com.google.gson.annotations.SerializedName

data class Rack(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    @SerializedName("godown_id")
    val godownId: String,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    // For displaying godown name in UI
    val godownName: String? = null
)

data class CreateRackRequest(
    val name: String,
    val description: String? = null,
    @SerializedName("godown_id")
    val godownId: String
)

data class UpdateRackRequest(
    val name: String? = null,
    val description: String? = null,
    @SerializedName("godown_id")
    val godownId: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null
)

data class RackWithGodown(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    @SerializedName("godown_id")
    val godownId: String,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val godowns: Godown? = null // Nested godown data from Supabase join
)