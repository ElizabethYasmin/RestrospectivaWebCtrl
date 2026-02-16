package com.example.retromultiplataformkotin.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.retromultiplataformkotin.ui.components.*
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image

@Composable
fun RetroSessionScreen(
    session: RetroSession,
    currentUserId: String,
    isLeader: Boolean,
    onSubmitCard: (String) -> Unit,
    onApproveCard: (String) -> Unit,
    onVoteCard: (String) -> Unit,
    onNextPhase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }

    val phaseColor = when (session.phase) {
        RetroPhase.WAITING -> RetroColors.Cyan40
        RetroPhase.WENT_WELL -> RetroColors.GreenWentWell
        RetroPhase.TO_IMPROVE -> RetroColors.OrangeImprove
        RetroPhase.ACTION_ITEMS -> RetroColors.BlueAction
        RetroPhase.RESULTS -> RetroColors.PurpleResults
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroColors.DarkBg)
            .padding(16.dp),
    ) {
        // Phase header
        PhaseHeader(currentPhase = session.phase)

        Spacer(modifier = Modifier.height(12.dp))

        // Online avatars strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(session.members, key = { it.id }) { member ->
                MiniAvatar(member = member)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            val visibleCards = if (isLeader) {
                session.cards.filter { it.phase == session.phase }
            } else {
                session.cards.filter { it.phase == session.phase && it.isApproved }
            }

            items(visibleCards, key = { it.id }) { card ->
                RetroCardItem(
                    card = card,
                    author = session.members.find { it.id == card.authorId },
                    isLeader = isLeader,
                    currentPhase = session.phase,
                    phaseColor = phaseColor,
                    onApprove = { onApproveCard(card.id) },
                    onVote = { onVoteCard(card.id) },
                )
            }

            if (visibleCards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isLeader) "Aún no hay aportes. ¡Esperando al equipo!"
                            else "Esperando que aprueben los aportes...",
                            color = RetroColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input area (not shown in RESULTS phase)
        if (session.phase != RetroPhase.RESULTS && session.phase != RetroPhase.WAITING) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = when (session.phase) {
                                RetroPhase.WENT_WELL -> "Algo que salió bien..."
                                RetroPhase.TO_IMPROVE -> "Algo que podemos mejorar..."
                                RetroPhase.ACTION_ITEMS -> "Propón una acción..."
                                else -> ""
                            },
                            color = RetroColors.TextSecondary,
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RetroColors.TextPrimary,
                        unfocusedTextColor = RetroColors.TextPrimary,
                        focusedBorderColor = phaseColor,
                        unfocusedBorderColor = RetroColors.DarkCard,
                        cursorColor = phaseColor,
                        focusedContainerColor = RetroColors.DarkCard,
                        unfocusedContainerColor = RetroColors.DarkCard,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                )
                Spacer(modifier = Modifier.width(8.dp))
                RetroButton(
                    text = "📤",
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSubmitCard(inputText.trim())
                            inputText = ""
                        }
                    },
                    color = phaseColor,
                    enabled = inputText.isNotBlank(),
                )
            }
        }

        // Leader controls
        if (isLeader) {
            Spacer(modifier = Modifier.height(8.dp))

            val nextPhaseText = when (session.phase) {
                RetroPhase.WAITING -> "▶ Comenzar"
                RetroPhase.WENT_WELL -> "▶ Siguiente: ¿Qué mejorar?"
                RetroPhase.TO_IMPROVE -> "▶ Siguiente: Acciones"
                RetroPhase.ACTION_ITEMS -> "▶ Ver Resultados"
                RetroPhase.RESULTS -> ""
            }
            if (nextPhaseText.isNotEmpty()) {
                RetroButton(
                    text = nextPhaseText,
                    onClick = onNextPhase,
                    color = RetroColors.Gold,
                    textColor = RetroColors.TextOnAccent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MiniAvatar(
    member: TeamMember,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(RetroColors.DarkCard)
                .border(
                    1.dp,
                    if (member.isLeader) RetroColors.Gold else RetroColors.Cyan40.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(member.avatarRes),
                contentDescription = member.name,
                modifier = Modifier.fillMaxSize().padding(2.dp),
            )
        }
        Text(
            text = member.name.take(6),
            fontSize = 8.sp,
            color = RetroColors.TextSecondary,
        )
    }
}

@Composable
private fun RetroCardItem(
    card: RetroCard,
    author: TeamMember?,
    isLeader: Boolean,
    currentPhase: RetroPhase,
    phaseColor: Color,
    onApprove: () -> Unit,
    onVote: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RetroColors.DarkCard)
            .border(
                1.dp,
                if (card.isApproved) phaseColor.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
    ) {
        Column {
            // Author row
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (author != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(RetroColors.DarkSurface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(author.avatarRes),
                            contentDescription = author.name,
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = author.name,
                        color = RetroColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                if (!card.isApproved && isLeader) {
                    RetroChip(
                        text = "Pendiente",
                        color = Color.Gray,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Text(
                text = card.content,
                color = RetroColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Vote button (for ACTION_ITEMS phase)
                if (currentPhase == RetroPhase.ACTION_ITEMS && card.isApproved) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(phaseColor.copy(alpha = 0.1f))
                            .clickable(onClick = onVote)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⬆", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${card.votes}",
                                color = phaseColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Approve button (leader only)
                if (isLeader && !card.isApproved) {
                    RetroButton(
                        text = "✅ Aprobar",
                        onClick = onApprove,
                        color = RetroColors.GreenWentWell,
                    )
                }
            }
        }
    }
}
