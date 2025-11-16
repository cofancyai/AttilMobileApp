package com.attil.inventory.presentation.management.rack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attil.inventory.data.model.management.CreateRackRequest
import com.attil.inventory.data.model.management.Godown
import com.attil.inventory.data.model.management.Rack
import com.attil.inventory.data.model.management.RackWithGodown
import com.attil.inventory.data.model.management.UpdateRackRequest
import com.attil.inventory.data.repository.GodownRepository
import com.attil.inventory.data.repository.RackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RackUiState(
    val racks: List<RackWithGodown> = emptyList(),
    val godowns: List<Godown> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingGodowns: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class RackViewModel @Inject constructor(
    private val rackRepository: RackRepository,
    private val godownRepository: GodownRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RackUiState())
    val uiState: StateFlow<RackUiState> = _uiState.asStateFlow()

    init {
        loadRacks()
        loadGodowns()
    }

    fun loadRacks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            rackRepository.getAllRacks().collectLatest { result ->
                result.onSuccess { racks ->
                    _uiState.value = _uiState.value.copy(
                        racks = racks,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun loadGodowns() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingGodowns = true)
            godownRepository.getAllGodowns().collectLatest { result ->
                result.onSuccess { godowns ->
                    _uiState.value = _uiState.value.copy(
                        godowns = godowns,
                        isLoadingGodowns = false
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingGodowns = false,
                        error = exception.message ?: "Failed to load godowns"
                    )
                }
            }
        }
    }

    fun createRack(name: String, description: String, godownId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            val request = CreateRackRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() },
                godownId = godownId
            )

            rackRepository.createRack(request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isCreating = false)
                    loadRacks() // Refresh the list
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = exception.message ?: "Failed to create rack"
                    )
                }
            }
        }
    }

    fun updateRack(id: String, name: String, description: String, godownId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            val request = UpdateRackRequest(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() },
                godownId = godownId
            )

            rackRepository.updateRack(id, request).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadRacks() // Refresh the list
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = exception.message ?: "Failed to update rack"
                    )
                }
            }
        }
    }

    fun deleteRack(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            rackRepository.deleteRack(id).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    loadRacks() // Refresh the list
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message ?: "Failed to delete rack"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}