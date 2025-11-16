package com.attil.inventory.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.AuthState
import com.attil.inventory.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState())
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    init {
        Log.d("AuthViewModel", "🚀 AuthViewModel CREATED!")
        // Don't check login status on init - force fresh login
        _authState.value = AuthState(isLoggedIn = false)
        Log.d("AuthViewModel", "Set initial auth state to logged out")
    }

    fun login(username: String, password: String) {
        Log.d("AuthViewModel", "🔑 LOGIN called - Username: '$username', Password: '$password'")
        viewModelScope.launch {
            Log.d("AuthViewModel", "Starting repository login...")
            authRepository.login(username, password).collectLatest { state ->
                Log.d("AuthViewModel", "📨 Auth state received - isLoggedIn: ${state.isLoggedIn}, error: ${state.error}")
                _authState.value = state
            }
        }
    }

    fun checkLoginStatus() {
        Log.d("AuthViewModel", "Checking login status...")
        viewModelScope.launch {
            authRepository.isLoggedIn().collectLatest { isLoggedIn ->
                Log.d("AuthViewModel", "Login status: $isLoggedIn")
                if (isLoggedIn) {
                    authRepository.getCurrentUser().collectLatest { user ->
                        Log.d("AuthViewModel", "Current user: ${user?.username}")
                        _authState.value = AuthState(isLoggedIn = true, user = user)
                    }
                } else {
                    _authState.value = AuthState(isLoggedIn = false)
                }
            }
        }
    }

    fun logout() {
        Log.d("AuthViewModel", "🚪 LOGOUT called")
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState(isLoggedIn = false)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            val currentUser = _authState.value.user
            if (currentUser != null) {
                _changePasswordState.value = ChangePasswordState(isLoading = true)

                authRepository.changePassword(
                    userId = currentUser.id,
                    currentPassword = currentPassword,
                    newPassword = newPassword
                ).collectLatest { result ->
                    result.fold(
                        onSuccess = { message ->
                            _changePasswordState.value = ChangePasswordState(
                                isLoading = false,
                                isSuccess = true,
                                message = message
                            )
                        },
                        onFailure = { error ->
                            _changePasswordState.value = ChangePasswordState(
                                isLoading = false,
                                isSuccess = false,
                                error = error.message ?: "Failed to change password"
                            )
                        }
                    )
                }
            } else {
                _changePasswordState.value = ChangePasswordState(
                    isLoading = false,
                    isSuccess = false,
                    error = "User not logged in"
                )
            }
        }
    }

    fun clearChangePasswordState() {
        _changePasswordState.value = ChangePasswordState()
    }
}

data class ChangePasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val message: String? = null
)