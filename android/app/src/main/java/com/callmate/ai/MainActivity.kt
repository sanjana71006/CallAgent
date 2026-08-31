package com.callmate.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.callmate.ai.core.theme.CallMateAITheme
import com.callmate.ai.domain.model.AppThemeMode
import com.callmate.ai.presentation.auth.AuthViewModel
import com.callmate.ai.presentation.call.CallViewModel
import com.callmate.ai.presentation.history.HistoryViewModel
import com.callmate.ai.presentation.home.HomeViewModel
import com.callmate.ai.presentation.navigation.NavGraph
import com.callmate.ai.presentation.settings.SettingsViewModel
import com.callmate.ai.presentation.settings.address.AddressViewModel

class MainActivity : ComponentActivity() {

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        val app = application as CallMateApp
        val homeViewModel = HomeViewModel(app.callRepository, app.settingsRepository)
        val callViewModel = CallViewModel(
            app.callProvider,
            app.callRepository,
            app.settingsRepository,
            app.speechToTextManager,
            app.textToSpeechManager
        )
        val historyViewModel = HistoryViewModel(app.callRepository)
        val authViewModel = AuthViewModel(app.authRepository)
        val settingsViewModel = SettingsViewModel(
            app.settingsRepository,
            app.callRepository,
            app.addressRepository,
            app.authRepository
        )
        val addressViewModel = AddressViewModel(app.addressRepository)

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (settingsUiState.themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> systemInDark
            }

            CallMateAITheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    callViewModel = callViewModel,
                    historyViewModel = historyViewModel,
                    settingsViewModel = settingsViewModel,
                    addressViewModel = addressViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
