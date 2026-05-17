package com.kreeda.ankana.ui.screens.scores

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kreeda.ankana.ui.components.KreedaHeader
import com.kreeda.ankana.ui.components.SportPicker
import com.kreeda.ankana.ui.util.DateUtil
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

@Composable
fun PostScoreScreen(
    onDone: () -> Unit,
    viewModel: PostScoreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedId) { if (state.savedId != null) onDone() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        KreedaHeader(title = "Post a Score", subtitle = "Add to the village wall")

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("Teams")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.teamA,
                    onValueChange = viewModel::setTeamA,
                    label = { Text("Team A") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.teamB,
                    onValueChange = viewModel::setTeamB,
                    label = { Text("Team B") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            SectionLabel("Score")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.scoreA,
                    onValueChange = viewModel::setScoreA,
                    label = { Text(state.teamA.ifBlank { "Team A" }) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text("–", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(
                    value = state.scoreB,
                    onValueChange = viewModel::setScoreB,
                    label = { Text(state.teamB.ifBlank { "Team B" }) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionLabel("Sport")
            SportPicker(selected = state.sport, onSelected = viewModel::setSport)

            SectionLabel("Match day")
            DayBackPicker(selected = state.date, onSelected = viewModel::setDate)

            SectionLabel("Note (optional)")
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = { Text("e.g. Final over thriller") },
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
                    if (state.saving) "Posting…" else "Post score",
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
private fun DayBackPicker(selected: String, onSelected: (String) -> Unit) {
    val today = remember { DateUtil.today() }
    val options = remember {
        (0..6).map { offset ->
            val d = today.minus(offset, DateTimeUnit.DAY)
            DateUtil.friendlyLabel(d) to d.toString()
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
