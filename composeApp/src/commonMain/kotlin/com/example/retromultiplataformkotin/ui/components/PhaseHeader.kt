package com.example.retromultiplataformkotin.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
) {
    val phaseColor = when (currentPhase) {
        RetroPhase.WAITING -> RetroColors.Cyan40
        RetroPhase.WENT_WELL -> RetroColors.GreenWentWell
        RetroPhase.TO_IMPROVE -> RetroColors.OrangeImprove
        RetroPhase.ACTION_ITEMS -> RetroColors.BlueAction
        RetroPhase.RESULTS -> RetroColors.PurpleResults
    }

    val phaseIndex = RetroPhase.entries.indexOf(currentPhase)
    val progress = (phaseIndex + 1).toFloat() / RetroPhase.entries.size

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
