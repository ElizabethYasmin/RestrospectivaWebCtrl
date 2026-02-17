package com.example.retromultiplataformkotin.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.retromultiplataformkotin.model.SprintMood
import com.example.retromultiplataformkotin.model.TeamMember
import com.example.retromultiplataformkotin.model.RetroPhase
import com.example.retromultiplataformkotin.theme.RetroColors
import com.example.retromultiplataformkotin.ui.components.PhaseHeader
import com.example.retromultiplataformkotin.ui.components.RetroButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun MoodCheckScreen(
    members: List<TeamMember>,
    moods: Map<String, String>,
    isLeader: Boolean,
    onMoodSelected: (String) -> Unit,
    onContinue: () -> Unit,
    currentMemberId: String,
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
    val hasSubmitted = moods.containsKey(currentMemberId)
    var selectedMood by remember { mutableStateOf<SprintMood?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroColors.DarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        PhaseHeader(
            currentPhase = RetroPhase.MOOD_CHECK,
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

        Spacer(modifier = Modifier.height(20.dp))

        // Mood selector
        if (!hasSubmitted) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SprintMood.entries.forEach { mood ->
                    MoodOption(
                        mood = mood,
                        isSelected = selectedMood == mood,
                        onClick = {
                            selectedMood = mood
                            onMoodSelected(mood.emoji)
                        },
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RetroColors.GreenWentWell.copy(alpha = 0.15f))
                    .border(1.dp, RetroColors.GreenWentWell.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Tu mood: ${moods[currentMemberId]} ¡Registrado!",
                    color = RetroColors.GreenWentWell,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Team mood overview
        Text(
            text = "Mood del equipo (${moods.size}/${members.size})",
            fontWeight = FontWeight.Bold,
            color = RetroColors.TextPrimary,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(members, key = { it.id }) { member ->
                MemberMoodCard(
                    member = member,
                    mood = moods[member.id],
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Leader continue button
        if (isLeader) {
            RetroButton(
                text = "▶ Continuar al Lobby",
                onClick = onContinue,
                color = RetroColors.Gold,
                textColor = RetroColors.TextOnAccent,
                modifier = Modifier.fillMaxWidth(),
                enabled = moods.isNotEmpty(),
            )
        } else {
            Text(
                text = "Esperando a que el líder continúe...",
                color = RetroColors.TextSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MoodOption(
    mood: SprintMood,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        if (isSelected) RetroColors.Gold else RetroColors.DarkCard
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) RetroColors.Gold.copy(alpha = 0.15f) else RetroColors.DarkCard)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(text = mood.emoji, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood.label,
            color = if (isSelected) RetroColors.Gold else RetroColors.TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun MemberMoodCard(
    member: TeamMember,
    mood: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RetroColors.DarkCard)
            .padding(8.dp),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(48.dp)
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
            // Mood badge
            Text(
                text = mood ?: "❓",
                fontSize = 16.sp,
                modifier = Modifier.offset(x = 4.dp, y = (-4).dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = member.name.take(8),
            fontSize = 10.sp,
            color = RetroColors.TextSecondary,
        )
    }
}
