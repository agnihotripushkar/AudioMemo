package com.example.audiomemo.features.meetings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audiomemo.R
import com.example.audiomemo.features.meetings.ui.state.MeetingDetailsUiState
import com.example.audiomemo.features.summary.domain.model.Summary
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.domain.model.Transcript
import com.example.audiomemo.ui.theme.AudioMemoTheme
import com.example.audiomemo.ui.theme.DividerColor
import com.example.audiomemo.ui.theme.NavyElevated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MeetingDetailsScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MeetingDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) { viewModel.setSessionId(sessionId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MeetingDetailsContent(uiState = uiState, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetingDetailsContent(
    uiState: MeetingDetailsUiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            when (uiState) {
                is MeetingDetailsUiState.Loading -> {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
                is MeetingDetailsUiState.Success -> {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = uiState.sessionTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2
                                )
                                Text(
                                    text = buildSubtitle(uiState.startTime, uiState.durationMs),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is MeetingDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is MeetingDetailsUiState.Success -> {
                MeetingDetailsSuccess(
                    state = uiState,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun MeetingDetailsSuccess(
    state: MeetingDetailsUiState.Success,
    modifier: Modifier = Modifier
) {
    val tabTranscript = stringResource(R.string.meeting_details_tab_transcript)
    val tabSummary = stringResource(R.string.meeting_details_tab_summary)

    val tabs = buildList {
        add(tabTranscript)
        if (state.summary != null) add(tabSummary)
    }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        if (tabs.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = { HorizontalDivider(color = DividerColor) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> TranscriptTab(transcripts = state.transcripts)
            1 -> SummaryTab(summary = state.summary!!)
        }
    }
}

@Composable
private fun TranscriptTab(transcripts: List<Transcript>) {
    if (transcripts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.meeting_details_no_transcript),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyElevated, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = stringResource(R.string.meeting_details_chunk_label, transcript.chunkIndex + 1),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = transcript.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SummaryTab(summary: Summary) {
    if (summary.status == SummaryStatus.GENERATING || summary.status == SummaryStatus.PENDING) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp), color = MaterialTheme.colorScheme.primary)
                Text(
                    text = stringResource(R.string.meeting_details_generating_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (summary.title.isNotBlank()) {
            Text(
                text = summary.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            HorizontalDivider(color = DividerColor)
        }

        if (summary.summary.isNotBlank()) {
            SummarySection(
                title = stringResource(R.string.summary_section_summary),
                body = summary.summary
            )
        }
        if (summary.keyPoints.isNotBlank()) {
            SummarySection(
                title = stringResource(R.string.summary_section_key_points),
                body = summary.keyPoints
            )
        }
        if (summary.actionItems.isNotBlank()) {
            SummarySection(
                title = stringResource(R.string.summary_section_action_items),
                body = summary.actionItems
            )
        }
    }
}

@Composable
private fun SummarySection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyElevated, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
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
    } else dateStr
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun MeetingDetailsPreview() {
    AudioMemoTheme {
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

@Preview(showBackground = true, name = "Loading state")
@Composable
private fun MeetingDetailsLoadingPreview() {
    AudioMemoTheme {
        MeetingDetailsContent(
            uiState = MeetingDetailsUiState.Loading,
            onNavigateBack = {}
        )
    }
}
