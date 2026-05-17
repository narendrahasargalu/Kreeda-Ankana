package com.kreeda.ankana.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kreeda.ankana.data.model.Sport
import com.kreeda.ankana.data.model.Sports

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SportPicker(
    selected: Sport,
    onSelected: (Sport) -> Unit,
    modifier: Modifier = Modifier
) {
    var customMode by rememberSaveable { mutableStateOf(selected.isCustom) }
    var customText by rememberSaveable { mutableStateOf(if (selected.isCustom) selected.displayName else "") }

    val options = remember(selected) {
        buildList {
            addAll(Sports.predefined)
            if (selected.isCustom && Sports.predefined.none { it.id == selected.id }) {
                add(selected)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { sport ->
                FilterChip(
                    selected = !customMode && selected.id == sport.id,
                    onClick = {
                        customMode = false
                        onSelected(sport)
                    },
                    label = { Text("${sport.emoji} ${sport.displayName}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            FilterChip(
                selected = customMode,
                onClick = { customMode = true },
                label = { Text("➕ Other") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        if (customMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = {
                        customText = it
                        if (it.isNotBlank()) onSelected(Sport.custom(it))
                    },
                    label = { Text("Enter sport") },
                    placeholder = { Text("e.g. Lagori, Gilli Danda") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {}),
                    modifier = Modifier.weight(1f)
                )
                if (customText.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        customText = ""
                        customMode = false
                        onSelected(Sports.default)
                    }) { Text("Clear") }
                }
            }
        }
    }
}

@Composable
fun SportTag(sport: Sport, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                "${sport.emoji} ${sport.displayName}",
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier.padding(0.dp)
    )
}
