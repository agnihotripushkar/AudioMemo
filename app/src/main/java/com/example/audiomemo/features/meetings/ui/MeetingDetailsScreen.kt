package com.example.audiomemo.features.meetings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.audiomemo.features.meetings.ui.state.MeetingDetailsUiState
import com.example.audiomemo.features.summary.domain.model.Summary
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.domain.model.Transcript
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val DarkTeal = Color(0xFF1A3D4E)

@Composable
fun MeetingDetailsScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MeetingDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) {
        viewModel.setSessionId(sessionId)
    }

    val uiState by viewModel.uiState.collectAsState()

    MeetingDetailsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun MeetingDetailsContent(
    uiState: MeetingDetailsUiState,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        when (uiState) {
            is MeetingDetailsUiState.Loading -> {
                // Top bar placeholder
                Surface(color = Color.White, shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkTeal)
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MeetingDetailsUiState.Success -> {
                MeetingDetailsSuccess(
                    state = uiState,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun MeetingDetailsSuccess(
    state: MeetingDetailsUiState.Success,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = buildList {
        add("Transcript")
        if (state.summary != null) add("Summary")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with title
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkTeal)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.sessionTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTeal,
                            maxLines = 2
                        )
                        Text(
                            text = buildSubtitle(state.startTime, state.durationMs),
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }

                // Tab row
                if (tabs.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = DarkTeal,
                        edgePadding = 16.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tab content
        when (tabs.getOrNull(selectedTab)) {
            "Transcript" -> TranscriptTab(transcripts = state.transcripts)
            "Summary" -> SummaryTab(summary = state.summary!!)
        }
    }
}

@Composable
private fun TranscriptTab(transcripts: List<Transcript>) {
    if (transcripts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No transcript available yet",
                color = Color(0xFF888888),
                fontSize = 15.sp
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            transcripts.forEach { transcript ->
                TranscriptChunkCard(transcript = transcript)
            }
        }
    }
}

@Composable
private fun TranscriptChunkCard(transcript: Transcript) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Chunk ${transcript.chunkIndex + 1}",
                fontSize = 11.sp,
                color = Color(0xFFAAAAAA),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = transcript.text,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SummaryTab(summary: Summary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (summary.status == SummaryStatus.GENERATING || summary.status == SummaryStatus.PENDING) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Text("Generating summary…", color = Color(0xFF888888), fontSize = 14.sp)
                }
            }
            return
        }

        if (summary.title.isNotBlank()) {
            Text(
                text = summary.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DarkTeal
            )
            Divider()
        }

        if (summary.summary.isNotBlank()) {
            SummarySection(title = "Summary", body = summary.summary)
        }

        if (summary.keyPoints.isNotBlank()) {
            SummarySection(title = "Key Points", body = summary.keyPoints)
        }

        if (summary.actionItems.isNotBlank()) {
            SummarySection(title = "Action Items", body = summary.actionItems)
        }
    }
}

@Composable
private fun SummarySection(title: String, body: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A6E7E)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
        }
    }
}

private fun buildSubtitle(startTimeMs: Long, durationMs: Long): String {
    val dateStr = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        .format(Date(startTimeMs))
    return if (durationMs > 0) {
        val mins = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val secs = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        val dur = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        "$dateStr · $dur"
    } else {
        dateStr
    }
}

@Preview(showBackground = true)
@Composable
private fun MeetingDetailsPreview() {
    MaterialTheme {
        MeetingDetailsContent(
            uiState = MeetingDetailsUiState.Success(
                sessionTitle = "Team Standup – Sprint Review",
                startTime = System.currentTimeMillis() - 3_600_000,
                durationMs = 1_800_000,
                transcripts = listOf(
                    Transcript(id = 1, sessionId = 1, chunkIndex = 0, text = "Hey team, let's start the standup.", createdAt = System.currentTimeMillis()),
                    Transcript(id = 2, sessionId = 1, chunkIndex = 1, text = "I worked on the dashboard feature yesterday.", createdAt = System.currentTimeMillis())
                ),
                summary = Summary(
                    id = 1,
                    sessionId = 1,
                    title = "Team Standup",
                    summary = "Reviewed sprint progress and identified blockers.",
                    keyPoints = "• Sprint velocity up 15%\n• Two bugs resolved",
                    actionItems = "- Update test coverage\n- Schedule retrospective",
                    status = SummaryStatus.DONE
                )
            ),
            onNavigateBack = {}
        )
    }
}
