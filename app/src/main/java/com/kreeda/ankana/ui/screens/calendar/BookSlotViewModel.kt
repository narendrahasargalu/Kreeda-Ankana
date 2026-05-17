package com.kreeda.ankana.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.model.Booking
import com.kreeda.ankana.data.model.Sport
import com.kreeda.ankana.data.model.Sports
import com.kreeda.ankana.data.repository.BookingRepository
import com.kreeda.ankana.data.repository.BookingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookSlotUiState(
    val teamName: String = "",
    val sport: Sport = Sports.default,
    val saving: Boolean = false,
    val error: String? = null,
    val savedId: Long? = null
)

@HiltViewModel
class BookSlotViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookSlotUiState())
    val state: StateFlow<BookSlotUiState> = _state.asStateFlow()

    fun setTeamName(name: String) {
        _state.value = _state.value.copy(teamName = name, error = null)
    }

    fun setSport(sport: Sport) {
        _state.value = _state.value.copy(sport = sport)
    }

    fun save(date: String, hour: Int) {
        val s = _state.value
        if (s.teamName.isBlank()) {
            _state.value = s.copy(error = "Team name is required")
            return
        }
        if (s.saving) return

        _state.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            val result = repository.book(
                Booking(teamName = s.teamName.trim(), sport = s.sport, date = date, hour = hour)
            )
            _state.value = when (result) {
                is BookingResult.Success -> _state.value.copy(saving = false, savedId = result.id)
                is BookingResult.SlotTaken -> _state.value.copy(
                    saving = false,
                    error = "This slot was just booked. Pick another."
                )
                is BookingResult.Error -> _state.value.copy(saving = false, error = result.message)
            }
        }
    }
}
