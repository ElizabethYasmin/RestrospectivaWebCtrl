package com.example.retromultiplataformkotin.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retromultiplataformkotin.model.TeamData
import com.example.retromultiplataformkotin.model.TeamMember
import com.example.retromultiplataformkotin.theme.RetroColors
import com.example.retromultiplataformkotin.ui.components.AvatarCard
import com.example.retromultiplataformkotin.ui.components.RetroButton

@Composable
fun LobbyScreen(
    members: List<TeamMember>,
    onlineMembers: Set<String>,
    isLeader: Boolean,
    onStartRetro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
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
        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "🎮 RETRO QUEST",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = RetroColors.Cyan40,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Sprint Retrospective",
            style = MaterialTheme.typography.titleMedium,
            color = RetroColors.Gold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Waiting message
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RetroColors.DarkCard)
                .border(1.dp, RetroColors.Cyan40.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🟢",
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${onlineMembers.size}/${members.size} jugadores conectados",
                    color = RetroColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = " ...",
                    color = RetroColors.Cyan40.copy(alpha = dotAlpha),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Team grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(members, key = { it.id }) { member ->
                AvatarCard(
                    member = member,
                    isOnline = member.id in onlineMembers,
                    showBounce = member.id in onlineMembers,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start button (only for leader)
        if (isLeader) {
            RetroButton(
                text = "🚀 INICIAR RETROSPECTIVA",
                onClick = onStartRetro,
                color = RetroColors.Gold,
                textColor = RetroColors.TextOnAccent,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = "Esperando a que el líder inicie la sesión...",
                color = RetroColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
