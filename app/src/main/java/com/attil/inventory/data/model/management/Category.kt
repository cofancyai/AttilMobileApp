package com.attil.inventory.data.model.management

import com.google.gson.annotations.SerializedName

data class Category(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CreateCategoryRequest(
    val name: String,
    val description: String? = null
)

data class UpdateCategoryRequest(
    val name: String? = null,
    val description: String? = null
)