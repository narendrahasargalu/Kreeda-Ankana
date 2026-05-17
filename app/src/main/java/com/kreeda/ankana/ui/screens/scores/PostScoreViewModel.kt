package com.kreeda.ankana.ui.screens.scores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.model.Score
import com.kreeda.ankana.data.model.Sport
import com.kreeda.ankana.data.model.Sports
import com.kreeda.ankana.data.repository.ScoreRepository
import com.kreeda.ankana.ui.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostScoreUiState(
    val teamA: String = "",
    val teamB: String = "",
    val scoreA: String = "",
    val scoreB: String = "",
    val sport: Sport = Sports.default,
    val date: String = DateUtil.todayIso(),
    val note: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val savedId: Long? = null
)

@HiltViewModel
class PostScoreViewModel @Inject constructor(
    private val repository: ScoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PostScoreUiState())
    val state: StateFlow<PostScoreUiState> = _state.asStateFlow()

    fun setTeamA(v: String) { _state.value = _state.value.copy(teamA = v, error = null) }
    fun setTeamB(v: String) { _state.value = _state.value.copy(teamB = v, error = null) }
    fun setScoreA(v: String) {
        if (v.length <= 4 && v.all(Char::isDigit)) _state.value = _state.value.copy(scoreA = v, error = null)
    }
    fun setScoreB(v: String) {
        if (v.length <= 4 && v.all(Char::isDigit)) _state.value = _state.value.copy(scoreB = v, error = null)
    }
    fun setSport(v: Sport) { _state.value = _state.value.copy(sport = v) }
    fun setDate(v: String) { _state.value = _state.value.copy(date = v) }
    fun setNote(v: String) { _state.value = _state.value.copy(note = v) }

    fun save() {
        val s = _state.value
        if (s.teamA.isBlank() || s.teamB.isBlank()) {
            _state.value = s.copy(error = "Both team names are required")
            return
        }
        val a = s.scoreA.toIntOrNull()
        val b = s.scoreB.toIntOrNull()
        if (a == null || b == null) {
            _state.value = s.copy(error = "Enter scores for both teams")
            return
        }
        if (s.saving) return
        _state.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            val id = repository.post(
                Score(
                    teamA = s.teamA.trim(),
                    teamB = s.teamB.trim(),
                    scoreA = a,
                    scoreB = b,
                    sport = s.sport,
                    date = s.date,
                    note = s.note.takeIf { it.isNotBlank() }?.trim()
                )
            )
            _state.value = _state.value.copy(saving = false, savedId = id)
        }
    }
}
