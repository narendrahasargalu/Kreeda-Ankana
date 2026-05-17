package com.kreeda.ankana.ui.screens.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.Sport
import com.kreeda.ankana.data.model.Sports
import com.kreeda.ankana.data.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostChallengeUiState(
    val teamName: String = "",
    val sport: Sport = Sports.default,
    val preferredDate: String? = null,
    val preferredHour: Int? = null,
    val note: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val savedId: Long? = null
)

@HiltViewModel
class PostChallengeViewModel @Inject constructor(
    private val repository: ChallengeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PostChallengeUiState())
    val state: StateFlow<PostChallengeUiState> = _state.asStateFlow()

    fun setTeamName(v: String) { _state.value = _state.value.copy(teamName = v, error = null) }
    fun setSport(v: Sport) { _state.value = _state.value.copy(sport = v) }
    fun setPreferredDate(v: String?) { _state.value = _state.value.copy(preferredDate = v) }
    fun setPreferredHour(v: Int?) { _state.value = _state.value.copy(preferredHour = v) }
    fun setNote(v: String) { _state.value = _state.value.copy(note = v) }

    fun save() {
        val s = _state.value
        if (s.teamName.isBlank()) {
            _state.value = s.copy(error = "Your team name is required")
            return
        }
        if (s.saving) return
        _state.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            val id = repository.post(
                Challenge(
                    teamName = s.teamName.trim(),
                    sport = s.sport,
                    preferredDate = s.preferredDate,
                    preferredHour = s.preferredHour,
                    note = s.note.takeIf { it.isNotBlank() }?.trim()
                )
            )
            _state.value = _state.value.copy(saving = false, savedId = id)
        }
    }
}
