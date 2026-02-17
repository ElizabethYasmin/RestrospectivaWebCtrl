package com.example.retromultiplataformkotin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retromultiplataformkotin.model.TeamData
import com.example.retromultiplataformkotin.model.TeamMember
import com.example.retromultiplataformkotin.model.RetroPhase
import com.example.retromultiplataformkotin.theme.RetroColors
import com.example.retromultiplataformkotin.ui.components.PhaseHeader
import com.example.retromultiplataformkotin.ui.components.RetroButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun LeaderVoteScreen(
    members: List<TeamMember>,
    leaderVoteTotals: Map<String, Int>,
    hasVoted: Boolean,
    isLeader: Boolean,
    onSubmitVotes: (Map<String, Int>) -> Unit,
    onShowPodium: () -> Unit,
    onNewRetro: () -> Unit,
    modifier: Modifier = Modifier,
    remainingSeconds: Int = -1,
    totalSeconds: Int = 0,
    isTimerPaused: Boolean = true,
    isTimerStarted: Boolean = false,
    onTimerStart: () -> Unit = {},
    onTimerPause: () -> Unit = {},
    onTimerReset: () -> Unit = {},
    onSetDuration: (Int) -> Unit = {},
) {
    val leaders = members.filter { it.id in TeamData.leaderIds }
    var starAllocation by remember { mutableStateOf(TeamData.leaderIds.associateWith { 0 }) }
    val totalUsed = starAllocation.values.sum()
    val maxStars = 5

    // Show podium view if already voted
    if (hasVoted) {
        PodiumScreen(
            leaders = leaders,
            voteTotals = leaderVoteTotals,
            isLeader = isLeader,
            onNewRetro = onNewRetro,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroColors.DarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        PhaseHeader(
            currentPhase = RetroPhase.LEADER_VOTE,
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds,
            isTimerPaused = isTimerPaused,
            isTimerStarted = isTimerStarted,
            isLeader = isLeader,
            onTimerStart = onTimerStart,
            onTimerPause = onTimerPause,
            onTimerReset = onTimerReset,
            onSetDuration = onSetDuration,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Reparte tus $maxStars estrellas entre los líderes",
            color = RetroColors.TextSecondary,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Stars remaining
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (totalUsed == maxStars) RetroColors.GreenWentWell.copy(alpha = 0.15f)
                    else RetroColors.DarkCard
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Estrellas disponibles: ${maxStars - totalUsed}/$maxStars",
                color = if (totalUsed == maxStars) RetroColors.GreenWentWell else RetroColors.Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leader cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(leaders, key = { it.id }) { leader ->
                LeaderVoteCard(
                    leader = leader,
                    stars = starAllocation[leader.id] ?: 0,
                    remainingStars = maxStars - totalUsed,
                    onStarChanged = { newStars ->
                        starAllocation = starAllocation.toMutableMap().apply {
                            this[leader.id] = newStars
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RetroButton(
            text = "📤 Enviar Votos",
            onClick = { onSubmitVotes(starAllocation.filter { it.value > 0 }) },
            color = RetroColors.Gold,
            textColor = RetroColors.TextOnAccent,
            modifier = Modifier.fillMaxWidth(),
            enabled = totalUsed == maxStars,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LeaderVoteCard(
    leader: TeamMember,
    stars: Int,
    remainingStars: Int,
    onStarChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RetroColors.DarkCard)
            .border(
                1.dp,
                if (stars > 0) RetroColors.Gold.copy(alpha = 0.4f) else RetroColors.DarkSurface,
                RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(RetroColors.DarkSurface),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(leader.avatarRes),
                contentDescription = leader.name,
                modifier = Modifier.fillMaxSize().padding(4.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leader.name,
                fontWeight = FontWeight.Bold,
                color = RetroColors.TextPrimary,
                fontSize = 16.sp,
            )
            Text(
                text = leader.role,
                color = RetroColors.TextSecondary,
                fontSize = 11.sp,
            )
        }

        // Stars
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(5) { index ->
                val isFilled = index < stars
                Text(
                    text = if (isFilled) "⭐" else "☆",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clickable {
                            val desired = index + 1
                            if (desired <= stars) {
                                // Tap filled star = reduce to that level (or zero)
                                onStarChanged(if (desired == 1 && stars == 1) 0 else desired)
                            } else {
                                // Adding stars: cap by remaining
                                val additional = desired - stars
                                if (additional <= remainingStars) {
                                    onStarChanged(desired)
                                } else {
                                    onStarChanged(stars + remainingStars)
                                }
                            }
                        }
                        .padding(2.dp),
                )
            }
        }
    }
}
