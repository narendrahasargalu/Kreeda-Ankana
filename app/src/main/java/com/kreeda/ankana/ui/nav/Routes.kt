package com.kreeda.ankana.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val path: String) {
    data object Calendar : Route("calendar")
    data object Challenges : Route("challenges")
    data object Scores : Route("scores")
    data object Settings : Route("settings")

    /** Pre-fills date + hour from a calendar tap. */
    data object BookSlot : Route("book_slot/{date}/{hour}") {
        fun route(date: String, hour: Int) = "book_slot/$date/$hour"
        const val ARG_DATE = "date"
        const val ARG_HOUR = "hour"
    }

    /** Pre-fills the team that wants to accept this challenge. */
    data object AcceptChallenge : Route("accept_challenge/{id}") {
        fun route(id: Long) = "accept_challenge/$id"
        const val ARG_ID = "id"
    }

    data object PostChallenge : Route("post_challenge")
    data object PostScore : Route("post_score")
}

data class TopLevelDestination(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

val topLevelDestinations = listOf(
    TopLevelDestination(Route.Calendar, "Ground", Icons.Filled.CalendarMonth),
    TopLevelDestination(Route.Challenges, "Challenges", Icons.Filled.SportsCricket),
    TopLevelDestination(Route.Scores, "Scores", Icons.Filled.EmojiEvents),
    TopLevelDestination(Route.Settings, "Settings", Icons.Filled.Settings)
)
