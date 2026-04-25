package com.example.audiomemo.features.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audiomemo.R
import com.example.audiomemo.core.preferences.AccentColor
import com.example.audiomemo.core.preferences.AppFont
import com.example.audiomemo.core.preferences.ThemeMode
import com.example.audiomemo.ui.theme.AudioMemoTheme

@Composable
fun AppearanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val selectedTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedFont by viewModel.appFont.collectAsStateWithLifecycle()
    val selectedAccent by viewModel.accentColor.collectAsStateWithLifecycle()

    AppearanceContent(
        selectedTheme = selectedTheme,
        selectedFont = selectedFont,
        selectedAccent = selectedAccent,
        onThemeSelect = viewModel::setThemeMode,
        onFontSelect = viewModel::setAppFont,
        onAccentSelect = viewModel::setAccentColor,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceContent(
    selectedTheme: ThemeMode,
    selectedFont: AppFont,
    selectedAccent: AccentColor,
    onThemeSelect: (ThemeMode) -> Unit,
    onFontSelect: (AppFont) -> Unit,
    onAccentSelect: (AccentColor) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.appearance_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.appearance_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SectionHeader(
                    icon = Icons.Default.PhoneAndroid,
                    text = stringResource(R.string.appearance_section_theme)
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                ThemeSelector(
                    selected = selectedTheme,
                    onSelect = onThemeSelect
                )
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }

            item {
                SectionHeader(
                    icon = Icons.Default.TextFormat,
                    text = stringResource(R.string.appearance_section_font)
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                FontChipRow(
                    selected = selectedFont,
                    onSelect = onFontSelect
                )
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }

            item {
                SectionHeader(
                    icon = Icons.Default.Palette,
                    text = stringResource(R.string.appearance_section_color)
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                ColorThemeGrid(
                    selected = selectedAccent,
                    onSelect = onAccentSelect
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ── Theme selector (stacked list) ─────────────────────────────────────────────

@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeListItem(
            label = stringResource(R.string.appearance_theme_light),
            subtitle = stringResource(R.string.appearance_theme_light_sub),
            icon = Icons.Default.LightMode,
            selected = selected == ThemeMode.LIGHT,
            onClick = { onSelect(ThemeMode.LIGHT) }
        )
        ThemeListItem(
            label = stringResource(R.string.appearance_theme_dark),
            subtitle = stringResource(R.string.appearance_theme_dark_sub),
            icon = Icons.Default.DarkMode,
            selected = selected == ThemeMode.DARK,
            onClick = { onSelect(ThemeMode.DARK) }
        )
        ThemeListItem(
            label = stringResource(R.string.appearance_theme_system),
            subtitle = stringResource(R.string.appearance_theme_system_sub),
            icon = Icons.Default.PhoneAndroid,
            selected = selected == ThemeMode.SYSTEM,
            onClick = { onSelect(ThemeMode.SYSTEM) }
        )
    }
}

@Composable
private fun ThemeListItem(
    label: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

// ── Font chip row (horizontal scroll) ─────────────────────────────────────────

@Composable
private fun FontChipRow(
    selected: AppFont,
    onSelect: (AppFont) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(AppFont.entries) { font ->
            FilterChip(
                selected = font == selected,
                onClick = { onSelect(font) },
                label = {
                    Text(
                        text = font.displayName,
                        fontFamily = font.toFontFamily(),
                        fontWeight = if (font == selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(50)
            )
        }
    }
}

// ── Color theme grid (2-col with 3D spheres) ──────────────────────────────────

@Composable
private fun ColorThemeGrid(
    selected: AccentColor,
    onSelect: (AccentColor) -> Unit
) {
    val rows = remember { AccentColor.entries.chunked(2) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { accent ->
                    ColorThemeCard(
                        accent = accent,
                        selected = accent == selected,
                        onClick = { onSelect(accent) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColorThemeCard(
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected)
        accent.color.copy(alpha = 0.20f)
    else
        MaterialTheme.colorScheme.surfaceVariant

    val borderMod = if (selected)
        Modifier.border(2.dp, accent.color, RoundedCornerShape(16.dp))
    else
        Modifier

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .then(borderMod)
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Sphere3D(
            color = accent.color,
            darkColor = accent.dark,
            lightColor = accent.light,
            size = 72.dp,
            showCheck = selected
        )
        Text(
            text = accent.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) accent.color else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Sphere3D(
    color: Color,
    darkColor: Color,
    lightColor: Color,
    size: Dp,
    showCheck: Boolean
) {
    val density = LocalDensity.current
    val sizePx = remember(size, density) { with(density) { size.toPx() } }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color.White.copy(alpha = 0.60f),
                        0.25f to lightColor.copy(alpha = 0.90f),
                        0.60f to color,
                        1.00f to darkColor,
                    ),
                    center = Offset(sizePx * 0.32f, sizePx * 0.28f),
                    radius = sizePx * 0.88f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showCheck) {
            Box(
                modifier = Modifier
                    .size(size * 0.48f)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.28f)
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark – Violet")
@Composable
private fun AppearanceDarkVioletPreview() {
    AudioMemoTheme {
        AppearanceContent(
            selectedTheme = ThemeMode.SYSTEM,
            selectedFont = AppFont.DEFAULT,
            selectedAccent = AccentColor.VIOLET,
            onThemeSelect = {},
            onFontSelect = {},
            onAccentSelect = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark – Rose")
@Composable
private fun AppearanceDarkRosePreview() {
    AudioMemoTheme(accentColor = AccentColor.ROSE) {
        AppearanceContent(
            selectedTheme = ThemeMode.DARK,
            selectedFont = AppFont.POPPINS,
            selectedAccent = AccentColor.ROSE,
            onThemeSelect = {},
            onFontSelect = {},
            onAccentSelect = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Light – Emerald")
@Composable
private fun AppearanceLightEmeraldPreview() {
    AudioMemoTheme(darkTheme = false, accentColor = AccentColor.EMERALD) {
        AppearanceContent(
            selectedTheme = ThemeMode.LIGHT,
            selectedFont = AppFont.PLAYFAIR_DISPLAY,
            selectedAccent = AccentColor.EMERALD,
            onThemeSelect = {},
            onFontSelect = {},
            onAccentSelect = {},
            onNavigateBack = {}
        )
    }
}
