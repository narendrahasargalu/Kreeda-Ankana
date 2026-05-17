package com.kreeda.ankana.ui.screens.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kreeda.ankana.ui.components.KreedaHeader
import com.kreeda.ankana.ui.components.SportPicker
import com.kreeda.ankana.ui.util.DateUtil
import com.kreeda.ankana.ui.util.GROUND_HOURS
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

@Composable
fun PostChallengeScreen(
    onDone: () -> Unit,
    viewModel: PostChallengeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedId) { if (state.savedId != null) onDone() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        KreedaHeader(title = "Post a Challenge", subtitle = "Find someone to play")

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("Your team")
            OutlinedTextField(
                value = state.teamName,
                onValueChange = viewModel::setTeamName,
                label = { Text("Team name") },
                singleLine = true,
                isError = state.error != null && state.teamName.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel("Sport")
            SportPicker(selected = state.sport, onSelected = viewModel::setSport)

            SectionLabel("Preferred day (optional)")
            DayPicker(selected = state.preferredDate, onSelected = viewModel::setPreferredDate)

            SectionLabel("Preferred hour (optional)")
            HourPicker(selected = state.preferredHour, onSelected = viewModel::setPreferredHour)

            SectionLabel("Message (optional)")
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = { Text("Add a short note") },
                placeholder = { Text("e.g. Looking for a strong batting side") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)

            Button(
                onClick = viewModel::save,
                enabled = !state.saving,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    if (state.saving) "Posting…" else "Post challenge",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun DayPicker(selected: String?, onSelected: (String?) -> Unit) {
    val today = remember { DateUtil.today() }
    val options = remember {
        buildList {
            add("Any" to null)
            (0..6).forEach { offset ->
                val d = today.plus(offset, DateTimeUnit.DAY)
                add(DateUtil.friendlyLabel(d) to d.toString())
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (label, value) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun HourPicker(selected: Int?, onSelected: (Int?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text("Any") }
        )
        GROUND_HOURS.forEach { hour ->
            FilterChip(
                selected = selected == hour,
                onClick = { onSelected(hour) },
                label = { Text(DateUtil.hour12(hour)) }
            )
        }
    }
}
