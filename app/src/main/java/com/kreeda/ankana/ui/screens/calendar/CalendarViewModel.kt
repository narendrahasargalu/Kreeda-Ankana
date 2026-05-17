package com.kreeda.ankana.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreeda.ankana.data.model.Booking
import com.kreeda.ankana.data.repository.BookingRepository
import com.kreeda.ankana.ui.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate>(DateUtil.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val bookings: StateFlow<List<Booking>> = _selectedDate
        .flatMapLatest { date -> repository.observeForDate(date.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            repository.refresh()
            _refreshing.value = false
        }
    }

    fun showPrevDay() {
        _selectedDate.value = DateUtil.prevDay(_selectedDate.value)
    }

    fun showNextDay() {
        _selectedDate.value = DateUtil.nextDay(_selectedDate.value)
    }

    fun jumpToToday() {
        _selectedDate.value = DateUtil.today()
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }
}
