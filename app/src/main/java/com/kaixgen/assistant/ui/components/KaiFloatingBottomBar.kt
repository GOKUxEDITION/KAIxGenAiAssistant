package com.kaixgen.assistant.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaixgen.assistant.R
import com.kaixgen.assistant.navigation.KaiBottomTab
import com.kaixgen.assistant.ui.theme.KaiTheme

/**
 * Floating Pill Bottom Navigation Bar supporting all 5 PRD sections:
 * Lock · KAI · Routines · Safety · More.
 */
@Composable
fun KaiFloatingBottomBar(
    selectedTab: KaiBottomTab,
    onTabSelected: (KaiBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(40.dp),
                    ambientColor = extendedColors.subtleShadow,
                    spotColor = extendedColors.subtleShadow
                ),
            shape = RoundedCornerShape(40.dp),
            color = extendedColors.cardSurface,
            border = BorderStroke(1.dp, extendedColors.cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KaiBottomNavItem(
                    label = stringResource(R.string.nav_voice_lock),
                    selectedIcon = Icons.Rounded.Lock,
                    unselectedIcon = Icons.Outlined.Lock,
                    isSelected = selectedTab == KaiBottomTab.VOICE_LOCK,
                    isCenterHighlight = false,
                    onClick = { onTabSelected(KaiBottomTab.VOICE_LOCK) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_nav_voice_lock")
                )

                KaiBottomNavItem(
                    label = stringResource(R.string.nav_kai_buddy),
                    selectedIcon = Icons.Rounded.GraphicEq,
                    unselectedIcon = Icons.Rounded.GraphicEq,
                    isSelected = selectedTab == KaiBottomTab.KAI_BUDDY,
                    isCenterHighlight = true,
                    onClick = { onTabSelected(KaiBottomTab.KAI_BUDDY) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_nav_kai_buddy")
                )

                KaiBottomNavItem(
                    label = stringResource(R.string.nav_routines),
                    selectedIcon = Icons.Rounded.Bolt,
                    unselectedIcon = Icons.Outlined.Bolt,
                    isSelected = selectedTab == KaiBottomTab.ROUTINES,
                    isCenterHighlight = false,
                    onClick = { onTabSelected(KaiBottomTab.ROUTINES) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_nav_routines")
                )

                KaiBottomNavItem(
                    label = stringResource(R.string.nav_find_phone),
                    selectedIcon = Icons.Rounded.Shield,
                    unselectedIcon = Icons.Outlined.Shield,
                    isSelected = selectedTab == KaiBottomTab.FIND_PHONE,
                    isCenterHighlight = false,
                    onClick = { onTabSelected(KaiBottomTab.FIND_PHONE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_nav_find_phone")
                )

                KaiBottomNavItem(
                    label = stringResource(R.string.nav_more),
                    selectedIcon = Icons.Rounded.Dashboard,
                    unselectedIcon = Icons.Outlined.Dashboard,
                    isSelected = selectedTab == KaiBottomTab.MORE,
                    isCenterHighlight = false,
                    onClick = { onTabSelected(KaiBottomTab.MORE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_nav_more")
                )
            }
        }
    }
}

@Composable
private fun KaiBottomNavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    isCenterHighlight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    val tint by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "nav_item_tint"
    )

    val badgeSize by animateDpAsState(
        targetValue = if (isSelected && isCenterHighlight) 40.dp else 28.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "center_badge_size"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = activeColor),
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(
                    if (isSelected && isCenterHighlight) activeColor else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (isSelected && isCenterHighlight) Color.White else tint,
                modifier = Modifier.size(if (isSelected && isCenterHighlight) 22.dp else 22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = tint,
            maxLines = 1,
            softWrap = false
        )
    }
}
