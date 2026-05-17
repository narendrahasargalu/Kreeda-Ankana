package com.kreeda.ankana.ui.screens.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AcceptChallengeUiState(
    val teamName: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

@HiltViewModel
class AcceptChallengeViewModel @Inject constructor(
    private val repository: ChallengeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AcceptChallengeUiState())
    val state: StateFlow<AcceptChallengeUiState> = _state.asStateFlow()

    fun setTeamName(v: String) { _state.value = _state.value.copy(teamName = v, error = null) }

    fun accept(challengeId: Long) {
        val s = _state.value
        if (s.teamName.isBlank()) {
            _state.value = s.copy(error = "Your team name is required")
            return
        }
        if (s.saving) return
        _state.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            repository.accept(challengeId, s.teamName.trim())
            _state.value = _state.value.copy(saving = false, done = true)
        }
    }
}
