package com.example.retromultiplataformkotin.ui.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retromultiplataformkotin.model.TeamMember
import com.example.retromultiplataformkotin.theme.RetroColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun AvatarCard(
    member: TeamMember,
    isOnline: Boolean = true,
    showBounce: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (showBounce) -8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(90.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.offset(y = offsetY.dp),
        ) {
            // Glow border for leader
            val borderColor = when {
                member.isLeader -> RetroColors.Gold
                isOnline -> RetroColors.Cyan40
                else -> Color.Gray
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    .background(RetroColors.DarkCard),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(member.avatarRes),
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                )
            }

            // Leader crown badge
            if (member.isLeader) {
                Text(
                    text = "👑",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                )
            }

            // Online indicator
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RetroColors.GreenWentWell)
                        .border(1.dp, RetroColors.DarkBg, RoundedCornerShape(6.dp))
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = member.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = RetroColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = member.role,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = RetroColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
