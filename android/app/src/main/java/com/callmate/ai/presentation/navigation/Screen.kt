package com.callmate.ai.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object IncomingCall : Screen("incoming_call")
    object LiveCall : Screen("live_call/{callId}") {
        fun createRoute(callId: String) = "live_call/$callId"
    }
    object CallHistory : Screen("call_history")
    object CallDetails : Screen("call_details/{callId}") {
        fun createRoute(callId: String) = "call_details/$callId"
    }
    object Settings : Screen("settings")

    // "You" Section Sub-screens
    object PersonalDetails : Screen("personal_details")
    object YourInstructions : Screen("your_instructions")
    object AssistantHealthCheck : Screen("assistant_health_check")
    object SilentMode : Screen("silent_mode")
    object VoiceLanguage : Screen("voice_language")
    object YourAddresses : Screen("your_addresses")
    object AddEditAddress : Screen("add_edit_address?addressId={addressId}") {
        fun createRoute(addressId: String? = null): String {
            return if (addressId != null) "add_edit_address?addressId=$addressId" else "add_edit_address"
        }
    }
    object WhatsAppUpdates : Screen("whatsapp_updates")
    object AccountData : Screen("account_data")
    object HelpCenter : Screen("help_center")
}
