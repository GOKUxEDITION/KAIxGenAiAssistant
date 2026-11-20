package com.kaixgen.assistant.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.ui.theme.KaiBlueDeep
import com.kaixgen.assistant.ui.theme.KaiCyanAccent
import com.kaixgen.assistant.ui.theme.KaiTheme

@Composable
fun KaiElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 22.dp,
    content: @Composable () -> Unit
) {
    val extendedColors = KaiTheme.extendedColors
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = 10.dp,
            shape = shape,
            ambientColor = extendedColors.subtleShadow,
            spotColor = extendedColors.subtleShadow
        )
        .clip(shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = extendedColors.cardSurface,
        border = BorderStroke(1.dp, extendedColors.cardBorder),
        content = content
    )
}

@Composable
fun KaiCircularIconBadge(
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Two-tone bold headline matching screenshots 1, 3, and 4
 * (e.g. "Set Your [Voice] Phrase", "Just [say it]", "Set Backup [Passcode]").
 */
@Composable
fun KaiTwoToneHeadline(
    prefix: String,
    highlighted: String,
    suffix: String = "",
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = onBackground, fontWeight = FontWeight.Bold)) {
                append(prefix)
            }
            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                append(highlighted)
            }
            if (suffix.isNotEmpty()) {
                withStyle(SpanStyle(color = onBackground, fontWeight = FontWeight.Bold)) {
                    append(suffix)
                }
            }
        },
        style = MaterialTheme.typography.headlineLarge,
        textAlign = textAlign,
        modifier = modifier
    )
}

/**
 * Compact GOKUxEDITION • Powered by KAIxGenShopLabs signature pill for primary tabs & overlays.
 */
@Composable
fun KaiBrandSignatureBadge(
    modifier: Modifier = Modifier,
    lightOnDark: Boolean = false
) {
    val extendedColors = KaiTheme.extendedColors
    val containerColor = if (lightOnDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        extendedColors.micContainer.copy(alpha = 0.72f)
    }
    val borderColor = if (lightOnDark) {
        Color.White.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    }
    val titleColor = if (lightOnDark) Color.White else MaterialTheme.colorScheme.primary
    val subtitleColor = if (lightOnDark) {
        Color.White.copy(alpha = 0.80f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = null,
                tint = if (lightOnDark) KaiCyanAccent else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "GOKUxEDITION",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = titleColor
            )
            Text(
                text = "\u2022 KAIxGenShopLabs",
                style = MaterialTheme.typography.labelSmall,
                color = subtitleColor
            )
        }
    }
}

/**
 * Full Developer & Brand Signature Card showcasing Dipak Choudhary, GOKUxEDITION,
 * KAIxGenShopLabs, and permanent package identity com.kaixgen.assistant.
 */
@Composable
fun KaiDeveloperSignatureCard(
    modifier: Modifier = Modifier
) {
    val extendedColors = KaiTheme.extendedColors
    KaiElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(KaiBlueDeep, KaiCyanAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "KAIxGen Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "GOKUxEDITION \u2022 Powered by KAIxGenShopLabs",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = extendedColors.micContainer.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SignatureMetaRow(label = "Developer & Architect", value = "Dipak Choudhary")
                    SignatureMetaRow(label = "Brand Signature", value = "GOKUxEDITION")
                    SignatureMetaRow(label = "Studio Engine", value = "KAIxGenShopLabs")
                    SignatureMetaRow(label = "Package ID", value = "com.kaixgen.assistant")
                }
            }
        }
    }
}

@Composable
private fun SignatureMetaRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
