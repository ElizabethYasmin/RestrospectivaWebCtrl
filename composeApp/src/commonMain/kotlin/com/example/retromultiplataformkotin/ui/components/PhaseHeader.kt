package com.example.retromultiplataformkotin.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retromultiplataformkotin.model.RetroPhase
import com.example.retromultiplataformkotin.theme.RetroColors

@Composable
fun PhaseHeader(
    currentPhase: RetroPhase,
    modifier: Modifier = Modifier,
    remainingSeconds: Int = -1,
    totalSeconds: Int = 0,
    isTimerPaused: Boolean = true,
    isTimerStarted: Boolean = false,
    isLeader: Boolean = false,
    onTimerStart: () -> Unit = {},
    onTimerPause: () -> Unit = {},
    onTimerReset: () -> Unit = {},
    onSetDuration: (Int) -> Unit = {},
) {
    val phaseColor = when (currentPhase) {
        RetroPhase.MOOD_CHECK -> RetroColors.Gold
        RetroPhase.WAITING -> RetroColors.Cyan40
        RetroPhase.WENT_WELL -> RetroColors.GreenWentWell
        RetroPhase.TO_IMPROVE -> RetroColors.OrangeImprove
        RetroPhase.ACTION_ITEMS -> RetroColors.BlueAction
        RetroPhase.RESULTS -> RetroColors.PurpleResults
        RetroPhase.LEADER_VOTE -> RetroColors.Gold
    }

    val phaseIndex = RetroPhase.entries.indexOf(currentPhase)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RetroColors.DarkCard)
            .border(1.dp, phaseColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = currentPhase.emoji,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentPhase.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = phaseColor,
                )
                Text(
                    text = currentPhase.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = RetroColors.TextSecondary,
                )
            }
        }

        // Timer section
        if (totalSeconds > 0 || isLeader) {
            Spacer(modifier = Modifier.height(10.dp))
            TimerSection(
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds,
                isPaused = isTimerPaused,
                isStarted = isTimerStarted,
                isLeader = isLeader,
                phaseColor = phaseColor,
                onStart = onTimerStart,
                onPause = onTimerPause,
                onReset = onTimerReset,
                onSetDuration = onSetDuration,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RetroPhase.entries.forEachIndexed { index, phase ->
                val dotColor = if (index <= phaseIndex) phaseColor else RetroColors.DarkSurface
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(dotColor)
                        .border(1.dp, phaseColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                )
                if (index < RetroPhase.entries.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index < phaseIndex) phaseColor.copy(alpha = 0.5f)
                                else RetroColors.DarkSurface,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerSection(
    remainingSeconds: Int,
    totalSeconds: Int,
    isPaused: Boolean,
    isStarted: Boolean,
    isLeader: Boolean,
    phaseColor: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSetDuration: (Int) -> Unit,
) {
    val isExpired = totalSeconds > 0 && isStarted && remainingSeconds <= 0
    val isWarning = remainingSeconds in 1..30

    // Blink animation when expired
    val infiniteTransition = rememberInfiniteTransition()
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isExpired) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val timerColor by animateColorAsState(
        targetValue = when {
            isExpired -> Color(0xFFFF4444)
            isWarning -> RetroColors.OrangeImprove
            else -> phaseColor
        },
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RetroColors.DarkSurface.copy(alpha = 0.5f))
            .padding(12.dp),
    ) {
        // Timer display + controls
        if (totalSeconds > 0) {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Clock icon
                Text(
                    text = if (isExpired) "\u23f0" else "\u23f1\ufe0f",
                    fontSize = 20.sp,
                    modifier = Modifier.alpha(if (isExpired) blinkAlpha else 1f),
                )
                Spacer(Modifier.width(8.dp))

                // Countdown
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = timerColor,
                    modifier = Modifier.alpha(if (isExpired) blinkAlpha else 1f),
                )

                Spacer(Modifier.width(12.dp))

                // Progress bar
                if (totalSeconds > 0) {
                    val progress = remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = timerColor,
                        trackColor = RetroColors.DarkSurface,
                    )
                }

                // Leader controls
                if (isLeader) {
                    Spacer(Modifier.width(8.dp))
                    if (isPaused || !isStarted) {
                        TimerButton(text = "\u25b6\ufe0f", onClick = onStart)
                    } else {
                        TimerButton(text = "\u23f8\ufe0f", onClick = onPause)
                    }
                    Spacer(Modifier.width(4.dp))
                    TimerButton(text = "\u23f9\ufe0f", onClick = onReset)
                }
            }
        }

        // Duration picker chips (leader only, shown when no timer is set or timer is reset)
        if (isLeader && (totalSeconds == 0 || (!isStarted && isPaused))) {
            if (totalSeconds > 0) {
                Spacer(Modifier.height(8.dp))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "\u23f1\ufe0f",
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                listOf(1, 3, 5, 10).forEach { minutes ->
                    val isSelected = totalSeconds == minutes * 60
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) phaseColor.copy(alpha = 0.3f)
                                else RetroColors.DarkSurface,
                            )
                            .border(
                                1.dp,
                                if (isSelected) phaseColor else phaseColor.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp),
                            )
                            .clickable { onSetDuration(minutes * 60) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "${minutes}m",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) phaseColor else RetroColors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(RetroColors.DarkSurface)
            .clickable(onClick = onClick),
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}
