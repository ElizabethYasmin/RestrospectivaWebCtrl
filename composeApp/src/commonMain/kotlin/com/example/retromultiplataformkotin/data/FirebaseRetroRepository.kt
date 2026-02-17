package com.example.retromultiplataformkotin.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.database.DatabaseReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseCard(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val phase: String = "",
    val votes: Int = 0,
    val isApproved: Boolean = false,
)

@Serializable
data class FirebaseSession(
    val id: String = "",
    val phase: String = "WAITING",
    val createdAt: Long = 0,
)

@Serializable
data class FirebaseMemberPresence(
    val memberId: String = "",
    val memberName: String = "",
    val online: Boolean = false,
    val lastSeen: Long = 0,
)

@Serializable
data class FirebaseMoodEntry(
    val memberId: String = "",
    val mood: String = "",
)

@Serializable
data class FirebaseLeaderVote(
    val voterId: String = "",
    val votes: Map<String, Int> = emptyMap(),
)

@Serializable
data class FirebaseTimer(
    val durationSeconds: Int = 0,
    val startedAt: Long = 0L,
    val isPaused: Boolean = true,
    val pausedAt: Long = 0L,
)

class FirebaseRetroRepository {

    private val auth = Firebase.auth
    private val db = Firebase.database

    private fun sessionRef(sessionId: String): DatabaseReference =
        db.reference("sessions").child(sessionId)

    private fun cardsRef(sessionId: String): DatabaseReference =
        db.reference("sessions").child(sessionId).child("cards")

    private fun presenceRef(sessionId: String): DatabaseReference =
        db.reference("sessions").child(sessionId).child("presence")

    private fun moodsRef(sessionId: String): DatabaseReference =
        db.reference("sessions").child(sessionId).child("moods")

    private fun leaderVotesRef(sessionId: String): DatabaseReference =
        db.reference("sessions").child(sessionId).child("leaderVotes")

    private fun timerRef(sessionId: String): DatabaseReference =
        db.reference("sessions").child(sessionId).child("timer")

    // --- Auth ---

    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously()
        return result.user?.uid ?: throw Exception("Auth failed")
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // --- Session ---

    suspend fun createSession(sessionId: String) {
        val session = FirebaseSession(
            id = sessionId,
            phase = "WAITING",
            createdAt = currentTimeMillis(),
        )
        sessionRef(sessionId).child("info").setValue(session)
    }

    suspend fun updatePhase(sessionId: String, phase: String) {
        sessionRef(sessionId).child("info").child("phase").setValue(phase)
    }

    fun observePhase(sessionId: String): Flow<String> {
        return sessionRef(sessionId).child("info").child("phase")
            .valueEvents
            .map { snapshot ->
                snapshot.value<String?>() ?: "WAITING"
            }
    }

    // --- Cards ---

    suspend fun submitCard(sessionId: String, card: FirebaseCard) {
        cardsRef(sessionId).child(card.id).setValue(card)
    }

    suspend fun approveCard(sessionId: String, cardId: String) {
        cardsRef(sessionId).child(cardId).child("isApproved").setValue(true)
    }

    suspend fun voteCard(sessionId: String, cardId: String, newVotes: Int) {
        cardsRef(sessionId).child(cardId).child("votes").setValue(newVotes)
    }

    fun observeCards(sessionId: String): Flow<List<FirebaseCard>> {
        return cardsRef(sessionId)
            .valueEvents
            .map { snapshot ->
                snapshot.children.mapNotNull { child ->
                    try {
                        child.value<FirebaseCard>()
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }

    // --- Moods ---

    suspend fun submitMood(sessionId: String, memberId: String, mood: String) {
        val entry = FirebaseMoodEntry(memberId = memberId, mood = mood)
        moodsRef(sessionId).child(memberId).setValue(entry)
    }

    fun observeMoods(sessionId: String): Flow<Map<String, String>> {
        return moodsRef(sessionId)
            .valueEvents
            .map { snapshot ->
                snapshot.children.mapNotNull { child ->
                    try {
                        val entry = child.value<FirebaseMoodEntry>()
                        entry.memberId to entry.mood
                    } catch (e: Exception) {
                        null
                    }
                }.toMap()
            }
    }

    // --- Leader Votes ---

    suspend fun submitLeaderVotes(sessionId: String, voterId: String, votes: Map<String, Int>) {
        val entry = FirebaseLeaderVote(voterId = voterId, votes = votes)
        leaderVotesRef(sessionId).child(voterId).setValue(entry)
    }

    fun observeLeaderVotes(sessionId: String): Flow<Map<String, Int>> {
        return leaderVotesRef(sessionId)
            .valueEvents
            .map { snapshot ->
                val totals = mutableMapOf<String, Int>()
                snapshot.children.forEach { child ->
                    try {
                        val entry = child.value<FirebaseLeaderVote>()
                        entry.votes.forEach { (leaderId, stars) ->
                            totals[leaderId] = (totals[leaderId] ?: 0) + stars
                        }
                    } catch (_: Exception) {}
                }
                totals
            }
    }

    // --- Presence ---

    suspend fun setPresence(sessionId: String, memberId: String, memberName: String, online: Boolean) {
        val ref = presenceRef(sessionId).child(memberId)
        if (online) {
            val presence = FirebaseMemberPresence(
                memberId = memberId,
                memberName = memberName,
                online = true,
                lastSeen = currentTimeMillis(),
            )
            ref.setValue(presence)
            // Auto-remove when client disconnects (app killed, network lost, etc.)
            ref.onDisconnect().removeValue()
        } else {
            ref.removeValue()
        }
    }

    fun observePresence(sessionId: String): Flow<Set<String>> {
        return presenceRef(sessionId)
            .valueEvents
            .map { snapshot ->
                snapshot.children
                    .mapNotNull { child ->
                        try {
                            val presence = child.value<FirebaseMemberPresence>()
                            if (presence.online) presence.memberId else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .toSet()
            }
    }

    // --- Timer ---

    suspend fun setTimer(sessionId: String, timer: FirebaseTimer) {
        timerRef(sessionId).setValue(timer)
    }

    fun observeTimer(sessionId: String): Flow<FirebaseTimer> {
        return timerRef(sessionId)
            .valueEvents
            .map { snapshot ->
                try {
                    snapshot.value<FirebaseTimer>()
                } catch (e: Exception) {
                    FirebaseTimer()
                }
            }
    }

    // --- Cleanup ---

    suspend fun clearPresence(sessionId: String) {
        presenceRef(sessionId).removeValue()
    }

    suspend fun deleteSession(sessionId: String) {
        sessionRef(sessionId).removeValue()
    }

}

internal expect fun currentTimeMillis(): Long
