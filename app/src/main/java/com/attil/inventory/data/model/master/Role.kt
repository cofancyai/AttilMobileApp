package com.attil.inventory.data.model.master

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

// Custom deserializer to handle permissions field that can be either object, array, or string
class PermissionsDeserializer : JsonDeserializer<String?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): String? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> json.asString
            json.isJsonArray -> {
                val array = json.asJsonArray
                array.joinToString(",") { it.asString }
            }
            json.isJsonObject -> {
                // Convert object to string representation
                json.toString()
            }
            else -> null
        }
    }
}

data class Role(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("permissions")
    @JsonAdapter(PermissionsDeserializer::class)
    val permissions: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class CreateRoleRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("permissions")
    val permissions: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true
)

data class UpdateRoleRequest(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("permissions")
    val permissions: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean? = null
)