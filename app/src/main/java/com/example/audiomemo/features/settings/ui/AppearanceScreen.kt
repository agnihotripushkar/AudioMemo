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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audiomemo.R
import com.example.audiomemo.core.preferences.AppFont
import com.example.audiomemo.core.preferences.ThemeMode
import com.example.audiomemo.ui.theme.AccentGreen
import com.example.audiomemo.ui.theme.AudioMemoTheme
import com.example.audiomemo.ui.theme.DividerColor
import com.example.audiomemo.ui.theme.NavyElevated
import com.example.audiomemo.ui.theme.TextSecondary
import com.example.audiomemo.ui.theme.TextTertiary

@Composable
fun AppearanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val selectedTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedFont by viewModel.appFont.collectAsStateWithLifecycle()

    AppearanceContent(
        selectedTheme = selectedTheme,
        selectedFont = selectedFont,
        onThemeSelect = viewModel::setThemeMode,
        onFontSelect = viewModel::setAppFont,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceContent(
    selectedTheme: ThemeMode,
    selectedFont: AppFont,
    onThemeSelect: (ThemeMode) -> Unit,
    onFontSelect: (AppFont) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appearance_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
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
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { SectionHeader(text = stringResource(R.string.appearance_section_theme).uppercase()) }
            item {
                ThemeSelector(
                    selected = selectedTheme,
                    onSelect = onThemeSelect
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { SectionHeader(text = stringResource(R.string.appearance_section_font).uppercase()) }
            item {
                FontSelector(
                    selected = selectedFont,
                    onSelect = onFontSelect
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
}

@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ThemeOption(
            label = stringResource(R.string.appearance_theme_system),
            icon = Icons.Default.PhoneAndroid,
            selected = selected == ThemeMode.SYSTEM,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(ThemeMode.SYSTEM) }
        )
        ThemeOption(
            label = stringResource(R.string.appearance_theme_light),
            icon = Icons.Default.LightMode,
            selected = selected == ThemeMode.LIGHT,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(ThemeMode.LIGHT) }
        )
        ThemeOption(
            label = stringResource(R.string.appearance_theme_dark),
            icon = Icons.Default.DarkMode,
            selected = selected == ThemeMode.DARK,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AccentGreen.copy(alpha = 0.10f) else NavyElevated)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AccentGreen else DividerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) AccentGreen else TextSecondary,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) AccentGreen else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun FontSelector(
    selected: AppFont,
    onSelect: (AppFont) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = NavyElevated, shape = RoundedCornerShape(12.dp))
    ) {
        AppFont.entries.forEachIndexed { index, font ->
            FontOption(
                font = font,
                description = stringResource(font.descriptionRes),
                selected = selected == font,
                onClick = { onSelect(font) }
            )
            if (index < AppFont.entries.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .height(1.dp)
                        .background(DividerColor)
                )
            }
        }
    }
}

@Composable
private fun FontOption(
    font: AppFont,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val fontFamily = font.toFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (selected) AccentGreen.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aa",
                fontFamily = fontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) AccentGreen else TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = font.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = fontFamily,
                color = TextSecondary
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark – System")
@Composable
private fun AppearanceDarkSystemPreview() {
    AudioMemoTheme {
        AppearanceContent(
            selectedTheme = ThemeMode.SYSTEM,
            selectedFont = AppFont.DEFAULT,
            onThemeSelect = {},
            onFontSelect = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark – Poppins")
@Composable
private fun AppearanceDarkPoppinsPreview() {
    AudioMemoTheme {
        AppearanceContent(
            selectedTheme = ThemeMode.DARK,
            selectedFont = AppFont.POPPINS,
            onThemeSelect = {},
            onFontSelect = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Light – Playfair")
@Composable
private fun AppearanceLightPlayfairPreview() {
    AudioMemoTheme(darkTheme = false) {
        AppearanceContent(
            selectedTheme = ThemeMode.LIGHT,
            selectedFont = AppFont.PLAYFAIR_DISPLAY,
            onThemeSelect = {},
            onFontSelect = {},
            onNavigateBack = {}
        )
    }
}
