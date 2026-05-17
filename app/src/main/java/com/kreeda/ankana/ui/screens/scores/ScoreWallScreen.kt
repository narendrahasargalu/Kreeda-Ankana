package com.kreeda.ankana.ui.screens.scores

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
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kreeda.ankana.data.model.Score
import com.kreeda.ankana.ui.components.EmptyState
import com.kreeda.ankana.ui.components.KreedaHeader
import com.kreeda.ankana.ui.components.SportTag
import com.kreeda.ankana.ui.components.TeamSearchField
import com.kreeda.ankana.ui.theme.ScorePodium
import com.kreeda.ankana.ui.util.DateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreWallScreen(
    onPostScore: () -> Unit,
    viewModel: ScoreWallViewModel = hiltViewModel()
) {
    val scores by viewModel.scores.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            KreedaHeader(title = "Score Wall", subtitle = "Recent village matches")
            TeamSearchField(query = query, onQueryChange = viewModel::setQuery)

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (scores.isEmpty()) {
                    EmptyState(
                        emoji = "🏆",
                        title = "No scores yet",
                        hint = "Pull to refresh, or tap + to post the first match."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(scores, key = { it.id }) { score -> ScoreCard(score) }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onPostScore,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Post Score", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun ScoreCard(score: Score) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    DateUtil.friendlyLabel(DateUtil.parseIso(score.date)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                SportTag(score.sport)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamSide(
                    team = score.teamA,
                    score = score.scoreA,
                    isWinner = score.winner == score.teamA,
                    modifier = Modifier.weight(1f),
                    align = TextAlign.Start
                )
                Text(
                    "vs",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                TeamSide(
                    team = score.teamB,
                    score = score.scoreB,
                    isWinner = score.winner == score.teamB,
                    modifier = Modifier.weight(1f),
                    align = TextAlign.End
                )
            }
            if (!score.note.isNullOrBlank()) {
                Text(
                    "“${score.note}”",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TeamSide(
    team: String,
    score: Int,
    isWinner: Boolean,
    modifier: Modifier,
    align: TextAlign
) {
    Column(modifier = modifier) {
        Text(
            text = team,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isWinner) FontWeight.Black else FontWeight.SemiBold,
            color = if (isWinner) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            textAlign = align,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = if (isWinner) ScorePodium else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = align,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
