package com.kreeda.ankana.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kreeda.ankana.data.model.Booking
import com.kreeda.ankana.ui.components.KreedaHeader
import com.kreeda.ankana.ui.theme.BookedSlot
import com.kreeda.ankana.ui.theme.FreeSlot
import com.kreeda.ankana.ui.util.DateUtil
import com.kreeda.ankana.ui.util.GROUND_HOURS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBookSlot: (date: String, hour: Int) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val byHour = remember(bookings) { bookings.associateBy { it.hour } }

    var pickerOpen by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        KreedaHeader(title = "The Ground", subtitle = "Village notice board")

        DateBar(
            label = "${DateUtil.friendlyLabel(date)} • ${DateUtil.shortLabel(date)}",
            onPrev = viewModel::showPrevDay,
            onNext = viewModel::showNextDay,
            onPickDate = { pickerOpen = true }
        )

        if (pickerOpen) {
            DatePickerSheet(
                initial = date,
                onDismiss = { pickerOpen = false },
                onConfirm = { picked ->
                    viewModel.setDate(picked)
                    pickerOpen = false
                }
            )
        }

        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(GROUND_HOURS.toList()) { hour ->
                    val booking = byHour[hour]
                    SlotCard(
                        hour = hour,
                        booking = booking,
                        onClick = { if (booking == null) onBookSlot(date.toString(), hour) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBar(label: String, onPrev: () -> Unit, onNext: () -> Unit, onPickDate: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day")
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
            AssistChip(
                onClick = onPickDate,
                label = { Text("Pick date") },
                leadingIcon = { Icon(Icons.Filled.Today, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next day")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initial: kotlinx.datetime.LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (kotlinx.datetime.LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toUtcMillis(initial)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) onConfirm(DateUtil.fromUtcMillis(millis)) else onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun SlotCard(hour: Int, booking: Booking?, onClick: () -> Unit) {
    val booked = booking != null
    val container = if (booked) BookedSlot else FreeSlot

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HourChip(hour = hour, booked = booked)
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                if (booking != null) {
                    Text(
                        text = booking.teamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${booking.sport.emoji} ${booking.sport.displayName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Free slot",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to book",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!booked) {
                PlusBadge()
            }
        }
    }
}

@Composable
private fun HourChip(hour: Int, booked: Boolean) {
    val bg = if (booked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DateUtil.hour12(hour),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlusBadge() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
    }
}
