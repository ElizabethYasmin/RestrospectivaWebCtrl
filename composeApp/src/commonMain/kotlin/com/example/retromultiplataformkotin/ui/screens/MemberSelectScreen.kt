package com.example.retromultiplataformkotin.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retromultiplataformkotin.model.TeamMember
import com.example.retromultiplataformkotin.theme.RetroColors
import org.jetbrains.compose.resources.painterResource
import retromultiplataformkotin.composeapp.generated.resources.Res
import retromultiplataformkotin.composeapp.generated.resources.app_logo

@Composable
fun MemberSelectScreen(
    members: List<TeamMember>,
    isConnected: Boolean,
    takenMemberIds: Set<String> = emptySet(),
    onMemberSelected: (String) -> Unit,
    onClearStale: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroColors.DarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Logo
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = "Retro Quest Logo",
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "RETROSPECTIVA",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RetroColors.Cyan40,
        )

        // Connection status
        val statusText = if (isConnected) "Conectado" else "Conectando..."
        val statusColor = if (isConnected) RetroColors.GreenWentWell else RetroColors.OrangeImprove
        Text(text = statusText, color = statusColor, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Elige tu personaje",
                fontWeight = FontWeight.Bold,
                color = RetroColors.TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            if (takenMemberIds.isNotEmpty()) {
                Text(
                    text = "\uD83D\uDD04 Liberar todos",
                    color = RetroColors.OrangeImprove,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClearStale)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(members, key = { it.id }) { member ->
                val isTaken = member.id in takenMemberIds
                SelectableMemberCard(
                    member = member,
                    isTaken = isTaken,
                    onClick = { if (!isTaken) onMemberSelected(member.id) },
                )
            }
        }
    }
}

@Composable
private fun SelectableMemberCard(
    member: TeamMember,
    isTaken: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = if (isTaken) 0.4f else 1f
    val borderColor = when {
        isTaken -> RetroColors.TextSecondary.copy(alpha = 0.2f)
        else -> RetroColors.Cyan40.copy(alpha = 0.2f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(12.dp))
            .background(RetroColors.DarkCard)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isTaken, onClick = onClick)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(RetroColors.DarkSurface),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(member.avatarRes),
                contentDescription = member.name,
                modifier = Modifier.fillMaxSize().padding(4.dp),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = member.name,
            fontWeight = FontWeight.Bold,
            color = if (isTaken) RetroColors.TextSecondary else RetroColors.TextPrimary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = if (isTaken) "Ya elegido" else member.role,
            color = if (isTaken) RetroColors.OrangeImprove else RetroColors.TextSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (member.isLeader && !isTaken) {
            Text(text = "\uD83D\uDC51 L\u00edder", color = RetroColors.Gold, fontSize = 10.sp)
        }
    }
}
