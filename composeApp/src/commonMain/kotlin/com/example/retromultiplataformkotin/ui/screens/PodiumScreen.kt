package com.example.retromultiplataformkotin.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retromultiplataformkotin.model.TeamMember
import com.example.retromultiplataformkotin.theme.RetroColors
import com.example.retromultiplataformkotin.ui.components.RetroButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun PodiumScreen(
    leaders: List<TeamMember>,
    voteTotals: Map<String, Int>,
    isLeader: Boolean,
    onNewRetro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ranked = leaders
        .map { it to (voteTotals[it.id] ?: 0) }
        .sortedByDescending { it.second }

    val first = ranked.getOrNull(0)
    val second = ranked.getOrNull(1)
    val third = ranked.getOrNull(2)
    val rest = ranked.drop(3)

    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroColors.DarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🏆 PODIO DE LÍDERES 🏆",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = RetroColors.Gold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Podium layout: 2nd | 1st | 3rd
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ) {
            // 2nd place
            if (second != null) {
                PodiumPosition(
                    member = second.first,
                    stars = second.second,
                    rank = 2,
                    podiumHeight = 120.dp,
                    podiumColor = Color(0xFFC0C0C0),
                    glowAlpha = 0f,
                    modifier = Modifier.weight(1f),
                )
            }

            // 1st place
            if (first != null) {
                PodiumPosition(
                    member = first.first,
                    stars = first.second,
                    rank = 1,
                    podiumHeight = 160.dp,
                    podiumColor = RetroColors.Gold,
                    glowAlpha = glowAlpha,
                    modifier = Modifier.weight(1f),
                )
            }

            // 3rd place
            if (third != null) {
                PodiumPosition(
                    member = third.first,
                    stars = third.second,
                    rank = 3,
                    podiumHeight = 90.dp,
                    podiumColor = Color(0xFFCD7F32),
                    glowAlpha = 0f,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rest of rankings
        if (rest.isNotEmpty()) {
            rest.forEachIndexed { index, (member, stars) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RetroColors.DarkCard)
                        .padding(12.dp),
                ) {
                    Text(
                        text = "#${index + 4}",
                        color = RetroColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RetroColors.DarkSurface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(member.avatarRes),
                            contentDescription = member.name,
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = member.name,
                        color = RetroColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "$stars ⭐",
                        color = RetroColors.Gold,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // New retro button
        if (isLeader) {
            RetroButton(
                text = "🔄 Nueva Retrospectiva",
                onClick = onNewRetro,
                color = RetroColors.Cyan40,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PodiumPosition(
    member: TeamMember,
    stars: Int,
    rank: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    podiumColor: Color,
    glowAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val rankEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp),
    ) {
        // Rank emoji
        Text(text = rankEmoji, fontSize = 28.sp)

        Spacer(modifier = Modifier.height(4.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(if (rank == 1) 72.dp else 56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(RetroColors.DarkSurface)
                .then(
                    if (glowAlpha > 0f) {
                        Modifier.border(2.dp, RetroColors.Gold.copy(alpha = glowAlpha), RoundedCornerShape(12.dp))
                    } else {
                        Modifier.border(1.dp, podiumColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(member.avatarRes),
                contentDescription = member.name,
                modifier = Modifier.fillMaxSize().padding(4.dp),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = member.name,
            fontWeight = FontWeight.Bold,
            color = RetroColors.TextPrimary,
            fontSize = if (rank == 1) 14.sp else 12.sp,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "$stars ⭐",
            color = RetroColors.Gold,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Podium block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(podiumColor.copy(alpha = 0.3f))
                .border(
                    1.dp,
                    podiumColor.copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${rank}°",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = podiumColor,
            )
        }
    }
}
