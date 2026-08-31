package com.callmate.ai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callmate.ai.domain.model.AssistantSettings
import com.callmate.ai.domain.model.Call
import com.callmate.ai.domain.model.Importance
import com.callmate.ai.domain.repository.CallRepository
import com.callmate.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val assistantEnabled: Boolean = true,
    val assistantName: String = "CallMate AI",
    val totalCallsCount: Int = 0,
    val screenedCount: Int = 0,
    val importantCount: Int = 0,
    val spamCount: Int = 0,
    val recentCalls: List<Call> = emptyList(),
    val isBackendHealthy: Boolean = true
)

class HomeViewModel(
    private val callRepository: CallRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        observeCalls()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        assistantEnabled = settings.enabled,
                        assistantName = settings.assistantName
                    )
                }
            }
        }
    }

    private fun observeCalls() {
        viewModelScope.launch {
            callRepository.getAllCalls().collect { calls ->
                val screened = calls.count { it.status == "SCREENED" || it.status == "COMPLETED" }
                val important = calls.count { it.importance == Importance.HIGH || it.importance == Importance.URGENT }
                val spam = calls.count { it.isSpam }

                _uiState.update {
                    it.copy(
                        totalCallsCount = calls.size,
                        screenedCount = screened,
                        importantCount = important,
                        spamCount = spam,
                        recentCalls = calls.take(5)
                    )
                }
            }
        }
    }

    fun toggleAssistant(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAssistantEnabled(enabled)
        }
    }
}
