package com.kreeda.ankana.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kreeda.ankana.ui.screens.calendar.BookSlotScreen
import com.kreeda.ankana.ui.screens.calendar.CalendarScreen
import com.kreeda.ankana.ui.screens.challenges.AcceptChallengeScreen
import com.kreeda.ankana.ui.screens.challenges.ChallengesScreen
import com.kreeda.ankana.ui.screens.challenges.PostChallengeScreen
import com.kreeda.ankana.ui.screens.scores.PostScoreScreen
import com.kreeda.ankana.ui.screens.scores.ScoreWallScreen
import com.kreeda.ankana.ui.screens.settings.SettingsScreen

@Composable
fun KreedaNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar = topLevelDestinations.any { it.route.path == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { dest ->
                        val selected = currentRoute == dest.route.path
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(dest.route.path) {
                                        popUpTo(Route.Calendar.path) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors()
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Calendar.path,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Calendar.path) {
                CalendarScreen(
                    onBookSlot = { date, hour ->
                        navController.navigate(Route.BookSlot.route(date, hour))
                    }
                )
            }

            composable(Route.Challenges.path) {
                ChallengesScreen(
                    onPostChallenge = { navController.navigate(Route.PostChallenge.path) },
                    onAccept = { id -> navController.navigate(Route.AcceptChallenge.route(id)) }
                )
            }

            composable(Route.Scores.path) {
                ScoreWallScreen(
                    onPostScore = { navController.navigate(Route.PostScore.path) }
                )
            }

            composable(Route.Settings.path) {
                SettingsScreen()
            }

            composable(
                route = Route.BookSlot.path,
                arguments = listOf(
                    navArgument(Route.BookSlot.ARG_DATE) { type = NavType.StringType },
                    navArgument(Route.BookSlot.ARG_HOUR) { type = NavType.IntType }
                )
            ) { entry ->
                val date = entry.arguments?.getString(Route.BookSlot.ARG_DATE).orEmpty()
                val hour = entry.arguments?.getInt(Route.BookSlot.ARG_HOUR) ?: 6
                BookSlotScreen(
                    date = date,
                    hour = hour,
                    onDone = { navController.popBackStack() }
                )
            }

            composable(Route.PostChallenge.path) {
                PostChallengeScreen(onDone = { navController.popBackStack() })
            }

            composable(Route.PostScore.path) {
                PostScoreScreen(onDone = { navController.popBackStack() })
            }

            composable(
                route = Route.AcceptChallenge.path,
                arguments = listOf(navArgument(Route.AcceptChallenge.ARG_ID) { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong(Route.AcceptChallenge.ARG_ID) ?: 0L
                AcceptChallengeScreen(challengeId = id, onDone = { navController.popBackStack() })
            }
        }
    }
}
