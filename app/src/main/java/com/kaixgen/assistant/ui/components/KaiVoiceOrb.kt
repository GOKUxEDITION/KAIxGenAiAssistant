package com.kaixgen.assistant.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.R
import com.kaixgen.assistant.domain.model.VoiceOrbState
import com.kaixgen.assistant.ui.theme.KaiBluePrimary
import com.kaixgen.assistant.ui.theme.KaiCyanAccent
import com.kaixgen.assistant.ui.theme.KaiCyanGlow
import com.kaixgen.assistant.ui.theme.KaiDangerRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Multi-layered fluid organic KAI Voice Orb built in Jetpack Compose Canvas.
 * Visually mirrors the glowing blue-cyan liquid aura of 03_buddy_tab.jpg while supporting
 * all 5 PRD states: IDLE (breathe), LISTENING (amplitude-reactive), THINKING (swirl),
 * SPEAKING (pulse rings), and ERROR (warm red tint).
 */
@Composable
fun KaiVoiceOrb(
    orbState: VoiceOrbState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 256.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "kai_orb_motion")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    VoiceOrbState.THINKING -> 2_200
                    VoiceOrbState.LISTENING -> 3_400
                    VoiceOrbState.SPEAKING -> 2_800
                    else -> 6_500
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_phase"
    )

    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_breathe"
    )

    val smoothAmplitude by animateFloatAsState(
        targetValue = when (orbState) {
            VoiceOrbState.LISTENING -> amplitude.coerceIn(0.25f, 1f)
            VoiceOrbState.SPEAKING -> 0.55f
            VoiceOrbState.THINKING -> 0.35f
            else -> 0.08f
        },
        animationSpec = tween(160),
        label = "orb_amplitude"
    )

    val coreStartColor by animateColorAsState(
        targetValue = when (orbState) {
            VoiceOrbState.ERROR -> KaiDangerRed
            VoiceOrbState.THINKING -> Color(0xFF1A4FD8)
            else -> KaiBluePrimary
        },
        label = "core_start"
    )

    val coreEndColor by animateColorAsState(
        targetValue = when (orbState) {
            VoiceOrbState.ERROR -> Color(0xFFFF7A6B)
            VoiceOrbState.SPEAKING -> Color(0xFF00D4FF)
            else -> KaiCyanAccent
        },
        label = "core_end"
    )

    val outerAuraColor by animateColorAsState(
        targetValue = when (orbState) {
            VoiceOrbState.ERROR -> Color(0xFFFFB4AB)
            else -> KaiCyanGlow
        },
        label = "outer_aura"
    )

    val orbContentDescription = stringResource(R.string.cd_voice_orb)
    val labelText = when (orbState) {
        VoiceOrbState.IDLE -> stringResource(R.string.orb_tap_to_speak)
        VoiceOrbState.LISTENING -> stringResource(R.string.orb_listening)
        VoiceOrbState.THINKING -> stringResource(R.string.orb_thinking)
        VoiceOrbState.SPEAKING -> stringResource(R.string.orb_speaking)
        VoiceOrbState.ERROR -> "Tap to retry"
    }

    val centerIcon = when (orbState) {
        VoiceOrbState.THINKING -> Icons.Rounded.AutoAwesome
        VoiceOrbState.SPEAKING -> Icons.Rounded.GraphicEq
        VoiceOrbState.ERROR -> Icons.Rounded.PriorityHigh
        else -> Icons.Rounded.Mic
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = orbContentDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minDim = this.size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = minDim * 0.34f * breathe * (1f + smoothAmplitude * 0.12f)

            // Outer translucent silk aura layers (matching 03_buddy_tab.jpg fluid glow)
            drawOrganicBlob(
                center = center,
                radius = baseRadius * 1.32f,
                phase = phase,
                lobes = 5,
                wobble = 0.08f + smoothAmplitude * 0.06f,
                brush = Brush.radialGradient(
                    colors = listOf(
                        outerAuraColor.copy(alpha = 0.24f),
                        outerAuraColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.48f
                )
            )

            drawOrganicBlob(
                center = center,
                radius = baseRadius * 1.18f,
                phase = -phase * 1.25f + 1.1f,
                lobes = 4,
                wobble = 0.075f + smoothAmplitude * 0.05f,
                brush = Brush.linearGradient(
                    colors = listOf(
                        outerAuraColor.copy(alpha = 0.32f),
                        coreEndColor.copy(alpha = 0.20f)
                    ),
                    start = Offset(center.x - baseRadius, center.y - baseRadius),
                    end = Offset(center.x + baseRadius, center.y + baseRadius)
                )
            )

            // Main luminous liquid sphere body
            val mainPath = buildOrganicPath(
                center = center,
                radius = baseRadius,
                phase = phase * 0.9f,
                lobes = 4,
                wobble = 0.055f + smoothAmplitude * 0.04f
            )

            drawPath(
                path = mainPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        coreStartColor,
                        Color(0xFF007CEB),
                        coreEndColor
                    ),
                    start = Offset(center.x - baseRadius * 0.7f, center.y - baseRadius * 0.9f),
                    end = Offset(center.x + baseRadius * 0.85f, center.y + baseRadius * 0.95f)
                )
            )

            // Glossy translucent cyan-white liquid edge rim
            drawPath(
                path = mainPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f),
                        outerAuraColor.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.45f)
                    ),
                    start = Offset(center.x - baseRadius, center.y - baseRadius),
                    end = Offset(center.x + baseRadius, center.y + baseRadius)
                ),
                style = Stroke(width = minDim * 0.012f)
            )

            // Subtle inner specular glow in upper-left quadrant
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.26f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - baseRadius * 0.28f, center.y - baseRadius * 0.32f),
                    radius = baseRadius * 0.65f
                ),
                radius = baseRadius * 0.65f,
                center = Offset(center.x - baseRadius * 0.28f, center.y - baseRadius * 0.32f)
            )
        }

        // Interactive circular center tap zone (generous > 150dp touch target)
        Box(
            modifier = Modifier
                .size(size * 0.68f)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    role = Role.Button,
                    onClick = onClick
                )
                .testTag("kai_voice_orb_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = centerIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

private fun DrawScope.drawOrganicBlob(
    center: Offset,
    radius: Float,
    phase: Float,
    lobes: Int,
    wobble: Float,
    brush: Brush
) {
    val path = buildOrganicPath(center, radius, phase, lobes, wobble)
    drawPath(path = path, brush = brush)
}

private fun buildOrganicPath(
    center: Offset,
    radius: Float,
    phase: Float,
    lobes: Int,
    wobble: Float
): Path {
    val steps = 64
    val path = Path()
    for (i in 0..steps) {
        val angle = (i.toFloat() / steps) * (2f * PI.toFloat())
        val wave = sin(angle * lobes + phase) * wobble +
            cos(angle * (lobes - 1) - phase * 0.8f) * (wobble * 0.55f)
        val r = radius * (1f + wave)
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
