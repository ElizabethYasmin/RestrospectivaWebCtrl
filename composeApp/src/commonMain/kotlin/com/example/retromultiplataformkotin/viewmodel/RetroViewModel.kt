package com.example.retromultiplataformkotin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.retromultiplataformkotin.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RetroViewModel : ViewModel() {

    // Current user — for now hardcoded as leader; later from Firebase auth
    val currentUserId = "sho"
    val isLeader: Boolean get() = TeamData.members.find { it.id == currentUserId }?.isLeader == true

    private val _session = MutableStateFlow(
        RetroSession(
            id = "session-1",
            phase = RetroPhase.WAITING,
            members = TeamData.members,
        )
    )
    val session: StateFlow<RetroSession> = _session.asStateFlow()

    // Simulated online members (for now all are "online")
    private val _onlineMembers = MutableStateFlow(
        TeamData.members.map { it.id }.toSet()
    )
    val onlineMembers: StateFlow<Set<String>> = _onlineMembers.asStateFlow()

    fun startRetro() {
        _session.update { it.copy(phase = RetroPhase.WENT_WELL) }
    }

    fun nextPhase() {
        _session.update { current ->
            val next = when (current.phase) {
                RetroPhase.WAITING -> RetroPhase.WENT_WELL
                RetroPhase.WENT_WELL -> RetroPhase.TO_IMPROVE
                RetroPhase.TO_IMPROVE -> RetroPhase.ACTION_ITEMS
                RetroPhase.ACTION_ITEMS -> RetroPhase.RESULTS
                RetroPhase.RESULTS -> RetroPhase.RESULTS
            }
            current.copy(phase = next)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun submitCard(content: String) {
        val card = RetroCard(
            id = Uuid.random().toString(),
            authorId = currentUserId,
            content = content,
            phase = _session.value.phase,
            isApproved = false, // needs leader approval
        )
        _session.update { it.copy(cards = it.cards + card) }
    }

    fun approveCard(cardId: String) {
        if (!isLeader) return
        _session.update { session ->
            session.copy(
                cards = session.cards.map { card ->
                    if (card.id == cardId) card.copy(isApproved = true) else card
                }
            )
        }
    }

    fun voteCard(cardId: String) {
        _session.update { session ->
            session.copy(
                cards = session.cards.map { card ->
                    if (card.id == cardId) card.copy(votes = card.votes + 1) else card
                }
            )
        }
    }

    fun resetSession() {
        _session.update {
            RetroSession(
                id = "session-new",
                phase = RetroPhase.WAITING,
                members = TeamData.members,
            )
        }
    }
}
