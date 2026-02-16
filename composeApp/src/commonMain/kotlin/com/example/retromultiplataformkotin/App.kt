package com.example.retromultiplataformkotin

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.retromultiplataformkotin.model.RetroPhase
import com.example.retromultiplataformkotin.theme.RetroTheme
import com.example.retromultiplataformkotin.ui.screens.LobbyScreen
import com.example.retromultiplataformkotin.ui.screens.ResultsScreen
import com.example.retromultiplataformkotin.ui.screens.RetroSessionScreen
import com.example.retromultiplataformkotin.viewmodel.RetroViewModel

@Composable
fun App() {
    RetroTheme {
        val viewModel = viewModel { RetroViewModel() }
        val session by viewModel.session.collectAsState()
        val onlineMembers by viewModel.onlineMembers.collectAsState()

        AnimatedContent(
            targetState = session.phase,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it / 3 } togetherWith
                    fadeOut() + slideOutHorizontally { -it / 3 }
            },
        ) { phase ->
            when (phase) {
                RetroPhase.WAITING -> {
                    LobbyScreen(
                        members = session.members,
                        onlineMembers = onlineMembers,
                        isLeader = viewModel.isLeader,
                        onStartRetro = { viewModel.startRetro() },
                    )
                }

                RetroPhase.RESULTS -> {
                    ResultsScreen(
                        session = session,
                        onNewRetro = { viewModel.resetSession() },
                        isLeader = viewModel.isLeader,
                    )
                }

                else -> {
                    RetroSessionScreen(
                        session = session,
                        currentUserId = viewModel.currentUserId,
                        isLeader = viewModel.isLeader,
                        onSubmitCard = { viewModel.submitCard(it) },
                        onApproveCard = { viewModel.approveCard(it) },
                        onVoteCard = { viewModel.voteCard(it) },
                        onNextPhase = { viewModel.nextPhase() },
                    )
                }
            }
        }
    }
}
