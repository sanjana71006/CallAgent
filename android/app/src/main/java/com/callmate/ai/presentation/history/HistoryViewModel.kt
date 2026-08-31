package com.callmate.ai.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callmate.ai.domain.model.Call
import com.callmate.ai.domain.model.CallCategory
import com.callmate.ai.domain.model.Importance
import com.callmate.ai.domain.model.TranscriptMessage
import com.callmate.ai.domain.repository.CallRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val calls: List<Call> = emptyList(),
    val filteredCalls: List<Call> = emptyList(),
    val selectedFilter: String = "All",
    val searchQuery: String = "",
    val selectedCall: Call? = null,
    val selectedCallTranscripts: List<TranscriptMessage> = emptyList(),
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadCalls()
    }

    private fun loadCalls() {
        viewModelScope.launch {
            callRepository.getAllCalls().collect { allCalls ->
                _uiState.update { current ->
                    current.copy(
                        calls = allCalls,
                        filteredCalls = applyFilters(allCalls, current.selectedFilter, current.searchQuery)
                    )
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { current ->
            current.copy(
                selectedFilter = filter,
                filteredCalls = applyFilters(current.calls, filter, current.searchQuery)
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredCalls = applyFilters(current.calls, current.selectedFilter, query)
            )
        }
    }

    fun loadCallDetails(callId: String) {
        viewModelScope.launch {
            callRepository.getCallById(callId).collect { call ->
                _uiState.update { it.copy(selectedCall = call) }
            }
        }
        viewModelScope.launch {
            callRepository.getTranscriptsForCall(callId).collect { transcripts ->
                _uiState.update { it.copy(selectedCallTranscripts = transcripts) }
            }
        }
    }

    fun deleteCall(callId: String) {
        viewModelScope.launch {
            callRepository.deleteCall(callId)
        }
    }

    private fun applyFilters(calls: List<Call>, filter: String, query: String): List<Call> {
        return calls.filter { call ->
            val matchesFilter = when (filter) {
                "All" -> true
                "Important" -> call.importance == Importance.HIGH || call.importance == Importance.URGENT
                "Spam" -> call.isSpam || call.category == CallCategory.SPAM
                "Work" -> call.category == CallCategory.WORK
                "Personal" -> call.category == CallCategory.PERSONAL
                "Delivery" -> call.category == CallCategory.DELIVERY
                "Recruitment" -> call.category == CallCategory.RECRUITMENT
                else -> true
            }

            val matchesQuery = query.isBlank() ||
                    call.callerName.contains(query, ignoreCase = true) ||
                    call.phoneNumber.contains(query, ignoreCase = true) ||
                    call.summary.contains(query, ignoreCase = true) ||
                    call.purpose.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }
}
