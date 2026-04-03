package com.example.audiomemo.features.home.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audiomemo.R
import com.example.audiomemo.features.meetings.ui.MeetingListItem
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.domain.model.SessionState
import com.example.audiomemo.ui.theme.AccentGreen
import com.example.audiomemo.ui.theme.AccentGreenDark
import com.example.audiomemo.ui.theme.AccentGreenLight
import com.example.audiomemo.ui.theme.AudioMemoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToTranscript: () -> Unit,
    onNavigateToMeetings: () -> Unit = {},
    onNavigateToMeetingDetails: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }
    val recentMeetings by viewModel.recentMeetings.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            permissionDeniedMessage = null
            onNavigateToTranscript()
        } else {
            permissionDeniedMessage = context.getString(R.string.home_mic_permission_denied)
        }
    }

    HomeContent(
        recentMeetings = recentMeetings,
        permissionDeniedMessage = permissionDeniedMessage,
        onNavigateToMeetings = onNavigateToMeetings,
        onMeetingClick = onNavigateToMeetingDetails,
        onCaptureClick = {
            val hasAudio = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasAudio) {
                onNavigateToTranscript()
            } else {
                val perms = buildList {
                    add(Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }.toTypedArray()
                permissionLauncher.launch(perms)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    recentMeetings: List<MeetingListItem>,
    permissionDeniedMessage: String?,
    onNavigateToMeetings: () -> Unit,
    onMeetingClick: (Long) -> Unit,
    onCaptureClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AudioMemo",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineLarge

                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            HomeBottomNavBar(onMeetingsClick = onNavigateToMeetings)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        )
        {
            // Top bar
            //HomeTopBar(modifier = Modifier.statusBarsPadding())

            //HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

            // Record button hero
            item {
                RecordButtonSection(
                    onCaptureClick = onCaptureClick,
                    permissionDeniedMessage = permissionDeniedMessage
                )
            }

            item {
                TimerComponent()
            }

            // Recent recordings header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_recent),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (recentMeetings.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.home_view_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToMeetings() }
                        )
                    }
                }
            }

            // Recording cards or empty state
            if (recentMeetings.isEmpty()) {
                item { EmptyRecordingsState() }
            } else {
                items(recentMeetings, key = { it.sessionId }) { meeting ->
                    RecordingCard(
                        meeting = meeting,
                        onClick = { onMeetingClick(meeting.sessionId) }
                    )
                }
            }

        }
    }
}

@Composable
private fun TimerComponent() {
    val blink by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "00:00:00",
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Red.copy(alpha = blink)
                    )

            )
            Text(
                text = "Tap To Record",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }


    }
}


// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Record button ─────────────────────────────────────────────────────────────

@Composable
private fun RecordButtonSection(
    onCaptureClick: () -> Unit,
    permissionDeniedMessage: String?
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val sectionHeightPx = with(density) { 320.dp.toPx() }
    val bgColor = MaterialTheme.colorScheme.background
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6D3FD4),              // vivid violet core
                        Color(0xFF3B1F7A).copy(alpha = 0.8f), // mid purple
                        Color(0xFF1A1040).copy(alpha = 0.5f), // soft fade
                        bgColor.copy(alpha = 0f)
                    ),
                    center = Offset(
                        screenWidthPx / 2f, sectionHeightPx / 2f
                    ),
                    radius = screenWidthPx * 0.72f
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentGreenLight,   // #A78BFA — lighter center
                                AccentGreen,        // #8B5CF6 — mid
                                AccentGreenDark,    // #7C3AED — darker edge
                            )
                        )
                    )
                    .clickable(onClick = onCaptureClick)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.cd_record),
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            if (permissionDeniedMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = permissionDeniedMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

        }

    }
}

// ── Recording card ────────────────────────────────────────────────────────────

@Composable
private fun RecordingCard(
    meeting: MeetingListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mic icon badge
            Surface(
                shape = CircleShape,
                color = AccentGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Title + meta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatDate(meeting.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = formatDuration(meeting.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Status badge + chevron
            Column(horizontalAlignment = Alignment.End) {
                SummaryStatusBadge(status = meeting.summaryStatus)
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatusBadge(status: SummaryStatus?) {
    val (labelRes, color) = when (status) {
        SummaryStatus.DONE -> R.string.home_status_summarized to AccentGreen
        SummaryStatus.GENERATING -> R.string.home_status_processing to MaterialTheme.colorScheme.tertiary
        SummaryStatus.FAILED -> R.string.home_status_failed to MaterialTheme.colorScheme.error
        else -> return
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyRecordingsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.home_no_recordings_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.home_no_recordings_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// ── Bottom nav bar ────────────────────────────────────────────────────────────

private enum class HomeTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    Home(R.string.nav_tab_home, Icons.Default.Home),
    Meetings(R.string.nav_tab_meetings, Icons.Default.MeetingRoom),
    Settings(R.string.nav_tab_settings, Icons.Default.Settings)
}

@Composable
private fun HomeBottomNavBar(onMeetingsClick: () -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        HomeTab.entries.forEach { tab ->
            val selected = tab == HomeTab.Home
            NavigationBarItem(
                selected = selected,
                onClick = {
                    when (tab) {
                        HomeTab.Meetings -> onMeetingsClick()
                        else -> { /* Home = current, Settings = future */
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelRes),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentGreen,
                    selectedTextColor = AccentGreen,
                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                    unselectedTextColor = MaterialTheme.colorScheme.outline,
                    indicatorColor = AccentGreen.copy(alpha = 0.12f)
                )
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(millis))

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AudioMemoTheme {
        HomeContent(
            recentMeetings = listOf(
                MeetingListItem(
                    sessionId = 1L,
                    title = "Team Standup – Sprint Review",
                    startTime = System.currentTimeMillis() - 3_600_000,
                    durationMs = 15 * 60 * 1000L,
                    sessionState = SessionState.STOPPED,
                    summaryStatus = SummaryStatus.DONE
                ),
                MeetingListItem(
                    sessionId = 2L,
                    title = "Product Roadmap Discussion",
                    startTime = System.currentTimeMillis() - 86_400_000,
                    durationMs = 45 * 60 * 1000L,
                    sessionState = SessionState.STOPPED,
                    summaryStatus = SummaryStatus.GENERATING
                )
            ),
            permissionDeniedMessage = null,
            onNavigateToMeetings = {},
            onMeetingClick = {},
            onCaptureClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenEmptyPreview() {
    AudioMemoTheme {
        HomeContent(
            recentMeetings = emptyList(),
            permissionDeniedMessage = null,
            onNavigateToMeetings = {},
            onMeetingClick = {},
            onCaptureClick = {}
        )
    }
}