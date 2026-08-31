package com.callmate.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callmate.ai.domain.model.UserAddress

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val label: String, // Home, College, Work, Other
    val addressName: String,
    val fullAddress: String,
    val additionalDetails: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): UserAddress {
        return UserAddress(
            id = id,
            label = label,
            addressLine = fullAddress,
            addressName = addressName,
            deliveryInstructions = additionalDetails
        )
    }

    companion object {
        fun fromDomain(domain: UserAddress): AddressEntity {
            return AddressEntity(
                id = domain.id,
                label = domain.label,
                addressName = domain.addressName,
                fullAddress = domain.addressLine,
                additionalDetails = domain.deliveryInstructions,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
