package com.kaixgen.assistant.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaixgen.assistant.R
import com.kaixgen.assistant.ui.components.KaiGlassPanel
import com.kaixgen.assistant.ui.theme.KaiTheme
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val badge: String,
    val title: String,
    val subtitle: String,
    val highlights: List<String>,
    val icon: ImageVector? = null,
    val showBrandLogo: Boolean = false
)

@Composable
fun WelcomeOnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val ext = KaiTheme.extendedColors
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            badge = "WELCOME TO KAIXGENSHOPLABS",
            title = "KAIxGenAiAssistant",
            subtitle = "Har Smartphone Ka Smart Assistant — Offline-first Indian Hinglish AI, Voice Biometric Lock & Real-Life Super Utilities.",
            highlights = listOf(
                "Offline-fast Hinglish & Hindi Voice Control",
                "Apple iOS OLED Dark + Liquid Glass UI",
                "Built by Dipak Choudhary (GOKUxEDITION)"
            ),
            showBrandLogo = true
        ),
        OnboardingPage(
            badge = "VOICE LOCK & ANTI-THEFT",
            title = "Apni Aawaz Se Lock & Unlock",
            subtitle = "Apne khud ke Custom Lock aur Unlock Words set karein — bina haath lagaye phone secure rakhein.",
            highlights = listOf(
                "Custom Lock Words -> Real Screen-Off Lock",
                "16-Band Speaker Voiceprint + PBKDF2 Backup PIN",
                "CameraX Intruder Selfie + Charger/Pocket Siren"
            ),
            icon = Icons.Rounded.Lock
        ),
        OnboardingPage(
            badge = "EVERYDAY SUPERPOWERS",
            title = "Driving, AI Chat & Smart Money",
            subtitle = "Ek hi app me sab kuch jo roz ki zindagi me kaam aaye — bina extra apps install kiye.",
            highlights = listOf(
                "Double-Clap Find Phone + SOS & Fake Call",
                "Direct WhatsApp (Bina Number Save Kiye)",
                "EMI, GST, Bill Split, Driving Mode & Hybrid AI"
            ),
            icon = Icons.Rounded.DirectionsCar
        ),
        OnboardingPage(
            badge = "100% PRIVACY FIRST",
            title = "Aapka Data, Sirf Aapke Phone Me",
            subtitle = "Raw microphone audio kabhi save nahi hota. Har vault key Android Keystore AES-256-GCM se protected hai.",
            highlights = listOf(
                "Zero raw audio persistence on disk",
                "FLAG_SECURE protection on Lock & PIN screens",
                "Opt-in Cloud AI — full control in Privacy Center"
            ),
            icon = Icons.Rounded.VerifiedUser
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${pages.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.mutedText
                )
                TextButton(onClick = onFinishOnboarding) {
                    Text("Skip", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (page.showBrandLogo) {
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_kai_foreground),
                                contentDescription = "KAIxGenAiAssistant Logo",
                                modifier = Modifier.size(116.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(ext.micContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = page.icon ?: Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = page.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = page.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    KaiGlassPanel(modifier = Modifier.fillMaxWidth(), highlightAccent = index == 0) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            page.highlights.forEach { bullet ->
                                Text(
                                    text = "✓  $bullet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { i ->
                    val selected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(width = if (selected) 24.dp else 8.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else ext.divider
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinishOnboarding()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (isLastPage) "Get Started — Launch KAI" else "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
