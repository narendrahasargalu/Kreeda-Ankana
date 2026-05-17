package com.kreeda.ankana.ui.screens.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.ChallengeStatus
import com.kreeda.ankana.ui.components.EmptyState
import com.kreeda.ankana.ui.components.KreedaHeader
import com.kreeda.ankana.ui.components.SportTag
import com.kreeda.ankana.ui.components.TeamSearchField
import com.kreeda.ankana.ui.util.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    onPostChallenge: () -> Unit,
    onAccept: (Long) -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            KreedaHeader(
                title = "Challenges",
                subtitle = "Open friendly matches"
            )
            TeamSearchField(query = query, onQueryChange = viewModel::setQuery)

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (challenges.isEmpty()) {
                    EmptyState(
                        emoji = "🏟️",
                        title = "No open challenges",
                        hint = "Pull to refresh, or tap + to post a challenge."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(challenges, key = { it.id }) { challenge ->
                            ChallengeCard(
                                challenge = challenge,
                                onAccept = { onAccept(challenge.id) },
                                onClose = { viewModel.close(challenge.id) }
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onPostChallenge,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(
                "Post Challenge",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge, onAccept: () -> Unit, onClose: () -> Unit) {
    val isOpen = challenge.status == ChallengeStatus.OPEN
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpen) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = challenge.teamName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                SportTag(challenge.sport)
            }

            val whenText = buildString {
                val d = challenge.preferredDate
                val h = challenge.preferredHour
                when {
                    d != null && h != null -> append("Wants ${DateUtil.friendlyLabel(DateUtil.parseIso(d))} at ${DateUtil.hour12(h)}")
                    d != null -> append("Wants ${DateUtil.friendlyLabel(DateUtil.parseIso(d))}")
                    h != null -> append("Wants ${DateUtil.hour12(h)} any day")
                    else -> append("Any day, any time")
                }
            }
            Text(
                text = whenText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (!challenge.note.isNullOrBlank()) {
                Text(
                    text = "“${challenge.note}”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            when (challenge.status) {
                ChallengeStatus.OPEN -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Accept", fontWeight = FontWeight.Bold) }
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Close") }
                    }
                }
                ChallengeStatus.ACCEPTED -> {
                    Text(
                        text = "✅ Accepted by ${challenge.acceptedBy ?: "rival team"}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                ChallengeStatus.CLOSED -> {
                    Text(
                        text = "Closed",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}
