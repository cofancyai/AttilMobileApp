package com.attil.inventory.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username_input")
    val username: String,

    @SerializedName("password_input")
    val password: String
)

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: com.attil.inventory.data.model.master.User? = null,
    val error: String? = null
)