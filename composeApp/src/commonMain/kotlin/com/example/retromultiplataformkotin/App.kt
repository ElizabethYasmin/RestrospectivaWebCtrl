package com.example.retromultiplataformkotin

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.retromultiplataformkotin.model.RetroPhase
import com.example.retromultiplataformkotin.theme.RetroTheme
import com.example.retromultiplataformkotin.ui.screens.LeaderVoteScreen
import com.example.retromultiplataformkotin.ui.screens.LobbyScreen
import com.example.retromultiplataformkotin.ui.screens.MemberSelectScreen
import com.example.retromultiplataformkotin.ui.screens.MoodCheckScreen
import com.example.retromultiplataformkotin.ui.screens.ResultsScreen
import com.example.retromultiplataformkotin.ui.screens.RetroSessionScreen
import com.example.retromultiplataformkotin.viewmodel.RetroViewModel

@Composable
fun App() {
    RetroTheme {
        val viewModel = viewModel { RetroViewModel() }
        val session by viewModel.session.collectAsState()
        val onlineMembers by viewModel.onlineMembers.collectAsState()
        val currentMemberId by viewModel.currentMemberId.collectAsState()
        val isConnected by viewModel.isConnected.collectAsState()
        val moods by viewModel.moods.collectAsState()
        val leaderVotes by viewModel.leaderVotes.collectAsState()
        val hasVotedLeaders by viewModel.hasVotedLeaders.collectAsState()

        var hasSelectedMember by remember { mutableStateOf(false) }

        AnimatedContent(
            targetState = if (!hasSelectedMember) "SELECT" else session.phase.name,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it / 3 } togetherWith
                    fadeOut() + slideOutHorizontally { -it / 3 }
            },
        ) { state ->
            when (state) {
                "SELECT" -> {
                    MemberSelectScreen(
                        members = session.members,
                        isConnected = isConnected,
                        onMemberSelected = { memberId ->
                            viewModel.selectMember(memberId)
                            hasSelectedMember = true
                        },
                    )
                }

                RetroPhase.MOOD_CHECK.name -> {
                    MoodCheckScreen(
                        members = session.members,
                        moods = moods,
                        isLeader = viewModel.isLeader,
                        onMoodSelected = { viewModel.submitMood(it) },
                        onContinue = { viewModel.nextPhase() },
                        currentMemberId = currentMemberId,
                    )
                }

                RetroPhase.WAITING.name -> {
                    LobbyScreen(
                        members = session.members,
                        onlineMembers = onlineMembers,
                        isLeader = viewModel.isLeader,
                        onStartRetro = { viewModel.startRetro() },
                    )
                }

                RetroPhase.RESULTS.name -> {
                    ResultsScreen(
                        session = session,
                        isLeader = viewModel.isLeader,
                        onNextPhase = { viewModel.nextPhase() },
                    )
                }

                RetroPhase.LEADER_VOTE.name -> {
                    LeaderVoteScreen(
                        members = session.members,
                        leaderVoteTotals = leaderVotes,
                        hasVoted = hasVotedLeaders,
                        isLeader = viewModel.isLeader,
                        onSubmitVotes = { viewModel.submitLeaderVotes(it) },
                        onShowPodium = { },
                        onNewRetro = { viewModel.resetSession() },
                    )
                }

                else -> {
                    RetroSessionScreen(
                        session = session,
                        currentUserId = currentMemberId,
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
