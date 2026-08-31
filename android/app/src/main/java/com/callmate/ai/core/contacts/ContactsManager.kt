package com.callmate.ai.core.contacts

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

data class PhoneContact(
    val id: String,
    val name: String,
    val phoneNumber: String
)

class ContactsManager(private val context: Context) {

    /**
     * Resolves caller name from device contacts given a phone number.
     * Returns the contact name if found, or null if it's an unknown caller.
     */
    fun resolveContactName(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        try {
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        return it.getString(nameIndex)
                    }
                }
            }
        } catch (e: SecurityException) {
            return null
        } catch (e: Exception) {
            return null
        }
        return null
    }

    /**
     * Fetches all device contacts with phone numbers.
     */
    fun getAllContacts(): List<PhoneContact> {
        val contactsList = mutableListOf<PhoneContact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val id = if (idIdx != -1) it.getString(idIdx) else ""
                    val name = if (nameIdx != -1) it.getString(nameIdx) else "Unknown"
                    val number = if (numIdx != -1) it.getString(numIdx) else ""
                    if (number.isNotBlank()) {
                        contactsList.add(PhoneContact(id, name, number))
                    }
                }
            }
        } catch (e: Exception) {
        }

        return contactsList
    }
}
