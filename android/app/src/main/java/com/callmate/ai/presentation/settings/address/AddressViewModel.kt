package com.callmate.ai.presentation.settings.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callmate.ai.domain.model.UserAddress
import com.callmate.ai.domain.repository.AddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddressUiState(
    val addresses: List<UserAddress> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AddressViewModel(
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    init {
        loadAddresses()
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            addressRepository.getAddresses().collect { list ->
                _uiState.update { it.copy(addresses = list, isLoading = false) }
            }
        }
    }

    suspend fun getAddressById(id: String): UserAddress? {
        return addressRepository.getAddressById(id)
    }

    fun saveAddress(
        id: String?,
        label: String,
        addressName: String,
        fullAddress: String,
        additionalDetails: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedName = addressName.trim()
        val trimmedAddress = fullAddress.trim()

        if (trimmedName.isEmpty()) {
            onError("Please enter a name for this address (e.g., My Apartment).")
            return
        }
        if (trimmedAddress.isEmpty()) {
            onError("Please enter the complete street address.")
            return
        }

        viewModelScope.launch {
            try {
                val addressToSave = UserAddress(
                    id = id ?: UUID.randomUUID().toString(),
                    label = label,
                    addressName = trimmedName,
                    addressLine = trimmedAddress,
                    deliveryInstructions = additionalDetails.trim()
                )
                addressRepository.saveAddress(addressToSave)
                _uiState.update { it.copy(successMessage = "Address saved successfully") }
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to save address: ${e.localizedMessage}")
            }
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            try {
                addressRepository.deleteAddress(id)
                _uiState.update { it.copy(successMessage = "Address deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete address: ${e.localizedMessage}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
