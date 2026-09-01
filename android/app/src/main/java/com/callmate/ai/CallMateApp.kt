package com.callmate.ai

import android.app.Application
import com.callmate.ai.core.audio.SpeechToTextManager
import com.callmate.ai.core.audio.TextToSpeechManager
import com.callmate.ai.core.network.TokenManager
import com.callmate.ai.core.telephony.CallProvider
import com.callmate.ai.core.telephony.SimulatorCallProvider
import com.callmate.ai.data.local.CallMateDatabase
import com.callmate.ai.data.repository.AddressRepositoryImpl
import com.callmate.ai.data.repository.AuthRepositoryImpl
import com.callmate.ai.data.repository.CallRepositoryImpl
import com.callmate.ai.data.repository.SettingsRepositoryImpl
import com.callmate.ai.domain.repository.AddressRepository
import com.callmate.ai.domain.repository.AuthRepository
import com.callmate.ai.domain.repository.CallRepository
import com.callmate.ai.domain.repository.SettingsRepository

class CallMateApp : Application() {

    lateinit var database: CallMateDatabase
        private set

    lateinit var tokenManager: TokenManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var callRepository: CallRepository
        private set

    lateinit var addressRepository: AddressRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var callProvider: CallProvider
        private set

    lateinit var speechToTextManager: SpeechToTextManager
        private set

    lateinit var textToSpeechManager: TextToSpeechManager
        private set

    lateinit var contactsManager: com.callmate.ai.core.contacts.ContactsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = CallMateDatabase.getDatabase(this)
        tokenManager = TokenManager(this)
        settingsRepository = SettingsRepositoryImpl(this)
        authRepository = AuthRepositoryImpl(tokenManager, database.userProfileDao(), settingsRepository)

        callRepository = CallRepositoryImpl(database.callDao(), database.transcriptDao())
        addressRepository = AddressRepositoryImpl(database.addressDao())
        callProvider = SimulatorCallProvider()
        contactsManager = com.callmate.ai.core.contacts.ContactsManager(this)

        speechToTextManager = SpeechToTextManager(this)
        textToSpeechManager = TextToSpeechManager(this).apply {
            initialize()
        }
    }

    companion object {
        lateinit var instance: CallMateApp
            private set
    }
}
