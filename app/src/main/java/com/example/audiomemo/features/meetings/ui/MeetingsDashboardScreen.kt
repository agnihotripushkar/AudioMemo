package com.example.audiomemo.features.meetings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.audiomemo.features.meetings.ui.state.MeetingsDashboardUiState
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.domain.model.SessionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val DarkTeal = Color(0xFF1A3D4E)
private val AccentOrange = Color(0xFFE8872A)

@Composable
fun MeetingsDashboardScreen(
    onNavigateBack: () -> Unit,
    onMeetingClick: (Long) -> Unit,
    viewModel: MeetingsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MeetingsDashboardContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onMeetingClick = onMeetingClick
    )
}

@Composable
private fun MeetingsDashboardContent(
    uiState: MeetingsDashboardUiState,
    onNavigateBack: () -> Unit,
    onMeetingClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        // Top bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkTeal
                    )
                }
                Text(
                    text = "Meetings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTeal
                )
            }
        }

        when (uiState) {
            is MeetingsDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MeetingsDashboardUiState.Success -> {
                if (uiState.meetings.isEmpty()) {
                    EmptyMeetingsContent()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(uiState.meetings, key = { it.sessionId }) { meeting ->
                            MeetingCard(
                                item = meeting,
                                onClick = { onMeetingClick(meeting.sessionId) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMeetingsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MicNone,
                contentDescription = null,
                tint = Color(0xFFBBBBBB),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No meetings yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF888888)
            )
            Text(
                text = "Capture your first note to get started",
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA)
            )
        }
    }
}

@Composable
private fun MeetingCard(
    item: MeetingListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color(0xFFEEF6FB), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = formatDay(item.startTime),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTeal
                )
                Text(
                    text = formatMonth(item.startTime),
                    fontSize = 11.sp,
                    color = Color(0xFF4A6E7E),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTime(item.startTime),
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                    if (item.durationMs > 0) {
                        Text(
                            text = "·",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                        Text(
                            text = formatDuration(item.durationMs),
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Summary status indicator
            SummaryStatusBadge(item.summaryStatus)
        }
    }
}

@Composable
private fun SummaryStatusBadge(status: SummaryStatus?) {
    when (status) {
        SummaryStatus.DONE -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Summary ready",
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(20.dp)
        )
        SummaryStatus.GENERATING, SummaryStatus.PENDING -> Icon(
            imageVector = Icons.Default.HourglassEmpty,
            contentDescription = "Summary generating",
            tint = AccentOrange,
            modifier = Modifier.size(20.dp)
        )
        else -> Unit
    }
}

private fun formatDay(ms: Long) =
    SimpleDateFormat("d", Locale.getDefault()).format(Date(ms))

private fun formatMonth(ms: Long) =
    SimpleDateFormat("MMM", Locale.getDefault()).format(Date(ms)).uppercase()

private fun formatTime(ms: Long) =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ms))

private fun formatDuration(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

@Preview(showBackground = true)
@Composable
private fun MeetingsDashboardPreview() {
    MaterialTheme {
        MeetingsDashboardContent(
            uiState = MeetingsDashboardUiState.Success(
                meetings = listOf(
                    MeetingListItem(
                        sessionId = 1L,
                        title = "Team Standup – Sprint Review",
                        startTime = System.currentTimeMillis() - 3_600_000,
                        durationMs = 1_800_000,
                        sessionState = SessionState.STOPPED,
                        summaryStatus = SummaryStatus.DONE
                    ),
                    MeetingListItem(
                        sessionId = 2L,
                        title = "Meeting – Mar 15, 2026 at 9:00 AM",
                        startTime = System.currentTimeMillis() - 86_400_000,
                        durationMs = 600_000,
                        sessionState = SessionState.STOPPED,
                        summaryStatus = SummaryStatus.GENERATING
                    )
                )
            ),
            onNavigateBack = {},
            onMeetingClick = {}
        )
    }
}
