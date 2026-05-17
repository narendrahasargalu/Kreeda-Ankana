package com.kreeda.ankana.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kreeda.ankana.ui.components.KreedaHeader
import com.kreeda.ankana.ui.components.SportPicker
import com.kreeda.ankana.ui.util.DateUtil

@Composable
fun BookSlotScreen(
    date: String,
    hour: Int,
    onDone: () -> Unit,
    viewModel: BookSlotViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedId) {
        if (state.savedId != null) onDone()
    }

    val parsed = DateUtil.parseIso(date)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        KreedaHeader(
            title = "Book this slot",
            subtitle = "${DateUtil.friendlyLabel(parsed)} • ${DateUtil.slotRangeLabel(hour)}"
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Who's playing?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = state.teamName,
                onValueChange = viewModel::setTeamName,
                label = { Text("Team name") },
                placeholder = { Text("e.g. Banashankari Boys") },
                singleLine = true,
                isError = state.error != null && state.teamName.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Pick a sport",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SportPicker(
                selected = state.sport,
                onSelected = viewModel::setSport
            )

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.save(date = date, hour = hour) },
                enabled = !state.saving,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (state.saving) "Saving…" else "Book slot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

