package com.callmate.ai.data.repository

import com.callmate.ai.data.local.dao.AddressDao
import com.callmate.ai.data.local.entity.AddressEntity
import com.callmate.ai.domain.model.UserAddress
import com.callmate.ai.domain.repository.AddressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddressRepositoryImpl(
    private val addressDao: AddressDao
) : AddressRepository {

    override fun getAddresses(): Flow<List<UserAddress>> {
        return addressDao.getAllAddresses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAddressById(id: String): UserAddress? {
        return addressDao.getAddressById(id)?.toDomain()
    }

    override suspend fun saveAddress(address: UserAddress) {
        addressDao.insertAddress(AddressEntity.fromDomain(address))
    }

    override suspend fun deleteAddress(id: String) {
        addressDao.deleteAddressById(id)
    }

    override suspend fun clearAllAddresses() {
        addressDao.clearAllAddresses()
    }
}
