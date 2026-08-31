package com.callmate.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.callmate.ai.presentation.auth.AuthState
import com.callmate.ai.presentation.auth.AuthViewModel
import com.callmate.ai.presentation.auth.LoginScreen
import com.callmate.ai.presentation.auth.RegisterScreen
import com.callmate.ai.presentation.call.CallViewModel
import com.callmate.ai.presentation.call.IncomingCallScreen
import com.callmate.ai.presentation.call.LiveCallScreen
import com.callmate.ai.presentation.history.CallDetailsScreen
import com.callmate.ai.presentation.history.CallHistoryScreen
import com.callmate.ai.presentation.history.HistoryViewModel
import com.callmate.ai.presentation.home.HomeScreen
import com.callmate.ai.presentation.home.HomeViewModel
import com.callmate.ai.presentation.onboarding.OnboardingScreen
import com.callmate.ai.presentation.settings.SettingsScreen
import com.callmate.ai.presentation.settings.SettingsViewModel
import com.callmate.ai.presentation.settings.account.AccountDataScreen
import com.callmate.ai.presentation.settings.address.AddEditAddressScreen
import com.callmate.ai.presentation.settings.address.AddressViewModel
import com.callmate.ai.presentation.settings.address.YourAddressesScreen
import com.callmate.ai.presentation.settings.health.AssistantHealthCheckScreen
import com.callmate.ai.presentation.settings.help.HelpCenterScreen
import com.callmate.ai.presentation.settings.instructions.YourInstructionsScreen
import com.callmate.ai.presentation.settings.personal.PersonalDetailsScreen
import com.callmate.ai.presentation.settings.silent.SilentModeScreen
import com.callmate.ai.presentation.settings.voice.VoiceLanguageScreen
import com.callmate.ai.presentation.settings.whatsapp.WhatsAppUpdatesScreen
import com.callmate.ai.presentation.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    callViewModel: CallViewModel,
    historyViewModel: HistoryViewModel,
    settingsViewModel: SettingsViewModel,
    addressViewModel: AddressViewModel,
    authViewModel: AuthViewModel,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val isLoggedIn = authViewModel.uiState.value.isLoggedIn || authViewModel.authState.value is AuthState.Authenticated
                    if (isLoggedIn) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    val isLoggedIn = authViewModel.uiState.value.isLoggedIn
                    if (isLoggedIn) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToIncomingCall = {
                    callViewModel.startSimulatedCall()
                    navController.navigate(Screen.IncomingCall.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.CallHistory.route)
                },
                onNavigateToCallDetails = { callId ->
                    navController.navigate(Screen.CallDetails.createRoute(callId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.IncomingCall.route) {
            IncomingCallScreen(
                viewModel = callViewModel,
                onAnswerTapped = {
                    val currentCall = callViewModel.uiState.value.currentCall
                    val callId = currentCall?.id ?: "sim-001"
                    navController.navigate(Screen.LiveCall.createRoute(callId)) {
                        popUpTo(Screen.IncomingCall.route) { inclusive = true }
                    }
                },
                onDeclineTapped = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.LiveCall.route,
            arguments = listOf(navArgument("callId") { type = NavType.StringType })
        ) { backStackEntry ->
            LiveCallScreen(
                viewModel = callViewModel,
                onCallEnded = {
                    val callId = backStackEntry.arguments?.getString("callId") ?: ""
                    if (callId.isNotEmpty()) {
                        navController.navigate(Screen.CallDetails.createRoute(callId)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    } else {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                }
            )
        }

        composable(Screen.CallHistory.route) {
            CallHistoryScreen(
                viewModel = historyViewModel,
                onNavigateToCallDetails = { callId ->
                    navController.navigate(Screen.CallDetails.createRoute(callId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.CallDetails.route,
            arguments = listOf(navArgument("callId") { type = NavType.StringType })
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            CallDetailsScreen(
                callId = callId,
                viewModel = historyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // You Screen (Root)
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToPersonalDetails = {
                    navController.navigate(Screen.PersonalDetails.route)
                },
                onNavigateToYourInstructions = {
                    navController.navigate(Screen.YourInstructions.route)
                },
                onNavigateToHealthCheck = {
                    navController.navigate(Screen.AssistantHealthCheck.route)
                },
                onNavigateToSilentMode = {
                    navController.navigate(Screen.SilentMode.route)
                },
                onNavigateToVoiceLanguage = {
                    navController.navigate(Screen.VoiceLanguage.route)
                },
                onNavigateToYourAddresses = {
                    navController.navigate(Screen.YourAddresses.route)
                },
                onNavigateToWhatsAppUpdates = {
                    navController.navigate(Screen.WhatsAppUpdates.route)
                },
                onNavigateToAccountData = {
                    navController.navigate(Screen.AccountData.route)
                },
                onNavigateToHelpCenter = {
                    navController.navigate(Screen.HelpCenter.route)
                }
            )
        }

        // Sub-screens of "You" Section
        composable(Screen.PersonalDetails.route) {
            PersonalDetailsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.YourInstructions.route) {
            YourInstructionsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AssistantHealthCheck.route) {
            AssistantHealthCheckScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SilentMode.route) {
            SilentModeScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VoiceLanguage.route) {
            VoiceLanguageScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.YourAddresses.route) {
            YourAddressesScreen(
                viewModel = addressViewModel,
                onNavigateToAddAddress = {
                    navController.navigate(Screen.AddEditAddress.createRoute())
                },
                onNavigateToEditAddress = { addressId ->
                    navController.navigate(Screen.AddEditAddress.createRoute(addressId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditAddress.route,
            arguments = listOf(
                navArgument("addressId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getString("addressId")
            AddEditAddressScreen(
                addressId = addressId,
                viewModel = addressViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.WhatsAppUpdates.route) {
            WhatsAppUpdatesScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AccountData.route) {
            AccountDataScreen(
                viewModel = settingsViewModel,
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HelpCenter.route) {
            HelpCenterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
