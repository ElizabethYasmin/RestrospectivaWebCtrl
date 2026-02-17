package com.example.retromultiplataformkotin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retromultiplataformkotin.data.FirebaseCard
import com.example.retromultiplataformkotin.data.FirebaseRetroRepository
import com.example.retromultiplataformkotin.data.FirebaseTimer
import com.example.retromultiplataformkotin.data.currentTimeMillis
import kotlinx.coroutines.delay
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
    private val _currentMemberId = MutableStateFlow("")
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

    // Whether the user has selected a member in this session
    private val _hasSelectedMember = MutableStateFlow(false)
    val hasSelectedMember: StateFlow<Boolean> = _hasSelectedMember.asStateFlow()

    // Timer
    private val _timer = MutableStateFlow(FirebaseTimer())
    val timer: StateFlow<FirebaseTimer> = _timer.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    init {
        connectToFirebase()
    }

    private fun connectToFirebase() {
        viewModelScope.launch {
            try {
                // Sign in anonymously
                repo.signInAnonymously()
                _isConnected.value = true

                // Create session (idempotent)
                repo.createSession(sessionId)

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

                // Observe timer in real time
                launch {
                    repo.observeTimer(sessionId).collect { t ->
                        _timer.value = t
                    }
                }

                // Local tick loop to compute remaining seconds
                launch {
                    while (true) {
                        val t = _timer.value
                        _remainingSeconds.value = when {
                            t.durationSeconds == 0 || t.startedAt == 0L -> t.durationSeconds
                            t.isPaused && t.pausedAt > 0L -> (t.durationSeconds - ((t.pausedAt - t.startedAt) / 1000)).toInt().coerceAtLeast(0)
                            t.isPaused -> t.durationSeconds
                            else -> (t.durationSeconds - ((currentTimeMillis() - t.startedAt) / 1000)).toInt().coerceAtLeast(0)
                        }
                        delay(500L)
                    }
                }
            } catch (e: Exception) {
                _isConnected.value = false
            }
        }
    }

    fun selectMember(memberId: String) {
        _currentMemberId.value = memberId
        _hasSelectedMember.value = true
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
            // Reset timer when changing phase
            repo.setTimer(sessionId, FirebaseTimer())
            repo.updatePhase(sessionId, next.name)
        }
    }

    // --- Timer controls (leader only) ---

    fun setTimerDuration(seconds: Int) {
        viewModelScope.launch {
            repo.setTimer(sessionId, FirebaseTimer(durationSeconds = seconds, startedAt = 0L, isPaused = true, pausedAt = 0L))
        }
    }

    fun startTimer() {
        val current = _timer.value
        val now = currentTimeMillis()
        // If resuming from pause, shift startedAt forward so remaining time stays correct
        val elapsed = if (current.pausedAt > 0L) current.pausedAt - current.startedAt else 0L
        val newStartedAt = now - elapsed
        viewModelScope.launch {
            repo.setTimer(sessionId, current.copy(startedAt = newStartedAt, isPaused = false, pausedAt = 0L))
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            repo.setTimer(sessionId, _timer.value.copy(isPaused = true, pausedAt = currentTimeMillis()))
        }
    }

    fun resetTimer() {
        viewModelScope.launch {
            repo.setTimer(sessionId, FirebaseTimer())
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
        _hasSelectedMember.value = false
        _moods.value = emptyMap()
        _leaderVotes.value = emptyMap()
        _onlineMembers.value = emptySet()
        viewModelScope.launch {
            repo.deleteSession(sessionId)
            repo.createSession(sessionId)
        }
    }

    fun clearStalePresence() {
        viewModelScope.launch {
            repo.clearPresence(sessionId)
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
