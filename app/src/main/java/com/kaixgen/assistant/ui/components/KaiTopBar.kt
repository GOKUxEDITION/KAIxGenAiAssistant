package com.kaixgen.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.R
import com.kaixgen.assistant.ui.theme.KaiBlueDeep
import com.kaixgen.assistant.ui.theme.KaiCyanAccent
import com.kaixgen.assistant.ui.theme.KaiTheme
import com.kaixgen.assistant.ui.theme.KaiWarningOrange

/**
 * Primary Brand Top Bar matching UI screenshots (Logo + KAIxGen + Listening status chip + Settings gear).
 */
@Composable
fun KaiBrandTopBar(
    isListeningActive: Boolean,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    showStatusChip: Boolean = true
) {
    val extendedColors = KaiTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            KaiBrandLogoMark()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.brand_header_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.brandDeep
                )
            )
            if (showStatusChip) {
                Spacer(modifier = Modifier.width(10.dp))
                ListeningStatusPill(isActive = isListeningActive)
            }
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .testTag("top_bar_settings_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Sub-screen Top Bar matching 04_set_backup_passcode.jpg (Circular outlined back button + optional settings).
 */
@Composable
fun KaiSubScreenTopBar(
    onBackClick: () -> Unit,
    onSettingsClick: (() -> Unit)? = null,
    title: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .testTag("sub_screen_back_button")
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.25.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (onSettingsClick != null) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("sub_screen_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun KaiBrandLogoMark(modifier: Modifier = Modifier) {
    val logoDescription = stringResource(R.string.cd_brand_logo)
    Box(
        modifier = modifier
            .size(36.dp)
            .semantics { contentDescription = logoDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp, bottomEnd = 11.dp, bottomStart = 3.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(KaiBlueDeep, KaiCyanAccent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(10.dp)
                .clip(CircleShape)
                .background(KaiWarningOrange)
                .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
        )
    }
}

@Composable
private fun ListeningStatusPill(isActive: Boolean) {
    val extendedColors = KaiTheme.extendedColors
    val dotColor = if (isActive) extendedColors.success else extendedColors.warning
    val bgColor = if (isActive) extendedColors.lockContainer else extendedColors.warningContainer
    val label = if (isActive) {
        stringResource(R.string.status_listening)
    } else {
        stringResource(R.string.status_paused)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(0.75.dp, dotColor.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
