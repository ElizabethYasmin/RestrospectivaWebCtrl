package com.example.retromultiplataformkotin.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.retromultiplataformkotin.model.*
import com.example.retromultiplataformkotin.theme.RetroColors
import com.example.retromultiplataformkotin.ui.components.RetroButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun ResultsScreen(
    session: RetroSession,
    isLeader: Boolean,
    onNextPhase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wentWellCards = session.cards.filter { it.phase == RetroPhase.WENT_WELL && it.isApproved }
    val improveCards = session.cards.filter { it.phase == RetroPhase.TO_IMPROVE && it.isApproved }
    val actionCards = session.cards
        .filter { it.phase == RetroPhase.ACTION_ITEMS && it.isApproved }
        .sortedByDescending { it.votes }

    // MVP: member with most approved cards
    val mvpId = session.cards
        .filter { it.isApproved }
        .groupBy { it.authorId }
        .maxByOrNull { it.value.size }
        ?.key
    val mvpMember = session.members.find { it.id == mvpId }

    // Participation stats
    val participantIds = session.cards.map { it.authorId }.toSet()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(RetroColors.DarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Trophy header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🏆",
                fontSize = 64.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "RETRO COMPLETADA",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = RetroColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Stats summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatBox(
                    emoji = "🌟",
                    value = "${wentWellCards.size}",
                    label = "Logros",
                    color = RetroColors.GreenWentWell,
                )
                StatBox(
                    emoji = "🔧",
                    value = "${improveCards.size}",
                    label = "Mejoras",
                    color = RetroColors.OrangeImprove,
                )
                StatBox(
                    emoji = "🎯",
                    value = "${actionCards.size}",
                    label = "Acciones",
                    color = RetroColors.BlueAction,
                )
                StatBox(
                    emoji = "👥",
                    value = "${participantIds.size}",
                    label = "Participaron",
                    color = RetroColors.Cyan40,
                )
            }
        }

        // MVP
        if (mvpMember != null) {
            item {
                MvpCard(member = mvpMember)
            }
        }

        // Top action items
        if (actionCards.isNotEmpty()) {
            item {
                Text(
                    text = "🎯 Top Acciones Votadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroColors.BlueAction,
                )
            }
            itemsIndexed(actionCards.take(5)) { index, card ->
                ActionResultItem(
                    rank = index + 1,
                    card = card,
                    author = session.members.find { it.id == card.authorId },
                )
            }
        }

        // Highlights
        if (wentWellCards.isNotEmpty()) {
            item {
                Text(
                    text = "🌟 Lo Mejor del Sprint",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroColors.GreenWentWell,
                )
            }
            items(wentWellCards) { card ->
                SummaryCardItem(
                    card = card,
                    author = session.members.find { it.id == card.authorId },
                    accentColor = RetroColors.GreenWentWell,
                )
            }
        }

        // Leader vote button
        if (isLeader) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                RetroButton(
                    text = "⭐ Votar por Líderes",
                    onClick = onNextPhase,
                    color = RetroColors.Gold,
                    textColor = RetroColors.TextOnAccent,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatBox(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(RetroColors.DarkCard)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = RetroColors.TextSecondary,
        )
    }
}

@Composable
private fun MvpCard(
    member: TeamMember,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RetroColors.DarkCard)
            .border(2.dp, RetroColors.Gold.copy(alpha = glowAlpha), RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "⭐ MVP del Sprint ⭐", color = RetroColors.Gold, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RetroColors.DarkSurface)
                    .border(2.dp, RetroColors.Gold, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(member.avatarRes),
                    contentDescription = member.name,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RetroColors.TextPrimary,
            )
            Text(
                text = member.role,
                color = RetroColors.TextSecondary,
                fontSize = 12.sp,
            )
            Text(
                text = "¡Mayor participación en la retro!",
                color = RetroColors.Gold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ActionResultItem(
    rank: Int,
    card: RetroCard,
    author: TeamMember?,
    modifier: Modifier = Modifier,
) {
    val rankEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RetroColors.DarkCard)
            .border(1.dp, RetroColors.BlueAction.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(text = rankEmoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = card.content,
                color = RetroColors.TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            if (author != null) {
                Text(
                    text = "por ${author.name}",
                    color = RetroColors.TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "⬆", fontSize = 16.sp)
            Text(
                text = "${card.votes}",
                color = RetroColors.BlueAction,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun SummaryCardItem(
    card: RetroCard,
    author: TeamMember?,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RetroColors.DarkCard)
            .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = card.content,
                color = RetroColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (author != null) {
                Text(
                    text = "— ${author.name}",
                    color = RetroColors.TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}
