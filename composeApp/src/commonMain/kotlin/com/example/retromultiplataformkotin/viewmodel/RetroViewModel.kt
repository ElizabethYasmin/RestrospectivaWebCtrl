package com.example.retromultiplataformkotin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retromultiplataformkotin.data.FirebaseCard
import com.example.retromultiplataformkotin.data.FirebaseRetroRepository
import com.example.retromultiplataformkotin.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RetroViewModel : ViewModel() {

    private val repo = FirebaseRetroRepository()

    // The session ID that everyone joins
    private val sessionId = "retro-live"

    // Current member info — selected by user in lobby
    private val _currentMemberId = MutableStateFlow("sho")
    val currentMemberId: StateFlow<String> = _currentMemberId.asStateFlow()

    val isLeader: Boolean get() = TeamData.members.find { it.id == _currentMemberId.value }?.isLeader == true

    private val _session = MutableStateFlow(
        RetroSession(
            id = sessionId,
            phase = RetroPhase.WAITING,
            members = TeamData.members,
        )
    )
    val session: StateFlow<RetroSession> = _session.asStateFlow()

    private val _onlineMembers = MutableStateFlow<Set<String>>(emptySet())
    val onlineMembers: StateFlow<Set<String>> = _onlineMembers.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Moods: memberId → emoji
    private val _moods = MutableStateFlow<Map<String, String>>(emptyMap())
    val moods: StateFlow<Map<String, String>> = _moods.asStateFlow()

    // Leader votes: leaderId → total stars
    private val _leaderVotes = MutableStateFlow<Map<String, Int>>(emptyMap())
    val leaderVotes: StateFlow<Map<String, Int>> = _leaderVotes.asStateFlow()

    // Whether current user has already voted for leaders
    private val _hasVotedLeaders = MutableStateFlow(false)
    val hasVotedLeaders: StateFlow<Boolean> = _hasVotedLeaders.asStateFlow()

    init {
        connectToFirebase()
    }

    private fun connectToFirebase() {
        viewModelScope.launch {
            try {
                // Sign in anonymously
                repo.signInAnonymously()
                _isConnected.value = true

                // Create session if leader (idempotent)
                repo.createSession(sessionId)

                // Mark self as online
                val member = TeamData.members.find { it.id == _currentMemberId.value }
                if (member != null) {
                    repo.setPresence(sessionId, member.id, member.name, online = true)
                }

                // Observe phase changes in real time
                launch {
                    repo.observePhase(sessionId).collect { phaseName ->
                        val phase = try {
                            RetroPhase.valueOf(phaseName)
                        } catch (e: Exception) {
                            RetroPhase.WAITING
                        }
                        _session.update { it.copy(phase = phase) }
                    }
                }

                // Observe cards in real time
                launch {
                    repo.observeCards(sessionId).collect { firebaseCards ->
                        val cards = firebaseCards.map { fc ->
                            RetroCard(
                                id = fc.id,
                                authorId = fc.authorId,
                                content = fc.content,
                                phase = try {
                                    RetroPhase.valueOf(fc.phase)
                                } catch (e: Exception) {
                                    RetroPhase.WENT_WELL
                                },
                                votes = fc.votes,
                                isApproved = fc.isApproved,
                            )
                        }
                        _session.update { it.copy(cards = cards) }
                    }
                }

                // Observe presence in real time
                launch {
                    repo.observePresence(sessionId).collect { onlineIds ->
                        _onlineMembers.value = onlineIds
                    }
                }

                // Observe moods in real time
                launch {
                    repo.observeMoods(sessionId).collect { moodMap ->
                        _moods.value = moodMap
                    }
                }

                // Observe leader votes in real time
                launch {
                    repo.observeLeaderVotes(sessionId).collect { voteTotals ->
                        _leaderVotes.value = voteTotals
                    }
                }
            } catch (e: Exception) {
                _isConnected.value = false
            }
        }
    }

    fun selectMember(memberId: String) {
        _currentMemberId.value = memberId
        viewModelScope.launch {
            val member = TeamData.members.find { it.id == memberId }
            if (member != null) {
                repo.setPresence(sessionId, member.id, member.name, online = true)
            }
        }
    }

    fun submitMood(mood: String) {
        viewModelScope.launch {
            repo.submitMood(sessionId, _currentMemberId.value, mood)
        }
    }

    fun startRetro() {
        _hasVotedLeaders.value = false
        _moods.value = emptyMap()
        _leaderVotes.value = emptyMap()
        viewModelScope.launch {
            // Clean previous session data and start fresh
            repo.deleteSession(sessionId)
            repo.createSession(sessionId)
            val member = TeamData.members.find { it.id == _currentMemberId.value }
            if (member != null) {
                repo.setPresence(sessionId, member.id, member.name, online = true)
            }
            repo.updatePhase(sessionId, RetroPhase.MOOD_CHECK.name)
        }
    }

    fun nextPhase() {
        val next = when (_session.value.phase) {
            RetroPhase.WAITING -> RetroPhase.MOOD_CHECK
            RetroPhase.MOOD_CHECK -> RetroPhase.WENT_WELL
            RetroPhase.WENT_WELL -> RetroPhase.TO_IMPROVE
            RetroPhase.TO_IMPROVE -> RetroPhase.ACTION_ITEMS
            RetroPhase.ACTION_ITEMS -> RetroPhase.RESULTS
            RetroPhase.RESULTS -> RetroPhase.LEADER_VOTE
            RetroPhase.LEADER_VOTE -> RetroPhase.LEADER_VOTE
        }
        viewModelScope.launch {
            repo.updatePhase(sessionId, next.name)
        }
    }

    fun submitLeaderVotes(votes: Map<String, Int>) {
        _hasVotedLeaders.value = true
        viewModelScope.launch {
            repo.submitLeaderVotes(sessionId, _currentMemberId.value, votes)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun submitCard(content: String) {
        val cardId = Uuid.random().toString()
        val member = TeamData.members.find { it.id == _currentMemberId.value }
        val firebaseCard = FirebaseCard(
            id = cardId,
            authorId = _currentMemberId.value,
            authorName = member?.name ?: "Anónimo",
            content = content,
            phase = _session.value.phase.name,
            votes = 0,
            isApproved = false,
        )
        viewModelScope.launch {
            repo.submitCard(sessionId, firebaseCard)
        }
    }

    fun approveCard(cardId: String) {
        if (!isLeader) return
        viewModelScope.launch {
            repo.approveCard(sessionId, cardId)
        }
    }

    fun voteCard(cardId: String) {
        val card = _session.value.cards.find { it.id == cardId } ?: return
        viewModelScope.launch {
            repo.voteCard(sessionId, cardId, card.votes + 1)
        }
    }

    fun resetSession() {
        _hasVotedLeaders.value = false
        _moods.value = emptyMap()
        _leaderVotes.value = emptyMap()
        viewModelScope.launch {
            repo.deleteSession(sessionId)
            repo.createSession(sessionId)
            val member = TeamData.members.find { it.id == _currentMemberId.value }
            if (member != null) {
                repo.setPresence(sessionId, member.id, member.name, online = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Mark offline when leaving
        viewModelScope.launch {
            val member = TeamData.members.find { it.id == _currentMemberId.value }
            if (member != null) {
                repo.setPresence(sessionId, member.id, member.name, online = false)
            }
        }
    }
}
