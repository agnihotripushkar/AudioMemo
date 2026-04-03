package com.example.audiomemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.audiomemo.ui.theme.AudioMemoTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.audiomemo.features.home.ui.HomeScreen
import com.example.audiomemo.features.meetings.ui.MeetingDetailsScreen
import com.example.audiomemo.features.meetings.ui.MeetingsDashboardScreen
import com.example.audiomemo.features.summary.ui.SummaryScreen
import com.example.audiomemo.features.transcript.ui.TranscriptScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AudioMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToTranscript = {
                                    navController.navigate("transcript")
                                },
                                onNavigateToMeetings = {
                                    navController.navigate("meetings")
                                },
                                onNavigateToMeetingDetails = { sessionId ->
                                    navController.navigate("meeting-details/$sessionId")
                                }
                            )
                        }
                        composable("transcript") {
                            TranscriptScreen(
                                onNavigateToSummary = { sessionId ->
                                    navController.navigate("summary/$sessionId")
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = "summary/{sessionId}",
                            arguments = listOf(
                                navArgument("sessionId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
                            SummaryScreen(
                                sessionId = sessionId,
                                onNavigateBack = {
                                    navController.popBackStack("home", inclusive = false)
                                },
                                onViewTranscript = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("meetings") {
                            MeetingsDashboardScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onMeetingClick = { sessionId ->
                                    navController.navigate("meeting-details/$sessionId")
                                }
                            )
                        }
                        composable(
                            route = "meeting-details/{sessionId}",
                            arguments = listOf(
                                navArgument("sessionId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
                            MeetingDetailsScreen(
                                sessionId = sessionId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}