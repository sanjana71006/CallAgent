package com.callmate.ai.domain.repository

import com.callmate.ai.domain.model.UserAddress
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    fun getAddresses(): Flow<List<UserAddress>>
    suspend fun getAddressById(id: String): UserAddress?
    suspend fun saveAddress(address: UserAddress)
    suspend fun deleteAddress(id: String)
    suspend fun clearAllAddresses()
}
