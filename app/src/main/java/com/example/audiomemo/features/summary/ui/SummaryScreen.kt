package com.example.audiomemo.features.summary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.audiomemo.R
import com.example.audiomemo.features.summary.domain.model.Summary
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.summary.ui.state.SummaryUiState
import com.example.audiomemo.ui.theme.AccentGreen
import com.example.audiomemo.ui.theme.AudioMemoTheme

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun SummaryScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onViewTranscript: () -> Unit = {},
    viewModel: SummaryViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) { viewModel.setSessionId(sessionId) }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is SummaryUiState.Loading -> LoadingContent()
            is SummaryUiState.Failed  -> FailedContent(
                onRetry = { viewModel.retrySummary() },
                onNavigateBack = onNavigateBack
            )
            is SummaryUiState.Success -> SummaryContent(
                summary = state.summary,
                onNavigateBack = onNavigateBack,
                onViewTranscript = onViewTranscript
            )
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(
                color = AccentGreen,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.summary_generating),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Failed ────────────────────────────────────────────────────────────────────

@Composable
private fun FailedContent(
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.summary_failed_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.summary_failed_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.summary_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            OutlinedButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.summary_go_back),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

// ── Success ───────────────────────────────────────────────────────────────────

@Composable
private fun SummaryContent(
    summary: Summary,
    onNavigateBack: () -> Unit,
    onViewTranscript: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Top bar
        SummaryTopBar(
            onNavigateBack = onNavigateBack,
            modifier = Modifier.statusBarsPadding()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

        // Scrollable content
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title + date
            item {
                Text(
                    text = summary.title.ifBlank { stringResource(R.string.summary_default_title) },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.summary_ai_generated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(8.dp))
            }

            // Overview / Summary card
            if (summary.summary.isNotBlank()) {
                item {
                    SectionCard(
                        icon = Icons.Default.Notes,
                        title = stringResource(R.string.summary_section_summary)
                    ) {
                        Text(
                            text = summary.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Key points card
            if (summary.keyPoints.isNotBlank()) {
                item {
                    val points = parseLines(summary.keyPoints)
                    SectionCard(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.summary_section_key_points)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (points.isNotEmpty()) {
                                points.forEach { point ->
                                    BulletRow(text = point)
                                }
                            } else {
                                Text(
                                    text = summary.keyPoints,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Action items card
            if (summary.actionItems.isNotBlank()) {
                item {
                    val items = parseLines(summary.actionItems)
                    SectionCard(
                        icon = Icons.Default.Checklist,
                        title = stringResource(R.string.summary_section_action_items)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (items.isNotEmpty()) {
                                items.forEach { item ->
                                    ChecklistRow(text = item)
                                }
                            } else {
                                Text(
                                    text = summary.actionItems,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // Bottom action bar
        SummaryBottomBar(
            onViewTranscript = onViewTranscript,
            onDone = onNavigateBack
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun SummaryTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = stringResource(R.string.summary_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        IconButton(onClick = { /* share — future */ }) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.cd_share),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Section card ──────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

// ── List row components ───────────────────────────────────────────────────────

@Composable
private fun BulletRow(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .background(AccentGreen, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ChecklistRow(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

@Composable
private fun SummaryBottomBar(
    onViewTranscript: () -> Unit,
    onDone: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onViewTranscript,
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.summary_view_transcript),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.summary_done),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Splits a multi-line string into non-blank lines, stripping leading bullet/dash prefixes. */
private fun parseLines(text: String): List<String> =
    text.lines()
        .map { it.trimStart().removePrefix("•").removePrefix("-").trim() }
        .filter { it.isNotBlank() }

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview() {
    AudioMemoTheme {
        SummaryContent(
            summary = Summary(
                id = 1,
                sessionId = 1,
                title = "Team Standup – Sprint Review",
                summary = "The team reviewed sprint progress, discussed blockers, and planned next steps for the upcoming release cycle.",
                keyPoints = "• Sprint velocity increased by 15%\n• Two critical bugs were resolved\n• Design handoff completed for onboarding flow",
                actionItems = "- Update test coverage to 80%\n- Schedule retrospective for Friday\n- Review PR for audio chunking fix",
                status = SummaryStatus.DONE
            ),
            onNavigateBack = {},
            onViewTranscript = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryLoadingPreview() {
    AudioMemoTheme { LoadingContent() }
}

@Preview(showBackground = true)
@Composable
fun SummaryFailedPreview() {
    AudioMemoTheme {
        FailedContent(onRetry = {}, onNavigateBack = {})
    }
}
