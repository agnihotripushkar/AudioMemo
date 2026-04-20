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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.example.audiomemo.R
import com.example.audiomemo.features.home.ui.HomeBottomNavBar
import com.example.audiomemo.features.home.ui.HomeTab
import com.example.audiomemo.features.meetings.ui.state.MeetingsDashboardUiState
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.domain.model.SessionState
import com.example.audiomemo.ui.theme.AccentGreen
import com.example.audiomemo.ui.theme.AudioMemoTheme
import com.example.audiomemo.ui.theme.NavyCard
import com.example.audiomemo.ui.theme.NavyElevated
import com.example.audiomemo.ui.theme.SuccessGreen
import com.example.audiomemo.ui.theme.TextSecondary
import com.example.audiomemo.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MeetingsDashboardScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onMeetingClick: (Long) -> Unit,
    viewModel: MeetingsDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MeetingsDashboardContent(
        uiState = uiState,
        onNavigateToHome = onNavigateToHome,
        onNavigateToSettings = onNavigateToSettings,
        onMeetingClick = onMeetingClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetingsDashboardContent(
    uiState: MeetingsDashboardUiState,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onMeetingClick: (Long) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.meetings_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            HomeBottomNavBar(
                selectedTab = HomeTab.Meetings,
                onHomeClick = onNavigateToHome,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is MeetingsDashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            }

            is MeetingsDashboardUiState.Success -> {
                if (uiState.meetings.isEmpty()) {
                    EmptyMeetingsContent(modifier = Modifier.padding(paddingValues))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
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
private fun EmptyMeetingsContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MicNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = stringResource(R.string.meetings_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.meetings_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
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
        colors = CardDefaults.cardColors(containerColor = NavyElevated),
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
                    .background(NavyCard, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = formatDay(item.startTime),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
                Text(
                    text = formatMonth(item.startTime),
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatTime(item.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (item.durationMs > 0) {
                        Text(text = "·", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        Text(
                            text = formatDuration(item.durationMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            SummaryStatusBadge(item.summaryStatus)
        }
    }
}

@Composable
private fun SummaryStatusBadge(status: SummaryStatus?) {
    when (status) {
        SummaryStatus.DONE -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = stringResource(R.string.meetings_cd_summary_ready),
            tint = SuccessGreen,
            modifier = Modifier.size(20.dp)
        )
        SummaryStatus.GENERATING, SummaryStatus.PENDING -> Icon(
            imageVector = Icons.Default.HourglassEmpty,
            contentDescription = stringResource(R.string.meetings_cd_generating),
            tint = AccentGreen,
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun MeetingsDashboardPreview() {
    AudioMemoTheme {
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
                        title = "Product Roadmap Discussion",
                        startTime = System.currentTimeMillis() - 86_400_000,
                        durationMs = 600_000,
                        sessionState = SessionState.STOPPED,
                        summaryStatus = SummaryStatus.GENERATING
                    )
                )
            ),
            onMeetingClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun MeetingsDashboardEmptyPreview() {
    AudioMemoTheme {
        MeetingsDashboardContent(
            uiState = MeetingsDashboardUiState.Success(meetings = emptyList()),
            onMeetingClick = {}
        )
    }
}
